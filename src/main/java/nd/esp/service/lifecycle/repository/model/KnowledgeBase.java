package nd.esp.service.lifecycle.repository.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;

/**
 * 知识库
 * @author xuzy
 *
 */

@Entity
@Table(name = "knowledge_base")
public class KnowledgeBase extends EspEntity {
	private static final long serialVersionUID = 8683345223450774854L;
	private String kpid;
	private String knid;
	public String getKpid() {
		return kpid;
	}
	public void setKpid(String kpid) {
		this.kpid = kpid;
	}
	public String getKnid() {
		return knid;
	}
	public void setKnid(String knid) {
		this.knid = knid;
	}
	@Override
	public IndexSourceType getIndexType() {
		return IndexSourceType.KnowledgebaseType;
	}
	
}