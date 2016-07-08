package nd.esp.service.lifecycle.repository.model;

import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.repository.index.NoIndexBean;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.sql.Timestamp;

@NoIndexBean
@Entity
@Table( name ="resource_security_key")
public class ResourceSecurityKey extends EspEntity {

	@Column(name = "security_key")
    private String securityKey;

    @Column(name = "create_time")
    protected BigDecimal createTime;

    public String getSecurityKey() {
        return securityKey;
    }

    public void setSecurityKey(String securityKey) {
        this.securityKey = securityKey;
    }

    public BigDecimal getCreateTime() {
        return createTime;
    }

    public void setCreateTime(BigDecimal createTime) {
        this.createTime = createTime;
    }

    @Override
    public IndexSourceType getIndexType() {
        return null;
    }
    
}
