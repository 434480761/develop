
/**   
 * @Title: Item.java 
 * @Package: com.nd.esp.store.common 
 * @Description: TODO
 * @author Rainy(yang.lin)  
 * @date 2015年5月7日 下午8:34:27 
 * @version 1.3.1 
 */


package nd.esp.service.lifecycle.repository.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/** 
 * @Description 
 * @author Rainy(yang.lin)  
 * @date 2015年5月7日 下午8:34:27 
 * @version V1.0
 */

public class Item {
	@JsonProperty("resource_type")
	String resourceType;
	int count;


	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	@Override
	public String toString() {
		return "Item [resourceType=" + resourceType + ", count=" + count + "]";
	}
}
