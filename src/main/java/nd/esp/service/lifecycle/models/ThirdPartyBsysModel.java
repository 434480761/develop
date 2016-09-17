package nd.esp.service.lifecycle.models;

import nd.esp.service.lifecycle.annotations.Column;

/**
 * <p>Title: ThirdPartyBsysModel         </p>
 * <p>Description: ThirdPartyBsysModel </p>
 * <p>Copyright: Copyright (c) 2016     </p>
 * <p>Company: ND Co., Ltd.       </p>
 * <p>Create Time: 2016年6月30日           </p>
 * @author lianggz
 */
public class ThirdPartyBsysModel {
    
    @Column(name = "identifier")
    private String identifier;
      
    @Column(name = "bsysname")
    private String bsysname;
    
    @Column(name = "bsysadmin")
    private String bsysadmin;
    
    @Column(name = "user_id")
    private String userid;
    
    @Column(name = "bsyskey")
    private String bsyskey;
     
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
}