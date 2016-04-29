
/**   
 * @Title: Ebook.java 
 * @Package: com.nd.esp.repository.model 
 * @Description: TODO
 * @author Rainy(yang.lin)  
 * @date 2015年6月2日 上午11:06:03 
 * @version 1.3.1 
 */


package nd.esp.service.lifecycle.repository.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import nd.esp.service.lifecycle.repository.DataConverter;
import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;

/** 
 * @Description 
 * @author Rainy(yang.lin)  
 * @date 2015年6月2日 上午11:06:03 
 * @version V1.0
 */
@Entity
@Table(name = "ebooks")
public class Ebook extends Education {

	
	/** @Fields serialVersionUID: */
	  	
	private static final long serialVersionUID = 1L;

	@Column(name = "isbn")
	private String isbn;
	
	/*陈允进*/
    /****************************ext_properties begin***************************/
	// 电子教材的附件访问地址
    @Column(name = "attachments")
    @DataConverter(target="attachments", type=List.class)
    private String dbattachments;
    @Transient
    private List<String> attachments;
	// 电子教材的采用课标内容
	@Column(name = "criterion")
	private String criterion;
    /****************************ext_properties end***************************/
	

    /**
	 * Description 
	 * @return 
	 * @see com.nd.esp.repository.IndexMapper#getIndexType() 
	 */
	@Override
	public IndexSourceType getIndexType() {
		this.setPrimaryCategory(IndexSourceType.EbookType.getName());
		return IndexSourceType.EbookType;
	}
	public String getIsbn() {
		return isbn;
	}
	public void setIsbn(String isbn) {
		this.isbn = isbn;
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
    public static long getSerialversionuid() {
        return serialVersionUID;
    }
    public String getCriterion() {
        return criterion;
    }
    public void setCriterion(String criterion) {
        this.criterion = criterion;
    }
}
