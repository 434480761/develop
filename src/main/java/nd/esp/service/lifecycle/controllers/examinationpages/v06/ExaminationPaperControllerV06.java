package nd.esp.service.lifecycle.controllers.examinationpages.v06;

import javax.servlet.http.HttpServletRequest;

import nd.esp.service.lifecycle.entity.elasticsearch.Resource;
import nd.esp.service.lifecycle.models.v06.ExaminationPaperModel;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.services.elasticsearch.AsynEsResourceService;
import nd.esp.service.lifecycle.services.examinationpapers.v06.ExaminationPaperServiceV06;
import nd.esp.service.lifecycle.services.offlinemetadata.OfflineService;
import nd.esp.service.lifecycle.support.annotation.MarkAspect4Format2Category;
import nd.esp.service.lifecycle.support.annotation.MarkAspect4OfflineJsonToCS;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.support.busi.ValidResultHelper;
import nd.esp.service.lifecycle.support.busi.elasticsearch.ResourceTypeSupport;
import nd.esp.service.lifecycle.support.enums.OperationType;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;
import nd.esp.service.lifecycle.vos.examinationpapers.v06.ExaminationPaperViewModel;
import nd.esp.service.lifecycle.vos.valid.Valid4UpdateGroup;
import nd.esp.service.lifecycle.vos.valid.ValidGroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 试卷V0.6 API
 * 
 * @author xuzy
 */
@RestController
@RequestMapping(value={"/v0.6/examinationpapers","/v0.6/exercisesset"})
public class ExaminationPaperControllerV06 {
    private static final Logger LOG = LoggerFactory.getLogger(ExaminationPaperControllerV06.class);

    private static final int CONTROLLER_CREATE_TYPE = 0;// 新增试卷操作
    private static final int CONTROLLER_UPDATE_TYPE = 1;// 修改试卷操作
    private static final int CONTROLLER_PATCH_TYPE = 2;// 局部修改试卷操作
    @Autowired
    @Qualifier("examinationPaperServiceV06")
    private ExaminationPaperServiceV06 examinationPaperService;

    @Autowired
    private OfflineService offlineService;
    
    @Autowired
    private AsynEsResourceService esResourceOperation;

    /**
     * 试卷创建
     * 
     * @param viewModel 试卷对象
     * @param validResult BindingResult
     * @param id 试卷id
     * @return
     * @since
     */
    @MarkAspect4Format2Category
    @MarkAspect4OfflineJsonToCS
    @RequestMapping(value = "/{id}", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ExaminationPaperViewModel create(@Validated(ValidGroup.class) @RequestBody ExaminationPaperViewModel viewModel,
                                            BindingResult validResult,
                                            @PathVariable String id,HttpServletRequest request) {
    	String resType = getResType(request);
    	
        // 校验入参
        checkParams(viewModel, validResult, id, CONTROLLER_CREATE_TYPE,resType);

        viewModel = examinationPaperApi(viewModel, CONTROLLER_CREATE_TYPE,resType);

        return viewModel;
    }

    /**
     * 试卷修改
     * 
     * @param viewModel 试卷对象
     * @param validResult BindingResult
     * @param id 试卷id
     * @return
     * @since
     */
    @MarkAspect4Format2Category
    @MarkAspect4OfflineJsonToCS
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ExaminationPaperViewModel update(@Validated(Valid4UpdateGroup.class) @RequestBody ExaminationPaperViewModel viewModel,
                                            BindingResult validResult,
                                            @PathVariable String id,HttpServletRequest request) {
    	String resType = getResType(request);
    	
        // 校验入参
        checkParams(viewModel, validResult, id, CONTROLLER_UPDATE_TYPE,resType);

        viewModel = examinationPaperApi(viewModel, CONTROLLER_UPDATE_TYPE,resType);

        return viewModel;
    }

    /**
     * 试卷局部修改
     *
     * @param viewModel 试卷对象
     * @param validResult BindingResult
     * @param id 试卷id
     * @return
     * @since
     */
    @MarkAspect4Format2Category
    @RequestMapping(value = "/{id}", method = RequestMethod.PATCH, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ExaminationPaperViewModel patch(@Validated(Valid4UpdateGroup.class) @RequestBody ExaminationPaperViewModel viewModel,
                                            BindingResult validResult, @PathVariable String id,
                                            @RequestParam(value = "notice_file", required = false,defaultValue = "true") boolean notice,HttpServletRequest request){
    	String resType = getResType(request);
    	
        // 校验入参
        checkParams(viewModel, validResult, id, CONTROLLER_PATCH_TYPE,resType);

        viewModel = examinationPaperApi(viewModel, CONTROLLER_PATCH_TYPE,resType);

        if(notice) {
            offlineService.writeToCsAsync(resType, id);
        }

        // offline metadata(coverage) to elasticsearch
        if (ResourceTypeSupport.isValidEsResourceType(resType)) {
            esResourceOperation.asynAdd(
                    new Resource(resType, id));
        }

        return viewModel;
    }

    /**
     * 对入参进行校验
     * 
     * @param viewModel 试卷对象
     * @param validResult BindingResult
     * @param id 试卷id
     * @param type 业务类型
     * @since
     */
    private void checkParams(ExaminationPaperViewModel viewModel,
                             BindingResult validResult,
                             String id,
                             int type,
                             String resType) {
    	viewModel.setIdentifier(id);
        // 入参合法性校验
        if (type == CONTROLLER_CREATE_TYPE) {
            ValidResultHelper.valid(validResult,
                                    "LC/CREATE_"+capitalResType(resType)+"_PARAM_VALID_FAIL",
                                    "examinationPaperControllerV06",
                                    "create");
            CommonHelper.inputParamValid(viewModel, "11111",OperationType.CREATE);
        } else if (type == CONTROLLER_UPDATE_TYPE) {
            ValidResultHelper.valid(validResult,
                                    "LC/UPDATE_"+capitalResType(resType)+"_PARAM_VALID_FAIL",
                                    "examinationPaperControllerV06",
                                    "update");
            CommonHelper.inputParamValid(viewModel, "11111",OperationType.UPDATE);
        }
    }

    /**
     * 业务统一方法
     * 
     * @param viewModel 试卷对象
     * @param type 业务类型
     * @return ExaminationPaperViewModel
     * @since
     */
    private ExaminationPaperViewModel examinationPaperApi(ExaminationPaperViewModel viewModel, int type,String resType) {
        ExaminationPaperModel model = null;
        if (type == CONTROLLER_CREATE_TYPE) {
            model = CommonHelper.convertViewModelIn(viewModel,
            		ExaminationPaperModel.class,
            		getResourceNdCode(resType));
            // 创建试卷
            
            LOG.info(resType+"v06创建操作，业务逻辑处理");
            
            model = examinationPaperService.createExaminationPaper(model,resType);
        } else if (type == CONTROLLER_UPDATE_TYPE) {
            model = CommonHelper.convertViewModelIn(viewModel,
            		ExaminationPaperModel.class,
            		getResourceNdCode(resType));
            // 修改试卷
            
            LOG.info(resType+"v06更新操作，业务逻辑处理");
            
            model = examinationPaperService.updateExaminationPaper(model,resType);
        } else if (type == CONTROLLER_PATCH_TYPE) {
            model = CommonHelper.convertViewModelIn(viewModel,
            		ExaminationPaperModel.class,
            		getResourceNdCode(resType),true);
            // 修改试卷

            LOG.info(resType+"v06局部更新操作，业务逻辑处理");

            model = examinationPaperService.patchExaminationPaper(model,resType);
        }

        viewModel = CommonHelper.convertViewModelOut(model, ExaminationPaperViewModel.class);

        return viewModel;
    }
    
    /**
     * 获取资源类型
     * @param request
     * @return
     */
    private String getResType(HttpServletRequest request){
    	String url = request.getRequestURI();
    	String resType = url.substring(url.indexOf("v0.6")+5, url.lastIndexOf("/"));
    	return resType;
    }
    
    /**
     * 根据资源类型获取对应的枚举
     * @param resType
     * @return
     */
    private ResourceNdCode getResourceNdCode(String resType){
    	if(ResourceNdCode.examinationpapers.toString().equals(resType)){
    		return ResourceNdCode.examinationpapers;
    	}else{
    		return ResourceNdCode.exercisesset;
    	}
    }
    
    /**
     * 将资源类型变为大写
     * @param resType
     * @return
     */
    private String capitalResType(String resType){
    	if(resType.equals(IndexSourceType.ExaminationPapersType.getName())){
    		return "EXAMINATIONPAPER";
    	}else if(resType.equals(IndexSourceType.Exercisesset.getName())){
    		return "EXERCISESSET";
    	}
    	return "";
    }

}
