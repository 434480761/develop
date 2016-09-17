package nd.esp.service.lifecycle.repository.model.icrs;

import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
/**
 * 智慧课堂的数据统计 仓储Model
 * @author xiezy
 * @date 2016年9月12日
 */
@Entity
@Table(name = "icrs_resource")
public class IcrsResource extends EspEntity {
	private static final long serialVersionUID = 7445220408935464116L;

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
	private String teachmaterialUuid;
	@Column(name = "chapter_uuid")
	private String chapterUuid;
	@Column(name = "grade_code")
	private String gradeCode;
	@Column(name = "subject_code")
	private String subjectCode;

	@Transient
	private Timestamp createTime;
	@Column(name = "create_time")
	private BigDecimal dbcreateTime;

	@Column(name = "create_date")
	private String createDate;
	@Column(name = "create_hour")
	private Integer createHour;

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

	public String getTeachmaterialUuid() {
		return teachmaterialUuid;
	}

	public void setTeachmaterialUuid(String teachmaterialUuid) {
		this.teachmaterialUuid = teachmaterialUuid;
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

	public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	public Integer getCreateHour() {
		return createHour;
	}

	public void setCreateHour(Integer createHour) {
		this.createHour = createHour;
	}

	public Timestamp getCreateTime() {
		if (this.dbcreateTime != null) {
			this.createTime = new Timestamp(dbcreateTime.longValue());
		}
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
		if (this.createTime != null) {
			this.dbcreateTime = new BigDecimal(createTime.getTime());
		}
	}

	public BigDecimal getDbcreateTime() {
		return dbcreateTime;
	}

	public void setDbcreateTime(BigDecimal dbcreateTime) {
		this.dbcreateTime = dbcreateTime;
	}

	@Override
	public IndexSourceType getIndexType() {
		return null;
	}
}
