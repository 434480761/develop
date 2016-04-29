package nd.esp.service.lifecycle.vos.knowledges.v06;

import org.hibernate.validator.constraints.NotBlank;

/**
 * 06知识点用于移动的viewModel
 * <p>Create Time: 2015年9月6日           </p>
 * @author caocr
 */
public class KnowledgeViewModel4Move {
    /**
     * 知识点的id
     */
    private String identifier;
    
    /**
     * 父级节点
     */
    @NotBlank(message="{knowledgeViewModel4Move.parent.notBlank.validmsg}")
    private String parent;
    
    /**
     * 目标参照知识点的id,如果不填写，默认增加在同级节点的最末一个节点
     */
    private String target;
    
    /**
     * 方向，默认值是next
     */
    private String direction;
    
    public String getIdentifier() {
        return identifier;
    }
    
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    
    public String getParent() {
        return parent;
    }
    
    public void setParent(String parent) {
        this.parent = parent;
    }
    
    public String getTarget() {
        return target;
    }
    
    public void setTarget(String target) {
        this.target = target;
    }
    
    public String getDirection() {
        return direction;
    }
    
    public void setDirection(String direction) {
        this.direction = direction;
    }
}
