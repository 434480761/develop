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
    private String resType;
    private Map<String, Map<String, List<String>>> params;
    private int from = 0;
    private int end = 10;
    public static final String DEFINE_SCRIPT="List<String> ids = new ArrayList<String>();";
    public static final String GET_COUNT="List<Object> resultList = results.toList();count = ids.size();resultList << 'COUNT:' + count;resultList";
    public void setIndex(String index) {
        this.index = index;
    }

    public void setWords(String words) {
        this.words = words;
    }

    public void setResType(String resType) {
        this.resType = resType;
    }

    public void setParams(Map<String, Map<String, List<String>>> params) {
        this.params = params;
    }

    public void setRange(int from, int size) {
        this.from = from;
        this.end = size + from;
    }


    /**
     * 构建indexQuery查询脚本
     * :> List<String> ids = new ArrayList<String>();
     * graph.indexQuery("mixed_ndresource","v.\"search_coverage_string\":(Org/nd/* ) ").vertices().collect{ids.add(it.getElement().id())};
     * results=g.V(ids.toArray()).range(0,10).valueMap();
     * List<Object> resultList = results.toList();
     * count =ids.size();resultList << 'COUNT:'+count;
     * resultList
     * @return
     */
    public String generateScript() {
        StringBuffer query=new StringBuffer();
        StringBuffer baseQuery=new StringBuffer("graph.indexQuery(\"").append(this.index).append("\",\"");
        baseQuery.append(dealWithWords(this.words));
        baseQuery.append(dealWithParams());
        baseQuery.append(dealWithResType());

        //baseQuery.deleteCharAt(baseQuery.length()-1);
        baseQuery.append("\")");
        baseQuery.append(".vertices().collect{ids.add(it.getElement().id())};if(ids.size()==0){return};");
        baseQuery.append("results = g.V(ids.toArray()).range(").append(from).append(",").append(end).append(")");
        baseQuery.append(".as('v').union(select('v'),out('has_category_code'),out('has_categories_path'),out('has_tech_info')).valueMap();");

        query.append(DEFINE_SCRIPT).append(baseQuery).append(GET_COUNT);

        return query.toString();
    }

    /**
     * 处理可用资源（primary_category、lc_enable）
     * @return
     */
    private String dealWithResType() {
        StringBuffer query = new StringBuffer();
        query.append("AND v.\\\"primary_category\\\":(").append(this.resType).append(") ");
        query.append("AND v.\\\"lc_enable\\\":(true)");

        return query.toString();
    }

    /**
     * 处理分词
     * @param words
     * @return
     */
    private String dealWithWords(String words) {
        if (words == null) return "";
        if ("".equals(words.trim()) || ",".equals(words.trim())) return "";

        StringBuffer query = new StringBuffer();
        WordsCover[] covers=WordsCover.values();
        int coversLength=covers.length;
        for (int i = 0; i < coversLength; i++) {
            query.append("v.\\\"");
            query.append(covers[i]);
            query.append("\\\":(");
            query.append(words);
            query.append(")");
            if (i != coversLength - 1) query.append(" OR ");
        }

        return "("+query.toString()+")";
    }

    /**
     * 处理 category（code、path）、coverage 参数
     * @return
     */
    private String dealWithParams() {
        if (CollectionUtils.isEmpty(this.params)) return "";
        StringBuffer query = new StringBuffer();
        String codeStr = dealWithSingleParam(TitanKeyWords.search_code_string.toString(), this.params.get(ES_SearchField.cg_taxoncode.toString()));
        String pathStr = dealWithSingleParam(TitanKeyWords.search_path_string.toString(), this.params.get(ES_SearchField.cg_taxonpath.toString()));
        String coverageStr = dealWithSingleParam(TitanKeyWords.search_coverage_string.toString(), this.params.get(ES_SearchField.coverages.toString()));

        if(!"".equals(codeStr)){
            query.append(" AND ").append(codeStr);
        }
        if(!"".equals(pathStr)){
            query.append("AND ").append(pathStr);
        }
        if(!"".equals(coverageStr)){
            query.append("AND ").append(coverageStr);
        }
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
                    code = code.toLowerCase();

                    if (ES_OP.eq.toString().equals(codeKey) || ES_OP.in.toString().equals(codeKey)) {
                        if (code.contains(PropOperationConstant.OP_AND)) {
                            String[] strs=code.split(PropOperationConstant.OP_AND);
                            code = "(*" + strs[0].trim() + "*" + " AND " + "*" + strs[1].trim() + "*)";
                            //code = "(" + code.replaceAll(PropOperationConstant.OP_AND, "AND").trim() + ")";

                        }else{
                            code = "*" + code.trim() + "*";
                        }
                        queryCondition.append(code).append(" ");

                    } else if (ES_OP.ne.toString().equals(codeKey)) {
                        queryCondition.append("-").append("*").append(code.trim()).append("*").append(" ");
                    }
                }
            }

            query.append(queryCondition.toString());
            query.deleteCharAt(query.length()-1);
            query.append(") ");
        }else{
            return "";
        }
        return query.toString();
    }

    public enum WordsCover {
        title, description, keywords, tags, edu_description, cr_description
    }
}
