package nd.esp.service.lifecycle.models;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
/**
 * 请求素材资源，课件颗粒所需要的信息封装，包括url，method，key，失效时间等等
 * @author johnny
 * @version 1.0
 * @created 24-3月-2015 12:06:03
 */
@JsonInclude(Include.NON_NULL)
public class AccessModel implements Serializable{
	private static final long serialVersionUID = 1L;

	/**
	 * 访问后端服务认证所需的key
	 */
	private String accessKey;
	
	/**
	 * 访问后端数据服务api接口的方法
	 */
	private String accessMethod = "POST";
	/**
	 * 用户访问数据服务的接口url
	 */
	private String accessUrl;
	
	/**
	 * 预览地址
	 */
	private Map<String,String> preview;
	
	/**
	 * 生成uuid
	 */
	private UUID uuid;
	
	/**
	 * key的有效截止时间
	 */
	private Date expireTime;
	
	/**
	 * 访问后端服务认证所需的key
	 */
	private String sessionId;
	
	/**
	 * 上传文件的目标地址
	 */
	private String distPath;
	
	/**
	 * 错误提示信息
	 */
	private String errorMessage;

	public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getAccessKey() {
		return accessKey;
	}

	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	public Map<String, String> getPreview() {
		return preview;
	}

	public void setPreview(Map<String, String> preview) {
		this.preview = preview;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public String getDistPath() {
		return distPath;
	}

	public void setDistPath(String distPath) {
		this.distPath = distPath;
	}

	public String getAccessMethod() {
		return accessMethod;
	}

	public void setAccessMethod(String accessMethod) {
		this.accessMethod = accessMethod;
	}

	public String getAccessUrl() {
		return accessUrl;
	}

	public void setAccessUrl(String accessUrl) {
		this.accessUrl = accessUrl;
	}

	public Date getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(Date expireTime) {
		this.expireTime = expireTime;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
}