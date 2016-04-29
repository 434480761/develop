package com.nd.esp.task.worker.buss.packaging.repository;

import com.nd.esp.task.worker.buss.packaging.entity.lifecycle.UploadParam;
import com.nd.esp.task.worker.buss.packaging.entity.lifecycle.UploadResponse;
import com.nd.esp.task.worker.buss.packaging.model.CoursewareModel;

/**
 * @title 课件DAO
 * @desc
 * @atuh lwx
 * @createtime on 2015年6月11日 下午8:23:33
 */
public interface CoursewareDao {
    

    public CoursewareModel create(CoursewareModel model);

    public CoursewareModel get(String  identifier) ;

    public CoursewareModel update(CoursewareModel model) ;

    public boolean delete(CoursewareModel model) ;
    
    /**   
     * @desc: 获取上传信息
     * @createtime: 2015年6月11日 
     * @author: liuwx 
     * @param identifier
     * @return
     */
    @Deprecated
    public UploadResponse getUpload(String identifier);
    
    
    /**   
     * @desc: 获取上传信息
     * @createtime: 2015年7月06日 
     * @author: liuwx 
     * @param uploadParam
     * @return
     */
    public UploadResponse getUpload(UploadParam uploadParam );
  
}
