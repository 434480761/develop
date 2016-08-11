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
    Map<String, String> resource;
    List<Map<String, String>> taxOnCodeLines;
    List<Map<String, String>> taxOnCodeIdLines;
    List<Map<String, String>> techInfoLines;
    String taxOnPath;

    public Map<String, String> getResource() {
        return resource;
    }

    public TitanResultItem setResource(Map<String, String> resource) {
        this.resource = resource;
        return this;
    }

    public List<Map<String, String>> getTaxOnCodeLines() {
        return taxOnCodeLines;
    }

    public TitanResultItem setTaxOnCodeLines(List<Map<String, String>> taxOnCodeLines) {
        this.taxOnCodeLines = taxOnCodeLines;
        return this;
    }

    public List<Map<String, String>> getTaxOnCodeIdLines() {
        return taxOnCodeIdLines;
    }

    public TitanResultItem setTaxOnCodeIdLines(List<Map<String, String>> taxOnCodeIdLines) {
        this.taxOnCodeIdLines = taxOnCodeIdLines;
        return this;
    }

    public List<Map<String, String>> getTechInfoLines() {
        return techInfoLines;
    }

    public TitanResultItem setTechInfoLines(List<Map<String, String>> techInfoLines) {
        this.techInfoLines = techInfoLines;
        return this;
    }

    public String getTaxOnPath() {
        return taxOnPath;
    }

    public TitanResultItem setTaxOnPath(String taxOnPath) {
        this.taxOnPath = taxOnPath;
        return this;
    }

}
