package nd.esp.service.lifecycle.models.v06;

import java.util.Date;

import nd.esp.service.lifecycle.educommon.models.ResourceModel;

public class KnowledgeBaseModel extends ResourceModel{
	public String identifier;
	public String kpid;
	public String knid;
	public String kcid;
	public String kcCode;
	public String title;
	public String description;
	public String creator;
	public Date createTime;
	public String kcName;
	public String kpName;
	
	public String getKcCode() {
		return kcCode;
	}
	public void setKcCode(String kcCode) {
		this.kcCode = kcCode;
	}
	public String getKcid() {
		return kcid;
	}
	public void setKcid(String kcid) {
		this.kcid = kcid;
	}
	public String getKcName() {
		return kcName;
	}
	public void setKcName(String kcName) {
		this.kcName = kcName;
	}
	public String getKpName() {
		return kpName;
	}
	public void setKpName(String kpName) {
		this.kpName = kpName;
	}
	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	public String getKpid() {
		return kpid;
	}
	public void setKpid(String kpid) {
		this.kpid = kpid;
	}
	public String getKnid() {
		return knid;
	}
	public void setKnid(String knid) {
		this.knid = knid;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getCreator() {
		return creator;
	}
	public void setCreator(String creator) {
		this.creator = creator;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
}
