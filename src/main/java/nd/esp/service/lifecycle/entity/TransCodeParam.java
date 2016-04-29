package nd.esp.service.lifecycle.entity;

/**
 * @title 转码触发需要的参数
 * @desc
 * @atuh lwx
 * @createtime on 2015年8月18日 下午5:24:37
 */
public class TransCodeParam {

    private String instanceKey;

    private String resType;

    private String resId;

    private String sourceFileId;

    private String referer;
    
    private String subType;
    
    private String statusBackup;

    private TransCodeParam() {
    }

    public static TransCodeParam build() {
        return new TransCodeParam();
    }

    public TransCodeParam buildInstanceKey(String instanceKey) {
        this.instanceKey = instanceKey;
        return this;
    }

    public TransCodeParam buildResType(String resType) {
        this.resType = resType;
        return this;
    }

    public TransCodeParam buildResId(String resId) {
        this.resId = resId;
        return this;
    }

    public TransCodeParam buildSourceFileId(String sourceFileId) {
        this.sourceFileId = sourceFileId;
        return this;
    }

    public TransCodeParam buildReferer(String referer) {
        this.referer = referer;
        return this;
    }
    
    public TransCodeParam buildStatusBackup(String statusBackup) {
        this.statusBackup = statusBackup;
        return this;
    }

    public String getInstanceKey() {
        return instanceKey;
    }

    public String getResType() {
        return resType;
    }

    public String getResId() {
        return resId;
    }

    public String getSourceFileId() {
        return sourceFileId;
    }

    public String getReferer() {
        return referer;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public String getStatusBackup() {
        return statusBackup;
    }

    public void setStatusBackup(String statusBackup) {
        this.statusBackup = statusBackup;
    }

}
