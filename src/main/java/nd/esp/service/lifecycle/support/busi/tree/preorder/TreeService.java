/* =============================================================
 * Created: [2015年11月27日] by linsm
 * =============================================================
 *
 * Copyright 2014-2015 NetDragon Websoft Inc. All Rights Reserved
 *
 * =============================================================
 */

package nd.esp.service.lifecycle.support.busi.tree.preorder;

/**
 * @author linsm
 * @since 
 * only operate on left, right ,parent, level
 */
public interface TreeService {
    
    @Deprecated
    TreeModel insertSubTree(TreeModel target, TreeModel parent, TreeModel current,TreeDirection treeDirection);
    
    /**
     * 插入叶子结点
     * @author linsm
     * @param model
     * @param treeDirection
     * @return
     * @since
     */
    TreeModel insertLeaf(TreeTrargetAndParentModel model,TreeDirection treeDirection);
    
    /**
     * 移动子树
     * @author linsm
     * @param model
     * @param current
     * @param treeDirection
     * @since
     */
    void moveSubTree(TreeTrargetAndParentModel model, TreeModel current, TreeDirection treeDirection);

    /**
     * 删除子树
     * @author linsm
     * @param current
     * @since
     */
    void removeSubTree(TreeModel current);
}
