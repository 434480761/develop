package nd.esp.service.lifecycle.support.categorysync;

import nd.esp.service.lifecycle.educommon.services.impl.CommonServiceHelper;
import nd.esp.service.lifecycle.support.enums.SynVariable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
/**
 * 维度数据同步 定时任务
 * @author xiezy
 * @date 2016年10月19日
 */
//@Component
public class CategorySyncTask {
	private final static Logger LOG= LoggerFactory.getLogger(CategorySyncTask.class);
	
	@Autowired
	private CommonServiceHelper commonServiceHelper;
	@Autowired
	private CategorySyncServiceHelper categorySyncServiceHelper;
	
	/**
	 * 执行定时任务 -- 每天凌晨1点开始跑
	 * @author xiezy
	 * @date 2016年10月19日
	 */
//	@Scheduled(cron="0 0 1 * * ?")
	public void runTask(){
		if(commonServiceHelper.queryAndUpdateSynVariable(SynVariable.categorySyncTask.getValue()) == 0){
			return;
		}
		LOG.info("维度数据同步开始...");
		
		categorySyncServiceHelper.syncCategory();
		
		LOG.info("维度数据同步结束...");
		commonServiceHelper.initSynVariable(SynVariable.icrsSyncTask.getValue());
	}
}
