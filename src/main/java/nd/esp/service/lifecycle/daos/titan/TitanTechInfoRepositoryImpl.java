package nd.esp.service.lifecycle.daos.titan;

import nd.esp.service.lifecycle.daos.titan.inter.TitanCommonRepository;
import nd.esp.service.lifecycle.daos.titan.inter.TitanRepositoryUtils;
import nd.esp.service.lifecycle.daos.titan.inter.TitanTechInfoRepository;
import nd.esp.service.lifecycle.repository.model.TechInfo;
import nd.esp.service.lifecycle.support.busi.titan.TitanSyncType;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.utils.TitanScritpUtils;
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
public class TitanTechInfoRepositoryImpl implements TitanTechInfoRepository {
    private static final Logger LOG = LoggerFactory
            .getLogger(TitanTechInfoRepositoryImpl.class);
    @Autowired
    private TitanCommonRepository titanCommonRepository;

    @Autowired
    private TitanRepositoryUtils titanRepositoryUtils;

    @Override
    public TechInfo add(TechInfo techInfo) {
        if(techInfo == null){
            return null;
        }

        TechInfo techInfoNew = addOrUpdateTechInfo(techInfo);
        if(techInfoNew == null){
//            LOG.info("techInfo处理出错");
            titanRepositoryUtils.titanSync4MysqlAdd(TitanSyncType.SAVE_OR_UPDATE_ERROR,
                    techInfo.getResType(),techInfo.getResource());
        }
        return techInfoNew;
    }

    @Override
    public List<TechInfo> batchAdd(List<TechInfo> techInfos) {
        if(CollectionUtils.isEmpty(techInfos)){
            return new ArrayList<>();
        }

        List<TechInfo> techInfoList = new ArrayList<>();
        for(TechInfo techInfo : techInfos){
            if(addOrUpdateTechInfo(techInfo)!=null){
                techInfoList.add(techInfo);
            } else {
//                LOG.info("techInfo处理出错");
                titanRepositoryUtils.titanSync4MysqlAdd(TitanSyncType.SAVE_OR_UPDATE_ERROR,
                        techInfo.getResType(),techInfo.getResource());
            }
        }

        return techInfoList;
    }

    @Override
    public TechInfo update(TechInfo techInfo) {

        add(techInfo);

        return null;
    }

    @Override
    public List<TechInfo> batchUpdate(List<TechInfo> techInfos) {
        if(CollectionUtils.isEmpty(techInfos)){
            return new ArrayList<>();
        }
        //FIXME batchUpdate
        batchAdd(techInfos);
        return null;
    }

    /**
     * tech_info暂时不需要删除
     * */
    public void remove(TechInfo techInfo) {

    }

    private boolean deleteAll(String primaryCategory, String resource) {
        String deleteScriptBuffer = "g.V().has(primaryCategory,'identifier',resource).outE().hasLabel('has_tech_info').inV().drop()";
        Map<String, Object> deleteParam = new HashMap<>();
        deleteParam.put("primaryCategory", primaryCategory);
        deleteParam.put("resource", resource);
        try {
            titanCommonRepository.executeScript(deleteScriptBuffer, deleteParam);
        } catch (Exception e) {
            LOG.error("titan_repository error:{};identifier:{}" ,e.getMessage(),resource);
            //TODO titan sync
            return false;
        }

        return true;
    }

    private TechInfo addOrUpdateTechInfo(TechInfo techInfo){
        String checkTechInfoExist = "g.E().hasLabel('has_tech_info').has('identifier',edgeIdentifier).id()";
        Map<String, Object> checkTechInfoParam = new HashMap<>();
        checkTechInfoParam.put("edgeIdentifier",techInfo.getIdentifier());
        String oldTechInfoId = null;
        try {
            oldTechInfoId = titanCommonRepository.executeScriptUniqueString(checkTechInfoExist, checkTechInfoParam);
        } catch (Exception e) {
            LOG.error("titan_repository error:{};identifier:{}" ,e.getMessage(),techInfo.getResource());
            //TODO titan sync
            return null;
        }

        StringBuffer scriptBuffer = null;
        Map<String, Object> graphParams = null;

        boolean isAdd = false;
        String techInfoEdgeId = null;
        if(StringUtils.isEmpty(oldTechInfoId)){
            isAdd = true;
            scriptBuffer = new StringBuffer("techinfo = graph.addVertex(T.label, type");
            graphParams = TitanScritpUtils.getParamAndChangeScript(scriptBuffer,techInfo);
            scriptBuffer.append(");g.V().has(primaryCategory,'identifier',sourceIdentifier).next().addEdge('has_tech_info',techinfo ,'identifier',edgeIdentifier");

            graphParams.putAll(TitanScritpUtils.getParamAndChangeScript(scriptBuffer, techInfo));

            scriptBuffer.append(").id();");
            graphParams.put("type", "tech_info");
            graphParams.put("primaryCategory",techInfo.getResType());
            graphParams.put("sourceIdentifier",techInfo.getResource());
            graphParams.put("edgeIdentifier",techInfo.getIdentifier());
            try {
                techInfoEdgeId = titanCommonRepository.executeScriptUniqueString(scriptBuffer.toString(), graphParams);
            } catch (Exception e) {
                LOG.error("titan_repository error:{};identifier:{}" ,e.getMessage(),techInfo.getResource());
                //TODO titan sync
                return null;
            }
        } else {
            scriptBuffer = new StringBuffer("g.V().has('identifier',identifier)");
            graphParams = TitanScritpUtils.getParamAndChangeScript4Update(scriptBuffer, techInfo);
            scriptBuffer.append(";");
            graphParams.put("identifier",techInfo.getIdentifier());

            StringBuffer updateEdge = new StringBuffer("g.E().has('identifier',identifier)");
            Map<String, Object>  updateEdgeParam = TitanScritpUtils.getParamAndChangeScript4Update(updateEdge,techInfo);

            try {
                titanCommonRepository.executeScript(scriptBuffer.toString(), graphParams);
                titanCommonRepository.executeScript(updateEdge.toString(), updateEdgeParam);
            } catch (Exception e) {
                LOG.error("titan_repository error:{};identifier:{}" ,e.getMessage(),techInfo.getResource());
                //TODO titan sync
                return null;
            }
        }

        if(isAdd && StringUtils.isEmpty(techInfoEdgeId)){
            return null;
        }

        return techInfo;
    }

    @Override
    public boolean deleteAllByResource(String primaryCategory, String identifier) {


        try {
            titanCommonRepository.deleteAllOutVertexByResourceAndVertexLabel(primaryCategory,identifier,"tech_info");
        } catch (Exception e) {
            LOG.error("titan_repository error:{}" ,e.getMessage());
            return false;
        }

        return true;
    }
}
