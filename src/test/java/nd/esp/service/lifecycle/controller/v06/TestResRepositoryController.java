package nd.esp.service.lifecycle.controller.v06;

import java.util.Map;

import nd.esp.service.lifecycle.BaseControllerConfig;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import com.nd.gaea.rest.testconfig.MockUtil;

public class TestResRepositoryController extends BaseControllerConfig{
    
    Logger logger = Logger.getLogger(this.getClass().getName());
    
    @Test
    @SuppressWarnings("unchecked")
    public void testController(){
        logger.info("公私有库-物理存储空间Controller层单元测试开始");
        String json = "";
        String uri = "";
        String resStr;
        Map<String,Object> m = null;
        
        try {
            //1.申请物理资源存储空间
            uri = "/v0.6/resources/repository";
            json = "{" + "\"repository_name\":\"福州市教学资源库\"," + "\"target_type\":\"Org\"," + "\"target\":\"xiezy\","
                    + "\"repository_admin\":\"279904\"," + "\"enable\":true," + "\"status\":\"APPLY\"" + "}";
            resStr = MockUtil.mockCreate(mockMvc, uri, json);
            m = ObjectUtils.fromJson(resStr, Map.class);
            Assert.assertNotNull("申请物理资源存储空间失败", m);
            Assert.assertNotNull("申请物理资源存储空间失败", m.get("identifier"));
            Assert.assertEquals("申请物理资源存储空间失败", "Org", m.get("target_type"));
            logger.info("申请物理资源存储空间验证通过");
            
            String id = (String)m.get("identifier");
            
            //2.修改物理空间信息
            m.put("repository_admin", "36327");
            uri = "/v0.6/resources/repository/" + id;
            resStr = MockUtil.mockPut(mockMvc, uri, ObjectUtils.toJson(m));
            m = ObjectUtils.fromJson(resStr, Map.class);
            Assert.assertNotNull("修改资源物理空间信息失败", m);
            Assert.assertNotNull("修改资源物理空间信息失败", m.get("identifier"));
            Assert.assertEquals("修改资源物理空间信息失败", "36327", m.get("repository_admin"));
            logger.info("修改资源物理空间信息验证通过");
            
            //3.通过ID获取物理空间信息
            uri = "/v0.6/resources/repository/" + id;
            resStr = MockUtil.mockGet(mockMvc, uri, null);
            m = ObjectUtils.fromJson(resStr, Map.class);
            Assert.assertNotNull("通过ID获取物理空间信息失败", m);
            Assert.assertEquals("通过ID获取物理空间信息失败", id, m.get("identifier"));
            logger.info("通过ID获取物理空间信息验证通过");
            
            //4.获取物理空间信息
            uri = "/v0.6/resources/repository?type=Org&target=xiezy";
            resStr = MockUtil.mockGet(mockMvc, uri, null);
            m = ObjectUtils.fromJson(resStr, Map.class);
            Assert.assertNotNull("获取物理空间信息失败", m);
            Assert.assertEquals("获取物理空间信息失败", id, m.get("identifier"));
            logger.info("获取物理空间信息验证通过");
            
            //5.通过ID删除资源物理空间信息
            uri = "/v0.6/resources/repository/" + id;
            resStr = MockUtil.mockDelete(mockMvc, uri, null);
            m = ObjectUtils.fromJson(resStr, Map.class);
            Assert.assertNotNull("通过ID删除资源物理空间信息失败", m);
            Assert.assertNotNull("通过ID删除资源物理空间信息失败", m.get("process_code"));
            Assert.assertEquals("通过ID删除资源物理空间信息失败", "LC/DELETE_REPOSITORY_SUCCESS",(String)m.get("process_code"));
            logger.info("通过ID删除资源物理空间信息验证通过");
            
            //6.删除资源物理空间信息
            uri = "/v0.6/resources/repository?type=Org&target=xiezy";
            resStr = MockUtil.mockGet(mockMvc, uri, null);
            m = ObjectUtils.fromJson(resStr, Map.class);
            Assert.assertNull("通过ID删除资源物理空间信息失败", m);
            logger.info("删除资源物理空间信息验证通过");
        } catch (Exception e) {
            logger.error("公私有库-物理存储空间Controller层单元测试出错！");
        }
        logger.info("公私有库-物理存储空间Controller层单元测试结束");
    }
}
