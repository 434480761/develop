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

    public void setResource(Map<String, String> resource) {
        this.resource = resource;
    }

    public List<Map<String, String>> getTaxOnCodeLines() {
        return taxOnCodeLines;
    }

    public void setTaxOnCodeLines(List<Map<String, String>> taxOnCodeLines) {
        this.taxOnCodeLines = taxOnCodeLines;
    }

    public List<Map<String, String>> getTaxOnCodeIdLines() {
        return taxOnCodeIdLines;
    }

    public void setTaxOnCodeIdLines(List<Map<String, String>> taxOnCodeIdLines) {
        this.taxOnCodeIdLines = taxOnCodeIdLines;
    }

    public List<Map<String, String>> getTechInfoLines() {
        return techInfoLines;
    }

    public void setTechInfoLines(List<Map<String, String>> techInfoLines) {
        this.techInfoLines = techInfoLines;
    }

    public String getTaxOnPath() {
        return taxOnPath;
    }

    public void setTaxOnPath(String taxOnPath) {
        this.taxOnPath = taxOnPath;
    }
}
