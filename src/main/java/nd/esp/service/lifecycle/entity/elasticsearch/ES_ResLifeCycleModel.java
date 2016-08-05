package nd.esp.service.lifecycle.entity.elasticsearch;

import java.util.Date;

public class ES_ResLifeCycleModel {
	/**
	 * 标识
	 */
	private String version;
	private String status;
	private boolean enable;
	// private String resContributes; // FIXME
	private String creator;
	private String publisher;
	private String provider;
	private String providerSource;
	private String providerMode;
	private Date createTime; // change to long
	private Date lastUpdate; // change to long

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

	// public String getResContributes() {
	// return resContributes;
	// }
	//
	// public void setResContributes(String resContributes) {
	// this.resContributes = resContributes;
	// }

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

	public Long getCreateTime() {
		if(createTime == null){
			return 0L;
		}
		return createTime.getTime();
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Long getLastUpdate() {
		if(lastUpdate == null){
			return 0L;
		}
		return lastUpdate.getTime();
	}

	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public String getProviderMode() {
		return providerMode;
	}

	public void setProviderMode(String providerMode) {
		this.providerMode = providerMode;
	}

}
