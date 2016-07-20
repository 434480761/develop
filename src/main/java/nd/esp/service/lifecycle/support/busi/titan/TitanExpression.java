package nd.esp.service.lifecycle.support.busi.titan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nd.esp.service.lifecycle.educommon.vos.constant.PropOperationConstant;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.http.HttpStatus;

import nd.esp.service.lifecycle.support.LifeCircleException;

/**
 * 用于产生整个脚本与参数（由TitanQueryVertex, TitanQueryEdgeAndVertex,TitanEdgeExpression)
 *
 * @author linsm
 */
public class TitanExpression implements TitanScriptGenerator {

    private boolean needTechInfo = true;
    private boolean needTaxoncode = true;
    private boolean needTaxonpath = true;

    private String innerCondition;// 用于保存中间产生的条件；主要避免重复产生脚本：总数，分页

    private Map<String, String> orderMap;
    private int from;
    private int end;

    public void setOrderMap(Map<String, String> orderMap) {
        this.orderMap = orderMap;
    }

    public void setRange(int from, int size) {
        this.from = from;
        this.end = size + from;
    }

    private TitanQueryEdgeAndVertex firstTitanQueryEdgeAndVertex;

    // relation, coverage,category
    private List<TitanScriptGenerator> edgeExpressions = new ArrayList<TitanScriptGenerator>();
    // resource condition
    private TitanQueryVertex titanQueryVertex;

    public TitanQueryEdgeAndVertex getFirstTitanQueryEdgeAndVertex() {
        return firstTitanQueryEdgeAndVertex;
    }

    public void setFirstTitanQueryEdgeAndVertex(
            TitanQueryEdgeAndVertex firstTitanQueryEdgeAndVertex) {
        this.firstTitanQueryEdgeAndVertex = firstTitanQueryEdgeAndVertex;
    }

    public TitanExpression addCondition(TitanEdgeExpression titanExpression) {
        edgeExpressions.add(titanExpression);
        return this;
    }

    @Deprecated
    // 详见： TitanEdgeExpression.generateScript
    public TitanExpression addCondition(TitanQueryEdgeAndVertex titanQueryEdge) {
        edgeExpressions.add(titanQueryEdge);
        return this;
    }

    public TitanExpression addCondition(TitanQueryVertex titanQueryVertex) {
        this.titanQueryVertex = titanQueryVertex;
        return this;
    }

    public String generateScriptForCount(Map<String, Object> scriptParamMap) {
        if (this.innerCondition == null) {
            buildConditions(scriptParamMap);
        }
        return new StringBuffer(TitanKeyWords.TOTALCOUNT.toString()).append("=")
                .append(this.innerCondition + (".count();")).toString();
    }

    private void buildConditions(Map<String, Object> scriptParamMap) {
        if (titanQueryVertex == null) {
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "LC/Titan/Expression", "LC/Titan/Expression");
        }
        StringBuffer scriptBuffer = new StringBuffer("g.");
        if (firstTitanQueryEdgeAndVertex != null) {

            firstTitanQueryEdgeAndVertex.setIsEdgeBeforeVertex(false);
            scriptBuffer.append(firstTitanQueryEdgeAndVertex
                    .generateScript(scriptParamMap));
            if (firstTitanQueryEdgeAndVertex.getTitanQueryEdge()
                    .getTitanEdgeDirection() == TitanDirection.in) {
                titanQueryVertex.setTitanDirection(TitanDirection.out);
            } else {
                titanQueryVertex.setTitanDirection(TitanDirection.in);
            }
            scriptBuffer.append(".");

        }
        scriptBuffer.append(titanQueryVertex.generateScript(scriptParamMap));
        scriptBuffer.append(".as('x')");
        for (TitanScriptGenerator titanEdgeExpression : edgeExpressions) {
            if (titanEdgeExpression.isNotHavingAnyCondition()) {
                continue;
            }
            scriptBuffer.append(".select('x').");
            scriptBuffer.append(titanEdgeExpression
                    .generateScript(scriptParamMap));

        }
        scriptBuffer.append(".select('x')");
        this.innerCondition = scriptBuffer.toString();
    }

    @Override
    public String generateScript(Map<String, Object> scriptParamMap) {
        if (this.innerCondition == null) {
            buildConditions(scriptParamMap);
        }
        StringBuffer scriptBuffer = new StringBuffer(this.innerCondition);
        // (k,v)=>(order_field,desc)
        // 1、DESC=decr 从大到小排序 2、ACS=incr 从小到大排序
        scriptBuffer.append(".order()");
        for (String field : this.orderMap.keySet()) {
            if (field == null)
                continue;
            final String value = orderMap.get(field);
            String sortBy = null;
            if (value != null) {
                if (value.trim().toUpperCase()
                        .equals(PropOperationConstant.OP_ASC)) {
                    sortBy = TitanKeyWords.incr.toString();
                } else {
                    sortBy = TitanKeyWords.decr.toString();
                }
            } else {
                sortBy = TitanKeyWords.decr.toString();
            }
            scriptBuffer.append(".by('").append(field).append("',")
                    .append(sortBy).append(")");
        }

        // FIXME for now only get the identifier
        // values('identifier','title','enable','questions') --
        // 不能采用这种方式，当有时值会null时，就会打乱顺序，没有key
        //scriptBuffer.append(".valueMap()");

        if (this.end > 0) {
            scriptBuffer.append(".range(").append(from).append(",").append(end)
                    .append(")");
        }
        scriptBuffer = new StringBuffer(TitanKeyWords.RESULT.toString()).append("=").append(scriptBuffer);

        if (needTaxoncode || needTaxonpath || needTechInfo) {
            scriptBuffer.append(".as('v').union(select('v')");
            if (needTaxoncode) {
                scriptBuffer.append(",out('has_category_code')");
            }
            if (needTaxonpath) {
                scriptBuffer.append(",out('has_categories_path')");

            }
            if (needTechInfo) {
                scriptBuffer.append(",out('has_tech_info')");
            }
            scriptBuffer.append(")");
        }
        scriptBuffer.append(".valueMap();");
        return scriptBuffer.toString();

    }

    public String generateScriptForResultAndCount(Map<String, Object> scriptParamMap) {
        //this.end++;
        String scriptForCount = generateScriptForCount(scriptParamMap);
        String scriptForResult = generateScript(scriptParamMap);
        return new StringBuffer(scriptForCount).append(scriptForResult)
                .append("List<Object> resultList="
                        + TitanKeyWords.RESULT.toString() +
                        ".toList();")
                .append("List<Object> countsList="
                        + TitanKeyWords.TOTALCOUNT.toString() +
                        ".toList();")
                .append("resultList << '" + TitanKeyWords.TOTALCOUNT.toString() + ":'+" +
                        "countsList[0];").toString();
    }

    /************************************
     * TEST
     *********************************/
    public static void main(String[] args) {
        Map<String, Object> scriptParamMap = new HashMap<String, Object>();
        // System.out.println(generateTestExample().generateScript(scriptParamMap));
        System.out.println(generateTestExample().generateScriptForResultAndCount(scriptParamMap));
        System.out.println(scriptParamMap);
    }

    public static TitanExpression generateTestExample() {
        TitanExpression titanExpression = new TitanExpression();
        titanExpression.addCondition(TitanQueryVertex.generateTestExample());
        titanExpression.addCondition(TitanEdgeExpression.generateTestExample());
        Map<String, String> or = new HashedMap<String, String>();
        or.put("field1", "desc");
        or.put("field21", "asc");
        titanExpression.setOrderMap(or);
        titanExpression.setRange(1, 10);
        return titanExpression;
    }

    @Override
    public Boolean isNotHavingAnyCondition() {
        // TODO Auto-generated method stub
        return null;
    }
}
