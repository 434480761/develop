package nd.esp.service.lifecycle.utils.titan.script.script;

import nd.esp.service.lifecycle.utils.titan.script.model.TitanModel;

import java.util.Map;

/**
 * Created by Administrator on 2016/8/24.
 */
public class TitanScriptModelEdge extends TitanScriptModel{
    private Map<String, Object> resourceKeyMap;
    private String targetKeyMap;

    public Map<String, Object> getResourceKeyMap() {
        return resourceKeyMap;
    }

    public void setResourceKeyMap(Map<String, Object> resourceKeyMap) {
        this.resourceKeyMap = resourceKeyMap;
    }

    public String getTargetKeyMap() {
        return targetKeyMap;
    }

    public void setTargetKeyMap(String targetKeyMap) {
        this.targetKeyMap = targetKeyMap;
    }
}
