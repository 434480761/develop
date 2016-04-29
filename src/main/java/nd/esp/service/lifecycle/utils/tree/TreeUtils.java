package nd.esp.service.lifecycle.utils.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 通用树工具类
 * <p>Create Time: 2015年4月21日           </p>
 * @author xiezy
 */
public class TreeUtils {
	private static final Logger LOG = LoggerFactory.getLogger(TreeUtils.class);
    /**
     *节点字典
     */
    public static Map<String, TreeNode> nodeDictionary = new HashMap<String, TreeNode>();
    
    /**
     * 获取树--用于支持其他静态方法
     * <p>Create Time: 2015年4月21日   </p>
     * <p>Create author: xiezy   </p>
     * @param nodes         构造成树的集合
     * @param rootNodeId    根节点的id,不能随意给,应为对应业务一级节点的parentId(父节点id)
     * @return
     */
    public static TreeNode getTree(List<? extends TreeNode> nodes,String rootNodeId) {
        //传入的nodes不能为null
        Assert.assertNotNull("传入的nodes不能为NULL", nodes);
        
        // 在构造树前,将字典Map清空
        nodeDictionary.clear();
        
        /*
         * 构造根节点 
         * 规则： 
         * 1.根节点是虚拟的且唯一的
         * 2.根节点的id,不能随意给,应为对应业务一级节点的parentId(父节点id),尽量不为null
         * 3.根节点深度level为0 
         * 4.parentId为null
         */
        TreeNode rootNode = new TreeNode();
        rootNode.setNodeId(rootNodeId);
        rootNode.setNodeName("根节点--为虚拟节点");
        rootNode.setLevel(0);
        rootNode.setIsLeaf(false);
        // 将根节点加到字典Map中
        nodeDictionary.put(rootNodeId, rootNode);
        if (nodes.size() > 0) {
            // 构造树
            rootNode = constructTree(rootNode, nodes, 0);
            LOG.info("构造树成功！");
        }
        
        return rootNode;
    }

    /**
     * 	构造树的递归函数
     * <p>Create Time: 2015年4月21日   </p>
     * <p>Create author: xiezy   </p>
     * @param rootNode          根节点
     * @param chapterList       教材的章节集合
     * @param rootLevel         根节点深度
     * @return
     */
    private static TreeNode constructTree(TreeNode rootNode, List<? extends TreeNode> nodes, int rootLevel) {
        // 用于保存子节点集合
        List<TreeNode> childrenList = new ArrayList<TreeNode>();

        // 构造根节点
        for (TreeNode node : nodes) {
            if (node.getParentId().equals(rootNode.getNodeId())) {
                TreeNode childNode = new TreeNode();
                // 设置子节点信息
                childNode.setNodeId(node.getNodeId());
                childNode.setNodeName(node.getNodeName());
                childNode.setParentId(rootNode.getNodeId());
                // 设置深度
                childNode.setLevel(rootLevel + 1);
                // 添加到子节点集合
                childrenList.add(childNode);
                // 加到字典Map中
                nodeDictionary.put(childNode.getNodeId(), childNode);
            }
        }
        // 设置子节点
        rootNode.setChildren(childrenList);
        // 设置是否为叶子节点
        if (childrenList.size() == 0) {
            rootNode.setIsLeaf(true);
        }
        else {
            rootNode.setIsLeaf(false);
        }

        // 递归构造子节点
        for (TreeNode treeNode : childrenList) {
            // 进去子节点构造时深度+1
            constructTree(treeNode, nodes, ++rootLevel);
            // 递归调用返回时，构造子节点的兄弟节点，深度要和子节点深度一样，因为之前加1，所以现在要减1
            --rootLevel;
        }

        return rootNode;
    }
    
    /**
     * 获取根节点下的全部子节点(不包含根节点)
     * <p>Create Time: 2015年4月22日   </p>
     * <p>Create author: xiezy   </p>
     * @param nodes               用于构造树的集合
     * @param rootNodeId    根节点的id,不能随意给,应为对应业务一级节点的parentId(父节点id)
     * @param allChildren         用于保存全部子节点的集合,即返回集
     * @return
     */
    public static List<TreeNode> getAllChildren(List<? extends TreeNode> nodes,String rootNodeId,List<TreeNode> allChildren){
        //构造并获取树
        TreeNode rootNode = getTree(nodes,rootNodeId);
        //调用递归函数
        return recursive4GetChildren(allChildren,rootNode);
    }
    
    /**
     * 获取特定节点下的全部子节点(不包含本身)
     * <p>Create Time: 2015年4月22日   </p>
     * <p>Create author: xiezy   </p>
     * @param nodes             用于构造树的集合
     * @param rootNodeId        根节点的id,不能随意给,应为对应业务一级节点的parentId(父节点id)
     * @param id                开始节点的id
     * @param allChildren       用于保存全部子节点的集合,即返回集
     * @return                  若返回null,说明id对应节点不存在于树中
     */
    public static List<TreeNode> getAllChildrenById(List<? extends TreeNode> nodes,String rootNodeId,String id,List<TreeNode> allChildren){
        //构造并获取树
        getTree(nodes,rootNodeId);
        
        //判断id节点是否在树中
        if(nodeDictionary.containsKey(id)){
            TreeNode node = nodeDictionary.get(id);
            return recursive4GetChildren(allChildren,node);
        }else{//不存在
            return null;
        }
    }
    
    /**
     * 获取全部节点的递归函数
     * <p>Create Time: 2015年4月22日   </p>
     * <p>Create author: xiezy   </p>
     * @param allChildren       用于保存全部子节点的集合,即返回集
     * @param startNodes        开始的节点,即获取这个节点下面的全部节点
     * @return
     */
    private static List<TreeNode> recursive4GetChildren(List<TreeNode> allChildren,TreeNode startNodes){
        //判断是否到叶子节点
        if(startNodes.getChildren() != null && startNodes.getChildren().size() > 0){//不是叶节点
            for(TreeNode treeNode : startNodes.getChildren()){
                allChildren.add(treeNode);
                recursive4GetChildren(allChildren,treeNode);
            }
//            //将子节点加入返回集中
//            allChildren.addAll(startNodes.getChildren());
//            //递归
//            for (TreeNode treeNode : startNodes.getChildren()) {
//                recursive4GetChildren(allChildren,treeNode);
//            }
        }else{//是叶子节点
            return null;
        }
        return allChildren;
    }
    
    /**
     * 获取一级节点到当前节点的路径信息
     * <p>Create Time: 2015年4月22日   </p>
     * <p>Create author: xiezy   </p>
     * @param nodes             用于构造树的集合
     * @param rootNodeId        根节点的id,不能随意给,应为对应业务一级节点的parentId(父节点id)
     * @param id                要查询的节点id
     * @param separator         分隔符
     * @return
     */
    public static String getFullPathFromRoot(List<? extends TreeNode> nodes,String rootNodeId,String id,String separator){
        //保存节点名的集合
        List<String> nameList = new ArrayList<String>();
        //获取从一级节点到当前节点路径上的节点列表(包含当前节点)
        List<TreeNode> items = getListFromRoot(nodes,rootNodeId,id);
        //遍历得到节点名集合
        for(TreeNode tn : items){
            String nodeName = tn.getNodeName();
            nameList.add(nodeName);
        }
        //拼成字符串
        return StringUtils.join(nameList.toArray(), separator);
    }
    
    /**
     * 获取从一级节点到当前节点路径上的节点列表(包含当前节点),由于根节点为虚拟节点,故不返回	
     * <p>Create Time: 2015年4月21日   </p>
     * <p>Create author: xiezy   </p>
     * @param nodes         用于构造树的集合
     * @param rootNodeId    根节点的id,不能随意给,应为对应业务一级节点的parentId(父节点id)
     * @param id            要查询的节点id
     * @return              如果返回null,说明id对应节点不存在于构造的树中
     */
    public static List<TreeNode> getListFromRoot(List<? extends TreeNode> nodes,String rootNodeId,String id){
        //构造并获取树
        getTree(nodes,rootNodeId);
        
        //返回的列表
        List<TreeNode> items = new ArrayList<TreeNode>();
        
        //判断是否存在树中
        if(nodeDictionary.containsKey(id)){//找到相应的节点
            TreeNode node = nodeDictionary.get(id);
            while(node.getParentId() != null){
                items.add(node);
                node = nodeDictionary.get(node.getParentId());
            }
        }else{//id对应的节点不存在,返回null
            return null;
        }
        
        //反转
        Collections.reverse(items);
        
        return items;
    }
    
    /**
     * 根据id获取节点
     * <p>Create Time: 2015年4月22日   </p>
     * <p>Create author: xiezy   </p>
     * @param nodes         用于构造树的集合
     * @param rootNodeId    根节点的id,不能随意给,应为对应业务一级节点的parentId(父节点id)
     * @param id            要查询的节点id
     * @return              如果返回null,说明id对应节点不存在于构造的树中
     */
    public static TreeNode getNode(List<? extends TreeNode> nodes,String rootNodeId,String id){
        //构造并获取树
        getTree(nodes,rootNodeId);
        
        //通过字典获取
        if(nodeDictionary.containsKey(id)){
            TreeNode node = nodeDictionary.get(id);
            return node;
        }
        return null;
    }
    
    /**
     * 根据id批量获取节点
     * <p>Create Time: 2015年4月22日   </p>
     * <p>Create author: xiezy   </p>
     * @param nodes         用于构造树的集合
     * @param rootNodeId    根节点的id,不能随意给,应为对应业务一级节点的parentId(父节点id)
     * @param ids           要查询的节点id集合
     * @return              有的id可能不存在于树中,仅返回存在于树中的节点
     */
    public static List<TreeNode> getNodes(List<? extends TreeNode> nodes,String rootNodeId,List<String> ids){
        //返回集
        List<TreeNode> result = new ArrayList<TreeNode>();
        String notExist = "";
        
        //构造并获取树
        getTree(nodes,rootNodeId);
        
        for(String id : ids){
            //通过字典获取
            if(nodeDictionary.containsKey(id)){//存在
                TreeNode node = nodeDictionary.get(id);
                result.add(node);
            }else{//不存在,记录
                notExist += id + ",";
            }
        }
        
        //当有不存在的id时
        if(!notExist.isEmpty()){
            LOG.info("这些id:" + notExist + "对应的节点不存在于树中");
        }
        
        return result;
    }
    
    /**
     * 获取id对应节点的父节点
     * <p>Create Time: 2015年4月23日   </p>
     * <p>Create author: xiezy   </p>
     * @param nodes             用于构造树的集合
     * @param rootNodeId        根节点的id,不能随意给,应为对应业务一级节点的parentId(父节点id)
     * @param id                要查询的节点id
     * @return                  如果返回null,说明id对应节点不存在于构造的树中
     */
    public static TreeNode getParentNode(List<? extends TreeNode> nodes,String rootNodeId,String id){
        //获取id对应的节点
        TreeNode treeNode = getNode(nodes,rootNodeId,id);
        if(treeNode != null){
            return nodeDictionary.get(treeNode.getParentId());
        }else{//id对应节点不存在
            return null;
        }
    }
    
    /**
     * 获取节点的当前深度,顶级为0
     * <p>Create Time: 2015年4月21日   </p>
     * <p>Create author: xiezy   </p>
     * @param nodes         用于构造树的集合
     * @param rootNodeId    根节点的id,不能随意给,应为对应业务一级节点的parentId(父节点id)
     * @param id            要查询的节点id
     * @return              返回-1说明id对应节点不存在
     */
    public static Integer getLevel(List<? extends TreeNode> nodes,String rootNodeId,String id) {
        //构造并获取树
        getTree(nodes,rootNodeId);
        
        //通过字典获取
        if(nodeDictionary.containsKey(id)){
            TreeNode node = nodeDictionary.get(id);
            return node.getLevel();
        }
        
        return -1;
    }
    
    /**
     * 判断节点是否存在对应树中
     * <p>Create Time: 2015年4月21日   </p>
     * <p>Create author: xiezy   </p>
     * @param nodes         用于构造树的集合
     * @param rootNodeId    根节点的id,不能随意给,应为对应业务一级节点的parentId(父节点id)
     * @param id            用于判断是否存在的id
     * @return
     */
    public static Boolean contains(List<? extends TreeNode> nodes,String rootNodeId,String id) {
        //构造并获取树
        getTree(nodes,rootNodeId);
        
        if(nodeDictionary.containsKey(id)){
            return true;
        }
        return false;
    }
    
    /**
     * 判断给点的两个节点是否包含关系
     * <p>Create Time: 2015年4月23日   </p>
     * <p>Create author: xiezy   </p>
     * @param nodes             用于构造树的集合
     * @param rootNodeId        根节点的id,不能随意给,应为对应业务一级节点的parentId(父节点id)
     * @param parent            用于判断的节点
     * @param child             用于判断的节点
     * @return
     */
    public static Boolean containsRelation(List<? extends TreeNode> nodes,String rootNodeId,TreeNode parent,TreeNode child){
        //传入的parent和child不能为null
        Assert.assertNotNull("传入的parent不能为NULL", parent);
        Assert.assertNotNull("传入的child不能为NULL", child);
        
        //构造并获取树
        getTree(nodes,rootNodeId);
        
        TreeNode temp = child;
        while(temp != null){
            if(temp.getNodeId().equals(parent.getNodeId())){
                return true;
            }else{
                temp = nodeDictionary.get(temp.getParentId());
            }
        }
        
        return false;
    }
    
    /**
     * 删除给定节点,及其全部子节点
     * <p>Create Time: 2015年4月24日   </p>
     * <p>Create author: xiezy   </p>
     * @param nodes             用于构造树的集合
     * @param rootNodeId        根节点的id,不能随意给,应为对应业务一级节点的parentId(父节点id)
     * @param id                要删除的节点id
     * @return                  如果返回null,说明id对应节点不存在树中;返回的集合包含id对应节点以及全部子节点
     */
    public static List<TreeNode> removeNode(List<? extends TreeNode> nodes,String rootNodeId,String id){
        //保存被删除的节点
        List<TreeNode> removeNodes = new ArrayList<TreeNode>();
        
        //构造并获取树
        getTree(nodes,rootNodeId);
        
        if(nodeDictionary.containsKey(id)){//存在
            //1.将id对应节点放入removeNodes中
            removeNodes.add(nodeDictionary.get(id));
            //2.递归出id对应节点下的全部子节点,并放入removeNodes中
            List<TreeNode> children = new ArrayList<TreeNode>();
            getAllChildrenById(removeNodes, rootNodeId, id, children);
            if(children.size()>0){
                removeNodes.addAll(children);
            }
            //3.找到id对应节点的父节点,并从其父节点的children中删除
            TreeNode parentNode = nodeDictionary.get(nodeDictionary.get(id).getParentId());
            List<TreeNode> childrenOfParent = parentNode.getChildren();
            childrenOfParent.remove(nodeDictionary.get(id));
        }else {//id对应节点不存在
            return null;
        }
        
        return removeNodes;
    }
}
