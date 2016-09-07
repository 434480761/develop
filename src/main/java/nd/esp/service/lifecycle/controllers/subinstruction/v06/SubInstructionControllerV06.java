package nd.esp.service.lifecycle.controllers.subinstruction.v06;

import java.util.UUID;

import nd.esp.service.lifecycle.models.v06.SubInstructionModel;
import nd.esp.service.lifecycle.services.subinstruction.v06.SubInstructionService;
import nd.esp.service.lifecycle.support.annotation.MarkAspect4Format2Category;
import nd.esp.service.lifecycle.support.annotation.MarkAspect4OfflineToES;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.support.busi.ValidResultHelper;
import nd.esp.service.lifecycle.support.enums.OperationType;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;
import nd.esp.service.lifecycle.vos.subinstruction.v06.SubInstructionViewModel;
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
 * 子教学目标V0.6API
 * 
 * @author yanguanyu(290536)
 */
@RestController
@RequestMapping("/v0.6/subInstruction")
public class SubInstructionControllerV06 {
    @Autowired
    @Qualifier("subInstructionServiceV06")
    private SubInstructionService subInstructionService;


    // private static final Logger LOG = LoggerFactory.getLogger(SubInstructionControllerV06.class);

    /**
     * 创建子教学目标对象
     * 
     * @param avm 子教学目标对象
     * @return
     */
    @MarkAspect4Format2Category
    @MarkAspect4OfflineToES
    @RequestMapping(method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public SubInstructionViewModel create(@Validated(ValidInstructionalObjectiveDefaultGroup.class) @RequestBody SubInstructionViewModel avm,
                                                  BindingResult validResult) {
        //TODO 添加子教学目标对应的信息
        // 入参合法性校验
        ValidResultHelper.valid(validResult,
                                "LC/CREATE_INSTRUCTIONALOBJECTIVE_PARAM_VALID_FAIL",
                                "SubInstructionControllerV06",
                                "create");
        avm.setIdentifier(UUID.randomUUID().toString());// 后续要使用，不得不在controller层生成uuid,categories
        CommonHelper.inputParamValid(avm, "10110", OperationType.CREATE);
        return operate(avm, OperationType.CREATE);

    }

    /**
     * 修改子教学目标对象
     * 
     * @param avm 子教学目标对象
     * @return
     */
    @MarkAspect4Format2Category
    @MarkAspect4OfflineToES
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public SubInstructionViewModel update(@Validated(ValidInstructionalObjectiveDefault4UpdateGroup.class) @RequestBody SubInstructionViewModel avm,
                                                  BindingResult validResult,
                                                  @PathVariable String id) {
        //TODO 添加子教学目标对应的信息
        // 入参合法性校验
        ValidResultHelper.valid(validResult,
                "LC/UPDATE_INSTRUCTIONALOBJECTIVE_PARAM_VALID_FAIL",
                "SubInstructionControllerV06",
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
    private SubInstructionViewModel operate(SubInstructionViewModel avm, OperationType operationType) {
        SubInstructionModel am = CommonHelper.convertViewModelIn(avm,
                SubInstructionModel.class,ResourceNdCode.subInstruction);

        if (operationType == OperationType.CREATE) {
            // 创建子教学目标
            am = subInstructionService.createSubInstruction(am);
        } else {
            // 更新子教学目标
            am = subInstructionService.updateSubInstruction(am);
        }
        avm = CommonHelper.convertViewModelOut(am, SubInstructionViewModel.class);
        avm.setTechInfo(null); // 没有这个属性
        return avm;
    }


}
