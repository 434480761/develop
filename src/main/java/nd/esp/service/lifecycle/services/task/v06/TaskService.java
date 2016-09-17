package nd.esp.service.lifecycle.services.task.v06;

import java.util.List;








import java.util.Map;

import nd.esp.service.lifecycle.repository.model.TaskStatusInfo;
import org.springframework.transaction.annotation.Transactional;

public interface TaskService {

    //创建调度任务； 若相同bussId的任务已存在，更新taskId, 返回原taskId; 若为新任务，返回null
    String CreateOrRestartTask(TaskStatusInfo newTaskInfo);
    
    //完成调度任务
    void FinishTask(String taskId, Map<String, String> uriParams, String argument);
    
    //获取所有正在运行的调度任务
    List<TaskStatusInfo> getAllRunningTask();
    
    //获取所有失败的任务
    List<TaskStatusInfo> getAllFailTask();

    //更新任务信息
    void UpdateTaskInfo(TaskStatusInfo taskInfo);

    //处理无效任务，改状态为失败，写入错误信息
    void DealInvalidTask(String taskId, String errMsg);

    void DealInvalidTask(String taskId, String errMsg, String status);

}
