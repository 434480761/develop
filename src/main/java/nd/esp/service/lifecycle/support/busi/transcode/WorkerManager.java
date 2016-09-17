package nd.esp.service.lifecycle.support.busi.transcode;

import nd.esp.service.lifecycle.educommon.vos.ResClassificationViewModel;
import nd.esp.service.lifecycle.educommon.vos.ResLifeCycleViewModel;
import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;
import nd.esp.service.lifecycle.entity.WorkerParam;
import nd.esp.service.lifecycle.support.busi.ModelPropertiesValidUitl;
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
 * @title 调度workder管理类
 * @Desc
 * @create 2015年10月13日 17:15:40
 * @see WorkerParam
 * */
public class WorkerManager {

    private final static Logger LOG = LoggerFactory.getLogger(WorkerManager.class);

    private final static String COURSEWARE_TEMPLATE_CODE="$RA0501";
    private final static String ASSET_VIDEO_CODE="$RA0103";




    /**
     * 获取素材转码参数
     * @param resource
     * @param resType
     * @return
     */
    public static WorkerParam getAssetTransCodeParam(ResourceViewModel resource, String resType) {

        return getWorkParam(resource, resType, new AssetTransCodeParamStrategy());

    }

    /**
     * 获取默认worker的转码参数
     * @param resource 暂时未使用
     * @param resType
     * @return
     */
    public static WorkerParam getDefaultWorkParam(ResourceViewModel resource, String resType) {

        return getWorkParam(resource, resType, new DefaultTransCodeParamStrategy());

    }


    /**
     *
     * 自定义获取worker的参数
     * @param resource
     * @param resType
     * @param strategy
     * @return
     */
    public static WorkerParam getWorkParam(Object resource, String resType, TransCodeParamStrategy strategy) {


        if (null != strategy) {

            return strategy.getParam(resource, resType);
        }

        return null;
    }

  










    interface TransCodeParamStrategy{

        public WorkerParam getParam(Object obj,String resType);
    }


    static class AssetTransCodeParamStrategy implements  TransCodeParamStrategy{

        @Override
        public WorkerParam getParam(Object obj,String resType) {

            Assert.assertNotNull("资源视图对象不能为空", obj);
            ResourceViewModel resource=null;
            //todo
            if(obj instanceof ResourceViewModel){
                resource=(ResourceViewModel)obj;
                Map<String, List<? extends ResClassificationViewModel>> models= resource.getCategories();
                if(CollectionUtils.isNotEmpty(models)){
                    List<? extends ResClassificationViewModel> list = models.get("assets_type");
                    if(CollectionUtils.isNotEmpty(list)){
                        for( ResClassificationViewModel resClassificationViewModel:list){
                            if(null!=resClassificationViewModel){
                                String code=  resClassificationViewModel.getTaxoncode();
                                if(COURSEWARE_TEMPLATE_CODE.equals(code)){
                                    return WorkerParam.createTranscodeParam("coursewares");//触发课件模板转码
                                }
//                              else if (ASSET_VIDEO_CODE.equals(code)){// 判断是否是音频
//                                  return WorkerParam.createVideoTranscodeParam();//触发视频转码
//                              }
                            }
                        }

                    }
                }
            }

            return null;
        }
    }




    /**
     * 默认转码参数策略
     *
     */
    static class DefaultTransCodeParamStrategy implements  TransCodeParamStrategy{

        @Override
        public WorkerParam getParam(Object obj,String resType) {
            Assert.assertNotNull("获取转码参数对象参数不能为空", resType);
            return  WorkerParam.createTranscodeParam(resType);
        }
    }




    /**
     * 生成视频转码队列所需要参数
     * @return
     * @since 
     */
    public static WorkerParam createVideoWorkerParam() {
        // TODO Auto-generated method stub
        return null;
    }





}

