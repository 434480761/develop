package nd.esp.service.lifecycle.support.busi.titan;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nd.esp.service.lifecycle.educommon.vos.constant.IncludesConstant;
import nd.esp.service.lifecycle.support.enums.ES_SearchField;
import nd.esp.service.lifecycle.utils.CollectionUtils;

/**
 * titan 工具类
 * 
 * @author linsm
 *
 */
public class TitanUtils {

	public static String generateScriptForInclude(List<String> includes) {
		if (CollectionUtils.isEmpty(includes)) return "";
		StringBuffer scriptBuffer = new StringBuffer();
		String begin = ".as('v').union(select('v')";
		String end = ")";

		for (String include : includes) {
			if (include.equals(IncludesConstant.INCLUDE_TI)) {
				scriptBuffer.append(",out('has_tech_info')");
			} else if (include.equals(IncludesConstant.INCLUDE_CG)) {
				scriptBuffer.append(",out('has_category_code')");
				scriptBuffer.append(",out('has_categories_path')");
			}
		}

		if ("".equals(scriptBuffer.toString())) return "";
		return begin + scriptBuffer.toString() + end;
	}

	// 生成脚本参数名字，避免多个值冲突
	public static String generateKey(Map<String, Object> scriptParamMap,
			String originKey) {
		int i = 0;
		String key = null;
		do {
			key = originKey + i;
			i++;
		} while (scriptParamMap.containsKey(key));
		return key;
	}

	/*
	 * 将参数类型转换成titan字段类型
	 */
	public static List<Object> changeToTitanType(String fieldName,
			List<String> valueList) {
		if (ES_SearchField.lc_create_time.toString().equals(fieldName)
				|| ES_SearchField.lc_last_update.toString().equals(fieldName)) {
			List<Object> values = new ArrayList<Object>();
			if (CollectionUtils.isNotEmpty(valueList)) {
				for (String value : valueList) {
					values.add(Timestamp.valueOf(value).getTime());
				}
			}
			return values;
		}

		return new ArrayList<Object>(valueList);
	}

}
