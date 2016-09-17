package nd.esp.service.lifecycle.educommon.vos;

import java.math.BigDecimal;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import nd.esp.service.lifecycle.vos.valid.CopyrightDefault;


/**
 * @author johnny
 * @version 1.0
 * @created 08-7月-2015 10:18:50
 */
public class ResRightViewModel {

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
	
	@Max(groups={CopyrightDefault.class},value=7258089600000l,message="{resourceViewModel.copyright.rightStartDate.maxValue.validmsg}")
	@Min(groups={CopyrightDefault.class},value=946656000000l,message="{resourceViewModel.copyright.rightStartDate.minValue.validmsg}")
	private BigDecimal rightStartDate;
	
	@Max(groups={CopyrightDefault.class},value=7258089600000l,message="{resourceViewModel.copyright.rightEndDate.maxValue.validmsg}")
	@Min(groups={CopyrightDefault.class},value=946656000000l,message="{resourceViewModel.copyright.rightEndDate.minValue.validmsg}")
	private BigDecimal rightEndDate;
	
	private Boolean hasRight = false;

	public ResRightViewModel(){

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