package nd.esp.service.lifecycle.utils.titan.script.model;

import nd.esp.service.lifecycle.utils.titan.script.annotation.*;

/**
 * Created by Administrator on 2016/9/14.
 */
@TitanEdge(label = "has_category_code")
public class TitanResourceCategoryEdge extends TitanModel{
    @TitanEdgeTargetKey(target ="cg_taxoncode")
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

    @TitanCompositeKey
    @TitanField(name = "identifier")
    private String identifier;

    @TitanField(name = "cg_taxonpath")
    private String taxonpath;

//    @TitanEdgeResourceKey(source="primary_category")
    private String primaryCategory;

    @TitanEdgeResourceKey(source="identifier")
    private String resource;

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

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getTaxonpath() {
        return taxonpath;
    }

    public void setTaxonpath(String taxonpath) {
        this.taxonpath = taxonpath;
    }

    public String getPrimaryCategory() {
        return primaryCategory;
    }

    public void setPrimaryCategory(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }
}
