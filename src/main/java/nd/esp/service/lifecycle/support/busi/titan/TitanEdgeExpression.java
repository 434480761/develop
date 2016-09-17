package nd.esp.service.lifecycle.support.busi.titan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 某种类型关系整体（如category,coverage,relation):  通过titan logic op 组成一颗条件树。
 * @author linsm
 *
 */
public class TitanEdgeExpression implements TitanScriptGenerator {

	// and, or
	private TitanOp titanOp = TitanOp.and;

	// List<TitanQueryEdge> simpleConditions = new ArrayList<TitanQueryEdge>();
	//
	// List<TitanEdgeExpression> compositeConditions = new
	// ArrayList<TitanEdgeExpression>();
	//条件树的一层，结点由TitanEdgeExpression 和 TitanEdgeQueryEdgeAndVertex通过操作符组成
	private List<TitanScriptGenerator> conditions = new ArrayList<TitanScriptGenerator>();

	@Override
	public Boolean isNotHavingAnyCondition() {
		return conditions.size() == 0;
	}

	public void setTitanOp(TitanOp titanOp) {
		this.titanOp = titanOp;
	}

	/**
	 * 添加叶子结点
	 * @param titanQueryEdge
	 * @return
	 */
	public TitanEdgeExpression addCondition(
			TitanQueryEdgeAndVertex titanQueryEdge) {
		conditions.add(titanQueryEdge);
		return this;
	}

	/**
	 * 添加内部结点
	 * @param compositeCondition
	 * @return
	 */
	public TitanEdgeExpression addCondition(
			TitanEdgeExpression compositeCondition) {
		conditions.add(compositeCondition);
		return this;
	}

	@Override
	public String generateScript(Map<String, Object> scriptParamMap) {
		if (conditions.size() == 0) {
			// throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
			// "LC/Edge/Expression", "LC/Edge/Expression");
			return "";
			// } else if (conditions.size() == 1) {
			//
			// // FIXME there is a problem with
			// // 当存在多条边里，b
			// // 会返回重复的数据(tools,4a1c381d-133f-4268-963a-233d889e8f1e)--脏数据的情况
			// // a,
			// //
			// g.V().hasLabel('assets').has('enable',true).as('x').select('x').or(outE('has_coverage').inV().hasLabel('coverage').has('target_type','User').has('target','890399')).select('x').count()
			// // b,
			// //
			// g.V().hasLabel('assets').has('enable',true).as('x').select('x').outE('has_coverage').inV().hasLabel('coverage').has('target_type','User').has('target','890399').select('x').count()
			// return conditions.get(0).generateScript(scriptParamMap);
		} else {
			StringBuffer scriptBuffer = new StringBuffer(titanOp.toString());
			scriptBuffer.append("(");
			for (TitanScriptGenerator titanQueryEdge : conditions) {
				scriptBuffer.append(titanQueryEdge
						.generateScript(scriptParamMap));
				scriptBuffer.append(",");
			}

			// FIXME remove the last "," if existed;
			if (scriptBuffer.lastIndexOf(",") == scriptBuffer.length() - 1) {
				scriptBuffer.deleteCharAt(scriptBuffer.length() - 1);
			}

			scriptBuffer.append(")");
			return scriptBuffer.toString();

		}
	}

	/******************************** TEST ******************************/
	public static void main(String[] args) {

		Map<String, Object> scriptParamMap = new HashMap<String, Object>();
		System.out
				.println(generateTestExample().generateScript(scriptParamMap));
		System.out.println(scriptParamMap);
	}

	public static TitanEdgeExpression generateTestExample() {
		TitanEdgeExpression titanEdgeExpression = new TitanEdgeExpression();

		// titanEdgeExpression.addCondition(TitanQueryEdge.generateTestExample());
		titanEdgeExpression.addCondition(TitanQueryEdgeAndVertex
				.generateTestExample());

		// TitanEdgeExpression subTitanEdgeExpression = new
		// TitanEdgeExpression();
		// // subTitanEdgeExpression.addCondition(TitanQueryEdge
		// // .generateTestExample());
		// subTitanEdgeExpression.addCondition(TitanQueryEdgeAndVertex
		// .generateTestExample());
		//
		// titanEdgeExpression.addCondition(subTitanEdgeExpression);
		// titanEdgeExpression.addCondition(subTitanEdgeExpression);

		return titanEdgeExpression;
	}

	public static enum TitanOp {
		and, or,
		// eq, // only one condition
	}
}
