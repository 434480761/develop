/**
 * 
 */
package nd.esp.service.lifecycle.controller.v06;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import nd.esp.service.lifecycle.educommon.vos.ResClassificationViewModel;
import nd.esp.service.lifecycle.educommon.vos.ResLifeCycleViewModel;
import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;
import nd.esp.service.lifecycle.impl.SimpleJunitTest4ResourceImpl;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;
import nd.esp.service.lifecycle.vos.chapters.v06.ChapterViewModel;
import nd.esp.service.lifecycle.vos.educationrelation.v06.EducationRelationViewModel;
import nd.esp.service.lifecycle.vos.knowledges.v06.ChapterKnowledgeViewModel;
import nd.esp.service.lifecycle.vos.knowledges.v06.KnowledgeExtPropertiesViewModel;
import nd.esp.service.lifecycle.vos.knowledges.v06.KnowledgeRelationsViewModel4Add;
import nd.esp.service.lifecycle.vos.knowledges.v06.KnowledgeViewModel4In;
import nd.esp.service.lifecycle.vos.knowledges.v06.KnowledgeViewModel4Move;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.reflect.TypeToken;
import com.nd.gaea.rest.o2o.JacksonCustomObjectMapper;
import com.nd.gaea.rest.testconfig.MockUtil;

/**
 * @author Administrator
 *
 */
public class TestKnowledgeController extends SimpleJunitTest4ResourceImpl {
    private final static Logger LOG = LoggerFactory.getLogger(TestResourceAnnotationsController.class);

    private JacksonCustomObjectMapper objectMapper = new JacksonCustomObjectMapper();
    private String knowledgeSource = null;
    private String knowledgeTarget1 = null;
    private String knowledgeTarget2 = null;
    private String knowledgeTarget3 = null;
    private String mid = null;
    private String cid = null;

    @Override
    public void before() {

    }

	@Test
    @Override
    public void doTest() {
        /******************************************************************/
        /****************************创建知识点测试 开始************************/
        /******************************************************************/
        
        //正常创建知识点（不带知识点关联数据）
        Map<String, Object> result = createKnowledge("ROOT", null, null, null, null, true);
        Assert.assertNotNull("创建知识点失败", result);
        knowledgeSource = (String)result.get("identifier");
        Assert.assertNotNull("创建知识点失败", knowledgeSource);
        
        // 正常创建知识点（不带知识点关联数据）
        result = createKnowledge(knowledgeSource, null, null, null, null, true);
        Assert.assertNotNull("创建知识点失败", result);
        knowledgeTarget2 = (String) result.get("identifier");
        Assert.assertNotNull("创建知识点失败", knowledgeTarget2);
        
        // 正常创建知识点（不带知识点关联数据）
        result = createKnowledge(knowledgeSource, knowledgeTarget2, "pre", null, null, true);
        Assert.assertNotNull("创建知识点失败", result);
        knowledgeTarget1 = (String) result.get("identifier");
        Assert.assertNotNull("创建知识点失败", knowledgeTarget1);
        
        // 正常创建知识点（带知识点关联数据）
        result = createKnowledge(knowledgeTarget2, null, "pre", knowledgeSource, knowledgeTarget1, true);
        Assert.assertNotNull("创建知识点失败", result);
        knowledgeTarget3 = (String) result.get("identifier");
        Assert.assertNotNull("创建知识点失败", knowledgeTarget3);
        
        //创建知识点（知识点关联源知识点不存在）
        result = createKnowledge(null, knowledgeTarget1, "pre", "abc", knowledgeTarget1, true);
        Assert.assertNotNull("创建知识点失败", result);
        Assert.assertNotNull("创建知识点失败", result.get("code"));
        Assert.assertEquals("创建知识点失败",
                            LifeCircleErrorMessageMapper.KnowledgeNotFound.getCode(),
                            (String) result.get("code"));
        
        // 创建知识点（知识点关联目标知识点不存在）
        result = createKnowledge(null, knowledgeTarget1, "pre", knowledgeSource, "abc", true);
        Assert.assertNotNull("创建知识点失败", result);
        Assert.assertNotNull("创建知识点失败", result.get("code"));
        Assert.assertEquals("创建知识点失败",
                            LifeCircleErrorMessageMapper.KnowledgeNotFound.getCode(),
                            (String) result.get("code"));
        
        //direction设置为非pre和next
        result = createKnowledge("ROOT", knowledgeTarget1, "wrong_direction", null, null, true);
        Assert.assertNotNull("创建知识点失败", result);
        Assert.assertNotNull("创建知识点失败", result.get("code"));
        Assert.assertEquals("创建知识点失败",
                            LifeCircleErrorMessageMapper.DirectionParamError.getCode(),
                            (String) result.get("code"));
        //不带categories创建知识点
        result = createKnowledge("ROOT", null, null, null, null, false);
        Assert.assertNotNull("创建知识点失败", result);
        Assert.assertNotNull("创建知识点失败", result.get("code"));
        Assert.assertEquals("创建知识点失败",
                            LifeCircleErrorMessageMapper.CheckParamValidFail.getCode(),
                            (String) result.get("code"));
        
        //parent和target同时为空创建知识点
        result = createKnowledge(null, null, "pre", null, null, true);
        Assert.assertNotNull("创建知识点失败", result);
        Assert.assertNotNull("创建知识点失败", result.get("code"));
        Assert.assertEquals("创建知识点失败",
                            LifeCircleErrorMessageMapper.KnowledgeCheckParamFail.getCode(),
                            (String) result.get("code"));
        
        //删除知识点（不存在的知识点）
        String wrong_uuid = UUID.randomUUID().toString();
        String resultStr = testDelete("knowledges", wrong_uuid);
        result = ObjectUtils.fromJson(resultStr, Map.class);
        Assert.assertNotNull("删除知识点失败", result);
        Assert.assertNotNull("删除知识点失败", result.get("code"));
        Assert.assertEquals("删除知识点失败",
                            LifeCircleErrorMessageMapper.KnowledgeNotFound.getCode(),
                            (String) result.get("code"));
        
        // 删除知识点（存在子知识点的知识点）
        resultStr = testDelete("knowledges", knowledgeSource);
        result = ObjectUtils.fromJson(resultStr, Map.class);
        Assert.assertNotNull("删除知识点失败", result);
        Assert.assertNotNull("删除知识点失败", result.get("code"));
        Assert.assertEquals("删除知识点失败",
                            LifeCircleErrorMessageMapper.KnowledgeHaveChildrens.getCode(),
                            (String) result.get("code"));
        
        /******************************************************************/
        /****************************创建知识点测试结束************************/
        /*****************************************************************/
        /****************************修改知识点测试开始***********************/
        /****************************************************************/
        
        // 正常修改知识点
        result = updateKnowledge(knowledgeSource, null, null, null, null, null, true);
        Assert.assertNotNull("修改知识点失败", result);
        Assert.assertNotNull("创建知识点失败", (String) result.get("identifier"));
        
        /******************************************************************/
        /****************************修改知识点测试结束************************/
        /******************************************************************/
        /****************************移动知识点测试开始************************/
        /******************************************************************/
        
        //正常移动知识点
        result = moveKnowledge(knowledgeTarget3, knowledgeSource, knowledgeTarget1, "next");
        Assert.assertNull("移动知识点失败", result);
        
        // direction为空移动知识点
        result = moveKnowledge(knowledgeTarget3, knowledgeSource, knowledgeTarget2, null);
        Assert.assertNull("移动知识点失败", result);
        
        // direction为非pre或next移动知识点
        result = moveKnowledge(knowledgeTarget3, knowledgeSource, knowledgeTarget2, "wrong_direction");
        Assert.assertNotNull("移动知识点失败", result);
        Assert.assertNotNull("移动知识点失败", result.get("code"));
        Assert.assertEquals("移动知识点失败",
                            LifeCircleErrorMessageMapper.DirectionParamError.getCode(),
                            (String) result.get("code"));
        
        //移动的知识点不存在
        result = moveKnowledge("wrong_uuid", knowledgeSource, knowledgeTarget1, "next");
        Assert.assertNotNull("移动知识点失败", result);
        Assert.assertNotNull("移动知识点失败", result.get("code"));
        Assert.assertEquals("移动知识点失败",
                            LifeCircleErrorMessageMapper.KnowledgeNotFound.getCode(),
                            (String) result.get("code"));
        
        // 目标知识点不存在
        result = moveKnowledge(knowledgeTarget3, knowledgeSource, "wrong_uuid", "next");
        Assert.assertNotNull("移动知识点失败", result);
        Assert.assertNotNull("移动知识点失败", result.get("code"));
        Assert.assertEquals("移动知识点失败",
                            LifeCircleErrorMessageMapper.KnowledgeNotFound.getCode(),
                            (String) result.get("code"));
        
        // parent和target同时为空（移动知识点的时候不可能，因为parent必须传值）
//        result = moveKnowledge(knowledgeTarget3, null, null, "next");
//        Assert.assertNotNull("移动知识点失败", result);
//        Assert.assertNotNull("移动知识点失败", result.get("code"));
//        Assert.assertEquals("移动知识点失败", "target and parent can't be both null", (String) result.get("message"));
        
        // 正常移动知识点
        result = moveKnowledge(knowledgeTarget3, knowledgeTarget1, null, "next");
        Assert.assertNull("移动知识点失败", result);
        
        // 移动位置不合法(target不为空)
        result = moveKnowledge(knowledgeTarget1, knowledgeTarget3, knowledgeTarget3, "next");
        Assert.assertNotNull("移动知识点失败", result);
        Assert.assertNotNull("移动知识点失败", result.get("code"));
        Assert.assertEquals("移动知识点失败",
                            LifeCircleErrorMessageMapper.CheckParamValidFail.getCode(),
                            (String) result.get("code"));

        // 移动位置不合法(parent不为空，target为空)
        result = moveKnowledge(knowledgeTarget1, knowledgeTarget3, null, "next");
        Assert.assertNotNull("移动知识点失败", result);
        Assert.assertNotNull("移动知识点失败", result.get("code"));
        Assert.assertEquals("移动知识点失败",
                            LifeCircleErrorMessageMapper.CheckParamValidFail.getCode(),
                            (String) result.get("code"));
        
        /******************************************************************/
        /****************************移动知识点测试结束************************/
        /******************************************************************/
        /****************************知识点关联测试开始************************/
        /******************************************************************/
        
        // 正常添加知识点关联
        result = addKnowledgeRelation(knowledgeTarget2, knowledgeTarget3);
        Assert.assertNotNull("添加知识点关联失败", result);
        String krId = (String) result.get("identifier");
        Assert.assertNotNull("添加知识点关联失败", krId);

        // 添加知识点关联（源知识点不存在）
        result = addKnowledgeRelation("abc", knowledgeTarget2);
        Assert.assertNotNull("添加知识点关联失败", result);
        Assert.assertNotNull("添加知识点关联失败", result.get("code"));
        Assert.assertEquals("添加知识点关联失败",
                            LifeCircleErrorMessageMapper.KnowledgeNotFound.getCode(),
                            (String) result.get("code"));

        // 添加知识点关联（目标知识点不存在）
        result = addKnowledgeRelation(knowledgeTarget1, "abc");
        Assert.assertNotNull("添加知识点关联失败", result);
        Assert.assertNotNull("添加知识点关联失败", result.get("code"));
        Assert.assertEquals("添加知识点关联失败",
                            LifeCircleErrorMessageMapper.KnowledgeNotFound.getCode(),
                            (String) result.get("code"));
        
        //正常查看知识点关联
        List<Map<String, Object>> result1 = getKnowledgeRelations("lcms-special-context_type-dev-test",
                                       "1",
                                       "lcms-special-context_object-dev-test",
                                       knowledgeTarget2);
        Assert.assertNotNull("查看知识点关联失败", result1);
        Assert.assertNotNull("查看知识点关联失败", result1.get(0));
        Assert.assertNotNull("查看知识点关联失败", result1.get(0).get("context_type"));
        
        // 查看知识点关联(knowledge为空)
        result = getKnowledgeRelationsException("lcms-special-context_type-dev-test",
                                       "1",
                                       "lcms-special-context_object-dev-test",
                                       null);
        Assert.assertNotNull("查看知识点关联失败", result);
        Assert.assertNotNull("查看知识点关联失败", result.get("code"));
        Assert.assertEquals("查看知识点关联失败",
                            LifeCircleErrorMessageMapper.CheckGetKnowledgeRelationsKnowledgeParamFail.getCode(),
                            (String) result.get("code"));
        
        // 查看知识点关联(contexttype为空)
        result = getKnowledgeRelationsException(null, "1", "lcms-special-context_object-dev-test", knowledgeTarget1);
        Assert.assertNotNull("查看知识点关联失败", result);
        Assert.assertNotNull("查看知识点关联失败", result.get("code"));
        Assert.assertEquals("查看知识点关联失败",
                            LifeCircleErrorMessageMapper.CheckvGetKnowledgeRelationsContexttypeParamFail.getCode(),
                            (String) result.get("code"));
        
        // 查看知识点关联(contextobjectid为空)
        result = getKnowledgeRelationsException("lcms-special-context_type-dev-test", "1", null, knowledgeTarget1);
        Assert.assertNotNull("查看知识点关联失败", result);
        Assert.assertNotNull("查看知识点关联失败", result.get("code"));
        Assert.assertEquals("查看知识点关联失败",
                            LifeCircleErrorMessageMapper.CheckvGetKnowledgeRelationsContextobjectidParamFail.getCode(),
                            (String) result.get("code"));
        
        // 查看知识点关联（源知识点和目标知识点删除）
        // 先删除源知识点和目标知识点
        testDelete("knowledges", knowledgeTarget2);
        testDelete("knowledges", knowledgeTarget3);
        result1 = getKnowledgeRelations("lcms-special-context_type-dev-test",
                                        "1",
                                        "lcms-special-context_object-dev-test",
                                        knowledgeTarget2);
        Assert.assertNotNull("查看知识点关联失败", result1);
        Assert.assertNotNull("查看知识点关联失败", result1.get(0));
        Assert.assertNotNull("查看知识点关联失败", result1.get(0).get("context_type"));
        Assert.assertNull("查看知识点关联失败", result1.get(0).get("source"));
        Assert.assertNull("查看知识点关联失败", result1.get(0).get("target"));
        
        //正常删除知识点关联
        result = deleteKnowledgeRelations(krId);
        Assert.assertNotNull("删除知识点关联失败", result);
        Assert.assertNotNull("删除知识点关联失败", result.get("process_code"));
        Assert.assertEquals("删除知识点关联失败",
                            LifeCircleErrorMessageMapper.DeleteKnowledgesRelationSuccess.getCode(),
                            (String) result.get("process_code"));
        
        //删除知识点关联(uuid格式不对)
        result = deleteKnowledgeRelations("wrong-uuid");
        Assert.assertNotNull("删除知识点关联失败", result);
        Assert.assertNotNull("删除知识点关联失败", result.get("code"));
        Assert.assertEquals("删除知识点关联失败",
                            LifeCircleErrorMessageMapper.CheckIdentifierFail.getCode(),
                            (String) result.get("code"));
        
        /******************************************************************/
        /****************************知识点关联测试结束************************/
        /******************************************************************/
        /****************************章节知识点标签测试开始*********************/
        /******************************************************************/
        
        //正常添加知识点标签
        // 创建教材
        ResourceViewModel rvm = testCreate("teachingmaterials", null, null);
        Assert.assertNotNull("测试创建教材不通过", rvm);
        mid = rvm.getIdentifier();
        
        //创建章节
        result = createChapter(mid, null);
        Assert.assertNotNull("创建章节失败", result);
        cid = (String) result.get("identifier");
        Assert.assertNotNull("创建章节失败", cid);
        
        //创建章节知识点关系
        result = createChapterKnowledgeRelation("chapters",
                                                cid,
                                                knowledgeSource,
                                                TestEducationRelationController.DEFAULT_RELATION_TYPE,
                                                TestEducationRelationController.DEFAULT_LABEL,
                                                "knowledges",
                                                1,
                                                true);
        Assert.assertNotNull("创建教案和知识点的资源关系失败", result);
        Assert.assertNotNull("创建教案和知识点的资源关系失败", result.get("identifier"));
        
        List<String> tags = new ArrayList<String>();
        tags.add("tag_test3");
        
        List<Map<String, Object>> resultList = addChapterKnowledgeTag(cid, knowledgeSource, tags);
        Assert.assertNotNull("添加知识点标签失败", resultList);
        Assert.assertNotNull("添加知识点标签失败", resultList.get(0));
        Assert.assertNotNull("添加知识点标签失败", resultList.get(0).get("tags"));
        Assert.assertTrue("添加知识点标签失败",
                          resultList.get(0).get("tags").toString().contains("tag_test3"));
        
        //添加知识点标签(outline为空)
        result = addChapterKnowledgeTagException(null, knowledgeSource, tags);
        Assert.assertNotNull("添加知识点标签失败", result);
        Assert.assertNotNull("添加知识点标签失败", result.get("code"));
        Assert.assertEquals("添加知识点标签失败",
                            LifeCircleErrorMessageMapper.CheckAddTagsOutlineParamFail.getCode(),
                            (String) result.get("code"));
        
        // 添加知识点标签(knowledge为空)
        result = addChapterKnowledgeTagException(cid, null, tags);
        Assert.assertNotNull("添加知识点标签失败", result);
        Assert.assertNotNull("添加知识点标签失败", result.get("code"));
        Assert.assertEquals("添加知识点标签失败",
                            LifeCircleErrorMessageMapper.CheckAddTagsKnowledgeParamFail.getCode(),
                            (String) result.get("code"));

        // 添加知识点标签(tags为空)
        result = addChapterKnowledgeTagException(cid, knowledgeSource, null);
        Assert.assertNotNull("添加知识点标签失败", result);
        Assert.assertNotNull("添加知识点标签失败", result.get("code"));
        Assert.assertEquals("添加知识点标签失败",
                            LifeCircleErrorMessageMapper.CheckAddTagsTagsParamFail.getCode(),
                            (String) result.get("code"));
        
        // 添加知识点标签(关系不存在)
        result = addChapterKnowledgeTagException(cid, knowledgeTarget1, tags);
        Assert.assertNotNull("删除知识点标签失败", result);
        Assert.assertNotNull("删除知识点标签失败", result.get("code"));
        Assert.assertEquals("删除知识点标签失败",
                            LifeCircleErrorMessageMapper.ResourceRelationNotExist.getCode(),
                            (String) result.get("code"));
        
        // 正常删除知识点标签
        result = deleteChapterKnowledgeTag(cid, knowledgeSource, "tag_test3");
        Assert.assertNotNull("删除知识点标签失败", result);
        Assert.assertNotNull("删除知识点标签失败", result.get("process_code"));
        Assert.assertEquals("删除知识点标签失败",
                            LifeCircleErrorMessageMapper.DeleteKnowledgesChapterSuccess.getCode(),
                            (String) result.get("process_code"));
        
        //删除知识点标签(uuid格式错误)
        result = deleteChapterKnowledgeTag(cid, "wrong_uuid", "tag_test3");
        Assert.assertNotNull("删除知识点标签失败", result);
        Assert.assertNotNull("删除知识点标签失败", result.get("code"));
        Assert.assertEquals("删除知识点标签失败",
                            LifeCircleErrorMessageMapper.CheckIdentifierFail.getCode(),
                            (String) result.get("code"));
        
        // 删除知识点标签(tag为空)
        result = deleteChapterKnowledgeTag(cid, knowledgeSource, null);
        Assert.assertNotNull("删除知识点标签失败", result);
        Assert.assertNotNull("删除知识点标签失败", result.get("code"));
        Assert.assertEquals("删除知识点标签失败",
                            LifeCircleErrorMessageMapper.CheckDeleteTagTagParamFail.getCode(),
                            (String) result.get("code"));
        
        // 删除知识点标签(outline为空)
        result = deleteChapterKnowledgeTag(null, knowledgeSource, "tag_test3");
        Assert.assertNotNull("删除知识点标签失败", result);
        Assert.assertNotNull("删除知识点标签失败", result.get("code"));
        Assert.assertEquals("删除知识点标签失败",
                            LifeCircleErrorMessageMapper.CheckDeleteTagOutlineParamFail.getCode(),
                            (String) result.get("code"));
        
        // 删除知识点标签(关系不存在)
        result = deleteChapterKnowledgeTag(cid, knowledgeTarget1, "tag_test3");
        Assert.assertNotNull("删除知识点标签失败", result);
        Assert.assertNotNull("删除知识点标签失败", result.get("code"));
        Assert.assertEquals("删除知识点标签失败",
                            LifeCircleErrorMessageMapper.ResourceRelationNotExist.getCode(),
                            (String) result.get("code"));
        
        // 删除知识点标签(删除的标签不存在)
        result = deleteChapterKnowledgeTag(cid, knowledgeSource, "wrong_tag");
        Assert.assertNotNull("删除知识点标签失败", result);
        Assert.assertNotNull("删除知识点标签失败", result.get("code"));
        Assert.assertEquals("删除知识点标签失败",
                            LifeCircleErrorMessageMapper.TagNotExist.getCode(),
                            (String) result.get("code"));
        
        /******************************************************************/
        /****************************章节知识点标签测试结束**********************/
        /******************************************************************/
    }
    
    
    
    @Override
    public void after() {
        super.after();
        // 删除知识点、教材、章节
        testDelete("knowledges", knowledgeTarget2);
        testDelete("knowledges", knowledgeTarget3);
        testDelete("knowledges", knowledgeTarget1);
        testDelete("knowledges", knowledgeSource);
        deleteChapter(mid, cid);
        testDelete("teachingmaterials", mid);
    }

    /**
     * 创建章节
     * 
     * @param mid 教材id
     * @return
     * @since
     */
    public Map<String, Object> createChapter(String mid, String chapterId) {
        ChapterViewModel viewModel = TestEducationRelationController.getCreateChapterInfo(mid);
        if (StringUtils.isNotEmpty(chapterId)) {
            viewModel.setParent(chapterId);
        }
        String uri = "/v0.6/teachingmaterials/" + mid + "/chapters";
        Map<String, Object> result = null;
        try {
            String json = objectMapper.writeValueAsString(viewModel);
            String resStr = MockUtil.mockCreate(mockMvc, uri, json);
            result = ObjectUtils.fromJson(resStr, Map.class);
        } catch (Exception e) {
            LOG.error("测试知识点-章节创建出错！");
        }

        return result;
    }
    
    /**
     * 删除章节
     * 
     * @param mid
     * @param chapterId
     * @return
     * @since
     */
    public Map<String, Object> deleteChapter(String mid, String chapterId) {
        String uri = "/v0.6/teachingmaterials/" + mid + "/chapters/" + chapterId + "?is_real = true";
        Map<String, Object> result = null;
        try {
            String resStr = MockUtil.mockDelete(mockMvc, uri, null);
            result = ObjectUtils.fromJson(resStr, Map.class);
        } catch (Exception e) {
            LOG.error("测试知识点-删除章节出错！");
        }

        return result;
    }
    
    /**
     * 创建资源知识点
     * 
     * @param parent
     * @param target
     * @param direction
     * @param knowledgeSource
     * @param knowledgeTarget
     * @return
     * @since
     */
    public Map<String, Object> createKnowledge(String parent,
                                               String target,
                                               String direction,
                                               String knowledgeSource,
                                               String knowledgeTarget,
                                               boolean haveCategories) {
        KnowledgeViewModel4In viewModel = getCreateKnowledgeInfo(parent,
                                                                 target,
                                                                 direction,
                                                                 knowledgeSource,
                                                                 knowledgeTarget,
                                                                 haveCategories);
        String uri = "/v0.6/knowledges";
        Map<String,Object>  result = null;
        try {
            String json = objectMapper.writeValueAsString(viewModel);
            String resStr = MockUtil.mockCreate(mockMvc, uri, json);
            result = ObjectUtils.fromJson(resStr, Map.class);
        }  catch (Exception e) {
            LOG.error("测试知识点-知识点创建出错！");
        }
        
        return result;
    }
    
    /**
     * 修改资源知识点
     * 
     * @param parent
     * @param target
     * @param direction
     * @param knowledgeSource
     * @param knowledgeTarget
     * @return
     * @since
     */
    public Map<String, Object> updateKnowledge(String knowledgeId,
                                               String parent,
                                               String target,
                                               String direction,
                                               String knowledgeSource,
                                               String knowledgeTarget,
                                               boolean haveCategories) {
        KnowledgeViewModel4In viewModel = getCreateKnowledgeInfo(parent,
                                                                 target,
                                                                 direction,
                                                                 knowledgeSource,
                                                                 knowledgeTarget,
                                                                 haveCategories);
        List<String> tags = new ArrayList<String>();
        tags.add("lcms-special-tag-update-dev-test1");
        tags.add("lcms-special-tag-update-dev-test2");
        viewModel.setTags(tags);
        String uri = "/v0.6/knowledges/" + knowledgeId;
        Map<String,Object>  result = null;
        try {
            String json = objectMapper.writeValueAsString(viewModel);
            String resStr = MockUtil.mockPut(mockMvc, uri, json);
            result = ObjectUtils.fromJson(resStr, Map.class);
        }  catch (Exception e) {
            LOG.error("测试知识点-知识点修改出错！");
        }
        
        return result;
    }
    
    /**
     * 移动知识点
     * 
     * @param knowledgeId
     * @param parent
     * @param target
     * @param direction
     * @return
     * @since
     */
    public Map<String, Object> moveKnowledge(String knowledgeId, String parent, String target, String direction) {
        KnowledgeViewModel4Move viewModel = getMoveKnowledgeInfo(parent, target, direction);
        String uri = "/v0.6/knowledges/" + knowledgeId + "/actions/move";
        Map<String, Object> result = null;
        try {
            String json = objectMapper.writeValueAsString(viewModel);
            String resStr = MockUtil.mockPut(mockMvc, uri, json);
            result = ObjectUtils.fromJson(resStr, Map.class);
        } catch (Exception e) {
            LOG.error("测试知识点-知识点移动出错！");
        }

        return result;
    }
    
    /**
     * 添加知识点关联
     * 
     * @param source
     * @param target
     * @return
     * @since
     */
    public Map<String, Object> addKnowledgeRelation(String source, String target) {
        KnowledgeRelationsViewModel4Add viewModel = getAddKnowledgeRelationInfo(source, target);
        String uri = "/v0.6/knowledges/relations";
        Map<String, Object> result = null;
        try {
            String json = objectMapper.writeValueAsString(viewModel);
            String resStr = MockUtil.mockCreate(mockMvc, uri, json);
            result = ObjectUtils.fromJson(resStr, Map.class);
        } catch (Exception e) {
            LOG.error("测试知识点-添加知识点关联出错！");
        }

        return result;
    }
    
    /**
     * 添加章节知识点标签
     * 
     * @param cid
     * @param kid
     * @param tags
     * @return
     * @since
     */
    public List<Map<String, Object>> addChapterKnowledgeTag(String cid, String kid, List<String> tags){
        List<ChapterKnowledgeViewModel> viewModels = new ArrayList<ChapterKnowledgeViewModel>();
        ChapterKnowledgeViewModel viewModel = new ChapterKnowledgeViewModel();
        viewModel.setOutline(cid);
        viewModel.setKnowledge(kid);
        viewModel.setTags(tags);
        viewModels.add(viewModel);
        String uri = "/v0.6/knowledges/tags";
        List<Map<String, Object>> result = null;
        try {
            String json = objectMapper.writeValueAsString(viewModels);
            String resStr = MockUtil.mockCreate(mockMvc, uri, json);
            result = ObjectUtils.fromJson(resStr, new TypeToken<List<Map<String, Object>>>() {
            });
        } catch (Exception e) {
            LOG.error("测试知识点-添加章节知识点标签出错！");
        }

        return result;
    }
    
    /**
     * 添加章节知识点标签
     * 
     * @param cid
     * @param kid
     * @param tags
     * @return
     * @since
     */
    public Map<String, Object> addChapterKnowledgeTagException(String cid, String kid, List<String> tags){
        List<ChapterKnowledgeViewModel> viewModels = new ArrayList<ChapterKnowledgeViewModel>();
        ChapterKnowledgeViewModel viewModel = new ChapterKnowledgeViewModel();
        viewModel.setOutline(cid);
        viewModel.setKnowledge(kid);
        viewModel.setTags(tags);
        viewModels.add(viewModel);
        String uri = "/v0.6/knowledges/tags";
        Map<String, Object> result = null;
        try {
            String json = objectMapper.writeValueAsString(viewModels);
            String resStr = MockUtil.mockCreate(mockMvc, uri, json);
            result = ObjectUtils.fromJson(resStr, Map.class);
        } catch (Exception e) {
            LOG.error("测试知识点-添加章节知识点标签出错！");
        }

        return result;
    }
    
    /**
     * 删除章节知识点标签
     * 
     * @param cid
     * @param kid
     * @param tag
     * @return
     * @since
     */
    public Map<String, Object> deleteChapterKnowledgeTag(String cid, String kid, String tag){
        String uri = "/v0.6/knowledges/" + kid + "/tags?tag=";
        if(StringUtils.isNotEmpty(tag)) {
            uri += tag;
        }
        
        uri += "&outline=";
        if(StringUtils.isNotEmpty(cid)) {
            uri += cid;
        }
        Map<String, Object> result = null;
        try {
            String resStr = MockUtil.mockDelete(mockMvc, uri, null);
            result = ObjectUtils.fromJson(resStr, Map.class);
        } catch (Exception e) {
            LOG.error("测试知识点-删除章节知识点标签出错！");
        }

        return result;
    }
    
    /**
     * 创建章节知识点关系
     * 
     * @param cid
     * @param kid
     * @return
     * @since
     */
    public Map<String, Object> createChapterKnowledgeRelation(String resType,
                                                              String source,
                                                              String target,
                                                              String relationType,
                                                              String label,
                                                              String targetType,
                                                              Integer orderNum,
                                                              boolean haveLifecycle) {
        EducationRelationViewModel viewModel = TestEducationRelationController.getEducationRelationViewModel(target,
                                                                                                             relationType,
                                                                                                             label,
                                                                                                             targetType,
                                                                                                             orderNum,
                                                                                                             haveLifecycle);
        String uri = "/v0.6/" + resType + "/" + source + "/relations";
        Map<String, Object> result = null;
        try {
            String json = objectMapper.writeValueAsString(viewModel);
            String resStr = MockUtil.mockCreate(mockMvc, uri, json);
            result = ObjectUtils.fromJson(resStr, Map.class);
        } catch (Exception e) {
            LOG.error("测试知识点-创建章节知识点关系出错！");
        }

        return result;
    }
    
    /**
     * 获取知识点关联
     * 
     * @param contexttype
     * @param relationtype
     * @param contextobjectid
     * @param knowledgeId
     * @return
     * @since
     */
    public List<Map<String, Object>> getKnowledgeRelations(String contexttype,
                                                           String relationtype,
                                                           String contextobjectid,
                                                           String knowledgeId) {
        String uri = "/v0.6/knowledges/relations?relationtype=";
        if(StringUtils.isNotEmpty(relationtype)){
            uri += relationtype;
        }
        
        uri += "&knowledge=";
        if (StringUtils.isNotEmpty(knowledgeId)) {
            uri += knowledgeId;
        }
        
        uri += "&contextobjectid=";
        if(StringUtils.isNotEmpty(contextobjectid)){
            uri += contextobjectid;
        }
        
        uri += "&contexttype=";
        if(StringUtils.isNotEmpty(contexttype)){
            uri += contexttype;
        }
        List<Map<String, Object>> result = null;
        try {
            String resStr = MockUtil.mockGet(mockMvc, uri, null);
            result = ObjectUtils.fromJson(resStr, new TypeToken<List<Map<String, Object>>>() {
            });
        } catch (Exception e) {
            LOG.error("测试知识点-获取知识点关联出错！");
        }

        return result;
    }
    
    /**
     * 获取知识点关联(异常)
     * 
     * @param contexttype
     * @param relationtype
     * @param contextobjectid
     * @param knowledgeId
     * @return
     * @since
     */
    public Map<String, Object> getKnowledgeRelationsException(String contexttype,
                                                     String relationtype,
                                                     String contextobjectid,
                                                     String knowledgeId) {
        String uri = "/v0.6/knowledges/relations?relationtype=";
        if(StringUtils.isNotEmpty(relationtype)){
            uri += relationtype;
        }
        
        uri += "&knowledge=";
        if (StringUtils.isNotEmpty(knowledgeId)) {
            uri += knowledgeId;
        }
        
        uri += "&contextobjectid=";
        if(StringUtils.isNotEmpty(contextobjectid)){
            uri += contextobjectid;
        }
        
        uri += "&contexttype=";
        if(StringUtils.isNotEmpty(contexttype)){
            uri += contexttype;
        }
        Map<String, Object> result = null;
        try {
            String resStr = MockUtil.mockGet(mockMvc, uri, null);
            result = ObjectUtils.fromJson(resStr, Map.class);
        } catch (Exception e) {
            LOG.error("测试知识点-获取知识点关联出错！");
        }

        return result;
    }
    
    /**
     * 删除知识点关联
     * 
     * @param krId
     * @return
     * @since
     */
    public Map<String, Object> deleteKnowledgeRelations(String krId) {
        String uri = "/v0.6/knowledges/relations/" + krId;
        Map<String, Object> result = null;
        try {
            String resStr = MockUtil.mockDelete(mockMvc, uri, null);
            result = ObjectUtils.fromJson(resStr, Map.class);
        } catch (Exception e) {
            LOG.error("测试知识点-删除知识点关联出错！");
        }

        return result;
    }
    
    /**
     * 获取创建知识点元数据
     * 
     * @param parent
     * @param target
     * @param direction
     * @param knowledgeSource
     * @param knowledgeTarget
     * @param haveCategories
     * @return
     * @since
     */
    public static KnowledgeViewModel4In getCreateKnowledgeInfo(String parent,
                                                               String target,
                                                               String direction ,
                                                               String knowledgeSource,
                                                               String knowledgeTarget,
                                                               boolean haveCategories) {
        KnowledgeViewModel4In viewModel = new KnowledgeViewModel4In();
        viewModel.setTitle("LC单元测试默认用例");
        viewModel.setDescription("lcms-special-description-dev-test");
        viewModel.setLanguage("zh_cn");
        List<String> keywords = new ArrayList<String>();
        keywords.add("方程");
        keywords.add("一元一次");
        viewModel.setKeywords(keywords);
        List<String> tags = new ArrayList<String>();
        tags.add("lcms-special-tag-create-dev-test1");
        tags.add("lcms-special-tag-create-dev-test2");
        viewModel.setTags(tags);
        ResLifeCycleViewModel lifeCycle = new ResLifeCycleViewModel();
        lifeCycle.setVersion("v0.1");
        lifeCycle.setStatus("CREATING");
        lifeCycle.setEnable(true);
        lifeCycle.setCreator("caocr_test");
        lifeCycle.setPublisher("esp-lifecycle");
        lifeCycle.setProvider("nd");
        lifeCycle.setProviderSource("nd company");
        viewModel.setLifeCycle(lifeCycle);
        
        Map<String, List<? extends ResClassificationViewModel>> categories = new HashMap<String, List<? extends ResClassificationViewModel>>();
        List<ResClassificationViewModel> value = new ArrayList<ResClassificationViewModel>();
        ResClassificationViewModel path = new ResClassificationViewModel();
        if (haveCategories) {
            path.setTaxonpath("K12/$ON030000/$ON030200/$SB02300/$E004000/$E004001");
            path.setTaxoncode("$SB02300");
            value.add(path);
            categories.put("subject", value);
        }
        
        path = new ResClassificationViewModel();
        path.setTaxonpath("");
        path.setTaxoncode("$RA0205");
        value = new ArrayList<ResClassificationViewModel>();
        value.add(path);
        categories.put("res_type", value);
        viewModel.setCategories(categories);
        
        if (StringUtils.isNotEmpty(parent) || StringUtils.isNotEmpty(target)|| StringUtils.isNotEmpty(direction)) {
            KnowledgeExtPropertiesViewModel extPropertiesViewModel = new KnowledgeExtPropertiesViewModel();
            if (StringUtils.isNotEmpty(parent)) {
                extPropertiesViewModel.setParent(parent);
            }
            
            if (StringUtils.isNotEmpty(target)) {
                extPropertiesViewModel.setTarget(target);
            }
            
            if (StringUtils.isNotEmpty(direction )) {
                extPropertiesViewModel.setDirection(direction);
            }
            viewModel.setPosition(extPropertiesViewModel);
        }

        if (StringUtils.isNotEmpty(knowledgeSource) && StringUtils.isNotEmpty(knowledgeTarget)) {
            List<KnowledgeRelationsViewModel4Add> relationsViewModels = new ArrayList<KnowledgeRelationsViewModel4Add>();
            KnowledgeRelationsViewModel4Add relationsViewModel = new KnowledgeRelationsViewModel4Add();
            relationsViewModel.setSource(knowledgeSource);
            relationsViewModel.setTarget(knowledgeTarget);
            relationsViewModel.setRelationType("1");
            relationsViewModel.setContextType("lcms-special-context_type-dev-test");
            relationsViewModel.setContextObject("lcms-special-context_object-dev-test");
            relationsViewModels.add(relationsViewModel);
            viewModel.setKnowledgeRelations(relationsViewModels);
        }

        return viewModel;
    }
    
    /**
     * 获取移动知识点元数据
     * 
     * @param parent
     * @param target
     * @param direction
     * @return
     * @since
     */
    public static KnowledgeViewModel4Move getMoveKnowledgeInfo(String parent, String target, String direction) {
        KnowledgeViewModel4Move viewModel = new KnowledgeViewModel4Move();
        viewModel.setParent(parent);
        viewModel.setDirection(direction);
        viewModel.setTarget(target);
        
        return viewModel;
    }
    
    /**
     * 获取知识点关联数据
     * 
     * @param source
     * @param target
     * @return
     * @since
     */
    public static KnowledgeRelationsViewModel4Add getAddKnowledgeRelationInfo(String source, String target) {
        KnowledgeRelationsViewModel4Add viewModel = new KnowledgeRelationsViewModel4Add();
        viewModel.setSource(source);
        viewModel.setTarget(target);
        viewModel.setRelationType("1");
        viewModel.setContextType("lcms-special-context_type-dev-test");
        viewModel.setContextObject("lcms-special-context_object-dev-test");
        
        return viewModel;
    }

}
