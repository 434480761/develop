package nd.esp.service.lifecycle.daos.titan;

import nd.esp.service.lifecycle.daos.titan.inter.TitanCommonRepository;
import nd.esp.service.lifecycle.daos.titan.inter.TitanRepositoryUtils;
import nd.esp.service.lifecycle.daos.titan.inter.TitanStatisticalRepository;
import nd.esp.service.lifecycle.repository.model.ResourceStatistical;
import nd.esp.service.lifecycle.repository.model.TechInfo;
import nd.esp.service.lifecycle.support.busi.titan.TitanKeyWords;
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
 * Created by liuran on 2016/8/17.
 */
@Repository
public class TitanStatisticalRepositoryImpl implements TitanStatisticalRepository{
    private static final Logger LOG = LoggerFactory
            .getLogger(TitanStatisticalRepositoryImpl.class);

    @Autowired
    private TitanCommonRepository titanCommonRepository;

    @Autowired
    private TitanRepositoryUtils titanRepositoryUtils;

    @Override
    public ResourceStatistical add(ResourceStatistical entity) {

        if(entity == null){
            return null;
        }

        ResourceStatistical result = addOrUpdateStatistical(entity);
        if(result == null){
            titanRepositoryUtils.titanSync4MysqlAdd(TitanSyncType.SAVE_OR_UPDATE_ERROR,
                    entity.getResType(),entity.getResource());
        }

        return entity;
    }

    @Override
    public List<ResourceStatistical> batchAdd(List<ResourceStatistical> entityList) {
        if(CollectionUtils.isEmpty(entityList)){
            return new ArrayList<>();
        }

        List<ResourceStatistical> statisticalList = new ArrayList<>();
        for(ResourceStatistical statistical : entityList){
            if(addOrUpdateStatistical(statistical)!=null){
                statisticalList.add(statistical);
            } else {
//                LOG.info("techInfo处理出错");
                titanRepositoryUtils.titanSync4MysqlAdd(TitanSyncType.SAVE_OR_UPDATE_ERROR,
                        statistical.getResType(),statistical.getResource());
            }
        }

        return statisticalList;
    }

    @Override
    public ResourceStatistical update(ResourceStatistical entity) {
        return add(entity);
    }

    @Override
    public List<ResourceStatistical> batchUpdate(List<ResourceStatistical> entityList) {
        if (CollectionUtils.isEmpty(entityList)){
            return null;
        }

        return batchAdd(entityList);
    }

    @Override
    public boolean delete(String id) {
        try {
            titanCommonRepository.deleteVertexById(id);
        } catch (Exception e) {
            titanRepositoryUtils.titanSync4MysqlAdd(TitanSyncType.DELETE_TECH_INFO_ERROR,
                    TitanKeyWords.statistical.toString(),id);
            return false;
        }
        return true;
    }

    @Override
    public boolean batchDelete(List<String> ids) {
        if(CollectionUtils.isEmpty(ids)){
            return true;
        }
        for (String id : ids){
            delete(id);
        }

        return true;
    }

    @Override
    public boolean deleteAllByResource(String primaryCategory, String identifier) {
        try {
            titanCommonRepository.deleteAllOutVertexByResourceAndVertexLabel(primaryCategory,identifier,TitanKeyWords.statistical.toString());
        } catch (Exception e) {
            LOG.error("titan_repository error:{}" ,e.getMessage());
            return false;
        }

        return true;
    }

    private ResourceStatistical addOrUpdateStatistical(ResourceStatistical statistical){
        String checkTechInfoExist = "g.E().hasLabel('"+ TitanKeyWords.has_statistical.toString()+"').has('identifier',edgeIdentifier).id()";
        Map<String, Object> checkTechInfoParam = new HashMap<>();
        checkTechInfoParam.put("edgeIdentifier",statistical.getIdentifier());
        String oldTechInfoId = null;
        try {
            oldTechInfoId = titanCommonRepository.executeScriptUniqueString(checkTechInfoExist, checkTechInfoParam);
        } catch (Exception e) {
            LOG.error("titan_repository error:{};identifier:{}" ,e.getMessage(),statistical.getResource());
            //TODO titan sync
            return null;
        }

        StringBuffer scriptBuffer = null;
        Map<String, Object> graphParams = null;

        boolean isAdd = false;
        String techInfoEdgeId = null;
        if(StringUtils.isEmpty(oldTechInfoId)){
            isAdd = true;
            scriptBuffer = new StringBuffer("statistical = graph.addVertex(T.label, type");
            graphParams = TitanScritpUtils.getParamAndChangeScript(scriptBuffer,statistical);
            scriptBuffer.append(");g.V().has(primaryCategory,'identifier',sourceIdentifier).next().addEdge('"+TitanKeyWords.has_statistical.toString()+"',statistical");

            graphParams.putAll(TitanScritpUtils.getParamAndChangeScript(scriptBuffer, statistical));

            scriptBuffer.append(").id();");
            graphParams.put("type", TitanKeyWords.statistical.toString());
            graphParams.put("primaryCategory",statistical.getResType());
            graphParams.put("sourceIdentifier",statistical.getResource());
            graphParams.put("edgeIdentifier",statistical.getIdentifier());
            try {
                techInfoEdgeId = titanCommonRepository.executeScriptUniqueString(scriptBuffer.toString(), graphParams);
            } catch (Exception e) {
                LOG.error("titan_repository error:{};identifier:{}" ,e.getMessage(),statistical.getResource());
                //TODO titan sync
                return null;
            }
        } else {
            scriptBuffer = new StringBuffer("g.V().has('identifier',identifier)");
            graphParams = TitanScritpUtils.getParamAndChangeScript4Update(scriptBuffer, statistical);
            scriptBuffer.append(";");
            graphParams.put("identifier",statistical.getIdentifier());

            StringBuffer updateEdge = new StringBuffer("g.E().has('identifier',identifier)");
            Map<String, Object>  updateEdgeParam = TitanScritpUtils.getParamAndChangeScript4Update(updateEdge,statistical);
            updateEdgeParam.put("identifier", statistical.getIdentifier());
            try {
                titanCommonRepository.executeScript(scriptBuffer.toString(), graphParams);
                titanCommonRepository.executeScript(updateEdge.toString(), updateEdgeParam);
            } catch (Exception e) {
                LOG.error("titan_repository error:{};identifier:{}" ,e.getMessage(),statistical.getResource());
                //TODO titan sync
                return null;
            }
        }

        if(isAdd && StringUtils.isEmpty(techInfoEdgeId)){
            return null;
        }

        return statistical;
    }


}
