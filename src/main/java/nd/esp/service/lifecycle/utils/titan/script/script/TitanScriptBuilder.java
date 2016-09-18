package nd.esp.service.lifecycle.utils.titan.script.script;

import nd.esp.service.lifecycle.utils.titan.script.model.TitanModel;
import nd.esp.service.lifecycle.utils.titan.script.utils.ParseAnnotation;

import java.util.*;

/**
 * Created by Administrator on 2016/8/24.
 */
public class TitanScriptBuilder {
    private StringBuilder script = new StringBuilder();
    private Map<String, Object> param = new HashMap<>();
    private List<String> methodNames = new ArrayList<>();
    private enum KeyWords {
        node,edge,source,target,addVertex,has,next,id,addEdge,property
    }
    private int firstIndex =0;
    public TitanScriptBuilder(){
        this.firstIndex = 0;
    }

    public StringBuilder getScript() {
        return script;
    }

    public void setScript(StringBuilder script) {
        this.script = script;
    }

    public Map<String, Object> getParam() {
        return param;
    }

    public void setParam(Map<String, Object> param) {
        this.param = param;
    }

    private class Variable{
        String variable;
        String suffix;
        int secondIndex =0;

        public int secondIndex() {
            secondIndex ++ ;
            return secondIndex;
        }

        public Variable(String variable, String suffix) {
            this.variable = variable;
            this.suffix = suffix;
        }

        public String getSuffix() {
            secondIndex ++ ;
            return suffix +"_" +secondIndex;
        }

        public String getVariable() {
            return variable;
        }
    }

    /**
     * 更新数据脚本，边和点通用
     * */
    public TitanScriptBuilder update(TitanModel model){
        TitanScriptModel titanScriptModel = ParseAnnotation.createScriptModel(model);

        if (titanScriptModel == null){
            return this;
        }
        Variable variable = createVariable(titanScriptModel.getType());
        StringBuilder script = new StringBuilder() ;
        Map<String, Object> param = new HashMap<>();
        update(titanScriptModel, variable, script ,param);

        dealScriptAndParam(script, param);
        return this;
    }

    /**
     * 添加数据脚本，边和点通用
     * */
    public TitanScriptBuilder add(TitanModel model){
        TitanScriptModel titanScriptModel = ParseAnnotation.createScriptModel(model);
        if (titanScriptModel == null){
            return this;
        }
        Variable variable = createVariable(titanScriptModel.getType());
        StringBuilder script = new StringBuilder() ;
        Map<String, Object> param = new HashMap<>();
        add(titanScriptModel, variable, script ,param);

        dealScriptAndParam(script, param);
        return this;
    }

    /**
     * 添加或者更新脚本，边和点通用
     * */
    public TitanScriptBuilder addOrUpdate(TitanModel model){
        if (model == null){
            return this;
        }

        TitanScriptModel titanScriptModel = ParseAnnotation.createScriptModel(model);
        Variable variable = createVariable(titanScriptModel.getType());
        StringBuilder script = new StringBuilder() ;
        Map<String, Object> param = new HashMap<>();

        addOrUpdate(titanScriptModel,variable,script,param);
        dealScriptAndParam(script, param);
        return this;
    }

    /**
     * 添加前检查数据是否已经存在，如果不存在则添加，否则不进行任何操作，边和点通用
     * */
    public TitanScriptBuilder addBeforeCheckExist(TitanModel model){
        TitanScriptModel titanScriptModel = ParseAnnotation.createScriptModel(model);
        if (titanScriptModel == null){
            return this;
        }
        Variable variable = createVariable(titanScriptModel.getType());
        StringBuilder script = new StringBuilder() ;
        Map<String, Object> param = new HashMap<>();
        addBeforeCheckExist(titanScriptModel, variable, script ,param);

        dealScriptAndParam(script, param);
        return this;
    }

    /**
     * 获取节点
     * */
    public TitanScriptBuilder get(TitanModel model){
        TitanScriptModel titanScriptModel = ParseAnnotation.createScriptModel(model);
        if (titanScriptModel == null){
            return this;
        }
        Variable variable = createVariable(titanScriptModel.getType());
        StringBuilder script = new StringBuilder() ;
        Map<String, Object> param = new HashMap<>();
        uniqueVertexOrNode(titanScriptModel, variable, script ,param);

        dealScriptAndParam(script, param);
        return this;
    }

    public TitanScriptBuilder delete(TitanModel model){
        TitanScriptModel titanScriptModel = ParseAnnotation.createScriptModel(model);
        if (titanScriptModel == null){
            return this;
        }
        Variable variable = createVariable(titanScriptModel.getType());
        StringBuilder script = new StringBuilder() ;
        Map<String, Object> param = new HashMap<>();
        delete(titanScriptModel, variable, script ,param);

        dealScriptAndParam(script, param);
        return this;
    }

    /**
     * 整个脚本结束，组合脚本中的方法，边和点通用
     * */
    public TitanScriptBuilder scriptEnd(){
        script.append("public void method(){");
        for (String methodName : methodNames){
            script.append(methodName).append("();");
        }

        script.append("};method()");

        return null;
    }

    /**
     * 删除节点，必须是一个单独节点
     * */



    public TitanScriptBuilder methodEnd(){
        return null;
    }

    /**
     * 为脚本添加方法头不合尾部
     * @param script 脚本
     * @param param 脚本参数
     * */
    private void dealScriptAndParam(StringBuilder script, Map<String, Object> param){
        String method = "method" + firstIndex;
        methodNames.add(method);
//        this.script.append("public void ").append(method).append("(){").append(script).append(" ").append("}").append(";");
        this.script.append(script).append(";");
        this.param.putAll(param);
        this.firstIndex ++ ;
    }

    private void delete(TitanScriptModel titanScriptModel, Variable variable, StringBuilder script , Map<String, Object> param){
        script.append("g");
        if (titanScriptModel instanceof TitanScriptModelVertex){
            script.append(".V()");
        }
        if (titanScriptModel instanceof TitanScriptModelEdge){
            script.append(".E()");
        }
        appendHas(titanScriptModel.getCompositeKeyMap(),variable,script,param);
        appendDrop(script);
    }

    /**
     * @param titanScriptModel titan脚本参数
     * @param variable 单个脚本所需要的一些参数
     * @param script 脚本
     * @param param 脚本参数
     * 脚本模板 if(exist){update_script}else{add_script}
     * */
    private void addOrUpdate(TitanScriptModel titanScriptModel, Variable variable, StringBuilder script , Map<String, Object> param){
        StringBuilder ifConditionScript = new StringBuilder("!");
        uniqueVertexOrNode(titanScriptModel,variable, ifConditionScript, param);
        appendIterator(ifConditionScript);
        appendHasNext(ifConditionScript);

        StringBuilder ifRunScript = new StringBuilder();
        add(titanScriptModel,variable ,ifRunScript ,param);

        StringBuilder elseRunScript = new StringBuilder();
        update(titanScriptModel, variable, elseRunScript, param);

        ifElseScript(script,ifConditionScript.toString(),ifRunScript.toString(),elseRunScript.toString());

    }

    /**
     * @param titanScriptModel titan脚本参数
     * @param variable 单个脚本所需要的一些参数
     * @param script 脚本
     * @param param 脚本参数
     * 脚本模板 if(!exist){add_script}
     * */
    private void addBeforeCheckExist(TitanScriptModel titanScriptModel,Variable variable, StringBuilder script , Map<String, Object> param){
        StringBuilder ifConditionScript = new StringBuilder("!");
        uniqueVertexOrNode(titanScriptModel,variable, ifConditionScript, param);
        appendIterator(ifConditionScript);
        appendHasNext(ifConditionScript);

        StringBuilder ifRunScript = new StringBuilder();
        add(titanScriptModel,variable ,ifRunScript ,param);

        ifScript(script, ifConditionScript.toString(), ifRunScript.toString());
    }

    /**
     * @param titanScriptModel titan脚本参数
     * @param variable 单个脚本所需要的一些参数
     * @param script 脚本
     * @param param 脚本参数
     * 脚本模板 add_script : node_0 = graph.addVertex(T.label,label,pro1,value1,...).id()
     * */
    private void add(TitanScriptModel titanScriptModel,Variable variable, StringBuilder script , Map<String, Object> param){
        if (titanScriptModel instanceof TitanScriptModelVertex){
            script.append(variable.getVariable()).append("=graph");
            appendAddVertex(titanScriptModel.getFieldMap(),titanScriptModel.getLabel(),variable,script,param);
        }
        if (titanScriptModel instanceof TitanScriptModelEdge){
            TitanScriptModelEdge edge = (TitanScriptModelEdge) titanScriptModel;
            script.append("g.V()");
            appendHas(edge.getResourceKeyMap(),variable,script,param);
            appendNext(script);

            StringBuilder addEdgeInner = new StringBuilder("g.V()");
            appendHas(edge.getTargetKeyMap(),variable,addEdgeInner,param);
            appendNext(addEdgeInner);

            appendAddEdge(edge.getFieldMap(),edge.getLabel(),addEdgeInner.toString(),variable,script,param);

        }

        appendId(script);
    }

    /**
     * @param titanScriptModel titan脚本参数
     * @param variable 单个脚本所需要的一些参数
     * @param script 脚本
     * @param param 脚本参数
     * 脚本模板 update_script:g.V().has('pro1',value1)...property('pro2',value2)...properties('pro3','pro4').drop()
     * */
    private void update(TitanScriptModel titanScriptModel,Variable variable, StringBuilder script , Map<String, Object> param){
        if (titanScriptModel instanceof TitanScriptModelVertex){
            script.append("g.V()");
        }
        if (titanScriptModel instanceof TitanScriptModelEdge){
            script.append("g.E()");
        }
        Map<String, Object> updateValues = new HashMap<>();
        List<String> dropValues = new ArrayList<>();
        for (String key : titanScriptModel.getFieldMap().keySet()){
            Object obj = titanScriptModel.getFieldMap().get(key);

            if (obj == null){
                dropValues.add(key);
            } else {
                updateValues.put(key, obj);
            }

        }
//        appendNext(script);
        appendHas(titanScriptModel.getCompositeKeyMap(),variable,script,param);
        appendProperty(updateValues,variable,script,param);
//        appendProperties(dropValues,script);
//        appendDrop(script);
        script.append(";");
    }

    /**
     * 获取唯一的节点或者边
     * @param titanScriptModel titan脚本参数
     * @param variable 单个脚本所需要的一些参数
     * @param script 脚本
     * @param param 脚本参数
     * 脚本模板：g.V().has('pro1',value1)....id();
     * */
    private void uniqueVertexOrNode(TitanScriptModel titanScriptModel, Variable variable, StringBuilder script , Map<String, Object> param){
        script.append("g");
        if (titanScriptModel instanceof TitanScriptModelVertex){
            script.append(".V()");
        }
        if (titanScriptModel instanceof TitanScriptModelEdge){
            script.append(".E()");
        }
        appendHas(titanScriptModel.getCompositeKeyMap(),variable,script,param);
        appendId(script);
    }



    private Variable createVariable(TitanScriptModel.Type type){
        Variable variable = null;
        String suffix = ""+firstIndex;
        if (type.equals(TitanScriptModel.Type.E)){
           variable = new Variable(KeyWords.edge.toString()+suffix, suffix);
        }
        if (type.equals(TitanScriptModel.Type.V)){
            variable = new Variable(KeyWords.node.toString()+suffix, suffix);
        }

        return variable;
    }

    private void appendHas(Map<String, Object> values , Variable variable, StringBuilder script , Map<String, Object> param){
        append(KeyWords.has,values,variable,script,param);
    }

    private void appendProperty(Map<String, Object> values, Variable variable, StringBuilder script, Map<String, Object> param){
        append(KeyWords.property,values,variable,script,param);
    }

    private void appendProperties(List<String> keys, StringBuilder script){
        if (keys == null || keys.isEmpty()){
            return;
        }
        script.append(".properties(");
        for (int i=0; i< keys.size() ; i++){
            if (i == 0){
                script.append("'").append(keys.get(i)).append("'");
            } else {
                script.append(",").append("'").append(keys.get(i)).append("'");
            }
        }

        script.append(")");
    }

    private void ifScript(StringBuilder script, String conditionScript,String ifRunScript){
        script.append("if(").append(conditionScript).append("){").append(ifRunScript).append("}");
    }
    private void ifElseScript(StringBuilder script, String conditionScript,String ifRunScript, String elseRunScript){
        ifScript(script, conditionScript, ifRunScript);
        script.append("else{").append(elseRunScript).append("}");
    }

    private void appendDrop(StringBuilder script){
        script.append(".drop()");
    }

    private void appendIterator(StringBuilder script){
        script.append(".iterator()");
    }

    private void appendHasNext(StringBuilder script){
        script.append(".hasNext()");
    }

    private void appendAddVertex(Map<String, Object> values,String label , Variable variable, StringBuilder script , Map<String, Object> param){
        String labelName = "nodeLabel"+variable.getSuffix();
        script.append(".addVertex(T.label,").append(labelName);
        param.put(labelName, label);
        for (String key : values.keySet()){
            if (values.get(key)==null){
                continue;
            }
            String name = key + variable.getSuffix();
            script.append(",'").append(key).append("',").append(name);
            param.put(name, values.get(key));
        }

        script.append(")");
    }

    private void appendAddEdge(Map<String, Object> values ,String label,String innerScript , Variable variable, StringBuilder script , Map<String, Object> param){
        String labelName = "edgeLabel"+variable.getSuffix();
        script.append(".addEdge(").append(labelName).append(",").append(innerScript);
        for (String key : values.keySet()){
            if (values.get(key)==null){
                continue;
            }
            String name = key + variable.getSuffix();
            script.append(",'").append(key).append("',").append(name);
            param.put(name, values.get(key));
        }
        param.put(labelName, label);
        script.append(")");
    }

    private void appendNext(StringBuilder script){
        script.append(".next()");
    }

    private void appendId(StringBuilder script){
        script.append(".id()");
    }

    private void append(KeyWords type, Map<String, Object> values , Variable variable, StringBuilder script , Map<String, Object> param){
        if (values == null || values.isEmpty()){
            return;
        }
        for (String key : values.keySet()){
            String name = key + variable.getSuffix();
            script.append(".").append(type.toString()).append("('").append(key).append("',").append(name).append(")");
            param.put(name, values.get(key));
        }
    }
}
