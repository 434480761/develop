package nd.esp.service.lifecycle.models;

import java.sql.Date;

public class TeacherOutputResource {
	
	 /**
     * 教师id
     */
    private String teacherId;
    /**
     * 教师名字
     */
    private String teacherName;
    /**
     * 学科cod
     */
    private String gradeCode;
    /**
     * 学科
     */
    private String grade;
    /**
     * 科目cod
     */
    private String subjectCode;
    /**
     * 科目名称
     */
    private String subject;
    /**
     *创建日期
     */
    private Date data;
    /**
     * 标签
     */
    
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
	public String getSubjectCode() {
		return subjectCode;
	}
	public void setSubjectCode(String subjectCode) {
		this.subjectCode = subjectCode;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public Date getData() {
		return data;
	}
	public void setData(Date data) {
		this.data = data;
	}

    
}
