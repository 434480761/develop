package nd.esp.service.lifecycle.daos.titan;

import nd.esp.service.lifecycle.daos.titan.inter.TitanCommonRepository;
import nd.esp.service.lifecycle.daos.titan.inter.TitanCoverageRepository;
import nd.esp.service.lifecycle.repository.model.ResCoverage;
import nd.esp.service.lifecycle.support.enums.ES_Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class TitanCoverageRepositoryImpl implements TitanCoverageRepository {
	private final static Logger LOG = LoggerFactory.getLogger(TitanCoverageRepositoryImpl.class);
	private static Map<String, Long> coverageCacheMap = new HashMap<>();
	private static List<String> coverageCacheTargetList = new ArrayList<>();
	static {
		coverageCacheTargetList.add("nd_org_shareing");
		coverageCacheTargetList.add("nd_org_owner");
		coverageCacheTargetList.add("qa_debug_test");
		coverageCacheTargetList.add("qa_group_test");
	}
	@Autowired
	private TitanCommonRepository titanCommonRepository;

	@Override
	public ResCoverage add(ResCoverage resCoverage) {

		Long coverageNodeId = getCoverageNodeId(resCoverage);
		if (coverageNodeId == null) {
			// coverage node not exist
			// create coverage node and create has_coveage edge
			StringBuffer scriptBuffer = new StringBuffer(
					"graph.addVertex(T.label,'coverage','target_type',target_type,'strategy',strategy,'target',target).id()");
			Map<String, Object> innerGraphParams = new HashMap<String, Object>();
			innerGraphParams.put(ES_Field.target_type.toString(),
					resCoverage.getTargetType());
			innerGraphParams.put(ES_Field.target.toString(),
					resCoverage.getTarget());
			innerGraphParams.put(ES_Field.strategy.toString(),
					resCoverage.getStrategy());

			coverageNodeId = titanCommonRepository.executeScriptUniqueLong(scriptBuffer.toString() , innerGraphParams);
		}

		if(coverageNodeId == null){
			return null;
		}

		String script = "g.V().has(primaryCategory,'identifier',identifier).next()" +
				".addEdge('has_coverage',g.V(coverageNodeId).next(),'identifier',edgeIdentifier).id()";
		Map<String, Object> graphParams = new HashMap<String, Object>();
		graphParams.put("primaryCategory", resCoverage.getResType());
		graphParams.put("identifier", resCoverage.getResource());
		graphParams.put("coverageNodeId", coverageNodeId);
		graphParams.put("edgeIdentifier",resCoverage.getIdentifier());

		titanCommonRepository.executeScript(script , graphParams);

		return resCoverage;
	}


	private Long getCoverageNodeId(ResCoverage resCoverage) {

		if(resCoverage==null || resCoverage.getTargetType()==null
				||resCoverage.getTarget()==null
				||resCoverage.getStrategy()==null){
			return null;
		}

		Long coverageId;

		//缓存所有的nd、qa的覆盖范围
		String key = resCoverage.getTarget()+"_"+resCoverage.getTargetType()+"_"+resCoverage.getStrategy();
		if(coverageCacheTargetList.contains(key.toLowerCase())){
			coverageId = coverageCacheMap.get(key);
			if(coverageId == null){
				coverageId = getCoverageIdFormTitan(resCoverage);
				coverageCacheMap.put(key, coverageId);
			}
		} else {
			coverageId = getCoverageIdFormTitan(resCoverage);
		}

		return coverageId;
	}

	private Long getCoverageIdFormTitan(ResCoverage resCoverage){
		String scriptString = "g.V().hasLabel('coverage').has('target_type',target_type).has('target',target).has('strategy',strategy).id()";

		Map<String, Object> graphParams = new HashMap<String, Object>();
		graphParams.put(ES_Field.target_type.toString(),
				resCoverage.getTargetType());
		graphParams.put(ES_Field.target.toString(), resCoverage.getTarget());
		graphParams
				.put(ES_Field.strategy.toString(), resCoverage.getStrategy());

		return titanCommonRepository.executeScriptUniqueLong(scriptString, graphParams);
	}

	@Override
	public List<ResCoverage> batchAdd(List<ResCoverage> resCoverages) {
		List<ResCoverage> list = new ArrayList<>();
		for (ResCoverage resCoverage : resCoverages) {
			ResCoverage rc = add(resCoverage);
			list.add(rc);
		}
		return list;
	}

	/**
	 *覆盖范围是多对一的关系，修改覆盖范围的方式是，先删除旧的关系，再建立新关系
	 * */
	@Override
	public ResCoverage update(ResCoverage resCoverage) {
		delete(resCoverage.getIdentifier());
		add(resCoverage);
		return null;
	}

	@Override
	public List<ResCoverage> batchUpdate(List<ResCoverage> resCoverageSet) {
		for(ResCoverage resCoverage : resCoverageSet){
			update(resCoverage);
		}
		return null;
	}

	@Override
	public boolean delete(String id) {

		if(id==null){
			return false;
		}
		String script = "g.E().has('identifier',identifier)";
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("identifier",id);

		titanCommonRepository.executeScript(script,paramMap);

		return true;
	}

	@Override
	public boolean batchDelete(List<String> ids) {
		if(ids == null){
			return false;
		}

		for(String id : ids){
			delete(id);
		}

		return false;
	}

	@Override
	public long countCoverage() {
		return 0;
	}

	@Override
	public long countCover() {
		return 0;
	}

}
