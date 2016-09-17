package com.nd.esp.task.worker.buss.packaging.service.impls;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nd.esp.task.worker.buss.packaging.entity.lifecycle.UploadParam;
import com.nd.esp.task.worker.buss.packaging.entity.lifecycle.UploadResponse;
import com.nd.esp.task.worker.buss.packaging.model.CoursewareModel;
import com.nd.esp.task.worker.buss.packaging.repository.CoursewareDao;
import com.nd.esp.task.worker.buss.packaging.service.CoursewareService;
import com.nd.esp.task.worker.buss.packaging.support.LifeCircleException;

/**
 * @title 课件的实现
 * @desc
 * @atuh lwx
 * @createtime on 2015年6月11日 下午7:49:02
 */
@Service("coursewareService")
public class CoursewareServiceImpl implements CoursewareService{
    
    @Autowired
    private CoursewareDao coursewareDao;

    @Override
    public CoursewareModel create(CoursewareModel model) {
        // TODO Auto-generated method stub
        return this.coursewareDao.create(model);
    }

    @Override
    public CoursewareModel get(String  identifier) {
        // TODO Auto-generated method stub
        return this.coursewareDao.get(identifier);
    }

    @Override
    public CoursewareModel update(CoursewareModel model) {
        // TODO Auto-generated method stub
        return this.coursewareDao.update(model);
    }

    @Override
    public boolean delete(CoursewareModel model) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public UploadResponse getUpload(String identifier)throws LifeCircleException {
      
        return  this.coursewareDao.getUpload(identifier);
    }

    @Override
    public UploadResponse getUpload(UploadParam uploadParam) throws LifeCircleException {
        // TODO Auto-generated method stub
        return this.coursewareDao.getUpload(uploadParam);
    }
    
    
    

}
