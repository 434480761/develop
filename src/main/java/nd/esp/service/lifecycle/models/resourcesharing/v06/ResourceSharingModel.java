package nd.esp.service.lifecycle.models.resourcesharing.v06;
/**
 * 资源分享 Model
 * @author xiezy
 * @date 2016年8月29日
 */
public class ResourceSharingModel {
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
