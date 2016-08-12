package nd.esp.service.lifecycle.services.titan;

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
 * @Title KnowledegTitanResultItem
 * @Package nd.esp.service.lifecycle.services.titan
 * <p/>
 * *****************************************
 * @Description
 * @date 2016/8/12
 */
public class KnowledegTitanResultItem extends TitanResultItem {
    private String parent;
    private int order;

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
