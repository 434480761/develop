package nd.esp.service.lifecycle.educommon.support.param;

import java.util.List;
import java.util.Set;

import nd.esp.service.lifecycle.educommon.support.QueryType;

/**
 * 通用查询参数Model,与Controller入参资源类型一致
 * @author xiezy
 * @date 2016年7月25日
 */
public class CommonQueryParamViewModel {
	private String resType;//资源类型
	private String resCode;//资源类型Code,用于支持多种资源查询
	private String includes;
	private Set<String> categories;//维度分类
	private Set<String> categoryExclude;//排除的维度分类
	private Set<String> relations;//关系
	private Set<String> coverages;//覆盖范围
	private List<String> props;
	private List<String> orderBy;//排序
	private String reverse;//反转
	private Boolean printable;
	private String printableKey;
	private String statisticsType;
	private String statisticsPlatform;
	private boolean forceStatus;
	private List<String> tags;
	private boolean showVersion;
	private String words;//关键字
	private String limit;//分页参数
	private boolean isNotManagement;//判断是否是管理端的查询
	private QueryType queryType;
	
	public String getResType() {
		return resType;
	}
	public void setResType(String resType) {
		this.resType = resType;
	}
	public String getResCode() {
		return resCode;
	}
	public void setResCode(String resCode) {
		this.resCode = resCode;
	}
	public String getIncludes() {
		return includes;
	}
	public void setIncludes(String includes) {
		this.includes = includes;
	}
	public Set<String> getCategories() {
		return categories;
	}
	public void setCategories(Set<String> categories) {
		this.categories = categories;
	}
	public Set<String> getCategoryExclude() {
		return categoryExclude;
	}
	public void setCategoryExclude(Set<String> categoryExclude) {
		this.categoryExclude = categoryExclude;
	}
	public Set<String> getRelations() {
		return relations;
	}
	public void setRelations(Set<String> relations) {
		this.relations = relations;
	}
	public Set<String> getCoverages() {
		return coverages;
	}
	public void setCoverages(Set<String> coverages) {
		this.coverages = coverages;
	}
	public List<String> getProps() {
		return props;
	}
	public void setProps(List<String> props) {
		this.props = props;
	}
	public List<String> getOrderBy() {
		return orderBy;
	}
	public void setOrderBy(List<String> orderBy) {
		this.orderBy = orderBy;
	}
	public String getReverse() {
		return reverse;
	}
	public void setReverse(String reverse) {
		this.reverse = reverse;
	}
	public Boolean getPrintable() {
		return printable;
	}
	public void setPrintable(Boolean printable) {
		this.printable = printable;
	}
	public String getPrintableKey() {
		return printableKey;
	}
	public void setPrintableKey(String printableKey) {
		this.printableKey = printableKey;
	}
	public String getStatisticsType() {
		return statisticsType;
	}
	public void setStatisticsType(String statisticsType) {
		this.statisticsType = statisticsType;
	}
	public String getStatisticsPlatform() {
		return statisticsPlatform;
	}
	public void setStatisticsPlatform(String statisticsPlatform) {
		this.statisticsPlatform = statisticsPlatform;
	}
	public boolean isForceStatus() {
		return forceStatus;
	}
	public void setForceStatus(boolean forceStatus) {
		this.forceStatus = forceStatus;
	}
	public List<String> getTags() {
		return tags;
	}
	public void setTags(List<String> tags) {
		this.tags = tags;
	}
	public boolean isShowVersion() {
		return showVersion;
	}
	public void setShowVersion(boolean showVersion) {
		this.showVersion = showVersion;
	}
	public String getWords() {
		return words;
	}
	public void setWords(String words) {
		this.words = words;
	}
	public String getLimit() {
		return limit;
	}
	public void setLimit(String limit) {
		this.limit = limit;
	}
	public boolean isNotManagement() {
		return isNotManagement;
	}
	public void setNotManagement(boolean isNotManagement) {
		this.isNotManagement = isNotManagement;
	}
	public QueryType getQueryType() {
		return queryType;
	}
	public void setQueryType(QueryType queryType) {
		this.queryType = queryType;
	}
}
