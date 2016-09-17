package nd.esp.service.lifecycle.support.busi;

import java.util.Map;

import nd.esp.service.lifecycle.educommon.vos.ResTechInfoViewModel;
import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.StringUtils;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @title 对象属性校验工具类
 * @desc
 * @atuh lwx
 * @createtime on 2015年8月17日 下午11:19:39
 */
public class ModelPropertiesValidUitl {
	private static final Logger LOG = LoggerFactory.getLogger(ModelPropertiesValidUitl.class);
    
    /**	
     * @desc:校验资源href属性的合法性  
     * <p>这里暂时没有校验href的格式是否合法</p>
     * @createtime: 2015年8月17日 
     * @author: liuwx 
     * @param resource
     */
    public static void verificationHref(ResourceViewModel resource) {
        
        Assert.assertNotNull("资源视图对象不能为空", resource);
        if(null!=resource.getLifeCycle()){
       
            Map<String,? extends ResTechInfoViewModel> techInfoMap = resource.getTechInfo();
            if(CollectionUtils.isEmpty(techInfoMap)){
                LOG.error("teachinfo属性不能为空");
                throw new IllegalArgumentException("技术属性不能为空");
            }
            if(!techInfoMap.containsKey("source") ){
                    
                LOG.error("teachinfo属性中的source为空");
                throw new IllegalArgumentException("teachinfo中的source不能为空"); 
            }
            if( StringUtils.isEmpty(techInfoMap.get("source").getLocation())){
                LOG.error("teachinfo属性中的source对应的location不能为空");
                throw new IllegalArgumentException("teachinfo中的source对应的location为空"); 
            }
            //todo href格式校验
        }
         
    }

}
