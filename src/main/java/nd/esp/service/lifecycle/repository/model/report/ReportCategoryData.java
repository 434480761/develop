package nd.esp.service.lifecycle.repository.model.report;

import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
@Entity
@Table(name="category_datas")
public class ReportCategoryData extends EspEntity {

	private static final long serialVersionUID = 1L;
	private String category;
	
	@Column(name="nd_code")
	private String ndCode;
	
	private String parent;
	
	@Column(name = "last_update")
	private BigDecimal lastUpdate;
	
	@Column(name = "create_time")
	private Timestamp createTime;
	
	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getNdCode() {
		return ndCode;
	}

	public void setNdCode(String ndCode) {
		this.ndCode = ndCode;
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public BigDecimal getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(BigDecimal lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	@Override
	public IndexSourceType getIndexType() {
		return null;
	}

}
