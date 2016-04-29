package nd.esp.service.lifecycle.vos.knowledges.v06;

import nd.esp.service.lifecycle.vos.valid.LessPropertiesDefault;

import org.hibernate.validator.constraints.NotBlank;

/**
 * 知识点和知识点之间的关系
 * 
 * @author caocr
 */
public class KnowledgeRelationsViewModel4Add {
    // 知识点关系id
    private String identifier;

    // 源知识点
    @NotBlank(message = "{knowledgeViewModel.knowledge_relations.source.notBlank.validmsg}", groups = { LessPropertiesDefault.class })
    private String source;

    // 目标知识点
    @NotBlank(message = "{knowledgeViewModel.knowledge_relations.target.notBlank.validmsg}", groups = { LessPropertiesDefault.class })
    private String target;

    // 知识点之间的关系类型
    @NotBlank(message = "{knowledgeViewModel.knowledge_relations.relation_type.notBlank.validmsg}", groups = { LessPropertiesDefault.class })
    private String relationType;

    // 上下文类型
    @NotBlank(message = "{knowledgeViewModel.knowledge_relations.context_type.notBlank.validmsg}", groups = { LessPropertiesDefault.class })
    private String contextType;

    // 上下文对象
    @NotBlank(message = "{knowledgeViewModel.knowledge_relations.context_object.notBlank.validmsg}", groups = { LessPropertiesDefault.class })
    private String contextObject;

    /**
     * 获取知识点关系id
     * 
     * @return 知识点关系id
     * @since
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * 设置知识点关系id
     * 
     * @param identifier 知识点关系id
     * @since
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * 获取源知识点
     * 
     * @return 源知识点
     * @since
     */
    public String getSource() {
        return source;
    }

    /**
     * 设置源知识点
     * 
     * @param source 源知识点
     * @since
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * 获取目标知识点
     * 
     * @return 目标知识点
     * @since
     */
    public String getTarget() {
        return target;
    }

    /**
     * 设置目标知识点
     * 
     * @param target 目标知识点
     * @since
     */
    public void setTarget(String target) {
        this.target = target;
    }

    /**
     * 获取知识点之间的关系类型
     * 
     * @return 知识点之间的关系类型
     * @since
     */
    public String getRelationType() {
        return relationType;
    }

    /**
     * 设置知识点之间的关系类型
     * 
     * @param relation_type 知识点之间的关系类型
     * @since
     */
    public void setRelationType(String relationType) {
        this.relationType = relationType;
    }

    /**
     * 获取上下文类型
     * 
     * @return 上下文类型
     * @since
     */
    public String getContextType() {
        return contextType;
    }

    /**
     * 设置上下文类型
     * 
     * @param context_type 上下文类型
     * @since
     */
    public void setContextType(String contextType) {
        this.contextType = contextType;
    }

    /**
     * 获取上下文对象
     * 
     * @return 上下文对象
     * @since
     */
    public String getContextObject() {
        return contextObject;
    }

    /**
     * 设置上下文对象
     * 
     * @param context_type 上下文对象
     * @since
     */
    public void setContextObject(String contextObject) {
        this.contextObject = contextObject;
    }

}
