/**   
 * @Title: ResourcePreviewsRepository.java 
 * @Package: com.nd.esp.repository.sdk 
 * @author ql  
 * @date 2015年12月23日
 */


package nd.esp.service.lifecycle.repository.sdk;

import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.model.ResourcePreviews;

import org.springframework.data.jpa.repository.JpaRepository;

/** 
 * @Description 
 * @author Rainy(yang.lin)  
 * @date 2015年6月18日 上午10:21:18 
 * @version V1.0
 */

public interface ResourcePreviewsRepository extends ResourceRepository<ResourcePreviews>,
JpaRepository<ResourcePreviews, String> {

}
