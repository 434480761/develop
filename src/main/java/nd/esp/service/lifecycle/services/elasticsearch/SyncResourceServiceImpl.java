package nd.esp.service.lifecycle.services.elasticsearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import nd.esp.service.lifecycle.daos.elasticsearch.EsResourceOperation;
import nd.esp.service.lifecycle.daos.elasticsearch.EsSyncDao;
import nd.esp.service.lifecycle.entity.elasticsearch.Resource;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.support.busi.elasticsearch.ResourceTypeSupport;
import nd.esp.service.lifecycle.utils.StringUtils;

@Service
public class SyncResourceServiceImpl implements SyncResourceService {
	private static final Logger logger = LoggerFactory
			.getLogger(SyncResourceServiceImpl.class);

	@Autowired
	private EsSyncDao esSyncDao;

	@Autowired
	private EsResourceOperation esResourceOperation;

	@Override
	public boolean syncDelete(Resource resource) {
		try {
			esResourceOperation.delete(resource);
			return true;
		} catch (Exception e) {
			logger.error(e.getMessage());
			try {
				esSyncDao.beforeDelete(resource);
			} catch (EspStoreException e1) {
				logger.error(e1.getMessage());
			}
		}

		return false;
	}

	@Override
	public int syncBatchDelete(Set<Resource> resourceSet) {
		int successNum = 0;
		if (CollectionUtils.isEmpty(resourceSet)) {
			logger.info("del with zero num data set");
			return successNum;
		}
		Set<Resource> failResourceSet = new HashSet<Resource>(resourceSet);
		try {
			BulkResponse bulkResponse = esResourceOperation
					.batchDelete(resourceSet);

			if (bulkResponse != null && bulkResponse.getItems() != null) {
				for (BulkItemResponse item : bulkResponse.getItems()) {
					if (item.isFailed()) {
						logForItem(item);
					} else {
						successNum++;
						failResourceSet.remove(new Resource(item.getType(),
								item.getId()));
					}
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			if (CollectionUtils.isNotEmpty(failResourceSet))
				try {
					esSyncDao.batchBeforeDelete(failResourceSet);
				} catch (EspStoreException e) {
					logger.error(e.getMessage());
				}
		}

		return successNum;
	}

	private void logForItem(BulkItemResponse item) {
		logger.error("primary_category:{};identifier:{};message:{}",
				item.getType(), item.getId(), item.getFailureMessage());

	}

	@Override
	public boolean syncAdd(Resource resource) {
		try {
			esResourceOperation.add(resource);
			return true;
		} catch (Exception e) {
			logger.error(e.getMessage());
			try {
				esSyncDao.beforeUpdate(resource);
			} catch (EspStoreException e1) {
				logger.error(e1.getMessage());
			}
		}

		return false;
	}

	@Override
	public int syncBatchAdd(Set<Resource> resourceSet) {
		int successNum = 0;
		if (CollectionUtils.isEmpty(resourceSet)) {
			logger.info("del with zero num data set");
			return successNum;
		}
		Set<Resource> failResourceSet = new HashSet<Resource>(resourceSet);
		try {
			BulkResponse bulkResponse = esResourceOperation
					.batchAdd(resourceSet);

			if (bulkResponse != null && bulkResponse.getItems() != null) {
				for (BulkItemResponse item : bulkResponse.getItems()) {
					if (item.isFailed()) {
						logForItem(item);
					} else {
						successNum++;
						failResourceSet.remove(new Resource(item.getType(),
								item.getId()));
					}
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			if (CollectionUtils.isNotEmpty(failResourceSet))
				try {
					esSyncDao.batchBeforeUpdate(failResourceSet);
				} catch (EspStoreException e) {
					logger.error(e.getMessage());
				}
		}

		return successNum;
	}

	@Override
	public int syncBatchDelete(String resourceType, Set<String> uuidSet) {
		if (!ResourceTypeSupport.isValidEsResourceType(resourceType)
				|| CollectionUtils.isEmpty(uuidSet)) {
			return 0;
		}
		Set<Resource> resourceSet = new HashSet<Resource>();
		for (String uuid : uuidSet) {
			resourceSet.add(new Resource(resourceType, uuid));
		}
		return syncBatchDelete(resourceSet);
	}

	@Override
	public int syncBatchAddForTask(Set<Resource> resourceSet) {
		int successNum = 0;
		if (CollectionUtils.isEmpty(resourceSet)) {
			return successNum;
		}

		try {
			try {
				esSyncDao.batchBeforeUpdate(resourceSet);
			} catch (EspStoreException e) {
				logBatchMessages(resourceSet, e);
			}

			try {
				BulkResponse bulkResponse = esResourceOperation
						.batchAdd(resourceSet);

				Set<Resource> successSyncResourceSet = new HashSet<Resource>();
				if (bulkResponse != null && bulkResponse.getItems() != null
						&& bulkResponse.getItems().length != 0) {
					for (BulkItemResponse item : bulkResponse.getItems()) {
						if (item.isFailed()) {
							logForItem(item);
						} else {
							successSyncResourceSet.add(new Resource(item
									.getType(), item.getId()));
							successNum++;
						}
					}
				}
				if (CollectionUtils.isNotEmpty(successSyncResourceSet)) {
					try {
						esSyncDao.batchAfterUpdate(successSyncResourceSet);
					} catch (EspStoreException e) {
						logBatchMessages(successSyncResourceSet, e);
					}

				}

			} catch (IOException e) {
				logBatchMessages(resourceSet, e);
			}

		} catch (Exception e) {
			logBatchMessages(resourceSet, e);
		}
		return successNum;

	}

	private void logBatchMessages(Set<Resource> resourceSet, Exception e) {
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

	@Override
	public int syncBatchDeleteForTask(Set<Resource> resourceSet) {
		int successNum = 0;
		if (CollectionUtils.isEmpty(resourceSet)) {
			return successNum;
		}

		try {
			try {
				esSyncDao.batchBeforeDelete(resourceSet);
			} catch (EspStoreException e) {
				logBatchMessages(resourceSet, e);
			}

			BulkResponse bulkResponse = esResourceOperation
					.batchDelete(resourceSet);

			Set<Resource> successSyncResourceSet = new HashSet<Resource>();
			if (bulkResponse != null && bulkResponse.getItems() != null
					&& bulkResponse.getItems().length != 0) {
				for (BulkItemResponse item : bulkResponse.getItems()) {
					if (item.isFailed()) {
						logForItem(item);
					} else {
						successSyncResourceSet.add(new Resource(item.getType(),
								item.getId()));
						successNum++;
					}
				}
			}
			if (CollectionUtils.isNotEmpty(successSyncResourceSet)) {
				try {
					esSyncDao.batchAfterDelete(successSyncResourceSet);
				} catch (EspStoreException e) {
					logBatchMessages(successSyncResourceSet, e);
				}

			}

		} catch (Exception e) {
			logBatchMessages(resourceSet, e);
		}
		return successNum;
	}

}
