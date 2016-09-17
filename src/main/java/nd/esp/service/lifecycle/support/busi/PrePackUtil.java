package nd.esp.service.lifecycle.support.busi;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import nd.esp.service.lifecycle.educommon.models.ResCoverageModel;
import nd.esp.service.lifecycle.educommon.models.ResourceModel;
import nd.esp.service.lifecycle.educommon.services.impl.CommonServiceHelper;
import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;
import nd.esp.service.lifecycle.entity.PackagingParam;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.ResCoverage;
import nd.esp.service.lifecycle.repository.sdk.ResCoverage4QuestionDBRepository;
import nd.esp.service.lifecycle.repository.sdk.ResCoverageRepository;
import nd.esp.service.lifecycle.support.Constant;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.vos.statics.CoverageConstant;
import nd.esp.service.lifecycle.services.packaging.v06.PackageService;

/**
 * 
 * 触发预打包线程
 * <p>Create Time: 2015年12月17日           </p>
 * @author qil
 */
@Component
public class PrePackUtil {
    private final static Logger LOG = LoggerFactory.getLogger(PrePackUtil.class);
    
    private final static ExecutorService executorService = CommonHelper.getForkJoinPool();
    
    @Autowired
    private ResCoverageRepository resCoverageRepository;
    
    @Autowired
    private ResCoverage4QuestionDBRepository resCoverage4QtiRepository;
    
    @Autowired
    private PackageService packageService;
    
    public static final String ONLINE_STATUS = "ONLINE";
    
    public void tryPrePack(ResourceViewModel resource, String resType, boolean bLowPriority) {
        executorService.execute(new PrePackThread(resource, resType, bLowPriority));
    }
    
    public class PrePackThread extends Thread {
        private ResourceViewModel resource;
        private String resType;
        private boolean bCreate;
        private boolean bLowPriority;
        
        public PrePackThread(ResourceViewModel resource, String resType, boolean bLowPriority) {
            this.resource = resource;
            this.resType = resType;
            this.bCreate = bCreate;
            this.bLowPriority = bLowPriority;
        }
        

        @Override
        public void run() {
            if(isNeedPrePack(resource, resType)) {
                PackagingParam param = new PackagingParam();
                param.setResType(this.resType);
                param.setUuid(this.resource.getIdentifier());
                param.setPath(PackageUtil.getResourcePath(this.resource));
                if(bLowPriority) {
                    int priority = Integer.parseInt(Constant.PACKAGING_PRIORITY);
                    if(priority>0) {
                        --priority;
                    }
                    param.setPriority(priority);
                }
                try {
                    packageService.triggerPackaging(param);
                } catch (Exception e) {
                    LOG.error("习题更新预打包失败", e);
                }
            }
        }
    }
    
    
    /**
     * 根据策略判断是否需预打包
     * <p>Description:              </p>
     * <p>Create Time: 2015年12月17日   </p>
     * <p>Create author: qil   </p>
     * @param param
     */
    private boolean isNeedPrePack(ResourceViewModel resource, String resType) {
        if(!Constant.ENABLE_PRE_PACK) {
            return false;
        }
        
        if(!IndexSourceType.QuestionType.getName().equals(resType) 
                && !IndexSourceType.ToolsType.getName().equals(resType)
                && !IndexSourceType.SourceCourseWareObjectType.getName().equals(resType)) {
            return false;
        }
        
        
        
        // 根据resType和resourceId查询出资源覆盖范围
        ResCoverage resCoverage = new ResCoverage();
        resCoverage.setResource(resource.getIdentifier());
        resCoverage.setResType(resType);
        

        try {
            List<ResCoverage> resCoverages = null;
            if(!CommonServiceHelper.isQuestionDb(resType)) {
                resCoverages = resCoverageRepository.getAllByExample(resCoverage);
            } else {
                resCoverages = resCoverage4QtiRepository.getAllByExample(resCoverage);
            }
            if(CollectionUtils.isNotEmpty(resCoverages)) {
                for(ResCoverage coverage:resCoverages) {
                    if(CoverageConstant.STRATEGY_OWNER.equals(coverage.getStrategy())
                            && CoverageConstant.TARGET_TYPE_ORG.equals(coverage.getTargetType()) 
                            && CoverageConstant.ORG_CODE_ND.equals(coverage.getTarget())) {
                        if(resource.getLifeCycle()==null || !ONLINE_STATUS.equals(resource.getLifeCycle().getStatus())) {
                            return false;
                        }
                    }
                }
            }
        } catch (EspStoreException e) {
            LOG.error("预打包，查询资源覆盖范围失败：",e);
        }
        
        
        return true;
    }
}


