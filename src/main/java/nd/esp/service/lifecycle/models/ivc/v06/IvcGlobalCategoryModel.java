package nd.esp.service.lifecycle.models.ivc.v06;

import java.util.List;

/**
 * 全局分类约束
 * @author xuzy
 *
 */
public class IvcGlobalCategoryModel {
	//对资源类型的约束(包含)
	private List<String> includeResType;
	//对其他分类类型的约束(包含)
	private List<String> includeOtherType;
	//对资源类型的约束(排除)
	private List<String> excludeResType;
	//对其他分类类型的约束(排除)
	private List<String> excludeOtherType;
	
	public List<String> getIncludeResType() {
		return includeResType;
	}
	public void setIncludeResType(List<String> includeResType) {
		this.includeResType = includeResType;
	}
	public List<String> getIncludeOtherType() {
		return includeOtherType;
	}
	public void setIncludeOtherType(List<String> includeOtherType) {
		this.includeOtherType = includeOtherType;
	}
	public List<String> getExcludeResType() {
		return excludeResType;
	}
	public void setExcludeResType(List<String> excludeResType) {
		this.excludeResType = excludeResType;
	}
	public List<String> getExcludeOtherType() {
		return excludeOtherType;
	}
	public void setExcludeOtherType(List<String> excludeOtherType) {
		this.excludeOtherType = excludeOtherType;
	}
	
}
