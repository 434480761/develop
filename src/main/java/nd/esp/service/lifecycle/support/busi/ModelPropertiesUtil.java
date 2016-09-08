package nd.esp.service.lifecycle.support.busi;

import java.util.List;

import nd.esp.service.lifecycle.educommon.models.ResLifeCycleModel;
import nd.esp.service.lifecycle.educommon.models.ResTechInfoModel;
import nd.esp.service.lifecycle.educommon.models.ResourceModel;
import nd.esp.service.lifecycle.utils.StringUtils;

import org.junit.Assert;

/**
 * @title 模型对象属性获取工具类
 * @desc
 * @atuh lwx
 * @createtime on 2015年8月19日 下午8:08:27
 */
public class ModelPropertiesUtil {
    //获取源资源对象的属性key value
    public static final String SOURCE_VALUE = "source";
    
    public static String getStatus(  ResourceModel resourceModel){
        
        Assert.assertNotNull("资源视图对象不能为空", resourceModel);
        
        ResLifeCycleModel lifeCycleModel= resourceModel.getLifeCycle();
        
        Assert.assertNotNull("资源视图对象中的生命对象属性不能为空", lifeCycleModel);
        
        return lifeCycleModel.getStatus();
        
    }
    
    public static String getStatus( ResLifeCycleModel lifeCycleModel){
        
        Assert.assertNotNull("资源视图对象中的生命对象属性不能为空", lifeCycleModel);
        
        return lifeCycleModel.getStatus();
    }
    
    
    /**	
     * @desc:获取资源对象技术属性中键值为source的技术属性对象
     * @createtime: 2015年8月19日 
     * @author: liuwx 
     * @param resourceModel
     * @return
     */
    public static ResTechInfoModel getTechInfoInSource(ResourceModel resourceModel){
        return getAssignTechInfo(resourceModel,SOURCE_VALUE);
    }
    
    /**	
     * @desc:获取指定的资源对象中技术属性对象  
     * @createtime: 2015年8月19日 
     * @author: liuwx 
     * @param resourceModel
     * @param key
     * @return
     */
    public static ResTechInfoModel getAssignTechInfo(ResourceModel resourceModel,String key){
        if(StringUtils.isEmpty(key)){
            key = SOURCE_VALUE;
        }
        List<ResTechInfoModel>techInfoModels=  resourceModel.getTechInfoList();
        for(ResTechInfoModel techInfoModel:techInfoModels ){
            if(techInfoModel.getTitle().equals(key)){
                return techInfoModel;
            }
        }
        return null;
    }
}
