package nd.esp.service.lifecycle.services.notify;

import java.util.List;
import java.util.Map;

import nd.esp.service.lifecycle.repository.model.NotifyModel;
import nd.esp.service.lifecycle.repository.model.ResourceRelation;
import nd.esp.service.lifecycle.services.notify.models.NotifyInstructionalobjectivesRelationModel;
import nd.esp.service.lifecycle.support.enums.OperationType;

public interface NotifyInstructionalobjectivesService {
	
	/**
	 * 调用智能出题的通知接口
	 * @author xiezy
	 * @date 2016年4月21日
	 * @param list
	 * @param isTimingTask
	 * @return
	 */
	public List<NotifyModel> notifySmartq(List<NotifyModel> list,boolean isTimingTask);
	
	/**
	 * 教学目标 变更的异步通知
	 * @author xiezy
	 * @date 2016年4月18日
	 * @param resId
	 * @param nowStatus
	 * @param oldStatus
	 * @param operationType
	 */
	public void asynNotify4Resource(final String resId, final String nowStatus, final String oldStatus,
			final List<NotifyInstructionalobjectivesRelationModel> deleteRelations, final OperationType operationType);
	
	/**
	 * 当课时或章节变更 影响教学目标的异步通知
	 * @author xiezy
	 * @date 2016年4月19日
	 * @param lessonId
	 * @param deleteRelations
	 * @param operationType
	 */
	public void asynNotify4LessonOrChapter(final String type, final String id, final List<NotifyInstructionalobjectivesRelationModel> deleteRelations, 
			final OperationType operationType);
	
	/**
	 * 当覆盖范围变更 影响教学目标的异步通知
	 * @author xiezy
	 * @date 2016年4月20日
	 * @param map key为教学目标的id,value为教学目标 增删改 前是否有ND覆盖范围的Boolean值
	 */
	public void asynNotify4Coverage(Map<String, Boolean> map);
	
	/**
	 * 创建关系时影响教学目标的异步通知
	 * @author xiezy
	 * @date 2016年4月20日
	 * @param resType
	 * @param resId
	 * @param targetType
	 * @param tagetId
	 */
	public void asynNotify4Relation(String resType, final String resId,String targetType, final String tagetId);
	
	/**
	 * 删除关系时影响教学目标的异步通知
	 * @author xiezy
	 * @date 2016年4月21日
	 * @param relations
	 */
	public void asynNotify4RelationOnDelete(List<ResourceRelation> relations);
	
	/**
	 * 获取教学目标对应的status
	 * @author xiezy
	 * @date 2016年4月14日
	 * @param resId
	 * @return
	 */
	public String getResourceStatus(String resId);
	
	/**
	 * 查询教学目标相关联的章节id和课时id
	 * @author xiezy
	 * @date 2016年4月14日
	 * @param resId
	 * @return
	 */
	public List<NotifyInstructionalobjectivesRelationModel> resourceBelongToRelations(String resId);
	
	/**
	 * 已知课时id,查询相关联的章节id和教学目标id(教学目标id状态为ONLINE,enable=1)
	 * @author xiezy
	 * @date 2016年4月19日
	 * @return
	 */
	public List<NotifyInstructionalobjectivesRelationModel> resourceBelongToRelations4LessonOrChapter(String type,String id);
	
	/**
	 * 判断资源是否有ND库的覆盖范围
	 * @author xiezy
	 * @date 2016年4月13日
	 * @param resId
	 * @return
	 */
	public boolean resourceBelongToNDLibrary(String resId);
}
