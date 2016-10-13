package com.nd.esp.task.worker.buss.document_transcode.entity.lifecycle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.Map;

/**
 * @title 上传下载模型
 * @desc
 * @atuh lwx
 * @createtime on 2015年6月11日 下午8:00:07
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UploadResponse implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /** 访问key */
    private String accessKey;

    /** 请求方法 */
    private String accessMethod;

    /** 请求URL */
    private String accessUrl;

    /** 失效日期时间 */
    private String expireTime;

    /** UUID */
    private String uuid;

    /** 预览资源 */
    private Map<String, String> preview;

    /** SessionId */
    private String sessionId;

    private String distPath;

    /**
     * @return the accessKey
     */
    public String getAccessKey() {
        return accessKey;
    }

    /**
     * @param accessKey the accessKey to set
     */
    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    /**
     * @return the accessMethod
     */
    public String getAccessMethod() {
        return accessMethod;
    }

    /**
     * @param accessMethod the accessMethod to set
     */
    public void setAccessMethod(String accessMethod) {
        this.accessMethod = accessMethod;
    }

    /**
     * @return the accessUrl
     */
    public String getAccessUrl() {
        return accessUrl;
    }

    /**
     * @param accessUrl the accessUrl to set
     */
    public void setAccessUrl(String accessUrl) {
        this.accessUrl = accessUrl;
    }

    /**
     * @return the expireTime
     */
    public String getExpireTime() {
        return expireTime;
    }

    /**
     * @param expireTime the expireTime to set
     */
    public void setExpireTime(String expireTime) {
        this.expireTime = expireTime;
    }

    /**
     * @return the uuid
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * @param uuid the uuid to set
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * @return the preview
     */
    public Map<String, String> getPreview() {
        return preview;
    }

    /**
     * @param preview the preview to set
     */
    public void setPreview(Map<String, String> preview) {
        this.preview = preview;
    }

    /**
     * @return the sessionId
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * @param sessionId the sessionId to set
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getDistPath() {
        return distPath;
    }

    public void setDistPath(String distPath) {
        this.distPath = distPath;
    }

}
