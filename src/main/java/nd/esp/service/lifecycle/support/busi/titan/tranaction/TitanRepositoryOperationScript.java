package nd.esp.service.lifecycle.support.busi.titan.tranaction;

import java.util.Map;

/**
 * Created by Administrator on 2016/10/14.
 */
public class TitanRepositoryOperationScript extends TitanRepositoryOperationBase{
    private Map<String, Object> customScriptParam;
    private String customScript;

    public String getCustomScript() {
        return customScript;
    }

    public void setCustomScript(String customScript) {
        this.customScript = customScript;
    }

    public Map<String, Object> getCustomScriptParam() {
        return customScriptParam;
    }

    public void setCustomScriptParam(Map<String, Object> customScriptParam) {
        this.customScriptParam = customScriptParam;
    }
}
