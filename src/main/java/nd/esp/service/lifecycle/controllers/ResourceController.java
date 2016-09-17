package nd.esp.service.lifecycle.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nd.esp.service.lifecycle.educommon.models.ResourceModel;
import nd.esp.service.lifecycle.educommon.services.NDResourceService;
import nd.esp.service.lifecycle.educommon.services.impl.CommonServiceHelper;
import nd.esp.service.lifecycle.educommon.vos.constant.IncludesConstant;
import nd.esp.service.lifecycle.entity.PackagingParam;
import nd.esp.service.lifecycle.models.ArchiveModel;
import nd.esp.service.lifecycle.services.packaging.v06.PackageService;
import nd.esp.service.lifecycle.services.task.v06.QueryTaskService;
import nd.esp.service.lifecycle.support.Constant;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.busi.PackageUtil;
import nd.esp.service.lifecycle.support.busi.PackageUtil.PackStatus;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;

import org.jboss.logging.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import nd.esp.service.lifecycle.repository.model.TaskStatusInfo;
import nd.esp.service.lifecycle.repository.sdk.TaskStatusInfoRepository;

/**
 * 资源对外发布的接口： 1、资源的上传：提供资源实体文件的上传操作，返回的是拥有授权令牌的存储地址
 * 2、资源的按需下载：在符合资源【课件、课件颗粒等】，通过main文件加载page或者addon
 * 3、资源的打包下载：资源进行打包下载。如果是素材等，单文件不需要打包操作，直接下载 4、资源的创建：资源创建此处主要是指元数据记录的创建。
 * 5、获取详细：获取元数据信息 6、高级分类检索：按照分类体系进行维度检索
 * 7、元数据的修改：修改元数据信息，参数是元数据信息和文件信息，如果元数据为null，文件的id传入进来，那么直接返回更新实体文件的路径
 * 8、资源的删除【伪删除】：删除资源信息，包括删除符合资源中的文件。
 *
 * @author johnny
 * @version 1.0
 * @created 24-3月-2015 12:06:08
 */
@RestController
@RequestMapping({"/v0.6/{res_type}"})
public class ResourceController {

	private final Logger LOG = LoggerFactory.getLogger(ResourceController.class);

	@Autowired
	@Qualifier("PackageServiceImpl")
	private PackageService packageService;
	
	@Autowired
	private PackageUtil packageUtil;
	
	@Autowired
	private QueryTaskService queryTaskService;
	
	@Autowired
    private TaskStatusInfoRepository taskRepository;
	
	@Autowired
    private NDResourceService ndResourceService;
	
	@Autowired
    private CommonServiceHelper commonHelper;
	
	/**
	 * @desc 创建session接口
	 * @param fid
	 * @param uid
	 */
	@RequestMapping(value="/create_session/{uid}",method = RequestMethod.GET)
	public String requestSession(@PathVariable String uid){

		return 	this.packageService.createSession(uid);

	}
    /**
     * 资源的打包下载：资源进行打包下载。如果是素材等，单文件不需要打包操作，直接下载
     * ---打包本地测试方法
     *
     * @URLpattern：/{res_apptype /{id}/archive @Method:Get
     *                           {res_apptype}:资源的应用类型，主要指素材，课件等等 {id}：主要是资源的id
     *
     * @param fid
     * @param res_type
     */
//	  @RequestMapping(value = "/{id}/archive", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
//    public ArchiveModel requestArchivingNew(@PathVariable String id, 
//            @RequestParam(value = "target", required = false) String target,
//            @PathVariable String res_type, @Param String uid, @Param boolean webp_first) {
//        // ${ref-path}/edu_product/esp/coursewares/id.pkg/main.xml
//        if(StringUtils.isEmpty(target)) {
//            target = PackageUtil.TARGET_DEFALUT;
//        }
//
//        commonHelper.assertDownloadable(res_type);
//        // 根据uuid获取资源详细信息
//        ResourceModel resource = null;
//        try {
//            resource = ndResourceService.getDetail(res_type,id,IncludesConstant.getIncludesList());
//        } catch (LifeCircleException e) { //资源不存在
//            throw e;
//        }
//        
//        try {
//            if (resource == null) {
//                LOG.error("资源不存在");
//                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
//                        LifeCircleErrorMessageMapper.ResourceRequestArchivingFail.getCode(), "资源不存在");
//            }
//            String path = PackageUtil.getResourcePath(resource);
//            // 策略判断,如果path不符合规范,则提示
//            // ${ref-path}/edu/esp/%s/%s.pkg/main.xml
//            if ((!path.contains(Constant.CS_INSTANCE_MAP.get(Constant.CS_DEFAULT_INSTANCE).getPath())
//                    && !path.contains(Constant.CS_INSTANCE_MAP.get(Constant.CS_DEFAULT_INSTANCE_OTHER).getPath()))
//                  || !path.contains(id+".pkg")) {
//                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
//                        LifeCircleErrorMessageMapper.ResourceRequestArchivingFail.getCode(), "元数据的href不符合规范格式");
//            }
//
//            // 2.开始打包
//            String openID = id + "_" + target;
//            if (webp_first) {
//                openID += "_webp";
//            }
//            taskService.CreateOrRestartTask(res_type, TaskServiceImpl.TASK_BUSS_TYPE_PACK, openID, "NULL", null);
//
//            packageUtil.archiving(path, target, id, res_type, uid, webp_first);
//
//        } catch (Exception e) {
//            LOG.error("打包请求出错", e);
//            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
//                    LifeCircleErrorMessageMapper.ResourceRequestArchivingFail.getCode(),
//                    LifeCircleErrorMessageMapper.ResourceRequestArchivingFail.getMessage() + ":" + e.getMessage());
//        }
//
//        ArchiveModel archiveModel = PackStatus.PENDING.getArchiveModel();
//        archiveModel.setArchiveState(PackStatus.START.getStatus());
//        archiveModel.setMessage("开始打包（本地测试）");
//
//        return archiveModel;
//    }

    /**
     * 资源的打包下载：资源进行打包下载。如果是素材等，单文件不需要打包操作，直接下载
     *
     * @URLpattern：/{res_apptype /{id}/archive @Method:Get
     *                           {res_apptype}:资源的应用类型，主要指素材，课件等等 {id}：主要是资源的id
     *
     * @param fid
     * @param res_type
     */
    @RequestMapping(value = "/{id}/archive", method = RequestMethod.POST, produces ={MediaType.APPLICATION_JSON_VALUE})
    public ArchiveModel requestArchivingTask(@PathVariable String id, @PathVariable String res_type,
            @Param String target, @Param String uid, @Param boolean webp_first,
            @RequestParam(defaultValue="true") boolean no_ogg) {

        commonHelper.assertDownloadable(res_type);
        
        // 根据uuid获取资源详细信息
        ResourceModel resource = null;
        try {
            resource = ndResourceService.getDetail(res_type,id,IncludesConstant.getValidIncludes(IncludesConstant.INCLUDE_TI));
        } catch (LifeCircleException e) { //资源不存在
            throw e;
        }
        ArchiveModel archiveModel = null;
        target = StringUtils.isEmpty(target) ? PackageUtil.TARGET_DEFALUT : target;
        
        PackagingParam param = new PackagingParam();
        param.setUid(uid);
        param.setResType(res_type);
        param.setUuid(id);
        param.setPath(PackageUtil.getResourcePath(resource));
        param.setTarget(target);
        param.setbWebpFirst(webp_first);
        param.setbNoOgg(no_ogg);
        try {
            if (resource == null) {
                LOG.error("打包资源不存在");
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        LifeCircleErrorMessageMapper.ResourceRequestArchivingFail.getCode(), "打包资源不存在");
            }
            
            packageService.triggerPackaging(param);
        } catch (JpaSystemException e) {
        	if(e.getMessage().contains("Duplicate entry")) {
	        	try {
					packageService.triggerPackaging(param);
				} catch (Exception e1) {
					throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
			                  LifeCircleErrorMessageMapper.ResourceRequestArchivingFail.getCode(),
			                  LifeCircleErrorMessageMapper.ResourceRequestArchivingFail.getMessage() + ":" + e.getMessage());
				}
        	}
        } catch (Exception e) {
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                  LifeCircleErrorMessageMapper.ResourceRequestArchivingFail.getCode(),
                  LifeCircleErrorMessageMapper.ResourceRequestArchivingFail.getMessage() + ":" + e.getMessage());
        }

        try {
            // 更新status状态
            archiveModel = PackStatus.PENDING.getArchiveModel();
            archiveModel.setArchiveState(PackStatus.START.getStatus());
            archiveModel.setMessage(PackStatus.START.getMessage());

        } catch (Exception e) {
            LOG.error("获取打包信息失败:", e);
            throw new RuntimeException("获取打包信息失败:" + e.getMessage());
        }

        return archiveModel;
    }
    
    /**
     * 资源的打包下载：资源进行打包下载。如果是素材等，单文件不需要打包操作，直接下载
     *
     * @URLpattern：/{res_apptype /{id}/archive @Method:Get
     *                           {res_apptype}:资源的应用类型，主要指素材，课件等等 {id}：主要是资源的id
     *
     * @param fid
     * @param res_type
     */
    @RequestMapping(value = "/{id}/archive_webp", method = RequestMethod.POST, produces ={MediaType.APPLICATION_JSON_VALUE})
    public ArchiveModel requestArchivingWebp(@PathVariable String id, @PathVariable String res_type,
            @Param String target, @Param String uid,
            @RequestParam(defaultValue="true") boolean no_ogg) {

        return requestArchivingTask(id, res_type, target, uid, true, no_ogg);
    }
    
    /**
     * 资源的打包下查询
     *
     * @URLpattern：/{res_apptype /{id}/archive @Method:Get
     *                           {res_apptype}:资源的应用类型，主要指素材，课件等等 {id}：主要是资源的id
     *
     * @param fid
     * @param res_type
     */
    @RequestMapping(value = "/{id}/archiveinfo", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ArchiveModel querryArchive(@PathVariable String id, @Param String target, 
            @PathVariable String res_type, @Param String uid, @Param boolean webp) {
        target=StringUtils.isEmpty(target)?PackageUtil.TARGET_DEFALUT:target;
        
        // 根据uuid获取资源详细信息
        ResourceModel resource = null;
        try {
            resource = ndResourceService.getDetail(res_type,id,IncludesConstant.getValidIncludes(""));
        } catch (LifeCircleException e) { //资源不存在
            throw e;
        }
        if (resource == null) {
            throw new LifeCircleException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.ResourceRequestArchivingFail.getCode(),"资源不存在");
        } 
        
        ArchiveModel archiveModel =null;
        String bussId = id+"_"+target;
        if(webp) {
            bussId += "_webp";
        }
        PackStatus status = PackageUtil.PackStatus.UNPACK;
        try {
            TaskStatusInfo pkRepository = taskRepository.get(bussId);
            if(pkRepository!=null) {
                status = PackageUtil.PackStatus.getPackageStatus(pkRepository.getStatus());
            }
            archiveModel = status.getArchiveModel();
            
            if(pkRepository!=null) {
                if (status == PackStatus.READY) {
                    Map<String, String> st = ObjectUtils.fromJson(pkRepository.getStoreInfo(), Map.class);
                    if(CollectionUtils.isNotEmpty(st)){
                        String url = Constant.CS_INSTANCE_MAP.get(Constant.CS_DEFAULT_INSTANCE).getUrl() + "/static" + st.get("location");
                        archiveModel.setAccessUrl(url);
                        archiveModel.setMd5(st.get("md5"));
                        
                    }
                }else  if(status==PackStatus.PENDING ){//如果是打包中，触发一个异步的请求操作
                    final List<TaskStatusInfo> taskInfos = new ArrayList<TaskStatusInfo>();
                    taskInfos.add(pkRepository);
                    try {
                        new Thread() {
                            @Override
                            public void run() {
                                queryTaskService.QueryTaskStatus(taskInfos);
                            }
                        }.start();
                    }catch (Exception e){
                        LOG.error("单个异步查询调度任务失败",e);
                    }

                }else  if(status==PackStatus.ERROR){//错误的概率比较小,因此放在后面
                    String errMsg =  pkRepository.getErrMsg();
                    if(null!=errMsg) {
                        archiveModel.setMessage(errMsg);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("获取打包信息失败:",e);
            throw new RuntimeException("获取打包信息失败:"+e.getMessage());
        }
        
        
        
        return archiveModel;
    }
}
