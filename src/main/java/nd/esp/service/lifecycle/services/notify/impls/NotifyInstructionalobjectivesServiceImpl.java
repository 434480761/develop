package nd.esp.service.lifecycle.services.notify.impls;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import nd.esp.service.lifecycle.app.LifeCircleApplicationInitializer;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.NotifyModel;
import nd.esp.service.lifecycle.repository.model.ResourceRelation;
import nd.esp.service.lifecycle.repository.sdk.NotifyRepository;
import nd.esp.service.lifecycle.services.notify.NotifyInstructionalobjectivesService;
import nd.esp.service.lifecycle.services.notify.dao.NotifyInstructionalobjectivesDao;
import nd.esp.service.lifecycle.services.notify.models.NotifyInstructionalobjectivesRelationModel;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.support.enums.LifecycleStatus;
import nd.esp.service.lifecycle.support.enums.OperationType;
import nd.esp.service.lifecycle.utils.CollectionUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.nd.gaea.client.http.WafSecurityHttpClient;
import com.nd.gaea.util.WafJsonMapper;
/**
 * 教学目标通知服务 -- 对接智能出题
 * @author xiezy
 * @date 2016年4月13日
 */
@Service("NotifyService")
public class NotifyInstructionalobjectivesServiceImpl implements NotifyInstructionalobjectivesService{
	private static final Logger LOG = LoggerFactory.getLogger(NotifyInstructionalobjectivesServiceImpl.class);
	
	private final static ExecutorService executorService = CommonHelper.getPrimaryExecutorService();
	
	//状态
	private final static String STATUS_CREATED = "CREATED";
	private final static String STATUS_UPDATE = "UPDATE";
	private final static String STATUS_DELETE = "DELETE";
	
	@Autowired
	private NotifyRepository notifyRepository;
	@Autowired
	private NotifyInstructionalobjectivesDao notifydao;
	
	@Override
	public void asynNotify4Resource(final String resId, final String nowStatus, final String oldStatus,
			final List<NotifyInstructionalobjectivesRelationModel> deleteRelations, final OperationType operationType) {
		if(needToNotify(resId, nowStatus, oldStatus, operationType)){
			executorService.execute(new Runnable() {

				@Override
				public void run() {
					//需要通知的list
					List<NotifyModel> notifyList = new ArrayList<NotifyModel>();
					
					//查询教学目标对应title
					String title = notifydao.getResourceTitle(resId);
					
					//update时是否需要关心关系的东西
					boolean updateMeanCreated = 
							operationType==OperationType.UPDATE && (nowStatus==LifecycleStatus.ONLINE.getCode() && oldStatus!=LifecycleStatus.ONLINE.getCode());
					boolean updateMeanDelete = 
							operationType==OperationType.UPDATE && (nowStatus!=LifecycleStatus.ONLINE.getCode() && oldStatus==LifecycleStatus.ONLINE.getCode());
					
					//判断通知状态
					String notifyStatus = "";
					if(operationType==OperationType.CREATE || updateMeanCreated){
						notifyStatus = STATUS_CREATED;
					}else if(operationType==OperationType.DELETE || updateMeanDelete){
						notifyStatus = STATUS_DELETE;
					}else{
						notifyStatus = STATUS_UPDATE;
					}
					
					if(operationType==OperationType.CREATE 
							|| updateMeanCreated || updateMeanDelete){
						//先查相关关系
						List<NotifyInstructionalobjectivesRelationModel> relateRelations = notifydao.resourceBelongToRelations(resId);
						if(CollectionUtils.isNotEmpty(relateRelations)){
							for(NotifyInstructionalobjectivesRelationModel nrm : relateRelations){
								NotifyModel notifyModel = getNotifyModel(resId, title, nrm, notifyStatus);
								notifyList.add(notifyModel);
							}
						}else{
							NotifyModel notifyModel = getNotifyModel(resId, title, null, notifyStatus);
							notifyList.add(notifyModel);
						}
					}else if(operationType==OperationType.DELETE){
						if(CollectionUtils.isNotEmpty(deleteRelations)){
							for(NotifyInstructionalobjectivesRelationModel nrm : deleteRelations){
								NotifyModel notifyModel = getNotifyModel(resId, title, nrm, notifyStatus);
								notifyList.add(notifyModel);
							}
						}else{
							NotifyModel notifyModel = getNotifyModel(resId, title, null, notifyStatus);
							notifyList.add(notifyModel);
						}
					}else{
						NotifyModel notifyModel = getNotifyModel(resId, title, null, notifyStatus);
						notifyList.add(notifyModel);
					}
					
					//通知智能出题
					notifySmartq(notifyList,false);
				}
				
			});
		}
	}
	
	/**
	 * 判断是否需要通知
	 * @author xiezy
	 * @date 2016年4月13日
	 */
	private boolean needToNotify(String resId, String nowStatus, String oldStatus, 
			OperationType operationType){
		boolean statusMatchCondition = false;
		if(((operationType==OperationType.CREATE || operationType==OperationType.DELETE) && nowStatus.equals(LifecycleStatus.ONLINE.getCode())) ||
		   (operationType==OperationType.UPDATE && (nowStatus.equals(LifecycleStatus.ONLINE.getCode()) || oldStatus.equals(LifecycleStatus.ONLINE.getCode())))){
			
			statusMatchCondition = true;
		}
		
		if(statusMatchCondition && notifydao.resourceBelongToNDLibrary(resId)){
			return true;
		}
		return false;
	}
	
	/**
	 * 构造通知模型-NotifyModel
	 * @author xiezy
	 * @date 2016年4月14日
	 * @param resId
	 * @param title
	 * @param nrm
	 * @param status
	 * @return
	 */
	private NotifyModel getNotifyModel(String resId, String title, NotifyInstructionalobjectivesRelationModel nrm, String status){
		NotifyModel notifyModel = new NotifyModel();
		notifyModel.setIdentifier(UUID.randomUUID().toString());
		notifyModel.setTitle(title);
		notifyModel.setTeachingObjectId(resId);
		if(nrm!=null){
			notifyModel.setLessonPeriodId(nrm.getLessonId());
			notifyModel.setChapterId(nrm.getChapterId());
		}else {
			notifyModel.setLessonPeriodId("");
			notifyModel.setChapterId("");
		}
		notifyModel.setStatus(status);
		
		return notifyModel;
	}
	
	/**
	 * 调用智能出题notify接口
	 * @author xiezy
	 * @date 2016年4月14日
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<NotifyModel> notifySmartq(List<NotifyModel> list,boolean isTimingTask){
		List<NotifyModel> notifySuccessModels = new ArrayList<NotifyModel>();
		
		if(CollectionUtils.isNotEmpty(list)){
			WafSecurityHttpClient wafSecurityHttpClient = new WafSecurityHttpClient();
			
			StringBuilder url = new StringBuilder();
			url.append(LifeCircleApplicationInitializer.properties.getProperty("smartq.uri"));
			url.append("v0.1/api/teachingObjective/lc/notify");
			
			List<Map<String, Object>> requestBody = new ArrayList<Map<String,Object>>();
			try {
				requestBody = WafJsonMapper.parse(WafJsonMapper.toJson(list), List.class);
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			
			HttpHeaders httpHeaders=new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<List<Map<String, Object>>> entity = new HttpEntity<List<Map<String, Object>>>(requestBody, httpHeaders);
			
            boolean requestSuccess = true;
            
//			Map<String, Object> response = new HashMap<String, Object>();
			try {
				wafSecurityHttpClient.executeForObject(url.toString(), HttpMethod.POST, entity, Map.class);
			} catch (Exception e) {
				requestSuccess = false;
				if(!isTimingTask){
					//如果出错,将需要推送的记录先保存到数据库中
					try {
						notifyRepository.batchAdd(list);
					} catch (EspStoreException e1) {
						LOG.error("保存失败");
					}
				}
			}
			
			if(requestSuccess){
				notifySuccessModels.addAll(list);
			}
		}
		
		return notifySuccessModels;
	}
	
	@Override
	public void asynNotify4LessonOrChapter(final String type, final String id, final List<NotifyInstructionalobjectivesRelationModel> deleteRelations,
			final OperationType operationType) {
		//课时 只有当【增删】时才会对教学目标造成影响(主要是关系的影响),从而进一步判断是否需要通知
		List<NotifyInstructionalobjectivesRelationModel> relateRelations = new ArrayList<NotifyInstructionalobjectivesRelationModel>();
		if(operationType==OperationType.CREATE){
			relateRelations = notifydao.resourceBelongToRelations4LessonOrChapter(type,id);
			if(CollectionUtils.isNotEmpty(relateRelations)){
				for(NotifyInstructionalobjectivesRelationModel nrm : relateRelations){
					asynNotify4Resource(nrm.getInstructionalobjectiveId(), LifecycleStatus.ONLINE.getCode(),
							null, null, operationType);
				}
			}
		}else if (operationType==OperationType.DELETE) {
			if(CollectionUtils.isNotEmpty(deleteRelations)){
				//根据教学目标id分组
				Map<String, List<NotifyInstructionalobjectivesRelationModel>> map = CommonHelper.newHashMap();
				for(NotifyInstructionalobjectivesRelationModel nrm : deleteRelations){
					if(!map.containsKey(nrm.getInstructionalobjectiveId())){
						List<NotifyInstructionalobjectivesRelationModel> list = new ArrayList<NotifyInstructionalobjectivesRelationModel>();
						list.add(nrm);
						map.put(nrm.getInstructionalobjectiveId(), list);
					}else{
						map.get(nrm.getInstructionalobjectiveId()).add(nrm);
					}
				}
				
				if(CollectionUtils.isNotEmpty(map)){
					for(String key : map.keySet()){
						asynNotify4Resource(key, LifecycleStatus.ONLINE.getCode(), 
								null, map.get(key), operationType);
					}
				}
			}
		}
	}
	
	@Override
	public void asynNotify4Coverage(Map<String, Boolean> map) {
		final Map<String, String> needNotifyMap = new HashMap<String, String>();
		if(CollectionUtils.isNotEmpty(map)){
			for(String key : map.keySet()){
				if(notifydao.getResourceStatus(key).equals(LifecycleStatus.ONLINE.getCode())){
					boolean haveNdCoverage = notifydao.resourceBelongToNDLibrary(key);
					if(haveNdCoverage != map.get(key)){//需要通知
						if(haveNdCoverage){
							needNotifyMap.put(key, STATUS_CREATED);
						}else{
							needNotifyMap.put(key, STATUS_DELETE);
						}
					}
				}
			}
		}
		
		if(CollectionUtils.isNotEmpty(needNotifyMap)){
			executorService.execute(new Runnable() {

				@Override
				public void run() {
					//需要通知的list
					List<NotifyModel> notifyList = new ArrayList<NotifyModel>();
					
					for(String resId : needNotifyMap.keySet()){
						String title = notifydao.getResourceTitle(resId);
						
						//先查相关关系
						List<NotifyInstructionalobjectivesRelationModel> relateRelations = notifydao.resourceBelongToRelations(resId);
						if(CollectionUtils.isNotEmpty(relateRelations)){
							for(NotifyInstructionalobjectivesRelationModel nrm : relateRelations){
								NotifyModel notifyModel = getNotifyModel(resId, title, nrm, needNotifyMap.get(resId));
								notifyList.add(notifyModel);
							}
						}else{
							NotifyModel notifyModel = getNotifyModel(resId, title, null, needNotifyMap.get(resId));
							notifyList.add(notifyModel);
						}
					}
					
					//通知智能出题
					notifySmartq(notifyList,false);
				}
				
			});
		}
	}
	
	@Override
	public void asynNotify4Relation(String resType, final String resId,String targetType, final String tagetId) {
		if(resType.equals(IndexSourceType.LessonType.getName()) 
				&& targetType.equals(IndexSourceType.InstructionalObjectiveType.getName())){
			executorService.execute(new Runnable() {

				@Override
				public void run() {
					String status = notifydao.getResourceStatus(tagetId);
					if(status.equals(LifecycleStatus.ONLINE.getCode()) 
							&& notifydao.resourceBelongToNDLibrary(tagetId)){
						//需要通知的list
						List<NotifyModel> notifyList = new ArrayList<NotifyModel>();
						
						String title = notifydao.getResourceTitle(tagetId);
						
						//查询相关的章节
						Set<String> chapterIds = notifydao.resourceBelongToRelation4ChapterIds(tagetId, resId);
						if(CollectionUtils.isNotEmpty(chapterIds)){
							for(String chapterId : chapterIds){
								NotifyInstructionalobjectivesRelationModel nrm = new NotifyInstructionalobjectivesRelationModel();
								nrm.setChapterId(chapterId);
								nrm.setLessonId(resId);
								
								NotifyModel notifyModel = getNotifyModel(tagetId, title, nrm, STATUS_CREATED);
								notifyList.add(notifyModel);
							}
						}
						
						//通知智能出题
						notifySmartq(notifyList,false);
					}
				}
				
			});
		}else if(resType.equals(IndexSourceType.ChapterType.getName()) 
				&& targetType.equals(IndexSourceType.LessonType.getName())){
			executorService.execute(new Runnable() {

				@Override
				public void run() {
					//需要通知的list
					List<NotifyModel> notifyList = new ArrayList<NotifyModel>();
					
					//查询相关的教学目标
					Set<String> ioIds = notifydao.resourceBelongToRelation4instructionalobjectives(resId, tagetId);
					if(CollectionUtils.isNotEmpty(ioIds)){
						NotifyInstructionalobjectivesRelationModel nrm = new NotifyInstructionalobjectivesRelationModel();
						nrm.setChapterId(resId);
						nrm.setLessonId(tagetId);
						
						for(String ioId : ioIds){
							if(notifydao.resourceBelongToNDLibrary(ioId)){
								String title = notifydao.getResourceTitle(ioId);
								
								NotifyModel notifyModel = getNotifyModel(ioId, title, nrm, STATUS_CREATED);
								notifyList.add(notifyModel);
							}
						}
					}
					
					//通知智能出题
					notifySmartq(notifyList,false);
				}
				
			});
		}
	}
	
	@Override
	public void asynNotify4RelationOnDelete(List<ResourceRelation> relations) {
		final List<ResourceRelation> lessonToInstructionalobjectives = 
				new ArrayList<ResourceRelation>();
		final List<ResourceRelation> chapterToLesson = new ArrayList<ResourceRelation>();
		
		if(CollectionUtils.isNotEmpty(relations)){
			for(ResourceRelation relation : relations){
				if(relation.getResType().equals(IndexSourceType.LessonType.getName())
						&& relation.getResourceTargetType().equals(IndexSourceType.InstructionalObjectiveType.getName())){
					lessonToInstructionalobjectives.add(relation);
				}else if(relation.getResType().equals(IndexSourceType.ChapterType.getName())
						&& relation.getResourceTargetType().equals(IndexSourceType.LessonType.getName())){
					chapterToLesson.add(relation);
				}
			}
		}
		
		if(CollectionUtils.isNotEmpty(lessonToInstructionalobjectives)
				|| CollectionUtils.isNotEmpty(chapterToLesson)){
			executorService.execute(new Runnable() {

				@Override
				public void run() {
					//需要通知的list
					List<NotifyModel> notifyList = new ArrayList<NotifyModel>();
					
					if(CollectionUtils.isNotEmpty(lessonToInstructionalobjectives)){
						for(ResourceRelation rr : lessonToInstructionalobjectives){
							String status = notifydao.getResourceStatus(rr.getTarget());
							if(needToNotify(rr.getTarget(), status, null, OperationType.DELETE)){
								String title = notifydao.getResourceTitle(rr.getTarget());
								
								Set<String> chapterIds = 
										notifydao.resourceBelongToRelation4ChapterIdsByRid(rr.getIdentifier());
								if(CollectionUtils.isNotEmpty(chapterIds)){
									for(String chapterId : chapterIds){
										NotifyInstructionalobjectivesRelationModel nrm = new NotifyInstructionalobjectivesRelationModel();
										nrm.setChapterId(chapterId);
										nrm.setLessonId(rr.getSourceUuid());
										
										NotifyModel notifyModel = getNotifyModel(rr.getTarget(), title, nrm, STATUS_DELETE);
										notifyList.add(notifyModel);
									}
								}
							}
						}
					}
					
					if(CollectionUtils.isNotEmpty(chapterToLesson)){
						for(ResourceRelation rr : chapterToLesson){
							Set<String> ioIds = 
									notifydao.resourceBelongToRelation4instructionalobjectivesByRid(rr.getIdentifier());
							if(CollectionUtils.isNotEmpty(ioIds)){
								NotifyInstructionalobjectivesRelationModel nrm = new NotifyInstructionalobjectivesRelationModel();
								nrm.setChapterId(rr.getSourceUuid());
								nrm.setLessonId(rr.getTarget());
								
								for(String ioId : ioIds){
									if(notifydao.resourceBelongToNDLibrary(ioId)){
										String title = notifydao.getResourceTitle(ioId);
										
										NotifyModel notifyModel = getNotifyModel(ioId, title, nrm, STATUS_DELETE);
										notifyList.add(notifyModel);
									}
								}
							}
						}
					}
					
					//通知智能出题
					notifySmartq(notifyList,false);
				}
				
			});
		}
	}
	
	@Override
	public String getResourceStatus(String resId) {
		return notifydao.getResourceStatus(resId);
	}

	@Override
	public List<NotifyInstructionalobjectivesRelationModel> resourceBelongToRelations(String resId) {
		return notifydao.resourceBelongToRelations(resId);
	}

	@Override
	public List<NotifyInstructionalobjectivesRelationModel> resourceBelongToRelations4LessonOrChapter(String type,String id) {
		return notifydao.resourceBelongToRelations4LessonOrChapter(type,id);
	}

	@Override
	public boolean resourceBelongToNDLibrary(String resId) {
		return notifydao.resourceBelongToNDLibrary(resId);
	}
}
