package nd.esp.service.lifecycle.services.titan;

import java.util.*;

import nd.esp.service.lifecycle.daos.titan.inter.TitanResourceRepository;
import nd.esp.service.lifecycle.educommon.models.*;
import nd.esp.service.lifecycle.educommon.vos.constant.PropOperationConstant;
import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.busi.elasticsearch.EsIndexQueryBuilder;
import nd.esp.service.lifecycle.support.busi.titan.*;
import nd.esp.service.lifecycle.support.enums.ES_Field;
import nd.esp.service.lifecycle.support.enums.ES_OP;
import nd.esp.service.lifecycle.support.enums.ES_SearchField;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.ParamCheckUtil;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.vos.ListViewModel;
import nd.esp.service.lifecycle.vos.educationrelation.v06.RelationForQueryViewModel;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.driver.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;


@Service
public class TitanSearchServiceImpl implements TitanSearchService {

    @Autowired
    private TitanResourceRepository<Education> titanResourceRepository;

    private final Logger LOG = LoggerFactory.getLogger(TitanSearchServiceImpl.class);

    @Override
    public ListViewModel<ResourceModel> search(String resType,
                                               List<String> includes,
                                               Map<String, Map<String, List<String>>> params,
                                               Map<String, String> orderMap, int from, int size, boolean reverse, String words) {

        long generateScriptBegin = System.currentTimeMillis();
        TitanExpression titanExpression = new TitanExpression();

        Map<String, Object> scriptParamMap = new HashMap<String, Object>();

        dealWithOrderAndRange(titanExpression, orderMap, from, size);
        dealWithRelation(titanExpression, params.get("relation"), reverse);
        params.remove("relation");
        // for now only deal with code
        dealWithTaxoncode(titanExpression,
                params.get(ES_SearchField.cg_taxoncode.toString()));
        params.remove(ES_SearchField.cg_taxoncode.toString());

        dealWithTaxonpath(titanExpression,
                params.get(ES_SearchField.cg_taxonpath.toString()));
        params.remove(ES_SearchField.cg_taxonpath.toString());

        dealWithCoverage(titanExpression,
                params.get(ES_SearchField.coverages.toString()));
        params.remove(ES_SearchField.coverages.toString());

        TitanQueryVertexWithWords resourceQueryVertex = new TitanQueryVertexWithWords();
        resourceQueryVertex.setWords(words);
        resourceQueryVertex.setVertexLabel(resType);
        Map<String, Map<Titan_OP, List<Object>>> resourceVertexPropertyMap = new HashMap<String, Map<Titan_OP, List<Object>>>();
        resourceQueryVertex.setPropertiesMap(resourceVertexPropertyMap);
        resourceVertexPropertyMap
                .put(ES_SearchField.lc_enable.toString(),
                        generateFieldCondtion(
                                ES_SearchField.lc_enable.toString(), true));
        dealWithResource(resourceQueryVertex, params);
        titanExpression.addCondition(resourceQueryVertex);

        //for count and result
        String scriptForResultAndCount = titanExpression.generateScriptForResultAndCount(scriptParamMap);
        LOG.info("titan generate script consume times:" + (System.currentTimeMillis() - generateScriptBegin));

        System.out.println(scriptForResultAndCount);
        System.out.println(scriptParamMap);
        long searchBegin = System.currentTimeMillis();
        ResultSet resultSet = titanResourceRepository.search(scriptForResultAndCount, scriptParamMap);
        LOG.info("titan search consume times:" + (System.currentTimeMillis() - searchBegin));

        return getListViewModelResourceModel(resultSet,resType,includes);
    }


    @Override
    public ListViewModel<ResourceModel> searchWithAdditionProperties(
            String resType, List<String> includes,
            Map<String, Map<String, List<String>>> params,
            Map<String, String> orderMap, int from, int size, boolean reverse,
            String words) {

        long generateScriptBegin = System.currentTimeMillis();
        TitanExpression titanExpression = new TitanExpression();
        titanExpression.setIncludes(includes);
        titanExpression.setResType(resType);
        titanExpression.setRange(from, size);
        //dealWithInclude(titanExpression,includes);

        Map<String, Object> scriptParamMap = new HashMap<String, Object>();

        // FIXME 处理order by
        List<TitanOrder> orderList = new ArrayList<>();
        dealWithOrderByEnum(titanExpression, scriptParamMap, orderMap, orderList,false);
        titanExpression.setOrderList(orderList);
        dealWithRelation(titanExpression, params.get("relation"), reverse);
        params.remove("relation");

        TitanQueryVertexWithWords resourceQueryVertex = new TitanQueryVertexWithWords();
        resourceQueryVertex.setIsFilter(resType);

        Map<String, Map<Titan_OP, List<Object>>> resourceVertexPropertyMap = new HashMap<String, Map<Titan_OP, List<Object>>>();
        resourceQueryVertex.setPropertiesMap(resourceVertexPropertyMap);
        // for now only deal with code
        dealWithSearchCode(resourceQueryVertex,
                params.get(ES_SearchField.cg_taxoncode.toString()));
        params.remove(ES_SearchField.cg_taxoncode.toString());

        dealWithSearchPath(resourceQueryVertex,
                params.get(ES_SearchField.cg_taxonpath.toString()));
        params.remove(ES_SearchField.cg_taxonpath.toString());

        dealWithSearchCoverage(resourceVertexPropertyMap,
                params.get(ES_SearchField.coverages.toString()));
        params.remove(ES_SearchField.coverages.toString());

        resourceQueryVertex.setWords(words);
        // FIXME
        // resourceQueryVertex.setVertexLabel(resType);

        resourceVertexPropertyMap.put(TitanKeyWords.primary_category.toString(),generateFieldCondtion(TitanKeyWords.primary_category.toString(), resType));
        resourceVertexPropertyMap.put(ES_SearchField.lc_enable.toString(),
                        generateFieldCondtion( ES_SearchField.lc_enable.toString(), true));
        dealWithResource(resourceQueryVertex, params);
        titanExpression.addCondition(resourceQueryVertex);

        // for count and result
        String scriptForResultAndCount = titanExpression.generateScriptForResultAndCount(scriptParamMap);
        LOG.info("titan generate script consume times:" + (System.currentTimeMillis() - generateScriptBegin));

        System.out.println(scriptForResultAndCount);
        System.out.println(scriptParamMap);
        long searchBegin = System.currentTimeMillis();
        ResultSet resultSet = titanResourceRepository.search(scriptForResultAndCount, scriptParamMap);
        LOG.info("titan search consume times:"+ (System.currentTimeMillis() - searchBegin));

        return getListViewModelResourceModel(resultSet,resType,includes);
    }

    @Override
    public ListViewModel<ResourceModel> searchWithStatistics(Set<String> resTypeSet,
                                                             List<String> includes,
                                                             Map<String, Map<String, List<String>>> params,
                                                             Map<String, String> orderMap, int from, int size,
                                                             boolean reverse,String words, boolean forceStatus,
                                                             List<String> tags, boolean showVersion){
        long generateScriptBegin = System.currentTimeMillis();
        TitanExpression titanExpression = new TitanExpression();
        titanExpression.setIncludes(includes);
        //titanExpression.setResType("resType");
        // FIXME
        titanExpression.setResTypeSet(resTypeSet);
        titanExpression.setRange(from, size);

        Map<String, Object> scriptParamMap = new HashMap<String, Object>();
        // FIXME 处理order by
        List<TitanOrder> orderList = new ArrayList<>();
        dealWithShowVersionOrder(titanExpression,resTypeSet,orderMap, showVersion, orderList);// FIXME
        dealWithOrderByEnum(titanExpression, scriptParamMap, orderMap, orderList,showVersion);
        if (isOrderBySortNum(reverse, orderMap, params.get("relation"))) {
            titanExpression.setOrderBySortNum(true, TitanKeyWords.sort_num.toString(),TitanOrder.checkSortOrder(orderMap.get(TitanKeyWords.sort_num.toString())));
        }
        titanExpression.setOrderList(orderList);
        dealWithPrintable(titanExpression, params.get("ti_printable"));
        params.remove("ti_printable");
        dealWithRelation(titanExpression, params.get("relation"), reverse);
        params.remove("relation");

        TitanQueryVertexWithWords resourceQueryVertex = new TitanQueryVertexWithWords();
        // 判断words是否要过滤edu_description
        resourceQueryVertex.setIsFilter(resTypeSet);// FIXME

        Map<String, Map<Titan_OP, List<Object>>> resourceVertexPropertyMap = new HashMap<String, Map<Titan_OP, List<Object>>>();
        resourceQueryVertex.setPropertiesMap(resourceVertexPropertyMap);
        // for now only deal with code
        dealWithSearchCode(resourceQueryVertex,
                params.get(ES_SearchField.cg_taxoncode.toString()));
        params.remove(ES_SearchField.cg_taxoncode.toString());

        dealWithSearchPath(resourceQueryVertex,
                params.get(ES_SearchField.cg_taxonpath.toString()));
        params.remove(ES_SearchField.cg_taxonpath.toString());

        dealWithSearchCoverage(resourceVertexPropertyMap,
                params.get(ES_SearchField.coverages.toString()));
        params.remove(ES_SearchField.coverages.toString());
        // TODO　处理tags tag之间存在顺序问题需要修改逻辑
        dealTags4Statistics(resourceVertexPropertyMap,tags);

        resourceQueryVertex.setWords(words);

        resourceVertexPropertyMap.put(TitanKeyWords.primary_category.toString(),generateFieldsCondtion(TitanKeyWords.primary_category.toString(), resTypeSet));// FIXME
        resourceVertexPropertyMap.put(ES_SearchField.lc_enable.toString(),
                generateFieldCondtion( ES_SearchField.lc_enable.toString(), true));
        dealWithResource(resourceQueryVertex, params);
        titanExpression.addCondition(resourceQueryVertex);

        // for count and result
        String scriptForResultAndCount = titanExpression.generateScriptForResultAndCount(scriptParamMap);
        LOG.info("titan generate script consume times:" + (System.currentTimeMillis() - generateScriptBegin));

        System.out.println(scriptForResultAndCount);
        System.out.println(scriptParamMap);
        long searchBegin = System.currentTimeMillis();
        ResultSet resultSet = titanResourceRepository.search(scriptForResultAndCount, scriptParamMap);
        LOG.info("titan search consume times:"+ (System.currentTimeMillis() - searchBegin));

        return getListViewModelResourceModel(resultSet,resTypeSet,includes);// FIXME

    }



    @Override
    public ListViewModel<ResourceModel> searchUseES(String resType, List<String> fields,
                                                    List<String> includes,
                                                    Map<String, Map<String, List<String>>> params,
                                                    Map<String, String> orderMap, int from, int size, boolean reverse, String words) {
        // 1、构建查询脚本
        EsIndexQueryBuilder builder = new EsIndexQueryBuilder();
        builder.setWords(words).setParams(params).setResType(resType).setRange(from, size).setIncludes(includes).setFields(fields);
        List<TitanOrder> orders = dealWithOrder4EsIndexQuery(orderMap);
        builder.setOrders(orders);
        String script = builder.generateScriptAfterEsUpdate();
        LOG.info("script:" + script);
        // 2、查询
        ResultSet resultSet = titanResourceRepository.search(script, null);
        // 3、解析
        return getListViewModelResourceModel(resultSet, resType,includes);
    }

    @Override
    public ListViewModel<RelationForQueryViewModel> queryListByResType(String resType, String sourceUuid, String categories, String targetType, String label, String tags, String relationType, String limit, boolean reverse,boolean recursion, String coverage) {

        long generateScriptBegin = System.currentTimeMillis();
        boolean isOrderByON=checkIsOrderByNum(resType,targetType);
        TitanExpression titanExpression = new TitanExpression();
        //titanExpression.setNeedRelationValues(true);
        titanExpression.setIncludes(null);
        titanExpression.setResType(resType);
        titanExpression.setRelationQueryOrderBy(true, isOrderByON ? TitanKeyWords.order_num.toString() : TitanKeyWords.sort_num.toString());
       /* titanExpression.setOrderByEdgeField(true);
        if (isOrderByON) {
            titanExpression.setOrderByEdgeFieldName(TitanKeyWords.order_num.toString());
        } else {
            titanExpression.setOrderByEdgeFieldName(TitanKeyWords.sort_num.toString());
        }*/

        Map<String, Object> scriptParamMap = new HashMap<String, Object>();
        Integer result[] = ParamCheckUtil.checkLimit(limit);
        //dealWithOrderAndRange(titanExpression, null, result[0], result[1]);
        titanExpression.setRange(result[0], result[1]);
        dealWithRelation4queryListByResType(titanExpression,resType,sourceUuid,label,tags,relationType,reverse,recursion);
        TitanQueryVertexWithWords resourceQueryVertex = new TitanQueryVertexWithWords();
        Map<String, Map<Titan_OP, List<Object>>> resourceVertexPropertyMap = new HashMap<String, Map<Titan_OP, List<Object>>>();
        resourceQueryVertex.setPropertiesMap(resourceVertexPropertyMap);
        resourceVertexPropertyMap.put(TitanKeyWords.primary_category.toString(),generateFieldCondtion(TitanKeyWords.primary_category.toString(), targetType));
        resourceVertexPropertyMap.put(ES_SearchField.lc_enable.toString(),generateFieldCondtion( ES_SearchField.lc_enable.toString(), true));

        // 处理维度
        // if(StringUtils.isNotEmpty(categories)) dealWithSearchCode4queryListByResType(resourceQueryVertex,Arrays.asList(categories.split(",")));
        if(StringUtils.isNotEmpty(categories)) dealWithSearchCode4queryListByResType(resourceQueryVertex,categories);
        //覆盖范围 Map<String, List<String>> coverageConditions
        if(StringUtils.isNotEmpty(coverage)) dealWithSearchCoverage4queryListByResType(resourceVertexPropertyMap,coverage);

        titanExpression.addCondition(resourceQueryVertex);

        // for count and result
        String scriptForResultAndCount = titanExpression.generateScriptForResultAndCount(scriptParamMap);
        LOG.info("titan generate script consume times:" + (System.currentTimeMillis() - generateScriptBegin));

        System.out.println(scriptForResultAndCount);
        System.out.println(scriptParamMap);
        long searchBegin = System.currentTimeMillis();
        ResultSet resultSet = titanResourceRepository.search(scriptForResultAndCount, scriptParamMap);
        LOG.info("titan search consume times:"+ (System.currentTimeMillis() - searchBegin));


        return getListViewModelRelationForQueryViewModel(resultSet,targetType,reverse);
    }

    @Override
    public ListViewModel<RelationForQueryViewModel> batchQueryResources(String resType, Set<String> sids, String targetType, String label, String tags, String relationType, String limit, boolean reverse) {

        long generateScriptBegin = System.currentTimeMillis();
        boolean isOrderByON=checkIsOrderByNum(resType,targetType);
        TitanExpression titanExpression = new TitanExpression();
        //titanExpression.setNeedRelationValues(true);
        titanExpression.setIncludes(null);
        titanExpression.setResType(resType);
        titanExpression.setRelationQueryOrderBy(true, isOrderByON ? TitanKeyWords.order_num.toString() : TitanKeyWords.sort_num.toString());
        /*titanExpression.setOrderByEdgeField(true);
        if (isOrderByON) {
            titanExpression.setOrderByEdgeFieldName(TitanKeyWords.order_num.toString());
        } else {
            titanExpression.setOrderByEdgeFieldName(TitanKeyWords.sort_num.toString());
        }*/

        Map<String, Object> scriptParamMap = new HashMap<String, Object>();
        Integer result[] = ParamCheckUtil.checkLimit(limit);
        //dealWithOrderAndRange(titanExpression, null, result[0], result[1]);
        titanExpression.setRange(result[0], result[1]);
        dealWithRelation4batchQueryResources(titanExpression,resType,sids,label,tags,relationType,reverse);
        TitanQueryVertex resourceQueryVertex = new TitanQueryVertex();
        Map<String, Map<Titan_OP, List<Object>>> resourceVertexPropertyMap = new HashMap<String, Map<Titan_OP, List<Object>>>();
        resourceQueryVertex.setPropertiesMap(resourceVertexPropertyMap);
        resourceVertexPropertyMap.put(TitanKeyWords.primary_category.toString(),generateFieldCondtion(TitanKeyWords.primary_category.toString(), targetType));
        resourceVertexPropertyMap.put(ES_SearchField.lc_enable.toString(),generateFieldCondtion( ES_SearchField.lc_enable.toString(), true));
        titanExpression.addCondition(resourceQueryVertex);

        // for count and result
        String scriptForResultAndCount = titanExpression.generateScriptForResultAndCount(scriptParamMap);
        LOG.info("titan generate script consume times:" + (System.currentTimeMillis() - generateScriptBegin));

        System.out.println(scriptForResultAndCount);
        System.out.println(scriptParamMap);
        long searchBegin = System.currentTimeMillis();
        ResultSet resultSet = titanResourceRepository.search(scriptForResultAndCount, scriptParamMap);
        LOG.info("titan search consume times:"+ (System.currentTimeMillis() - searchBegin));


        return getListViewModelRelationForQueryViewModel(resultSet,targetType,reverse);
    }

    /**
     * 新增支持sort_num的排序，目的是提供根据资源关系创建顺序自定义排序的能力     new-2016.04.07
     * 1）当reverse=false，relation参数有且只有一个的时候该排序参数生效
     * 2）目前资源仅支持 assets
     *
     * 资源未做限制
     * @param reverse
     * @param orderMap
     * @param relation
     * @return
     */
    private boolean isOrderBySortNum(boolean reverse, Map<String, String> orderMap, Map<String, List<String>> relation) {
        if (reverse) return false;
        if (CollectionUtils.isEmpty(orderMap)) return false;
        if (!orderMap.containsKey(TitanKeyWords.sort_num.toString())) return false;
        if (CollectionUtils.isEmpty(relation)) return false;
        // 只处理eq
        List<String> eq = relation.get(PropOperationConstant.OP_EQ);
        if (CollectionUtils.isEmpty(eq)) return false;
        if (eq.size() != 1) return false;
        return true;
    }

    /**
     * 章节跟课时建关系以及课时和教学目标建关系会按order_num升序显示，其余的都是sortnum
     * chapters lessons
     * lessons instructionalobjectives
     * @param resType
     * @param targetType
     * @return
     */
    private boolean checkIsOrderByNum(String resType, String targetType) {
        if (ResourceNdCode.chapters.toString().equals(resType) || ResourceNdCode.chapters.toString().equals(targetType)) {
            if (ResourceNdCode.lessons.toString().equals(resType) || ResourceNdCode.lessons.toString().equals(targetType)) {
                return true;
            }
        }
        if (ResourceNdCode.instructionalobjectives.toString().equals(resType) || ResourceNdCode.instructionalobjectives.toString().equals(targetType)) {
            if (ResourceNdCode.lessons.toString().equals(resType) || ResourceNdCode.lessons.toString().equals(targetType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 解析查询结果
     * @param resultSet
     * @param resType
     * @return
     */
    private ListViewModel<RelationForQueryViewModel> getListViewModelRelationForQueryViewModel(ResultSet resultSet, String resType,boolean reverse) {
        List<String> resultStr = new ArrayList<>();
        if (resultSet != null) {
            long getResultBegin = System.currentTimeMillis();
            Iterator<Result> iterator = resultSet.iterator();
            while (iterator.hasNext()) {
                resultStr.add(iterator.next().getString());
            }
            LOG.info("get result set consume times:" + (System.currentTimeMillis() - getResultBegin));
            return TitanResultParse2.parseToListViewRelationForQueryViewModel(resType, resultStr,reverse);
        }
        return null;
    }

    /**
     * 解析查询结果
     * @param resultSet
     * @param resType
     * @return
     */
    private ListViewModel<ResourceModel> getListViewModelResourceModel(ResultSet resultSet, String resType, List<String> includes) {
        Set<String> resTypeSet = new HashSet<>();
        resTypeSet.add(resType);
        return getListViewModelResourceModel(resultSet, resTypeSet, includes);
    }
    /**
     * 解析查询结果
     * @param resultSet
     * @param resTypeSet
     * @return
     */
    private ListViewModel<ResourceModel> getListViewModelResourceModel(ResultSet resultSet, Set<String> resTypeSet, List<String> includes) {
        List<String> resultStr = new ArrayList<>();
        if (resultSet != null) {
            long getResultBegin = System.currentTimeMillis();
            try {
                Iterator<Result> iterator = resultSet.iterator();
                while (iterator.hasNext()) {
                    resultStr.add(iterator.next().getString());
                }
            } catch (Exception e) {
                LOG.error("script error" + e.getMessage());
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR, "LC/titan/query", "out of time or script has error");
            }
            LOG.info("get result set consume times:" + (System.currentTimeMillis() - getResultBegin));
            return TitanResultParse.parseToListViewResourceModel(resTypeSet, resultStr, includes, false);
        } else {
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR, "LC/titan/query", "out of time or script has error");
        }

    }


    /**
     * 处理coverage
     * @param vertexPropertiesMap
     * @param coverage
     */
    private void dealWithSearchCoverage4queryListByResType(
            Map<String, Map<Titan_OP, List<Object>>> vertexPropertiesMap,
            String coverage) {
        if(StringUtils.isNotEmpty(coverage)){
            Map<Titan_OP, List<Object>> searchCoverageConditionMap = new HashedMap<Titan_OP, List<Object>>();
            List<Object> coverageList = new ArrayList<>();
            coverageList.add(coverage);
            Titan_OP op = Titan_OP.in;
            String[] tmp = coverage.split("/");
            if (tmp.length > 2) {
                if(tmp[0].contains("*") ||tmp[1].contains("*") ){
                    op = Titan_OP.like;
                }
            }

            if (CollectionUtils.isNotEmpty(coverageList)) searchCoverageConditionMap.put(op, coverageList);
            if (CollectionUtils.isNotEmpty(searchCoverageConditionMap)) vertexPropertiesMap.put(TitanKeyWords.search_coverage.toString(), searchCoverageConditionMap);
        }
    }

    /**
     *
     *  a.支持基础属性中的tags查询
     *  b.多个tags之间为OR的关系, 如 tags=A&tags=B
     *  c.AND关系支持方式为 tags=A and B,表示tags同时有A和B的时候才满足
     *  d.注意:tags是数组的json串存储在数据库中，假设有 tags1=["A","BC"] 和 tags2=["AB","C”]，若tags=A，则只有tags满足，也就是对单个tags是完全匹配的
     *  tags=nd and sdp.esp -->.*\"nd\".*\"sdp.esp\".*
     *  tags=nd -->.*\"nd\".*
     *  "nd","sdp.esp"
     * @param vertexPropertiesMap
     * @param tags
     */
    private void dealTags4Statistics(Map<String, Map<Titan_OP, List<Object>>> vertexPropertiesMap, List<String> tags) {
        if (CollectionUtils.isNotEmpty(tags)) {
            Map<Titan_OP, List<Object>> tagsConditionMap = new HashedMap<Titan_OP, List<Object>>();
            Set<String> tagSet = new HashSet<>();
            List<Object> tagsList = new ArrayList<>();
            tagSet.addAll(tags);
            for (String tag : tagSet) {
                if (tag.contains(" and ")) {
                    // FIXME 临时方案
                    String[] andTags = tag.split(" and ");
                    List<List<String>> permTags = new ArrayList<>();
                    int length = andTags.length;
                    perm(andTags, 0, length - 1, permTags);
                    if (CollectionUtils.isNotEmpty(permTags)) {
                        for (List<String> likeTags : permTags) {
                            int size = likeTags.size();
                            String tmp = "";
                            for (int i = 0; i < size; i++) {
                                tmp = tmp + "\\\"" + likeTags.get(i) + "\\\"";
                                if (i != size - 1) tmp = tmp + "*";
                            }
                            tmp = "*" + tmp + "*";
                            tagsList.add(tmp);
                        }
                    }
                } else {
                    tagsList.add("*\\\"" + tag + "\\\"*");
                }
            }
            if (CollectionUtils.isNotEmpty(tagsList)) tagsConditionMap.put(Titan_OP.like, tagsList);
            if (CollectionUtils.isNotEmpty(tagsConditionMap))
                vertexPropertiesMap.put(ES_Field.tags.toString(), tagsConditionMap);
        }
    }

    /**
     *
     * @param buf
     * @param start
     * @param end
     * @param permTags
     */
    private void perm(String[] buf, int start, int end, List<List<String>> permTags) {
        if (start == end) {// 当只要求对数组中一个字母进行全排列时，只要就按该数组输出即可
            List<String>  tags=new ArrayList<>();
            for (int i = 0; i <= end; i++) {
                tags.add(buf[i]);
              //  System.out.println(buf[i]);
            }
            permTags.add(tags);
            //System.out.println("\n---------------");
        } else {// 多个字母全排列
            for (int i = start; i <= end; i++) {
                String temp = buf[start];// 交换数组第一个元素与后续的元素
                buf[start] = buf[i];
                buf[i] = temp;
                perm(buf, start + 1, end, permTags);// 后续元素递归全排列
                temp = buf[start];// 将交换后的数组还原
                buf[start] = buf[i];
                buf[i] = temp;
            }
        }
    }

    /**
     * 处理coverage
     * @param vertexPropertiesMap
     * @param coverageConditions
     */
    private void dealWithSearchCoverage(
            Map<String, Map<Titan_OP, List<Object>>> vertexPropertiesMap,
            Map<String, List<String>> coverageConditions) {
        if (coverageConditions != null) {
            Map<Titan_OP, List<Object>> searchCoverageConditionMap = new HashedMap<Titan_OP, List<Object>>();
            List<Object> inCoverage = new ArrayList<>();
            List<Object> neCoverage = new ArrayList<>();
            for (Map.Entry<String, List<String>> entry : coverageConditions.entrySet()) {
                List<String> coverages = entry.getValue();
                if (CollectionUtils.isEmpty(coverages)) continue;

                for (String coverage : coverages) {
                    if (coverage == null) continue;
                    int length = coverage.split("/").length;

                    if (length == 3) {
                        coverage = coverage.replace("*", "").trim() + "/";
                    } else if (length == 4) {
                        coverage = coverage.replace("*", "").trim();
                    } else {
                        continue;
                    }

                    if (ES_OP.in.toString().equals(entry.getKey())) {
                        inCoverage.add(coverage);
                    } else if (ES_OP.ne.toString().equals(entry.getKey())) {
                        neCoverage.add(coverage);
                    }
                }

            }

            if (CollectionUtils.isNotEmpty(inCoverage)) searchCoverageConditionMap.put(Titan_OP.in, inCoverage);
            if (CollectionUtils.isNotEmpty(neCoverage)) searchCoverageConditionMap.put(Titan_OP.ne, neCoverage);
            if (CollectionUtils.isNotEmpty(searchCoverageConditionMap)) vertexPropertiesMap.put(TitanKeyWords.search_coverage.toString(), searchCoverageConditionMap);
        }

    }


    /**
     * 处理category path
     * @param resourceQueryVertex
     * @param taxonpathConditions
     */
    private void dealWithSearchPath(TitanQueryVertexWithWords resourceQueryVertex,
                                    Map<String, List<String>> taxonpathConditions) {
        if (CollectionUtils.isEmpty(taxonpathConditions)) return;

        // in
        List<String> eqTaxonpathConditions = taxonpathConditions.get(ES_OP.eq.toString());
        // deal with path eq (can contain * like)
        if (CollectionUtils.isNotEmpty(eqTaxonpathConditions)) {
            Map<String, Object> searchPathsConditions = new HashMap<>();
            List<Object> likeValueList = new ArrayList<Object>();
            List<Object> eqValueList = new ArrayList<Object>();

            for (String value : eqTaxonpathConditions) {
                if (value == null) continue;
                if (value.contains("*")) {
                    likeValueList.add(value);
                } else {
                    eqValueList.add(value);
                }
            }

            if (CollectionUtils.isNotEmpty(eqValueList)) searchPathsConditions.put(PropOperationConstant.OP_IN, eqValueList);
            if (CollectionUtils.isNotEmpty(likeValueList)) searchPathsConditions.put(PropOperationConstant.OP_LIKE, likeValueList);
            if (CollectionUtils.isNotEmpty(searchPathsConditions)) resourceQueryVertex.setSearchPathsConditions(searchPathsConditions);
        }

        // ne 目前只是支持code
        List<String> neConditions = taxonpathConditions.get(ES_OP.ne.toString());
        if (CollectionUtils.isNotEmpty(neConditions)) {
            Map<Titan_OP, List<Object>> neSearchPathConditionMap = new HashedMap<Titan_OP, List<Object>>();
            List<Object> neSearchPath = new ArrayList<Object>();
            for (String neCondition : neConditions) {
                if (neCondition == null) continue;
                neSearchPath.add(neCondition);
            }
            if (CollectionUtils.isNotEmpty(neSearchPath)) neSearchPathConditionMap.put(Titan_OP.ne, neSearchPath);
            if (CollectionUtils.isNotEmpty(neConditions)) resourceQueryVertex.getPropertiesMap().put(TitanKeyWords.search_path.toString(), neSearchPathConditionMap);
        }

    }


    private void dealWithSearchCode4queryListByResType(TitanQueryVertexWithWords resourceQueryVertex, String categories) {
        Set<String> set = new HashSet<>();
        set.addAll(Arrays.asList(categories.split(",")));
        List<String> eqConditionsCode = new ArrayList<>();
        List<String> eqConditionsPath = new ArrayList<>();
        for (String s : set) {
            if (s.contains("/")) {
                eqConditionsPath.add(s);
            } else {
                eqConditionsCode.add(s);
            }
        }

        if (CollectionUtils.isNotEmpty(eqConditionsCode)) {
            Map<String, List<String>> codeConditions = new HashMap<>();
            codeConditions.put(ES_OP.eq.toString(), eqConditionsCode);
            dealWithSearchCode(resourceQueryVertex, codeConditions);
        }
        if (CollectionUtils.isNotEmpty(eqConditionsPath)) {
            Map<String, List<String>> taxonpathConditions = new HashMap<>();
            taxonpathConditions.put(ES_OP.eq.toString(), eqConditionsPath);
            dealWithSearchPath(resourceQueryVertex, taxonpathConditions);
        }
    }

    /**
     *
     * @param resourceQueryVertex
     * @param eqConditions
     */
    private void dealWithSearchCode4queryListByResType(TitanQueryVertex resourceQueryVertex,
                                    List<String> eqConditions) {
        if (CollectionUtils.isEmpty(eqConditions)) return;

        // in
        if (CollectionUtils.isNotEmpty(eqConditions)) {
            Map<Titan_OP, List<Object>> conditionMap = new HashedMap<Titan_OP, List<Object>>();
            List<Object> codes = new ArrayList<Object>();
            for (String neCondition : eqConditions) {
                if (neCondition == null) continue;
                codes.add(neCondition);
            }
            if (CollectionUtils.isNotEmpty(codes)) conditionMap.put(Titan_OP.eq, codes);
            if (CollectionUtils.isNotEmpty(conditionMap)) resourceQueryVertex.getPropertiesMap().put(TitanKeyWords.search_code.toString(), conditionMap);
        }

    }
    /**
     * 处理category code
     * @param resourceQueryVertex
     * @param codeConditions
     */
    private void dealWithSearchCode(TitanQueryVertexWithWords resourceQueryVertex,
                                    Map<String, List<String>> codeConditions) {
        if (CollectionUtils.isEmpty(codeConditions)) return;


        // ne
        List<Object> neLikeSearchCode = new ArrayList<Object>();
        List<String> nqConditions = codeConditions.get(ES_OP.ne.toString());
        if (CollectionUtils.isNotEmpty(nqConditions)) {
            Map<String, Object> neLikeConditionMap = new HashedMap<>();
            List<Object> neSearchCode = new ArrayList<Object>();
            for (String neCondition : nqConditions) {
                if (neCondition == null) continue;
                if (neCondition.contains("*")) {
                    // ne like
                    neLikeSearchCode.add(neCondition);
                } else {
                    neSearchCode.add(neCondition);
                }
            }
            // 处理ne
            Map<Titan_OP, List<Object>> searchCodeConditionMap = new HashedMap<Titan_OP, List<Object>>();
            if (CollectionUtils.isNotEmpty(neSearchCode)) searchCodeConditionMap.put(Titan_OP.ne, neSearchCode);
            if (CollectionUtils.isNotEmpty(searchCodeConditionMap)) resourceQueryVertex.getPropertiesMap().put(TitanKeyWords.search_code.toString(), searchCodeConditionMap);
            // ne like
            if (CollectionUtils.isNotEmpty(neLikeSearchCode)) neLikeConditionMap.put(PropOperationConstant.OP_NE+PropOperationConstant.OP_LIKE, neLikeSearchCode);
            if (CollectionUtils.isNotEmpty(neLikeConditionMap)) resourceQueryVertex.setNeLikesearchCodesConditions(neLikeConditionMap);

        }





        // in
        List<String> eqConditions = codeConditions.get(ES_OP.eq.toString());
        if (CollectionUtils.isNotEmpty(eqConditions)) {
            Map<String, Object> conditionMap = new HashedMap<>();
            List<Object> inSearchCode = new ArrayList<>();
            List<Object> andSearchCode = new ArrayList<>();
            List<Object> likeSearchCode = new ArrayList<>();

            for (String eqCondition : eqConditions) {
                if (eqCondition == null) continue;
                if (eqCondition.contains(PropOperationConstant.OP_AND)) {// contains and
                    String[] codes = eqCondition.split(PropOperationConstant.OP_AND);
                    andSearchCode.add(Arrays.asList(codes));
                } else if (eqCondition.contains("*")) {// contains * like
                    likeSearchCode.add(eqCondition.trim());
                } else {
                    inSearchCode.add(eqCondition.trim());
                }
            }
            if (CollectionUtils.isNotEmpty(andSearchCode)) conditionMap.put(PropOperationConstant.OP_AND, andSearchCode);
            if (CollectionUtils.isNotEmpty(likeSearchCode)) conditionMap.put(PropOperationConstant.OP_LIKE, likeSearchCode);
            if (CollectionUtils.isNotEmpty(inSearchCode)) conditionMap.put(PropOperationConstant.OP_IN, inSearchCode);
            if (CollectionUtils.isNotEmpty(conditionMap)) resourceQueryVertex.setSearchCodesConditions(conditionMap);
        }



    }


    /**
     * 处理 order by/limit
     * @param titanExpression
     * @param orderMap
     * @param from
     * @param size
     */
    private void dealWithOrderAndRange(TitanExpression titanExpression,
                                       Map<String, String> orderMap, int from, int size) {
        // 默认使用创建时间排序，desc
        if (CollectionUtils.isEmpty(orderMap)) {
            orderMap = new HashMap<String, String>();
            orderMap.put(ES_SearchField.lc_create_time.toString(),
                    PropOperationConstant.OP_DESC);
        }
        titanExpression.setOrderMap(orderMap);
        titanExpression.setRange(from, size);

    }

    /**
     * .outE('has_tech_info').has('ti_printable',false).select('x').dedup()
     * @param titanExpression
     * @param print
     */
    private void dealWithPrintable(TitanExpression titanExpression, Map<String, List<String>> print) {
        // 只处理eq
        if (CollectionUtils.isEmpty(print)) return;
        List<String> eqPrint = print.get(PropOperationConstant.OP_EQ);
        if (CollectionUtils.isEmpty(eqPrint)) return;
        // 只处理第一个 .append(",out('").append(TitanKeyWords.has_tech_info.toString()).append("')")
        String condition = eqPrint.get(0);
        StringBuffer script = new StringBuffer(".outE('has_tech_info')");
        //.has('ti_printable',true)
        if (condition.contains("#")) {
            String[] conditions = condition.split("#");
            script.append(".has('ti_printable',").append("true".equals(conditions[0]) ? "true)" : "false)");
            script.append(".has('ti_title','").append(conditions[1]).append("')");
        } else {
            script.append(".has('ti_printable',").append("true".equals(condition) ? "true)" : "false)");
        }
        //script.append(".select('x').dedup()");
        // TODO 1、参数和脚本分离 2、常量字符替换成枚举
        titanExpression.setPrintable(true, script.toString());
    }

    /**
     * create_time,last_update,title,size(该size为资源属性tech_info中key为href中的size),statisticals,top,scores,votes,views,viplevel
     *
     * show_version
     * a.用于显示资源版本,默认false
     * b.当为true的时候显示全部资源版本
     * c.当为true的时候且oderby为空的时候,默认按m_identifier和版本升序排(ORDER BY ndr.m_identifier ASC,ndr.version ASC)
     * d.当为true的时候且oderby为不为空的时候,排序方式为:优先以m_identifier升序,再加上orderby中的排序方式
     *
     *新增支持sort_num的排序，目的是提供根据资源关系创建顺序自定义排序的能力     new-2016.04.07
     * 1）当reverse=false，relation参数有且只有一个的时候该排序参数生效
     * 2）目前资源仅支持 assets
     *
     * statistics_type,statistics_platform,仅当orderby=statisticals asc/desc时生效,其余情况忽略
     * 处理 order by
     * out('has_tech_info').has('ti_title','href').values('ti_size'),incr
     * out('has_resource_statistical').has('sta_key_title','download').has('sta_data_from','TOTAL').values('sta_key_value'),incr
     * .order().by(choose(__.out('has_category_code').has('cg_taxoncode',textRegex('RL.*')),__.values('cg_taxoncode'),__.constant('RL9999999')),incr)
     * :> g.V().has('identifier','a10f58dc-e6ab-4d16-9699-c1fab3e154d0').has('lc_enable',true).has('primary_category','chapters').outE('has_relation').has('enable',true).as('e').inV().has('lc_enable',true).has('primary_category','assets').as('x').select('x')
     * .choose(__.outE('has_resource_statistical').has('sta_key_title','downloads').has('sta_data_from','TOTAL'),select('x').outE('has_resource_statistical').has('sta_key_title','downloads').has('sta_data_from','TOTAL').values('sta_key_value'),__.constant('0.0'))
     */


    /**
     *
     * @param titanExpression
     * @param scriptParamMap
     * @param orderMap
     * @param orderList
     */
    private void dealWithOrderByEnum(TitanExpression titanExpression,
                                     Map<String, Object> scriptParamMap,
                                     Map<String, String> orderMap,
                                     List<TitanOrder> orderList,boolean isShowVersion) {
        // TODO 通过枚举生成order by 的脚本
        if (CollectionUtils.isNotEmpty(orderMap)) {
            Set<String> orderFields = orderMap.keySet();
            for (String field : orderFields) {
                TitanOrderFields.fromString(field).generateScript(titanExpression, orderMap.get(field), scriptParamMap, orderList,isShowVersion);
            }
        }
        // 默认排序
        if (CollectionUtils.isEmpty(orderList)) {
            TitanOrder order = new TitanOrder();
            order.setField(ES_SearchField.lc_create_time.toString()).setOrderByField( ES_SearchField.lc_create_time.toString()).setSortOrder(TitanOrder.SORTORDER.DESC.toString());
            orderList.add(order);
        }
        titanExpression.setOrderList(orderList);
    }

    /**
     * 处理分词检索的排序
     * 为分词检索接口设置order by字段的数据类型
     * @param orderMap
     * @return
     */
    public List<TitanOrder> dealWithOrder4EsIndexQuery(Map<String, String> orderMap) {
        List<TitanOrder> orderList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(orderMap)) {
            Set<String> orderFields = orderMap.keySet();
            for (String field : orderFields) {
                if (ES_SearchField.title.toString().equals(field) || ES_SearchField.lc_create_time.toString().equals(field) || ES_SearchField.lc_last_update.toString().equals(field)) {
                    TitanOrder order = new TitanOrder();
                    // field,field,ASC/DESC,dataType
                    order.setOrderByField(field).setField(field).setSortOrder(orderMap.get(field).toUpperCase()).setDataType(TitanUtils.convertToEsDataType(field));
                    orderList.add(order);
                }
            }
        }
        // TODO 是否要加上默认排序
        return orderList;
    }

    /**
     *
     * .select('x').aggregate('subversion').emit().repeat(outE('has_relation').has('res_type','assets').has('relation_type','VERSION').inV().aggregate('subversion')).times(1).select('subversion').unfold().dedup().as('select_version_result')
     * choose(select('x').has('has_resource_statistical').has('sta_key_title','downloads').has('sta_data_from','TOTAL'),select('x').outE('has_resource_statistical').has('sta_key_title','downloads').has('sta_data_from','TOTAL').values('sta_key_value'),__.constant('0.0'))
     * @param orderMap
     * @param showVersion
     * @param orderList
     */
    private void dealWithShowVersionOrder(TitanExpression titanExpression, Set<String> resTypeSet, Map<String, String> orderMap, boolean showVersion, List<TitanOrder> orderList) {
        if (showVersion) {
            // TODO 拼接 subversion 脚本
            StringBuffer script = new StringBuffer(".select('x').aggregate('subversion').emit().repeat(outE('has_relation').has('res_type',within(");
            for (String resType : resTypeSet) {
                script.append("'").append(resType).append("'").append(",");
            }
            // remove the last ","
            script.deleteCharAt(script.length() - 1);
            script.append(")).has('relation_type','VERSION').inV().aggregate('subversion')).times(1).select('subversion').unfold().dedup().as('version_result')");

            titanExpression.setShowSubVersion(true, script.toString(), TitanKeyWords.select_version_result.toString());
            TitanOrder o1 = new TitanOrder();
            o1.setField(ES_SearchField.m_identifier.toString());
            o1.setScript(".select('version_result').choose(select('version_result').has('" + ES_SearchField.m_identifier.toString() + "'),select('version_result').values('" + ES_SearchField.m_identifier.toString() + "'),__.constant(''))");
            o1.setSortOrder(TitanOrder.SORTORDER.ASC.toString());
            orderList.add(o1);
            if (CollectionUtils.isEmpty(orderMap)) {
                // 当为true的时候且oderby为空的时候
                TitanOrder o2 = new TitanOrder();
                o2.setField(ES_SearchField.m_identifier.toString());
                o2.setScript(".select('version_result').choose(select('version_result').has('" + ES_SearchField.lc_version.toString() + "'),select('version_result').values('" + ES_SearchField.lc_version.toString() + "'),__.constant(''))");
                o2.setSortOrder(TitanOrder.SORTORDER.ASC.toString());
                orderList.add(o2);
            }
        }
    }

    private void dealWithTaxonpath(TitanExpression titanExpression,
                                   Map<String, List<String>> taxonpathConditions) {
        if (CollectionUtils.isEmpty(taxonpathConditions)) {
            return;
        }

        TitanEdgeExpression taxonpathTitanEdgeExpression = new TitanEdgeExpression();
        taxonpathTitanEdgeExpression.setTitanOp(TitanEdgeExpression.TitanOp.or);
        titanExpression.addCondition(taxonpathTitanEdgeExpression);

        TitanQueryEdgeAndVertex titanQueryEdgeAndVertex = new TitanQueryEdgeAndVertex();
        taxonpathTitanEdgeExpression.addCondition(titanQueryEdgeAndVertex);

        TitanQueryEdge titanQueryEdge = new TitanQueryEdge();
        titanQueryEdge.setEdgeLabel(TitanKeyWords.has_categories_path
                .toString());
        titanQueryEdgeAndVertex.setTitanQueryEdge(titanQueryEdge);
        TitanQueryVertex titanQueryVertex = new TitanQueryVertex();
        titanQueryVertex.setVertexLabel(TitanKeyWords.categories_path
                .toString());
        Map<String, Map<Titan_OP, List<Object>>> vertexPropertiesMap = new HashMap<String, Map<Titan_OP, List<Object>>>();
        Map<Titan_OP, List<Object>> taxonpathConditionMap = new HashMap<Titan_OP, List<Object>>();

        List<Object> eqValueList = new ArrayList<Object>();
        List<Object> neValueList = new ArrayList<Object>();
        List<Object> likeValueList = new ArrayList<Object>();

        // deal with path eq (can contain * like)
        List<String> eqTaxonpathConditions = taxonpathConditions.get(ES_OP.eq
                .toString());

        if (CollectionUtils.isNotEmpty(eqTaxonpathConditions)) {
            for (String value : eqTaxonpathConditions) {
                if (value == null) {
                    continue;
                }
                if (value.contains("*")) {
                    likeValueList.add(value);
                } else {
                    eqValueList.add(value);
                }
            }

        }

        // deal with path ne
        List<String> neTaxonpathConditions = taxonpathConditions.get(ES_OP.ne
                .toString());

        if (CollectionUtils.isNotEmpty(neTaxonpathConditions)) {

            for (String value : neTaxonpathConditions) {
                neValueList.add(value);
            }
        }

        if (!eqValueList.isEmpty()) {
            taxonpathConditionMap.put(Titan_OP.eq, eqValueList);
        }

        if (!neValueList.isEmpty()) {
            taxonpathConditionMap.put(Titan_OP.ne, neValueList);
        }
        if (!likeValueList.isEmpty()) {
            taxonpathConditionMap.put(Titan_OP.like, likeValueList);
        }

        vertexPropertiesMap.put(ES_SearchField.cg_taxonpath.toString(),
                taxonpathConditionMap);
        titanQueryVertex.setPropertiesMap(vertexPropertiesMap);
        titanQueryEdgeAndVertex.setTitanQueryVertex(titanQueryVertex);

    }

    private void dealWithResource(TitanQueryVertex resourceQueryVertex,
                                  Map<String, Map<String, List<String>>> params) {
        if (CollectionUtils.isEmpty(params)) {
            return;
        }

        for (Map.Entry<String, Map<String, List<String>>> param : params
                .entrySet()) {
            Map<String, List<String>> conditions = param.getValue();
            if (CollectionUtils.isEmpty(conditions)) {
                continue;
            }
            Map<Titan_OP, List<Object>> fieldValueMap = new HashMap<Titan_OP, List<Object>>();
            for (Map.Entry<String, List<String>> condition : conditions
                    .entrySet()) {
                List<String> conditionValues = condition.getValue();
                if (CollectionUtils.isEmpty(conditionValues)) {
                    continue;
                }
                //System.out.println(condition.getKey());
                // System.out.println(Titan_OP.fromString(condition.getKey().toString()));
                fieldValueMap.put(Titan_OP.fromString(condition.getKey()),
                        TitanUtils.changeToTitanType(param.getKey(),
                                conditionValues));
            }
            resourceQueryVertex.getPropertiesMap().put(param.getKey(),
                    fieldValueMap);

        }

    }


    private void dealWithRelation4queryListByResType(TitanExpression titanExpression,
                                   String resType,
                                   String sourceUuid,
                                   String label,
                                   String tags,
                                   String relationType,
                                   boolean reverse,
                                   boolean recursionBoolean) {

        TitanEdgeExpression relationTitanEdgeExpression = new TitanEdgeExpression();
        relationTitanEdgeExpression.setTitanOp(TitanEdgeExpression.TitanOp.and);
        titanExpression.addCondition(relationTitanEdgeExpression);
        TitanQueryEdgeAndVertex eqRelationTitanEdgeAndVertexExpression = new TitanQueryEdgeAndVertex();
        if (reverse) {
            eqRelationTitanEdgeAndVertexExpression.setTitanDirection(TitanDirection.out);
        } else {
            eqRelationTitanEdgeAndVertexExpression.setTitanDirection(TitanDirection.in);
        }

        TitanQueryEdge titanQueryEdge = new TitanQueryEdge();
        eqRelationTitanEdgeAndVertexExpression
                .setTitanQueryEdge(titanQueryEdge);
        titanQueryEdge.setEdgeLabel(TitanKeyWords.has_relation.toString());
        TitanQueryVertex titanQueryVertex = null;
        // 关系递归（考虑章节与知识点）
        if (recursionBoolean) {
            titanQueryVertex = new TitanQueryVertexForTree();
            if (ResourceNdCode.chapters.toString().equals(resType)) {
                ((TitanQueryVertexForTree) titanQueryVertex).setTreeEdgeLabel(TitanKeyWords.tree_has_chapter.toString());
            } else if (ResourceNdCode.knowledges.toString().equals(resType)) {
                ((TitanQueryVertexForTree) titanQueryVertex).setTreeEdgeLabel(TitanKeyWords.tree_has_knowledge.toString());
            }
        } else {
            titanQueryVertex = new TitanQueryVertex();
        }

        eqRelationTitanEdgeAndVertexExpression.setTitanQueryVertex(titanQueryVertex);
        Map<String, Map<Titan_OP, List<Object>>> edgePropertiesMap = new HashMap<String, Map<Titan_OP, List<Object>>>();
        edgePropertiesMap.put(ES_Field.enable.toString(),generateFieldCondtion(ES_Field.enable.toString(), true));
        // relationType-->relation_type label-->rr_label tags-->tags
        if(StringUtils.isNotEmpty(relationType)) edgePropertiesMap.put(TitanKeyWords.relation_type.toString(), generateFieldCondtion(TitanKeyWords.relation_type.toString(), relationType));
        if(StringUtils.isNotEmpty(label)) edgePropertiesMap.put(TitanKeyWords.rr_label.toString(), generateFieldCondtion(TitanKeyWords.rr_label.toString(), label));
        // FIXME tags处理成like
        if (StringUtils.isNotEmpty(tags)) {
            Set<String> tagSet = new HashSet<>();
            tagSet.addAll(Arrays.asList(tags.split(",")));
            String tagsLike="*";
            for (String tag : tagSet) {
                //加上双引号
                tagsLike = tagsLike  +"\\\""+tag+"\\\"*";
            }
            if(!"*".equals(tagsLike)) edgePropertiesMap.put(ES_SearchField.tags.toString(), generateFieldCondtionWithLike(ES_SearchField.tags.toString(), tagsLike));
        }
        //if(StringUtils.isNotEmpty(tags)) edgePropertiesMap.put(ES_SearchField.tags.toString(), generateFieldCondtionWithLike(ES_SearchField.tags.toString(), "*" + tags + "*"));

        titanQueryEdge.setPropertiesMap(edgePropertiesMap);
        Map<String, Map<Titan_OP, List<Object>>> vertexPropertiesMap = new HashMap<String, Map<Titan_OP, List<Object>>>();
        titanQueryVertex.setPropertiesMap(vertexPropertiesMap);
        vertexPropertiesMap.put(TitanKeyWords.primary_category.toString(),generateFieldCondtion(TitanKeyWords.primary_category.toString(), resType));
        vertexPropertiesMap.put(ES_Field.identifier.toString(),generateFieldCondtion(ES_Field.identifier.toString(),sourceUuid));
        vertexPropertiesMap.put(ES_SearchField.lc_enable.toString(),generateFieldCondtion(ES_SearchField.lc_enable.toString(), true));
        if (titanExpression.getFirstTitanQueryEdgeAndVertex() == null) {
            titanExpression.setFirstTitanQueryEdgeAndVertex(eqRelationTitanEdgeAndVertexExpression);
        }
        else {
            relationTitanEdgeExpression.addCondition(eqRelationTitanEdgeAndVertexExpression);
        }

    }
    private void dealWithRelation4batchQueryResources(TitanExpression titanExpression,
                                   String resType,
                                   Set<String> sourceUuid,
                                   String label,
                                   String tags,
                                   String relationType,
                                   boolean reverse) {

        TitanEdgeExpression relationTitanEdgeExpression = new TitanEdgeExpression();
        relationTitanEdgeExpression.setTitanOp(TitanEdgeExpression.TitanOp.and);
        titanExpression.addCondition(relationTitanEdgeExpression);
        TitanQueryEdgeAndVertex eqRelationTitanEdgeAndVertexExpression = new TitanQueryEdgeAndVertex();
        if (reverse) {
            eqRelationTitanEdgeAndVertexExpression.setTitanDirection(TitanDirection.out);
        } else {
            eqRelationTitanEdgeAndVertexExpression.setTitanDirection(TitanDirection.in);
        }

        TitanQueryEdge titanQueryEdge = new TitanQueryEdge();
        eqRelationTitanEdgeAndVertexExpression
                .setTitanQueryEdge(titanQueryEdge);
        titanQueryEdge.setEdgeLabel(TitanKeyWords.has_relation.toString());
        TitanQueryVertex titanQueryVertex = new TitanQueryVertex();

        eqRelationTitanEdgeAndVertexExpression.setTitanQueryVertex(titanQueryVertex);
        Map<String, Map<Titan_OP, List<Object>>> edgePropertiesMap = new HashMap<String, Map<Titan_OP, List<Object>>>();
        edgePropertiesMap.put(ES_Field.enable.toString(),generateFieldCondtion(ES_Field.enable.toString(), true));
        // relationType-->relation_type label-->rr_label tags-->tags
        if(StringUtils.isNotEmpty(relationType)) edgePropertiesMap.put(TitanKeyWords.relation_type.toString(), generateFieldCondtion(TitanKeyWords.relation_type.toString(), relationType));
        if(StringUtils.isNotEmpty(label)) edgePropertiesMap.put(TitanKeyWords.rr_label.toString(), generateFieldCondtion(TitanKeyWords.rr_label.toString(), label));
        // FIXME tags处理成like
        if (StringUtils.isNotEmpty(tags)) {
            Set<String> tagSet = new HashSet<>();
            tagSet.addAll(Arrays.asList(tags.split(",")));
            String tagsLike="*";
            for (String tag : tagSet) {
                tagsLike = tagsLike  +"\\\""+tag+"\\\"" + "*";
            }
            if(!"*".equals(tagsLike)) edgePropertiesMap.put(ES_SearchField.tags.toString(), generateFieldCondtionWithLike(ES_SearchField.tags.toString(), tagsLike));
        }
       // if(StringUtils.isNotEmpty(tags)) edgePropertiesMap.put(ES_SearchField.tags.toString(), generateFieldCondtionWithLike(ES_SearchField.tags.toString(), "*" + tags + "*"));

        titanQueryEdge.setPropertiesMap(edgePropertiesMap);
        Map<String, Map<Titan_OP, List<Object>>> vertexPropertiesMap = new HashMap<String, Map<Titan_OP, List<Object>>>();
        titanQueryVertex.setPropertiesMap(vertexPropertiesMap);
        vertexPropertiesMap.put(TitanKeyWords.primary_category.toString(),generateFieldCondtion(TitanKeyWords.primary_category.toString(), resType));
        //Map<Titan_OP, List<Object>>
        vertexPropertiesMap.put(ES_Field.identifier.toString(),generateFieldsCondtion(ES_Field.identifier.toString(),sourceUuid));
        vertexPropertiesMap.put(ES_SearchField.lc_enable.toString(),generateFieldCondtion(ES_SearchField.lc_enable.toString(), true));
        if (titanExpression.getFirstTitanQueryEdgeAndVertex() == null) {
            titanExpression.setFirstTitanQueryEdgeAndVertex(eqRelationTitanEdgeAndVertexExpression);
        }
        else {
            relationTitanEdgeExpression.addCondition(eqRelationTitanEdgeAndVertexExpression);
        }

    }


    /**
     * @param titanExpression
     * @param relationConditions
     * @author gsw
     */
    private void dealWithRelation4Statistics(TitanExpression titanExpression,
                                  Map<String, List<String>> relationConditions, boolean reverse) {

        // for now only deal with eq
        if (CollectionUtils.isEmpty(relationConditions)) {
            return;
        }
        List<String> eqRelationConditions = relationConditions
                .get(PropOperationConstant.OP_EQ.toString());

        if (CollectionUtils.isEmpty(eqRelationConditions)) {
            return;
        }

        TitanEdgeExpression relationTitanEdgeExpression = new TitanEdgeExpression();
        relationTitanEdgeExpression.setTitanOp(TitanEdgeExpression.TitanOp.and);
        titanExpression.addCondition(relationTitanEdgeExpression);
        for (String eqRelationCondition : eqRelationConditions) {
            if (StringUtils.isEmpty(eqRelationCondition)) {
                continue;
            }
            String[] chunks = eqRelationCondition.split("/");
            if (chunks.length != 3) {
                continue;
            }
            TitanQueryEdgeAndVertex eqRelationTitanEdgeAndVertexExpression = new TitanQueryEdgeAndVertex();

            if (reverse) {
                eqRelationTitanEdgeAndVertexExpression.setTitanDirection(TitanDirection.out);
            } else {
                eqRelationTitanEdgeAndVertexExpression.setTitanDirection(TitanDirection.in);
            }
            // 边
            TitanQueryEdge titanQueryEdge = new TitanQueryEdge();
            eqRelationTitanEdgeAndVertexExpression.setTitanQueryEdge(titanQueryEdge);

            titanQueryEdge.setEdgeLabel(TitanKeyWords.has_relation.toString());
            //
            TitanQueryVertex titanQueryVertex = null;
            // 关系递归（考虑章节与知识点）
            if (chunks[1].endsWith("$")) {
                chunks[1] = chunks[1].substring(0, chunks[1].length() - 1); // 去除$标志
                titanQueryVertex = new TitanQueryVertexForTree();
                if (ResourceNdCode.chapters.toString().equals(chunks[0])) {
                    ((TitanQueryVertexForTree) titanQueryVertex).setTreeEdgeLabel(TitanKeyWords.tree_has_chapter
                            .toString());
                } else if (ResourceNdCode.knowledges.toString().equals(
                        chunks[0])) {
                    ((TitanQueryVertexForTree) titanQueryVertex).setTreeEdgeLabel(TitanKeyWords.tree_has_knowledge
                            .toString());
                }

            } else {
                titanQueryVertex = new TitanQueryVertex();
            }

            eqRelationTitanEdgeAndVertexExpression.setTitanQueryVertex(titanQueryVertex);

            Map<String, Map<Titan_OP, List<Object>>> edgePropertiesMap = new HashMap<String, Map<Titan_OP, List<Object>>>();
            edgePropertiesMap.put(ES_Field.enable.toString(), generateFieldCondtion(ES_Field.enable.toString(), true));
            // if(!"*".equals(chunks[2])){
            // edgePropertiesMap.put(key, chunks[2]);
            // }
            titanQueryEdge.setPropertiesMap(edgePropertiesMap);
            Map<String, Map<Titan_OP, List<Object>>> vertexPropertiesMap = new HashMap<String, Map<Titan_OP, List<Object>>>();
            titanQueryVertex.setPropertiesMap(vertexPropertiesMap);
            vertexPropertiesMap.put(
                    TitanKeyWords.primary_category.toString(),
                    generateFieldCondtion(TitanKeyWords.primary_category.toString(),
                            chunks[0]));
            vertexPropertiesMap.put(
                    ES_Field.identifier.toString(),
                    generateFieldCondtion(ES_Field.identifier.toString(),
                            chunks[1]));
            vertexPropertiesMap.put(
                    ES_SearchField.lc_enable.toString(),
                    generateFieldCondtion(ES_SearchField.lc_enable.toString(),
                            true));
            if (titanExpression.getFirstTitanQueryEdgeAndVertex() == null) {
                titanExpression
                        .setFirstTitanQueryEdgeAndVertex(eqRelationTitanEdgeAndVertexExpression);
            }
            else {
                relationTitanEdgeExpression
                        .addCondition(eqRelationTitanEdgeAndVertexExpression);
            }

        }

    }

    /**
     * @param titanExpression
     * @param relationConditions
     * @author linsm
     */
    private void dealWithRelation(TitanExpression titanExpression,
                                  Map<String, List<String>> relationConditions, boolean reverse) {
        /*
         * for (Map<String, String> relation : relations) { String r =
		 * relation.get("stype") + "/" + relation.get("suuid") + "/"; if
		 * (relation.get("rtype") == null) { r += "*"; } else { r +=
		 * relation.get("rtype"); } reEq.add(r); }
		 */

        // for now only deal with eq
        if (CollectionUtils.isEmpty(relationConditions)) {
            return;
        }
        List<String> eqRelationConditions = relationConditions
                .get(PropOperationConstant.OP_EQ.toString());

        if (CollectionUtils.isEmpty(eqRelationConditions)) {
            return;
        }

        TitanEdgeExpression relationTitanEdgeExpression = new TitanEdgeExpression();
        relationTitanEdgeExpression.setTitanOp(TitanEdgeExpression.TitanOp.and);
        titanExpression.addCondition(relationTitanEdgeExpression);
        for (String eqRelationCondition : eqRelationConditions) {
            if (StringUtils.isEmpty(eqRelationCondition)) {
                continue;
            }
            // System.out.println(eqRelationCondition);
            String[] chunks = eqRelationCondition.split("/");
            if (chunks.length != 3) {
                continue;
            }
            TitanQueryEdgeAndVertex eqRelationTitanEdgeAndVertexExpression = new TitanQueryEdgeAndVertex();

            if (reverse) {
                eqRelationTitanEdgeAndVertexExpression
                        .setTitanDirection(TitanDirection.out);
            } else {
                eqRelationTitanEdgeAndVertexExpression
                        .setTitanDirection(TitanDirection.in);
            }
            TitanQueryEdge titanQueryEdge = new TitanQueryEdge();
            eqRelationTitanEdgeAndVertexExpression
                    .setTitanQueryEdge(titanQueryEdge);
            titanQueryEdge.setEdgeLabel(TitanKeyWords.has_relation.toString());
            TitanQueryVertex titanQueryVertex = null;
            // 关系递归（考虑章节与知识点）
            if (chunks[1].endsWith("$")) {
                chunks[1] = chunks[1].substring(0, chunks[1].length() - 1); // 去除$标志
                titanQueryVertex = new TitanQueryVertexForTree();
                if (ResourceNdCode.chapters.toString().equals(chunks[0])) {
                    ((TitanQueryVertexForTree) titanQueryVertex).setTreeEdgeLabel(TitanKeyWords.tree_has_chapter
                            .toString());
                } else if (ResourceNdCode.knowledges.toString().equals(
                        chunks[0])) {
                    ((TitanQueryVertexForTree) titanQueryVertex).setTreeEdgeLabel(TitanKeyWords.tree_has_knowledge
                            .toString());
                }
                // service层，不必考虑参数非法；
                // else {
                // throw new LifeCircleException(
                // HttpStatus.INTERNAL_SERVER_ERROR, "LC/titan/QUERY",
                // "recursive relation only support chapter or knowledge");
                // }

            } else {
                titanQueryVertex = new TitanQueryVertex();
            }
            eqRelationTitanEdgeAndVertexExpression
                    .setTitanQueryVertex(titanQueryVertex);
//            titanQueryVertex.setVertexLabel(chunks[0]);
            Map<String, Map<Titan_OP, List<Object>>> edgePropertiesMap = new HashMap<String, Map<Titan_OP, List<Object>>>();
            edgePropertiesMap.put(ES_Field.enable.toString(),
                    generateFieldCondtion(ES_Field.enable.toString(), true));
            // if(!"*".equals(chunks[2])){
            // edgePropertiesMap.put(key, chunks[2]);
            // }
            titanQueryEdge.setPropertiesMap(edgePropertiesMap);
            Map<String, Map<Titan_OP, List<Object>>> vertexPropertiesMap = new HashMap<String, Map<Titan_OP, List<Object>>>();
            titanQueryVertex.setPropertiesMap(vertexPropertiesMap);
            vertexPropertiesMap.put(
                    TitanKeyWords.primary_category.toString(),
                    generateFieldCondtion(TitanKeyWords.primary_category.toString(),
                            chunks[0]));
            vertexPropertiesMap.put(
                    ES_Field.identifier.toString(),
                    generateFieldCondtion(ES_Field.identifier.toString(),
                            chunks[1]));
            vertexPropertiesMap.put(
                    ES_SearchField.lc_enable.toString(),
                    generateFieldCondtion(ES_SearchField.lc_enable.toString(),
                            true));
            if (titanExpression.getFirstTitanQueryEdgeAndVertex() == null) {
                titanExpression
                        .setFirstTitanQueryEdgeAndVertex(eqRelationTitanEdgeAndVertexExpression);
            } 
            /*else if (eqRelationTitanEdgeAndVertexExpression
                    .getTitanQueryVertex() instanceof TitanQueryVertexForTree
                    || (!(titanExpression.getFirstTitanQueryEdgeAndVertex()
                    .getTitanQueryVertex() instanceof TitanQueryVertexForTree)
                    && titanExpression
                    .getFirstTitanQueryEdgeAndVertex()
                    .getTitanQueryVertex().getVertexLabel()
                    .equals(ResourceNdCode.chapters.toString()) && !eqRelationTitanEdgeAndVertexExpression
                    .getTitanQueryVertex().getVertexLabel()
                    .equals(ResourceNdCode.chapters.toString()))) {

                // 选择条件：有递归树时，一定使用，优化使用非章节的关系（章节上资源较多）
                relationTitanEdgeExpression.addCondition(titanExpression
                        .getFirstTitanQueryEdgeAndVertex());
                titanExpression
                        .setFirstTitanQueryEdgeAndVertex(eqRelationTitanEdgeAndVertexExpression);
            }*/
            else {
                relationTitanEdgeExpression
                        .addCondition(eqRelationTitanEdgeAndVertexExpression);
            }

        }

    }

    private Map<Titan_OP, List<Object>> generateFieldCondtion(String fieldName,
                                                              Object value) {
        Map<Titan_OP, List<Object>> propertiesMap = new HashMap<Titan_OP, List<Object>>();
        List<Object> properties = new ArrayList<Object>();
        properties.add(value);
        if (ES_SearchField.cg_taxoncode.toString().equals(fieldName)
                && value instanceof String && ((String) value).contains("*")) {
            propertiesMap.put(Titan_OP.like, properties);
        } else {
            propertiesMap.put(Titan_OP.eq, properties);
        }

        return propertiesMap;
    }

    private Map<Titan_OP, List<Object>> generateFieldsCondtion(String fieldName,
                                                              Object value) {
        Map<Titan_OP, List<Object>> propertiesMap = new HashMap<Titan_OP, List<Object>>();
        List<Object> properties = new ArrayList<Object>();
        if (value instanceof Set) {
            properties.addAll(Arrays.asList(((Set) value).toArray()));
        } else {
            properties.add(value);
        }
        if (ES_SearchField.cg_taxoncode.toString().equals(fieldName)
                && value instanceof String && ((String) value).contains("*")) {
            propertiesMap.put(Titan_OP.like, properties);
        } else {
            propertiesMap.put(Titan_OP.eq, properties);
        }

        return propertiesMap;
    }

    private Map<Titan_OP, List<Object>> generateFieldCondtionWithLike(String fieldName,
                                                              Object value) {
        Map<Titan_OP, List<Object>> propertiesMap = new HashMap<Titan_OP, List<Object>>();
        List<Object> properties = new ArrayList<Object>();
        properties.add(value);
        if (ES_SearchField.tags.toString().equals(fieldName)
                && value instanceof String && ((String) value).contains("*")) {
            propertiesMap.put(Titan_OP.like, properties);
        } else {
            propertiesMap.put(Titan_OP.eq, properties);
        }

        return propertiesMap;
    }



    /**
     * for now only deal with code (eq),not deal with like (*)
     *
     * @param titanExpression
     * @param categoryConditions
     * @author linsm
     */
    private void dealWithTaxoncode(TitanExpression titanExpression,
                                   Map<String, List<String>> categoryConditions) {
        if (CollectionUtils.isEmpty(categoryConditions)) {
            return;
        }
        List<String> eqCategoryConditions = categoryConditions.get(ES_OP.eq
                .toString());

        if (CollectionUtils.isEmpty(eqCategoryConditions)) {
            return;
        }
        TitanEdgeExpression categoryTitanEdgeExpression = new TitanEdgeExpression();
        categoryTitanEdgeExpression.setTitanOp(TitanEdgeExpression.TitanOp.or);
        titanExpression.addCondition(categoryTitanEdgeExpression);
        for (String eqCategoryCondition : eqCategoryConditions) {
            if (eqCategoryCondition.contains(PropOperationConstant.OP_AND)) {
                TitanEdgeExpression andCategoryTitanEdgeExpression = new TitanEdgeExpression();
                andCategoryTitanEdgeExpression
                        .setTitanOp(TitanEdgeExpression.TitanOp.and);
                categoryTitanEdgeExpression
                        .addCondition(andCategoryTitanEdgeExpression);
                String[] chunkCodes = eqCategoryCondition
                        .split(PropOperationConstant.OP_AND);
                for (String chunkCode : chunkCodes) {
                    if (StringUtils.isNotEmpty(chunkCode)) {
                        String realValue = chunkCode.trim();
                        setCategoryCodeCondition(
                                andCategoryTitanEdgeExpression, realValue);
                    }
                }
            } else {
                setCategoryCodeCondition(categoryTitanEdgeExpression,
                        eqCategoryCondition);
            }
        }

    }

    private void setCategoryCodeCondition(
            TitanEdgeExpression categoryTitanEdgeExpression, String realValue) {

        TitanQueryEdgeAndVertex categoryTitanQueryEdgeAndVertex = new TitanQueryEdgeAndVertex();
        categoryTitanEdgeExpression
                .addCondition(categoryTitanQueryEdgeAndVertex);
        TitanQueryEdge titanQueryEdge = new TitanQueryEdge();
        categoryTitanQueryEdgeAndVertex.setTitanQueryEdge(titanQueryEdge);
        titanQueryEdge.setEdgeLabel(TitanKeyWords.has_category_code.toString());
        TitanQueryVertex titanQueryVertex = new TitanQueryVertex();
        categoryTitanQueryEdgeAndVertex.setTitanQueryVertex(titanQueryVertex);
        titanQueryVertex.setVertexLabel(TitanKeyWords.category_code.toString());
        Map<String, Map<Titan_OP, List<Object>>> vertexPropertiesMap = new HashMap<String, Map<Titan_OP, List<Object>>>();
        vertexPropertiesMap
                .put(ES_SearchField.cg_taxoncode.toString(),
                        generateFieldCondtion(ES_SearchField.cg_taxoncode.toString(),
                                realValue));
        titanQueryVertex.setPropertiesMap(vertexPropertiesMap);

    }

    /**
     * @param titanExpression
     * @param coverageConditions
     * @author linsm
     */
    private void dealWithCoverage(TitanExpression titanExpression,
                                  Map<String, List<String>> coverageConditions) {
        if (coverageConditions != null) {
            for (Map.Entry<String, List<String>> entry : coverageConditions
                    .entrySet()) {
                // for now only deal with in
                if (ES_OP.in.toString().equals(entry.getKey())) {
                    // not deal with status
                    List<String> coverages = entry.getValue();
                    if (CollectionUtils.isEmpty(coverages)) {
                        continue;
                    }
                    TitanEdgeExpression coverageTitanEdgeExpression = new TitanEdgeExpression();
                    coverageTitanEdgeExpression
                            .setTitanOp(TitanEdgeExpression.TitanOp.or);
                    for (String coverage : coverages) {
                        String[] chunks = coverage.split("/");
                        if (chunks == null
                                || (chunks.length != 3 && chunks.length != 4)) {
                            continue;
                        }
                        TitanQueryEdgeAndVertex coverageTitanQueryEdgeAndVertex = null;
                        if (chunks.length == 4) {
                            // status;
                            TitanQueryEdgeAndVertexForCoverage coverageTitanQueryEdgeAndVertexForCoverage = new TitanQueryEdgeAndVertexForCoverage();
                            coverageTitanQueryEdgeAndVertexForCoverage
                                    .setStatus(chunks[3]);
                            coverageTitanQueryEdgeAndVertex = coverageTitanQueryEdgeAndVertexForCoverage;
                        } else {
                            coverageTitanQueryEdgeAndVertex = new TitanQueryEdgeAndVertex();
                        }

                        TitanQueryEdge titanQueryEdge = new TitanQueryEdge();
                        coverageTitanQueryEdgeAndVertex
                                .setTitanQueryEdge(titanQueryEdge);
                        titanQueryEdge.setEdgeLabel(TitanKeyWords.has_coverage
                                .toString());
                        TitanQueryVertex titanQueryVertex = new TitanQueryVertex();
                        coverageTitanQueryEdgeAndVertex
                                .setTitanQueryVertex(titanQueryVertex);
                        titanQueryVertex.setVertexLabel(TitanKeyWords.coverage
                                .toString());

                        if (!"*".equals(chunks[0])) {
                            titanQueryVertex.getPropertiesMap().put(
                                    ES_Field.target_type.toString(),
                                    generateFieldCondtion(
                                            ES_Field.target_type.toString(),
                                            chunks[0]));
                        }
                        if (!"*".equals(chunks[1])) {
                            titanQueryVertex.getPropertiesMap().put(
                                    ES_Field.target.toString(),
                                    generateFieldCondtion(
                                            ES_Field.target.toString(),
                                            chunks[1]));
                        }
                        if (!"*".equals(chunks[2])) {
                            titanQueryVertex.getPropertiesMap().put(
                                    ES_Field.strategy.toString(),
                                    generateFieldCondtion(
                                            ES_Field.strategy.toString(),
                                            chunks[2]));
                        }
                        coverageTitanEdgeExpression
                                .addCondition(coverageTitanQueryEdgeAndVertex);
                    }
                    titanExpression.addCondition(coverageTitanEdgeExpression);
                }
            }
        }

    }

    /*************************
     * TEST
     **************************************/

    public static void main(String[] args) {
        /*Map<String, Map<String, List<String>>> params = new HashMap<String, Map<String, List<String>>>();

        Map<String, List<String>> coverageConditions = new HashMap<String, List<String>>();
        params.put(ES_SearchField.coverages.toString(), coverageConditions);
        List<String> inCoverageConditions = new ArrayList<String>();
        coverageConditions.put(ES_OP.in.toString(), inCoverageConditions);

        inCoverageConditions.add("Org/nd/OWNER/ONLINE");
        inCoverageConditions.add("User/890399/VIEW");

        System.out.println(params);*/
        // new TitanSearchServiceImpl().search(ResourceNdCode.tools.toString(),
        // null, params, null, 0, 0, false);
        TitanSearchServiceImpl t = new TitanSearchServiceImpl();
        List<String> list = testData();
        // t.generateResourceModel(list);


    }

    public static List<String> testData() {
        List<String> list = new ArrayList<String>();
        list.add("{preview=[{\"240\":\"\"}], cr_author=[UNKNOW], identifier=[9f665a4d-96b0-4ead-a8b5-7d11d06980ab], cr_right=[UNKNOW], lc_last_update=[1464167860717], keywords=[[]], lc_version=[v1.0], description=[], language=[zh_CN], lc_status=[ONLINE], custom_properties=[{\"app_version\":\"\",\"apk_package_name\":\"\",\"apk_activity_name\":\"\",\"chapters\":\"[{\\\"id\\\":\\\"805736b2-c78e-4c79-94f9-7568552fac57\\\",\\\"desc\\\":\\\"人教版>上册>小学>一年级>语文>识字（一）>1 一去二三里\\\",\\\"phase\\\":\\\"$ON020000\\\",\\\"grade\\\":\\\"$ON020100\\\",\\\"subject\\\":\\\"$SB0100\\\",\\\"edition\\\":\\\"$E001000\\\",\\\"subedition\\\":\\\"$E001001\\\",\\\"$$hashKey\\\":\\\"0ZE\\\"}]\"}], title=[img], tags=[[\"\"]], lc_provider=[NetDragon], lc_enable=[true], lc_provider_source=[img.dat], lc_creator=[2107196619], lc_create_time=[1464166854516]}");
        list.add("==>{taxoncode=[$ON020100]}");
        list.add("==>{taxoncode=[$SB0100]}");
        list.add("==>{taxoncode=[$ON020000]}");
        list.add("==>{taxonpath=[K12/$ON020000/$ON020100/$SB0100/$E001000/$E001001]}");
        list.add("==>{identifier=[18196da8-85a1-4241-a79d-3a8ce3196a8b], ti_md5=[], ti_title=[source], ti_location=[${ref-path}/prepub_content_edu_product/esp/assets/9f665a4d-96b0-4ead-a8b5-7d11d06980ab.pkg/4251f652-3fec-4344-b3d2-7196ff011030.dat], ti_size=[196], ti_format=[application/octet-stream], ti_requirements=[[]]}");
        list.add("==>{identifier=[b2281434-3654-4b5c-940a-e22dd81c7843], ti_md5=[], ti_title=[href], ti_location=[${ref-path}/prepub_content_edu_product/esp/assets/9f665a4d-96b0-4ead-a8b5-7d11d06980ab.pkg/4251f652-3fec-4344-b3d2-7196ff011030.dat], ti_size=[196], ti_format=[application/octet-stream], ti_requirements=[null]}");
        return list;
    }

}
