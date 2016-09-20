package nd.esp.service.lifecycle.utils.titan.script.script;

import nd.esp.service.lifecycle.support.busi.titan.TitanKeyWords;
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

    private class ScriptAndParam{
        public ScriptAndParam(StringBuilder script, Map<String, Object> param){
            this.script = script;
            this.param = param;
        }
        StringBuilder script ;
        Map<String, Object> param;

        public StringBuilder getScript() {
            return script;
        }

        public Map<String, Object> getParam() {
            return param;
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

        dealScriptAndParam(update(titanScriptModel, variable), titanScriptModel);
        return this;
    }

    /**
     * 添加数据脚本，边和点通用
     * */
    public  TitanScriptBuilder add(TitanModel model){
        TitanScriptModel titanScriptModel = ParseAnnotation.createScriptModel(model);
        if (titanScriptModel == null){
            return this;
        }
        Variable variable = createVariable(titanScriptModel.getType());

        dealScriptAndParam(add(titanScriptModel, variable),titanScriptModel);
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
        dealScriptAndParam(addOrUpdate(titanScriptModel,variable),titanScriptModel);
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

        dealScriptAndParam(addBeforeCheckExist(titanScriptModel, variable), titanScriptModel);
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
//        uniqueVertexOrNode(titanScriptModel, variable, script ,param);

//        dealScriptAndParam(script, param);
        return this;
    }

    public TitanScriptBuilder delete(TitanModel model){
        TitanScriptModel titanScriptModel = ParseAnnotation.createScriptModel(model);
        if (titanScriptModel == null){
            return this;
        }
        Variable variable = createVariable(titanScriptModel.getType());

        dealScriptAndParam(delete(titanScriptModel, variable), titanScriptModel);
        return this;
    }

    public TitanScriptBuilder deleteNullProperty(TitanModel model){
        TitanScriptModel titanScriptModel = ParseAnnotation.createScriptModel(model);
        if (titanScriptModel == null){
            return this;
        }
        Variable variable = createVariable(titanScriptModel.getType());
        StringBuilder script = new StringBuilder() ;
        Map<String, Object> param = new HashMap<>();
        deleteNodeNullProperty(titanScriptModel,variable,script,param);

//        dealScriptAndParam(script, param);

        return this;
    }

    /**
     * 整个脚本结束，组合脚本中的方法，边和点通用
     * */
    public TitanScriptBuilder scriptEnd(){
        for (String methodName : methodNames){
            script.append(methodName).append("();");
        }

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
     * */
    private void dealScriptAndParam(ScriptAndParam scriptAndParam, TitanScriptModel type){
        String returnType = null;
        if (type == null){
            returnType = "void";
        }else if (type instanceof TitanScriptModelEdge){
            returnType = "Edge";
        } else if (type instanceof TitanScriptModelVertex){
            returnType = "Vertex";
        }
        String method = "method" + firstIndex;
        methodNames.add(method);
        this.script.append("public ").append(returnType).append(" ").append(method).append("(){").append(scriptAndParam.getScript()).append(" ").append("}").append(";");
//        this.script.append(script).append(";");
        this.param.putAll(scriptAndParam.getParam());
        this.firstIndex ++ ;
    }

    private void deleteNodeNullProperty(TitanScriptModel titanScriptModel, Variable variable, StringBuilder script , Map<String, Object> param){
        script.append("g");
        if (titanScriptModel instanceof TitanScriptModelVertex){
            script.append(".V()");
        }
        if (titanScriptModel instanceof TitanScriptModelEdge){
            script.append(".E()");
        }
        appendHas(titanScriptModel.getCompositeKeyMap(),variable,script,param);

        List<String> dropValues = new ArrayList<>();
        for (String key : titanScriptModel.getFieldMap().keySet()){
            Object obj = titanScriptModel.getFieldMap().get(key);
            if (obj == null){
                dropValues.add(key);
            }
        }

        dropValues.add(TitanKeyWords.search_code.toString());
        dropValues.add(TitanKeyWords.search_coverage.toString());
        dropValues.add(TitanKeyWords.search_path.toString());
        dropValues.add(TitanKeyWords.search_code_string.toString());
        dropValues.add(TitanKeyWords.search_coverage_string.toString());
        dropValues.add(TitanKeyWords.search_path_string.toString());

        appendProperties(dropValues,script);
        appendDrop(script);
    }

    private ScriptAndParam delete(TitanScriptModel titanScriptModel, Variable variable){
        StringBuilder script = new StringBuilder();
        Map<String, Object> param = new HashMap<>();
        if (titanScriptModel instanceof TitanScriptModelVertex){
            script.append("Iterator<Vertex> it=g.V()");
        }
        if (titanScriptModel instanceof TitanScriptModelEdge){
            script.append("Iterator<Edge> it=g.E()");
        }

        appendHas(titanScriptModel.getCompositeKeyMap(),variable,script,param);
        appendIterator(script);
        script.append(";");

        StringBuilder whileRunScript = new StringBuilder( "it");
        appendNext(whileRunScript);
        appendRemove(whileRunScript);

        StringBuilder whileCondition = new StringBuilder("it");
        appendHasNext(whileCondition);

        whileScript(script,whileCondition.toString(),whileRunScript.toString());

        return new ScriptAndParam(script, param);
    }

    /**
     * @param titanScriptModel titan脚本参数
     * @param variable 单个脚本所需要的一些参数
     * 脚本模板 if(exist){update_script}else{add_script}
     * */
    private ScriptAndParam addOrUpdate(TitanScriptModel titanScriptModel, Variable variable){
        StringBuilder script = new StringBuilder();
        Map<String, Object> param = new HashMap<>();
        StringBuilder ifConditionScript = new StringBuilder("!");
        uniqueVertexOrNode(titanScriptModel,variable, ifConditionScript, param);
        appendIterator(ifConditionScript);
        appendHasNext(ifConditionScript);

        ScriptAndParam ifScriptAndParam = add(titanScriptModel,variable);
        param.putAll(ifScriptAndParam.getParam());

        ScriptAndParam elseScriptAndParam = update(titanScriptModel, variable);
        param.putAll(elseScriptAndParam.getParam());

        ifElseScript(script,ifConditionScript.toString(),ifScriptAndParam.getScript().toString(),elseScriptAndParam.getScript().toString());

        return new ScriptAndParam(script, param);
    }

    /**
     * @param titanScriptModel titan脚本参数
     * @param variable 单个脚本所需要的一些参数
     * 脚本模板 if(!exist){add_script}
     * */
    private ScriptAndParam addBeforeCheckExist(TitanScriptModel titanScriptModel,Variable variable){
        StringBuilder script = new StringBuilder();
        Map<String, Object> param = new HashMap<>();
        StringBuilder ifConditionScript = new StringBuilder("!");
        uniqueVertexOrNode(titanScriptModel,variable, ifConditionScript, param);
        appendIterator(ifConditionScript);
        appendHasNext(ifConditionScript);

        ScriptAndParam ifScriptAndParam = add(titanScriptModel,variable);
        param.putAll(ifScriptAndParam.getParam());

        ifScript(script, ifConditionScript.toString(), ifScriptAndParam.getScript().toString());

        return new ScriptAndParam(script, param);
    }

    /**
     * @param titanScriptModel titan脚本参数
     * @param variable 单个脚本所需要的一些参数

     * 脚本模板 add_script : node_0 = graph.addVertex(T.label,label,pro1,value1,...).id()
     * */
    private ScriptAndParam add(TitanScriptModel titanScriptModel,Variable variable){

        StringBuilder script = new StringBuilder();
        Map<String, Object> param = new HashMap<>();
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

        return new ScriptAndParam(script, param);
    }

    /**
     * @param titanScriptModel titan脚本参数
     * @param variable 单个脚本所需要的一些参数
     * 脚本模板 update_script:g.V().has('pro1',value1)...property('pro2',value2)...properties('pro3','pro4').drop()
     * */
    private ScriptAndParam update(TitanScriptModel titanScriptModel,Variable variable){
        StringBuilder script = new StringBuilder();
        Map<String, Object> param = new HashMap<>();
        String ele = "ele";
        if (titanScriptModel instanceof TitanScriptModelVertex){
            script.append("Vertex ").append(ele).append("=g.V()");
        }
        if (titanScriptModel instanceof TitanScriptModelEdge){
            script.append("Edge ").append(ele).append("=g.E()");
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
        appendHas(titanScriptModel.getCompositeKeyMap(),variable,script,param);
        appendNext(script);
        appendProperty4Next(ele, updateValues,variable,script,param);
        appendRemoveAllProperty(ele,dropValues, script);

        appendReturn(script, ele);
        return new ScriptAndParam(script, param);
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

    private void appendRemoveAllProperty(String element,List<String> list , StringBuilder script){
        for (String field : list){
            appendRemoveProperty4Next(element, script, field);
        }
    }

    private void appendRemoveProperty4Next(String element,StringBuilder script, String field){
        script.append(";");
        script.append(element);
        appendProperty(script, field);
        appendRemove(script);
    }

    private void appendHas(Map<String, Object> values , Variable variable, StringBuilder script , Map<String, Object> param){
        if (values == null || values.isEmpty()){
            return;
        }
        for (String key : values.keySet()){
            String name = key + variable.getSuffix();
            script.append(".").append(KeyWords.has.toString()).append("('").append(key).append("',").append(name).append(")");
            param.put(name, values.get(key));
        }
    }


    private void appendProperty4Next(String element,Map<String, Object> values, Variable variable, StringBuilder script, Map<String, Object> param){
        for (String key : values.keySet()){
            script.append(";").append(element);
            String name = key + variable.getSuffix();
            script.append(".").append(KeyWords.property.toString()).append("('").append(key).append("',").append(name).append(")");
            param.put(name, values.get(key));
        }
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
        script.append("else{").append(elseRunScript).append(";").append("}");
    }

    private void whileScript(StringBuilder script, String conditionScript,String whileRunScript){
        script.append("while(").append(conditionScript).append("){").append(whileRunScript).append(";").append("}");
    }

    private void appendReturn(StringBuilder script ,String element){
        script.append(";return ").append(element).append(";");
    }

    private void appendRemove(StringBuilder script){
        script.append(".remove()");
    }

    private void appendProperty(StringBuilder script, String field){
        script.append(".property('").append(field).append("')");
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

}
