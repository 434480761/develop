/* =============================================================
 * Created: [2015年11月30日] by linsm
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
 *
 */
public class TreeTrargetAndParentModel {

    private TreeModel target; //目标结点
    private TreeModel parent; //目标父结点
    public TreeModel getTarget() {
        return target;
    }
    public void setTarget(TreeModel target) {
        this.target = target;
    }
    public TreeModel getParent() {
        return parent;
    }
    public void setParent(TreeModel parent) {
        this.parent = parent;
    }
}
