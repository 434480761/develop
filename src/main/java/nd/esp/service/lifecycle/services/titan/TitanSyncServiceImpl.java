package nd.esp.service.lifecycle.services.titan;

import nd.esp.service.lifecycle.daos.coverage.v06.CoverageDao;
import nd.esp.service.lifecycle.daos.educationrelation.v06.EducationRelationDao;
import nd.esp.service.lifecycle.daos.titan.TitanResourceRepositoryImpl;
import nd.esp.service.lifecycle.daos.titan.inter.*;
import nd.esp.service.lifecycle.educommon.dao.NDResourceDao;
import nd.esp.service.lifecycle.entity.elasticsearch.Resource;
import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.EspRepository;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.*;
import nd.esp.service.lifecycle.repository.sdk.impl.ServicesManager;
import nd.esp.service.lifecycle.support.busi.elasticsearch.ResourceTypeSupport;
import nd.esp.service.lifecycle.support.busi.titan.TitanResourceUtils;
import nd.esp.service.lifecycle.support.busi.titan.TitanSyncType;
import nd.esp.service.lifecycle.support.enums.LifecycleStatus;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * Created by liuran on 2016/7/6.
 */
@Repository
public class TitanSyncServiceImpl implements TitanSyncService{
    private final static Logger LOG = LoggerFactory.getLogger(TitanSyncServiceImpl.class);
    @Autowired
    private TitanTechInfoRepository titanTechInfoRepository;

    @Autowired
    private TitanRelationRepository titanRelationRepository;

    @Autowired
    private CoverageDao coverageDao;

    @Autowired
    private NDResourceDao ndResourceDao;

    @Autowired
    private EducationRelationDao educationRelationdao;

    @Autowired
    private TitanRepositoryUtils titanRepositoryUtils;

    @Autowired
    private TitanImportRepository titanImportRepository;

    @Autowired
    private TitanResourceRepository<Education> titanResourceRepository;

    @Autowired
    private TitanCoverageRepository titanCoverageRepository;

    @Autowired
    private TitanCategoryRepository titanCategoryRepository;

    @Autowired
    private TitanStatisticalRepository titanStatisticalRepository;

    @Autowired
    private TitanCommonRepository titanCommonRepository;


    @Override
    public boolean deleteResource(String primaryCategory, String identifier) {
        boolean deleteSuccess = delete(primaryCategory, identifier);
        if(deleteSuccess){
            titanRepositoryUtils.titanSync4MysqlDeleteAll(primaryCategory, identifier);
        }

        return false;
    }

    @Override
    public boolean reportResource(String primaryCategory, String identifier ,TitanSyncType titanSyncType) {
        if(ResourceNdCode.fromString(primaryCategory)==null){
            titanRepositoryUtils.titanSync4MysqlImportAdd(titanSyncType,primaryCategory,identifier);
            return true;
        }

        EspRepository<?> espRepository = ServicesManager.get(primaryCategory);
        Education education;
        try {
            education = (Education) espRepository.get(identifier);
        } catch (EspStoreException e) {
            titanRepositoryUtils.titanSync4MysqlImportAdd(titanSyncType,primaryCategory,identifier);
            return false;
        }

        if(education == null){
            //资源不存在触发删除资源
            deleteResource(primaryCategory, identifier);
            return true;
        }

        boolean reportSuccess = false;
        try {
            if (titanSyncType.equals(TitanSyncType.SAVE_OR_UPDATE_ERROR)){
                boolean deleteSuccess = delete(primaryCategory, identifier);
                if(deleteSuccess){
                    reportSuccess = report(education);
                }
            } else if (titanSyncType.equals(TitanSyncType.VERSION_SYNC)){
                if(deleteCoverageTechInfoCategory(primaryCategory, identifier)) {
                    reportSuccess = report4Import(education);
                }
            }
        } catch (Exception e){
            LOG.error(e.getLocalizedMessage());
        }

        if(reportSuccess){
            if (TitanSyncType.VERSION_SYNC.equals(titanSyncType)){
                titanRepositoryUtils.titanSyncUpdateErrorType(titanSyncType,primaryCategory,identifier, TitanSyncType.RELATION);
            } else {
                titanRepositoryUtils.titanSync4MysqlDeleteAll(primaryCategory,identifier);
            }
        } else {
            titanRepositoryUtils.titanSync4MysqlImportAdd(titanSyncType,primaryCategory,identifier);
        }

        return false;
    }

    @Override
    public boolean batchDeleteResource(Set<Resource> resourceSet) {
        if(CollectionUtils.isEmpty(resourceSet)){
            return true;
        }
        for (Resource resource : resourceSet){
            deleteResource(resource.getResourceType(), resource.getIdentifier());
        }
        return true;
    }

    @Override
    public boolean syncEducation(String primaryCategory, String identifier) {
        EspRepository<?> espRepository = ServicesManager.get(primaryCategory);
        Education education;
        try {
            education = (Education) espRepository.get(identifier);
        } catch (EspStoreException e) {
            titanRepositoryUtils.titanSync4MysqlAdd(TitanSyncType.SAVE_OR_UPDATE_ERROR,
                    primaryCategory, identifier);
            return false;
        }
        try{
            titanResourceRepository.update(education);
        } catch (Exception e){
            LOG.info("titan_repository error");
        }


        return false;
    }

    private boolean delete(String primaryCategory, String identifier){
        boolean techInfoDeleted = titanTechInfoRepository.deleteAllByResource(primaryCategory, identifier);
        boolean resourceDeleted = titanResourceRepository.delete(primaryCategory, identifier);
        if(techInfoDeleted && resourceDeleted){
            LOG.info("titan_sync : delete {} success",identifier);
        }
        return techInfoDeleted && resourceDeleted;
    }

    private boolean deleteCoverageTechInfoCategory(String primaryCategory, String identifier){
        boolean techInfoDeleted = titanTechInfoRepository.deleteAllByResource(primaryCategory, identifier);
        if (!techInfoDeleted){
            return false;
        }
        String script = "g.V().has(primaryCategory,'identifier',identifier).outE('has_coverage','has_category_code','has_categories_path').drop()";
        Map<String, Object> param = new HashMap<>();
        param.put("primaryCategory",primaryCategory);
        param.put("identifier",identifier);
        try {
            titanCommonRepository.executeScript(script, param);
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    private boolean report4Import(Education education){
        if(education == null){
            return true;
        }

        if(StringUtils.isEmpty(education.getPrimaryCategory())){
            return false;
        }
        LOG.info("titan_sync : report resource start primaryCategory：{}  identifier:{}",
                education.getPrimaryCategory(),education.getIdentifier());
        String primaryCategory = education.getPrimaryCategory();

        Set<String> uuids = new HashSet<>();
        uuids.add(education.getIdentifier());


        List<String> resourceTypes = new ArrayList<>();
        resourceTypes.add(primaryCategory);

        List<ResCoverage> resCoverageList = coverageDao.queryCoverageByResource(primaryCategory, uuids);
        List<ResourceCategory> resourceCategoryList = ndResourceDao.queryCategoriesUseHql(resourceTypes, uuids);
        List<TechInfo> techInfos = ndResourceDao.queryTechInfosUseHql(resourceTypes,uuids);
        List<ResourceStatistical> statisticalList = ndResourceDao.queryStatisticalUseHql(resourceTypes,uuids);

        List<ResCoverage> coverageList = TitanResourceUtils.distinctCoverage(resCoverageList);
        List<TechInfo> techInfoList = TitanResourceUtils.distinctTechInfo(techInfos);

        Education resultEducation = titanResourceRepository.add(education);
        if(resultEducation == null){
            return false;
        }

        List<ResCoverage> resultCoverage = titanCoverageRepository.batchAdd(coverageList);
        if(coverageList.size() != resultCoverage.size()){
            return false;
        }

        List<ResourceCategory> resultCategory = titanCategoryRepository.batchAdd(resourceCategoryList);
        if(resourceCategoryList.size()!=resultCategory.size()){
            return false;
        }

        List<TechInfo> resultTechInfos = titanTechInfoRepository.batchAdd(techInfoList);
        if(techInfoList.size()!=resultTechInfos.size()){
            return false;
        }


        List<ResourceStatistical> resultStatisticals = titanStatisticalRepository.batchAdd(statisticalList);
        if (statisticalList.size() != resultStatisticals.size()){
            return false;
        }
        LOG.info("titan_sync : report {} success",education.getIdentifier());
        return true;
    }

    /**
     *
     * */
    private boolean report(Education education){
        if(education == null){
            return true;
        }

        if(StringUtils.isEmpty(education.getPrimaryCategory())){
            return false;
        }
        LOG.info("titan_sync : report resource start primaryCategory：{}  identifier:{}",
                education.getPrimaryCategory(),education.getIdentifier());
        String primaryCategory = education.getPrimaryCategory();

        Set<String> uuids = new HashSet<>();
        uuids.add(education.getIdentifier());


        List<String> resourceTypes = new ArrayList<>();
        resourceTypes.add(primaryCategory);

        List<ResCoverage> resCoverageList = coverageDao.queryCoverageByResource(primaryCategory, uuids);
        List<ResourceCategory> resourceCategoryList = ndResourceDao.queryCategoriesUseHql(resourceTypes, uuids);
        List<TechInfo> techInfos = ndResourceDao.queryTechInfosUseHql(resourceTypes,uuids);
        List<ResourceStatistical> statisticalList = ndResourceDao.queryStatisticalUseHql(resourceTypes,uuids);

//        List<ResCoverage> coverageList = TitanResourceUtils.distinctCoverage(resCoverageList);
//        List<TechInfo> techInfoList = TitanResourceUtils.distinctTechInfo(techInfos);

//        Education resultEducation = titanResourceRepository.add(education);
//        if(resultEducation == null){
//            return false;
//        }
//
//        List<ResCoverage> resultCoverage = titanCoverageRepository.batchAdd(coverageList);
//        if(coverageList.size() != resultCoverage.size()){
//            return false;
//        }
//
//        List<ResourceCategory> resultCategory = titanCategoryRepository.batchAdd(resourceCategoryList);
//        if(resourceCategoryList.size()!=resultCategory.size()){
//            return false;
//        }
//
//        List<TechInfo> resultTechInfos = titanTechInfoRepository.batchAdd(techInfoList);
//        if(techInfoList.size()!=resultTechInfos.size()){
//            return false;
//        }



        boolean success = titanImportRepository.importOneData(education,resCoverageList,resourceCategoryList,techInfos);
        if (!success){
            return false;
        }

        List<ResourceStatistical> resultStatisticals = titanStatisticalRepository.batchAdd(statisticalList);
        if (statisticalList.size() != resultStatisticals.size()){
            return false;
        }

        List<ResourceRelation> resourceRelations =
                educationRelationdao.batchGetRelationByResourceSourceOrTarget(primaryCategory, uuids);
        List<ResourceRelation> resultResourceRelations = titanRelationRepository.batchAdd(resourceRelations);
        if(resourceRelations.size() != resultResourceRelations.size()){
            return false;
        }
        LOG.info("titan_sync : report {} success",education.getIdentifier());
        return true;
    }

    private boolean checkExist(String primaryCategory, String identifier){
        Long id = null;
        try {
            id = titanCommonRepository.getVertexIdByLabelAndId(primaryCategory, identifier);
        } catch (Exception e) {
            return false;
        }
        if (id == null){
            return false;
        }

        return true;
    }
}
