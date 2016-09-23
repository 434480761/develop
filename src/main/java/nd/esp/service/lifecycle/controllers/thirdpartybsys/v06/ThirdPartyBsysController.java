package nd.esp.service.lifecycle.controllers.thirdpartybsys.v06;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import nd.esp.service.lifecycle.models.ApiModel;
import nd.esp.service.lifecycle.models.ThirdPartyBsysModle;
import nd.esp.service.lifecycle.models.ivc.v06.IvcConfigModel;
import nd.esp.service.lifecycle.services.thirdpartybuss.v06.ThirdPartyBussService;
import nd.esp.service.lifecycle.support.Constant;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.support.busi.ValidResultHelper;
import nd.esp.service.lifecycle.support.thirdpartybsys.ThirdpartybsysConstant;
import nd.esp.service.lifecycle.utils.MessageConvertUtil;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;
import nd.esp.service.lifecycle.vos.ListViewModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v0.6")
public class ThirdPartyBsysController {
	
    @Autowired
    private ThirdPartyBussService bsysService;

    /**
     * 注册第三方服务。
     * @Method POST
     * @urlpattern  /v0.6/3dbsys/servicekey/registry
     * 
     * @param serviceModel
     */
    @RequestMapping(value = "/3dbsys/servicekey/registry", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody ThirdPartyBsysModle registerService(@Valid @RequestBody ThirdPartyBsysModle serviceModel, BindingResult validResult) {
        //入参合法性校验
        ValidResultHelper.valid(validResult, LifeCircleErrorMessageMapper.CreateBsysFail.getCode(),
                "ThirdPartyBsysController","registerService");
        
        String json = ObjectUtils.toJson(serviceModel.getBsysivcconfig());
        IvcConfigModel model = CommonHelper.convertJson2IvcConfig(json);
        if(model==null || model.getGlobalLoad()==null) {
        	throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR, "LC/IVC_CONFIG_JSON_ERROR", "业务系统访问权限格式配置错误。 ");
        }
        
        ThirdPartyBsysModle rtModel = bsysService.registerService(serviceModel, false);
        return rtModel;
    }

    /**
     * 查询第三方服务。
     * @Method GET
     * @urlpattern  /v0.6/3dbsys/servicekey?bsysname=备课&admin=johnny&limit=(0,20)
     * 
     * @param bsysname
     * @param admin
     * @param limit
     */
    @RequestMapping(value = "/3dbsys/servicekey", params = { "limit" }, method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody ListViewModel<ThirdPartyBsysModle> queryServiceInfo(@RequestParam(required=false) String bsysname, 
    		@RequestParam(required=false) String admin, @RequestParam String limit) {
        ListViewModel<ThirdPartyBsysModle> listServiceModel = bsysService.queryServiceInfo(bsysname, admin, limit);
        return listServiceModel;
    }

    /**
     * 修改第三方服务。通过ID修改第三方服务
     * @Method PUT
     * @urlpattern  /v0.6/3dbsys/servicekey/{uuid}
     * 
     * @param uuid
     * @param serviceModel
     */
    @RequestMapping(value = "/3dbsys/servicekey/{uuid}", method = RequestMethod.PUT, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody ThirdPartyBsysModle modifyLifecycleStep(@PathVariable String uuid, 
    		@Valid @RequestBody ThirdPartyBsysModle serviceModel, BindingResult validResult) {
    	ValidResultHelper.valid(validResult, LifeCircleErrorMessageMapper.UpdateBsysFail.getCode(),
                "ThirdPartyBsysController","registerService");
    	String json = ObjectUtils.toJson(serviceModel.getBsysivcconfig());
        IvcConfigModel model = CommonHelper.convertJson2IvcConfig(json);
        if(model==null || model.getGlobalLoad()==null) {
        	throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR, "LC/IVC_CONFIG_JSON_ERROR", "业务系统访问权限格式配置错误。 ");
        }
        
    	serviceModel.setIdentifier(uuid);
    	ThirdPartyBsysModle rtModel = bsysService.modifyService(uuid, serviceModel);
        return rtModel;
    }
    
    /**
     * 删除第三方服务。通过ID删除第三方服务
     * @Method DELETE
     * @urlpattern  /v0.6/3dbsys/servicekey/{uuid}
     * 
     * @param uuid
     */
    @RequestMapping(value = "/3dbsys/servicekey/{uuid}", method = RequestMethod.DELETE, produces = { MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody Map<String, String> delLifecycleStep(@PathVariable String uuid) {
    	bsysService.deleteService(uuid);
        return MessageConvertUtil
                .getMessageString(LifeCircleErrorMessageMapper.DeleteBsysSuccess);
    }
    
    @RequestMapping(value = "/api/list", params = {}, method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody ListViewModel<ApiModel> queryApiList() {
    	ListViewModel<ApiModel> list =bsysService.queryApiList();
		return list;
    }
    
    /**
     * 注册第三方服务。 -- 专门提供给E-Learning使用
     * @author xiezy
     * @date 2016年9月23日
     * @param serviceModel
     * @param validResult
     * @param request
     * @return
     */
    @SuppressWarnings("unchecked")
	@RequestMapping(value = "/3dbsys/servicekey/auto_registry", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody ThirdPartyBsysModle autoRegisterService(
    		@Valid @RequestBody ThirdPartyBsysModle serviceModel, BindingResult validResult,
    		HttpServletRequest request) {
    	
    	//获取请求头中的bsyskey
    	String bsyskey = request.getHeader(Constant.BSYSKEY);
    	if(!StringUtils.hasText(bsyskey) || !bsyskey.equals(Constant.BSYSKEY_ELEARNING)){
    		throw new LifeCircleException(HttpStatus.FORBIDDEN, LifeCircleErrorMessageMapper.CreateBsysFail.getCode(),
    				"仅E-Learning有权限申请,请在请求头中带上对应的bsyskey");
    	}
    	
    	//入参合法性校验
        ValidResultHelper.valid(validResult, LifeCircleErrorMessageMapper.CreateBsysFail.getCode(),
                "ThirdPartyBsysController","registerService");
        
        //将权限设置为默认权限
        serviceModel.setBsysivcconfig(ObjectUtils.fromJson(ThirdpartybsysConstant.DEFAULT_BSYSIVCCONFIG, Map.class));
        String json = ObjectUtils.toJson(serviceModel.getBsysivcconfig());
        IvcConfigModel model = CommonHelper.convertJson2IvcConfig(json);
        if(model==null || model.getGlobalLoad()==null) {
        	throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR, "LC/IVC_CONFIG_JSON_ERROR", "业务系统访问权限格式配置错误。 ");
        }
        
        ThirdPartyBsysModle rtModel = bsysService.registerService(serviceModel, true);
        rtModel.setBsysivcconfig(null);
        return rtModel;
    }
}
