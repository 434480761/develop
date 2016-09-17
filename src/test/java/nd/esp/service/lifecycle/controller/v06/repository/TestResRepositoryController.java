package nd.esp.service.lifecycle.controller.v06.repository;

import java.util.Map;

import nd.esp.service.lifecycle.impl.SimpleJunitTest4ResourceImpl;
import nd.esp.service.lifecycle.vos.repository.v06.ResRepositoryViewModel;
import nd.esp.service.lifecycle.vos.repository.v06.ResRepositoryViewModelForUpdate;
import nd.esp.service.lifecycle.vos.statics.ResRepositoryConstant;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nd.gaea.rest.testconfig.MockUtil;

public class TestResRepositoryController extends SimpleJunitTest4ResourceImpl{
    private static final Logger logger = LoggerFactory.getLogger(TestResRepositoryController.class);
    
//    @Test
    public void testAll(){
        String target = "279904xie" + System.currentTimeMillis();
        
        //申请物理资源存储空间
        //1.正常成功
        ResRepositoryViewModel rvmSuccess = testCreateRepository(ResRepositoryConstant.TARGET_TYPE_ORG, target, ResRepositoryConstant.STATUS_RUNNING);
        Assert.assertNotNull("物理空间创建失败", rvmSuccess);
        Assert.assertEquals("物理空间创建失败", "单元测试", rvmSuccess.getRepositoryName());
        //2.创建失败,targetType不对
        ResRepositoryViewModel rvm = testCreateRepository("ABC", target, ResRepositoryConstant.STATUS_REMOVE);
        Assert.assertNull("targetype不对但创建成功", rvm.getIdentifier());
        //3.创建失败,status不对
        rvm = testCreateRepository(ResRepositoryConstant.TARGET_TYPE_ORG, target, "ABC");
        Assert.assertNull("status不对但创建成功", rvm.getIdentifier());
        //4.创建失败,已创建
        rvm = testCreateRepository(ResRepositoryConstant.TARGET_TYPE_ORG, target, ResRepositoryConstant.STATUS_RUNNING);
        Assert.assertNull("已创建但创建成功", rvm.getIdentifier());
        //5.先删除再创建,成功
        //5.1 删除
        Map<String, String> map4Delete = testDeleteRepository(rvmSuccess.getIdentifier());
        Assert.assertEquals("删除失败", 2, map4Delete.size());
        //5.2 创建
        rvmSuccess = testCreateRepository(ResRepositoryConstant.TARGET_TYPE_ORG, target, ResRepositoryConstant.STATUS_RUNNING);
        Assert.assertNotNull("物理空间创建失败", rvmSuccess);
        Assert.assertEquals("物理空间创建失败", "单元测试", rvmSuccess.getRepositoryName());
        
        //通过ID获取物理空间信息
        //1.正常获取
        ResRepositoryViewModel rvmDetail = testGetRepositoryById(rvmSuccess.getIdentifier());
        Assert.assertNotNull("物理空间获取失败", rvmDetail);
        Assert.assertEquals("物理空间获取失败", "单元测试", rvmDetail.getRepositoryName());
        //2.不存在的id
        rvmDetail = testGetRepositoryById("abcaskd11233");
        Assert.assertNull("不存在的id但物理空间获取成功", rvmDetail);
        //3.先删除再获取
        testDeleteRepository(rvmSuccess.getIdentifier());
        rvmDetail = testGetRepositoryById(rvmSuccess.getIdentifier());
        Assert.assertNull("已删除但物理空间获取成功", rvmDetail);
        
        //再创建
        rvmSuccess = testCreateRepository(ResRepositoryConstant.TARGET_TYPE_ORG, target, ResRepositoryConstant.STATUS_RUNNING);
        //获取物理空间信息
        //1.正常获取
        rvmDetail = testGetRepositoryByCondition(ResRepositoryConstant.TARGET_TYPE_ORG, target);
        Assert.assertNotNull("物理空间获取失败", rvmDetail);
        Assert.assertEquals("物理空间获取失败", "单元测试", rvmDetail.getRepositoryName());
        //2.获取失败,targetType不对
        rvmDetail = testGetRepositoryByCondition("ABC", target);
        Assert.assertNull("targetType不对但获取成功", rvmDetail.getIdentifier());
        //3.获取失败,target为空
        rvmDetail = testGetRepositoryByCondition(ResRepositoryConstant.TARGET_TYPE_ORG, "");
        Assert.assertNull("target为空但获取成功", rvmDetail.getIdentifier());
        //4.正常获取,但是不存在
        rvmDetail = testGetRepositoryByCondition(ResRepositoryConstant.TARGET_TYPE_ORG, "xiezeyong12597555555");
        Assert.assertNull("不存在但获取成功", rvmDetail);
        //5.先删除再获取
        testDeleteRepository(rvmSuccess.getIdentifier());
        rvmDetail = testGetRepositoryByCondition(ResRepositoryConstant.TARGET_TYPE_ORG, target);
        Assert.assertNull("已删除但获取成功", rvmDetail);
        
        //再创建
        rvmSuccess = testCreateRepository(ResRepositoryConstant.TARGET_TYPE_ORG, target, ResRepositoryConstant.STATUS_RUNNING);
        //修改物理空间
        //1.修改不成功,status不对
        ResRepositoryViewModel rvm4Update = testUpdateRepository(rvmSuccess.getIdentifier(), "ABC");
        Assert.assertNull("status不对但修改成功", rvm4Update.getIdentifier());
        //2.修改不成功,rid不存在
        rvm4Update = testUpdateRepository("123442243243211", ResRepositoryConstant.STATUS_APPLY);
        Assert.assertNull("rid不存在但修改成功", rvm4Update.getIdentifier());
        //3.修改成功
        rvm4Update = testUpdateRepository(rvmSuccess.getIdentifier(), ResRepositoryConstant.STATUS_APPLY);
        Assert.assertNotNull("物理空间修改失败", rvm4Update);
        Assert.assertEquals("物理空间修改失败", "单元测试Update", rvm4Update.getRepositoryName());
        
        //通过ID删除资源物理空间信息 
        //rid不存在
        map4Delete = testDeleteRepository("123442243243211");
        Assert.assertNotEquals("rid不存在但删除成功", 2, map4Delete.size());
        
        //通过目标类型和目标标识删除私有空间
        //1.删除不成功,targetType不对
        map4Delete = testDeleteRepositoryByCondition("ABC", target);
        Assert.assertNotEquals("targetType不对但删除成功", 2, map4Delete.size());
        //2.删除不成功,target为空
        map4Delete = testDeleteRepositoryByCondition(ResRepositoryConstant.TARGET_TYPE_ORG, "");
        Assert.assertNotEquals("target为空但删除成功", 2, map4Delete.size());
        //3.删除不成功,不存在
        map4Delete = testDeleteRepositoryByCondition(ResRepositoryConstant.TARGET_TYPE_ORG, "xiezeyong12597555555");
        Assert.assertNotEquals("不存在但删除成功", 2, map4Delete.size());
        //4.删除成功
        map4Delete = testDeleteRepositoryByCondition(ResRepositoryConstant.TARGET_TYPE_ORG, target);
        Assert.assertEquals("删除失败", 2, map4Delete.size());
    }
    
    private ResRepositoryViewModel testCreateRepository(String targetType,String target,String status){
        String uri = "/v0.6/resources/repository";
        String json = toJson(getResRepositoryViewModel(targetType, target, status));
        String resStr = "";
        try {
            resStr = MockUtil.mockPost(mockMvc, uri, json);
        } catch (Exception e) {
            logger.error("create Repository error",e);
        }
        ResRepositoryViewModel rvm = fromJson(resStr, ResRepositoryViewModel.class);
        
        return rvm;
    }
    
    private Map<String, String> testDeleteRepository(String rid){
        String uri = "/v0.6/resources/repository/" + rid;
        String resStr = "";
        try {
            resStr = MockUtil.mockDelete(mockMvc, uri, null);
        } catch (Exception e) {
            logger.error("delete Repository error",e);
        }
        Map<String, String> map4Delete = fromJson(resStr, Map.class);
        
        return map4Delete;
    }
    
    private Map<String, String> testDeleteRepositoryByCondition(String type,String target){
        String uri = "/v0.6/resources/repository?type=" + type +"&target=" + target;
        String resStr = "";
        try {
            resStr = MockUtil.mockDelete(mockMvc, uri, null);
        } catch (Exception e) {
            logger.error("delete Repository error",e);
        }
        Map<String, String> map4Delete = fromJson(resStr, Map.class);
        
        return map4Delete;
    }
    
    private ResRepositoryViewModel testGetRepositoryById(String rid){
        String uri = "/v0.6/resources/repository/" + rid;
        String resStr = "";
        try {
            resStr = MockUtil.mockGet(mockMvc, uri, null);
        } catch (Exception e) {
            logger.error("get Repository error",e);
        }
        ResRepositoryViewModel rvmDetail = fromJson(resStr, ResRepositoryViewModel.class);
        
        return rvmDetail;
    }
    
    private ResRepositoryViewModel testGetRepositoryByCondition(String targetType,String target){
        String uri = "/v0.6/resources/repository?type=" + targetType + "&target=" + target;
        String resStr = "";
        try {
            resStr = MockUtil.mockGet(mockMvc, uri, null);
        } catch (Exception e) {
            logger.error("get Repository error",e);
        }
        ResRepositoryViewModel rvmDetail = fromJson(resStr, ResRepositoryViewModel.class);
        
        return rvmDetail;
    }
    
    private ResRepositoryViewModel testUpdateRepository(String rid,String status){
        String uri = "/v0.6/resources/repository/" + rid;
        String json = toJson(getResRepositoryViewModelForUpdate(status));
        String resStr = "";
        try {
            resStr = MockUtil.mockPut(mockMvc, uri, json);
        } catch (Exception e) {
            logger.error("update Repository error",e);
        }
        ResRepositoryViewModel rvmUpdate = fromJson(resStr, ResRepositoryViewModel.class);
        
        return rvmUpdate;
    }
    
    private ResRepositoryViewModel getResRepositoryViewModel(String targetType,String target,String status){
        ResRepositoryViewModel rvm = new ResRepositoryViewModel();
        rvm.setRepositoryName("单元测试");
        rvm.setTargetType(targetType);
        rvm.setTarget(target);
        rvm.setRepositoryAdmin("279904");
        rvm.setEnable(true);
        rvm.setStatus(status);
        
        return rvm;
    }
    
    private ResRepositoryViewModelForUpdate getResRepositoryViewModelForUpdate(String status){
        ResRepositoryViewModelForUpdate rvm = new ResRepositoryViewModelForUpdate();
        rvm.setRepositoryName("单元测试Update");
        rvm.setRepositoryAdmin("279904");
        rvm.setEnable(true);
        rvm.setStatus(status);
        
        return rvm;
    }
}
