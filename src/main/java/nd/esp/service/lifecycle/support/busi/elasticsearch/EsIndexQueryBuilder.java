package nd.esp.service.lifecycle.support.busi.elasticsearch;

import nd.esp.service.lifecycle.educommon.vos.constant.PropOperationConstant;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.busi.titan.TitanKeyWords;
import nd.esp.service.lifecycle.support.busi.titan.TitanUtils;
import nd.esp.service.lifecycle.support.enums.ES_OP;
import nd.esp.service.lifecycle.support.enums.ES_SearchField;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.Arrays;
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
    private int size=10;
    private List<String> includes;
    private List<String> fields;

    public static final String DEFINE_SCRIPT="List<String> ids = new ArrayList<String>();";
    public static final String GET_COUNT="List<Object> resultList = results.toList();count = ids.size();resultList << 'TOTALCOUNT:' + count;resultList";
    public static final String COUNT="List<Object> resultList = results.toList();Long count = builder.count();resultList << 'TOTALCOUNT:' + count;resultList";
    public static final String BUILDER_CLASS="com.thinkaurelius.titan.graphdb.query.graph.IndexQueryBuilder ";

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
        this.size = size;
        this.end = size + from;
    }

    public void setIncludes(List<String> includes) {
        this.includes = includes;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
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
        String wordSegmentation=dealWithWordsContainsNot(this.words);
        String other=dealWithParams();
       // System.out.println(dealWithProp());
        if("".endsWith(wordSegmentation.trim())){
            other=other.trim().replaceFirst("AND","");
        }
        baseQuery.append(wordSegmentation);
        baseQuery.append(other);
        baseQuery.append(dealWithResType());

        //baseQuery.deleteCharAt(baseQuery.length()-1);
        baseQuery.append("\")");
        baseQuery.append(".vertices().collect{ids.add(it.getElement().id())};if(ids.size()==0){return};");
        baseQuery.append(getRangeIds());
        baseQuery.append("results = g.V(rangeids.toArray())");
        //baseQuery.append("results = g.V(ids.toArray()).range(").append(from).append(",").append(end).append(")");
        //baseQuery.append(".as('v').union(select('v'),out('has_category_code'),out('has_categories_path'),out('has_tech_info')).valueMap();");
        baseQuery.append(TitanUtils.generateScriptForInclude(this.includes));
        baseQuery.append(".valueMap();");

        query.append(DEFINE_SCRIPT).append(baseQuery).append(GET_COUNT);

        return query.toString();
    }

    /**
     * List<String> ids = new ArrayList<String>();
     * com.thinkaurelius.titan.graphdb.query.graph.IndexQueryBuilder builder = graph.indexQuery("mixed_ndresource","(v.\"keywords\":(test) OR v.\"title\":(test))").offset(0).limit(10);
     * builder.vertices().collect{ids.add(it.getElement().id())};
     * if(ids.size()==0){return};
     * results = g.V(ids.toArray()).valueMap();
     * List<Object> resultList = results.toList();
     * Long count = builder.count();
     * resultList << 'TOTALCOUNT:' + count;resultList
     * @return
     */
    public String generateScriptAfterEsUpdate() {
        StringBuffer query=new StringBuffer();
        StringBuffer baseQuery=new StringBuffer("builder = graph.indexQuery(\"").append(this.index).append("\",\"");
        String wordSegmentation=dealWithWordsContainsNot(this.words);
        String other=dealWithParams();
        String property = dealWithProp();
        //System.out.println(dealWithProp());
        if ("".endsWith(wordSegmentation.trim())) {
            other = other.trim().replaceFirst("AND", "");
        }
        baseQuery.append(wordSegmentation);
        baseQuery.append(other);
        baseQuery.append(dealWithResType());
        if(!"".endsWith(property.trim())){
            baseQuery.append(" AND ").append(property);
        }
        baseQuery.append("\")");
        baseQuery.append(".offset(").append(this.from).append(")");
        baseQuery.append(".limit(").append(this.size).append(");");
        baseQuery.append("builder.vertices().collect{ids.add(it.getElement().id())};if(ids.size()==0){return};");
        baseQuery.append("results = g.V(ids.toArray())");
        baseQuery.append(TitanUtils.generateScriptForInclude(this.includes));
        baseQuery.append(".valueMap();");
        query.append(DEFINE_SCRIPT).append(BUILDER_CLASS).append(baseQuery).append(COUNT);

        return query.toString();
    }

    /**
     * 处理可用资源（primary_category、lc_enable）
     * @return
     */
    private String dealWithResType() {
        StringBuffer query = new StringBuffer();
        query.append(" AND v.\\\"primary_category\\\":(").append(this.resType).append(")");
        query.append(" AND v.\\\"lc_enable\\\":(true)");

        return query.toString();
    }

    /**
     * 处理分词
     * @param words
     * @return
     */
    private String dealWithWords(String words,boolean isOnlyNot) {
        if (words == null) return "";
        if ("".equals(words.trim()) || ",".equals(words.trim())) return "";

        StringBuffer query = new StringBuffer();
        int fieldSize=fields.size();
        for (int i = 0; i < fieldSize; i++) {
            query.append("v.\\\"").append(this.fields.get(i)).append("\\\":(").append(words).append(")");
            if (i != fieldSize - 1) {
                if (isOnlyNot) {
                    query.append(" AND ");
                } else {
                    query.append(" OR ");
                }
            }
        }

        return "("+query.toString()+")";
    }

    /**
     * 处理分词
     * @param words
     * @return
     */
    private String dealWithWordsContainsNot(String words) {
        if (words == null) return "";
        if ("".equals(words.trim()) || ",".equals(words.trim())) return "";

        // 没有“-”
        List<String> notWords =fetchContainsNotWords(words);
        if (notWords.size() == 0) return dealWithWords(words, false);

        // 只有“-”
        String not = notWords.get(0);
        if(not.equals(words)) return dealWithWords(words,true);

        if (!words.startsWith("-")) {//"-"要在表达式最前面
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.CommonSearchParamError.getCode(), this.words + ",words格式错误,‘-’要放在表达式最前面");
        }

        String has = words.replace(not, "").trim();
        if (has.startsWith("AND ")) {
            has = has.replaceFirst("AND ", "");
        } else {
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.CommonSearchParamError.getCode(), "不支持查询条件:"+this.words);
        }

        StringBuffer queryNot = new StringBuffer();
        StringBuffer queryHas = new StringBuffer(" AND (");
        int fieldSize=fields.size();
        for (int i = 0; i < fieldSize; i++) {
            queryNot.append("v.\\\"").append(this.fields.get(i)).append("\\\":(").append(not).append(")");
            queryHas.append("v.\\\"").append(this.fields.get(i)).append("\\\":(").append(has).append(")");
            if (i != fieldSize - 1) {
                queryNot.append(" AND ");
                queryHas.append(" OR ");
            }
        }
        queryHas.append(")");

        return "(" + queryNot.toString() + queryHas.toString() + ")";
    }

    /**
     * 检查words里的条件是否以"-"开头
     * @param words
     * @return
     */
    private boolean ckeckContainsNot(String words) {
        if (words != null) {
            String[] opts = words.replaceAll("\\)", "").replaceAll("\\(", "").trim().split(" ");
            for (String opt : opts) {
                if (opt.trim().startsWith("-")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 提取words里的条件是否以"-"开头
     * @param words
     * @return
     */
    private List<String> fetchContainsNotWords(String words) {
        List<String> notWords = new ArrayList<>();
        if (words != null) {
            String[] opts = words.replaceAll("\\)", "").replaceAll("\\(", "").trim().split(" ");
            for (String opt : opts) {
                if (opt.trim().startsWith("-")) {
                    notWords.add(opt.trim());
                }
            }
        }
        return notWords;
    }

    /**
     * 截取分页的ID
     * @return
     */
    private String getRangeIds(){
        StringBuffer range = new StringBuffer();
        range.append("List<String> rangeids = new ArrayList<String>();");
        range.append("for(int i=").append(this.from).append(";i<ids.size() && i<").append(this.end).append(";i++){rangeids.add(ids.get(i))};");

        return range.toString();

    }

    /**
     *
     *  1）不同【属性】时，prop之间为 AND
     *  2）相同【属性】，不同【操作符】时，prop之间为 AND， eq和in两者之间除外，可理解为eq和in本质上一样
     *  3）相同【属性】，相同【操作符】时，prop之间为 OR（ne除外，ne时为 AND）
     * @return
     */
    private String dealWithProp() {
        if (CollectionUtils.isEmpty(this.params)) return "";
       // String propsCover= Arrays.asList(PropsCover.values()).toString();
        StringBuffer query = new StringBuffer();
        // FIXME 处理资源的属性
        int paramCount = 0;
        for (Map.Entry<String, Map<String, List<String>>> entry : params.entrySet()) {
            String field = entry.getKey();
            int fieldSize = params.entrySet().size();
            String base = "v.\\\"" + field + "\\\":(";
            Map<String, List<String>> optMap = entry.getValue();
           // System.out.println(field + " " + optMap);
            int optSizeCount = 0;
            for (Map.Entry<String, List<String>> optEntry : optMap.entrySet()) {
                String optName = optEntry.getKey();
                List<String> optList = optEntry.getValue();
                int optSize = optMap.entrySet().size();// in ne like 有几个
                int optListSize = optList.size();// 每个操作符的值的个数
                if ("in".equals(optName)) {
                    query.append(base);
                    for (int i = 0; i < optListSize; i++) {
                        query.append(optList.get(i));
                        if (i != optListSize - 1) query.append(" OR ");
                    }
                    // query.append(")");

                } else if ("ne".equals(optName)) {
                    for (int i = 0; i < optListSize; i++) {
                        query.append(base).append("-").append(optList.get(i));
                        if (i != optListSize - 1) query.append(" AND ");
                    }
                    // query.append(")");
                } else if ("like".equals(optName)) {
                    for (int i = 0; i < optListSize; i++) {
                        query.append(base).append("*").append(optList.get(i)).append("*");
                        if (i != optListSize - 1) query.append(" OR ");
                    }
                }else if("gt".equals(optName)){}
                query.append(")");
                if (optSizeCount != optSize - 1) query.append(" AND ");
                optSizeCount++;
            }
            if (paramCount != fieldSize - 1) query.append(" AND ");
            paramCount++;

        }


        return query.toString();

    }

    /**
     * 处理 category（code、path）、coverage 参数
     * @return
     */
    private String dealWithParams() {
        if (CollectionUtils.isEmpty(this.params)) return "";
        StringBuffer query = new StringBuffer();
        String codeStr = dealWithSingleParam(TitanKeyWords.search_code_string.toString(), this.params.get(ES_SearchField.cg_taxoncode.toString()));
        this.params.remove(ES_SearchField.cg_taxoncode.toString());
        String pathStr = dealWithSingleParam(TitanKeyWords.search_path_string.toString(), this.params.get(ES_SearchField.cg_taxonpath.toString()));
        this.params.remove(ES_SearchField.cg_taxonpath.toString());
        String coverageStr = dealWithSingleParam(TitanKeyWords.search_coverage_string.toString(), this.params.get(ES_SearchField.coverages.toString()));
        this.params.remove(ES_SearchField.coverages.toString());
        if(!"".equals(codeStr)){
            query.append(" AND ").append(codeStr);
        }
        if(!"".equals(pathStr)){
            query.append(" AND ").append(pathStr);
        }
        if(!"".equals(coverageStr)){
            query.append(" AND ").append(coverageStr);
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

    public enum PropsCover {
        publisher, creator, title, status, provider, author, identifier, languange, edulanguage, tags, keywords, ndres_code
    }


}
