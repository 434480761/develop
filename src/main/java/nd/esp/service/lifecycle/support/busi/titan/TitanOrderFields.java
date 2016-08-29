package nd.esp.service.lifecycle.support.busi.titan;

import nd.esp.service.lifecycle.support.enums.ES_SearchField;

import java.util.HashMap;
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
 * @Title TitanOrderFields
 * @Package nd.esp.service.lifecycle.support.busi.titan
 * <p/>
 * *****************************************
 * @Description
 * @date 2016/8/23
 */
public enum TitanOrderFields {
    title, lc_create_time, lc_last_update, ti_size, sort_num, cg_taxoncode, sta_key_value, top, scores, votes, views;

    private final static String VIP_LEVEL_LIKE = "RL.*";
    /**
     * 放置字符串值与枚举值的对应关系
     */
    private static Map<String, TitanOrderFields> map = new HashMap<>();

    // 初始化值
    static {
        for (TitanOrderFields field : TitanOrderFields.values()) {
            map.put(field.toString(), field);
        }
    }

    // 通过字符串值返回枚举值
    public static TitanOrderFields fromString(String opString) {
        return map.get(opString);
    }

    /**
     *
     * .choose(select('x').outE('has_resource_statistical').has('sta_key_title','downloads').has('sta_data_from','TOTAL')
     * ,select('x').outE('has_resource_statistical').has('sta_key_title','downloads').has('sta_data_from','TOTAL').values('sta_key_value')
     * ,__.constant(new Double(0.0)))
     * @param titanExpression
     * @param fieldValue
     * @param scriptParamMap
     * @param orderList
     */
    public void generateScript(TitanExpression titanExpression, String fieldValue, Map<String, Object> scriptParamMap, List<TitanOrder> orderList,boolean isShowVersion) {
        String asResult = "select('x')";
        if (isShowVersion) asResult = TitanKeyWords.select_version_result.toString();
        String orderBy = checkSortOrder(fieldValue);
        String edgeScript = "";
        StringBuffer script = new StringBuffer();
        TitanOrder order = new TitanOrder();
        if (this.equals(title) || this.equals(lc_create_time) || this.equals(lc_last_update)) {
            order.setOrderByField(this.toString());
        } else if (this.equals(sta_key_value)) {
            //desc#downloads#TOTAL
            String[] tmp = fieldValue.split("#");
            orderBy = checkSortOrder(tmp[0]);
            String keyTitle = TitanUtils.generateKey(scriptParamMap, TitanKeyWords.sta_key_title.toString());
            scriptParamMap.put(keyTitle, tmp[1]);
            String dataFrom = TitanUtils.generateKey(scriptParamMap, TitanKeyWords.sta_data_from.toString());
            scriptParamMap.put(dataFrom, tmp[2]);
            edgeScript = "outE('"
                    + TitanKeyWords.has_resource_statistical.toString()
                    + "').has('"
                    +TitanKeyWords.sta_key_title.toString()
                    +"',"
                    + keyTitle
                    + ").has('"
                    +TitanKeyWords.sta_data_from.toString()
                    +"',"
                    + dataFrom
                    + ")";
            script.append(".").append(asResult).append(".choose(").append(asResult).append(".").append(edgeScript).append(",")
                    .append(asResult).append(".").append(edgeScript).append(".values('").append(TitanOrderFields.sta_key_value.toString())
                    .append("'),__.constant(new Double(0.0)))");
            order.setScript(script.toString());
            // 边上的统计数据需要取回
            titanExpression.setStatistics(true, "," + edgeScript);
        } else if (this.equals(top) || this.equals(scores) || this.equals(votes) || this.equals(views)) {
            String keyTitle = TitanUtils.generateKey(scriptParamMap, TitanKeyWords.sta_key_title.toString());
            scriptParamMap.put(keyTitle, this.toString());
            edgeScript = "outE('"
                    + TitanKeyWords.has_resource_statistical.toString()
                    + "').has('"
                    + TitanKeyWords.sta_key_title.toString()
                    + "',"
                    + keyTitle
                    + ")";
            script.append(".").append(asResult).append(".choose(").append(asResult).append(".").append(edgeScript).append(",")
                    .append(asResult).append(".").append(edgeScript).append(".values('").append(TitanOrderFields.sta_key_value.toString())
                    .append("'),__.constant(new Double(0.0)))");
            order.setScript(script.toString());
            // 边上的统计数据需要取回
            titanExpression.setStatistics(true, "," + edgeScript);
        } else if (this.equals(ti_size)) {
            String valueKey = TitanUtils.generateKey(scriptParamMap, TitanKeyWords.ti_title.toString());
            scriptParamMap.put(valueKey, TitanKeyWords.href.toString());
            edgeScript = "outE('"
                    + TitanKeyWords.has_tech_info.toString()
                    + "').has('"
                    +TitanKeyWords.ti_title.toString()
                    +"',"
                    + valueKey
                    + ")";
            script.append(".").append(asResult).append(".choose(").append(asResult).append(".").append(edgeScript).append(",")
                    .append(asResult).append(".").append(edgeScript).append(".values('").append(TitanKeyWords.ti_size.toString())
                    .append("'),__.constant(new Long(0)))");
            order.setScript(script.toString());
        } /*else if (this.equals(sort_num)) {
        }*/ else if (this.equals(cg_taxoncode)) {
            String valueKey = TitanUtils.generateKey(scriptParamMap, ES_SearchField.cg_taxoncode.toString());
            scriptParamMap.put(valueKey, VIP_LEVEL_LIKE);
            edgeScript = "outE('"
                    + TitanKeyWords.has_category_code.toString()
                    + "').has('" + ES_SearchField.cg_taxoncode.toString()
                    + "',textRegex("
                    + valueKey
                    + "))";
            script.append(".").append(asResult).append(".choose(").append(asResult).append(".").append(edgeScript).append(",")
                    .append(asResult).append(".").append(edgeScript).append(".values('")
                    .append(ES_SearchField.cg_taxoncode.toString()).append("'),__.constant(''))");
            order.setScript(script.toString());
        } else {
            return;
        }

        order.setField(this.toString()).setSortOrder(orderBy);
        orderList.add(order);
    }

    /**
     * @param order
     * @return
     */
    private String checkSortOrder(String order) {
        if ("asc".equals(order)) {
            return TitanOrder.SORTORDER.ASC.toString();
        }
        return TitanOrder.SORTORDER.DESC.toString();
    }
}
