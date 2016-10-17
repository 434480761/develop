package nd.esp.service.lifecycle.utils.titan.script.script;

import java.util.Map;
import java.util.Objects;

/**
 * Created by Administrator on 2016/8/24.
 */
public class TitanScriptModel {
    public enum Type {
        E,V,S
    }
    private Type type;
    private String label;
    private Map<String, Object> compositeKeyMap;
    private Map<String, Object> fieldMap;
    private Map<String, String> fieldNameAndTitanNameMap;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Map<String, Object> getCompositeKeyMap() {
        return compositeKeyMap;
    }

    public void setCompositeKeyMap(Map<String, Object> compositeKeyMap) {
        this.compositeKeyMap = compositeKeyMap;
    }

    public Map<String, Object> getFieldMap() {
        return fieldMap;
    }

    public void setFieldMap(Map<String, Object> fieldMap) {
        this.fieldMap = fieldMap;
    }

    public Map<String, String> getFieldNameAndTitanNameMap() {
        return fieldNameAndTitanNameMap;
    }

    public void setFieldNameAndTitanNameMap(Map<String, String> fieldNameAndTitanNameMap) {
        this.fieldNameAndTitanNameMap = fieldNameAndTitanNameMap;
    }
}
