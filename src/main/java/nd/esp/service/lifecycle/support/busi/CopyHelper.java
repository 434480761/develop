package nd.esp.service.lifecycle.support.busi;

import java.util.List;

import nd.esp.service.lifecycle.educommon.models.ResourceModel;
import nd.esp.service.lifecycle.educommon.services.NDResourceService;
import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;
import nd.esp.service.lifecycle.educommon.vos.constant.IncludesConstant;
import nd.esp.service.lifecycle.entity.cs.CsSession;
import nd.esp.service.lifecycle.models.AccessModel;
import nd.esp.service.lifecycle.services.ContentService;
import nd.esp.service.lifecycle.support.Constant;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.cs.ContentServiceHelper;
import nd.esp.service.lifecycle.utils.CollectionUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.nd.gaea.client.http.WafSecurityHttpClient;

@Configuration
@Component
public class CopyHelper {
    private static final Logger LOG = LoggerFactory.getLogger(CopyHelper.class);

    @Autowired
    private ContentService contentService;

    final LifeCircleErrorMessageMapper errorMessageMapper=    LifeCircleErrorMessageMapper.CopyFail;

    @Autowired
    private NDResourceService ndResourceService;

    /** 
     * <p>Description: 拷贝功能             </p>
     * <p>Create Time: 2015年8月12日   </p>
     * <p>Create author: luiwx   </p>
     * @param resType
     * @param srcModel
     * @param uploadResponse
     * @param coverage
     * @return
     */
    public String copyOnLC(String instanceKey, String resType, ResourceModel  srcModel,
            AccessModel uploadResponse, String coverage) {

        String newCoursewareId = "";
        newCoursewareId = uploadResponse.getUuid().toString();

        // 开始拷贝
        // 获取原始课件的上传信息
        try {
            AccessModel lastUploadResponse = ndResourceService.getUploadUrl(resType, srcModel.getIdentifier(),
                    CsSession.CS_DEFAULT_UID, true, coverage);
            //LOG.info("实例:" + instanceKey + ",coverage:" + coverage);
            // 原始课件的根目录
            String srcPath = lastUploadResponse.getDistPath();
            String parentPath = getParentPath(srcPath);
            String descPath = uploadResponse.getDistPath();
            String dirname = descPath.substring(descPath.lastIndexOf("/") + 1);

            //LOG.info("href对应的实例键值:" + instanceKey);// ${ref-path}/edu
            // 支持多实例操作
            //String instanceKey=StringUtils.isEmpty(coverage)?Constant.CS_DEFAULT_INSTANCE:Constant.CS_DEFAULT_INSTANCE_OTHER;
            Constant.CSInstanceInfo instanceInfo = Constant.CS_INSTANCE_MAP.get(instanceKey);
            //Constant.CSInstanceInfo instanceInfo = CommonHelper.getCsInstance(lastUploadResponse.getDistPath());
            //update by liuwx at 20151202 将权限放到最大
            String  path = instanceInfo.getPath();
            String rootPath = path.substring(0,path.indexOf("/",1));
            // 通过LC接口获取顶层实例的session
            CsSession csSession = contentService.getAssignSession(rootPath, instanceInfo.getServiceId());
            String topSession = csSession.getSession();
            // 创建目录项
//            contentService.createDir(parentPath, dirname, topSession);//原http api请求
            //CS SDK方式
            ContentServiceHelper.createDir(parentPath, dirname, instanceInfo.getServiceName(), topSession);
            //拷贝目录项
//            contentService.copyDir(srcPath, descPath, topSession);//原http api请求
            //CS SDK方式
            ContentServiceHelper.copyDirOnNdr(srcPath, descPath, instanceInfo.getServiceName(), topSession);
        } catch (Exception e) {

            LOG.warn("调用CS失败", e);

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR, "CS/CALL_METHOD_FAIL", "调用CS方法错误:"
                    + e.getMessage());
        }
        
		// ResourceModel newModel = new ResourceModel();
		/*
		 * // todo BeanUtils.copyProperties(srcModel, newModel);
		 * newModel.setIdentifier(newCoursewareId); // 目录替换 // todo
		 * 明确下是否是写入到storeinfo的href中 ResTechInfoModel hrefModel
		 * =ModelPropertiesUtil.getAssignTechInfo(srcModel, "href");
		 * 
		 * List<ResTechInfoModel>techInfoModels= srcModel.getTechInfoList();
		 * for(ResTechInfoModel techInfoModel:techInfoModels ){
		 * if(techInfoModel.getTitle().equals("href")){ String
		 * newLocation=replaceHref
		 * (techInfoModel.getLocation(),srcModel.getIdentifier
		 * (),newCoursewareId); techInfoModel.setLocation(newLocation); break; }
		 * 
		 * } newModel.setTechInfoList(techInfoModels);
		 */
		// 开始和更新时间 不赋值

        return newCoursewareId;
    }
    
    /**
     * 获取父类的路径
     */
    private String getParentPath(String path) {

        return path.substring(0, path.lastIndexOf("/"));
    }
    
    /**  
     * <p>Description:  拷贝完成之后，触发创建            </p>
     * <p>Create Time: 2015年8月12日   </p>
     * <p>Create author: Administrator   </p>
     * @param resType
     * @param copyModel
     */
    public void callToCreate(String resType, ResourceViewModel copyModel ){
        
        WafSecurityHttpClient wafSecurityHttpClient = new WafSecurityHttpClient(Constant.WAF_CLIENT_RETRY_COUNT);
        String url = Constant.LIFE_CYCLE_DOMAIN_URL + "/v0.6/" + resType + "/" + copyModel.getIdentifier();
        LOG.info("拷贝回调创建元数据请求地址:"+url);
        HttpHeaders httpHeaders = new HttpHeaders();
        HttpEntity<ResourceViewModel> entity = new HttpEntity<ResourceViewModel>(copyModel, httpHeaders);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        ResourceViewModel callBack= null;
        try {
            callBack=wafSecurityHttpClient.executeForObject(url, HttpMethod.POST, entity, ResourceViewModel.class);
        } catch (Exception e) {
            if (null == callBack) {

                LOG.warn("创建资源metadata失败",e);

                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,errorMessageMapper.getCode() ,errorMessageMapper.getMessage()+ e.getMessage());
            }
        }
    }
    
    // store_info中只保留href属性
    /**	
     * @desc: 过滤拷贝后的extends属性 
     * <p>业务相对比较简单，因此就没有考虑用过滤器来做了</p> 
     * @createtime: 2015年8月20日 
     * @author: liuwx 
     * @param copyModel
     * @param availableIncludes
     */
    public void filterStoreinfo( ResourceModel    copyModel,List<String>availableIncludes ){
        
        if(CollectionUtils.isNotEmpty(availableIncludes)){
            boolean ti_filter=true;
            boolean lc_filter=true;
            boolean edu_filter=true;
            boolean cg_filter=true;
            boolean cr_filter=true;
            if(availableIncludes.containsAll(IncludesConstant.getIncludesList())){
                 ti_filter=false;
                 lc_filter=false;
                 edu_filter=false;
                 cg_filter=false;
                 cr_filter=false;
            }else {
                ti_filter= !availableIncludes.contains(IncludesConstant.INCLUDE_TI);
                lc_filter= !availableIncludes.contains(IncludesConstant.INCLUDE_LC);
                edu_filter= !availableIncludes.contains(IncludesConstant.INCLUDE_EDU);
                cg_filter= !availableIncludes.contains(IncludesConstant.INCLUDE_CG);
                cr_filter= !availableIncludes.contains(IncludesConstant.INCLUDE_CR);
            }
            if(ti_filter){
                copyModel.setTechInfoList(null);
            }
            if(lc_filter){
                copyModel.setLifeCycle(null);
            }
            if(edu_filter){
                copyModel.setEducationInfo(null);
            }
            if(cg_filter){
                copyModel.setCategoryList(null);
            }
            if(cr_filter){
                copyModel.setCopyright(null);
            }
        }
    }
    
    @SuppressWarnings("unused")
	private String replaceHref(String oldHref,String oldUUID,String newUUID){
        String newHref="";
        newHref= oldHref.replace(oldUUID, newUUID);
        return newHref;
    }
}
