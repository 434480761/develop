package nd.esp.service.lifecycle.support.busi.titan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nd.esp.service.lifecycle.educommon.vos.constant.PropOperationConstant;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.http.HttpStatus;

import nd.esp.service.lifecycle.support.LifeCircleException;

/**
 * 用于产生整个脚本与参数（由TitanQueryVertex, TitanQueryEdgeAndVertex,TitanEdgeExpression)
 *
 * @author linsm
 */
public class TitanExpression implements TitanScriptGenerator {

    private String resType;
    private List<String> includes;
    private boolean relationQueryOrderBy = false;
    private boolean isOrderBySortNum = false;
    private String orderByEdgeFieldName;
    private List<TitanOrder> orderList;
    private boolean needStatistics = false;
    private String statisticsScript;
    private boolean needPrintable = false;
    private String printableScript;
    private String orderBy4SortNum = "incr";

    public void setStatistics(boolean needStatistics, String statisticsScript) {
        this.needStatistics = needStatistics;
        this.statisticsScript = statisticsScript;
    }

    public void setPrintable(boolean needPrintable, String printableScript) {
        this.needPrintable = needPrintable;
        this.printableScript = printableScript;
    }

    public void setRelationQueryOrderBy(boolean relationQueryOrderBy, String orderByEdgeFieldName) {
        this.relationQueryOrderBy = relationQueryOrderBy;
        this.orderByEdgeFieldName = orderByEdgeFieldName;
    }

    public void setOrderBySortNum(boolean isOrderBySortNum, String orderByEdgeFieldName, String orderBy4SortNum) {
        this.isOrderBySortNum = isOrderBySortNum;
        this.orderByEdgeFieldName = orderByEdgeFieldName;
        this.orderBy4SortNum = orderBy4SortNum;
    }

    public void setOrderList(List<TitanOrder> orderList) {
        this.orderList = orderList;
    }

    public void setIncludes(List<String> includes) {
        this.includes = includes;
    }

    private String innerCondition;// 用于保存中间产生的条件；主要避免重复产生脚本：总数，分页

    private Map<String, String> orderMap;
    private int from;
    private int end;

    public void setResType(String resType) {
        this.resType = resType;
    }

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
        //scriptBuffer.append(".select('x')");
        // 在这里去重和加上处理printable
        // .outE('has_tech_info').has('ti_printable',true).select('x').dedup()
        if (this.needPrintable) scriptBuffer.append(printableScript);
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
        if (this.relationQueryOrderBy || this.isOrderBySortNum) {
            //.select('e').order().by('order_num',decr).select('x')
           // scriptBuffer.append(".select('x').order().by('lc_create_time',decr).select('e').order().by(choose(select('e').has('").append(this.orderByEdgeFieldName).append("'),select('e').values('").append(this.orderByEdgeFieldName).append("'),__.constant(0)),").append(this.orderBy4SortNum).append(").select('x')");
            scriptBuffer.append(".select('x').order().by('lc_create_time',decr).select('e').choose(select('e').has('").append(this.orderByEdgeFieldName).append("'),select('e').values('").append(this.orderByEdgeFieldName).append("'),__.constant(new Float(0)))").append(".order().by(").append(this.orderBy4SortNum).append(")");

        } else {
            appendOrderBy(scriptBuffer);
        }

        // FIXME for now only get the identifier
        // values('identifier','title','enable','questions') --
        // 不能采用这种方式，当有时值会null时，就会打乱顺序，没有key
        //scriptBuffer.append(".valueMap()");

        if (this.end > 0) {
            // range 前select('x')
            scriptBuffer.append(".select('x').range(").append(from).append(",").append(end)
                    .append(")");
        }
        scriptBuffer = new StringBuffer(TitanKeyWords.RESULT.toString()).append("=").append(scriptBuffer);
        // 拼接include
        scriptBuffer.append(TitanUtils.generateScriptForInclude(this.includes,this.resType,this.relationQueryOrderBy,this.needStatistics,this.statisticsScript));
        //scriptBuffer.append(".valueMap();");
        return scriptBuffer.toString();

    }

    /**
     * 拼接order by
     * @param scriptBuffer
     */
    private void appendOrderBy(StringBuffer scriptBuffer) {
        if (CollectionUtils.isNotEmpty(this.orderList)) {
            int size = this.orderList.size();
            for (int i = size - 1; i >= 0; i--) {
                TitanOrder order = this.orderList.get(i);
                if (order.getScript() != null) {
                    scriptBuffer.append(order.getScript());
                }
                scriptBuffer.append(".order().by(");
                if (order.getOrderByField() != null) {
                    scriptBuffer.append("'").append(order.getOrderByField()).append("',");
                }
                scriptBuffer.append(order.getSortOrder()).append(")");
            }
        }
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
                .append("resultList << '" + TitanKeyWords.TOTALCOUNT.toString() + "='+" +
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
