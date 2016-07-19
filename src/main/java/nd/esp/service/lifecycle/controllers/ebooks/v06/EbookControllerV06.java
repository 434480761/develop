package nd.esp.service.lifecycle.controllers.ebooks.v06;

import nd.esp.service.lifecycle.entity.elasticsearch.Resource;
import nd.esp.service.lifecycle.models.v06.EbookModel;
import nd.esp.service.lifecycle.services.ebooks.v06.EbookService;
import nd.esp.service.lifecycle.services.elasticsearch.AsynEsResourceService;
import nd.esp.service.lifecycle.services.offlinemetadata.OfflineService;
import nd.esp.service.lifecycle.support.annotation.MarkAspect4Format2Category;
import nd.esp.service.lifecycle.support.annotation.MarkAspect4OfflineJsonToCS;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.support.busi.ValidResultHelper;
import nd.esp.service.lifecycle.support.busi.elasticsearch.ResourceTypeSupport;
import nd.esp.service.lifecycle.support.enums.OperationType;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;
import nd.esp.service.lifecycle.vos.ebooks.v06.EbookViewModel;
import nd.esp.service.lifecycle.vos.valid.Valid4UpdateGroup;
import nd.esp.service.lifecycle.vos.valid.ValidGroup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 电子教材V0.6API
 * 
 * @author linsm
 */
@RestController
@RequestMapping("/v0.6/ebooks")
public class EbookControllerV06 {
    @Autowired
    @Qualifier("ebookServiceV06")
    private EbookService ebookService;

    @Autowired
    private OfflineService offlineService;
    @Autowired
    private AsynEsResourceService esResourceOperation;

    // private static final Logger LOG = LoggerFactory.getLogger(EbookControllerV06.class);

    /**
     * 创建电子教材对象
     * 
     * @param ebooViewModel 电子教材对象
     * @return
     */
    @MarkAspect4Format2Category
    @MarkAspect4OfflineJsonToCS
    @RequestMapping(value = "/{id}", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public EbookViewModel create(@Validated(ValidGroup.class) @RequestBody EbookViewModel ebooViewModel,
                                 BindingResult validResult,
                                 @PathVariable String id) {
        // 入参合法性校验
        ValidResultHelper.valid(validResult, "LC/CREATE_EBOOK_PARAM_VALID_FAIL", "EbookControllerV06", "create");
        ebooViewModel.setIdentifier(id);
        // 业务校验
        CommonHelper.inputParamValid(ebooViewModel, null, OperationType.CREATE);
        return operate(ebooViewModel, OperationType.CREATE);
    }

    /**
     * 修改电子教材对象
     * 
     * @param ebookViewModel 电子教材对象
     * @return
     */
    @MarkAspect4Format2Category
    @MarkAspect4OfflineJsonToCS
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public EbookViewModel update(@Validated(Valid4UpdateGroup.class) @RequestBody EbookViewModel ebookViewModel,
                                 BindingResult validResult,
                                 @PathVariable String id) {
        ValidResultHelper.valid(validResult, "LC/UPDATE_EBOOK_PARAM_VALID_FAIL", "EbookControllerV06", "update");
        ebookViewModel.setIdentifier(id);
        // 业务校验
        CommonHelper.inputParamValid(ebookViewModel, null, OperationType.UPDATE);
        return operate(ebookViewModel, OperationType.UPDATE);

    }

    /**
     * 修改电子教材对象
     *
     * @param ebookViewModel 电子教材对象
     * @return
     */
    @MarkAspect4Format2Category
    @RequestMapping(value = "/{id}", method = RequestMethod.PATCH, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public EbookViewModel patch(@Validated(Valid4UpdateGroup.class) @RequestBody EbookViewModel ebookViewModel,
                                 BindingResult validResult, @PathVariable String id,
                                 @RequestParam(value = "notice_file", required = false,defaultValue = "true") boolean notice){
//        ValidResultHelper.valid(validResult, "LC/UPDATE_EBOOK_PARAM_VALID_FAIL", "EbookControllerV06", "update");
        ebookViewModel.setIdentifier(id);
        // 业务校验
//        CommonHelper.inputParamValid(ebookViewModel, null, OperationType.UPDATE);
        EbookModel ebookModel = CommonHelper.convertViewModelIn(ebookViewModel, EbookModel.class, ResourceNdCode.ebooks, true);

        // 更新电子教材
        ebookModel = ebookService.patch(ebookModel);

        ebookViewModel = CommonHelper.convertViewModelOut(ebookModel, EbookViewModel.class);

        if(notice) {
            offlineService.writeToCsAsync(ResourceNdCode.ebooks.toString(), id);
        }
        // offline metadata(coverage) to elasticsearch
        if (ResourceTypeSupport.isValidEsResourceType(ResourceNdCode.ebooks.toString())) {
            esResourceOperation.asynAdd(
                    new Resource(ResourceNdCode.ebooks.toString(), id));
        }
        return ebookViewModel;

    }

    /**
     * @param ebookViewModel
     * @param operationType
     * @return
     * @since
     */
    private EbookViewModel operate(EbookViewModel ebookViewModel, OperationType operationType) {
        // 用来判断LifeCycle是否需要返回
        // boolean lcFlag = true;
        // // 入参合法性校验
        // // UUID校验
        // if (!CommonHelper.checkUuidPattern(ebookViewModel.getIdentifier())) {
        //
        // LOG.error( LifeCircleErrorMessageMapper.CheckIdentifierFail.getMessage());
        //
        // throw new WafSimpleException(LifeCircleErrorMessageMapper.CheckIdentifierFail.getCode(),
        // LifeCircleErrorMessageMapper.CheckIdentifierFail.getMessage());
        // }
        //
        // // keywords字符长度校验
        // if (!CommonHelper.checkListLength(ebookViewModel.getKeywords(), 1000)) {
        //
        // LOG.error( LifeCircleErrorMessageMapper.CheckKeywordsLengthFail.getMessage());
        //
        // throw new WafSimpleException(LifeCircleErrorMessageMapper.CheckKeywordsLengthFail.getCode(),
        // LifeCircleErrorMessageMapper.CheckKeywordsLengthFail.getMessage());
        // }
        // // tags字符长度校验
        // if (!CommonHelper.checkListLength(ebookViewModel.getTags(), 1000)) {
        //
        // LOG.error( LifeCircleErrorMessageMapper.CheckTagsLengthFail.getMessage());
        //
        // throw new WafSimpleException(LifeCircleErrorMessageMapper.CheckTagsLengthFail.getCode(),
        // LifeCircleErrorMessageMapper.CheckTagsLengthFail.getMessage());
        // }
        //
        // // techInfo属性校验
        // Map<String, ? extends ResTechInfoViewModel> techInfoMap = ebookViewModel.getTechInfo();
        // if (techInfoMap == null || !techInfoMap.containsKey("href")) {
        //
        // LOG.error( LifeCircleErrorMessageMapper.ChecTechInfoFail.getMessage());
        //
        // throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
        // LifeCircleErrorMessageMapper.ChecTechInfoFail);
        // }
        //
        // // categories属性校验
        // Map<String, List<? extends ResClassificationViewModel>> categories = ebookViewModel.getCategories();
        // if (CollectionUtils.isNotEmpty(categories)) {
        // for (String key : categories.keySet()) {
        // List<? extends ResClassificationViewModel> resClassificationViewModels = categories.get(key);
        // if (resClassificationViewModels != null && !resClassificationViewModels.isEmpty()) {
        // for (ResClassificationViewModel resClassificationViewModel : resClassificationViewModels) {
        // String path = resClassificationViewModel.getTaxonpath();
        // if (StringUtils.isNotEmpty(path)) {
        // if (!CommonHelper.checkK12Pattern(path)) {
        // // 目前只做了简单校验，当path为非空字符串，保证有6段（K12）
        // LOG.error("taxonpath不对，{}",path);
        //
        // LOG.error( LifeCircleErrorMessageMapper.CheckTaxonpathFail.getMessage());
        //
        // throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
        // LifeCircleErrorMessageMapper.CheckTaxonpathFail);
        // }
        // }
        // }
        // }
        // }
        // }
        //
        // if(operationType == OperationType.CREATE){
        // List<? extends ResCoverageViewModel> coverageList = ebookViewModel.getCoverages();
        // if(CollectionUtils.isNotEmpty(coverageList)){
        // int num = 0;
        // for (ResCoverageViewModel resCoverageViewModel : coverageList) {
        // if(resCoverageViewModel.getStrategy().toUpperCase().equals("OWNER")){
        // num++;
        // }
        // }
        // if(num > 1){
        // throw new
        // LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.CheckCoverageFail);
        // }
        // }
        // }

        // // model入参转换
        // EbookModel ebookModel = BeanMapperUtils.beanMapper(ebookViewModel, EbookModel.class);
        // if (ebookModel.getLifeCycle() == null) {
        // lcFlag = false;
        // }
        // ebookModel.setCategoryList(CommonHelper.map2List4Categories(ebookViewModel.getCategories(),
        // ebookViewModel.getIdentifier(),ResourceNdCode.ebooks));
        // ebookModel.setTechInfoList(CommonHelper.map2List4TechInfo(ebookViewModel.getTechInfo()));

        EbookModel ebookModel = CommonHelper.convertViewModelIn(ebookViewModel, EbookModel.class, ResourceNdCode.ebooks);
        if (operationType == OperationType.CREATE) {
            // 创建电子教材
            ebookModel = ebookService.create(ebookModel);
        } else { // FIXME 枚举是否可以采用更鲁棒的方式？
            // 更新电子教材
            ebookModel = ebookService.update(ebookModel);
        }

//        // model出参转换
//        ebookViewModel = ObjectUtils.fromJson(ObjectUtils.toJson(ebookModel), EbookViewModel.class);
//        // hvm = BeanMapperUtils.beanMapper(hm, EbookViewModel.class);
//        ebookViewModel.setCategories(CommonHelper.list2map4Categories(ebookModel.getCategoryList(), "res_type"));
//        ebookViewModel.setTechInfo(CommonHelper.list2Map4TechInfo(ebookModel.getTechInfoList()));
//
//        // 无须返回前台，将relations、coverages赋值为空
//        ebookViewModel.setRelations(null);
//        ebookViewModel.setCoverages(null);
//
//        if (!lcFlag) {
//            ebookViewModel.setLifeCycle(null);
//        }
        ebookViewModel = CommonHelper.convertViewModelOut(ebookModel, EbookViewModel.class);
        return ebookViewModel;
    }
}
