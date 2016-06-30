package nd.esp.service.lifecycle.support.enums;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2016  </p>
 * <p>Company:ND Co., Ltd.  </p>
 * <p>Create Time: 2016/6/29 </p>
 *
 * @author lanyl
 */
public enum  RolesEnum {

	SUPERADMIN("136817"),
	COVERAGEADMIN("136817")
	;

	private String value;

	public String getValue() {
		return value;
	}

	private RolesEnum(String value) {
		this.value = value;
	}
}
