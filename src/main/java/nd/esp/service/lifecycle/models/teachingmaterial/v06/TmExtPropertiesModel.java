package nd.esp.service.lifecycle.models.teachingmaterial.v06;

import java.util.List;

/**
 * 教材扩展属性类
 * @author xuzy
 *
 */
public class TmExtPropertiesModel {
	/**
	 * 教材的isbn编码
	 */
	private String isbn;
	
	/**
	 * 教材的附件访问地址
	 */
	private List<String> attachments;
	
	/**
	 * 教材的采用课标内容
	 */
	private String criterion;

	public String getIsbn() {
		return isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}

	public List<String> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<String> attachments) {
		this.attachments = attachments;
	}

	public String getCriterion() {
		return criterion;
	}

	public void setCriterion(String criterion) {
		this.criterion = criterion;
	}
	
}
