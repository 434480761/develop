package nd.esp.service.lifecycle.repository.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
/**
 * 类描述:bean
 * 创建人:
 * 创建时间:2015-05-13 18:52:3
 * @version
 */
  
@Entity
@Table(name="knowledge_relations")
public class KnowledgeRelation extends EspEntity {
	
	
	/**
	* 
	*/
	@Column(name="context_object")
 	private String contextObject; 
	/**
	* 
	*/
	@Column(name="context_type")
 	private String contextType; 
	
	/**
	* 
	*/
	@Column(name="relation_type")
 	private String relationType; 
	/**
	* 
	*/
	@Column(name="source")
 	private String source; 
	/**
	* 
	*/
	@Column(name="target")
 	private String target; 

	public void setSource(String source) {
		this.source = source;
	}
	
	public String getSource() {
		return this.source;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	
	public String getTarget() {
		return this.target;
	}

	@Override
	public IndexSourceType getIndexType() {
		return IndexSourceType.KnowledgeRelationType;
	}

	public String getContextObject() {
		return contextObject;
	}

	public void setContextObject(String contextObject) {
		this.contextObject = contextObject;
	}

	public String getContextType() {
		return contextType;
	}

	public void setContextType(String contextType) {
		this.contextType = contextType;
	}

	public String getRelationType() {
		return relationType;
	}

	public void setRelationType(String relationType) {
		this.relationType = relationType;
	}

}