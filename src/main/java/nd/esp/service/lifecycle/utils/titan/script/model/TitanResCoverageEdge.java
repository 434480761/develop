package nd.esp.service.lifecycle.utils.titan.script.model;

import nd.esp.service.lifecycle.utils.titan.script.annotation.TitanEdge;
import nd.esp.service.lifecycle.utils.titan.script.annotation.TitanField;
import nd.esp.service.lifecycle.utils.titan.script.annotation.TitanVertex;

/**
 * Created by Administrator on 2016/8/24.
 */
@TitanEdge(label = "has_coverage")
public class TitanResCoverageEdge {

    @TitanField(name = "target_type")
    private String targetType;

    @TitanField(name = "strategy")
    private String strategy;

    @TitanField(name = "target")
    private String target;

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }
}
