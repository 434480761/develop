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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by liuran on 2016/7/6.
 */
@Repository
public class TitanSyncServiceImpl implements TitanSyncService{
    private final static Logger LOG = LoggerFactory.getLogger(TitanSyncServiceImpl.class);
    @Autowired
    private TitanTechInfoRepository titanTechInfoRepository;

    @Autowired
    private TitanResourceRepository<Education> titanResourceRepository;

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

        delete(primaryCategory, identifier);

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

    private boolean delete(String primaryCategory, String identifier){
        LOG.info("titan sync : delete resource start primaryCategory：{}  identifier:{}",primaryCategory,identifier);
        boolean techInfoDeleted = titanTechInfoRepository.deleteAllByResource(primaryCategory, identifier);
        boolean resourceDeleted = titanResourceRepository.delete(primaryCategory, identifier);
        LOG.info("titan sync : delete {} success",identifier);
        return techInfoDeleted && resourceDeleted;
    }

    /**
     *
     * */
    private boolean report(Education education){
        if(education == null){
            return true;
        }
        LOG.info("titan sync : report resource start primaryCategory：{}  identifier:{}",
                education.getPrimaryCategory(),education.getIdentifier());
        String primaryCategory = education.getPrimaryCategory();

        Set<String> uuids = new HashSet<>();
        uuids.add(education.getIdentifier());

        List<String> resourceTypes = new ArrayList<>();
        resourceTypes.add(primaryCategory);

        List<ResCoverage> resCoverageList = coverageDao.queryCoverageByResource(primaryCategory, uuids);
        List<ResourceCategory> resourceCategoryList = ndResourceDao.queryCategoriesUseHql(resourceTypes, uuids);
        List<TechInfo> techInfos = ndResourceDao.queryTechInfosUseHql(resourceTypes,uuids);

        List<ResourceRelation> resourceRelations =
                educationRelationdao.batchGetRelationByResourceSourceOrTarget(primaryCategory, uuids);

        boolean educationSuccess = titanImportRepository
                .importOneData(education,resCoverageList,resourceCategoryList,techInfos);
        if(!educationSuccess){
            return false;
        }
        List<ResourceRelation> resultResourceRelations = titanRelationRepository.batchAdd(resourceRelations);
        if(resourceRelations.size() != resultResourceRelations.size()){
            return  false;
        }

        LOG.info("titan sync : report {} success",education.getIdentifier());
        return true;
    }
}
