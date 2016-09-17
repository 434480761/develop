
/**   
 * @Title: NoIndexBean.java 
 * @Package: com.nd.esp.repository.index 
 * @Description: TODO
 * @author Rainy(yang.lin)  
 * @date 2015年6月24日 下午1:35:03 
 * @version 1.3.1 
 */


package nd.esp.service.lifecycle.repository.index;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 
 * @Description 
 * @author Rainy(yang.lin)  
 * @date 2015年6月24日 下午1:35:03 
 * @version V1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface NoIndexBean {

}
