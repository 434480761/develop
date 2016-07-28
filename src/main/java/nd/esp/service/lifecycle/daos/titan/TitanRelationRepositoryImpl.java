package nd.esp.service.lifecycle.daos.titan;

import nd.esp.service.lifecycle.daos.titan.inter.TitanCommonRepository;
import nd.esp.service.lifecycle.daos.titan.inter.TitanRelationRepository;
import nd.esp.service.lifecycle.daos.titan.inter.TitanRepositoryUtils;
import nd.esp.service.lifecycle.repository.model.ResourceRelation;
import nd.esp.service.lifecycle.support.busi.titan.TitanSyncType;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.TitanScritpUtils;
import org.apache.tinkerpop.gremlin.driver.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class TitanRelationRepositoryImpl implements TitanRelationRepository {
	@Autowired
	private TitanRepositoryUtils titanRepositoryUtils;

	@Autowired
	private TitanCommonRepository titanCommonRepository;

	private static final Logger LOG = LoggerFactory
			.getLogger(TitanRelationRepositoryImpl.class);

	@Override
	public ResourceRelation add(ResourceRelation resourceRelation) {
		// FIXME 暂时不考虑关系边是否已经存在（目前的配置允许重复边的存在）
		if(resourceRelation == null){
			return null;
		}
		ResourceRelation result = addRelation(resourceRelation);
		if(result == null){
			if(titanRepositoryUtils.checkRelationExistInMysql(resourceRelation)){
				LOG.info("resourceRelation出错");
				titanRepositoryUtils.titanSync4MysqlAdd(TitanSyncType.SAVE_OR_UPDATE_ERROR, resourceRelation);
			} else {
				return resourceRelation;
			}
		}
		return result;
	}

	@Override
	public List<ResourceRelation> batchAdd(
			List<ResourceRelation> resourceRelations) {
		if(CollectionUtils.isEmpty(resourceRelations)){
			return new ArrayList<>();
		}
		List<ResourceRelation> resourceRelationList = new ArrayList<>();
		for (ResourceRelation resourceRelation : resourceRelations) {
			ResourceRelation rr = addRelation(resourceRelation);
			if(rr != null){
				resourceRelationList.add(rr);
			} else {
				if(titanRepositoryUtils.checkRelationExistInMysql(resourceRelation)){
					LOG.info("resourceRelation出错");
					titanRepositoryUtils.titanSync4MysqlAdd(TitanSyncType.SAVE_OR_UPDATE_ERROR, resourceRelation);
				} else {
					resourceRelationList.add(resourceRelation);
				}
			}
		}
		return resourceRelationList;
	}

	@Override
	public ResourceRelation update(ResourceRelation resourceRelation) {
		if(resourceRelation ==null ){
			return null;
		}

		ResourceRelation result = updateRelation(resourceRelation);
		if(result == null){
			if(titanRepositoryUtils.checkRelationExistInMysql(resourceRelation)){
				LOG.info("resourceRelation出错");
				titanRepositoryUtils.titanSync4MysqlAdd(TitanSyncType.SAVE_OR_UPDATE_ERROR, resourceRelation);
			} else {
				return resourceRelation;
			}
		}
		return result;
	}

	@Override
	public List<ResourceRelation> batchUpdate(List<ResourceRelation> entityList) {
		if(CollectionUtils.isEmpty(entityList)){
			return new ArrayList<>();
		}
		List<ResourceRelation> resourceRelationList = new ArrayList<>();
		for (ResourceRelation resourceRelation : entityList){
			ResourceRelation result = updateRelation(resourceRelation);
			if(result != null){
				resourceRelationList.add(result);
			} else {
				if(titanRepositoryUtils.checkRelationExistInMysql(resourceRelation)){
					LOG.info("resourceRelation出错");
					titanRepositoryUtils.titanSync4MysqlAdd(TitanSyncType.SAVE_OR_UPDATE_ERROR, resourceRelation);
				} else {
					resourceRelationList.add(resourceRelation);
				}
			}
		}
		return resourceRelationList;
	}

	@Override
	public void deleteRelationSoft(String primaryCategory, String identifier) {
		String script = "g.V().has(primaryCategory,'identifier',identifier).bothE().hasLabel('has_relation').property('enable','false')";
		Map<String,Object> paramMap = new HashMap<>();
		paramMap.put("primaryCategory", primaryCategory);
		paramMap.put("identifier", identifier);
		paramMap.put("primaryCategory", primaryCategory);

		try {
			titanCommonRepository.executeScript(script, paramMap);
		} catch (Exception e) {
			e.printStackTrace();
			LOG.info("resourceRelation出错");
			titanRepositoryUtils.titanSync4MysqlAdd(TitanSyncType.SAVE_OR_UPDATE_ERROR,
					primaryCategory,identifier);
		}
	}

	@Override
	public boolean delete(String identifier) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void batchAdd4Import(List<ResourceRelation> resourceRelations) {
		if(CollectionUtils.isEmpty(resourceRelations)){
			return ;
		}
		for (ResourceRelation resourceRelation : resourceRelations) {
			addRelation(resourceRelation);
		}
	}

	private ResourceRelation addRelation(ResourceRelation resourceRelation){
		StringBuffer scriptBuffer = new StringBuffer(
				"g.V().hasLabel(source_primaryCategory).has('identifier',source_identifier).next()" +
						".addEdge('has_relation'," +
						"g.V().hasLabel(target_primaryCategory).has('identifier',target_identifier).next(),'identifier',edgeIdentifier");

		Map<String, Object> createRelationParams = TitanScritpUtils
				.getParamAndChangeScript(scriptBuffer, resourceRelation);

		scriptBuffer.append(").id()");

		createRelationParams.put("source_primaryCategory", resourceRelation.getResType());
		createRelationParams.put("source_identifier", resourceRelation.getSourceUuid());
		createRelationParams.put("target_primaryCategory", resourceRelation.getResourceTargetType());
		createRelationParams.put("target_identifier", resourceRelation.getTarget());
		createRelationParams.put("edgeIdentifier", resourceRelation.getIdentifier());

		String edgeId;
		try {
			edgeId = titanCommonRepository.executeScriptUniqueString(scriptBuffer.toString(), createRelationParams);
		} catch (Exception e) {
			LOG.error("titan_repository error:{};identifier:{}" ,e.getMessage(),resourceRelation.getIdentifier());
			//TODO titan sync
			return null;
		}
		if(edgeId == null){
			return null;
		}

		return resourceRelation;
	}

	private ResourceRelation updateRelation(ResourceRelation resourceRelation){
		StringBuffer scriptBuffer = new StringBuffer("g.E().has('identifier',identifier)");
		Map<String, Object> graphParams = TitanScritpUtils.getParamAndChangeScript4Update(scriptBuffer,
				resourceRelation);
		graphParams.put("identifier", resourceRelation.getIdentifier());
		System.out.println(scriptBuffer.toString());
		String edgeId;
		try {
			edgeId = titanCommonRepository.executeScriptUniqueString(scriptBuffer.toString() ,graphParams);
		} catch (Exception e) {
			LOG.error("titan_repository error:{};identifier:{}" ,e.getMessage(),resourceRelation.getIdentifier());
			//TODO titan sync
			return null;
		}
		return resourceRelation;
	}

}
