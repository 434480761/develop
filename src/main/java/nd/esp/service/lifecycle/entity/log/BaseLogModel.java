package nd.esp.service.lifecycle.entity.log;
/**
 * @title 日志对象基类
 * @desc
 * @atuh lwx
 * @createtime on 2015/9/25 14:32
 */
public class BaseLogModel {
	

    protected long startTime;

    protected String executeResult;

    protected long executeTime;


    protected long endTime;


    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getExecuteResult() {
        return executeResult;
    }

    public void setExecuteResult(String executeResult) {
        this.executeResult = executeResult;
    }

    public long getExecuteTime() {
        return executeTime;
    }

    public void setExecuteTime(long executeTime) {
        this.executeTime = executeTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
}
