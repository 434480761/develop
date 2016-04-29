/* =============================================================
 * Created: [2015年6月23日] by linsm
 * =============================================================
 *
 * Copyright 2014-2015 NetDragon Websoft Inc. All Rights Reserved
 *
 * =============================================================
 */

package nd.esp.service.lifecycle.controller.v06;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import nd.esp.service.lifecycle.app.LifeCircleWebConfig;
import nd.esp.service.lifecycle.impl.SimpleJunitTest4ResourceImpl;
import nd.esp.service.lifecycle.models.AccessModel;
import nd.esp.service.lifecycle.support.Constant;
import nd.esp.service.lifecycle.utils.StringUtils;

import org.apache.commons.io.Charsets;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestTemplate;



import com.fasterxml.jackson.annotation.JsonProperty;
import com.nd.gaea.rest.o2o.JacksonCustomObjectMapper;
import com.nd.gaea.rest.testconfig.AbstractSpringJunit4Config;
import com.nd.gaea.rest.testconfig.MockUtil;

/**
 * 用于测试通用的上传、下载接口
 * 
 * @author linsm
 * @since
 */
public class TestUploadAndDownload extends SimpleJunitTest4ResourceImpl {
    protected final Logger logger = Logger.getLogger(this.getClass().getName());
    private JacksonCustomObjectMapper objectMapper = new JacksonCustomObjectMapper();

    private final String ACCESS_KEY = "accessKey";
    private final String UPLOAD_ACCESS_METHOD = "POST";
    private final String DOWNLOAD_ACCESS_METHOD = "GET";
    private final String VERSION = "v0.6";


    @Before
    public void myBefore() {

    }

    /**
     * 测试上传功能：none, existeduuid, renew
     * 
     * @since
     */
//    @Test
    public void testUpload() {
        String resourceType = "";

        resourceType = "assets";
        logger.info(resourceType + "上传测试开始");
        testNoneUpload(resourceType);
        testExistedUuidUpload(resourceType);
        testRenewUpload(resourceType);
        logger.info(resourceType + "上传测试结束");

    }

    /**
     * 测试下载功能，存在两种情况：有无key,(access_url 若无， 则是href ,若有，则从store_info中取），暂不验证这个，key 不赋值
     */
//    @Test
    public void testDownload() {
        String resourceType = "";

        resourceType = "assets";
        logger.info(resourceType + "测试下载开始");
        testWithoutKeyDownload(resourceType);
        logger.info(resourceType + "测试下载结束");

    }

    @After
    public void myAfter() {

    }

    /**
     * 续约，不管是否存在元数据，类似于none(暂时不做区分，只不过uuid由调用 方生成)
     * 
     * @param resourceType
     * @since
     */
    private void testRenewUpload(String resourceType) {
        String uuid = UUID.randomUUID().toString();

        String uri = "/"+VERSION+"/" + resourceType + "/" + uuid + "/uploadurl?uid=" + getUserId() + "&renew=true";
        AccessModel model = null;
        try {
            String result = MockUtil.mockGet(mockMvc, uri, "");
            model = objectMapper.readValue(result, AccessModel.class);

        } catch (Exception e) {
            logger.error(e);
        }
        AssertUpload(resourceType, model);
        Assert.assertEquals("uuid", uuid, model.getUuid().toString());
        logger.info(resourceType + " renew测试上传成功");

    }

    /**
     * 已存在元数据资源，获取 session
     * 
     * @param resourceType
     * @since
     */
    private void testExistedUuidUpload(String resourceType) {
        String uuid = getUuidBySearch(resourceType);
        if (StringUtils.isEmpty(uuid)) {
            logger.info("不存在资源：" + resourceType);
            return;
        }

        String uri = "/"+VERSION+"/" + resourceType + "/" + uuid + "/uploadurl?uid=" + getUserId();
        AccessModel model = null;
        try {
            String result = MockUtil.mockGet(mockMvc, uri, "");
            model = objectMapper.readValue(result, AccessModel.class);

        } catch (Exception e) {
            logger.error(e);
        }
        AssertUpload(resourceType, model);
        Assert.assertEquals("uuid", uuid, model.getUuid().toString());
        logger.info(resourceType + " 根据现存资源测试上传成功");

    }

    /**
     * 通过查询接口，获取一个已存在的资源
     * 
     * @param resourceType
     * @return
     * @since
     */
    private String getUuidBySearch(String resourceType) {
        String uri = "/"+VERSION+"/" + resourceType +"/actions/query?words=&limit=(0,1)";
        try {
            String result = MockUtil.mockGet(mockMvc, uri, "");
            MyListViewModel listViewModel = objectMapper.readValue(result, MyListViewModel.class);
            if (listViewModel.getItems().size() > 0) {
                return listViewModel.getItems().get(0).getIdentifier();

            }

        } catch (Exception e) {
            logger.error(e);
        }
        return "";
    }

    /**
     * 不存在资源元数据，随机生成uuid，获取session
     * 
     * @param resourceType
     * @since
     */
    private void testNoneUpload(String resourceType) {
        String uri = "/"+VERSION+"/" + resourceType + "/none/uploadurl?uid=" + getUserId()+"&coverage=Org/nd";
        AccessModel model = null;
        try {
            String result = MockUtil.mockGet(mockMvc, uri, "");
            model = objectMapper.readValue(result, AccessModel.class);
        } catch (Exception e) {
            logger.error(e);
        }
        AssertUpload(resourceType, model);
        AssertSession(model.getDistPath(), model.getSessionId());
        logger.info(resourceType + " none测试上传成功");
    }

    /**
     * 用于验证session(通过访问cs的接口）
     * 
     * @param distPath
     * @param sessionId
     * @since
     */
    private void AssertSession(String distPath, String sessionId) {
        String uri = Constant.CS_API_URL + "/dentries?session=" + sessionId;
        List<String> paths = new ArrayList<String>();
        paths.add(distPath.substring(0, distPath.lastIndexOf('/'))); // session的范围应文鑫的要求，扩大了,也可能会存在问题，如还未创建 /ebooks等。
        BatchGetDentryByPaths param = new BatchGetDentryByPaths();
        param.setPaths(paths);
        HttpEntity<BatchGetDentryByPaths> httpEntity = new HttpEntity<BatchGetDentryByPaths>(param);
        // waf不支持patch
        // WafSecurityHttpClient wafSecurityHttpClient = new WafSecurityHttpClient();
        // wafSecurityHttpClient.p
        RestTemplate template = new RestTemplate();

        template.setRequestFactory(new HttpComponentsClientHttpRequestFactory());

        // 只是为了调试，设置了拦截器
        ClientHttpRequestInterceptor inter = new ClientHttpRequestInterceptor() {

            @Override
            public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
                Map<String, String> requestheders = request.getHeaders().toSingleValueMap();

                ClientHttpResponse response = execution.execute(request, body);

                return response;
            }
        };

        template.setInterceptors(Arrays.asList(inter));
        ResponseEntity<String> result = null;
        try {

            result = template.exchange(uri, HttpMethod.PATCH, httpEntity, String.class);
            logger.info(result.getBody());
        } catch (Exception e) {
            logger.error(e);
        }

        Assert.assertNotNull("distPath: " + distPath + "  sessionId: " + sessionId + " 取cs目录出错", result);

    }

    /**
     * 验证上传结果（并没有使用session) dis_path, uuid(与dis_path对应）, access_key, access_method
     * 
     * @param resourceType
     * @param model
     * @since
     */
    private void AssertUpload(String resourceType, AccessModel model) {
        Assert.assertNotNull("resourceType: " + resourceType, model);
        Assert.assertTrue("dis_path",
                          model.getDistPath().contains(resourceType + "/" + model.getUuid().toString() + ".pkg"));
        Assert.assertEquals("access_key", model.getAccessKey(), ACCESS_KEY);
        Assert.assertEquals("access_method", model.getAccessMethod(), UPLOAD_ACCESS_METHOD);
    }

    /**
     * @param resourceType
     * @since
     */
    private void testWithoutKeyDownload(String resourceType) {
        String uuid = getUuidBySearch(resourceType);
        if (StringUtils.isEmpty(uuid)) {
            logger.info("不存在资源：" + resourceType);
            return;
        }

        String uri = "/"+VERSION+"/" + resourceType + "/" + uuid + "/downloadurl?uid=" + getUserId();
        AccessModel model = null;
        try {
            String result = MockUtil.mockGet(mockMvc, uri, "");
            model = objectMapper.readValue(result, AccessModel.class);
        } catch (Exception e) {
            logger.error(e);
        }
        AssertDownload(resourceType, model);
        Assert.assertEquals("uuid", uuid, model.getUuid().toString());
        logger.info(resourceType + " 不传key测试下载成功");

    }

    /**
     * 验证下载结果：access_key, access_method
     * 
     * @param resourceType
     * @param model
     * @since
     */
    private void AssertDownload(String resourceType, AccessModel model) {
        Assert.assertNotNull("resourceType: " + resourceType, model);
        Assert.assertEquals("access_key", model.getAccessKey(), ACCESS_KEY);
        Assert.assertEquals("access_method", model.getAccessMethod(), DOWNLOAD_ACCESS_METHOD);

    }


    /**
     * 用于获取目录信息（参数）
     * 
     * @author linsm
     * @since
     */
    private static class BatchGetDentryByPaths {

        @JsonProperty("dentry_ids")
        List<String> dentryIds;

        List<String> paths;

        public List<String> getPaths() {
            return paths;
        }

        public void setPaths(List<String> paths) {
            this.paths = paths;
        }

        public List<String> getDentryIds() {
            return dentryIds;
        }

        public void setDentryIds(List<String> dentryIds) {
            this.dentryIds = dentryIds;
        }

    }

    /**
     * 用于接收查询结果
     * 
     * @author linsm
     * @since
     */
    private static class MyListViewModel {
        Long total;
        List<MyResource> items;

        public Long getTotal() {
            return total;
        }

        public void setTotal(Long total) {
            this.total = total;
        }

        public List<MyResource> getItems() {
            return items;
        }

        public void setItems(List<MyResource> items) {
            this.items = items;
        }
    }

    /**
     * 兼容所有资源
     * 
     * @author linsm
     * @since
     */
    private static class MyResource {
        String identifier;

        public String getIdentifier() {
            return identifier;
        }

        public void setIdentifier(String identifier) {
            this.identifier = identifier;
        }
    }

}
