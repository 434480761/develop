package nd.esp.service.lifecycle.support.busi.elasticsearch;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nd.esp.service.lifecycle.educommon.services.impl.CommonServiceHelper;
import nd.esp.service.lifecycle.entity.elasticsearch.Resource;
import nd.esp.service.lifecycle.services.elasticsearch.SyncResourceService;
import nd.esp.service.lifecycle.support.enums.SynVariable;
import nd.esp.service.lifecycle.utils.CollectionUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EsSyncTimerTask {

	private static final int MAX_TRY_TIMES = 5;

	@Autowired
	private SyncResourceService syncResourceService;
	@Autowired
	private CommonServiceHelper commonServiceHelper;

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

		syncResourceService.syncBatchAddForTask(queryResources(true));
		syncResourceService.syncBatchDeleteForTask(queryResources(false));

		commonServiceHelper.initSynVariable(SynVariable.esSynTask.getValue());
	}

	/**
	 * @return
	 */
	private Set<Resource> queryResources(boolean isUpdate) {
		StringBuffer stringBuffer = new StringBuffer(
				"select resource,primary_category from es_sync ");
		stringBuffer.append("where try_times<");
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
