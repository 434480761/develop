package nd.esp.service.lifecycle.daos.titan;

import java.util.*;

import nd.esp.service.lifecycle.daos.coverage.v06.CoverageDao;
import nd.esp.service.lifecycle.daos.titan.inter.TitanCommonRepository;
import nd.esp.service.lifecycle.daos.titan.inter.TitanRepositoryUtils;
import nd.esp.service.lifecycle.daos.titan.inter.TitanResourceRepository;
import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.model.ResCoverage;
import nd.esp.service.lifecycle.support.busi.titan.TitanSyncType;
import nd.esp.service.lifecycle.utils.StringUtils;
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

    @Autowired
    private TitanRepositoryUtils titanRepositoryUtils;

    @Override
    public M add(M model) {
    	if(model == null){
            return null;
        }
        M modelNew = addResource(model);
        if(modelNew == null){
//            LOG.info("resource保存出错");
            titanRepositoryUtils.titanSync4MysqlAdd(TitanSyncType.SAVE_OR_UPDATE_ERROR,
                    model.getPrimaryCategory(), model.getIdentifier());
        }
        return modelNew;
    }

    @Override
    public M update(M model) {
    	if (model == null) {
            return null;
        }

        M modelNew = updateResource(model);
        if(modelNew == null){
//            LOG.info("resource保存出错");
            titanRepositoryUtils.titanSync4MysqlAdd(TitanSyncType.SAVE_OR_UPDATE_ERROR,
                    model.getPrimaryCategory(), model.getIdentifier());
        }
        return modelNew;
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
    public boolean delete(String id) {
        return false;
    }

    @Override
    public boolean batchDelete(List<String> ids) {
        return false;
    }

    @Override
    public boolean delete(String primaryCategory, String identifier) {
    	 //真删除
        try {
            titanCommonRepository.deleteVertexByLabelAndIdentifier(primaryCategory, identifier);
        } catch (Exception e) {
            LOG.error("titan_repository error:{};identifier:{}" ,e.getMessage(),identifier);
            //TODO titan sync
            return false;
        }
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
    public Vertex get(String primaryCategory, String identifier) {

        return null;
    }


    @Override
    public ResultSet search(String script, Map<String, Object> scriptParamMap) {
        // FIXME
    	try {
            return titanCommonRepository.executeScriptResultSet(script, scriptParamMap);
        } catch (Exception e) {
            LOG.error("titan_repository error:{}" ,e.getMessage());
        }
    	
    	return null;
    }
    
    private M addResource(M model){
        Long oldNodeId = null;
        try {
            oldNodeId = titanCommonRepository.getVertexIdByLabelAndId(model.getPrimaryCategory(), model.getIdentifier());
        } catch (Exception e) {
            LOG.error("titan_repository error:{};identifier:{}" ,e.getMessage(),model.getIdentifier());
            //TODO titan sync
            return null;
        }

        if(oldNodeId != null){
            return updateResource(model);
        } else {
            Long nodeId ;
            StringBuffer scriptBuffer = new StringBuffer("graph.addVertex(T.label, type");
            Map<String, Object> graphParams = TitanScritpUtils.getParamAndChangeScript(scriptBuffer,model);
            scriptBuffer.append(").id()");
            graphParams.put("type", model.getPrimaryCategory());

            try {
                nodeId = titanCommonRepository.executeScriptUniqueLong(scriptBuffer.toString() ,graphParams);
            } catch (Exception e) {
                LOG.error("titan_repository error:{};identifier:{}" ,e.getMessage(),model.getIdentifier());
                //TODO titan sync
                return null;
            }

            if (nodeId == null){
//                LOG.error("资源保存到titan失败");
                //TODO titan sync
                return null;
            }
        }

        return model;
    }
    
    private M updateResource(M model){
        Long id;
        try {
            id = titanCommonRepository.getVertexIdByLabelAndId(model.getPrimaryCategory(), model.getIdentifier());
        } catch (Exception e) {
//            e.printStackTrace();
//            LOG.error("资源更新到titan失败");
            //TODO titan sync
            return null;
        }
        if (id == null) {
            return null;
        }

        StringBuffer scriptBuffer = new StringBuffer("v=g.V(" + id + ")");
        Map<String, Object> graphParams = TitanScritpUtils.getParamAndChangeScript4Update(scriptBuffer,
                model);
        //TODO 更新操作需要返回ID进行判断更新的过程是否出现异常
        Long nodeId ;
        try {
            titanCommonRepository.executeScript(scriptBuffer.toString() ,graphParams);
        } catch (Exception e) {
            LOG.error("titan_repository error:{};identifier:{}" ,e.getMessage(),model.getIdentifier());
            //TODO titan sync
            return null;
        }
        updateResourceCoverage(model.getPrimaryCategory(), model.getIdentifier(), model.getStatus());
        
        return model;
    }

    private void updateResourceCoverage(String primaryCategory, String identifier, String status){

        Set<String> searchCoverages = new HashSet<>();
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
        try {
			titanCommonRepository.executeScript(script.toString(), param);
		} catch (Exception e) {
			// TODO Auto-generated catch block
            LOG.error("titan_repository error:{};identifier:{}" ,e.getMessage(),identifier);
		}


        script = new StringBuffer("g.V()has(primaryCategory,'identifier',identifier)");
        TitanScritpUtils.getSetScriptAndParam(script, param ,"search_coverage" ,searchCoverages);

        String searchCoverageString = StringUtils.join(searchCoverages,",").toLowerCase();
        script.append(".property('search_coverage_string',searchCoverageString)");
        param.put("searchCoverageString", searchCoverageString);

        try {
			titanCommonRepository.executeScript(script.toString(), param);
		} catch (Exception e) {
			// TODO Auto-generated catch block
            LOG.error("titan_repository error:{};identifier:{}" ,e.getMessage(),identifier);
		}
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
