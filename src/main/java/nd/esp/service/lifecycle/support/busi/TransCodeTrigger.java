package nd.esp.service.lifecycle.support.busi;

import java.util.concurrent.ExecutorService;

import nd.esp.service.lifecycle.entity.TransCodeParam;
import nd.esp.service.lifecycle.entity.cs.CsSession;
import nd.esp.service.lifecycle.services.ContentService;
import nd.esp.service.lifecycle.services.lifecycle.v06.LifecycleServiceV06;
import nd.esp.service.lifecycle.support.Constant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @title 转码触发器
 * @desc
 * @atuh lwx
 * @createtime on 2015年8月18日 下午5:16:30
 */
@Component
public class TransCodeTrigger {
    
    private final static ExecutorService executorService = CommonHelper.getPrimaryExecutorService();
    
    private final static Logger LOG = LoggerFactory.getLogger(TransCodeTrigger.class);
    
    @Autowired
    private ContentService contentService;
    
    @Autowired
    private TransCodeUtil transCodeUtil;
    
    @Autowired
    @Qualifier("lifecycleServiceV06")
    private LifecycleServiceV06 lifecycleService;
    
    /**	
     * @desc:触发转码方法  
     * @createtime: 2015年8月18日 
     * @author: liuwx 
     * @param codeParam
     */
    public void trigger(TransCodeParam codeParam ){
        executorService.execute(new TranscodeThread(codeParam));
    }
    
    /**
     * 触发视频转码
     */
    public void triggerVideo(TransCodeParam codeParam){
        executorService.execute(new VideoTranscodeThread(codeParam));
    }

    public void triggerImage(TransCodeParam codeParam ){
        executorService.execute(new ImageTranscodeThread(codeParam));
    }
    
    /**
     * 视频转码线程
     * @author linsm
     * @since 
     *
     */
    class VideoTranscodeThread extends Thread{
        private TransCodeParam codeParam;
        public VideoTranscodeThread(TransCodeParam codeParam){
            this.codeParam = codeParam;
        }
        
        @Override
        public void run(){
            try {
                
                LOG.info("source对应的实例键值:{}",codeParam.getInstanceKey());//${ref-path}/edu
                
                Constant.CSInstanceInfo instanceInfo=  Constant.CS_INSTANCE_MAP.get(codeParam.getInstanceKey());
                //FIXME 暂时取得最大的session(整个实例)
                String session= SessionUtil.createSession(instanceInfo);
                
                LOG.info("source实例对应的sessionID:{}",session);
                
                String domain=TransCodeUtil.getDomain(codeParam.getReferer());
                int priority=TransCodeUtil.getTranscodePriority(domain);
                
                if(codeParam.isbOnlyOgv()) {
                    priority = 0;
                }
                
                transCodeUtil.triggerVideoTransCode(codeParam,session,priority);
                   
            } catch (Exception e) {
                
                LOG.warn("创建转码任务失败:",e);
                
//                // 更新数据库中的状态
//                CommonHelper.updateStatusInDB(codeParam.getResType(),
//                                              codeParam.getResId(),
//                                              TransCodeUtil.getTransErrStatus(true));
                lifecycleService.addLifecycleStep(codeParam.getResType(),
                                                  codeParam.getResId(),
                                                  false,
                                                  "创建转码任务失败: " + e.getMessage());  
            }
        }
        
    }
    
    class TranscodeThread extends Thread{
        private TransCodeParam codeParam;
         public TranscodeThread (TransCodeParam codeParam){
             this.codeParam = codeParam;
         }
        public void run(){
            try {
                LOG.info("href对应的实例键值:{}",codeParam.getInstanceKey());//${ref-path}/edu
                Constant.CSInstanceInfo instanceInfo=  Constant.CS_INSTANCE_MAP.get(codeParam.getInstanceKey());
                String session= SessionUtil.createSession(instanceInfo);
                LOG.info("href实例对应的sessionID:{}",session);
                String domain=TransCodeUtil.getDomain(codeParam.getReferer());
                int priority=TransCodeUtil.getTranscodePriority(  domain);
                String templateInstanceKey=SessionUtil.getHrefInstanceKey("${ref-path}/"+TransCodeUtil.PPT_TEMPLATE_PATH);
                Constant.CSInstanceInfo templateInstanceInfo=  Constant.CS_INSTANCE_MAP.get(templateInstanceKey);
                CsSession csSession2=contentService.getAssignSession(templateInstanceInfo.getPath(), templateInstanceInfo.getServiceId());
                String templateSession=csSession2.getSession();
                String templatePath= TransCodeUtil.getFullTranscodeTemplatePath(codeParam.getResType(),templateSession);
                //暂时不使用两个配置
                //String taskUrl= TransCodeUtil.getTransCodeTaskUrl(instanceKey);
                
                transCodeUtil.TriggerTransCode(codeParam, session, priority, templatePath);
                   
            } catch (Exception e) {
                LOG.warn("创建转码任务失败:",e);
                       
                lifecycleService.addLifecycleStep(codeParam.getResType(), codeParam.getResId(), false, e.getMessage());
            }
            
        }
    }

    class ImageTranscodeThread extends Thread{
        private TransCodeParam codeParam;
        public ImageTranscodeThread (TransCodeParam codeParam){
            this.codeParam = codeParam;
        }
        public void run(){
            try {
                LOG.info("href对应的实例键值:{}",codeParam.getInstanceKey());//${ref-path}/edu
                Constant.CSInstanceInfo instanceInfo=  Constant.CS_INSTANCE_MAP.get(codeParam.getInstanceKey());
                String session= SessionUtil.createSession(instanceInfo);
                LOG.info("href实例对应的sessionID:{}",session);
                String domain=TransCodeUtil.getDomain(codeParam.getReferer());
                int priority=TransCodeUtil.getTranscodePriority(  domain);

                //暂时不使用两个配置
                //String taskUrl= TransCodeUtil.getTransCodeTaskUrl(instanceKey);

                transCodeUtil.TriggerImageTransCode(codeParam, session, priority);

            } catch (Exception e) {
                LOG.warn("创建转码任务失败:",e);

                lifecycleService.addLifecycleStep(codeParam.getResType(), codeParam.getResId(), false, e.getMessage());
            }

        }
    }


}
