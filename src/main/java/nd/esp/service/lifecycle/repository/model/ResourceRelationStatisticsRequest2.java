
/**   
 * @Title: ResourceRelationStatisticsRequest2.java 
 * @Package: com.nd.esp.repository.model 
 * @Description: TODO
 * @author Rainy(yang.lin)  
 * @date 2015年6月11日 上午10:23:16 
 * @version 1.3.1 
 */


package nd.esp.service.lifecycle.repository.model;

import java.util.List;

import nd.esp.service.lifecycle.repository.EspEntity;

/** 
 * @Description 
 * @author Rainy(yang.lin)  
 * @date 2015年6月11日 上午10:23:16 
 * @version V1.0
 */

public class ResourceRelationStatisticsRequest2 {
	/** 条件和连接实体. */
	private EspEntity entity;
	
	/** The target type. */
	private List<String> targetType;

	public EspEntity getEntity() {
		return entity;
	}

	public void setEntity(EspEntity entity) {
		this.entity = entity;
	}

	public List<String> getTargetType() {
		return targetType;
	}

	public void setTargetType(List<String> targetType) {
		this.targetType = targetType;
	}
	
	
}
