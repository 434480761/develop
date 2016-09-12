package nd.esp.service.lifecycle.repository.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import nd.esp.service.lifecycle.repository.DataConverter;


@Entity
@Table(name = "icrs_resource")
@NamedQueries({
    @NamedQuery(name="queryIcrsByteacherId",query="select i from icrs_resource i where e.teacher_id=:teacher_id")		   
    
})
public class Icrs{
	
	@Column(name="identifier")
	protected String identifier;
	
	@Column(name="res_uuid")
	protected String resUuid;
	
	@Column(name="res_type")
	protected String resType;
	
	@Column(name="school_id")
	protected String schoolId;
	
	@Column(name="teacher_id")
	protected String teacherId;
	
	@Column(name="teacher_name")
	protected String teacherName;
	
	@Column(name="teachmaterial_uuid")
	protected String teachMaterialUuid;
	
	@Column(name="chapter_uuid")
	protected String chapterUuid;
	
	@Column(name="grade_code")
	protected String gradeCode;
	
	@Column(name="grade")
	protected String grade;
	
	@Column(name="subject_code")
	protected String subjectcode;
	
	@Column(name="subject")
	protected String subject;
	
	@Column(name="create_time")
	protected BigDecimal createTime;
	
	@Column(name="create_date")
	protected Date createDate;
	
	@Column(name="create_hour")
	protected Integer createHour;

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getResUuid() {
		return resUuid;
	}

	public void setResUuid(String resUuid) {
		this.resUuid = resUuid;
	}

	public String getResType() {
		return resType;
	}

	public void setResType(String resType) {
		this.resType = resType;
	}

	public String getSchoolId() {
		return schoolId;
	}

	public void setSchoolId(String schoolId) {
		this.schoolId = schoolId;
	}

	public String getTeacherId() {
		return teacherId;
	}

	public void setTeacherId(String teacherId) {
		this.teacherId = teacherId;
	}

	public String getTeacherName() {
		return teacherName;
	}

	public void setTeacherName(String teacherName) {
		this.teacherName = teacherName;
	}

	public String getTeachMaterialUuid() {
		return teachMaterialUuid;
	}

	public void setTeachMaterialUuid(String teachMaterialUuid) {
		this.teachMaterialUuid = teachMaterialUuid;
	}

	public String getChapterUuid() {
		return chapterUuid;
	}

	public void setChapterUuid(String chapterUuid) {
		this.chapterUuid = chapterUuid;
	}

	public String getGradeCode() {
		return gradeCode;
	}

	public void setGradeCode(String gradeCode) {
		this.gradeCode = gradeCode;
	}

	public String getGrade() {
		return grade;
	}

	public void setGrade(String grade) {
		this.grade = grade;
	}

	public String getSubjectcode() {
		return subjectcode;
	}

	public void setSubjectcode(String subjectcode) {
		this.subjectcode = subjectcode;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public BigDecimal getCreateTime() {
		return createTime;
	}

	public void setCreateTime(BigDecimal createTime) {
		this.createTime = createTime;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Integer getCreateHour() {
		return createHour;
	}

	public void setCreateHour(Integer createHour) {
		this.createHour = createHour;
	}
	
		
}
