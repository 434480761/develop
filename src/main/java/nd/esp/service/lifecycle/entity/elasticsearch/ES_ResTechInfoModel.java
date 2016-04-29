package nd.esp.service.lifecycle.entity.elasticsearch;

import java.util.List;

public class ES_ResTechInfoModel {
	private String format;
	private long size;
	private String location;
	private List<ES_TechnologyRequirementModel> requirements;
	private String md5;
	/**
	 * 技术属性关联的资源对象
	 */
	private String title;
	private String entry;
	
	/**
	 * 密钥
	 */
	private String secureKey;
	
	private Boolean printable;

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public List<ES_TechnologyRequirementModel> getRequirements() {
		return requirements;
	}

	public void setRequirements(List<ES_TechnologyRequirementModel> requirements) {
		this.requirements = requirements;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getEntry() {
		return entry;
	}

	public void setEntry(String entry) {
		this.entry = entry;
	}

	public String getSecureKey() {
		return secureKey;
	}

	public void setSecureKey(String secureKey) {
		this.secureKey = secureKey;
	}

	public Boolean getPrintable() {
		return printable;
	}

	public void setPrintable(Boolean printable) {
		this.printable = printable;
	}

}
