package nd.esp.service.lifecycle.entity.elasticsearch;

public class ES_TechnologyRequirementModel {
	private String identifier;
	private String type;
	private String name;
	private String minVersion;
	private String maxVersion;
	private String installation;
	private String installationFile;
	private String value;

	
	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
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
