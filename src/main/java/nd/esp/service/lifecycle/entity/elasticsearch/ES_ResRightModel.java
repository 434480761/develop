package nd.esp.service.lifecycle.entity.elasticsearch;

import java.math.BigDecimal;

public class ES_ResRightModel {
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
	private boolean hasRight;

	public String getRight() {
		return right;
	}

	public void setRight(String right) {
		this.right = right;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
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

	public boolean isHasRight() {
		return hasRight;
	}

	public void setHasRight(boolean hasRight) {
		this.hasRight = hasRight;
	}
}
