package nd.esp.service.lifecycle.daos.titan;

import nd.esp.service.lifecycle.daos.titan.inter.TitanCommonRepository;
import nd.esp.service.lifecycle.daos.titan.inter.TitanRelationRepository;
import nd.esp.service.lifecycle.repository.model.ResourceRelation;
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
	private Client client;

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

		titanCommonRepository.executeScript(scriptBuffer.toString(), createRelationParams);

		return null;
	}

	@Override
	public List<ResourceRelation> batchAdd(
			List<ResourceRelation> resourceRelations) {
		List<ResourceRelation> resourceRelationList = new ArrayList<>();
		for (ResourceRelation resourceRelation : resourceRelations) {
			ResourceRelation rr = add(resourceRelation);
			if(rr != null){
				resourceRelationList.add(rr);
			}
		}
		return resourceRelationList;
	}

	@Override
	public ResourceRelation update(ResourceRelation resourceRelation) {
		if(resourceRelation ==null ){
			return null;
		}

		String id = titanCommonRepository.getEdgeIdByLabelAndId("has_relation", resourceRelation.getIdentifier());
		if (id == null) {
			return null;
		}

		StringBuffer scriptBuffer = new StringBuffer("g.E('" + id + "')");
		Map<String, Object> graphParams = TitanScritpUtils.getParamAndChangeScript4Update(scriptBuffer,
				resourceRelation);
		titanCommonRepository.executeScript(scriptBuffer.toString() ,graphParams);
		return null;
	}

	@Override
	public List<ResourceRelation> batchUpdate(List<ResourceRelation> entityList) {
		return null;
	}

	@Override
	public void deleteRelationSoft(String primaryCategory, String identifier) {
		String script = "g.V().has(primaryCategory,'identifier',identifier).bothE().hasLabel('has_relation').property('enable','false')";
		Map<String,Object> paramMap = new HashMap<>();
		paramMap.put("primaryCategory", primaryCategory);
		paramMap.put("identifier", identifier);
		paramMap.put("primaryCategory", primaryCategory);

		titanCommonRepository.executeScript(script, paramMap);
	}

	@Override
	public boolean delete(String identifier) {
		// TODO Auto-generated method stub
		return false;
	}

}
