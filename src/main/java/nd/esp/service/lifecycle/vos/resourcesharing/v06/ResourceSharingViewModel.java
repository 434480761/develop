package nd.esp.service.lifecycle.vos.resourcesharing.v06;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * 资源分享 viewModel
 * @author xiezy
 * @date 2016年8月29日
 */
@JsonInclude(Include.NON_NULL)
public class ResourceSharingViewModel {
	private String identifier;
	private String title;
	private String resource;
	private String resourceType;
	private String protectPasswd;
	
	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getResource() {
		return resource;
	}
	public void setResource(String resource) {
		this.resource = resource;
	}
	public String getResourceType() {
		return resourceType;
	}
	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}
	public String getProtectPasswd() {
		return protectPasswd;
	}
	public void setProtectPasswd(String protectPasswd) {
		this.protectPasswd = protectPasswd;
	}
}
