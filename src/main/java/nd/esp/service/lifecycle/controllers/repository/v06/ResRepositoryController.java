package nd.esp.service.lifecycle.controllers.repository.v06;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import javax.validation.Valid;

import nd.esp.service.lifecycle.models.v06.ResRepositoryModel;
import nd.esp.service.lifecycle.services.repository.v06.ResRepositoryService;
import nd.esp.service.lifecycle.support.Constant;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.busi.ValidResultHelper;
import nd.esp.service.lifecycle.utils.BeanMapperUtils;
import nd.esp.service.lifecycle.utils.MessageConvertUtil;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.vos.repository.v06.ResRepositoryViewModel;
import nd.esp.service.lifecycle.vos.repository.v06.ResRepositoryViewModelForUpdate;
import nd.esp.service.lifecycle.vos.statics.ResRepositoryConstant;

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
 * 物理存储空间Controller
 * <p>Create Time: 2015年7月16日           </p>
 * @author xiezy
 */
@RestController
@RequestMapping("/v0.6/resources/repository")
public class ResRepositoryController {
    private static final Logger LOG = LoggerFactory.getLogger(ResRepositoryController.class);
    
    @Autowired
    @Qualifier("resRepositoryService")
    private ResRepositoryService resRepositoryService;
    
    /**
     * 申请物理资源存储空间	
     * <p>Create Time: 2015年7月16日   </p>
     * <p>Create author: xiezy   </p>
     * @param repositoryViewModel
     * @param validResult
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResRepositoryViewModel createRepository(@Valid @RequestBody ResRepositoryViewModel repositoryViewModel,BindingResult validResult){
        //校验
        ValidResultHelper.valid(validResult, "LC/CREATE_REPOSITORY_PARAM_VALID_FAIL", "ResRepositoryController", "createRepository");

        if(!ResRepositoryConstant.isRepositoryTargetType(repositoryViewModel.getTargetType())){
            LOG.error("targetType不在可选范围内-targetType取值范围为:Org,Group");
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.TargetTypeIsNotExist);
        }
        
        if(!ResRepositoryConstant.isRepositoryStatus(repositoryViewModel.getStatus())){
            LOG.error("Status不在可选范围内-Status取值范围为:APPLY,RUNNING,REMOVE");
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.StatusIsNotExist);
        }
        
        //同一组织机构不能重复创建
        ResRepositoryModel repositoryModel4Deatil = 
                resRepositoryService.getRepositoryDetailByCondition(repositoryViewModel.getTargetType(), repositoryViewModel.getTarget());
        if(repositoryModel4Deatil != null && repositoryModel4Deatil.getEnable()){
            LOG.error("物理资源存储空间已申请");
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.RepositoryIsExist);
        }
        
        if(repositoryModel4Deatil == null){//第一次创建
            //完善model属性
            repositoryViewModel.setIdentifier(UUID.randomUUID().toString());
            repositoryViewModel.setCreateTime(new Date());
            repositoryViewModel.setRepositoryPath(Constant.CS_SESSION_PATH + "/" + repositoryViewModel.getTargetType() + "/" + repositoryViewModel.getTarget());
        }else if(!repositoryModel4Deatil.getEnable()){//已创建过,但是enable为false的情况,重新创建时将enable设为true
            repositoryViewModel.setIdentifier(repositoryModel4Deatil.getIdentifier());
            repositoryViewModel.setCreateTime(new Date());
            repositoryViewModel.setRepositoryPath(repositoryModel4Deatil.getRepositoryPath());
            repositoryViewModel.setEnable(true);
        }
        
        //viewModel->model
        ResRepositoryModel repositoryModel = BeanMapperUtils.beanMapper(repositoryViewModel, ResRepositoryModel.class);
        //调用Service
        repositoryModel = resRepositoryService.createRepository(repositoryModel);
        //model->viewModel
        repositoryViewModel = BeanMapperUtils.beanMapper(repositoryModel, ResRepositoryViewModel.class);
        
        return repositoryViewModel;
    }
    
    /**
     * 获取物理空间信息	
     * <p>Create Time: 2015年7月16日   </p>
     * <p>Create author: xiezy   </p>
     * @param type      存储空间的类型，Org代表组织机构
     * @param target    目标的标识
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResRepositoryViewModel getRepositoryDetailByCondition(@RequestParam String type,@RequestParam String target){
        if(!ResRepositoryConstant.isRepositoryTargetType(type)){
            LOG.error("targetType不在可选范围内-targetType取值范围为:Org,Group");
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.TargetTypeIsNotExist);
        }
        
        if(StringUtils.isEmpty(target)){
            LOG.error("target不能为空");
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "LC/TARGET_NOT_BLANK","target不能为空");
        }
        
        ResRepositoryModel repositoryModel = resRepositoryService.getRepositoryDetailByCondition(type, target);
        if(repositoryModel == null || !repositoryModel.getEnable()){
            return null;
        }
        
        return BeanMapperUtils.beanMapper(repositoryModel, ResRepositoryViewModel.class);
    }
    
    /**
     * 通过ID获取物理空间信息
     * <p>Create Time: 2015年7月16日   </p>
     * <p>Create author: xiezy   </p>
     * @param id    私有空间的id标识
     * @return
     */
    @RequestMapping(value="/{id}",method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResRepositoryViewModel getRepositoryDetailById(@PathVariable String id){
        ResRepositoryModel repositoryModel = resRepositoryService.getRepositoryDetailById(id);
        if(repositoryModel == null || !repositoryModel.getEnable()){
            return null;
        }
        
        return BeanMapperUtils.beanMapper(repositoryModel, ResRepositoryViewModel.class);
    }
    
    /**
     * 修改资源物理空间信息	
     * <p>Create Time: 2015年7月16日   </p>
     * <p>Create author: xiezy   </p>
     * @param id    私有空间的id标识
     * @param repositoryViewModelForUpdate
     * @param validResult
     * @return
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces={MediaType.APPLICATION_JSON_VALUE})
    public ResRepositoryViewModel updateRepository(@PathVariable String id,
            @Valid @RequestBody ResRepositoryViewModelForUpdate repositoryViewModelForUpdate,BindingResult validResult){
        //校验
        ValidResultHelper.valid(validResult, "LC/CREATE_REPOSITORY_PARAM_VALID_FAIL", "ResRepositoryController", "updateRepository");        
        
        if(!ResRepositoryConstant.isRepositoryStatus(repositoryViewModelForUpdate.getStatus())){
            LOG.error("Status不在可选范围内-Status取值范围为:APPLY,RUNNING,REMOVE");
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.StatusIsNotExist);
        }
        
        ResRepositoryViewModel repositoryViewModel4Detail = getRepositoryDetailById(id);
        if(repositoryViewModel4Detail == null){
            LOG.error("资源物理空间信息不存在");
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.RepositoryNotFind);
        }
        
        //将不需要修改的属性赋值到更新的model中
        repositoryViewModelForUpdate.setIdentifier(repositoryViewModel4Detail.getIdentifier());
        repositoryViewModelForUpdate.setCreateTime(repositoryViewModel4Detail.getCreateTime());
        repositoryViewModelForUpdate.setTargetType(repositoryViewModel4Detail.getTargetType());
        repositoryViewModelForUpdate.setTarget(repositoryViewModel4Detail.getTarget());
        repositoryViewModelForUpdate.setRepositoryPath(repositoryViewModel4Detail.getRepositoryPath());
        
        //viewModel->model
        ResRepositoryModel repositoryModel = BeanMapperUtils.beanMapper(repositoryViewModelForUpdate, ResRepositoryModel.class);
        //调用Service
        repositoryModel = resRepositoryService.updateRepository(repositoryModel);
        //model->viewModel
        return BeanMapperUtils.beanMapper(repositoryModel, ResRepositoryViewModel.class);
    }
    
    /**
     * 通过目标类型和目标标识删除私有空间
     * <p>Create Time: 2015年7月17日   </p>
     * <p>Create author: xiezy   </p>
     * @param type
     * @param target
     * @return
     */
    @RequestMapping(method = RequestMethod.DELETE)
    public @ResponseBody Map<String, String> deleteRepositoryByCondition(@RequestParam String type,@RequestParam String target){
        if(!ResRepositoryConstant.isRepositoryTargetType(type)){
            LOG.error("targetType不在可选范围内-targetType取值范围为:Org,Group");
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.TargetTypeIsNotExist);
        }
        
        if(StringUtils.isEmpty(target)){
            LOG.error("target不能为空");
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "LC/TARGET_NOT_BLANK","target不能为空");
        }
        
        return deleteRepository(type, target, null, false);
    }
    
    /**
     * 通过ID删除资源物理空间信息	
     * <p>Create Time: 2015年7月17日   </p>
     * <p>Create author: xiezy   </p>
     * @param id
     * @return
     */
    @RequestMapping(value = "/{id}",method = RequestMethod.DELETE)
    public @ResponseBody Map<String, String> deleteRepositoryById(@PathVariable String id){
        return deleteRepository(null, null, id, true);
    }
    
    /**
     * 删除资源物理空间信息	
     * <p>Create Time: 2015年7月17日   </p>
     * <p>Create author: xiezy   </p>
     * @param type
     * @param target
     * @param id
     * @param byId  是否通过id删除
     * @return
     */
    private Map<String, String> deleteRepository(String type,String target,String id,boolean byId){
        ResRepositoryViewModel repositoryViewModel4Detail = null;
        if(byId){
            repositoryViewModel4Detail = getRepositoryDetailById(id);
        }else{
            repositoryViewModel4Detail = getRepositoryDetailByCondition(type, target);
        }
        
        if(repositoryViewModel4Detail == null){
            LOG.error("资源物理空间信息不存在");
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.RepositoryNotFind);
        }
        
        //viewModel->model
        ResRepositoryModel repositoryModel = BeanMapperUtils.beanMapper(repositoryViewModel4Detail, ResRepositoryModel.class);
        //伪删除
        repositoryModel.setEnable(false);
        
        boolean isSuccess = resRepositoryService.deleteRepository(repositoryModel);
        if (!isSuccess) {
            return MessageConvertUtil
                    .getMessageString(LifeCircleErrorMessageMapper.DeleteRepositoryFail);
        }
        
        return MessageConvertUtil
                .getMessageString(LifeCircleErrorMessageMapper.DeleteRepositorySuccess);
    }
}
