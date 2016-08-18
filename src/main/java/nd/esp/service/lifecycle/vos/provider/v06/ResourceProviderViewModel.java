package nd.esp.service.lifecycle.vos.provider.v06;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

/**
 * 资源提供商 viewModel
 * @author xiezy
 * @date 2016年8月15日
 */
public class ResourceProviderViewModel {
	private String identifier;
	
	@NotBlank(message="{resourceProviderViewModel.title.notBlank.validmsg}") 
	@Length(max=100,message="{resourceProviderViewModel.title.maxlength.validmsg}")
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
