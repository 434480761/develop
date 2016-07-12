package nd.esp.service.lifecycle.daos.titan;

import nd.esp.service.lifecycle.daos.titan.inter.TitanCommonRepository;
import nd.esp.service.lifecycle.support.StaticDatas;
import nd.esp.service.lifecycle.support.busi.titan.GremlinClientFactory;
import nd.esp.service.lifecycle.utils.TitanScritpUtils;
import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.driver.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * Created by liuran on 2016/5/31.
 */
@Repository
public class TitanCommonRepositoryImpl implements TitanCommonRepository {
	private final static Logger LOG = LoggerFactory.getLogger(TitanCommonRepositoryImpl.class);
    @Autowired
    private Client client;

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    @Override
    /**
     * 脚本执行后可能获取唯一的Long类型ID
     * 1、节点添加
     * 2、单个节点的查找
     * 3、单条关系的查找
     * */
    public Long executeScriptUniqueLong(String script, Map<String, Object> params) throws Exception {

        return submitUniqueLong(script, params);
    }

    @Override
    /**
     * 脚本执行后可以获取唯一的StringID
     * 1、关系的添加
     * 2、单条路径的查找
     * */
    public String executeScriptUniqueString(String script, Map<String, Object> params) throws Exception {
        return submitUniqueString(script, params);
    }

    @Override
    /**
     * 执行的脚本不需要返回值
     * */
    public void executeScript(String script, Map<String, Object> params) throws Exception {
        submitScript(script, params);
    }

    @Override
    /**
     * 执行的脚本不需要返回值
     * */
    public void executeScript(String script) throws Exception {
        submitScript(script, null);
    }


    /**
     * 通过源资和目标资源删除关系
     */
    @Override
    public void dropEdge(String sourceType, String sourceId, String targetType, String targetId) throws Exception {
        String script = "g.V().has(sourceType,'identifier',sourceId).outE()" +
                ".as('x').select('x').inV().has(targetType,'identifier',targetId)" +
                ".select('x').drop()";
        Map<String, Object> param = new HashMap<>();
        param.put("sourceType", sourceType);
        param.put("sourceId", sourceId);
        param.put("targetType", targetType);
        param.put("targetId", targetId);

        submitScript(script, param);
    }

    @Override
    public String addEdge(String sourceType, String sourceid, String targetType, String targetid, String edgeLabel, Object params) throws Exception {
        Long sourceNodeId = getVertexIdByLabelAndId(sourceType, sourceid);
        if(sourceNodeId == null){
            LOG.info("源资源不存在:"+sourceid);
        }

        Long targetNodeId = getVertexIdByLabelAndId(targetType, targetid);
        if(targetNodeId == null){
            LOG.info("目标资源不存在:"+sourceid);
        }

        StringBuffer script = new StringBuffer("g.V(sourceNodeId).next().addEdge(edgeLabel,g.V(targetNodeId)");
        Map<String, Object> paramMap = TitanScritpUtils.getParamAndChangeScript(script, params);
        paramMap.put("sourceNodeId",sourceNodeId);
        paramMap.put("edgeLabel",edgeLabel);
        paramMap.put("targetNodeId",targetNodeId);
        script.append(").id()");

        return submitUniqueString(script.toString(), paramMap);
    }




    /**
     * 通过源资源和目标资源删除关系（软删除）
     */
    @Override
    public String dropEdgeSoft(String sourceType, String sourceId, String targetType, String targetId) throws Exception {
        String script = "g.V().has(sourceType,'identifier',sourceId).outE()" +
                ".as('x').select('x').inV().has(targetType,'identifier',targetId)" +
                ".select('x').property('enable','false').id()";
        Map<String, Object> param = new HashMap<>();
        param.put("sourceType", sourceType);
        param.put("sourceId", sourceId);
        param.put("targetType", targetType);
        param.put("targetId", targetId);

        return submitUniqueString(script, param);
    }

    @Override
    public void dropVertexById(Long id) throws Exception {
        String script = "g.V(" + id + ").drop();";
        submitScript(script, null);
    }

    @Override
    public Long dropVertexSoftById(Long id) throws Exception {
        String script = "g.V(" + id + ").property('lc_enable','false').id()";
        Map<String, Object> param = new HashMap<>();
        param.put("id", id);

        return submitUniqueLong(script, param);
    }

    @Override
    public Long getVertexIdByLabelAndId(String primaryCategory, String identifier) throws Exception {
        String script = "g.V().has(primaryCategory,'identifier',identifier).id()";
        Map<String, Object> param = new HashMap<>();
        param.put("primaryCategory", primaryCategory);
        param.put("identifier", identifier);

        return executeScriptUniqueLong(script, param);
    }

    @Override
    public String getEdgeIdByLabelAndId(String primaryCategory, String identifier) throws Exception {
        String script = "g.E().has(primaryCategory,'identifier',identifier).id()";
        Map<String, Object> param = new HashMap<>();
        param.put("primaryCategory", primaryCategory);
        param.put("identifier", identifier);
        return executeScriptUniqueString(script,param);
    }

    @Override
    public ResultSet executeScriptResultSet(String script, Map<String, Object> params) throws Exception {
        return submitScriptResultRet(script, params);
    }

    @Override
    public ResultSet executeScriptResultSet(String script) throws Exception {
        return submitScriptResultRet(script, null);
    }

    @Override
    public List<Long> executeScriptListLong(String script, Map<String, Object> params) throws Exception {
        return submitListLong(script, params);
    }

    @Override
    public List<Double> executeScriptListDouble(String script, Map<String, Object> params) throws Exception {
        return submitListDouble(script, params);
    }

    @Override
    public Double executeScriptUniqueDouble(String script, Map<String, Object> params) throws Exception {
        return submitUniqueDouble(script, params);
    }

    @Override
    public void deleteVertexById(String id) throws Exception {
        String script = "g.V().has('identifier',identifier).drop()";
        HashMap<String, Object> pramaMap = new HashMap<>();
        pramaMap.put("identifier",id);

        submitScript(script, pramaMap);
    }

    @Override
    public void deleteVertexByLabelAndIdentifier(String primaryCategory, String identifier) throws Exception {
        String script = "g.V().has(primaryCategory,'identifier',identifier).drop()";
        Map<String,Object> param = new HashMap<>();
        param.put("identifier", identifier);
        param.put("primaryCategory", primaryCategory);

        submitScript(script, param);
    }

    @Override
    public void deleteAllOutVertexByResourceAndVertexLabel(String primaryCategory,
                                                           String identifier,
                                                           String vertexLabel) throws Exception {
        String script = "g.V().has(primaryCategory,'identifier',identifier).outE().inV().hasLabel(vertexLabel).drop()";
        Map<String, Object> param = new HashMap<>();
        param.put("primaryCategory", primaryCategory);
        param.put("identifier", identifier);
        param.put("vertexLabel", vertexLabel);

        executeScript(script, param);
    }

    @Override
    public void deleteEdgeById(String id) throws Exception {
        String script = "g.E().has('identifier',identifier).drop()";
        HashMap<String, Object> pramaMap = new HashMap<>();
        pramaMap.put("identifier",id);
        submitScript(script, pramaMap);
    }

    @Override
    public void butchDeleteVertexById(List<String> ids) throws Exception {
        for(String id : ids){
            deleteVertexById(id);
        }
    }

    @Override
    public void butchDeleteEdgeById(List<String> ids) throws Exception {
        for(String id : ids){
            deleteEdgeById(id);
        }
    }

    private Double submitUniqueDouble(String script, Map<String, Object> params) throws Exception {
        if(!StaticDatas.TITAN_SWITCH){
            return 0D;
        }
        Double id = null;
        try {
            ResultSet resultSet = client.submit(script, params);
            Iterator<Result> iterator = resultSet.iterator();
            if (iterator.hasNext()) {
                id = iterator.next().getDouble();
            }
        } catch (RuntimeException ex) {
            LOG.error("gremlin submit script:{" + script + "}|params:{" + params + "}");
            throw ex;
        }

        return id;
    }

    private List<Double> submitListDouble(String script, Map<String, Object> params) throws Exception {
        if(!StaticDatas.TITAN_SWITCH){
            return new ArrayList<>();
        }
        List<Double> ids = new LinkedList<>();
        try {
            ResultSet resultSet = client.submit(script, params);
            Iterator<Result> iterator = resultSet.iterator();
            while (iterator.hasNext()) {
                Double id = iterator.next().getDouble();
                ids.add(id);
            }
        } catch (RuntimeException ex) {
            LOG.error("gremlin submit script:{" + script + "}|params:{" + params + "}");
            throw ex;
        }

        return ids;
    }

    private List<Long> submitListLong(String script, Map<String, Object> params) throws Exception {
        if(!StaticDatas.TITAN_SWITCH){
            return new ArrayList<>();
        }
        List<Long> ids = new LinkedList<>();
        try {
            ResultSet resultSet = client.submit(script, params);
            Iterator<Result> iterator = resultSet.iterator();
            while (iterator.hasNext()) {
                Long id = iterator.next().getLong();
                ids.add(id);
            }
        } catch (RuntimeException ex) {
            LOG.error("gremlin submit script:{" + script + "}|params:{" + params + "}");
            throw ex;
        }

        return ids;
    }

    private void submitScript(String script, Map<String, Object> params) throws Exception{
        if(!StaticDatas.TITAN_SWITCH){
            return;
        }
        try {
            ResultSet resultSet = client.submit(script, params);
            Iterator<Result> iterator = resultSet.iterator();
            if (iterator.hasNext()) {
                iterator.next();
            }
        } catch (RuntimeException ex) {
            LOG.error("gremlin submit script:{" + script + "}|params:{" + params + "}");
            throw ex;
        }
    }

    private ResultSet submitScriptResultRet(String script, Map<String, Object> params) throws Exception{
        if(!StaticDatas.TITAN_SWITCH){
            return null;
        }
        ResultSet resultSet = null;
        try {
            resultSet = client.submit(script, params);
        } catch (RuntimeException ex) {
            LOG.error("gremlin submit script:{" + script + "}|params:{" + params + "}");
            throw ex;
        }

        return resultSet;
    }

    /**
     * @return id 节点或边的ID
     * id==null 节点不存在或提交出现异常
     */
    private Long submitUniqueLong(String script, Map<String, Object> params) throws Exception {
        if(!StaticDatas.TITAN_SWITCH){
            return 0L;
        }
        Long id = null;
        try {
            ResultSet resultSet = client.submit(script, params);
            Iterator<Result> iterator = resultSet.iterator();
            if (iterator.hasNext()) {
                id = iterator.next().getLong();
            }
        } catch (RuntimeException ex) {
            LOG.error("gremlin submit script:{" + script + "}|params:{" + params + "}");
            throw ex;
        }

        return id;
    }

    /**
     * @return id 节点、边或路径的ID
     * id==null 节点不存在或提交出现异常
     */
    private String submitUniqueString(String script, Map<String, Object> params) throws Exception {
        if(!StaticDatas.TITAN_SWITCH){
            return "****";
        }
        String id = null;
        try {
            ResultSet resultSet = client.submit(script, params);
            Iterator<Result> iterator = resultSet.iterator();
            if (iterator.hasNext()) {
                id = iterator.next().getString();
            }
        } catch (RuntimeException ex) {
            LOG.error("gremlin submit script:{" + script + "}|params:{" + params + "}");
            throw ex;
        }
        return id;
    }

    public static void main(String[] args) {
        GremlinClientFactory factory = new GremlinClientFactory();
        factory.init();
        Client client = factory.getGremlinClient();

        String script = "g.V().has('identifier','004516e5-a1f9-4c5a-b03a-ded9048412a0').outE().hasLabel('has_chapter').values('left')";

        ResultSet resultSet = client.submit(script);
        Iterator<Result> iterator = resultSet.iterator();
        while (iterator.hasNext()){
            System.out.println(iterator.next().getLong());
        }

    }

}
