package nd.esp.service.lifecycle.daos.titan.inter;

import nd.esp.service.lifecycle.support.busi.titan.TitanTreeType;

import java.util.List;

/**
 * Created by liuran on 2016/6/7.
 */
public interface TitanTreeRepository {
    public void deleteOldRelation(TitanTreeType titanTreeType, String identifier);
    public void createNewRelation(TitanTreeType treeType, Long parentNodeId,Long nodeId, Double order);
    public List<Double> getAllChildOrderByParent(TitanTreeType treeType, Long parentNodeId);
    public Double getTargetOrder(TitanTreeType treeType,Long parentNodeId, String identifier);
    public Long getNodeId(String primaryCategory, String identifier);
    public Long getSourceId(TitanTreeType treeType, String identifier);
    public Long getSubjectId(String taxoncode);
    public Long getParentByTarget(TitanTreeType treeType, String identifier);
    public Long getKnowledgeRoot(TitanTreeType treeType, String identifier);
    public String getKnowledgeSubjectCode(TitanTreeType treeType, String identifier);
}
