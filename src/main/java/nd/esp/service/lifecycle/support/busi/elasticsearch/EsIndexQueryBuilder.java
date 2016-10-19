package nd.esp.service.lifecycle.support.busi.elasticsearch;

import nd.esp.service.lifecycle.educommon.vos.constant.PropOperationConstant;
import nd.esp.service.lifecycle.support.Constant;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.busi.titan.TitanKeyWords;
import nd.esp.service.lifecycle.support.busi.titan.TitanOrder;
import nd.esp.service.lifecycle.support.busi.titan.TitanUtils;
import nd.esp.service.lifecycle.support.enums.ES_OP;
import nd.esp.service.lifecycle.support.enums.ES_SearchField;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.StringUtils;

import org.springframework.http.HttpStatus;

import java.util.*;

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

    private String index=Constant.TITAN_ES_RESOURCE_INDEX;
    private String words;
    //private String resType;
    private Set<String> resTypeSet;
    private Map<String, Map<String, List<String>>> params;
    private int from = 0;
    private int end = 10;
    private int size=10;
    private List<String> includes;
    private List<String> fields;
    private List<TitanOrder> orders;

    private static final String DOUBLE_BLANK_AND = " AND ";
    private static final String DOUBLE_BLANK_OR = " OR ";
    private static final String DEFINE_SCRIPT="List<String> ids = new ArrayList<String>();";
    private static final String COUNT="List<Object> resultList = results.toList();Long count = builder.count();resultList << 'TOTALCOUNT=' + count;resultList";
    private static final String BUILDER_CLASS="com.thinkaurelius.titan.graphdb.query.graph.IndexQueryBuilder ";

    public EsIndexQueryBuilder setIndex(String index) {
        this.index = index;
        return this;
    }

    public EsIndexQueryBuilder setWords(String words) {
        this.words = words;
        return this;
    }

    /*public EsIndexQueryBuilder setResType(String resType) {
        this.resType = resType;
        return this;
    }*/

    public EsIndexQueryBuilder setResTypeSet(Set<String> resTypeSet) {
        this.resTypeSet = resTypeSet;
        return this;
    }

    public EsIndexQueryBuilder setParams(Map<String, Map<String, List<String>>> params) {
        this.params = params;
        return this;
    }

    public EsIndexQueryBuilder setRange(int from, int size) {
        this.from = from;
        this.size = size;
        this.end = size + from;
        return this;
    }

    public EsIndexQueryBuilder setIncludes(List<String> includes) {
        this.includes = includes;
        return this;
    }

    public EsIndexQueryBuilder setFields(List<String> fields) {
        this.fields = fields;
        return this;
    }

    public EsIndexQueryBuilder setOrders(List<TitanOrder> orders) {
        this.orders = orders;
        return this;
    }

    /**
     * List<String> orders = new ArrayList<String>();
     * orders.add('_score#DESC#double');
     * orders.add('lc_last_update#DESC#long');
     * List<String> ids = new ArrayList<String>();
     * com.thinkaurelius.titan.graphdb.query.graph.IndexQueryBuilder builder =
     * graph.indexQuery("mixed_ndresource",
     * "(v.\"cr_description\":(High) OR v.\"description\":(High) OR v.\"keywords\":(High) OR v.\"tags\":(High) OR v.\"edu_description\":(High) OR v.\"title\":(High))
     * AND v.\"search_coverage_string\":(*org\\/495331477993\\/shareing*)
     * AND v.\"primary_category\":(assets OR lessons)
     * AND v.\"lc_enable\":(true)")
     * .offset(0).limit(6)
     * .addParameter(new Parameter('order_by',orders));
     * builder.vertices().collect{ids.add(it.getElement().id())};
     * if(ids.size()==0){return 'TOTALCOUNT=0'};
     * results = g.V(ids.toArray()).as('v')
     * .union(select('v'),outE('has_category_code'),out('has_tech_info')).valueMap(true);
     * List<Object> resultList = results.toList();
     * Long count = builder.count();
     * resultList << 'TOTALCOUNT=' + count;resultList
     * @return
     */
    public String generateScript() {
        StringBuffer query=new StringBuffer();
        StringBuffer baseQuery=new StringBuffer("builder = graph.indexQuery(\"").append(this.index).append("\",\"");
        String wordSegmentation = dealWithWordsContainsNot(this.words);
        String coverage = dealWithParams4Exact();
        String property = dealWithProp();
        if ("".endsWith(wordSegmentation.trim())) {
            coverage = coverage.trim().replaceFirst("AND", "").trim();
        }
        baseQuery.append(wordSegmentation);
        baseQuery.append(coverage);
        baseQuery.append(dealWithResType());
        if(!"".endsWith(property.trim())){
            baseQuery.append(DOUBLE_BLANK_AND).append(property);
        }
        baseQuery.append("\")");
        baseQuery.append(".offset(").append(this.from).append(")");
        baseQuery.append(".limit(").append(this.size).append(")");
        baseQuery.append(".addParameter(new Parameter('order_by',orders));");
        baseQuery.append("builder.vertices().collect{ids.add(it.getElement().id())};if(ids.size()==0){return 'TOTALCOUNT=0'};");
        baseQuery.append("results = g.V(ids.toArray())");
        baseQuery.append(TitanUtils.generateScriptForInclude(this.includes,this.resTypeSet,false,false,null));
        query.append(dealWithOrders()).append(DEFINE_SCRIPT).append(BUILDER_CLASS).append(baseQuery).append(COUNT);

        return query.toString();
    }

    /**
     *
     * @return
     */
    private String dealWithOrders() {
        StringBuffer orderScript = new StringBuffer();
        orderScript.append("List<String> orders = new ArrayList<String>();");
        for (TitanOrder order : orders) {
            String field = order.getField();
            // FIXME 暂时的特殊处理
            if (ES_SearchField.title.toString().equals(field)) field = "title__STRING";
            orderScript.append("orders.add('")
                    .append(field).append("#").append(order.getSortOrder()).append("#").append(order.getDataType())
                    .append("');");
        }
        return orderScript.toString();
    }

    /**
     * 处理可用资源（primary_category、lc_enable）
     * @return
     */
    private String dealWithResType() {
        StringBuffer query = new StringBuffer();
        query.append(DOUBLE_BLANK_AND).append("v.\\\"primary_category\\\":(");
        for (String res : this.resTypeSet) {
            query.append(res);
            query.append(DOUBLE_BLANK_OR);
        }
        query.delete(query.length() - 4, query.length());
        query.append(")");

        query.append(DOUBLE_BLANK_AND).append("v.\\\"lc_enable\\\":(true)");

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
                    query.append(DOUBLE_BLANK_AND);
                } else {
                    query.append(DOUBLE_BLANK_OR);
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
                queryNot.append(DOUBLE_BLANK_AND);
                queryHas.append(DOUBLE_BLANK_OR);
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
     *  + - && || ! ( ) { } [ ] ^ " ~ * ? : \ /
     * @param props
     */
   /* private void transferredMeaning(String key, Map<String, List<String>> props) {
        Map<String, List<String>> newProp = new HashMap<>();
        for (Map.Entry<String, List<String>> prop : props.entrySet()) {
            List<String> optList = prop.getValue();
            List<String> newOptList = new ArrayList<>();
            for (String s : optList) {
                s = s.trim();
                if (s.contains("\\")) s = s.replaceAll("\\\\", "\\\\\\\\\\\\");
                if (s.contains("/")) s = s.replaceAll("/", "\\\\\\\\/");

                if (s.contains("[")) s = s.replaceAll("\\[", "\\\\\\\\[");
                if (s.contains("]")) s = s.replaceAll("]", "\\\\\\\\]");
                if (s.contains("\"")) s = s.replaceAll("\"", "\\\\\\\\\\\\\"");

                //if (s.contains("+"))
                if (s.contains("-")) s = s.replaceAll("-", "\\\\\\\\-");
                if (s.contains("!")) s = s.replaceAll("!", "\\\\\\\\!");
                if (s.contains("(")) s = s.replaceAll("\\(", "\\\\\\\\(");
                if (s.contains(")")) s = s.replaceAll("\\)", "\\\\\\\\)");
                if (s.contains("{")) s = s.replaceAll("\\{", "\\\\\\\\{");
                if (s.contains("}")) s = s.replaceAll("\\}", "\\\\\\\\}");
                //if (s.contains("^")) s = s.replaceAll("^", "\\\\\\\\^");
                if (s.contains("?")) s = s.replaceAll("\\?", "\\\\\\\\?");
                if (s.contains(":")) s = s.replaceAll(":", "\\\\\\\\:");
                if (s.contains("~")) s = s.replaceAll("~", "\\\\\\\\~");
                if (s.contains("*")) s = s.replaceAll("\\*", "\\\\\\\\*");

                newOptList.add(s);
            }
            newProp.put(key, newOptList);
        }
        this.params.put(key, newProp);
    }*/

    /**
     *  + - && || ! ( ) { } [ ] ^ " ~ * ? : \ /
     * @param props
     */
    private void transferredMeaning(Map<String, Map<String, List<String>>> props) {
        for (Map.Entry<String, Map<String, List<String>>> propsEntry : props.entrySet()) {
            Map<String, List<String>> newProp = new HashMap<>();
            String key = propsEntry.getKey();
            if (key.equals(ES_SearchField.lc_create_time.toString())) continue;
            if (key.equals(ES_SearchField.lc_last_update.toString())) continue;
            Map<String, List<String>> prop = propsEntry.getValue();
            for (Map.Entry<String, List<String>> entry : prop.entrySet()) {
                List<String> optList = entry.getValue();
                List<String> newOptList = new ArrayList<>();
                for (String s : optList) {
                    s = s.trim();
                    if (s.contains("\\")) s = s.replaceAll("\\\\", "\\\\\\\\\\\\");
                    if (s.contains("/")) s = s.replaceAll("/", "\\\\\\\\/");

                    if (s.contains("[")) s = s.replaceAll("\\[", "\\\\\\\\[");
                    if (s.contains("]")) s = s.replaceAll("]", "\\\\\\\\]");
                    if (s.contains("\"")) s = s.replaceAll("\"", "\\\\\\\\\\\\\"");

                    //if (s.contains("+"))
                    if (s.contains("-")) s = s.replaceAll("-", "\\\\\\\\-");
                    if (s.contains("!")) s = s.replaceAll("!", "\\\\\\\\!");
                    if (s.contains("(")) s = s.replaceAll("\\(", "\\\\\\\\(");
                    if (s.contains(")")) s = s.replaceAll("\\)", "\\\\\\\\)");
                    if (s.contains("{")) s = s.replaceAll("\\{", "\\\\\\\\{");
                    if (s.contains("}")) s = s.replaceAll("\\}", "\\\\\\\\}");
                    //if (s.contains("^")) s = s.replaceAll("^", "\\\\\\\\^");
                    if (s.contains("?")) s = s.replaceAll("\\?", "\\\\\\\\?");
                    if (s.contains(":")) s = s.replaceAll(":", "\\\\\\\\:");
                    if (s.contains("~")) s = s.replaceAll("~", "\\\\\\\\~");
                    if (s.contains("*")) s = s.replaceAll("\\*", "\\\\\\\\*");

                    newOptList.add(s);
                }
                newProp.put(entry.getKey(), newOptList);
            }
            this.params.put(key, newProp);
        }
    }

    /**
     *
     *  1）不同【属性】时，prop之间为 AND
     *  2）相同【属性】，不同【操作符】时，prop之间为 AND， eq和in两者之间除外，可理解为eq和in本质上一样
     *  3）相同【属性】，相同【操作符】时，prop之间为 OR（ne除外，ne时为 AND）
     *  以下不分词需要转换：
     *  keywords__STRING,language__STRING,tags__STRING,title__STRING
     *  keywords,language,tags,title
     * @return
     */
    private String dealWithProp() {
        /*if (CollectionUtils.isEmpty(this.params)) return "";

        if (this.params.containsKey(ES_SearchField.keywords.toString())) {
            transferredMeaning(ES_SearchField.keywords.toString(), this.params.get(ES_SearchField.keywords.toString()));
        }
        if (this.params.containsKey(ES_SearchField.tags.toString())) {
            transferredMeaning(ES_SearchField.tags.toString(), this.params.get(ES_SearchField.tags.toString()));
        }
        if (this.params.containsKey(ES_SearchField.description.toString())) {
            transferredMeaning(ES_SearchField.description.toString(), this.params.get(ES_SearchField.description.toString()));
        }
        if (this.params.containsKey(ES_SearchField.title.toString())) {
            transferredMeaning(ES_SearchField.description.toString(), this.params.get(ES_SearchField.description.toString()));
        }
*/

        transferredMeaning(this.params);
        StringBuffer query = new StringBuffer();
        int paramCount = 0;
        for (Map.Entry<String, Map<String, List<String>>> entry : params.entrySet()) {
            String propName = entry.getKey();
            // 不使用分词,,需要修改titan-core,升级后才支持
            if ("description,keywords,language,tags,title".contains(propName)) {
                propName = propName + "__STRING";
            }
            int propSize = params.entrySet().size();// prop数量
            String base = "v.\\\"" + propName + "\\\":(";
            Map<String, List<String>> optMap = entry.getValue();
            int optSizeCount = 0;
            for (Map.Entry<String, List<String>> optEntry : optMap.entrySet()) {
                String optName = optEntry.getKey().trim().toLowerCase();
                List<String> optList = optEntry.getValue();
                int optSize = optMap.entrySet().size();// in ne like 有几个
                int optListSize = optList.size();// 每个操作符的值的个数
                query.append(base);
                if ("in".equals(optName)) {
                    for (int i = 0; i < optListSize; i++) {
                        query.append(optList.get(i));
                        if (i != optListSize - 1) query.append(DOUBLE_BLANK_OR);
                    }
                } else if ("ne".equals(optName)) {
                    for (int i = 0; i < optListSize; i++) {
                        query.append("-").append(optList.get(i));
                        if (i != optListSize - 1) query.append(DOUBLE_BLANK_AND);
                    }
                }
                //由于大写不支持like，暂时不支持like,需要修改titan-es,升级后才支持
                else if ("like".equals(optName)) {
                    for (int i = 0; i < optListSize; i++) {
                        query.append("*").append(optList.get(i)).append("*");
                        if (i != optListSize - 1) query.append(DOUBLE_BLANK_OR);
                    }
                } else if ("gt,lt,ge,le".contains(optName)) {
                    for (int i = 0; i < optListSize; i++) {
                        String range = toRangeByOpt(optName, optList.get(i));
                        query.append(range);
                        if (i != optListSize - 1) query.append(DOUBLE_BLANK_OR);
                    }
                }
                query.append(")");
                if (optSizeCount != optSize - 1) query.append(DOUBLE_BLANK_AND);
                optSizeCount++;
            }
            if (paramCount != propSize - 1) query.append(DOUBLE_BLANK_AND);
            paramCount++;

        }
        return query.toString();
    }

    /**
     * 根据操作符返回时间串的时间戳范围
     * 支持的操作符有 gt(大于) , lt  (小于) , ge(大于等于) ,le(小于等于)
     * @param optName
     * @param date
     * @return
     */
    private String toRangeByOpt(String optName, String date) {
        long toTimeStamp = StringUtils.strDateToTimeStamp(date.trim());
        String range = null;
        if (PropOperationConstant.OP_GT.equals(optName)) {// 大于
            // [toTimeStamp+1 TO 9999999999999]
            toTimeStamp = toTimeStamp + 1;
            range = "[" + toTimeStamp + " TO 9999999999999]";
        } else if (PropOperationConstant.OP_LT.equals(optName)) {// 小于
            // [0 TO　toTimeStamp-1]
            toTimeStamp = toTimeStamp - 1;
            range = "[0 TO " + toTimeStamp + "]";
        } else if (PropOperationConstant.OP_GE.equals(optName)) {// 大于等于
            // [toTimeStamp TO 9999999999999]
            range = "[" + toTimeStamp + " TO 9999999999999]";
        } else if (PropOperationConstant.OP_LE.equals(optName)) {// 小于等于
            // [0 TO　toTimeStamp]
            range = "[0 TO " + toTimeStamp + "]";
        }
        return range;
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
            query.append(DOUBLE_BLANK_AND).append(codeStr);
        }
        if(!"".equals(pathStr)){
            query.append(DOUBLE_BLANK_AND).append(pathStr);
        }
        if(!"".equals(coverageStr)){
            query.append(DOUBLE_BLANK_AND).append(coverageStr);
        }
        return query.toString();
    }

    private String dealWithParams4Exact() {
        if (CollectionUtils.isEmpty(this.params)) return "";
        StringBuffer query = new StringBuffer();
        String codeStr = dealWithSingleParam4Exact(TitanKeyWords.search_code.toString(), this.params.get(ES_SearchField.cg_taxoncode.toString()));
        this.params.remove(ES_SearchField.cg_taxoncode.toString());
        String pathStr = dealWithSingleParam4Exact(TitanKeyWords.search_path.toString(), this.params.get(ES_SearchField.cg_taxonpath.toString()));
        this.params.remove(ES_SearchField.cg_taxonpath.toString());
        String coverageStr = dealWithSingleParam4Exact(TitanKeyWords.search_coverage.toString(), this.params.get(ES_SearchField.coverages.toString()));
        this.params.remove(ES_SearchField.coverages.toString());
        if(!"".equals(codeStr)){
            query.append(DOUBLE_BLANK_AND).append(codeStr);
        }
        if(!"".equals(pathStr)){
            query.append(DOUBLE_BLANK_AND).append(pathStr);
        }
        if(!"".equals(coverageStr)){
            query.append(DOUBLE_BLANK_AND).append(coverageStr);
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
                List<String> values = entry.getValue();
                String codeKey = entry.getKey();
                if (CollectionUtils.isEmpty(values)) continue;
                for (String value : values) {
                    if (value.contains("$")) {
                        value = value.replace("$", "\\$");
                    }
                    if (value.contains("/")) {
                        value = value.replace("/", "\\\\/");
                    }
                    value = value.toLowerCase();

                    if (ES_OP.eq.toString().equals(codeKey) || ES_OP.in.toString().equals(codeKey)) {
                        if (value.contains(PropOperationConstant.OP_AND)) {
                            String[] strs=value.split(PropOperationConstant.OP_AND);
                            // TODO 处理成精确的
                            value = "(*" + strs[0].trim() + "*" + DOUBLE_BLANK_AND + "*" + strs[1].trim() + "*)";
                        }else{
                            // TODO 处理成精确的
                            value = "*" + value.trim() + "*";
                        }
                        queryCondition.append(value).append(" ");

                    } else if (ES_OP.ne.toString().equals(codeKey)) {
                        queryCondition.append("-").append("*").append(value.trim()).append("*").append(" ");
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

    private String dealWithSingleParam4Exact(String property, Map<String, List<String>> searchList) {
        StringBuffer query = new StringBuffer();
        if (CollectionUtils.isNotEmpty(searchList)) {
            query.append("v.\\\"");
            query.append(property);
            query.append("\\\":(");
            StringBuffer queryCondition = new StringBuffer();
            for (Map.Entry<String, List<String>> entry : searchList.entrySet()) {
                List<String> values = entry.getValue();
                String codeKey = entry.getKey();
                if (CollectionUtils.isEmpty(values)) continue;
                for (String value : values) {
                    if (TitanKeyWords.search_coverage.toString().equals(property)) {
                        if (value.split("/").length == 3) {
                            value = value + "/";
                        }
                    }
                    if (value.contains("$")) {
                        value = value.replace("$", "\\$");
                    }
                    if (value.contains("/")) {
                        value = value.replace("/", "\\\\/");
                    }
                    //value = value.toLowerCase();

                    if (ES_OP.eq.toString().equals(codeKey) || ES_OP.in.toString().equals(codeKey)) {
                        if (value.contains(PropOperationConstant.OP_AND)) {
                            String[] strs=value.split(PropOperationConstant.OP_AND);
                            value = "(" + strs[0].trim() +  DOUBLE_BLANK_AND +  strs[1].trim() + ")";
                        }else{
                            value =  value.trim() ;
                        }
                        queryCondition.append(value).append(" ");

                    } else if (ES_OP.ne.toString().equals(codeKey)) {
                        queryCondition.append("-").append(value.trim()).append(" ");
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

    /**
     *
     * debug/qa//,debug/qa/test/creating,debug/qa//creating,debug/qa/test/
     * k12/$on030000/$on030200/$sb0501012/$e004000/$e004001
     * $f050005,$on030000,pt01001,$ra0100
     * @param property
     * @param searchList
     * @return
     */
    private String dealWithSingleParamPrecise(String property, Map<String, List<String>> searchList) {
        StringBuffer query = new StringBuffer();
        if (CollectionUtils.isNotEmpty(searchList)) {
            query.append("v.\\\"");
            query.append(property);
            query.append("\\\":(");
            StringBuffer queryCondition = new StringBuffer();
            for (Map.Entry<String, List<String>> entry : searchList.entrySet()) {
                List<String> values = entry.getValue();
                String codeKey = entry.getKey();
                if (CollectionUtils.isEmpty(values)) continue;
                for (String value : values) {
                    if (value.contains("$")) {
                        value = value.replace("$", "\\$");
                    }
                    if (value.contains("/")) {
                        value = value.replace("/", "\\\\/");
                    }
                    value = value.toLowerCase();

                    if (ES_OP.eq.toString().equals(codeKey) || ES_OP.in.toString().equals(codeKey)) {
                        if (value.contains(PropOperationConstant.OP_AND)) {
                            String[] andOP = value.split(PropOperationConstant.OP_AND);
                            int length = andOP.length;
                            value = "(";
                            for (int i = 0; i < length; i++) {
                                value = value + toPreciseStr(property, andOP[i].trim());
                                if (i != length - 1) value = value + DOUBLE_BLANK_AND;
                            }
                            value = value +")";
                        }else{
                            value = toPreciseStr(property,value.trim());
                        }
                        queryCondition.append(value).append(" ");

                    } else if (ES_OP.ne.toString().equals(codeKey)) {
                        queryCondition.append("-").append("*").append(value.trim()).append("*").append(" ");
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

    /**
     * debug/qa//,debug/qa/test/creating,debug/qa//creating,debug/qa/test/
     * k12/$on030000/$on030200/$sb0501012/$e004000/$e004001
     * $f050005,$on030000,pt01001,$ra0100
     * 一共有四种情况： 1) xxx 2): xxx,*  3): *,xxx,* 4) *,xxx
     * @param value
     * @return
     */
    private String toPreciseStr(String property, String value) {
        if (TitanKeyWords.search_coverage_string.toString().equals(property)) {
            int length = value.split("/").length;
            if (length != 4) {
                if (length == 3) value = value + "\\\\/";
            }
        }
        StringBuffer script = new StringBuffer("(");
        script.append(value)
                .append(DOUBLE_BLANK_OR)
                .append(value).append(",*")
                .append(DOUBLE_BLANK_OR)
                .append("*,").append(value).append(",*")
                .append(DOUBLE_BLANK_OR)
                .append("*,").append(value)
                .append(")");

        return script.toString();
    }

    public enum WordsCover {
        title, description, keywords, tags, edu_description, cr_description
    }

    public enum PropsCover {
        publisher, creator, title, status, provider, author, identifier, languange, edulanguage, tags, keywords, ndres_code
    }

    public static void main(String[] args) {
        System.out.println("2016-07-05 21:50:14");
        String timestamp = StringUtils.strDateToTimeStamp("2016-07-05 21:50:14")+"";
        System.out.println(timestamp);
        System.out.println(StringUtils.strTimeStampToDate(timestamp));
        System.out.println(StringUtils.strTimeStampToDate("1467726614001"));
       // System.out.println("1:"+toRangeByOpt("gt","2016-07-05 21:50:14"));
        //System.out.println("2:"+toRangeByOpt("gt","2016\\\\-07\\\\-05 21\\\\:50\\\\:14"));
    }


}
