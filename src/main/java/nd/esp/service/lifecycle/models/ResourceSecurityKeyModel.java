package nd.esp.service.lifecycle.models;

import java.sql.Timestamp;

import nd.esp.service.lifecycle.annotations.Column;

public class ResourceSecurityKeyModel {

	@Column(name = "identifier")
    private String identifier;
	
	@Column(name = "security_key")
    private String securityKey;

    @Column(name = "update_time")
    protected Timestamp updateTime;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getSecurityKey() {
        return securityKey;
    }

    public void setSecurityKey(String securityKey) {
        this.securityKey = securityKey;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }
}