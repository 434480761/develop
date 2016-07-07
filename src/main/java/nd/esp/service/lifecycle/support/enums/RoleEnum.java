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
public enum RoleEnum {

	SUPERADMIN("SuperAdmin"),
	COVERAGEADMIN("CoverageAdmin"),
	RESCREATOR("ResCreator"),
	CATEGORYDATAADMIN("CategoryDataAdmin"),
	RESCONSUMER("ResConsumer"),
	GUEST("Guest"),
	BEARERTOKEN("role_biz_server")
	;

	private String value;

	public String getValue() {
		return value;
	}

	private RoleEnum(String value) {
		this.value = value;
	}
}
