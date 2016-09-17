package nd.esp.service.lifecycle.models.v06;

/**
 * 知识点位置属性 
 *
 * @author caocr
 *
 */
public class KnowledgePositionModel {
    // 用于标识知识点的父级节点，如果target有值，此值将被忽略 
    private String parent;
    
    //增加节点的参照对象。parent和target必须具备其一
    private String target;
    
    //目标参照物的方向，之前或者之后。如果此值不存在，默认是next。如果target不存在，传递了parent，此值的默认值为子节点的末尾 
    private String direction;
    
    /**
     * 获取知识点的父级节点
     * 
     * @return
     * @since
     */
    public String getParent() {
        return parent;
    }
    
    /**
     * 设置知识点的父级节点
     * 
     * @param parent 知识点的父级节点
     * @since
     */
    public void setParent(String parent){
        this.parent = parent;
    }
    
    /**
     * 获取知识点的参照对象
     * 
     * @return 参照对象
     * @since
     */
    public String getTraget() {
        return target;
    }
    
    /**
     * 设置知识点的参照对象
     * 
     * @param target 参照对象
     * @since
     */
    public void setTraget(String target){
        this.target = target;
    }
    
    /**
     * 获取知识点的目标参照物的方向
     * 
     * @return
     * @since
     */
    public String getDirection() {
        return direction;
    }
    
    /**
     * 设置知识点的目标参照物的方向
     * 
     * @param direction 目标参照物的方向
     * @since
     */
    public void setDirection(String direction){
        this.direction = direction;
    }

}
