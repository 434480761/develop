package nd.esp.service.lifecycle.utils.titan.script.model;

import nd.esp.service.lifecycle.utils.titan.script.annotation.TitanCompositeKey;
import nd.esp.service.lifecycle.utils.titan.script.annotation.TitanEdge;
import nd.esp.service.lifecycle.utils.titan.script.annotation.TitanField;

/**
 * Created by Administrator on 2016/9/14.
 */
@TitanEdge(label = "has_category_code")
public class TitanResourceCategoryEdge extends TitanModel{
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
    protected String identifier;

    @TitanField(name = "cg_taxonpath")
    private String taxonpath;
}
