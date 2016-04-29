package com.nd.esp.task.worker.buss.media_transcode.service;

import com.nd.esp.task.worker.buss.media_transcode.entity.lifecycle.UploadParam;
import com.nd.esp.task.worker.buss.media_transcode.entity.lifecycle.UploadResponse;
import com.nd.esp.task.worker.buss.media_transcode.model.CoursewareModel;
import com.nd.esp.task.worker.buss.media_transcode.support.LifeCircleException;

/**
 * @title 课件的相关接口
 * @desc
 * @atuh lwx
 * @createtime on 2015年6月11日 下午7:46:16
 */
public interface CoursewareService  extends ResourceService<CoursewareModel>{
    
    
    /**	
     * @desc: 获取上传信息
     * @createtime: 2015年6月11日 
     * @author: liuwx 
     * @param identifier
     * @return
     */
    public UploadResponse getUpload(String identifier)throws LifeCircleException;
    /**   
     * @desc: 获取上传信息
     * @createtime: 2015年7月06日 
     * @author: liuwx 
     * @param uploadParam
     * @return
     */
    public UploadResponse getUpload(UploadParam uploadParam )throws LifeCircleException;

}
