package nd.esp.service.lifecycle.educommon.vos;


/**
 * 资源存储的物理实例信息。关于公司有库的概念。存储都存储在同一个存储中，通过逻辑进行控制隔离。另外一种可以通过物理上的存储进行隔离。具体的隔离方式是申请独立的存储
 * 空间。存放当前组织的资源内容。
 * @author johnny
 * @version 1.0
 * @created 08-7月-2015 10:18:50
 */
public class ResRepositoryViewModel {

	private String identifier;
	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getRepositoryName() {
		return repositoryName;
	}

	public void setRepositoryName(String repositoryName) {
		this.repositoryName = repositoryName;
	}

	public String getTargetType() {
		return targetType;
	}

	public void setTargetType(String targetType) {
		this.targetType = targetType;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getRepositoryAdmin() {
		return repositoryAdmin;
	}

	public void setRepositoryAdmin(String repositoryAdmin) {
		this.repositoryAdmin = repositoryAdmin;
	}

	public String getRepDescription() {
		return repDescription;
	}

	public void setRepDescription(String repDescription) {
		this.repDescription = repDescription;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	private String repositoryName;
	/**
	 * Org，Group，Role
	 */
	private String targetType;
	/**
	 * 目标对象的具体关联值
	 */
	private String target;
	/**
	 * 管理员的id
	 */
	private String repositoryAdmin;
	private String repDescription;
	/**
	 * 存储空间的服务名称
	 */
	private String serviceName;

	public ResRepositoryViewModel(){

	}
}