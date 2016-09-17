
/**   
 * @Title: ResRepository.java 
 * @Package: com.nd.esp.repository.model 
 * @Description: TODO
 * @author Rainy(yang.lin)  
 * @date 2015年7月17日 上午10:56:50 
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
 * @date 2015年7月17日 上午10:56:50 
 * @version V1.0
 */
@Entity
@Table( name = "res_repository_infos")
@NoIndexBean
public class ResRepoInfo extends EspEntity {

	@Column( name = "repository_name")
	private String repositoryName;
	
	@Column( name = "target_type")
	private String targetType;
	
	private String target;
	
	@Column( name = "repository_admin")
	private String repositoryAdmin;
	
	@Column( name = "repository_path")
	private String repositoryPath;
	
	private Boolean enable = true;
	
	@Column( name = "create_time")
	private Timestamp createTime;
	
	@Column( name = "status")
	private String status;
	/**
	 * Description 
	 * @return 
	 * @see com.nd.esp.repository.IndexMapper#getIndexType() 
	 */

	@Override
	public IndexSourceType getIndexType() {
		return null;
	}
	public String getRepositoryName() {
		return repositoryName;
	}
	public void setRepositoryName(String repositoryName) {
		this.repositoryName = repositoryName;
	}
	public String getTargetType() {
		return targetType;
	}
	public void setTargetType(String targetType) {
		this.targetType = targetType;
	}
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	public String getRepositoryAdmin() {
		return repositoryAdmin;
	}
	public void setRepositoryAdmin(String repositoryAdmin) {
		this.repositoryAdmin = repositoryAdmin;
	}
	public String getRepositoryPath() {
		return repositoryPath;
	}
	public void setRepositoryPath(String repositoryPath) {
		this.repositoryPath = repositoryPath;
	}
	public Boolean getEnable() {
		return enable;
	}
	public void setEnable(Boolean enable) {
		this.enable = enable;
	}
	public Timestamp getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}

}
