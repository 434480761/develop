package nd.esp.service.lifecycle.services.task.v06;

import java.io.IOException;
import java.util.Map;

import nd.esp.service.lifecycle.repository.model.TaskStatusInfo;

/**
 * <p>Create Time: 2015年11月10日           </p>
 * @author ql
 */
public interface PackCallbackService {

    void packCallback(Map<String, String> callbackParams, TaskStatusInfo taskInfo) throws IOException;
    
}
