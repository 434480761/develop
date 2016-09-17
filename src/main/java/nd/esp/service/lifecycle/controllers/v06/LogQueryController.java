package nd.esp.service.lifecycle.controllers.v06;

import nd.esp.service.lifecycle.entity.log.BaseLogModel;
import nd.esp.service.lifecycle.support.busi.TranscodeTimerTask;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @title 日志查询API入口
 * @desc
 * @atuh lwx
 * @createtime on 2015/9/25 14:20
 */
@RestController()
@RequestMapping("/v0.6/log_query")
public class LogQueryController {


    /**
     * 查询转码执行后的日志信息
     *
     * @return
     */
   @RequestMapping(value = "/actions/{date}/transCode",produces = MediaType.APPLICATION_JSON_VALUE,method = RequestMethod.GET)
    public List<BaseLogModel> queryTransCodeTaskLog(@PathVariable(value="date")String date){

       BaseLogModel coursewareTranscodeLog= TranscodeTimerTask.getCoursewareTranscodeLog();
       BaseLogModel lessonplanTranscodeLog= TranscodeTimerTask.getLessonplanTranscodeLog();

       List<BaseLogModel>logs =new ArrayList<>();
       logs.add(coursewareTranscodeLog);
       logs.add(lessonplanTranscodeLog);

       return logs;
    }

}
