package nd.esp.service.lifecycle.support.busi.titan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;

import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.utils.StringUtils;
/**
 * 边条件
 * @author linsm
 *
 */
public class TitanQueryEdge extends TitanQueryElement {
	private TitanDirection titanEdgeDirection = TitanDirection.out;

	private String edgeLabel;

	public TitanDirection getTitanEdgeDirection() {
		return titanEdgeDirection;
	}

	public void setTitanEdgeDirection(TitanDirection titanEdgeDirection) {
		this.titanEdgeDirection = titanEdgeDirection;
	}

	public String getEdgeLabel() {
		return edgeLabel;
	}

	public void setEdgeLabel(String edgeLabel) {
		this.edgeLabel = edgeLabel;
	}

	@Override
	public String generateScript(Map<String, Object> scriptParamMap) {
		if (StringUtils.isEmpty(edgeLabel) || scriptParamMap == null) {
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					"LC/QUERY/PARAM", "edgeLabel||scriptParamMap");
		}
		StringBuffer scriptBuffer = new StringBuffer();
		switch (titanEdgeDirection) {
		case out:
			scriptBuffer.append(TitanKeyWords.outE.toString())
					.append(appendEdgeLabel(scriptParamMap))
					.append(appendConditions(scriptParamMap));
			// .append(".")
			// .append(TitanKeyWords.inV.toString())
			// .append(appendVertexLabel(scriptParamMap))
			// .append(appendConditions(scriptParamMap));
			break;
		case in:
			scriptBuffer.append(TitanKeyWords.inE.toString())
					.append(appendEdgeLabel(scriptParamMap))
					.append(appendConditions(scriptParamMap));
			// .append(".")
			// .append(TitanKeyWords.outV.toString())
			// .append(super.generateScript(scriptParamMap));
			break;
		default:
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					"LC/QUERY/PARAM", "titanEdgeDirection");
		}
		// 加上取回边上关系的数据
		return scriptBuffer.toString()+".as('e')";
	}

	private String appendEdgeLabel(Map<String, Object> scriptParamMap) {
		String script = new String("(");
		String key = TitanUtils.generateKey(scriptParamMap, edgeLabel);
		script += key;
		script += ")";
		scriptParamMap.put(key, edgeLabel);
		return script;

	}

	/****************************** TEST *******************************/
	public static void main(String[] args) {
		testGenerateScript();
	}

	private static void testGenerateScript() {

		Map<String, Object> scriptParamMap = new HashMap<String, Object>();

		System.out
				.println(generateTestExample().generateScript(scriptParamMap));
		System.out.println(scriptParamMap);

	}

	public static TitanQueryEdge generateTestExample() {
		TitanQueryEdge titanQueryEdge = new TitanQueryEdge();
		TitanDirection titanEdgeDirection = TitanDirection.out;
		String edgeLabel = "has_relation";
		Map<String, Map<Titan_OP, List<Object>>> edgePropertiesMap = new HashMap<String, Map<Titan_OP, List<Object>>>();
		Map<Titan_OP, List<Object>> enableConditions = new HashMap<Titan_OP, List<Object>>();
		List<Object> enableEqConditions = new ArrayList<Object>();
		enableEqConditions.add("true");
		enableEqConditions.add("false");
		enableConditions.put(Titan_OP.eq, enableEqConditions);
		edgePropertiesMap.put("enable", enableConditions);

		Map<Titan_OP, List<Object>> identifierConditions = new HashMap<Titan_OP, List<Object>>();
		List<Object> identifierNeqConditions = new ArrayList<Object>();
		identifierNeqConditions.add("b3a866d5-f2ab-4dd5-89fb-ebba073496fb");
		identifierNeqConditions.add("b3a866d5-f2ab-4dd5-89fb-ebba073496fb");
		identifierConditions.put(Titan_OP.ne, identifierNeqConditions);
		List<Object> identifierEqConditions = new ArrayList<Object>();
		identifierEqConditions.add("111");
		identifierEqConditions.add("222");
		identifierConditions.put(Titan_OP.eq, identifierEqConditions);
		edgePropertiesMap.put("identifier", identifierConditions);

		titanQueryEdge.setEdgeLabel(edgeLabel);
		// FIXME
		titanQueryEdge.setPropertiesMap(edgePropertiesMap);
		titanQueryEdge.setTitanEdgeDirection(titanEdgeDirection);
		return titanQueryEdge;
	}

}
