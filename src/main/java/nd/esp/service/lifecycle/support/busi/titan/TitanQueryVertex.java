package nd.esp.service.lifecycle.support.busi.titan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.utils.StringUtils;

import org.springframework.http.HttpStatus;

/**
 * 点条件
 * @author linsm
 *
 */
public class TitanQueryVertex extends TitanQueryElement {

	private TitanDirection titanVertexDirection = TitanDirection.no;
	private String vertexLabel;

	public TitanDirection getTitanDirection() {
		return titanVertexDirection;
	}

	public void setTitanDirection(TitanDirection titanDirection) {
		this.titanVertexDirection = titanDirection;
	}

	public void setVertexLabel(String vertexLabel) {
		this.vertexLabel = vertexLabel;
	}

	public String getVertexLabel() {
		return vertexLabel;
	}

	@Override
	public String generateScript(Map<String, Object> scriptParamMap) {
//		if (StringUtils.isEmpty(vertexLabel)) {
//			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
//					"LC/QUERY/PARAM", "vertexLabel");
//		}
		String head = null;
		switch (this.titanVertexDirection) {
		case no:
			head = TitanKeyWords.V.toString();
			break;
		case in:
			head = TitanKeyWords.inV.toString();
			break;
		case out:
			head = TitanKeyWords.outV.toString();
			break;
		default:
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					"LC/QUERY/PARAM", "titanVertexDirection");
		}
		
		if (StringUtils.isEmpty(vertexLabel)){
			return head+"()"+ super.generateScript(scriptParamMap);
		}else{
			return head + appendVertexLabel(scriptParamMap)
					+ super.generateScript(scriptParamMap);
		}
		
	}

	protected String appendVertexLabel(Map<String, Object> scriptParamMap) {
		String script = new String("().");
		script += TitanKeyWords.hasLabel.toString() + "(";
		String key = TitanUtils.generateKey(scriptParamMap, vertexLabel);
		scriptParamMap.put(key, vertexLabel);
		script += key;
		script += ")";
		return script;
	}

	/************************************ TEST *********************************/
	public static void main(String[] args) {
		testGenerateScript();
	}

	private static void testGenerateScript() {

		Map<String, Object> scriptParamMap = new HashMap<String, Object>();

		System.out
				.println(generateTestExample().generateScript(scriptParamMap));
		System.out.println(scriptParamMap);

	}

	public static TitanQueryVertex generateTestExample() {
		TitanQueryVertex titanQueryVertex = new TitanQueryVertex();
		titanQueryVertex.setTitanDirection(TitanDirection.in);
		String vertexLabel = "chapters";
		Map<String, Object> edgePropertiesMap = new HashMap<String, Object>();
		edgePropertiesMap.put("enable", false);
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

		titanQueryVertex.setVertexLabel(vertexLabel);
		titanQueryVertex.setPropertiesMap(vertexPropertiesMap);
		return titanQueryVertex;
	}

}
