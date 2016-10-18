package nd.esp.service.lifecycle.daos.titan;

import nd.esp.service.lifecycle.daos.titan.inter.TitanCommonRepository;
import nd.esp.service.lifecycle.support.busi.titan.GremlinClientFactory;
import nd.esp.service.lifecycle.support.staticdata.StaticDatas;

import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.driver.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * Created by liuran on 2016/5/31.
 */
@Repository
public class TitanCommonRepositoryImpl implements TitanCommonRepository {
	private final static Logger LOG = LoggerFactory.getLogger(TitanCommonRepositoryImpl.class);

    public Client singleClient() {
        return GremlinClientFactory.getSingleClient();
    }

    public Client searchClient(){
        return GremlinClientFactory.getSearchClient();
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

    @Override
    public Long getVertexIdByLabelAndId(String primaryCategory, String identifier) throws Exception {
        String script = "g.V().has('identifier',identifier).has('primary_category',primaryCategory).id()";
        Map<String, Object> param = new HashMap<>();
        param.put("primaryCategory", primaryCategory);
        param.put("identifier", identifier);

        return executeScriptUniqueLong(script, param);
    }
    @Override
    public Long getEnableVertexIdByLabelAndId(String primaryCategory, String identifier) throws Exception {
        String script = "g.V().has('primary_category',primaryCategory).has('identifier',identifier).has('lc_enable',lc_enable).id()";
        Map<String, Object> param = new HashMap<>();
        param.put("primaryCategory", primaryCategory);
        param.put("identifier", identifier);
        param.put("lc_enable", true);

        return executeScriptUniqueLong(script, param);
    }

    @Override
    public ResultSet executeScriptResultSet(String script, Map<String, Object> params) throws Exception {
        return submitScriptResultRet(script, params);
    }

    @Override
    public ResultSet executeScriptSearchResultSet(String script, Map<String, Object> params) throws Exception {
        return submitScriptSearchResultRet(script, params);
    }

    @Override
    public ResultSet executeScriptResultSet(String script) throws Exception {
        return submitScriptResultRet(script, null);
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
        String script = "g.V().has('identifier',identifier).has('primary_category',primaryCategory).drop()";
        Map<String,Object> param = new HashMap<>();
        param.put("identifier", identifier);
        param.put("primaryCategory", primaryCategory);

        submitScript(script, param);
    }

    @Override
    public void deleteAllOutVertexByResourceAndVertexLabel(String primaryCategory,
                                                           String identifier,
                                                           String vertexLabel) throws Exception {
        String script = "g.V().has('identifier',identifier).has('primary_category',primaryCategory).outE().inV().hasLabel(vertexLabel).drop()";
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
    public void batchDeleteEdgeByIds(List<String> ids) throws Exception {
        for (String id : ids){
            deleteEdgeById(id);
        }
    }

    @Override
    public void butchDeleteVertexById(List<String> ids) throws Exception {
        Map<String, Object> params = new HashMap<>();
        StringBuffer withInScript = new StringBuffer("within(");
        for (int i=0; i <ids.size() ;i ++){
            String indentifierName = "identifier"+i;
            if(i == 0){
                withInScript.append(indentifierName);
            } else {
                withInScript.append(",").append(indentifierName);
            }
            params.put(indentifierName, ids.get(i));
        }
        withInScript.append(")");
        String script = "g.V().has('identifier',"+withInScript.toString()+").drop();";
        executeScript(script, params);
    }

    @Override
    public void butchDeleteEdgeById(List<String> ids) throws Exception {
        for(String id : ids){
            deleteEdgeById(id);
        }
    }

    @Override
    public String getEdgeLabelById(String identifier) throws Exception {
        String script = "g.E().has('identifier',identifier).label();";
        Map<String, Object> param = new HashMap<>();
        param.put("identifier", identifier);
        return executeScriptUniqueString(script, param);
    }

    private Double submitUniqueDouble(String script, Map<String, Object> params) throws Exception {
        if(!StaticDatas.TITAN_SWITCH){
            return null;
        }
        Double id = null;
        try {
            ResultSet resultSet = singleClient().submit(script, params);
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
            ResultSet resultSet = singleClient().submit(script, params);
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


    private void submitScript(String script, Map<String, Object> params) throws Exception{
        if(!StaticDatas.TITAN_SWITCH){
            return;
        }
        try {
            ResultSet resultSet = singleClient().submit(script, params);
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
            resultSet = singleClient().submit(script, params);
        } catch (RuntimeException ex) {
            LOG.error("gremlin submit script:{" + script + "}|params:{" + params + "}");
            throw ex;
        }

        return resultSet;
    }

    private ResultSet submitScriptSearchResultRet(String script, Map<String, Object> params) throws Exception{
        ResultSet resultSet = null;
        try {
            resultSet = searchClient().submit(script, params);
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
            return null;
        }
        Long id = null;
        try {
            ResultSet resultSet = singleClient().submit(script, params);
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
            return null;
        }
        String id = null;
        try {
            ResultSet resultSet = singleClient().submit(script, params);
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
//        GremlinClientFactory factory = new GremlinClientFactory();
////        factory.init();
//
//        String script = "g.V().has('identifier','004516e5-a1f9-4c5a-b03a-ded9048412a0').outE().hasLabel('has_chapter').values('left')";
//
//        ResultSet resultSet = client.submit(script);
//        Iterator<Result> iterator = resultSet.iterator();
//        while (iterator.hasNext()){
//            System.out.println(iterator.next().getLong());
//        }

    }
}
