package nd.esp.service.lifecycle.services.notify.task;

import java.util.ArrayList;
import java.util.List;

import nd.esp.service.lifecycle.educommon.services.impl.CommonServiceHelper;
import nd.esp.service.lifecycle.repository.model.NotifyModel;
import nd.esp.service.lifecycle.repository.sdk.NotifyRepository;
import nd.esp.service.lifecycle.services.notify.NotifyInstructionalobjectivesService;
import nd.esp.service.lifecycle.support.enums.SynVariable;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时发送通知请求 
 * @author xiezy
 * @date 2016年4月21日
 */
@Component
public class TimingNotifyInstructionalobjectivesTask {
	@Autowired
	private CommonServiceHelper commonServiceHelper;
	
	@Qualifier(value="defaultJdbcTemplate")
	@Autowired
	private JdbcTemplate defaultJdbcTemplate;
	
	@Autowired
	private NotifyRepository notifyRepository;
	
	@Autowired
	private NotifyInstructionalobjectivesService notifyService;
	
//	@Scheduled(fixedRate=60000)
	@Scheduled(cron="0 0 4 * * ?")
	public void notifyTask(){
		if(commonServiceHelper.queryAndUpdateSynVariable(SynVariable.notifTask.getValue()) == 0){
			return;
		}
		
		//查询需要通知的
		List<NotifyModel> notifyList =  notifyRepository.findAll();
		if(CollectionUtils.isNotEmpty(notifyList)){
			//通知
			List<NotifyModel> successNotifyModels = notifyService.notifySmartq(notifyList, true);
			
			if(CollectionUtils.isNotEmpty(successNotifyModels)){
				List<String> deleteIds = new ArrayList<String>();
				for(NotifyModel nrm : successNotifyModels){
					deleteIds.add(nrm.getIdentifier());
				}
				
				//删除
				if(CollectionUtils.isNotEmpty(deleteIds)){
					deleteNotifyModelAfterSuccess(deleteIds);
				}
			}
		}
		
		commonServiceHelper.initSynVariable(SynVariable.notifTask.getValue());
	}
	
	/**
	 * 删除记录
	 * @author xiezy
	 * @date 2016年4月21日
	 * @param ids
	 */
	private void deleteNotifyModelAfterSuccess(List<String> ids){
		String sql = "DELETE FROM notify_backups WHERE identifier IN ('" + StringUtils.join(ids, "','") + "')";
		defaultJdbcTemplate.execute(sql);
	}
}
