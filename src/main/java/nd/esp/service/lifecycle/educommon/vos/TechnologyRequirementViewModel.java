package nd.esp.service.lifecycle.educommon.vos;

import nd.esp.service.lifecycle.support.annotation.Reg;
import nd.esp.service.lifecycle.vos.valid.RequirementDefault;

import org.hibernate.validator.constraints.NotBlank;


/**
 * @author johnny
 * @version 1.0
 * @created 08-7月-2015 10:18:50
 */
public class TechnologyRequirementViewModel {
	@NotBlank(message="{TechnologyRequirementViewModel.requirements.type.notBlank.validmsg}",groups={RequirementDefault.class})
	@Reg(message="{TechnologyRequirementViewModel.requirements.type.reg.validmsg}",pattern="^(HARDWARE|SOFTWARE|NETWORK|PROFILE|QUOTA)$",isNullValid=false,groups={RequirementDefault.class})
	private String type;
	@NotBlank(message="{TechnologyRequirementViewModel.requirements.name.notBlank.validmsg}",groups={RequirementDefault.class})
	private String name;
	private String minVersion;
	private String maxVersion;
	private String installation;
	private String installationFile;
	private String value;

	public TechnologyRequirementViewModel(){

	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMinVersion() {
		return minVersion;
	}

	public void setMinVersion(String minVersion) {
		this.minVersion = minVersion;
	}

	public String getMaxVersion() {
		return maxVersion;
	}

	public void setMaxVersion(String maxVersion) {
		this.maxVersion = maxVersion;
	}

	public String getInstallation() {
		return installation;
	}

	public void setInstallation(String installation) {
		this.installation = installation;
	}

	public String getInstallationFile() {
		return installationFile;
	}

	public void setInstallationFile(String installationFile) {
		this.installationFile = installationFile;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}