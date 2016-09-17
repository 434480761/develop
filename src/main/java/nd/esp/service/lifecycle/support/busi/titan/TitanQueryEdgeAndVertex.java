package nd.esp.service.lifecycle.support.busi.titan;

import java.util.HashMap;
import java.util.Map;

import nd.esp.service.lifecycle.support.LifeCircleException;

import org.springframework.http.HttpStatus;

/**
 * 由边与点组成条件
 * @author linsm
 *
 */
public class TitanQueryEdgeAndVertex implements TitanScriptGenerator {

	private Boolean isEdgeBeforeVertex = true;

	private TitanDirection titanDirection = TitanDirection.out;
	private TitanQueryVertex titanQueryVertex;
	private TitanQueryEdge titanQueryEdge;

	public Boolean getIsEdgeBeforeVertex() {
		return isEdgeBeforeVertex;
	}

	public void setIsEdgeBeforeVertex(Boolean isEdgeBeforeVertex) {
		this.isEdgeBeforeVertex = isEdgeBeforeVertex;
	}

	public TitanDirection getTitanDirection() {
		return titanDirection;
	}

	public void setTitanDirection(TitanDirection titanDirection) {
		this.titanDirection = titanDirection;
	}

	public TitanQueryVertex getTitanQueryVertex() {
		return titanQueryVertex;
	}

	public void setTitanQueryVertex(TitanQueryVertex titanQueryVertex) {
		this.titanQueryVertex = titanQueryVertex;
	}

	public TitanQueryEdge getTitanQueryEdge() {
		return titanQueryEdge;
	}

	public void setTitanQueryEdge(TitanQueryEdge titanQueryEdge) {
		this.titanQueryEdge = titanQueryEdge;
	}

	@Override
	public String generateScript(Map<String, Object> scriptParamMap) {
		if (this.isEdgeBeforeVertex) {
			switch (this.titanDirection) {
			case in:
				this.titanQueryEdge.setTitanEdgeDirection(TitanDirection.in);
				this.titanQueryVertex.setTitanDirection(TitanDirection.out);
				break;
			case out:
				this.titanQueryEdge.setTitanEdgeDirection(TitanDirection.out);
				this.titanQueryVertex.setTitanDirection(TitanDirection.in);
				break;
			default:
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
						"LC/QUERY/PARAM", "titanDirection");
			}
			return this.titanQueryEdge.generateScript(scriptParamMap) + "."
					+ this.titanQueryVertex.generateScript(scriptParamMap);
		} else {
			switch (this.titanDirection) {
			case in:
				this.titanQueryEdge.setTitanEdgeDirection(TitanDirection.out);
				break;
			case out:
				this.titanQueryEdge.setTitanEdgeDirection(TitanDirection.in);
				break;
			default:
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
						"LC/QUERY/PARAM", "titanDirection");
			}
			this.titanQueryVertex.setTitanDirection(TitanDirection.no);
			return this.titanQueryVertex.generateScript(scriptParamMap) + "."
					+ this.titanQueryEdge.generateScript(scriptParamMap);
		}

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

	public static TitanQueryEdgeAndVertex generateTestExample() {
		TitanQueryEdgeAndVertex titanQueryEdgeAndVertex = new TitanQueryEdgeAndVertex();
		titanQueryEdgeAndVertex.setIsEdgeBeforeVertex(true);
		titanQueryEdgeAndVertex.setTitanDirection(TitanDirection.out);
		titanQueryEdgeAndVertex.setTitanQueryEdge(TitanQueryEdge
				.generateTestExample());
		titanQueryEdgeAndVertex.setTitanQueryVertex(TitanQueryVertex
				.generateTestExample());
		return titanQueryEdgeAndVertex;
	}

	@Override
	public Boolean isNotHavingAnyCondition() {
		return true;
	}

}
