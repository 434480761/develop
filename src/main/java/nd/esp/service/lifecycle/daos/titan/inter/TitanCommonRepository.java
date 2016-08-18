package nd.esp.service.lifecycle.daos.titan.inter;

import org.apache.tinkerpop.gremlin.driver.ResultSet;

import java.util.List;
import java.util.Map;

/**
 * Created by liuran on 2016/5/31.
 */
public interface TitanCommonRepository {
	public Long executeScriptUniqueLong(String script, Map<String, Object> params) throws Exception;
    public String executeScriptUniqueString(String script, Map<String, Object> params) throws Exception;
    public void executeScript(String script, Map<String, Object> params) throws Exception;
    public void executeScript(String script) throws Exception;
    public Long getVertexIdByLabelAndId(String primaryCategory, String identifier) throws Exception;
    public Long getEnableVertexIdByLabelAndId(String primaryCategory, String identifier) throws Exception;
    public ResultSet executeScriptResultSet(String script, Map<String, Object> params) throws Exception;
    public ResultSet executeScriptResultSet(String script) throws Exception;
    public List<Double> executeScriptListDouble(String script, Map<String, Object> params) throws Exception;
    public Double executeScriptUniqueDouble(String script, Map<String, Object> params) throws Exception;

    public void deleteVertexById(String id) throws Exception;
    /**
     * 通过Label和ID删除节点
     * */
    public void deleteVertexByLabelAndIdentifier(String primaryCategory, String identifier) throws Exception;
    /**
     * 通过
     * */
    public void deleteAllOutVertexByResourceAndVertexLabel(String primaryCategory, String identifier , String vertexLabel) throws Exception;
    public void deleteEdgeById(String id) throws Exception;
    public void batchDeleteEdgeByIds(List<String> ids) throws Exception;
    public void butchDeleteVertexById(List<String> ids) throws Exception;
    public void butchDeleteEdgeById(List<String> ids) throws Exception;
    public String getEdgeLabelById(String identifier) throws Exception;


}
