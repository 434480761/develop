package nd.esp.service.lifecycle.vos.v06;
/**
 * 资源标签统计view model
 * @author xuzy
 *
 */
public class ResourceTagViewModel {
	private String resource;
	private String tag;
	private int count;
	public String getResource() {
		return resource;
	}
	public void setResource(String resource) {
		this.resource = resource;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	
}
