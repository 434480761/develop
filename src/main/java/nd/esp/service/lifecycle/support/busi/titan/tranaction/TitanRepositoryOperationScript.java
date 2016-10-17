package nd.esp.service.lifecycle.support.busi.titan.tranaction;

/**
 * Created by Administrator on 2016/10/14.
 */
public class TitanRepositoryOperationScript extends TitanRepositoryOperation{
    private Object[] customScriptParam;
    private String customScript;

    public TitanRepositoryOperationScript(String script, Object ... params){
        customScriptParam = params;
        customScript = script;
        setOperationType(TitanOperationType.script);
    }

    public String getCustomScript() {
        return customScript;
    }

    public Object[] getCustomScriptParam() {
        return customScriptParam;
    }

    @Override
    public void setOperationType(TitanOperationType operationType) {
        super.setOperationType(TitanOperationType.script);
    }
}
