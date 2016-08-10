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
import nd.esp.service.lifecycle.repository.model.ResCoverage;
import nd.esp.service.lifecycle.repository.model.ResourceCategory;
import nd.esp.service.lifecycle.repository.model.ResourceRelation;
import nd.esp.service.lifecycle.repository.model.TechInfo;
import nd.esp.service.lifecycle.repository.sdk.impl.ServicesManager;
import nd.esp.service.lifecycle.support.busi.elasticsearch.ResourceTypeSupport;
import nd.esp.service.lifecycle.support.busi.titan.TitanSyncType;
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


    @Override
    public boolean deleteResource(String primaryCategory, String identifier) {
        boolean deleteSuccess = delete(primaryCategory, identifier);
        if(deleteSuccess){
            titanRepositoryUtils.titanSync4MysqlDeleteAll(primaryCategory, identifier);
        }

        return false;
    }

    @Override
    public boolean reportResource(String primaryCategory, String identifier) {
        if(ResourceNdCode.fromString(primaryCategory)==null){
            return true;
        }

        boolean deleteSuccess = delete(primaryCategory, identifier);
        if(!deleteSuccess){
            return false;
        }
        EspRepository<?> espRepository = ServicesManager.get(primaryCategory);
        Education education;
        try {
            education = (Education) espRepository.get(identifier);
        } catch (EspStoreException e) {
            e.printStackTrace();
            return false;
        }


        if(education == null){
            titanRepositoryUtils.titanSync4MysqlAdd(TitanSyncType.DROP_RESOURCE_ERROR,primaryCategory,identifier);
        } else {
            boolean reportSuccess = report(education);
            if(reportSuccess){
                titanRepositoryUtils.titanSync4MysqlDelete(TitanSyncType.SAVE_OR_UPDATE_ERROR,primaryCategory,identifier);
            } else {
                titanRepositoryUtils.titanSync4MysqlImportAdd(TitanSyncType.SAVE_OR_UPDATE_ERROR,primaryCategory,identifier);
            }
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


        Map<String,ResCoverage> coverageMap = new HashMap<>();
        if(CollectionUtils.isNotEmpty(resCoverageList)){
            for(ResCoverage coverage : resCoverageList){
                String key = coverage.getTarget()+coverage.getStrategy()+coverage.getTargetType();
                if(coverageMap.get(key)==null){
                    coverageMap.put(key, coverage);
                }
            }
        }

        Map<String, ResourceCategory> categoryMap = new HashMap<>();
        if(CollectionUtils.isNotEmpty(resourceCategoryList)){
            for (ResourceCategory resourceCategory : resourceCategoryList){
                if(categoryMap.get(resourceCategory.getTaxoncode())==null){
                    categoryMap.put(resourceCategory.getTaxoncode(), resourceCategory);
                }

            }
        }

        Map<String, TechInfo> techInfoMap = new HashMap<>();
        if(CollectionUtils.isNotEmpty(techInfos)){
            for (TechInfo techInfo : techInfos){
                if(techInfoMap.get(techInfo.getTitle()) == null){
                    techInfoMap.put(techInfo.getTitle(), techInfo);
                }
            }
        }

        List<ResCoverage> coverageList = new ArrayList<>();
        coverageList.addAll(coverageMap.values());
        List<ResourceCategory> categoryList = new ArrayList<>();
        categoryList.addAll(categoryMap.values());
        List<TechInfo> techInfoList = new ArrayList<>();
        techInfoList.addAll(techInfoMap.values());

        Education resultEducation = titanResourceRepository.add(education);
        if(resultEducation == null){
            return false;
        }


        List<ResCoverage> resultCoverage = titanCoverageRepository.batchAdd(coverageList);
        if(resCoverageList.size() != resultCoverage.size()){
            return false;
        }

        List<ResourceCategory> resultCategory = titanCategoryRepository.batchAdd(categoryList);
        if(resourceCategoryList.size()!=resultCategory.size()){
            return false;
        }

        List<ResourceRelation> resourceRelations =
                educationRelationdao.batchGetRelationByResourceSourceOrTarget(primaryCategory, uuids);
        List<ResourceRelation> resultResourceRelations = titanRelationRepository.batchAdd(resourceRelations);
        if(resourceRelations.size() != resultResourceRelations.size()){
            return false;
        }


        List<TechInfo> resultTechInfos = titanTechInfoRepository.batchAdd(techInfoList);
        if(techInfos.size()!=resultTechInfos.size()){
            return false;
        }

        LOG.info("titan_sync : report {} success",education.getIdentifier());
        return true;
    }
}
