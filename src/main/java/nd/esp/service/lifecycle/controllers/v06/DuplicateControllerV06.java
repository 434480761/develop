package nd.esp.service.lifecycle.controllers.v06;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nd.esp.service.lifecycle.controllers.ResourceController;
import nd.esp.service.lifecycle.educommon.models.ResLifeCycleModel;
import nd.esp.service.lifecycle.educommon.models.ResTechInfoModel;
import nd.esp.service.lifecycle.educommon.models.ResourceModel;
import nd.esp.service.lifecycle.educommon.services.NDResourceService;
import nd.esp.service.lifecycle.educommon.services.impl.CommonServiceHelper;
import nd.esp.service.lifecycle.educommon.services.impl.NDResourceServiceImpl;
import nd.esp.service.lifecycle.educommon.support.ParameterVerificationHelper;
import nd.esp.service.lifecycle.educommon.vos.ResCoverageViewModel;
import nd.esp.service.lifecycle.educommon.vos.ResLifeCycleViewModel;
import nd.esp.service.lifecycle.educommon.vos.ResTechInfoViewModel;
import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;
import nd.esp.service.lifecycle.educommon.vos.constant.IncludesConstant;
import nd.esp.service.lifecycle.entity.cs.CsSession;
import nd.esp.service.lifecycle.models.AccessModel;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.support.Constant;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.support.busi.CopyHelper;
import nd.esp.service.lifecycle.support.busi.ModelPropertiesUtil;
import nd.esp.service.lifecycle.support.busi.ResourceTypesUtil;
import nd.esp.service.lifecycle.support.busi.SessionUtil;
import nd.esp.service.lifecycle.support.busi.TransCodeUtil;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @title 资源拷贝
 * <p>支持v0.6最新的资源类型,目前已测试通过的有素材,课件,教案,课件颗粒</p>
 * @desc
 * @atuh lwx
 * @createtime on 2015年8月19日 下午5:15:43
 * @update on 2015年8月26日 下午5:03:43 将日志从common.log改成slf.log
 * @update on 2016.0107 新增用户ID参数
 * @see ResourceController
 */
@RestController
@RequestMapping("/v0.6/{res_type}")
public class DuplicateControllerV06 {
    private static final Logger LOG = LoggerFactory.getLogger(DuplicateControllerV06.class);

    @Autowired
    private CopyHelper copyHelper;

    @Autowired
    private NDResourceService ndResourceService;

    @Autowired
    CommonServiceHelper commonServiceHelper;

    final LifeCircleErrorMessageMapper copyFailMapper = LifeCircleErrorMessageMapper.CopyFail;
    final String failCode = copyFailMapper.getCode();
    
    private final static String TI_KEY_HREF = "href";
    private final static String TI_KEY_SOURCE = "source";
    
    /**
     * @param id
     * @param resType
     * @param include 默认 {@link IncludesConstant#getIncludesList()}
     * @return
     * @desc: 拷贝方法入口
     * <p>去掉0.3版本中队列的支持flag</p>
     * @createtime: 2015年8月19日
     * @author: liuwx
     * @update by liuwx at 20151022 修订覆盖范围等属性未被同步拷贝的bug
     */
    @RequestMapping(value = "/{uuid}/actions/copy", method = RequestMethod.POST)
    public ResourceViewModel copyResources(@PathVariable(value = "uuid") String id,
                                           @PathVariable(value = "res_type") String resType,
                                           @RequestParam(value = "include", required = false, defaultValue = "TI,LC,EDU,CG,CR") String include,
                                           @RequestParam(value = "to", required = false, defaultValue = "") String to,
                                           @RequestParam(value = "isAll", required = false, defaultValue = "false") boolean isAll,
                                           @RequestParam(value = "creator", required = false) String creator,
                                           @RequestParam(value = "isComplete", required = false, defaultValue = "false") boolean isComplete) {
    	
    	long startTime = System.currentTimeMillis();
        //校验资源类型 不仅仅局限于教案,课件,素材,课件颗粒
        // ResourceTypesUtil.checkResType(resType, LifeCircleErrorMessageMapper.ResourceTypeNotSupportFail);
        commonServiceHelper.assertDownloadable(resType);

        //获取资源对象(由于getDetail方法中已经对异常进行捕获和处理,这里就一并去掉了)
        //原先是 {@link IncludesConstant#getIncludesList()},然后在去filter,现在是获取明细的时候,直接带入参数
        List<String> availableIncludes = IncludesConstant.getValidIncludes(include);
        List<ResCoverageViewModel> coverages = new ArrayList<>();

        /**
         * 获取源资源,并进行相关前置判断
         * condition 1：生命周期
         * condition 2：生命周期对应的状态范围
         * condition 3：实例是否合法，为拷贝做判断(也可以放在拷贝那边去做处理)
         * */
        ResourceModel resourceModel = ndResourceService.getDetail(resType, id, availableIncludes, isAll);
        //ndResourceService.getDetail中如果资源不存在会抛出异常,因此resourceModel不会为null,不会出现空指针异常
        ResLifeCycleModel cycleModel = resourceModel.getLifeCycle();
        //判断对象的生命周期属性,如果为空,不允许发起拷贝
        if (null == cycleModel || StringUtils.isEmpty(cycleModel.getStatus())) {

            LifeCircleErrorMessageMapper CheckLifecycleFail = LifeCircleErrorMessageMapper.CheckLifecycleFail;

            LOG.error(CheckLifecycleFail.getMessage());

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR, CheckLifecycleFail);
        }
        String status = cycleModel.getStatus();
        //todo
        //等后续数据规范了,下面这段校验需要开启
        /*   boolean checkStatus=false;
        for(LifecycleStatus ss:LifecycleStatus.values()){
            
            if(ss.getCode().equals(status)){
                checkStatus=true;
                break;
            }
        }
        if(!checkStatus){
            LOG.error("资源生命周期属性中status状态不对");
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR, "LC/COPY_FAIL", "资源生命周期属性中status状态不对");
            
        }*/

        boolean belongToTransCodeType = TransCodeUtil.isConverEd(status, true) || TransCodeUtil.isConverEd(status, false) || TransCodeUtil.specialConverse(resType, status);
        //判断是否是需要转码的类型
        if (ResourceTypesUtil.belongtoTranscodeType(resType)) {
            if (!belongToTransCodeType) {

                LOG.warn("不合法的状态,不允许拷贝.status={}", status);

                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR, failCode, "不合法的状态[" + status + "],不允许拷贝");
            }
        }
        // 获取Href属性
        ResTechInfoModel techInfoModel = ModelPropertiesUtil.getAssignTechInfo(resourceModel, TI_KEY_HREF);
        boolean isAssets_pic = false;//特殊处理素材图片
        if (techInfoModel == null && IndexSourceType.AssetType.getName().equals(resType)) {
            techInfoModel = ModelPropertiesUtil.getAssignTechInfo(resourceModel, TI_KEY_SOURCE);
            isAssets_pic = true;
        }
        if (techInfoModel == null) {

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR, failCode, "技术属性没有对应的数据");
        }

        String href = techInfoModel.getLocation();
        //通过href换取对应的实例名
        String instanceKey = SessionUtil.getHrefInstanceKey(href);
        if (StringUtils.isEmpty(instanceKey)) {

            LOG.warn("未定义的实例:" + instanceKey);

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR, failCode, "未定义的实例:" + instanceKey);
        }

        //根据实例名获取覆盖范围值
        String coverage = StringUtils.EMPTY;

        ResCoverageViewModel resCoverageViewModel = null;
        //校验to的合法性
        if (StringUtils.hasText(to)) {
            //校验合法性
            ParameterVerificationHelper.coverageVerification(to);
            resCoverageViewModel = ParameterVerificationHelper.convertResCoverageViewModel(to);
        }

        /* edu_product/esp/
        edu/esp/coursewares --coverage
        edu/rsd/worksapce rsd/worksapce*/
        String organization = NDResourceServiceImpl.getRootPathFromLocation(href);
        if (organization.contains("esp")) {
            if (!Constant.CS_DEFAULT_INSTANCE.equals(instanceKey)) {
                coverage = Constant.DEFAULT_COVERAGE_VALUE;
            }
        } else {
            coverage = organization.substring(organization.indexOf("/", 1) + 1);
        }

        //获取上传信息
        AccessModel uploadResponse = ndResourceService.getUploadUrl(resType, Constant.DEFAULT_UPLOAD_URL_ID,
                CsSession.CS_DEFAULT_UID, false, coverage);
        
        LOG.info("资源拷贝接口， 获取session用时："+(System.currentTimeMillis()-startTime)+"ms");
        startTime = System.currentTimeMillis();
        
        String newUUID = copyHelper.copyOnLC(instanceKey, resType, resourceModel, uploadResponse, coverage);
        
        LOG.info("资源拷贝接口， 调用cs接口拷贝资源文件用时："+(System.currentTimeMillis()-startTime)+"ms");
        startTime = System.currentTimeMillis();
        
        //todo 保留后相关属性,全部保留,后续可能会根据业务进行调整filterXX
        // store_info中只保留href属性
        //copyHelper.filterStoreinfo(copyModel, availableIncludes);
        //转成前端需要的viewmodel
        // ResourceViewModel resourceViewModel = convertToViewModel(resType, copyModel);
        resourceModel.setIdentifier(newUUID);
        resourceModel.setLanguage("zh-CN");

        ResourceViewModel resourceViewModel = CommonHelper.changeToView(resourceModel, resType, availableIncludes, commonServiceHelper);
        //需要将lifecycle中的状态进行替换 update 20151022  by liuwx 屏蔽掉 程序不再做处理
        /*
        if (belongToTransCodeType) {
            //需要将状态替换成新资源能识别到的状态
            //这里使用try catch是因为旧数据,有可能出现lifecycle为空的情况,为了不影响正常流程
            try {
                ResLifeCycleViewModel cycleViewModel = resourceViewModel.getLifeCycle();
                cycleViewModel.setStatus(TransCodeUtil.getTransEdStatus(true));
            } catch (Exception e) {
                LOG.warn("设置拷贝后的lifecycle状态属性报错:", e);
            }

        }*/
        //对tech_info中的路径进行修改
        Map<String, ? extends ResTechInfoViewModel> techs = resourceViewModel.getTechInfo();
        Map<String, ResTechInfoViewModel> new_tech = new HashMap<>();
        if (CollectionUtils.isNotEmpty(techs)) {

            for (Map.Entry<String, ? extends ResTechInfoViewModel> entry : techs.entrySet()) {
                String key = entry.getKey();
                ResTechInfoViewModel value = entry.getValue();
                if (value != null) {
                	if(isComplete){
                		String location = value.getLocation();
                        if (StringUtils.hasText(location)) {
                            value.setLocation(location.replace(id, resourceViewModel.getIdentifier()));

                        }
                        String entryStr = value.getEntry();
                        if (StringUtils.hasText(entryStr)) {
                            value.setEntry(entryStr.replace(id, resourceViewModel.getIdentifier()));
                        }
                	}else if ((isAssets_pic && TI_KEY_SOURCE.equals(key) || !TI_KEY_SOURCE.equals(key))) {
                        String location = value.getLocation();
                        if (StringUtils.hasText(location)) {
                            value.setLocation(location.replace(id, resourceViewModel.getIdentifier()));

                        }
                        String entryStr = value.getEntry();
                        if (StringUtils.hasText(entryStr)) {
                            value.setEntry(entryStr.replace(id, resourceViewModel.getIdentifier()));
                        }
                    }
                    new_tech.put(key, value);
                }
            }
        }
        resourceViewModel.setTechInfo(new_tech);
        
		// 对preview中的路径进行修改
		if (isComplete) {
			Map<String, String> oldPreview = resourceViewModel.getPreview();
			Map<String, String> newPreview = new HashMap<String, String>();
			if (CollectionUtils.isNotEmpty(oldPreview)) {
				for (Map.Entry<String, String> entry : oldPreview.entrySet()) {
					String key = entry.getKey();
					String value = entry.getValue();
					if (StringUtils.hasText(value)) {
						value = value.replace(resType + "/" + id, resType + "/" + resourceViewModel.getIdentifier());
							
						newPreview.put(key, value);
					}
				}
			}
			
			resourceViewModel.setPreview(newPreview);
		}
        
        //校验to的合法性
        if (null != resCoverageViewModel) {
            String title = resourceViewModel.getTitle();
            if (StringUtils.hasText(title)) {
                if (title.length() > 50) {
                    title = title.substring(0, 47) + "...";
                }
            }
            resCoverageViewModel.setTargetTitle(title);
            coverages.add(resCoverageViewModel);
        }
        //设置覆盖范围
        List<? extends ResCoverageViewModel> originCoverages = resourceViewModel.getCoverages();
        if (CollectionUtils.isNotEmpty(coverages)) {
            if (CollectionUtils.isNotEmpty(originCoverages)) {
                coverages.addAll(originCoverages);
            }
            resourceViewModel.setCoverages(coverages);
        }
        if (checkCreator(creator)) {

            ResLifeCycleViewModel resLifeCycleViewModel = resourceViewModel.getLifeCycle();
            if (null != resLifeCycleViewModel) {
                resLifeCycleViewModel.setCreator(creator);
            } else {
                LOG.warn("但是技术属性对象为空,设置新的creator:{}失败", creator);

            }
        }

        //回调各资源API
        copyHelper.callToCreate(resType, resourceViewModel);
        
        LOG.info("资源拷贝接口， 调用LC接口创建资源元数据用时："+(System.currentTimeMillis()-startTime)+"ms");
        startTime=System.currentTimeMillis();


        resourceViewModel.setCoverages(null);
        resourceViewModel.setmIdentifier(newUUID);
        return resourceViewModel;
    }

    @SuppressWarnings("unused")
	@Deprecated
    private ResourceViewModel convertToViewModel(String resType, ResourceModel copyModel) {

        boolean lcFlag = true;
        if (copyModel.getLifeCycle() == null) {
            lcFlag = false;
        }

        //model出参转换
        ResourceViewModel resourceViewModel = null;
        try {

            resourceViewModel = CommonHelper.convertViewModelOut(copyModel, ResourceViewModel.class, resType);
        } catch (Exception e) {
            LOG.warn("拷贝-->转换视图模型失败", e);
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR, copyFailMapper);
        }
        //ResourceViewModel resourceViewModel = ObjectUtils.fromJson(ObjectUtils.toJson(copyModel), ResourceViewModel.class);
        //avm = BeanMapperUtils.beanMapper(am, AssetViewModel.class);
        //历史留意问题 对素材的categories中的key进行单独处理
        String type = "res_type";
        if (resType.equals("assets")) {
            type = "assets_type";
        }
        //通用输出
        resourceViewModel.setCategories(CommonHelper.list2map4Categories(copyModel.getCategoryList(), type));
        resourceViewModel.setTechInfo(CommonHelper.list2Map4TechInfo(copyModel.getTechInfoList()));

        //无须返回前台，将relations、coverages赋值为空
        resourceViewModel.setRelations(null);
        //resourceViewModel.setCoverages(null);
        if (!lcFlag) {
            resourceViewModel.setLifeCycle(null);
        }

        return resourceViewModel;
    }


    /**
     * 判断creator是否都是数字
     */
    private boolean creatorIsNum(String creator) {
        Pattern pattern = Pattern.compile("[0-9]+");
        Matcher matcher = pattern.matcher(creator);
        return matcher.matches();
    }

    /**
     * 校验creator是否符合条件 eg 123456 is ok ,123abc is bad
     * <p>如果符合条件,则替换掉源资源的creator</p>
     *
     * @param creator
     * @return
     */
    private boolean checkCreator(String creator) {
        if (StringUtils.hasText(creator) && creatorIsNum(creator)) {
            LOG.info("传入的creator:{}符合条件", creator);
            return true;
        }
        return false;
    }
}
