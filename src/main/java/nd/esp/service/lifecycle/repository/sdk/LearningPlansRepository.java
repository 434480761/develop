
/**   
 * @Title: LessonPlansRepository.java 
 * @Package: com.nd.esp.repository.sdk 
 * @Description: TODO
 * @author Rainy(yang.lin)  
 * @date 2015年6月19日 上午11:05:34 
 * @version 1.3.1 
 */


package nd.esp.service.lifecycle.repository.sdk;

import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.model.LearningPlan;

import org.springframework.data.jpa.repository.JpaRepository;

/** 
 * @Description 
 * @author Rainy(yang.lin)  
 * @date 2015年6月19日 上午11:05:34 
 * @version V1.0
 */

public interface LearningPlansRepository extends ResourceRepository<LearningPlan>,
JpaRepository<LearningPlan, String>{

}
