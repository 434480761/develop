package nd.esp.service.lifecycle.educommon.support.param;

import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 * 通用查询参数Model,与Service和Dao的资源类型一致
 * @author xiezy
 * @date 2016年7月25日
 */
public class CommonQueryParamModel {
	private String resType;//资源类型
	private String resCode;//资源类型Code,用于支持多种资源查询
	private List<String> includes;
	private Set<String> categories;//维度分类
	private Set<String> categoryExclude;//排除的维度分类
	private List<Map<String,String>> relations;//关系
	private List<String> coverages;//覆盖范围
	private Map<String,Set<String>> props;
	private Map<String, String> orderBy;//排序
	private boolean reverse;//反转
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
	
	//TITAN_ES 专用
	private List<String> fieldsList;
	
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
	public List<String> getIncludes() {
		return includes;
	}
	public void setIncludes(List<String> includes) {
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
	public List<Map<String, String>> getRelations() {
		return relations;
	}
	public void setRelations(List<Map<String, String>> relations) {
		this.relations = relations;
	}
	public List<String> getCoverages() {
		return coverages;
	}
	public void setCoverages(List<String> coverages) {
		this.coverages = coverages;
	}
	public Map<String, Set<String>> getProps() {
		return props;
	}
	public void setProps(Map<String, Set<String>> props) {
		this.props = props;
	}
	public Map<String, String> getOrderBy() {
		return orderBy;
	}
	public void setOrderBy(Map<String, String> orderBy) {
		this.orderBy = orderBy;
	}
	public boolean isReverse() {
		return reverse;
	}
	public void setReverse(boolean reverse) {
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
	public List<String> getFieldsList() {
		return fieldsList;
	}
	public void setFieldsList(List<String> fieldsList) {
		this.fieldsList = fieldsList;
	}
}
