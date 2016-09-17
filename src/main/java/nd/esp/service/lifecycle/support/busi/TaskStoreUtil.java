package nd.esp.service.lifecycle.support.busi;

import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.TaskStatusInfo;
import nd.esp.service.lifecycle.repository.sdk.TaskStatusInfoRepository;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.Date;

/**
 * @author liuwx
 * @version 1.0
 * @title LC任务存储工具类
 * @Desc
 * @create 2015年10月13日 下午3:45:26
 */
public class TaskStoreUtil {

    public static final String BUSS_TYPE_TRANSCODE="transcode";

    private TaskStatusInfoRepository taskStatusInfoRepository;

    private TaskStatusInfo taskStatusInfo;



    public TaskStoreUtil(TaskStatusInfoRepository taskStatusInfoRepository) {


        taskStatusInfo=new TaskStatusInfo();
        this.taskStatusInfoRepository = taskStatusInfoRepository;
    }


    public  TaskStoreUtil initTaskStatusInfo(TaskStatusInfo taskStatusInfo){

        this.taskStatusInfo=taskStatusInfo;

        return this;
    }


    public TaskStoreUtil buildBussType(String bussType){

        taskStatusInfo.setBussType(bussType);
        return this;
    }
    public TaskStoreUtil buildTaskId(String taskId){

        taskStatusInfo.setTaskId(taskId);
        return this;
    }
    public TaskStoreUtil buildStatus(String status){

        taskStatusInfo.setStatus(status);
        return this;
    }
    public TaskStoreUtil buildUpdateTime(Date date){
        Timestamp time = new Timestamp(date.getTime());
        taskStatusInfo.setUpdateTime(time);
        return this;
    }
    public TaskStoreUtil buildUpdateTime(){
        Date date =new Date();
        Timestamp time = new Timestamp(date.getTime());
        taskStatusInfo.setUpdateTime(time);
        return this;
    }



    /**
     *
     * @param resType
     * @param identifier
     * @param taskId
     * @return
     */
    public TaskStoreUtil initTransCodeTask(String resType, String identifier,String taskId) {
        taskStatusInfo.setIdentifier(identifier);
        taskStatusInfo.setIdentifier(identifier);
        taskStatusInfo.setBussId(identifier);
        taskStatusInfo.setBussType(BUSS_TYPE_TRANSCODE);
        taskStatusInfo.setResType(resType);
        taskStatusInfo.setUuid(identifier);
        taskStatusInfo.setStatus(PackageUtil.PackStatus.PENDING.getStatus());
        taskStatusInfo.setTaskId(taskId);
        buildUpdateTime();
        return  this;
    }


    public void createTask(TaskStatusInfo taskStatusInfo)throws EspStoreException {

        this.taskStatusInfo=taskStatusInfo;
        createTask();

    }
    public void createTask()throws EspStoreException {
        Assert.assertNotNull("创建任务状态的对象不能为空",taskStatusInfo);
        taskStatusInfoRepository.add(taskStatusInfo);

    }

    public void updateTask(TaskStatusInfo taskStatusInfo)throws EspStoreException {

        this.taskStatusInfo=taskStatusInfo;
        updateTask();

    }
    public void updateTask()throws EspStoreException {
        Assert.assertNotNull("更新任务状态的对象不能为空",taskStatusInfo);
        taskStatusInfoRepository.update(taskStatusInfo);
    }




}
