package nd.esp.service.lifecycle.support.busi.titan;

import java.sql.Timestamp;
import java.util.*;

import nd.esp.service.lifecycle.educommon.vos.constant.IncludesConstant;
import nd.esp.service.lifecycle.support.enums.ES_SearchField;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;
import nd.esp.service.lifecycle.utils.CollectionUtils;

/**
 * titan 工具类
 * 
 * @author linsm
 *
 */
public class TitanUtils {

	/**
	 *
	 * @param includes
	 * @param resType
	 * @param relationQuery （queryListByResType、batchQueryResources）
	 * @param needStatistics 需要统计数据
	 * @param statisticsScript 统计数据脚本
	 * @return
	 */
	public static String generateScriptForInclude(List<String> includes, String resType, boolean relationQuery, boolean needStatistics, String statisticsScript) {
		Set<String> resTypeSet = new HashSet<>();
		resTypeSet.add(resType);
		return generateScriptForInclude(includes, resTypeSet, relationQuery, needStatistics, statisticsScript);
	}

	/**
	 *
	 * @param includes
	 * @param resTypeSet
	 * @param relationQuery （queryListByResType、batchQueryResources）
	 * @param needStatistics 需要统计数据
	 * @param statisticsScript 统计数据脚本
     * @return
     */
	public static String generateScriptForInclude(List<String> includes, Set<String> resTypeSet, boolean relationQuery, boolean needStatistics, String statisticsScript) {
		StringBuffer scriptBuffer = new StringBuffer();
		String begin = ".as('v').union(select('v')";
		String end = ")";
		String defaultStr=".valueMap(true);";// 取回label

		if(CollectionUtils.isNotEmpty(includes)) {
			for (String include : includes) {
				if (include.equals(IncludesConstant.INCLUDE_TI)) {
					scriptBuffer.append(",out('").append(TitanKeyWords.has_tech_info.toString()).append("')");
				} else if (include.equals(IncludesConstant.INCLUDE_CG)) {
					// code、id和path都从边上取(cg_taxoncode identifier cg_taxonpath)
					scriptBuffer.append(",outE('").append(TitanKeyWords.has_category_code.toString()).append("')");
				}
			}
		}
		// queryListByResType、batchQueryResources（relationQuery=true）这两个查询需要关系边上的数据（但不需要取回tree_has_knowledge）
		if (CollectionUtils.isNotEmpty(resTypeSet)) {
			if (resTypeSet.contains(ResourceNdCode.knowledges.toString()) && !relationQuery) {
				// order
				scriptBuffer.append(",inE('").append(TitanKeyWords.tree_has_knowledge.toString()).append("')");
				// parent
				scriptBuffer.append(",inE('").append(TitanKeyWords.tree_has_knowledge.toString()).append("').outV()");
			}
		}

		if (relationQuery) {
			scriptBuffer.append(",select('e')");
		}

		if (needStatistics) {
			scriptBuffer.append(statisticsScript);
		}

		if ("".equals(scriptBuffer.toString())) return defaultStr;
		return begin + scriptBuffer.toString() + end + defaultStr;
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

	/**
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		Set<String> set = new HashSet<>();
		System.out.println("测试非knowledges");
		set.add(ResourceNdCode.assets.toString());
		System.out.println(generateScriptForInclude(IncludesConstant.getIncludesList(), set, false,false,null));
		System.out.println(generateScriptForInclude(IncludesConstant.getIncludesList(), set, true,false,null));
		System.out.println(generateScriptForInclude(null, set, false,false,null));
		System.out.println(generateScriptForInclude(null, set, true,false,null));

		System.out.println("测试knowledges");
		set.add(ResourceNdCode.knowledges.toString());
		System.out.println(generateScriptForInclude(IncludesConstant.getIncludesList(), set, false,false,null));
		System.out.println(generateScriptForInclude(IncludesConstant.getIncludesList(), set, true,false,null));
		System.out.println(generateScriptForInclude(null, set, false,false,null));
		System.out.println(generateScriptForInclude(null, set, true,false,null));
	}

}
