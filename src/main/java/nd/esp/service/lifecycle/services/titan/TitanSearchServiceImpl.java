package nd.esp.service.lifecycle.services.titan;

import java.util.*;

import com.google.gson.reflect.TypeToken;

import nd.esp.service.lifecycle.daos.titan.inter.TitanResourceRepository;
import nd.esp.service.lifecycle.educommon.models.*;
import nd.esp.service.lifecycle.educommon.vos.constant.PropOperationConstant;
import nd.esp.service.lifecycle.models.teachingmaterial.v06.TeachingMaterialModel;
import nd.esp.service.lifecycle.models.teachingmaterial.v06.TmExtPropertiesModel;
import nd.esp.service.lifecycle.models.v06.EbookExtPropertiesModel;
import nd.esp.service.lifecycle.models.v06.EbookModel;
import nd.esp.service.lifecycle.models.v06.QuestionExtPropertyModel;
import nd.esp.service.lifecycle.models.v06.QuestionModel;
import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.support.busi.titan.TitanDirection;
import nd.esp.service.lifecycle.support.busi.titan.TitanEdgeExpression;
import nd.esp.service.lifecycle.support.busi.titan.TitanExpression;
import nd.esp.service.lifecycle.support.busi.titan.TitanKeyWords;
import nd.esp.service.lifecycle.support.busi.titan.TitanQueryEdge;
import nd.esp.service.lifecycle.support.busi.titan.TitanQueryEdgeAndVertex;
import nd.esp.service.lifecycle.support.busi.titan.TitanQueryEdgeAndVertexForCoverage;
import nd.esp.service.lifecycle.support.busi.titan.TitanQueryVertex;
import nd.esp.service.lifecycle.support.busi.titan.TitanQueryVertexForTree;
import nd.esp.service.lifecycle.support.busi.titan.TitanQueryVertexWithWords;
import nd.esp.service.lifecycle.support.busi.titan.TitanUtils;
import nd.esp.service.lifecycle.support.busi.titan.Titan_OP;
import nd.esp.service.lifecycle.support.enums.ES_Field;
import nd.esp.service.lifecycle.support.enums.ES_OP;
import nd.esp.service.lifecycle.support.enums.ES_SearchField;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;
import nd.esp.service.lifecycle.vos.ListViewModel;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.driver.ResultSet;
import org.elasticsearch.index.query.QueryBuilder;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
        System.out.println("params:" + params);
        System.out.println("cg_taxoncode:" + params.get(ES_SearchField.cg_taxoncode.toString()));
        System.out.println("cg_taxonpath:" + params.get(ES_SearchField.cg_taxonpath.toString()));
        System.out.println("coverages:" + params.get(ES_SearchField.coverages.toString()));
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

        ListViewModel<ResourceModel> viewModels = new ListViewModel<ResourceModel>();
        List<ResourceModel> items = new ArrayList<ResourceModel>();

        //for count and result
        String scriptForResultAndCount = titanExpression.generateScriptForResultAndCount(scriptParamMap);
        LOG.info("titan generate script consume times:" + (System.currentTimeMillis() - generateScriptBegin));

        System.out.println(scriptForResultAndCount);
        System.out.println(scriptParamMap);
        long searchBegin = System.currentTimeMillis();
        ResultSet resultSet = titanResourceRepository.search(scriptForResultAndCount, scriptParamMap);
        LOG.info("titan search consume times:" + (System.currentTimeMillis() - searchBegin));

        List<String> resultStr = new ArrayList<>();
        long getResultBegin = System.currentTimeMillis();
        Iterator<Result> iterator = resultSet.iterator();
        while (iterator.hasNext()) {
            resultStr.add(iterator.next().getString());
        }
        LOG.info("get resultset consume times:" + (System.currentTimeMillis() - getResultBegin));

        long parseBegin = System.currentTimeMillis();
        List<String> otherLines = new ArrayList<>();
        String taxOnPath = null;
        String mainResult = null;
        int count = 0;
        for (String line : resultStr) {
            if (count > 0 && (line.contains(ES_SearchField.lc_create_time.toString()) || line.startsWith(TitanKeyWords.TOTALCOUNT.toString()))) {
                items.add(getItem(resType, mainResult, otherLines, taxOnPath));
                otherLines.clear();
                taxOnPath = null;
            }

            if (line.startsWith(TitanKeyWords.TOTALCOUNT.toString())) {
                viewModels.setTotal(Long.parseLong(line.split(":")[1].trim()));
            } else if (line.contains(ES_SearchField.cg_taxonpath.toString())) {
                line = line.split("=")[1];
                int length = line.length();
                if (length > 2) {
                    taxOnPath = line.substring(1, length - 2);
                }
            } else if (line.contains(ES_SearchField.lc_create_time.toString())) {
                mainResult = line;
            } else {
                otherLines.add(line);
            }
            count++;
        }
        LOG.info("parse consume times:" + (System.currentTimeMillis() - parseBegin));

        viewModels.setItems(items);
        return viewModels;

    }


    @Override
    public ListViewModel<ResourceModel> searchWithAdditionProperties(
            String resType, List<String> includes,
            Map<String, Map<String, List<String>>> params,
            Map<String, String> orderMap, int from, int size, boolean reverse,
            String words) {
        System.out.println("params:" + params);
        System.out.println("cg_taxoncode:" + params.get(ES_SearchField.cg_taxoncode.toString()));
        System.out.println("cg_taxonpath:" + params.get(ES_SearchField.cg_taxonpath.toString()));
        System.out.println("coverages:" + params.get(ES_SearchField.coverages.toString()));
        long generateScriptBegin = System.currentTimeMillis();
        TitanExpression titanExpression = new TitanExpression();

        Map<String, Object> scriptParamMap = new HashMap<String, Object>();

        dealWithOrderAndRange(titanExpression, orderMap, from, size);
        dealWithRelation(titanExpression, params.get("relation"), reverse);
        params.remove("relation");

        TitanQueryVertexWithWords resourceQueryVertex = new TitanQueryVertexWithWords();

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

        resourceVertexPropertyMap.put("primary_category",
                generateFieldCondtion("primary_category", resType));
        resourceVertexPropertyMap
                .put(ES_SearchField.lc_enable.toString(),
                        generateFieldCondtion(
                                ES_SearchField.lc_enable.toString(), true));
        dealWithResource(resourceQueryVertex, params);
        titanExpression.addCondition(resourceQueryVertex);

        ListViewModel<ResourceModel> viewModels = new ListViewModel<ResourceModel>();
        List<ResourceModel> items = new ArrayList<ResourceModel>();

        // for count and result
        String scriptForResultAndCount = titanExpression
                .generateScriptForResultAndCount(scriptParamMap);
        LOG.info("titan generate script consume times:"
                + (System.currentTimeMillis() - generateScriptBegin));

        System.out.println(scriptForResultAndCount);
        System.out.println(scriptParamMap);
        long searchBegin = System.currentTimeMillis();
        ResultSet resultSet = titanResourceRepository.search(
                scriptForResultAndCount, scriptParamMap);
        LOG.info("titan search consume times:"
                + (System.currentTimeMillis() - searchBegin));

        List<String> resultStr = new ArrayList<>();
        long getResultBegin = System.currentTimeMillis();
        Iterator<Result> iterator = resultSet.iterator();
        while (iterator.hasNext()) {
            resultStr.add(iterator.next().getString());
        }
        //System.out.println(resultStr);
        LOG.info("get resultset consume times:"
                + (System.currentTimeMillis() - getResultBegin));

        long parseBegin = System.currentTimeMillis();
        List<String> otherLines = new ArrayList<>();
        String taxOnPath = null;
        String mainResult = null;
        int count = 0;
        for (String line : resultStr) {
            if (count > 0
                    && (line.contains(ES_SearchField.lc_create_time.toString()) || line
                    .startsWith(TitanKeyWords.TOTALCOUNT.toString()))) {
                items.add(getItem(resType, mainResult, otherLines, taxOnPath));
                otherLines.clear();
                taxOnPath = null;
            }

            if (line.startsWith(TitanKeyWords.TOTALCOUNT.toString())) {
                viewModels.setTotal(Long.parseLong(line.split(":")[1].trim()));
            } else if (line.contains(ES_SearchField.cg_taxonpath.toString())) {
                line = line.split("=")[1];
                int length = line.length();
                if (length > 2) {
                    taxOnPath = line.substring(1, length - 2);
                }
            } else if (line.contains(ES_SearchField.lc_create_time.toString())) {
                mainResult = line;
            } else {
                otherLines.add(line);
            }
            count++;
        }
        LOG.info("parse consume times:"
                + (System.currentTimeMillis() - parseBegin));

        viewModels.setItems(items);
        return viewModels;

    }


    @Override
    public ListViewModel<ResourceModel> searchUseES(String resType,
                                               List<String> includes,
                                               Map<String, Map<String, List<String>>> params,
                                               Map<String, String> orderMap, int from, int size, boolean reverse, String words) {
    	//FIXME to do the es search
        // words:title, description, keywords, tags, edu_description, cr_description
        // cg_taxonpath={eq=[K12/$ON030000/$ON030200/$SB0500/$E004000/$E004001]}
        // cg_taxoncode={ne=[$F050006], eq=[$F050004 and  $RA0100, $RT0206]}
        // coverages={in=[User/89/OWNER]

        Map<String, Object> scriptParamMap = new HashMap<String, Object>();
        StringBuffer query = new StringBuffer("it = graph.indexQuery(\"mixed_ndresource\",\"");
        query.append(dealWithWords(words));
        //query.append(dealWithWords(words,scriptParamMap));
       // query.append(dealWithParams(params,scriptParamMap));
        query.append("\").limit(10).vertices()*.getElement().iterator(); List<Object> resultList= new ArrayList<Object>();while(it.hasNext()) {resultList<<(it.next().properties().toList())};resultList");

        System.out.println("script:"+query.toString());
        System.out.println("scriptParamMap:"+scriptParamMap);

        /*GremlinClientFactory factory=new GremlinClientFactory();
        factory.init();
        Client client= factory.getGremlinClient();

        ResultSet resultSet = client.submit(query.toString(),scriptParamMap);*/
        ResultSet resultSet = titanResourceRepository.search(query.toString(), scriptParamMap);
        Iterator<Result> iterator = resultSet.iterator();
        while (iterator.hasNext()) {
            System.out.println(iterator.next().getString());
        }
        return null;
    }

    private String dealWithWords(String words,Map<String, Object> scriptParamMap){
        StringBuffer query=new StringBuffer();
        for(TitanQueryVertexWithWords.WordsCover field:TitanQueryVertexWithWords.WordsCover.values()){
            String key=TitanUtils.generateKey(scriptParamMap, "words");
            query.append("v.\\\"");
            query.append(field);
            query.append("\\\":(");
            query.append(key);
            query.append(") ");
            scriptParamMap.put(key,words.replaceAll(",",""));
        }
        return query.toString();
    }
    private String dealWithWords(String words){
        StringBuffer query=new StringBuffer();
        for(TitanQueryVertexWithWords.WordsCover field:TitanQueryVertexWithWords.WordsCover.values()){
            query.append("v.\\\"");
            query.append(field);
            query.append("\\\":(");
            query.append(words.replaceAll(",",""));
            query.append(") ");
        }
        return query.toString();
    }

    private String dealWithParams(Map<String, Map<String, List<String>>> params) {
        // cg_taxonpath={eq=[K12/$ON030000/$ON030200/$SB0500/$E004000/$E004001]}
        // cg_taxoncode={ne=[$F050006], eq=[$F050004 and  $RA0100, $RT0206]}
        // coverages={in=[User/89/OWNER]
        StringBuffer query = new StringBuffer();
        Map<String, List<String>> searchCodeString = params.get(ES_SearchField.cg_taxoncode.toString());
        String codeStr=dealWithSingleParam("search_code_string",searchCodeString);
        //System.out.println(codeStr);
        Map<String, List<String>> searchPathString = params.get(ES_SearchField.cg_taxonpath.toString());
        String pathStr=dealWithSingleParam("search_path_string",searchPathString);
        // System.out.println(pathStr);
        Map<String, List<String>> searchCoverageString = params.get(ES_SearchField.coverages.toString());
        String coverageStr=dealWithSingleParam("search_coverage_string",searchCoverageString);
        //System.out.println(coverageStr);
        query.append(codeStr).append(pathStr).append(coverageStr);
        return query.toString();
    }

    private String dealWithParams(Map<String, Map<String, List<String>>> params, Map<String, Object> scriptParamMap) {
        // cg_taxonpath={eq=[K12/$ON030000/$ON030200/$SB0500/$E004000/$E004001]}
        // cg_taxoncode={ne=[$F050006], eq=[$F050004 and  $RA0100, $RT0206]}
        // coverages={in=[User/89/OWNER]
        StringBuffer query = new StringBuffer();
        Map<String, List<String>> searchCodeString = params.get(ES_SearchField.cg_taxoncode.toString());
        String codeStr=dealWithSingleParam("search_code_string",searchCodeString,scriptParamMap);
        //System.out.println(codeStr);
        Map<String, List<String>> searchPathString = params.get(ES_SearchField.cg_taxonpath.toString());
        String pathStr=dealWithSingleParam("search_path_string",searchPathString,scriptParamMap);
        // System.out.println(pathStr);
        Map<String, List<String>> searchCoverageString = params.get(ES_SearchField.coverages.toString());
        String coverageStr=dealWithSingleParam("search_coverage_string",searchCoverageString,scriptParamMap);
        //System.out.println(coverageStr);
        query.append(codeStr).append(pathStr).append(coverageStr);
        return query.toString();
    }

    private String dealWithSingleParam(String property, Map<String, List<String>> searchList) {
        StringBuffer query = new StringBuffer();
        if (CollectionUtils.isNotEmpty(searchList)) {
            query.append("v.\\\"");
            query.append(property);
            query.append("\\\":(");
            StringBuffer queryCondition = new StringBuffer();
            for (Map.Entry<String, List<String>> entry : searchList.entrySet()) {
                List<String> codes = entry.getValue();
                String codeKey = entry.getKey();
                if (CollectionUtils.isEmpty(codes)) continue;
                for (String code : codes) {
                    // FIXME $ 需要转义?
                    if (ES_OP.eq.toString().equals(codeKey)||ES_OP.in.toString().equals(codeKey)) {
                        if (code.contains(PropOperationConstant.OP_AND)) {
                            code = "("+code.replaceAll(PropOperationConstant.OP_AND, "AND").trim()+")";
                        }
                        queryCondition.append(code.trim()).append(" ");

                    } else if (ES_OP.ne.toString().equals(codeKey)) {
                        queryCondition.append("-").append(code.trim()).append(" ");
                    }
                }
            }

            query.append(queryCondition.toString());
            query.append(") ");


        }
        return query.toString();
    }

    private String dealWithSingleParam(String property, Map<String, List<String>> searchList, Map<String, Object> scriptParamMap) {
        StringBuffer query = new StringBuffer();
        if (CollectionUtils.isNotEmpty(searchList)) {
            String key = TitanUtils.generateKey(scriptParamMap, property);
            query.append("v.\\\"");
            query.append(property);
            query.append("\\\":(");
            StringBuffer queryCondition = new StringBuffer();
            for (Map.Entry<String, List<String>> entry : searchList.entrySet()) {
                List<String> codes = entry.getValue();
                String codeKey = entry.getKey();
                if (CollectionUtils.isEmpty(codes)) continue;
                for (String code : codes) {
                    // FIXME $ 需要转义?
                    if (ES_OP.eq.toString().equals(codeKey)||ES_OP.in.toString().equals(codeKey)) {
                        if (code.contains(PropOperationConstant.OP_AND)) {
                            code = "("+code.replaceAll(PropOperationConstant.OP_AND, "AND").trim()+")";
                        }
                        queryCondition.append(code.trim()).append(" ");

                    } else if (ES_OP.ne.toString().equals(codeKey)) {
                        queryCondition.append("-").append(code.trim()).append(" ");
                    }
                }
            }

            query.append(key);
            query.append(") ");
            scriptParamMap.put(key, queryCondition.toString());


        }
        return query.toString();
    }


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
            if (CollectionUtils.isNotEmpty(searchCoverageConditionMap)) vertexPropertiesMap.put("search_coverage", searchCoverageConditionMap);
        }

    }


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
            if (CollectionUtils.isNotEmpty(neConditions)) resourceQueryVertex.getPropertiesMap().put("search_path", neSearchPathConditionMap);
        }

    }


    private void dealWithSearchCode(TitanQueryVertexWithWords resourceQueryVertex,
                                    Map<String, List<String>> codeConditions) {
        if (CollectionUtils.isEmpty(codeConditions)) return;

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

        // ne
        List<String> nqConditions = codeConditions.get(ES_OP.ne.toString());
        if (CollectionUtils.isNotEmpty(nqConditions)) {
            List<Object> neSearchCode = new ArrayList<Object>();
            for (String neCondition : nqConditions) {
                if (neCondition == null) continue;
                neSearchCode.add(neCondition);
            }
            Map<Titan_OP, List<Object>> searchCodeConditionMap = new HashedMap<Titan_OP, List<Object>>();
            if (CollectionUtils.isNotEmpty(neSearchCode)) searchCodeConditionMap.put(Titan_OP.ne, neSearchCode);
            if (CollectionUtils.isNotEmpty(searchCodeConditionMap)) resourceQueryVertex.getPropertiesMap().put("search_code", searchCodeConditionMap);

        }

    }



    /**
     * @param resType
     * @param mainResult
     * @param otherLines
     * @param taxOnPath
     * @return
     */
    private ResourceModel getItem(String resType, String mainResult, List<String> otherLines, String taxOnPath) {
        if (ResourceNdCode.ebooks.toString().equals(resType)) {
            return generateEbookModel(mainResult, otherLines, taxOnPath);
        } else if (ResourceNdCode.teachingmaterials.toString().equals(resType)) {
            generateTeachingMaterialModel(mainResult, otherLines, taxOnPath);
        } /*else if (ResourceNdCode.guidancebooks.toString().equals(resType)) {
        } */ else if (ResourceNdCode.questions.toString().equals(resType)) {
            return generateQuestionModel(mainResult, otherLines, taxOnPath);
        }
        return generateResourceModel(mainResult, otherLines, taxOnPath);
    }

    /**
     * @param mainResult
     * @param strInOneItem
     * @param taxOnPath
     * @return
     */
    private TeachingMaterialModel generateTeachingMaterialModel(String mainResult, List<String> strInOneItem, String taxOnPath) {
        TeachingMaterialModel item = new TeachingMaterialModel();
        Map<String, String> fieldMap = TitanResultParse.toMap(mainResult);
        TitanResultParse.dealMainResult(item, fieldMap);
        TmExtPropertiesModel extProperties = new TmExtPropertiesModel();
        extProperties.setIsbn(fieldMap.get("ext_isbn"));
        extProperties.setCriterion(fieldMap.get("ext_criterion"));
        String attachments = fieldMap.get("ext_attachments");
        if (attachments != null) {
            extProperties.setAttachments(Arrays.asList(attachments.replaceAll("\"", "").split(",")));
        }
        item.setExtProperties(extProperties);
        generateModel(item, strInOneItem, taxOnPath);
        return item;
    }

    /**
     * @param mainResult
     * @param strInOneItem
     * @param taxOnPath
     * @return
     */
    private EbookModel generateEbookModel(String mainResult, List<String> strInOneItem, String taxOnPath) {
        EbookModel item = new EbookModel();
        Map<String, String> fieldMap = TitanResultParse.toMap(mainResult);
        TitanResultParse.dealMainResult(item, fieldMap);
        EbookExtPropertiesModel extProperties = new EbookExtPropertiesModel();
        extProperties.setIsbn(fieldMap.get("ext_isbn"));
        extProperties.setCriterion(fieldMap.get("ext_criterion"));
        String attachments = fieldMap.get("ext_attachments");
        if (attachments != null) {
            extProperties.setAttachments(Arrays.asList(attachments.replaceAll("\"", "").split(",")));
        }
        item.setExtProperties(extProperties);
        generateModel(item, strInOneItem, taxOnPath);
        return item;
    }

    /**
     * @param mainResult
     * @param strInOneItem
     * @param taxOnPath
     * @return
     */
    private QuestionModel generateQuestionModel(String mainResult, List<String> strInOneItem, String taxOnPath) {
        QuestionModel item = new QuestionModel();
        Map<String, String> fieldMap = TitanResultParse.toMap(mainResult);
        TitanResultParse.dealMainResult(item, fieldMap);
        QuestionExtPropertyModel extProperties = new QuestionExtPropertyModel();

        String discrimination = fieldMap.get("ext_discrimination");
        if (discrimination != null) {
            extProperties.setDiscrimination(Float.parseFloat(discrimination.trim()));
        }
        String answer = fieldMap.get("ext_answer");
        if (answer != null) {
            @SuppressWarnings("unchecked")
            Map<String, String> answerMap = ObjectUtils.fromJson(answer, Map.class);
            extProperties.setAnswer(answerMap);
        }
        String content = fieldMap.get("ext_item_content");
        if (content != null) {
            @SuppressWarnings("unchecked")
            Map<String, String> contentMap = ObjectUtils.fromJson(content, Map.class);
            extProperties.setItemContent(contentMap);
        }
        String criterion = fieldMap.get("ext_criterion");
        if (criterion != null) {
            @SuppressWarnings("unchecked")
            Map<String, String> criterionMap = ObjectUtils.fromJson(criterion, Map.class);
            extProperties.setCriterion(criterionMap);
        }
        String score = fieldMap.get("ext_score");
        if (score != null) {
            extProperties.setScore(Float.parseFloat(score.trim()));
        }
        String secrecy = fieldMap.get("ext_secrecy");
        if (secrecy != null) {
            extProperties.setSecrecy(Integer.parseInt(secrecy.trim()));
        }
        String modifiedDifficulty = fieldMap.get("ext_modified_difficulty");
        if (modifiedDifficulty != null) {
            extProperties.setModifiedDifficulty(Float.parseFloat(modifiedDifficulty.trim()));
        }
        String difficulty = fieldMap.get("ext_ext_difficulty");
        if (difficulty != null) {
            extProperties.setExtDifficulty(Float.parseFloat(difficulty.trim()));
        }
        String modifiedDiscrimination = fieldMap.get("ext_modified_discrimination");
        if (modifiedDiscrimination != null) {
            extProperties.setModifiedDiscrimination(Float.parseFloat(modifiedDiscrimination.trim()));
        }
        String usedTime = fieldMap.get("ext_used_time");
        if (usedTime != null) {
            extProperties.setUsedTime(Integer.parseInt(usedTime.trim()));
        }
        String exposalDate = fieldMap.get("ext_exposal_date");
        if (exposalDate != null) {
            extProperties.setExposalDate(new Date(new Long(exposalDate)));
        }
        extProperties.setAutoRemark("true".equals(fieldMap.get("ext_is_auto_remark")));

        item.setQuestionType(fieldMap.get("ext_question_type"));
        item.setExtProperties(extProperties);
        generateModel(item, strInOneItem, taxOnPath);
        return item;
    }

    /**
     * @param mainResult
     * @param strInOneItem
     * @param taxOnPath
     * @return
     */
    private ResourceModel generateResourceModel(String mainResult, List<String> strInOneItem, String taxOnPath) {
        ResourceModel item = new ResourceModel();
        Map<String, String> fieldMap = TitanResultParse.toMap(mainResult);
        TitanResultParse.dealMainResult(item, fieldMap);
        generateModel(item, strInOneItem, taxOnPath);
        return item;
    }

    /**
     * @param strInOneItem
     * @param taxOnPath
     */
    private void generateModel(ResourceModel item, List<String> strInOneItem, String taxOnPath) {
        // ResourceModel item =null;
        List<ResTechInfoModel> techInfoList = new ArrayList<>();
        List<ResClassificationModel> categoryList = new ArrayList<>();

        for (String str : strInOneItem) {
            Map<String, String> fieldMap = TitanResultParse.toMap(str);
            if (str.contains(ES_SearchField.ti_md5.toString())) { //tech_info
                techInfoList.add(TitanResultParse.dealTI(fieldMap));
            } else if (str.contains(ES_SearchField.cg_taxoncode.toString())) {// categoryList
                categoryList.add(TitanResultParse.dealCG(fieldMap, taxOnPath));
            }
        }
        item.setTechInfoList(techInfoList);
        item.setCategoryList(categoryList);
        //return item;
    }


    private void dealWithOrderAndRange(TitanExpression titanExpression,
                                       Map<String, String> orderMap, int from, int size) {
        // 默认使用创建时间排序，desc
        if (CollectionUtils.isEmpty(orderMap)) {
            orderMap = new HashMap<String, String>();
            orderMap.put(ES_SearchField.lc_create_time.toString(),
                    PropOperationConstant.OP_DESC.toString());
        }
        titanExpression.setOrderMap(orderMap);
        titanExpression.setRange(from, size);

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
                    ((TitanQueryVertexForTree) titanQueryVertex).setTreeEdgeLabel(TitanKeyWords.has_chapter
                            .toString());
                } else if (ResourceNdCode.knowledges.toString().equals(
                        chunks[0])) {
                    ((TitanQueryVertexForTree) titanQueryVertex).setTreeEdgeLabel(TitanKeyWords.has_knowledge
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
                    "primary_category",
                    generateFieldCondtion("primary_category",
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

    @Test
    public void testJson() {
        /*String text = "{\"identifier\":null,\"type\":\"QUOTA\",\"name\":\"resolution\",\"minVersion\":null,\"maxVersion\":null,\"installation\":null,\"installationFile\":null,\"value\":\"7952*5304\",\"ResourceModel\":null}";
        @SuppressWarnings("unchecked")
        Map<String, String> map = ObjectUtils.fromJson(text, Map.class);
        // System.out.println(map.get("identifier") == null);
        String t1 = "K12/$ON020000/$ON020100/$SB0100/$E001000/$E001001";
        String t2 = "$ON020000";
        // System.out.println(t1.contains(t2));
        // System.out.println(StringUtils.strTimeStampToDate("1464764846605"));*/

    /*    TitanQueryVertexWithWords resourceQueryVertex = new TitanQueryVertexWithWords();

        Map<String, Map<Titan_OP, List<Object>>> resourceVertexPropertyMap = new HashMap<String, Map<Titan_OP, List<Object>>>();
        resourceQueryVertex.setPropertiesMap(resourceVertexPropertyMap);
        Map<Titan_OP, List<Object>> c = new HashedMap<Titan_OP, List<Object>>();
        resourceQueryVertex.getPropertiesMap().put("22222", c);
        System.out.println(resourceQueryVertex);*/

        System.out.println(CollectionUtils.isNotEmpty(new ArrayList<>()));

    }


}