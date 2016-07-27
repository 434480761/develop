package nd.esp.service.lifecycle.support.busi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import nd.esp.service.lifecycle.app.LifeCircleApplicationInitializer;
import nd.esp.service.lifecycle.app.LifeCycleEspStoreConfig;
import nd.esp.service.lifecycle.educommon.models.ResClassificationModel;
import nd.esp.service.lifecycle.educommon.models.ResLifeCycleModel;
import nd.esp.service.lifecycle.educommon.models.ResTechInfoModel;
import nd.esp.service.lifecycle.educommon.models.ResourceModel;
import nd.esp.service.lifecycle.educommon.services.NDResourceService;
import nd.esp.service.lifecycle.educommon.vos.ResLifeCycleViewModel;
import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;
import nd.esp.service.lifecycle.educommon.vos.constant.IncludesConstant;
import nd.esp.service.lifecycle.entity.TransCodeParam;
import nd.esp.service.lifecycle.entity.WorkerParam;
import nd.esp.service.lifecycle.services.lifecycle.v06.LifecycleServiceV06;
import nd.esp.service.lifecycle.services.task.v06.TaskService;
import nd.esp.service.lifecycle.services.task.v06.impls.TaskServiceImpl;
import nd.esp.service.lifecycle.support.Constant;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.busi.transcode.WorkerManager;
import nd.esp.service.lifecycle.support.enums.LifecycleStatus;
import nd.esp.service.lifecycle.utils.BeanMapperUtils;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;
import nd.esp.service.lifecycle.utils.videoConvertStrategy.ConvertRuleSet;
import nd.esp.service.lifecycle.vos.assets.v06.AssetViewModel;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.mysema.commons.lang.URLEncoder;

import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.TaskStatusInfo;
import nd.esp.service.lifecycle.repository.model.TechInfo;
import nd.esp.service.lifecycle.repository.sdk.TaskStatusInfoRepository;
import nd.esp.service.lifecycle.repository.sdk.TechInfoRepository;

public class TransCodeUtil {

    private final static Logger LOG = LoggerFactory.getLogger(TransCodeUtil.class);

    @Autowired
    private TaskService taskService;

    @Autowired
    private TransCodeTrigger transCodeTrigger;

    @Autowired
    private NDResourceService ndResourceService;
    
    @Autowired
    @Qualifier("lifecycleServiceV06")
    private LifecycleServiceV06 lifecycleService;
    
    @Autowired
    private TechInfoRepository techInfoRepository;



    /**转码优先级相关配置信息*/
    public final static String TRANSCODE_INFO = LifeCircleApplicationInitializer.worker_properties.getProperty("transcode_info");
    /**执行环境值 eg dev、test etc..*/
    public final static String TASK_EXECUTE_ENV = LifeCircleApplicationInitializer.worker_properties.getProperty("task_execute_env");

    private final static List<TranscodePriority> TRANSCODEPRIORITY_LIST = new ArrayList<TranscodePriority>();
    /**默认转码优先级*/
    public final static int TRANSCODE_DEFAULT_PRIORITY = 1;
    /**课件转码模板*/
    public final static String PPT_TEMPLATE_PATH = LifeCircleApplicationInitializer.worker_properties.getProperty("ppt_template_path");

    /**教案转码模板*/
    public final static String DOC_TEMPLATE_PATH = LifeCircleApplicationInitializer.worker_properties.getProperty("doc_template_path");
    //课件转码模板地址 session参数
    public final static String PPT_TEMPLATE_PATH_FULL = Constant.CS_API_URL + "/%s/static/" + PPT_TEMPLATE_PATH;
    //*.doc转码模板地址 session参数
    public final static String DOC_TEMPLATE_PATH_FULL = Constant.CS_API_URL + "/%s/static/" + DOC_TEMPLATE_PATH;

    static {
        String infos[] = TRANSCODE_INFO.split(",");
        for (String info : infos) {
            String[] single = info.split("\\|");
            TranscodePriority transcodePriority = new TranscodePriority(single[0], Integer.valueOf(single[1]));
            TRANSCODEPRIORITY_LIST.add(transcodePriority);
        }
    }

    public final static String CONVERT_STATUS_UNCONVERTED = "CONVERT_UN";
    public final static String CONVERT_STATUS_CONVERTING = "CONVERT_ING";
    public final static String CONVERT_STATUS_CONVERTED = "CONVERT_ED";
    public final static String CONVERT_STATUS_CONVERT_ERR = "CONVERT_ER";
    public final static String CONVERT_STOREINFO_KEY = "href";
    public final static String CONVERT_SOURCE_KEY = "source";
    public final static int DEFAULT_COVER_NUM = 16;
    public final static String CUT_COMMAND="thumbnail";
    public final static String CUT_COVER_TARGET = "targetCover";
    public final static String PARAM_JOIN = "-join";
    
    public final static String SUBTYPE_AUDIO = "audio";
    public final static String SUBTYPE_VIDEO = "video";

    public final static String NEW_CONVERT_STATUS_UNCONVERTED = LifecycleStatus.TRANSCODE_WAITING.getCode();
    public final static String NEW_CONVERT_STATUS_CONVERTING = LifecycleStatus.TRANSCODING.getCode();
    public final static String NEW_CONVERT_STATUS_CONVERTED = LifecycleStatus.TRANSCODED.getCode();
    public final static String NEW_CONVERT_STATUS_CONVERT_ERR = LifecycleStatus.TRANSCODE_ERROR.getCode();
    /**
     * 相对路径头
     */
    public final static String REF_PATH = "${ref-path}";
    // public final static String CONVERT_TRIGGER_URL = LifeCircleApplicationInitializer.properties.getProperty("task_submit_utl");
    //临时转码配置
    //public final static String CONVERT_TRIGGER_OTHER_CONFIG = LifeCircleApplicationInitializer.properties.getProperty("trancode_task_submit_config_other");
    private final static Pattern pattern = Pattern.
            compile("(http://|ftp://|https://|www){0,1}[^\u4e00-\u9fa5\\s]*?\\.(com|net|cn|me|tw|fr)[^\u4e00-\u9fa5\\s]*");

    //private final static List<Map<String,String>>OTHER_CONFIG=new ArrayList<Map<String,String>>();
  /*static {
    String configs[]=  CONVERT_TRIGGER_OTHER_CONFIG.split(",");
    for(String config:configs){
       String c[]= config.split("\\|");
        String other_key=c[0];//${ref-path}/edu_product
        String other_url=c[1];//http://esp-store-task_b.web.sdp.101.com/concurrent/service/proxy/submit-execution
        Map<String,String>map=new HashMap<String, String>();
        map.put(other_key, other_url);
        OTHER_CONFIG.add(map);
    }
      
  }*/
//    public static void main(String[] args) {
//        String s = "${ref-path}/edu_product|http://esp-store-task_b.web.sdp.101.com/concurrent/service/proxy/submit-execution";
//        String configs[] = s.split(",");
//        for (String config : configs) {
//            String c[] = config.split("\\|");
//            String other_key = c[0];//${ref-path}/edu_product
//            String other_url = c[1];//http://esp-store-t
//            System.out.println(other_key);
//        }
//    }

    /**
     * @param session
     * @return
     * @desc:获取模板的完整路径
     * @createtime: 2015年7月1日
     * @author: liuwx
     */
    public static String getFullTranscodeTemplatePath(String resType, String session) {
        if (resType.equals(IndexSourceType.LessonPlansType.getName()) 
                || resType.equals(IndexSourceType.LearningPlansType.getName())) {
            return String.format(DOC_TEMPLATE_PATH_FULL, session);
        }

        return String.format(PPT_TEMPLATE_PATH_FULL, session);
    }


    /**
     * 判断是否转码完成
     *
     * @param status
     * @return
     * @desc:
     * @createtime: 2015年6月16日
     * @see  TransCodeUtil#isConverEd(String , boolean)
     * @author: liuwx
     */
    @Deprecated
    public static boolean isConverEd(String status) {

        return isConverEd(status, false);

    }

    /**
     * @param status
     * @param latestVersion
     * @return
     * @desc: 判断是否转码完成
     * @createtime: 2015年8月19日
     * @author: liuwx
     */
    public static boolean isConverEd(String status, boolean latestVersion) {
        if (latestVersion) {
            return NEW_CONVERT_STATUS_CONVERTED.equals(status);
        }
        return CONVERT_STATUS_CONVERTED.equals(status);

    }

    /**
     * @param status
     * @param resType
     * @return
     * @desc: 根据业务, 以下状态
     * @createtime: 2015年9月18日
     * @updatetime  201510.13 支持所有资源特殊状态的拷贝
     * @author: liuwx
     */
    public static boolean specialConverse(String resType, String status) {
        //if (IndexSourceType.LessonPlansType.getName().equals(resType)||IndexSourceType.SourceCourseWareType.getName().equals(resType))

            for (LifecycleStatus s : LifecycleStatus.getSpecialConverseStatus()) {
                if (s.getCode().equals(status)) {
                    return true;
                }

            }
        return false;
    }

    /**
     * 临时方案,为了生产调度到灰度的转码任务,已废除at20150630
     * @desc:通过key获取对应转码任务的地址
     * <p>eg:${ref-path}/edu,${ref-path}/edu_product </p> 
     * @createtime: 2015年6月26日
     * @author: liuwx
     * @param key
     * @return
     */
    //@Deprecated
    /*public static String getTransCodeTaskUrl(String key){
        String taskUrl=StringUtils.EMPTY;
        for(Map<String,String> map:OTHER_CONFIG){
           if(StringUtils.isNotEmpty(taskUrl=map.get(key))){
               break;
           }
        }
        taskUrl=StringUtils.isEmpty(taskUrl)?CONVERT_TRIGGER_URL:taskUrl;
        return taskUrl;
    }*/


    /**
     * <p>Description: 截取referer中的域名
     * 例如:referer:http://esp-lms-prepare.dev.web.nd/ 得到的是esp-lms-prepare.dev.web.nd
     * </p>
     * <p>Create Time: 2015年6月11日   </p>
     * <p>Create author: liuwx   </p>
     *
     * @param referer
     * @return
     */
    public static String getDomain(String referer) {
        if (StringUtils.hasText(referer)) {
            try {
                return referer.split("http://")[1].replace("/", "");
            } catch (Exception e) {
                LOG.warn("获取domain失败:{}", e.getMessage());
            }
        }
        return StringUtils.EMPTY;

    }


    /**
     * <p>Description: 通过domain获取对应的优先级   </p>
     * <p>Create Time: 2015年6月10日   </p>
     * <p>Create author: liuwx   </p>
     *
     * @param domain
     * @return
     * @see TransCodeUtil#getTranscodePriorityByReferer(String)
     * <p>建议使用getTranscodePriorityByReferer代替getTranscodePriority,这样不用多做一步 @link{TransCodeUtil#getDomain(String)}</p>
     */
    public static int getTranscodePriority(String domain) {
        if (StringUtils.hasText(domain)) {
            for (TranscodePriority transcodePriority : TRANSCODEPRIORITY_LIST) {
                if (transcodePriority.check(domain)) {
                    return transcodePriority.getPriority();
                }
            }
        }
        return TRANSCODE_DEFAULT_PRIORITY;
    }


    /**
     * <p>Description: 通过referer获取对应的优先级   </p>
     * <p>Create Time: 2015年6月16日   </p>
     * <p>Create author: liuwx   </p>
     *
     * @param referer
     * @return
     */
    public static int getTranscodePriorityByReferer(String referer) {
        String domain = getDomain(referer);
        return getTranscodePriority(domain);
    }


    /**
     * <p>Description:   转码触发方法(使用默认的转码任务地址)           </p>
     * <p> 额外新添加优先级参数          </p>
     * <p>Create Time: 2015年6月10日   </p>
     * <p>Create author: qiling   </p>
     * <p>Update author: liuwx   </p>
     *
     * @param codeParam
     * @param session
     * @param priority
     */
    public void TriggerImageTransCode(TransCodeParam codeParam, String session, int priority) {
        String url = Constant.TASK_SUBMIT_URL;
        String sourcePath = codeParam.getSourceFileId();
        String location = Constant.CS_INSTANCE_MAP.get(Constant.CS_DEFAULT_INSTANCE).getUrl()
                + "/download?path=" + URLEncoder.encodeURL(sourcePath) + "&session=" + session;

        LOG.error("转码发送的url地址:" + url);
        try {

            Map<String, String> arg = new HashMap<>();
            arg.put("cmd","101ppt_pic");
            arg.put("pic_path", location);

            String trascodePath = sourcePath.substring(0, sourcePath.lastIndexOf("/")) + "/transcode";
            arg.put("pic_transcode_path", trascodePath);
            //新增部分内容
            String uploadUrl = Constant.CS_INSTANCE_MAP.get(Constant.CS_DEFAULT_INSTANCE).getUrl()
                    + "/upload?session=" + session;
            arg.put("upload_api", uploadUrl);
//            String callBackUrl = Constant.LIFE_CYCLE_DOMAIN_URL + "/v0.6/" + codeParam.getResType() + "/transcode/image_callback?identifier="+codeParam.getResId();
            String callBackUrl = "http://192.168.253.17:8080/v0.6/" + codeParam.getResType() + "/transcode/image_callback?identifier="+codeParam.getResId();
            arg.put("callback", callBackUrl);

            String argument = ObjectUtils.toJson(arg);
            WorkerParam param = WorkerParam.createImageTranscodeParam();

            param.setIdentifier(codeParam.getResId());
            param.setPriority(priority);
            param.setArgument(argument);

            //HttpClientUtils.httpPost(url, parameters);
            RestTemplate restTemplate = new RestTemplate();
            //ResponseEntity<String> rt =  restTemplate.postForEntity(url, parameters, String.class);
            ResponseEntity<String> rt = restTemplate.postForEntity(url, param, String.class);

            LOG.info("创建转码任务返回的任务ID:" + rt.getBody());

            String taskId = null;
            if (!StringUtils.isEmpty(rt.getBody())) {
                Map<String, Object> rtMap = BeanMapperUtils.mapperOnString(rt.getBody(), Map.class);
                if (rtMap.get("executionId") != null) {
                    taskId = String.valueOf(rtMap.get("executionId"));
                }
            }
            TaskStatusInfo taskInfo = new TaskStatusInfo();
            taskInfo.setResType(codeParam.getResType());
            taskInfo.setBussType(TaskServiceImpl.TASK_BUSS_TYPE_IMAGE_TRANSCODE);
            taskInfo.setBussId(codeParam.getResId());
            taskInfo.setTaskId(taskId);
            taskInfo.setDescription(codeParam.getStatusBackup());
            taskInfo.setPriority(-priority);
            taskService.CreateOrRestartTask(taskInfo);

        } catch (Exception e) {
            LOG.error("发送转码任务失败", e);
            lifecycleService.addLifecycleStep(codeParam.getResType(), codeParam.getResId(), false, e.getMessage());
        }
    }


    /**
     * <p>Description:   转码触发方法(使用默认的转码任务地址)           </p>
     * <p> 额外新添加优先级参数          </p>
     * <p>Create Time: 2015年6月10日   </p>
     * <p>Create author: qiling   </p>
     * <p>Update author: liuwx   </p>
     *
     * @param codeParam
     * @param session
     * @param templatePath
     * @param priority
     */
    public void TriggerTransCode(TransCodeParam codeParam, String session, int priority,
                                 String templatePath) {
        String url = Constant.TASK_SUBMIT_URL;
        LOG.error("转码发送的url地址:" + url);
        TriggerTransCode(url, codeParam, session, priority, templatePath);

    }

    /**
     * <p>Description:   转码触发方法           </p>
     * <p> 额外新添加优先级参数          </p>
     * <p>Create Time: 2015年6月10日   </p>
     * <p>Create author: liuwx   </p>
     *
     * @param taskUrl    触发转码任务的url
     * @param codeParam
     * @param session
     * @param priority
     * @param templatePath
     */
    public void TriggerTransCode(String taskUrl, TransCodeParam codeParam, String session, int priority,
                   String templatePath) {
        //实例目录名 eg edu、edu_product ect..
        String csInstanceName=codeParam.getInstanceKey().substring(codeParam.getInstanceKey().indexOf("/")+1);
        Assert.assertNotNull("转码模板地址不能为空", templatePath);
        String location = Constant.CS_INSTANCE_MAP.get(Constant.CS_DEFAULT_INSTANCE).getUrl()
                + "/download?path=" + URLEncoder.encodeURL(codeParam.getSourceFileId());

        String url = taskUrl;
        LOG.error("转码发送的url地址:" + url);
        try {

            Map<String, String> arg = new HashMap<>();
            arg.put("location", location);
            //新增部分内容
            arg.put("session", session);
            String callBackUrl = Constant.LIFE_CYCLE_DOMAIN_URL + "/v0.6/" + codeParam.getResType() + "/transcode/callback";
            arg.put("callback_url", callBackUrl);
            arg.put("cs_api_url", Constant.CS_API_URL);
            arg.put("cs_instance_name", csInstanceName);
            arg.put("task_execute_env", TASK_EXECUTE_ENV);
            if (codeParam.getResType().equals(IndexSourceType.LessonPlansType.getName()) 
                    || codeParam.getResType().equals(IndexSourceType.LearningPlansType.getName())) {
                String coursewares_upload = String.format("/%s/esp/%s", csInstanceName, codeParam.getResType());
                arg.put("coursewares_upload", coursewares_upload);
                String assets_upload = String.format("/%s/esp/%s", csInstanceName, codeParam.getResType());
                arg.put("assets_upload", assets_upload);
            } else {
                String coursewares_upload = String.format("/%s/esp/coursewares", csInstanceName);
                arg.put("coursewares_upload", coursewares_upload);
                String assets_upload = String.format("/%s/esp/assets/ppts", csInstanceName);
                arg.put("assets_upload", assets_upload);
            }
            //转码模板
            arg.put("template_path", templatePath);
            String argument = ObjectUtils.toJson(arg);
            //update bu liuwx 20151013 23:00
            WorkerParam param =null;
            if("assets".equals(codeParam.getResType())){
                ResourceModel resource=ndResourceService.getDetail(codeParam.getResType(),codeParam.getResId(), IncludesConstant.getIncludesList());
                AssetViewModel assetViewModel=CommonHelper.convertViewModelOut(resource,AssetViewModel.class,"assets_type");
                param=WorkerManager.getAssetTransCodeParam(assetViewModel, codeParam.getResType());

            }else {
                param= WorkerManager.getDefaultWorkParam(null, codeParam.getResType());
                //param= WorkerParam.createTranscodeParam(resType);
            }
            if(param==null) {
                throw new LifeCircleException("资源类型编码不支持转码");
            }

            param.setIdentifier(codeParam.getResId());
            param.setPriority(priority);
            param.setArgument(argument);

            //HttpClientUtils.httpPost(url, parameters);
            RestTemplate restTemplate = new RestTemplate();
            //ResponseEntity<String> rt =  restTemplate.postForEntity(url, parameters, String.class);
            ResponseEntity<String> rt = restTemplate.postForEntity(url, param, String.class);

            LOG.info("创建转码任务返回的任务ID:" + rt.getBody());

            String taskId = null;
            if (!StringUtils.isEmpty(rt.getBody())) {
                Map<String, Object> rtMap = BeanMapperUtils.mapperOnString(rt.getBody(), Map.class);
                if (rtMap.get("executionId") != null) {
                    taskId = String.valueOf(rtMap.get("executionId"));
                }
            }
            TaskStatusInfo taskInfo = new TaskStatusInfo();
            taskInfo.setResType(codeParam.getResType());
            taskInfo.setBussType(TaskServiceImpl.TASK_BUSS_TYPE_TRANSCODE);
            taskInfo.setBussId(codeParam.getResId());
            taskInfo.setTaskId(taskId);
            taskInfo.setDescription(codeParam.getStatusBackup());
            taskInfo.setPriority(-priority);
            taskService.CreateOrRestartTask(taskInfo);

        } catch (Exception e) {
            LOG.error("发送转码任务失败", e);
            lifecycleService.addLifecycleStep(codeParam.getResType(), codeParam.getResId(), false, e.getMessage());
        }
    }

    /**
     * @param resource
     * @return
     * @desc:是否能满足转码条件(适用新资源)
     * @createtime: 2015年8月18日
     * @author: liuwx
     * @see nd.esp.service.lifecycle.support.busi.transcode.TransCodeManager#canTransCode(ResourceViewModel, String)
     */
    @Deprecated
    public static boolean canTranscode(ResourceViewModel resource) {
        Assert.assertNotNull("资源视图对象不能为空", resource);
        ResLifeCycleViewModel lifeCycleViewModel = resource.getLifeCycle();
        if (null != lifeCycleViewModel && LifecycleStatus.isNeedTranscode(lifeCycleViewModel.getStatus())) {
            try {
                ModelPropertiesValidUitl.verificationHref(resource);
                return true;
            } catch (Exception e) {
                LOG.error(e.getMessage());
            }

        }
        return false;

    }



    /**
     * @param status
     * @param latestVersion 默认为false 则判断旧资源状态
     * @return
     * @desc: 是否能满足转码条件(适用新旧资源)
     * @createtime: 2015年8月18日
     * @author: liuwx
     */
    public static boolean canTranscode(String status, boolean latestVersion) {
        Assert.assertNotNull("status不能为空", status);
        if (latestVersion) {
            return TransCodeUtil.NEW_CONVERT_STATUS_UNCONVERTED.equals(status);

        }
        return TransCodeUtil.CONVERT_STATUS_UNCONVERTED.equals(status);
    }

    /**
     * @param status
     * @return
     * @desc: 是否能满足转码条件(适用新旧资源)
     * @createtime: 2015年8月18日
     * @author: liuwx
     */
    public static boolean canTranscode(String status) {

        return canTranscode(status, true);
    }


    /**
     * @param latestVersion true 返回新资源类型状态
     * @return
     * @desc:获取转码中状态
     * @createtime: 2015年8月18日
     * @author: liuwx
     */
    public static String getTransIngStatus(boolean latestVersion) {
        return latestVersion ? NEW_CONVERT_STATUS_CONVERTING : CONVERT_STATUS_CONVERTING;

    }

    /**
     * @param latestVersion true 返回新资源类型状态
     * @return
     * @desc:获取转码完成状态
     * @createtime: 2015年8月18日
     * @author: liuwx
     */
    public static String getTransEdStatus(boolean latestVersion) {
        return latestVersion ? NEW_CONVERT_STATUS_CONVERTED : CONVERT_STATUS_CONVERTED;

    }


    /**
     * @param latestVersion true 返回新资源类型状态
     * @return
     * @desc:获取转码失败状态
     * @createtime: 2015年8月18日
     * @author: liuwx
     */
    public static String getTransErrStatus(boolean latestVersion) {
        return latestVersion ? NEW_CONVERT_STATUS_CONVERT_ERR : CONVERT_STATUS_CONVERT_ERR;

    }


    /**
     * 为了兼容视频转码，在这里封装了一层，（用于判断转码类型）
     * 
     * @author linsm
     * @param resourceModel
     * @param resType
     * @since
     */
    public void triggerTransCode(ResourceModel resourceModel, String resType) {
        triggerTransCode(resourceModel, resType, null);
    }
    
    public void triggerTransCode(ResourceModel resourceModel, String resType, String statusBackup) {
        triggerTransCode(resourceModel, resType, statusBackup, false);
    }
    
    public void triggerTransCode(ResourceModel resourceModel, String resType, String statusBackup, boolean bOnlyOgv) {
        if (isVideoTransCode(resourceModel, resType)) {
            triggerVideoTransCode(resourceModel, resType, statusBackup, bOnlyOgv);
        } else if(Constant.AUDIO_TRANSCODE && isAudioTransCode(resourceModel, resType)) {
            triggerVideoTransCode(resourceModel, resType, statusBackup, bOnlyOgv);
        } else if(isImageTransCode(resourceModel,resType)) {
            triggerImageTransCode(resourceModel, resType, statusBackup);
        } else {
            triggerWordOrPptTransCode(resourceModel, resType, statusBackup);
        }
    }
    

    /**
     * @author linsm
     * @param resourceModel
     * @param resType
     * @since
     */
    private void triggerVideoTransCode(ResourceModel resourceModel, String resType, String statusBackup, boolean bOnlyOgv) {
        List<ResTechInfoModel> techInfos = resourceModel.getTechInfoList();
        boolean isSourcePathExist = false;
        boolean isHrefExist = false;
        String sourceLocation = "";
        String hrefLocation = "";
        ResTechInfoModel hrefTechInfo = null;
        for (ResTechInfoModel techInfo : techInfos) {
            if (techInfo.getTitle().equals(TransCodeUtil.CONVERT_SOURCE_KEY)) {
                sourceLocation = techInfo.getLocation();//${ref-path}/edu/esp/lessonplans/2558b42d-ae05-42fc-b62b-797fe554867d.pkg/xxx.ppt
                isSourcePathExist = true;
            }
            if(techInfo.getTitle().equals(TransCodeUtil.CONVERT_STOREINFO_KEY)) {
                hrefLocation = techInfo.getLocation();
                hrefTechInfo = techInfo;
                isHrefExist = true;
            }
            if(isSourcePathExist && isHrefExist) {
                break;
            }
        }
        
        if(!isSourcePathExist && hrefTechInfo!=null && StringUtils.isNotEmpty(hrefLocation)) {
            sourceLocation = hrefLocation;
            isSourcePathExist = true;
            hrefTechInfo.setTitle(TransCodeUtil.CONVERT_SOURCE_KEY);
            TechInfo ti = BeanMapperUtils.beanMapper(hrefTechInfo, TechInfo.class);
            ti.setResource(resourceModel.getIdentifier());
            ti.setResType(resType);
            ti.setRequirements(ObjectUtils.toJson(hrefTechInfo.getRequirements()));
            try {
                techInfoRepository.update(ti);
            } catch (EspStoreException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        if(bOnlyOgv && isVideoTransCode(resourceModel, resType) && isHrefExist) {
            sourceLocation = hrefLocation;
        }
        
        ResLifeCycleModel lifeCycle = resourceModel.getLifeCycle();
        if (!isSourcePathExist) {

            LOG.error("转码资源[" + resourceModel.getIdentifier() + "]source未上传");

            // FIXME 这个只能返回给调用方，没有写到数据库中
            lifeCycle = resourceModel.getLifeCycle();
            lifeCycle.setStatus(TransCodeUtil.getTransErrStatus(true));
            resourceModel.setLifeCycle(lifeCycle);
//            // 更新数据库中的状态
//            CommonHelper.updateStatusInDB(resType, resourceModel.getIdentifier(), TransCodeUtil.getTransErrStatus(true));
            lifecycleService.addLifecycleStep(resType,
                                              resourceModel.getIdentifier(),
                                              false,
                                              "转码资源[" + resourceModel.getIdentifier() + "]source未上传");
            return;
        } else {
            
            LOG.info("source对应的值:" + sourceLocation);
            
            String instanceKey = SessionUtil.getHrefInstanceKey(sourceLocation);
            
            LOG.info("source对应的实例键值:" + instanceKey);//${ref-path}/edu
            
            if (Constant.CS_INSTANCE_MAP.get(instanceKey) == null) {
                
                LOG.error("转码资源[" + resourceModel.getIdentifier() + "]source地址格式错误");
                
                lifeCycle = resourceModel.getLifeCycle();
                lifeCycle.setStatus(TransCodeUtil.getTransErrStatus(true));
                resourceModel.setLifeCycle(lifeCycle);
                // 更新数据库中的状态
//                CommonHelper.updateStatusInDB(resType,
//                                              resourceModel.getIdentifier(),
                // TransCodeUtil.getTransErrStatus(true));
                lifecycleService.addLifecycleStep(resType, resourceModel.getIdentifier(), false, "转码资源["
                        + resourceModel.getIdentifier() + "]source地址格式错误");
                return;
            }
            String path = sourceLocation.replace(TransCodeUtil.REF_PATH, "");
            
            TransCodeParam codeParam = TransCodeParam.build();
            codeParam.buildInstanceKey(instanceKey);
            codeParam.buildResType(resType);
            codeParam.buildResId(resourceModel.getIdentifier());
            codeParam.buildSourceFileId(path);
            codeParam.buildReferer(getReferer());
            codeParam.buildStatusBackup(statusBackup);
            codeParam.setbOnlyOgv(bOnlyOgv);
            if(isAudioTransCode(resourceModel, resType)) {
                codeParam.setSubType(SUBTYPE_AUDIO);
            } else {
                codeParam.setSubType(SUBTYPE_VIDEO);
            }
            transCodeTrigger.triggerVideo(codeParam);
            /*new TranscodeThread(instanceKey,IndexSourceType.LessonPlansType.getName(),
                    lessonPlansModel.getIdentifier(),path,request.getHeader("Referer")).start();*/
        }
        
        

    }
    

    /**
     * 获取referer
     * @return
     * @since 
     */
    private String getReferer() {
        String referer="";
        try{

            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                    .getRequest();
            referer= request.getHeader("Referer");
        }catch (Exception e){
            LOG.warn("获取referer失败:",e.getCause());
        }
        return referer;
    }

    /**
     * 判断是否进行图片转码（检查资源类型:assets,类型：$RT0402）
     *
     * @param resourceModel
     * @param resType
     * @author linsm
     * @return
     * @since
     */
    private boolean isImageTransCode(ResourceModel resourceModel, String resType) {
        if (!IndexSourceType.AssetType.getName().equals(resType)) {
            return false;
        }
        List<ResClassificationModel> categories = resourceModel.getCategoryList();
        if (CollectionUtils.isNotEmpty(categories)) {
            for (ResClassificationModel category : categories) {
                if (category != null) {
                    String ndCode = category.getTaxoncode();
                    if (StringUtils.isNotEmpty(ndCode)) {
                        // 媒体类型:视频
                        if ("$RT0402".equals(ndCode)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * 判断是否进行视频转码（检查资源类型:assets,检查媒体类型：$F030000, 通过前缀来判断，$F03）
     * 
     * @param resourceModel
     * @param resType
     * @author linsm
     * @return
     * @since
     */
    private boolean isVideoTransCode(ResourceModel resourceModel, String resType) {
        if (!IndexSourceType.AssetType.getName().equals(resType)) {
            return false;
        }
        List<ResClassificationModel> categories = resourceModel.getCategoryList();
        if (CollectionUtils.isNotEmpty(categories)) {
            for (ResClassificationModel category : categories) {
                if (category != null) {
                    String ndCode = category.getTaxoncode();
                    if (StringUtils.isNotEmpty(ndCode)) {
                        // 媒体类型:视频
                        if (ndCode.startsWith("$F03")) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * 判断是否进行音频转码（检查资源类型:assets,检查媒体类型：$F020000, 通过前缀来判断，$F03）
     * 
     * @param resourceModel
     * @param resType
     * @author ql
     * @return
     * @since
     */
    private boolean isAudioTransCode(ResourceModel resourceModel, String resType) {
        if (!IndexSourceType.AssetType.getName().equals(resType)) {
            return false;
        }
        List<ResClassificationModel> categories = resourceModel.getCategoryList();
        if (CollectionUtils.isNotEmpty(categories)) {
            for (ResClassificationModel category : categories) {
                if (category != null) {
                    String ndCode = category.getTaxoncode();
                    if (StringUtils.isNotEmpty(ndCode)) {
                        // 媒体类型:音频
                        if (ndCode.startsWith("$F02")) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * @param resourceModel
     * @desc:触发转码任务
     * @createtime: 2015年8月18日
     * @author: qil
     * @author linsm  为了兼容视频转码，修改了方法的名称
     */
    private void triggerImageTransCode(ResourceModel resourceModel, String resType, String statusBackup) {
        String referer="";
        try{

            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                    .getRequest();
            referer= request.getHeader("Referer");
        }catch (Exception e){
            LOG.warn("资源{} 获取referer失败:",resType,e.getCause());
        }

        List<ResTechInfoModel> techInfos = resourceModel.getTechInfoList();
        boolean isHrefExist = false;
        String sourceLocation = "";
        String hrefLocation = "";
        for (ResTechInfoModel techInfo : techInfos) {
            if (techInfo.getTitle().equals(TransCodeUtil.CONVERT_SOURCE_KEY)) {
                sourceLocation = techInfo.getLocation();//${ref-path}/edu/esp/lessonplans/2558b42d-ae05-42fc-b62b-797fe554867d.pkg/xxx.ppt
                isHrefExist = true;
                break;
            }
            if(techInfo.getTitle().equals(TransCodeUtil.CONVERT_STOREINFO_KEY)) {
                hrefLocation = techInfo.getLocation();
            }
        }

        if (!isHrefExist && StringUtils.isNotEmpty(hrefLocation)) {
            sourceLocation = hrefLocation;
        }

        LOG.info("source对应的值:" + sourceLocation);
        String instanceKey = SessionUtil.getHrefInstanceKey(sourceLocation);
        LOG.info("source对应的实例键值:" + instanceKey);//${ref-path}/edu
        if (Constant.CS_INSTANCE_MAP.get(instanceKey) == null) {
            LOG.error("转码资源[" + resourceModel.getIdentifier() + "]source地址格式错误");
            ResLifeCycleModel lifeCycle = resourceModel.getLifeCycle();
            lifeCycle.setStatus(TransCodeUtil.CONVERT_STATUS_CONVERT_ERR);
            resourceModel.setLifeCycle(lifeCycle);
        }
        String path = sourceLocation.replace(TransCodeUtil.REF_PATH, "");
        TransCodeParam codeParam = TransCodeParam.build();
        codeParam.buildInstanceKey(instanceKey);
        codeParam.buildResType(resType);
        codeParam.buildResId(resourceModel.getIdentifier());
        codeParam.buildSourceFileId(path);
        codeParam.buildReferer(referer);
        codeParam.buildStatusBackup(statusBackup);
        transCodeTrigger.triggerImage(codeParam);
        /*new TranscodeThread(instanceKey,IndexSourceType.LessonPlansType.getName(),
                lessonPlansModel.getIdentifier(),path,request.getHeader("Referer")).start();*/
    }

    /**
     * @param resourceModel
     * @desc:触发转码任务
     * @createtime: 2015年8月18日
     * @author: liuwx
     * @author linsm  为了兼容视频转码，修改了方法的名称
     */
    private void triggerWordOrPptTransCode(ResourceModel resourceModel, String resType, String statusBackup) {
        String referer="";
        try{

            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                    .getRequest();
            referer= request.getHeader("Referer");
        }catch (Exception e){
            LOG.warn("资源{} 获取referer失败:",resType,e.getCause());
        }

        List<ResTechInfoModel> techInfos = resourceModel.getTechInfoList();
        boolean isHrefExist = false;
        String sourceLocation = "";
        String hrefLocation = "";
        ResTechInfoModel hrefTechInfo = null;
        for (ResTechInfoModel techInfo : techInfos) {
            if (techInfo.getTitle().equals(TransCodeUtil.CONVERT_SOURCE_KEY)) {
                sourceLocation = techInfo.getLocation();//${ref-path}/edu/esp/lessonplans/2558b42d-ae05-42fc-b62b-797fe554867d.pkg/xxx.ppt
                isHrefExist = true;
                break;
            }
            if(techInfo.getTitle().equals(TransCodeUtil.CONVERT_STOREINFO_KEY)) {
                hrefLocation = techInfo.getLocation();
                hrefTechInfo = techInfo;
            }
        }
        
//        if(!isHrefExist && resType.equals(IndexSourceType.LessonPlansType.getName()) 
//                && StringUtils.isNotEmpty(hrefLocation) && (hrefLocation.endsWith(".doc")||hrefLocation.endsWith(".docx"))) {
//            sourceLocation = hrefLocation;
//            isHrefExist = true;
//            hrefTechInfo.setTitle(TransCodeUtil.CONVERT_SOURCE_KEY);
//        }
        
        ResLifeCycleModel lifeCycle = resourceModel.getLifeCycle();
        if (!isHrefExist) {
            LOG.error("转码资源[" + resourceModel.getIdentifier() + "]source未上传");
            lifeCycle = resourceModel.getLifeCycle();
            lifeCycle.setStatus(TransCodeUtil.getTransErrStatus(true));
            resourceModel.setLifeCycle(lifeCycle);
        } else {
            LOG.info("source对应的值:" + sourceLocation);
            String instanceKey = SessionUtil.getHrefInstanceKey(sourceLocation);
            LOG.info("source对应的实例键值:" + instanceKey);//${ref-path}/edu
            if (Constant.CS_INSTANCE_MAP.get(instanceKey) == null) {
                LOG.error("转码资源[" + resourceModel.getIdentifier() + "]source地址格式错误");
                lifeCycle = resourceModel.getLifeCycle();
                lifeCycle.setStatus(TransCodeUtil.CONVERT_STATUS_CONVERT_ERR);
                resourceModel.setLifeCycle(lifeCycle);
            }
            String path = sourceLocation.replace(TransCodeUtil.REF_PATH, "");
            TransCodeParam codeParam = TransCodeParam.build();
            codeParam.buildInstanceKey(instanceKey);
            codeParam.buildResType(resType);
            codeParam.buildResId(resourceModel.getIdentifier());
            codeParam.buildSourceFileId(path);
            codeParam.buildReferer(referer);
            codeParam.buildStatusBackup(statusBackup);
            transCodeTrigger.trigger(codeParam);
            /*new TranscodeThread(instanceKey,IndexSourceType.LessonPlansType.getName(),
                    lessonPlansModel.getIdentifier(),path,request.getHeader("Referer")).start();*/
        }
    }


    private static class TranscodePriority {
        private String referer;
        private int priority;

        public TranscodePriority(String referer, int priority) {
            this.referer = referer;
            this.priority = priority;
        }

        public boolean check(String referer) {
            if (this.referer.equals(referer)) {
                return true;
            }
            return false;
        }

        public int getPriority() {
            return priority;
        }

    }


    /**
     * 提交转码请求到调度系统
     * 
     * @author linsm
     * @param codeParam
     * @param session
     * @param priority
     * @throws EspStoreException 
     * @throws IOException 
     * @since
     */
    public void triggerVideoTransCode(TransCodeParam codeParam, String session, int priority) 
            throws EspStoreException, IOException {
        String url = Constant.TASK_SUBMIT_URL;

        LOG.info("转码发送的url地址:" + url);

        String location = Constant.CS_INSTANCE_MAP.get(codeParam.getInstanceKey()).getUrl() + "/download?path="
                + URLEncoder.encodeURL(codeParam.getSourceFileId());

            Map<String, Object> arg = new HashMap<>();

            arg.put("location", location);

            LOG.info("location:" + location);

            arg.put("session", session);

            LOG.info("session:" + session);

            // 通过路径不同，走向视频转码回调
            String callBackUrl = Constant.LIFE_CYCLE_DOMAIN_URL + "/v0.6/" + codeParam.getResType()
                    + "/transcode/videoCallback";

            arg.put("callback_api", callBackUrl);

            LOG.info("callback_api:" + callBackUrl);

            arg.put("cs_api_url", Constant.CS_API_URL);
            arg.put("task_execute_env", TASK_EXECUTE_ENV);
            
            // 视频转码所独有的参数：
            String fileType = codeParam.getSourceFileId().substring(codeParam.getSourceFileId().lastIndexOf('.') + 1);

            LOG.info("视频格式:" + fileType);

            List<String> scripts = ConvertRuleSet.getConvertRuleSet().productScript(fileType);
            
            // 暂时与source 放在同一个目录
            String targetPath = codeParam.getSourceFileId().substring(0, codeParam.getSourceFileId().lastIndexOf('/'));
            
            if(codeParam.isbOnlyOgv() && SUBTYPE_VIDEO.equals(codeParam.getSubType())) {
                targetPath = targetPath.substring(0, targetPath.lastIndexOf(".pkg")+4);
                for(Iterator<String> iter=scripts.iterator(); iter.hasNext(); ) {
                    String cmd = iter.next();
                    if(cmd.startsWith("ffmpeg -i")) {
                        iter.remove();
                    }
                }
            }

            LOG.info("视频转码脚本:" + scripts);

            arg.put("commands", scripts);

            LOG.info("视频转码后目标目录：" + targetPath);

            arg.put("target_location", targetPath);

            Map<String, String> extParam = new HashMap<String, String>();
            if(SUBTYPE_VIDEO.equals(codeParam.getSubType())) {
                extParam.put("targetFmt", "mp4");
                extParam.put("subtype", SUBTYPE_VIDEO);
            } else if(SUBTYPE_AUDIO.equals(codeParam.getSubType())) {
                extParam.put("targetFmt", "mp3");
                extParam.put("subtype", SUBTYPE_AUDIO);
            }
            extParam.put("coverNum", String.valueOf(getCoverNum(scripts)));
            
            LOG.info("coverNum：" + extParam.get("coverNum"));
            
            arg.put("ext_param", extParam);

            String argument = ObjectUtils.toJson(arg);
            WorkerParam param = WorkerParam.createVideoTranscodeParam();

            param.setIdentifier(codeParam.getResId());
            param.setPriority(priority);
            param.setArgument(argument);


            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> rt = restTemplate.postForEntity(url, param, String.class);

            LOG.info("创建转码任务返回的任务ID:" + rt.getBody());

            String taskId = null;
            if (!StringUtils.isEmpty(rt.getBody())) {
                Map<String, Object> rtMap = BeanMapperUtils.mapperOnString(rt.getBody(), Map.class);
                if (rtMap.get("executionId") != null) {
                    taskId = String.valueOf(rtMap.get("executionId"));
                }
            }
            TaskStatusInfo taskInfo = new TaskStatusInfo();
            taskInfo.setResType("assets");
            taskInfo.setBussType(TaskServiceImpl.TASK_BUSS_TYPE_TRANSCODE);
            taskInfo.setBussId(codeParam.getResId());
            taskInfo.setTaskId(taskId);
            taskInfo.setDescription(codeParam.getStatusBackup());
            taskInfo.setPriority(-priority);
            taskService.CreateOrRestartTask(taskInfo);
            
    }

    /**
     * 在截图命令中取得 -join 后的参数，并计算出 大图包含小图的个数
     * @author linsm
     * @param scripts
     * @return
     * @since 
     */
    private static int getCoverNum(List<String> scripts) {
        String cutCommand = getCommand(scripts,CUT_COMMAND,CUT_COVER_TARGET);
        String param = getParam(cutCommand,PARAM_JOIN);
        if(StringUtils.isNotEmpty(param)){
            String[] paramChunks = param.split("x");
            if(paramChunks != null && paramChunks.length>1){
               return Integer.valueOf(paramChunks[0])*Integer.valueOf(paramChunks[1]);
            }
        }
        return DEFAULT_COVER_NUM;
    }

    /**
     * 取得命令行中的参数
     * @author linsm
     * @param command
     * @param paramJoin
     * @return
     * @since 
     */
    private static String  getParam(String command, String paramJoin) {
        if(StringUtils.isEmpty(command)||StringUtils.isEmpty(paramJoin)){
            return null;
        }
        //保证参数非空第一
        String temp = command.substring(command.indexOf(paramJoin)+paramJoin.length()+1);
        
        if(StringUtils.isNotEmpty(temp)){
            String[] chunks = temp.split(" ");
            if(chunks != null && chunks.length>0){
                return chunks[0];
            }
        }
        return null;
    }

    /**
     * 根据命令名和关键字取得命令
     * @author linsm
     * @param scripts
     * @param cutCommand
     * @param specialKey 主要用于区分同一命令的不同用途（如cover, cut)
     * @return
     * @since 
     */
    private static String getCommand(List<String> scripts, String cutCommand,String specialKey) {
        if(CollectionUtils.isEmpty(scripts)){
            return null;
        }
        for(String script:scripts){
            if(StringUtils.isNotEmpty(script)&& script.contains(cutCommand)&&script.contains(specialKey)){
                return script;
            }
        }
        return null;
    }
    
    /**
     * just for test getCoverNum
     * @author linsm
     * @param args
     * @since
     */
    public static void main(String[] args) {
        System.out.println(getCoverNum(ConvertRuleSet.getConvertRuleSet().productScript("ts")));
    }

}
