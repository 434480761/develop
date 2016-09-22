package nd.esp.service.lifecycle.utils.titan.script.model;

import nd.esp.service.lifecycle.utils.titan.script.annotation.*;

/**
 * Created by Administrator on 2016/8/24.
 */
@TitanEdge(label = "has_coverage")
public class TitanResCoverageEdge extends TitanModel{

    @TitanEdgeTargetKey(target = "target_type")
    @TitanField(name = "target_type")
    private String targetType;

    @TitanEdgeTargetKey(target = "strategy")
    @TitanField(name = "strategy")
    private String strategy;

    @TitanEdgeTargetKey(target = "target")
    @TitanField(name = "target")
    private String target;

    @TitanCompositeKey
    @TitanField(name = "identifier")
    private String identifier;

    @TitanEdgeResourceKey(source = "identifier")
    private String resource;

    @TitanEdgeResourceKey(source = "primary_category")
    private String resType;

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getResType() {
        return resType;
    }

    public void setResType(String resType) {
        this.resType = resType;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

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
