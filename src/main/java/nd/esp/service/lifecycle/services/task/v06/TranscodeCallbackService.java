package nd.esp.service.lifecycle.services.task.v06;

import java.io.IOException;

import nd.esp.service.lifecycle.entity.TransCodeCallBackParam;

import nd.esp.service.lifecycle.repository.model.TaskStatusInfo;

public interface TranscodeCallbackService {

    void transcodeCallback(TransCodeCallBackParam argument, TaskStatusInfo taskInfo) throws IOException;
}
