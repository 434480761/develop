package nd.esp.service.lifecycle.models.ivc.v06;

import java.util.List;
/**
 * 映射地址
 * @author xuzy
 *
 */
public class IvcUrlModel {
	//接口指定的方法
	private List<String> method;
	//URL的负载设置
	private IvcLoadModel load;
	//URL值
	private String url;
	
	public List<String> getMethod() {
		return method;
	}
	public void setMethod(List<String> method) {
		this.method = method;
	}
	public IvcLoadModel getLoad() {
		return load;
	}
	public void setLoad(IvcLoadModel load) {
		this.load = load;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	
}
