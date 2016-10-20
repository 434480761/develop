package nd.esp.service.lifecycle.repository.model.categorysync;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;

@Entity
@Table(name = "category_sync_error")
public class CategorySyncError extends EspEntity {

	private static final long serialVersionUID = -2152680999236898673L;

	@Column(name = "sync_category")
	private String syncCategory;
	@Column(name = "res_type")
	private String resType;
	@Column(name = "resource")
	private String resource;
	@Column(name = "message")
	private String message;
	@Column(name = "code")
	private Integer code;

	public String getSyncCategory() {
		return syncCategory;
	}

	public void setSyncCategory(String syncCategory) {
		this.syncCategory = syncCategory;
	}

	public String getResType() {
		return resType;
	}

	public void setResType(String resType) {
		this.resType = resType;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	@Override
	public IndexSourceType getIndexType() {
		return null;
	}
}
