package nd.esp.service.lifecycle.support.busi.elasticsearch;

import nd.esp.service.lifecycle.educommon.vos.constant.PropOperationConstant;
import nd.esp.service.lifecycle.support.busi.titan.TitanKeyWords;
import nd.esp.service.lifecycle.support.busi.titan.TitanUtils;
import nd.esp.service.lifecycle.support.enums.ES_OP;
import nd.esp.service.lifecycle.support.enums.ES_SearchField;
import nd.esp.service.lifecycle.utils.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * ******************************************
 * <p/>
 * Copyright 2016
 * NetDragon All rights reserved
 * <p/>
 * *****************************************
 * <p/>
 * *** Company ***
 * NetDragon
 * <p/>
 * *****************************************
 * <p/>
 * *** Team ***
 * <p/>
 * <p/>
 * *****************************************
 *
 * @author gsw(806801)
 * @version V1.0
 * @Title EsIndexQueryBuilder
 * @Package nd.esp.service.lifecycle.support.busi.elasticsearch
 * <p/>
 * *****************************************
 * @Description
 * @date 2016/7/13
 */
public class EsIndexQueryBuilder {

    private String index="mixed_ndresource";
    private String words;
    private Map<String, Map<String, List<String>>> params;
    private String limit=".limit(10)";
    public static final String SCRIPT=" List<Object> resultList= new ArrayList<Object>();while(RESULT.hasNext()) {resultList<<(RESULT.next().properties().toList())};resultList << 'COUNT:'+COUNT;resultList";

    public void setIndex(String index) {
        this.index = index;
    }

    public void setWords(String words) {
        this.words = words;
    }

    public void setParams(Map<String, Map<String, List<String>>> params) {
        this.params = params;
    }

    public void setLimit(String limit) {
        this.limit = ".limit(" + limit + ")";
    }


    public String generateScript() {
        StringBuffer query=new StringBuffer();
        StringBuffer count=new StringBuffer();
        StringBuffer result=new StringBuffer();
        StringBuffer baseQuery=new StringBuffer("graph.indexQuery(\"").append(this.index).append("\",\"");
        baseQuery.append(dealWithWords(this.words));
        baseQuery.append(dealWithParams(this.params));
        baseQuery.deleteCharAt(baseQuery.length()-1);
        baseQuery.append("\")");
        count.append("COUNT = ").append(baseQuery).append(".vertices()*.getElement()").append(".size();");
        result.append("RESULT = ").append(baseQuery).append(this.limit).append(".vertices()*.getElement()").append(".iterator();");
        query.append(count).append(result).append(SCRIPT);

        return query.toString();
    }


    private String dealWithWords(String words) {
        if (words == null) return "";
        if ("".equals(words.trim()) || ",".equals(words.trim())) return "";
        StringBuffer query = new StringBuffer();
        for (WordsCover field : WordsCover.values()) {
            query.append("v.\\\"");
            query.append(field);
            query.append("\\\":(");
            query.append(words.replaceAll(",", ""));
            query.append(") ");
        }

        return query.toString();
    }

    private String dealWithParams(Map<String, Map<String, List<String>>> params) {
        StringBuffer query = new StringBuffer();
        Map<String, List<String>> searchCodeString = params.get(ES_SearchField.cg_taxoncode.toString());
        String codeStr = dealWithSingleParam(TitanKeyWords.search_code_string.toString(), searchCodeString);
        Map<String, List<String>> searchPathString = params.get(ES_SearchField.cg_taxonpath.toString());
        String pathStr = dealWithSingleParam(TitanKeyWords.search_path_string.toString(), searchPathString);
        Map<String, List<String>> searchCoverageString = params.get(ES_SearchField.coverages.toString());
        String coverageStr = dealWithSingleParam(TitanKeyWords.search_coverage_string.toString(), searchCoverageString);
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
                    if (code.contains("$")) {
                        code = code.replace("$", "\\$");
                    }
                    if (code.contains("/")) {
                        code = code.replace("/", "\\\\/");
                    }
                    if (TitanKeyWords.search_path_string.toString().equals(property)) {
                        code = "'" + code.trim() + "'";
                    }

                    // FIXME $ 需要转义
                    if (ES_OP.eq.toString().equals(codeKey) || ES_OP.in.toString().equals(codeKey)) {
                        if (code.contains(PropOperationConstant.OP_AND)) {
                            code = "(" + code.replaceAll(PropOperationConstant.OP_AND, "AND").trim() + ")";
                        }
                        queryCondition.append(code.trim()).append(" ");

                    } else if (ES_OP.ne.toString().equals(codeKey)) {
                        queryCondition.append("-").append(code.trim()).append(" ");
                    }
                }
            }

            query.append(queryCondition.toString());
            query.deleteCharAt(query.length()-1);
            query.append(") ");
        }
        return query.toString();
    }

    /*public String generateScript(Map<String, Object> scriptParamMap) {
    }*/

   /* private String dealWithWords(String words, Map<String, Object> scriptParamMap) {
        StringBuffer query = new StringBuffer();
        for (WordsCover field : WordsCover.values()) {
            String key = TitanUtils.generateKey(scriptParamMap, "words");
            query.append("v.\\\"");
            query.append(field);
            query.append("\\\":(");
            query.append(key);
            query.append(") ");
            scriptParamMap.put(key, words.replaceAll(",", ""));
        }
        return query.toString();
    }*/


   /* private String dealWithParams(Map<String, Map<String, List<String>>> params, Map<String, Object> scriptParamMap) {
        // cg_taxonpath={eq=[K12/$ON030000/$ON030200/$SB0500/$E004000/$E004001]}
        // cg_taxoncode={ne=[$F050006], eq=[$F050004 and  $RA0100, $RT0206]}
        // coverages={in=[User/89/OWNER]
        StringBuffer query = new StringBuffer();
        Map<String, List<String>> searchCodeString = params.get(ES_SearchField.cg_taxoncode.toString());
        String codeStr = dealWithSingleParam("search_code_string", searchCodeString, scriptParamMap);
        //System.out.println(codeStr);
        Map<String, List<String>> searchPathString = params.get(ES_SearchField.cg_taxonpath.toString());
        String pathStr = dealWithSingleParam("search_path_string", searchPathString, scriptParamMap);
        // System.out.println(pathStr);
        Map<String, List<String>> searchCoverageString = params.get(ES_SearchField.coverages.toString());
        String coverageStr = dealWithSingleParam("search_coverage_string", searchCoverageString, scriptParamMap);
        //System.out.println(coverageStr);
        query.append(codeStr).append(pathStr).append(coverageStr);
        return query.toString();
    }*/
   /* private String dealWithSingleParam(String property, Map<String, List<String>> searchList, Map<String, Object> scriptParamMap) {
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
                    if (ES_OP.eq.toString().equals(codeKey) || ES_OP.in.toString().equals(codeKey)) {
                        if (code.contains(PropOperationConstant.OP_AND)) {
                            code = "(" + code.replaceAll(PropOperationConstant.OP_AND, "AND").trim() + ")";
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
    }*/

    public enum WordsCover {
        title, description, keywords, tags, edu_description, cr_description
    }
}
