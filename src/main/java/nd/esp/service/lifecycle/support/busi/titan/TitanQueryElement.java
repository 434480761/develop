package nd.esp.service.lifecycle.support.busi.titan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;

import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.utils.CollectionUtils;

/**
 * 查询基础模块：包含属性条件
 * @author linsm
 *
 */
public class TitanQueryElement implements TitanScriptGenerator {
	
	// field->Titan_OP->values：属性map
	private Map<String, Map<Titan_OP, List<Object>>> propertiesMap = new HashMap<String, Map<Titan_OP, List<Object>>>();

	public Map<String, Map<Titan_OP, List<Object>>> getPropertiesMap() {
		return propertiesMap;
	}

	public void setPropertiesMap(
			Map<String, Map<Titan_OP, List<Object>>> propertiesMap) {
		this.propertiesMap = propertiesMap;
	}

	@Override
	public String generateScript(Map<String, Object> scriptParamMap) {
		if (scriptParamMap == null) {
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					"LC/QUERY/PARAM", "scriptParamMap");
		}
		return appendConditions(scriptParamMap);
	}

	protected String appendConditions(Map<String, Object> scriptParamMap) {
		return appendConditions(scriptParamMap, propertiesMap);
	}

	protected String appendConditions(Map<String, Object> scriptParamMap,
			Map<String, Map<Titan_OP, List<Object>>> propertiesMap) {
		if (CollectionUtils.isEmpty(propertiesMap)) {
			return "";
		}
		StringBuffer scriptBuffer = new StringBuffer();
		for (Map.Entry<String, Map<Titan_OP, List<Object>>> fieldEntry : propertiesMap
				.entrySet()) {
			if (CollectionUtils.isEmpty(fieldEntry.getValue())) {
				// not have any Titan_OP;
				continue;
			}
			for (Map.Entry<Titan_OP, List<Object>> titanOpEntry : fieldEntry
					.getValue().entrySet()) {
				scriptBuffer.append(titanOpEntry.getKey().generateScipt(
						fieldEntry.getKey(), titanOpEntry.getValue(),
						scriptParamMap));
			}

		}

		return scriptBuffer.toString();
	}

	/************************************ TEST *********************************/
	public static void main(String[] args) {
		testGenerateScript();
	}

	private static void testGenerateScript() {
		TitanQueryElement titanQueryEdge = new TitanQueryElement();
		Map<String, Map<Titan_OP, List<Object>>> vertexPropertiesMap = new HashMap<String, Map<Titan_OP, List<Object>>>();
		Map<Titan_OP, List<Object>> enableConditions = new HashMap<Titan_OP, List<Object>>();
		List<Object> enableEqConditions = new ArrayList<Object>();
		enableEqConditions.add("true");
		enableEqConditions.add("false");
		enableConditions.put(Titan_OP.eq, enableEqConditions);
		vertexPropertiesMap.put("enable", enableConditions);

		Map<Titan_OP, List<Object>> identifierConditions = new HashMap<Titan_OP, List<Object>>();
		List<Object> identifierNeqConditions = new ArrayList<Object>();
		identifierNeqConditions.add("b3a866d5-f2ab-4dd5-89fb-ebba073496fb");
		identifierNeqConditions.add("b3a866d5-f2ab-4dd5-89fb-ebba073496fb");
		identifierConditions.put(Titan_OP.ne, identifierNeqConditions);
		List<Object> identifierEqConditions = new ArrayList<Object>();
		identifierEqConditions.add("111");
		identifierEqConditions.add("222");
		identifierConditions.put(Titan_OP.eq, identifierEqConditions);
		vertexPropertiesMap.put("identifier", identifierConditions);

		titanQueryEdge.setPropertiesMap(vertexPropertiesMap);

		Map<String, Object> scriptParamMap = new HashMap<String, Object>();

		System.out.println(titanQueryEdge.generateScript(scriptParamMap));
		System.out.println(scriptParamMap);

	}

	@Override
	public Boolean isNotHavingAnyCondition() {
		return true;
	}
}
