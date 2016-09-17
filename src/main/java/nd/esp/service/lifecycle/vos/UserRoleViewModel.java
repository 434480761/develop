package nd.esp.service.lifecycle.vos;


import java.util.List;

/**
 *
 * <p>Title: 角色列表model        </p>
 * <p>Description: UserRoleViewModel </p>
 * <p>Copyright: Copyright (c) 2016     </p>
 * <p>Company: ND Co., Ltd.       </p>
 * <p>Create Time: 2016年06月30日           </p>
 * @author lanyl
 */
public class UserRoleViewModel {
    /**
     * 用户ID
     */
    private String userId;

	/**
	 * 用户名称
	 */
	private String userName;
	/**
	 * 覆盖范围的数组
	 */
	private List<String> coverages;
	/**
	 * 资源类型数组
	 */
	private List<String> resTypes;

	private List<RoleViewModel> roles;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public List<String> getCoverages() {
		return coverages;
	}

	public void setCoverages(List<String> coverages) {
		this.coverages = coverages;
	}

	public List<String> getResTypes() {
		return resTypes;
	}

	public void setResTypes(List<String> resTypes) {
		this.resTypes = resTypes;
	}

	public List<RoleViewModel> getRoles() {
		return roles;
	}

	public void setRoles(List<RoleViewModel> roles) {
		this.roles = roles;
	}
}
