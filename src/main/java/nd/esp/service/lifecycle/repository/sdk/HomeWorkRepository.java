
/**   
 * @Title: HomeWorkRepository.java 
 * @Package: com.nd.esp.repository.sdk 
 * @Description: TODO
 * @author Rainy(yang.lin)  
 * @date 2015年6月23日 下午3:03:16 
 * @version 1.3.1 
 */


package nd.esp.service.lifecycle.repository.sdk;

import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.model.HomeWork;

import org.springframework.data.jpa.repository.JpaRepository;

/** 
 * @Description 
 * @author Rainy(yang.lin)  
 * @date 2015年6月23日 下午3:03:16 
 * @version V1.0
 */

public interface HomeWorkRepository extends ResourceRepository<HomeWork>,JpaRepository<HomeWork, String>{

}
