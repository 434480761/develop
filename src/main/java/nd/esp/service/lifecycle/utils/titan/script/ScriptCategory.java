package nd.esp.service.lifecycle.utils.titan.script;

import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.model.ResourceCategory;
import nd.esp.service.lifecycle.support.busi.titan.TitanKeyWords;
import nd.esp.service.lifecycle.support.enums.ES_SearchField;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liuran on 2016/8/5.
 */
public class ScriptCategory extends ScriptAbstract{
    private ResourceCategory resourceCategory;
    public ScriptCategory(ResourceCategory resourceCategory){
        this.resourceCategory = resourceCategory;
    }
    @Override
    public String name() {
        return "CG";
    }

    @Override
    String resourceIdentifier() {
        return resourceCategory.getResource();
    }

    @Override
    String resourcePrimaryCategory() {
        return resourceCategory.getPrimaryCategory();
    }

    @Override
    String nodeLabel() {
        return TitanKeyWords.category_code.toString();
    }

    @Override
    String edgeLabel() {
        return TitanKeyWords.has_category_code.toString();
    }


    @Override
    Map<String, Object> customProperty() {
        return null;
    }

    @Override
    EspEntity entity() {
        return resourceCategory;
    }

    @Override
    Map<String, Object> searchUniqueNodeProperty() {
        Map<String, Object> map = new HashMap<>();
        map.put(ES_SearchField.cg_category_code.toString(),resourceCategory);
        return map;
    }

    @Override
    Map<String, Object> searchUniqueEdgeProperty() {
        Map<String, Object> map = new HashMap<>();
        map.put(ES_SearchField.identifier.toString(),resourceCategory.getIdentifier());
        return map;
    }
}
