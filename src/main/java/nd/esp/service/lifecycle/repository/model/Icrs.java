package nd.esp.service.lifecycle.repository.model;

import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.ibm.icu.math.BigDecimal;

@Entity
@Table(name = "icrs_resource")
public class Icrs {

	@Column(name = "identifier")
	private String identifier;
	
	@Column(name = "res_uuid")
	private String resUuid;
	
	@Column(name = "res_type")
	private String resType;
	
	@Column(name = "school_id")
	private String schoolId;
	
	@Column(name = "teacher_id")
	private String teacherId;
	
	@Column(name = "teacher_name")
	private String teacherName;
	
	@Column(name = "teachmaterial_uuid")
	private String teachMaterialUuid;
	
	@Column(name = "chapter_uuid")
	private String chapterUuid;
	
	@Column(name = "grade_code")
	private String gradeCode;
	
//	@Column(name = "grade")
//	private String grade;
	
	@Column(name = "subject_code")
	private String subjectCode;
	
//	@Column(name = "subject")
//	private String subject;
	
	@Column(name = "create_time")
	private BigDecimal createTime;
	
	@Column(name = "create_date")
	private Date createDate;
	
	@Column(name = "create_hour")
	private Integer createHour;

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

	

	public String getSubjectCode() {
		return subjectCode;
	}

	public void setSubjectCode(String subjectCode) {
		this.subjectCode = subjectCode;
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
