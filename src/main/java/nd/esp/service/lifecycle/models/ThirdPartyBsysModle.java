package nd.esp.service.lifecycle.models;

import java.util.Map;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

public class ThirdPartyBsysModle {
    

    private String identifier;
    
    @NotBlank(message="{thirdPartyBsysModle.bsysname.notBlank.validmsg}")
    @Length(max=250,message="{thirdPartyBsysModle.bsysname.maxlength.validmsg}")
    private String bsysname;
    
    @NotBlank(message="{thirdPartyBsysModle.bsysadmin.notBlank.validmsg}")
    @Length(max=250,message="{thirdPartyBsysModle.bsysadmin.maxlength.validmsg}")
    private String bsysadmin;
    
    private String userid;
    
    private String bsyskey;
    
    private Map<String,Object> bsysivcconfig;
    
    
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

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

    public Map<String,Object> getBsysivcconfig() {
        return bsysivcconfig;
    }

    public void setBsysivcconfig(Map<String,Object> bsysivcconfig) {
        this.bsysivcconfig = bsysivcconfig;
    }
    
}
