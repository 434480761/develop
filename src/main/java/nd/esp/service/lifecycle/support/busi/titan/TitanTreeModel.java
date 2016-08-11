package nd.esp.service.lifecycle.support.busi.titan;

import nd.esp.service.lifecycle.support.busi.tree.preorder.TreeDirection;

/**
 * Created by liuran on 2016/6/7.
 */
public class TitanTreeModel {
    //根节点：章节教材ID、知识学科tanoncode
    private String root;
    //父节点：父节点ID，如果ROOT表示知识点根节点是学科
    private String parent;
    //目标节点
    private String target;
    //资源ID
    private String source;
    //目标节点的方向
    private TreeDirection treeDirection;
    //树形结构的类型，章节、知识点
    private TitanTreeType treeType;
    
    private Long titanRootId;

    public void setRoot(String root) {
        this.root = root;
    }

    public String getRoot() {
        return root;
    }

    public String getParent() {
        return parent;
    }

    public String getTarget() {
        return target;
    }

    public String getSource() {
        return source;
    }

    public TreeDirection getTreeDirection() {
        return treeDirection;
    }

    public TitanTreeType getTreeType() {
        return treeType;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setTreeDirection(TreeDirection treeDirection) {
        this.treeDirection = treeDirection;
    }

    public void setTreeType(TitanTreeType treeType) {
        this.treeType = treeType;
    }

	public Long getTitanRootId() {
		return titanRootId;
	}

	public void setTitanRootId(Long titanRootId) {
		this.titanRootId = titanRootId;
	}
}
