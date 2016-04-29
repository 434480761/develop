/**   
 * @Title: ResourceRelationStatistics.java 
 * @Package: com.nd.esp.store.common.entity 
 * @Description: TODO
 * @author Rainy(yang.lin)  
 * @date 2015年5月7日 下午6:55:55 
 * @version 1.3.1 
 */

package nd.esp.service.lifecycle.repository.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @Description
 * @author Rainy(yang.lin)
 * @date 2015年5月7日 下午6:55:55
 * @version V1.0
 */

public class ResourceRelationStatistics {
	private String identifier;
	@JsonProperty("resource_type")
	private String resourceType;
	private int count;
	private List<Item> items;


	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public List<Item> getItems() {
		return items;
	}

	public void setItems(List<Item> items) {
		this.items = items;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	@Override
	public String toString() {
		return "ResourceRelationStatistics [identifier=" + identifier
				+ ", resourceType=" + resourceType + ", count=" + count
				+ ", items=" + items + "]";
	}
}

