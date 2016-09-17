
/**   
 * @Title: Contributes.java 
 * @Package: com.nd.esp.repository.model 
 * @Description: TODO
 * @author Rainy(yang.lin)  
 * @date 2015年7月7日 上午11:52:24 
 * @version 1.3.1 
 */


package nd.esp.service.lifecycle.repository.model;


import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.repository.index.NoIndexBean;

/** 
 * @Description 
 * @author Rainy(yang.lin)  
 * @date 2015年7月7日 上午11:52:24 
 * @version V1.0
 */
@Entity
@Table(name = "contributes")
@NoIndexBean
public class Contribute  extends EspEntity{

	
	/** @Fields serialVersionUID: */
	  	
	private static final long serialVersionUID = 9187853804249214956L;

	@Column( name = "target_name")
	private String targetName;
	
	@Column( name = "target_type")
	private String targetType;
	
	@Column( name = "target_id")
	private String targetId;
	
	@Column( name = "contribute_time")
	private Timestamp contributeTime; 
	
	private String message;
	
	@Column( name = "life_status")
	private String lifeStatus;
	
	private Float process;
	
	@Column( name = "role_id")
	private String roleId;
	
	@Column( name = "role_name")
	private String roleName;
	
	private String resource;
	
	@Column( name = "res_type")
	private String resType;

	public String getTargetName() {
		return targetName;
	}

	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}

	public String getTargetType() {
		return targetType;
	}

	public void setTargetType(String targetType) {
		this.targetType = targetType;
	}

	public String getTargetId() {
		return targetId;
	}

	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}

	public Timestamp getContributeTime() {
		return contributeTime;
	}

	public void setContributeTime(Timestamp contributeTime) {
		this.contributeTime = contributeTime;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getLifeStatus() {
		return lifeStatus;
	}

	public void setLifeStatus(String lifeStatus) {
		this.lifeStatus = lifeStatus;
	}

	public Float getProcess() {
		return process;
	}

	public void setProcess(Float process) {
		this.process = process;
	}

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}
	
	
	@Override
	public IndexSourceType getIndexType() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getResType() {
		return resType;
	}

	public void setResType(String resType) {
		this.resType = resType;
	}
}
