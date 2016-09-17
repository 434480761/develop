package nd.esp.service.lifecycle.repository.model;

import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import nd.esp.service.lifecycle.repository.DataConverter;
import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;

/**
 * 类描述:bean 创建人: 创建时间:2015-05-13 18:52:3
 * 
 * @version
 */

@Entity
@Table(name = "courseware_objects")
public class CoursewareObject extends Education {

	public static final String PROP_ANNOTATIONS = "annotations";
	public static final String PROP_OBJECTIVES = "objectives";

	/**
	* 
	*/
	@Column(name = "annotations")
	@DataConverter(target="annotations",type=Map.class)
	private String dbannotations;

	@Transient
	private Map<String, String> annotations;

	/**
	* 
	*/
	@Column(name = "objectives")
	@DataConverter(target="objectives",type=Map.class)
	private String dbobjectives;

	@Transient
	private Map<String, String> objectives;

	@Override
	public IndexSourceType getIndexType() {
		this.setPrimaryCategory(IndexSourceType.SourceCourseWareObjectType.getName());
		return IndexSourceType.SourceCourseWareObjectType;
	}

	public String getDbannotations() {
		return dbannotations;
	}

	public void setDbannotations(String dbannotations) {
		this.dbannotations = dbannotations;
	}

	public Map<String, String> getAnnotations() {
		return annotations;
	}

	public void setAnnotations(Map<String, String> annotations) {
		this.annotations = annotations;
	}

	public String getDbobjectives() {
		return dbobjectives;
	}

	public void setDbobjectives(String dbobjectives) {
		this.dbobjectives = dbobjectives;
	}

	public Map<String, String> getObjectives() {
		return objectives;
	}

	public void setObjectives(Map<String, String> objectives) {
		this.objectives = objectives;
	}

}