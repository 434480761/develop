
/**   
 * @Title: ResCoverageRepository.java 
 * @Package: com.nd.esp.repository.sdk 
 * @Description: TODO
 * @author Rainy(yang.lin)  
 * @date 2015年6月18日 上午10:21:18 
 * @version 1.3.1 
 */


package nd.esp.service.lifecycle.repository.sdk;

import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.model.TaskStatusInfo;

import org.springframework.data.jpa.repository.JpaRepository;

/** 
 * @Description 
 * @author Rainy(yang.lin)  
 * @date 2015年6月18日 上午10:21:18 
 * @version V1.0
 */

public interface TaskStatusInfoRepository extends ResourceRepository<TaskStatusInfo>,
JpaRepository<TaskStatusInfo, String> {

}
