/* =============================================================
 * Created: [2015年7月16日] by caocr
 * =============================================================
 *
 * Copyright 2014-2015 NetDragon Websoft Inc. All Rights Reserved
 *
 * =============================================================
 */

package nd.esp.service.lifecycle.controllers.learningplan.v06;

import nd.esp.service.lifecycle.entity.elasticsearch.Resource;
import nd.esp.service.lifecycle.models.v06.LearningPlanModel;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.services.elasticsearch.AsynEsResourceService;
import nd.esp.service.lifecycle.services.learningplans.v06.LearningPlansServiceV06;
import nd.esp.service.lifecycle.services.offlinemetadata.OfflineService;
import nd.esp.service.lifecycle.support.annotation.MarkAspect4Format2Category;
import nd.esp.service.lifecycle.support.annotation.MarkAspect4OfflineJsonToCS;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.support.busi.TransCodeUtil;
import nd.esp.service.lifecycle.support.busi.ValidResultHelper;
import nd.esp.service.lifecycle.support.busi.elasticsearch.ResourceTypeSupport;
import nd.esp.service.lifecycle.support.busi.transcode.TransCodeManager;
import nd.esp.service.lifecycle.support.enums.OperationType;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;
import nd.esp.service.lifecycle.vos.learningplans.v06.LearningPlanViewModel;
import nd.esp.service.lifecycle.vos.valid.Valid4UpdateGroup;
import nd.esp.service.lifecycle.vos.valid.ValidGroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author caocr
 * @since
 */
@RestController
@RequestMapping("/v0.6/learningplans")
public class LearningPlansControllerV06 {
    private static final Logger LOG = LoggerFactory.getLogger(LearningPlansControllerV06.class);
    
    @Autowired
    private LearningPlansServiceV06 learningPlansService;
    
    @Autowired
    private TransCodeUtil transCodeUtil;

    @Autowired
    private OfflineService offlineService;
    @Autowired
    private AsynEsResourceService esResourceOperation;
    
    @MarkAspect4Format2Category
    @MarkAspect4OfflineJsonToCS
    @RequestMapping(value = "{uuid}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public LearningPlanViewModel create(@Validated(ValidGroup.class) @RequestBody LearningPlanViewModel lpvm,
                                        BindingResult validResult,
                                        @PathVariable String uuid) {
        // 校验入参
        checkParams(lpvm, validResult, uuid, true);

        // model入参转换
        LearningPlanModel model = CommonHelper.convertViewModelIn(lpvm,
                                                                  LearningPlanModel.class,
                                                                  ResourceNdCode.learningplans);
        
        boolean bTranscode = TransCodeManager.canTransCode(lpvm, IndexSourceType.LearningPlansType.getName());
        if(bTranscode) {
            model.getLifeCycle().setStatus(TransCodeUtil.getTransIngStatus(true));
        }

        LOG.info("学案V06创建学案操作，业务逻辑处理");

        // 创建学案
        model = learningPlansService.create(model);

        // model出参转换
        lpvm = CommonHelper.convertViewModelOut(model, LearningPlanViewModel.class);
        
        if (bTranscode) {
            transCodeUtil.triggerTransCode(model, IndexSourceType.LearningPlansType.getName());
        }

        return lpvm;
    }

    @MarkAspect4Format2Category
    @MarkAspect4OfflineJsonToCS
    @RequestMapping(value = "{uuid}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public LearningPlanViewModel update(@Validated(Valid4UpdateGroup.class) @RequestBody LearningPlanViewModel lpvm,
                                        BindingResult validResult,
                                        @PathVariable String uuid) {
        // 校验入参
        checkParams(lpvm, validResult, uuid, false);

        // model入参转换
        LearningPlanModel model = CommonHelper.convertViewModelIn(lpvm,
                                                                  LearningPlanModel.class,
                                                                  ResourceNdCode.learningplans);

        LOG.info("学案V06更新学案操作，业务逻辑处理");

        // 修改学案
        model = learningPlansService.update(model);

        // model出参转换
        lpvm = CommonHelper.convertViewModelOut(model, LearningPlanViewModel.class);
        
        return lpvm;
    }

    @MarkAspect4Format2Category
    @RequestMapping(value = "{uuid}", method = RequestMethod.PATCH, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public LearningPlanViewModel patch(@Validated(Valid4UpdateGroup.class) @RequestBody LearningPlanViewModel lpvm,
                                        BindingResult validResult, @PathVariable String uuid,
                                        @RequestParam(value = "notice_file", required = false,defaultValue = "true") boolean notice){
        // 校验入参
//        checkParams(lpvm, validResult, uuid, false);

        // model入参转换
        LearningPlanModel model = CommonHelper.convertViewModelIn(lpvm,
                LearningPlanModel.class,
                ResourceNdCode.learningplans, true);

        LOG.info("学案V06更新学案操作，业务逻辑处理");

        // 修改学案
        model = learningPlansService.patch(model);

        // model出参转换
        lpvm = CommonHelper.convertViewModelOut(model, LearningPlanViewModel.class);

        if(notice) {
            offlineService.writeToCsAsync(ResourceNdCode.learningplans.toString(), uuid);
        }
        // offline metadata(coverage) to elasticsearch
        if (ResourceTypeSupport.isValidEsResourceType(ResourceNdCode.learningplans.toString())) {
            esResourceOperation.asynAdd(
                    new Resource(ResourceNdCode.learningplans.toString(), uuid));
        }

        return lpvm;
    }

    /**
     * 对入参进行校验
     * 
     * @param viewModel 学案对象
     * @param validResult BindingResult
     * @param id 学案id
     * @param isCreate 是否是创建业务
     * @since
     */
    private void checkParams(LearningPlanViewModel viewModel,
                             BindingResult validResult,
                             String id, boolean isCreate) {
    	viewModel.setIdentifier(id);
        // 入参合法性校验
        if (isCreate) {
            ValidResultHelper.valid(validResult,
                                    "LC/CREATE_LEARNINGPLANS_PARAM_VALID_FAIL",
                                    "LearningPlansControllerV06",
                                    "create");
            CommonHelper.inputParamValid(viewModel, "10111",OperationType.CREATE);
        } else {
            ValidResultHelper.valid(validResult,
                                    "LC/UPDATE_LEARNINGPLANS_PARAM_VALID_FAIL",
                                    "LearningPlansControllerV06",
                                    "update");
            CommonHelper.inputParamValid(viewModel, "10111",OperationType.UPDATE);
        }
    }

}
