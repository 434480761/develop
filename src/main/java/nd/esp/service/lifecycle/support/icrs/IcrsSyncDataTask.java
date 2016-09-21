package nd.esp.service.lifecycle.support.icrs;

import nd.esp.service.lifecycle.educommon.services.impl.CommonServiceHelper;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.support.enums.SynVariable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 智慧教室-课堂数据统计平台 定时同步数据任务
 * @author xiezy
 * @date 2016年9月18日
 */
@Component
public class IcrsSyncDataTask {
	private final static Logger LOG= LoggerFactory.getLogger(IcrsSyncDataTask.class);
	
	@Autowired
	private CommonServiceHelper commonServiceHelper;
	@Autowired
	private IcrsServiceHelper icrsServiceHelper;
	
	/**
	 * 执行定时任务 -- 50分钟跑一次,同步一个小时前更新的数据
	 * @param createTime
	 */
	@Scheduled(fixedRate=3000000)
	public void runTask(){
		if(commonServiceHelper.queryAndUpdateSynVariable(SynVariable.icrsSyncTask.getValue()) == 0){
			return;
		}
		LOG.info("ICRS同步数据开始...");
		
		icrsServiceHelper.syncIcrsByType(IndexSourceType.AssetType.getName(), false);
		icrsServiceHelper.syncIcrsByType(IndexSourceType.SourceCourseWareType.getName(), false);
		icrsServiceHelper.syncIcrsByType(IndexSourceType.SourceCourseWareObjectType.getName(), false);
		icrsServiceHelper.syncIcrsByType(IndexSourceType.QuestionType.getName(), false);
		
		LOG.info("ICRS同步数据结束...");
		commonServiceHelper.initSynVariable(SynVariable.icrsSyncTask.getValue());
	}
}
