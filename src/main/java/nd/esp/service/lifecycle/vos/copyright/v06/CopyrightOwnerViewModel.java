package nd.esp.service.lifecycle.vos.copyright.v06;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
/**
 * 资源版权方 viewModel
 * @author xiezy
 * @date 2016年8月16日
 */
public class CopyrightOwnerViewModel {
	private String identifier;

	@NotBlank(message = "{copyrightOwnerViewModel.title.notBlank.validmsg}")
	@Length(max = 200, message = "{copyrightOwnerViewModel.title.maxlength.validmsg}")
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
