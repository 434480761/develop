package nd.esp.service.lifecycle.repository.model.report;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
@Entity
@Table(name="chapters")
public class ReportChapter extends EspEntity {
	private static final long serialVersionUID = 1L;

	@Column(name="create_time")
	private Timestamp createTime;
	
	@Column(name="last_update")
	private Timestamp lastUpdate;
	
	@Column(name = "parent")
	private String parent;
	
	@Column(name = "teaching_material")
	private String teachingMaterial;

	@Column(name="tree_left")
	private Integer left;
	
	@Column(name="tree_right")
	private Integer right;
	
	@Column(name="operation_flag")
	private String operationFlag;

	
	public String getOperationFlag() {
		return operationFlag;
	}


	public void setOperationFlag(String operationFlag) {
		this.operationFlag = operationFlag;
	}


	public Timestamp getCreateTime() {
		return createTime;
	}


	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}


	public Timestamp getLastUpdate() {
		return lastUpdate;
	}


	public void setLastUpdate(Timestamp lastUpdate) {
		this.lastUpdate = lastUpdate;
	}


	public String getParent() {
		return parent;
	}


	public void setParent(String parent) {
		this.parent = parent;
	}


	public String getTeachingMaterial() {
		return teachingMaterial;
	}


	public void setTeachingMaterial(String teachingMaterial) {
		this.teachingMaterial = teachingMaterial;
	}


	public Integer getLeft() {
		return left;
	}


	public void setLeft(Integer left) {
		this.left = left;
	}


	public Integer getRight() {
		return right;
	}


	public void setRight(Integer right) {
		this.right = right;
	}


	@Override
	public IndexSourceType getIndexType() {
		return null;
	}

}
