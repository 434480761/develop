package nd.esp.service.lifecycle.services.lifecycle.v06.impls;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import nd.esp.service.lifecycle.educommon.models.ResContributeModel;
import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.EspRepository;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.index.OffsetPageRequest;
import nd.esp.service.lifecycle.repository.model.Contribute;
import nd.esp.service.lifecycle.repository.sdk.ContributeRepository;
import nd.esp.service.lifecycle.repository.sdk.impl.ServicesManager;
import nd.esp.service.lifecycle.services.elasticsearch.AsynEsResourceService;
import nd.esp.service.lifecycle.services.lifecycle.v06.LifecycleServiceV06;
import nd.esp.service.lifecycle.services.offlinemetadata.OfflineService;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.busi.TransCodeUtil;
import nd.esp.service.lifecycle.utils.BeanMapperUtils;
import nd.esp.service.lifecycle.utils.ParamCheckUtil;
import nd.esp.service.lifecycle.vos.ListViewModel;
import nd.esp.service.lifecycle.vos.lifecycle.v06.ResContributeViewModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author qil
 * @version 1.0
 * @created 17-7月-2015 12:06:04
 */
@Service("lifecycleServiceV06")
@Transactional
public class LifecycleServiceImplV06 implements LifecycleServiceV06{
	private static final Logger LOG = LoggerFactory.getLogger(LifecycleServiceImplV06.class);
    
    /**
     * SDK注入
     */
    @Autowired
    private ContributeRepository contributeRepository;

    
    //by lsm 用于更新离线元数据
    @Autowired
    private OfflineService offlineService;
    
    @Autowired
    private AsynEsResourceService esResourceOperation;
   
    @Override
    public ResContributeViewModel addLifecycleStep(String resType, String resId, ResContributeModel contributeModel) {
        return addLifecycleStep(resType, resId, contributeModel, true);
    }
    
    /**
     * 增加生命周期阶段。 对于生命周期的某个环节，进行添加
     * @Method POST
     * @urlpattern  {res_type}/{uuid}/lifecycle/steps
     * 
     * @param resType
     * @param resId
     * @param contribute
     */
    @Override
    public ResContributeViewModel addLifecycleStep(String resType, String resId, ResContributeModel contributeModel,
            boolean bUpdateTime) {
        EspEntity entry = CheckResource(resType, resId);
        
        //生成SDK的入参对象,并进行model转换
        Contribute contribute = new Contribute();
        contribute = BeanMapperUtils.beanMapper(contributeModel, Contribute.class);
        contribute.setLifeStatus(contributeModel.getLifecycleStatus());
        contribute.setResType(resType);
        contribute.setResource(resId);
        contribute.setIdentifier(UUID.randomUUID().toString());
        contribute.setContributeTime(new Timestamp(new Date().getTime()));
        
        Contribute rtContribute = null;
        try {
            //调用SDK,添加
            rtContribute = contributeRepository.add(contribute);
        } catch (EspStoreException e) {
            LOG.error("添加生命周期阶段失败", e);
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CreateLifecycleFail);
        }
        
        
        //如果返回null,则抛出异常
        if (null == rtContribute) {
            LOG.error("添加生命周期阶段失败");
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CreateLifecycleFail);
        }
        
        //更新状态及更新时间 -- 经商讨去掉修改资源状态的回调
//        UpdatePropertyInResource(resType, entry, rtContribute.getLifeStatus(), bUpdateTime);
        
        
        //处理返回结果
        ResContributeViewModel contributeViewModel = BeanMapperUtils.beanMapper(rtContribute, ResContributeViewModel.class);
        contributeViewModel.setLifecycleStatus(rtContribute.getLifeStatus());
        
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
    @Override
    public Map<String,ResContributeViewModel> addLifecycleStepBulk(String resType, List<String> resIds,
            ResContributeModel contributeModel) {
        if(resIds == null) {
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CreateBatchLifecycleFail.getCode(),
                    LifeCircleErrorMessageMapper.CreateBatchLifecycleFail.getMessage()+":resources参数不能为空");
        }
        List<EspEntity> entries = new ArrayList<EspEntity>();
        for(String resId:resIds) {
            EspEntity entry = CheckResource(resType, resId);
            entries.add(entry);
        }
        
        //生成SDK的入参对象,并进行model转换
        List<Contribute> listContribute = new ArrayList<Contribute>();
        List<Contribute> rtContributes = null;
        for(String resId:resIds) {
            Contribute contribute = new Contribute();
            contribute = BeanMapperUtils.beanMapper(contributeModel, Contribute.class);
            contribute.setLifeStatus(contributeModel.getLifecycleStatus());
            contribute.setResType(resType);
            contribute.setIdentifier(UUID.randomUUID().toString());
            contribute.setResource(resId);
            contribute.setContributeTime(new Timestamp(new Date().getTime()));
            listContribute.add(contribute);
        }
        
        try {
            //调用SDK,添加
            rtContributes = contributeRepository.batchAdd(listContribute);
        } catch (EspStoreException e) {
            LOG.error("批量添加生命周期阶段失败", e);
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CreateBatchLifecycleFail);
        }
        
        //如果返回null,则抛出异常
        if (null == rtContributes || rtContributes.size()!=resIds.size()) {
            LOG.error("批量添加生命周期阶段失败");
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CreateBatchLifecycleFail);
        }
        
        Map<String,ResContributeViewModel> contributeViewModels = new HashMap<String,ResContributeViewModel>();
        for(int i=0;i<resIds.size();++i) {
            if (null == rtContributes.get(i)) {
                LOG.error("批量添加生命周期阶段失败");
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        LifeCircleErrorMessageMapper.CreateBatchLifecycleFail);
            }
            //更新状态及更新时间 -- 经商讨去掉修改资源状态的回调
//            UpdatePropertyInResource(resType, entries.get(i), rtContributes.get(i).getLifeStatus(), true);
            
            //处理返回结果
            ResContributeViewModel contributeViewModel = BeanMapperUtils.beanMapper(rtContributes.get(i), ResContributeViewModel.class);
            contributeViewModel.setLifecycleStatus(rtContributes.get(i).getLifeStatus());
            contributeViewModels.put(resIds.get(i), contributeViewModel);
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
    @Override
    public ListViewModel<ResContributeViewModel> getLifecycleSteps(String resType, String resId, String limit) {
        CheckResource(resType, resId);
        
        Contribute contributeExample = new Contribute();
        contributeExample.setResource(resId);
        contributeExample.setResType(resType);
        
        ListViewModel<ResContributeViewModel> list = new ListViewModel<ResContributeViewModel>();
        //分页参数
        Integer result[] = ParamCheckUtil.checkLimit(limit);
        list.setLimit(limit);
        Pageable pageable = new OffsetPageRequest(result[0], result[1], Direction.DESC, "contributeTime");
        Page<Contribute> contributeResult = null;
        try {
            contributeResult = contributeRepository.getPageByExample(contributeExample, pageable);
            if(contributeResult != null) {
                list.setTotal(contributeResult.getTotalElements());
                List<ResContributeViewModel> items = new ArrayList<ResContributeViewModel>();
                for(Contribute contribute:contributeResult.getContent()) {
                    ResContributeViewModel contributeViewModel = BeanMapperUtils.beanMapper(contribute, ResContributeViewModel.class);
                    contributeViewModel.setLifecycleStatus(contribute.getLifeStatus());
                    items.add(contributeViewModel);
                }
                list.setItems(items);
            }
        } catch (EspStoreException e) {
            LOG.error("获取指定资源的生命周期阶段详细失败", e);
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.QueryLifecycleFail);
        }

        return list;
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
    @Override
    public ResContributeViewModel modifyLifecycleStep(String resType, String resId, ResContributeModel contributeModel) {
        CheckResource(resType, resId);
        
        Contribute originContribute = null;
        try {
            originContribute = contributeRepository.get(contributeModel.getIdentifier());
        } catch (EspStoreException e1) {
            LOG.error("获取指定的生命周期阶段详细失败", e1);
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.GetLifecycleFail);
        }
        if(originContribute == null) {
            LOG.error("指定的生命周期阶段不存在");
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.LifecycleNotFound);
        }
        
        //生成SDK的入参对象,并进行model转换
        Contribute contribute = new Contribute();
        contribute = BeanMapperUtils.beanMapper(contributeModel, Contribute.class);
        contribute.setLifeStatus(contributeModel.getLifecycleStatus());
        contribute.setResType(resType);
        contribute.setResource(resId);
        contribute.setContributeTime(new Timestamp(new Date().getTime()));
        
        Contribute rtContribute = null;
        try {
            //调用SDK,更新
            rtContribute = contributeRepository.update(contribute);
        } catch (EspStoreException e) {
            LOG.error("修改生命周期阶段失败", e);
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.UpdateLifecycleFail);
        }
        
        
        //如果返回null,则抛出异常
        if (null == rtContribute) {
            LOG.error("修改生命周期阶段失败");
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.UpdateLifecycleFail);
        }
        
        
        //处理返回结果
        ResContributeViewModel contributeViewModel = BeanMapperUtils.beanMapper(rtContribute, ResContributeViewModel.class);
        contributeViewModel.setLifecycleStatus(rtContribute.getLifeStatus());
        
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
    @Override
    public Map<String,ResContributeViewModel> modifyLifecycleStepBulk(String resType, List<String> resIds, 
            List<ResContributeModel> contributeModels) {
        
        for(String resId:resIds) {
            CheckResource(resType, resId);
        }
        
        for(ResContributeModel contributeModel:contributeModels) {
            Contribute originContribute = null;
            try {
                originContribute = contributeRepository.get(contributeModel.getIdentifier());
            } catch (EspStoreException e1) {
                LOG.error("获取指定的生命周期阶段详细失败", e1);
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        LifeCircleErrorMessageMapper.GetLifecycleFail);
            }
            if(originContribute == null) {
                LOG.error("指定的生命周期阶段不存在");
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        LifeCircleErrorMessageMapper.LifecycleNotFound);
            }
        }
        
        Map<String,ResContributeViewModel> contributeViewModels = new HashMap<String,ResContributeViewModel>();
        //生成SDK的入参对象,并进行model转换
        for(int i=0;i<contributeModels.size();++i) {
            Contribute contribute = new Contribute();
            contribute = BeanMapperUtils.beanMapper(contributeModels.get(i), Contribute.class);
            contribute.setLifeStatus(contributeModels.get(i).getLifecycleStatus());
            contribute.setResType(resType);
            contribute.setResource(resIds.get(i));
            contribute.setContributeTime(new Timestamp(new Date().getTime()));
            
            Contribute rtContribute = null;
            try {
                //调用SDK,添加
                rtContribute = contributeRepository.add(contribute);
            } catch (EspStoreException e) {
                LOG.error("批量添加生命周期阶段失败", e);
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        LifeCircleErrorMessageMapper.CreateBatchLifecycleFail);
            }
            
            //如果返回null,则抛出异常
            if (null == rtContribute) {
                LOG.error("批量添加生命周期阶段失败");
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        LifeCircleErrorMessageMapper.CreateBatchLifecycleFail);
            }
            
            //处理返回结果
            ResContributeViewModel contributeViewModel = BeanMapperUtils.beanMapper(rtContribute, ResContributeViewModel.class);
            contributeViewModel.setLifecycleStatus(rtContribute.getLifeStatus());
            contributeViewModels.put(resIds.get(i),contributeViewModel);
        }
        
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
    @Override
    public boolean delLifecycleStep(String resType, String resId, String stepId) {
        
        try {
            Contribute contribute = contributeRepository.get(stepId);
            if(contribute == null || !contribute.getResType().equals(resType) 
                    || !contribute.getResource().equals(resId)) {
                LOG.error("指定的生命周期阶段不存在");
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        LifeCircleErrorMessageMapper.LifecycleNotFound);
            }
            contributeRepository.del(stepId);
        } catch (EspStoreException e) {
            LOG.error("删除生命周期阶段失败");
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.DeleteLifecycleFail);
        }
        
        return true;
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
    @Override
    public boolean delLifecycleStepsBulk(String resType, String resId, Set<String> stepIds) {
        List<String> ids = new ArrayList<String>();
        for(String stepId:stepIds) {
            try {
                Contribute contribute = contributeRepository.get(stepId);
                if(contribute == null || !contribute.getResType().equals(resType) 
                        || !contribute.getResource().equals(resId)) {
                    LOG.error("指定的生命周期阶段不存在");
                    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                            LifeCircleErrorMessageMapper.LifecycleNotFound);
                }
                ids.add(stepId);
            } catch (EspStoreException e) {
                LOG.error("删除生命周期阶段失败");
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        LifeCircleErrorMessageMapper.DeleteLifecycleFail);
            }
        }
        try {
            contributeRepository.batchDel(ids);
        } catch (EspStoreException e) {
            LOG.error("删除生命周期阶段失败");
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.DeleteLifecycleFail);
        }
        return true;
    }
    
    /**
     * update相对应资源表的lifecycle字段  
     * <p>Create Time: 2015年7月20日   </p>
     * <p>Create author: qil   </p>
     * @param resType   资源类型
     * @param entry    资源id集合
     */
	private void UpdatePropertyInResource(String resType, EspEntity entry, String status, boolean bUpdateTime){
        try {
            //获取通用SDK仓库
            EspRepository espRepository = ServicesManager.get(resType);
            Education object = (Education)entry;
            
            //覆盖status, last_update字段
            if(bUpdateTime) {
                object.setLastUpdate(new Timestamp(new Date().getTime()));
            }
            object.setStatus(status);
            
            //更新
            espRepository.update(entry);
            
        } catch (Exception e) {
            LOG.error("更新lifecycle相关字段失败", e);
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.UpdateLifecyclePropertyFail);
        }
        
    }
    
    /**
     * 校验资源存在
     * <p>Create Time: 2015年7月20日   </p>
     * <p>Create author: qil   </p>
     * @param resType   资源类型
     * @param resIds    资源id集合
     */
    private EspEntity CheckResource(String resType, String resId){
        try {
            //获取通用SDK仓库
            EspRepository espRepository = ServicesManager.get(resType);
            EspEntity entry = espRepository.get(resId);
            
            if(entry == null){
                LOG.error("目标资源不存在");
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        LifeCircleErrorMessageMapper.ResourceNotFound);
            }
            
            return entry;
        } catch (Exception e) {
            LOG.error("目标资源不存在", e);
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.ResourceNotFound);
        }
    }

    /* (non-Javadoc)
     * @see nd.esp.service.lifecycle.services.lifecycle.v06.LifecycleServiceV06#addLifecycleStep(java.lang.String, java.lang.String, java.lang.Boolean, java.lang.String)
     */
    @Override
    public ResContributeViewModel addLifecycleStep(String resType, String resId, Boolean isSuccess, String message) {
        ResContributeModel contributeModel = new ResContributeModel();
        contributeModel.setTargetId("777");
        contributeModel.setTargetName("LCMS");
        contributeModel.setTargetType("USER");
        contributeModel.setMessage(message);
        if(isSuccess){
            contributeModel.setLifecycleStatus(TransCodeUtil.getTransEdStatus(true));
            contributeModel.setProcess(100.0f);
        }else{
            contributeModel.setLifecycleStatus(TransCodeUtil.getTransErrStatus(true));
            contributeModel.setProcess(0.0f);
        }
        return this.addLifecycleStep(resType, resId, contributeModel, false);
    }
}
