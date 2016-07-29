package nd.esp.service.lifecycle.services.titan;

import nd.esp.service.lifecycle.daos.coverage.v06.CoverageDao;
import nd.esp.service.lifecycle.daos.educationrelation.v06.EducationRelationDao;
import nd.esp.service.lifecycle.daos.titan.inter.*;
import nd.esp.service.lifecycle.educommon.dao.NDResourceDao;
import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.EspRepository;
import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.ds.ComparsionOperator;
import nd.esp.service.lifecycle.repository.ds.Item;
import nd.esp.service.lifecycle.repository.ds.LogicalOperator;
import nd.esp.service.lifecycle.repository.ds.ValueUtils;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.*;
import nd.esp.service.lifecycle.repository.sdk.KnowledgeRelationRepository;
import nd.esp.service.lifecycle.repository.sdk.ResourceRelation4QuestionDBRepository;
import nd.esp.service.lifecycle.repository.sdk.ResourceRelationRepository;
import nd.esp.service.lifecycle.repository.sdk.TitanSyncRepository;
import nd.esp.service.lifecycle.repository.sdk.impl.ServicesManager;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;

import java.util.*;

/**
 * Created by liuran on 2016/7/28.
 */
public class TitanResourceServiceNewImpl implements TitanResourceServiceNew{
    private static String s_primaryCategory ;
    private static Integer s_page;
    private static Integer s_totalPage;

    @Autowired
    private CoverageDao coverageDao;

    @Autowired
    private NDResourceDao ndResourceDao;

    @Autowired
    private EducationRelationDao educationRelationdao;

    @Autowired
    private ResourceRelation4QuestionDBRepository resourceRelation4QuestionDBRepository;

    @Autowired
    private ResourceRelationRepository resourceRelationRepository;

    @Autowired
    private KnowledgeRelationRepository knowledgeRelationRepository;

    @Autowired
    private TitanImportRepository titanImportRepository;

    @Autowired
    private TitanRelationRepository titanRelationRepository;

    @Autowired
    private TitanChapterRelationRepository titanChapterRelationRepository;

    @Autowired
    private TitanKnowledgeRelationRepository titanKnowledgeRelationRepository;


    private static final Logger LOG = LoggerFactory
            .getLogger(TitanResourceServiceNewImpl.class);


    @Override
    public void importOneData4Script(String primaryCategory, String identifier) {
        EspRepository<?> espRepository = ServicesManager.get(primaryCategory);
        Education education = null;
        try {
            education = (Education) espRepository.get(identifier);
        } catch (EspStoreException e) {
            e.printStackTrace();
        }

        if (education == null){
            return;
        }
        Set<String> uuids = new HashSet<>();
        uuids.add(education.getIdentifier());

        List<ResCoverage> resCoverageList = coverageDao.queryCoverageByResource(primaryCategory, uuids);

        List<String> resourceTypes = new ArrayList<String>();
        resourceTypes.add(primaryCategory);
        List<ResourceCategory> resourceCategoryList = ndResourceDao.queryCategoriesUseHql(resourceTypes, uuids);


        List<String> primaryCategorys = new ArrayList<>();
        primaryCategorys.add(primaryCategory);
        List<TechInfo> techInfos = ndResourceDao.queryTechInfosUseHql(primaryCategorys,uuids);
        titanImportRepository.importOneData(education, resCoverageList,resourceCategoryList,techInfos);
    }

    @Override
    public void importData4Script(String primaryCategory) {
        AbstractPageQuery abstractPageQuery = new ImportData4ScriptPageQuery();
        abstractPageQuery.doing(primaryCategory);
    }

    @Override
    public String importStatus() {
        return "primaryCategory:" +s_primaryCategory +"  totalPage:" + s_totalPage +"  page"+ s_page;
    }

    @Override
    public void createChapterRelation() {
        AbstractPageQuery abstractPageQuery = new CreateRelation4Chapter();
        abstractPageQuery.doing("chapters");
    }

    @Override
    public void importAllRelation() {
        long size = 0L;
        pageQueryRelation(resourceRelationRepository);
        pageQueryRelation(resourceRelation4QuestionDBRepository);
    }

    @Override
    public void createKnowledgeRelation() {
        AbstractPageQuery abstractPageQuery = new CreateRelation4Knowledge();
        abstractPageQuery.doing("knowledges");
    }

    @Override
    public void importKnowledgeRelation() {
        pageQueryKnowledgeRelation(knowledgeRelationRepository);
    }

    @Override
    public void checkResource(String primaryCategory) {
        AbstractPageQuery abstractPageQuery = new CheckResourcePageQuery();
        abstractPageQuery.doing(primaryCategory);
    }

    @Override
    public void checkOneData(String primaryCategory, String id) {
        EspRepository<?> espRepository = ServicesManager.get(primaryCategory);
        Education education = null;
        try {
            education = (Education) espRepository.get(id);
        } catch (EspStoreException e) {
            e.printStackTrace();
        }

        if (education == null){
            return;
        }
        Set<String> uuids = new HashSet<>();
        uuids.add(education.getIdentifier());

        List<ResCoverage> resCoverageList = coverageDao.queryCoverageByResource(primaryCategory, uuids);

        List<String> resourceTypes = new ArrayList<String>();
        resourceTypes.add(primaryCategory);
        List<ResourceCategory> resourceCategoryList = ndResourceDao.queryCategoriesUseHql(resourceTypes, uuids);

        List<String> primaryCategorys = new ArrayList<>();
        primaryCategorys.add(primaryCategory);
        List<TechInfo> techInfos = ndResourceDao.queryTechInfosUseHql(primaryCategorys,uuids);

        List<ResourceRelation> resourceRelations = educationRelationdao.batchGetRelationByResourceSourceOrTarget(primaryCategory, uuids);

        titanImportRepository.checkResourceAllInTitan(education,resCoverageList,resourceCategoryList,techInfos, resourceRelations);

    }

    @Override
    public void checkAllData(String primaryCategory) {
        AbstractPageQuery abstractPageQuery = new CheckResourceAllPageQuery();
        abstractPageQuery.doing(primaryCategory);
    }

    abstract class  AbstractPageQuery{
        public long doing(String primaryCategory) {
            String fieldName = "dblastUpdate";

            long indexNum = 0;
            // 分页
            int page = 0;
            int row = 500;
            EspRepository<?> espRepository = ServicesManager.get(primaryCategory);
            @SuppressWarnings("rawtypes")
            Page resourcePage = new PageImpl(new ArrayList());
            @SuppressWarnings("rawtypes")
            List entitylist = null;

            List<Item<? extends Object>> items = new ArrayList<>();

            Item<String> resourceTypeItem = new Item<String>();
            resourceTypeItem.setKey("primaryCategory");
            resourceTypeItem.setComparsionOperator(ComparsionOperator.EQ);
            resourceTypeItem.setLogicalOperator(LogicalOperator.AND);
            resourceTypeItem.setValue(ValueUtils.newValue(primaryCategory));
            items.add(resourceTypeItem);

            Sort sort = new Sort(Sort.Direction.ASC, fieldName);
            do {
                Pageable pageable = new PageRequest(page, row, sort);

                try {
                    resourcePage = espRepository.findByItems(items, pageable);
                    if (resourcePage == null) {
                        break;
                    }
                    entitylist = resourcePage.getContent();
                    if (entitylist == null) {
                        continue;
                    }
                    List<Education> resources = new ArrayList<Education>();
                    for (Object object : entitylist) {
                        Education education = (Education) object;
                        resources.add(education);
                    }
                    if(entitylist.size()==0){
                        continue;
                    }
                    long size = operate(resources,primaryCategory);
                    indexNum += size;
                    LOG.info( "type: {} totalPage:{} page:{}",primaryCategory, resourcePage.getTotalPages(),page);
                } catch (Exception e) {
                    e.printStackTrace();
                    LOG.error(e.getMessage());
                    LOG.error("importTitanMySqlError page:{} primaryCategory:{}",page,primaryCategory);
                }
                setStatisticParam(primaryCategory,resourcePage.getTotalPages(),page);
            } while (++page < resourcePage.getTotalPages());

            return indexNum;
        }

        abstract long operate(List<Education> educations ,String primaryCategory);
    }

    class CheckResourcePageQuery extends  AbstractPageQuery{

        @Override
        long operate(List<Education> educations, String primaryCategory) {
            for(Education education : educations){
                titanImportRepository.checkResourceExistInTitan(education);
            }
            return 0;
        }
    }

    public class ImportData4ScriptPageQuery extends AbstractPageQuery{

        @Override
        long operate(List<Education> educations, String primaryCategory) {
            if(CollectionUtils.isEmpty(educations)){
                return 0L;
            }
            Set<String> uuids = new HashSet<String>();
            for (Education education : educations) {
                uuids.add(education.getIdentifier());
            }

            List<ResCoverage> resCoverageList = coverageDao.queryCoverageByResource(primaryCategory, uuids);
            Map<String, List<ResCoverage>> resCoverageMap = new HashMap<>();
            for (ResCoverage resCoverage : resCoverageList){
                List<ResCoverage> resCoverages = resCoverageMap.get(resCoverage.getResource());
                if(resCoverages == null){
                    resCoverages = new ArrayList<>();
                    resCoverageMap.put(resCoverage.getResource(), resCoverages);
                }

                resCoverages.add(resCoverage);
            }

            List<String> resourceTypes = new ArrayList<String>();
            resourceTypes.add(primaryCategory);
            List<ResourceCategory> resourceCategoryList = ndResourceDao.queryCategoriesUseHql(resourceTypes, uuids);
            Map<String, List<ResourceCategory>> resourceCategoryMap = new HashMap<>();
            for (ResourceCategory resourceCategory : resourceCategoryList){
                List<ResourceCategory> resourceCategories = resourceCategoryMap.get(resourceCategory.getResource());
                if(resourceCategories == null){
                    resourceCategories = new ArrayList<>();
                    resourceCategoryMap.put(resourceCategory.getResource(), resourceCategories);
                }

                resourceCategories.add(resourceCategory);
            }

            List<String> primaryCategorys = new ArrayList<>();
            primaryCategorys.add(primaryCategory);
            List<TechInfo> techInfos = ndResourceDao.queryTechInfosUseHql(primaryCategorys,uuids);
            Map<String, List<TechInfo>> techInfoMap = new HashMap<>();
            for (TechInfo techInfo : techInfos){
                List<TechInfo> techInfoList = techInfoMap.get(techInfo.getResource());
                if(techInfoList == null){
                    techInfoList = new ArrayList<>();
                    techInfoMap.put(techInfo.getResource(), techInfoList);
                }

                techInfoList.add(techInfo);
            }
            for (Education education : educations){
                List<TechInfo> sourceTechInfo = techInfoMap.get(education.getIdentifier());
                List<ResCoverage> sourceResCoverage = resCoverageMap.get(education.getIdentifier());
                List<ResourceCategory> resourceCategory = resourceCategoryMap.get(education.getIdentifier());
                titanImportRepository.importOneData(education,sourceResCoverage,resourceCategory,sourceTechInfo);
            }
            return educations.size();
        }
    }

    /**
     * 创建章节关系
     * */
    class CreateRelation4Chapter extends  AbstractPageQuery{
        @Override
        long operate(List<Education> educations,String primaryCategory) {

            List<Chapter> resources = new ArrayList<>();
            for (Education object : educations) {
                Chapter chapter = (Chapter) object;
                resources.add(chapter);
            }
            long size =	titanChapterRelationRepository.batchCreateRelation(resources);
            titanChapterRelationRepository.updateRelationOrderValue(resources,primaryCategory);
            return 0;
        }
    }

    /**
     * 导入知识点关系
     * */
    class CreateRelation4Knowledge extends  AbstractPageQuery{
        @Override
        long operate(List<Education> educations, String primaryCategory) {
            List<Chapter> resources = new ArrayList<>();
            for (Education object : educations) {
                Chapter knowledge = (Chapter) object;
                resources.add(knowledge);
            }
            titanKnowledgeRelationRepository.batchCreateRelation4Tree(resources);
            titanChapterRelationRepository.updateRelationOrderValue(resources,primaryCategory);
            return 0L;
        }
    }

    class CheckResourceAllPageQuery extends  AbstractPageQuery{

        @Override
        long operate(List<Education> educations, String primaryCategory) {

            if(CollectionUtils.isEmpty(educations)){
                return 0L;
            }
            Set<String> uuids = new HashSet<String>();
            for (Education education : educations) {
                uuids.add(education.getIdentifier());
            }

            List<ResCoverage> resCoverageList = coverageDao.queryCoverageByResource(primaryCategory, uuids);
            Map<String, List<ResCoverage>> resCoverageMap = new HashMap<>();
            for (ResCoverage resCoverage : resCoverageList){
                List<ResCoverage> resCoverages = resCoverageMap.get(resCoverage.getResource());
                if(resCoverages == null){
                    resCoverages = new ArrayList<>();
                    resCoverageMap.put(resCoverage.getResource(), resCoverages);
                }

                resCoverages.add(resCoverage);
            }

            List<String> resourceTypes = new ArrayList<String>();
            resourceTypes.add(primaryCategory);
            List<ResourceCategory> resourceCategoryList = ndResourceDao.queryCategoriesUseHql(resourceTypes, uuids);
            Map<String, List<ResourceCategory>> resourceCategoryMap = new HashMap<>();
            for (ResourceCategory resourceCategory : resourceCategoryList){
                List<ResourceCategory> resourceCategories = resourceCategoryMap.get(resourceCategory.getResource());
                if(resourceCategories == null){
                    resourceCategories = new ArrayList<>();
                    resourceCategoryMap.put(resourceCategory.getResource(), resourceCategories);
                }

                resourceCategories.add(resourceCategory);
            }

            List<String> primaryCategorys = new ArrayList<>();
            primaryCategorys.add(primaryCategory);
            List<TechInfo> techInfos = ndResourceDao.queryTechInfosUseHql(primaryCategorys,uuids);
            Map<String, List<TechInfo>> techInfoMap = new HashMap<>();
            for (TechInfo techInfo : techInfos){
                List<TechInfo> techInfoList = techInfoMap.get(techInfo.getResource());
                if(techInfoList == null){
                    techInfoList = new ArrayList<>();
                    techInfoMap.put(techInfo.getResource(), techInfoList);
                }

                techInfoList.add(techInfo);
            }
            for (Education education : educations){
                List<TechInfo> sourceTechInfo = techInfoMap.get(education.getIdentifier());
                List<ResCoverage> sourceResCoverage = resCoverageMap.get(education.getIdentifier());
                List<ResourceCategory> resourceCategory = resourceCategoryMap.get(education.getIdentifier());
                titanImportRepository.checkResourceAllInTitan(education,sourceResCoverage,resourceCategory,sourceTechInfo ,null);
            }

            return 0;
        }
    }

    private void setStatisticParam(String primaryCategory , Integer totalPage , Integer page){
        s_primaryCategory = primaryCategory;
        s_totalPage = totalPage;
        s_page = page;
    }

    public long pageQueryRelation(ResourceRepository resourceRepository) {
        String fieldName = "identifier";

        long indexNum = 0;
        // 分页
        int page = 0;
        int row = 500;
        @SuppressWarnings("rawtypes")
        Page resourcePage = new PageImpl(new ArrayList());;
        @SuppressWarnings("rawtypes")
        List entitylist = null;

        List<Item<? extends Object>> items = new ArrayList<>();

        Sort sort = new Sort(Sort.Direction.ASC, fieldName);
        do {
            Pageable pageable = new PageRequest(page, row, sort);

            try {
                resourcePage = resourceRepository.findByItems(items, pageable);
                if (resourcePage == null) {
                    break;
                }
                entitylist = resourcePage.getContent();
                if (entitylist == null) {
                    continue;
                }
                List<ResourceRelation> resourceRelations = new ArrayList<ResourceRelation>();
                for (Object object : entitylist) {
                    ResourceRelation relation = (ResourceRelation) object;
                    resourceRelations.add(relation);
                }
                if(entitylist.size()==0){
                    continue;
                }
                titanRelationRepository.batchAdd4Import(resourceRelations);

                LOG.info("import relation:totalPage:{}  page:{}",resourcePage.getTotalPages(),page);
            } catch (Exception e) {
                e.printStackTrace();
                LOG.error(e.getMessage());
            }
            setStatisticParam("relations", resourcePage.getTotalPages(), page);
        } while (++page < resourcePage.getTotalPages());

        return indexNum;
    }

    public long pageQueryKnowledgeRelation(ResourceRepository resourceRepository) {
        String fieldName = "identifier";

        long total = 0;
        // 分页
        int page = 0;
        int row = 500;
        @SuppressWarnings("rawtypes")
        Page resourcePage = new PageImpl(new ArrayList());
        @SuppressWarnings("rawtypes")
        List entitylist = null;

        List<Item<? extends Object>> items = new ArrayList<>();

        Sort sort = new Sort(Sort.Direction.ASC, fieldName);
        do {
            Pageable pageable = new PageRequest(page, row, sort);

            try {
                resourcePage = resourceRepository.findByItems(items, pageable);
                if (resourcePage == null) {
                    break;
                }
                entitylist = resourcePage.getContent();
                if (entitylist == null) {
                    continue;
                }
                List<KnowledgeRelation> knowledgeRelations = new ArrayList<KnowledgeRelation>();
                for (Object object : entitylist) {
                    KnowledgeRelation relation = (KnowledgeRelation) object;
                    knowledgeRelations.add(relation);
                }
                if(entitylist.size()==0){
                    continue;
                }

                total = total + titanKnowledgeRelationRepository.batchAdd(knowledgeRelations).size();

                LOG.info("import relation:totalPage:{}  page:{}",resourcePage.getTotalPages(),page);
            } catch (Exception e) {
                e.printStackTrace();
                LOG.error(e.getMessage());
            }
        } while (++page < resourcePage.getTotalPages());

        return total;
    }


}
