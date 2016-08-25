package nd.esp.service.lifecycle.utils.titan.script.script;

import java.util.Map;
import java.util.Objects;

/**
 * Created by Administrator on 2016/8/24.
 */
public class TitanScriptModel {
    private String label;
    private Map<String, Objects> primaryKeyMap;
    private Map<String, Objects> valueMap;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Map<String, Objects> getPrimaryKeyMap() {
        return primaryKeyMap;
    }

    public void setPrimaryKeyMap(Map<String, Objects> primaryKeyMap) {
        this.primaryKeyMap = primaryKeyMap;
    }

    public Map<String, Objects> getValueMap() {
        return valueMap;
    }

    public void setValueMap(Map<String, Objects> valueMap) {
        this.valueMap = valueMap;
    }
}
