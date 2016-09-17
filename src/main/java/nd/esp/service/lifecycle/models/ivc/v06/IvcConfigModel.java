package nd.esp.service.lifecycle.models.ivc.v06;

import java.util.List;

/**
 * 访问控制策略model
 * @author xuzy
 *
 */
public class IvcConfigModel {
	//可访问的url集合
	private List<IvcUrlModel> includeUrlList;
	//无权限访问的url集合
	private List<IvcUrlModel> excludeUrlList;
	private List<String> globalIps;
	private IvcLoadModel globalLoad;
	private List<String> globalCoverage;
	private IvcGlobalCategoryModel globalCategory;
	
	public List<IvcUrlModel> getIncludeUrlList() {
		return includeUrlList;
	}
	public void setIncludeUrlList(List<IvcUrlModel> includeUrlList) {
		this.includeUrlList = includeUrlList;
	}
	public List<IvcUrlModel> getExcludeUrlList() {
		return excludeUrlList;
	}
	public void setExcludeUrlList(List<IvcUrlModel> excludeUrlList) {
		this.excludeUrlList = excludeUrlList;
	}
	public List<String> getGlobalIps() {
		return globalIps;
	}
	public void setGlobalIps(List<String> globalIps) {
		this.globalIps = globalIps;
	}
	public IvcLoadModel getGlobalLoad() {
		return globalLoad;
	}
	public void setGlobalLoad(IvcLoadModel globalLoad) {
		this.globalLoad = globalLoad;
	}
	public List<String> getGlobalCoverage() {
		return globalCoverage;
	}
	public void setGlobalCoverage(List<String> globalCoverage) {
		this.globalCoverage = globalCoverage;
	}
	public IvcGlobalCategoryModel getGlobalCategory() {
		return globalCategory;
	}
	public void setGlobalCategory(IvcGlobalCategoryModel globalCategory) {
		this.globalCategory = globalCategory;
	}
	
}
