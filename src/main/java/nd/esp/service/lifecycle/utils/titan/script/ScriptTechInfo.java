package nd.esp.service.lifecycle.utils.titan.script;

import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.model.TechInfo;
import nd.esp.service.lifecycle.support.busi.titan.TitanKeyWords;
import nd.esp.service.lifecycle.support.enums.ES_SearchField;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liuran on 2016/8/5.
 */
public class ScriptTechInfo extends ScriptAbstract{
    private TechInfo techInfo;
    public ScriptTechInfo(TechInfo techInfo){
        this.techInfo = techInfo;
    }
    @Override
    public String name() {
        return "TI";
    }

    @Override
    String resourceIdentifier() {
        return techInfo.getResource();
    }

    @Override
    String resourcePrimaryCategory() {
        return techInfo.getResType();
    }

    @Override
    String nodeLabel() {
        return TitanKeyWords.tech_info.toString();
    }

    @Override
    String edgeLabel() {
        return TitanKeyWords.has_tech_info.toString();
    }


    @Override
    Map<String, Object> customProperty() {
        return null;
    }

    @Override
    EspEntity entity() {
        return techInfo;
    }

    @Override
    Map<String, Object> searchUniqueNodeProperty() {
        Map<String, Object> map = new HashMap<>();
        map.put(ES_SearchField.identifier.toString(), techInfo.getIdentifier());
        return map;
    }

    @Override
    Map<String, Object> searchUniqueEdgeProperty() {
        Map<String, Object> map = new HashMap<>();
        map.put(ES_SearchField.identifier.toString(),techInfo.getIdentifier());
        return map;
    }
}
