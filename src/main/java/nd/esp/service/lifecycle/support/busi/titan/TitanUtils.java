package nd.esp.service.lifecycle.support.busi.titan;

import java.sql.Timestamp;
//import java.time.Instant;
import java.util.*;

import nd.esp.service.lifecycle.educommon.vos.constant.IncludesConstant;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
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

	public static String addParamToScript(String script, Map<String, Object> scriptParamMap) {
		for (Map.Entry<String, Object> entry : scriptParamMap.entrySet()) {
			String value;
			if (entry.getKey().contains("lc_last_update") || entry.getKey().contains("lc_create_time")) {
				value = entry.getValue().toString();
			} else {
				value = "'" + entry.getValue().toString() + "'";
			}
			script = script.replace(entry.getKey(), value);
		}
		script = script.replace("lc_'true'", "true");
		script = script.replace("'true'", "true");
		return script;
	}

	private final static String[] MOVE_FIELDS = {"primary_category", "lc_enable", "lc_create_time", "lc_last_update", "lc_status", "search_code", "search_path", "search_coverage"};

	/**
	 * 优化：把过滤条件移到边上
	 * @param script
	 * @param reverse
     * @return
     */
	public static String optimizeMoveConditionsToEdge(String script, boolean reverse,Map<String, Object> scriptParamMap) {
		String totalCount = script.substring(script.indexOf("TOTALCOUNT=g.V()"), script.indexOf(".count();"));
		String result = script.substring(script.indexOf("RESULT=g.V()"), script.indexOf(".valueMap(true);"));
		String vConditions, prefix;
		if (reverse) {
			prefix = "source_r_";
			vConditions = totalCount.substring(script.indexOf(".outV()"), script.indexOf(".as('x')"));
		} else {
			prefix = "target_r_";
			vConditions = totalCount.substring(script.indexOf(".inV()"), script.indexOf(".as('x')"));
		}
		String optimizeScript = moveConditionsToEdge(vConditions, prefix,scriptParamMap);
		script = script.replace(totalCount, totalCount.replace(".as('e')" + vConditions, optimizeScript));
		script = script.replace(result, result.replace(".as('e')" + vConditions, optimizeScript));
		return script;
	}

	/**
	 * primary_category,lc_enable,lc_create_time,lc_last_update,lc_status,search_code_string,search_path_string,search_coverage_string
	 * @param vConditions
	 * @return
     */
	private static String moveConditionsToEdge(String vConditions, String prefix, Map<String, Object> scriptParamMap) {
		String[] conditions = vConditions.split("\\.");
		Map<String, String> optimizeConditions = optimizeConditions(conditions, scriptParamMap);
		StringBuffer eConditions = new StringBuffer();
		for (String condition : conditions) {
			for (String field : MOVE_FIELDS) {
				if (condition.contains(field)) {
					String suffix = "";
					if ("search_code".equals(field) || "search_path".equals(field) || "search_coverage".equals(field)) suffix = "_string";
					vConditions = vConditions.replace("." + condition, "");// 点上把这个条件移除
					String eCondition = optimizeConditions.get(condition).replace("'" + field + "'", "'" + prefix + field + suffix + "'");
					eConditions.append(".").append(eCondition);// 边上加上这个条件
					optimizeConditions.remove(condition);
				}
			}
		}
		// 优化点上剩余条件
		if (CollectionUtils.isNotEmpty(optimizeConditions)) {
			for (Map.Entry<String, String> entry : optimizeConditions.entrySet()) {
				vConditions = vConditions.replace(entry.getKey(), entry.getValue());
			}
		}

		return eConditions.append(".as('e')").append(vConditions).toString();
	}

	/**
	 *
	 * @param tmpConditions
	 * @param scriptParamMap
     * @return
     */
	private static Map<String, String> optimizeConditions(String[] tmpConditions, Map<String, Object> scriptParamMap) {
		Map<String, String> conditions = new HashMap<>();
		for (String c : tmpConditions) {
			if (!"".equals(c) && !"outV()".equals(c) && !"inV()".equals(c)) {
				// 暂时只处理 or
				String value = null;
				if (c.startsWith("or(")) {
					// or(has('search_code',search_code0))
					if (CommonHelper.getSubStrAppearTimes(c, "has") == 1) {
						value = c.substring(3, c.length() - 1);
					} else {
						value = c;
					}

				} else if (c.contains("'search_coverage'")) {
					int size = CommonHelper.getSubStrAppearTimes(c, "search_coverage") - 1;
					//coverage = "[\\S\\s]*" + coverage + "[\\S\\s]*";
					scriptParamMap.put("search_coverage0", "[\\S\\s]*" + "\\\"" + scriptParamMap.get("search_coverage0").toString().toLowerCase() + "\\\"" + "[\\S\\s]*");
					if (size == 1) {
						value = "has('search_coverage',textRegex(search_coverage0))";
					} else {
						value = "or(has('search_coverage',textRegex(search_coverage0))";
						for (int i = 1; i < size; i++) {
							value = value + ",has('search_coverage',textRegex(search_coverage" + i + "))";
							scriptParamMap.put("search_coverage" + i, "[\\S\\s]*" + "\\\"" + scriptParamMap.get("search_coverage" + i).toString().toLowerCase() + "\\\"" + "[\\S\\s]*");
						}
						value = value + ")";
					}
				} else {
					value = c;
				}
				conditions.put(c, value);
			}
		}
		return conditions;
	}

	/**
	 * 优化多个关系时的查询脚本
	 * 暂时只处理两个关系的查询
	 * @param script
	 * @return
	 */
	public static String optimizeMultiRelationsQuery(String script) {
		String totalCount = script.substring(script.indexOf("TOTALCOUNT=g.V()"), script.indexOf(".count();"));
		String result = script.substring(script.indexOf("RESULT=g.V()"), script.indexOf(".valueMap(true);"));
		Map<String, String> mapForTotalCount = fetchScript(totalCount);
		Map<String, String> mapForResult = fetchScript(result);

		StringBuffer scriptBuffer = new StringBuffer("Set<Long> ids0=new HashSet<Long>();Set<Long> retain=new HashSet<Long>();");
		// TODO 获取交集
		scriptBuffer.append(mapForTotalCount.get("relation1")).append(".collect{ids0.add(it.id())};");
		scriptBuffer.append("if(ids0.size()==0){return 'TOTALCOUNT=0'};");
		List<String> relations = dealRelation(mapForTotalCount.get("relation2"));
		int size = relations.size();
		for (int i = 0; i < size; i++) {
			String id = "ids" + (i + 1);
			scriptBuffer.append("Set<Long> ").append(id).append("=new HashSet<Long>();");
			scriptBuffer.append(relations.get(i)).append(".collect{").append(id).append(".add(it.id())};");
			scriptBuffer.append("if(").append(id).append(".size()==0){return 'TOTALCOUNT=0'};");
		}
		scriptBuffer.append("if(ids0.size()<ids1.size()){ids0.retainAll(ids1);retain=ids0;}else{ids1.retainAll(ids0);retain=ids1;};");
		scriptBuffer.append("if(retain.size()==0){return 'TOTALCOUNT=0'};");
		if (size > 1) {
			for (int i = 1; i < size; i++) {
				String id = "ids" + (i + 1);
				scriptBuffer.append("if(retain.size()<").append(id).append(".size()){retain.retainAll(").append(id).append(");}else{").append(id).append(".retainAll(retain);retain=").append(id).append(";};");
				scriptBuffer.append("if(retain.size()==0){return 'TOTALCOUNT=0'};");
			}
		}

		// TODO 加上过滤条件
		scriptBuffer.append("TOTALCOUNT=g.V(retain.toArray())").append(mapForTotalCount.get("conditions")).append(".as('x')").append(mapForTotalCount.get("script")).append(".count();");
		// TODO 取得 count
		scriptBuffer.append("long count=TOTALCOUNT.toList()[0];if(count==0){return 'TOTALCOUNT=0'};");
		// TODO 查询结果数据
		scriptBuffer.append("RESULT=g.V(retain.toArray())").append(mapForResult.get("conditions")).append(".as('x')").append(mapForResult.get("script")).append(".valueMap(true);");
		// TODO 处理返回数据
		scriptBuffer.append("List<Object> resultList=RESULT.toList();resultList << 'TOTALCOUNT='+count;");

		//System.out.println(scriptBuffer.toString());
		return scriptBuffer.toString();
	}

	/**
	 * .and(inE(has_relation1).has('enable',enable1).as('e').outV().has('identifier',identifier1).has('lc_enable',lc_enable2).has('primary_category',primary_category2),inE(has_relation2).has('enable',enable2).as('e').outV().has('identifier',identifier2).has('lc_enable',lc_enable3).has('primary_category',primary_category3))
	 * .and(inE(has_relation1).has('enable',enable1).as('e').outV().has('identifier',identifier1).has('lc_enable',lc_enable2).has('primary_category',primary_category2))
	 * @param relation
	 * @return
	 */
	private static List<String> dealRelation(String relation) {
		List<String> list = new ArrayList<>();
		if (relation.startsWith(".and(")) relation = relation.replace(".and(", "");
		if (relation.endsWith("))")) relation = relation.substring(0, relation.length() - 1);
		// ), --> )),
		relation = relation.replaceAll("\\),", "\\)),");
		String[] relations = relation.split("\\),");
		for (String r : relations) {
			StringBuffer scriptBuffer = new StringBuffer("g.V()");
			String[] ev = r.split(".as\\('e'\\)");
			String e = ev[0];
			String v = ev[1];
			v = v.substring(v.indexOf(".has("), v.length());
			if (r.contains("inE(has_relation")) {
				//e = e.substring(e.indexOf("inE(has_relation"), e.length());
				scriptBuffer.append(v);
				e = e.replace("inE(", "outE(");
				scriptBuffer.append(".").append(e).append(".inV()");
			} else if (r.contains("outE(has_relation")) {
				//e = e.substring(e.indexOf("outE(has_relation"), e.length());
				scriptBuffer.append(v);
				e = e.replace("outE(", "inE(");
				scriptBuffer.append(".").append(e).append(".outV()");
			}/*else{

		}*/
			list.add(scriptBuffer.toString());
		}


		return list;
	}

	/**
	 * @param script
	 * @return
	 */
	private static Map<String, String> fetchScript(String script) {
		String relation1 = script.substring(script.indexOf("g.V().has"), script.indexOf(".as('e')"));
		script = script.replace(relation1, "");
		if (relation1.contains("outE")) {
			relation1 = relation1 + ".inV()";
		} else if (relation1.contains("inE")) {
			relation1 = relation1 + ".outV()";
		}
		// 条件
		String conditions = script.substring(script.indexOf(".has("), script.indexOf(".as('x')"));
		script = script.replace(conditions, "");
		script = script.substring(script.indexOf(".and("), script.length());

		String relation2 = null;
		if (script.contains(".and(inE(")) {
			relation2 = script.substring(script.indexOf(".and(inE("), script.indexOf(".select('x')"));
		} else if (script.contains(".and(outE(")) {
			relation2 = script.substring(script.indexOf(".and(outE("), script.indexOf(".select('x')"));
		}
		if (relation2 != null) script = script.replace(relation2, "");

		Map<String, String> map = new HashMap<>();

		map.put("relation1", relation1);
		map.put("conditions", conditions);
		map.put("relation2", relation2);
		map.put("script", script);
		//System.out.println(relation1);
		//System.out.println(conditions);
		//System.out.println(relation2);
		//System.out.println(script);
		return map;
	}


	/**
	 * 处理orderMap 返回一个有序的 order排序
	 * @param orderMap
	 * @param showVersion
	 * @param reverse
	 * @param relations
	 * @param statisticsType
	 * @param statisticsPlatform
     * @return
     */
	public static Map<String, String> dealOrderMap(Map<String, String> orderMap, boolean showVersion, boolean reverse, List<Map<String, String>> relations, String statisticsType, String statisticsPlatform) {
		Map<String, String> orders = new LinkedHashMap<>();
		// show_version
		if (showVersion) {
			orders.put(ES_SearchField.m_identifier.toString(), "asc");
			if (CollectionUtils.isEmpty(orderMap)) orders.put(ES_SearchField.lc_version.toString(), "asc");
		}
		// add orderMap
		if (CollectionUtils.isNotEmpty(orderMap)) {
			Set<String> orderFields = orderMap.keySet();
			for (String field : orderFields) {
				String value = orderMap.get(field);
				if (TitanOrderFields.sta_key_value.toString().equals(field))
					value = value + "#" + statisticsType + "#" + statisticsPlatform;
				orders.put(field, value);
			}
		}
		// sort_num
		if (!isOrderBySortNum(reverse, orderMap, relations)) orders.remove(TitanOrderFields.sort_num.toString());

		// 默认排序
		if (CollectionUtils.isEmpty(orders)) orders.put(ES_SearchField.lc_create_time.toString(),"desc");

		return orders;
	}

	/**
	 * 新增支持sort_num的排序，目的是提供根据资源关系创建顺序自定义排序的能力     new-2016.04.07
	 * 1）当reverse=false，relation参数有且只有一个的时候该排序参数生效
	 * 2）目前资源仅支持 assets
	 * <p/>
	 * 资源未做限制
	 *
	 * @param reverse
	 * @param orderMap
	 * @param relations
	 * @return
	 */
	public static boolean isOrderBySortNum(boolean reverse, Map<String, String> orderMap, List<Map<String, String>> relations) {
		if (reverse) return false;
		if (CollectionUtils.isEmpty(orderMap)) return false;
		if (!orderMap.containsKey(TitanKeyWords.sort_num.toString())) return false;
		if (CollectionUtils.isEmpty(relations)) return false;
		if (relations.size() != 1) return false;
		return true;
	}
	/**
	 * 获取字段所对应的es数据类型
	 * @param field
	 * @return
     */
	public static String convertToEsDataType(String field) {
		if (ES_SearchField.lc_last_update.toString().equals(field) || ES_SearchField.lc_create_time.toString().equals(field)) {
			return convertToEsDataType(Long.class);
		} else if (ES_SearchField.title.toString().equals(field)) {
			return convertToEsDataType(String.class);
		}
		return null;
	}

	public static String convertToEsDataType(Class<?> dataType) {
		if(String.class.isAssignableFrom(dataType)) {
			return "string";
		}
		else if (Integer.class.isAssignableFrom(dataType)) {
			return "integer";
		}
		else if (Long.class.isAssignableFrom(dataType)) {
			return "long";
		}
		else if (Float.class.isAssignableFrom(dataType)) {
			return "float";
		}
		else if (Double.class.isAssignableFrom(dataType)) {
			return "double";
		}
		else if (Boolean.class.isAssignableFrom(dataType)) {
			return "boolean";
		}
		else if (Date.class.isAssignableFrom(dataType)) {
			return "date";
		}
		/*else if (Instant.class.isAssignableFrom(dataType)) {
			return "date";
		}*/
		/*else if (Geoshape.class.isAssignableFrom(datatype)) {
			return "geo_point";
		}*/

		return null;
	}

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
		Set<String> statisticsScriptSet = new HashSet<>();
		statisticsScriptSet.add(statisticsScript);
		return generateScriptForInclude(includes, resTypeSet, relationQuery, needStatistics, statisticsScriptSet);
	}

	/**
	 *
	 * @param includes
	 * @param resTypeSet
	 * @param relationQuery （queryListByResType、batchQueryResources）
	 * @param needStatistics 需要统计数据
	 * @param statisticsScriptSet 统计数据脚本
     * @return
     */
	public static String generateScriptForInclude(List<String> includes, Set<String> resTypeSet, boolean relationQuery, boolean needStatistics, Set<String> statisticsScriptSet) {
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
			for (String statisticsScript : statisticsScriptSet) {
				scriptBuffer.append(statisticsScript);
			}
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
		/*Set<String> set = new HashSet<>();
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
		System.out.println(generateScriptForInclude(null, set, true,false,null));*/
		optimizeMultiRelationsQuery("TOTALCOUNT=g.V().has('identifier',identifier0).has('lc_enable',lc_enable0).has('primary_category',primary_category0).outE(has_relation0).has('enable',enable0).as('e').inV().has('lc_enable',lc_enable1).has('primary_category',primary_category1).has('search_coverage',within(search_coverage0)).has('lc_status',within(lc_status0)).or(has('search_code',search_code0)).as('x').select('x').and(inE(has_relation1).has('enable',enable1).as('e').outV().has('identifier',identifier1).has('lc_enable',lc_enable2).has('primary_category',primary_category2)).select('x').count();long count=TOTALCOUNT.toList()[0];if(count==0){return 'TOTALCOUNT=0'};RESULT=g.V().has('identifier',identifier0).has('lc_enable',lc_enable0).has('primary_category',primary_category0).outE(has_relation0).has('enable',enable0).as('e').inV().has('lc_enable',lc_enable1).has('primary_category',primary_category1).has('search_coverage',within(search_coverage0)).has('lc_status',within(lc_status0)).or(has('search_code',search_code0)).as('x').select('x').and(inE(has_relation1).has('enable',enable1).as('e').outV().has('identifier',identifier1).has('lc_enable',lc_enable2).has('primary_category',primary_category2)).select('x').order().by('lc_create_time',decr).select('x').range(0,10).as('v').union(select('v'),outE('has_category_code')).valueMap(true);List<Object> resultList=RESULT.toList();resultList << 'TOTALCOUNT='+count;");
		System.out.println("------------");
		optimizeMultiRelationsQuery("TOTALCOUNT=g.V().has('identifier',identifier0).has('lc_enable',lc_enable0).has('primary_category',primary_category0).outE(has_relation0).has('enable',enable0).as('e').inV().has('lc_enable',lc_enable1).has('primary_category',primary_category1).has('search_coverage',within(search_coverage0)).has('lc_status',within(lc_status0)).or(has('search_code',search_code0)).as('x').select('x').and(inE(has_relation1).has('enable',enable1).as('e').outV().has('identifier',identifier1).has('lc_enable',lc_enable2).has('primary_category',primary_category2)).select('x').aggregate('subversion').emit().repeat(outE('has_relation').has('res_type',within('instructionalobjectives')).has('relation_type','VERSION').inV().aggregate('subversion')).times(1).select('subversion').unfold().dedup().as('version_result').select('version_result').count();long count=TOTALCOUNT.toList()[0];if(count==0){return 'TOTALCOUNT=0'};RESULT=g.V().has('identifier',identifier0).has('lc_enable',lc_enable0).has('primary_category',primary_category0).outE(has_relation0).has('enable',enable0).as('e').inV().has('lc_enable',lc_enable1).has('primary_category',primary_category1).has('search_coverage',within(search_coverage0)).has('lc_status',within(lc_status0)).or(has('search_code',search_code0)).as('x').select('x').and(inE(has_relation1).has('enable',enable1).as('e').outV().has('identifier',identifier1).has('lc_enable',lc_enable2).has('primary_category',primary_category2)).select('x').aggregate('subversion').emit().repeat(outE('has_relation').has('res_type',within('instructionalobjectives')).has('relation_type','VERSION').inV().aggregate('subversion')).times(1).select('subversion').unfold().dedup().as('version_result').select('version_result').choose(select('version_result').has('lc_version'),select('version_result').values('lc_version'),__.constant('')).order().by(incr).select('version_result').choose(select('version_result').has('m_identifier'),select('version_result').values('m_identifier'),__.constant('')).order().by(incr).select('version_result').range(0,10).as('v').union(select('v'),outE('has_category_code')).valueMap(true);List<Object> resultList=RESULT.toList();resultList << 'TOTALCOUNT='+count;");
		System.out.println("------------");
		optimizeMultiRelationsQuery("TOTALCOUNT=g.V().has('identifier',identifier0).has('lc_enable',lc_enable0).has('primary_category',primary_category0).outE(has_relation0).has('enable',enable0).as('e').inV().has('lc_enable',lc_enable1).has('primary_category',primary_category1).has('search_coverage',within(search_coverage0)).has('lc_status',within(lc_status0)).or(has('search_code',search_code0)).as('x').select('x').and(inE(has_relation1).has('enable',enable1).as('e').outV().has('identifier',identifier1).has('lc_enable',lc_enable2).has('primary_category',primary_category2)).select('x').aggregate('subversion').emit().repeat(outE('has_relation').has('res_type',within('instructionalobjectives')).has('relation_type','VERSION').inV().aggregate('subversion')).times(1).select('subversion').unfold().dedup().as('version_result').select('version_result').outE('has_tech_info').has('ti_printable',true).has('ti_title','source').select('version_result').dedup().select('version_result').count();long count=TOTALCOUNT.toList()[0];if(count==0){return 'TOTALCOUNT=0'};RESULT=g.V().has('identifier',identifier0).has('lc_enable',lc_enable0).has('primary_category',primary_category0).outE(has_relation0).has('enable',enable0).as('e').inV().has('lc_enable',lc_enable1).has('primary_category',primary_category1).has('search_coverage',within(search_coverage0)).has('lc_status',within(lc_status0)).or(has('search_code',search_code0)).as('x').select('x').and(inE(has_relation1).has('enable',enable1).as('e').outV().has('identifier',identifier1).has('lc_enable',lc_enable2).has('primary_category',primary_category2)).select('x').aggregate('subversion').emit().repeat(outE('has_relation').has('res_type',within('instructionalobjectives')).has('relation_type','VERSION').inV().aggregate('subversion')).times(1).select('subversion').unfold().dedup().as('version_result').select('version_result').outE('has_tech_info').has('ti_printable',true).has('ti_title','source').select('version_result').dedup().select('version_result').choose(select('version_result').has('lc_version'),select('version_result').values('lc_version'),__.constant('')).order().by(incr).select('version_result').choose(select('version_result').has('m_identifier'),select('version_result').values('m_identifier'),__.constant('')).order().by(incr).select('version_result').range(0,10).as('v').union(select('v'),outE('has_category_code')).valueMap(true);List<Object> resultList=RESULT.toList();resultList << 'TOTALCOUNT='+count;");
		System.out.println("------------");
		optimizeMultiRelationsQuery("TOTALCOUNT=g.V().has('identifier',identifier0).has('lc_enable',lc_enable0).has('primary_category',primary_category0).outE(has_relation0).has('enable',enable0).as('e').inV().has('lc_enable',lc_enable1).has('primary_category',primary_category1).has('search_coverage',within(search_coverage0)).has('lc_status',within(lc_status0)).or(has('search_code',search_code0)).as('x').select('x').and(inE(has_relation1).has('enable',enable1).as('e').outV().has('identifier',identifier1).has('lc_enable',lc_enable2).has('primary_category',primary_category2)).select('x').outE('has_tech_info').has('ti_printable',true).has('ti_title','source').select('x').dedup().select('x').count();long count=TOTALCOUNT.toList()[0];if(count==0){return 'TOTALCOUNT=0'};RESULT=g.V().has('identifier',identifier0).has('lc_enable',lc_enable0).has('primary_category',primary_category0).outE(has_relation0).has('enable',enable0).as('e').inV().has('lc_enable',lc_enable1).has('primary_category',primary_category1).has('search_coverage',within(search_coverage0)).has('lc_status',within(lc_status0)).or(has('search_code',search_code0)).as('x').select('x').and(inE(has_relation1).has('enable',enable1).as('e').outV().has('identifier',identifier1).has('lc_enable',lc_enable2).has('primary_category',primary_category2)).select('x').outE('has_tech_info').has('ti_printable',true).has('ti_title','source').select('x').dedup().select('x').order().by('lc_create_time',decr).select('x').range(0,10).as('v').union(select('v'),outE('has_category_code')).valueMap(true);List<Object> resultList=RESULT.toList();resultList << 'TOTALCOUNT='+count;");


	}

}
