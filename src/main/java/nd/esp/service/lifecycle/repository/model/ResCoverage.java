
/**   
 * @Title: ResCoverage.java 
 * @Package: com.nd.esp.repository.model 
 * @Description: TODO
 * @author Rainy(yang.lin)  
 * @date 2015年6月18日 上午10:14:05 
 * @version 1.3.1 
 */


package nd.esp.service.lifecycle.repository.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.solr.client.solrj.beans.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.repository.index.NoIndexBean;

/** 
 * @Description 
 * @author Rainy(yang.lin)  
 * @date 2015年6月18日 上午10:14:05 
 * @version V1.0
 */
@Entity
@Table(name = "res_coverages")
@NoIndexBean
@NamedQuery(name = "batchGetCoverageByResource", query = "SELECT rc FROM ResCoverage rc WHERE rc.resType=:rt AND resource IN (:rids)")
public class ResCoverage extends EspEntity {

	
	/**
	 * Logging
	 */
	private static Logger logger = LoggerFactory.getLogger(ResCoverage.class);
	
	/** @Fields serialVersionUID: */
	  	
	private static final long serialVersionUID = 1L;

	@Column(name ="target_type")
	private String targetType;
	
	@Column(name ="strategy")
	private String strategy;
	
	@Column(name ="target")
	private String target;
	
	@Column(name ="target_title")
	private String targetTitle;
	
	@Column(name="resource")
	private String resource;
	
	@Column(name ="res_type")
	private String resType;
	

    /**
	 * Description 
	 * @return 
	 * @see com.nd.esp.repository.IndexMapper#getIndexType() 
	 */ 
		
	@Override
	public IndexSourceType getIndexType() {
		return IndexSourceType.ResCoverageType;
	}

	public String getTargetType() {
		return targetType;
	}

	public void setTargetType(String targetType) {
		this.targetType = targetType;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getTargetTitle() {
		return targetTitle;
	}

	public void setTargetTitle(String targetTitle) {
		this.targetTitle = targetTitle;
	}

	public String getResType() {
		return resType;
	}

	public void setResType(String resType) {
		this.resType = resType;
	}

	public String getStrategy() {
		return strategy;
	}

	public void setStrategy(String strategy) {
		this.strategy = strategy;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

}
