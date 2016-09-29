package nd.esp.service.lifecycle.controllers.taskcallback.v06;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Query;

import nd.esp.service.lifecycle.entity.elasticsearch.Resource;
import nd.esp.service.lifecycle.repository.model.TaskStatusInfo;
import nd.esp.service.lifecycle.repository.sdk.TaskStatusInfoRepository;
import nd.esp.service.lifecycle.services.elasticsearch.AsynEsResourceService;
import nd.esp.service.lifecycle.services.offlinemetadata.OfflineService;
import nd.esp.service.lifecycle.services.task.v06.TaskService;
import nd.esp.service.lifecycle.services.task.v06.TranscodeCallbackService;
import nd.esp.service.lifecycle.services.titan.TitanSyncService;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.utils.BeanMapperUtils;
import nd.esp.service.lifecycle.utils.MessageConvertUtil;
import nd.esp.service.lifecycle.utils.StringUtils;

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
@RequestMapping("/{version}/{res_type}/transcode")
public class TranscodeCallbackController {
    private static final Logger LOG = LoggerFactory.getLogger(TranscodeCallbackController.class);
    
    
    @Autowired
    private TaskStatusInfoRepository taskRepository;
    
    @Autowired
    private TaskService taskService;
    
    @Autowired
    private TranscodeCallbackService transcodeCallbackService;
    
    @Autowired
    private AsynEsResourceService esResourceOperation;
    
    @Autowired
    private OfflineService offlineService;

    @Autowired
    private TitanSyncService titanSyncService;
    
    /**
     * 课件转码回调接口
     * 
     * @param ids
     * @return
     * @since
     */
    @RequestMapping(value = "/callback", params = { "identifier", "status", "xml_path" }, method ={RequestMethod.POST}, produces = { MediaType.APPLICATION_JSON_VALUE })
    @ResponseBody
    Map<String, String> normalTranscodCallback( @RequestBody String requestBody, 
            @PathVariable String version, @PathVariable String res_type,
            @RequestParam(value = "identifier", required = true) String id, 
            @RequestParam(value = "status", required = true) int status, 
            @RequestParam(value = "xml_path", required = true) String xml_path) throws IOException{
        String taskId = null;
        Map<String,Object> m=BeanMapperUtils.mapperOnString(requestBody, Map.class);
        if(m.get("executionId") != null){
            LOG.info("回调的map中executionId的值:"+m.get("executionId"));
            taskId = String.valueOf(m.get("executionId"));
        }
        
        TaskStatusInfo taskInfo = null;
        try {
            Query query = taskRepository.getEntityManager().createNamedQuery("queryByTaskId");
            query.setParameter("taskid", taskId);
            taskInfo = (TaskStatusInfo) query.getSingleResult();
        } catch (Exception e1) {
            LOG.error("未在任务表中找到任务id: "+taskId);
        }
        
        if(null == taskInfo) {
            LOG.info("回调的任务："+taskId+"已取消或不在任务表");
            return MessageConvertUtil.getMessageString(LifeCircleErrorMessageMapper.ConvertCallbackSuccess);
        }
        
        Map<String, String> callbackParams = new HashMap<String,String>();
        callbackParams.put("identifier", id);
        callbackParams.put("status", String.valueOf(status));
        callbackParams.put("xml_path", xml_path);
        taskService.FinishTask(taskId, callbackParams, "");
        
        //异步过程：同步元数据
        offlineService.writeToCsAsync(res_type, id);
        esResourceOperation.asynAdd(new Resource(res_type, id));

        titanSyncService.syncEducation(res_type,id);

        return MessageConvertUtil.getMessageString(LifeCircleErrorMessageMapper.ConvertCallbackSuccess);
    }

    /**
     * 图片转码回调接口,更新lcms task 表， 资源元数据
     *
     * @author qil
     * @return
     * @since
     */
    @RequestMapping(value = "/image_callback", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
    @ResponseBody
    Map<String, String> imageTranscodeCallback(@RequestBody String requestBody,
                                               @PathVariable String version,
                                               @PathVariable String res_type,
                                               @RequestParam(value = "identifier", required = true) String id) throws IOException {

        String taskId = null;
        Map<String,Object> m=BeanMapperUtils.mapperOnString(requestBody, Map.class);
        if(m.get("executionId") != null){
            LOG.info("回调的map中executionId的值:"+m.get("executionId"));
            taskId = String.valueOf(m.get("executionId"));
        }

        Query query = taskRepository.getEntityManager().createNamedQuery("queryByTaskId");
        query.setParameter("taskid", taskId);
        TaskStatusInfo taskInfo = (TaskStatusInfo) query.getSingleResult();

        if(null == taskInfo) {
            LOG.info("回调的任务："+taskId+"已取消或不在任务表");
            return MessageConvertUtil.getMessageString(LifeCircleErrorMessageMapper.ConvertCallbackSuccess);
        }

        if(StringUtils.isNotEmpty(requestBody)) {
            taskService.FinishTask(taskId, new HashMap<String,String>(), requestBody);

            //异步过程：同步元数据
            offlineService.writeToCsAsync(res_type, id);
            esResourceOperation.asynAdd(new Resource(res_type, id));
            titanSyncService.syncEducation(res_type,id);
        }

        return MessageConvertUtil.getMessageString(LifeCircleErrorMessageMapper.ConvertCallbackSuccess);
    }
    
    /**
     * 视频转码回调接口,更新lcms task 表， 资源元数据（仅支持assets)( preview, techinfo, 包含requirement（文件元数据））
     * 
     * @author linsm
     * @param ids
     * @return
     * @since
     */
    @RequestMapping(value = "/videoCallback", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
    @ResponseBody
    Map<String, String> videoTranscodeCallback(@PathVariable String version,
                                               @PathVariable String res_type,
                                               @RequestParam(value = "identifier", required = true) String id,
                                               @RequestBody Map<String,Object> body) throws IOException {

        String taskId = null;
        if(body.get("executionId") != null){
            LOG.info("回调的map中executionId的值:"+body.get("executionId"));
            taskId = String.valueOf(body.get("executionId"));
        }
        
        Query query = taskRepository.getEntityManager().createNamedQuery("queryByTaskId");
        query.setParameter("taskid", taskId);
        TaskStatusInfo taskInfo = (TaskStatusInfo) query.getSingleResult();
        
        if(null == taskInfo) {
            LOG.info("回调的任务："+taskId+"已取消或不在任务表");
            return MessageConvertUtil.getMessageString(LifeCircleErrorMessageMapper.ConvertCallbackSuccess);
        }
        
        String argument = String.valueOf(body.get("argument"));
        if(StringUtils.isNotEmpty(argument)) {
            taskService.FinishTask(taskId, new HashMap<String,String>(), argument);
            
            //异步过程：同步元数据
            offlineService.writeToCsAsync(res_type, id);
            esResourceOperation.asynAdd(new Resource(res_type, id));
            titanSyncService.syncEducation(res_type,id);
        }

        return MessageConvertUtil.getMessageString(LifeCircleErrorMessageMapper.ConvertCallbackSuccess);
    }

    /**
     * 图片转码回调接口,更新lcms task 表， 资源元数据
     *
     * @author qil
     * @return
     * @since
     */
    @RequestMapping(value = "/document_callback", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
    @ResponseBody
    Map<String, String> documnetTranscodeCallback(@PathVariable String version,
                                                  @PathVariable String res_type,
                                                  @RequestParam(value = "identifier", required = true) String id,
                                                  @RequestBody Map<String,Object> body) throws IOException {

        String taskId = null;
        if(body.get("executionId") != null){
            LOG.info("回调的map中executionId的值:"+body.get("executionId"));
            taskId = String.valueOf(body.get("executionId"));
        }

        Query query = taskRepository.getEntityManager().createNamedQuery("queryByTaskId");
        query.setParameter("taskid", taskId);
        TaskStatusInfo taskInfo = (TaskStatusInfo) query.getSingleResult();

        if(null == taskInfo) {
            LOG.info("回调的任务："+taskId+"已取消或不在任务表");
            return MessageConvertUtil.getMessageString(LifeCircleErrorMessageMapper.ConvertCallbackSuccess);
        }

        String argument = String.valueOf(body.get("argument"));
        if(StringUtils.isNotEmpty(argument)) {
            taskService.FinishTask(taskId, new HashMap<String,String>(), argument);

            //异步过程：同步元数据
            offlineService.writeToCsAsync(res_type, id);
            esResourceOperation.asynAdd(new Resource(res_type, id));
            titanSyncService.syncEducation(res_type,id);
        }

        return MessageConvertUtil.getMessageString(LifeCircleErrorMessageMapper.ConvertCallbackSuccess);
    }

}
