package nd.esp.service.lifecycle.controllers.taskcallback.v06;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Query;

import nd.esp.service.lifecycle.entity.elasticsearch.Resource;
import nd.esp.service.lifecycle.repository.model.TaskStatusInfo;
import nd.esp.service.lifecycle.repository.sdk.TaskStatusInfoRepository;
import nd.esp.service.lifecycle.services.elasticsearch.AsynEsResourceService;
import nd.esp.service.lifecycle.services.task.v06.PackCallbackService;
import nd.esp.service.lifecycle.services.task.v06.TaskService;
import nd.esp.service.lifecycle.services.titan.TitanSyncService;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.utils.BeanMapperUtils;
import nd.esp.service.lifecycle.utils.MessageConvertUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/* @author qil
*  @version 1.0
*  @created 2015.5.28 15:06:08
*/

@RestController
@RequestMapping("/v0.6/{res_type}/packaging")
public class PackCallbackController {
    private static final Logger LOG = LoggerFactory.getLogger(PackCallbackController.class);
    
    
    @Autowired
    TaskStatusInfoRepository taskRepository;
    
    @Autowired
    private PackCallbackService packCallbackService;
    
    @Autowired
    private TaskService taskService;
    
    @Autowired
    private AsynEsResourceService esResourceOperation;

    @Autowired
    private TitanSyncService titanSyncService;
    
    /**
     * 课件转码回调接口
     * 
     * @param ids
     * @return
     * @since
     */
    @RequestMapping(value = "/callback", method ={RequestMethod.POST}, produces = { MediaType.APPLICATION_JSON_VALUE })
    @ResponseBody
    Map<String, String> packCallback( @PathVariable String res_type,
            @RequestBody String requestBody,
            @RequestParam(value = "identifier", required = true) String id, 
            @RequestParam(value = "target", required = false) String target, 
            @RequestParam(value = "icplayer", required = true) String icplayer, 
            @RequestParam(value = "status", required = true) String status, 
            @RequestParam(value = "pack_info", required = false) String pack_info,
            @RequestParam(value = "err_msg", required = false) String err_msg, 
            @RequestParam(value = "webp_first", required = false) boolean webp_first) throws IOException{
        
        String task_id = null;
        Map<String,Object> m=BeanMapperUtils.mapperOnString(requestBody, Map.class);
        if(m.get("executionId") != null){
            LOG.info("回调的map中executionId的值:"+m.get("executionId"));
            task_id = String.valueOf(m.get("executionId"));
        }
        
        TaskStatusInfo taskInfo = null;
        try {
            Query query = taskRepository.getEntityManager().createNamedQuery("queryByTaskId");
            query.setParameter("taskid", task_id);
            taskInfo = (TaskStatusInfo) query.getSingleResult();
        } catch (Exception e1) {
            LOG.error("未在任务表中找到任务id: "+task_id);
        }
        
        if(null == taskInfo) {
            LOG.info("回调的任务："+task_id+"已取消或不在任务表");
            return MessageConvertUtil.getMessageString(LifeCircleErrorMessageMapper.ConvertCallbackSuccess);
        }
        
        
        Map<String, String> callbackParams = new HashMap<String,String>();
        callbackParams.put("identifier", id);
        callbackParams.put("target", target);
        callbackParams.put("status", status);
        callbackParams.put("pack_info", pack_info);
        callbackParams.put("err_msg", err_msg);
        callbackParams.put("webp_first", String.valueOf(webp_first));
        callbackParams.put("res_type", res_type);
        
        taskService.FinishTask(task_id, callbackParams, "");
        
        //异步过程：同步元数据
        //bylsm 同步数据到elasticsearch (与祁凌确认，都是callbackParams中的res_type,identifier)
        esResourceOperation.asynAdd(new Resource(res_type, id));


        titanSyncService.syncEducation(res_type,id);

        return MessageConvertUtil.getMessageString(LifeCircleErrorMessageMapper.ConvertCallbackSuccess);
    }
    
}

