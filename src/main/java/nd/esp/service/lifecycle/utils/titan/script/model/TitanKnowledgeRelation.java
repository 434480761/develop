package nd.esp.service.lifecycle.utils.titan.script.model;

import nd.esp.service.lifecycle.utils.titan.script.annotation.*;

/**
 * Created by Administrator on 2016/9/21.
 */
@TitanEdge(label = "has_knowledge_relation")
public class TitanKnowledgeRelation extends TitanModel{
    @TitanCompositeKey
    @TitanField(name = "identifier")
    protected String identifier;

    @TitanField(name = "context_object")
    private String contextObject;

    @TitanField(name = "context_type")
    private String contextType;

    @TitanField(name = "relation_type")
    private String relationType;

    @TitanEdgeResourceKey(source = "identifier")
    private String source;

    @TitanEdgeTargetKey(target = "identifier")
    private String target;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getContextObject() {
        return contextObject;
    }

    public void setContextObject(String contextObject) {
        this.contextObject = contextObject;
    }

    public String getContextType() {
        return contextType;
    }

    public void setContextType(String contextType) {
        this.contextType = contextType;
    }

    public String getRelationType() {
        return relationType;
    }

    public void setRelationType(String relationType) {
        this.relationType = relationType;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }
}
