package nd.esp.service.lifecycle.repository.model;

import java.util.List;

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
@Table(name = "teaching_activities")
public class TeachingActivities extends Education {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	/**
	* 
	*/
	@Column(name = "lesson_objectives")
	@DataConverter(target="lessonObjectives",type=List.class)
	private String dblessonobjectives;


	@Transient
	private List<String> lessonObjectives;

	/**
	* 
	*/
	@Column(name = "typical_age_range")
	@DataConverter(target="typicalAgeRange",type=List.class)
	private String dbtypicalagerange;

	@Transient
	private List<String> typicalAgeRange;
	/**
	* 
	*/
	@Column(name = "typical_learning_time")
	private String typicalLearningTime;


	@Override
	public IndexSourceType getIndexType() {
		this.setPrimaryCategory(IndexSourceType.SourceCourseWareType.getName());
		return IndexSourceType.SourceCourseWareType;
	}

	public void setTypicalAgeRange(List<String> typicalagerange) {
		this.typicalAgeRange = typicalagerange;
	}

	public String getTypicalLearningTime() {
		return typicalLearningTime;
	}

	public void setTypicalLearningTime(String typicalLearningTime) {
		this.typicalLearningTime = typicalLearningTime;
	}

	public String getDblessonobjectives() {
		return dblessonobjectives;
	}

	public void setDblessonobjectives(String dblessonobjectives) {
		this.dblessonobjectives = dblessonobjectives;
	}


	public List<String> getLessonObjectives() {
		return lessonObjectives;
	}

	public void setLessonObjectives(List<String> lessonObjectives) {
		this.lessonObjectives = lessonObjectives;
	}

	public String getDbtypicalagerange() {
		return dbtypicalagerange;
	}

	public void setDbtypicalagerange(String dbtypicalagerange) {
		this.dbtypicalagerange = dbtypicalagerange;
	}

	public List<String> getTypicalAgeRange() {
		return typicalAgeRange;
	}

}