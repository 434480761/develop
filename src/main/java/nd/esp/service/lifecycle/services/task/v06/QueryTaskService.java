package nd.esp.service.lifecycle.services.task.v06;

import java.util.List;

import nd.esp.service.lifecycle.repository.model.TaskStatusInfo;


public interface QueryTaskService {
    
    void QueryTaskStatus(List<TaskStatusInfo> taskInfos);

    void QueryAllRunningTaskStatus();

}
