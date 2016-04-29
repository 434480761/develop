package nd.esp.service.lifecycle.educommon.models;

import java.util.Date;


/**
 * @author johnny
 * @version 1.0
 * @created 08-7月-2015 10:18:49
 */
public class ResAnnotationModel<T> {

	private String identifier;
	private String userId;
	private String userName;
	private Date annotationDate;
	/**
	 * 批注内容
	 */
	private String annotation;
	private T source;
	private T target;
	/**
	 * module的id
	 */
	private String moduleId;
	private int state;

	public ResAnnotationModel(){

	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Date getAnnotationDate() {
		return annotationDate;
	}

	public void setAnnotationDate(Date annotationDate) {
		this.annotationDate = annotationDate;
	}

	public String getAnnotation() {
		return annotation;
	}

	public void setAnnotation(String annotation) {
		this.annotation = annotation;
	}

	public T getSource() {
		return source;
	}

	public void setSource(T source) {
		this.source = source;
	}

	public T getTarget() {
		return target;
	}

	public void setTarget(T target) {
		this.target = target;
	}

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}
}