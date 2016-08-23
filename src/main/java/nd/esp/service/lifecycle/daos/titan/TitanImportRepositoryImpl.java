package nd.esp.service.lifecycle.daos.titan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nd.esp.service.lifecycle.daos.titan.inter.TitanCommonRepository;
import nd.esp.service.lifecycle.daos.titan.inter.TitanImportRepository;
import nd.esp.service.lifecycle.daos.titan.inter.TitanRelationRepository;
import nd.esp.service.lifecycle.daos.titan.inter.TitanRepositoryUtils;
import nd.esp.service.lifecycle.educommon.models.ResourceModel;
import nd.esp.service.lifecycle.educommon.services.NDResourceService;
import nd.esp.service.lifecycle.educommon.services.titanV07.NDResourceTitanService;
import nd.esp.service.lifecycle.educommon.vos.constant.IncludesConstant;
import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.model.ResCoverage;
import nd.esp.service.lifecycle.repository.model.ResourceCategory;
import nd.esp.service.lifecycle.repository.model.ResourceRelation;
import nd.esp.service.lifecycle.repository.model.TechInfo;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.busi.titan.TitanResourceUtils;
import nd.esp.service.lifecycle.support.busi.titan.TitanSyncType;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.TitanScritpUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * Created by liuran on 2016/7/26.
 */
@Repository
public class TitanImportRepositoryImpl implements TitanImportRepository{
    private static final Logger LOG = LoggerFactory
            .getLogger(TitanImportRepositoryImpl.class);
    @Autowired
    private TitanCommonRepository titanCommonRepository;

    @Autowired
    private TitanRelationRepository titanRelationRepository;

    @Autowired
    private TitanRepositoryUtils titanRepositoryUtils;

    @Autowired
    private NDResourceTitanService ndResourceTitanService;

    @Autowired
    private NDResourceService ndResourceService;

    @Override
    /**
     * @return false 导入数据失败 ；true 导入成功
     * */
    public boolean importOneData(Education education, List<ResCoverage> resCoverageList, List<ResourceCategory> resourceCategoryList, List<TechInfo> techInfos) {

        List<ResCoverage> coverageList = TitanResourceUtils.distinctCoverage(resCoverageList).get(education.getIdentifier());
        List<ResourceCategory> categoryList = TitanResourceUtils.distinctCategory(resourceCategoryList).get(education.getIdentifier());
        List<TechInfo> techInfoList = TitanResourceUtils.distinctTechInfo(techInfos).get(education.getIdentifier());
        List<String> categoryPathList = TitanResourceUtils.distinctCategoryPath(resourceCategoryList);

        //生成导入资源的脚本
        Map<String, Object> result = TitanScritpUtils.buildScript(education,coverageList,categoryList,techInfoList,categoryPathList);
        //校验addVertex中的参数个数过多，个数超过250返回为null
        if(CollectionUtils.isEmpty(result)){
            //TODO 这种错误情况保存到数据库中，返回true
            titanSync(education);
        } else {
            Long educationId = null;
            try {
                String script = result.get("script").toString();
                Map<String, Object> param = (Map<String, Object>) result.get("param");
                educationId = titanCommonRepository.executeScriptUniqueLong(script, param);
            } catch (Exception e) {
                LOG.error("titanImportErrorData:{}" ,education.getIdentifier());
                return  false;
            }
            if(educationId == null){
                return false;
            }
        }

        return true;
    }

    /**
     * 导入关系，使用创建关系脚本
     * */
    @Override
    public boolean batchImportRelation(List<ResourceRelation> resourceRelation) {
        titanRelationRepository.batchAdd4Import(resourceRelation);
        return false;
    }

    /**
     * 检查资源在titan中是否存在
     * */
    @Override
    public boolean checkResourceExistInTitan(Education education) {
        String script = "g.V().has(primaryCategory,'identifier',identifier).count();";
        Map<String, Object> param = new HashMap<>();
        param.put("primaryCategory",education.getPrimaryCategory());
        param.put("identifier",education.getIdentifier());

        Long count = null;
        try {
            count = titanCommonRepository.executeScriptUniqueLong(script, param);
        } catch (Exception e) {
           LOG.info(e.getLocalizedMessage());
        }

        if(count == null || count == 0){

            titanSync(TitanSyncType.CHECK_NOT_EXIST,education.getPrimaryCategory(),education.getIdentifier());
        } else if(count > 1){
            titanSync(TitanSyncType.CHECK_REPEAT,education.getPrimaryCategory(),education.getIdentifier());
        } else {
            return true;
        }

        return false;
    }

    class Builder{
        
        StringBuilder sb = new StringBuilder();
        
        public Builder(String head){
            sb.append(head);
        }
        
        public Builder has(String key, String value){
            sb.append(".has(").append("'").append(key).append("'").append(",").append(value).append(")");
            return this;
        }
        
        public Builder hasLabel(String key, String value){
            sb.append(".hasLabel(").append("'").append(key).append("'").append(",").append(value).append(")");
            return this;
        }
        
        public Builder outE(){
            sb.append(".outE()");
            return this;
        }
        
        public Builder inV(){
            sb.append(".inV()");
            return this;
        }
        
        public String builder(){
            return sb.toString();
        }

        public Builder id(){
            sb.append(".id()");
            return this;
        }
        
        public Builder count(){
            sb.append(".count()");
            return this;
        }
        
        @Override
        public String toString() {
            return "Builder [sb=" + sb + "]";
        }
    }
    
    public void checkTechInfos(Education education,List<ResourceCategory> resourceCategoryList){
        String[] techInfoField = new String[]{"description", "identifier", "ti_entry", "ti_format", "ti_location", "ti_md5", "ti_requirements", "ti_secure_key", "ti_size","ti_title"};
        
    }
    
    public void checkNdResource(Education education){
//        g.V().has('identifier', 'xxx')
        String[] ndResourceField = new String[]{"cr_author", "cr_description", "cr_has_right", "cr_right", "cr_right_end_date", "cr_right_start_date", "custom_properties", "description"
                , "edu_age_range", "edu_description", "edu_difficulty", "edu_end_user_type", "edu_interactivity", "edu_interactivity_level", "edu_language", "edu_learning_time", 
                "edu_semantic_density", "keywords", "language", "lc_create_time", "lc_creator", "lc_enalbe", "lc_last_update", "lc_provider", "lc_provider_mode"
                , "lc_provider_source", "lc_publisher", "lc_status", "lc_version", "preview", "tags", "title", "search_code", "search_coverage", "search_path"};
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("primaryCategory", education.getPrimaryCategory());
        paramMap.put("identifier",education.getIdentifier());
        
        List<Object> educationField = new ArrayList<Object>();
        educationField.add(education.getAuthor());
        educationField.add(education.getCrDescription());
        educationField.add(education.getHasRight());
        educationField.add(education.getCrRight());
        educationField.add(education.getRightEndDate());
        educationField.add(education.getRightStartDate());
        educationField.add(education.getCustomProperties());
        educationField.add(education.getDescription());
        educationField.add(education.getAgeRange());
        educationField.add(education.getEduDescription());
        educationField.add(education.getDifficulty());
        educationField.add(education.getEndUserType());
        educationField.add(education.getInteractivity());
        educationField.add(education.getInteractivityLevel());
        educationField.add(education.getEduLanguage());
        educationField.add(education.getLearningTime());
        educationField.add(education.getSemanticDensity());
        educationField.add(education.getKeywords());
        educationField.add(education.getLanguage());
        educationField.add(education.getCreateTime());
        educationField.add(education.getCreator());
        educationField.add(education.getEnable());
        educationField.add(education.getLastUpdate());
        educationField.add(education.getProvider());
        educationField.add(education.getProviderMode());
        educationField.add(education.getProviderSource());
        educationField.add(education.getPublisher());
        educationField.add(education.getStatus());
        educationField.add(education.getVersion());
        educationField.add(education.getPreview());
        educationField.add(education.getTags());
        educationField.add(education.getTitle());
//        educationField.add(education.getcod);
//        educationField.add(education.getTags());
//        educationField.add(education.getTags());
        
    }
    
    public void checkResourceAllInTitan2(Education education, List<ResCoverage> resCoverageList, List<ResourceCategory> resourceCategoryList, List<TechInfo> techInfos, List<ResourceRelation> resourceRelationList) {
        checkCategories(education, resourceCategoryList);
    }
    
//    时间 2016-08-17
    private long criticalDate = 1471363200000L;

    String[] categoryField = new String[]{"cg_taxonpath", "cg_taxoncode", "cg_taxonname", "cg_category_code", "cg_short_name", "cg_category_name"};
    String hasCategoryCode = "has_category_code";
    public void checkCategories(Education education,List<ResourceCategory> resourceCategoryList){
        
        String baseScript = "g.V().has(primaryCategory, 'identifier', identifier)";
        
        Map<String, Object> paramMap = initParamMap(education, hasCategoryCode);
        for (int index =0 ;index <resourceCategoryList.size() ;index++){
            ResourceCategory resourceCategory = resourceCategoryList.get(index);
            List<Object> resourceCategoryPartField = fillResourceCategoryPartField(resourceCategory);
            fillParamMap(categoryField, paramMap, resourceCategoryPartField);
            
            Builder builder = generateCheckResourceCategoryScript(categoryField, baseScript, hasCategoryCode,
                    resourceCategoryPartField);
    
            Long count = 0L;
            try {
                count = titanCommonRepository.executeScriptUniqueLong(builder.count().builder(), paramMap);
                System.out.println(builder.builder());
                System.out.println(paramMap);
                System.out.println(count);
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage());
            }
            if (count == null) {
                LOG.error("与 titan 的连接断开或查询脚本发生异常，脚本：{}", builder.builder());
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        LifeCircleErrorMessageMapper.CheckDuplicateIdFail.getCode(),
                        "与 titan 的连接断开或查询脚本发生异常" + builder.builder());
            }
            if (count == 0) {
                LOG.info("count == 0, primarycategory:{}, education identifier:{}", education.getPrimaryCategory(), education.getIdentifier());
                titanSync(TitanSyncType.CHECK_NOT_EXIST,education.getPrimaryCategory(),education.getIdentifier());
            } else if(count > 1){
                LOG.info("count > 1, primarycategory:{}, education identifier:{}", education.getPrimaryCategory(), education.getIdentifier());
                titanSync(TitanSyncType.CHECK_REPEAT,education.getPrimaryCategory(),education.getIdentifier());
            }
            
        }
//        if (beginDate.longValue() >= criticalDate) {
//            checkCategoryGECriticalDate(education, resourceCategoryList, baseScript);
//        }else{
//            checkCategoryLTCriticalDate(education, resourceCategoryList, baseScript);
//        }
        
    }

    private Multimap<String, ResourceCategory> toResourceCategoryMultimap(List<ResourceCategory> categories){
        Multimap<String, ResourceCategory> multimap = ArrayListMultimap.create();
        for (ResourceCategory resourceCategory : categories) {
            multimap.put(resourceCategory.getTaxoncode(), resourceCategory);
        }
        return multimap;
    }
    
    private void checkCategoriesLTCriticalDate(Education education, List<ResourceCategory> resourceCategoryList, String baseScript) {
        
        Multimap<String, ResourceCategory> resourceCategoryMap = toResourceCategoryMultimap(resourceCategoryList);
        Set<String> keySet = resourceCategoryMap.keySet();
        int size = keySet.size();
        String[] arr = keySet.toArray(new String[size]);
        for (int i = 0; i < size; i++) {
            Map<String, Object> paramMap = initParamMap(education, hasCategoryCode);
            Long count = 0L;
            Collection<ResourceCategory> resourceCategories = resourceCategoryMap.get(arr[i]);
            for (ResourceCategory resourceCategory : resourceCategories) {
                List<Object> resourceCategoryPartField = fillResourceCategoryPartField(resourceCategory);
                fillParamMap(categoryField, paramMap, resourceCategoryPartField);
                
                Builder builder = generateCheckResourceCategoryScript(categoryField, baseScript, hasCategoryCode,
                        resourceCategoryPartField);
                
                try {
                    Long scriptReturnCount = titanCommonRepository.executeScriptUniqueLong(builder.count().builder(), paramMap);
                    if (scriptReturnCount == null) {
                        count = null;
                    }else{
                        count += scriptReturnCount;
                    }
                    System.out.println(builder.builder());
                    System.out.println(paramMap);
                    System.out.println(count);
                } catch (Exception e) {
                    LOG.error(e.getLocalizedMessage());
                }
            }
//            count = 1 || 2 为正常
            if (count == null) {
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        LifeCircleErrorMessageMapper.CheckDuplicateIdFail.getCode(),
                        "与 titan 的连接断开或查询脚本发生异常");
            }
            if (count == 0) {
                titanSync(TitanSyncType.CHECK_NOT_EXIST,education.getPrimaryCategory(),education.getIdentifier());
            } else if(count >= 3){
                titanSync(TitanSyncType.CHECK_REPEAT,education.getPrimaryCategory(),education.getIdentifier());
            }
        }
    }

    private Builder generateCheckResourceCategoryScript(String[] categoryField, String baseScript,
            String hasCategoryCode, List<Object> resourceCategoryPartField) {
        Builder builder = new Builder(baseScript).outE().hasLabel(hasCategoryCode, hasCategoryCode);
        for (int j = 0; j < categoryField.length; j++) {
            if (resourceCategoryPartField.get(j) != null) {
                // builder = g.V().has(primaryCategory, 'identifier', identifier).outE().hasLabel('has_category_code',has_category_code).has('cg_taxonpath',cg_taxonpath).has('cg_taxoncode',cg_taxoncode).has('cg_taxonname',cg_taxonname).has('cg_category_code',cg_category_code).has('cg_short_name',cg_short_name).has('cg_category_name',cg_category_name)
                builder.has(categoryField[j], categoryField[j]);
            }
        }
        return builder;
    }

    private void fillParamMap(String[] categoryField, Map<String, Object> paramMap,
            List<Object> resourceCategoryPartField) {
        for (int j = 0; j < categoryField.length; j++) {
            if (resourceCategoryPartField.get(j) != null) {
                paramMap.put(categoryField[j], resourceCategoryPartField.get(j));
            }
        }
    }

    private Map<String, Object> initParamMap(Education education, String hasCategoryCode) {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("primaryCategory", education.getPrimaryCategory());
        paramMap.put("identifier",education.getIdentifier());
        
        paramMap.put(hasCategoryCode, hasCategoryCode);
        return paramMap;
    }

    private List<Object> fillResourceCategoryPartField(ResourceCategory resourceCategory) {
        List<Object> resourceCategoryPartField = new ArrayList<Object>();
        resourceCategoryPartField.add(resourceCategory.getTaxonpath());
        resourceCategoryPartField.add(resourceCategory.getTaxoncode());
        resourceCategoryPartField.add(resourceCategory.getTaxonname());
        resourceCategoryPartField.add(resourceCategory.getCategoryCode());
        resourceCategoryPartField.add(resourceCategory.getShortName());
        resourceCategoryPartField.add(resourceCategory.getCategoryName());
        return resourceCategoryPartField;
    }

    private void checkCategoriesGECriticalDate(Education education, List<ResourceCategory> resourceCategoryList, String baseScript) {
        Map<String, Object> paramMap = initParamMap(education, hasCategoryCode);
        for (int index =0 ;index <resourceCategoryList.size() ;index++){
            ResourceCategory resourceCategory = resourceCategoryList.get(index);
            List<Object> resourceCategoryPartField = fillResourceCategoryPartField(resourceCategory);
            fillParamMap(categoryField, paramMap, resourceCategoryPartField);
            
            Builder builder = generateCheckResourceCategoryScript(categoryField, baseScript, hasCategoryCode,
                    resourceCategoryPartField);
    
            Long count = 0L;
            try {
                count = titanCommonRepository.executeScriptUniqueLong(builder.count().builder(), paramMap);
                System.out.println(builder.builder());
                System.out.println(paramMap);
                System.out.println(count);
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage());
            }
            if (count == null) {
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        LifeCircleErrorMessageMapper.CheckDuplicateIdFail.getCode(),
                        "与 titan 的连接断开或查询脚本发生异常" + builder.builder());
            }
            if (count == 0) {
                titanSync(TitanSyncType.CHECK_NOT_EXIST,education.getPrimaryCategory(),education.getIdentifier());
            } else if(count > 1){
                titanSync(TitanSyncType.CHECK_REPEAT,education.getPrimaryCategory(),education.getIdentifier());
            }
            
        }
    }
    
    /**
     * 检查资源的coverage、techInfo、category是否存在
     * 1、校验资源是否存在  2、检验关系的条数是否合理  3、校验mysql对应的关系是否存在
     * */
    @Override
    //FIXME 现在边上都有ID，后面通过ID对边进行检测
    public boolean checkResourceAllInTitan(Education education, List<ResCoverage> resCoverageList, List<ResourceCategory> resourceCategoryList, List<TechInfo> techInfos, List<ResourceRelation> resourceRelationList) {
        String baseScript = "g.V().has(primaryCategory,'identifier',identifier)";

        List<String> categoryPathSet = TitanResourceUtils.distinctCategoryPath(resourceCategoryList);
        List<ResCoverage> coverageList = TitanResourceUtils.distinctCoverage(resCoverageList).get(education.getIdentifier());
        List<ResourceCategory> categoryList = TitanResourceUtils.distinctCategory(resourceCategoryList).get(education.getIdentifier());
        List<TechInfo> techInfoList = TitanResourceUtils.distinctTechInfo(techInfos).get(education.getIdentifier());

        //产生资源校验的脚本
        List<String> innerScriptList = new ArrayList<>();
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("primaryCategory", education.getPrimaryCategory());
        paramMap.put("identifier",education.getIdentifier());
        for(int index = 0; index < coverageList.size() ;index ++){
            ResCoverage coverage = coverageList.get(index);
            String targetType = "target_type"+index;
            String strategy = "strategy" + index;
            String target = "target" +index;
            String script =  "outE().hasLabel('has_coverage').inV().has('target_type',"+targetType+")" +
                    ".has('strategy',"+strategy+").has('target',"+target+")";
            paramMap.put(targetType, coverage.getTargetType());
            paramMap.put(strategy, coverage.getStrategy());
            paramMap.put(target, coverage.getTarget());
            innerScriptList.add(script);
        }

        for (int index =0 ;index < techInfoList.size() ;index++){
            TechInfo techInfo = techInfoList.get(index);
            String techInfoTitle = "techInfoTitle"+index;
            String script = "outE().hasLabel('has_tech_info').inV().has('ti_title',"+techInfoTitle+")";
            paramMap.put(techInfoTitle,techInfo.getTitle());
            innerScriptList.add(script);
        }

        for (int index =0 ;index <categoryList.size() ;index++){
            ResourceCategory resourceCategory = categoryList.get(index);
            String taxoncode = "taxoncode" + index;
            String script = "outE().hasLabel('has_category_code').inV().has('cg_taxoncode',"+taxoncode+")";

            paramMap.put(taxoncode, resourceCategory.getTaxoncode());
            innerScriptList.add(script);
        }

        for(int index =0 ;index <categoryPathSet.size() ;index ++){
            String path = categoryPathSet.get(index);
            String categoryPath = "path" + index;
            String script = "outE().hasLabel('has_categories_path').inV().has('cg_taxonpath',"+categoryPath+")";
            paramMap.put(categoryPath, path);
            innerScriptList.add(script);
        }
        StringBuffer checkAllScript = new StringBuffer(baseScript);
        if(CollectionUtils.isNotEmpty(innerScriptList)){
            checkAllScript.append(".and(");
            for (int i=0 ; i < innerScriptList.size() ;i++){
                if(i == 0){
                    checkAllScript.append(innerScriptList.get(i));
                } else {
                    checkAllScript.append(",").append(innerScriptList.get(i));
                }
            }
            checkAllScript.append(").id()");
        } else {
            checkAllScript.append(".id()");
        }
        Long id = null;
        try {

            id = titanCommonRepository.executeScriptUniqueLong(checkAllScript.toString(), paramMap);
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage());
        }

        //校验资源是否存在
        if(id == null){
            LOG.info("数据校验：数据不存在 primaryCategory:{}  identifier:{}",education.getPrimaryCategory(),education.getIdentifier());
            return false;
        }

        //保证titan里面不会出现多余的数据
        String getEdgeNumber = baseScript + ".outE().or(hasLabel('has_coverage'),hasLabel('has_tech_info')" +
                ",hasLabel('has_category_code'),hasLabel('has_categories_path')).count();";
        Map<String, Object> getEdgeNumberParam = new HashMap<>();
        getEdgeNumberParam.put("primaryCategory", education.getPrimaryCategory());
        getEdgeNumberParam.put("identifier",education.getIdentifier());
        Long edgeCount = null;
        try {
            edgeCount = titanCommonRepository.executeScriptUniqueLong(getEdgeNumber,getEdgeNumberParam);
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage());
        }

        //category在保存的时候没有做去重处理，在导入数据的时候有做去重处理
        if(CollectionUtils.isEmpty(resourceCategoryList)){
            resourceCategoryList = new ArrayList<>();
        }
        Integer edgeCountMysql = coverageList.size() + techInfoList.size() + resourceCategoryList.size() + categoryPathSet.size();
        if (edgeCount == null || edgeCount.intValue() > edgeCountMysql ){
            LOG.info("数据校验：数据有误 primaryCategory:{}  identifier:{}",education.getPrimaryCategory(),education.getIdentifier());
            return false;
        }

        return true;
    }

    @Override
    public boolean checkResourceAllInTitanDetail(Education education, List<ResCoverage> resCoverageList, List<ResourceCategory> resourceCategoryList, List<TechInfo> techInfos, List<ResourceRelation> resourceRelationList) {
        List<String> includes = new ArrayList<>();
        includes.add(IncludesConstant.INCLUDE_CG);
        includes.add(IncludesConstant.INCLUDE_EDU);
        includes.add(IncludesConstant.INCLUDE_TI);
        includes.add(IncludesConstant.INCLUDE_CR);
        includes.add(IncludesConstant.INCLUDE_LC);

        //通过获取详情检查资源的每个字段
        ResourceModel titan = ndResourceService.getDetail(education.getPrimaryCategory(), education.getIdentifier(), includes, true);
        ResourceModel mysql = ndResourceTitanService.getDetail(education.getPrimaryCategory(), education.getIdentifier(), includes, true);
        boolean success = equalModel(titan, mysql);


        //校验mysql关系的条数等于titan边的条数据
        String baseScript = "g.V().has(primaryCategory,'identifier',identifier).outE().or(hasLabel('has_coverage'),hasLabel('has_tech_info')" +
                ",hasLabel('has_category_code'),hasLabel('has_categories_path')).count();";

        //校验coverage数据正确性


        //校验资源关系数据正确性


        //校验章节知识点关系正确性

        return false;
    }


    private void titanSync(Education education){
        titanRepositoryUtils.titanSync4MysqlAdd(TitanSyncType.IMPORT_DATA_ERROR,
                education.getPrimaryCategory(), education.getIdentifier(),999);
    }

    /**
     * 保存异常数据到titan sync表中
     * */
    private void titanSync(TitanSyncType syncType, String parmaryCategory, String identifier){
        titanRepositoryUtils.titanSync4MysqlAdd(syncType, parmaryCategory, identifier,999);
    }

    private boolean equalModel(ResourceModel titan, ResourceModel mysql){
        return true;
    }

    public static void main(String[] args){
        System.out.println(Float.MAX_VALUE);
    }
}
