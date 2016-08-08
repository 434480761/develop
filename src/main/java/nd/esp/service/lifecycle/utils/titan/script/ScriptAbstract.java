package nd.esp.service.lifecycle.utils.titan.script;

import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.TitanScritpUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by liuran on 2016/8/5.
 */
public abstract class ScriptAbstract {
    public class Result{
        private String script ;
        private String graphResultIdName;
        private Map<String, Object> param;
        private String methodName;

        /**
         * @param script 脚本
         * @param param 脚本参数
         * @param graphResultIdName 脚本执行成功后返回的唯一ID名称
         * */
        public Result(String script, Map<String, Object> param,String graphResultIdName){
            this.script = script;
            this.param = param;
            this.graphResultIdName = graphResultIdName;
        }

        public String getMethodName() {
            return methodName;
        }

        public void setMethodName(String methodName) {
            this.methodName = methodName;
        }

        public String getScript() {
            return script;
        }

        public void setScript(String script) {
            this.script = script;
        }

        public Map<String, Object> getParam() {
            return param;
        }

        public void setParam(Map<String, Object> param) {
            this.param = param;
        }

        public String getGraphResultIdName() {
            return graphResultIdName;
        }

        public void setGraphResultIdName(String graphResultIdName) {
            this.graphResultIdName = graphResultIdName;
        }
    }

    public enum KeyWords {
        script, params
    }

    abstract String name();
    abstract String resourceIdentifier();
    abstract String resourcePrimaryCategory();
    abstract String nodeLabel();
    abstract String edgeLabel();
    abstract Map<String,Object> customProperty();
    abstract EspEntity entity();
    abstract Map<String,Object> searchUniqueNodeProperty();
    abstract Map<String,Object> searchUniqueEdgeProperty();
    /**
     * 添加一个节点
     * */
    public Result addNode(int methodIndex){
        Result result = addNodeScript(methodIndex);
        return result;
    }

    public Result addEdge(int methodIndex){
//        return createEdge(methodIndex, node.getGraphResultIdName());
        return null;
    }

    public Result addNodeAndEdge(int methodIndex){
        Result node = addNodeScript(methodIndex);
        Result edge = createEdge(methodIndex, node.getGraphResultIdName());

        String reslutScript = node.getScript() + edge.getScript();
        Map<String, Object> resultParam = new HashMap<>();
        resultParam.putAll(node.getParam());
        resultParam.putAll(edge.getParam());

        return new Result(reslutScript,resultParam,edge.getGraphResultIdName());
    }

    /**
     * 更新节点数据
     * */
    public Result updateNode(int methodIndex){
        return null;
    }

    public Result updateEdge(int methodIndex){
        return null;
    }

    public Result updateNodeAndEdge(int methodIndex){
        return null;
    }

    /**
     * 对节点数据更新或者增加
     * */
    public Result addOrUpdateNode(int methodIndex){
        return null;
    }

    public Result addOrUpdateEdge(int methodIndex){
        return null;
    }

    public Result addOrUpdateNodeAndEdge(int methodIndex){
        return null;
    }


    public Result delete(int methodIndex){
        return null;
    }

    private String methodStart(String suffix){
        return  "public String " + name() + suffix +"{";
    }

    private String methodEnd(){
        return ";};";
    }

    private Result getUniqueEdge(int methodIndex){
        return searchUniqueScript("edge", methodIndex);
    }

    private Result getUniqueNode(int methodIndex){
        return searchUniqueScript("node", methodIndex);
    }


    private Result searchUniqueScript(String type, int methodIndex){
        String nameSuffix;
        if("node".equals(type)){
            nameSuffix = "N"+name()+methodIndex;
        } else {
            nameSuffix = "E"+name()+methodIndex;
        }
        String graphReslutName = "get"+nameSuffix;
        String labelName = "type"+nameSuffix;
        StringBuilder scirpt;
        if("node".equals(type)){
            scirpt = new StringBuilder(graphReslutName+"=g.V().hasLabel("+labelName+")");
        } else {
            scirpt = new StringBuilder(graphReslutName+"=g.E().hasLabel("+labelName+")");
        }
        Map<String, Object> param = new HashMap<>();
        param.put(labelName,edgeLabel());
        for(String key : searchUniqueEdgeProperty().keySet()){
            scirpt.append(".has('").append(key).append("',").append(key).append(nameSuffix).append(")");
        }

        scirpt.append(".id();");

        return new Result(scirpt.toString(),param,graphReslutName);
    }


    private Result createEdge(int methodIndex ,String nodeIdName){
        String nameSuffix = name() + methodIndex +"e";
        String graphResultName = "node"+nameSuffix;
        String resourceIdentifierName = "identifierNode"+nameSuffix;
        String resourceType = "typeNode" + nameSuffix;
        String edgeLabelName = "type"+nameSuffix;
        String addEdgeHead = graphResultName + "=g.V().hasLabel("+resourceType+").has('identifier',"+resourceIdentifierName+")" +
                ".next().addEdge("+edgeLabelName+",g.V("+nodeIdName+").next()";
        StringBuffer createEdge = new StringBuffer(addEdgeHead);
        Map<String, Object> scriptParam = createScriptAndParam(createEdge, nameSuffix);
        scriptParam.put(edgeLabelName, edgeLabel());
        scriptParam.put(resourceType, resourcePrimaryCategory());
        scriptParam.put(resourceIdentifierName, resourceIdentifier());

        createEdge.append(").id();");

        Result result = new Result(createEdge.toString(), scriptParam, graphResultName);
        return result;
    }

    private Result addNodeScript(int methodIndex){
        String graphResultName = "node"+name()+methodIndex;
        String nameSuffix = name() + methodIndex;
        StringBuffer script = new StringBuffer(graphResultName + "=graph.addVertex(T.label,");
        String typeName = "type"+ nameSuffix;
        script.append(typeName);

        Map<String, Object> resultParam = createScriptAndParam(script, nameSuffix);
        resultParam.put(typeName, nodeLabel());

        script.append(").id();");

        return  new Result(script.toString(), resultParam, graphResultName);
    }

    private Result updateNodeScript(int methodIndex){

        return null;
    }


    private Map<String, Object> createScriptAndParam(StringBuffer script, String nameSuffix){
        Map<String, Object> graphParams = TitanScritpUtils.getParam4NotNull(entity());
        if(CollectionUtils.isNotEmpty(customProperty())){
            graphParams.putAll(customProperty());
        }

        Map<String, Object> resultParam = new HashMap<>();


        for (String key : graphParams.keySet()) {
            String propertyName = key + nameSuffix;
            Object value = graphParams.get(key);
            if (value == null) {
                continue;
            }
            if (value instanceof String) {
                script.append(",'").append(key).append("',").append(propertyName);
                resultParam.put(propertyName, value);
            } else if (value instanceof List) {
                List list = (List) value;
                int index = 0;
                for (Object obj : list) {
                    String propertyNameNew = propertyName + index;
                    script.append(",'").append(key).append("',").append(propertyNameNew);
                    resultParam.put(propertyNameNew, obj);
                    index++;
                }
            } else if(value instanceof Set){
                Set set = (Set) value;
                int index = 0;
                for (Object obj : set) {
                    String propertyNameNew = propertyName + index;
                    script.append(",'").append(key).append("',").append(propertyNameNew);
                    resultParam.put(propertyNameNew, obj);
                    index++;
                }
            }
        }

        return resultParam;
    }
}
