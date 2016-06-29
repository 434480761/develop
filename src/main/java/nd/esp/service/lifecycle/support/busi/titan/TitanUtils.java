package nd.esp.service.lifecycle.support.busi.titan;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nd.esp.service.lifecycle.support.enums.ES_SearchField;
import nd.esp.service.lifecycle.utils.CollectionUtils;

/**
 * titan 工具类
 * 
 * @author linsm
 *
 */
public class TitanUtils {
	private static final Logger LOG = LoggerFactory.getLogger(TitanUtils.class);
	private static final SimpleDateFormat queryDateFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

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
					try {
						values.add(queryDateFormat.parse(value).getTime());
					} catch (ParseException e) {
						LOG.error(e.getLocalizedMessage());
					}
				}
			}
			return values;
		}

		return new ArrayList<Object>(valueList);
	}

}
