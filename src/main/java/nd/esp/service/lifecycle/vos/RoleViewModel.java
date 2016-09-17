package nd.esp.service.lifecycle.vos;

/**
 * <p>Title: RoleViewModel</p>
 * <p>Description: RoleViewModel</p>
 * <p>Copyright: Copyright (c) 2016  </p>
 * <p>Company:ND Co., Ltd.  </p>
 * <p>Create Time: 2016/7/11 </p>
 *
 * @author lanyl
 */
public class RoleViewModel {

	/**
	 * 角色ID
	 */
	private String roleId;
	/**
	 * 覆角色名称
	 */
	private String roleName;

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
}
