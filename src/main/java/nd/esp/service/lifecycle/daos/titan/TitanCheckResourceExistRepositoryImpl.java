package nd.esp.service.lifecycle.daos.titan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nd.esp.service.lifecycle.daos.titan.inter.TitanCheckResourceExistRepository;
import nd.esp.service.lifecycle.daos.titan.inter.TitanCommonRepository;
import nd.esp.service.lifecycle.daos.titan.inter.TitanRepositoryUtils;
import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.model.ResCoverage;
import nd.esp.service.lifecycle.repository.model.ResourceCategory;
import nd.esp.service.lifecycle.repository.model.ResourceRelation;
import nd.esp.service.lifecycle.repository.model.ResourceStatistical;
import nd.esp.service.lifecycle.repository.model.TechInfo;
import nd.esp.service.lifecycle.services.titan.TitanResultParse;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.busi.titan.CheckResourceModel;
import nd.esp.service.lifecycle.support.busi.titan.TitanKeyWords;
import nd.esp.service.lifecycle.support.busi.titan.TitanSyncType;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.utils.TitanScritpUtils;

import org.apache.commons.collections.SetUtils;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.driver.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

@Repository("titanCheckResourceExistRepository")
public class TitanCheckResourceExistRepositoryImpl implements TitanCheckResourceExistRepository{
    
    private final Logger LOG = LoggerFactory.getLogger(TitanCheckResourceExistRepositoryImpl.class);
    
    @Autowired
    private TitanRepositoryUtils titanRepositoryUtils;
    
    @Autowired
    private TitanCommonRepository titanCommonRepository;
    
    @Override
    public void checkOneResourceInTitan(Education education, List<ResCoverage> resCoverages, List<ResourceCategory> resourceCategories, List<TechInfo> techInfos, List<ResourceRelation> resourceRelations, List<ResourceStatistical> statistic) {
        checkCategories(education, resourceCategories);
        checkTechInfos(education, techInfos);
        checkResCoverage(education, resCoverages);
        checkResourceStatistic(education, statistic);
        checkNdResource(education, resourceCategories, resCoverages);
        checkResourceRelations(resourceRelations);
    }
    
    @Override
    public void checkResourcesInTitan(CheckResourceModel checkResourceModel){
        checkCategories(checkResourceModel.getEducation(), checkResourceModel.getResourceCategories());
        checkTechInfos(checkResourceModel.getEducation(), checkResourceModel.getTechInfos());
        checkResCoverage(checkResourceModel.getEducation(), checkResourceModel.getResCoverages());
        checkResourceStatistic(checkResourceModel.getEducation(), checkResourceModel.getResourceStatistic());
        checkNdResource(checkResourceModel.getEducation(), checkResourceModel.getResourceCategories(), checkResourceModel.getResCoverages());
    }
    
    
    final String relationEdgeLabel = TitanKeyWords.has_relation.toString();
    @Override
    public void checkResourceRelations(List<ResourceRelation> relations){
        Builder baseBuilder = new Builder("g.V()").has("identifier", "source_uuid").outE().hasLabel(relationEdgeLabel);
        for (int index =0 ;index < relations.size() ;index++){
            Map<String, Object> initParams = new HashMap<String, Object>();
            ResourceRelation relation = relations.get(index);
            List<Object> resourceRelationPartField = fillResourceRelationPartField(relation);
            Map<String, Object> params = fillParams(initParams, resourceRelationPartField, resourceRelation);
            
            Builder builder = generateScript(baseBuilder.build(), resourceRelationPartField, resourceRelation).inV().has("identifier", "target_uuid");
            Long count = executeScriptUniqueLong(params, builder.count());
            saveAbnormalData(params, relation, builder, count);
        }
    }
    
    private void saveAbnormalData(Map<String, Object> paramMap, ResourceRelation relation, Builder builder, Long count) {
        if (count == null) {
            LOG.error("查询脚本执行异常, script:{}, param:{}", builder.build(), paramMap);
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CheckDuplicateIdFail.getCode(),
                    "查询脚本发生异常" + builder.build());
        }
        if (count == 0) {
            if (titanRepositoryUtils.checkRelationExistInMysql(relation)) {
                LOG.info("mysql 中数据在titan 中不存在, script:{}, param:{}", builder.build(), paramMap);
                titanSync(TitanSyncType.CHECK_RR_NOT_EXIST, relation.getResType(), relation.getSourceUuid());
            }
        } else if(count > 1){
            if (titanRepositoryUtils.checkRelationExistInMysql(relation)) {
                LOG.info("titan 中数据重复, script:{}, param:{}", builder.build(), paramMap);
                titanSync(TitanSyncType.CHECK_RR_REPEAT,relation.getResType(), relation.getSourceUuid());
            }
        }
    }
    
    final String categoryEdgeLabel = TitanKeyWords.has_category_code.toString();
    final String educationIdentifier = "educationIdentifier";
    final String primaryCategory = "primaryCategory";
    final String[] categoryNodeField = new String[]{"cg_taxoncode"};
   
    /**
     * 校验 has_category_code_e 和 category_code_v
     * 和刘然沟通后，目前category_code_v只需要校验 cg_category_code 字段
     * @param education
     * @param categories
     * @since 1.2.6
     * @see
     */
    private void checkCategories(Education education,List<ResourceCategory> categories){
        Builder baseBuilder = new Builder("g.V()").has(primaryCategory,"identifier", educationIdentifier).outE().hasLabel(categoryEdgeLabel);
        for (int index =0 ;index <categories.size() ;index++){
            List<Object> resourceCategoryEdgePartField = fillResourceCategoryPartField(categories.get(index));
            Map<String, Object> initParams = initParams(education, categoryEdgeLabel);
            Map<String, Object> params = fillParams(initParams, resourceCategoryEdgePartField, categoryEdgesField);
            
            Builder builder = generateScript(baseBuilder.build(), resourceCategoryEdgePartField, categoryEdgesField).inV();
    
            List<Object> resourceCategoryNodePartField = new ArrayList<Object>();
            resourceCategoryNodePartField.add(categories.get(index).getCategoryCode());
            Builder nodeBuilder = generateScript(builder.build(), resourceCategoryNodePartField, categoryNodeField);
            Long count = executeScriptUniqueLong(params, nodeBuilder.count());
            saveAbnormalData(params, builder, count, TitanSyncType.CHECK_CG_NOT_EXIST, TitanSyncType.CHECK_CG_REPEAT);
        }
    }
    
    final String techInfoEdgeLabel = TitanKeyWords.has_tech_info.toString();
    
    /**
     * 根据tech_infos 表的title 字段去重， 相同资源的tech_infos 记录，根据title 分组，titan 中存在一条即可
     * @param education
     * @param techInfos
     * @since 1.2.6
     * @see
     */
    private void checkTechInfos(Education education, List<TechInfo> techInfos){
        Builder baseBuilder = new Builder("g.V()").has(primaryCategory,"identifier", educationIdentifier).outE().hasLabel(techInfoEdgeLabel);
        Multimap<String, TechInfo> techInfoMultiMap = toTechInfoMultimap(techInfos);
        Set<String> keySet = techInfoMultiMap.keySet();
        int size = keySet.size();
        String[] arr = keySet.toArray(new String[size]);
        Builder nodeBuilder = null;
        Map<String, Object> params = new HashMap<String, Object>(1);
        Map<String, Object> repeatParmas = new HashMap<String, Object>(1);
        for (int i = 0; i < size; i++) {
        	Long count = 0L;
            Collection<TechInfo> teachInfoList = techInfoMultiMap.get(arr[i]);
            for (TechInfo techInfo : teachInfoList) {
                Map<String, Object> initParams = initParams(education, techInfoEdgeLabel);
                List<Object> techInfoPartField = fillTeachInfoPartField(techInfo);
                repeatParmas = params;
                params = fillParams(initParams, techInfoPartField, techInfoField);
                // 数据库中可能存在 tech_info，除了identifier 不一样，其他字段完全一样的多条数据，假设只有两条A，B，导入titan根据title去重只能保留一条数据存入，
                // 会导致数据重复误差（tech_info 检验没有带上参数identifer，因为导入数据方不能够保证根据title去重）                
                if (repeatParmas.equals(params)) {
                	continue;
                }
                
                Builder builder = generateScript(baseBuilder.build(), techInfoPartField, techInfoField).inV();
                
                nodeBuilder = generateScript(builder.build(), techInfoPartField, techInfoField);
                Long tmp = executeScriptUniqueLong(params, nodeBuilder.count());
                count += scriptExecuteException(builder, params, tmp);
            }
            techInfoAbnormalData(nodeBuilder, params, count);
        }
    }

    private void techInfoAbnormalData(Builder nodeBuilder, Map<String, Object> params, Long count) {
        if (count.intValue() >= 2) {
            LOG.info("techInfo: titan 中数据重复, has_tech_info 边数量:{} script:{}, param:{}", count, nodeBuilder.build(), params);
            titanSync(TitanSyncType.CHECK_TI_REPEAT, params.get(primaryCategory).toString(), params.get(educationIdentifier).toString());
            return;
        }
        if (count.intValue() == 0) {
            LOG.info("techInfo: mysql 中数据在 titan 中不存在, script:{}, param:{}", nodeBuilder.build(), params);
            titanSync(TitanSyncType.CHECK_TI_NOT_EXIST, params.get(primaryCategory).toString(), params.get(educationIdentifier).toString());
        }
    }
    

    private Long scriptExecuteException(Builder builder, Map<String, Object> paramMap, Long tmp) {
        if (tmp == null) {
            LOG.error("查询脚本执行异常, script:{}, param:{}", builder.build(), paramMap);
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CheckDuplicateIdFail.getCode(),
                    "查询脚本发生异常:" + builder.build());
        }
        return tmp;
    }
    
    
    private void saveAbnormalData(Builder builder, Map<String, Object> paramMap, Long count, TitanSyncType notExist,
            TitanSyncType repeat) {
        // count = 1 为正常
        if (count == 0) {
            LOG.info("mysql 中数据在titan 中不存在, script:{}, param:{}", builder.build(), paramMap);
            titanSync(notExist, paramMap.get(primaryCategory).toString(), paramMap.get(educationIdentifier).toString());
        } else if (count >= 2) {
            LOG.info("titan 中数据重复, script:{}, param:{}", builder.build(), paramMap);
            titanSync(repeat, paramMap.get(primaryCategory).toString(), paramMap.get(educationIdentifier).toString());
        }
    }
    
    private Multimap<String, TechInfo> toTechInfoMultimap(List<TechInfo> techInfos){
        Multimap<String, TechInfo> multimap = ArrayListMultimap.create();
        for (TechInfo techInfo : techInfos) {
            multimap.put(techInfo.getTitle(), techInfo);
        }
        return multimap;
    }
    
    final String coverageEdgeLabel = TitanKeyWords.has_coverage.toString();
    
    /**
     * 校验has_coverage_e 和 coverage_v 数据
     * 根据 stragy, target, target_type 去重
     * @param education
     * @param resCoverages
     * @since 1.2.6
     * @see
     */
    private void checkResCoverage(Education education,List<ResCoverage> resCoverages){
        Builder baseBuilderEdge = new Builder("g.V()").has(primaryCategory,"identifier", educationIdentifier).outE().hasLabel(coverageEdgeLabel);
        Multimap<String, ResCoverage> coverageMultiMap = toCoverageMultimap(resCoverages);
        Set<String> keySet = coverageMultiMap.keySet();
        int size = keySet.size();
        String[] arr = keySet.toArray(new String[size]);
        Builder builder = null;
        Map<String, Object> params = null;
        for (int i = 0; i < size; i++) {
            Long count = 0L;
            Collection<ResCoverage> coverages = coverageMultiMap.get(arr[i]);
            for (ResCoverage coverage : coverages) {
                Map<String, Object> initParams = initParams(education, coverageEdgeLabel);
                List<Object> techInfoPartFieldEdges = fillResCoverageEdgePartField(coverage);
                params = fillParams(initParams, techInfoPartFieldEdges, coverageEdgeField);
                builder = generateScript(baseBuilderEdge.build(), techInfoPartFieldEdges, coverageEdgeField);
                Builder baseBuilderNode = new Builder(builder);
                Long tmp = executeScriptUniqueLong(params, builder.count());
                count = scriptExecuteException(builder, params, tmp);
                
                if (count == 1) {
//                    Builder baseBuilderNode = new Builder(baseBuilderEdge).outE().hasLabel(coverageEdgeLabel).inV();
                    List<Object> techInfoPartFieldNodes = fillResCoverageNodePartField(coverage);
                    Builder nodeBuilder = generateScript(baseBuilderNode.inV().build(), techInfoPartFieldNodes, coverageNodeField);
                    Long nodeCount = executeScriptUniqueLong(params, nodeBuilder.count());
                    saveAbnormalData(nodeBuilder, params, nodeCount, TitanSyncType.CHECK_RC_NOT_EXIST, TitanSyncType.CHECK_RC_REPEAT);
                }
            }
            saveAbnormalData(builder, params, count, TitanSyncType.CHECK_RC_NOT_EXIST,TitanSyncType.CHECK_RC_REPEAT);
        }
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
    
    
    final String statisticEdgeLabel = TitanKeyWords.has_resource_statistical.toString();
    /**
     * 校验 has_resource_staistical_e 和 statistical_v 数据，mysql 中数据在 titan 中是否存在
     * @param education
     * @param statistic
     * @since 1.2.6
     * @see
     */
    private void checkResourceStatistic(Education education,List<ResourceStatistical> statistic){
        Builder baseBuilder = new Builder("g.V()").has(primaryCategory,"identifier", educationIdentifier).outE().hasLabel(statisticEdgeLabel);
        for (int index =0 ;index < statistic.size() ;index++){
            Map<String, Object> initParams = initParams(education, statisticEdgeLabel);
            List<Object> resourceStatisticPartField = fillResourceStatisticPartField(statistic.get(index));
            Map<String, Object> params = fillParams(initParams, resourceStatisticPartField, resourceStatistic);
            // 刘然反应：sta_update_time 字段值与数据库不一致，只能比较它是否存在            
            Builder builder = generateScript(baseBuilder.build(), resourceStatisticPartField, resourceStatistic).has("sta_update_time").inV();
            Builder nodeBuilder = generateScript(builder.build(), resourceStatisticPartField, resourceStatistic).has("sta_update_time");
            Long count = executeScriptUniqueLong(params, nodeBuilder.count());
            saveAbnormalData(params, builder, count, TitanSyncType.CHECK_STA_NOT_EXIST,TitanSyncType.CHECK_STA_REPEAT);
        }
    }
    
    String[] ndResource = new String[]{"cr_author", "cr_description", "cr_has_right", "cr_right", "cr_right_end_date", "cr_right_start_date", "custom_properties", "description"
            , "edu_age_range", "edu_description", "edu_difficulty", "edu_end_user_type", "edu_interactivity", "edu_interactivity_level", "edu_language", "edu_learning_time", 
            "edu_semantic_density", "keywords", "language", "lc_create_time", "lc_creator", "lc_enable", "lc_last_update", "lc_provider", "lc_provider_mode"
            , "lc_provider_source", "lc_publisher", "lc_status", "lc_version", "preview", "tags", "title"};
    /**
     * 填充顺序必须和 ndResource 保持一致
     * @param education
     * @param categories
     * @param coverages
     */
    private void checkNdResource(Education education, List<ResourceCategory> categories, List<ResCoverage> coverages){
        Map<String, Object> initParams = new HashMap<String, Object>();
        initParams.put(educationIdentifier, education.getIdentifier());
        initParams.put(primaryCategory, education.getPrimaryCategory());
        
        Builder baseBuilder = new Builder("g.V()").has(primaryCategory,"identifier", educationIdentifier);
        
        List<Object> ndResourcePartField = getNdResourcePartField(education);
        
        Map<String, Object> params = fillParams(initParams, ndResourcePartField, ndResource);
        
        Builder builder = generateScript(baseBuilder.build(), ndResourcePartField, ndResource);
        
        ResultSet resultSet = executeScript(params, builder);
        Iterator<Result> iterator = resultSet.iterator();
        
        int count = 0;
        String result = null;
        while (iterator.hasNext()) {
            result = iterator.next().getString();
            ++count;
        }
        
        if (count > 1) {
            LOG.info("titan 中数据重复, script:{}, param:{}", builder.build(), initParams);
            titanSync(TitanSyncType.CHECK_NR_REPEAT, initParams.get(primaryCategory).toString(), initParams.get(educationIdentifier).toString());
        }
        
        if(!StringUtils.isEmpty(result)){
            Map<String, String> valueMap = TitanResultParse.toMap(result);

            boolean isDataNotExist = isDataExist(valueMap, getTaxonPath(categories), getTaxOnCode(categories), getResCoverage(coverages, education.getStatus()));

            if (isDataNotExist) {
                LOG.info("mysql 中数据在titan 中不存在, script:{}, param:{}", builder.build(), params);
                titanSync(TitanSyncType.CHECK_NR_NOT_EXIST, params.get(primaryCategory).toString(), params.get(educationIdentifier).toString());
            }
        } else {
            LOG.info("mysql 中数据在titan 中不存在, script:{}, param:{}", builder.build(), params);
            titanSync(TitanSyncType.CHECK_NR_NOT_EXIST, params.get(primaryCategory).toString(), params.get(educationIdentifier).toString());
        }
    }
    
    /**
     * true -- 该记录在 titan 中存在
     * false -- 该记录在 titan 中不存在
     * @param valueMap
     * @param paths
     * @param codes
     * @param coverageSet
     * @return
     * @since 1.2.6
     * @see
     */
    private boolean isDataExist(Map<String, String> valueMap, Set<String> paths, Set<String> codes,
            Set<String> coverageSet) {
        boolean isCodeEqual = isEqual(valueMap.get("search_code"), codes);
        boolean isPathEqual = isEqual(valueMap.get("search_path"), paths);
        boolean isCoverageEqual = isEqual(valueMap.get("search_coverage"), coverageSet);
        
        boolean isCodeStringEqual = isEqual(valueMap.get("search_code_string"), TitanScritpUtils.appendQuoMark(codes));
        boolean isPathStringEqual = isEqual(valueMap.get("search_path_string"), TitanScritpUtils.appendQuoMark(paths));
        boolean isCoverageStringEqual = isEqual(valueMap.get("search_coverage_string"), TitanScritpUtils.appendQuoMark(coverageSet));
        return !isCodeEqual || !isPathEqual || !isCoverageEqual || !isCodeStringEqual || !isPathStringEqual || !isCoverageStringEqual;
    }
    
    private boolean isEqual(String value1, Set<String> value2) {
        return SetUtils.isEqualSet(value2, split(value1));
    }
    
    /**
     * value: a, B, c, d --> [A, B, C, D]
     * @param value 以英文逗号分隔的字符串
     * @return
     * @since 1.2.6
     * @see
     */
    private List<String> split(String value) {
        // titan 中 value = "" 对应mysql 中 null         
        if (StringUtils.isNotEmpty(value)) {
            // \\s+ 去除空格，回车等字符
            return Arrays.asList(value.replaceAll("\\s+", "").toUpperCase().split(","));
        }
        return Lists.newArrayList();
    }
    
    private Set<String> getTaxonPath(List<ResourceCategory> categories) {
        Set<String> paths = Sets.newHashSet();
        for (ResourceCategory category : categories) {
            if (category.getTaxonpath() != null && !category.getTaxonpath().equals("")) {
                paths.add(category.getTaxonpath().toUpperCase());
            }
        }
        return paths;
    }
    
    private Set<String> getResCoverage(List<ResCoverage> coverages, String status) {
        Set<String> coverageSet = Sets.newHashSet();
        for (ResCoverage coverage : coverages) {
            Set<String> allResourceCoverage = TitanScritpUtils.getAllResourceCoverage(coverage, status);
            for (String rc : allResourceCoverage) {
                coverageSet.add(rc.toUpperCase());
            }
        }
        return coverageSet;
    }

    private Set<String> getTaxOnCode(List<ResourceCategory> categories) {
        Set<String> codes = Sets.newHashSet();
        for (ResourceCategory category : categories) {
            codes.add(category.getTaxoncode().toUpperCase());
        }
        return codes;
    }
    
    private ResultSet executeScript(Map<String, Object> params, Builder builder) {
        ResultSet resultSet = null;
        try {
            resultSet = titanCommonRepository.executeScriptResultSet(
                    builder.valueMap().build(), params);
        } catch (Exception e) {
            LOG.error("查询脚本执行异常, script:{}, param:{}", builder.build(), params);
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CheckDuplicateIdFail.getCode(),
                    "查询脚本发生异常:" + builder.build() + e.getMessage());
        }
        return resultSet;
    }
    
    /**
     * 保存异常数据到titan sync表中
     * */
    private void titanSync(TitanSyncType syncType, String parmaryCategory, String identifier){
        titanRepositoryUtils.titanSync4MysqlAdd(syncType, parmaryCategory, identifier,999);
    }
    
    /**
     * 生成类似字符串： builder = g.V().has(primaryCategory, 'identifier', educationIdentifier).outE().hasLabel('has_category_code',has_category_code).has('x',x).has('a',a).has('b',b).has('c',c) 
     * @param script
     * @param partFieldValue
     * @param field
     * @return
     * @since 1.2.6
     * @see
     */
    private Builder generateScript(String script, List<Object> partFieldValue, String[] field) {
        Builder newBuilder = new Builder(script);
        for (int j = 0; j < field.length; j++) {
            if (partFieldValue.get(j) != null && partFieldValue.get(j).toString().length() <= 10000) {
                newBuilder.has(field[j], field[j]);
            }
        }
        return newBuilder;
    }

    private Map<String, Object> fillParams(Map<String, Object> params, List<Object> partFieldValue, String[] field) {
        HashMap<String, Object> newHashMap = Maps.newHashMap(params);
        for (int j = 0; j < field.length; j++) {
            if (partFieldValue.get(j) != null && partFieldValue.get(j).toString().length() <= 10000) {
                newHashMap.put(field[j], partFieldValue.get(j));
            }
        }
        return newHashMap;
    }

    private Map<String, Object> initParams(Education education, String label) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(primaryCategory, education.getPrimaryCategory());
        params.put(educationIdentifier,education.getIdentifier());
        
        params.put(label, label);
        return params;
    }
    
    final String[] categoryEdgesField = new String[]{"cg_taxonpath", "cg_taxoncode", "cg_taxonname", "cg_category_code", "cg_short_name", "cg_category_name", "identifier"};
    /**
     * 填充顺序必须与 categoryEdgesField 中的字段保持一致
     * @param resourceCategory
     * @return
     */
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

    final String[] coverageEdgeField = new String[]{"identifier", "strategy", "target", "target_type"};
    /**
     * 填充顺序必须与 coverageEdgeField 中的字段保持一致
     * @param coverage
     * @return
     */
    private List<Object> fillResCoverageEdgePartField(ResCoverage coverage) {
        List<Object> coveragePartField = new ArrayList<Object>();
        coveragePartField.add(coverage.getIdentifier());
        coveragePartField.add(coverage.getStrategy());
        coveragePartField.add(coverage.getTarget());
        coveragePartField.add(coverage.getTargetType());
        return coveragePartField;
    }
    
    final String[] resourceRelation = new String[]{"enable", "identifier", "order_num", "relation_type", "rr_label", "sort_num", "res_type", "source_uuid", "tags", "resource_target_type", "target_uuid"};
    /**
     * 填充顺序必须与 resourceRelation 中的字段保持一致
     * @param relations
     * @return
     */
    private List<Object> fillResourceRelationPartField(ResourceRelation relations) {
        List<Object> resourceRelationPartField = new ArrayList<Object>();
        resourceRelationPartField.add(relations.getEnable());
        resourceRelationPartField.add(relations.getIdentifier());
        resourceRelationPartField.add(relations.getOrderNum());
        resourceRelationPartField.add(relations.getRelationType());
        resourceRelationPartField.add(relations.getLabel());
        resourceRelationPartField.add(relations.getSortNum());
        resourceRelationPartField.add(relations.getResType());
        resourceRelationPartField.add(relations.getSourceUuid());
        resourceRelationPartField.add(relations.getDbtags());
        resourceRelationPartField.add(relations.getResourceTargetType());
        resourceRelationPartField.add(relations.getTarget());
        return resourceRelationPartField;
    }
    
    final String[] resourceStatistic = new String[]{"identifier", "sta_data_from", "sta_key_title", "sta_key_value", "sta_res_type", "sta_resource", "sta_title"};
    /**
     * 填充顺序必须与 resourceStatistic 中的字段保持一致
     * @param statistic
     * @return
     */
    private List<Object> fillResourceStatisticPartField(ResourceStatistical statistic) {
        List<Object> resourceStatisticPartField = new ArrayList<Object>();
        resourceStatisticPartField.add(statistic.getIdentifier());
        resourceStatisticPartField.add(statistic.getDataFrom());
        resourceStatisticPartField.add(statistic.getKeyTitle());
        resourceStatisticPartField.add(statistic.getKeyValue());
        resourceStatisticPartField.add(statistic.getResType());
        resourceStatisticPartField.add(statistic.getResource());
        resourceStatisticPartField.add(statistic.getTitle());
        if (statistic.getUpdateTime() != null) {
            resourceStatisticPartField.add(String.valueOf(statistic.getUpdateTime().getTime()));
        } else {
            resourceStatisticPartField.add(null);
        }
        return resourceStatisticPartField;
    }

    String[] coverageNodeField = new String[]{"strategy", "target", "target_type"};
    /**
     *  填充顺序必须与 coverageNodeField 中的字段保持一致
     * @param coverage
     * @return
     */
    private List<Object> fillResCoverageNodePartField(ResCoverage coverage) {
        List<Object> coveragePartField = new ArrayList<Object>();
        coveragePartField.add(coverage.getStrategy());
        coveragePartField.add(coverage.getTarget());
        coveragePartField.add(coverage.getTargetType());
        return coveragePartField;
    }
    
    private Long executeScriptUniqueLong(Map<String, Object> paramMap, Builder builder) {
        Long count = 0L;
        try {
            count = titanCommonRepository.executeScriptUniqueLong(builder.build(), paramMap);
        } catch (Exception e) {
            LOG.error("与 titan 的连接断开或查询脚本执行异常, script:{}, param:{}", builder.build(), paramMap);
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CheckDuplicateIdFail.getCode(),
                    "与 titan 的连接断开或查询脚本发生异常" + builder.build() + e.getMessage());
        }
        return count;
    }

    private void saveAbnormalData(Map<String, Object> paramMap, Builder builder, Long count, TitanSyncType notExist,
            TitanSyncType repeat) {
        // count 等于 1 正常，其他情况都不正常
        if (count == null) {
            LOG.error("查询脚本执行异常, script:{}, param:{}", builder.build(), paramMap);
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CheckDuplicateIdFail.getCode(),
                    "查询脚本发生异常" + builder.build());
        }
        if (count == 0) {
            LOG.info("mysql 中数据在titan 中不存在, script:{}, param:{}", builder.build(), paramMap);
            titanSync(notExist, paramMap.get(primaryCategory).toString(), paramMap.get(educationIdentifier).toString());
        } else if (count > 1) {
            LOG.info("titan 中数据重复, script:{}, param:{}", builder.build(), paramMap);
            titanSync(repeat, paramMap.get(primaryCategory).toString(), paramMap.get(educationIdentifier).toString());
        }
    }
    
    private List<Object> getNdResourcePartField(Education education) {
        List<Object> ndResourcePartField = new ArrayList<Object>(35);
        ndResourcePartField.add(education.getAuthor());
        ndResourcePartField.add(education.getCrDescription());
        ndResourcePartField.add(education.getHasRight());
        ndResourcePartField.add(education.getCrRight());
        if (education.getRightEndDate() != null) {
            ndResourcePartField.add(String.valueOf(education.getRightEndDate().longValue()));
        } else {
            ndResourcePartField.add(null);
        }
        if (education.getRightStartDate() != null) {
            ndResourcePartField.add(String.valueOf(education.getRightStartDate().longValue()));
        } else {
            ndResourcePartField.add(null);
        }
        ndResourcePartField.add(education.getCustomProperties());
        ndResourcePartField.add(education.getDescription());
        ndResourcePartField.add(education.getAgeRange());
        ndResourcePartField.add(education.getDbEduDescription());
        ndResourcePartField.add(education.getDifficulty());
        ndResourcePartField.add(education.getEndUserType());
        ndResourcePartField.add(education.getInteractivity());
        ndResourcePartField.add(education.getInteractivityLevel());
        ndResourcePartField.add(education.getEduLanguage());
        ndResourcePartField.add(education.getLearningTime());
        ndResourcePartField.add(education.getSemanticDensity());
        ndResourcePartField.add(education.getDbkeywords());
        ndResourcePartField.add(education.getLanguage());
        if (education.getCreateTime() != null) {
            ndResourcePartField.add(String.valueOf(education.getCreateTime().getTime()));
        } else {
            ndResourcePartField.add(null);
        }
        ndResourcePartField.add(education.getCreator());
        ndResourcePartField.add(education.getEnable());
        if (education.getLastUpdate() !=null ) {
            ndResourcePartField.add(String.valueOf(education.getLastUpdate().getTime()));
        } else {
            ndResourcePartField.add(null);
        }
        ndResourcePartField.add(education.getProvider());
        ndResourcePartField.add(education.getProviderMode());
        ndResourcePartField.add(education.getProviderSource());
        ndResourcePartField.add(education.getPublisher());
        ndResourcePartField.add(education.getStatus());
        ndResourcePartField.add(education.getVersion());
        ndResourcePartField.add(education.getDbpreview());
        ndResourcePartField.add(education.getDbtags());
        ndResourcePartField.add(education.getTitle());
        return ndResourcePartField;
    }
    
    
    final String[] techInfoField = new String[]{"description", "ti_entry", "ti_format", "ti_location", "ti_md5", "ti_requirements", "ti_secure_key", "ti_size","ti_title", "ti_printable"};
    /**
     * 填充顺序必须要和 techInfoField 保持一致
     * @param techInfo
     * @return
     */
    private List<Object> fillTeachInfoPartField(TechInfo techInfo) {
        List<Object> techInfoPartField = new ArrayList<Object>();
        techInfoPartField.add(techInfo.getDescription());
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
    
    private void titanSync(Education education){
        titanRepositoryUtils.titanSync4MysqlAdd(TitanSyncType.IMPORT_DATA_ERROR,
                education.getPrimaryCategory(), education.getIdentifier(),999);
    }
    
    class Builder {

        StringBuilder sb = new StringBuilder();

        public Builder(Builder builder) {
            this.sb = new StringBuilder(builder.sb);
        }

        public Builder(String head) {
            sb.append(head);
        }

        public Builder has(String key, String value) {
            sb.append(".has(").append("'").append(key).append("'").append(",").append(value).append(")");
            return this;
        }

        public Builder has(String primaryCategory, String key, String value) {
            sb.append(".has(").append(primaryCategory).append(",'").append(key).append("'").append(",").append(value)
                    .append(")");
            return this;
        }

        public Builder hasLabel(String label) {
            sb.append(".hasLabel('").append(label).append("')");
            return this;
        }

        public Builder has(String key) {
            sb.append(".has('").append(key).append("')");
            return this;
        }
        
        public Builder outE() {
            sb.append(".outE()");
            return this;
        }

        public Builder inV() {
            sb.append(".inV()");
            return this;
        }

        public String build() {
            return sb.toString();
        }

        public Builder id() {
            sb.append(".id()");
            return this;
        }

        public Builder count() {
            sb.append(".count()");
            return this;
        }

        public Builder valueMap() {
            sb.append(".valueMap()");
            return this;
        }

        @Override
        public String toString() {
            return "Builder [sb=" + sb + "]";
        }
    }
}

