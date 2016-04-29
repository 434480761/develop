package nd.esp.service.lifecycle.repository.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;

/**
 * 类描述:bean 创建人: 创建时间:2015-05-13 18:52:3
 * 
 * @version
 *
 * @udpate 新增node_left,node_right,parent三个属性 at20150907 19:54
 */

@Entity
@Table(name = "knowledges")
@NamedQueries({
    @NamedQuery(name="getKnowledgeCountByCode", query="SELECT count(k) FROM Knowledge k WHERE k.primaryCategory = 'knowledges' AND k.identifier IN (SELECT rc.resource FROM ResourceCategory rc WHERE rc.taxoncode = :subjectCode AND rc.resource IN (SELECT rc.resource FROM rc WHERE rc.taxoncode = :knowledgeCode))"),
    @NamedQuery(name="getKnowledgeCountByParentAndCode", query="SELECT count(k) FROM Knowledge k WHERE k.primaryCategory = 'knowledges' AND k.parent =:kparent  AND k.identifier IN (SELECT rc.resource FROM ResourceCategory rc WHERE rc.taxoncode = :subjectCode AND rc.resource IN (SELECT rc.resource FROM rc WHERE rc.taxoncode = :knowledgeCode))"),
    @NamedQuery(name="getLastKnowledgeOnSameLevel", query="SELECT k.nodeLeft, k.nodeRight FROM Knowledge k WHERE k.primaryCategory = 'knowledges' AND k.identifier IN (SELECT rc.resource FROM ResourceCategory rc WHERE rc.taxoncode = :subjectCode AND rc.resource IN (SELECT rc.resource FROM rc WHERE rc.taxoncode = :knowledgeCode)) AND k.parent=:pid ORDER BY k.nodeLeft DESC")
})
public class Knowledge extends Education {

	public static final String PROP_STOREDINFO = "storedinfo";
	/**
	* 
	*/
	@Column(name = "subject")
	private String subject;



	@Column(name = "node_left")
	protected Integer nodeLeft;


	@Column(name = "node_right")
	protected Integer nodeRight;


	@Column(name = "parent")
	protected String parent;


	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getSubject() {
		return this.subject;
	}

	public Integer getNodeLeft() {
		return nodeLeft;
	}

	public void setNodeLeft(Integer nodeLeft) {
		this.nodeLeft = nodeLeft;
	}

	public Integer getNodeRight() {
		return nodeRight;
	}

	public void setNodeRight(Integer nodeRight) {
		this.nodeRight = nodeRight;
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	@Override
	public IndexSourceType getIndexType() {
		this.setPrimaryCategory(IndexSourceType.KnowledgeType.getName());
		return IndexSourceType.KnowledgeType;
	}
}