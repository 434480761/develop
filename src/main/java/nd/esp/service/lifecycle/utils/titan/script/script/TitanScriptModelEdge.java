package nd.esp.service.lifecycle.utils.titan.script.script;

import java.util.Map;

/**
 * Created by Administrator on 2016/8/24.
 */
public class TitanScriptModelEdge extends TitanScriptModel{
    private Map<String, Object> resourceKeyMap;
    private Map<String, Object> targetKeyMap;

    public Map<String, Object> getResourceKeyMap() {
        return resourceKeyMap;
    }

    public void setResourceKeyMap(Map<String, Object> resourceKeyMap) {
        this.resourceKeyMap = resourceKeyMap;
    }

    public Map<String, Object> getTargetKeyMap() {
        return targetKeyMap;
    }

    public void setTargetKeyMap(Map<String, Object> targetKeyMap) {
        this.targetKeyMap = targetKeyMap;
    }
}
