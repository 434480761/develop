package nd.esp.service.lifecycle.utils.titan.script.model;

import nd.esp.service.lifecycle.utils.titan.script.annotation.TitanCompositeKey;
import nd.esp.service.lifecycle.utils.titan.script.annotation.TitanField;

/**
 * Created by Administrator on 2016/8/24.
 */
public class TitanEducation extends TitanModel{
    @TitanCompositeKey
    @TitanField
    private String identifier;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}
