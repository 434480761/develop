package nd.esp.service.lifecycle.controllers.examinationpages.v06;

import nd.esp.service.lifecycle.entity.elasticsearch.Resource;
import nd.esp.service.lifecycle.models.v06.ExaminationPaperModel;
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
@RequestMapping("/v0.6/examinationpapers")
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
                                            @PathVariable String id) {
        // 校验入参
        checkParams(viewModel, validResult, id, CONTROLLER_CREATE_TYPE);

        viewModel = examinationPaperApi(viewModel, CONTROLLER_CREATE_TYPE);

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
                                            @PathVariable String id) {
        // 校验入参
        checkParams(viewModel, validResult, id, CONTROLLER_UPDATE_TYPE);

        viewModel = examinationPaperApi(viewModel, CONTROLLER_UPDATE_TYPE);

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
                                            @RequestParam(value = "notice_file", required = false,defaultValue = "true") boolean notice){
        // 校验入参
        checkParams(viewModel, validResult, id, CONTROLLER_PATCH_TYPE);

        viewModel = examinationPaperApi(viewModel, CONTROLLER_PATCH_TYPE);

        if(notice) {
            offlineService.writeToCsAsync(ResourceNdCode.examinationpapers.toString(), id);
        }

        // offline metadata(coverage) to elasticsearch
        if (ResourceTypeSupport.isValidEsResourceType(ResourceNdCode.examinationpapers.toString())) {
            esResourceOperation.asynAdd(
                    new Resource(ResourceNdCode.examinationpapers.toString(), id));
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
                             int type) {
    	viewModel.setIdentifier(id);
        // 入参合法性校验
        if (type == CONTROLLER_CREATE_TYPE) {
            ValidResultHelper.valid(validResult,
                                    "LC/CREATE_EXAMINATIONPAPER_PARAM_VALID_FAIL",
                                    "examinationPaperControllerV06",
                                    "create");
            CommonHelper.inputParamValid(viewModel, "11111",OperationType.CREATE);
        } else if (type == CONTROLLER_UPDATE_TYPE) {
            ValidResultHelper.valid(validResult,
                                    "LC/UPDATE_EXAMINATIONPAPER_PARAM_VALID_FAIL",
                                    "eourseWareObjectControllerV06",
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
    private ExaminationPaperViewModel examinationPaperApi(ExaminationPaperViewModel viewModel, int type) {
        ExaminationPaperModel model = null;
        if (type == CONTROLLER_CREATE_TYPE) {
            model = CommonHelper.convertViewModelIn(viewModel,
            		ExaminationPaperModel.class,
                    ResourceNdCode.examinationpapers);
            // 创建试卷
            
            LOG.info("试卷V06创建试卷操作，业务逻辑处理");
            
            model = examinationPaperService.createExaminationPaper(model);
        } else if (type == CONTROLLER_UPDATE_TYPE) {
            model = CommonHelper.convertViewModelIn(viewModel,
            		ExaminationPaperModel.class,
                    ResourceNdCode.examinationpapers);
            // 修改试卷
            
            LOG.info("试卷v06更新试卷操作，业务逻辑处理");
            
            model = examinationPaperService.updateExaminationPaper(model);
        } else if (type == CONTROLLER_PATCH_TYPE) {
            model = CommonHelper.convertViewModelIn(viewModel,
            		ExaminationPaperModel.class,
                    ResourceNdCode.examinationpapers,true);
            // 修改试卷

            LOG.info("试卷v06局部更新试卷操作，业务逻辑处理");

            model = examinationPaperService.patchExaminationPaper(model);
        }

        viewModel = CommonHelper.convertViewModelOut(model, ExaminationPaperViewModel.class);

        return viewModel;
    }

}
