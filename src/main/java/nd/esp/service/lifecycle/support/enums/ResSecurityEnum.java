package nd.esp.service.lifecycle.support.enums;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * <p>Title: ResSecurityEnum  </p>
 * <p>Description: ResSecurityEnum </p>
 * <p>Copyright: Copyright (c) 2016     </p>
 * <p>Company: ND Co., Ltd.       </p>
 * <p>Create Time: 2016年7月14日           </p>
 * @author lianggz
 */
public enum ResSecurityEnum {

    ENCRYPTION(1),
	NORMAL(2)
	;

	private Integer value;

	public Integer getValue() {
		return value;
	}

	private ResSecurityEnum(Integer value) {
		this.value = value;
	}
	
	/**
     * 获取枚举正则表达式
     * @return
     */
    public static String getRegex(){
        List<String> list = new ArrayList<String>();
        for (ResSecurityEnum e : ResSecurityEnum.values()){
            list.add(e.getValue()+"");
        }
        return StringUtils.join(list, "|");
    }
}
