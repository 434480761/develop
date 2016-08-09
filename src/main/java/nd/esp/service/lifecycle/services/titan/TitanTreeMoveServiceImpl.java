package nd.esp.service.lifecycle.services.titan;

import nd.esp.service.lifecycle.daos.titan.inter.TitanTreeRepository;
import nd.esp.service.lifecycle.support.busi.titan.TitanTreeModel;
import nd.esp.service.lifecycle.support.busi.titan.TitanTreeType;
import nd.esp.service.lifecycle.support.busi.tree.preorder.TreeDirection;
import nd.esp.service.lifecycle.utils.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class TitanTreeMoveServiceImpl implements TitanTreeMoveService {
    private static final Logger LOG = LoggerFactory
            .getLogger(TitanTreeMoveServiceImpl.class);
    @Autowired
    private TitanTreeRepository titanTreeRepository;

    @Override
    public void addNode(TitanTreeModel titanTreeModel) {
        try{
        	if(StringUtils.isEmpty(titanTreeModel.getTarget())&&titanTreeModel.getTreeType()==TitanTreeType.knowledges){
        		Long titanRootId= getKnowledgeRootId(titanTreeModel.getRoot());
            	titanTreeModel.setTitanRootId(titanRootId);
        	}
            createNewRelation(titanTreeModel);
        }catch (Exception e){
            e.printStackTrace();
            LOG.info("move node error");
        }
    }

    private Long getKnowledgeRootId(String root) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
    public void moveNode(TitanTreeModel titanTreeModel) {
        try{
        	if(StringUtils.isEmpty(titanTreeModel.getTarget())&&titanTreeModel.getTreeType()==TitanTreeType.knowledges){
        		Long titanRootId = getTitanRootId(titanTreeModel.getSource());
        		titanTreeModel.setTitanRootId(titanRootId);
        	}
            deleteOldRelation(titanTreeModel);
            createNewRelation(titanTreeModel);
        }catch (Exception e){
            e.printStackTrace();
            LOG.info("move node error");
        }

    }

    private Long getTitanRootId(String source) {
		String script = "s = g.V().has('identifier','000901ca-7c6c-4c80-8b1c-4408f8b11d15').id();Long last =0;while(s.iterator().hasNext()){last = s.iterator().next();s = g.V(last).in('has_chapter').id();};last;";
		
		return null;
	}

	private void deleteOldRelation(TitanTreeModel titanTreeModel){
        titanTreeRepository.deleteOldRelation(titanTreeModel.getTreeType(), titanTreeModel.getSource());
    }

    private void createNewRelation(TitanTreeModel titanTreeModel){
        String target = titanTreeModel.getTarget();
        String parent = titanTreeModel.getParent();
        Long parentNodeId = null;
        Long nodeId = null;
        Double order = 100D;
//        //TODO 在移动知识点的时候无法获取学科，暂时从titan中根据知识点获取学科维度数据
//        if(TitanTreeType.knowledges ==  titanTreeModel.getTreeType()
//                && "ROOT".equals(titanTreeModel.getParent())
//                && (titanTreeModel.getRoot()==null
//                || titanTreeModel.getRoot().equals(""))){
//            titanTreeModel.setRoot(titanTreeRepository.getKnowledgeSubjectCode(titanTreeModel.getTreeType(),titanTreeModel.getSource()));
//        }
        //TODO 指定默认方向
        if(titanTreeModel.getTreeDirection() == null){
            titanTreeModel.setTreeDirection(TreeDirection.next);
        }

        //获取sourceID
        nodeId = titanTreeRepository.getSourceId(titanTreeModel.getTreeType(), titanTreeModel.getSource());
        if (nodeId == null) {
            LOG.info("{source:{} titan中找不到对应的资源",titanTreeModel.getSource());
            return;
        }

        //获取targetID
        if (target != null && !"".equals(target)) {
            parentNodeId = titanTreeRepository.getParentByTarget(titanTreeModel.getTreeType(), target);
            if (parentNodeId == null) {
                LOG.info("{target:{} titan中找不到target对应的父节点",target);
                return;
            }
            Double targetOrder = titanTreeRepository.getTargetOrder(titanTreeModel.getTreeType(),parentNodeId, titanTreeModel.getTarget());
            if(targetOrder == null){
                LOG.info("{target:{} titan中找不到target order",target);
                return;
            }
            List<Double> childOrders = titanTreeRepository.getAllChildOrderByParent(titanTreeModel.getTreeType(), parentNodeId);
            if(childOrders != null && childOrders.size() != 0){
                Collections.sort(childOrders);
                int indexOfTargetOrder = childOrders.indexOf(targetOrder);
                switch (titanTreeModel.getTreeDirection()){
                    case pre:
                        if(indexOfTargetOrder==0){
                            order = targetOrder / 2;
                        } else {
                            order = (childOrders.get(indexOfTargetOrder) + childOrders.get(indexOfTargetOrder - 1)) / 2;
                        }
                        break;
                    case next:
                        if(indexOfTargetOrder == childOrders.size()-1){
                            order = targetOrder + 10;
                        } else {
                            order = (childOrders.get(indexOfTargetOrder) + childOrders.get(indexOfTargetOrder + 1)) / 2;
                        }
                        break;
                }
            }
        } else if (parent != null) {
            switch (titanTreeModel.getTreeType()) {
                case knowledges:
                    //父节点是学科
                    if ("ROOT".equals(parent)) {
//                        parentNodeId = titanTreeRepository.getSubjectId(titanTreeModel.getRoot());
                    	parentNodeId = titanTreeModel.getTitanRootId();
                    }
                    //父节点是知识点
                    else {
                        parentNodeId = titanTreeRepository.getNodeId("knowledges", titanTreeModel.getParent());
                    }
                    break;
                case chapters:
                    //父节点是教材
                    if (titanTreeModel.getParent().equals(titanTreeModel.getRoot())) {
                        parentNodeId = titanTreeRepository.getNodeId("teachingmaterials", titanTreeModel.getParent());
                    }
                    //父节点是章节
                    else {
                        parentNodeId = titanTreeRepository.getNodeId("chapters", titanTreeModel.getParent());
                    }
                    break;
            }
            if (parentNodeId == null) {
                LOG.info("{target:{} titan中找不到parent对应的节点",target);
                return;
            }
            List<Double> childOrders = titanTreeRepository.getAllChildOrderByParent(titanTreeModel.getTreeType(), parentNodeId);
            if (childOrders != null && childOrders.size() != 0) {
                Collections.sort(childOrders);
                //TODO 获取最大的编号并加一个值使order变成最大
                order = childOrders.get(childOrders.size() - 1) + 10;
            }
        }

        titanTreeRepository.createNewRelation(titanTreeModel.getTreeType(),parentNodeId,nodeId,order);

    }

}
