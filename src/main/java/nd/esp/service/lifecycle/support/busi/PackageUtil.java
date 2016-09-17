/* =============================================================
 * Created: [2015年5月20日] by Administrator
 * =============================================================
 *
 * Copyright 2014-2015 NetDragon Websoft Inc. All Rights Reserved
 *
 * =============================================================
 */

package nd.esp.service.lifecycle.support.busi;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import nd.esp.service.lifecycle.educommon.models.ResTechInfoModel;
import nd.esp.service.lifecycle.educommon.models.ResourceModel;
import nd.esp.service.lifecycle.educommon.vos.ResTechInfoViewModel;
import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;
import nd.esp.service.lifecycle.models.ArchiveModel;
import nd.esp.service.lifecycle.services.packaging.v06.PackageService;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.gson.reflect.TypeToken;

import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.TaskStatusInfo;
import nd.esp.service.lifecycle.repository.sdk.TaskStatusInfoRepository;

/**
 * <h2>打包工具相关的工具</h2>
 * <p>
 * </p>
 *
 * @author liuwx
 * @since
 * @create 2015年5月20日 下午5:10:21
 */
public class PackageUtil {
    
    
    /** 存储于内存中,用来存放打包的信息(状态) */
    //@Deprecated
    //private static final Map<String, PackageStatus> PACKAGE_INFO = Collections.synchronizedMap(new HashMap<String, PackageStatus>());
    /** 打包线程,目前工作进程数5个 */
    
    private final static ExecutorService executorService = CommonHelper.getForkJoinPool();
    //public final static String OFFLINE_ICPLAYER = "offline_icplayer";

    public final static String TARGET_DEFALUT = "default";
    public final static String TARGET_STUDENT = "student";
    public final static String TARGET_COMBINED = "combined";
    
    public final static String OFFLINE = "offline";
    public final static String PLUS_ICPLAYER = "_icplayer";
    /**storeinfo中对应的打包状态key*/
    public final static String PACKAGE_STATUS_KEY="status";
    /**storeinfo中对应的错误信息key*/
    public final static String PACKAGE_ERRMSG_KEY="err_msg";
    
    private final Logger LOG = LoggerFactory.getLogger(PackageUtil.class);
    /**单实例队列，用于更新数据(防止同步刷新的时候锁住)*/
    public static Set<String> updateQueue = Collections.synchronizedSet(new HashSet<String>());
    //public static Set<String> updateQueue = Collections.newSetFromMap(new ConcurrentHashMap<String,Boolean>());

    @Autowired
    @Qualifier("PackageServiceImpl")
    private PackageService packageService;
    
    @Autowired
    TaskStatusInfoRepository taskRepository;
    
    /**打包额外的错误信息*/
    public  String packageAppendMessage;
    
    
//    public final static String CONVERT_STATUS_UNCONVERTED = "CONVERT_UN";
//    public final static String CONVERT_STATUS_CONVERTING = "CONVERT_ING";
//    public final static String CONVERT_STATUS_CONVERTED = "CONVERT_ED";
//    public final static String CONVERT_STATUS_CONVERT_ERR = "CONVERT_ER";
//    public final static String CONVERT_STOREINFO_KEY = "href";
//    public final static String CONVERT_TRIGGER_URL = LifeCircleApplicationInitializer.properties.getProperty("sdp_transcode_service_url");
    
    public static void TriggerPackaging(String identifier, String resType,
            String uid,String path, String icplayer) {
        try {
            path = URLEncoder.encode(path, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            
        }

//        String response = null;
//        String url_=CONVERT_TRIGGER_URL+"?identifier="+identifier+"&location="+url;
//
//        try{
//            response = HttpClientUtils.httpGet(url_);
//        } catch (Exception e) {
//        }
    }

    public void archiving(String path,
                          String target,
                          String id,
                          String resType,
                          String uid,
                          boolean webpFirst) throws Exception {

        PackageThread packageThread = new PackageThread(path, target, id, resType, uid, webpFirst);
        //executorService.submit(packageThread);
        executorService.execute(packageThread);

    }
    
    
    /**
     * 设置课件的打包信息
     * @param id
     * @param icplayer
     * @param status
     * @since 
     */
    public void setPackageInfo(Map<String, String> storeInfo,PackStatus status){
        
        storeInfo.put(PACKAGE_STATUS_KEY, status.getStatus());
    }

    /**
     * 获取storeinfo中的打包的键
     * 
     * @param icplayer
     * @return
     * @since
     */
    public static String getStoreInfoKey(String target, boolean icplayer) {
        if(StringUtils.isEmpty(target) || target.equals(PackageUtil.TARGET_DEFALUT)) {
            target = PackageUtil.OFFLINE;
        }
        String key = icplayer ? target+PackageUtil.PLUS_ICPLAYER : target;
        return key;
    }
    
   
    
    

    
  

    public String getPackageAppendMessage() {
        return packageAppendMessage;
    }

    

    /**
     * 打包状态类
     */
    public static enum PackStatus {
        START("start", "开始打包") ,
        UNPACK("unpack", "还未打包过") ,
        PENDING("pending", "打包中") ,
        READY("ready", "打包完成") ,
        ERROR("error", "打包错误") ;
        String status;
        String message;

        PackStatus(String status, String message) {
            this.status = status;
            this.message = message;
        }

        public String getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }
        
        public  ArchiveModel getArchiveModel(){
            ArchiveModel archiveModel = new ArchiveModel();
            archiveModel.setAccessMethod(RequestMethod.GET.toString());
            archiveModel.setArchiveState(status);
            archiveModel.setMessage(message);
            archiveModel.setAccessUrl("");
            return archiveModel;
            
        }
        
        public static PackStatus getPackageStatus(String statusVal){
            PackStatus st=PackStatus.UNPACK;
            for(PackStatus status: PackStatus.values()){
                if(status.getStatus().equals(statusVal)){
                    st=status;
                    break;
                }
               
            }
           return st;
            
        }

    }
    public void setPackageAppendMessage(String packageAppendMessage) {
        this.packageAppendMessage = packageAppendMessage;
    }


    private class PackageThread implements Runnable {

        String path;
        String id;
        String target;
        String resType;
        String uid;
        boolean webpFirst;

        public PackageThread(String path,
                             String target,
                             String id,
                             String resType,
                             String uid,
                             boolean webpFirst) {

            this.path = path;
            this.id = id;
            this.target = target;
            this.resType = resType;
            this.uid = uid;
            this.webpFirst = webpFirst;
        }

        @Override
        public void run() {
            boolean bPackIcplayer = false;

            Map<String, Map<String, Object>> responses = null;

            Map<String,String>storeinfo=new HashMap<String, String>();
            Date date = new Date();
            Timestamp time = new Timestamp(date.getTime());
            
            String bussId = id+"_"+target;
            if(webpFirst) {
                bussId += "_webp";
            }

            try {
                long start=System.currentTimeMillis();
                StringBuffer logMsg = new StringBuffer();
                responses = packageService.archivingLocal(path, target, id, resType, uid, webpFirst, logMsg);
                
                String storeinfo_key = getStoreInfoKey(target, bPackIcplayer);
                storeinfo=convertTostoreInfoMap(responses.get(storeinfo_key));

                try {
                    TaskStatusInfo pkRepository = taskRepository.get(bussId);
                    if(pkRepository!=null) {
                        if(pkRepository.getTaskId()==null) {
                            pkRepository.setStatus(PackageUtil.PackStatus.READY.getStatus());
                            pkRepository.setStoreInfo(ObjectUtils.toJson(storeinfo));
                            pkRepository.setUpdateTime(time);
                            taskRepository.update(pkRepository);
                        }
                    }
                } catch (EspStoreException e) {
                    LOG.error("更新任务状态表失败:"+e.getMessage());
                }

                long end=(System.currentTimeMillis()-start)/1000;
                LOG.info("打包完成,共耗时:"+end);
            } catch (Exception e) {
                //追加错误信息
                LOG.error("打包失败:"+e.getMessage());
                try {
                    TaskStatusInfo pkRepository = taskRepository.get(bussId);
                    if(pkRepository!=null) {
                        if(pkRepository.getTaskId()==null) {
                            pkRepository.setStatus(PackageUtil.PackStatus.ERROR.getStatus());
                            pkRepository.setErrMsg(e.getMessage());
                            pkRepository.setUpdateTime(time);
                            taskRepository.update(pkRepository);
                        }
                    }
                } catch (EspStoreException e1) {
                    LOG.error("更新任务状态表失败:"+e1.getMessage());
                }
            }
        }

    }
    
    /**
     * 更新storeinfo 数据库状态
     * @param resType
     * @param lvm
     * @param storeInfo
     * @since 
     */
    /*public  void updateResourceStoreInfo(String resType,LearningObjectModelExtends lvm,Map<String, String> storeInfo){
        try {
            lvm.setStoreInfo(storeInfo);
            resourcesDetailUtil.updateResourceDetail(resType,lvm);
        } catch (Exception e) {
            LOG.error("更新storeinfo失败:"+e.getMessage());
        }
        
    }*/
    
    
    /**
     * cs返回的信息,转成storeinfo需要的
     * 
     * @param response
     * @return
     * @since 
     */
    public static Map<String,String>convertTostoreInfoMap( Map<String, Object>response ){
        
        Map<String,String> map=new HashMap<String, String>();
        if(CollectionUtils.isNotEmpty(response)){
            Object obj=response.get("path");
            String path=obj==null?"":obj.toString();
            if(StringUtils.hasLength(path)){
                map.put("location", path);
                Map<String,Object>inode=ObjectUtils.fromJson(ObjectUtils.toJson(response.get("inode")), Map.class);
                map.put("format", String.valueOf(inode.get("mime")));
                map.put("md5", String.valueOf(inode.get("md5")));
                map.put("size", String.valueOf(inode.get("size")));
            }
        }
        return map;
    }
    
    
    /**
     * 获取资源的path
     * @param lvm
     * @param resType
     * @return
     * @since 
     */
    public static String getResourcePath(ResourceModel resource){
        Assert.assertNotNull(resource);
        
        List<ResTechInfoModel> techInfos = resource.getTechInfoList();
        if(null != techInfos) {
            for(ResTechInfoModel techInfo:techInfos) {
                if(techInfo.getTitle().equals(TransCodeUtil.CONVERT_STOREINFO_KEY)) {
                    String href = techInfo.getLocation();
                    if(StringUtils.hasText(href)){
                        return href.substring(0, href.lastIndexOf('/'))
                                .replace("${ref-path}", "");
                    }
                }
            }
        }
                
        return "";
    }
    
    /**
     * 获取资源的path
     * @param lvm
     * @param resType
     * @return
     * @since 
     */
    public static String getResourcePath(ResourceViewModel resource){
        Assert.assertNotNull(resource);
        
        List<ResTechInfoModel> techInfos = CommonHelper.map2List4TechInfo(resource.getTechInfo());
        
        if(null != techInfos) {
            for(ResTechInfoModel techInfo:techInfos) {
                if(techInfo.getTitle().equals(TransCodeUtil.CONVERT_STOREINFO_KEY)) {
                    String href = techInfo.getLocation();
                    if(StringUtils.hasText(href)){
                        return href.substring(0, href.lastIndexOf('/'))
                                .replace("${ref-path}", "");
                    }
                }
            }
        }
                
        return "";
    }
    
    
//    private class UpdateThread implements Runnable{
//        
//        private String resType;
//        
//        private LearningObjectModelExtends lvm;
//        private String storeinfo_key;
//        
//        public UpdateThread(String resType,LearningObjectModelExtends lvm,String storeinfo_key){
//            this.resType=resType;
//            this.lvm=lvm;
//            this.storeinfo_key=storeinfo_key;
//        }
//
//      
//        @Override
//        public void run() {
//           try {
//            resourcesDetailUtil.updateResourceDetail(resType,lvm,storeinfo_key);
//        } catch (Exception e) {
//            LOG.error("打包更新失败:"+e.getMessage());
//        }
//            
//        }
//        
//        
//    }

}
