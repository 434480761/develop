package com.nd.esp.task.worker.buss.media_transcode.model;

import java.util.List;
import java.util.Map;

public class TranscodeResult {
    private int status;
    private String href;
    private String errMsg;
    private Map<String,String> metadata;
    private String transcodeType;
    private List<String> previews;
    private String cover;
    //多码率目标地址
    private Map<String,String> locations;
    
    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }
    public String getHref() {
        return href;
    }
    public void setHref(String href) {
        this.href = href;
    }
    public String getErrMsg() {
        return errMsg;
    }
    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }
    public Map<String,String> getMetadata() {
        return metadata;
    }
    public void setMetadata(Map<String,String> metadata) {
        this.metadata = metadata;
    }
    public String getTranscodeType() {
        return transcodeType;
    }
    public void setTranscodeType(String transcodeType) {
        this.transcodeType = transcodeType;
    }
    public List<String> getPreviews() {
        return previews;
    }
    public void setPreviews(List<String> previews) {
        this.previews = previews;
    }
    public String getCover() {
        return cover;
    }
    public void setCover(String cover) {
        this.cover = cover;
    }
    public Map<String,String> getLocations() {
        return locations;
    }
    public void setLocations(Map<String,String> locations) {
        this.locations = locations;
    }
}
