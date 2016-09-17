
/**   
 * @Title: LessonPlans.java 
 * @Package: com.nd.esp.repository.model 
 * @Description: TODO
 * @author Rainy(yang.lin)  
 * @date 2015年6月19日 上午10:40:01 
 * @version 1.3.1 
 */


package nd.esp.service.lifecycle.repository.model;

import javax.persistence.Entity;
import javax.persistence.Table;

import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;

/** 
 * @Description 
 * @author Rainy(yang.lin)  
 * @date 2015年6月19日 上午10:40:01 
 * @version V1.0
 */
@Entity
@Table(name = "learning_plans")
public class LearningPlan extends Education {

	/**
	 * Description 
	 * @return 
	 * @see com.nd.esp.repository.IndexMapper#getIndexType() 
	 */

	@Override
	public IndexSourceType getIndexType() {
		this.setPrimaryCategory(IndexSourceType.LearningPlansType.getName());
		return IndexSourceType.LearningPlansType;
	}

}
