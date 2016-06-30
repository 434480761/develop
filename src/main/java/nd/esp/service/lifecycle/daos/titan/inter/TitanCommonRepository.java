package nd.esp.service.lifecycle.daos.titan.inter;

import org.apache.tinkerpop.gremlin.driver.ResultSet;

import java.util.List;
import java.util.Map;

/**
 * Created by liuran on 2016/5/31.
 */
public interface TitanCommonRepository {
    public Long executeScriptUniqueLong(String script, Map<String, Object> params);
    public String executeScriptUniqueString(String script, Map<String, Object> params);
    public void executeScript(String script, Map<String, Object> params);
    public void executeScript(String script);
    public String dropEdgeSoft(String sourceType, String sourceid, String targetType, String targetid);
    public void dropEdge(String sourceType, String sourceid, String targetType, String targetid);
    public String addEdge(String sourceType, String sourceid, String targetType, String targetid, String edgeLabel, Object params);
    public void dropVertexById(Long id);
    public Long dropVertexSoftById(Long id);
    public Long getVertexIdByLabelAndId(String primaryCategory, String identifier);
    public String getEdgeIdByLabelAndId(String primaryCategory, String identifier);
    public ResultSet executeScriptResultSet(String script, Map<String, Object> params);
    public ResultSet executeScriptResultSet(String script);
    public List<Long> executeScriptListLong(String script, Map<String, Object> params);
    public List<Double> executeScriptListDouble(String script, Map<String, Object> params);
    public Double executeScriptUniqueDouble(String script, Map<String, Object> params);

    public void deleteVertexById(String id);
    public void deleteEdgeById(String id);
    public void butchDeleteVertexById(List<String> ids);
    public void butchDeleteEdgeById(List<String> ids);

    public void addSetProperty(String identifier, String primaryCategory, String fieldName, String value);

}
