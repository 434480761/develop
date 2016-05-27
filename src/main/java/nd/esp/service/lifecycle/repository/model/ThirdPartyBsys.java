package nd.esp.service.lifecycle.repository.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.repository.index.NoIndexBean;

@NoIndexBean
@Entity
@Table( name ="third_party_bsys")
public class ThirdPartyBsys extends EspEntity {

	@Column(name = "bsysname")
    private String bsysname;
    
    @Column(name = "bsysadmin")
    private String bsysadmin;
    
    @Column(name = "bsyskey")
    private String bsyskey;
    
    @Column(name = "bsysivcconfig")
    private String bsysivcconfig;

    @Column(name = "user_id")
    private String userid;
    
    @Column(name = "update_time")
    private Timestamp updateTime;
    
    @Column(name = "create_time")
    private Timestamp createTime;
    

    public String getBsysname() {
        return bsysname;
    }

    public void setBsysname(String bsysname) {
        this.bsysname = bsysname;
    }

    public String getBsysadmin() {
        return bsysadmin;
    }

    public void setBsysadmin(String bsysadmin) {
        this.bsysadmin = bsysadmin;
    }
    
    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getBsyskey() {
        return bsyskey;
    }

    public void setBsyskey(String bsyskey) {
        this.bsyskey = bsyskey;
    }

    public String getBsysivcconfig() {
        return bsysivcconfig;
    }

    public void setBsysivcconfig(String bsysivcconfig) {
        this.bsysivcconfig = bsysivcconfig;
    }
    
    public Timestamp getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

    @Override
    public IndexSourceType getIndexType() {
        return null;
    }
    
}
