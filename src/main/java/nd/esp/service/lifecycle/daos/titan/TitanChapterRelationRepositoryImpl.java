package nd.esp.service.lifecycle.daos.titan;

import nd.esp.service.lifecycle.daos.titan.inter.TitanChapterRelationRepository;
import nd.esp.service.lifecycle.daos.titan.inter.TitanCommonRepository;
import nd.esp.service.lifecycle.repository.model.Chapter;
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

        String createScript = "g.V(parentId).next().addEdge('has_chapter',g.V(chapterId).next(),'left',leftValue)";

        Map<String,Object> scriptParams = new HashMap<>();
        scriptParams.put("parentId",parentId);
        scriptParams.put("chapterId",chapterId);

        scriptParams.put("leftValue",leftValue);

//        client.submit(createScript,scriptParams);
        titanCommonRepository.executeScript(createScript , scriptParams);

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
        String edgelabe ;
        if("chapters".equals(type)){
            edgelabe = "has_chapter";
        } else if("knowledges".equals(type)) {
            edgelabe = "has_knowledge";
        } else {
            return ;
        }

        for(Chapter chapter : chapters){
            String scriptLeft = "g.V().has(type,'identifier',identifier).inE().hasLabel(edgelabe).values('left');";
            String scriptOrder = "g.V().has(type,'identifier',identifier).inE().hasLabel(edgelabe).property('order',order)";
            Map<String, Object> params = new HashMap<>();
            params.put("type",type);
            params.put("identifier", chapter.getIdentifier());
            params.put("edgelabe", edgelabe);

            Long left = titanCommonRepository.executeScriptUniqueLong(scriptLeft, params);

            if(left == null){
                continue;
            }

            Double order = new Double(left);


            Map<String, Object> paramOrder = new HashMap<>();
            paramOrder.put("type",type);
            paramOrder.put("identifier", chapter.getIdentifier());
            paramOrder.put("edgelabe", edgelabe);
            paramOrder.put("order", order);

            titanCommonRepository.executeScript(scriptOrder, paramOrder);
        }
    }


    private Long getNodeId(String nodeId , String parentPrimaryCategory){
        if(nodeId==null){
            return null;
        }
        String queryScript = "g.V().has(primaryCategory , 'identifier' ,nodeId).id()";
        Map<String,Object> queryParam = new HashMap<>();
        queryParam.put("primaryCategory",parentPrimaryCategory);
        queryParam.put("nodeId",nodeId);


        return titanCommonRepository.executeScriptUniqueLong(queryScript, queryParam);
    }

}
