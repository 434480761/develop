/**   
 * @Title: ResourceRelationStatisticsRequest.java 
 * @Package: com.nd.esp.store.controller 
 * @Description: TODO
 * @author Rainy(yang.lin)  
 * @date 2015年5月7日 下午7:38:02 
 * @version 1.3.1 
 */

package nd.esp.service.lifecycle.repository.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

// TODO: Auto-generated Javadoc
/**
 * The Class ResourceRelationStatisticsRequest.
 *
 * @author Rainy(yang.lin)
 * @version V1.0
 * @Description 
 * @date 2015年5月7日 下午7:38:02
 */

public class ResourceRelationStatisticsRequest {
	
	/** The sources. */
	private List<String> sources;
	
	/** The group. */
	private String group;
	
	/** The target type. */
	@JsonProperty("target_type")
	private List<String> targetType;
	
	/** The categorys. */
	private List<String> categorys;

	/**
	 * Gets the sources.
	 *
	 * @return the sources
	 */
	public List<String> getSources() {
		return sources;
	}

	/**
	 * Sets the sources.
	 *
	 * @param sources the new sources
	 */
	public void setSources(List<String> sources) {
		this.sources = sources;
	}

	/**
	 * Gets the group.
	 *
	 * @return the group
	 */
	public String getGroup() {
		return group;
	}

	/**
	 * Sets the group.
	 *
	 * @param group the new group
	 */
	public void setGroup(String group) {
		this.group = group;
	}

	/**
	 * Gets the categorys.
	 *
	 * @return the categorys
	 */
	public List<String> getCategorys() {
		return categorys;
	}

	/**
	 * Sets the categorys.
	 *
	 * @param categorys the new categorys
	 */
	public void setCategorys(List<String> categorys) {
		this.categorys = categorys;
	}

	/**
	 * Gets the target type.
	 *
	 * @return the target type
	 */
	public List<String> getTargetType() {
		return targetType;
	}

	/**
	 * Sets the target type.
	 *
	 * @param targetType the new target type
	 */
	public void setTargetType(List<String> targetType) {
		this.targetType = targetType;
	}

}
