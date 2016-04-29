package nd.esp.service.lifecycle.support.busi.transcode;

import nd.esp.service.lifecycle.educommon.vos.ResClassificationViewModel;
import nd.esp.service.lifecycle.educommon.vos.ResCoverageViewModel;
import nd.esp.service.lifecycle.educommon.vos.ResLifeCycleViewModel;
import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;
import nd.esp.service.lifecycle.entity.WorkerParam;
import nd.esp.service.lifecycle.support.busi.ModelPropertiesValidUitl;
import nd.esp.service.lifecycle.support.busi.TransCodeUtil;
import nd.esp.service.lifecycle.support.enums.LifecycleStatus;
import nd.esp.service.lifecycle.utils.ArrayUtils;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * @author liuwx
 * @version 1.0
 * @title 转码触发管理类
 * @Desc
 * @create 2015年10月13日 17:15:40
 */
public class TransCodeManager {

    private final static Logger LOG = LoggerFactory.getLogger(TransCodeManager.class);

    private final static String COURSEWARE_TEMPLATE_CODE="$RA0501";




    /**
     * 是否允许转码(默认方式 通过状态判断)
     * @param resource
     * @param resType
     * @return
     */
    public static boolean canTransCode(ResourceViewModel resource, String resType) {

        return canTransCode(resource,resType,new DefaultTransCodeStrategy());

    }

    /**
     * 是否允许转码(通过分类维度来判断)
     * @param resource
     * @param resType
     * @return
     */
    @Deprecated
    public static boolean canTransCodeByCategory(ResourceViewModel resource, String resType) {

        return canTransCode(resource,resType,new CategoryTransCodeStrategy());

    }








    public static boolean canTransCode(ResourceViewModel resource, String resType, TransCodeStrategy strategy) {


        if (null != strategy) {

            return strategy.judge(resource, resType);
        }

        return false;
    }
    public static boolean canTransCode(ResourceViewModel resource, String resType, TransCodeStrategy []strategies) {

        boolean canTrigger=true;
        if(ArrayUtils.isNotEmpty(strategies)){
            for(TransCodeStrategy strategy:strategies){
                if (null != strategy) {
                    if(!(canTrigger= strategy.judge(resource, resType))){
                        break;
                    }
                }
            }
        }
        return canTrigger;
    }




    public static void main(String[] args) {

        TransCodeManager.canTransCode(null, "coursewares", new TransCodeStrategy() {
            @Override
            public boolean judge(Object obj, String resType) {
                return true;
            }
        });



    }


    interface TransCodeStrategy {

        public boolean judge(Object obj, String resType);

    }




    //提供已知模板策略实现


    /***
     *标准转码策略
     */
    static class DefaultTransCodeStrategy implements TransCodeStrategy{


        @Override
        public boolean judge(Object obj, String resType) {
            Assert.assertNotNull("资源视图对象不能为空", obj);

            ResourceViewModel resource=null;
            //todo
            if(obj instanceof ResourceViewModel){
                resource=(ResourceViewModel)obj;
                ResLifeCycleViewModel lifeCycleViewModel = resource.getLifeCycle();
                if (null != lifeCycleViewModel && LifecycleStatus.isNeedTranscode(lifeCycleViewModel.getStatus())) {
                    try {
                        ModelPropertiesValidUitl.verificationHref(resource);
                        return true;
                    } catch (Exception e) {
                        LOG.error(e.getMessage());
                    }

                }
            }
            
            return false;
        }
    }

    static  class SpeciceTransCodeStrategy implements TransCodeStrategy{


        @Override
        public boolean judge(Object obj, String resType) {
            return false;
        }
    }


    /**
     *
     * 分类维度策略(好像做错了,擦)
     */
    @Deprecated
    static  class CategoryTransCodeStrategy implements TransCodeStrategy{


        @Override
        public boolean judge(Object obj, String resType) {
            Assert.assertNotNull("资源视图对象不能为空", obj);
            ResourceViewModel resource=null;
            //todo
            if(obj instanceof ResourceViewModel){
                resource=(ResourceViewModel)obj;
                if("assets".equals(resType)){//暂时只支持素材资源通过分类维度进行触发转码
                    Map<String, List<? extends ResClassificationViewModel>> models= resource.getCategories();
                    if(CollectionUtils.isNotEmpty(models)){
                        List<? extends ResClassificationViewModel> list = models.get("assets_type");
                        if(CollectionUtils.isNotEmpty(list)){
                            for( ResClassificationViewModel resClassificationViewModel:list){
                                if(null!=resClassificationViewModel){
                                    String code=  resClassificationViewModel.getTaxoncode();
                                    if(COURSEWARE_TEMPLATE_CODE.equals(code)){
                                        return  true;
                                    }
                                }
                            }

                        }
                    }
                }
            }
            

            return false;
        }
    }






}

