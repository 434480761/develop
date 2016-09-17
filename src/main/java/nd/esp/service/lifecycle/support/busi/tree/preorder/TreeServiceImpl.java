/* =============================================================
 * Created: [2015年11月27日] by linsm
 * =============================================================
 *
 * Copyright 2014-2015 NetDragon Websoft Inc. All Rights Reserved
 *
 * =============================================================
 */

package nd.esp.service.lifecycle.support.busi.tree.preorder;

import junit.framework.Assert;
import nd.esp.service.lifecycle.daos.teachingmaterial.v06.ChapterDao;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.vos.chapters.v06.ChapterConstant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author linsm
 * @since
 */
@Service
public class TreeServiceImpl implements TreeService {

    // FIXME 到时与知识点结合在一起，
    @Autowired
    private ChapterDao chapterDao;

    /*
     * (non-Javadoc)
     * @see
     * nd.esp.service.lifecycle.support.busi.tree.preorder.TreeService#insertSubTree(nd.esp.service.lifecycle.support
     * .busi.tree.preorder.TreeModel, nd.esp.service.lifecycle.support.busi.tree.preorder.TreeModel,
     * nd.esp.service.lifecycle.support.busi.tree.preorder.TreeModel,
     * nd.esp.service.lifecycle.support.busi.tree.preorder.TreeDirection)
     */
    @Deprecated
    @Override
    public TreeModel insertSubTree(TreeModel target, TreeModel parent, TreeModel current, TreeDirection treeDirection) {

        return null;

    }

    /*
     * @author linsm
     * @since
     */
    @Override
    public void moveSubTree(TreeTrargetAndParentModel model, TreeModel current, TreeDirection treeDirection) {
        Assert.assertNotNull(model);

        TreeModel target = model.getTarget();
        TreeModel parent = model.getParent();
        // 是否与子树合在交叉在一起
        checkMoveValid(target, parent, current);

        int span = current.getRight() - current.getLeft();

        TreeModel newCurrent = getNewCurrent(target, parent, span, treeDirection);

        // 需要移动
        if (current.getLeft() != newCurrent.getLeft()) {
            int difference = newCurrent.getLeft() - current.getLeft();
            int moveSubTreeSpan = current.getRight()-current.getLeft()+1;  //移动子树占用长度
            String mid= newCurrent.getRoot();

            // FIXME 临时方案，移到负的。
            int temp = current.getRight() + 1;
            synchronized (this) {
                chapterDao.moveChapters2TargetPosition(mid, -temp, current.getLeft(), current.getRight());
                if (difference > 0) {
                    // 向后移
                    // 去除移动子树本身占用的空间
                    difference = difference - (moveSubTreeSpan); // 扣除自身占用空间长度
                    chapterDao.moveBackChapters(mid, -moveSubTreeSpan, current.getRight(), newCurrent.getLeft());
                } else {
                    chapterDao.moveForwardChapters(mid, moveSubTreeSpan, current.getLeft(), newCurrent.getLeft());
                }

                // FIXME 临时方案，移到正的temp
                chapterDao.moveChapters2TargetPosition(mid,
                                                       difference + temp,
                                                       current.getLeft() - temp,
                                                       current.getRight() - temp);

                if (!newCurrent.getParent().equals(current.getParent())) {
                    chapterDao.updateChapterParent(current.getIdentifier(), newCurrent.getParent());
                }
            }

        }

    }

    /** 取得插入的位置，注意只保证newCurrent.getLeft有效 （当子树向后移动时，需要去掉自身的跨度）
     * @author linsm
     * @param target
     * @param parent
     * @param span
     * @param treeDirection
     * @return
     * @since
     */
    private TreeModel getNewCurrent(TreeModel target, TreeModel parent, int span, TreeDirection treeDirection) {
        TreeModel newCurrent = new TreeModel();

        if (target != null) {
            // not care about parent
            switch (treeDirection) {
                case pre:
                    newCurrent.setLeft(target.getLeft());
                    break;
                case next:
                    newCurrent.setLeft(target.getRight() + 1);
                    break;
                default:
                    throw new RuntimeException("not support the direction:" + treeDirection);
            }
            // FIXME
            // newCurrent.setLevel(target.getLevel());
            newCurrent.setParent(target.getParent());
            newCurrent.setRoot(target.getRoot());
            newCurrent.setRight(newCurrent.getLeft() + span); // leaf

        } else if (parent != null) {
            newCurrent.setLeft(parent.getRight());
            newCurrent.setRight(newCurrent.getLeft() + span);
            newCurrent.setParent(parent.getIdentifier());
            newCurrent.setRoot(parent.getRoot());
            // FIXME
            // newCurrent.setLevel(parent.getLevel()+1);
        } else {
            throw new RuntimeException("target and parent can't be both null");
        }

        return newCurrent;
    }

    /**
     * 验证移动位置是否合法（如子树中）
     * @author linsm
     * @param target
     * @param parent
     * @param current
     * @since
     */
    private void checkMoveValid(TreeModel target, TreeModel parent, TreeModel current) {
        if (target != null) {
            if (isSubNode(target, current)) {
                throw new LifeCircleException(LifeCircleErrorMessageMapper.CheckParamValidFail.getCode(),"target is in the subtree");
            }
        } else if (parent != null) {
            if (isSubNode(parent, current)) {
                throw new LifeCircleException(LifeCircleErrorMessageMapper.CheckParamValidFail.getCode(),"parent is in the subtree");
            }
        }

    }

    /**
     * 判断是否包含在子树中(但不包含子树根)
     * @author linsm
     * @param target
     * @param current
     * @return
     * @since
     */
    private boolean isSubNode(TreeModel target, TreeModel current) {
        if (target.getLeft() > current.getLeft() && target.getRight() < current.getRight()) {
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * @see
     * nd.esp.service.lifecycle.support.busi.tree.preorder.TreeService#removeSubTree(nd.esp.service.lifecycle.support
     * .busi.tree.preorder.TreeModel)
     */
    @Override
    public void removeSubTree(TreeModel current) {
        int span = current.getRight() - current.getLeft() + 1;
        int offset = -span;
        synchronized (this) {
            chapterDao.moveChapters(current.getRoot(),
                                    offset,
                                    current.getRight(),
                                    ChapterConstant.GREATER_THAN,
                                    "tree_left");
            chapterDao.moveChapters(current.getRoot(),
                                    offset,
                                    current.getRight(),
                                    ChapterConstant.GREATER_THAN,
                                    "tree_right");
        }

    }

    /*
     * (non-Javadoc)
     * @see
     * nd.esp.service.lifecycle.support.busi.tree.preorder.TreeService#insertLeaf(nd.esp.service.lifecycle.support.busi
     * .tree.preorder.TreeModel, nd.esp.service.lifecycle.support.busi.tree.preorder.TreeModel,
     * nd.esp.service.lifecycle.support.busi.tree.preorder.TreeModel,
     * nd.esp.service.lifecycle.support.busi.tree.preorder.TreeDirection)
     */
    @Override
    public TreeModel insertLeaf(TreeTrargetAndParentModel model, TreeDirection treeDirection) {
        TreeModel target = model.getTarget();
        TreeModel parent = model.getParent();
        int span = 1;
        TreeModel newCurrent = getNewCurrent(target, parent, span, treeDirection);
        int offset = span + 1;// insert a leaf
        int reference = newCurrent.getLeft();
        synchronized (this) {
            chapterDao.moveChapters(newCurrent.getRoot(),
                                    offset,
                                    reference,
                                    ChapterConstant.GREATER_THAN_OR_EQUAL,
                                    "tree_left");
            chapterDao.moveChapters(newCurrent.getRoot(),
                                    offset,
                                    reference,
                                    ChapterConstant.GREATER_THAN_OR_EQUAL,
                                    "tree_right");
        }

        return newCurrent;
    }

}
