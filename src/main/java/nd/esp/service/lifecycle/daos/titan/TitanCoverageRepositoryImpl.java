package nd.esp.service.lifecycle.daos.titan;

import nd.esp.service.lifecycle.daos.coverage.v06.CoverageDao;
import nd.esp.service.lifecycle.daos.titan.inter.TitanCommonRepository;
import nd.esp.service.lifecycle.daos.titan.inter.TitanCoverageRepository;
import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.EspRepository;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.ResCoverage;
import nd.esp.service.lifecycle.repository.sdk.impl.ServicesManager;
import nd.esp.service.lifecycle.services.titan.TitanResultParse;
import nd.esp.service.lifecycle.support.enums.ES_Field;
import nd.esp.service.lifecycle.utils.TitanScritpUtils;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.driver.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class TitanCoverageRepositoryImpl implements TitanCoverageRepository {
	private final static Logger LOG = LoggerFactory.getLogger(TitanCoverageRepositoryImpl.class);
	private static Map<String, Long> coverageCacheMap = new HashMap<>();
	private static List<String> coverageCacheTargetList = new ArrayList<>();

	@Autowired
	private CoverageDao coverageDao;


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

		addCoverage(resCoverage);

		addResourceCoverage(resCoverage);

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
		Map<String, String> sourceMap = new HashMap<>();
		for (ResCoverage resCoverage : resCoverages) {
			ResCoverage rc = addCoverage(resCoverage);
			list.add(rc);
			sourceMap.put(resCoverage.getResource(),resCoverage.getResType());
		}

		for(String identifier : sourceMap.keySet()){
			updateResourceCoverage(sourceMap.get(identifier), identifier);
		}

		return list;
	}

	/**
	 *覆盖范围是多对一的关系，修改覆盖范围的方式是，先删除旧的关系，再建立新关系
	 * */
	@Override
	public ResCoverage update(ResCoverage resCoverage) {
		updateCoverage(resCoverage);
		updateResourceCoverage(resCoverage.getResType(), resCoverage.getResource());
		return null;
	}

	@Override
	public List<ResCoverage> batchUpdate(List<ResCoverage> resCoverageSet) {
		Map<String, String> sourceMap = new HashMap<>();
		for(ResCoverage resCoverage : resCoverageSet){
			updateCoverage(resCoverage);
			sourceMap.put(resCoverage.getResource(),resCoverage.getResType());
		}
		for(String identifier : sourceMap.keySet()){
			updateResourceCoverage(sourceMap.get(identifier), identifier);
		}
		return null;
	}

	@Override
	public boolean delete(String id) {

		if(id==null){
			return false;
		}

		String srciptResource = "g.E().has('identifier',identifier).outV().valueMap()";
		Map<String, Object> paramResource = new HashMap<>();
		paramResource.put("identifier",id);
		ResultSet resultSet = titanCommonRepository.executeScriptResultSet(srciptResource, paramResource);

		Iterator<Result> iterator = resultSet.iterator();
		String result = "";
		if (iterator.hasNext()){
			result = iterator.next().getString();
		}
		Map<String,String> valueMap = TitanResultParse.toMap(result);
		String resourceIdentifier = valueMap.get("identifier");
		String primaryCategory = valueMap.get("primary_category");
		updateResourceCoverage(primaryCategory, resourceIdentifier);


		String script = "g.E().has('identifier',identifier).drop()";
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

	private ResCoverage updateCoverage(ResCoverage resCoverage){
		delete(resCoverage.getIdentifier());
		add(resCoverage);
		return resCoverage;
	}

	private ResCoverage addCoverage(ResCoverage resCoverage){
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

	private void updateResourceCoverage(String primaryCategory, String identifier){
		Education education = getEducation(primaryCategory, identifier);

		List<String> searchCoverages = new ArrayList<>();
		Set<String> uuids = new HashSet<>();
		uuids.add(identifier);
		List<ResCoverage> resCoverageList = coverageDao.queryCoverageByResource(primaryCategory, uuids);
		for (ResCoverage resCoverage : resCoverageList){
			searchCoverages.addAll(getAllResourceCoverage(resCoverage, education.getStatus()));
		}

		String deleteScript = "g.V().has(primaryCategory,'identifier',identifier).properties('search_coverage').drop();";
		Map<String, Object> param = new HashMap<>();
		param.put("primaryCategory" ,primaryCategory);
		param.put("identifier" ,identifier);
		titanCommonRepository.executeScript(deleteScript, param);

		StringBuffer addScript = new StringBuffer("g.V().has(primaryCategory,'identifier',identifier)");
		TitanScritpUtils.getSetScriptAndParam(addScript, param,"search_coverage",searchCoverages);

		titanCommonRepository.executeScript(addScript.toString(), param);

	}

	private void addResourceCoverage(ResCoverage resCoverage){

		Education education = getEducation(resCoverage.getResType(), resCoverage.getResource());

		List<String> searchCoverages = getAllResourceCoverage(resCoverage, education.getStatus());

		StringBuffer addScript = new StringBuffer("g.V().has(primaryCategory,'identifier',identifier)");
		Map<String, Object> param = new HashMap<>();
		param.put("primaryCategory" ,resCoverage.getResType());
		param.put("identifier" ,resCoverage.getResource());
		TitanScritpUtils.getSetScriptAndParam(addScript, param,"search_coverage",searchCoverages);

		titanCommonRepository.executeScript(addScript.toString(), param);
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


	private Education getEducation(String primaryCategory, String identifier){
		EspRepository<?> espRepository = ServicesManager.get(primaryCategory);
		Education education = null;
		try {
			education = (Education) espRepository.get(identifier);
		} catch (EspStoreException e) {
			e.printStackTrace();
		}

		return education;
	}

}
