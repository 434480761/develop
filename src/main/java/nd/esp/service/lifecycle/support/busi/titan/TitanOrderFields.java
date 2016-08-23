package nd.esp.service.lifecycle.support.busi.titan;

import java.util.HashMap;
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

    public String generateScipt( TitanExpression titanExpression,Map<String, Object> scriptParamMap) {
        StringBuffer scriptBuffer = new StringBuffer();
        if (this.equals(title)) {

        } else if (this.equals(lc_create_time)) {
        } else if (this.equals(lc_last_update)) {
        } else if (this.equals(ti_size)) {
            return "choose(__.outE('has_tech_info').has('ti_title','href'),__.values('ti_size'),__.constant(0))";
        } else if (this.equals(sort_num)) {
        } else if (this.equals(cg_taxoncode)) {
        } else if (this.equals(top)) {
        } else if (this.equals(scores)) {
        } else if (this.equals(votes)) {
        } else if (this.equals(views)) {
        }
        return scriptBuffer.toString();
    }
}
