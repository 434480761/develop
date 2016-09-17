package nd.esp.service.lifecycle.vos.vrlife;

import java.util.List;
/**
 * vrlife 资源状态审核转出Model
 * @author xiezy
 * @date 2016年7月19日
 */
public class StatusReviewViewModel4Out {
	private String identifier;
	private String status;
	private List<String> tags;
	
	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public List<String> getTags() {
		return tags;
	}
	public void setTags(List<String> tags) {
		this.tags = tags;
	}
}
