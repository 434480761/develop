package nd.esp.service.lifecycle.entity.lifecycle;

import nd.esp.service.lifecycle.support.enums.AdapterTaskResultStatus;

/**
 * @title 适配器执行结果对象
 * @desc
 * @atuh lwx
 * @createtime on 2015/12/21 16:34
 */
public class AdapterTaskResult {


    private String status;

    //执行成功数
    private int successCount;

    //执行失败数
    private int failCount;

    //执行总数
    private int executeCount;

    //未执行数
    private int unexecuteCount;

    //总数 =executeCount+unexecuteCount
    private int totalCount;


    public AdapterTaskResult(){

        this.status=AdapterTaskResultStatus.UNSTART.getStatus();

    }
    public AdapterTaskResult(String status){

        this.status= status;
    }


    public AdapterTaskResult (AdapterTaskResultStatus adapterTaskResultStatus){

        this.status= adapterTaskResultStatus.getStatus();
    }




    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getFailCount() {
        return failCount;
    }

    public void setFailCount(int failCount) {
        this.failCount = failCount;
    }

    public int getExecuteCount() {
        return executeCount;
    }

    public void setExecuteCount(int executeCount) {
        this.executeCount = executeCount;
    }

    public int getTotalCount() {
    	totalCount = executeCount+unexecuteCount;
        return totalCount;
    }


    public int getUnexecuteCount() {
        return unexecuteCount;
    }

    public void setUnexecuteCount(int unexecuteCount) {
        this.unexecuteCount = unexecuteCount;
    }

    public void increaseSuccessCount(){

        successCount++;
        executeCount++;
    }

    public void increaseFailCount(){

        failCount++;
        executeCount++;
    }
    public void increaseUnexecuteCount(){

        unexecuteCount++;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
}
