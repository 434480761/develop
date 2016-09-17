package nd.esp.service.lifecycle.models;



/**
 * @title 文件会话模型对象
 * @author liuwx
 * @version 1.0
 * @create 2015年3月19日 下午3:32:35
 */
public class FileSessionModel {
	
	private String path;
	
	private String uid;
	
	private String role;
	
	private long expireTime;
	
	private String sessionId;

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public long getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(long expireTime) {
		this.expireTime = expireTime;
	}
	
	

}
