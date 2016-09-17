package nd.esp.service.lifecycle.utils.tree;

import java.util.List;

/**
 * Tree节点
 * <p>Create Time: 2015年4月21日           </p>
 * @author xiezy
 */
public class TreeNode {
    /**
     * id
     */
    protected String nodeId;
    /**
     * 节点名
     */
    protected String nodeName;
    /**
     * 父节点id
     */
    protected String parentId;
    /**
     * 子节点集合
     */
    private List<TreeNode> children;
    /**
     * 深度
     */
    private Integer level;
    /**
     * 是否为叶子节点
     */
    private Boolean isLeaf;
    
    public String getNodeId() {
        return nodeId;
    }
    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }
    public String getNodeName() {
        return nodeName;
    }
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }
    public String getParentId() {
        return parentId;
    }
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
    public List<TreeNode> getChildren() {
        return children;
    }
    public void setChildren(List<TreeNode> children) {
        this.children = children;
    }
    public Integer getLevel() {
        return level;
    }
    public void setLevel(Integer level) {
        this.level = level;
    }
    public Boolean getIsLeaf() {
        return isLeaf;
    }
    public void setIsLeaf(Boolean isLeaf) {
        this.isLeaf = isLeaf;
    }
    
    @Override
    public String toString() {
        return "TreeNode [nodeId=" + nodeId + ", nodeName=" + nodeName + "]";
    }
}
