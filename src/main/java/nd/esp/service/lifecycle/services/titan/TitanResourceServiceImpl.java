package nd.esp.service.lifecycle.services.titan;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nd.esp.service.lifecycle.daos.coverage.v06.CoverageDao;
import nd.esp.service.lifecycle.daos.educationrelation.v06.EducationRelationDao;
import nd.esp.service.lifecycle.daos.titan.inter.*;
import nd.esp.service.lifecycle.educommon.dao.NDResourceDao;
import nd.esp.service.lifecycle.educommon.services.impl.CommonServiceHelper;
import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.EspRepository;
import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.ds.ComparsionOperator;
import nd.esp.service.lifecycle.repository.ds.Item;
import nd.esp.service.lifecycle.repository.ds.LogicalOperator;
import nd.esp.service.lifecycle.repository.ds.ValueUtils;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.*;
import nd.esp.service.lifecycle.repository.sdk.*;
import nd.esp.service.lifecycle.repository.sdk.impl.ServicesManager;
import nd.esp.service.lifecycle.support.busi.elasticsearch.ResourceTypeSupport;
import nd.esp.service.lifecycle.support.busi.titan.CheckResourceModel;
import nd.esp.service.lifecycle.support.busi.titan.TitanResourceUtils;
import nd.esp.service.lifecycle.support.busi.titan.TitanSyncType;
import nd.esp.service.lifecycle.support.busi.titan.tranaction.TitanOperationType;
import nd.esp.service.lifecycle.support.busi.titan.tranaction.TitanRepositoryOperation;
import nd.esp.service.lifecycle.support.busi.titan.tranaction.TitanSubmitTransaction;
import nd.esp.service.lifecycle.support.busi.titan.tranaction.TitanTransaction;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.utils.TitanScritpUtils;

import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.driver.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.ibm.icu.math.BigDecimal;

@Service
public class TitanResourceServiceImpl implements TitanResourceService {
    private static String s_primaryCategory;
    private static Integer s_page;
    private static Integer s_totalPage;
    private static final Logger LOG = LoggerFactory
            .getLogger(TitanResourceServiceImpl.class);
    @Autowired
    private CoverageDao coverageDao;

    @Autowired
    private NDResourceDao ndResourceDao;

    @Autowired
    private EducationRelationDao educationRelationdao;

    @Autowired
    private TitanChapterRelationRepository titanChapterRelationRepository;

    @Autowired
    private TitanKnowledgeRelationRepository titanKnowledgeRelationRepository;

    @Autowired
    private ResourceRelation4QuestionDBRepository resourceRelation4QuestionDBRepository;

    @Autowired
    private ResourceRelationRepository resourceRelationRepository;

    @Autowired
    private KnowledgeRelationRepository knowledgeRelationRepository;

    @Autowired
    private TitanCommonRepository titanCommonRepository;

    @Autowired
    private TitanImportRepository titanImportRepository;

    @Autowired
    private CategoryDataRepository categoryDataRepository;


    @Autowired
    private TitanUpdateDataRepository titanUpdateDataRepository;


    @Qualifier(value = "defaultJdbcTemplate")
    @Autowired
    private JdbcTemplate defaultJdbcTemplate;
    @Qualifier(value = "questionJdbcTemplate")
    @Autowired
    private JdbcTemplate questionJdbcTemplate;

    @Autowired
    @Qualifier(value = "defaultJdbcTemplate")
    private JdbcTemplate jdbcTemplate;


    @Autowired
    private ResourceStatisticalRepository resourceStatisticalRepository;

    @Autowired
    private ResourceStatistical4QuestionDBRepository resourceStatistical4QuestionDBRepository;

    @Autowired
    private TitanSyncRepository titanSyncRepository;

    @Autowired
    private TitanRelationRepository titanRelationRepository;

    @Autowired
    private TitanRepositoryUtils titanRepositoryUtils;

    @Autowired
    private TitanCheckResourceExistRepository titanCheckResourceExistRepository;

    @Autowired
    private TitanSubmitTransaction titanSubmitTransaction;

    @Override
    public long importData4Script(String primaryCategory) {
        AbstractPageQuery abstractPageQuery = new ImprotData4ScriptPageQuery();
        abstractPageQuery.doing(primaryCategory);
        return 0;
    }

    @Override
    public long createChapterRelation() {
        AbstractPageQuery abstractPageQuery = new CreateRelation4Chapter();

        return abstractPageQuery.doing("chapters");
    }

    @Override
    public long createKnowledgeRealtion() {
        AbstractPageQuery abstractPageQuery = new CreateRelation4Knowledge();
        return abstractPageQuery.doing("knowledges");
    }

    @Override
    public void updateChapterRelation() {
        AbstractPageQuery abstractPageQuery = new UpdateDataPageQuery();
        abstractPageQuery.doing("chapters");
    }

    @Override
    public void updateKnowledgeRelation() {
        AbstractPageQuery abstractPageQuery = new UpdateDataPageQuery();
        abstractPageQuery.doing("knowledges");
    }

    @Override
    public void timeTaskImport(Integer page, String type) {
        TimeTaskPageQuery timeTaskPageQuery = new TimeTaskPageQuery4Import(page, type);
        timeTaskPageQuery.schedule();
    }

    @Override
    public long importAllRelation() {
        long size = 0L;
        AbstractPageQueryRelation abstractPageQueryRelation = new AbstractPageQueryRelationCreate();
        size = abstractPageQueryRelation.pageQueryRelation(resourceRelationRepository);
        size = size + abstractPageQueryRelation.pageQueryRelation(resourceRelation4QuestionDBRepository);
        return size;
    }

    @Override
    public void importAllRelationPage(Integer page) {
        AbstractPageQueryRelation abstractPageQueryRelation = new AbstractPageQueryRelationCreate(page);
        abstractPageQueryRelation.pageQueryRelation(resourceRelationRepository);
        abstractPageQueryRelation = new AbstractPageQueryRelationCreate(0);
        abstractPageQueryRelation.pageQueryRelation(resourceRelation4QuestionDBRepository);
    }

    @Override
    public void updateRelationRedRelation(Integer page) {
        AbstractPageQueryRelation abstractPageQueryRelation
                = new AbstractPageQueryRelationRedProperty(page);
        abstractPageQueryRelation.pageQueryRelation(resourceRelationRepository);

        abstractPageQueryRelation = new AbstractPageQueryRelationRedProperty(0);
        abstractPageQueryRelation.pageQueryRelation(resourceRelation4QuestionDBRepository);
    }

    @Override
    public void repairAllRelation() {
        AbstractPageQueryRelation abstractPageQueryRelation = new AbstractPageQueryRelationRepair();
        abstractPageQueryRelation.pageQueryRelation(resourceRelationRepository);
        abstractPageQueryRelation.pageQueryRelation(resourceRelation4QuestionDBRepository);
    }

    @Override
    public void repairOne(String primaryCategory, String id) {

        EspRepository<?> espRepository = ServicesManager.get(primaryCategory);
        Education educationOld = null;
        try {
            educationOld = (Education) espRepository.get(id);
        } catch (EspStoreException e) {
            LOG.error(e.getLocalizedMessage());
        }

        if (educationOld == null) {
            return;
        }

        List<Education> educations = new ArrayList<>();
        educations.add(educationOld);

        repairData(educations, primaryCategory);
    }

    @Override
    public void timeTaskRepair(Integer page, String type) {
        TimeTaskPageQuery timeTaskPageQuery = new TimeTaskPageQuery4Repair(page, type);
        timeTaskPageQuery.schedule();
    }

    @Override
    public long importKnowledgeRelation() {
        return pageQueryKnowledgeRelation(knowledgeRelationRepository);
    }

    @Override
    public void timeTaskImport4Update(Integer page, String type) {
        TimeTaskPageQuery4Update timeTaskPageQuery4Update = new TimeTaskPageQuery4Update(page, type);
        timeTaskPageQuery4Update.schedule();
    }

    @Override
    public void importOneData4Script(String primaryCategory, String id) {
        EspRepository<?> espRepository = ServicesManager.get(primaryCategory);
        Education education = null;
        try {
            education = (Education) espRepository.get(id);
        } catch (EspStoreException e) {
            LOG.error(e.getLocalizedMessage());
        }

        if (education == null) {
            return;
        }
        importData(education, primaryCategory);
    }

    @Override
    public void checkOneData(String primaryCategory, String id) {
        EspRepository<?> espRepository = ServicesManager.get(primaryCategory);
        Education education = null;
        try {
            education = (Education) espRepository.get(id);
        } catch (EspStoreException e) {
            LOG.error(e.getLocalizedMessage());
        }

        if (education == null) {
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
        List<TechInfo> techInfos = ndResourceDao.queryTechInfosUseHql(primaryCategorys, uuids);


        List<ResourceRelation> resourceRelations = educationRelationdao.batchGetRelationByResourceSourceOrTarget(primaryCategory, uuids);

        List<ResourceStatistical> resourceStatistic = ndResourceDao.queryStatisticalUseHql(resourceTypes, uuids);
        titanCheckResourceExistRepository.checkOneResourceInTitan(education, resCoverageList, resourceCategoryList, techInfos, resourceRelations, resourceStatistic);

    }

    @Override
    public String importStatus() {
        return "primaryCategory:" + s_primaryCategory + "  totalPage:" + s_totalPage + "  page" + s_page;
    }

    @Override
    public void code() {
        String sql = "select nd_code from category_datas";
        List<String> codes = jdbcTemplate.queryForList(sql, String.class);
        for (String code : codes) {
            String script = "g.V().has('cg_taxoncode',code).has('identifier')";
            Map<String, Object> param = new HashMap<>();
            param.put("code", code);
            try {
                ResultSet resultSet = titanCommonRepository.executeScriptResultSet(script, param);
                Iterator<Result> iterator = resultSet.iterator();
                if (iterator.hasNext()) {
                    Integer id = iterator.next().getInt();
                    if (id > 1) {
                        LOG.info(code);
                    }
                }
            } catch (Exception e) {

            }
        }
    }

    @Override
    public void repairData(String primaryCategory) {
        AbstractPageQuery abstractPageQuery = new RepairDataPageQuery();
        abstractPageQuery.doing(primaryCategory);
    }

    @Override
    public void importStatistical() {
        AbstractPageQueryStatistical abstractPageQuery = new ImportPageQueryStatistical(ResourceNdCode.assets.toString());
        abstractPageQuery.pageQueryStatistical();

        abstractPageQuery = new ImportPageQueryStatistical(ResourceNdCode.questions.toString());
        abstractPageQuery.pageQueryStatistical();
    }

    @Override
    public void checkAllData(String primaryCategory) {
        AbstractPageQuery abstractPageQuery = new CheckResourceAllPageQuery();
        abstractPageQuery.doing(primaryCategory);
    }

    /**
     * 对某种资源数据做校验，例如：assets，questions
     *
     * @param primaryCategory 资源类型  例如：assets，questions
     * @see
     * @since 1.2.6
     */
    @Override
    public void checkOneResourceTypeData(String primaryCategory, Date beginDate, Date endDate) {
        AbstractPageQuery abstractPageQuery = new CheckResource(BigDecimal.valueOf(beginDate.getTime()), BigDecimal.valueOf(endDate.getTime()));
        abstractPageQuery.doing(primaryCategory);
    }

    @Override
    public void checkAllResourceRelations() {
        AbstractPageQueryRelation abstractPageQueryRelation = new AbstractPageQueryRelationCheck();
        abstractPageQueryRelation.pageQueryRelation(resourceRelationRepository);
        abstractPageQueryRelation.pageQueryRelation(resourceRelation4QuestionDBRepository);
    }

    @Override
    public boolean changeSyncType(String newType, String oldType, Integer executTimes) {

        String fieldName = "createTime";

        long total = 0;
        // 分页
        int page = 0;
        int row = 500;
        @SuppressWarnings("rawtypes")
        Page resourcePage = new PageImpl(new ArrayList());
        @SuppressWarnings("rawtypes")
        List entitylist = null;

        List<Item<? extends Object>> items = new ArrayList<>();

        Item<String> resourceTypeItem = new Item<String>();
        resourceTypeItem.setKey("type");
        resourceTypeItem.setComparsionOperator(ComparsionOperator.EQ);
        resourceTypeItem.setLogicalOperator(LogicalOperator.AND);
        resourceTypeItem.setValue(ValueUtils.newValue(oldType));
        items.add(resourceTypeItem);

        Sort sort = new Sort(Direction.ASC, fieldName);
        do {
            Pageable pageable = new PageRequest(page, row, sort);

            try {
                resourcePage = titanSyncRepository.findByItems(items, pageable);
                if (resourcePage == null) {
                    break;
                }
                entitylist = resourcePage.getContent();
                if (entitylist == null) {
                    continue;
                }
                if (entitylist.size() == 0) {
                    continue;
                }
                for (Object obj : entitylist){
                    TitanSync titanSync = (TitanSync) obj;
                    titanSyncRepository.delete(titanSync);
                    titanRepositoryUtils.titanSync4MysqlAdd(TitanSyncType.value(newType),titanSync.getPrimaryCategory(),titanSync.getResource(),executTimes);
                }

                LOG.info("import relation:totalPage:{}  page:{}", resourcePage.getTotalPages(), page);
            } catch (Exception e) {
                LOG.error(e.getMessage());
            }
        } while (++page < resourcePage.getTotalPages());
        return true;
    }

    @Override
    public boolean deleteSyncType(String type) {
        String sql = "DELETE FROM titan_sync  where type='" + type + "'";
        jdbcTemplate.execute(sql);
        return true;
    }

    @Override
    public void detailErrorRelation() {
        pageQueryTitanSync4Questions();
    }



    @Override
    public void checkResource(String primaryCategory) {
        AbstractPageQuery abstractPageQuery = new CheckResourcePageQuery();
        abstractPageQuery.doing(primaryCategory);
    }

    private void pageQueryTitanSync4Questions() {
        String fieldName = "createTime";

        long total = 0;
        // 分页
        int page = 0;
        int row = 500;
        @SuppressWarnings("rawtypes")
        Page resourcePage = new PageImpl(new ArrayList());
        @SuppressWarnings("rawtypes")
        List entitylist = null;

        List<Item<? extends Object>> items = new ArrayList<>();

        Item<String> resourceTypeItem = new Item<String>();
        resourceTypeItem.setKey("type");
        resourceTypeItem.setComparsionOperator(ComparsionOperator.EQ);
        resourceTypeItem.setLogicalOperator(LogicalOperator.AND);
        resourceTypeItem.setValue(ValueUtils.newValue("RELATION"));
        items.add(resourceTypeItem);

        Sort sort = new Sort(Direction.ASC, fieldName);
        do {
            Pageable pageable = new PageRequest(page, row, sort);

            try {
                resourcePage = titanSyncRepository.findByItems(items, pageable);
                if (resourcePage == null) {
                    break;
                }
                entitylist = resourcePage.getContent();
                if (entitylist == null) {
                    continue;
                }
                Map<String, Set<String>> sourceMap = new HashMap<>();
                for (Object object : entitylist) {
                    TitanSync st = (TitanSync) object;
                    Set<String> sourceList = sourceMap.get(st.getPrimaryCategory());
                    if (sourceList == null){
                        sourceList = new HashSet<>();
                        sourceMap.put(st.getPrimaryCategory(), sourceList);
                    }
                    sourceList.add(st.getResource());
                }
                if (entitylist.size() == 0) {
                    continue;
                }

                for (String primaryCategory : sourceMap.keySet()){
                    List<ResourceRelation> resourceRelations = educationRelationdao
                            .batchGetRelationByResourceSourceOrTarget(primaryCategory, sourceMap.get(primaryCategory));
                    titanImportRepository.batchImportRelation(resourceRelations);
                    LOG.info("");
                }
                LOG.info("import relation:totalPage:{}  page:{}", resourcePage.getTotalPages(), page);
            } catch (Exception e) {
                LOG.error(e.getMessage());
            }
        } while (++page < resourcePage.getTotalPages());
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

        Sort sort = new Sort(Direction.ASC, fieldName);
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
                if (entitylist.size() == 0) {
                    continue;
                }
                //TODO check
                total = total + titanKnowledgeRelationRepository.batchAdd(knowledgeRelations).size();

                LOG.info("import relation:totalPage:{}  page:{}", resourcePage.getTotalPages(), page);
            } catch (Exception e) {
                LOG.error(e.getMessage());
            }
        } while (++page < resourcePage.getTotalPages());

        return total;
    }

    public abstract class AbstractPageQueryRelation {
        private Integer startPage = 0;
        public AbstractPageQueryRelation(Integer page){
            this.startPage = page;
        }

        public AbstractPageQueryRelation(){
            this.startPage = 0;
        }
        public long pageQueryRelation(ResourceRepository resourceRepository) {
            String fieldName = "identifier";

            long indexNum = 0;
            // 分页
            int page = startPage;
            int row = 500;
            @SuppressWarnings("rawtypes")
            Page resourcePage = new PageImpl(new ArrayList());
            ;
            @SuppressWarnings("rawtypes")
            List entitylist = null;

            List<Item<? extends Object>> items = new ArrayList<>();

            Sort sort = new Sort(Direction.ASC, fieldName);
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
                    if (entitylist.size() == 0) {
                        continue;
                    }
                    method(resourceRelations);
                    LOG.info("import relation:totalPage:{}  page:{}", resourcePage.getTotalPages(), page);
                } catch (Exception e) {
                    LOG.error(e.getLocalizedMessage());
                }
                setStatisticParam("relations", resourcePage.getTotalPages(), page);
            } while (++page < resourcePage.getTotalPages());

            startPage = 0;
            return indexNum;
        }

        public abstract void method(List<ResourceRelation> resourceRelations);
    }

    public class AbstractPageQueryRelationRedProperty extends AbstractPageQueryRelation {
        public AbstractPageQueryRelationRedProperty(Integer page){
            super(page);
        }
        @Override
        public void method(List<ResourceRelation> resourceRelations) {

            for (ResourceRelation relation : getAllExistRelation(resourceRelations)){
                TitanTransaction titanTransaction = new TitanTransaction(null);
                TitanRepositoryOperation operation = new TitanRepositoryOperation();
                operation.setEntity(relation);
                operation.setOperationType(TitanOperationType.update_relation_red_property);
                titanTransaction.addNextStep(operation);
                titanSubmitTransaction.submit(titanTransaction);
            }
        }
    }

    public class AbstractPageQueryRelationCheck extends AbstractPageQueryRelation {

        public AbstractPageQueryRelationCheck(Integer startPage) {
            super(startPage);
        }

        public AbstractPageQueryRelationCheck() {
            super();
        }

        @Override
        public void method(List<ResourceRelation> resourceRelations) {
            List<ResourceRelation> existRelation = getAllExistRelation(resourceRelations);
            titanCheckResourceExistRepository.checkResourceRelations(existRelation);
        }
    }

    public class AbstractPageQueryRelationCreate extends AbstractPageQueryRelation {
        public AbstractPageQueryRelationCreate(Integer startPage) {
            super(startPage);
        }

        public AbstractPageQueryRelationCreate() {
            super();
        }
        @Override
        public void method(List<ResourceRelation> resourceRelations) {
            List<ResourceRelation> existRelation = getAllExistRelation(resourceRelations);
            titanImportRepository.batchImportRelation(existRelation);
        }
    }

    public class AbstractPageQueryRelationRepair extends AbstractPageQueryRelation {

        public AbstractPageQueryRelationRepair() {
            super();
        }

        public AbstractPageQueryRelationRepair(Integer startPage) {
            super(startPage);
        }

        @Override
        public void method(List<ResourceRelation> resourceRelations) {
            List<ResourceRelation> existRelation = getAllExistRelation(resourceRelations);
            titanUpdateDataRepository.batchUpdateRelation(existRelation);
        }
    }

    abstract class AbstractPageQuery {
        BigDecimal beginDate;
        BigDecimal endDate;

        public AbstractPageQuery() {

        }

        public AbstractPageQuery(BigDecimal beginDate, BigDecimal endDate) {
            this.beginDate = beginDate;
            this.endDate = endDate;
        }

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
            if (beginDate != null && endDate != null) {
                Item<BigDecimal> resourceTypeItemBegin = new Item<BigDecimal>();
                resourceTypeItemBegin.setKey(fieldName);
                resourceTypeItemBegin.setComparsionOperator(ComparsionOperator.GE);
                resourceTypeItemBegin.setLogicalOperator(LogicalOperator.AND);
                resourceTypeItemBegin.setValue(ValueUtils.newValue(beginDate));

                Item<BigDecimal> resourceTypeItemEnd = new Item<BigDecimal>();
                resourceTypeItemEnd.setKey(fieldName);
                resourceTypeItemEnd.setComparsionOperator(ComparsionOperator.LT);
                resourceTypeItemEnd.setLogicalOperator(LogicalOperator.AND);
                resourceTypeItemEnd.setValue(ValueUtils.newValue(endDate));

                items.add(resourceTypeItemBegin);
                items.add(resourceTypeItemEnd);
            }
            items.add(resourceTypeItem);

            Sort sort = new Sort(Direction.ASC, fieldName);
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
                    if (entitylist.size() == 0) {
                        continue;
                    }
                    long size = operate(resources, primaryCategory);
                    indexNum += size;
                    LOG.info("type: {} totalPage:{} page:{}", primaryCategory, resourcePage.getTotalPages(), page);
                } catch (Exception e) {
                    e.printStackTrace();
                    LOG.error("importTitanMySqlError page:{} primaryCategory:{}", page, primaryCategory);
                }
                setStatisticParam(primaryCategory, resourcePage.getTotalPages(), page);
            } while (++page < resourcePage.getTotalPages());

            return indexNum;
        }

        abstract long operate(List<Education> educations, String primaryCategory);
    }

    class CheckResource extends AbstractPageQuery {
        public CheckResource(BigDecimal beginDate, BigDecimal endDate) {
            super(beginDate, endDate);
        }

        @Override
        long operate(List<Education> educations, String primaryCategory) {
            if (CollectionUtils.isEmpty(educations)) {
                return 0L;
            }

            Set<String> uuids = getEducationIdentifierSet(educations);

            List<ResCoverage> resCoverageList = coverageDao.queryCoverageByResource(primaryCategory, uuids);
            Multimap<String, ResCoverage> resCoverageMultimap = toResCoverageMultimap(resCoverageList);

            Multimap<String, TechInfo> techInfoMultimap = queryTechInfoUseHql(primaryCategory, uuids);

            Multimap<String, ResourceCategory> resourceCategoryMultimap = queryCategoroiesUseHql(primaryCategory, uuids);

            Multimap<String, ResourceStatistical> resourceStatisticalMultimap = queryStatisticalUseHql(primaryCategory, uuids);

            for (Education education : educations) {
                List<TechInfo> techInfos = new ArrayList<TechInfo>(techInfoMultimap.get(education.getIdentifier()));
                List<ResCoverage> resCoverages = new ArrayList<ResCoverage>(resCoverageMultimap.get(education.getIdentifier()));
                List<ResourceCategory> resourceCategories = new ArrayList<ResourceCategory>(resourceCategoryMultimap.get(education.getIdentifier()));
                List<ResourceStatistical> statistic = new ArrayList<ResourceStatistical>(resourceStatisticalMultimap.get(education.getIdentifier()));
                CheckResourceModel checkResourceModel = new CheckResourceModel.Builder(education).techInfos(techInfos).
                        resCoverages(resCoverages).resourceCategories(resourceCategories).statistic(statistic).builder();
//                titanImportRepository.checkResourceAllInTitan2(education,resCoverages,resourceCategories,techInfos ,null, statistic);
                titanCheckResourceExistRepository.checkResourcesInTitan(checkResourceModel);
            }
            return 0;
        }

        private Multimap<String, ResourceStatistical> queryStatisticalUseHql(String primaryCategory, Set<String> uuids) {
            List<String> resourceTypes = new ArrayList<String>();
            resourceTypes.add(primaryCategory);
            List<ResourceStatistical> resourceStatistic = ndResourceDao.queryStatisticalUseHql(resourceTypes, uuids);
            Multimap<String, ResourceStatistical> resourceCategoryMultimap = toResourceStatisticalMultimap(resourceStatistic);
            return resourceCategoryMultimap;
        }

        private Multimap<String, ResourceCategory> queryCategoroiesUseHql(String primaryCategory, Set<String> uuids) {
            List<String> resourceTypes = new ArrayList<String>();
            resourceTypes.add(primaryCategory);
            List<ResourceCategory> resourceCategoryList = ndResourceDao.queryCategoriesUseHql(resourceTypes, uuids);
            Multimap<String, ResourceCategory> resourceCategoryMultimap = toResourceCategoryMultimap(resourceCategoryList);
            return resourceCategoryMultimap;
        }

        private Multimap<String, TechInfo> queryTechInfoUseHql(String primaryCategory, Set<String> uuids) {
            List<String> primaryCategorys = new ArrayList<String>();
            primaryCategorys.add(primaryCategory);
            List<TechInfo> techInfos = ndResourceDao.queryTechInfosUseHql(primaryCategorys, uuids);
            Multimap<String, TechInfo> techInfoMultimap = toTechInfoMultimap(techInfos);
            return techInfoMultimap;
        }

        private Set<String> getEducationIdentifierSet(List<Education> educations) {
            Set<String> uuids = new HashSet<String>();
            for (Education education : educations) {
                uuids.add(education.getIdentifier());
            }
            return uuids;
        }

        private Multimap<String, ResCoverage> toResCoverageMultimap(List<ResCoverage> coverages) {
            Multimap<String, ResCoverage> multimap = ArrayListMultimap.create();

            for (ResCoverage resCoverage : coverages) {
                multimap.put(resCoverage.getResource(), resCoverage);
            }
            return multimap;
        }

        private Multimap<String, TechInfo> toTechInfoMultimap(List<TechInfo> techInfos) {
            Multimap<String, TechInfo> multimap = ArrayListMultimap.create();
            for (TechInfo techInfo : techInfos) {
                multimap.put(techInfo.getResource(), techInfo);
            }
            return multimap;
        }

        private Multimap<String, ResourceCategory> toResourceCategoryMultimap(List<ResourceCategory> categories) {
            Multimap<String, ResourceCategory> multimap = ArrayListMultimap.create();
            for (ResourceCategory resourceCategory : categories) {
                multimap.put(resourceCategory.getResource(), resourceCategory);
            }
            return multimap;
        }

        private Multimap<String, ResourceStatistical> toResourceStatisticalMultimap(List<ResourceStatistical> statistic) {
            Multimap<String, ResourceStatistical> multimap = ArrayListMultimap.create();
            for (ResourceStatistical stat : statistic) {
                multimap.put(stat.getResource(), stat);
            }
            return multimap;
        }
    }

    class CheckResourcePageQuery extends AbstractPageQuery {

        @Override
        long operate(List<Education> educations, String primaryCategory) {
            for (Education education : educations) {
                titanImportRepository.checkResourceExistInTitan(education);
            }
            return 0;
        }
    }

    class CheckResourceAllPageQuery extends AbstractPageQuery {

        @Override
        long operate(List<Education> educations, String primaryCategory) {

            if (CollectionUtils.isEmpty(educations)) {
                return 0L;
            }
            Set<String> uuids = new HashSet<String>();
            for (Education education : educations) {
                uuids.add(education.getIdentifier());
            }

            List<ResCoverage> resCoverageList = coverageDao.queryCoverageByResource(primaryCategory, uuids);
            Map<String, List<ResCoverage>> resCoverageMap = TitanResourceUtils.groupCoverage(resCoverageList);


            List<String> resourceTypes = new ArrayList<String>();
            resourceTypes.add(primaryCategory);
            List<ResourceCategory> resourceCategoryList = ndResourceDao.queryCategoriesUseHql(resourceTypes, uuids);
            Map<String, List<ResourceCategory>> resourceCategoryMap = TitanResourceUtils.groupCategory(resourceCategoryList);


            List<String> primaryCategorys = new ArrayList<>();
            primaryCategorys.add(primaryCategory);
            List<TechInfo> techInfos = ndResourceDao.queryTechInfosUseHql(primaryCategorys, uuids);
            Map<String, List<TechInfo>> techInfoMap = TitanResourceUtils.groupTechInfo(techInfos);

            for (Education education : educations) {
                List<TechInfo> sourceTechInfo = techInfoMap.get(education.getIdentifier());
                List<ResCoverage> sourceResCoverage = resCoverageMap.get(education.getIdentifier());
                List<ResourceCategory> resourceCategory = resourceCategoryMap.get(education.getIdentifier());
                titanImportRepository.checkResourceAllInTitan(education, sourceResCoverage, resourceCategory, sourceTechInfo, null);
            }

            return 0;
        }
    }


    /**
     * 创建章节关系
     */
    class CreateRelation4Chapter extends AbstractPageQuery {
        @Override
        long operate(List<Education> educations, String primaryCategory) {

            List<Chapter> resources = new ArrayList<>();
            for (Education object : educations) {
                Chapter chapter = (Chapter) object;
                resources.add(chapter);
            }
            //TODO check
            long size = titanChapterRelationRepository.batchCreateRelation(resources);
            titanChapterRelationRepository.updateRelationOrderValue(resources, primaryCategory);
            return size;
        }
    }


    class UpdateDataPageQuery extends AbstractPageQuery {
        @Override
        long operate(List<Education> educations, String primaryCategory) {
            List<Chapter> resources = new ArrayList<>();
            for (Education object : educations) {
                Chapter chapter = (Chapter) object;
                resources.add(chapter);
            }
            //TODO check
            titanChapterRelationRepository.updateRelationOrderValue(resources, primaryCategory);
            return educations.size();
        }
    }


    /**
     * 导入知识点关系
     */
    class CreateRelation4Knowledge extends AbstractPageQuery {
        @Override
        long operate(List<Education> educations, String primaryCategory) {
            List<Chapter> resources = new ArrayList<>();
            for (Education object : educations) {
                Chapter knowledge = (Chapter) object;
                resources.add(knowledge);
            }
            //TODO check
            titanKnowledgeRelationRepository.batchCreateRelation4Tree(resources);
            titanChapterRelationRepository.updateRelationOrderValue(resources, primaryCategory);
            return 0L;
        }
    }


    public class ImprotData4ScriptPageQuery extends AbstractPageQuery {

        @Override
        long operate(List<Education> educations, String primaryCategory) {

            return importData(educations, primaryCategory);
        }
    }

    public class RepairDataPageQuery extends AbstractPageQuery {

        @Override
        long operate(List<Education> educations, String primaryCategory) {
            return repairData(educations, primaryCategory);
        }
    }

    private long repairData(List<Education> educations, String primaryCategory) {
        if (CollectionUtils.isEmpty(educations)) {
            return 0L;
        }
        Set<String> uuids = new HashSet<String>();
        for (Education education : educations) {
            uuids.add(education.getIdentifier());
        }

        List<ResCoverage> resCoverageList = coverageDao.queryCoverageByResource(primaryCategory, uuids);
        Map<String, List<ResCoverage>> resCoverageMap = TitanResourceUtils.groupCoverage(resCoverageList);


        List<String> resourceTypes = new ArrayList<String>();
        resourceTypes.add(primaryCategory);
        List<ResourceCategory> resourceCategoryList = ndResourceDao.queryCategoriesUseHql(resourceTypes, uuids);
        Map<String, List<ResourceCategory>> resourceCategoryMap = TitanResourceUtils.groupCategory(resourceCategoryList);


        List<String> primaryCategorys = new ArrayList<>();
        primaryCategorys.add(primaryCategory);
        List<TechInfo> techInfos = ndResourceDao.queryTechInfosUseHql(primaryCategorys, uuids);
        Map<String, List<TechInfo>> techInfoMap = TitanResourceUtils.groupTechInfo(techInfos);

        for (Education education : educations) {
            List<TechInfo> sourceTechInfo = techInfoMap.get(education.getIdentifier());
            List<ResCoverage> sourceResCoverage = resCoverageMap.get(education.getIdentifier());
            List<ResourceCategory> resourceCategory = resourceCategoryMap.get(education.getIdentifier());
            titanUpdateDataRepository.updateOneData(education, sourceResCoverage, resourceCategory, sourceTechInfo);
        }

        return educations.size();
    }

    private long importData(Education education, String primaryCategory) {
        if (education == null) {
            return 0L;
        }

        List<Education> educationList = new ArrayList<>();
        educationList.add(education);
        return importData(educationList, primaryCategory);
    }

    private long importData(List<Education> educations, String primaryCategory) {
        if (CollectionUtils.isEmpty(educations)) {
            return 0L;
        }
        Set<String> uuids = new HashSet<String>();
        for (Education education : educations) {
            uuids.add(education.getIdentifier());
        }

        List<ResCoverage> resCoverageList = coverageDao.queryCoverageByResource(primaryCategory, uuids);
        Map<String, List<ResCoverage>> resCoverageMap = TitanResourceUtils.groupCoverage(resCoverageList);

        List<String> resourceTypes = new ArrayList<String>();
        resourceTypes.add(primaryCategory);
        List<ResourceCategory> resourceCategoryList = ndResourceDao.queryCategoriesUseHql(resourceTypes, uuids);
        Map<String, List<ResourceCategory>> resourceCategoryMap = TitanResourceUtils.groupCategory(resourceCategoryList);

        List<String> primaryCategorys = new ArrayList<>();
        primaryCategorys.add(primaryCategory);
        List<TechInfo> techInfos = ndResourceDao.queryTechInfosUseHql(primaryCategorys, uuids);
        Map<String, List<TechInfo>> techInfoMap = TitanResourceUtils.groupTechInfo(techInfos);

        for (Education education : educations) {
            List<TechInfo> sourceTechInfo = techInfoMap.get(education.getIdentifier());
            List<ResCoverage> sourceResCoverage = resCoverageMap.get(education.getIdentifier());
            List<ResourceCategory> resourceCategory = resourceCategoryMap.get(education.getIdentifier());
            boolean success = titanImportRepository.importOneData(education, sourceResCoverage, resourceCategory, sourceTechInfo);
            if (!success){
                titanRepositoryUtils.titanSync4MysqlAdd(TitanSyncType.IMPORT_DATA_ERROR,
                        education.getPrimaryCategory(), education.getIdentifier(),999);
            }
        }

        return educations.size();
    }

    public abstract class AbstractPageQueryStatistical {
        private ResourceRepository resourceRepository;
        private String dbType;

        public String getDbType() {
            return dbType;
        }

        public AbstractPageQueryStatistical(String dbType) {
            this.dbType = dbType;
            if (CommonServiceHelper.isQuestionDb(dbType)) {
                resourceRepository = resourceStatistical4QuestionDBRepository;
            } else {
                resourceRepository = resourceStatisticalRepository;
            }
        }

        public long pageQueryStatistical() {
            String fieldName = "identifier";

            long indexNum = 0;
            // 分页
            int page = 0;
            int row = 500;
            @SuppressWarnings("rawtypes")
            Page resourcePage = new PageImpl(new ArrayList());
            ;
            @SuppressWarnings("rawtypes")
            List entitylist = null;

            List<Item<? extends Object>> items = new ArrayList<>();

            Sort sort = new Sort(Direction.ASC, fieldName);
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
                    List<ResourceStatistical> resourceStatisticals = new ArrayList<ResourceStatistical>();
                    for (Object object : entitylist) {
                        ResourceStatistical statistical = (ResourceStatistical) object;
                        resourceStatisticals.add(statistical);
                    }
                    if (entitylist.size() == 0) {
                        continue;
                    }
                    method(resourceStatisticals);
                    LOG.info("import relation:totalPage:{}  page:{}", resourcePage.getTotalPages(), page);
                } catch (Exception e) {
                    LOG.error(e.getLocalizedMessage());
                }
                setStatisticParam("relations", resourcePage.getTotalPages(), page);
            } while (++page < resourcePage.getTotalPages());

            return indexNum;
        }

        public abstract void method(List<ResourceStatistical> resourceStatisticals);
    }

    private class ImportPageQueryStatistical extends AbstractPageQueryStatistical {

        public ImportPageQueryStatistical(String dbType) {
            super(dbType);
        }

        @Override
        public void method(List<ResourceStatistical> resourceStatisticals) {
            titanImportRepository.importStatistical(getAllExistStaistical(resourceStatisticals));
        }

        private List<ResourceStatistical> getAllExistStaistical(List<ResourceStatistical> resourceStatisticalList) {

            StringBuffer inSql = new StringBuffer();
            Set<String> ids = new HashSet<>();
            for (ResourceStatistical statistical : resourceStatisticalList) {
                ids.add(statistical.getResource());
            }

            if (CollectionUtils.isEmpty(ids)) {
                return new ArrayList<>();
            }

            appendSqlInScript(inSql, ids);

            String sql = "select identifier from ndresource where identifier IN (" + inSql + ")";

            List<String> resultId = new ArrayList<String>();

            if (CommonServiceHelper.isQuestionDb(getDbType())) {
                resultId = questionJdbcTemplate.queryForList(sql, String.class);
            } else {
                resultId = defaultJdbcTemplate.queryForList(sql, String.class);
            }

            List<ResourceStatistical> result = new ArrayList<>();
            for (ResourceStatistical statistical : resourceStatisticalList) {
                if (resultId.contains(statistical.getResource())) {
                    result.add(statistical);
                }
            }

            return result;
        }
    }

    class ImportStatisticalPageQuery extends AbstractPageQuery {

        @Override
        long operate(List<Education> educations, String primaryCategory) {
            Set<String> uuids = new HashSet<String>();
            for (Education education : educations) {
                uuids.add(education.getIdentifier());
            }
            List<String> types = new ArrayList<>();
            types.add(primaryCategory);

            List<ResourceStatistical> statisticalList = ndResourceDao.queryStatisticalUseHql(types, uuids);

            Map<String, List<ResourceStatistical>> stringListMap = TitanResourceUtils.groupStatistical(statisticalList);
            for (List<ResourceStatistical> statisticals : stringListMap.values()) {
                titanImportRepository.importStatistical(statisticals);
            }
            return 0;
        }
    }


    abstract private class TimeTaskPageQuery {

        private String primaryCategory = null;
        private Integer page = 0;
        private Integer totalPage = null;
        private List<String> primaryCategorys = null;
        private Iterator<String> iterator = null;

        public TimeTaskPageQuery(Integer page, String type) {
            primaryCategorys = new LinkedList<>();
            primaryCategorys.add("chapters");
            primaryCategorys.addAll(ResourceTypeSupport.getAllValidEsResourceTypeList());
            iterator = primaryCategorys.iterator();
            //把遍历器遍历到指定的位置
            if (type != null && !type.equals("")) {
                while (iterator.hasNext()) {
                    String next = iterator.next();
                    if (type.equals(next)) {
                        break;
                    }
                }
            }

            this.page = page;
            totalPage = 10000;
            primaryCategory = type;
        }

        public void schedule() {
            while (true) {
                if (isWeek() || !isWeek() && isScheduleTime()) {
                    //更新分页条件
                    if ((primaryCategory == null || totalPage == null || page > totalPage) && iterator.hasNext()) {
                        primaryCategory = iterator.next();
                        page = 0;
                    }

                    if (page > totalPage && !iterator.hasNext()) {
                        LOG.info("数导入完成");
                        break;
                    }

                    LOG.info("importing... primaryCategory:{} totalPage:{} page:{}", primaryCategory, totalPage, page);

                    totalPage = pageQuery(primaryCategory, page);
                    page++;
                } else {
                    try {
                        LOG.info("sleeping... 执行时间:周末、21:00-5:00  primaryCategory:{} totalPage:{} page:{}",
                                primaryCategory, totalPage, page);
                        Thread.sleep(60000);
                    } catch (InterruptedException e) {
                        LOG.error(e.getLocalizedMessage());
                    }
                }
            }
        }

        private Integer pageQuery(String primaryCategory, Integer page) {
            String fieldName = "dblastUpdate";

            int row = 500;
            EspRepository<?> espRepository = ServicesManager.get(primaryCategory);
            @SuppressWarnings("rawtypes")
            Page resourcePage = null;
            @SuppressWarnings("rawtypes")
            List entitylist = null;

            List<Item<? extends Object>> items = new ArrayList<>();

            Item<String> resourceTypeItem = new Item<String>();
            resourceTypeItem.setKey("primaryCategory");
            resourceTypeItem.setComparsionOperator(ComparsionOperator.EQ);
            resourceTypeItem.setLogicalOperator(LogicalOperator.AND);
            resourceTypeItem.setValue(ValueUtils.newValue(primaryCategory));
            items.add(resourceTypeItem);

            Sort sort = new Sort(Direction.ASC, fieldName);
            Pageable pageable = new PageRequest(page, row, sort);

            try {
                resourcePage = espRepository.findByItems(items, pageable);
                if (resourcePage == null) {
                    return null;
                }
                entitylist = resourcePage.getContent();
                if (entitylist == null) {
                    return resourcePage.getTotalPages();
                }
                List<Education> resources = new ArrayList<Education>();
                for (Object object : entitylist) {
                    Education education = (Education) object;
                    resources.add(education);
                }
                if (entitylist.size() == 0) {
                    return resourcePage.getTotalPages();
                }

                importDataOperate(resources, primaryCategory);

            } catch (Exception e) {
                LOG.error(e.getMessage());
            }
            if (resourcePage == null) {
                return null;
            }
            setStatisticParam(primaryCategory, resourcePage.getTotalPages(), page);
            return resourcePage.getTotalPages();
        }

        private boolean isWeek() {
            int week = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
            if (week == 1 || week == 7) {
                return true;
            }

            return true;
        }

        private boolean isScheduleTime() {
            int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            if (hour < 5 || hour > 20) {
                return true;
            }
            return false;
        }

        abstract long importDataOperate(List<Education> educations, String primaryCategory);
    }

    public class TimeTaskPageQuery4Import extends TimeTaskPageQuery {

        public TimeTaskPageQuery4Import(Integer page, String type) {
            super(page, type);
        }

        @Override
        long importDataOperate(List<Education> educations, String primaryCategory) {
            return importData(educations, primaryCategory);
        }
    }

    public class TimeTaskPageQuery4Repair extends TimeTaskPageQuery {

        public TimeTaskPageQuery4Repair(Integer page, String type) {
            super(page, type);
        }

        @Override
        long importDataOperate(List<Education> educations, String primaryCategory) {
            return repairData(educations, primaryCategory);
        }
    }

    private class TimeTaskPageQuery4Update {

        private String primaryCategory = null;
        private Integer page = 0;
        private Integer totalPage = null;
        private List<String> primaryCategorys = null;
        private Iterator<String> iterator = null;

        public TimeTaskPageQuery4Update(Integer page, String type) {
            primaryCategorys = new LinkedList<>();
            primaryCategorys.add("chapters");
            primaryCategorys.addAll(ResourceTypeSupport.getAllValidEsResourceTypeList());
            iterator = primaryCategorys.iterator();
            //把遍历器遍历到指定的位置
            if (type != null && !type.equals("")) {
                while (iterator.hasNext()) {
                    String next = iterator.next();
                    if (type.equals(next)) {
                        break;
                    }
                }
            }

            this.page = page;
            totalPage = 10000;
            primaryCategory = type;
        }

        public void schedule() {
            while (true) {
                if (isWeek() || !isWeek() && isScheduleTime()) {
                    //更新分页条件
                    if ((primaryCategory == null || totalPage == null || page > totalPage) && iterator.hasNext()) {
                        primaryCategory = iterator.next();
                        page = 0;
                    }

                    if (page > totalPage && !iterator.hasNext()) {
                        LOG.info("数导入完成");
                        break;
                    }

                    LOG.info("当前执行--primaryCategory:{};totalPage:{};page:{}", primaryCategory, totalPage, page);
                    totalPage = pageQuery(primaryCategory, page);
                    page++;
                } else {
                    try {
                        LOG.info("sleep");
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        LOG.error(e.getLocalizedMessage());
                    }
                }
            }
        }

        private Integer pageQuery(String primaryCategory, Integer page) {
            String fieldName = "dblastUpdate";

            int row = 500;
            EspRepository<?> espRepository = ServicesManager.get(primaryCategory);
            @SuppressWarnings("rawtypes")
            Page resourcePage = null;
            @SuppressWarnings("rawtypes")
            List entitylist = null;

            List<Item<? extends Object>> items = new ArrayList<>();

            Item<String> resourceTypeItem = new Item<String>();
            resourceTypeItem.setKey("primaryCategory");
            resourceTypeItem.setComparsionOperator(ComparsionOperator.EQ);
            resourceTypeItem.setLogicalOperator(LogicalOperator.AND);
            resourceTypeItem.setValue(ValueUtils.newValue(primaryCategory));
            items.add(resourceTypeItem);

            Sort sort = new Sort(Direction.ASC, fieldName);
            Pageable pageable = new PageRequest(page, row, sort);

            try {
                resourcePage = espRepository.findByItems(items, pageable);
                if (resourcePage == null) {
                    return 0;
                }
                entitylist = resourcePage.getContent();
                if (entitylist == null) {
                    return resourcePage.getTotalPages();
                }
                List<Education> resources = new ArrayList<Education>();
                for (Object object : entitylist) {
                    Education education = (Education) object;
                    resources.add(education);
                }
                if (entitylist.size() == 0) {
                    return resourcePage.getTotalPages();
                }

                importDataOperate4update(resources, primaryCategory);

            } catch (Exception e) {
                LOG.error(e.getMessage());
            }
            if (resourcePage == null) {
                return 0;
            }
            setStatisticParam(primaryCategory, resourcePage.getTotalPages(), page);

            return resourcePage.getTotalPages();
        }

        private boolean isWeek() {
            int week = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
            if (week == 1 || week == 7) {
                return true;
            }

            return true;
        }

        private boolean isScheduleTime() {
            int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            if (hour < 5 || hour > 20) {
                return true;
            }
            return false;
        }
    }

    private void importDataOperate4update(List<Education> educations, String primaryCategory) {
        Set<String> uuids = new HashSet<String>();
        for (Education education : educations) {
            uuids.add(education.getIdentifier());
        }
        //后去coverage、category
        List<ResCoverage> resCoverageList = coverageDao.queryCoverageByResource(primaryCategory, uuids);
        List<String> resourceTypes = new ArrayList<String>();
        resourceTypes.add(primaryCategory);
        List<ResourceCategory> resourceRepositoryList = ndResourceDao.queryCategoriesUseHql(resourceTypes, uuids);

        Map<String, List<ResCoverage>> coverageMap = TitanResourceUtils.groupCoverage(resCoverageList);
        Map<String, List<ResourceCategory>> categoryMap = TitanResourceUtils.groupCategory(resourceRepositoryList);

        //保存数据
        for (Education education : educations) {
            String uuid = education.getIdentifier();
            Set<String> resCoverages = new HashSet<>();

            List<ResCoverage> tempCoverageList = coverageMap.get(uuid);
            List<ResourceCategory> tempCategoryList = categoryMap.get(uuid);
            if (CollectionUtils.isNotEmpty(tempCoverageList)) {
                for (ResCoverage resCoverage : tempCoverageList) {
                    resCoverages.addAll(TitanScritpUtils.getAllResourceCoverage(resCoverage, education.getStatus()));
                }
            }

            Set<String> paths = new HashSet<>(TitanResourceUtils.distinctCategoryPath(tempCategoryList));
            Set<String> categoryCodes = new HashSet<>(TitanResourceUtils.distinctCategoryCode(tempCategoryList));

            String dropScript = "g.V().has(primaryCategory,'identifier',identifier)." +
                    "properties('search_coverage','search_code','search_path','search_path_string','search_code_string','search_coverage_string').drop()";
            Map<String, Object> dropParam = new HashMap<>();
            dropParam.put("primaryCategory", primaryCategory);
            dropParam.put("identifier", education.getIdentifier());

            StringBuffer script = new StringBuffer("g.V().has(primaryCategory,'identifier',identifier).property('primary_category',primaryCategory)");
            Map<String, Object> param = new HashMap<>();
            param.put("primaryCategory", primaryCategory);
            param.put("identifier", education.getIdentifier());

            addSetProperty("search_coverage", resCoverages, script, param);
            addSetProperty("search_code", categoryCodes, script, param);
            addSetProperty("search_path", paths, script, param);

            if (CollectionUtils.isNotEmpty(paths)) {
                String searchPathString = StringUtils.join(paths, ",").toLowerCase();
                script.append(".property('search_path_string',searchPathString)");
                param.put("searchPathString", searchPathString);

            }

            if (CollectionUtils.isNotEmpty(categoryCodes)) {
                String searchCodeString = StringUtils.join(categoryCodes, ",").toLowerCase();
                script.append(".property('search_code_string',searchCodeString)");
                param.put("searchCodeString", searchCodeString);
            }

            if (CollectionUtils.isNotEmpty(resCoverages)) {
                String searchCoverageString = StringUtils.join(resCoverages, ",").toLowerCase();
                script.append(".property('search_coverage_string',searchCoverageString)");
                param.put("searchCoverageString", searchCoverageString);
            }


            try {
                titanCommonRepository.executeScript(dropScript, dropParam);
                titanCommonRepository.executeScript(script.toString(), param);
            } catch (Exception e) {
                LOG.error("titan_repository error:{}", e.getMessage());
            }
        }
    }

    private void addSetProperty(String fieldName,
                                Set<String> values, StringBuffer script, Map<String, Object> param) {
        if (values == null || values.size() == 0) {
            return;
        }
        int index = 0;
        for (String value : values) {
            String paramKey = fieldName + index;
            index++;
            script.append(".property(set,'").append(fieldName).append("',").append(paramKey).append(")");
            param.put(paramKey, value);
        }
    }

    private void setStatisticParam(String primaryCategory, Integer totalPage, Integer page) {
        s_primaryCategory = primaryCategory;
        s_totalPage = totalPage;
        s_page = page;
    }

    public List<ResourceRelation> getAllExistRelation(List<ResourceRelation> resourceRelationList) {
        StringBuffer inSql = new StringBuffer();
        StringBuffer inSqlChpater = new StringBuffer();
        Set<String> ids = new HashSet<>();
        Set<String> chaptersIds = new HashSet<String>();
        for (ResourceRelation relation : resourceRelationList) {
            if (ResourceNdCode.chapters.toString().equals(relation.getResType())
                    || ResourceNdCode.knowledges.toString().equals(relation.getResType())) {
                chaptersIds.add(relation.getSourceUuid());
            } else {
                ids.add(relation.getSourceUuid());
            }

            if (ResourceNdCode.chapters.toString().equals(relation.getResourceTargetType())
                    || ResourceNdCode.knowledges.toString().equals(relation.getResourceTargetType())) {
                chaptersIds.add(relation.getTarget());
            } else {
                ids.add(relation.getTarget());
            }
        }

        appendSqlInScript(inSql, ids);
        appendSqlInScript(inSqlChpater, chaptersIds);

        String sql = "select identifier from ndresource where identifier IN (" + inSql + ")";
        String sqlChapter = "select identifier from chapters where identifier IN (" + inSqlChpater + ")";

        List<String> questionsResult = new ArrayList<String>();
        List<String> resultDefault = new ArrayList<String>();
        List<String> resultDefaultChapter = new ArrayList<String>();

        if (CollectionUtils.isNotEmpty(ids)) {
            questionsResult = questionJdbcTemplate.queryForList(sql, String.class);
            resultDefault = defaultJdbcTemplate.queryForList(sql, String.class);
        }
        if (CollectionUtils.isNotEmpty(chaptersIds)) {
            resultDefaultChapter = defaultJdbcTemplate.queryForList(sqlChapter, String.class);
        }

        List<String> existIds = new ArrayList<>();
        existIds.addAll(questionsResult);
        existIds.addAll(resultDefault);
        existIds.addAll(resultDefaultChapter);
        List<ResourceRelation> resultRelation = new ArrayList<>();
        for (ResourceRelation relation : resourceRelationList) {
            if (existIds.contains(relation.getSourceUuid()) && existIds.contains(relation.getTarget())) {
                resultRelation.add(relation);
            }
        }

        return resultRelation;
    }

    private void appendSqlInScript(StringBuffer sql, Set<String> values) {
        if (CollectionUtils.isEmpty(values)) {
            return;
        }

        int index = 0;
        for (String id : values) {
            if (index == 0) {
                sql.append("'").append(id).append("'");
            } else {
                sql.append(",").append("'").append(id).append("'");
            }
            index++;
        }
    }

}
