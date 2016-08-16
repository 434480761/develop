package nd.esp.service.lifecycle.support.busi.elasticsearch;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nd.esp.service.lifecycle.daos.elasticsearch.EsSyncDao;
import nd.esp.service.lifecycle.educommon.services.NDResourceService;
import nd.esp.service.lifecycle.educommon.services.impl.CommonServiceHelper;
import nd.esp.service.lifecycle.educommon.services.impl.NDResourceServiceImpl;
import nd.esp.service.lifecycle.entity.elasticsearch.Resource;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.services.elasticsearch.SyncResourceService;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.enums.SynVariable;
import nd.esp.service.lifecycle.utils.CollectionUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EsSyncTimerTask {

	public static final int MAX_TRY_TIMES = 5;
	private static final Logger LOG = LoggerFactory.getLogger(EsSyncTimerTask.class);

	@Autowired
	private SyncResourceService syncResourceService;
	@Autowired
	private CommonServiceHelper commonServiceHelper;
	@Autowired
	private NDResourceService ndresourceService;
	
	@Autowired
	private EsSyncDao esSyncDao;

	@Autowired
	@Qualifier(value = "defaultJdbcTemplate")
	private JdbcTemplate jdbcTemplate;

	// cron="0/15 * *  * * ? "
	@Scheduled(fixedDelay = 10000)
	public void runQuery() {
		if (commonServiceHelper.queryAndUpdateSynVariable(SynVariable.esSynTask
				.getValue()) == 0) {
			return;
		}

		syncResourceService.syncBatchAddForTask(queryResourcesForSyncTask(true));
		syncResourceService.syncBatchDeleteForTask(queryResourcesForSyncTask(false));

		commonServiceHelper.initSynVariable(SynVariable.esSynTask.getValue());
	}
	
	/**
	 * 用于清理已经不存在于MySQL中的数据（已经被清理脏数据的接口处理了）
	 */
	@Scheduled(fixedDelay = 10000)
	public void runQueryForCleanES_SYNC() {
		if (commonServiceHelper.queryAndUpdateSynVariable(SynVariable.esSynTask
				.getValue()) == 0) {
			return;
		}
		try {
			Set<Resource> oldResourceForUpdate = queryResources(true, false);
			if (CollectionUtils.isNotEmpty(oldResourceForUpdate)) {
				Set<Resource> resourceForClean = new HashSet<Resource>();
				for (Resource resource : oldResourceForUpdate) {
					if (resource != null) {
						try {
							NDResourceServiceImpl.checkResourceExist(
									commonServiceHelper,
									resource.getResourceType(),
									resource.getIdentifier(), true);
						} catch (LifeCircleException e) {
							resourceForClean.add(resource);
						}
					}
				}
				if (CollectionUtils.isNotEmpty(resourceForClean)) {
					try {
						esSyncDao.batchAfterUpdate(resourceForClean);
					} catch (EspStoreException e) {
						LOG.error(e.getLocalizedMessage());
					}
				}

			}
		} catch (Exception e) {
			LOG.error(e.getLocalizedMessage());
		}

		commonServiceHelper.initSynVariable(SynVariable.esSynTask.getValue());
	}
	
	
	

	/**
	 * @param isUpdate 是否更新操作
	 * @return
	 */
	private Set<Resource> queryResourcesForSyncTask(boolean isUpdate) {
		return queryResources(isUpdate, true);
	}
	
	/**
	 * 用于查询 es-sync表中的数据
	 * @param isUpdate 是否更新操作
	 * @param isForSyncTask 是否用于同步数据
	 * @return
	 */
	private Set<Resource> queryResources(boolean isUpdate,boolean isForSyncTask) {
		StringBuffer stringBuffer = new StringBuffer(
				"select resource,primary_category from es_sync ");
		stringBuffer.append("where try_times");
		if(isForSyncTask){
			stringBuffer.append("<");
		}else{
			stringBuffer.append(">=");
		}
		stringBuffer.append(MAX_TRY_TIMES);
		stringBuffer.append(" and enable =1");
		stringBuffer.append(" and sync_type=");
		if (isUpdate) {
			stringBuffer.append(1);
		} else {
			stringBuffer.append(0);
		}
		stringBuffer.append(" order by try_times asc limit 0,11");
		List<Map<String, Object>> returnList = null;
		returnList = jdbcTemplate.queryForList(stringBuffer.toString());
		Set<Resource> set = new HashSet<Resource>();
		if (CollectionUtils.isNotEmpty(returnList)) {

			for (Map<String, Object> map : returnList) {
				set.add(new Resource((String) map.get("primary_category"),
						(String) map.get("resource")));
			}

		}
		return set;
	}

}
