package nd.esp.service.lifecycle.services.titan;

import java.util.ArrayList;
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
 * @Title TitanResultItem
 * @Package nd.esp.service.lifecycle.services.titan
 * <p/>
 * *****************************************
 * @Description
 * @date 2016/8/10
 */
public class TitanResultItem {
    private Map<String, String> resource;
    private List<Map<String, String>> category;// code/id/path
    private List<Map<String, String>> techInfo;
    private Map<String, String> relationValues;// 边上的关系数据
    private Map<String, String> statisticsValues;

    public Map<String, String> getResource() {
        return resource;
    }

    public void setResource(Map<String, String> resource) {
        this.resource = resource;
    }

    public List<Map<String, String>> getCategory() {
        return category;
    }

    public void setCategory(List<Map<String, String>> category) {
        this.category = category;
    }

    public List<Map<String, String>> getTechInfo() {
        return techInfo;
    }

    public void setTechInfo(List<Map<String, String>> techInfo) {
        this.techInfo = techInfo;
    }

    public Map<String, String> getRelationValues() {
        return relationValues;
    }

    public void setRelationValues(Map<String, String> relationValues) {
        this.relationValues = relationValues;
    }

    public Map<String, String> getStatisticsValues() {
        return statisticsValues;
    }

    public void setStatisticsValues(Map<String, String> statisticsValues) {
        this.statisticsValues = statisticsValues;
    }
}
