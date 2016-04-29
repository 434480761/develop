package nd.esp.service.lifecycle.entity.elasticsearch.v1;

/**
 * 扩展属性，用于：ebooks, guidancebooks, teachingmaterials
 * 
 * @author linsm
 *
 */
public class EsBook extends EsNdResource {
	private String isbn;

	private String dbattachments;
	private String criterion;

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

	public String getCriterion() {
		return criterion;
	}

	public void setCriterion(String criterion) {
		this.criterion = criterion;
	}

}
