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

        public void setSuffix(String suffix) {
            this.suffix = suffix;
        }

        public String getVariable() {
            return variable;
        }

        public void setVariable(String variable) {
            this.variable = variable;
        }
    }

    /**
     * 添加节点，脚本可以单独执行，返回titan内部ID
     * */
    public TitanScriptBuilder update(TitanModel model){
        TitanScriptModel titanScriptModel = ParseAnnotation.createScriptModel(model);
        update(titanScriptModel);
        return this;
    }

    /**
     * 获取节点
     * */
    public TitanScriptBuilder get(TitanScriptModel titanScriptModel){
        return null;
    }

    /**
     * 删除节点，必须是一个单独节点
     * */
    public TitanScriptBuilder delete(TitanScriptModel titanScriptModel){
        return null;
    }

    public TitanScriptBuilder add(TitanModel model){
        TitanScriptModel titanScriptModel = ParseAnnotation.createScriptModel(model);
        add(titanScriptModel);
        return this;
    }

    public TitanScriptBuilder saveOrUpdate(TitanModel model){
        TitanScriptModel titanScriptModel = ParseAnnotation.createScriptModel(model);
        add(titanScriptModel);
        return this;
    }

    public TitanScriptBuilder methodEnd(){
        return null;
    }

    public TitanScriptBuilder scriptEnd(){
        return null;
    }

    public TitanScriptBuilder ifStart(){
        return null;
    }

    public TitanScriptBuilder ifEnd(){
        return null;
    }

    public TitanScriptBuilder elseStart(){
        return null;
    }

    public TitanScriptBuilder elseEnd(){
        return null;
    }

    private void add(TitanScriptModel titanScriptModel){
        Variable variable = createVariable(titanScriptModel.getType());
        StringBuilder script = new StringBuilder() ;

        Map<String, Object> param = new HashMap<>();
        if (titanScriptModel instanceof TitanScriptModelVertex){
            script = new StringBuilder(variable.getVariable()+"=graph");
            appendAddVertex(titanScriptModel.getFieldMap(),titanScriptModel.getLabel(),variable,script,param);
        }
        if (titanScriptModel instanceof TitanScriptModelEdge){
            TitanScriptModelEdge edge = (TitanScriptModelEdge) titanScriptModel;
            script = new StringBuilder("g.V()");
            appendHas(edge.getResourceKeyMap(),variable,script,param);
            appendNext(script);

            StringBuilder addEdgeInner = new StringBuilder("g.V()");
            appendHas(edge.getTargetKeyMap(),variable,addEdgeInner,param);
            appendNext(addEdgeInner);

            appendAddEdge(edge.getFieldMap(),edge.getLabel(),addEdgeInner.toString(),variable,script,param);

        }

        appendId(script);

        this.script.append(script).append(";");
        this.param.putAll(param);

    }

    private void update(TitanScriptModel titanScriptModel){
        Variable variable = createVariable(titanScriptModel.getType());
        StringBuilder script = new StringBuilder() ;

        Map<String, Object> param = new HashMap<>();
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

        appendHas(titanScriptModel.getCompositeKeyMap(),variable,script,param);
        appendProperty(updateValues,variable,script,param);
        appendProperties(dropValues,script);

        script.append(";");



        this.script.append(script).append(";");



        this.param.putAll(param);
    }

    private void getTitanId(TitanScriptModel titanScriptModel){
        Variable variable = createVariable(titanScriptModel.getType());
        StringBuilder script = new StringBuilder("g.");
        if (titanScriptModel instanceof TitanScriptModelVertex){
            script.append("V()");
        }
        if (titanScriptModel instanceof TitanScriptModelEdge){
            script.append("E()");
        }
        appendHas(titanScriptModel.getCompositeKeyMap(),variable,script,param);
        appendId(script);
        this.script.append(script).append(";");
        this.param.putAll(param);
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
                script.append(keys.get(i));
            } else {
                script.append(",").append(keys.get(i));
            }
        }

        script.append(")");
    }

    private void appendDrop(StringBuilder script){
        script.append(".drop()");
    }

    private void appendAddVertex(Map<String, Object> values,String label , Variable variable, StringBuilder script , Map<String, Object> param){
        String labelName = "nodeLabel"+variable.getSuffix();
        script.append(".addVertex(").append(labelName);
        param.put(labelName, label);
        for (String key : values.keySet()){
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
            String name = key + variable.getSuffix();
            script.append(",'").append(key).append("',").append(name);
            param.put(name, values.get(key));
        }
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


    class Builder{
        StringBuilder sb = new StringBuilder();
        public Builder(Builder builder){
            this.sb = new StringBuilder(builder.sb);
        }
        public Builder(String head){
            sb.append(head);
        }
        public Builder has(String key, String value){
            sb.append(".has(").append("'").append(key).append("'").append(",").append(value).append(")");
            return this;
        }
        public Builder hasLabel(String key, String value){
            sb.append(".hasLabel(").append("'").append(key).append("'").append(",").append(value).append(")");
            return this;
        }
        public Builder outE(){
            sb.append(".outE()");
            return this;
        }
        public Builder inV(){
            sb.append(".inV()");
            return this;
        }
        public StringBuilder end(){
            return sb.append(";");
        }
        public Builder id(){
            sb.append(".id()");
            return this;
        }
        public Builder count(){
            sb.append(".count()");
            return this;
        }
    }

}
