package nd.esp.service.lifecycle.entity;

public class PackagingParam {
    private String uid;
    private String resType;
    private String uuid;
    private String target;
    private boolean bWebpFirst;
    private boolean bNoOgg;
    private String path;
    private Integer priority;
    
    public PackagingParam(){}
    public PackagingParam(Integer priority){
        
        this.priority=priority;
    }
    
    public Integer getPriority() {
        return priority;
    }
    public void setPriority(Integer priority) {
        this.priority = priority;
    }
    public String getUid() {
        return uid;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }
    public String getResType() {
        return resType;
    }
    public void setResType(String resType) {
        this.resType = resType;
    }
    public String getUuid() {
        return uuid;
    }
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    public String getTarget() {
        return target;
    }
    public void setTarget(String target) {
        this.target = target;
    }
    public boolean isbWebpFirst() {
        return bWebpFirst;
    }
    public void setbWebpFirst(boolean bWebpFirst) {
        this.bWebpFirst = bWebpFirst;
    }
    public boolean isbNoOgg() {
        return bNoOgg;
    }
    public void setbNoOgg(boolean bNoOgg) {
        this.bNoOgg = bNoOgg;
    }
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
    
    
}
