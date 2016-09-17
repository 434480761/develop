package nd.esp.service.lifecycle.controllers.coursewareobjecttemplate.v06;

import nd.esp.service.lifecycle.entity.elasticsearch.Resource;
import nd.esp.service.lifecycle.models.v06.CourseWareObjectTemplateModel;
import nd.esp.service.lifecycle.services.coursewareobjecttemplate.v06.CourseWareObjectTemplateServiceV06;
import nd.esp.service.lifecycle.services.elasticsearch.AsynEsResourceService;
import nd.esp.service.lifecycle.services.offlinemetadata.OfflineService;
import nd.esp.service.lifecycle.support.annotation.MarkAspect4Format2Category;
import nd.esp.service.lifecycle.support.annotation.MarkAspect4OfflineJsonToCS;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.support.busi.ValidResultHelper;
import nd.esp.service.lifecycle.support.busi.elasticsearch.ResourceTypeSupport;
import nd.esp.service.lifecycle.support.enums.OperationType;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;
import nd.esp.service.lifecycle.vos.coursewareobjecttemplate.v06.CoursewareObjectTemplateViewModel;
import nd.esp.service.lifecycle.vos.valid.Valid4UpdateGroup;
import nd.esp.service.lifecycle.vos.valid.ValidGroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 课件颗粒模板接口V0.6API
 * @author liur
 * */
@RestController
@RequestMapping(value = "/v0.6/coursewareobjecttemplates")
public class CoursewareObjectTemplateController {

    private static final Logger LOG = LoggerFactory.getLogger(CoursewareObjectTemplateController.class);

    private static final int CONTROLLER_CREATE_TYPE = 1;

    private static final int CONTROLLER_UPDATE_TYPE = 2;

    @Autowired
    @Qualifier(value = "CourseWareObjectTemplateServiceImplV06")
    private CourseWareObjectTemplateServiceV06 courseTemplateService;

    @Autowired
    private OfflineService offlineService;
    @Autowired
    private AsynEsResourceService esResourceOperation;

    @MarkAspect4Format2Category
    @MarkAspect4OfflineJsonToCS
    @RequestMapping(value = "/{id}", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE },
            produces = { MediaType.APPLICATION_JSON_VALUE })
    public CoursewareObjectTemplateViewModel create(
            @Validated(ValidGroup.class) @RequestBody CoursewareObjectTemplateViewModel ctvm,
            BindingResult validResult, @PathVariable String id) {

        checkParams(ctvm, validResult, id, CONTROLLER_CREATE_TYPE);
        ctvm.setIdentifier(id);

        CourseWareObjectTemplateModel ctm = CommonHelper.convertViewModelIn(ctvm , CourseWareObjectTemplateModel.class,
                ResourceNdCode.coursewareobjecttemplates);

        LOG.info("课件颗粒模板操作--创建功能，业务逻辑处理");
        
        // 业务逻辑操作
        ctm = courseTemplateService.createCourseWareObjectTemplate(ctm);

        ctvm=CommonHelper.convertViewModelOut(ctm, CoursewareObjectTemplateViewModel.class);

        return ctvm;
    }

    @MarkAspect4Format2Category
    @MarkAspect4OfflineJsonToCS
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = { MediaType.APPLICATION_JSON_VALUE },
            produces = { MediaType.APPLICATION_JSON_VALUE })
    public CoursewareObjectTemplateViewModel update(
            @Validated(Valid4UpdateGroup.class) @RequestBody CoursewareObjectTemplateViewModel ctvm,
            BindingResult validResult, @PathVariable String id) {

        checkParams(ctvm, validResult, id, CONTROLLER_UPDATE_TYPE);
        ctvm.setIdentifier(id);

        CourseWareObjectTemplateModel ctm = CommonHelper.convertViewModelIn(ctvm , CourseWareObjectTemplateModel.class,
                ResourceNdCode.coursewareobjecttemplates);
        
        LOG.info("课件颗粒模板操作--更新功能，业务逻辑处理");

        ctm = courseTemplateService.updateCourseWareObjectTemplate(ctm);

        ctvm=CommonHelper.convertViewModelOut(ctm, CoursewareObjectTemplateViewModel.class);

        return ctvm;
    }

    @MarkAspect4Format2Category
    @RequestMapping(value = "/{id}", method = RequestMethod.PATCH, consumes = { MediaType.APPLICATION_JSON_VALUE },
            produces = { MediaType.APPLICATION_JSON_VALUE })
    public CoursewareObjectTemplateViewModel patch(
            @Validated(Valid4UpdateGroup.class) @RequestBody CoursewareObjectTemplateViewModel ctvm,
            BindingResult validResult, @PathVariable String id,
            @RequestParam(value = "notice_file", required = false,defaultValue = "true") boolean notice){

//        checkParams(ctvm, validResult, id, CONTROLLER_UPDATE_TYPE);
        ctvm.setIdentifier(id);

        CourseWareObjectTemplateModel ctm = CommonHelper.convertViewModelIn(ctvm , CourseWareObjectTemplateModel.class,
                ResourceNdCode.coursewareobjecttemplates, true);

        LOG.info("课件颗粒模板操作--更新功能，业务逻辑处理");

        ctm = courseTemplateService.patchCourseWareObjectTemplate(ctm);

        ctvm=CommonHelper.convertViewModelOut(ctm, CoursewareObjectTemplateViewModel.class);

        if(notice) {
            offlineService.writeToCsAsync(ResourceNdCode.coursewareobjecttemplates.toString(), id);
        }
        // offline metadata(coverage) to elasticsearch
        if (ResourceTypeSupport.isValidEsResourceType(ResourceNdCode.coursewareobjecttemplates.toString())) {
            esResourceOperation.asynAdd(
                    new Resource(ResourceNdCode.coursewareobjecttemplates.toString(), id));
        }

        return ctvm;
    }

    private void checkParams( CoursewareObjectTemplateViewModel ctvm, BindingResult validResult,
             String id, int type) {
    	ctvm.setIdentifier(id);
        /**
         * 如参的合法性验证
         * */
        if (type == CONTROLLER_CREATE_TYPE) {
            
            ValidResultHelper.valid(validResult, "LC/CREATE_COURSEWAREOBJECT_PARAM_VALID_FAIL",
                    "CoursewareObjectTemplateControllerV06", "create");
            CommonHelper.inputParamValid(ctvm, null, OperationType.CREATE);
            
        }
        else if (type == CONTROLLER_UPDATE_TYPE) {
            
            ValidResultHelper.valid(validResult, "LC/UPDATE_COURSEWAREOBJECT_PARAM_VALID_FAIL",
                    "CoursewareObjectTemplateControllerV06", "update");
            CommonHelper.inputParamValid(ctvm, null, OperationType.UPDATE);
        }
    }
}
