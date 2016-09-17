package nd.esp.service.lifecycle.vos.vrlife;

import java.util.List;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
/**
 * vrlife 资源状态审核中的tags model
 * @author xiezy
 * @date 2016年7月19日
 */
public class StatusReviewTags {
	/**
	 * 操作符.add,delete
	 */
	@NotBlank(message="{statusReviewTags.operation.notBlank.validmsg}")
	private String operation;
	/**
	 * 需要处理的标签
	 */
	@NotEmpty(message="{statusReviewTags.tags.notEmpty.validmsg}")
	private List<String> tags;
	
	public String getOperation() {
		return operation;
	}
	public void setOperation(String operation) {
		this.operation = operation;
	}
	public List<String> getTags() {
		return tags;
	}
	public void setTags(List<String> tags) {
		this.tags = tags;
	}
}
