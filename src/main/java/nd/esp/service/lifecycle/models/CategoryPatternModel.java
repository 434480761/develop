package nd.esp.service.lifecycle.models;


/**
 * 分类关系的模式。主要定义分类模式的顺序和关系，并且说明分类模式的应用场景和目的
 * @author linsm
 * @version 0.3
 * @created 20-4月-2015 15:26:21
 */
public class CategoryPatternModel {

	
	/**
	 * 描述
	 */
	private String description;
	/**
	 * 分类模式的主键
	 */
	private String identifier;
	/**
	 * 分类模式的名称
	 */
	private String title;
	/**
	 * 维度分类维度的目的
	 */
	private String purpose;
	/**
	 * 使用的场景
	 */
	private String scope;
	/**
	 * 模式的英文标识
	 */
	private String patternName;
	
	/**
	 * 维度路径 
	 * 比如：$O$S$E
	 */
	private String patternPath;
	
	/**
	 * 该模式包含的段数,多个数值用','分隔
	 * 如5,6
	 */
	private String segment;
	
	public String getPatternPath() {
        return patternPath;
    }
    public void setPatternPath(String patternPath) {
        this.patternPath = patternPath;
    }
    public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getPurpose() {
		return purpose;
	}
	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}
	public String getScope() {
		return scope;
	}
	public void setScope(String scope) {
		this.scope = scope;
	}
	public String getPatternName() {
		return patternName;
	}
	public void setPatternName(String patternName) {
		this.patternName = patternName;
	}
	public String getSegment() {
		return segment;
	}
	public void setSegment(String segment) {
		this.segment = segment;
	}



}