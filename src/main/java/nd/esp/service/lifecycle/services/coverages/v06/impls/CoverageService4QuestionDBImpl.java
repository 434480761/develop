package nd.esp.service.lifecycle.services.coverages.v06.impls;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nd.esp.service.lifecycle.daos.coverage.v06.CoverageDao;
import nd.esp.service.lifecycle.models.coverage.v06.CoverageModel;
import nd.esp.service.lifecycle.models.coverage.v06.CoverageModelForUpdate;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.ResCoverage;
import nd.esp.service.lifecycle.repository.sdk.ResCoverage4QuestionDBRepository;
import nd.esp.service.lifecycle.services.coverages.v06.CoverageService;
import nd.esp.service.lifecycle.support.DbName;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.utils.BeanMapperUtils;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.vos.coverage.v06.CoverageViewModel;
import nd.esp.service.lifecycle.vos.statics.CoverageConstant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service(value="coverageService4QuestionDBImpl")
@Transactional(value="questionTransactionManager")
public class CoverageService4QuestionDBImpl implements CoverageService{
    private static final Logger LOG = LoggerFactory.getLogger(CoverageService4QuestionDBImpl.class);
    /**
     * SDK注入
     */
    @Autowired
    private ResCoverage4QuestionDBRepository resCoverageRepository;
    @Autowired
    private CoverageDao coverageDao;
    
    @Override
    public CoverageViewModel createCoverage(CoverageModel coverageModel) {
        //判断一个资源是否已经有OWNER的覆盖策略
        if(coverageModel.getStrategy().equals(CoverageConstant.STRATEGY_OWNER)){
            this.checkResourceHaveOwnerOnlyOne(coverageModel.getResType(), coverageModel.getResource(),null,true);
        }
        
        //生成SDK的入参对象,并进行model转换
        ResCoverage resCoverage = new ResCoverage();
        resCoverage = BeanMapperUtils.beanMapper(coverageModel, ResCoverage.class);
        
        ResCoverage rc = null;
        try {
            //调用SDK,添加
            rc = resCoverageRepository.add(resCoverage);
        } catch (EspStoreException e) {
            
            LOG.error("添加资源覆盖范围失败", e);
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getLocalizedMessage());
        }
        
        //如果返回null,则抛出异常
        if (null == rc) {
            
            LOG.error("添加资源覆盖范围失败");
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CreateCoverageFail);
        }
        
        //处理返回结果
        CoverageViewModel coverageViewModel = BeanMapperUtils.beanMapper(rc, CoverageViewModel.class);
        
        return coverageViewModel;
    }

    @Override
    public List<CoverageViewModel> batchCreateCoverage(List<CoverageModel> coverageModels,boolean isCreateWithResource) {
        
        for(CoverageModel cm : coverageModels){
            if(isCreateWithResource){
                //参数校验,保证批量传入的覆盖范围,一个资源的覆盖范围，有且仅有一个OWNNER的覆盖范围策略
                CommonHelper.checkCoverageHaveOnlyOneOwner(coverageModels, true);
                
                //判断覆盖范围类型是否在可选范围内
                if(!CoverageConstant.isCoverageTargetType(cm.getTargetType(),false)){
                    
                    LOG.error("覆盖范围类型不在可选范围内");
                    
                    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                            LifeCircleErrorMessageMapper.CoverageTargetTypeNotExist);
                }
                //判断资源操作类型是否在可选范围内
                if(!CoverageConstant.isCoverageStrategy(cm.getStrategy(),false)){
                    
                    LOG.error("资源操作类型不在可选范围内");
                    
                    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                            LifeCircleErrorMessageMapper.CoverageStrategyNotExist);
                }
            }else{
                //判断一个资源是否已经有OWNER的覆盖策略
                if(cm.getStrategy().equals(CoverageConstant.STRATEGY_OWNER)){
                    this.checkResourceHaveOwnerOnlyOne(cm.getResType(), cm.getResource(),null,true);
                }
            }
        }
        
        //生成SDK的入参对象,并进行model转换
        List<ResCoverage> params = new ArrayList<ResCoverage>();
        for (CoverageModel rc : coverageModels) {
            ResCoverage cvm = BeanMapperUtils.beanMapper(rc, ResCoverage.class);
            params.add(cvm);
        }
        
        List<ResCoverage> resCoverages = null;
        try {
            //调用SDK,批量添加
            resCoverages = resCoverageRepository.batchAdd(params);
        } catch (EspStoreException e) {
            
            LOG.error("批量添加资源覆盖范围失败", e);
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getMessage());
        }
        
        //如果返回null,则抛出异常
        if (null == resCoverages) {
            
            LOG.error("批量添加资源覆盖范围失败");
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.BatchCreateCoverageFail);
        }
        
        //处理返回结果
        List<CoverageViewModel> resultList = new ArrayList<CoverageViewModel>();
        for (ResCoverage rc : resCoverages) {
            CoverageViewModel cvm = BeanMapperUtils.beanMapper(rc, CoverageViewModel.class);
            resultList.add(cvm);
        }
        return resultList;
    }

    @Override
    public CoverageViewModel getCoverageDetail(String rcid) {
        ResCoverage resCoverage = null;
        try {
            resCoverage = resCoverageRepository.get(rcid);
        } catch (EspStoreException e) {
            
            LOG.error("获取资源覆盖范围失败", e);
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getMessage());
        }
        
        if(resCoverage == null){
            return null;
        }
        
        //处理返回结果
        CoverageViewModel coverageViewModel = BeanMapperUtils.beanMapper(resCoverage, CoverageViewModel.class);
        return coverageViewModel;
    }

    @Override
    public Map<String, CoverageViewModel> batchGetCoverageDetail(List<String> rcids) {
        Map<String, CoverageViewModel> map = new HashMap<String, CoverageViewModel>();
        
        try {
            //调用SDK,批量获取
            List<ResCoverage> resCoverages = resCoverageRepository.getAll(rcids);
            
            if(!CollectionUtils.isEmpty(resCoverages)){
                for(ResCoverage rc : resCoverages){
                    if(rc != null){
                        String rcid = rc.getIdentifier();
                        CoverageViewModel cvm = BeanMapperUtils.beanMapper(rc, CoverageViewModel.class);
                        map.put(rcid, cvm);
                    }
                }
            }
        } catch (EspStoreException e) {
            
            LOG.error("批量获取资源覆盖范围失败", e);
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getMessage());
        }
        
        return map;
    }
    
    @Override
    public CoverageViewModel updateCoverage(CoverageModelForUpdate coverageModelForUpdate) {
        //判断一个资源是否已经有OWNER的覆盖策略
        if(coverageModelForUpdate.getStrategy().equals(CoverageConstant.STRATEGY_OWNER)){
            this.checkResourceHaveOwnerOnlyOne(coverageModelForUpdate.getResType(), coverageModelForUpdate.getResource(),
                    coverageModelForUpdate.getIdentifier(),false);
        }
        
        CoverageViewModel coverageViewModel = new CoverageViewModel();
        //入参转换
        ResCoverage resCoverage = BeanMapperUtils.beanMapper(coverageModelForUpdate, ResCoverage.class);
        //调用SDK,更新
        try {
            resCoverage = resCoverageRepository.update(resCoverage);
            coverageViewModel = BeanMapperUtils.beanMapper(resCoverage, CoverageViewModel.class);
        } catch (EspStoreException e) {
            
            LOG.error("修改资源覆盖范围失败", e);
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getMessage());
        }
        
        return coverageViewModel;
    }
    
    @Override
    public boolean deleteCoverage(String rcid) {
        try {
            resCoverageRepository.del(rcid);
        } catch (EspStoreException e) {
            
            LOG.error("删除资源覆盖范围失败");
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getMessage());
        }
        
        return true;
    }

    @Override
    public boolean batchDeleteCoverage(List<String> rcids) {
        try {
            resCoverageRepository.batchDel(rcids);
        } catch (EspStoreException e) {
            
            LOG.error("批量删除资源覆盖范围失败");
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getMessage());
        }
        
        return true;
    }
    
    @Override
    public boolean batchDeleteCoverageByCondition(String resType, String resourceId, 
            String target, String targetType, String strategy) {
        //用于存放需要删除的资源关系id
        List<String> deleteIds = new ArrayList<String>();
        
        ResCoverage resCoverage = new ResCoverage();
        resCoverage.setResType(resType);
        resCoverage.setResource(resourceId);
        resCoverage.setTarget(StringUtils.isEmpty(target) ? null : target);
        resCoverage.setTargetType(StringUtils.isEmpty(targetType) ? null : targetType);
        resCoverage.setStrategy(StringUtils.isEmpty(strategy) ? null : strategy);
        
        List<ResCoverage> resCoverages = new ArrayList<ResCoverage>();
        try {
            resCoverages = resCoverageRepository.getAllByExample(resCoverage);
        
            //将找到的资源覆盖范围的id放入deleteIds
            for(ResCoverage rc : resCoverages){
                deleteIds.add(rc.getIdentifier());
            }
        } catch (EspStoreException e) {
            
            LOG.error("根据条件获取资源覆盖范围列表失败", e);
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getMessage());
        }
        
        try {
            if(deleteIds.isEmpty()){
                return true;
            }
            
            //调用SDK,批量删除资源覆盖范围
            resCoverageRepository.batchDel(deleteIds);
        } catch (EspStoreException e) {
            
            LOG.error("批量删除资源关系失败", e);
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getMessage());
        }
        
        return true;
    }
    
    @Override
    public List<CoverageViewModel> getCoveragesByResource(String resType, String resUuid,String targetType,String target,String strategy) {
        ResCoverage resCoverage = new ResCoverage();
        resCoverage.setResource(resUuid);
        resCoverage.setResType(resType);
        
        if(StringUtils.isNotEmpty(targetType)){
            resCoverage.setTargetType(targetType);
        }
        if(StringUtils.isNotEmpty(target)){
            resCoverage.setTarget(target);
        }
        if(StringUtils.isNotEmpty(strategy)){
            resCoverage.setStrategy(strategy);
        }
        
        List<ResCoverage> resCoverages = new ArrayList<ResCoverage>();
        try {
            resCoverages = resCoverageRepository.getAllByExample(resCoverage);
        } catch (EspStoreException e) {
            
            LOG.error("根据条件获取资源覆盖范围列表失败", e);
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getMessage());
        }
        List<CoverageViewModel> list = new ArrayList<CoverageViewModel>();
        for (ResCoverage rc : resCoverages) {
            CoverageViewModel cvm = BeanMapperUtils.beanMapper(rc, CoverageViewModel.class);
            list.add(cvm);
        }
        return list;
    }
    
    @Override
    public Map<String, List<CoverageViewModel>> batchGetCoverageByResource(String resType, List<String> rids) {
        
        return coverageDao.batchGetCoverageByResource(resType, rids,DbName.QUESTION);
    }
    
    /**   ===============================Helper=================================   **/
    
    @Override
    public CoverageViewModel getCoverageByCondition(String targetType,String target,String strategy,String resource) {
        ResCoverage resCoverage = new ResCoverage();
        resCoverage.setTargetType(targetType);
        resCoverage.setTarget(target);
        resCoverage.setStrategy(strategy);
        resCoverage.setResource(resource);
        
        ResCoverage rc = null;
        try {
            rc = resCoverageRepository.getByExample(resCoverage);
        } catch (EspStoreException e) {
            
            LOG.error("根据条件获取资源覆盖范围失败", e);
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getMessage());
        }
        
        //若不存在，返回null
        if(rc == null){
            return null;
        }
        
        //处理返回结果
        CoverageViewModel coverageViewModel = BeanMapperUtils.beanMapper(rc, CoverageViewModel.class);
        
        return coverageViewModel;
    }
    
    /**
     * 判断一个资源是否已经有OWNER的覆盖策略 
     * <p>Create Time: 2015年10月12日   </p>
     * <p>Create author: xiezy   </p>
     * @param resType     资源类型
     * @param resourceId  资源id
     * @param coverageId  覆盖范围id(修改时才传)
     * @param isCreate    是否是创建
     */
    private void checkResourceHaveOwnerOnlyOne(String resType,String resourceId,String coverageId,boolean isCreate){
        ResCoverage resCoverage = new ResCoverage();
        resCoverage.setResource(resourceId);
        resCoverage.setResType(resType);
        resCoverage.setStrategy(CoverageConstant.STRATEGY_OWNER);
        
        List<ResCoverage> resCoverages = new ArrayList<ResCoverage>();
        try {
            resCoverages = resCoverageRepository.getAllByExample(resCoverage);
        } catch (EspStoreException e) {
            
            LOG.error("判断一个资源是否已经有OWNER的覆盖策略时--根据条件获取资源覆盖范围列表失败", e);
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.GetCoverageListByConditionFail.getCode(),e.getMessage());
        }
        
        if(isCreate){
            if(CollectionUtils.isNotEmpty(resCoverages)){//说明已经存在Strategy=OWNER的覆盖范围
                LOG.error("该资源已存在strategy=OWNER的覆盖范围--一个资源的覆盖范围，有且仅有一个OWNNER的覆盖范围策略");
                
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        LifeCircleErrorMessageMapper.CoverageAleadyHaveOwner.getCode(),
                        "该资源已存在strategy=OWNER的覆盖范围--一个资源的覆盖范围，有且仅有一个OWNNER的覆盖范围策略");
            } 
        }else{
            if((CollectionUtils.isNotEmpty(resCoverages) && resCoverages.size()>1) || 
               (CollectionUtils.isNotEmpty(resCoverages) && resCoverages.size()==1 && !resCoverages.get(0).getIdentifier().equals(coverageId))){
                LOG.error("该资源已存在strategy=OWNER的覆盖范围--一个资源的覆盖范围，有且仅有一个OWNNER的覆盖范围策略");
                
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        LifeCircleErrorMessageMapper.CoverageAleadyHaveOwner.getCode(),
                        "该资源已存在strategy=OWNER的覆盖范围--一个资源的覆盖范围，有且仅有一个OWNNER的覆盖范围策略");
            }
        }
    }
    
//***************************History******************************************//    
//    /**
//     * update相对应资源表的resCoverages字段 -- 去掉对于冗余字段的维护
//     * <p>Create Time: 2015年6月23日   </p>
//     * <p>Create author: xiezy   </p>
//     * @param resType               资源类型
//     * @param resIds                资源id集合
//     * @param coverages             由于事务原因需要特殊处理的覆盖范围
//     * @param resouceModel          资源实体,当isCreateWithResource为true时必传
//     * @param isCreateWithResource  判断是否是在创建资源时同时创建覆盖范围
//     * @param flag                  操作类型标识,0--创建,1--更新,2--删除.当isCreateWithResource为true时忽略
//     */
//    @Deprecated
//    private void updateResCoveragesInResource(String resType,Map<String,String> resIds,
//            Map<String, List<ResCoverage>> coverages,Education resouceModel,boolean isCreateWithResource,int flag){
//        //获取通用SDK仓库
//        EspRepository espRepository = ServicesManager.get(resType);
//        
//        if(isCreateWithResource){
//            /*
//             *  新增的资源
//             *  1.由于事务原因,暂未入库
//             *  2.isCreateWithResource为true是忽略flag标识
//             *  3.resouceModel不能为空
//             *  4.resIds忽略
//             *  5.coverages大小为1
//             */
//            if(resouceModel == null || CollectionUtils.isEmpty(coverages) ||coverages.size() != 1){
//                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
//                        LifeCircleErrorMessageMapper.UpdateResCoverageGetResourceFail.getCode(),
//                        "06创建资源同时覆盖范围时,修改冗余的覆盖范围资源时传参错误！");
//            }
//            
//            List<String> list4update = new ArrayList<String>();
//            for(String key : coverages.keySet()){
//                for(ResCoverage rc : coverages.get(key)){
//                    list4update.add(rc.getTargetType() + "/" + rc.getTarget() + "/" + rc.getStrategy());
//                }
//            }
//            
//            //覆盖resCoverages字段
//            resouceModel.setLastUpdate(new Timestamp(new Date().getTime()));
//            
//            try {
//                espRepository.update(resouceModel);
//            } catch (EspStoreException e) {
//                LOG.error("更新resCoverages字段失败", e);
//                
//                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
//                        LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getMessage());
//            }
//        }else{
//            //需要更新的集合
//            List<EspEntity> entityList = new ArrayList<EspEntity>();
//            
//            for(String resId : resIds.values()){
//                
//                EspEntity entry = null;
//                try {
//                    entry = espRepository.get(resId);
//                } catch (EspStoreException e) {
//                    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
//                            LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getMessage());
//                }
//                Education object = (Education)entry;
//                
//                if(object == null){
//                    
//                    LOG.error("更新resCoverages字段时,获取资源失败");
//                    
//                    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
//                            LifeCircleErrorMessageMapper.UpdateResCoverageGetResourceFail);
//                }
//                
//                //根据resType和resourceId查询出资源覆盖范围
//                List<CoverageViewModel> list4ResCoverages = 
//                        this.getCoveragesByResource(resType, resId,null,null,null);
//                
//                //根据flag的不同,对于list4ResCoverages的处理不同
////                if(flag == Constant.FLAG_CREATE){
////                    for(ResCoverage rc : coverages.get(resId)){
////                        CoverageViewModel cvm = BeanMapperUtils.beanMapper(rc, CoverageViewModel.class);
////                        list4ResCoverages.add(cvm);
////                    }
////                }else if(flag == Constant.FLAG_UPDATE){
////                    for(CoverageViewModel cvm : list4ResCoverages){
////                        for(ResCoverage rc : coverages.get(resId)){
////                            if(cvm.getIdentifier().equals(rc.getIdentifier())){
////                                cvm.setTargetType(rc.getTargetType());
////                                cvm.setTarget(rc.getTarget());
////                                cvm.setStrategy(rc.getStrategy());
////                                break;
////                            }
////                        }
////                    }
////                }else if(flag == Constant.FLAG_DELETE){
////                    List<CoverageViewModel> needRemoveList = new ArrayList<CoverageViewModel>();
////                    for(CoverageViewModel cvm : list4ResCoverages){
////                        for(ResCoverage rc : coverages.get(resId)){
////                            if(cvm.getIdentifier().equals(rc.getIdentifier())){
////                                needRemoveList.add(cvm);
////                                break;
////                            }
////                        }
////                    }
////                    
////                    if(CollectionUtils.isNotEmpty(needRemoveList)){
////                        list4ResCoverages.removeAll(needRemoveList);
////                    }
////                }
//                
//                //转为List<String>,格式:targetType/target/strategy
//                List<String> list4update = new ArrayList<String>();
//                for(CoverageViewModel coverageViewModel : list4ResCoverages){
//                    list4update.add(coverageViewModel.getTargetType() + "/" + coverageViewModel.getTarget() + "/" + coverageViewModel.getStrategy());
//                }
//                //覆盖resCoverages字段
//                object.setLastUpdate(new Timestamp(new Date().getTime()));
//                
//                //添加到待更新的集合中
//                entityList.add(entry);
//            }
//            
//            //批量更新
//            if(CollectionUtils.isNotEmpty(entityList)){
//                try {
//                    espRepository.batchAdd(entityList);
//                } catch (EspStoreException e) {
//                    LOG.error("更新resCoverages字段失败", e);
//                    
//                    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
//                            LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getMessage());
//                }
//            }    
//        }
//    }
}
