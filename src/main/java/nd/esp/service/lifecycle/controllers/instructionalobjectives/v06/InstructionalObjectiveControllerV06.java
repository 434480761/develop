package nd.esp.service.lifecycle.controllers.instructionalobjectives.v06;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import nd.esp.service.lifecycle.educommon.models.ResourceModel;
import nd.esp.service.lifecycle.educommon.services.impl.CommonServiceHelper;
import nd.esp.service.lifecycle.educommon.support.ParameterVerificationHelper;
import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;
import nd.esp.service.lifecycle.educommon.vos.constant.IncludesConstant;
import nd.esp.service.lifecycle.entity.elasticsearch.Resource;
import nd.esp.service.lifecycle.models.v06.InstructionalObjectiveModel;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.services.elasticsearch.AsynEsResourceService;
import nd.esp.service.lifecycle.services.instructionalobjectives.v06.InstructionalObjectiveService;
import nd.esp.service.lifecycle.services.notify.NotifyInstructionalobjectivesService;
import nd.esp.service.lifecycle.services.offlinemetadata.OfflineService;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.annotation.MarkAspect4Format2Category;
import nd.esp.service.lifecycle.support.annotation.MarkAspect4OfflineToES;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.support.busi.ValidResultHelper;
import nd.esp.service.lifecycle.support.busi.elasticsearch.ResourceTypeSupport;
import nd.esp.service.lifecycle.support.enums.OperationType;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.vos.ListViewModel;
import nd.esp.service.lifecycle.vos.instructionalobjectives.v06.InstructionalObjectiveViewModel;
import nd.esp.service.lifecycle.vos.statics.ResourceType;
import nd.esp.service.lifecycle.vos.valid.ValidInstructionalObjectiveDefault4UpdateGroup;
import nd.esp.service.lifecycle.vos.valid.ValidInstructionalObjectiveDefaultGroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

/**
 * 教学目标V0.6API
 *
 * @author linsm
 */
@RestController
@RequestMapping("/v0.6/instructionalobjectives")
public class InstructionalObjectiveControllerV06 {

    private static final Logger LOG = LoggerFactory.getLogger(InstructionalObjectiveControllerV06.class);

    @Autowired
    @Qualifier("instructionalObjectiveServiceV06")
    private InstructionalObjectiveService instructionalObjectiveService;

    @Autowired
    private NotifyInstructionalobjectivesService notifyService;

    @Autowired
    private OfflineService offlineService;
    @Autowired
    private AsynEsResourceService esResourceOperation;
    
    // private static final Logger LOG = LoggerFactory.getLogger(InstructionalObjectiveControllerV06.class);

    @Autowired
    CommonServiceHelper commonServiceHelper;


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
     * 修改教学目标对象
     *
     * @param rm 教学目标对象
     * @return
     */
    @MarkAspect4Format2Category
    @RequestMapping(value = "/{id}", method = RequestMethod.PATCH, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public InstructionalObjectiveViewModel patch(@Validated(ValidInstructionalObjectiveDefault4UpdateGroup.class) @RequestBody InstructionalObjectiveViewModel avm,
                                                  BindingResult validResult, @PathVariable String id,
                                                  @RequestParam(value = "notice_file", required = false,defaultValue = "true") boolean notice){

        // 入参合法性校验
//        ValidResultHelper.valid(validResult,
//                "LC/UPDATE_INSTRUCTIONALOBJECTIVE_PARAM_VALID_FAIL",
//                "InstructionalObjectiveControllerV06",
//                "update");
        avm.setIdentifier(id);
//        CommonHelper.inputParamValid(avm, "10111", OperationType.UPDATE);
        InstructionalObjectiveModel am = CommonHelper.convertViewModelIn(avm,
                InstructionalObjectiveModel.class,
                ResourceNdCode.instructionalobjectives, true);

        //add by xiezy - 2016.04.15
        //更新操作要先保存其原有状态
        String oldStatus = notifyService.getResourceStatus(avm.getIdentifier());

        am = instructionalObjectiveService.patchInstructionalObjective(am);

        //add by xiezy - 2016.04.15
        //异步通知智能出题
        notifyService.asynNotify4Resource(avm.getIdentifier(), avm.getLifeCycle().getStatus(), oldStatus, null, OperationType.UPDATE);

        avm = CommonHelper.convertViewModelOut(am, InstructionalObjectiveViewModel.class);
        avm.setTechInfo(null); // 没有这个属性

        if(notice) {
            offlineService.writeToCsAsync(ResourceNdCode.instructionalobjectives.toString(), id);
        }
        // offline metadata(coverage) to elasticsearch
        if (ResourceTypeSupport.isValidEsResourceType(ResourceNdCode.instructionalobjectives.toString())) {
            esResourceOperation.asynAdd(
                    new Resource(ResourceNdCode.instructionalobjectives.toString(), id));
        }
        return avm;
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
    /**
     * 根据教材章节id查询教学目标，并且按照课时，课时下的教学目标，章节下的教学目标三个顺序依次排序。
     *
     * @return
     */
    @RequestMapping(value = "/order_list", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE}, params = {"limit"})
    public ListViewModel<ResourceViewModel> getInstructionalObjectivesList(
            @RequestParam(required = false, value = "word") String words,
            @RequestParam String limit,
            @RequestParam(required = false, value = "coverage") Set<String> coverages,
            @RequestParam(required = false, value = "relation") Set<String> relations,
            @RequestParam(required = false, value = "include") String includes,
            @RequestParam(required = false, value = "reverse") String reverse) {
        ListViewModel<ResourceViewModel> resourceViewModelListViewModel = null;
        resourceViewModelListViewModel = requestQuering(includes, relations, coverages, words, limit, reverse);

        return resourceViewModelListViewModel;
    }

    @SuppressWarnings("unchecked")
    private ListViewModel<ResourceViewModel> requestQuering(String includes, Set<String> relations, Set<String> coverages, String words, String limit, String reverse) {

        //参数校验和处理
        Map<String, Object> paramMap =
                requestParamVerifyAndHandle(includes, relations, coverages, limit, reverse);

        // include
        List<String> includesList = (List<String>) paramMap.get("include");

        // relations,格式:stype/suuid/r_type
        List<Map<String, String>> relationsMap = (List<Map<String, String>>) paramMap.get("relation");

        // coverages,格式:Org/uuid/SHAREING
        List<String> coveragesList = (List<String>) paramMap.get("coverage");

        //limit
        limit = (String) paramMap.get("limit");

        //reverse,默认为false
        boolean reverseBoolean = (boolean) paramMap.get("reverse");

        String chapterId = "";

        //调用service,获取到业务模型的list
        ListViewModel<ResourceModel> rListViewModel = new ListViewModel<ResourceModel>();

        rListViewModel = instructionalObjectiveService.getResourcePageByChapterId(includesList, relationsMap, coveragesList, limit, reverseBoolean);

        //ListViewModel<ResourceModel> 转换为  ListViewModel<ResourceViewModel>
        ListViewModel<ResourceViewModel> result = new ListViewModel<ResourceViewModel>();
        result.setTotal(rListViewModel.getTotal());
        result.setLimit(rListViewModel.getLimit());
        //items处理
        List<ResourceViewModel> items = new ArrayList<ResourceViewModel>();
        for (ResourceModel resourceModel : rListViewModel.getItems()) {
            ResourceViewModel resourceViewModel = changeToView(resourceModel, "instructionalobjectives", includesList);
            items.add(resourceViewModel);
        }
        result.setItems(items);

        return result;
    }

    private Map<String, Object> requestParamVerifyAndHandle(String includes, Set<String> relations, Set<String> coverages, String limit, String reverse) {

        List<String> includesList = IncludesConstant.getValidIncludes(includes);

        relations = CollectionUtils.removeEmptyDeep(relations);

        // 3.relations,格式:stype/suuid/r_type
        List<Map<String, String>> relationsMap = new ArrayList<Map<String, String>>();
        if (CollectionUtils.isEmpty(relations)) {
            relationsMap = null;
        } else {
            for (String relation : relations) {
                Map<String, String> map = new HashMap<String, String>();
                //对于入参的coverage每个在最后追加一个空格，以保证elemnt的size为3
                relation = relation + " ";
                List<String> elements = Arrays.asList(relation.split("/"));
                //格式错误判断
                if (elements.size() != 3) {
                    LOG.error(relation + "--relation格式错误");
                    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                            LifeCircleErrorMessageMapper.CommonSearchParamError.getCode(),
                            relation + "--relation格式错误");
                }
                //判断源资源是否存在,stype + suuid
                if (!elements.get(1).trim().endsWith("$")) {//不为递归查询时才校验
                    CommonHelper.resourceExist(elements.get(0).trim(), elements.get(1).trim(), ResourceType.RESOURCE_SOURCE);
                }
                //r_type的特殊处理
                if (StringUtils.isEmpty(elements.get(2).trim())) {
                    elements.set(2, null);
                }
                map.put("stype", elements.get(0).trim());
                map.put("suuid", elements.get(1).trim());
                map.put("rtype", elements.get(2));
                relationsMap.add(map);
            }
        }

        // 4.coverages,格式:Org/uuid/SHAREING
        coverages = CollectionUtils.removeEmptyDeep(coverages);
        List<String> coveragesList = new ArrayList<String>();
        if (CollectionUtils.isEmpty(coverages)) {
            coveragesList = null;
        } else {
            for (String coverage : coverages) {
                String c = ParameterVerificationHelper.coverageVerification(coverage);
                coveragesList.add(c);
            }
        }

        //7. limit
        limit = CommonHelper.checkLimitMaxSize(limit);

        //reverse,默认为false
        boolean reverseBoolean = false;
        if (StringUtils.isNotEmpty(reverse) && reverse.equals("true")) {
            reverseBoolean = true;
        }

        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("include", includesList);
        paramMap.put("relation", relationsMap);
        paramMap.put("coverage", coveragesList);
        paramMap.put("reverse", reverseBoolean);
        paramMap.put("limit", limit);

        return paramMap;
    }

    private ResourceViewModel changeToView(ResourceModel model, String resourceType, List<String> includes) {
        return CommonHelper.changeToView(model, resourceType, includes, commonServiceHelper);
    }
}
