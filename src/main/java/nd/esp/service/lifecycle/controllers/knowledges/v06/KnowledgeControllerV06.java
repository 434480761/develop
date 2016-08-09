package nd.esp.service.lifecycle.controllers.knowledges.v06;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.validation.Valid;

import nd.esp.service.lifecycle.educommon.models.ResClassificationModel;
import nd.esp.service.lifecycle.entity.elasticsearch.Resource;
import nd.esp.service.lifecycle.models.v06.ChapterKnowledgeModel;
import nd.esp.service.lifecycle.models.v06.KnowledgeExtPropertiesModel;
import nd.esp.service.lifecycle.models.v06.KnowledgeModel;
import nd.esp.service.lifecycle.models.v06.KnowledgeRelationsModel;
import nd.esp.service.lifecycle.services.elasticsearch.AsynEsResourceService;
import nd.esp.service.lifecycle.services.knowledges.v06.KnowledgeService;
import nd.esp.service.lifecycle.services.offlinemetadata.OfflineService;
import nd.esp.service.lifecycle.services.titan.TitanTreeMoveService;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.annotation.MarkAspect4Format2Category;
import nd.esp.service.lifecycle.support.annotation.MarkAspect4OfflineToES;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.support.busi.ValidResultHelper;
import nd.esp.service.lifecycle.support.busi.elasticsearch.ResourceTypeSupport;
import nd.esp.service.lifecycle.support.busi.titan.TitanTreeModel;
import nd.esp.service.lifecycle.support.busi.titan.TitanTreeType;
import nd.esp.service.lifecycle.support.busi.tree.preorder.TreeDirection;
import nd.esp.service.lifecycle.support.enums.OperationType;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;
import nd.esp.service.lifecycle.utils.BeanMapperUtils;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.MessageConvertUtil;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.vos.chapters.v06.ChapterConstant;
import nd.esp.service.lifecycle.vos.knowledges.v06.ChapterKnowledgeViewModel;
import nd.esp.service.lifecycle.vos.knowledges.v06.KnowledgeExtPropertiesViewModel;
import nd.esp.service.lifecycle.vos.knowledges.v06.KnowledgeRelationsViewModel4Add;
import nd.esp.service.lifecycle.vos.knowledges.v06.KnowledgeRelationsViewModel4Get;
import nd.esp.service.lifecycle.vos.knowledges.v06.KnowledgeViewModel4In;
import nd.esp.service.lifecycle.vos.knowledges.v06.KnowledgeViewModel4Move;
import nd.esp.service.lifecycle.vos.knowledges.v06.KnowledgeViewModel4Out;
import nd.esp.service.lifecycle.vos.valid.LessPropertiesDefault;
import nd.esp.service.lifecycle.vos.valid.ValidCreateLessPropertiesGroup;
import nd.esp.service.lifecycle.vos.valid.ValidUpdateLessPropertiesGroup;

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

import com.nd.gaea.WafException;

/**
 * 知识点业务控制层
 * 
 * @author caocr
 */
@RestController
@RequestMapping("/v0.6/knowledges")
public class KnowledgeControllerV06 {
    @Autowired
    @Qualifier("knowledgeServiceV06")
    KnowledgeService knowledgeService;
    
    @Autowired
    private TitanTreeMoveService titanTreeMoveService;

    @Autowired
    private OfflineService offlineService;
    @Autowired
    private AsynEsResourceService esResourceOperation;

    /**
     * 创建知识点
     * 
     * @param viewModel
     * @param validResult
     * @return
     * @since
     */
    @MarkAspect4Format2Category
    @MarkAspect4OfflineToES
    @RequestMapping(method = { RequestMethod.POST }, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public KnowledgeViewModel4Out createKnowledge(@Validated(ValidCreateLessPropertiesGroup.class) @RequestBody KnowledgeViewModel4In viewModel,
                                              BindingResult validResult) {
        ValidResultHelper.valid(validResult,
                                "LC/CREATE_KNOWLEDGE_PARAM_VALID_FAIL",
                                "KnowledgeControllerV06",
                                "createKnowledge");
        viewModel.setIdentifier(UUID.randomUUID().toString());

        CommonHelper.inputParamValid(viewModel, "10110",OperationType.CREATE);
        
        // direction入参校验和设置默认值
        if (StringUtils.isEmpty(viewModel.getPosition().getDirection())) {// 如果direction为null或者""
            // 设置默认值,next
            viewModel.getPosition().setDirection(ChapterConstant.DIR_NEXT);
        } else if (!ChapterConstant.isDirection(viewModel.getPosition().getDirection())) {
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.DirectionParamError.getCode(),
                                          "direction的值目前只支持-pre和next");
        }
        
        if (StringUtils.isEmpty(viewModel.getPosition().getTarget())) {//当target为空的时候
            // 设置默认值,next
            viewModel.getPosition().setDirection(ChapterConstant.DIR_NEXT);
        }
        
        KnowledgeModel model = CommonHelper.convertViewModelIn(viewModel,
                                                               KnowledgeModel.class,
                                                               ResourceNdCode.knowledges);
        KnowledgeExtPropertiesModel extPropertiesModel = new KnowledgeExtPropertiesModel();
        extPropertiesModel.setDirection(viewModel.getPosition().getDirection());
        extPropertiesModel.setParent(viewModel.getPosition().getParent());
        extPropertiesModel.setTarget(viewModel.getPosition().getTarget());
        extPropertiesModel.setRootNode(viewModel.getPosition().getRootNode());
        model.setExtProperties(extPropertiesModel);

        model = knowledgeService.createKnowledge(model);
        
     // TODO titan保存章节树
        TitanTreeModel titanTreeModel = new TitanTreeModel();
        KnowledgeExtPropertiesViewModel position =  viewModel.getPosition();

        titanTreeModel.setTreeType(TitanTreeType.knowledges);
        titanTreeModel.setTreeDirection(TreeDirection.fromString(position.getDirection()));
        titanTreeModel.setTarget(position.getTarget());
        titanTreeModel.setParent(position.getParent());

        titanTreeModel.setSource(model.getIdentifier());

        // FIXME 有多个学科的时候只取其中一个
        if(org.apache.commons.lang3.StringUtils.isNotBlank(model.getExtProperties().getRootNode())){
        	titanTreeModel.setRoot(model.getExtProperties().getRootNode());
        }else{
        	 List<ResClassificationModel> categories = model.getCategoryList();
             for(ResClassificationModel category : categories){
                 if(category.getTaxoncode()!=null && category.getTaxoncode().contains("$S")){
                     titanTreeModel.setRoot(category.getTaxoncode());
                 }
             }
        }
       
        titanTreeMoveService.addNode(titanTreeModel);

        KnowledgeViewModel4Out viewModelOut = CommonHelper.convertViewModelOut(model, KnowledgeViewModel4Out.class);

        // 知识点没有techInfo属性
        viewModelOut.setTechInfo(null);
        viewModelOut.setEducationInfo(null);

        return viewModelOut;
    }

    /**
     * 更新知识点
     * 
     * @param viewModel
     * @param validResult
     * @param id
     * @return
     * @since
     */
    @MarkAspect4Format2Category
    @MarkAspect4OfflineToES
    @RequestMapping(value = "/{uuid}", method = { RequestMethod.PUT }, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public KnowledgeViewModel4Out updateKnowledge(@Validated(ValidUpdateLessPropertiesGroup.class) @RequestBody KnowledgeViewModel4In viewModel,
                                              BindingResult validResult,
                                              @PathVariable String uuid) {
        ValidResultHelper.valid(validResult,
                                "LC/UPDATE_KNOWLEDGE_PARAM_VALID_FAIL",
                                "KnowledgeControllerV06",
                                "updateKnowledge");
        viewModel.setIdentifier(uuid);

        CommonHelper.inputParamValid(viewModel, "10111",OperationType.UPDATE);

        KnowledgeModel model = CommonHelper.convertViewModelIn(viewModel,
                                                               KnowledgeModel.class,
                                                               ResourceNdCode.knowledges);
        model = knowledgeService.updateKnowledge(model);

        KnowledgeViewModel4Out viewModelOut = CommonHelper.convertViewModelOut(model, KnowledgeViewModel4Out.class);

        // 知识点没有techInfo属性
        viewModelOut.setTechInfo(null);
        viewModelOut.setExtProperties(null);
        viewModelOut.setEducationInfo(null);

        return viewModelOut;
    }

    /**
     * 更新知识点
     *
     * @param viewModel
     * @param validResult
     * @param id
     * @return
     * @since
     */
    @MarkAspect4Format2Category
    @RequestMapping(value = "/{uuid}", method = { RequestMethod.PATCH }, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public KnowledgeViewModel4Out patchKnowledge(@Validated(ValidUpdateLessPropertiesGroup.class) @RequestBody KnowledgeViewModel4In viewModel,
                                                  BindingResult validResult, @PathVariable String uuid,
                                                  @RequestParam(value = "notice_file", required = false,defaultValue = "true") boolean notice){
//        ValidResultHelper.valid(validResult,
//                "LC/UPDATE_KNOWLEDGE_PARAM_VALID_FAIL",
//                "KnowledgeControllerV06",
//                "updateKnowledge");
        viewModel.setIdentifier(uuid);

//        CommonHelper.inputParamValid(viewModel, "10111",OperationType.UPDATE);

        KnowledgeModel model = CommonHelper.convertViewModelIn(viewModel,
                KnowledgeModel.class,
                ResourceNdCode.knowledges, true);
        model = knowledgeService.patchKnowledge(model);

        KnowledgeViewModel4Out viewModelOut = CommonHelper.convertViewModelOut(model, KnowledgeViewModel4Out.class);

        // 知识点没有techInfo属性
        viewModelOut.setTechInfo(null);
        viewModelOut.setExtProperties(null);
        viewModelOut.setEducationInfo(null);

        if(notice) {
            offlineService.writeToCsAsync(ResourceNdCode.knowledges.toString(), uuid);
        }
        // offline metadata(coverage) to elasticsearch
        if (ResourceTypeSupport.isValidEsResourceType(ResourceNdCode.knowledges.toString())) {
            esResourceOperation.asynAdd(
                    new Resource(ResourceNdCode.knowledges.toString(), uuid));
        }

        return viewModelOut;
    }
    
    /**
     * 知识点的移动  
     * <p>Create Time: 2015年9月7日   </p>
     * <p>Create author: caocr   </p>
     * @param mid
     * @param cid
     */
    @RequestMapping(value = "/{uuid}/actions/move", method = RequestMethod.PUT, consumes = { MediaType.APPLICATION_JSON_VALUE })
    public void moveKnowledge(@Valid @RequestBody KnowledgeViewModel4Move knowledgeViewModel4Move,
                              BindingResult validResult,
                              @PathVariable(value="uuid") String kid) {
        //入参合法性校验
        ValidResultHelper.valid(validResult,
                                "LC/MOVE_KNOWLEDGE_PARAM_VALID_FAIL",
                                "KnowledgeControllerV06",
                                "moveKnowledge");
        
        // uuid校验
//        if (!CommonHelper.checkUuidPattern(kid)
//                || (StringUtils.isNotEmpty(knowledgeViewModel4Move.getTarget()) && !CommonHelper.checkUuidPattern(kid))) {
//            throw new WafException(LifeCircleErrorMessageMapper.CheckIdentifierFail.getCode(),
//                                   LifeCircleErrorMessageMapper.CheckIdentifierFail.getMessage());
//        }
//
//        if (!knowledgeViewModel4Move.getParent().equals("ROOT")
//                && !CommonHelper.checkUuidPattern(knowledgeViewModel4Move.getParent())) {
//            throw new WafException(LifeCircleErrorMessageMapper.CheckIdentifierFail.getCode(),
//                                   LifeCircleErrorMessageMapper.CheckIdentifierFail.getMessage());
//        }
    
        //direction入参校验和设置默认值
        if(StringUtils.isEmpty(knowledgeViewModel4Move.getDirection())){//如果direction为null或者""
            //设置默认值,next
            knowledgeViewModel4Move.setDirection(ChapterConstant.DIR_NEXT);
        }else if(!ChapterConstant.isDirection(knowledgeViewModel4Move.getDirection())){
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.DirectionParamError.getCode(),
                    "direction的值目前只支持-pre和next");
        }
        
        //转换为KnowledgeModel
        KnowledgeModel knowledgeModel = BeanMapperUtils.beanMapper(knowledgeViewModel4Move, KnowledgeModel.class);
        KnowledgeExtPropertiesModel extPropertiesModel = new KnowledgeExtPropertiesModel();
        extPropertiesModel.setDirection(knowledgeViewModel4Move.getDirection());
        extPropertiesModel.setParent(knowledgeViewModel4Move.getParent());
        extPropertiesModel.setTarget(knowledgeViewModel4Move.getTarget());
        knowledgeModel.setExtProperties(extPropertiesModel);
        knowledgeService.moveKnowledge(kid, knowledgeModel);
        
     // TODO titan保存章节树
        TitanTreeModel titanTreeModel = new TitanTreeModel();

        titanTreeModel.setTreeType(TitanTreeType.knowledges);
        titanTreeModel.setTreeDirection(TreeDirection.fromString(knowledgeViewModel4Move.getDirection()));
        titanTreeModel.setTarget(knowledgeViewModel4Move.getTarget());
        titanTreeModel.setParent(knowledgeViewModel4Move.getParent());
        titanTreeModel.setSource(kid);

        titanTreeMoveService.moveNode(titanTreeModel);
    }

    /**
     * 创建知识点关联
     * 
     * @param viewModel
     * @param validResult
     * @return
     * @since
     */
    @RequestMapping(value = "/relations", method = { RequestMethod.POST }, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public KnowledgeRelationsViewModel4Add addKnowledgeRelation(@Validated(LessPropertiesDefault.class) @RequestBody KnowledgeRelationsViewModel4Add viewModel,
                                                            BindingResult validResult) {
        ValidResultHelper.valid(validResult,
                                "LC/ADD_KNOWLEDGERELATION_PARAM_VALID_FAIL",
                                "KnowledgeControllerV06",
                                "addKnowledgeRelation");
        viewModel.setIdentifier(UUID.randomUUID().toString());

        KnowledgeRelationsModel model = BeanMapperUtils.beanMapper(viewModel, KnowledgeRelationsModel.class);

        model = knowledgeService.addKnowledgeRelation(model);

        viewModel = BeanMapperUtils.beanMapper(model, KnowledgeRelationsViewModel4Add.class);

        return viewModel;
    }

    /**
     * 查看知识点关联
     * 
     * @param contexttype 上下文类型
     * @param relationtype 关系类型
     * @param contextobjectid 上下文对象
     * @param knowledge 源知识点id
     * @return
     * @since
     */
    @RequestMapping(value = "/relations", method = { RequestMethod.GET }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody List<KnowledgeRelationsViewModel4Get> getKnowledgeRelations(@RequestParam(value = "contexttype", required = true) String contexttype,
                                                                                 @RequestParam(value = "relationtype", required = false) String relationtype,
                                                                                 @RequestParam(value = "contextobjectid", required = true) String contextobjectid,
                                                                                 @RequestParam(value = "knowledge", required = true) String knowledge) {
        // uuid校验
//        if (!CommonHelper.checkUuidPattern(knowledge) || !CommonHelper.checkUuidPattern(contextobjectid)) {
//            throw new WafException(LifeCircleErrorMessageMapper.CheckIdentifierFail.getCode(),
//                                   LifeCircleErrorMessageMapper.CheckIdentifierFail.getMessage());
//        }
        
        if (StringUtils.isEmpty(knowledge)) {
            throw new WafException(LifeCircleErrorMessageMapper.CheckGetKnowledgeRelationsKnowledgeParamFail.getCode(),
                                         LifeCircleErrorMessageMapper.CheckGetKnowledgeRelationsKnowledgeParamFail.getMessage());
        }
        
        if(StringUtils.isEmpty(contexttype)){
            throw new WafException(LifeCircleErrorMessageMapper.CheckvGetKnowledgeRelationsContexttypeParamFail.getCode(),
                                   LifeCircleErrorMessageMapper.CheckvGetKnowledgeRelationsContexttypeParamFail.getMessage());
        }
        
        if(StringUtils.isEmpty(contextobjectid)){
            throw new WafException(LifeCircleErrorMessageMapper.CheckvGetKnowledgeRelationsContextobjectidParamFail.getCode(),
                                   LifeCircleErrorMessageMapper.CheckvGetKnowledgeRelationsContextobjectidParamFail.getMessage());
        }
        
        List<KnowledgeRelationsModel> relations = knowledgeService.getKnowledgeRelations(contexttype,
                                                                                         relationtype,
                                                                                         contextobjectid,
                                                                                         knowledge);

        List<KnowledgeRelationsViewModel4Get> viewModelList = new ArrayList<KnowledgeRelationsViewModel4Get>();
        KnowledgeRelationsViewModel4Get viewModel = null;

        for (KnowledgeRelationsModel relation : relations) {
            viewModel = BeanMapperUtils.beanMapper(relation, KnowledgeRelationsViewModel4Get.class);
            KnowledgeViewModel4Out sourceViewModel = CommonHelper.convertViewModelOut(relation.getSourceKnowledgeModel(),
                                                                                      KnowledgeViewModel4Out.class);
            if (sourceViewModel != null) {
                sourceViewModel.setTechInfo(null);
                sourceViewModel.setPreview(null);
                sourceViewModel.setExtProperties(null);
                sourceViewModel.setEducationInfo(null);
            }
            KnowledgeViewModel4Out targetViewModel = CommonHelper.convertViewModelOut(relation.getTargetKnowledgeModel(),
                                                                                      KnowledgeViewModel4Out.class);
            if (targetViewModel != null) {
                targetViewModel.setTechInfo(null);
                targetViewModel.setPreview(null);
                targetViewModel.setExtProperties(null);
                targetViewModel.setEducationInfo(null);
            }
            viewModel.setSource(sourceViewModel);
            viewModel.setTarget(targetViewModel);
            viewModelList.add(viewModel);
        }

        return viewModelList;
    }

    @RequestMapping(value = "/relations/{id}", method = { RequestMethod.DELETE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody Map<String, String> deleteKnowledgeRelation(@PathVariable("id") String id) {
        // UUID校验
        if (!CommonHelper.checkUuidPattern(id)) {
            throw new WafException(LifeCircleErrorMessageMapper.CheckIdentifierFail.getCode(),
                                         LifeCircleErrorMessageMapper.CheckIdentifierFail.getMessage());
        }

        // 调用service
        knowledgeService.deleteKnowledgeRelation(id);

        return MessageConvertUtil.getMessageString(LifeCircleErrorMessageMapper.DeleteKnowledgesRelationSuccess);
    }

    @RequestMapping(value = "/tags", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public List<ChapterKnowledgeViewModel> addBatchChapterKnowledges(@RequestBody List<ChapterKnowledgeViewModel> knowledgeViewModels) {
//        ValidResultHelper.valid(validResult,
//                                "LC/ADD_BATCH_CHAPTER_KNOWLEDGE_TAGS_PARAM_VALID_FAIL",
//                                "KnowledgeControllerV06",
//                                "addBatchChapterKnowledges");
//        List<ChapterKnowledgeViewModel> knowledgeViewModels = new ArrayList<ChapterKnowledgeViewModel>();
//        knowledgeViewModels.add(knowledgeViewModels1);
        List<ChapterKnowledgeModel> knowledgeModels = new ArrayList<ChapterKnowledgeModel>();
        ChapterKnowledgeModel model = null;
        for (ChapterKnowledgeViewModel knowledgeViewModel : knowledgeViewModels) {
            if (StringUtils.isEmpty(knowledgeViewModel.getOutline())) {
                throw new WafException(LifeCircleErrorMessageMapper.CheckAddTagsOutlineParamFail.getCode(),
                                       LifeCircleErrorMessageMapper.CheckAddTagsOutlineParamFail.getMessage());
            }
            if (StringUtils.isEmpty(knowledgeViewModel.getKnowledge())) {
                throw new WafException(LifeCircleErrorMessageMapper.CheckAddTagsKnowledgeParamFail.getCode(),
                                       LifeCircleErrorMessageMapper.CheckAddTagsKnowledgeParamFail.getMessage());
            }
            if (CollectionUtils.isEmpty(knowledgeViewModel.getTags())) {
                throw new WafException(LifeCircleErrorMessageMapper.CheckAddTagsTagsParamFail.getCode(),
                                       LifeCircleErrorMessageMapper.CheckAddTagsTagsParamFail.getMessage());
            }
            model = BeanMapperUtils.beanMapper(knowledgeViewModel, ChapterKnowledgeModel.class);
            model.setIdentifier(UUID.randomUUID().toString());
            knowledgeModels.add(model);
        }

        knowledgeModels = knowledgeService.addBatchChapterKnowledges(knowledgeModels);

        List<ChapterKnowledgeViewModel> results = new ArrayList<ChapterKnowledgeViewModel>();
        ChapterKnowledgeViewModel result = null;
        for (ChapterKnowledgeModel knowledgeModel : knowledgeModels) {
            result = BeanMapperUtils.beanMapper(knowledgeModel, ChapterKnowledgeViewModel.class);
            results.add(result);
        }

        return results;
    }

    @RequestMapping(value = "/{id}/tags", method = { RequestMethod.DELETE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody Map<String, String> deleteChapterKnowledge(@PathVariable("id") String id,
                                                                    @RequestParam(value = "tag") String tag,
                                                                    @RequestParam(value = "outline") String outline) {
        // UUID校验
        if (!CommonHelper.checkUuidPattern(id)) {
            throw new WafException(LifeCircleErrorMessageMapper.CheckIdentifierFail.getCode(),
                                         LifeCircleErrorMessageMapper.CheckIdentifierFail.getMessage());
        }
        
        if (StringUtils.isEmpty(tag)) {
            throw new WafException(LifeCircleErrorMessageMapper.CheckDeleteTagTagParamFail.getCode(),
                                   LifeCircleErrorMessageMapper.CheckDeleteTagTagParamFail.getMessage());
        }
        
        if (StringUtils.isEmpty(outline)) {
            throw new WafException(LifeCircleErrorMessageMapper.CheckDeleteTagOutlineParamFail.getCode(),
                                   LifeCircleErrorMessageMapper.CheckDeleteTagOutlineParamFail.getMessage());
        }

        // 调用service
        knowledgeService.deleteKnowledgeChapterKnowledge(id, tag, outline);

        return MessageConvertUtil.getMessageString(LifeCircleErrorMessageMapper.DeleteKnowledgesChapterSuccess);
    }

}
