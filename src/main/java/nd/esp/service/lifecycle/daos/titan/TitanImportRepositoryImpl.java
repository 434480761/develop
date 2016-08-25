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
import nd.esp.service.lifecycle.daos.titan.inter.TitanStatisticalRepository;
import nd.esp.service.lifecycle.educommon.models.ResourceModel;
import nd.esp.service.lifecycle.educommon.services.NDResourceService;
import nd.esp.service.lifecycle.educommon.services.titanV07.NDResourceTitanService;
import nd.esp.service.lifecycle.educommon.vos.constant.IncludesConstant;
import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.model.ResCoverage;
import nd.esp.service.lifecycle.repository.model.ResourceCategory;
import nd.esp.service.lifecycle.repository.model.ResourceRelation;
import nd.esp.service.lifecycle.repository.model.ResourceStatistical;
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

    @Autowired
    private TitanStatisticalRepository titanStatisticalRepository;

    @Override
    /**
     * @return false 导入数据失败 ；true 导入成功
     * */
    public boolean importOneData(Education education, List<ResCoverage> resCoverageList, List<ResourceCategory> resourceCategoryList, List<TechInfo> techInfos) {

        List<ResCoverage> coverageList = TitanResourceUtils.distinctCoverage(resCoverageList);
        List<TechInfo> techInfoList = TitanResourceUtils.distinctTechInfo(techInfos);
        List<String> categoryPathList = TitanResourceUtils.distinctCategoryPath(resourceCategoryList);

        //生成导入资源的脚本
        Map<String, Object> result = TitanScritpUtils.buildScript(education,coverageList,resourceCategoryList,techInfoList,categoryPathList);
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
            	titanSync(education);
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean importStatistical(List<ResourceStatistical> statisticalList) {
//        Map<TitanScritpUtils.KeyWords, Object> result = TitanScritpUtils.buildStatisticalScript(statisticalList);
//        String script = result.get(TitanScritpUtils.KeyWords.script).toString();
//        Map<String, Object> param = (Map<String, Object>) result.get(TitanScritpUtils.KeyWords.params);
//        try {
//            titanCommonRepository.executeScript(script, param);
//        } catch (Exception e) {
//            LOG.info(e.getLocalizedMessage());
//        }
        titanStatisticalRepository.batchAdd4Import(statisticalList);
        return false;
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
        
        public Builder(Builder builder){
            this.sb = new StringBuilder(builder.sb);
        }
        
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
    
    String[] techInfoField = new String[]{"description", "identifier", "ti_entry", "ti_format", "ti_location", "ti_md5", "ti_requirements", "ti_secure_key", "ti_size","ti_title", "ti_printable"};
    String techInfoEdgeLabel = "has_tech_info";
    
    private void checkTechInfoHandle(Education education,List<TechInfo> techInfos){
        String baseScript = new StringBuilder("g.V().has(").append(primaryCategory).append(",'identifier', ").append(educationIdentifier).append(")").toString();
        Builder baseBuilder = new Builder(baseScript).outE().hasLabel(techInfoEdgeLabel, techInfoEdgeLabel);
        checkTechInfos(education, techInfos, baseBuilder);
        baseBuilder = new Builder(baseScript).outE().hasLabel(techInfoEdgeLabel, techInfoEdgeLabel).inV();
        checkTechInfos(education, techInfos, baseBuilder);
    }
    
    private void checkCoverageHandle(Education education,List<ResCoverage> resCoverage){
//        String baseScript = new StringBuilder("g.V().has(").append(primaryCategory).append(",'identifier', ").append(educationIdentifier).append(")").toString();
//        Builder baseBuilder = new Builder(baseScript).outE().hasLabel(coverageEdgeLabel, coverageEdgeLabel);
//        checkResCoverageEdges(education, resCoverage);
//        baseBuilder = new Builder(baseScript).outE().hasLabel(coverageEdgeLabel, coverageEdgeLabel).inV();
//        checkResCoverageEdges(education, resCoverage);
    }
    /**
     * 根据tech_infos 表的title 字段去重， 相同资源的tech_infos 记录，根据title 分组，存在一条即可
     * @param education
     * @param techInfos
     * @since 1.2.6
     * @see
     */
    private void checkTechInfos(Education education,List<TechInfo> techInfos, Builder baseBuilder){
        Multimap<String, TechInfo> techInfoMultiMap = toTechInfoMultimap(techInfos);
        Set<String> keySet = techInfoMultiMap.keySet();
        int size = keySet.size();
        String[] arr = keySet.toArray(new String[size]);
//        String baseScript = new StringBuilder("g.V().has(").append(primaryCategory).append(",'identifier', ").append(educationIdentifier).append(")").toString();
//        Builder baseBuilder = new Builder(baseScript).outE().hasLabel(techInfoEdgeLabel, techInfoEdgeLabel);
        Builder builder = null;
        Map<String, Object> paramMap = null;
        for (int i = 0; i < size; i++) {
            Long count = 0L;
            Collection<TechInfo> teachInfoList = techInfoMultiMap.get(arr[i]);
            for (TechInfo techInfo : teachInfoList) {
                paramMap = initParamMap(education, techInfoEdgeLabel);
                List<Object> techInfoPartField = fillTeachInfoPartField(techInfo);
                fillParamMap(paramMap, techInfoPartField, techInfoField);
                
                builder = generateCheckResourceCategoryScript(baseBuilder, techInfoPartField, techInfoField);
                
                Long tmp = executeScriptUniqueLong(paramMap, builder.count());
                count = techInfoEdgeScriptExecutionException(builder, paramMap, count, tmp);
            }
            saveAbnormalData(education, builder, paramMap, count);
        }
    }

    private void saveAbnormalData(Education education, Builder builder, Map<String, Object> paramMap, Long count) {
        // count = 1 为正常
        if (count == 0) {
            LOG.info("mysql 中数据在titan 中不存在, script:{}, param:{}", builder.builder(), paramMap);
            titanSync(TitanSyncType.CHECK_NOT_EXIST,education.getPrimaryCategory(),education.getIdentifier());
        } else if(count >= 2){
            LOG.info("titan 中数据重复, script:{}, param:{}", builder.builder(), paramMap);
            titanSync(TitanSyncType.CHECK_REPEAT,education.getPrimaryCategory(),education.getIdentifier());
        }
    }

    private Long techInfoEdgeScriptExecutionException(Builder builder, Map<String, Object> paramMap, Long count, Long tmp) {
        if (tmp != null) {
            count += tmp;
        }else {
            LOG.error("查询脚本执行异常, script:{}, param:{}", builder.builder(), paramMap);
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CheckDuplicateIdFail.getCode(),
                    "查询脚本发生异常" + builder.builder());
        }
        return count;
    }
    
    String[] coverageEdgeField = new String[]{"identifier", "strategy", "target", "target_type"};
    String coverageEdgeLabel = "has_coverage";
    private void checkResCoverage(Education education,List<ResCoverage> resCoverages){
        String baseScript = new StringBuilder("g.V().has(").append(primaryCategory).append(",'identifier', ").append(educationIdentifier).append(")").toString();
        Builder baseBuilderEdge = new Builder(baseScript).outE().hasLabel(coverageEdgeLabel, coverageEdgeLabel);
        Multimap<String, ResCoverage> coverageMultiMap = toCoverageMultimap(resCoverages);
        Set<String> keySet = coverageMultiMap.keySet();
        int size = keySet.size();
        String[] arr = keySet.toArray(new String[size]);
        Builder builder = null;
        Map<String, Object> paramMap = null;
        for (int i = 0; i < size; i++) {
            Long count = 0L;
            Collection<ResCoverage> coverages = coverageMultiMap.get(arr[i]);
            for (ResCoverage coverage : coverages) {
                paramMap = initParamMap(education, coverageEdgeLabel);
                List<Object> techInfoPartFieldEdges = fillResCoverageEdgePartField(coverage);
                fillParamMap(paramMap, techInfoPartFieldEdges, coverageEdgeField);
                builder = generateCheckResourceCategoryScript(baseBuilderEdge, techInfoPartFieldEdges, coverageEdgeField);
                Long tmp = executeScriptUniqueLong(paramMap, builder.count());
                count = techInfoEdgeScriptExecutionException(builder, paramMap, count, tmp);
                
                if (count == 1) {
                    Builder baseBuilderNode = new Builder(baseScript).outE().hasLabel(coverageEdgeLabel, coverageEdgeLabel).inV();
                    List<Object> techInfoPartFieldNodes = fillResCoverageNodePartField(coverage);
//                    fillParamMap(paramMap, techInfoPartFieldNodes, coverageNodeField);
                    Builder nodeBuilder = generateCheckResourceCategoryScript(baseBuilderNode, techInfoPartFieldNodes, coverageNodeField);
                    Long nodeCount = executeScriptUniqueLong(paramMap, nodeBuilder.count());
                    saveAbnormalData(education, nodeBuilder, paramMap, nodeCount);
                }
            }
            saveAbnormalData(education, builder, paramMap, count);
        }
    }
    
    String[] coverageNodeField = new String[]{"strategy", "target", "target_type"};
    private void checkResCoverageNodes(Education education,List<ResCoverage> resCoverages){
        String baseScript = new StringBuilder("g.V().has(").append(primaryCategory).append(",'identifier', ").append(educationIdentifier).append(")").toString();
        Builder baseBuilder = new Builder(baseScript).outE().hasLabel(coverageEdgeLabel, coverageEdgeLabel).inV();
        Multimap<String, ResCoverage> coverageMultiMap = toCoverageMultimap(resCoverages);
        Set<String> keySet = coverageMultiMap.keySet();
        int size = keySet.size();
        String[] arr = keySet.toArray(new String[size]);
        Builder builder = null;
        Map<String, Object> paramMap = null;
        for (int i = 0; i < size; i++) {
            Long count = 0L;
            Collection<ResCoverage> coverages = coverageMultiMap.get(arr[i]);
            for (ResCoverage coverage : coverages) {
                paramMap = initParamMap(education, coverageEdgeLabel);
                List<Object> techInfoPartField = fillResCoverageNodePartField(coverage);
                fillParamMap(paramMap, techInfoPartField, coverageNodeField);
                
                builder = generateCheckResourceCategoryScript(baseBuilder, techInfoPartField, coverageNodeField);
                
                Long tmp = executeScriptUniqueLong(paramMap, builder.count());
                count = techInfoEdgeScriptExecutionException(builder, paramMap, count, tmp);
            }
            saveAbnormalData(education, builder, paramMap, count);
        }
    }
    
    private List<Object> fillResCoverageNodePartField(ResCoverage coverage) {
        List<Object> coveragePartField = new ArrayList<Object>();
        coveragePartField.add(coverage.getStrategy());
        coveragePartField.add(coverage.getTarget());
        coveragePartField.add(coverage.getTargetType());
        return coveragePartField;
    }
    
    private List<Object> fillResCoverageEdgePartField(ResCoverage coverage) {
        List<Object> coveragePartField = new ArrayList<Object>();
        coveragePartField.add(coverage.getIdentifier());
        coveragePartField.add(coverage.getStrategy());
        coveragePartField.add(coverage.getTarget());
        coveragePartField.add(coverage.getTargetType());
        return coveragePartField;
    }
    
    
    private List<Object> fillTeachInfoPartField(TechInfo techInfo) {
        List<Object> techInfoPartField = new ArrayList<Object>();
        techInfoPartField.add(techInfo.getDescription());
        techInfoPartField.add(techInfo.getIdentifier());
        techInfoPartField.add(techInfo.getEntry());
        techInfoPartField.add(techInfo.getFormat());
        techInfoPartField.add(techInfo.getLocation());
        techInfoPartField.add(techInfo.getMd5());
        techInfoPartField.add(techInfo.getRequirements());
        techInfoPartField.add(techInfo.getSecureKey());
        techInfoPartField.add(techInfo.getSize());
        techInfoPartField.add(techInfo.getTitle());
        techInfoPartField.add(techInfo.getPrintable());
        return techInfoPartField;
    }
    
    private Multimap<String, TechInfo> toTechInfoMultimap(List<TechInfo> techInfos){
        Multimap<String, TechInfo> multimap = ArrayListMultimap.create();
        for (TechInfo techInfo : techInfos) {
            multimap.put(techInfo.getTitle(), techInfo);
        }
        return multimap;
    }
    
    /**
     * 根据 target,target_type,strategy 的唯一性去重
     * @param resCoverages
     * @return
     * @since 1.2.6
     * @see
     */
    private Multimap<String, ResCoverage> toCoverageMultimap(List<ResCoverage> resCoverages){
        Multimap<String, ResCoverage> multimap = ArrayListMultimap.create();
        for (ResCoverage coverage : resCoverages) {
            multimap.put(new StringBuilder(coverage.getTarget()).append(coverage.getTargetType()).append(coverage.getStrategy()).toString(), coverage);
        }
        return multimap;
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
    
    public void checkResourceAllInTitan2(Education education, List<ResCoverage> resCoverages, List<ResourceCategory> resourceCategories, List<TechInfo> techInfos, List<ResourceRelation> resourceRelationList) {
        checkCategoryEdges(education, resourceCategories);
        checkCategoryNodes(education, resourceCategories);
        checkTechInfoHandle(education, techInfos);
        checkResCoverage(education, resCoverages);
    }
    
    final String[] categoryEdgesField = new String[]{"cg_taxonpath", "cg_taxoncode", "cg_taxonname", "cg_category_code", "cg_short_name", "cg_category_name", "identifier"};
    final String categoryEdgeLabel = "has_category_code";
    final String educationIdentifier = "educationIdentifier";
    final String primaryCategory = "primaryCategory";
    public void checkCategoryEdges(Education education,List<ResourceCategory> resourceCategoryList){
        
        String baseScript = new StringBuilder("g.V().has(").append(primaryCategory).append(",'identifier', ").append(educationIdentifier).append(")").toString();
        Builder baseBuilder = new Builder(baseScript).outE().hasLabel(categoryEdgeLabel, categoryEdgeLabel);
        for (int index =0 ;index <resourceCategoryList.size() ;index++){
            Map<String, Object> paramMap = initParamMap(education, categoryEdgeLabel);
            List<Object> resourceCategoryPartField = fillResourceCategoryPartField(resourceCategoryList.get(index));
            fillParamMap(paramMap, resourceCategoryPartField, categoryEdgesField);
            
            Builder builder = generateCheckResourceCategoryScript(baseBuilder, resourceCategoryPartField, categoryEdgesField);
    
            Long count = executeScriptUniqueLong(paramMap, builder.count());
            saveCheckCategoryExceptionData(education, paramMap, builder, count);
        }
    }

    String[] categoryNodeField = new String[]{"cg_category_code"};
//    String techInfoLabel = "has_tech_info";
    
    /**
     * 和刘然沟通后，目前category_code_v只需要校验 cg_category_code 字段
     */
    public void checkCategoryNodes(Education education,List<ResourceCategory> resourceCategoryList){
        
        String baseScript = new StringBuilder("g.V().has(").append(primaryCategory).append(",'identifier', ").append(educationIdentifier).append(")").toString();
   
        Builder baseBuilder = new Builder(baseScript).outE().hasLabel(categoryEdgeLabel, categoryEdgeLabel).inV();
        for (int index =0 ;index <resourceCategoryList.size() ;index++){
            Map<String, Object> paramMap = initParamMap(education, categoryEdgeLabel);
            List<Object> resourceCategoryPartField = new ArrayList<Object>();
            resourceCategoryPartField.add(resourceCategoryList.get(index).getCategoryCode());
            fillParamMap(paramMap, resourceCategoryPartField, categoryNodeField);
            Builder builder = generateCheckResourceCategoryScript(baseBuilder, resourceCategoryPartField, categoryNodeField);
            
            Long count = executeScriptUniqueLong(paramMap, builder.count());
            saveCheckCategoryExceptionData(education, paramMap, builder, count);
        }
    }
    
    private void saveCheckCategoryExceptionData(Education education, Map<String, Object> paramMap, Builder builder, Long count) {
        // count 等于 1 正常，其他情况都不正常
        if (count == null) {
            LOG.error("查询脚本执行异常, script:{}, param:{}", builder.builder(), paramMap);
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CheckDuplicateIdFail.getCode(),
                    "查询脚本发生异常" + builder.builder());
        }
        if (count == 0) {
            LOG.info("mysql 中数据在titan 中不存在, script:{}, param:{}", builder.builder(), paramMap);
            titanSync(TitanSyncType.CHECK_NOT_EXIST,education.getPrimaryCategory(),education.getIdentifier());
        } else if(count > 1){
            LOG.info("titan 中数据重复, script:{}, param:{}", builder.builder(), paramMap);
            titanSync(TitanSyncType.CHECK_REPEAT,education.getPrimaryCategory(),education.getIdentifier());
        }
    }

    private Long executeScriptUniqueLong(Map<String, Object> paramMap, Builder builder) {
        Long count = 0L;
        try {
            count = titanCommonRepository.executeScriptUniqueLong(builder.builder(), paramMap);
        } catch (Exception e) {
            LOG.error("与 titan 的连接断开或查询脚本执行异常, script:{}, param:{}", builder.builder(), paramMap);
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CheckDuplicateIdFail.getCode(),
                    "与 titan 的连接断开或查询脚本发生异常" + builder.builder());
        }
        return count;
    }

    /**
     * 生成类似字符串： builder = g.V().has(primaryCategory, 'identifier', educationIdentifier).outE().hasLabel('has_category_code',has_category_code).has('x',x).has('a',a).has('b',b).has('c',c)
     */
    private Builder generateCheckResourceCategoryScript(Builder builder, List<Object> partField, String[] field) {
        Builder newBuilder = new Builder(builder);
        for (int j = 0; j < field.length; j++) {
            if (partField.get(j) != null) {
                newBuilder.has(field[j], field[j]);
            }
        }
        return newBuilder;
    }

    private void fillParamMap(Map<String, Object> paramMap, List<Object> partFieldValue, String[] field) {
        for (int j = 0; j < field.length; j++) {
            if (partFieldValue.get(j) != null) {
                paramMap.put(field[j], partFieldValue.get(j));
            }
        }
    }

    private Map<String, Object> initParamMap(Education education, String label) {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put(primaryCategory, education.getPrimaryCategory());
        paramMap.put(educationIdentifier,education.getIdentifier());
        
        paramMap.put(label, label);
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
        resourceCategoryPartField.add(resourceCategory.getIdentifier());
        return resourceCategoryPartField;
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
        List<ResCoverage> coverageList = TitanResourceUtils.groupCoverage(resCoverageList).get(education.getIdentifier());
        List<ResourceCategory> categoryList = TitanResourceUtils.groupCategory(resourceCategoryList).get(education.getIdentifier());
        List<TechInfo> techInfoList = TitanResourceUtils.groupTechInfo(techInfos).get(education.getIdentifier());

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
