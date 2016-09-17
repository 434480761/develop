package nd.esp.service.lifecycle.models.v06;


/**
 * 知识点和知识点之间的关系
 * 
 * @author caocr
 *
 */
public class KnowledgeRelationsModel {
    //知识点关系id
    private String identifier;
    
    //源知识点
    private String source;
    
    //目标知识点
    private String target;
    
    //知识点之间的关系类型
    private String relationType;
    
    //上下文类型 
    private String contextType;
    
    //上下文对象 
    private String contextObject;
    
    // 源知识点模型
    private KnowledgeModel sourceKnowledgeModel;

    // 目标知识点模型
    private KnowledgeModel targetKnowledgeModel;
    
    /**
     * 获取知识点关系id
     * 
     * @return 知识点关系id
     * @since
     */
    public String getIdentifier(){
        return identifier;
    }
    
    /**
     * 设置知识点关系id
     * 
     * @param identifier 知识点关系id
     * @since
     */
    public void setIdentifier(String identifier){
        this.identifier = identifier;
    }
    
    /**
     * 获取源知识点
     * 
     * @return 源知识点
     * @since
     */
    public String getSource(){
        return source;
    }
    
    /**
     * 设置源知识点
     * 
     * @param source 源知识点
     * @since
     */
    public void setSource(String source){
        this.source = source;
    }
    
    /**
     * 获取目标知识点
     * 
     * @return 目标知识点
     * @since
     */
    public String getTarget(){
        return target;
    }
    
    /**
     * 设置目标知识点
     * 
     * @param target 目标知识点
     * @since
     */
    public void setTarget(String target){
        this.target = target;
    }
    
    /**
     * 获取源知识点模型
     * 
     * @return 源知识点模型
     * @since
     */
    public KnowledgeModel getSourceKnowledgeModel(){
        return sourceKnowledgeModel;
    }
    
    /**
     * 设置源知识点模型
     * 
     * @param sourceKnowledgeModel 源知识点模型
     * @since
     */
    public void setSourceKnowledgeModel(KnowledgeModel sourceKnowledgeModel){
        this.sourceKnowledgeModel = sourceKnowledgeModel;
    }
    
    /**
     * 获取目标知识点模型
     * 
     * @return 目标知识点模型
     * @since
     */
    public KnowledgeModel getTargetKnowledgeModel(){
        return targetKnowledgeModel;
    }
    
    /**
     * 设置目标知识点模型
     * 
     * @param targetKnowledgeModel 目标知识点模型
     * @since
     */
    public void setTargetKnowledgeModel(KnowledgeModel targetKnowledgeModel){
        this.targetKnowledgeModel = targetKnowledgeModel;
    }
    
    /**
     * 获取知识点之间的关系类型
     * 
     * @return 知识点之间的关系类型
     * @since
     */
    public String getRelationType(){
        return relationType;
    }
    
    /**
     * 设置知识点之间的关系类型
     * 
     * @param relation_type 知识点之间的关系类型
     * @since
     */
    public void setRelationType(String relationType){
        this.relationType = relationType;
    }
    
    /**
     * 获取上下文类型 
     * 
     * @return 上下文类型 
     * @since
     */
    public String getContextType(){
        return contextType;
    }
    
    /**
     * 设置上下文类型 
     * 
     * @param context_type 上下文类型 
     * @since
     */
    public void setContextType(String contextType){
        this.contextType = contextType;
    }
    
    /**
     * 获取上下文对象 
     * 
     * @return 上下文对象
     * @since
     */
    public String getContextObject(){
        return contextObject;
    }
    
    /**
     * 设置上下文对象
     * 
     * @param context_type 上下文对象 
     * @since
     */
    public void setContextObject(String contextObject){
        this.contextObject = contextObject;
    }
    
}
