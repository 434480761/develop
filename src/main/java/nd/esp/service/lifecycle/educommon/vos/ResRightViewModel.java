package nd.esp.service.lifecycle.educommon.vos;


/**
 * @author johnny
 * @version 1.0
 * @created 08-7月-2015 10:18:50
 */
public class ResRightViewModel {

	/**
	 * 版权信息
	 */
	private String right;
	/**
	 * 版权描述信息
	 */
	private String description;
	/**
	 * 作者信息
	 */
	private String author;

	public ResRightViewModel(){

	}

	public String getRight() {
		return right;
	}

	public void setRight(String right) {
		this.right = right;
	}



	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}