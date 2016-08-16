package nd.esp.service.lifecycle.models.copyright.v06;
/**
 * 资源版权方 Model
 * @author xiezy
 * @date 2016年8月16日
 */
public class CopyrightOwnerModel {
	private String identifier;
	private String title;
	private String description;

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
