package nd.esp.service.lifecycle.support.busi.titan;

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
 * @Title TitanOrder
 * @Package nd.esp.service.lifecycle.support.busi.titan
 * <p/>
 * *****************************************
 * @Description
 * @date 2016/8/22
 */
public class TitanOrder {
    private String field;
    private String script;
    private String orderByField;
    private String sortOrder;

    public TitanOrder(String field, String script, String sortOrder) {
        this.field = field;
        this.script = script;
        this.sortOrder = sortOrder;
    }
    public TitanOrder() {}

    public String getField() {
        return field;
    }

    public TitanOrder setField(String field) {
        this.field = field;
        return this;
    }

    public String getScript() {
        return script;
    }

    public TitanOrder setScript(String script) {
        this.script = script;
        return this;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public TitanOrder setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
        return this;
    }

    public String getOrderByField() {
        return orderByField;
    }

    public TitanOrder setOrderByField(String orderByField) {
        this.orderByField = orderByField;
        return this;
    }

    public static String checkSortOrder(String order) {
        if ("asc".equals(order)) {
            return SORTORDER.ASC.toString();
        }
        return SORTORDER.DESC.toString();
    }


    public enum SORTORDER {
        // 1、DESC=decr 从大到小排序 2、ACS=incr 从小到大排序
        DESC("decr"), ASC("incr");
        private String name;

        SORTORDER(String name) {
            this.name = name;
        }

        SORTORDER() {

        }

        @Override
        public String toString() {
            if (name == null) {
                return super.toString();
            } else {
                return name;
            }
        }
    }
}
