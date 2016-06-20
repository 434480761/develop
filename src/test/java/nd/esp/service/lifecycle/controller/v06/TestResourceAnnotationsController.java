/**
 * 
 */
package nd.esp.service.lifecycle.controller.v06;

import java.util.Map;
import java.util.UUID;

import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;
import nd.esp.service.lifecycle.impl.SimpleJunitTest4ResourceImpl;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;
import nd.esp.service.lifecycle.vos.resourceannotations.v06.ResourceAnnotationViewModel;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nd.gaea.rest.o2o.JacksonCustomObjectMapper;
import com.nd.gaea.rest.testconfig.MockUtil;

/**
 * @author Administrator
 */
public class TestResourceAnnotationsController extends SimpleJunitTest4ResourceImpl {
    private final static Logger LOG = LoggerFactory.getLogger(TestResourceAnnotationsController.class);

    private JacksonCustomObjectMapper objectMapper = new JacksonCustomObjectMapper();
    
    private String sourceUuid = null;

    @Override
    public void before() {

    }
    
    @Override
    public void after() {
        super.after();
        // 删除教案
        testDelete("lessonplans", sourceUuid);
    }
    
    @Test
    @Override
    public void doTest() {
        Map<String, Object> result = createLessonPlan();
        Assert.assertNotNull("资源关系源资源教案创建失败", result);
        sourceUuid = (String) result.get("identifier");
        Assert.assertNotNull("资源关系源资源教案创建失败", sourceUuid);
        ResourceAnnotationViewModel inViewModel = getResourceAnnotation("DEFAULT_create");
        String uri = "/v0.6/lessonplans/" + sourceUuid + "/annotations";
        try {
            // 正常创建资源评注
            String json = objectMapper.writeValueAsString(inViewModel);
            String resStr = MockUtil.mockCreate(mockMvc, uri, json);
            result = ObjectUtils.fromJson(resStr, Map.class);
            Assert.assertNotNull("创建教案的资源评注失败", result);
            String annoUuid = (String) result.get("identifier");
            Assert.assertNotNull("创建教案的资源评注失败", annoUuid);

            // 正常修改资源评注
            uri = "/v0.6/lessonplans/" + sourceUuid + "/annotations/" + annoUuid;
            inViewModel = getResourceAnnotation("DEFAULT_update");
            json = objectMapper.writeValueAsString(inViewModel);
            resStr = MockUtil.mockPut(mockMvc, uri, json);
            result = ObjectUtils.fromJson(resStr, Map.class);
            Assert.assertNotNull("修改教案的资源评注失败", result);
            annoUuid = (String) result.get("identifier");
            Assert.assertNotNull("修改教案的资源评注失败", annoUuid);
            Assert.assertEquals("修改教案的资源评注失败", "DEFAULT_update", result.get("annotation_type"));

            // 正常修改资源评注
            uri = "/v0.6/lessonplans/" + sourceUuid + "/annotations/" + annoUuid;
            inViewModel.setScore(null);
            inViewModel.setScoreLevel(null);
            inViewModel.setAnnotationFrom(null);
            json = objectMapper.writeValueAsString(inViewModel);
            resStr = MockUtil.mockPut(mockMvc, uri, json);
            result = ObjectUtils.fromJson(resStr, Map.class);
            Assert.assertNotNull("修改教案的资源评注失败", result);
            annoUuid = (String) result.get("identifier");
            Assert.assertNotNull("修改教案的资源评注失败", annoUuid);
            Assert.assertEquals("修改教案的资源评注失败", "DEFAULT_update", result.get("annotation_type"));

            // 查询资源评注
            uri = "/v0.6/lessonplans/" + sourceUuid + "/annotations?limit=(0,20)";
            resStr = MockUtil.mockGet(mockMvc, uri, null);
            result = ObjectUtils.fromJson(resStr, Map.class);
            Assert.assertNotNull("查询教案的资源评注失败", result);
            Assert.assertNotNull("查询教案的资源评注失败", result.get("items"));

            // 查询资源评注(大于201条)
            uri = "/v0.6/lessonplans/" + sourceUuid + "/annotations?limit=(0,201)";
            resStr = MockUtil.mockGet(mockMvc, uri, null);
            result = ObjectUtils.fromJson(resStr, Map.class);
            Assert.assertNotNull("查询教案的资源评注失败", result);
            Assert.assertNotNull("查询教案的资源评注失败", result.get("items"));

            // 删除资源评注（通过评注id）
            uri = "/v0.6/lessonplans/" + sourceUuid + "/annotations/" + annoUuid;
            resStr = MockUtil.mockDelete(mockMvc, uri, null);
            result = ObjectUtils.fromJson(resStr, Map.class);
            Assert.assertNotNull("资源评注删除失败", result);
            Assert.assertNotNull("资源评注删除失败", result.get("process_code"));
            Assert.assertEquals("资源评注删除失败",
                                LifeCircleErrorMessageMapper.DeleteResourceAnnotationSuccess.getCode(),
                                (String) result.get("process_code"));
            // 测试资源评注不存在会不会抛异常
            resStr = MockUtil.mockDelete(mockMvc, uri, null);
            result = ObjectUtils.fromJson(resStr, Map.class);
            Assert.assertNotNull("异常信息返回为空", result);
            Assert.assertEquals("资源评注未找到", result.get("message"));

            // 删除资源评注（资源id）（没有资源评注）
            uri = "/v0.6/lessonplans/" + sourceUuid + "/annotations";
            resStr = MockUtil.mockDelete(mockMvc, uri, null);
            result = ObjectUtils.fromJson(resStr, Map.class);
            Assert.assertNotNull("资源评注删除失败", result);
            Assert.assertNotNull("资源评注删除失败", result.get("process_code"));
            Assert.assertEquals("资源评注删除失败",
                                LifeCircleErrorMessageMapper.DeleteResourceAnnotationSuccess.getCode(),
                                (String) result.get("process_code"));
            // 正常创建资源评注
            json = objectMapper.writeValueAsString(inViewModel);
            resStr = MockUtil.mockCreate(mockMvc, uri, json);
            result = ObjectUtils.fromJson(resStr, Map.class);
            Assert.assertNotNull("创建教案的资源评注失败", result);
            annoUuid = (String) result.get("identifier");
            Assert.assertNotNull("创建教案的资源评注失败", annoUuid);
            String entityIdentifier = (String) result.get("entity_identifier");
            Assert.assertNotNull("创建教案的资源评注失败", entityIdentifier);

            // 正常删除资源评注（资源id）
            resStr = MockUtil.mockDelete(mockMvc, uri, null);
            result = ObjectUtils.fromJson(resStr, Map.class);
            Assert.assertNotNull("资源评注删除失败", result);
            Assert.assertNotNull("资源评注删除失败", result.get("process_code"));
            Assert.assertEquals("资源评注删除失败",
                                LifeCircleErrorMessageMapper.DeleteResourceAnnotationSuccess.getCode(),
                                (String) result.get("process_code"));

            // 删除资源评注（用户id）（没有资源评注）
            uri = "/v0.6/lessonplans/" + sourceUuid + "/annotations/entity/" + entityIdentifier;
            resStr = MockUtil.mockDelete(mockMvc, uri, null);
            result = ObjectUtils.fromJson(resStr, Map.class);
            Assert.assertNotNull("资源评注删除失败", result);
            Assert.assertNotNull("资源评注删除失败", result.get("process_code"));
            Assert.assertEquals("资源评注删除失败",
                                LifeCircleErrorMessageMapper.DeleteResourceAnnotationSuccess.getCode(),
                                (String) result.get("process_code"));
            // 正常创建资源评注
            uri = "/v0.6/lessonplans/" + sourceUuid + "/annotations";
            json = objectMapper.writeValueAsString(inViewModel);
            resStr = MockUtil.mockCreate(mockMvc, uri, json);
            result = ObjectUtils.fromJson(resStr, Map.class);
            Assert.assertNotNull("创建教案的资源评注失败", result);
            entityIdentifier = (String) result.get("entity_identifier");
            Assert.assertNotNull("创建教案的资源评注失败", entityIdentifier);

            // 删除资源评注（用户id）
            uri = "/v0.6/lessonplans/" + sourceUuid + "/annotations/entity/" + entityIdentifier;
            resStr = MockUtil.mockDelete(mockMvc, uri, null);
            result = ObjectUtils.fromJson(resStr, Map.class);
            Assert.assertNotNull("资源评注删除失败", result);
            Assert.assertNotNull("资源评注删除失败", result.get("process_code"));
            Assert.assertEquals("资源评注删除失败",
                                LifeCircleErrorMessageMapper.DeleteResourceAnnotationSuccess.getCode(),
                                (String) result.get("process_code"));

        } catch (Exception e) {
            LOG.error("资源关系单元测试出错！");
        }
    }

    /**
     * 创建源资源教案
     * 
     * @return
     * @since
     */
    public Map<String, Object> createLessonPlan() {
        String uuid = UUID.randomUUID().toString();

        ResourceViewModel resourceViewModel = testCreate("lessonplans", uuid, null);
        if (resourceViewModel == null || !uuid.equals(resourceViewModel.getIdentifier())) {
            return null;
        }
        String resStr = null;
        try {
            resStr = objectMapper.writeValueAsString(resourceViewModel);
        } catch (JsonProcessingException e) {
            LOG.error("测试资源关系-教案创建出错！");
        }
        Map<String, Object> result = ObjectUtils.fromJson(resStr, Map.class);

        return result;
    }

    /**
     * 获取资源评注
     * 
     * @param annotationType
     * @return
     * @since
     */
    public ResourceAnnotationViewModel getResourceAnnotation(String annotationType) {
        ResourceAnnotationViewModel viewModel = new ResourceAnnotationViewModel();
        viewModel.setEntityIdentifier("caocr_633592");
        viewModel.setContent("lcms-special-content-dev-test");
        viewModel.setAnnotationType(annotationType);
        viewModel.setScore(8.88);
        viewModel.setScoreLevel(666);
        viewModel.setAnnotationFrom("lcms-special-annotation-dev-test");

        return viewModel;
    }

}
