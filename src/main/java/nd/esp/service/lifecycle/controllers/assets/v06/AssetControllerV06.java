package nd.esp.service.lifecycle.controllers.assets.v06;

import java.util.List;
import java.util.Map;
import java.util.Set;

import nd.esp.service.lifecycle.educommon.vos.ResClassificationViewModel;
import nd.esp.service.lifecycle.educommon.vos.ResTechInfoViewModel;
import nd.esp.service.lifecycle.entity.elasticsearch.Resource;
import nd.esp.service.lifecycle.models.v06.AssetModel;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.services.assets.v06.AssetServiceV06;
import nd.esp.service.lifecycle.services.elasticsearch.AsynEsResourceService;
import nd.esp.service.lifecycle.services.offlinemetadata.OfflineService;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.annotation.MarkAspect4Format2Category;
import nd.esp.service.lifecycle.support.annotation.MarkAspect4OfflineJsonToCS;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.support.busi.TransCodeUtil;
import nd.esp.service.lifecycle.support.busi.ValidResultHelper;
import nd.esp.service.lifecycle.support.busi.elasticsearch.ResourceTypeSupport;
import nd.esp.service.lifecycle.support.busi.transcode.TransCodeManager;
import nd.esp.service.lifecycle.support.enums.OperationType;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.vos.assets.v06.AssetViewModel;
import nd.esp.service.lifecycle.vos.valid.Valid4UpdateGroup;
import nd.esp.service.lifecycle.vos.valid.ValidGroup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 素材V0.6API
 * @author xuzy
 *
 */
@RestController
@RequestMapping("/v0.6/assets")
public class AssetControllerV06 {
	@Autowired
	@Qualifier("assetServiceV06")
	private AssetServiceV06 assetService;
	
	@Autowired
	private TransCodeUtil transCodeUtil;

	@Autowired
	private OfflineService offlineService;
	@Autowired
	private AsynEsResourceService esResourceOperation;
	
	/**
	 * 创建素材对象
	 * @param rm		素材对象
	 * @return
	 */
	@MarkAspect4Format2Category
	@MarkAspect4OfflineJsonToCS
	@RequestMapping(value = "/{id}", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
	public AssetViewModel create(@Validated(ValidGroup.class) @RequestBody AssetViewModel avm,BindingResult validResult,@PathVariable String id){
		//入参合法性校验
		ValidResultHelper.valid(validResult, "LC/CREATE_ASSET_PARAM_VALID_FAIL", "AssetControllerV06", "create");
		avm.setIdentifier(id);
		
		//业务校验
		CommonHelper.inputParamValid(avm,"10111",OperationType.CREATE);
		
		//课件模板需要转码tech_info单独作校验
		Map<String,? extends ResTechInfoViewModel> techInfoMap = avm.getTechInfo();
		if(!checkTechInfoData(avm)){
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.ChecTechInfoHrefOrSourceFail);
		}
		
		//tech_info属性特殊处理
		CommonHelper.copyTechInfoValue(techInfoMap);

		//model入参转换,部分数据初始化
		AssetModel am = CommonHelper.convertViewModelIn(avm, AssetModel.class,ResourceNdCode.assets);
		boolean bTranscode = TransCodeManager.canTransCode(avm, IndexSourceType.AssetType.getName());
		if(bTranscode || transCodeUtil.isImageTransCode(am, IndexSourceType.AssetType.getName())) {
		    am.getLifeCycle().setStatus(TransCodeUtil.getTransIngStatus(true));
		}
		
		//创建素材
		am = assetService.createAsset(am);

		//model转换
		avm = CommonHelper.convertViewModelOut(am,AssetViewModel.class,"assets_type");
		
		if (bTranscode || transCodeUtil.isImageTransCode(am, IndexSourceType.AssetType.getName())) {
            transCodeUtil.triggerTransCode(am, IndexSourceType.AssetType.getName());
        }
		return avm;
	}
	
	/**
	 * 修改素材对象
	 * @param rm		素材对象
	 * @return
	 */
	@MarkAspect4Format2Category
	@MarkAspect4OfflineJsonToCS
	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
	public AssetViewModel update(@Validated(Valid4UpdateGroup.class) @RequestBody AssetViewModel avm,BindingResult validResult,@PathVariable String id){
		//入参合法性校验
		ValidResultHelper.valid(validResult, "LC/UPDATE_ASSET_PARAM_VALID_FAIL", "AssetControllerV06", "update");
		avm.setIdentifier(id);
		
		//业务校验
		CommonHelper.inputParamValid(avm,"10111",OperationType.UPDATE);
		
		//课件模板需要转码tech_info单独作校验
		Map<String,? extends ResTechInfoViewModel> techInfoMap = avm.getTechInfo();
		if(!checkTechInfoData(avm)){
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.ChecTechInfoHrefOrSourceFail);
		}
		
		//tech_info属性特殊处理
		CommonHelper.copyTechInfoValue(techInfoMap);
		
		//model入参转换，部分数据初始化
		AssetModel am = CommonHelper.convertViewModelIn(avm, AssetModel.class,ResourceNdCode.assets);

		//修改素材
		am = assetService.updateAsset(am);
		
		//model转换
		avm = CommonHelper.convertViewModelOut(am,AssetViewModel.class,"assets_type");
		return avm;
	}

	/**
	 * 修改素材对象
	 * @param avm		素材对象
	 * @return
	 */
	@MarkAspect4Format2Category
	@RequestMapping(value = "/{id}", method = RequestMethod.PATCH, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
	public AssetViewModel patch(@Validated(Valid4UpdateGroup.class) @RequestBody AssetViewModel avm,BindingResult validResult,@PathVariable String id,
								@RequestParam(value = "notice_file", required = false,defaultValue = "true") boolean notice){
		//入参合法性校验
//		ValidResultHelper.valid(validResult, "LC/UPDATE_ASSET_PARAM_VALID_FAIL", "AssetControllerV06", "update");
		avm.setIdentifier(id);

		//业务校验
//		CommonHelper.inputParamValid(avm,"10111",OperationType.UPDATE);

		//课件模板需要转码tech_info单独作校验
//		Map<String,? extends ResTechInfoViewModel> techInfoMap = avm.getTechInfo();
//		if(!checkTechInfoData(avm)){
//			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.ChecTechInfoHrefOrSourceFail);
//		}

		//tech_info属性特殊处理
//		CommonHelper.copyTechInfoValue(techInfoMap);

		//model入参转换，部分数据初始化
		AssetModel am = CommonHelper.convertViewModelIn(avm, AssetModel.class,ResourceNdCode.assets,true);

		//修改素材
		am = assetService.patchAsset(am);

		//model转换
		avm = CommonHelper.convertViewModelOut(am, AssetViewModel.class, "assets_type");

		if(notice) {
			offlineService.writeToCsAsync(ResourceNdCode.assets.toString(), id);
		}
		// offline metadata(coverage) to elasticsearch
		if (ResourceTypeSupport.isValidEsResourceType(ResourceNdCode.assets.toString())) {
			esResourceOperation.asynAdd(
					new Resource(ResourceNdCode.assets.toString(), id));
		}
		return avm;
	}
	
	/**
	 * 校验tech_info数据合法性
	 * 1、素材的维度数据如果包含$RA06|07开头的不需要tech_info
	 * 2、教学目标的集合组合成的套件，维度数据中包含$RA0502、$RA0503不需要校验tech_info
	 * 3、其它素材必须包含href或source属性
	 * 
	 * @author:xuzy
	 * @date:2016年1月7日
	 * @param avm
	 * @return
	 */
	private boolean checkTechInfoData(AssetViewModel avm){
		Map<String, List<? extends ResClassificationViewModel>> categoryMap = avm.getCategories();
		if(CollectionUtils.isNotEmpty(categoryMap)){
			Set<String> keys = categoryMap.keySet();
			for (String key : keys) {
				List<? extends ResClassificationViewModel> categories = categoryMap.get(key);
				if(CollectionUtils.isNotEmpty(categories)){
					for (ResClassificationViewModel c : categories) {
						String taxoncode = c.getTaxoncode();
						if (taxoncode != null
								&& (taxoncode.startsWith("$RA06")
										|| taxoncode.startsWith("$RA07")
										|| taxoncode.equals("$RA0502") 
										|| taxoncode.equals("$RA0503"))) {
							return true;
						}
					}
				}
			}
		}

		Map<String, ? extends ResTechInfoViewModel> techInfoMap = avm.getTechInfo();
		if(techInfoMap != null && (techInfoMap.containsKey("href") || techInfoMap.containsKey("source"))){
			return true;
		}
		
		return false;
	}
}
