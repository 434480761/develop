package nd.esp.service.lifecycle.daos.titan;

import nd.esp.service.lifecycle.daos.titan.inter.TitanCommonRepository;
import nd.esp.service.lifecycle.daos.titan.inter.TitanTreeRepository;
import nd.esp.service.lifecycle.support.busi.titan.TitanKeyWords;
import nd.esp.service.lifecycle.support.busi.titan.TitanTreeType;
import nd.esp.service.lifecycle.support.busi.tree.preorder.TreeDirection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liuran on 2016/6/7.
 */
@Repository
public class TitanTreeRepositoryImpl implements TitanTreeRepository{
    private final static Logger LOG = LoggerFactory
            .getLogger(TitanCoverageRepositoryImpl.class);
    @Autowired
    private TitanCommonRepository titanCommonRepository;


    public void deleteOldRelation(TitanTreeType titanTreeType, String identifier){

        String script = "g.V().has(primaryCategory,'identifier',identifier).inE().hasLabel(relationType).drop()";

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("primaryCategory", titanTreeType.primaryCategory());
        paramMap.put("identifier", identifier);
        paramMap.put("relationType", titanTreeType.relation());

        try {
            titanCommonRepository.executeScript(script, paramMap);
        } catch (Exception e) {
            LOG.error("titan_repository error:{} identifier:{}" ,e.getMessage(),identifier);
            //TODO titan 异常处理
        }
    }

    @Override
    public void createNewRelation(TitanTreeType treeType, Long parentNodeId, Long nodeId, Double order) {
        String script = "g.V(parentNodeId).next().addEdge(relationType,g.V(nodeId).next(),'"+TitanKeyWords.tree_order.toString()+"',order);";
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("parentNodeId", parentNodeId);
        paramMap.put("nodeId", nodeId);
        paramMap.put("relationType", treeType.relation());
        paramMap.put("order",order);

        try {
            titanCommonRepository.executeScript(script, paramMap);
        } catch (Exception e) {
            LOG.error("titan_repository error:{}" ,e.getMessage());
            //TODO titan 异常处理
        }
    }

    @Override
    public Double getChildOrderByParentAndTargetOrder(TitanTreeType treeType, Long parentNodeId, TreeDirection direction , Double targetOrder) {

        TitanKeyWords operation ;
        TitanKeyWords orderBy ;
        if(direction.equals(TreeDirection.next)){
            operation = TitanKeyWords.gt;
            orderBy = TitanKeyWords.incr;
        } else {
            operation = TitanKeyWords.lt;
            orderBy = TitanKeyWords.decr;
        }

        String script = "g.V(parentNodeId).outE(relationType).has('"+TitanKeyWords.tree_order.toString()+"',"
                +operation.toString()+"(new Double(targetOrder))).values('"+TitanKeyWords.tree_order.toString()
                +"').order().by("+orderBy.toString()+").limit(1);";

//        String script = "g.V(parentNodeId).outE().hasLabel(relationType).values('order')";
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("parentNodeId",parentNodeId);
        paramMap.put("relationType",treeType.relation());
        paramMap.put("targetOrder",targetOrder);

        Double order = null;
        try {
            order = titanCommonRepository.executeScriptUniqueDouble(script, paramMap);
        } catch (Exception e) {
            LOG.error("titan_repository error:{}" ,e.getMessage());
            //TODO titan 异常处理
        }

        return order;
    }

    @Override
    public Double getChildMaxOrderByParent(TitanTreeType treeType, Long parentNodeId) {
        Double maxValue =  100000D;//by lsm 目前生产环境最多有4万多知识点，最大值是88761.0 所以暂时设置成这个值
        String script = "g.V(parentNodeId).outE(relationType).has('"+TitanKeyWords.tree_order.toString()
                +"',gt(new Double(maxValue))).values('"+TitanKeyWords.tree_order.toString()+"').order().by("
                +TitanKeyWords.decr.toString()+").limit(1)";
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("parentNodeId",parentNodeId);
        paramMap.put("relationType",treeType.relation());
        paramMap.put("maxValue", maxValue);

        Double order = null;
        try {
            order = titanCommonRepository.executeScriptUniqueDouble(script, paramMap);
        } catch (Exception e) {
            LOG.error("titan_repository error:{}" ,e.getMessage());
            //TODO titan 异常处理
        }

        if(order == null || order < 0){
            return maxValue;
        }

        return order;
    }

    @Override
    public Double getTargetOrder(TitanTreeType treeType,Long parentNodeId, String identifier) {
//        String script = "g.V(parentNodeId).outE().hasLabel(relationType)" +
//                ".as('x').inV().has(primaryCategory,'identifier',identifier).select('x').values('order')";
        String script = "g.V().has(primaryCategory,'identifier',identifier).inE(relationType).values('"
                +TitanKeyWords.tree_order.toString()+"')";
        Map<String, Object> paramMap = new HashMap<>();
//        paramMap.put("parentNodeId", parentNodeId);
        paramMap.put("relationType", treeType.relation());
        paramMap.put("primaryCategory", treeType.primaryCategory());
        paramMap.put("identifier", identifier);

        Double order = null;
        try {
            order = titanCommonRepository.executeScriptUniqueDouble(script, paramMap);
        } catch (Exception e) {
            LOG.error("titan_repository error:{}" ,e.getMessage());
            //TODO titan 异常处理
        }
        return order;
    }

    @Override
    public Long getNodeId(String primaryCategory, String identifier) {
        try {
            return titanCommonRepository.getVertexIdByLabelAndId(primaryCategory, identifier);
        } catch (Exception e) {
            LOG.error("titan_repository error:{}" ,e.getMessage());
            //TODO titan 异常处理
        }

        return null;
    }

    @Override
    public Long getSourceId(TitanTreeType treeType, String identifier) {
        String script = "g.V().has(primaryCategory,'identifier',identifier).id()";
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("primaryCategory", treeType.primaryCategory());
        paramMap.put("identifier",identifier);
        try {
            return titanCommonRepository.executeScriptUniqueLong(script, paramMap);
        } catch (Exception e) {
            LOG.error("titan_repository error:{}" ,e.getMessage());
            //TODO titan 异常处理
        }
        return null;
    }

    @Override
    public Long getSubjectId(String taxoncode) {
        String script = "g.V().has('cg_taxoncode',taxoncode).next().id()";
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("taxoncode", taxoncode);
        try {
            return titanCommonRepository.executeScriptUniqueLong(script, paramMap);
        } catch (Exception e) {
            LOG.error("titan_repository error:{}" ,e.getMessage());
            //TODO titan 异常处理
        }
        return null;
    }

    @Override
    public Long getParentByTarget(TitanTreeType treeType, String identifier) {
        String script = "g.V().has(nodeType,'identifier',identifier).inE().hasLabel(relationType).outV().id()";
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("identifier", identifier);
        paramMap.put("nodeType",treeType.primaryCategory());
        paramMap.put("relationType",treeType.relation());
        try {
            return titanCommonRepository.executeScriptUniqueLong(script, paramMap);
        } catch (Exception e) {
            LOG.error("titan_repository error:{}" ,e.getMessage());
            //TODO titan 异常处理
        }
        return null;
    }

    @Override
    public Long getKnowledgeRoot(TitanTreeType treeType, String identifier) {

        String script = "g.V().has('knowledges','identifier','identifier')" +
                ".outE().hasLabel('has_category_code').inV().has('cg_taxoncode',textRegex('\\\\$S.*')).id()";
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("identifier", identifier);
        try {
            return titanCommonRepository.executeScriptUniqueLong(script, paramMap);
        } catch (Exception e) {
            LOG.error("titan_repository error:{}" ,e.getMessage());
            //TODO titan 异常处理
        }

        return null;
    }

    @Override
    public String getKnowledgeSubjectCode(TitanTreeType treeType, String identifier) {
        //FIXME 一个知识点是否可以对应多个学科
        String script = "g.V().has('knowledges','identifier',identifier)" +
                ".outE().hasLabel('has_category_code').inV().has('cg_taxoncode',textRegex('\\\\$S.*')).values('cg_taxoncode')";
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("identifier", identifier);
        try {
            return titanCommonRepository.executeScriptUniqueString(script, paramMap);
        } catch (Exception e) {
            LOG.error("titan_repository error:{}" ,e.getMessage());
            //TODO titan 异常处理
        }
        return null;
    }

    @Override
    public Long getKnowledgeRootId(String root) {
        String script = "s = g.V().has('identifier',root).id();Long last =0;" +
                "while(s.iterator().hasNext()){last = s.iterator().next();s = g.V(last).in(has_knowledge).id();};last;";
        Map<String, Object> param = new HashMap<>();
        param.put("root", root);
        param.put("has_knowledge",TitanTreeType.knowledges.relation());
        try {
            return titanCommonRepository.executeScriptUniqueLong(script, param);
        } catch (Exception e) {
            LOG.error("titan_repository error:{}" ,e.getMessage());
        }
        return null;
    }

}
