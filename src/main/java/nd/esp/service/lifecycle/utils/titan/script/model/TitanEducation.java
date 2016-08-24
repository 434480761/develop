package nd.esp.service.lifecycle.utils.titan.script.model;

import nd.esp.service.lifecycle.utils.titan.script.annotation.TitanPrimaryKey;
import nd.esp.service.lifecycle.utils.titan.script.annotation.TitanVertex;

/**
 * Created by Administrator on 2016/8/24.
 */
public class TitanEducation extends TitanModel{
    @TitanPrimaryKey
    private String identifier;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}
