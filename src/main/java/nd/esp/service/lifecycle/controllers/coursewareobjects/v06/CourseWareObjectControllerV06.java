package nd.esp.service.lifecycle.controllers.coursewareobjects.v06;

import nd.esp.service.lifecycle.entity.elasticsearch.Resource;
import nd.esp.service.lifecycle.models.v06.CourseWareObjectModel;
import nd.esp.service.lifecycle.services.coursewareobjects.v06.CourseWareObjectServiceV06;
import nd.esp.service.lifecycle.services.elasticsearch.AsynEsResourceService;
import nd.esp.service.lifecycle.services.offlinemetadata.OfflineService;
import nd.esp.service.lifecycle.support.annotation.MarkAspect4Format2Category;
import nd.esp.service.lifecycle.support.annotation.MarkAspect4OfflineJsonToCS;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.support.busi.ValidResultHelper;
import nd.esp.service.lifecycle.support.busi.elasticsearch.ResourceTypeSupport;
import nd.esp.service.lifecycle.support.enums.OperationType;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;
import nd.esp.service.lifecycle.vos.coursewareobjects.v06.CourseWareObjectViewModel;
import nd.esp.service.lifecycle.vos.valid.ValidCoursewareObject4UpdateGroup;
import nd.esp.service.lifecycle.vos.valid.ValidCoursewareObjectGroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 课件颗粒V0.6 API
 * 
 * @author caocr
 */
@RestController
@RequestMapping("/v0.6/coursewareobjects")
public class CourseWareObjectControllerV06 {
    private static final Logger LOG = LoggerFactory.getLogger(CourseWareObjectControllerV06.class);

    private static final int CONTROLLER_CREATE_TYPE = 0;// 新增课件颗粒操作
    private static final int CONTROLLER_UPDATE_TYPE = 1;// 修改课件颗粒操作
    private static final int CONTROLLER_PATCH_TYPE = 2;// 局部修改课件颗粒操作
    @Autowired
    @Qualifier("courseWareObjectServiceV06")
    private CourseWareObjectServiceV06 courseWareObjectService;

    @Autowired
    private OfflineService offlineService;
    @Autowired
    private AsynEsResourceService esResourceOperation;

    /**
     * 课件颗粒创建
     * 
     * @param viewModel 课件颗粒对象
     * @param validResult BindingResult
     * @param id 课件颗粒id
     * @return
     * @since
     */
    @MarkAspect4Format2Category
    @MarkAspect4OfflineJsonToCS
    @RequestMapping(value = "/{id}", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public CourseWareObjectViewModel create(@Validated(ValidCoursewareObjectGroup.class) @RequestBody CourseWareObjectViewModel viewModel,
                                            BindingResult validResult,
                                            @PathVariable String id) {
        // 校验入参
        checkParams(viewModel, validResult, id, CONTROLLER_CREATE_TYPE);

        viewModel = coursewareObjectApi(viewModel, CONTROLLER_CREATE_TYPE);

        return viewModel;
    }

    /**
     * 课件颗粒修改
     * 
     * @param viewModel 课件颗粒对象
     * @param validResult BindingResult
     * @param id 课件颗粒id
     * @return
     * @since
     */
    @MarkAspect4Format2Category
    @MarkAspect4OfflineJsonToCS
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public CourseWareObjectViewModel update(@Validated(ValidCoursewareObject4UpdateGroup.class) @RequestBody CourseWareObjectViewModel viewModel,
                                            BindingResult validResult,
                                            @PathVariable String id) {
        // 校验入参
        checkParams(viewModel, validResult, id, CONTROLLER_UPDATE_TYPE);

        viewModel = coursewareObjectApi(viewModel, CONTROLLER_UPDATE_TYPE);

        return viewModel;
    }

    /**
     * 课件颗粒局部修改
     *
     * @param viewModel 课件颗粒对象
     * @param validResult BindingResult
     * @param id 课件颗粒id
     * @return
     * @since
     */
    @MarkAspect4Format2Category
    @RequestMapping(value = "/{id}", method = RequestMethod.PATCH, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public CourseWareObjectViewModel patch(@Validated(ValidCoursewareObject4UpdateGroup.class) @RequestBody CourseWareObjectViewModel viewModel,
                                            BindingResult validResult, @PathVariable String id,
                                            @RequestParam(value = "notice_file", required = false,defaultValue = "true") boolean notice){
        // 校验入参
        checkParams(viewModel, validResult, id, CONTROLLER_PATCH_TYPE);

        viewModel = coursewareObjectApi(viewModel, CONTROLLER_PATCH_TYPE);

        if(notice) {
            offlineService.writeToCsAsync(ResourceNdCode.coursewareobjects.toString(), id);
        }

        // offline metadata(coverage) to elasticsearch
        if (ResourceTypeSupport.isValidEsResourceType(ResourceNdCode.coursewareobjects.toString())) {
            esResourceOperation.asynAdd(
                    new Resource(ResourceNdCode.coursewareobjects.toString(), id));
        }

        return viewModel;
    }

    /**
     * 对入参进行校验
     * 
     * @param viewModel 课件颗粒对象
     * @param validResult BindingResult
     * @param id 课件颗粒id
     * @param type 业务类型
     * @since
     */
    private void checkParams(CourseWareObjectViewModel viewModel,
                             BindingResult validResult,
                             String id,
                             int type) {
    	viewModel.setIdentifier(id);
        // 入参合法性校验
        if (type == CONTROLLER_CREATE_TYPE) {
            ValidResultHelper.valid(validResult,
                                    "LC/CREATE_COURSEWAREOBJECT_PARAM_VALID_FAIL",
                                    "CourseWareObjectControllerV06",
                                    "create");
            CommonHelper.inputParamValid(viewModel, "11111",OperationType.CREATE);
        } else if (type == CONTROLLER_UPDATE_TYPE) {
            ValidResultHelper.valid(validResult,
                                    "LC/UPDATE_COURSEWAREOBJECT_PARAM_VALID_FAIL",
                                    "CourseWareObjectControllerV06",
                                    "update");
            CommonHelper.inputParamValid(viewModel, "11111",OperationType.UPDATE);
        }
    }

    /**
     * 业务统一方法
     * 
     * @param viewModel 课件颗粒对象
     * @param type 业务类型
     * @return CourseWareObjectViewModel
     * @since
     */
    private CourseWareObjectViewModel coursewareObjectApi(CourseWareObjectViewModel viewModel, int type) {
        CourseWareObjectModel model = null;
        if (type == CONTROLLER_CREATE_TYPE) {
            model = CommonHelper.convertViewModelIn(viewModel,
                    CourseWareObjectModel.class,
                    ResourceNdCode.coursewareobjects);
            // 创建课件颗粒
            
            LOG.info("课件颗粒V06创建课件颗粒操作，业务逻辑处理");
            
            model = courseWareObjectService.createCourseWareObject(model);
        } else if (type == CONTROLLER_UPDATE_TYPE) {
            model = CommonHelper.convertViewModelIn(viewModel,
                    CourseWareObjectModel.class,
                    ResourceNdCode.coursewareobjects);
            // 修改课件颗粒
            
            LOG.info("课件颗粒v06更新课件颗粒操作，业务逻辑处理");
            
            model = courseWareObjectService.updateCourseWareObject(model);
        } else if (type == CONTROLLER_PATCH_TYPE) {
            model = CommonHelper.convertViewModelIn(viewModel,
                    CourseWareObjectModel.class,
                    ResourceNdCode.coursewareobjects,true);
            // 修改课件颗粒

            LOG.info("课件颗粒v06局部更新课件颗粒操作，业务逻辑处理");

            model = courseWareObjectService.patchCourseWareObject(model);
        }

        viewModel = CommonHelper.convertViewModelOut(model, CourseWareObjectViewModel.class);

        return viewModel;
    }

}
