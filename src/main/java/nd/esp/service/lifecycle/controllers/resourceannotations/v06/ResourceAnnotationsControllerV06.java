package nd.esp.service.lifecycle.controllers.resourceannotations.v06;

import nd.esp.service.lifecycle.educommon.services.impl.CommonServiceHelper;
import nd.esp.service.lifecycle.models.v06.ResourceAnnotationModel;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.services.resourceannotations.v06.ResourceAnnotationsServiceV06;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.busi.ValidResultHelper;
import nd.esp.service.lifecycle.utils.BeanMapperUtils;
import nd.esp.service.lifecycle.utils.MessageConvertUtil;
import nd.esp.service.lifecycle.vos.ListViewModel;
import nd.esp.service.lifecycle.vos.resourceannotations.v06.ResourceAnnotationViewModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 资源评注 api v06
 * Created by caocr on 2015/11/25 0025.
 */
@RestController
@RequestMapping("/v0.6/{res_type}/{uuid}/annotations")
public class ResourceAnnotationsControllerV06 {
    private static final Logger LOG = LoggerFactory.getLogger(ResourceAnnotationsControllerV06.class);

    private static final int CONTROLLER_CREATE_TYPE = 0;// 新增资源评注操作
    private static final int CONTROLLER_UPDATE_TYPE = 1;// 修改资源评注操作

    @Autowired
    @Qualifier("resourceAnnotationsServiceImplV06")
    private ResourceAnnotationsServiceV06 resourceAnnotationsServiceV06;
    
    @Autowired
    @Qualifier("resourceAnnotationsService4QuestionDBImplV06")
    private ResourceAnnotationsServiceV06 resourceAnnotationsServiceQuestionDBV06;

    /**
     * 添加资源评注
     * 
     * @param resType 资源类型
     * @param resId 资源id
     * @param viewModel 资源评注
     * @param validResult BindingResult
     * @return 资源评注
     * @since
     */
    @RequestMapping(value = "", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResourceAnnotationViewModel addResourceAnnotation(@PathVariable("res_type") String resType,
                                                             @PathVariable("uuid") String resId,
                                                             @Valid @RequestBody ResourceAnnotationViewModel viewModel,
                                                             BindingResult validResult) {
        // 校验入参
        ValidResultHelper.valid(validResult,
                "LC/CREATE_RESOURCEANNOTATION_PARAM_VALID_FAIL",
                "ResourceAnnotationsControllerV06",
                "create");

        //随机一个评注uuid
        viewModel.setIdentifier(UUID.randomUUID().toString());

        return resourceAnnotationApi(viewModel, resType, resId, CONTROLLER_CREATE_TYPE);
    }

    /**
     * 修改资源评注
     * 
     * @param resType 资源类型
     * @param resId 资源id
     * @param annUuid 资源评注id
     * @param viewModel 资源评注
     * @param validResult BindingResult
     * @return 资源评注
     * @since
     */
    @RequestMapping(value = "/{ano_uuid}", method = RequestMethod.PUT, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResourceAnnotationViewModel updateResourceAnnotation(@PathVariable("res_type") String resType,
                                                                @PathVariable("uuid") String resId,
                                                                @PathVariable("ano_uuid") String annUuid,
                                                                @Valid @RequestBody ResourceAnnotationViewModel viewModel,
                                                                BindingResult validResult) {
        // 校验入参
        ValidResultHelper.valid(validResult,
                "LC/UPDATE_RESOURCEANNOTATION_PARAM_VALID_FAIL",
                "ResourceAnnotationsControllerV06",
                "update");

        viewModel.setIdentifier(annUuid);

        return resourceAnnotationApi(viewModel, resType, resId, CONTROLLER_UPDATE_TYPE);
    }
    
    /**
     * 通过评注id删除资源评注
     * 
     * @param resType 资源类型
     * @param resId 资源id
     * @param annoId 资源评注id
     * @return 资源评注
     * @since
     */
    @RequestMapping(value = "/{ano_uuid}", method = RequestMethod.DELETE)
    public @ResponseBody Map<String, String> deleteResourceAnnotationByAnnoId(@PathVariable("res_type") String resType,
                                                                              @PathVariable("uuid") String resId,
                                                                              @PathVariable("ano_uuid") String annoId) {
        boolean flag = getResourceAnnotationsService(resType).deleteResourceAnnotationByAnnoId(resType, resId, annoId);
        if (!flag) {
            return MessageConvertUtil.getMessageString(LifeCircleErrorMessageMapper.DeleteResourceAnnotationFail);
        }

        return MessageConvertUtil.getMessageString(LifeCircleErrorMessageMapper.DeleteResourceAnnotationSuccess);
    }
    
    /**
     * 通过资源id批量删除资源评注
     * 
     * @param resType 资源类型
     * @param resId 资源id
     * @return
     * @since
     */
    @RequestMapping(value = "", method = RequestMethod.DELETE)
    public @ResponseBody Map<String, String> deleteResourceAnnotationByResId(@PathVariable("res_type") String resType,
                                                                              @PathVariable("uuid") String resId) {
        boolean flag = getResourceAnnotationsService(resType).deleteResourceAnnotationByResId(resType, resId);
        if (!flag) {
            return MessageConvertUtil.getMessageString(LifeCircleErrorMessageMapper.DeleteResourceAnnotationFail);
        }

        return MessageConvertUtil.getMessageString(LifeCircleErrorMessageMapper.DeleteResourceAnnotationSuccess);
    }
    
    /**
     * 通过资源id、用户id批量删除资源评注
     * 
     * @param resType 资源类型
     * @param resId 资源id
     * @param entityId 用户id
     * @return
     * @since
     */
    @RequestMapping(value = "/entity/{entity_uuid}", method = RequestMethod.DELETE)
    public @ResponseBody Map<String, String> deleteResourceAnnotationByEntityId(@PathVariable("res_type") String resType,
                                                                                @PathVariable("uuid") String resId,
                                                                                @PathVariable("entity_uuid") String entityId) {
        boolean flag = getResourceAnnotationsService(resType).deleteResourceAnnotationByEntityId(resType, resId, entityId);
        if (!flag) {
            return MessageConvertUtil.getMessageString(LifeCircleErrorMessageMapper.DeleteResourceAnnotationFail);
        }

        return MessageConvertUtil.getMessageString(LifeCircleErrorMessageMapper.DeleteResourceAnnotationSuccess);
    }
    
    /**
     * 通过资源id查询资源评注
     * 
     * @param resType 资源类型
     * @param resId 资源id
     * @param limit 分页参数
     * @return
     * @since
     */
    @RequestMapping(value = "", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody ListViewModel<ResourceAnnotationViewModel> queryResourceAnnotationsByResId(@PathVariable("res_type") String resType,
                                                                                      @PathVariable("uuid") String resId,
                                                                                      @RequestParam String limit) {
        ListViewModel<ResourceAnnotationModel> resourceAnnotationModels = getResourceAnnotationsService(resType).queryResourceAnnotationsByResId(resType,
                                                                                                                                        resId,
                                                                                                                                        limit);
        ListViewModel<ResourceAnnotationViewModel> resourceAnnotationViewModels = new ListViewModel<ResourceAnnotationViewModel>();
        resourceAnnotationViewModels.setTotal(resourceAnnotationModels.getTotal());
        resourceAnnotationViewModels.setLimit(resourceAnnotationModels.getLimit());
        List<ResourceAnnotationViewModel> viewModels = new ArrayList<ResourceAnnotationViewModel>();
        for (ResourceAnnotationModel model : resourceAnnotationModels.getItems()) {
            viewModels.add(BeanMapperUtils.beanMapper(model, ResourceAnnotationViewModel.class));
        }
        resourceAnnotationViewModels.setItems(viewModels);
        
        return resourceAnnotationViewModels;
    }

    /**
     * 业务统一方法
     *
     * @param viewModel 资源评注对象
     * @param type 业务类型
     * @return ResourceAnnotationViewModel
     * @since
     */
    private ResourceAnnotationViewModel resourceAnnotationApi(ResourceAnnotationViewModel viewModel, String resType, String resId, int type) {
        ResourceAnnotationModel model = BeanMapperUtils.beanMapper(viewModel, ResourceAnnotationModel.class);

        if (type == CONTROLLER_CREATE_TYPE) {
            // 创建资源评注

            LOG.info("资源标注V06---添加资源标注操作，业务逻辑处理");

            model = getResourceAnnotationsService(resType).addResourceAnnotation(model, resType, resId);
        } else if (type == CONTROLLER_UPDATE_TYPE) {
            // 修改资源评注

            LOG.info("资源标注V06---更新资源标注操作，业务逻辑处理");

            model = getResourceAnnotationsService(resType).updateResourceAnnotation(model, resType, resId);
        }

        ResourceAnnotationViewModel result = BeanMapperUtils.beanMapper(model, ResourceAnnotationViewModel.class);
        result.setResource(resId);
        
        return result;
    }
    
    /**
     * 根据资源类型获取service层bean
     * @param resType
     * @return
     */
    private ResourceAnnotationsServiceV06 getResourceAnnotationsService(String resType){
    	if(!CommonServiceHelper.isQuestionDb(resType)){
    		return resourceAnnotationsServiceV06;
    	}
    	return resourceAnnotationsServiceQuestionDBV06;
    }
}
