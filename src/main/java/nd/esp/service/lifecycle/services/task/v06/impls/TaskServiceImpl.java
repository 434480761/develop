package nd.esp.service.lifecycle.services.task.v06.impls;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.LockModeType;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import nd.esp.service.lifecycle.repository.ds.ComparsionOperator;
import nd.esp.service.lifecycle.repository.ds.Item;
import nd.esp.service.lifecycle.repository.ds.LogicalOperator;
import nd.esp.service.lifecycle.repository.ds.ValueUtils;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.TaskStatusInfo;
import nd.esp.service.lifecycle.repository.sdk.TaskStatusInfoRepository;
import nd.esp.service.lifecycle.entity.TransCodeCallBackParam;
import nd.esp.service.lifecycle.services.task.v06.PackCallbackService;
import nd.esp.service.lifecycle.services.task.v06.TaskService;
import nd.esp.service.lifecycle.services.task.v06.TranscodeCallbackService;
import nd.esp.service.lifecycle.support.busi.PackageUtil;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;

@Service
public class TaskServiceImpl implements TaskService {
    
    private final Logger LOG = LoggerFactory.getLogger(TaskServiceImpl.class);
    
    public static final String TASK_BUSS_TYPE_PACK = "packaging";
    
    public static final String TASK_BUSS_TYPE_TRANSCODE = "transcode";

    public static final String TASK_BUSS_TYPE_IMAGE_TRANSCODE = "image_transcode";
    
    public static final String TASK_STATUS_FIELD="status";
    
    public static final int MAX_QUERY_ITEMS = 1000000;
    
    @Autowired
    private TaskStatusInfoRepository taskRepository;
    
    @Autowired
    private PackCallbackService packCallbackService;
    
    @Autowired
    private TranscodeCallbackService transcodeCallbackService;


    @Override
    public List<TaskStatusInfo> getAllRunningTask() {
        TaskStatusInfo example = new TaskStatusInfo();
        example.setStatus(PackageUtil.PackStatus.PENDING.getStatus());
        
        List<Item<? extends Object>> items = new ArrayList<>();
        Item<String> item = new Item<String>();
        item.setKey(TASK_STATUS_FIELD);
        item.setComparsionOperator(ComparsionOperator.EQ);
        item.setLogicalOperator(LogicalOperator.AND);
        item.setValue(ValueUtils.newValue(PackageUtil.PackStatus.PENDING.getStatus()));
        items.add(item);
        
        Page<TaskStatusInfo> entityPage = null;
        int page = 0;
        int rows = MAX_QUERY_ITEMS;
        Pageable pageable = new PageRequest(page, rows, Direction.ASC, "priority", "updateTime");
        
        try {
            entityPage = taskRepository.findByItems(items, pageable);
            return entityPage.getContent();
        } catch (EspStoreException e) {
            LOG.error("获取执行中任务失败",e);
        }
        return null;
    }

    @Override
    public List<TaskStatusInfo> getAllFailTask() {
        TaskStatusInfo example = new TaskStatusInfo();
        example.setStatus(PackageUtil.PackStatus.ERROR.getStatus());
        
        try {
            List<TaskStatusInfo> taskInfos = taskRepository.getAllByExample(example);
            return taskInfos;
        } catch (EspStoreException e) {
            LOG.error("获取错误任务失败",e);
        }
        return null;
    }

    @Override
    @Transactional
    public String CreateOrRestartTask(TaskStatusInfo newTaskInfo) {
        String oldTaskId = null;
        TaskStatusInfo taskInfo = null;
        try {
            taskInfo = taskRepository.get(newTaskInfo.getBussId());
        } catch (EspStoreException e) {
            LOG.error("获取任务信息失败",e);
        }
        if (taskInfo != null) {
            oldTaskId = taskInfo.getTaskId();
            taskInfo.setStatus(PackageUtil.PackStatus.PENDING.getStatus());
        } else {
            taskInfo = new TaskStatusInfo();
            taskInfo.setIdentifier(newTaskInfo.getBussId());
            taskInfo.setBussId(newTaskInfo.getBussId());
            taskInfo.setBussType(newTaskInfo.getBussType());
            taskInfo.setResType(newTaskInfo.getResType());
            String[] idArray = newTaskInfo.getBussId().split("_");
            taskInfo.setUuid(idArray[0]);
            taskInfo.setStatus(PackageUtil.PackStatus.PENDING.getStatus());
        }
        taskInfo.setTaskId(newTaskInfo.getTaskId());
        taskInfo.setPriority(newTaskInfo.getPriority());
        if(StringUtils.isNotEmpty(newTaskInfo.getDescription())) {
            taskInfo.setDescription(newTaskInfo.getDescription());
        }
        
        UpdateTaskInfo(taskInfo);
        
        return oldTaskId;
    }

    @Override
    public void FinishTask(String taskId, Map<String,String> uriParams, String argument) {
        
        TaskStatusInfo taskInfo = null;
        try {
            Query query = taskRepository.getEntityManager().createNamedQuery("queryByTaskId");
            query.setParameter("taskid", taskId);
            taskInfo = (TaskStatusInfo) query.getSingleResult();
        } catch (Exception e1) {
            LOG.error("未在任务表中找到任务id: "+taskId);
        }
        
        if(null != taskInfo) {

            if(TASK_BUSS_TYPE_PACK.equals(taskInfo.getBussType())) {
                if(CollectionUtils.isNotEmpty(uriParams)) {
                    try {
                        packCallbackService.packCallback(uriParams, taskInfo);
                    } catch (Exception e) {
                        LOG.error("打包任务回调失败:"+e.getMessage());
                    }
                } else {
                    taskInfo.setErrMsg("任务返回信息为null");
                    taskInfo.setStatus("error");
                }
                
            } else if(TASK_BUSS_TYPE_TRANSCODE.equals(taskInfo.getBussType())) {
                try {
                    TransCodeCallBackParam transCodeCallBackParam = null;
                    if(StringUtils.isNotEmpty(argument)) {
                        transCodeCallBackParam = ObjectUtils.fromJson(argument,
                                TransCodeCallBackParam.class);
                    } else if(CollectionUtils.isNotEmpty(uriParams)) {
                        transCodeCallBackParam = new TransCodeCallBackParam();
                        int status = Integer.parseInt(uriParams.get("status"));
                        transCodeCallBackParam.setStatus(status);
                        transCodeCallBackParam.setTranscodeType("normal");
                        transCodeCallBackParam.setHref("${ref-path}"+uriParams.get("xml_path") + "/" + taskInfo.getUuid() + ".pkg/main.xml");
                        transCodeCallBackParam.setErrMsg("");
                    } else {
                        transCodeCallBackParam = new TransCodeCallBackParam();
                        transCodeCallBackParam.setStatus(0);
                        transCodeCallBackParam.setErrMsg("任务返回信息为null");
                    }
                    transcodeCallbackService.transcodeCallback(transCodeCallBackParam, taskInfo);
                } catch (Exception e) {
                    LOG.error("转码任务回调失败:",e);
                }
            } else if(TASK_BUSS_TYPE_IMAGE_TRANSCODE.equals(taskInfo.getBussType())) {
                try {
                    TransCodeCallBackParam transCodeCallBackParam = null;
                    if (StringUtils.isNotEmpty(argument)) {
                        transCodeCallBackParam = ObjectUtils.fromJson(argument,
                                TransCodeCallBackParam.class);
                        transcodeCallbackService.imageTranscodeCallback(transCodeCallBackParam, taskInfo);
                    }
                } catch (Exception e) {
                    LOG.error("图片转码任务回调失败:",e);
                }
            }
            UpdateTaskInfo(taskInfo);
        }
    }

    @Override
    @Transactional
    public void DealInvalidTask(String taskId, String errMsg) {
        DealInvalidTask(taskId, errMsg, PackageUtil.PackStatus.ERROR.getStatus());
    }

    @Override
    @Transactional
    public void DealInvalidTask(String taskId, String errMsg, String status) {
        TaskStatusInfo taskInfo = null;
        try {
            Query query = taskRepository.getEntityManager().createNamedQuery("queryByTaskId");
            query.setParameter("taskid", taskId);
            taskInfo = (TaskStatusInfo) query.getSingleResult();
        } catch (Exception e1) {
            LOG.error("未在任务表中找到任务id: "+taskId);
            return;
        }
        
        if (taskInfo != null) {
            String oldTaskId = taskInfo.getTaskId();
//            taskRepository.getEntityManager().refresh(taskInfo, LockModeType.PESSIMISTIC_WRITE);
            if(!oldTaskId.equals(taskInfo.getTaskId())) {
                LOG.info("任务id被刷新， 取消处理无效任务");
                return;
            }

            taskInfo.setStatus(status);
            taskInfo.setErrMsg(errMsg);
            if(TASK_BUSS_TYPE_TRANSCODE.equals(taskInfo.getBussType())) {
                TransCodeCallBackParam transCodeCallBackParam = new TransCodeCallBackParam();
                transCodeCallBackParam.setStatus(0);
                transCodeCallBackParam.setErrMsg(errMsg);
                try {
                    transcodeCallbackService.transcodeCallback(transCodeCallBackParam, taskInfo);
                } catch (IOException e) {
                    LOG.error("更新转码状态失败",e);
                }
            }
            
            UpdateTaskInfo(taskInfo);
        }
    }
    
    @Override
    public void UpdateTaskInfo(TaskStatusInfo taskInfo) {
        if(null!=taskInfo) {
            Date date = new Date();
            Timestamp time = new Timestamp(date.getTime());
            try {
                taskInfo.setUpdateTime(time);
                taskRepository.update(taskInfo);
            } catch (EspStoreException e1) {
                LOG.error("更新任务状态失败:"+e1.getMessage());
            }
        }
    }
    
    
    

}
