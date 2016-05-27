package nd.esp.service.lifecycle.services.task.v06.impls;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import nd.esp.service.lifecycle.entity.elasticsearch.Resource;
import nd.esp.service.lifecycle.repository.model.TaskStatusInfo;
import nd.esp.service.lifecycle.services.elasticsearch.AsynEsResourceService;
import nd.esp.service.lifecycle.services.offlinemetadata.OfflineService;
import nd.esp.service.lifecycle.services.task.v06.QueryTaskService;
import nd.esp.service.lifecycle.services.task.v06.TaskService;
import nd.esp.service.lifecycle.support.Constant;
import nd.esp.service.lifecycle.support.busi.PackageUtil;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.utils.UrlParamParseUtil;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class QueryTaskServiceImpl implements QueryTaskService {
    private final Logger LOG = LoggerFactory.getLogger(QueryTaskServiceImpl.class);
    
    public final int TASK_ID_PAGE_SIZE = 50;
    
    @Autowired
    private TaskService taskService;
    
    @Autowired
    private OfflineService offlineService;
    
    @Autowired
    private AsynEsResourceService esResourceOperation;
    
    @Override
    public void QueryTaskStatus(List<TaskStatusInfo> taskInfos) {
        String url = Constant.TASK_SUBMIT_URL.substring(0, Constant.TASK_SUBMIT_URL.lastIndexOf("/"))
                + "/get-executions-result?executionIds=";
        
        
        if(taskInfos!=null && taskInfos.size()>0) {
            int page=0;
            do {
                String ids = "";
                boolean bFirst = true;
                int toIndex = (page+1)*TASK_ID_PAGE_SIZE;
                if((page+1)*TASK_ID_PAGE_SIZE>taskInfos.size()) {
                    toIndex = taskInfos.size();
                }
                for(TaskStatusInfo info:taskInfos.subList(page*TASK_ID_PAGE_SIZE, toIndex)) {
                    //过期失效任务置为失败  (视频转码不处理)
                    long passTime = System.currentTimeMillis()-info.getUpdateTime().getTime();
                    if(passTime>Constant.FILE_OPERATION_EXPIRETIME*1000 && 
                            !(info.getBussType().equals("transcode")&&info.getResType().equals("assets"))) {
                        info.setStatus(PackageUtil.PackStatus.ERROR.getStatus());
                        info.setErrMsg("任务超时未完成");
                        try {
                            taskService.UpdateTaskInfo(info);
                        } catch (Exception e) {
                            LOG.error("处理超时任务失败：",e);
                        }
                    } else if(info.getTaskId() != null && !"NULL".equals(info.getTaskId())) {
                        if(!bFirst) {
                            ids += (","+info.getTaskId());
                        } else {
                            ids += info.getTaskId();
                            bFirst = false;
                        }
                    }
                }
                if(bFirst) {
                    continue;
                }
                //String url = "http://192.168.46.101:8080/task-server-webapp/concurrent/service/proxy/get-executions-result?executionIds=1,2";
                RestTemplate rest = new RestTemplate();
                ResponseEntity<String> response=null;
                try {
                    response = rest.getForEntity(url+ids, String.class);
                } catch (RestClientException e) {
                    LOG.error("调用获取任务执行结果失败：",e);
                } 
                if(null != response) {
                    String strResponse = response.getBody();
                    if(StringUtils.isNotEmpty(strResponse)) {
                        Map<String,List<Map<String,Object>>> result = ObjectUtils.fromJson(strResponse, Map.class);
                        for(Map<String,Object> excution:result.get("executions")) {
                            try {
                                BigDecimal bigDecimal=new BigDecimal(String.valueOf(excution.get("id")));
                                String taskId=String.valueOf(bigDecimal.longValue());
                                //只处理完成和失败的记录(也可以先过滤集合中的数据)
                                if("COMPLETED".equals(excution.get("status")) || "FAILED".equals(excution.get("status"))) {
                                    if(null == excution.get("result")) {
                                        taskService.DealInvalidTask(taskId, "未取得任务执行结果");
                                    } else {
                                        //任务完成的，触发LC的回调
                                        String rtJson = String.valueOf(excution.get("result"));
                                        Map<String,String> rtMap = ObjectUtils.fromJson(rtJson, Map.class);
                                        String callbackUrl = rtMap.get("callback");
                                        String argument = rtMap.get("argument");
                                        
                                        LOG.info("callbak="+callbackUrl+"; executionId="+taskId
                                                +"; argument="+argument); 
                                        
                                        Map<String,String> params = UrlParamParseUtil.URLRequest(callbackUrl);
                                        String resType = "";
                                        if(callbackUrl.contains("/packaging")) {
                                            resType = callbackUrl.replaceAll("(?i).*/(.+)/packaging.*", "$1");
                                        } else if(callbackUrl.contains("/transcode")){
                                            resType = callbackUrl.replaceAll("(?i).*/(.+)/transcode.*", "$1");
                                        }
                                        params.put("res_type", resType);
                                        taskService.FinishTask(taskId, params, argument);

										// 异步过程：同步元数据
										// bylsm 同步数据到elasticsearch
										// (与祁凌确认，都是callbackParams中的res_type,identifier)
                                        if(callbackUrl.contains("/transcode")){
                                            offlineService.writeToCsAsync(resType, params.get("identifier"));
                                        }
										esResourceOperation
												.asynAdd(new Resource(
														resType,
														params.get("identifier")));

									}
                                } else if("CANCELED".equals(excution.get("status"))) {
                                    taskService.DealInvalidTask(taskId, "任务已被取消");
                                }
                            } catch (Exception e) {
                                LOG.error("处理完结任务失败，task_id="+String.valueOf(excution.get("id")),e);
                            }
                        }
                    }
                    ++page;
                }
            }while(page*TASK_ID_PAGE_SIZE<taskInfos.size());
            
        }
    }


    @Override
    public void QueryAllRunningTaskStatus() {
        List<TaskStatusInfo> taskInfos = null;
        try {
            taskInfos = taskService.getAllRunningTask();
        } catch (Exception e) {
            LOG.error("获取执行中任务列表失败：",e);
        }
        
        QueryTaskStatus(taskInfos);
    }

}
