package nd.esp.service.lifecycle.vos.knowledges.v06;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
/**
 * 知识点扩展属性(v06)
 * 
 * @author coacr
 *
 */
public class KnowledgeExtPropertiesViewModel {
    //用于标识知识点的父级节点
    private String parent;
    
    // 增加节点的参照对象。parent和target必须具备其一
    @JsonInclude(Include.NON_NULL)
    private String target;

    // 目标参照物的方向，之前或者之后。如果此值不存在，默认是next。如果target不存在，传递了parent，此值的默认值为子节点的末尾
    @JsonInclude(Include.NON_NULL)
    private String direction;
    
    //知识点的根节点,当rootNode为空时以学科的ndCode为根节点
    private String rootNode;
    
    /**
     * 获取知识点的父级节点
     * 
     * @return
     * @since
     */
    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }
    
    /**
     * 获取知识点的参照对象
     * 
     * @return 参照对象
     * @since
     */
    public String getTarget() {
        return target;
    }
    
    /**
     * 设置知识点的参照对象
     * 
     * @param target 参照对象
     * @since
     */
    public void setTarget(String target){
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

	public String getRootNode() {
		return rootNode;
	}

	public void setRootNode(String rootNode) {
		this.rootNode = rootNode;
	}
}
