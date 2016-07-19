package nd.esp.service.lifecycle.controllers.questions.v06;

import java.util.concurrent.ExecutorService;

import nd.esp.service.lifecycle.daos.questions.v06.QuestionDao;
import nd.esp.service.lifecycle.educommon.services.NDResourceService;
import nd.esp.service.lifecycle.entity.elasticsearch.Resource;
import nd.esp.service.lifecycle.models.v06.QuestionModel;
import nd.esp.service.lifecycle.services.elasticsearch.AsynEsResourceService;
import nd.esp.service.lifecycle.services.offlinemetadata.OfflineService;
import nd.esp.service.lifecycle.services.questions.v06.QuestionServiceV06;
import nd.esp.service.lifecycle.support.Constant;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.annotation.MarkAspect4Format2Category;
import nd.esp.service.lifecycle.support.annotation.MarkAspect4OfflineJsonToCS;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.support.busi.ValidResultHelper;
import nd.esp.service.lifecycle.support.busi.elasticsearch.ResourceTypeSupport;
import nd.esp.service.lifecycle.support.enums.OperationType;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;
import nd.esp.service.lifecycle.vos.questions.v06.QuestionViewModel;
import nd.esp.service.lifecycle.vos.valid.Valid4UpdateGroup;
import nd.esp.service.lifecycle.vos.valid.ValidGroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.nd.gaea.client.http.WafSecurityHttpClient;

/**
 * 习题V0.6API
 * @author ql
 *
 */
@RestController
@RequestMapping("/v0.6/questions")
public class QuestionControllerV06 {
	private static final Logger LOG = LoggerFactory.getLogger(QuestionControllerV06.class);
    @Autowired
    @Qualifier("questionServiceV06")
    private QuestionServiceV06 questionService;
    
    @Autowired
    private NDResourceService ndResourceService;
    
    @Autowired
    private QuestionDao questionDao;

    @Autowired
    private OfflineService offlineService;
    @Autowired
    private AsynEsResourceService esResourceOperation;
    
    private final static ExecutorService executorService = CommonHelper.getPrimaryExecutorService();
    private final static WafSecurityHttpClient WAF_SECURITY_HTTP_CLIENT = new WafSecurityHttpClient(Constant.WAF_CLIENT_RETRY_COUNT);
    
    
    
    
    /**
     * 创建习题对象
     * @param questionViewModel        习题对象
     * @return
     */
    @MarkAspect4Format2Category
    @MarkAspect4OfflineJsonToCS
    @RequestMapping(value = "/{id}", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public QuestionViewModel create(@Validated(ValidGroup.class) @RequestBody QuestionViewModel questionViewModel,BindingResult validResult,@PathVariable String id){
        //用来判断LifeCycle是否需要返回
        questionViewModel.setIdentifier(id);
        //入参合法性校验
        ValidResultHelper.valid(validResult, LifeCircleErrorMessageMapper.CreateQuestionFail.getCode(),
                "QuestionControllerV06","createQuestion");
        
        //业务校验
        CommonHelper.inputParamValid(questionViewModel,null,OperationType.CREATE);
        
        
        //model入参转换,部分数据初始化
        QuestionModel questionModel = CommonHelper.convertViewModelIn(questionViewModel, QuestionModel.class,ResourceNdCode.questions);
        //拓展属性可能为空
        if(null!=questionViewModel.getExtProperties()){
            questionModel.getExtProperties().setItemContent(questionViewModel.getExtProperties().getItemContent());
        }
        
        //创建习题
        questionModel = questionService.createQuestion(questionModel);
        
        //model转换
        questionViewModel = CommonHelper.convertViewModelOut(questionModel,QuestionViewModel.class);
        
        callSlidesAsync(id);
        
        return questionViewModel;
    }
    
    /**
     * 异步调用课件编辑器接口
     * @author linsm
     * @param uuid
     * @since 
     */
    private static void callSlidesAsync(final String uuid) {
        executorService.execute(new Runnable() {
            
            @Override
            public void run() {
                
                    callSlides(uuid);
               
            }

        });
        
    }
    /**
     *  调用课件编辑器接口
     * @author linsm
     * @param uuid
     * @since
     */
    private static void callSlides(String uuid) {
        String url = Constant.SLIDES_URI+"v1.3/questions/"+uuid+"/syncxml";
        LOG.debug("call slides url: {}",url);
        
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<String>(null, httpHeaders);
        try {
            WAF_SECURITY_HTTP_CLIENT.executeForObject(url, HttpMethod.PUT, requestEntity, String.class);
        } catch (Exception e) {
            LOG.error(e.getMessage()+" uuid: "+ uuid);
        }
    }

    /**
     * 修改习题对象
     * @param questionViewModel        习题对象
     * @return
     */
    @MarkAspect4Format2Category
    @MarkAspect4OfflineJsonToCS
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public QuestionViewModel update(@Validated(Valid4UpdateGroup.class) @RequestBody QuestionViewModel questionViewModel,BindingResult validResult,@PathVariable String id){

        questionViewModel.setIdentifier(id);
        //入参合法性校验
        ValidResultHelper.valid(validResult, LifeCircleErrorMessageMapper.UpdateQuestionFail.getCode(),
                "QuestionControllerV06","updateQuestion");
        
        //业务校验
        CommonHelper.inputParamValid(questionViewModel,null,OperationType.UPDATE);
        
        //model入参转换，部分数据初始化
        QuestionModel questionModel = CommonHelper.convertViewModelIn(questionViewModel, QuestionModel.class,ResourceNdCode.questions);
        
        //修改习题
        questionModel = questionService.updateQuestion(questionModel);
        
        //model出参转换
        questionViewModel = CommonHelper.convertViewModelOut(questionModel,QuestionViewModel.class);
        
        callSlidesAsync(id);
        
        return questionViewModel;
    }

    /**
     * 修改习题对象
     * @param questionViewModel        习题对象
     * @return
     */
    @MarkAspect4Format2Category
    @RequestMapping(value = "/{id}", method = RequestMethod.PATCH, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public QuestionViewModel patch(@Validated(Valid4UpdateGroup.class) @RequestBody QuestionViewModel questionViewModel,BindingResult validResult,@PathVariable String id,
                                   @RequestParam(value = "notice_file", required = false,defaultValue = "true") boolean notice){

        questionViewModel.setIdentifier(id);
        //入参合法性校验
//        ValidResultHelper.valid(validResult, LifeCircleErrorMessageMapper.UpdateQuestionFail.getCode(),
//                "QuestionControllerV06","updateQuestion");

        //业务校验
//        CommonHelper.inputParamValid(questionViewModel,null,OperationType.UPDATE);

        //model入参转换，部分数据初始化
        QuestionModel questionModel = CommonHelper.convertViewModelIn(questionViewModel, QuestionModel.class,ResourceNdCode.questions, true);

        //修改习题
        questionModel = questionService.patchQuestion(questionModel);

        //model出参转换
        questionViewModel = CommonHelper.convertViewModelOut(questionModel,QuestionViewModel.class);

        callSlidesAsync(id);

        if(notice) {
            offlineService.writeToCsAsync(ResourceNdCode.questions.toString(), id);
        }
        // offline metadata(coverage) to elasticsearch
        if (ResourceTypeSupport.isValidEsResourceType(ResourceNdCode.questions.toString())) {
            esResourceOperation.asynAdd(
                    new Resource(ResourceNdCode.questions.toString(), id));
        }

        return questionViewModel;
    }
    
//    @RequestMapping(value = "/batch_del_autoqti", method = RequestMethod.DELETE, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
//    public @ResponseBody Map<String,String> batchRemoveAutoqti(HttpServletRequest request, @RequestParam String chapterid){
//        String appFrom="";
//        try{
//            appFrom= request.getHeader("app_from");
//        }catch (Exception e){
//            LOG.warn("获取referer失败:",e.getCause());
//        }
//        
//        if(!appFrom.equals("intelli-questions")) {
//            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
//                    "LC/BATCH_DEL_AUTOQTI_UNAUTHORIZED",
//                    "未授权下架智能出题题目");
//        }
//        
//        questionDao.delBatchAutoQuestions(chapterid);
//        
//        return MessageConvertUtil.getMessageString(LifeCircleErrorMessageMapper.DeleteQuestionSuccess);
//    }
}

