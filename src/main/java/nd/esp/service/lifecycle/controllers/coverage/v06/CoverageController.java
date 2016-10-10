package nd.esp.service.lifecycle.controllers.coverage.v06;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.validation.Valid;

import nd.esp.service.lifecycle.educommon.services.impl.CommonServiceHelper;
import nd.esp.service.lifecycle.entity.elasticsearch.Resource;
import nd.esp.service.lifecycle.models.coverage.v06.CoverageModel;
import nd.esp.service.lifecycle.models.coverage.v06.CoverageModelForUpdate;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.services.coverages.v06.CoverageService;
import nd.esp.service.lifecycle.services.elasticsearch.AsynEsResourceService;
import nd.esp.service.lifecycle.services.notify.NotifyInstructionalobjectivesService;
import nd.esp.service.lifecycle.services.titan.TitanCommonService;
import nd.esp.service.lifecycle.services.titan.TitanCommonServiceImpl;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.support.busi.ValidResultHelper;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.MessageConvertUtil;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.vos.coverage.v06.CoverageViewModel;
import nd.esp.service.lifecycle.vos.statics.CoverageConstant;
import nd.esp.service.lifecycle.vos.statics.ResourceType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

/**
 * 公私有资源库 Controller
 * <p>Create Time: 2015年6月17日           </p>
 * @author xiezy
 */
@RestController
@RequestMapping(value={"/v0.6/{res_type}"})
public class CoverageController {
    private static final Logger LOG = LoggerFactory.getLogger(CoverageController.class);
    
    @Autowired
    @Qualifier("coverageServiceImpl")
    private CoverageService coverageService;
	
	@Autowired
    @Qualifier(value="coverageService4QuestionDBImpl")
    private CoverageService coverageService4QuestonDB;
	
	@Autowired
    private NotifyInstructionalobjectivesService notifyService;
    
    @Autowired
    private AsynEsResourceService esResourceOperation;
    
    /**
     * 增加资源覆盖范围	
     * <p>Create Time: 2015年6月18日   </p>
     * <p>Create author: xiezy   </p>
     * @param resType           源资源类型
     * @param coverageModel     创建时的入参
     * @param bindingResult     入参校验的绑定结果
     * @return
     */
    @RequestMapping(value = "/coverages/target", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public CoverageViewModel createCoverage(@PathVariable(value="res_type") String resType,
            @Valid @RequestBody CoverageModel coverageModel,BindingResult bindingResult){
        //校验入参
        ValidResultHelper.valid(bindingResult);
        //判断源资源是否存在,不存在将抛出not found的异常
        CommonHelper.resourceExist(resType, coverageModel.getResource(),ResourceType.RESOURCE_SOURCE);
        
        //add by xiezy - 2016.04.15
        Map<String,Boolean> notifyMap = new HashMap<String, Boolean>();
        if(resType.equals(IndexSourceType.InstructionalObjectiveType.getName())){
        	notifyMap.put(coverageModel.getResource(), 
            		notifyService.resourceBelongToNDLibrary(coverageModel.getResource()));
        }
        
        //判断覆盖范围类型是否在可选范围内
        if(!CoverageConstant.isCoverageTargetType(coverageModel.getTargetType(),false)){
            
            LOG.error("覆盖范围类型不在可选范围内");
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CoverageTargetTypeNotExist);
        }
        //判断资源操作类型是否在可选范围内
        if(!CoverageConstant.isCoverageStrategy(coverageModel.getStrategy(),false)){
            
            LOG.error("资源操作类型不在可选范围内");
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CoverageStrategyNotExist);
        }
        //对入参进行补全
        coverageModel.setResType(resType);
        coverageModel.setIdentifier(UUID.randomUUID().toString());
        //判断覆盖范围是否已经创建,若已创建返回"已存在"异常
        if(getCoverageService(resType).getCoverageByCondition(
                coverageModel.getTargetType(),coverageModel.getTarget(),
                coverageModel.getStrategy(),coverageModel.getResource()) != null){//已存在
            
            LOG.error("资源覆盖范围已存在");
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CoverageAleadyExist);
        }
        
        //add by lsm offline to elasticsearch
        Resource resource = new Resource(coverageModel.getResType(), coverageModel.getResource());
        
        //调用service,添加覆盖范围
        CoverageViewModel coverageViewModel = getCoverageService(resType).createCoverage(coverageModel);
        
        //add by lsm offline to elasticsearch
        esResourceOperation.asynAdd(resource);
        
        //add by xiezy - 2016.04.15
        //异步通知智能出题
        notifyService.asynNotify4Coverage(notifyMap);
        
        return coverageViewModel;
    }
    
    /**
     * 批量增加资源覆盖范围
     * <p>Create Time: 2015年6月18日   </p>
     * <p>Create author: xiezy   </p>
     * @param resType               源资源类型
     * @param coverageModels        创建时的入参集合
     * @param bindingResult
     * @return
     */
    @RequestMapping(value = "/coverages/bulk", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public List<CoverageViewModel> batchCreateCoverage(@PathVariable(value="res_type") String resType,
            @Valid @RequestBody List<CoverageModel> coverageModels,BindingResult bindingResult){
        //返回结果集
        List<CoverageViewModel> resultList = new ArrayList<CoverageViewModel>();
        //存放已经存在的覆盖范围的list
        List<CoverageViewModel> existList = new ArrayList<CoverageViewModel>();
        //存放需要新增的覆盖范围的list
        List<CoverageModel> notExistList = new ArrayList<CoverageModel>();
        
        //参数校验,保证批量传入的覆盖范围,一个资源的覆盖范围，有且仅有一个OWNNER的覆盖范围策略
        CommonHelper.checkCoverageHaveOnlyOneOwner(coverageModels, true);
        
        /**
         * 入参校验 + 逻辑校验
         */
        for(CoverageModel coverageModel : coverageModels){
            //手动校验入参字段
            String message = this.paramCheckForBatch(coverageModel);
            if(!StringUtils.isEmpty(message)){//数据有误
                
                LOG.error("入参数据错误:" + message);
                
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "LC/CREATE_COVERAGE_INPUT_ERROR",message);
            }
            //判断源资源是否存在,不存在将抛出not found的异常
            CommonHelper.resourceExist(resType, coverageModel.getResource(),ResourceType.RESOURCE_SOURCE);
            //判断覆盖范围类型是否在可选范围内
            if(!CoverageConstant.isCoverageTargetType(coverageModel.getTargetType(),false)){
                
                LOG.error("覆盖范围类型不在可选范围内");
                
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        LifeCircleErrorMessageMapper.CoverageTargetTypeNotExist);
            }
            //判断资源操作类型是否在可选范围内
            if(!CoverageConstant.isCoverageStrategy(coverageModel.getStrategy(),false)){
                
                LOG.error("资源操作类型不在可选范围内");
                
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        LifeCircleErrorMessageMapper.CoverageStrategyNotExist);
            }
            //对入参进行补全
            coverageModel.setResType(resType);
            coverageModel.setIdentifier(UUID.randomUUID().toString());
            //判断覆盖范围是否已经创建,若已创建返回已创建的覆盖范围,加入到existList中
            CoverageViewModel existCoverage = getCoverageService(resType).getCoverageByCondition(coverageModel.getTargetType(),coverageModel.getTarget(),
                        coverageModel.getStrategy(),coverageModel.getResource());
                    
            if(existCoverage != null){//已存在
                existList.add(existCoverage);
            }else{//不存在
                notExistList.add(coverageModel);
            }
        }
        
        if(!CollectionUtils.isEmpty(notExistList)){
        	//add by xiezy - 2016.04.15
            Map<String,Boolean> notifyMap = new HashMap<String, Boolean>();
            if(resType.equals(IndexSourceType.InstructionalObjectiveType.getName())){
            	for(CoverageModel coverageModel : notExistList){
            		if(!notifyMap.containsKey(coverageModel.getResource())){
            			notifyMap.put(coverageModel.getResource(), 
                        		notifyService.resourceBelongToNDLibrary(coverageModel.getResource()));
            		}
            	}
            }
        	
			// add by lsm offline to elasticsearch
        	Set<Resource> resources = new HashSet<Resource>();
        	for(CoverageModel coverageModel:notExistList){
        		resources.add(new Resource(coverageModel.getResType(), coverageModel.getResource()));
        	}
        	
            //调用service,批量增加覆盖范围
        	resultList = getCoverageService(resType).batchCreateCoverage(notExistList,false);
            
			// add by lsm offline to elasticsearch
            esResourceOperation.asynBatchAdd(resources);
            
            //add by xiezy - 2016.04.15
            //异步通知智能出题
            notifyService.asynNotify4Coverage(notifyMap);
        }
        
        //将已存在的加入到resultList中
        resultList.addAll(existList);
        
        return resultList;
    }
    
    /**
     * 获取资源覆盖范围	
     * <p>Create Time: 2015年6月18日   </p>
     * <p>Create author: xiezy   </p>
     * @param rcid      覆盖范围的id
     * @return
     */
    @RequestMapping(value = "/coverages/{rcid}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public CoverageViewModel getCoverageDetail(@PathVariable(value="rcid") String rcid,@PathVariable(value="res_type") String resType){
        CoverageViewModel coverageViewModel = getCoverageService(resType).getCoverageDetail(rcid);
        if(coverageViewModel == null){
            return null;
        }
        
        return coverageViewModel;
    }
    
    /**
     * 批量获取资源覆盖范围 
     * <p>Create Time: 2015年6月18日   </p>
     * <p>Create author: xiezy   </p>
     * @param rcids      覆盖范围的id集合
     * @return
     */
    @RequestMapping(value = "/coverages/list", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public Map<String,CoverageViewModel> batchGetCoverageDetail(@RequestParam(value="rcid") List<String> rcids,@PathVariable(value="res_type") String resType){
        return getCoverageService(resType).batchGetCoverageDetail(rcids);
    }
    
    /**
     * 修改资源覆盖范围	
     * <p>Create Time: 2015年6月18日   </p>
     * <p>Create author: xiezy   </p>
     * @param rcid                      覆盖范围的id
     * @param coverageModelForUpdate    修改时的入参
     * @param bindingResult             入参校验的绑定结果    
     * @return
     */
    @RequestMapping(value = "/coverages/{rcid}", method = RequestMethod.PUT, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public CoverageViewModel updateCoverage(@PathVariable(value="rcid") String rcid,@PathVariable(value="res_type") String resType,
            @Valid @RequestBody CoverageModelForUpdate coverageModelForUpdate,BindingResult bindingResult){
        //校验入参
        ValidResultHelper.valid(bindingResult);
        //判断rcid对应的覆盖范围是否存在,若不存在抛出异常
        CoverageViewModel viewModel4Detail = this.getCoverageDetail(rcid,resType);
        if(viewModel4Detail == null){
            
            LOG.error("资源覆盖范围不存在");
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CoverageNotExist);
        }
        
        //add by xiezy - 2016.04.15
        Map<String,Boolean> notifyMap = new HashMap<String, Boolean>();
        if(resType.equals(IndexSourceType.InstructionalObjectiveType.getName())){
        	notifyMap.put(viewModel4Detail.getResource(), 
            		notifyService.resourceBelongToNDLibrary(viewModel4Detail.getResource()));
        }
        
        //判断覆盖范围类型是否在可选范围内
        if(!CoverageConstant.isCoverageTargetType(coverageModelForUpdate.getTargetType(),false)){
            
            LOG.error("覆盖范围类型不在可选范围内");
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CoverageTargetTypeNotExist);
        }
        //判断资源操作类型是否在可选范围内
        if(!CoverageConstant.isCoverageStrategy(coverageModelForUpdate.getStrategy(),false)){
            
            LOG.error("资源操作类型不在可选范围内");
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CoverageStrategyNotExist);
        }
        //对入参进行补全
        coverageModelForUpdate.setIdentifier(rcid);
        coverageModelForUpdate.setResource(viewModel4Detail.getResource());
        coverageModelForUpdate.setResType(viewModel4Detail.getResType());
        
        //判断是否修改为已经创建的资源覆盖范围,若已创建返回"已存在"异常
        CoverageViewModel isExistOrNot = getCoverageService(resType).getCoverageByCondition(
                coverageModelForUpdate.getTargetType(),coverageModelForUpdate.getTarget(),
                coverageModelForUpdate.getStrategy(),coverageModelForUpdate.getResource());
        if(isExistOrNot != null && !isExistOrNot.getIdentifier().equals(rcid)){//已存在
            
            LOG.error("不能修改为已存在的资源覆盖范围");
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CanNotUpdateCoverageAleadyExist);
        }
        
		// add by lsm offline to elasticsearch
		Resource resource = new Resource(coverageModelForUpdate.getResType(),
				coverageModelForUpdate.getResource());

		// 调用service,修改覆盖范围
        CoverageViewModel coverageViewModel = getCoverageService(resType).updateCoverage(coverageModelForUpdate);

		// add by lsm offline to elasticsearch
		esResourceOperation.asynAdd(resource);
		
		//add by xiezy - 2016.04.15
        //异步通知智能出题
        notifyService.asynNotify4Coverage(notifyMap);
        
        return coverageViewModel;
    }
    
    /**
     * 批量修改资源覆盖范围	
     * <p>Create Time: 2015年6月18日   </p>
     * <p>Create author: xiezy   </p>
     * @param coverageModelForUpdates       修改时的入参集合
     * @param bindingResult
     * @return
     */
    @RequestMapping(value = "/coverages/bulk", method = RequestMethod.PUT, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public Map<String,CoverageViewModel> batchUpdateCoverage(@PathVariable(value="res_type") String resType,@Valid @RequestBody Map<String,CoverageModelForUpdate> coverageModelForUpdates,BindingResult bindingResult){
        //返回的结果集
        Map<String,CoverageViewModel> resultMap = new HashMap<String, CoverageViewModel>();
        
        //参数校验,保证批量传入的覆盖范围,一个资源的覆盖范围，有且仅有一个OWNNER的覆盖范围策略   //FIXME 这里存在一个bug,因为resource都为null
        CommonHelper.checkCoverageHaveOnlyOneOwner(Arrays.asList(coverageModelForUpdates.values().toArray()), false);
        
        //add by xiezy - 2016.04.15
        Map<String,Boolean> notifyMap = new HashMap<String, Boolean>();
        
		// add by lsm offline to elasticsearch
		Set<Resource> resources = new HashSet<Resource>();
        
        for(String rcid : coverageModelForUpdates.keySet()){
            //判断rcid对应的覆盖范围是否存在,若不存在不做处理
            CoverageViewModel viewModel4Detail = this.getCoverageDetail(rcid,resType);
            if(viewModel4Detail == null){
                continue;
            }
            
            CoverageModelForUpdate coverageModelForUpdate = coverageModelForUpdates.get(rcid);
            
            //判断覆盖范围类型是否在可选范围内
            if(!CoverageConstant.isCoverageTargetType(coverageModelForUpdate.getTargetType(),false)){
                
                LOG.error("覆盖范围类型不在可选范围内");
                
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        LifeCircleErrorMessageMapper.CoverageTargetTypeNotExist);
            }
            //判断资源操作类型是否在可选范围内
            if(!CoverageConstant.isCoverageStrategy(coverageModelForUpdate.getStrategy(),false)){
                
                LOG.error("资源操作类型不在可选范围内");
                
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        LifeCircleErrorMessageMapper.CoverageStrategyNotExist);
            }
            //对入参进行补全
            coverageModelForUpdate.setIdentifier(rcid);
            coverageModelForUpdate.setResource(viewModel4Detail.getResource());
            coverageModelForUpdate.setResType(viewModel4Detail.getResType());
            
            //判断是否修改为已经创建的资源覆盖范围,若已创建返回"已存在"的资源覆盖范围,否则进行修改
            CoverageViewModel existCoverage = 
                    getCoverageService(resType).getCoverageByCondition(
                    coverageModelForUpdate.getTargetType(),coverageModelForUpdate.getTarget(),
                    coverageModelForUpdate.getStrategy(),coverageModelForUpdate.getResource());
            if(existCoverage != null && !existCoverage.getIdentifier().equals(rcid)){//已存在
                resultMap.put(rcid, existCoverage);//FIXME 这里的rcid是否该换成existCoverage.getIdentifier()
            }else{//不存在
            	//add by xiezy - 2016.04.15
            	if(resType.equals(IndexSourceType.InstructionalObjectiveType.getName())){
            		notifyMap.put(coverageModelForUpdate.getResource(), 
                    		notifyService.resourceBelongToNDLibrary(coverageModelForUpdate.getResource()));
            	}
            	
				// add by lsm offline to elasticsearch
				resources.add(new Resource(coverageModelForUpdate.getResType(),
						coverageModelForUpdate.getResource()));

            	//调用service,修改覆盖范围
                CoverageViewModel coverageAfterUpdate = getCoverageService(resType).updateCoverage(coverageModelForUpdate);
                resultMap.put(rcid, coverageAfterUpdate);
            }
        }
        
		// add by lsm offline to elasticsearch
        esResourceOperation.asynBatchAdd(resources);
        
        //add by xiezy - 2016.04.15
        //异步通知智能出题
        notifyService.asynNotify4Coverage(notifyMap);
        
        return resultMap;
    }
    
    /**
     * 删除资源覆盖范围	
     * <p>Create Time: 2015年6月18日   </p>
     * <p>Create author: xiezy   </p>
     * @param rcid      覆盖范围的id
     * @return
     */
    @RequestMapping(value = "/coverages/{rcid}", method = RequestMethod.DELETE)
    public @ResponseBody Map<String,String> deleteCoverage(@PathVariable(value="res_type") String resType,@PathVariable(value="rcid") String rcid){
        //判断rcid对应的覆盖范围是否存在,若不存在返回异常
    	CoverageViewModel coverageViewModel = this.getCoverageDetail(rcid,resType);
        if(coverageViewModel == null){
            
            LOG.error("资源覆盖范围不存在");
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CoverageNotExist);
        }
        
        //add by xiezy - 2016.04.15
        Map<String,Boolean> notifyMap = new HashMap<String, Boolean>();
        if(resType.equals(IndexSourceType.InstructionalObjectiveType.getName())){
        	notifyMap.put(coverageViewModel.getResource(), 
            		notifyService.resourceBelongToNDLibrary(coverageViewModel.getResource()));
        }
        
		// add by lsm offline to elasticsearch
		Resource resource = new Resource(coverageViewModel.getResType(),
				coverageViewModel.getResource());

        //调用service,删除资源覆盖范围
        boolean flag = getCoverageService(resType).deleteCoverage(rcid);
        if (!flag) {//FIXME flag不可能为false,这段代码可以考虑去掉,下同
            return MessageConvertUtil
                    .getMessageString(LifeCircleErrorMessageMapper.DeleteCoverageFail);
        }

		// add by lsm offline to elasticsearch
        esResourceOperation.asynAdd(resource);
        
        //add by xiezy - 2016.04.15
        //异步通知智能出题
        notifyService.asynNotify4Coverage(notifyMap);
        
        return MessageConvertUtil
                .getMessageString(LifeCircleErrorMessageMapper.DeleteCoverageSuccess);
    }
    
    /**
     * 批量删除覆盖范围	
     * <p>Create Time: 2015年6月18日   </p>
     * <p>Create author: xiezy   </p>
     * @param rcids     覆盖范围id的集合
     * @return
     */
    @RequestMapping(value = "/coverages/bulk", method = RequestMethod.DELETE)
    public @ResponseBody Map<String,String> batchDeleteCoverage(@PathVariable(value="res_type") String resType,@RequestParam(value="rcid") List<String> rcids){
        List<String> deleteIds = new ArrayList<String>();
        
        //add by xiezy - 2016.04.15
        Map<String,Boolean> notifyMap = new HashMap<String, Boolean>();
        
		// add by lsm offline to elasticsearch
		Set<Resource> resources = new HashSet<Resource>();
        
        for(String rcid : rcids){
            //判断rcid对应的覆盖范围是否存在
        	CoverageViewModel coverageViewModel = this.getCoverageDetail(rcid,resType);
            if(coverageViewModel == null){
                
                LOG.info("----" + rcid + ":资源覆盖范围不存在----");
                
            }else{
                deleteIds.add(rcid);
                
                //add by xiezy - 2016.04.15
                if(resType.equals(IndexSourceType.InstructionalObjectiveType.getName())){
                	notifyMap.put(coverageViewModel.getResource(), 
                    		notifyService.resourceBelongToNDLibrary(coverageViewModel.getResource()));
                }
            	
				// add by lsm offline to elasticsearch
				resources.add(new Resource(coverageViewModel.getResType(),
						coverageViewModel.getResource()));
            	
            }
        }
        
        //没有要删除的资源覆盖范围
        if(CollectionUtils.isEmpty(deleteIds)){
            
            LOG.error("rcids中的资源覆盖范围都不存在");
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.AllCoverageNotExist);
        }
        
        //调用service,批量删除资源覆盖范围
        boolean flag = getCoverageService(resType).batchDeleteCoverage(deleteIds);
        if (!flag) {
            return MessageConvertUtil
                    .getMessageString(LifeCircleErrorMessageMapper.BatchDeleteCoverageFail);
        }

		// add by lsm offline to elasticsearch
        esResourceOperation.asynBatchAdd(resources);
        
        //add by xiezy - 2016.04.15
        //异步通知智能出题
        notifyService.asynNotify4Coverage(notifyMap);
    	
        return MessageConvertUtil
                .getMessageString(LifeCircleErrorMessageMapper.BatchDeleteCoverageSuccess);
    }
    
    /**
     * 通过目标类型，覆盖范围策略，目标范围的标识，资源类型，目标资源标识删除覆盖范围	
     * <p>Create Time: 2015年7月1日   </p>
     * <p>Create author: xiezy   </p>
     * @param resType           源资源类型
     * @param resourceId        源资源Id
     * @param target            目标标识
     * @param targetType        覆盖范围的目标类型
     * @param strategy          覆盖策略
     * @return
     */
    @RequestMapping(value = "/{resource}/coverages", method = RequestMethod.DELETE)
    public @ResponseBody Map<String,String> batchDeleteCoverageByCondition(
            @PathVariable(value="res_type") String resType,
            @PathVariable(value="resource") String resourceId,
            @RequestParam(value="target",required=false) String target,
            @RequestParam(value="target_type",required=false) String targetType,
            @RequestParam(value="strategy",required=false) String strategy){
    	//add by xiezy - 2016.04.15
        Map<String,Boolean> notifyMap = new HashMap<String, Boolean>();
        if(resType.equals(IndexSourceType.InstructionalObjectiveType.getName())){
        	notifyMap.put(resourceId, 
            		notifyService.resourceBelongToNDLibrary(resourceId));
        }
    	
        boolean flag = getCoverageService(resType).batchDeleteCoverageByCondition(resType, resourceId, target, targetType, strategy);
        if (!flag) {
            return MessageConvertUtil
                    .getMessageString(LifeCircleErrorMessageMapper.BatchDeleteCoverageFail);
        }
        
		// add by lsm offline to elasticsearch
        esResourceOperation.asynAdd(
				new Resource(resType, resourceId));
        
        //add by xiezy - 2016.04.15
        //异步通知智能出题
        notifyService.asynNotify4Coverage(notifyMap);
		
        return MessageConvertUtil
                .getMessageString(LifeCircleErrorMessageMapper.BatchDeleteCoverageSuccess);
    }
    
    /**
     * 获取某个资源所覆盖的范围	
     * <p>Create Time: 2015年6月18日   </p>
     * <p>Create author: xiezy   </p>
     * @param resType       源资源类型
     * @param resUuid       源资源id
     * @return
     */
    @RequestMapping(value = "/{res_uuid}/coverages", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public List<CoverageViewModel> getCoveragesByResource(
            @PathVariable(value="res_type") String resType,@PathVariable(value="res_uuid") String resUuid,
            @RequestParam(value="targetType",required=false) String targetType,
            @RequestParam(value="target",required=false) String target,
            @RequestParam(value="strategy",required=false) String strategy){
        
        //判断源资源是否存在,不存在将抛出not found的异常
        CommonHelper.resourceExist(resType, resUuid,ResourceType.RESOURCE_SOURCE);
        
        //判断覆盖范围类型是否在可选范围内
        if(StringUtils.isNotEmpty(targetType) && !CoverageConstant.isCoverageTargetType(targetType,true)){
            
            LOG.error("覆盖范围类型不在可选范围内");
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CoverageTargetTypeNotExist);
        }
        //判断资源操作类型是否在可选范围内
        if(StringUtils.isNotEmpty(strategy) && !CoverageConstant.isCoverageStrategy(strategy,true)){
            
            LOG.error("资源操作类型不在可选范围内");
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CoverageStrategyNotExist);
        }
        
        return getCoverageService(resType).getCoveragesByResource(resType, resUuid,targetType,target,strategy);
    }
    
    /**
     * 批量获取多个资源所覆盖的范围 	
     * <p>Create Time: 2015年8月17日   </p>
     * <p>Create author: xiezy   </p>
     * @param resType       源资源类型
     * @param rids
     * @return
     */
    @RequestMapping(value = "/coverages/bulk", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public Map<String, List<CoverageViewModel>> batchGetCoverageByResource(
            @PathVariable(value="res_type") String resType,
            @RequestParam(value="rid") Set<String> rids){
        List<String> ridList = setToList(rids);
        if(CollectionUtils.isEmpty(ridList)){
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "LC/BATCH_GET_COVERAGE_INPUT_ERROR","资源的rids不能为空");
        }
        
        List<String> existRids = new ArrayList<String>();
        for(String rid : ridList){
            if(CommonHelper.resourceExistNoException(resType, rid, ResourceType.RESOURCE_SOURCE)){
                existRids.add(rid);
            }
        }
        
        if(CollectionUtils.isEmpty(existRids)){
            return new HashMap<String, List<CoverageViewModel>>();
        }
        
        return getCoverageService(resType).batchGetCoverageByResource(resType, existRids);
    }
    
    /**   ===============================Helper=================================   **/
    
    /**
     * 批量创建/修改的入参手动校验	
     * <p>Create Time: 2015年6月18日   </p>
     * <p>Create author: xiezy   </p>
     * @param object           校验的对象
     * @param isCreate         是否是创建操作
     * @return
     */
    private String paramCheckForBatch(Object object){
        String message = "";
        
        CoverageModel coverageModel = (CoverageModel)object;
        //校验
        if(StringUtils.isEmpty(coverageModel.getTarget())){
            message += "target不能为空;";
        }
        if(StringUtils.isEmpty(coverageModel.getTargetType())){
            message += "target_type不能为空;";
        }
        if(StringUtils.isEmpty(coverageModel.getStrategy())){
            message += "strategy不能为空;";
        }
        if(StringUtils.isEmpty(coverageModel.getTargetTitle())){
            message += "target_title不能为空;";
        }
        if(StringUtils.isEmpty(coverageModel.getResource())){
            message += "resource不能为空;";
        }
    
        return message;
    }
    
    /**
     * set转list	
     * <p>Create Time: 2015年8月17日   </p>
     * <p>Create author: xiezy   </p>
     * @param target
     * @return
     */
    private List<String> setToList(Set<String> target) {
        List<String> results = new ArrayList<String>();
        if(CollectionUtils.isNotEmpty(target)){
            results.addAll(target);  
        }
        return results;
    }
    
    /**
     * 获取对应的Service实现
     * <p>Create Time: 2016年2月16日   </p>
     * <p>Create author: xuzy   </p>
     * @param resType
     * @return
     */
    private CoverageService getCoverageService(String resType){
        if(!CommonServiceHelper.isQuestionDb(resType)){
            return coverageService;
        }else{
            return coverageService4QuestonDB;
        }
    }
}
