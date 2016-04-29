package nd.esp.service.lifecycle.educommon.models;

import java.util.List;


/**
 * @author johnny
 * @version 1.0
 * @created 08-7月-2015 10:18:49
 */
public class ResTechInfoModel {

	/**
	 * 标识
	 */
	private String identifier;
	private String format;
	private long size;
	private String location;
	private List<TechnologyRequirementModel> requirements;
	private String md5;
	/**
	 * 技术属性关联的资源对象
	 */
	private ResourceModel resource;
	private String title;
	private String entry;
	/**
	 * 密钥
	 */
	private String secureKey;
	
	private Boolean printable;

	public ResTechInfoModel(){

	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

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

	public List<TechnologyRequirementModel> getRequirements() {
		return requirements;
	}

	public void setRequirements(List<TechnologyRequirementModel> requirements) {
		this.requirements = requirements;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	public ResourceModel getResource() {
		return resource;
	}

	public void setResource(ResourceModel resource) {
		this.resource = resource;
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