package nd.esp.service.lifecycle.support.enums;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2016  </p>
 * <p>Company:ND Co., Ltd.  </p>
 * <p>Create Time: 2016/7/12 </p>
 *
 * @author lanyl
 */
public enum CoverageStrategyEnum {

	//个人库
	USER("User");

	private String value;

	public String getValue() {
		return value;
	}

	private CoverageStrategyEnum(String value) {
		this.value = value;
	}
}
