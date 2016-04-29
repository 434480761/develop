package nd.esp.service.lifecycle.repository.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.solr.client.solrj.beans.Field;

import nd.esp.service.lifecycle.repository.DataConverter;
import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;

/**
 * 类描述:bean 创建人: 创建时间:2015-05-13 18:52:3
 * 
 * @version
 */

@Entity
@Table(name = "teachingmaterials")
@NamedQuery(name = "checkTeachingMaterialExist", query = "SELECT a FROM TeachingMaterial a,ResourceCategory b WHERE b.taxonpath = :taxonPath and a.identifier = b.resource and a.identifier <> :identifier and a.primaryCategory='teachingmaterials' and a.status = 'ONLINE'")
public class TeachingMaterial extends Education {

	public static final String PROP_ATTACHMENTS = "attachments";

	/**
	* 
	*/
	@Column(name = "attachments")
	@DataConverter(target="attachments", type=List.class)
	private String dbattachments;
	@Transient
	private List<String> attachments;

	/**
	* 
	*/
	@Column(name = "criterion")
	private String criterion;

	/**
	* 
	*/
	@Column(name = "edition")
	private String edition;

	/**
	* 
	*/
	@Column(name = "grade")
	private String grade;

	/**
	* 
	*/
	@Column(name = "isbn")
	private String isbn;

	/**
	* 
	*/
	@Column(name = "phase")
	private String phase;

	/**
	* 
	*/
	@Column(name = "subject")
	private String subject;

	public void setCriterion(String criterion) {
		this.criterion = criterion;
	}

	public String getCriterion() {
		return this.criterion;
	}

	public void setEdition(String edition) {
		this.edition = edition;
	}

	public String getEdition() {
		return this.edition;
	}


	public void setGrade(String grade) {
		this.grade = grade;
	}

	public String getGrade() {
		return this.grade;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}

	public String getIsbn() {
		return this.isbn;
	}

	public void setPhase(String phase) {
		this.phase = phase;
	}

	public String getPhase() {
		return this.phase;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getSubject() {
		return this.subject;
	}

	@Override
	public IndexSourceType getIndexType() {
//		this.setPrimaryCategory(IndexSourceType.TeachingMaterialType.getName());
		return IndexSourceType.TeachingMaterialType;
	}

	public String getDbattachments() {
		return dbattachments;
	}

	public void setDbattachments(String dbattachments) {
		this.dbattachments = dbattachments;
	}

	public List<String> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<String> attachments) {
		this.attachments = attachments;
	}
}