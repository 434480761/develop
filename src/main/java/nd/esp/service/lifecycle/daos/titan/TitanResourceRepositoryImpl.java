package nd.esp.service.lifecycle.daos.titan;

import java.util.*;

import nd.esp.service.lifecycle.daos.coverage.v06.CoverageDao;
import nd.esp.service.lifecycle.daos.titan.inter.TitanCommonRepository;
import nd.esp.service.lifecycle.daos.titan.inter.TitanResourceRepository;
import nd.esp.service.lifecycle.repository.Education;

import nd.esp.service.lifecycle.repository.model.ResCoverage;
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

    @Autowired
    private CoverageDao coverageDao;


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


        updateResourceCoverage(model.getPrimaryCategory(), model.getIdentifier(), model.getStatus());

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
        updateResourceCoverage(model.getPrimaryCategory(), model.getIdentifier(), model.getStatus());
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

    private void updateResourceCoverage(String primaryCategory, String identifier, String status){

        List<String> searchCoverages = new ArrayList<>();
        Set<String> uuids = new HashSet<>();
        uuids.add(identifier);
        List<ResCoverage> resCoverageList = coverageDao.queryCoverageByResource(primaryCategory, uuids);
        for (ResCoverage resCoverage : resCoverageList){
            searchCoverages.addAll(getAllResourceCoverage(resCoverage, status));
        }

        StringBuffer script = new StringBuffer("g.V().has(primaryCategory,'identifier',identifier).properties('search_coverage').drop();");
        Map<String, Object> param = new HashMap<>();
        param.put("primaryCategory" ,primaryCategory);
        param.put("identifier" ,identifier);
        titanCommonRepository.executeScript(script.toString(), param);


        script = new StringBuffer("g.V()has(primaryCategory,'identifier',identifier)");
        TitanScritpUtils.getSetScriptAndParam(script, param ,"search_coverage" ,searchCoverages);
        titanCommonRepository.executeScript(script.toString(), param);
    }

    private List<String> getAllResourceCoverage(ResCoverage resCoverage, String status){
        List<String> searchCoverages = new ArrayList<>();
        String value1 = resCoverage.getTargetType()+"/"+resCoverage.getTarget()+"/"+resCoverage.getStrategy() +"/"+status;
        String value2 = resCoverage.getTargetType()+"/"+resCoverage.getTarget()+"//"+status;
        String value3 = resCoverage.getTargetType()+"/"+resCoverage.getTarget()+"/"+resCoverage.getStrategy() +"/";
        String value4 = resCoverage.getTargetType()+"/"+resCoverage.getTarget()+"//";

        searchCoverages.add(value1);
        searchCoverages.add(value2);
        searchCoverages.add(value3);
        searchCoverages.add(value4);

        return searchCoverages;
    }
}
