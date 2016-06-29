package nd.esp.service.lifecycle.daos.titan;

import nd.esp.service.lifecycle.daos.titan.inter.TitanCommonRepository;
import nd.esp.service.lifecycle.daos.titan.inter.TitanTreeRepository;
import nd.esp.service.lifecycle.support.busi.titan.TitanTreeType;
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
    @Autowired
    private TitanCommonRepository titanCommonRepository;


    public void deleteOldRelation(TitanTreeType titanTreeType, String identifier){

        String script = "g.V().has(primaryCategory,'identifier',identifier).inE().hasLabel(relationType).drop()";

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("primaryCategory", titanTreeType.primaryCategory());
        paramMap.put("identifier", identifier);
        paramMap.put("relationType", titanTreeType.relation());

        titanCommonRepository.executeScript(script, paramMap);
    }

    @Override
    public void createNewRelation(TitanTreeType treeType, Long parentNodeId, Long nodeId, Double order) {
        String script = "g.V(parentNodeId).next().addEdge(relationType,g.V(nodeId).next(),'order',order);";
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("parentNodeId", parentNodeId);
        paramMap.put("nodeId", nodeId);
        paramMap.put("relationType", treeType.relation());
        paramMap.put("order",order);

        titanCommonRepository.executeScript(script, paramMap);
    }

    @Override
    public List<Double> getAllChildOrderByParent(TitanTreeType treeType, Long parentNodeId) {
        String script = "g.V(parentNodeId).outE().hasLabel(relationType).values('order')";
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("parentNodeId",parentNodeId);
        paramMap.put("relationType",treeType.relation());
        return titanCommonRepository.executeScriptListDouble(script, paramMap);
    }

    @Override
    public Double getTargetOrder(TitanTreeType treeType,Long parentNodeId, String identifier) {
        String script = "g.V(parentNodeId).outE().hasLabel(relationType)" +
                ".as('x').inV().has(primaryCategory,'identifier',identifier).select('x').values('order')";
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("parentNodeId", parentNodeId);
        paramMap.put("relationType", treeType.relation());
        paramMap.put("primaryCategory", treeType.primaryCategory());
        paramMap.put("identifier", identifier);
        return titanCommonRepository.executeScriptUniqueDouble(script, paramMap);
    }

    @Override
    public Long getNodeId(String primaryCategory, String identifier) {
        return titanCommonRepository.getVertexIdByLabelAndId(primaryCategory, identifier);
    }

    @Override
    public Long getSourceId(TitanTreeType treeType, String identifier) {
        String script = "g.V().has(primaryCategory,'identifier',identifier).id()";
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("primaryCategory", treeType.primaryCategory());
        paramMap.put("identifier",identifier);
        return titanCommonRepository.executeScriptUniqueLong(script, paramMap);
    }

    @Override
    public Long getSubjectId(String taxoncode) {
        String script = "g.V().has('cg_taxoncode',taxoncode).next().id()";
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("taxoncode", taxoncode);
        return titanCommonRepository.executeScriptUniqueLong(script, paramMap);
    }

    @Override
    public Long getParentByTarget(TitanTreeType treeType, String identifier) {
        String script = "g.V().has(nodeType,'identifier',identifier).inE().hasLabel(relationType).outV().id()";
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("identifier", identifier);
        paramMap.put("nodeType",treeType.primaryCategory());
        paramMap.put("relationType",treeType.relation());
        return titanCommonRepository.executeScriptUniqueLong(script, paramMap);
    }

    @Override
    public Long getKnowledgeRoot(TitanTreeType treeType, String identifier) {

        String script = "g.V().has('knowledges','identifier','identifier')" +
                ".outE().hasLabel('has_category_code').inV().has('cg_taxoncode',textRegex('\\\\$S.*')).id()";
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("identifier", identifier);
        return titanCommonRepository.executeScriptUniqueLong(script, paramMap);
    }

    @Override
    public String getKnowledgeSubjectCode(TitanTreeType treeType, String identifier) {
        //FIXME 一个知识点是否可以对应多个学科
        String script = "g.V().has('knowledges','identifier',identifier)" +
                ".outE().hasLabel('has_category_code').inV().has('cg_taxoncode',textRegex('\\\\$S.*')).values('cg_taxoncode')";
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("identifier", identifier);
        return titanCommonRepository.executeScriptUniqueString(script, paramMap);
    }

}
