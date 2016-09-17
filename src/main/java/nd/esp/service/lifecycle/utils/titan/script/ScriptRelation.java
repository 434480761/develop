package nd.esp.service.lifecycle.utils.titan.script;

import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.model.ResourceRelation;
import nd.esp.service.lifecycle.support.busi.titan.TitanKeyWords;

import java.util.List;
import java.util.Map;

/**
 * Created by liuran on 2016/8/5.
 */
public class ScriptRelation extends ScriptAbstract{
    private ResourceRelation resourceRelation;
    public ScriptRelation(ResourceRelation resourceRelation){
        this.resourceRelation = resourceRelation;
    }
    @Override
    public String name() {
        return "RR";
    }

    @Override
    String resourceIdentifier() {
        return resourceRelation.getSourceUuid();
    }

    @Override
    String resourcePrimaryCategory() {
        return resourceRelation.getResType();
    }

    @Override
    String nodeLabel() {
        return null;
    }

    @Override
    String edgeLabel() {
        return TitanKeyWords.has_relation.toString();
    }


    @Override
    Map<String, Object> customProperty() {
        return null;
    }

    @Override
    EspEntity entity() {
        return resourceRelation;
    }

    @Override
    Map<String, Object> searchUniqueNodeProperty() {
        return null;
    }

    @Override
    Map<String, Object> searchUniqueEdgeProperty() {
        return null;
    }
}
