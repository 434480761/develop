package nd.esp.service.lifecycle.daos.elasticsearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nd.esp.service.lifecycle.entity.elasticsearch.ES_BookResourceModel;
import nd.esp.service.lifecycle.entity.elasticsearch.ES_QuestionsResourceModel;
import nd.esp.service.lifecycle.entity.elasticsearch.ES_ResourceModel;
import nd.esp.service.lifecycle.entity.elasticsearch.Resource;
import nd.esp.service.lifecycle.support.Constant;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.StringUtils;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.deletebyquery.DeleteByQueryRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.QuerySourceBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.nd.gaea.client.http.WafHttpClient;
import com.nd.gaea.rest.o2o.JacksonCustomObjectMapper;

/**
 * 资源数据索引操作
 * 
 * @author linsm
 *
 */
@SuppressWarnings("deprecation")
@Repository
public class EsResourceOperationImpl implements EsResourceOperation {

	private static final Logger logger = LoggerFactory
			.getLogger(EsResourceOperationImpl.class);

	private static final int MAX_BULK_VALUE = 100;// 批量获取接口id个数
	private static final int WAF_CLIENT_CONNECT_TIMEOUT_VALUE = 100000;
	private static final int WAF_CLIENT_SOCKET_TIMEOUT_VALUE = 100000;
	// private static final int WAF_CLIENT_RETRY_COUNT = 3;
	public static final WafHttpClient WAF_HTTP_CLIENT = new WafHttpClient(
			WAF_CLIENT_CONNECT_TIMEOUT_VALUE, WAF_CLIENT_SOCKET_TIMEOUT_VALUE);
	// online
	private final static String LC_API_URL_FOR_ES = Constant.LIFE_CYCLE_DOMAIN_URL
			+ "/elasticsearch/index/data/{resourceType}/{identifier}/forES";
	private final static String LC_API_URL_FOR_ES_BATCH = Constant.LIFE_CYCLE_DOMAIN_URL
			+ "/elasticsearch/index/data/{resourceType}/list/forES?";
	// bylsm local
	// private final static String LC_API_URL_FOR_ES =
	// "http://localhost:8080/esp-lifecycle/elasticsearch/index/data/{resourceType}/{identifier}/forES";
	// private final static String LC_API_URL_FOR_ES_BATCH =
	// "http://localhost:8080/esp-lifecycle/elasticsearch/index/data/{resourceType}/list/forES?";

	public final static JacksonCustomObjectMapper ObjectMapper = new JacksonCustomObjectMapper();

	@Autowired
	private Client client;

	/**
	 * 删除索引数据
	 * 
	 * @author linsm
	 */
	@Override
	public DeleteResponse delete(Resource resource) {

		return client
				.prepareDelete(Constant.ES_INDEX_NAME,
						resource.getResourceType(), resource.getIdentifier())
				.execute().actionGet();
	}

	@Override
	public BulkResponse batchDelete(Set<Resource> resourceSet) {
		BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();
		for (Resource resource : resourceSet) {
			bulkRequestBuilder.add(client.prepareDelete(Constant.ES_INDEX_NAME,
					resource.getResourceType(), resource.getIdentifier()));
		}
		return bulkRequestBuilder.execute().actionGet();
	}

	/**
	 * 查询
	 * 
	 * @param resourceType
	 *            资源类型
	 * @param queryBuilder
	 *            查询条件
	 * @param from
	 *            偏离值
	 * @param size
	 *            一页的大小
	 * @param sortBuilders
	 *            排序
	 * @author linsm
	 * @return
	 */
	@Override
	public SearchHits search(String resourceType, QueryBuilder queryBuilder,
			int from, int size, List<SortBuilder> sortBuilders) {
		SearchRequestBuilder searchRequestBuilder = client
				.prepareSearch(Constant.ES_INDEX_NAME).setTypes(resourceType)
				.setQuery(queryBuilder).setFrom(from).setSize(size);
		if (sortBuilders != null) {
			for (SortBuilder sortBuilder : sortBuilders) {
				searchRequestBuilder.addSort(sortBuilder);
			}
		}
		SearchResponse searchResponse = searchRequestBuilder.execute()
				.actionGet();
		SearchHits searchHits = searchResponse.getHits();
		logger.info("elasticsearch took time:{} (ms)",
				searchResponse.getTookInMillis());
		return searchHits;
	}

	/**
	 * 删除索引数据
	 * 
	 * @param primaryCategory
	 *            资源类型
	 * @author linsm
	 */
	@Deprecated
	@Override
	public void delete(String primaryCategory) {
		DeleteByQueryRequest request = Requests.deleteByQueryRequest(
				Constant.ES_INDEX_NAME).types(primaryCategory);
		QueryBuilder query = QueryBuilders.matchAllQuery();
		QuerySourceBuilder querySourceBuilder = new QuerySourceBuilder();
		querySourceBuilder.setQuery(query);
		request.source(querySourceBuilder);
		client.deleteByQuery(request).actionGet();
	}

	@Override
	public IndexResponse add(Resource resource) throws JsonProcessingException {
		// if (!isValidResource(resource)) {
		// return;
		// }
		IndexRequestBuilder indexRequestBuilder = createIndexRequestBuilder(
				resource.getResourceType(), getDetail(resource));
		return client.index(indexRequestBuilder.request()).actionGet();
	}

	/**
	 * 判断resource 是否合法： 资源类型符合要求，资源id非空
	 * 
	 * @param resource
	 * @return
	 */
	// private boolean isValidResource(Resource resource) {
	// if (ResourceTypeSupport.isValidEsResourceType(resource
	// .getResourceType())
	// && StringUtils.isNotEmpty(resource.getIdentifier())) {
	// return true;
	// }
	// logger.info("invalid resource:{}/{}", resource.getResourceType(),
	// resource.getIdentifier());
	// return false;
	// }

	private IndexRequestBuilder createIndexRequestBuilder(String resourceType,
			ES_ResourceModel es_ResourceModel) throws JsonProcessingException {

		IndexRequestBuilder indexRequestBuilder = client.prepareIndex(
				Constant.ES_INDEX_NAME, resourceType,
				es_ResourceModel.getIdentifier()).setSource(
				EsResourceOperationImpl.ObjectMapper
						.writeValueAsBytes(es_ResourceModel));
		return indexRequestBuilder;
	}

	private ES_ResourceModel getDetail(Resource resource) {
		String resourceType = resource.getResourceType();
		String identifier = resource.getIdentifier();
		@SuppressWarnings("rawtypes")
		Class resourceEsClass = ES_ResourceModel.class;
		if (ResourceNdCode.questions.toString().equals(resourceType)) {
			resourceEsClass = ES_QuestionsResourceModel.class;
		} else if (ResourceNdCode.ebooks.toString().equals(resourceType)
				|| ResourceNdCode.teachingmaterials.toString().equals(
						resourceType)
				|| ResourceNdCode.guidancebooks.toString().equals(resourceType)) {
			resourceEsClass = ES_BookResourceModel.class;
		}
		Map<String, Object> varParamMap = new HashMap<String, Object>();
		varParamMap.put("resourceType", resourceType);
		varParamMap.put("identifier", identifier);
		ES_ResourceModel es_ResourceModel = WAF_HTTP_CLIENT.getForObject(
				LC_API_URL_FOR_ES, resourceEsClass, varParamMap);
		return es_ResourceModel;
	}

	private List<ES_ResourceModel> batchDeail(String resourceType,
			Set<String> uuidSet) {
		List<ES_ResourceModel> es_ResourceModels = new ArrayList<ES_ResourceModel>();
		if (CollectionUtils.isNotEmpty(uuidSet)) {
			StringBuffer stringBuffer = new StringBuffer();
			for (String uuid : uuidSet) {
				stringBuffer.append("&uuid=").append(uuid);
			}
			String responseResultString = WAF_HTTP_CLIENT.getForObject(
					LC_API_URL_FOR_ES_BATCH
							+ stringBuffer.toString().substring(1),
					String.class, resourceType);
			if (StringUtils.isNotEmpty(responseResultString)) {
				// extend property question, book
				if (ResourceNdCode.questions.toString().equals(resourceType)) {
					try {
						es_ResourceModels = ObjectMapper
								.readValue(
										responseResultString,
										new TypeReference<List<ES_QuestionsResourceModel>>() {
										});
					} catch (IOException e) {
						logger.error(e.getMessage());
					}
				} else if (ResourceNdCode.ebooks.toString()
						.equals(resourceType)
						|| ResourceNdCode.teachingmaterials.toString().equals(
								resourceType)
						|| ResourceNdCode.guidancebooks.toString().equals(
								resourceType)) {
					try {
						es_ResourceModels = ObjectMapper
								.readValue(
										responseResultString,
										new TypeReference<List<ES_BookResourceModel>>() {
										});
					} catch (IOException e) {
						logger.error(e.getMessage());
					}
				} else {
					try {
						es_ResourceModels = ObjectMapper.readValue(
								responseResultString,
								new TypeReference<List<ES_ResourceModel>>() {
								});
					} catch (IOException e) {
						logger.error(e.getMessage());
					}
				}

			}
		}

		return es_ResourceModels;
	}

	@Override
	public BulkResponse batchAdd(Set<Resource> resources) {
		if (CollectionUtils.isEmpty(resources)) {
			return null;
		}
		BulkRequestBuilder bulkRequest = client.prepareBulk();
		// 根据资源类型来区分：
		Map<String, List<String>> typeUuidSetMap = new HashMap<String, List<String>>();
		for (Resource resource : resources) {
			// if (!isValidResource(resource)) {
			// continue;
			// }
			List<String> value = typeUuidSetMap.get(resource.getResourceType());
			if (value == null) {
				value = new ArrayList<String>();
				typeUuidSetMap.put(resource.getResourceType(), value);
			}
			value.add(resource.getIdentifier());

		}

		if (CollectionUtils.isNotEmpty(typeUuidSetMap)) {
			for (Map.Entry<String, List<String>> entry : typeUuidSetMap
					.entrySet()) {
				int size = entry.getValue().size();
				while (size > MAX_BULK_VALUE) {
					addToBulkRequest(
							bulkRequest,
							entry.getKey(),
							batchDeail(
									entry.getKey(),
									new HashSet<String>(entry.getValue()
											.subList(size - MAX_BULK_VALUE,
													size))));
					size -= MAX_BULK_VALUE;
				}
				addToBulkRequest(
						bulkRequest,
						entry.getKey(),
						batchDeail(entry.getKey(), new HashSet<String>(entry
								.getValue().subList(0, size))));

			}
		}

		return bulkRequest.execute().actionGet();

	}

	private void addToBulkRequest(BulkRequestBuilder bulkRequest,
			String resourceType, List<ES_ResourceModel> es_ResourceModels) {
		if (CollectionUtils.isEmpty(es_ResourceModels)) {
			return;
		}
		for (ES_ResourceModel es_ResourceModel : es_ResourceModels) {
			try {
				bulkRequest.add(createIndexRequestBuilder(resourceType,
						es_ResourceModel));
			} catch (JsonProcessingException e) {
				logger.error(e.getMessage());
			}
		}

	}

	@Override
	public String getJson(Resource resource) throws JsonProcessingException {
		return ObjectMapper.writeValueAsString(getDetail(resource));
	}

}
