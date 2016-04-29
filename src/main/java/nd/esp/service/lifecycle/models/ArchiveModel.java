package nd.esp.service.lifecycle.models;




/**
 * @title 打包返回的信息模型
 * @Desc TODO
 * @author liuwx
 * @version 1.0
 * @create 2015年4月27日 下午3:51:49
 */
public class ArchiveModel {

	/**
	 * 访问后端服务认证所需的key
	 */
	private String archiveState;
	/**
	 * 访问后端数据服务api接口的方法
	 */
	private String accessMethod = "POST";
	/**
	 * 用户访问数据服务的接口url
	 */
	private String accessUrl;
	/**
	 * key的有效截止时间
	 */
	private String message;

	/**
	 * md5
	 * */
	private String md5;
	public String getArchiveState() {
		return archiveState;
	}

	public void setArchiveState(String archiveState) {
		this.archiveState = archiveState;
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

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public ArchiveModel(){

	}

	public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public ArchiveModel(String archiveState, String accessMethod,
			String accessUrl, String message) {
		super();
		this.archiveState = archiveState;
		this.accessMethod = accessMethod;
		this.accessUrl = accessUrl;
		this.message = message;
	}


}