package nd.esp.service.lifecycle.services.elasticsearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import nd.esp.service.lifecycle.daos.elasticsearch.EsResourceOperation;
import nd.esp.service.lifecycle.daos.elasticsearch.EsSyncDao;
import nd.esp.service.lifecycle.entity.elasticsearch.Resource;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.utils.StringUtils;

import org.apache.commons.collections4.CollectionUtils;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;

@Service
public class AsynEsResourceServiceImpl implements AsynEsResourceService {

	@Autowired
	private EsSyncDao esSyncDao;

	@Autowired
	private EsResourceOperation esResourceOperation;

	private final static ExecutorService executorService = CommonHelper.getPrimaryExecutorService();

	private static final Logger logger = LoggerFactory
			.getLogger(AsynEsResourceServiceImpl.class);

	@Override
	public void asynAdd(final Resource resource) {
		try {
			try {
				esSyncDao.beforeUpdate(resource);
			} catch (EspStoreException e) {
				logger.error("resourceType: {},uuid: {}" + e.getMessage(),
						resource.getResourceType(), resource.getIdentifier());
			}

			executorService.execute(new Runnable() {

				@Override
				public void run() {
					try {
						esResourceOperation.add(resource);
						try {
							esSyncDao.afterUpdate(resource);
						} catch (EspStoreException e) {
							logger.error(
									"resourceType: {},uuid: {}"
											+ e.getMessage(),
									resource.getResourceType(),
									resource.getIdentifier());
						}
					} catch (JsonProcessingException e) {
						logger.error(
								"resourceType: {},uuid: {}" + e.getMessage(),
								resource.getResourceType(),
								resource.getIdentifier());
					}

				}
			});
		} catch (Exception e) {
			logger.error("resourceType: {},uuid: {}" + e.getMessage(),
					resource.getResourceType(), resource.getIdentifier());
		}

	}

	@Override
	public void asynBatchAdd(final Set<Resource> resourceSet) {
		if (CollectionUtils.isEmpty(resourceSet)) {
			return;
		}

		try {
			try {
				esSyncDao.batchBeforeUpdate(resourceSet);
			} catch (EspStoreException e) {
				logBatchMessages(resourceSet, e);
			}

			executorService.execute(new Runnable() {

				@Override
				public void run() {
					try {
						BulkResponse bulkResponse = esResourceOperation
								.batchAdd(resourceSet);

						Set<Resource> successSyncResourceSet = new HashSet<Resource>();
						if (bulkResponse != null
								&& bulkResponse.getItems() != null
								&& bulkResponse.getItems().length != 0) {
							for (BulkItemResponse item : bulkResponse
									.getItems()) {
								if (item.isFailed()) {
									logForItem(item);
								} else {
									successSyncResourceSet.add(new Resource(
											item.getType(), item.getId()));
								}
							}
						}
						if (CollectionUtils.isNotEmpty(successSyncResourceSet)) {
							try {
								esSyncDao
										.batchAfterUpdate(successSyncResourceSet);
							} catch (EspStoreException e) {
								logBatchMessages(successSyncResourceSet, e);
							}

						}

					} catch (IOException e) {
						logBatchMessages(resourceSet, e);
					}

				}
			});
		} catch (Exception e) {
			logBatchMessages(resourceSet, e);
		}

	}

	protected void logBatchMessages(Set<Resource> resourceSet, Exception e) {
		List<String> messages = new ArrayList<String>();
		for (Resource resource : resourceSet) {
			StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append(resource.getResourceType()).append(":")
					.append(resource.getIdentifier());
			messages.add(stringBuffer.toString());
		}
		logger.error("resourceTypes/uuids: {}" + e.getMessage(),
				StringUtils.join(messages, ";"));

	}

	private void logForItem(BulkItemResponse item) {
		logger.error("primary_category:{};identifier:{};message:{}",
				item.getType(), item.getId(), item.getFailureMessage());

	}

}
