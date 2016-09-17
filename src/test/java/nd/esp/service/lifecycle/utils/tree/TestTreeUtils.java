package nd.esp.service.lifecycle.utils.tree;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nd.esp.service.lifecycle.utils.tree.TreeNode;
import nd.esp.service.lifecycle.utils.tree.TreeUtils;

import org.junit.Assert;
import org.junit.Test;

public class TestTreeUtils {
    
    @Test
    public void testGetTree(){
        List<TreeNode> nodes = createNodes();
        
        long start = System.currentTimeMillis();
        
        TreeNode rootNode = TreeUtils.getTree(nodes, "root");
        Assert.assertNotNull("rootNode为空,构造失败", rootNode);
        Assert.assertEquals("根节点id为root", "root", rootNode.getNodeId());
        Assert.assertEquals("一级节点个数不等于100个", 100, rootNode.getChildren().size());
        
        System.out.println("构造并获取tree耗时:" + (System.currentTimeMillis() - start));
        System.out.println("===============================================================");
    }
    
    @Test
    public void testGetAllChildren(){
        List<TreeNode> nodes = createNodes();
        
        long start = System.currentTimeMillis();
        
        List<TreeNode> allChildren = new ArrayList<TreeNode>();
        TreeUtils.getAllChildren(nodes, "root", allChildren);
        
        Assert.assertEquals("全部子节点个数不等于1100", 1100, allChildren.size());
        
        System.out.println("获取全部子节点耗时:" + (System.currentTimeMillis() - start));
        System.out.println("===============================================================");
    }
    
    @Test
    public void testGetAllChildrenById(){
        List<TreeNode> nodes = createNodes();
        
        long start = System.currentTimeMillis();
        
        List<TreeNode> allChildren = new ArrayList<TreeNode>();
        TreeUtils.getAllChildrenById(nodes, "root", "0", allChildren);
        
        Assert.assertEquals("id=0的节点下的子节点个数不等于550", 550, allChildren.size());
        
        System.out.println("获取特定节点下的全部子节点(不包含本身):" + (System.currentTimeMillis() - start));
        System.out.println("===============================================================");
    }
    
    @Test
    public void testGetFullPathFromRoot(){
        List<TreeNode> nodes = createNodes();
        
        long start = System.currentTimeMillis();
        
        String fullPath = TreeUtils.getFullPathFromRoot(nodes, "root", "601", ">>>");
        Assert.assertNotEquals("fullPath长度为0", 0, fullPath.length());
        
        System.out.println("路径：" + fullPath);
        System.out.println("获取一级节点到当前节点的路径信息:" + (System.currentTimeMillis() - start));
        System.out.println("===============================================================");
    }
    
    @Test
    public void testGetListFromRoot(){
        List<TreeNode> nodes = createNodes();
        
        long start = System.currentTimeMillis();
        
        List<TreeNode> nodesFromRoot = TreeUtils.getListFromRoot(nodes, "root", "601");
        Assert.assertNotEquals("nodesFromRoot大小为0", 0, nodesFromRoot.size());
        
        System.out.println(nodesFromRoot.size());
        System.out.println("获取从一级节点到当前节点路径上的节点列表(包含当前节点):" + (System.currentTimeMillis() - start));
        System.out.println("===============================================================");
    }
    
    @Test
    public void testGetNode(){
        List<TreeNode> nodes = createNodes();
        
        long start = System.currentTimeMillis();
        
        TreeNode node = TreeUtils.getNode(nodes, "root", "1");
        Assert.assertEquals("节点id与传入的不符", "1", node.getNodeId());
        
        System.out.println(node.toString());
        System.out.println("根据id获取节点:" + (System.currentTimeMillis() - start));
        System.out.println("===============================================================");
    }
    
    @Test
    public void testGetNodes(){
        List<TreeNode> nodes = createNodes();
        
        long start = System.currentTimeMillis();
        
        List<String> ids = new ArrayList<String>();
        ids.add("1");
        ids.add("101");
        ids.add("601");
        ids.add("1001");
        ids.add("20000");
        List<TreeNode> nodeList = TreeUtils.getNodes(nodes, "root", ids);
        Assert.assertNotEquals("nodeList大小为0", 0, nodeList.size());
        
        System.out.println(nodeList.size());
        System.out.println("根据id批量获取节点:" + (System.currentTimeMillis() - start));
        System.out.println("===============================================================");
    }
    
    @Test
    public void testGetParentNode(){
        List<TreeNode> nodes = createNodes();
        
        long start = System.currentTimeMillis();
        
        TreeNode parent = TreeUtils.getParentNode(nodes, "root", "101");
        Assert.assertEquals("获取的父节点不对", "0", parent.getNodeId());
        
        System.out.println(parent.toString());
        System.out.println("获取id对应节点的父节点:" + (System.currentTimeMillis() - start));
        System.out.println("===============================================================");
    }
    
    @Test
    public void testGetLevel(){
        List<TreeNode> nodes = createNodes();
        
        long start = System.currentTimeMillis();
        
        int level = TreeUtils.getLevel(nodes, "root", "602");
        Assert.assertEquals("当前深度不为3", 3, level);
        
        System.out.println("获取节点的当前深度:" + (System.currentTimeMillis() - start));
        System.out.println("===============================================================");
    }
    
    @Test
    public void testContains(){
        List<TreeNode> nodes = createNodes();
        
        long start = System.currentTimeMillis();
        
        boolean flag1 = TreeUtils.contains(nodes, "root", "3000");
        boolean flag2 = TreeUtils.contains(nodes, "root", "3");
        
        Assert.assertFalse("id=3000的节点在树中", flag1);
        Assert.assertTrue("id=3的节点不在树中", flag2);
        
        System.out.println("判断节点是否存在对应树中:" + (System.currentTimeMillis() - start));
        System.out.println("===============================================================");
    }
    
    @Test
    public void testContainsRelation(){
        List<TreeNode> nodes = createNodes();
        
        long start = System.currentTimeMillis();
        
        TreeNode parent = TreeUtils.getNode(nodes, "root", "0");
        TreeNode child = TreeUtils.getNode(nodes, "root", "100");
        
        boolean flag1 = TreeUtils.containsRelation(nodes, "root", parent, child);
        boolean flag2 = TreeUtils.containsRelation(nodes, "root", child, parent);
        boolean flag3 = TreeUtils.containsRelation(nodes, "root", parent, parent);
        
        Assert.assertTrue("parent与child不包含关系", flag1);
        Assert.assertFalse("parent与child不包含关系", flag2);
        Assert.assertTrue("parent与parent不包含关系", flag3);
        
        System.out.println("判断给点的两个节点是否包含关系:" + (System.currentTimeMillis() - start));
        System.out.println("===============================================================");
    }
    
    @Test
    public void testRemoveNodes(){
        List<TreeNode> nodes = createNodes();
        
        long start = System.currentTimeMillis();
        
        List<TreeNode> removeNodes = TreeUtils.removeNode(nodes, "root", "80");
        Assert.assertEquals("被删除的节点不止一个", 1, removeNodes.size());
        
        System.out.println("删除给定节点,及其全部子节点:" + (System.currentTimeMillis() - start));
        System.out.println("===============================================================");
        
        TreeNode node = TreeUtils.getNode(nodes, "root", "root");
        boolean isContains = node.getChildren().contains(removeNodes);
        Assert.assertFalse("删除不成功", isContains);
    }
    
    /**
     * 创建nodes
     * <p>Create Time: 2015年4月24日   </p>
     * <p>Create author: xiezy   </p>
     * @return
     */
    public List<TreeNode> createNodes(){
        long start = System.currentTimeMillis();
        
        List<TreeNode> nodes = new ArrayList<TreeNode>();
        
        //一级节点(100个)
        List<TreeNode> firstLevel = new ArrayList<TreeNode>();
        for(int i=0;i<100;i++){
            TreeNode node = new TreeNode();
            node.setNodeId(i+"");
            node.setNodeName("一级节点" + i);
            node.setParentId("root");
            firstLevel.add(node);
        }
        
        //二级节点(10*50 = 500个)
        List<TreeNode> secondLevel = new ArrayList<TreeNode>();
        for(int i=0;i<10;i++){
            int j = 100 + i*50;
            int k = 100 + i*50;
            for(;j<k+50;j++){
                TreeNode parentNode = firstLevel.get(i);
                TreeNode child = new TreeNode();
                child.setNodeId(j+"");
                child.setNodeName("二级节点" + j);
                child.setParentId(parentNode.getNodeId());
                secondLevel.add(child);
            }
        }
        
        
        //三级节点(500个)
        List<TreeNode> thirdLevel = new ArrayList<TreeNode>();
        for(int i=0;i<10;i++){
            int j = 600 + i*50;
            int k = 600 + i*50;
            for(;j<k+50;j++){
                TreeNode parentNode = secondLevel.get(i);
                TreeNode child = new TreeNode();
                child.setNodeId(j+"");
                child.setNodeName("三级节点" + j);
                child.setParentId(parentNode.getNodeId());
                secondLevel.add(child);
            }
        }
        
        nodes.addAll(firstLevel);
        nodes.addAll(thirdLevel);
        nodes.addAll(secondLevel);
        
        System.out.println("总共有" + nodes.size() + "个节点!");
        System.out.println("创建nodes耗时:" + (System.currentTimeMillis() - start));
        
        Collections.shuffle(nodes);
        return nodes;
    }
    
    /**
     * 递归输出子节点
     */
    public void printTreeNodes(TreeNode rootNode){
        for(TreeNode childNode : rootNode.getChildren()){
            StringBuilder nodePrefix = new StringBuilder();
            for(int i=0; i<childNode.getLevel(); i++){
                nodePrefix.append("  ");
            }
            if(childNode.getIsLeaf()){
                nodePrefix.append("-" + childNode.getLevel() + "-");
            } else {
                nodePrefix.append("+" + childNode.getLevel() + "+");
            }
            System.out.println(nodePrefix + childNode.getNodeName());
            this.printTreeNodes(childNode);
        }
    }
}
