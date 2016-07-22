package nd.esp.service.lifecycle.entity;

import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;
import nd.esp.service.lifecycle.support.Constant;

import org.springframework.beans.factory.annotation.Value;

import nd.esp.service.lifecycle.repository.common.IndexSourceType;

/**
 * @title 创建任务需要的bean
 * @desc
 * @atuh lwx
 * @createtime on 2015年6月12日 下午7:14:59
 */
public class WorkerParam {
   
    // 优先级
    private int priority;

    /***
     * 服务ID
     * @see config/worker.properties
     */
    private String serviceId;

    // 参数 放在body中
    private String argument;

    //LC 资源identifier
    private String identifier;
    
    //分组ID
    private String groupId;


    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

   



    public String getArgument() {
        return argument;
    }

    public void setArgument(String argument) {
        this.argument = argument;
    }
    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
    
    
    
    /**	
     * @desc: 获得拷贝worker需要的信息对象
     * <p>serviceId @see @link{Constant.WORKER_COPY_SERVICE}</p> 
     * <p>priority @see @link{Constant.WORKER_COPY_PRIORITY}</p> 
     * @createtime: 2015年6月16日 
     * @author: liuwx 
     * @return
     */
    public static  WorkerParam createCopyParam(){
        WorkerParam param=new WorkerParam();
        param.setServiceId(Constant.WORKER_COPY_SERVICE);
        param.setPriority(Integer.valueOf(Constant.WORKER_COPY_PRIORITY));
        param.setGroupId(Constant.WORKER_DEFAULT_GROUP_ID);
        return param;
        
    }
   

    /**	
     * @desc: 获得转码worker需要的信息对象
     * <p>serviceId @see @link{Constant.WORKER_TRANSCODE_SERVICE}</p> 
     * @createtime: 2015年6月16日 
     * @author: liuwx 
     * @return
     * @see nd.esp.service.lifecycle.support.busi.transcode.WorkerManager#getDefaultWorkParam(ResourceViewModel, String)
     */
    public static  WorkerParam createTranscodeParam(String resType){
        WorkerParam param=new WorkerParam();
        if(resType.equals(IndexSourceType.LessonPlansType.getName()) 
                || resType.equals(IndexSourceType.LearningPlansType.getName())) {
            param.setServiceId(Constant.WORKER_LESSONPLAN_TRANSCODE_SERVICE);
        } else {
            param.setServiceId(Constant.WORKER_TRANSCODE_SERVICE);
        }
        param.setGroupId(Constant.WORKER_DEFAULT_GROUP_ID);
        return param;
        
    }
    /**	
     * @desc: 获得打包worker需要的信息对象
     * <p>serviceId @see @link{Constant.TASK_PACKAGING_SERVICE_ID}</p> 
     * <p>priority @see @link{Constant.PACKAGING_PRIORITY}</p> 
     * @createtime: 2015年6月16日 
     * @author: liuwx 
     * @return
     */
    public static  WorkerParam createPackParam(){
        WorkerParam param=new WorkerParam();
        param.setServiceId(Constant.TASK_PACKAGING_SERVICE_ID);
        param.setPriority(Integer.valueOf(Constant.PACKAGING_PRIORITY));
        param.setGroupId(Constant.WORKER_DEFAULT_GROUP_ID);
        return param;
        
    }

    /**
     * 获取视频转码worker参数：设置group, service
     * @author linsm
     * @return WorkerParam 
     * @since
     */
    public static WorkerParam createVideoTranscodeParam() {
        // 找高扬要到group:2, service :5,还是采用默认的group:3
        WorkerParam param = new WorkerParam();
        param.setServiceId(Constant.WORKER_VIDEO_TRANSCODE_SERVICE);
        // FIXME 暂时只布了一台， group 先写死
        param.setGroupId(Constant.WORKER_DEFAULT_GROUP_ID);
        return param;

    }

    /**
     * 获取视频转码worker参数：设置group, service
     * @author linsm
     * @return WorkerParam
     * @since
     */
    public static WorkerParam createImageTranscodeParam() {
        // group:2, service :6
        WorkerParam param = new WorkerParam();
        param.setServiceId(Constant.WORKER_IMAGE_TRANSCODE_SERVICE);
        // FIXME 暂时只布了一台， group 先写死
        param.setGroupId(Constant.WORKER_DEFAULT_GROUP_ID);
        return param;

    }

}
