package nd.esp.service.lifecycle.controllers.instructionalobjectives.v06;

import java.util.UUID;

import nd.esp.service.lifecycle.models.v06.InstructionalObjectiveModel;
import nd.esp.service.lifecycle.services.instructionalobjectives.v06.InstructionalObjectiveService;
import nd.esp.service.lifecycle.services.notify.NotifyInstructionalobjectivesService;
import nd.esp.service.lifecycle.support.annotation.MarkAspect4Format2Category;
import nd.esp.service.lifecycle.support.annotation.MarkAspect4OfflineToES;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.support.busi.ValidResultHelper;
import nd.esp.service.lifecycle.support.enums.OperationType;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;
import nd.esp.service.lifecycle.vos.instructionalobjectives.v06.InstructionalObjectiveViewModel;
import nd.esp.service.lifecycle.vos.valid.ValidInstructionalObjectiveDefault4UpdateGroup;
import nd.esp.service.lifecycle.vos.valid.ValidInstructionalObjectiveDefaultGroup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 教学目标V0.6API
 * 
 * @author linsm
 */
@RestController
@RequestMapping("/v0.6/instructionalobjectives")
public class InstructionalObjectiveControllerV06 {
    @Autowired
    @Qualifier("instructionalObjectiveServiceV06")
    private InstructionalObjectiveService instructionalObjectiveService;

    @Autowired
    private NotifyInstructionalobjectivesService notifyService;
    
    // private static final Logger LOG = LoggerFactory.getLogger(InstructionalObjectiveControllerV06.class);

    /**
     * 创建教学目标对象
     * 
     * @param rm 教学目标对象
     * @return
     */
    @MarkAspect4Format2Category
    @MarkAspect4OfflineToES
    @RequestMapping(method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public InstructionalObjectiveViewModel create(@Validated(ValidInstructionalObjectiveDefaultGroup.class) @RequestBody InstructionalObjectiveViewModel avm,
                                                  BindingResult validResult) {
        // 入参合法性校验
        ValidResultHelper.valid(validResult,
                                "LC/CREATE_INSTRUCTIONALOBJECTIVE_PARAM_VALID_FAIL",
                                "InstructionalObjectiveControllerV06",
                                "create");
        avm.setIdentifier(UUID.randomUUID().toString());// 后续要使用，不得不在controller层生成uuid,categories
        CommonHelper.inputParamValid(avm, "10110", OperationType.CREATE);
        return operate(avm, OperationType.CREATE);

    }

    /**
     * 修改教学目标对象
     * 
     * @param rm 教学目标对象
     * @return
     */
    @MarkAspect4Format2Category
    @MarkAspect4OfflineToES
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public InstructionalObjectiveViewModel update(@Validated(ValidInstructionalObjectiveDefault4UpdateGroup.class) @RequestBody InstructionalObjectiveViewModel avm,
                                                  BindingResult validResult,
                                                  @PathVariable String id) {

        // 入参合法性校验
        ValidResultHelper.valid(validResult,
                                "LC/UPDATE_INSTRUCTIONALOBJECTIVE_PARAM_VALID_FAIL",
                                "InstructionalObjectiveControllerV06",
                                "update");
        avm.setIdentifier(id);
        CommonHelper.inputParamValid(avm, "10111", OperationType.UPDATE);
        return operate(avm, OperationType.UPDATE);
    }

    /**
     * @param avm
     * @param operationType
     * @since
     */
    private InstructionalObjectiveViewModel operate(InstructionalObjectiveViewModel avm, OperationType operationType) {
        InstructionalObjectiveModel am = CommonHelper.convertViewModelIn(avm,
                                                                         InstructionalObjectiveModel.class,
                                                                         ResourceNdCode.instructionalobjectives);
        //add by xiezy - 2016.04.15
        String oldStatus = "";
        if(operationType == OperationType.UPDATE){//如果是更新操作要先保存其原有状态
        	oldStatus = notifyService.getResourceStatus(avm.getIdentifier());
        }
        
        if (operationType == OperationType.CREATE) {
            // 创建教学目标
            am = instructionalObjectiveService.createInstructionalObjective(am);
        } else {
            // 更新教学目标
            am = instructionalObjectiveService.updateInstructionalObjective(am);
        }
        
        //add by xiezy - 2016.04.15
        //异步通知智能出题
        notifyService.asynNotify4Resource(avm.getIdentifier(), avm.getLifeCycle().getStatus(), oldStatus, null, operationType);

        avm = CommonHelper.convertViewModelOut(am, InstructionalObjectiveViewModel.class);
        avm.setTechInfo(null); // 没有这个属性
        return avm;
    }
}
