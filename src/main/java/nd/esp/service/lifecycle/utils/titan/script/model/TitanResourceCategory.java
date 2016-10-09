package nd.esp.service.lifecycle.utils.titan.script.model;

import nd.esp.service.lifecycle.utils.titan.script.annotation.TitanCompositeKey;
import nd.esp.service.lifecycle.utils.titan.script.annotation.TitanField;
import nd.esp.service.lifecycle.utils.titan.script.annotation.TitanVertex;
/**
 * Created by Administrator on 2016/9/14.
 */
@TitanVertex(label = "category_code")
public class TitanResourceCategory extends TitanModel{
    @TitanCompositeKey
    @TitanField(name = "cg_taxoncode")
    private String taxoncode;
    @TitanField(name = "cg_taxonname")
    private String taxonname;
    @TitanField( name = "cg_short_name")
    private String shortName;

    @TitanField( name = "cg_category_code")
    private String categoryCode;

    @TitanField( name = "cg_category_name")
    private String categoryName;

    public String getTaxoncode() {
        return taxoncode;
    }

    public void setTaxoncode(String taxoncode) {
        this.taxoncode = taxoncode;
    }

    public String getTaxonname() {
        return taxonname;
    }

    public void setTaxonname(String taxonname) {
        this.taxonname = taxonname;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
