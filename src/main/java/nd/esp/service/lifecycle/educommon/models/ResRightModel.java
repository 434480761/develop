package nd.esp.service.lifecycle.educommon.models;

import java.math.BigDecimal;


/**
 * @author johnny
 * @version 1.0
 * @created 08-7月-2015 10:18:50
 */
public class ResRightModel {

	/**
	 * 版权信息
	 */
	private String right;
	/**
	 * 版权描述信息
	 */
	private String description;
	/**
	 * 作者信息
	 */
	private String author;
	/**
	 * 版权开始时间，值为时间戳（精确到毫秒）
	 */
	private BigDecimal rightStartDate;
	
	/**
	 * 版权结束时间，值为时间戳（精确到毫秒）
	 */
	private BigDecimal rightEndDate;
	
	/**
	 * 是否有版权
	 */
	private Boolean hasRight;
	
	public ResourceModel m_ResourceModel;

	public ResRightModel(){

	}

	public String getRight() {
		return right;
	}

	public void setRight(String right) {
		this.right = right;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public ResourceModel getM_ResourceModel() {
		return m_ResourceModel;
	}

	public void setM_ResourceModel(ResourceModel m_ResourceModel) {
		this.m_ResourceModel = m_ResourceModel;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BigDecimal getRightStartDate() {
		return rightStartDate;
	}

	public void setRightStartDate(BigDecimal rightStartDate) {
		this.rightStartDate = rightStartDate;
	}

	public BigDecimal getRightEndDate() {
		return rightEndDate;
	}

	public void setRightEndDate(BigDecimal rightEndDate) {
		this.rightEndDate = rightEndDate;
	}

	public Boolean getHasRight() {
		return hasRight;
	}

	public void setHasRight(Boolean hasRight) {
		this.hasRight = hasRight;
	}
}