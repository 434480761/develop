package nd.esp.service.lifecycle.controllers.lifecycle.v06;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import nd.esp.service.lifecycle.app.LifeCircleWebConfig;
import nd.esp.service.lifecycle.educommon.models.ResContributeModel;
import nd.esp.service.lifecycle.educommon.services.impl.CommonServiceHelper;
import nd.esp.service.lifecycle.entity.elasticsearch.Resource;
import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.model.Contribute;
import nd.esp.service.lifecycle.services.elasticsearch.AsynEsResourceService;
import nd.esp.service.lifecycle.services.lifecycle.v06.LifecycleServiceV06;
import nd.esp.service.lifecycle.services.offlinemetadata.OfflineService;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.busi.ValidResultHelper;
import nd.esp.service.lifecycle.utils.MessageConvertUtil;
import nd.esp.service.lifecycle.vos.ListViewModel;
import nd.esp.service.lifecycle.vos.lifecycle.v06.ResContributeViewModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.nd.gaea.WafException;


@RestController
@RequestMapping("/v0.6/{res_type}")
public class LifecycleControllerV06 {
    
    @Autowired()
    @Qualifier("lifecycleServiceV06")
    private LifecycleServiceV06 lifecycleService;
    
    @Autowired()
    @Qualifier("lifecycleService4QtiV06")
    private LifecycleServiceV06 lifecycleService4Qti;

    @Autowired
    private OfflineService offlineService;
    
    @Autowired
    private AsynEsResourceService esResourceOperation;
    
    /**
     * 增加生命周期阶段。 对于生命周期的某个环节，进行添加
     * @Method POST
     * @urlpattern  {res_type}/{uuid}/lifecycle/steps
     * 
     * @param resType
     * @param resId
     * @param contribute
     */
    @RequestMapping(value = "/{uuid}/lifecycle/steps", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody ResContributeViewModel addLifecycleStep(@PathVariable String res_type, @PathVariable String uuid,
            @Valid @RequestBody ResContributeModel contributeModel, BindingResult validResult) {
        //入参合法性校验
        ValidResultHelper.valid(validResult, LifeCircleErrorMessageMapper.CreateLifecycleFail.getCode(),
                "LifecycleControllerV06","addLifecycleStep");
        
        LifecycleServiceV06 service = !CommonServiceHelper.isQuestionDb(res_type) ? lifecycleService : lifecycleService4Qti;
        
        ResContributeViewModel contributeViewModel = service.addLifecycleStep(res_type, uuid, contributeModel);
        
        offlineService.writeToCsAsync(res_type, uuid);
        esResourceOperation.asynAdd(new Resource(res_type, uuid));
        
        return contributeViewModel;
    }

    /**
     * 批量资源增加生命周期阶段。 可以对批量资源进行同一生命周期及阶段的添加
     * @Method POST
     * 
     * @urlpattern  {res_type}/lifecycle/steps/bulk
     * 
     * @param resType
     * @param resIds
     * @param contribute
     */
    @RequestMapping(value = "/lifecycle/steps/bulk", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody Map<String,ResContributeViewModel> addLifecycleStepBulk(@PathVariable String res_type, 
            @Valid @RequestBody ResContributeModel contributeModel, BindingResult validResult) {
        ValidResultHelper.valid(validResult, LifeCircleErrorMessageMapper.CreateBatchLifecycleFail.getCode(),
                "LifecycleControllerV06","addLifecycleStepBulk");
        
        LifecycleServiceV06 service = !CommonServiceHelper.isQuestionDb(res_type) ? lifecycleService : lifecycleService4Qti;
        
        List<String> uuids = contributeModel.getResources();
        Map<String,ResContributeViewModel> contributeViewModels = 
                service.addLifecycleStepBulk(res_type, uuids, contributeModel);
        
        for(String uuid:uuids) {
            offlineService.writeToCsAsync(res_type, uuid);
            esResourceOperation.asynAdd(new Resource(res_type, uuid));
        }
        
        return contributeViewModels;
    }

    /**
     * 获取指定资源的生命周期阶段详细。通过资源uuid获取资源阶段的详细信息
     * @Method GET
     * @urlpattern  {res_type}/{uuid}/lifecycle/steps?limit=(0,20)
     * 
     * @param resType
     * @param uuid
     */
    @RequestMapping(value = "/{uuid}/lifecycle/steps", params = { "limit" }, method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody ListViewModel<ResContributeViewModel> getLifecycleSteps(@PathVariable String res_type,
            @PathVariable String uuid, @RequestParam String limit) {
        LifecycleServiceV06 service = !CommonServiceHelper.isQuestionDb(res_type) ? lifecycleService : lifecycleService4Qti;
        
        ListViewModel<ResContributeViewModel> contributeViewModel = service.getLifecycleSteps(res_type, uuid, limit);
        return contributeViewModel;
    }

    /**
     * 修改资源生命周期阶段。通过资源的uuid，生命周期阶段id，修改当前阶段的信息
     * @Method PUT
     * @urlpattern  {res_type}/{uuid}/lifecycle/steps/{uuid}
     * 
     * @param resType
     * @param resId
     * @param contribute
     */
    @RequestMapping(value = "/{uuid}/lifecycle/steps/{step_id}", method = RequestMethod.PUT, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody ResContributeViewModel modifyLifecycleStep(@PathVariable String res_type, @PathVariable String uuid,
             @PathVariable String step_id, @Valid @RequestBody ResContributeModel contributeModel, BindingResult validResult) {
        ValidResultHelper.valid(validResult, LifeCircleErrorMessageMapper.UpdateLifecycleFail.getCode(),
                "LifecycleControllerV06","modifyLifecycleStep");
        
        LifecycleServiceV06 service = !CommonServiceHelper.isQuestionDb(res_type) ? lifecycleService : lifecycleService4Qti;
        
        contributeModel.setIdentifier(step_id);
        ResContributeViewModel contributeViewModel = service.modifyLifecycleStep(res_type, uuid, contributeModel);
        return contributeViewModel;
    }
    
    /**
     * 批量资源修改生命周期阶段。 批量修改资源的阶段生命周期阶段信息
     * @Method PUT
     * @urlpattern  {res_type}/lifecycle/steps/bulk
     * 
     * @param resType
     * @param resIds
     * @param contributes
     */
    @RequestMapping(value = "/lifecycle/steps/bulk", method = RequestMethod.PUT, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody Map<String,ResContributeViewModel> modifyLifecycleStepBulk(@PathVariable String res_type,
            @Valid @RequestBody Map<String, ResContributeModel> contributeModels, BindingResult validResult) {
//        ValidResultHelper.valid(validResult, LifeCircleErrorMessageMapper.UpdateBatchLifecycleFail.getCode(),
//                "LifecycleControllerV06","modifyLifecycleStepBulk");
        ResourceBundleMessageSource  source = LifeCircleWebConfig.getResourceBundleMessageSource();
        
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<ResContributeModel>> allViolations = new HashSet<ConstraintViolation<ResContributeModel>>();
        for(String key:contributeModels.keySet()) {
            Set<ConstraintViolation<ResContributeModel>> violations = validator.validate(contributeModels.get(key));
            allViolations.addAll(violations);
        }
        if(!allViolations.isEmpty()) {
            StringBuilder errors=new StringBuilder();
            List<String> el = new ArrayList<String>();
            for(ConstraintViolation<ResContributeModel> violation:allViolations) {
                String msg = violation.getMessage();
                String em = source.getMessage(msg.substring(1, msg.length()-1), null, LocaleContextHolder.getLocale());
                if(!el.contains(em)){
                    errors.append(em+";");
                    el.add(em);
                }
            }
            throw new WafException(LifeCircleErrorMessageMapper.UpdateBatchLifecycleFail.getCode(),errors.toString());
        }
        
        LifecycleServiceV06 service = !CommonServiceHelper.isQuestionDb(res_type) ? lifecycleService : lifecycleService4Qti;
        
        Map<String,ResContributeViewModel> contributeViewModels = 
                service.modifyLifecycleStepBulk(res_type, Arrays.asList(contributeModels.keySet().toArray(new String[1])),
                        Arrays.asList(contributeModels.values().toArray(new ResContributeModel[1])));
        return contributeViewModels;
    }
    
    /**
     * 删除资源生命周期阶段。通过ID删除生命周期阶段信息
     * @Method DELETE
     * @urlpattern  {res_type}/{uuid}/lifecycle/steps
     * 
     * @param resType
     * @param resId
     * @param contribute
     */
    @RequestMapping(value = "/{uuid}/lifecycle/steps/{step_id}", method = RequestMethod.DELETE, produces = { MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody Map<String, String> delLifecycleStep(@PathVariable String res_type,
            @PathVariable String uuid, @PathVariable String step_id) {
        LifecycleServiceV06 service = !CommonServiceHelper.isQuestionDb(res_type) ? lifecycleService : lifecycleService4Qti;
        
        service.delLifecycleStep(res_type, uuid, step_id);
        return MessageConvertUtil
                .getMessageString(LifeCircleErrorMessageMapper.DeleteLifecycleSuccess);
    }
    
    /**
     * 批量删除资源生命周期阶段。 通过ID数组，批量删除生命周期阶段信息
     * @Method DELETE
     * @urlpattern  {res_type}/lifecycle/steps/bulk
     * 
     * @param resType
     * @param resId
     * @param contributes
     */
    @RequestMapping(value = "/{uuid}/lifecycle/steps/bulk", method = RequestMethod.DELETE, produces = { MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody Map<String, String> delLifecycleStepsBulk(@PathVariable String res_type,
            @PathVariable String uuid, @RequestParam Set<String> stepid) {
        LifecycleServiceV06 service = !CommonServiceHelper.isQuestionDb(res_type) ? lifecycleService : lifecycleService4Qti;
        
        service.delLifecycleStepsBulk(res_type, uuid, stepid);
        return MessageConvertUtil
                .getMessageString(LifeCircleErrorMessageMapper.DeleteLifecycleSuccess);
    }
}
