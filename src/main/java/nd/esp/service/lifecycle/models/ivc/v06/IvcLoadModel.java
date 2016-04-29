package nd.esp.service.lifecycle.models.ivc.v06;
/**
 * 负载设置
 * @author xuzy
 *
 */
public class IvcLoadModel {
	//每秒请求数量
	private Long maxRps;
	//每个请求的数据请求量，只是针对于查询接口
	private Long maxDpr;
	
	public Long getMaxRps() {
		return maxRps;
	}
	public void setMaxRps(Long maxRps) {
		this.maxRps = maxRps;
	}
	public Long getMaxDpr() {
		return maxDpr;
	}
	public void setMaxDpr(Long maxDpr) {
		this.maxDpr = maxDpr;
	}
	
	
}
