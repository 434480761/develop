package nd.esp.service.lifecycle.utils.titan.script.script;

import nd.esp.service.lifecycle.utils.titan.script.model.TitanModel;
import nd.esp.service.lifecycle.utils.titan.script.utils.ParseAnnotation;

/**
 * Created by Administrator on 2016/8/24.
 */
public class TitanScriptBuilder {
    TitanScriptModel titanScriptModel;
    public TitanScriptBuilder(TitanModel titanModel){
        this.titanScriptModel = ParseAnnotation.createScriptModel(titanModel);
    }
    /**
     * 添加节点，脚本可以单独执行，返回titan内部ID
     * */
    public TitanScriptBuilder addNode(){
        return null;
    }

    /**
     * 添加节点，脚本可以单独执行，返回titan内部ID
     * */
    public TitanScriptBuilder updateNode(){
        return null;
    }

    /**
     * 获取节点
     * */
    public TitanScriptBuilder getNode(){
        return null;
    }

    /**
     * 删除节点，必须是一个单独节点
     * */
    public TitanScriptBuilder deleteNode(){
        return null;
    }

    public TitanScriptBuilder addEdge(){
        return null;
    }

    public TitanScriptBuilder updateEdge(){
        return null;
    }

    public TitanScriptBuilder deleteEdge(){
        return null;
    }

    public TitanScriptBuilder updateEdgeByPrimaryKey(){
        return null;
    }

    public TitanScriptBuilder methodEnd(){
        return null;
    }

    public TitanScriptBuilder scriptEnd(){
        return null;
    }


}
