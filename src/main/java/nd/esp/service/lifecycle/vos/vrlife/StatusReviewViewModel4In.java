package nd.esp.service.lifecycle.vos.vrlife;

import java.util.List;

import javax.validation.Valid;

import org.hibernate.validator.constraints.NotBlank;
/**
 * vrlife 资源审核入参model
 * @author xiezy
 * @date 2016年7月19日
 */
public class StatusReviewViewModel4In {
	/**
	 * 资源id
	 */
	private String identifier;
	/**
	 * 资源类型
	 */
	private String resType;
	/**
	 * 资源审核后的状态
	 */
	@NotBlank(message="{statusReviewViewModel4In.status.notBlank.validmsg}")
	private String status;
	/**
	 * 资源审核人
	 */
	@NotBlank(message="{statusReviewViewModel4In.reviewPerson.notBlank.validmsg}")
	private String reviewPerson;
	/**
	 * 资源标签
	 */
	@Valid
	private List<StatusReviewTags> tags;
	
	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	public String getResType() {
		return resType;
	}
	public void setResType(String resType) {
		this.resType = resType;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public List<StatusReviewTags> getTags() {
		return tags;
	}
	public void setTags(List<StatusReviewTags> tags) {
		this.tags = tags;
	}
	public String getReviewPerson() {
		return reviewPerson;
	}
	public void setReviewPerson(String reviewPerson) {
		this.reviewPerson = reviewPerson;
	}
}
