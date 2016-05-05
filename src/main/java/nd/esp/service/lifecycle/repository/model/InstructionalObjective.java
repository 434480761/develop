package nd.esp.service.lifecycle.repository.model;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
/**
 * 类描述:bean
 * 创建人:
 * 创建时间:2015-05-13 18:52:3
 * @version
 */
  
@Entity
@Table(name="instructional_objectives")
public class InstructionalObjective extends Education {
	
	
	/**
	* 
	*/
	@Column(name="lesson")
 	private String lesson; 
	
	private String classification;
	
	@Column(name="kb_id")
    private String kbId;
    
	@Column(name="oc_id")
    private String ocId;
	
	public void setLesson(String lesson) {
		this.lesson = lesson;
	}
	
	public String getClassification() {
		return classification;
	}

	public void setClassification(String classification) {
		this.classification = classification;
	}

	public String getLesson() {
		return this.lesson;
	}


	@Override
	public IndexSourceType getIndexType() {
		this.setPrimaryCategory(IndexSourceType.InstructionalObjectiveType.getName());
		return IndexSourceType.InstructionalObjectiveType;
	}

	public String getKbId() {
		return kbId;
	}

	public void setKbId(String kbId) {
		this.kbId = kbId;
	}

	public String getOcId() {
		return ocId;
	}

	public void setOcId(String ocId) {
		this.ocId = ocId;
	}

	
	
}