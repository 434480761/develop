package nd.esp.service.lifecycle.daos.titan;

import nd.esp.service.lifecycle.daos.titan.inter.TitanCommonRepository;
import nd.esp.service.lifecycle.daos.titan.inter.TitanKnowledgeRelationRepository;
import nd.esp.service.lifecycle.repository.model.Chapter;
import nd.esp.service.lifecycle.repository.model.KnowledgeRelation;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.support.busi.titan.TitanKeyWords;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.utils.TitanScritpUtils;

import org.apache.tinkerpop.shaded.minlog.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liuran on 2016/5/30.
 */
@Repository
public class TitanKnowledgeRelationRepositoryImpl implements TitanKnowledgeRelationRepository {
    @Autowired
    private TitanCommonRepository titanCommonRepository;

    private final static Logger LOG = LoggerFactory
            .getLogger(TitanCoverageRepositoryImpl.class);

    @Override
    public boolean createRelation4Tree(Chapter knowledge) {
        if (knowledge == null) {
            return false;
        }

        String queryParent;
        Map<String, Object> queryParentParam;

        Long parentId = null;
        Long childId = null;
        Integer leftValue = knowledge.getLeft();
        if (isSubjectCode(knowledge.getParent())) {
            queryParent = "g.V().has('cg_taxoncode',taxoncode).next().id()";
            queryParentParam = new HashMap<>();
            queryParentParam.put("taxoncode", knowledge.getParent());
            try {
                parentId = titanCommonRepository.executeScriptUniqueLong(queryParent,queryParentParam);
            } catch (Exception e) {
                LOG.error("titan_repository error:{};identifier:{}" ,e.getMessage(),knowledge.getIdentifier());
                //TODO titan 异常处理
            }
        } else if (isUuid(knowledge.getParent())) {
            queryParent = "g.V().has('knowledges','identifier',identifier).next().id()";
            queryParentParam = new HashMap<>();
            queryParentParam.put("identifier", knowledge.getParent());
            try {
                parentId = titanCommonRepository.executeScriptUniqueLong(queryParent,queryParentParam);
            } catch (Exception e) {
                LOG.error("titan_repository error:{};identifier:{}" ,e.getMessage(),knowledge.getIdentifier());
                //TODO titan 异常处理
            }
        }
        if (parentId == null) {
            return false;
        }

        String queryChild = "g.V().has('knowledges','identifier',identifier).next().id()";
        Map<String, Object> queryChildParam = new HashMap<>();
        queryChildParam.put("identifier", knowledge.getIdentifier());
        try {
            childId = titanCommonRepository.executeScriptUniqueLong(queryChild, queryChildParam);
        } catch (Exception e) {
            LOG.error("titan_repository error:{};identifier:{}" ,e.getMessage(),knowledge.getIdentifier());
            //TODO titan 异常处理
        }

        if (childId == null) {
            return false;
        }

        String createScript = "g.V(parentId).next().addEdge('"+TitanKeyWords.tree_has_knowledge.toString()
        		+"',g.V(childId).next(),'"+TitanKeyWords.tree_order.toString()+"',treeOrder)";

        Map<String, Object> createScriptParams = new HashMap<>();
        createScriptParams.put("parentId", parentId);
        createScriptParams.put("childId", childId);
        createScriptParams.put("treeOrder", new Float(leftValue));
        try {
            titanCommonRepository.executeScript(createScript, createScriptParams);
        } catch (Exception e) {
            LOG.error("titan_repository error:{};identifier:{}" ,e.getMessage(),knowledge.getIdentifier());
            //TODO titan 异常处理
        }

        return true;
    }

    @Override
    public long batchCreateRelation4Tree(List<Chapter> knowledges) {
        Long size = 0L;
        for (Chapter knowledge : knowledges) {
            if (createRelation4Tree(knowledge)) {
                size++;
            }
        }
        return size;
    }

    @Override
    public void deleteRelation4Tree(Chapter knowledges) {

    }

    @Override
    public KnowledgeRelation add(KnowledgeRelation knowledgeRelation) {
        if(knowledgeRelation == null){
            return null;
        }

        StringBuffer script = new StringBuffer("g.V().has(sourcePrimaryCategory , 'identifier',sourceIdentifier).next()" +
                ".addEdge('has_knowledge_relation',g.V().has(targetPrimaryCategory ,'identifier',targetIdentifier).next()");
        Map<String ,Object> params = TitanScritpUtils.getParamAndChangeScript(script,knowledgeRelation);
        params.put("sourcePrimaryCategory","knowledges");
        params.put("sourceIdentifier",knowledgeRelation.getSource());
        params.put("targetPrimaryCategory","knowledges");
        params.put("targetIdentifier",knowledgeRelation.getTarget());
        script.append(").id()");

        String id = null;
        try {
            id = titanCommonRepository.executeScriptUniqueString(script.toString(),params);
        } catch (Exception e) {
            LOG.error("titan_repository error:{};identifier:{}" ,e.getMessage(),knowledgeRelation.getSource());
            return null;
            //TODO titan 异常处理
        }

        return knowledgeRelation;
    }

    @Override
    public List<KnowledgeRelation> batchAdd(List<KnowledgeRelation> knowledgeRelations) {
        List<KnowledgeRelation> list = new ArrayList<>();
        for (KnowledgeRelation knowledgeRelation : knowledgeRelations){
            if(add(knowledgeRelation)!=null){
                list.add(knowledgeRelation);
            }
        }

        return list;
    }

    @Override
    public KnowledgeRelation update(KnowledgeRelation knowledgeRelation) {
        return null;
    }

    @Override
    public List<KnowledgeRelation> batchUpdate(List<KnowledgeRelation> knowledgeRelations) {
        return null;
    }

    private boolean isSubjectCode(String parent) {
        if (StringUtils.isEmpty(parent)) {
            return false;
        }

        return parent.contains("$SB");
    }

    private boolean isUuid(String parent) {
        if (StringUtils.isEmpty(parent)) {
            return false;
        }
        return CommonHelper.checkUuidPattern(parent);
    }
}
