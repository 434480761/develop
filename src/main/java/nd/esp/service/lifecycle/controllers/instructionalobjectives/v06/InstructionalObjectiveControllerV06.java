package nd.esp.service.lifecycle.controllers.instructionalobjectives.v06;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import nd.esp.service.lifecycle.models.v06.InstructionalObjectiveModel;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.services.instructionalobjectives.v06.InstructionalObjectiveService;
import nd.esp.service.lifecycle.services.notify.NotifyInstructionalobjectivesService;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.annotation.MarkAspect4Format2Category;
import nd.esp.service.lifecycle.support.annotation.MarkAspect4OfflineToES;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.support.busi.ValidResultHelper;
import nd.esp.service.lifecycle.support.enums.OperationType;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;
import nd.esp.service.lifecycle.utils.ParamCheckUtil;
import nd.esp.service.lifecycle.vos.ListViewModel;
import nd.esp.service.lifecycle.vos.instructionalobjectives.v06.InstructionalObjectiveViewModel;
import nd.esp.service.lifecycle.vos.valid.ValidInstructionalObjectiveDefault4UpdateGroup;
import nd.esp.service.lifecycle.vos.valid.ValidInstructionalObjectiveDefaultGroup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nullable;

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


    /**
     * 根据教学目标id查询出与之相关联的教材章节。
     * 分两种情况：
     * 1.教学目标与章节直接关联
     * 2.教学目标与课时关联，课时与章节关联
     * @param objectiveId
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/business/{objective_id}/chapters/paths", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<Map<String, Object>> getPaths(@PathVariable("objective_id") String objectiveId) {

        List<Map<String, Object>> result = instructionalObjectiveService.getChapterRelationById(objectiveId);
        return result;
    }

    /***
     * 查询没有被关联到章节或课时的教学目标
     * @param limit 分页参数
     * @param unrelationCategory 没有被关联的category，chapters或lessons，不填默认为同时没有被关联到章节和课时
     * @param knowledgeTypeCode 知识点类型维度code
     * @param instructionalObjectiveTypeId 教学目标类型Id
     */
    @RequestMapping(value = "/unrelation2chapters", method = RequestMethod.GET)
    public Object getUnrelation2Chapters(
            @RequestParam(value = "limit", defaultValue = "(0,15)") String limit,
            @RequestParam(value = "unrelationCategory", defaultValue = "") String unrelationCategory,
            @RequestParam(value = "knowledgeTypeCode", defaultValue = "") String knowledgeTypeCode,
            @RequestParam(value = "instructionalObjectiveTypeId",defaultValue = "") String instructionalObjectiveTypeId) {

        if (!"".equals(unrelationCategory) && !IndexSourceType.ChapterType.getName().equals(unrelationCategory) && !IndexSourceType.LessonType.getName().equals(unrelationCategory)) {
            throw new LifeCircleException(HttpStatus.BAD_REQUEST,
                    LifeCircleErrorMessageMapper.InvalidArgumentsError);
        }

        limit = CommonHelper.checkLimitMaxSize(limit);

        ListViewModel<InstructionalObjectiveModel> listViewModel = instructionalObjectiveService.getUnRelationInstructionalObjective(knowledgeTypeCode, instructionalObjectiveTypeId, unrelationCategory, limit);

        Collection<String> ids = Collections2.transform(listViewModel.getItems(), new Function<InstructionalObjectiveModel, String>() {
            @Nullable
            @Override
            public String apply(InstructionalObjectiveModel instructionalObjectiveModel) {
                return instructionalObjectiveModel.getIdentifier();
            }
        });

        Map<String, String> titles = instructionalObjectiveService.getInstructionalObjectiveTitle(ids);

        for (InstructionalObjectiveModel model : listViewModel.getItems()) {
            String title = titles.get(model.getIdentifier());
            model.setTitle(null == title ? model.getTitle():title);
        }

        return listViewModel;
    }
}
