package nd.esp.service.lifecycle.repository.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;

@Entity
@Table(name = "notify_backups")
public class NotifyModel extends EspEntity{
	
	private static final long serialVersionUID = 4174596589872290622L;
	/**
	 * 教学目标id
	 */
	@Column(name = "teaching_object_id")
	private String teachingObjectId;
	/**
	 * 对智能出题而已资源的状态
	 */
	private String status;
	/**
	 * 课时id
	 */
	@Column(name = "lesson_period_id")
	private String lessonPeriodId;
	/**
	 * 章节id
	 */
	@Column(name = "chapter_id")
	private String chapterId;
	
	public String getTeachingObjectId() {
		return teachingObjectId;
	}
	public void setTeachingObjectId(String teachingObjectId) {
		this.teachingObjectId = teachingObjectId;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getLessonPeriodId() {
		return lessonPeriodId;
	}
	public void setLessonPeriodId(String lessonPeriodId) {
		this.lessonPeriodId = lessonPeriodId;
	}
	public String getChapterId() {
		return chapterId;
	}
	public void setChapterId(String chapterId) {
		this.chapterId = chapterId;
	}
	
	@Override
	public IndexSourceType getIndexType() {
		return null;
	}
}
