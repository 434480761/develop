package nd.esp.service.lifecycle.daos.titan;

import nd.esp.service.lifecycle.daos.titan.inter.TitanChapterRelationRepository;
import nd.esp.service.lifecycle.daos.titan.inter.TitanCommonRepository;
import nd.esp.service.lifecycle.repository.model.Chapter;
import nd.esp.service.lifecycle.support.busi.titan.TitanKeyWords;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liuran on 2016/5/24.
 */

@Repository
public class TitanChapterRelationRepositoryImpl implements TitanChapterRelationRepository{
    private static final Logger LOG = LoggerFactory
            .getLogger(TitanCategoryRepositoryImpl.class);
    @Autowired
    private TitanCommonRepository titanCommonRepository;

    @Override
    public boolean createRelation(Chapter chapter) {
        String nodeId ;
        String parentPrimaryCategory;
        Integer leftValue = chapter.getLeft();
        if(chapter.getParent()!=null && chapter.getParent().equals(chapter.getTeachingMaterial())){
            nodeId = chapter.getTeachingMaterial();
            parentPrimaryCategory ="teachingmaterials";
        } else {
            nodeId = chapter.getParent();
            parentPrimaryCategory = chapter.getPrimaryCategory();
        }
        Long parentId = getNodeId(nodeId,parentPrimaryCategory);
        if (parentId == null){
            return false;
        }
        Long chapterId = getNodeId(chapter.getIdentifier(),chapter.getPrimaryCategory());
        if(chapterId == null){
            return false;
        }

        String createScript = "g.V(parentId).next().addEdge('"+TitanKeyWords.tree_has_chapter.toString()
        		+"',g.V(chapterId).next(),'"+TitanKeyWords.tree_order.toString()+"',treeOrder)";

        Map<String,Object> scriptParams = new HashMap<>();
        scriptParams.put("parentId",parentId);
        scriptParams.put("chapterId",chapterId);

        scriptParams.put("treeOrder",new Float(leftValue));

//        client.submit(createScript,scriptParams);
        try {
            titanCommonRepository.executeScript(createScript , scriptParams);
        } catch (Exception e) {
            LOG.error("titan_repository error:{};identifier:{}" ,e.getMessage(),chapter.getIdentifier());
            //TODO titan 异常处理
        }

        return true;
    }

    @Override
    public long batchCreateRelation(List<Chapter> chapters) {
        long count = 0;
        for (Chapter chapter : chapters){
            if(createRelation(chapter)){
                count ++ ;
            }
        }
        return count;
    }

    @Override
    public void deleteRelation(Chapter chapter) {

    }

    @Override
    public void updateRelationOrderValue(List<Chapter> chapters, String type) {
//        String edgelabe ;
//        if("chapters".equals(type)){
//            edgelabe = "has_chapter";
//        } else if("knowledges".equals(type)) {
//            edgelabe = "has_knowledge_new";
//        } else {
//            return ;
//        }
//
//        for(Chapter chapter : chapters){
//            String scriptLeft = "g.V().has(type,'identifier',identifier).inE().hasLabel(edgelabe).values('left');";
//            String scriptOrder = "g.V().has(type,'identifier',identifier).inE().hasLabel(edgelabe).property('order_new',order)";
//            Map<String, Object> params = new HashMap<>();
//            params.put("type",type);
//            params.put("identifier", chapter.getIdentifier());
//            params.put("edgelabe", edgelabe);
//
//            Long left = null;
//            try {
//                left = titanCommonRepository.executeScriptUniqueLong(scriptLeft, params);
//            } catch (Exception e) {
//                LOG.error("titan_repository error:{};identifier:{}" ,e.getMessage(),chapter.getIdentifier());
//                //TODO titan 异常处理
//            }
//
//            if(left == null){
//                continue;
//            }
//
//            Double order = new Double(left);
//
//
//            Map<String, Object> paramOrder = new HashMap<>();
//            paramOrder.put("type",type);
//            paramOrder.put("identifier", chapter.getIdentifier());
//            paramOrder.put("edgelabe", edgelabe);
//            paramOrder.put("order", order);
//
//            try {
//                titanCommonRepository.executeScript(scriptOrder, paramOrder);
//            } catch (Exception e) {
//                LOG.error("titan_repository error:{};identifier:{}" ,e.getMessage(),chapter.getIdentifier());
//                //TODO titan 异常处理
//            }
//        }
    }


    private Long getNodeId(String nodeId , String parentPrimaryCategory){
        if(nodeId==null){
            return null;
        }
        String queryScript = "g.V().has(primaryCategory , 'identifier' ,nodeId).id()";
        Map<String,Object> queryParam = new HashMap<>();
        queryParam.put("primaryCategory",parentPrimaryCategory);
        queryParam.put("nodeId",nodeId);


        try {
            return titanCommonRepository.executeScriptUniqueLong(queryScript, queryParam);
        } catch (Exception e) {
            LOG.error("titan_repository error:{};identifier:{}" ,e.getMessage(),nodeId);
            //TODO titan 异常处理
        }

        return null;
    }

}
