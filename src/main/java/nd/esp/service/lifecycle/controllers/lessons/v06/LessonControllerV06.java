package nd.esp.service.lifecycle.controllers.lessons.v06;

import java.util.UUID;

import nd.esp.service.lifecycle.entity.elasticsearch.Resource;
import nd.esp.service.lifecycle.models.v06.LessonModel;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.services.elasticsearch.AsynEsResourceService;
import nd.esp.service.lifecycle.services.lessons.v06.LessonsServiceV06;
import nd.esp.service.lifecycle.services.notify.NotifyInstructionalobjectivesService;
import nd.esp.service.lifecycle.services.offlinemetadata.OfflineService;
import nd.esp.service.lifecycle.support.annotation.MarkAspect4Format2Category;
import nd.esp.service.lifecycle.support.annotation.MarkAspect4OfflineToES;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.support.busi.ValidResultHelper;
import nd.esp.service.lifecycle.support.busi.elasticsearch.ResourceTypeSupport;
import nd.esp.service.lifecycle.support.enums.OperationType;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;
import nd.esp.service.lifecycle.vos.lessons.v06.LessonViewModel;
import nd.esp.service.lifecycle.vos.valid.ValidCreateLessPropertiesGroup;
import nd.esp.service.lifecycle.vos.valid.ValidUpdateLessPropertiesGroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 课时V0.6 API
 * 
 * @author caocr
 */
@RestController
@RequestMapping("/v0.6/lessons")
public class LessonControllerV06 {
    private static final Logger LOG = LoggerFactory.getLogger(LessonControllerV06.class);
    
    private static final int CONTROLLER_CREATE_TYPE = 0;// 新增课时操作
    private static final int CONTROLLER_UPDATE_TYPE = 1;// 修改课时操作
    private static final int CONTROLLER_PATCH_TYPE = 2;// 修改课时操作
    @Autowired
    @Qualifier("lessonServiceV06")
    private LessonsServiceV06 lessonsServiceV06;
    
    @Autowired
    private NotifyInstructionalobjectivesService notifyService;

    @Autowired
    private OfflineService offlineService;
    @Autowired
    private AsynEsResourceService esResourceOperation;

    /**
     * 课时创建
     * 
     * @param viewModel 课时对象
     * @param validResult BindingResult
     * @return
     * @since
     */
    @MarkAspect4Format2Category
    @MarkAspect4OfflineToES
    @RequestMapping(method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public LessonViewModel create(@Validated(ValidCreateLessPropertiesGroup.class) @RequestBody LessonViewModel viewModel,
                                            BindingResult validResult) {
        viewModel.setIdentifier(UUID.randomUUID().toString());
        
        //校验入参
        checkParams(viewModel, validResult, CONTROLLER_CREATE_TYPE);
        
        viewModel = lessonApi(viewModel, CONTROLLER_CREATE_TYPE);
        
        //add by xiezy - 2016.04.15
        //异步通知智能出题
        notifyService.asynNotify4LessonOrChapter(IndexSourceType.LessonType.getName(), viewModel.getIdentifier(), null, OperationType.CREATE);
        
        return viewModel;
    }

    /**
     * 课时修改
     * 
     * @param viewModel 课时对象
     * @param validResult BindingResult
     * @param id 课时id
     * @return
     * @since
     */
    @MarkAspect4Format2Category
    @MarkAspect4OfflineToES
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public LessonViewModel update(@Validated(ValidUpdateLessPropertiesGroup.class) @RequestBody LessonViewModel viewModel,
                                            BindingResult validResult,
                                            @PathVariable String id) {
        viewModel.setIdentifier(id);
        
        // 校验入参
        checkParams(viewModel, validResult, CONTROLLER_UPDATE_TYPE);
        
        viewModel = lessonApi(viewModel, CONTROLLER_UPDATE_TYPE);
        
        return viewModel;
    }

    /**
     * 课时修改
     *
     * @param viewModel 课时对象
     * @param validResult BindingResult
     * @param id 课时id
     * @return
     * @since
     */
    @MarkAspect4Format2Category
    @RequestMapping(value = "/{id}", method = RequestMethod.PATCH, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public LessonViewModel patch(@Validated(ValidUpdateLessPropertiesGroup.class) @RequestBody LessonViewModel viewModel,
                                  BindingResult validResult,
                                  @PathVariable String id,
                                  @RequestParam(value = "notice_file", required = false,defaultValue = "true") boolean notice){
        viewModel.setIdentifier(id);

        // 校验入参
//        checkParams(viewModel, validResult, CONTROLLER_PATCH_TYPE);

        viewModel = lessonApi(viewModel, CONTROLLER_PATCH_TYPE);

        if(notice) {
            offlineService.writeToCsAsync(ResourceNdCode.lessons.toString(), id);
            // offline metadata(coverage) to elasticsearch
            if (ResourceTypeSupport.isValidEsResourceType(ResourceNdCode.lessons.toString())) {
                esResourceOperation.asynAdd(
                        new Resource(ResourceNdCode.lessons.toString(), id));
            }
        }

        return viewModel;
    }

    /**
     * 对入参进行校验
     * 
     * @param viewModel 课时对象
     * @param validResult BindingResult
     * @param  type 业务类型
     * @since
     */
    private void checkParams(LessonViewModel viewModel, BindingResult validResult, int type) {
        // 入参合法性校验
        if(type == CONTROLLER_CREATE_TYPE){
            ValidResultHelper.valid(validResult,
                                    "LC/CREATE_LESSON_PARAM_VALID_FAIL",
                                    "LessonControllerV06",
                                    "create");
            // 业务校验
            CommonHelper.inputParamValid(viewModel, "10110",OperationType.CREATE);
        } else if(type == CONTROLLER_UPDATE_TYPE){
            ValidResultHelper.valid(validResult,
                                    "LC/UPDATE_LESSON_PARAM_VALID_FAIL",
                                    "LessonControllerV06",
                                    "update");
            // 业务校验
            CommonHelper.inputParamValid(viewModel, "10111",OperationType.UPDATE);
        }
        
    }
    
    /**
     * 业务统一方法
     * 
     * @param viewModel 课时对象
     * @param type 业务类型
     * @return LessonViewModel
     * @since
     */
    private LessonViewModel lessonApi(LessonViewModel viewModel, int type){
        // model入参转换
        LessonModel model = null;

        if (type == CONTROLLER_CREATE_TYPE) {
            model = CommonHelper.convertViewModelIn(viewModel, LessonModel.class, ResourceNdCode.lessons);
            // 创建课时
            
            LOG.info("课时v06创建课时操作，业务逻辑处理");
            
            model = lessonsServiceV06.create(model);
        } else if (type == CONTROLLER_UPDATE_TYPE) {
            model = CommonHelper.convertViewModelIn(viewModel, LessonModel.class, ResourceNdCode.lessons);
            // 修改课时
            
            LOG.info("课时v06更新课时操作，业务逻辑处理");
            
            model = lessonsServiceV06.update(model);
        } else if (type == CONTROLLER_PATCH_TYPE) {
            model = CommonHelper.convertViewModelIn(viewModel, LessonModel.class, ResourceNdCode.lessons, true);
            // 局部修改课时

            LOG.info("课时v06局部更新课时操作，业务逻辑处理");

            model = lessonsServiceV06.patch(model);
        }

        // model出参转换
        viewModel = CommonHelper.convertViewModelOut(model, LessonViewModel.class);

        // 课时没有techInfo属性、educationInfo属性、preview属性
        viewModel.setTechInfo(null);
        viewModel.setEducationInfo(null);
        viewModel.setPreview(null);

        return viewModel;
    }

}
