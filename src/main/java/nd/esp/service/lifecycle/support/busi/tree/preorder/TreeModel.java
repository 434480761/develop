/* =============================================================
 * Created: [2015年11月27日] by linsm
 * =============================================================
 *
 * Copyright 2014-2015 NetDragon Websoft Inc. All Rights Reserved
 *
 * =============================================================
 */

package nd.esp.service.lifecycle.support.busi.tree.preorder;

import nd.esp.service.lifecycle.repository.model.Chapter;

/**
 * @author linsm
 * @since 
 *
 */
public class TreeModel {
    
    private String identifier; //结点uuid
    private String root;  //chapter: teachingmaterial, knowledge:subject;
    private String parent; //when level = 1, parent = root;
    private int level;   //start from 1;
    private int left; //start from 1;
    private int right;//may start from 2;
    
    public TreeModel(){
        
    }
    
    public TreeModel(Chapter chapter){
        this.identifier = chapter.getIdentifier();
        this.root = chapter.getTeachingMaterial();
        this.parent = chapter.getParent();
        this.left = chapter.getLeft();
        this.right= chapter.getRight();
    }
    
    public String getRoot() {
        return root;
    }
    public void setRoot(String root) {
        this.root = root;
    }
    public String getParent() {
        return parent;
    }
    public void setParent(String parent) {
        this.parent = parent;
    }
    public int getLevel() {
        return level;
    }
    public void setLevel(int level) {
        this.level = level;
    }
    public int getLeft() {
        return left;
    }
    public void setLeft(int left) {
        this.left = left;
    }
    public int getRight() {
        return right;
    }
    public void setRight(int right) {
        this.right = right;
    }
    public String getIdentifier() {
        return identifier;
    }
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    
    

}
