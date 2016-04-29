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
@Table(name = "lessons")
public class Lesson extends Education {

	public static final String PROP_OUTLINE = "outline";
	public static final String PROP_RESOURCES = "resources";

	/**
	* 
	*/
	@Column(name = "outline")
	@DataConverter(target = "outline", type = Map.class)
	private String dboutline;

	@Transient
	private Map<String, String> outline;

	/**
	* 
	*/
	@Column(name = "resources")
	@DataConverter(target = "resources", type = Map.class)
	private String dbresources;

	@Transient
	private Map<String, String> resources;

	/**
	* 
	*/
	@Column(name = "teaching_material")
	private String teachingMaterial;
	
	private String classification;

	public String getClassification() {
		return classification;
	}

	public void setClassification(String classification) {
		this.classification = classification;
	}

	@Override
	public IndexSourceType getIndexType() {
		this.setPrimaryCategory(IndexSourceType.LessonType.getName());
		return IndexSourceType.LessonType;
	}

	public String getTeachingMaterial() {
		return teachingMaterial;
	}

	public void setTeachingMaterial(String teachingMaterial) {
		this.teachingMaterial = teachingMaterial;
	}

	public String getDboutline() {
		return dboutline;
	}

	public void setDboutline(String dboutline) {
		this.dboutline = dboutline;
	}

	public Map<String, String> getOutline() {
		return outline;
	}

	public void setOutline(Map<String, String> outline) {
		this.outline = outline;
	}

	public String getDbresources() {
		return dbresources;
	}

	public void setDbresources(String dbresources) {
		this.dbresources = dbresources;
	}

	public Map<String, String> getResources() {
		return resources;
	}

	public void setResources(Map<String, String> resources) {
		this.resources = resources;
	}

}