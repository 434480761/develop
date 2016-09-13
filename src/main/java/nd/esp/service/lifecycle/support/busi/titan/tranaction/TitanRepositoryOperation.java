package nd.esp.service.lifecycle.support.busi.titan.tranaction;

import nd.esp.service.lifecycle.repository.EspEntity;

import java.util.Map;

/**
 * Created by Administrator on 2016/9/12.
 */
public class TitanRepositoryOperation {
    private EspEntity entity;
    private Map<String, Object> customProperty;
    private String customScript;
    private Map<String, Object> customScriptParam;
    private TitanOperationType operationType;

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

    public Map<String, Object> getCustomProperty() {
        return customProperty;
    }

    public void setCustomProperty(Map<String, Object> customProperty) {
        this.customProperty = customProperty;
    }

    public EspEntity getEntity() {
        return entity;
    }

    public void setEntity(EspEntity entity) {
        this.entity = entity;
    }

    public TitanOperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(TitanOperationType operationType) {
        this.operationType = operationType;
    }
}
