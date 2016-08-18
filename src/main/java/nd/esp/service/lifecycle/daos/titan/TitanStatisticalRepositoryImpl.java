package nd.esp.service.lifecycle.daos.titan;

import nd.esp.service.lifecycle.daos.titan.inter.TitanCommonRepository;
import nd.esp.service.lifecycle.daos.titan.inter.TitanStatisticalRepository;
import nd.esp.service.lifecycle.repository.model.ResourceStatistical;
import nd.esp.service.lifecycle.support.busi.titan.TitanKeyWords;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.utils.TitanScritpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liuran on 2016/8/17.
 */
public class TitanStatisticalRepositoryImpl implements TitanStatisticalRepository{
    private static final Logger LOG = LoggerFactory
            .getLogger(TitanStatisticalRepositoryImpl.class);

    @Autowired
    private TitanCommonRepository titanCommonRepository;

    @Override
    public ResourceStatistical add(ResourceStatistical entity) {
        return null;
    }

    @Override
    public List<ResourceStatistical> batchAdd(List<ResourceStatistical> entityList) {
        return null;
    }

    @Override
    public ResourceStatistical update(ResourceStatistical entity) {
        return null;
    }

    @Override
    public List<ResourceStatistical> batchUpdate(List<ResourceStatistical> entityList) {
        return null;
    }

    @Override
    public boolean delete(String id) {
        return false;
    }

    @Override
    public boolean batchDelete(List<String> ids) {
        return false;
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
            scriptBuffer.append(");g.V().has(primaryCategory,'identifier',sourceIdentifier).next().addEdge('"+TitanKeyWords.has_statistical.toString()+"',statistical ,'identifier',edgeIdentifier");

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
