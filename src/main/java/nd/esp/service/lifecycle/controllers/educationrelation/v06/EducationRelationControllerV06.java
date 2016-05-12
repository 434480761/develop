package nd.esp.service.lifecycle.controllers.educationrelation.v06;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.Valid;

import nd.esp.service.lifecycle.educommon.services.impl.CommonServiceHelper;
import nd.esp.service.lifecycle.models.v06.BatchAdjustRelationOrderModel;
import nd.esp.service.lifecycle.models.v06.EducationRelationModel;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.services.educationrelation.v06.EducationRelationServiceForQuestionV06;
import nd.esp.service.lifecycle.services.educationrelation.v06.EducationRelationServiceV06;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.support.busi.ValidResultHelper;
import nd.esp.service.lifecycle.utils.BeanMapperUtils;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.MessageConvertUtil;
import nd.esp.service.lifecycle.vos.ListViewModel;
import nd.esp.service.lifecycle.vos.educationrelation.v06.EducationRelationViewModel;
import nd.esp.service.lifecycle.vos.educationrelation.v06.RelationForPathViewModel;
import nd.esp.service.lifecycle.vos.educationrelation.v06.RelationForQueryViewModel;
import nd.esp.service.lifecycle.vos.statics.CoverageConstant;
import nd.esp.service.lifecycle.vos.valid.CreateEducationRelationDefault;
import nd.esp.service.lifecycle.vos.valid.UpdateEducationRelationDefault;

import org.apache.commons.lang.StringUtils;
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

/**
 * 教育资源关系Controller(V0.6--增加生命周期)
 * 
 * @author caocr
 *
 */
@RestController
@RequestMapping("/v0.6/{res_type}")
public class EducationRelationControllerV06 {
    private final static Logger LOG = LoggerFactory.getLogger(EducationRelationControllerV06.class);
    
    @Autowired
    @Qualifier("educationRelationServiceV06")
    private EducationRelationServiceV06 educationRelationService;
    
    @Autowired
    @Qualifier("educationRelationServiceForQuestionV06")
    private EducationRelationServiceForQuestionV06 educationRelationServiceForQuestion;
    
    /**
     * 创建资源关系
     * 
     * @param resType                              源资源类型
     * @param sourceUuid                           源资源的id
     * @param educationRelationModel               创建时的入参
     * @param bindingResult                        入参校验的绑定结果
     * @return
     * @since
     */
    @RequestMapping(value = "/{source_uuid}/relations", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public EducationRelationViewModel createRelation(@PathVariable(value="res_type") String resType,@PathVariable(value="source_uuid") String sourceUuid,
                                              @Validated(CreateEducationRelationDefault.class) @RequestBody EducationRelationViewModel educationRelationViewModel,BindingResult bindingResult){
        // 校验入参
        ValidResultHelper.valid(bindingResult,
                                "LC/CREATE_RELATION_PARAM_VALID_FAIL",
                                "EducationRelationControllerV06",
                                "createRelation");

        //数据模型转换
        EducationRelationModel educationRelationModel = BeanMapperUtils.beanMapper(educationRelationViewModel,
                                                                                   EducationRelationModel.class);
        educationRelationModel.setResType(resType);
        educationRelationModel.setSource(sourceUuid);
        
        List<EducationRelationModel> educationRelationModels = new ArrayList<EducationRelationModel>();
        educationRelationModels.add(educationRelationModel);
        // 创建资源关系
        List<EducationRelationModel> resultList = null;
        if (CommonServiceHelper.isQuestionDb(resType)) {
            resultList = educationRelationServiceForQuestion.createRelation(educationRelationModels, false);
        } else {
            resultList = educationRelationService.createRelation(educationRelationModels, false);
        }
        
        if(CollectionUtils.isEmpty(resultList) || resultList.size() != 1){
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CreateEducationRelationFail);
        }
        
        //返回处理,这里只会有一条记录
        educationRelationViewModel = BeanMapperUtils.beanMapper(resultList.get(0), EducationRelationViewModel.class);
        //不需要显示
        educationRelationViewModel.setResourceTargetType(null);
        
        if (LOG.isInfoEnabled()) {
            LOG.info("资源关系V0.6创建成功,resType:{},sourceId:{},targetType:{},targetId:{},relationType:{}",
                     resType,
                     sourceUuid,
                     educationRelationViewModel.getResourceTargetType(),
                     educationRelationViewModel.getTarget(),
                     educationRelationViewModel.getRelationType());
        }
        
        return educationRelationViewModel;
    }
    
    /**
     * 修改资源关系
     * 
     * @param resType                                       源资源类型
     * @param sourceUuid                                    源资源的id
     * @param rid                                           资源关系id
     * @param educationRelationForUpdateModel               修改时传入的参数
     * @param bindingResult                                 入参校验的绑定结果
     * @return
     */
    @RequestMapping(value = "/{source_uuid}/relations/{rid}", method = RequestMethod.PUT, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public EducationRelationViewModel updateRelation(@PathVariable(value="res_type") String resType,@PathVariable(value="source_uuid") String sourceUuid, 
            @PathVariable(value="rid") String rid,@Validated(UpdateEducationRelationDefault.class) @RequestBody EducationRelationViewModel educationRelationViewModel,BindingResult bindingResult) {
        //校验入参
        ValidResultHelper.valid(bindingResult,
                                "LC/UPDATE_RELATION_PARAM_VALID_FAIL",
                                "EducationRelationControllerV06",
                                "updateRelation");
        
        // 数据模型转换
        EducationRelationModel educationRelationModel = BeanMapperUtils.beanMapper(educationRelationViewModel,
                                                                                   EducationRelationModel.class);
        
        //修改资源关系
        if (CommonServiceHelper.isQuestionDb(resType)) {
            educationRelationModel = educationRelationServiceForQuestion.updateRelation(resType, sourceUuid, rid, educationRelationModel);
        } else {
            educationRelationModel = educationRelationService.updateRelation(resType, sourceUuid, rid, educationRelationModel);
        }
        
        educationRelationViewModel = BeanMapperUtils.beanMapper(educationRelationModel, EducationRelationViewModel.class);
        // 不需要显示
        educationRelationViewModel.setResourceTargetType(null);
        
        if(LOG.isInfoEnabled()){
            LOG.info(
                    "资源关系V0.6修改成功,resType:{},sourceId:{},targetType:{},targetId:{},relationType:{}",
                    resType, sourceUuid,
                    educationRelationViewModel.getResourceTargetType(),
                    educationRelationViewModel.getTarget(),
                    educationRelationViewModel.getRelationType());
        }
        
        return educationRelationViewModel;
    }
    
    /**
     * 删除资源关系   
     * 
     * @param rid      资源关系id
     * @return
     */
    @RequestMapping(value = "/{source_uuid}/relations/{rid}", method = RequestMethod.DELETE)
    public @ResponseBody Map<String, String> deleteRelation(
            @PathVariable(value="res_type") String resType,@PathVariable(value="source_uuid") String sourceUuid,
            @PathVariable(value="rid") String rid) {
        boolean flag = true;
        if (CommonServiceHelper.isQuestionDb(resType)) {
            flag = educationRelationServiceForQuestion.deleteRelation(rid, sourceUuid, resType);
        } else {
            flag = educationRelationService.deleteRelation(rid, sourceUuid, resType);
        }
        if (!flag) {
            return MessageConvertUtil
                    .getMessageString(LifeCircleErrorMessageMapper.DeleteEducationRelationFail);
        }
        
        if(LOG.isInfoEnabled()){
            LOG.info("资源关系V0.6删除关系成功,rid:{}",rid);
        }
        
        return MessageConvertUtil
                .getMessageString(LifeCircleErrorMessageMapper.DeleteEducationRelationSuccess);
    }
    
    /**
     * 条件删除资源之间的关系  
     * 
     * @param resType          源资源类型
     * @param sourceUuid       源资源id
     * @param target           目标对象的id集合
     * @param relationType     关系类型
     * @param reverse          指定查询的关系是否进行反向查询。如果reverse参数不携带，默认是从s->t查询，如果reverse=true，反向查询T->S
     * @return
     */
    @RequestMapping(value = "/{source_uuid}/relations", method = RequestMethod.DELETE)
    public @ResponseBody Map<String, String> deleteRelationByTarget(
           @PathVariable(value="res_type") String resType,@PathVariable(value="source_uuid") String sourceUuid,
           @RequestParam(required=false)List<String> target,
           @RequestParam(required=false,value="relation_type")String relationType,
           @RequestParam(required=false) String reverse){
        boolean reverseBoolean=false;
        if("true".equals(reverse)){
            reverseBoolean=true;
        }
        
        boolean flag = true;
        boolean flag1 = true;
        boolean flag2 = true;
        if (reverseBoolean) {//当反转时，调两次接口
            flag1 = educationRelationServiceForQuestion.deleteRelationByTarget(resType,
                                                                               sourceUuid,
                                                                               target,
                                                                               relationType,
                                                                               reverseBoolean);
            flag2 = educationRelationService.deleteRelationByTarget(resType,
                                                                    sourceUuid,
                                                                    target,
                                                                    relationType,
                                                                    reverseBoolean);
            flag = flag1 && flag2;
        } else {
            if (CommonServiceHelper.isQuestionDb(resType)) {
                flag = educationRelationServiceForQuestion.deleteRelationByTarget(resType,
                                                                                  sourceUuid,
                                                                                  target,
                                                                                  relationType,
                                                                                  reverseBoolean);
            } else {
                flag = educationRelationService.deleteRelationByTarget(resType,
                                                                       sourceUuid,
                                                                       target,
                                                                       relationType,
                                                                       reverseBoolean);
            }
        }
        
        if (!flag) {
            return MessageConvertUtil
                    .getMessageString(LifeCircleErrorMessageMapper.DeleteBatchRelationFail);
        }
        return MessageConvertUtil
                .getMessageString(LifeCircleErrorMessageMapper.DeleteBatchRelationSuccess);
    }
    
    /**
     * 根据目标类型删除资源之间的关系  
     * 
     * @param resType          源资源类型
     * @param sourceUuid       源资源id
     * @param targetType       目标对象类型
     * @param relationType     关系类型
     * @param reverse          指定查询的关系是否进行反向查询。如果reverse参数不携带，默认是从s->t查询，如果reverse=true，反向查询T->S
     * @return
     */
    @RequestMapping(value = "/{source_uuid}/relations", method = RequestMethod.DELETE,params={"target_type"})
    public @ResponseBody Map<String, String> deleteRelationByTargetType(
           @PathVariable(value="res_type") String resType,@PathVariable(value="source_uuid") String sourceUuid,
           @RequestParam(value="target_type")List<String> targetType,
           @RequestParam(required=false,value="relation_type")String relationType,
           @RequestParam(required=false) String reverse){
        boolean reverseBoolean=false;
        if("true".equals(reverse)){
            reverseBoolean=true;
        }
        
        boolean flag = true;
        boolean flag1 = true;
        boolean flag2 = true;
        if (reverseBoolean) {//当反转时，调两次接口
            flag1 = educationRelationServiceForQuestion.deleteRelationByTargetType(resType,
                                                                                   sourceUuid,
                                                                                   targetType,
                                                                                   relationType,
                                                                                   reverseBoolean);
            flag2 = educationRelationService.deleteRelationByTargetType(resType,
                                                                        sourceUuid,
                                                                        targetType,
                                                                        relationType,
                                                                        reverseBoolean);
            flag = flag1 && flag2;
        } else {
            if (CommonServiceHelper.isQuestionDb(resType)) {
                flag = educationRelationServiceForQuestion.deleteRelationByTargetType(resType,
                                                                                      sourceUuid,
                                                                                      targetType,
                                                                                      relationType,
                                                                                      reverseBoolean);
            } else {
                flag = educationRelationService.deleteRelationByTargetType(resType,
                                                                           sourceUuid,
                                                                           targetType,
                                                                           relationType,
                                                                           reverseBoolean);
            }
        }
        if (!flag) {
            return MessageConvertUtil
                    .getMessageString(LifeCircleErrorMessageMapper.DeleteBatchRelationFail);
        }
        return MessageConvertUtil
                .getMessageString(LifeCircleErrorMessageMapper.DeleteBatchRelationSuccess);
    }
    
    /**
     * 获取资源之间的关系
     *     
     * @param id                   源资源的id标识
     * @param relationPath         查询的关系路径
     * @param reverse              关系是否进行反转
     * @param categoryPattern      分类维度的应用模式
     * @return
     */
    @RequestMapping(value = "/{id}/relations", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody List<List<RelationForPathViewModel>> getRelationsByConditions(
           @PathVariable(value="res_type") String resType,@PathVariable String id,
           @RequestParam(value="relation_path")String relationPath,
           @RequestParam(required=false) boolean reverse,
           @RequestParam(required=false,value="category_pattern")String categoryPattern){
        List<List<RelationForPathViewModel>> resultList = 
                educationRelationService.getRelationsByConditions(resType, id, Arrays.asList(relationPath.split("/")), reverse, categoryPattern);
                
        return resultList;
    }
    
    /**
     * 批量修改资源关系的顺序  
     * 
     * @param resType          元资源的类型
     * @param sourceUuid       源资源的id
     * @param target           需要移动的目标对象
     * @param destination      移动目的地靶心对象
     * @param adjoin           相邻对象的id，如果在第一个和最后一个的时候，不存在相邻对象，传入为none。
     * @param at               移动的方向标识，first是移动到第一个位置，last是将这个关系增加到列表的最后，middle是将目标增加到destination和adjoin中间。
     */
    @RequestMapping(value = "/{source_uuid}/relations/order", method = RequestMethod.PUT, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public void batchAdjustRelationOrder(@PathVariable(value = "res_type") String resType,
                                         @PathVariable(value = "source_uuid") String sourceUuid,
                                         @Valid @RequestBody List<BatchAdjustRelationOrderModel> batchAdjustRelationOrderModels,
                                         BindingResult bindingResult) {
        // 调用service
        if (CommonServiceHelper.isQuestionDb(resType)) {
            educationRelationServiceForQuestion.batchAdjustRelationOrder(resType,
                                                                         sourceUuid,
                                                                         batchAdjustRelationOrderModels);
        } else {
            educationRelationService.batchAdjustRelationOrder(resType, sourceUuid, batchAdjustRelationOrderModels);
        }
    }
    
    /**
     * 关系目标资源检索 
     * <p>Create Time: 2015年5月18日   </p>
     * @param resType          源资源类型
     * @param sourceUuid       源资源id
     * @param categories       分类维度数据
     * @param targetType       目标资源类型
     * @param label            资源关系标识
     * @param tags             资源关系标签
     * @param relationType     关系类型
     * @param limit            分页参数，第一个值为记录索引参数，第二个值为偏移量
     * @param reverse          指定查询的关系是否进行反向查询。如果reverse参数不携带，默认是从s->t查询，如果reverse=true，反向查询T->S
     * @param recursion        是否根据源资源进行递归查询,举例：通过给定的章节id，递归查询其子章节下的所有知识点信息(新增参数)
     * @param ctType           指定覆盖范围的查询类型，具体选值是Org，Role，User，Time，Space，Group
     * @param ct               指定覆盖范围的查询类型，可以是VIEW，PLAY，SHAREING，REPORTING,COPY，NONE
     * @param cTarget          指定查询覆盖范围目标的具体值
     * @return
     */
    @RequestMapping(value = "/{source_uuid}/targets", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody ListViewModel<RelationForQueryViewModel> searchByResType(@PathVariable(value = "res_type") String resType,
                                                                                  @PathVariable(value = "source_uuid") String sourceUuid,
                                                                                  @RequestParam(required = false) String categories,
                                                                                  @RequestParam(value = "target_type") String targetType,
                                                                                  @RequestParam(required = false) String label,
                                                                                  @RequestParam(required = false, value="relation_tags") String tags,
                                                                                  @RequestParam(value = "relation_type") String relationType,
                                                                                  @RequestParam String limit,
                                                                                  @RequestParam(required = false) String reverse,
                                                                                  @RequestParam(required = false) String recursion,
                                                                                  @RequestParam(required = false, value = "ct_type") String ctType,
                                                                                  @RequestParam(required = false) String ct,
                                                                                  @RequestParam(required = false, value = "ct_target") String cTarget) {
        if (StringUtils.isEmpty(targetType)) {

            LOG.error("目标资源类型必须要传值，不能为空");

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.CheckTargetTypeIsNull);
        }
        
        //覆盖范围参数处理
        if(StringUtils.isEmpty(ctType)){
            ctType = "*";
        }else{
            if(!CoverageConstant.isCoverageTargetType(ctType,true)){
                
                LOG.error("覆盖范围类型不在可选范围内");
                
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        LifeCircleErrorMessageMapper.CoverageTargetTypeNotExist);
            }
        }
        if(StringUtils.isEmpty(ct)){
            ct = "*";
        }else{
            if(!CoverageConstant.isCoverageStrategy(ct,true)){
                
                LOG.error("资源操作类型不在可选范围内");
                
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        LifeCircleErrorMessageMapper.CoverageStrategyNotExist);
            }
        }
        if(StringUtils.isEmpty(cTarget)){
            cTarget = "*";
        }
        String coverage = ctType + "/" + cTarget + "/" + ct;
        
        if(coverage.equals("*/*/*")){
            coverage = null;
        }
        
        //反向查询boolean,默认为false
        boolean reverseBoolean = false;
        if("true".equals(reverse)){
            reverseBoolean = true;
        }
        //递归查询boolean,默认为false
        boolean recursionBoolean = false;
        if("true".equals(recursion)){
            recursionBoolean = true;
        }
        
        limit = CommonHelper.checkLimitMaxSize(limit);
        
        
        ListViewModel<RelationForQueryViewModel> listViewModel = null;
        try {
            if(!recursionBoolean){
                listViewModel = educationRelationService.queryListByResTypeByDB(
                                   resType, sourceUuid, categories, targetType, label, tags, relationType, limit, reverseBoolean, coverage);
            }else if(IndexSourceType.ChapterType.getName().equals(resType)){
                listViewModel = educationRelationService.recursionQueryResourcesByDB(
                        resType, sourceUuid, categories, targetType, label, tags, relationType, limit,coverage);
            }else{
                
                LOG.error("递归查询res_type目前仅支持chapters");
                
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        LifeCircleErrorMessageMapper.RelationSupportTypeError);
            }
        } catch (EspStoreException e) {
            LOG.error("通过资源关系获取资源列表失败",e);
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.GetEducationRelationListFail.getCode(),e.getMessage());
        }
        
        return listViewModel;
    }
    
    /**
     * 在有些情景下，单个的获取源资源的目标资源列表的接口，业务系统使用起来过于频繁。此时业务方提出需要能够进行设置批量的源资源ID，
     * 通过源资源的ID快速的查询目标资源的列表。
                 1.接口提供设置源资源ID的列表进行批量查询
                 2.接口提供设置关系的类型
                 3.接口提供设置目标资源的类型	
     * <p>Create Time: 2015年10月19日   </p>
     * <p>Create author: xiezy   </p>
     * @param resType 源资源类型
     * @param sids 源资源id，可批量
     * @param targetType 目标资源类型
     * @param label            资源关系标识
     * @param tags             资源关系标签
     * @param relationType 关系类型
     * @param limit 分页参数
     */
    @RequestMapping(value = "/resources/relations/targets/bulk", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody ListViewModel<RelationForQueryViewModel> batchQueryResources(@PathVariable(value="res_type") String resType,
            @RequestParam(value="sid") Set<String> sids,
            @RequestParam(value="target_type") String targetType,
            @RequestParam(required = false) String label,
            @RequestParam(required = false, value="relation_tags") String tags,
            @RequestParam(required=false,value="relation_type") String relationType,
            @RequestParam String limit,
            @RequestParam(required=false) boolean reverse){
        if (StringUtils.isEmpty(targetType)) {

            LOG.error("目标资源类型必须要传值，不能为空");

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.CheckTargetTypeIsNull);
        }
        
        limit = CommonHelper.checkLimitMaxSize(limit);
        
//        return educationRelationService.batchQueryResources(resType, sids, targetType, relationType, limit);
        return educationRelationService.batchQueryResourcesByDB(resType, sids, targetType, label, tags, relationType, limit,reverse);
    }
}
