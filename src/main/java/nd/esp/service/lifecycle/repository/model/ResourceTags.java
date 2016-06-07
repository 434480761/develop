package nd.esp.service.lifecycle.repository.model;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
/**
 * 资源标签统计表
 * @author xuzy
 *
 */
@Entity
@Table(name="resource_tags")
public class ResourceTags extends EspEntity {
	private static final long serialVersionUID = 1L;

	private String resource;
	
	private String tag;
	
	private int count;
	
	@Column(name = "create_time")
	protected BigDecimal ct;
	
	@Column(name = "last_update")
	protected BigDecimal lu;
	
	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public BigDecimal getCt() {
		return ct;
	}

	public void setCt(BigDecimal ct) {
		this.ct = ct;
	}

	public BigDecimal getLu() {
		return lu;
	}

	public void setLu(BigDecimal lu) {
		this.lu = lu;
	}

	@Override
	public IndexSourceType getIndexType() {
		return null;
	}

}
