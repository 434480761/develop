package nd.esp.service.lifecycle.entity.elasticsearch;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;

import nd.esp.service.lifecycle.daos.elasticsearch.EsResourceOperationImpl;

public class ES_BookExtPropertyModel {
	/**
	 * 电子教材的isbn编码
	 */
	private String isbn;

	/**
	 * 电子教材的附件访问地址
	 */
	private List<String> attachments;

	/**
	 * 电子教材的采用课标内容
	 */
	private String criterion;

	public String getIsbn() {
		return isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}

	public String getAttachments() throws JsonProcessingException {
		return EsResourceOperationImpl.ObjectMapper
				.writeValueAsString(this.attachments);
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
