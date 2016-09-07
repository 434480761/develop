/* =============================================================
 * Created: [2015年11月5日] by linsm
 * =============================================================
 *
 * Copyright 2014-2015 NetDragon Websoft Inc. All Rights Reserved
 *
 * =============================================================
 */

package nd.esp.service.lifecycle.services.offlinemetadata;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import nd.esp.service.lifecycle.daos.offlinemetadata.OfflineDao;
import nd.esp.service.lifecycle.educommon.services.impl.NDResourceServiceImpl;
import nd.esp.service.lifecycle.educommon.vos.ResTechInfoViewModel;
import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;
import nd.esp.service.lifecycle.entity.cs.CsSession;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.TaskStatusInfo;
import nd.esp.service.lifecycle.repository.sdk.TaskStatusInfoRepository;
import nd.esp.service.lifecycle.services.ContentService;
import nd.esp.service.lifecycle.support.Constant.CSInstanceInfo;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.support.busi.PrePackUtil;
import nd.esp.service.lifecycle.support.cs.ContentServiceHelper;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.nd.gaea.rest.o2o.JacksonCustomObjectMapper;


/**
 * @author linsm
 * @since
 */
public class OfflineServiceImpl implements OfflineService {

    private final static Logger LOG = LoggerFactory.getLogger(OfflineServiceImpl.class);

    private final static ExecutorService executorService = CommonHelper.getForkJoinPool();

    private final static String HREF_KEY = "href";
    private final static String SOURCE_KEY = "source";
    
    private final static JacksonCustomObjectMapper OBJECT_MAPPER = new JacksonCustomObjectMapper();
    
    @Autowired
    private OfflineDao offlineDao;
    
    @Autowired
    private ContentService contentService;
    
    @Autowired
    private TaskStatusInfoRepository taskStatusInfoRepository;
    
    @Autowired
    private PrePackUtil prePackUtil;

    /**
     * 将资源元数据(LC,CG,TI,EDU,CR)离线到了cs 
     * @author linsm
     * update by lsm : 将失败“任务”写到任务表中  2015.11.18
     * update by lsm : 将断言改成exception-》拿到失败的原因 2015.11.18
     */
    @Override
    public Boolean writeToCsAsync(final String resType, final String uuid) {

        executorService.execute(new Runnable() {

            @Override
            public void run() {
                _writeToCs(resType, uuid);
            }
            
        });

        return true;
    }
    
    /**
     * @author linsm
     * @param resType
     * @param uuid
     * @since 
     */
    private void _writeToCs(String resType, String uuid) {

    	ResourceViewModel resource = null;
        try {

            LOG.info("write To Cs" + " resType:" + resType + " uuid: " + uuid);
            
            long start = System.currentTimeMillis();

            //修改bug, 为避免丢失扩展属性,getDetail 改成返回String
            String json = offlineDao.getDetail(resType, uuid);

            ResourceViewModel resourceViewModel = OBJECT_MAPPER.readValue(json, ResourceViewModel.class);
            
            if(resourceViewModel == null){
                throw new Exception("取不到资源视图");
            }
            resource = resourceViewModel;

//            LOG.info("get detail success");
            
            String location = getPath(resourceViewModel);
            
//            LOG.info("location:{} ",location);
            
            String rootPath = NDResourceServiceImpl.getRootPathFromLocation(location);
            
//            LOG.info("rootPath:{}"+rootPath);
            
            CSInstanceInfo csInstanceInfo = NDResourceServiceImpl.getCsInstanceAccordingRootPath(rootPath);

            String path = NDResourceServiceImpl.producePath(rootPath, resType, uuid);
            
//            LOG.info("path:{}"+path);
            
            CsSession csSession = contentService.getAssignSession(path, csInstanceInfo.getServiceId());
            
//            Assert.assertNotNull(csSession);
            if(csSession == null){
                throw new Exception("取不到session，路径:"+path);
            }
            
//            LOG.info("csSession:{}"+csSession.getSession());

            byte[] content = json.getBytes();
            
//			使用http api请求的方式
//            offlineDao.upFileToCs(content,
//                                  path,
//                                  "metadata.json",
//                                  csSession.getSession());
            
            //cs sdk方式
            ContentServiceHelper.uploadByByte(content, path, "metadata.json", 
            		path + "/metadata.json", "prepub_content_edu_product", csSession.getSession());
            
            LOG.info("consume time (ms):{}",System.currentTimeMillis()-start);
            
        } catch (Exception e) {

            String errorMessage = "offline to cs failed,reason: "+e.getMessage()+",resourceType: "+resType+",uuid: "+uuid;
            LOG.info(errorMessage);
            
            TaskStatusInfo taskStatusInfo= new TaskStatusInfo();
            taskStatusInfo.setIdentifier(UUID.randomUUID().toString());
            taskStatusInfo.setBussType("OFFLINE");
            taskStatusInfo.setStatus("OFFLINE_FAILED");  //主要为了与打包、转码的任务的状态分开
            taskStatusInfo.setErrMsg(errorMessage);
            taskStatusInfo.setResType(resType);
            taskStatusInfo.setUuid(uuid);
            taskStatusInfo.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            
            try {
                
                taskStatusInfoRepository.add(taskStatusInfo);
                
            } catch (EspStoreException e1) {
                
                LOG.error(errorMessage+",but create task fail,reason: "+e1.getMessage());
            }
        }
        
        if(null != resource) {
        	prePackUtil.tryPrePack(resource, resType, true);
        }
    }

    /**
     * 从资源视图中取得路径
     * 
     * @author linsm
     * @param resourceViewModel
     * @return
     * @since
     */
    private static String getPath(ResourceViewModel resourceViewModel) {
        Map<String, ? extends ResTechInfoViewModel> tech_info= resourceViewModel.getTechInfo();
        
//        Assert.assertNotNull(tech_info);
        if(tech_info == null){
            throw new RuntimeException("tech_info为空");
        }
        
        ResTechInfoViewModel resTechInfoViewModel = null;
        
        if(tech_info.containsKey(HREF_KEY)){
            resTechInfoViewModel = tech_info.get(HREF_KEY);
        }else{
            resTechInfoViewModel = tech_info.get(SOURCE_KEY);
        }
        
//        Assert.assertNotNull(resTechInfoViewModel);
        if(resTechInfoViewModel == null){
            throw new RuntimeException("tech_info 中 href 和 source 都为空");
        }
        
        String location = resTechInfoViewModel.getLocation();
        
//        Assert.assertNotNull(location);
        if(StringUtils.isEmpty(location)){
            throw new RuntimeException("地址为空");
        }
        
        return location;
    }

    /* (non-Javadoc)
     * @see nd.esp.service.lifecycle.services.offlinemetadata.OfflineService#writeToCsSyn(java.lang.String, java.lang.String)
     */
    @Override
    public Boolean writeToCsSync(String resType, String uuid) {
        _writeToCs(resType,uuid);
        return true;
    }

    /* (non-Javadoc)
     * @see nd.esp.service.lifecycle.services.offlinemetadata.OfflineService#batchWriteToCs(java.lang.String, java.util.Set)
     */
    @Override
    public Boolean batchWriteToCsAsync(String resType, Set<String> uuidSet) {
        if(CollectionUtils.isNotEmpty(uuidSet)){
            for(String uuid:uuidSet){
                writeToCsAsync(resType, uuid);
            }
        }
        return true;
    }

}
