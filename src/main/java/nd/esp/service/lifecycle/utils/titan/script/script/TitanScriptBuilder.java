package nd.esp.service.lifecycle.utils.titan.script.script;

import nd.esp.service.lifecycle.utils.titan.script.model.TitanModel;
import nd.esp.service.lifecycle.utils.titan.script.utils.ParseAnnotation;

/**
 * Created by Administrator on 2016/8/24.
 */
public class TitanScriptBuilder {
    private enum  KeyWrods{
        node,edge,source,target,addVertex
    }
    private int firstIndex =0;
    private int secondIndex=0;
    TitanScriptModel titanScriptModel;
    public TitanScriptBuilder(){
        this.firstIndex = 0;
        this.secondIndex = 0;
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

    private StringBuilder add(){
        if(titanScriptModel instanceof  TitanScriptModelVertex){

        } else if(titanScriptModel instanceof TitanScriptModelEdge){

        }

        return null;
    }

    private StringBuilder getTitanId(){
        String head = KeyWrods.node.toString() +firstIndex+"_"+secondIndex + "g."+titanScriptModel.getType()+"()";
        Builder builder = new Builder(head);
        for (String key : titanScriptModel.getCompositeKeyMap().keySet()){
            builder.has(key, key);
        }

        return builder.id().end();
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
