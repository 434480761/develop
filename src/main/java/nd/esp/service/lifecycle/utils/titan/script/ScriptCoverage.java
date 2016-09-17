package nd.esp.service.lifecycle.utils.titan.script;

import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.model.ResCoverage;
import nd.esp.service.lifecycle.support.busi.titan.TitanKeyWords;
import nd.esp.service.lifecycle.support.enums.ES_Field;
import nd.esp.service.lifecycle.support.enums.ES_SearchField;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liuran on 2016/8/5.
 */
public class ScriptCoverage extends ScriptAbstract{
    private ResCoverage resCoverage;
    public ScriptCoverage(ResCoverage resCoverage){
        this.resCoverage = resCoverage;
    }
    @Override
    public String name() {
        return "COV";
    }

    @Override
    String resourceIdentifier() {
        return resCoverage.getResource();
    }

    @Override
    String resourcePrimaryCategory() {
        return resCoverage.getResType();
    }

    @Override
    String nodeLabel() {
        return TitanKeyWords.coverage.toString();
    }

    @Override
    String edgeLabel() {
        return TitanKeyWords.has_coverage.toString();
    }

    @Override
    Map<String, Object> customProperty() {
        return null;
    }

    @Override
    EspEntity entity() {
        return resCoverage;
    }

    @Override
    Map<String, Object> searchUniqueNodeProperty() {
        Map<String, Object> map = new HashMap<>();
        map.put(ES_Field.target_type.toString(),resCoverage.getTargetType());
        map.put(ES_Field.target.toString(),resCoverage.getTarget());
        map.put(ES_Field.strategy.toString(),resCoverage.getStrategy());
        return map;
    }

    @Override
    Map<String, Object> searchUniqueEdgeProperty() {
        Map<String, Object> map = new HashMap<>();
        map.put(ES_SearchField.identifier.toString(),resCoverage.getIdentifier());
        return map;
    }
}
