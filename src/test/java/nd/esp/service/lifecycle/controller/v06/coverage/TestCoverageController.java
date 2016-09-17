package nd.esp.service.lifecycle.controller.v06.coverage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;
import nd.esp.service.lifecycle.impl.SimpleJunitTest4ResourceImpl;
import nd.esp.service.lifecycle.models.coverage.v06.CoverageModel;
import nd.esp.service.lifecycle.models.coverage.v06.CoverageModelForUpdate;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.vos.coverage.v06.CoverageViewModel;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nd.gaea.rest.testconfig.MockUtil;

public class TestCoverageController extends SimpleJunitTest4ResourceImpl {
    private static final Logger logger = LoggerFactory.getLogger(TestCoverageController.class);
    private final static Map<String, String> RESOURCE_MAP = new HashMap<String, String>();
    private final static String RES_TYPE = "assets";
    
//    @Test
    public void testAll(){
        //创建资源
        String uuid = UUID.randomUUID().toString();
        ResourceViewModel rvm = testCreate(RES_TYPE,uuid.toString(),null);
        Assert.assertEquals("测试创建资源不通过", uuid, rvm.getIdentifier());
        RESOURCE_MAP.put(uuid, RES_TYPE);
        
        String json = "";
        String uri = "";
        String resStr = "";
        
        //创建覆盖范围
        uri = "/v0.6/" + RES_TYPE + "/coverages/target";
        //0.正常创建
        CoverageViewModel cvm4Success = testCreateCovergae(uuid,"User","SHAREING");
        //再创建一个为了后面的测试
        CoverageViewModel cvm4Success2 = testCreateCovergae(uuid,"Group","FAVORITE");
        
        //1.创建失败,targetType不正常
        json = toJson(getCoverageModel(uuid,"ABC","SHAREING"));
        try {
            resStr = MockUtil.mockPost(mockMvc, uri, json);
        } catch (Exception e) {
            logger.error("create coverage error",e);
        }
        CoverageViewModel cvm = fromJson(resStr, CoverageViewModel.class);
        Assert.assertNull("targetType不正常但覆盖范围创建成功", cvm.getIdentifier());
        //2.创建失败,strategy不正常
        json = toJson(getCoverageModel(uuid,"User","BCD"));
        try {
            resStr = MockUtil.mockPost(mockMvc, uri, json);
        } catch (Exception e) {
            logger.error("create coverage error",e);
        }
        cvm = fromJson(resStr, CoverageViewModel.class);
        Assert.assertNull("strategy不正常但覆盖范围创建成功", cvm.getIdentifier());
        //3.创建失败,覆盖范围已存在
        json = toJson(getCoverageModel(uuid,"User","SHAREING"));
        try {
            resStr = MockUtil.mockPost(mockMvc, uri, json);
        } catch (Exception e) {
            logger.error("create coverage error",e);
        }
        cvm = fromJson(resStr, CoverageViewModel.class);
        Assert.assertNull("覆盖范围已存在但覆盖范围创建成功", cvm.getIdentifier());
        //4.创建失败,已有OWNER
        json = toJson(getCoverageModel(uuid,"User","OWNER"));
        try {
            resStr = MockUtil.mockPost(mockMvc, uri, json);
        } catch (Exception e) {
            logger.error("create coverage error",e);
        }
        cvm = fromJson(resStr, CoverageViewModel.class);
        Assert.assertNull("已有OWNER但覆盖范围创建成功", cvm.getIdentifier());
        
        //批量创建覆盖范围
        List<String> uuids = new ArrayList<String>();
        List<String> targetTypeList = new ArrayList<String>();
        List<String> targets = new ArrayList<String>();
        List<String> strategyList = new ArrayList<String>();
        List<String> targetTitleList = new ArrayList<String>();
        uri = "/v0.6/" + RES_TYPE + "/coverages/bulk";
        //1.创建失败,入参错误
        uuids.add("");
        targetTypeList.add("");
        targets.add("");
        strategyList.add("");
        targetTitleList.add("");
        json = toJson(getCoverageModelList(uuids, targetTypeList, targets, strategyList, targetTitleList));
        try {
            resStr = MockUtil.mockPost(mockMvc, uri, json);
        } catch (Exception e) {
            logger.error("batchCreate coverage error",e);
        }
        List<CoverageViewModel> cvmList = fromJson(resStr, List.class);
        Assert.assertNull("入参错误但创建成功", cvmList);
        //2.创建失败,targetType不对
        emptyLists(uuids, targetTypeList, targets, strategyList, targetTitleList);
        uuids.add(uuid);
        targetTypeList.add("ABC");
        targets.add("279904");
        strategyList.add("REPORTING");
        targetTitleList.add("覆盖范围单元测试");
        json = toJson(getCoverageModelList(uuids, targetTypeList, targets, strategyList, targetTitleList));
        try {
            resStr = MockUtil.mockPost(mockMvc, uri, json);
        } catch (Exception e) {
            logger.error("batchCreate coverage error",e);
        }
        cvmList = fromJson(resStr, List.class);
        Assert.assertNull("targetType不对但创建成功", cvmList);
        //3.创建失败,strategy不对
        emptyLists(uuids, targetTypeList, targets, strategyList, targetTitleList);
        uuids.add(uuid);
        targetTypeList.add("Org");
        targets.add("279904");
        strategyList.add("ABC");
        targetTitleList.add("覆盖范围单元测试");
        json = toJson(getCoverageModelList(uuids, targetTypeList, targets, strategyList, targetTitleList));
        try {
            resStr = MockUtil.mockPost(mockMvc, uri, json);
        } catch (Exception e) {
            logger.error("batchCreate coverage error",e);
        }
        cvmList = fromJson(resStr, List.class);
        Assert.assertNull("strategy不对但创建成功", cvmList);
        //4.创建失败,入参中有两个OWNER
        emptyLists(uuids, targetTypeList, targets, strategyList, targetTitleList);
        uuids.add(uuid);
        targetTypeList.add("User");
        targets.add("279904");
        strategyList.add("OWNER");
        targetTitleList.add("覆盖范围单元测试");
        uuids.add(uuid);
        targetTypeList.add("Org");
        targets.add("279904");
        strategyList.add("OWNER");
        targetTitleList.add("覆盖范围单元测试");
        json = toJson(getCoverageModelList(uuids, targetTypeList, targets, strategyList, targetTitleList));
        try {
            resStr = MockUtil.mockPost(mockMvc, uri, json);
        } catch (Exception e) {
            logger.error("batchCreate coverage error",e);
        }
        cvmList = fromJson(resStr, List.class);
        Assert.assertNull("入参中有两个OWNER但创建成功", cvmList);
        //5.创建成功,只有已存在的
        emptyLists(uuids, targetTypeList, targets, strategyList, targetTitleList);
        uuids.add(uuid);
        targetTypeList.add("User");
        targets.add("279904");
        strategyList.add("SHAREING");
        targetTitleList.add("覆盖范围单元测试");
        json = toJson(getCoverageModelList(uuids, targetTypeList, targets, strategyList, targetTitleList));
        try {
            resStr = MockUtil.mockPost(mockMvc, uri, json);
        } catch (Exception e) {
            logger.error("batchCreate coverage error",e);
        }
        cvmList = fromJson(resStr, List.class);
        Assert.assertEquals("批量创建成功失败", 1 , cvmList.size());
        //6.创建成功
        emptyLists(uuids, targetTypeList, targets, strategyList, targetTitleList);
        uuids.add(uuid);
        targetTypeList.add("User");
        targets.add("279904");
        strategyList.add("SHAREING");
        targetTitleList.add("覆盖范围单元测试");
        uuids.add(uuid);
        targetTypeList.add("Space");
        targets.add("279904");
        strategyList.add("FAVORITE");
        targetTitleList.add("覆盖范围单元测试");
        json = toJson(getCoverageModelList(uuids, targetTypeList, targets, strategyList, targetTitleList));
        try {
            resStr = MockUtil.mockPost(mockMvc, uri, json);
        } catch (Exception e) {
            logger.error("batchCreate coverage error",e);
        }
        List<CoverageViewModel> cvmList4Success = fromJson(resStr, List.class);
        Assert.assertEquals("批量创建成功失败", 2 , cvmList4Success.size());
        
        //获取详细
        //1.返回空,覆盖范围id不存在
        uri = "/v0.6/" + RES_TYPE + "/coverages/11111";
        try {
            resStr = MockUtil.mockGet(mockMvc, uri, null);
        } catch (Exception e) {
            logger.error("get coverage error",e);
        }
        cvm = fromJson(resStr, CoverageViewModel.class);
        Assert.assertNull("覆盖范围id不存在但返回成功", cvm);
        //2.正常返回
        uri = "/v0.6/" + RES_TYPE + "/coverages/" + cvm4Success.getIdentifier();
        try {
            resStr = MockUtil.mockGet(mockMvc, uri, null);
        } catch (Exception e) {
            logger.error("get coverage error",e);
        }
        cvm = fromJson(resStr, CoverageViewModel.class);
        Assert.assertNotNull("获取详细失败", cvm);
        Assert.assertEquals("获取详细失败", "覆盖范围单元测试", cvm4Success.getTargetTitle());
        
        //批量获取详细
        //1.rcid不存在,返回空
        uri = "/v0.6/" + RES_TYPE + "/coverages/list?rcid=111";
        try {
            resStr = MockUtil.mockGet(mockMvc, uri, null);
        } catch (Exception e) {
            logger.error("get coverage error",e);
        }
        Map<String,CoverageViewModel> cvmMap = fromJson(resStr, Map.class);
        Assert.assertTrue("rcid不存在但是获取成功", cvmMap.isEmpty());
        //2.批量获取成功
        uri = "/v0.6/" + RES_TYPE + "/coverages/list?rcid=" + cvm4Success.getIdentifier();
        try {
            resStr = MockUtil.mockGet(mockMvc, uri, null);
        } catch (Exception e) {
            logger.error("get coverage error",e);
        }
        cvmMap = fromJson(resStr, Map.class);
        Assert.assertEquals("批量获取失败", 1, cvmMap.size());
        
        //修改覆盖范围
        //1.覆盖范围id不存在,修改失败
        uri = "/v0.6/" + RES_TYPE + "/coverages/11111";
        json = toJson(getCoverageModelForUpdate("Space", "REPORTING"));
        try {
            resStr = MockUtil.mockPut(mockMvc, uri, json);
        } catch (Exception e) {
            logger.error("update coverage error",e);
        }
        CoverageViewModel cvm4Update = fromJson(resStr, CoverageViewModel.class);
        Assert.assertNull("覆盖范围id不存在但修改成功", cvm4Update.getIdentifier());
        //2.修改失败,targetType不对
        uri = "/v0.6/" + RES_TYPE + "/coverages/" + cvm4Success.getIdentifier();
        json = toJson(getCoverageModelForUpdate("ABC", "REPORTING"));
        try {
            resStr = MockUtil.mockPut(mockMvc, uri, json);
        } catch (Exception e) {
            logger.error("update coverage error",e);
        }
        cvm4Update = fromJson(resStr, CoverageViewModel.class);
        Assert.assertNull("targetType不对但修改成功", cvm4Update.getIdentifier());
        //3.修改失败,strategy不对
        json = toJson(getCoverageModelForUpdate("Space", "BCA"));
        try {
            resStr = MockUtil.mockPut(mockMvc, uri, json);
        } catch (Exception e) {
            logger.error("update coverage error",e);
        }
        cvm4Update = fromJson(resStr, CoverageViewModel.class);
        Assert.assertNull("strategy不对但修改成功", cvm4Update.getIdentifier());
        //4.修改失败,修改成已存在的覆盖范围
        uri = "/v0.6/" + RES_TYPE + "/coverages/" + cvm4Success2.getIdentifier();
        json = toJson(getCoverageModelForUpdate("User", "SHAREING"));
        try {
            resStr = MockUtil.mockPut(mockMvc, uri, json);
        } catch (Exception e) {
            logger.error("update coverage error",e);
        }
        cvm4Update = fromJson(resStr, CoverageViewModel.class);
        Assert.assertNull("修改成已存在的覆盖范围但修改成功", cvm4Update.getIdentifier());
        //5.修改失败,已有一个OWNER,再修改另一个为OWNER
        json = toJson(getCoverageModelForUpdate("Group", "OWNER"));
        try {
            resStr = MockUtil.mockPut(mockMvc, uri, json);
        } catch (Exception e) {
            logger.error("update coverage error",e);
        }
        cvm4Update = fromJson(resStr, CoverageViewModel.class);
        Assert.assertNull("再修改另一个为OWNER但修改成功", cvm4Update.getIdentifier());
        //6.修改成功
        CoverageModelForUpdate cvfu = getCoverageModelForUpdate("Group", "FAVORITE");
        cvfu.setTargetTitle("覆盖范围单元测试-update success");
        json = toJson(cvfu);
        try {
            resStr = MockUtil.mockPut(mockMvc, uri, json);
        } catch (Exception e) {
            logger.error("update coverage error",e);
        }
        cvm4Update = fromJson(resStr, CoverageViewModel.class);
        Assert.assertNotNull("修改失败",cvm4Update);
        Assert.assertEquals("修改失败", "覆盖范围单元测试-update success", cvm4Update.getTargetTitle());
        
        //批量修改
        uri = "/v0.6/" + RES_TYPE + "/coverages/bulk";
        //1.批量修改失败,targetType不对
        emptyLists(uuids, targetTypeList, targets, strategyList, targetTitleList);
        uuids.add(cvm4Success2.getIdentifier());
        targetTypeList.add("ABC");
        targets.add("279904");
        strategyList.add("FAVORITE");
        targetTitleList.add("覆盖范围单元测试");
        json = toJson(getCoverageModelForUpdateMap(uuids, targetTypeList, targets, strategyList, targetTitleList));
        try {
            resStr = MockUtil.mockPut(mockMvc, uri, json);
        } catch (Exception e) {
            logger.error("batchUpdate coverage error",e);
        }
        Map<String,CoverageViewModel> cvmMap4Update = fromJson(resStr, Map.class);
        Assert.assertNotEquals("targetType不对但是获取成功", 1, cvmMap4Update.values().size());
        //2.批量修改失败,strategy不对
        emptyLists(uuids, targetTypeList, targets, strategyList, targetTitleList);
        uuids.add(cvm4Success2.getIdentifier());
        targetTypeList.add("Org");
        targets.add("279904");
        strategyList.add("ABCD");
        targetTitleList.add("覆盖范围单元测试");
        json = toJson(getCoverageModelForUpdateMap(uuids, targetTypeList, targets, strategyList, targetTitleList));
        try {
            resStr = MockUtil.mockPut(mockMvc, uri, json);
        } catch (Exception e) {
            logger.error("batchUpdate coverage error",e);
        }
        cvmMap4Update = fromJson(resStr, Map.class);
        Assert.assertNotEquals("strategy不对但是获取成功", 1, cvmMap4Update.values().size());
        //3.批量修改成功,修改成已存在的
        emptyLists(uuids, targetTypeList, targets, strategyList, targetTitleList);
        uuids.add("1111111");
        targetTypeList.add("Org");
        targets.add("279904");
        strategyList.add("FAVORITE");
        targetTitleList.add("覆盖范围单元测试");
        uuids.add(cvm4Success2.getIdentifier());
        targetTypeList.add("User");
        targets.add("279904");
        strategyList.add("SHAREING");
        targetTitleList.add("覆盖范围单元测试");
        uuids.add(cvm4Success.getIdentifier());
        targetTypeList.add("User");
        targets.add("279904");
        strategyList.add("SHAREING");
        targetTitleList.add("覆盖范围单元测试");
        json = toJson(getCoverageModelForUpdateMap(uuids, targetTypeList, targets, strategyList, targetTitleList));
        try {
            resStr = MockUtil.mockPut(mockMvc, uri, json);
        } catch (Exception e) {
            logger.error("batchUpdate coverage error",e);
        }
        cvmMap4Update = fromJson(resStr, Map.class);
        Assert.assertEquals("批量修改失败", 2, cvmMap4Update.keySet().size());
        //4.批量修改成功
        emptyLists(uuids, targetTypeList, targets, strategyList, targetTitleList);
        uuids.add(cvm4Success2.getIdentifier());
        targetTypeList.add("Time");
        targets.add("279904");
        strategyList.add("REPORTING");
        targetTitleList.add("覆盖范围单元测试");
        json = toJson(getCoverageModelForUpdateMap(uuids, targetTypeList, targets, strategyList, targetTitleList));
        try {
            resStr = MockUtil.mockPut(mockMvc, uri, json);
        } catch (Exception e) {
            logger.error("batchUpdate coverage error",e);
        }
        cvmMap4Update = fromJson(resStr, Map.class);
        Assert.assertEquals("批量修改失败", 1, cvmMap4Update.keySet().size());
        
        //删除覆盖范围
        //1.删除失败,rcid不存在
        uri = "/v0.6/" + RES_TYPE + "/coverages/111111";
        try {
            resStr = MockUtil.mockDelete(mockMvc, uri, null);
        } catch (Exception e) {
            logger.error("delete coverage error",e);
        }
        Map<String,String> map4Delete = fromJson(resStr, Map.class);
        Assert.assertNotEquals("rcid不存在但删除成功", 2, map4Delete.keySet().size());
        //2.删除成功
        uri = "/v0.6/" + RES_TYPE + "/coverages/" + cvm4Success2.getIdentifier();
        try {
            resStr = MockUtil.mockDelete(mockMvc, uri, null);
        } catch (Exception e) {
            logger.error("delete coverage error",e);
        }
        map4Delete = fromJson(resStr, Map.class);
        Assert.assertEquals("删除失败", 2, map4Delete.keySet().size());
        
        //批量删除
        //再创建一个为了后面的测试
        CoverageViewModel cvm4Success3 = testCreateCovergae(uuid,"Group","FAVORITE");
        //1.删除失败,rcid都不存在
        uri = "/v0.6/" + RES_TYPE + "/coverages/bulk?rcid=11111";
        try {
            resStr = MockUtil.mockDelete(mockMvc, uri, null);
        } catch (Exception e) {
            logger.error("batchDelete coverage error",e);
        }
        map4Delete = fromJson(resStr, Map.class);
        Assert.assertNotEquals("rcids不存在但删除成功", 2, map4Delete.keySet().size());
        //2.删除成功
        uri = "/v0.6/" + RES_TYPE + "/coverages/bulk?rcid=" + cvm4Success3.getIdentifier();
        try {
            resStr = MockUtil.mockDelete(mockMvc, uri, null);
        } catch (Exception e) {
            logger.error("batchDelete coverage error",e);
        }
        map4Delete = fromJson(resStr, Map.class);
        Assert.assertEquals("删除失败", 2, map4Delete.keySet().size());
        
        //通过目标类型，覆盖范围策略，目标范围的标识，资源类型，目标资源标识删除覆盖范围
        //再创建一个为了后面的测试
        testCreateCovergae(uuid,"Group","FAVORITE");
        //1.删除成功,但是不存在的
        uri = "/v0.6/" + RES_TYPE + "/" + uuid + "/coverages?target_type=ABC";
        try {
            resStr = MockUtil.mockDelete(mockMvc, uri, null);
        } catch (Exception e) {
            logger.error("batchDelete coverage error",e);
        }
        map4Delete = fromJson(resStr, Map.class);
        Assert.assertEquals("删除失败", 2, map4Delete.keySet().size());
        //2.删除成功
        uri = "/v0.6/" + RES_TYPE + "/" + uuid + "/coverages?strategy=FAVORITE&target=279904";
        try {
            resStr = MockUtil.mockDelete(mockMvc, uri, null);
        } catch (Exception e) {
            logger.error("batchDelete coverage error",e);
        }
        map4Delete = fromJson(resStr, Map.class);
        Assert.assertEquals("删除失败", 2, map4Delete.keySet().size());
        
        //获取某个资源所覆盖的范围
        //1.查询成功,不带条件
        uri = "/v0.6/" + RES_TYPE + "/" + uuid + "/coverages";
        try {
            resStr = MockUtil.mockGet(mockMvc, uri, null);
        } catch (Exception e) {
            logger.error("get coverage error",e);
        }
        List<CoverageViewModel> cvmListById = fromJson(resStr, List.class);
        Assert.assertNotEquals("获取失败", 0, cvmListById.size());
        //2.查询失败,targetType不对
        uri = "/v0.6/" + RES_TYPE + "/" + uuid + "/coverages?targetType=ABC";
        try {
            resStr = MockUtil.mockGet(mockMvc, uri, null);
        } catch (Exception e) {
            logger.error("get coverage error",e);
        }
        cvmListById = fromJson(resStr, List.class);
        Assert.assertNull("targetType不对但获取成功", cvmListById);
        //3.查询失败,strategy不对
        uri = "/v0.6/" + RES_TYPE + "/" + uuid + "/coverages?strategy=ABC";
        try {
            resStr = MockUtil.mockGet(mockMvc, uri, null);
        } catch (Exception e) {
            logger.error("get coverage error",e);
        }
        cvmListById = fromJson(resStr, List.class);
        Assert.assertNull("strategy不对但获取成功", cvmListById);
        //4.查询成功
        uri = "/v0.6/" + RES_TYPE + "/" + uuid + "/coverages?targetType=User&target=279904&strategy=SHAREING";
        try {
            resStr = MockUtil.mockGet(mockMvc, uri, null);
        } catch (Exception e) {
            logger.error("get coverage error",e);
        }
        cvmListById = fromJson(resStr, List.class);
        Assert.assertNotEquals("获取失败", 0, cvmListById.size());
        
        //批量获取多个资源所覆盖的范围 
        //1.获取失败,rid为空
        uri = "/v0.6/" + RES_TYPE + "/coverages/bulk?rid=";
        try {
            resStr = MockUtil.mockGet(mockMvc, uri, null);
        } catch (Exception e) {
            logger.error("get coverage error",e);
        }
        Map<String, List<CoverageViewModel>> cvmMapById = fromJson(resStr, Map.class);
        Assert.assertNotEquals("rid为空但获取成功", 1, cvmMapById.size());
        //2.获取成功,但返回空
        uri = "/v0.6/" + RES_TYPE + "/coverages/bulk?rid=111122222";
        try {
            resStr = MockUtil.mockGet(mockMvc, uri, null);
        } catch (Exception e) {
            logger.error("get coverage error",e);
        }
        cvmMapById = fromJson(resStr, Map.class);
        Assert.assertEquals("获取失败", 0, cvmMapById.size());
        //2.获取成功
        uri = "/v0.6/" + RES_TYPE + "/coverages/bulk?rid=" + uuid;
        try {
            resStr = MockUtil.mockGet(mockMvc, uri, null);
        } catch (Exception e) {
            logger.error("get coverage error",e);
        }
        cvmMapById = fromJson(resStr, Map.class);
        Assert.assertEquals("获取失败", 1, cvmMapById.size());
    }
    
    @After
    public void after(){
        if(CollectionUtils.isNotEmpty(RESOURCE_MAP)){
            for(String id : RESOURCE_MAP.keySet()){
                testDelete(RESOURCE_MAP.get(id), id);
            }
        }
    }
    
    private CoverageViewModel testCreateCovergae(String uuid,String targetType,String strategy){
        String uri = "/v0.6/" + RES_TYPE + "/coverages/target";
        String json = toJson(getCoverageModel(uuid,targetType,strategy));
        String resStr = "";
        try {
            resStr = MockUtil.mockPost(mockMvc, uri, json);
        } catch (Exception e) {
            logger.error("create coverage error",e);
        }
        CoverageViewModel cvm4Success = fromJson(resStr, CoverageViewModel.class);
        Assert.assertNotNull("覆盖范围创建失败", cvm4Success);
        Assert.assertEquals("覆盖范围创建失败", "覆盖范围单元测试", cvm4Success.getTargetTitle());
        
        return cvm4Success;
    }
    
    private CoverageModel getCoverageModel(String uuid,String targetType,String strategy){
        CoverageModel cm = new CoverageModel();
        cm.setResource(uuid);
        cm.setTargetTitle("覆盖范围单元测试");
        cm.setTargetType(targetType);
        cm.setTarget("279904");
        cm.setStrategy(strategy);
        
        return cm;
    }
    
    private List<CoverageModel> getCoverageModelList(List<String> uuids,List<String> targetTypeList,List<String> targets,
            List<String> strategyList,List<String> targetTitleList){
        List<CoverageModel> list = new ArrayList<CoverageModel>();
        for(int i=0;i<uuids.size();i++){
            CoverageModel cm = new CoverageModel();
            cm.setResource(uuids.get(i));
            cm.setTargetTitle(targetTitleList.get(i));
            cm.setTargetType(targetTypeList.get(i));
            cm.setTarget(targets.get(i));
            cm.setStrategy(strategyList.get(i));
            
            list.add(cm);
        }
        
        return list;
    }
    
    private CoverageModelForUpdate getCoverageModelForUpdate(String targetType,String strategy){
        CoverageModelForUpdate cmUpdate = new CoverageModelForUpdate();
        cmUpdate.setTargetTitle("覆盖范围单元测试-update");
        cmUpdate.setTargetType(targetType);
        cmUpdate.setTarget("279904");
        cmUpdate.setStrategy(strategy);
        
        return cmUpdate;
    }
    
    private Map<String,CoverageModelForUpdate> getCoverageModelForUpdateMap(List<String> rcids,List<String> targetTypeList,List<String> targets,
            List<String> strategyList,List<String> targetTitleList){
        Map<String,CoverageModelForUpdate> map = new HashMap<String, CoverageModelForUpdate>();
        for(int i=0;i<rcids.size();i++){
            CoverageModelForUpdate cmUpdate = new CoverageModelForUpdate();
            cmUpdate.setTargetTitle(targetTitleList.get(i));
            cmUpdate.setTargetType(targetTypeList.get(i));
            cmUpdate.setTarget(targets.get(i));
            cmUpdate.setStrategy(strategyList.get(i));
            
            map.put(rcids.get(i), cmUpdate);
        }
        
        return map;
    }
    
    private void emptyLists(List<String> uuids,List<String> targetTypeList,List<String> targets,
            List<String> strategyList,List<String> targetTitleList){
        uuids.clear();
        targetTypeList.clear();
        targets.clear();
        strategyList.clear();
        targetTitleList.clear();
    }
}
