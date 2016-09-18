package nd.esp.service.lifecycle.support.icrs;
/**
 * 同步ICRS Model
 * @author xiezy
 * @date 2016年9月12日
 */
public class SyncIcrsModel {
	private String identifier;
	private Integer enable;
	private Long createTime;
	private String target;
	
	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	public Integer getEnable() {
		return enable;
	}
	public void setEnable(Integer enable) {
		this.enable = enable;
	}
	public Long getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
	}
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
}
