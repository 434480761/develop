/**   
 * @Title: StoreInfo.java 
 * @Package: com.nd.esp.repository.model 
 * @Description: TODO
 * @author Rainy(yang.lin)  
 * @date 2015年7月1日 下午4:38:37 
 * @version 1.3.1 
 */

package nd.esp.service.lifecycle.repository.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.repository.index.NoIndexBean;

/**
 * @Description
 * @author Rainy(yang.lin)
 * @date 2015年7月1日 下午4:38:37
 * @version V1.0
 */
@Entity
@Table(name = "tech_infos")
@NoIndexBean
@NamedQueries({
    @NamedQuery(name = "deleteTechInfoByResource", query = "delete from TechInfo ti where ti.resource=:resourceId"),
    @NamedQuery(name = "commonQueryGetTechInfos", query = "SELECT ti from TechInfo ti where ti.resType IN (:rts) AND ti.resource IN  (:sids)")
})
public class TechInfo extends EspEntity {

	/**
	 * Description
	 * 
	 * @return
	 * @see com.nd.esp.repository.IndexMapper#getIndexType()
	 */

	@Override
	public IndexSourceType getIndexType() {
		return null;
	}

	private String format;

	private Long size;

	private String md5;

	private String location;
	
	@Column(name="resource")
	private String resource;
	
	@Column(name="secure_key")
	private String secureKey;
	
	@Column(name = "res_type")
	private String resType;

	private String entry;
	@Column(length = 60000)
	private String requirements;
	
	@Column(name="printable")
	private Boolean printable;

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getRequirements() {
		return requirements;
	}

	public void setRequirements(String requirements) {
		this.requirements = requirements;
	}

	public String getEntry() {
		return entry;
	}

	public void setEntry(String entry) {
		this.entry = entry;
	}

	public String getResType() {
		return resType;
	}

	public void setResType(String resType) {
		this.resType = resType;
	}

	public Long getSize() {
		return size;
	}

	public void setSize(Long size) {
		this.size = size;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
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
