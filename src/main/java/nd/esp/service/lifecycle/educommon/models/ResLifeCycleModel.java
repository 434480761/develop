package nd.esp.service.lifecycle.educommon.models;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;


/**
 * @author johnny
 * @version 1.0
 * @created 08-7月-2015 10:18:49
 */
public class ResLifeCycleModel {

	/**
	 * 标识
	 */
	private String identifier;
	private String version;
	private String status;
	private boolean enable;
	private ResourceModel resource;
	private List<ResContributeModel> resContributes;
	private String creator;
	private String publisher;
	private String provider;
	private String providerSource;
	private Date createTime;
	private Date lastUpdate;

	public ResLifeCycleModel(){

	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public ResourceModel getResource() {
		return resource;
	}

	public void setResource(ResourceModel resource) {
		this.resource = resource;
	}

	public List<ResContributeModel> getResContributes() {
		return resContributes;
	}

	public void setResContributes(List<ResContributeModel> resContributes) {
		this.resContributes = resContributes;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getProviderSource() {
		return providerSource;
	}

	public void setProviderSource(String providerSource) {
		this.providerSource = providerSource;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
}