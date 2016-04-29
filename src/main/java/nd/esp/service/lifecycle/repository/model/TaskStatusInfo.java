
/**   
 * @Title: PackageStatus.java 
 * @Package: com.nd.esp.repository.model 
 * @Description: TODO
 * @author Rainy(yang.lin)  
 * @date 2015年7月2日 下午9:13:17 
 * @version 1.3.1 
 */


package nd.esp.service.lifecycle.repository.model;


import java.sql.Timestamp;

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
 * @date 2015年7月2日 下午9:13:17 
 * @version V1.0
 */
@NoIndexBean
@Entity
@Table( name ="task_status_infos")
@NamedQueries({
    @NamedQuery(name = "queryByTaskId", query = "SELECT t from TaskStatusInfo t where t.taskId=(:taskid)")
})
public class TaskStatusInfo extends EspEntity {
	
	@Column(name = "buss_id")
	private String bussId;
	
	@Column(name = "buss_type")
	private String bussType;
	
	@Column(name = "res_type")
	private String resType;
	
	@Column(name = "uuid")
	private String uuid;
	
	@Column(name = "task_id")
	private String taskId;
	
	private String status;
	
	@Column(name = "store_info")
	private String storeInfo;
	
	@Column(name = "err_msg")
	private String errMsg;
	
	@Column(name = "update_time")
	private Timestamp updateTime;
	
	@Column(name = "priority")
    private int priority;

	/**
	 * Description 
	 * @return 
	 * @see com.nd.esp.repository.IndexMapper#getIndexType() 
	 */ 
		
	@Override
	public IndexSourceType getIndexType() {
		return null;
	}

	public String getBussId() {
		return bussId;
	}

	public void setBussId(String bussId) {
		this.bussId = bussId;
	}

	public String getBussType() {
		return bussType;
	}

	public void setBussType(String bussType) {
		this.bussType = bussType;
	}

	public String getResType() {
		return resType;
	}

	public void setResType(String resType) {
		this.resType = resType;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStoreInfo() {
		return storeInfo;
	}

	public void setStoreInfo(String storeInfo) {
		this.storeInfo = storeInfo;
	}

	public String getErrMsg() {
		return errMsg;
	}

	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}

	public Timestamp getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}
	
	public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

}
