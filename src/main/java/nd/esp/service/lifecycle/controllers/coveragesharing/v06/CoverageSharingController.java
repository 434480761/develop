package nd.esp.service.lifecycle.controllers.coveragesharing.v06;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.validation.Valid;

import nd.esp.service.lifecycle.models.coveragesharing.v06.CoverageSharingModel;
import nd.esp.service.lifecycle.services.coveragesharing.v06.CoverageSharingService;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.busi.ValidResultHelper;
import nd.esp.service.lifecycle.utils.BeanMapperUtils;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.MessageConvertUtil;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.vos.ListViewModel;
import nd.esp.service.lifecycle.vos.coveragesharing.v06.CoverageSharingViewModel;
import nd.esp.service.lifecycle.vos.statics.CoverageConstant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nd.gaea.rest.security.authens.UserInfo;

/**
 * 库分享Controller
 * @author xiezy
 * @date 2016年8月24日
 */
@RestController
@RequestMapping(value={"/v0.6/resources/coverages/sharing"})
public class CoverageSharingController {
	
	@Autowired
	private CoverageSharingService coverageSharingService;
	
	/**
	 * 创建资源库之间的资源分享。 将A库的资源分享给B库，则在查询B库资源时会一并查询A库的资源。
	 * @author xiezy
	 * @date 2016年8月24日
	 * @param coverageSharingViewModel
	 * @param bindingResult
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
	public CoverageSharingViewModel createCoverageSharing(
			@Valid @RequestBody CoverageSharingViewModel coverageSharingViewModel,
			BindingResult validResult,
			@AuthenticationPrincipal UserInfo userInfo){
		//校验
		ValidResultHelper.valid(validResult, "LC/CREATE_COVERAGE_SHARING_PARAM_VALID_FAIL", "CoverageSharingController", "createCoverageSharing");
		
		//参数逻辑校验
		checkCoverage(coverageSharingViewModel.getSourceCoverage());
		checkCoverage(coverageSharingViewModel.getTargetCoverage());
		
		//判断是否已经分享过
		if(coverageSharingService.judgeSharingExistOrNot(
				coverageSharingViewModel.getSourceCoverage(), 
				coverageSharingViewModel.getTargetCoverage())){
			
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CoverageSharingExistFail);
		}
		
		//创建
		coverageSharingViewModel.setIdentifier(UUID.randomUUID().toString());
		CoverageSharingModel model = coverageSharingService.createCoverageSharing(
				BeanMapperUtils.beanMapper(coverageSharingViewModel, CoverageSharingModel.class), userInfo);
		
		return BeanMapperUtils.beanMapper(model, CoverageSharingViewModel.class);
	}
	
	/**
	 * 删除库分享
	 * @author xiezy
	 * @date 2016年8月24日
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/{sharing_uuid}", method = RequestMethod.DELETE)
	public Map<String,String> deleteCoverageSharing(@PathVariable(value="sharing_uuid") String id){
		
		coverageSharingService.deleteCoverageSharing(id);
		return MessageConvertUtil.getMessageString(LifeCircleErrorMessageMapper.DeleteCoverageSharingSuccess);
	}
	
	/**
	 * 获取库分享列表
	 * @author xiezy
	 * @date 2016年8月24日
	 * @param source
	 * @param target
	 * @param limit
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ListViewModel<CoverageSharingViewModel> getCoverageSharingList(
			 @RequestParam(required=false,value="source_coverage") String source,
	         @RequestParam(required=false,value="target_coverage") String target,
	         @RequestParam String limit){
		
		//校验
		if(StringUtils.isEmpty(source) && StringUtils.isEmpty(target)){
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CoverageSharingParamFail.getCode(),
                    "source_coverage和target_coverage二者至少传递一个");
		}
		
		//查询
		ListViewModel<CoverageSharingModel> csList = coverageSharingService.getCoverageSharingList(source, target, limit);
		
		//返回结果
		ListViewModel<CoverageSharingViewModel> viewListResult = new ListViewModel<CoverageSharingViewModel>();
		viewListResult.setLimit(csList.getLimit());
		viewListResult.setTotal(csList.getTotal());
		List<CoverageSharingModel> modelItems = csList.getItems();
		List<CoverageSharingViewModel> viewItems = new ArrayList<CoverageSharingViewModel>();
		if (CollectionUtils.isNotEmpty(modelItems)) {
			for (CoverageSharingModel model : modelItems) {
				CoverageSharingViewModel viewModel = BeanMapperUtils.beanMapper(model, CoverageSharingViewModel.class);
				viewItems.add(viewModel);
			}
		}
		viewListResult.setItems(viewItems);
		return viewListResult;
	}
	
	/**
	 * 校验覆盖范围
	 * @author xiezy
	 * @date 2016年8月24日
	 * @param coverage
	 */
	private void checkCoverage(String coverage){
		if(StringUtils.hasText(coverage)){
			List<String> elements = Arrays.asList(coverage.trim().split("/"));
			if(coverage.trim().endsWith("/") || elements.size() != 2){
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
	                    LifeCircleErrorMessageMapper.CoverageSharingParamFail.getCode(),
	                    coverage + "--格式错误,格式为:{target_type}/{target}");
			}
			
			if(!CoverageConstant.isCoverageTargetType(elements.get(0),false)){
	            
	            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
	                    LifeCircleErrorMessageMapper.CoverageTargetTypeNotExist);
	        }
		}else{
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CoverageSharingParamFail);
		}
	}
}
