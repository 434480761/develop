package com.nd.esp.task.worker.buss.packaging.repository.impl;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.nd.esp.task.worker.buss.packaging.Constant;
import com.nd.esp.task.worker.buss.packaging.entity.lifecycle.UploadParam;
import com.nd.esp.task.worker.buss.packaging.entity.lifecycle.UploadResponse;
import com.nd.esp.task.worker.buss.packaging.model.CoursewareModel;
import com.nd.esp.task.worker.buss.packaging.repository.CoursewareDao;
import com.nd.esp.task.worker.buss.packaging.utils.BeanMapperUtils;


/**
 * @title 课件DAO实现类
 * @desc
 * @atuh lwx
 * @createtime on 2015年6月11日 下午8:24:20
 */
@Component("coursewareDao")
public class CoursewareDaoImpl implements CoursewareDao {
    
    private final Log LOG=LogFactory.getLog(CoursewareDaoImpl.class);
    
    @Autowired
    private RestTemplate restTemplate;

    @Override
    public CoursewareModel create(CoursewareModel model) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CoursewareModel get(String  identifier) {
        //String url=Constant.LIFE_CYCLE_API_URL+"coursewares/"+identifier;
        String url=Constant.LIFE_CYCLE_API_URL+"/coursewares/"+identifier;
        CoursewareModel coursewareModel= restTemplate.getForObject(url, CoursewareModel.class);
        return coursewareModel;
    }
    @Override
    public CoursewareModel update(CoursewareModel model) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean delete(CoursewareModel model) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public UploadResponse getUpload(String identifier) {
        String url=Constant.LIFE_CYCLE_API_URL+"/coursewares/"+identifier+"/uploadurl?uid=777";
        String response= restTemplate.getForObject(url, String.class);
        UploadResponse uploadResponse=null;
        try {
            uploadResponse = BeanMapperUtils.mapperOnString(response, UploadResponse.class);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //UploadResponse uploadResponse=  ObjectUtils.fromJson(response,new TypeToken<UploadResponse>(){});
        return uploadResponse;
    }

    @Override
    public UploadResponse getUpload(UploadParam uploadParam) {
        //可以注入cs接口,传入指定的参数即可source_type
        String resourceType=uploadParam.getResourceType();
        String identifier= uploadParam.getUuid();
        String uid=uploadParam.getUid();
        String coverage=uploadParam.getCoverage();
        String url=Constant.LIFE_CYCLE_API_URL+"/%s/%s/uploadurl?uid=%s&coverage=%s";
        url=String.format(url, new Object[]{resourceType,identifier,uid,coverage});
       
        UploadResponse uploadResponse=null;
        try {
            String response= restTemplate.getForObject(url, String.class);
            uploadResponse = BeanMapperUtils.mapperOnString(response, UploadResponse.class);
        } catch (IOException e) {
           LOG.error("从LC获取上传地址异常:"+e.getMessage());
        }
        //UploadResponse uploadResponse=  ObjectUtils.fromJson(response,new TypeToken<UploadResponse>(){});
        return uploadResponse;
    }

}
