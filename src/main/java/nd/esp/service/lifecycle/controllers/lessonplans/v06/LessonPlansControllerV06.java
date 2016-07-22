package nd.esp.service.lifecycle.controllers.lessonplans.v06;

import java.util.UUID;

import nd.esp.service.lifecycle.entity.elasticsearch.Resource;
import nd.esp.service.lifecycle.models.v06.LessonPlanModel;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.services.Lessonplans.v06.LessonPlansServiceV06;
import nd.esp.service.lifecycle.services.elasticsearch.AsynEsResourceService;
import nd.esp.service.lifecycle.services.offlinemetadata.OfflineService;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.annotation.MarkAspect4Format2Category;
import nd.esp.service.lifecycle.support.annotation.MarkAspect4OfflineJsonToCS;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.support.busi.ModelPropertiesValidUitl;
import nd.esp.service.lifecycle.support.busi.TransCodeUtil;
import nd.esp.service.lifecycle.support.busi.ValidResultHelper;
import nd.esp.service.lifecycle.support.busi.elasticsearch.ResourceTypeSupport;
import nd.esp.service.lifecycle.support.busi.transcode.TransCodeManager;
import nd.esp.service.lifecycle.support.enums.LifecycleStatus;
import nd.esp.service.lifecycle.support.enums.OperationType;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;
import nd.esp.service.lifecycle.vos.lessonplans.v06.LessonPlanViewModel;
import nd.esp.service.lifecycle.vos.valid.Valid4UpdateGroup;
import nd.esp.service.lifecycle.vos.valid.ValidGroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v0.6/lessonplans")
public class LessonPlansControllerV06 {
    private static final int CONTROLLER_CREATE_TYPE = 0;// 新增教案操作
    private static final int CONTROLLER_UPDATE_TYPE = 1;// 修改教案操作
    private static final Logger LOG = LoggerFactory.getLogger(LessonPlansControllerV06.class);

    @Autowired
    private LessonPlansServiceV06 lessonPlansServiceV06;
    
    @Autowired
    private TransCodeUtil transCodeUtil;

    @Autowired
    private OfflineService offlineService;
    @Autowired
    private AsynEsResourceService esResourceOperation;
    
    @MarkAspect4Format2Category
    @MarkAspect4OfflineJsonToCS
    @RequestMapping(value = "{uuid}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public LessonPlanViewModel create(@Validated(ValidGroup.class) @RequestBody LessonPlanViewModel lvpm,
                                      BindingResult validResult,
                                      @PathVariable String uuid) {
        checkParams(lvpm, validResult, uuid, CONTROLLER_CREATE_TYPE);

        // model入参转换
        LessonPlanModel model = CommonHelper.convertViewModelIn(lvpm, LessonPlanModel.class, ResourceNdCode.lessonplans);

        LOG.info("教案V06---创建教案操作，业务逻辑处理");
        
        boolean bTranscode = TransCodeManager.canTransCode(lvpm, IndexSourceType.LessonPlansType.getName());
        if(bTranscode) {
            model.getLifeCycle().setStatus(TransCodeUtil.getTransIngStatus(true));
        }

        // 创建教案
        model = lessonPlansServiceV06.create(model);
        
        // model出参转换
        lvpm = CommonHelper.convertViewModelOut(model, LessonPlanViewModel.class);
        
        if (bTranscode) {
            transCodeUtil.triggerTransCode(model, IndexSourceType.LessonPlansType.getName());
        }

        return lvpm;

    }

    @MarkAspect4Format2Category
    @MarkAspect4OfflineJsonToCS
    @RequestMapping(value = "{uuid}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public LessonPlanViewModel update(@Validated(Valid4UpdateGroup.class) @RequestBody LessonPlanViewModel lpvm,
                                      BindingResult validResult,
                                      @PathVariable String uuid) {
        checkParams(lpvm, validResult, uuid, CONTROLLER_UPDATE_TYPE);

        // model入参转换
        LessonPlanModel model = CommonHelper.convertViewModelIn(lpvm, LessonPlanModel.class, ResourceNdCode.lessonplans);

        LOG.info("教案V06---更新教案操作，业务逻辑处理");

        // 修改教案
        model = lessonPlansServiceV06.update(model);

        // model出参转换
        lpvm = CommonHelper.convertViewModelOut(model, LessonPlanViewModel.class);
        
        return lpvm;
    }

    @MarkAspect4Format2Category
    @RequestMapping(value = "{uuid}", method = RequestMethod.PATCH, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public LessonPlanViewModel patch(@Validated(Valid4UpdateGroup.class) @RequestBody LessonPlanViewModel lpvm,
                                      BindingResult validResult, @PathVariable String uuid,
                                      @RequestParam(value = "notice_file", required = false,defaultValue = "true") boolean notice){
//        checkParams(lpvm, validResult, uuid, CONTROLLER_UPDATE_TYPE);

        // model入参转换
        LessonPlanModel model = CommonHelper.convertViewModelIn(lpvm, LessonPlanModel.class, ResourceNdCode.lessonplans, true);

        LOG.info("教案V06---更新教案操作，业务逻辑处理");

        // 修改教案
        model = lessonPlansServiceV06.patch(model);

        // model出参转换
        lpvm = CommonHelper.convertViewModelOut(model, LessonPlanViewModel.class);

        if(notice) {
            offlineService.writeToCsAsync(ResourceNdCode.lessonplans.toString(), uuid);
        }
        // offline metadata(coverage) to elasticsearch
        if (ResourceTypeSupport.isValidEsResourceType(ResourceNdCode.lessonplans.toString())) {
            esResourceOperation.asynAdd(
                    new Resource(ResourceNdCode.lessonplans.toString(), uuid));
        }

        return lpvm;
    }

    /**
     * 对入参进行校验
     * 
     * @param viewModel 教案对象
     * @param validResult BindingResult
     * @param id 教案id
     * @param type 业务类型
     * @since
     */
    private void checkParams(LessonPlanViewModel lvpm, BindingResult validResult, String id, int type) {
    	lvpm.setIdentifier(id);
    	// 入参合法性校验
        if (type == CONTROLLER_CREATE_TYPE) {
            ValidResultHelper.valid(validResult,
                                    "LC/CREATE_LESSONPLANS_PARAM_VALID_FAIL",
                                    "LessonPlansControllerV06",
                                    "create");
            // 业务校验
            CommonHelper.inputParamValid(lvpm, "10111",OperationType.CREATE);

        } else if (type == CONTROLLER_UPDATE_TYPE) {
            ValidResultHelper.valid(validResult,
                                    "LC/UPDATE_LESSONPLANS_PARAM_VALID_FAIL",
                                    "LessonPlansControllerV06",
                                    "update");
            // 业务校验
            CommonHelper.inputParamValid(lvpm, "10111",OperationType.UPDATE);

        }
        
        // techInfo属性校验lvpm.getLifeCycle()
        try {
            if (null != lvpm.getLifeCycle() && LifecycleStatus.isNeedTranscode(lvpm.getLifeCycle().getStatus())) {
                ModelPropertiesValidUitl.verificationHref(lvpm);
            }
        } catch (Exception e) {
            
            LOG.error("教案v06" + e.getMessage());
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR, "LC/CHECK_HREF_FAIL", e.getMessage());

        }

    }

}
