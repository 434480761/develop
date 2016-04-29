package nd.esp.service.lifecycle.services.elasticsearch;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Query;

import nd.esp.service.lifecycle.daos.elasticsearch.EsResourceOperation;
import nd.esp.service.lifecycle.educommon.models.ResCoverageModel;
import nd.esp.service.lifecycle.educommon.models.ResourceModel;
import nd.esp.service.lifecycle.educommon.services.NDResourceService;
import nd.esp.service.lifecycle.educommon.services.impl.CommonServiceHelper;
import nd.esp.service.lifecycle.educommon.vos.constant.IncludesConstant;
import nd.esp.service.lifecycle.entity.elasticsearch.Resource;
import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.EspRepository;
import nd.esp.service.lifecycle.repository.ds.ComparsionOperator;
import nd.esp.service.lifecycle.repository.ds.Item;
import nd.esp.service.lifecycle.repository.ds.LogicalOperator;
import nd.esp.service.lifecycle.repository.ds.ValueUtils;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.ResCoverage;
import nd.esp.service.lifecycle.repository.sdk.impl.ServicesManager;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.utils.CollectionUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * 用于重建索引，从mysql 数据库取数据
 * 
 * @author linsm
 *
 */
@Service
public class IndexDataServiceImpl implements IndexDataService {

	private static final Logger LOG = LoggerFactory
			.getLogger(IndexDataServiceImpl.class);
	@Autowired
	private NDResourceService ndResourceService;
	@Autowired
	private CommonServiceHelper commonServiceHelper;
	@Autowired
	private EsResourceOperation esResourceOperation;

	@Autowired
	private SyncResourceService syncResourceService;

	/**
	 * 重建索引
	 * 
	 * @param toDate
	 *            在该时间前
	 * @param fromDate
	 *            在该时间后
	 * @param resourceType
	 *            资源类型
	 * @author linsm
	 */
	@Override
	public long index(String resourceType, Date toDate, Date fromDate) {

		String fieldName = "dblastUpdate";

		long indexNum = 0;
		// 分页
		int page = 0;
		int row = 500;
		EspRepository<?> espRepository = ServicesManager.get(resourceType);
		@SuppressWarnings("rawtypes")
		Page resourcePage = null;
		@SuppressWarnings("rawtypes")
		List entitylist = null;

		List<Item<? extends Object>> items = new ArrayList<>();
		if (toDate != null) {
			Item<Long> leItem = new Item<Long>();
			leItem.setKey(fieldName);
			leItem.setComparsionOperator(ComparsionOperator.LE);
			leItem.setLogicalOperator(LogicalOperator.AND);
			leItem.setValue(ValueUtils.newValue(Long.valueOf(toDate.getTime())));
			items.add(leItem);
		}

		if (fromDate != null) {
			Item<Long> geItem = new Item<Long>();
			geItem.setKey(fieldName);
			geItem.setComparsionOperator(ComparsionOperator.GE);
			geItem.setLogicalOperator(LogicalOperator.AND);
			geItem.setValue(ValueUtils.newValue(Long.valueOf(fromDate.getTime())));
			items.add(geItem);
		}

		Item<String> resourceTypeItem = new Item<String>();
		resourceTypeItem.setKey("primaryCategory");
		resourceTypeItem.setComparsionOperator(ComparsionOperator.EQ);
		resourceTypeItem.setLogicalOperator(LogicalOperator.AND);
		resourceTypeItem.setValue(ValueUtils.newValue(resourceType));
		items.add(resourceTypeItem);

		Sort sort = new Sort(Direction.ASC, fieldName);

		do {
			Pageable pageable = new PageRequest(page, row, sort);

			// 分页查询
			try {
				resourcePage = espRepository.findByItems(items, pageable);
				if (resourcePage == null) {
					break;
				}
				entitylist = resourcePage.getContent();
				if (entitylist == null) {
					continue;
				}
				// for (Object object : entitylist) {
				// Education education = (Education) object;
				// try {
				// EsResourceOperationImpl.getEsOperation().add(
				// new Resource(resourceType, education
				// .getIdentifier()));
				// indexNum++;
				// } catch (Exception e) {
				// LOG.error(e.getMessage());
				// }
				// }

				// bylsm
				// 暂时还不能采用异步批量（异步不好，可能压垮服务，线程太多），批量也还不可取，内部还是调用单个获取信息；（需要批量能支持，获取删除数据）--done
				Set<Resource> resources = new HashSet<Resource>();
				for (Object object : entitylist) {
					Education education = (Education) object;
					resources.add(new Resource(resourceType, education
							.getIdentifier()));
				}
				// BulkResponse bulkResponse =
				// syncResourceService.batchAdd(resources);
				// if (bulkResponse != null && !bulkResponse.hasFailures()) {
				// indexNum += bulkResponse.getItems().length;
				// }
				indexNum += syncResourceService.syncBatchAdd(resources);
			} catch (Exception e) {
				LOG.error(e.getMessage());
			}
		} while (++page < resourcePage.getTotalPages());

		return indexNum;
	}

	/**
	 * 删除对应资源索引
	 * 
	 * @param resourceType
	 *            资源类型
	 * @author linsm
	 */
	@Override
	public void deleteResource(String resourceType) {
		esResourceOperation.delete(resourceType);

	}

	@SuppressWarnings("unchecked")
	@Override
	public ResourceModel getDetailForES(String resourceType, String uuid)
			throws EspStoreException {
		ResourceModel resourceModel = ndResourceService.getDetail(resourceType,
				uuid, IncludesConstant.getIncludesList(), true);
		// get coverage and relations;
		ResCoverage resCoverageCondition = new ResCoverage();
		resCoverageCondition.setResource(uuid);
		resCoverageCondition.setResType(resourceType);
		List<ResCoverage> resCoverageResults = commonServiceHelper
				.getResCoverageRepositoryByResType(resourceType)
				.getAllByExample(resCoverageCondition);

		resourceModel.setCoverages(changeToCoverageModel(resCoverageResults));

		// List<ResourceRelation> resourceRelationResults = new
		// ArrayList<ResourceRelation>();
		// //source relations
		// ResourceRelation srcResourceRelationCondition = new
		// ResourceRelation();
		// srcResourceRelationCondition.setEnable(null);//fix default value
		// true;
		// srcResourceRelationCondition.setRes(uuid);
		// srcResourceRelationCondition.setResType(resourceType);
		// List<ResourceRelation> srcRelationResults=
		// resourceRelationRepository.getAllByExample(srcResourceRelationCondition);
		// if (CollectionUtils.isNotEmpty(srcRelationResults)) {
		// resourceRelationResults.addAll(srcRelationResults);
		//
		// }
		// //target relations
		// ResourceRelation targetResourceRelationCondition = new
		// ResourceRelation();
		// targetResourceRelationCondition.setEnable(null);//fix default value
		// true;
		// targetResourceRelationCondition.setResTarget(uuid);
		// targetResourceRelationCondition.setResourceTargetType(resourceType);
		// List<ResourceRelation> targetRelationResults=
		// resourceRelationRepository.getAllByExample(targetResourceRelationCondition);
		// if (CollectionUtils.isNotEmpty(targetRelationResults)) {
		// resourceRelationResults.addAll(targetRelationResults);
		//
		// }
		// resourceModel.setRelations(changeToRelationModel(resourceRelationResults));
		return resourceModel;
	}

	// private List<ResRelationModel> changeToRelationModel(
	// List<ResourceRelation> resourceRelationResults) {
	// List<ResRelationModel> resRelationModels = new
	// ArrayList<ResRelationModel>();
	// if(CollectionUtils.isNotEmpty(resourceRelationResults)){
	// for(ResourceRelation resourceRelation:resourceRelationResults){
	// if(resourceRelation != null){
	// resRelationModels.add(changeToRelationModel(resourceRelation));
	// }
	// }
	// }
	// return resRelationModels;
	// }

	// private ResRelationModel changeToRelationModel(
	// ResourceRelation resourceRelation) {
	// ResRelationModel resRelationModel = new ResRelationModel();
	// resRelationModel.setEnable(resourceRelation.getEnable());
	// resRelationModel.setLabel(resourceRelation.getLabel());
	// resRelationModel.setOrderNum(resourceRelation.getOrderNum());
	// resRelationModel.setRelationType(resourceRelation.getRelationType());
	// resRelationModel.setSource(resourceRelation.getSourceUuid());
	// resRelationModel.setSourceType(resourceRelation.getResType());
	// resRelationModel.setTags(resourceRelation.getTags());
	// resRelationModel.setTarget(resourceRelation.getResTarget());
	// resRelationModel.setTargetType(resourceRelation.getResourceTargetType());
	// return resRelationModel;
	// }

	private List<ResCoverageModel> changeToCoverageModel(
			List<ResCoverage> resCoverageResults) {
		List<ResCoverageModel> resCoverageModels = new ArrayList<ResCoverageModel>();
		if (CollectionUtils.isNotEmpty(resCoverageResults)) {
			for (ResCoverage resCoverage : resCoverageResults) {
				if (resCoverage != null) {
					resCoverageModels.add(changeToCoverageModel(resCoverage));
				}
			}
		}

		return resCoverageModels;
	}

	private ResCoverageModel changeToCoverageModel(ResCoverage resCoverage) {
		ResCoverageModel resCoverageModel = new ResCoverageModel();
		resCoverageModel.setIdentifier(resCoverage.getIdentifier());
		// resCoverageModel.setResource(resCoverage.getResource());
		// 这个似乎是没有必要的
		resCoverageModel.setResourceType(resCoverage.getResType());
		resCoverageModel.setStrategy(resCoverage.getStrategy());
		resCoverageModel.setTarget(resCoverage.getTarget());
		resCoverageModel.setTargetTitle(resCoverage.getTargetTitle());
		resCoverageModel.setTargetType(resCoverage.getTargetType());
		return resCoverageModel;
	}

	@Override
	public List<ResourceModel> batchDetailForES(String resourceType,
			Set<String> uuids) {
		List<ResourceModel> batchResults = ndResourceService.batchDetail(
				resourceType, uuids, IncludesConstant.getIncludesList(), true);
		Map<String, List<ResCoverageModel>> resCoverages = getCoverages(
				resourceType, uuids);
		return getBatchResult(batchResults, resCoverages);
	}

	private List<ResourceModel> getBatchResult(
			List<ResourceModel> batchResults,
			Map<String, List<ResCoverageModel>> resCoverages) {
		if (batchResults == null) {
			return null;
		}
		for (ResourceModel resourceModel : batchResults) {
			if (resourceModel != null) {
				resourceModel.setCoverages(resCoverages.get(resourceModel
						.getIdentifier()));
			}
		}
		return batchResults;
	}

	private Map<String, List<ResCoverageModel>> getCoverages(
			String resourceType, Set<String> uuids) {
		Query query = commonServiceHelper
				.getResCoverageRepositoryByResType(resourceType)
				.getEntityManager()
				.createNamedQuery("batchGetCoverageByResource");
		query.setParameter("rt", resourceType);
		query.setParameter("rids", uuids);

		@SuppressWarnings("unchecked")
		List<ResCoverage> result = query.getResultList();
		Map<String, List<ResCoverageModel>> resultMap = new HashMap<String, List<ResCoverageModel>>();
		if (CollectionUtils.isNotEmpty(result)) {
			for (ResCoverage resCoverage : result) {
				if (resCoverage != null) {
					List<ResCoverageModel> value = resultMap.get(resCoverage
							.getResource());
					if (value == null) {
						value = new ArrayList<ResCoverageModel>();
						resultMap.put(resCoverage.getResource(), value);
					}
					value.add(changeToCoverageModel(resCoverage));
				}
			}
		}
		return resultMap;
	}

	@Override
	public String getJson(String resourceType, String uuid) {
		try {
			return esResourceOperation
					.getJson(new Resource(resourceType, uuid));
		} catch (JsonProcessingException e) {
			LOG.error(e.getMessage());
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					"LC/ES/getJson", e.getMessage());
		}
	}

	@Override
	public int index(String resourceType, Set<String> uidSet) {
		Set<Resource> resources = new HashSet<Resource>();
		for (String uid : uidSet) {
			resources.add(new Resource(resourceType, uid));
		}
		return syncResourceService.syncBatchAdd(resources);

	}

}
