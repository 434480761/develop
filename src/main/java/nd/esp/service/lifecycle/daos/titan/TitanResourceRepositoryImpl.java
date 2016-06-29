package nd.esp.service.lifecycle.daos.titan;

import java.util.*;

import nd.esp.service.lifecycle.daos.titan.inter.TitanCommonRepository;
import nd.esp.service.lifecycle.daos.titan.inter.TitanResourceRepository;
import nd.esp.service.lifecycle.repository.Education;

import nd.esp.service.lifecycle.utils.TitanScritpUtils;
import org.apache.tinkerpop.gremlin.driver.ResultSet;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class TitanResourceRepositoryImpl<M extends Education> implements
        TitanResourceRepository<M> {
    private final static Logger LOG = LoggerFactory.getLogger(TitanResourceRepositoryImpl.class);
    @Autowired
    private TitanCommonRepository titanCommonRepository;

    @Override
    public M add(M model) {
        if(model == null){
            return null;
        }

        Long oldNodeId = titanCommonRepository.getVertexIdByLabelAndId(model.getPrimaryCategory(), model.getIdentifier());

        if(oldNodeId != null){
            update(model);
        } else {
            StringBuffer scriptBuffer = new StringBuffer("graph.addVertex(T.label, type");
            Map<String, Object> graphParams = TitanScritpUtils.getParamAndChangeScript(scriptBuffer,model);
            scriptBuffer.append(" ).id()");
            graphParams.put("type", model.getPrimaryCategory());

            Long id = titanCommonRepository.executeScriptUniqueLong(scriptBuffer.toString() ,graphParams);

            if(id != null){
                return model;
            }
        }
        return null;
    }

    @Override
    public M update(M model) {
        if (model == null) {
            return null;
        }

        Long id = titanCommonRepository.getVertexIdByLabelAndId(model.getPrimaryCategory(), model.getIdentifier());
        if (id == null) {
            return null;
        }

        StringBuffer scriptBuffer = new StringBuffer("g.V(" + id + ")");
        Map<String, Object> graphParams = TitanScritpUtils.getParamAndChangeScript4Update(scriptBuffer,
                model);
        titanCommonRepository.executeScript(scriptBuffer.toString() ,graphParams);

        return model;
    }

    @Override
    public List<M> batchUpdate(List<M> models) {
        if(models!=null){
            for (M m : models){
                update(m);
            }
        }
        return null;
    }

    @Override
    public boolean delete(String primaryCategory, String identifier) {
        //软删除
        String script = "g.V().has(primaryCategory,'identifier',identifier)" +
                ".property('lc_enable','false').id()";
        Map<String,Object> param = new HashMap<>();
        param.put("identifier", primaryCategory);
        param.put("primaryCategory", primaryCategory);

        Long id = titanCommonRepository.executeScriptUniqueLong(script, param);
        return true;
    }

    @Override
    public List<M> batchAdd(List<M> modelSet) {
        for (M model : modelSet) {
            add(model);
        }
        return null;
    }

    @Override
    public long count(String primaryCategory) {
        return 0;
    }

    @Override
    public Vertex get(String primaryCategory, String identifier) {

        return null;
    }


    @Override
    public ResultSet search(String script, Map<String, Object> scriptParamMap) {
        // FIXME
        return titanCommonRepository.executeScriptResultSet(script, scriptParamMap);
    }
}
