/* =============================================================
 * Created: [2016年2月2日] by caocr
 * =============================================================
 *
 * Copyright 2014-2015 NetDragon Websoft Inc. All Rights Reserved
 *
 * =============================================================
 */

package nd.esp.service.lifecycle.controller.v06;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import nd.esp.service.lifecycle.educommon.vos.ResClassificationViewModel;
import nd.esp.service.lifecycle.educommon.vos.ResLifeCycleViewModel;
import nd.esp.service.lifecycle.educommon.vos.ResRelationViewModel;
import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;
import nd.esp.service.lifecycle.impl.SimpleJunitTest4ResourceImpl;
import nd.esp.service.lifecycle.models.v06.BatchAdjustRelationOrderModel;
import nd.esp.service.lifecycle.repository.model.Chapter;
import nd.esp.service.lifecycle.repository.model.Lesson;
import nd.esp.service.lifecycle.repository.model.ResourceCategory;
import nd.esp.service.lifecycle.repository.model.TeachingMaterial;
import nd.esp.service.lifecycle.repository.sdk.ChapterRepository;
import nd.esp.service.lifecycle.repository.sdk.LessonRepository;
import nd.esp.service.lifecycle.repository.sdk.ResourceCategoryRepository;
import nd.esp.service.lifecycle.repository.sdk.TeachingMaterialRepository;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;
import nd.esp.service.lifecycle.vos.chapters.v06.ChapterViewModel;
import nd.esp.service.lifecycle.vos.educationrelation.v06.EducationRelationLifeCycleViewModel;
import nd.esp.service.lifecycle.vos.educationrelation.v06.EducationRelationViewModel;
import nd.esp.service.lifecycle.vos.educationrelation.v06.RelationForQueryViewModel;
import nd.esp.service.lifecycle.vos.instructionalobjectives.v06.InstructionalObjectiveViewModel;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nd.gaea.rest.o2o.JacksonCustomObjectMapper;
import com.nd.gaea.rest.testconfig.MockUtil;

/**
 * @author caocr
 * @since 
 *
 */
public class TestEducationRelationController extends SimpleJunitTest4ResourceImpl {
    private final static Logger LOG = LoggerFactory.getLogger(TestEducationRelationController.class);
    
    public final static String DEFAULT_LABEL = "lcms-special-label-dev-test1";
    public final static String DEFAULT_RELATION_TYPE = "ASSOCIATE";
    public final static String DEFAULT_LIMIT = "(0,20)";
    
    @Autowired
    ResourceCategoryRepository categoryRepository;
    
    @Autowired
    TeachingMaterialRepository teachingMaterialRepository;
    
    @Autowired
    ChapterRepository chapterRepository;
    
    @Autowired
    LessonRepository lessonRepository;
    
    private JacksonCustomObjectMapper objectMapper = new JacksonCustomObjectMapper();
    
    private String objectiveId;

    private String lessonId;

    private String tmId;

    private String cid;

    private String questionId;

    private String subCid;

    private String tmId1;

    private String tmId2;

    private String chapterId1;

    private String chapterId2;

    private String cwoId;

    private String cwoId1;

    private String cwoId2;

    private String lessonId1;

    private String lessonId2;

    @Override
    public void before() {

    }
    
    @Override
    public void after() {
        // 删除知识点、教材、章节
        testDelete("instructionalobjectives", objectiveId);
        testDelete("lessons", lessonId);
        testDelete("lessons", lessonId1);
        testDelete("lessons", lessonId2);
        testDelete("coursewareobjects", cwoId);
        testDelete("coursewareobjects", cwoId1);
        testDelete("coursewareobjects", cwoId2);
        testDelete("questions", questionId);
        deleteChapter(tmId, subCid);
        deleteChapter(tmId, cid);
        testDelete("teachingmaterials", tmId);
        deleteChapter(tmId1, chapterId1);
        deleteChapter(tmId2, chapterId2);
        testDelete("teachingmaterials", tmId1);
        testDelete("teachingmaterials", tmId2);
    }

	@Test
    public void testAll() {
        /******************************************************************/
        /****************************创建资源关系测试 开始************************/
        /******************************************************************/
        
        //正常创建资源关系
        //创建源资源课时
        Map<String,Object> result = createLesson();
        Assert.assertNotNull("资源关系源资源课时创建失败", result);
        lessonId  = (String) result.get("identifier");
        Assert.assertNotNull("资源关系源资源课时创建失败", lessonId);
        
        // 创建目标资源教学目标
        result = createInstructionalObjective(lessonId);
        Assert.assertNotNull("资源关系目标资源教学目标创建失败", result);
        objectiveId = (String) result.get("identifier");
        Assert.assertNotNull("资源关系目标资源教学目标创建失败", objectiveId);
        
        //创建资源关系
        result = createRelation("lessons", lessonId, objectiveId, DEFAULT_RELATION_TYPE, DEFAULT_LABEL, "instructionalobjectives", 30, true);
        Assert.assertNotNull("创建课时和教学目标的资源关系失败", result);
        String relationId = (String) result.get("identifier");
        Assert.assertNotNull("创建课时和教学目标的资源关系失败", relationId);
        
        //重复创建直接返回
        result = createRelation("lessons", lessonId, objectiveId, DEFAULT_RELATION_TYPE, DEFAULT_LABEL, "instructionalobjectives", 30, true);
        Assert.assertNotNull("创建课时和教学目标的资源关系失败", result);
        Assert.assertNotNull("创建课时和教学目标的资源关系失败", result.get("identifier"));
        
        // 创建资源关系-label为空、relation_type和lifecycle为null
        result = createRelation("lessons", lessonId, objectiveId, null, null, "instructionalobjectives", 55, false);
        Assert.assertNotNull("创建课时和教学目标的资源关系失败", result);
        Assert.assertNotNull("创建课时和教学目标的资源关系失败", result.get("identifier"));
        
        //测试不存在的源资源类型
        result = createRelation("wrongrestype", lessonId, objectiveId, DEFAULT_RELATION_TYPE, DEFAULT_LABEL, "instructionalobjectives", 50, true);
        Assert.assertNotNull("异常信息返回为空", result);
        Assert.assertEquals("资源类型不对", result.get("message"));
        
        // 测试不存在的目标资源类型
        result = createRelation("lessons", lessonId, objectiveId, DEFAULT_RELATION_TYPE, DEFAULT_LABEL, "wrongtargettype", 50, true);
        Assert.assertNotNull("异常信息返回为空", result);
        Assert.assertEquals("资源类型不对", result.get("message"));
        
        //测试order_num重复
        result = createRelation("lessons", lessonId, objectiveId, DEFAULT_RELATION_TYPE, "lcms-special-label-dev-test2", "instructionalobjectives", 30, true);
        Assert.assertNotNull("异常信息返回为空", result);
        Assert.assertEquals("orderNum不允许重复", result.get("message"));
        
        //测试不存在的源资源
        String sourceUuid = UUID.randomUUID().toString();
        result = createRelation("lessons", sourceUuid, objectiveId, DEFAULT_RELATION_TYPE, "lcms-special-label-dev-test2", "instructionalobjectives", 30, true);
        Assert.assertNotNull("异常信息返回为空", result);
        Assert.assertEquals("LC/SOURCE_RESOURCE_NOT_FOUND", result.get("code"));
        
        // 测试不存在的目标资源
        result = createRelation("lessons", lessonId, UUID.randomUUID().toString(), DEFAULT_RELATION_TYPE, "lcms-special-label-dev-test2", "instructionalobjectives", 30, true);
        Assert.assertNotNull("异常信息返回为空", result);
        Assert.assertEquals("LC/TARGET_RESOURCE_NOT_FOUND", result.get("code"));
        
        // 创建资源时关系中有重复的order_num
        result = createInstructionalObjectiveWithSameOrderNum(lessonId);
        Assert.assertNotNull("异常信息返回为空", result);
        Assert.assertEquals("orderNum不允许重复", result.get("message"));

        // 创建章节和习题的资源关系
        // 创建教材
        result = createTeachingMaterial();
        Assert.assertNotNull("资源关系教材创建失败", result);
        tmId = (String) result.get("identifier");
        Assert.assertNotNull("资源关系教材创建失败", tmId);

        // 创建章节
        result = createChapter(tmId, null);
        Assert.assertNotNull("资源关系源资源章节创建失败", result);
        cid = (String) result.get("identifier");
        Assert.assertNotNull("资源关系源资源章节创建失败", cid);
        
        // 创建习题
        result = createQuestions();
        Assert.assertNotNull("资源关系目标资源习题创建失败", result);
        questionId = (String) result.get("identifier");
        Assert.assertNotNull("资源关系目标资源习题创建失败", questionId);
        
        // 创建章节和习题的资源关系
        result = createRelation("chapters", cid, questionId, DEFAULT_RELATION_TYPE, DEFAULT_LABEL, "questions", 30, true);
        Assert.assertNotNull("创建章节和习题的资源关系失败", result);
        Assert.assertNotNull("创建教案和习题的资源关系失败", result.get("identifier"));
        
        /******************************************************************/
        /****************************创建资源关系测试结束************************/
        /*****************************************************************/
        /****************************修改资源关系测试开始***********************/
        /****************************************************************/
        
        //正常修改资源关系
        result = updateRelation(relationId, "lessons", lessonId, DEFAULT_RELATION_TYPE, "lcms-special-label-dev-test3", 60, true, true);
        Assert.assertNotNull("修改课时和教学目标的资源关系失败", result);
        Assert.assertNotNull("修改课时和教学目标的资源关系失败", result.get("identifier"));
        Assert.assertEquals("lcms-special-label-dev-test3", result.get("label"));
        
        //测试order_num重复
        result = updateRelation(relationId, "lessons", lessonId, DEFAULT_RELATION_TYPE, "lcms-special-label-dev-test5", 60, true, true);
        Assert.assertNotNull("异常信息返回为空", result);
        Assert.assertEquals("orderNum不允许重复", result.get("message"));
        
        // 测试不存在的源资源类型
        result = updateRelation(relationId, "wrongrestype", lessonId, DEFAULT_RELATION_TYPE, "lcms-special-label-dev-test5", 60, true, true);
        Assert.assertNotNull("异常信息返回为空", result);
        Assert.assertEquals("资源类型不对", result.get("message"));
            
        //测试不存在的源资源id
        result = updateRelation(relationId, "lessons", UUID.randomUUID().toString(), DEFAULT_RELATION_TYPE, "lcms-special-label-dev-test5", 60, true, true);
        Assert.assertNotNull("异常信息返回为空", result);
        Assert.assertEquals("LC/SOURCE_RESOURCE_NOT_FOUND", result.get("code"));
            
        // 测试不存在的资源关系id
        result = updateRelation(UUID.randomUUID().toString(), "lessons", lessonId, DEFAULT_RELATION_TYPE, "lcms-special-label-dev-test5", 60, true, true);
        Assert.assertNotNull("异常信息返回为空", result);
        Assert.assertEquals("LC/RESOURCE_RELATION_NOT_EXIST", result.get("code"));
            
        // 测试不传某些属性
        result = updateRelation(relationId, "lessons", lessonId, null, null, 70, false, false);
        Assert.assertNotNull("修改教案和课时的资源关系失败", result);
        Assert.assertNotNull("修改教案和课时的资源关系失败", result.get("identifier"));
        Assert.assertEquals("lcms-special-label-dev-test3", result.get("label"));
        
        /******************************************************************/
        /****************************修改资源关系测试结束************************/
        /*****************************************************************/
        /****************************删除资源关系测试开始***********************/
        /****************************************************************/
        
        //正常删除资源关系--通过关系id
        result = deleteRelation(relationId, "lessons", lessonId);
        Assert.assertNotNull("资源关系删除失败", result);
        Assert.assertNotNull("资源关系删除失败",  result.get("process_code"));
        Assert.assertEquals("资源关系删除失败",
                            LifeCircleErrorMessageMapper.DeleteEducationRelationSuccess.getCode(),
                            (String) result.get("process_code"));
        
        // 测试资源关系不存在会不会抛异常
        result = deleteRelation(relationId, "lessons", lessonId);
        Assert.assertNotNull("异常信息返回为空", result);
        Assert.assertEquals("资源关系不存在", result.get("message"));
        
        // 通过目标资源id正常删除资源关系
        result = deleteRelationByTarget(cid, "chapters", questionId, "false");
        Assert.assertNotNull("资源关系批量删除失败", result);
        Assert.assertNotNull("资源关系批量删除失败", result.get("process_code"));
        Assert.assertEquals("资源关系批量删除失败",
                            LifeCircleErrorMessageMapper.DeleteBatchRelationSuccess.getCode(),
                            (String) result.get("process_code"));
        
        //通过目标资源id删除资源关系--target不存在
        result = deleteRelationByTarget(lessonId, "lessons", UUID.randomUUID().toString(), "false");
        Assert.assertNotNull("资源关系批量删除失败", result);
        Assert.assertNotNull("资源关系批量删除失败",  result.get("process_code"));
        Assert.assertEquals("资源关系批量删除失败",
                            LifeCircleErrorMessageMapper.DeleteBatchRelationSuccess.getCode(),
                            (String) result.get("process_code"));
        
        // 通过目标资源id正常反转删除资源关系
        result = deleteRelationByTarget(objectiveId, "instructionalobjectives", lessonId, "true");
        Assert.assertNotNull("资源关系批量删除失败", result);
        Assert.assertNotNull("资源关系批量删除失败", result.get("process_code"));
        Assert.assertEquals("资源关系批量删除失败",
                            LifeCircleErrorMessageMapper.DeleteBatchRelationSuccess.getCode(),
                            (String) result.get("process_code"));
        
        // 创建章节和习题的资源关系
        result = createRelation("chapters", cid, questionId, DEFAULT_RELATION_TYPE, DEFAULT_LABEL, "questions", 30, true);
        Assert.assertNotNull("创建章节和习题的资源关系失败", result);
        Assert.assertNotNull("创建教案和习题的资源关系失败", result.get("identifier"));
        
        // target不传
        result = deleteRelationByTarget(cid, "chapters", null, "false");
        Assert.assertNotNull("资源关系批量删除失败", result);
        Assert.assertNotNull("资源关系批量删除失败",  result.get("process_code"));
        Assert.assertEquals("资源关系批量删除失败",
                            LifeCircleErrorMessageMapper.DeleteBatchRelationSuccess.getCode(),
                            (String) result.get("process_code"));
        
        /******************************************************************/
        /****************************删除资源关系测试结束************************/
        /*****************************************************************/
        /****************************获取目标资源测试开始***********************/
        /****************************************************************/
        
        // 获取目标资源
        //创建子章节
        result = createChapter(tmId, cid);
        Assert.assertNotNull("资源关系源资源章节创建失败", result);
        subCid = (String) result.get("identifier");
        Assert.assertNotNull("资源关系源资源章节创建失败", subCid);
        
        // 创建章节和习题的资源关系
        result = createRelation("chapters", cid, questionId, DEFAULT_RELATION_TYPE, DEFAULT_LABEL, "questions", 30, true);
        Assert.assertNotNull("创建章节和习题的资源关系失败", result);
        Assert.assertNotNull("创建教案和习题的资源关系失败", result.get("identifier"));
        
        // 创建章节和习题的资源关系
        result = createRelation("chapters", subCid, questionId, DEFAULT_RELATION_TYPE, DEFAULT_LABEL, "questions", 30, true);
        Assert.assertNotNull("创建章节和习题的资源关系失败", result);
        Assert.assertNotNull("创建教案和习题的资源关系失败", result.get("identifier"));
        
        // 创建章节和课时的资源关系
        result = createRelation("chapters", cid, lessonId, DEFAULT_RELATION_TYPE, DEFAULT_LABEL, "lessons", 70, true);
        Assert.assertNotNull("创建章节和课时的资源关系失败", result);
        Assert.assertNotNull("创建教案和课时的资源关系失败", result.get("identifier"));
        
        //查目标资源
        String categories = "K12/$ON030000/$ON030200/$SB0500/$E004000/$E004001,K12/$ON030000/$ON030200/$SB0500/$E004000/*,$ON030000,$ON030*00";
        result = searceByResType("chapters", cid, "questions", categories, "User", "890399", "OWNER", DEFAULT_LABEL, "lcms-special-tag-dev-test1", DEFAULT_RELATION_TYPE, DEFAULT_LIMIT, "false", "false");
        Assert.assertNotNull("查询章节和习题的资源关系失败", result);
        Assert.assertNotNull("查询章节和习题的资源关系失败", result.get("items"));
        
        categories = "K12/$ON030000/$ON030200/$SB0500/$E004000/$E004001";
        result = searceByResType("chapters", cid, "questions", categories, "User", "890399", "OWNER", DEFAULT_LABEL, "lcms-special-tag-dev-test1", DEFAULT_RELATION_TYPE, DEFAULT_LIMIT, "false", "false");
        Assert.assertNotNull("查询章节和习题的资源关系失败", result);
        Assert.assertNotNull("查询章节和习题的资源关系失败", result.get("items"));
        
        categories = "$ON030000";
        result = searceByResType("chapters", cid, "questions", categories, null, "890399", "OWNER", DEFAULT_LABEL, "lcms-special-tag-dev-test1", DEFAULT_RELATION_TYPE, DEFAULT_LIMIT, "false", "false");
        Assert.assertNotNull("查询章节和习题的资源关系失败", result);
        Assert.assertNotNull("查询章节和习题的资源关系失败", result.get("items"));
        
        // 不带覆盖范围等参数查询
        result = searceByResType("chapters", cid, "questions", null, null, null, null, null, null, DEFAULT_RELATION_TYPE, DEFAULT_LIMIT, "false", "false");
        Assert.assertNotNull("查询章节和习题的资源关系失败", result);
        Assert.assertNotNull("查询章节和习题的资源关系失败", result.get("items"));
        
        // 反转查询
        result = searceByResType("questions", questionId, "chapters", null, null, null, null, null, null, DEFAULT_RELATION_TYPE, DEFAULT_LIMIT, "true", "false");
        Assert.assertNotNull("查询章节和习题的资源关系失败", result);
        Assert.assertNotNull("查询章节和习题的资源关系失败", result.get("items"));
        
        // 递归查询关系
        result = searceByResType("chapters", cid, "questions", null, null, null, null, null, null, DEFAULT_RELATION_TYPE, DEFAULT_LIMIT, "false", "true");
        Assert.assertNotNull("递归查询章节和习题的资源关系失败", result);
        Assert.assertNotNull("递归查询章节和习题的资源关系失败", result.get("items"));
        Assert.assertNotNull("递归查询章节和习题的资源关系失败", result.get("total"));
        Assert.assertEquals("递归查询章节和习题的资源关系失败", 2.0, result.get("total"));
        
        // 递归查询关系不支持的源资源类型
        result = searceByResType("lessonplans", cid, "questions", null, null, null, null, null, null, DEFAULT_RELATION_TYPE, DEFAULT_LIMIT, "false", "true");
        Assert.assertNotNull("异常信息返回为空", result);
        Assert.assertEquals(LifeCircleErrorMessageMapper.RelationSupportTypeError.getCode(), result.get("code"));
        
        // 目标资源类型传空
        result = searceByResType("chapters", cid, null, null, null, null, null, null, null, DEFAULT_RELATION_TYPE, DEFAULT_LIMIT, "false", "false");
        Assert.assertNotNull("异常信息返回为空", result);
        Assert.assertEquals(LifeCircleErrorMessageMapper.CheckTargetTypeIsNull.getCode(), result.get("code"));
        
        // 目标资源类型不存在
        result = searceByResType("chapters", cid, "AAA", null, null, null, null, null, null, DEFAULT_RELATION_TYPE, DEFAULT_LIMIT, "false", "false");
        Assert.assertNotNull("异常信息返回为空", result);
        Assert.assertEquals(LifeCircleErrorMessageMapper.CheckTargetTypeError.getCode(), result.get("code"));
        
        // 覆盖范围ct_type传不存在的
        result = searceByResType("chapters", cid, "questions", null, "AAA", null, null, null, null, DEFAULT_RELATION_TYPE, DEFAULT_LIMIT, "false", "false");
        Assert.assertNotNull("异常信息返回为空", result);
        Assert.assertEquals(LifeCircleErrorMessageMapper.CoverageTargetTypeNotExist.getCode(), result.get("code"));
        
        // 覆盖范围strategy传不存在的
        result = searceByResType("chapters", cid, "questions", null, null, null, "AAA", null, null, DEFAULT_RELATION_TYPE, DEFAULT_LIMIT, "false", "false");
        Assert.assertNotNull("异常信息返回为空", result);
        Assert.assertEquals(LifeCircleErrorMessageMapper.CoverageStrategyNotExist.getCode(), result.get("code"));
        
        //批量源id查询目标资源
        result = batchQueryResources("chapters", cid, "lessons", null, null, DEFAULT_RELATION_TYPE, DEFAULT_LIMIT);
        Assert.assertNotNull("查询章节和课时的资源关系失败", result);
        Assert.assertNotNull("查询章节和课时的资源关系失败", result.get("items"));
        
        //批量源id查询目标资源-target_type不传
        result = batchQueryResources("chapters", cid, null, null, null, null, DEFAULT_LIMIT);
        Assert.assertNotNull("异常信息返回为空", result);
        Assert.assertEquals(LifeCircleErrorMessageMapper.CheckTargetTypeIsNull.getCode(), result.get("code"));
        
        // 批量源id查询目标资源-source不传
        result = batchQueryResources("chapters", null, "lessons", null, null, DEFAULT_RELATION_TYPE, DEFAULT_LIMIT);
        Assert.assertNotNull("查询章节和课时的资源关系失败", result);
        Assert.assertEquals("查询章节和课时的资源关系失败", new ArrayList<RelationForQueryViewModel>(), result.get("items"));
        
        //源资源不存在
        result = searceByResType("chapters", UUID.randomUUID().toString(), "questions", null, null, null, null, null, null, DEFAULT_RELATION_TYPE, DEFAULT_LIMIT, "false", "false");
        Assert.assertNotNull("查询章节和习题的资源关系失败", result);
        Assert.assertEquals("查询章节和习题的资源关系失败", LifeCircleErrorMessageMapper.SourceResourceNotFond.getCode(), result.get("code"));
        
        /******************************************************************/
        /****************************获取目标资源测试结束************************/
        /*****************************************************************/
        /****************************删除资源关系测试开始***********************/
        /****************************************************************/
        
        // 通过目标资源类型正常删除资源关系
        List<String> targetTypes = new ArrayList<String>();
        targetTypes.add("questions");
        targetTypes.add("lessons");
        result = deleteRelationByTargetType(cid, "chapters", targetTypes, "false");
        Assert.assertNotNull("资源关系批量删除失败", result);
        Assert.assertNotNull("资源关系批量删除失败",  result.get("process_code"));
        Assert.assertEquals("资源关系批量删除失败",
                            LifeCircleErrorMessageMapper.DeleteBatchRelationSuccess.getCode(),
                            (String) result.get("process_code"));
        
        // 通过目标资源类型反转删除资源关系
//        // 创建章节和习题的资源关系
//        result = createRelation("chapters", cid, questionId, DEFAULT_RELATION_TYPE, DEFAULT_LABEL, "questions", 3, true);
//        Assert.assertNotNull("创建章节和习题的资源关系失败", result);
//        Assert.assertNotNull("创建教案和习题的资源关系失败", result.get("identifier"));
        
        targetTypes = new ArrayList<String>();
        targetTypes.add("chapters");
        result = deleteRelationByTargetType(questionId, "questions", targetTypes, "true");
        Assert.assertNotNull("资源关系批量删除失败", result);
        Assert.assertNotNull("资源关系批量删除失败",  result.get("process_code"));
        Assert.assertEquals("资源关系批量删除失败",
                            LifeCircleErrorMessageMapper.DeleteBatchRelationSuccess.getCode(),
                            (String) result.get("process_code"));
        
        //target_type不传
        // 创建章节和习题的资源关系
        result = createRelation("chapters", cid, questionId, DEFAULT_RELATION_TYPE, DEFAULT_LABEL, "questions", 30, true);
        Assert.assertNotNull("创建章节和习题的资源关系失败", result);
        Assert.assertNotNull("创建教案和习题的资源关系失败", result.get("identifier"));
        
        result = deleteRelationByTargetType(cid, "chapters", null, "false");
        Assert.assertNotNull("资源关系批量删除失败", result);
        Assert.assertNotNull("资源关系批量删除失败",  result.get("process_code"));
        Assert.assertEquals("资源关系批量删除失败",
                            LifeCircleErrorMessageMapper.DeleteBatchRelationSuccess.getCode(),
                            (String) result.get("process_code"));
        
        // 通过目标资源id删除资源关系--target_type不存在
        targetTypes = new ArrayList<String>();
        targetTypes.add("assets");
        result = deleteRelationByTargetType(cid, "chapters", targetTypes, "false");
        Assert.assertNotNull("资源关系批量删除失败", result);
        Assert.assertNotNull("资源关系批量删除失败", result.get("process_code"));
        Assert.assertEquals("资源关系批量删除失败",
                            LifeCircleErrorMessageMapper.DeleteBatchRelationSuccess.getCode(),
                            (String) result.get("process_code"));
        
        /******************************************************************/
        /****************************删除资源关系测试结束***********************/
        /*****************************************************************/
        /****************************获取资源关系路径测试开始*******************/
        /****************************************************************/
        
        //创建教材
        result = createTeachingMaterial1();
        Assert.assertNotNull("资源关系教材创建失败", result);
        tmId1 = (String) result.get("identifier");
        Assert.assertNotNull("资源关系教材创建失败", tmId1);
        
        //创建章节
        result = createChapter(tmId1, null);
        Assert.assertNotNull("资源关系源资源章节创建失败", result);
        chapterId1 = (String) result.get("identifier");
        Assert.assertNotNull("资源关系源资源章节创建失败", chapterId1);
        
        // 创建教材
        result = createTeachingMaterial2();
        Assert.assertNotNull("资源关系教材创建失败", result);
        tmId2 = (String) result.get("identifier");
        Assert.assertNotNull("资源关系教材创建失败", tmId2);
        
        // 创建章节
        result = createChapter(tmId2, null);
        Assert.assertNotNull("资源关系源资源章节创建失败", result);
        chapterId2 = (String) result.get("identifier");
        Assert.assertNotNull("资源关系源资源章节创建失败", chapterId2);
        
        //创建课件颗粒
        result = createCoursewareObject();
        Assert.assertNotNull("资源关系课件颗粒创建失败", result);
        cwoId = (String) result.get("identifier");
        Assert.assertNotNull("资源关系课件颗粒创建失败", cwoId);
        
        //创建课件颗粒
        result = createCoursewareObject();
        Assert.assertNotNull("资源关系课件颗粒创建失败", result);
        cwoId1 = (String) result.get("identifier");
        Assert.assertNotNull("资源关系课件颗粒创建失败", cwoId1);
        
        // 创建课件颗粒
        result = createCoursewareObject();
        Assert.assertNotNull("资源关系课件颗粒创建失败", result);
        cwoId2 = (String) result.get("identifier");
        Assert.assertNotNull("资源关系课件颗粒创建失败", cwoId2);
        
        //创建课时
        result = createLesson();
        Assert.assertNotNull("资源关系课时创建失败", result);
        lessonId1 = (String) result.get("identifier");
        Assert.assertNotNull("资源关系课时创建失败", lessonId1);
        
        // 创建课时
        result = createLesson();
        Assert.assertNotNull("资源关系课时创建失败", result);
        lessonId2 = (String) result.get("identifier");
        Assert.assertNotNull("资源关系课时创建失败", lessonId2);
        
        // 测试res_type和课时没有任何关系
        String resStr = getRelationsByConditions("coursewareobjects", cwoId, "false");
        Assert.assertFalse("查询资源关系失败", resStr.contains("title"));
        
        // 创建课件颗粒和课时的资源关系
        result = createRelation("coursewareobjects",
                                cwoId,
                                lessonId,
                                DEFAULT_RELATION_TYPE,
                                DEFAULT_LABEL,
                                "lessons",
                                1,
                                true);
        Assert.assertNotNull("创建课件颗粒和课时的资源关系失败", result);
        relationId = (String) result.get("identifier");
        Assert.assertNotNull("创建课件颗粒和课时的资源关系失败", relationId);

        // 创建课时和课件颗粒的资源关系
        result = createRelation("lessons",
                                lessonId,
                                cwoId1,
                                DEFAULT_RELATION_TYPE,
                                DEFAULT_LABEL,
                                "coursewareobjects",
                                1,
                                true);
        Assert.assertNotNull("创建课时和课件颗粒的资源关系失败", result);
        relationId = (String) result.get("identifier");
        Assert.assertNotNull("创建课时和课件颗粒的资源关系失败", relationId);

        // 测试不存在课时和章节的资源关系
        resStr = getRelationsByConditions("coursewareobjects", cwoId, "false");
        Assert.assertEquals("查询资源关系失败", "[]", resStr);

        // 创建课时和章节的资源关系
//        result = createRelation("lessons", lessonId, subCid, DEFAULT_RELATION_TYPE, DEFAULT_LABEL, "chapters", 1, true);
        result = createRelation("chapters", subCid, lessonId, DEFAULT_RELATION_TYPE, DEFAULT_LABEL, "lessons", 10, true);
        Assert.assertNotNull("创建课时和章节的资源关系失败", result);
        relationId = (String) result.get("identifier");
        Assert.assertNotNull("创建课时和章节的资源关系失败", relationId);

        // 正常测试
        resStr = getRelationsByConditions("coursewareobjects", cwoId1, "true");
        Assert.assertTrue("查询资源关系失败", resStr.contains("title"));

        // 测试反转
        // 创建课时和课件颗粒的资源关系
        result = createRelation("lessons",
                                lessonId,
                                cwoId,
                                DEFAULT_RELATION_TYPE,
                                DEFAULT_LABEL,
                                "coursewareobjects",
                                1,
                                true);
        Assert.assertNotNull("创建课时和课件颗粒的资源关系失败", result);
        relationId = (String) result.get("identifier");
        Assert.assertNotNull("创建课时和课件颗粒的资源关系失败", relationId);

        // 创建章节和课时的资源关系
        result = createRelation("chapters", subCid, lessonId, DEFAULT_RELATION_TYPE, DEFAULT_LABEL, "lessons", 10, true);
        Assert.assertNotNull("创建章节和课时的资源关系失败", result);
        relationId = (String) result.get("identifier");
        Assert.assertNotNull("创建章节和课时的资源关系失败", relationId);

        // 反转查询资源关系路径
        resStr = getRelationsByConditions("coursewareobjects", cwoId, "true");
        Assert.assertTrue("查询资源关系(反转)失败", resStr.contains("title"));

        try {
            // 测试课时删除(不删除关系)
            String uri = "/v0.6/lessons/" + lessonId + "?include=LC,CG";
            resStr = MockUtil.mockGet(mockMvc, uri, null);
            Assert.assertNotNull("获取课时失败", resStr);
            Assert.assertTrue("获取课时失败", resStr.contains("identifier"));
            Lesson lesson = ObjectUtils.fromJson(resStr, Lesson.class);
            lesson.setEnable(false);
            lesson = lessonRepository.update(lesson);
            Assert.assertNotNull("删除课时失败", lesson);
            Assert.assertNotNull("删除课时失败", lesson.getIdentifier());

            // 获取资源关系路径
            resStr = getRelationsByConditions("coursewareobjects", cwoId, "false");
            Assert.assertEquals("查询资源关系失败", "[]", resStr);

            // 测试维度中间有一段没有
            // 创建课时和章节的资源关系
            result = createRelation("lessons",
                                    lessonId1,
                                    chapterId1,
                                    DEFAULT_RELATION_TYPE,
                                    DEFAULT_LABEL,
                                    "chapters",
                                    1,
                                    true);
            Assert.assertNotNull("创建课时和章节的资源关系失败", result);
            relationId = (String) result.get("identifier");
            Assert.assertNotNull("创建课时和章节的资源关系失败", relationId);

            // 创建课件颗粒和课时的资源关系
            result = createRelation("coursewareobjects",
                                    cwoId1,
                                    lessonId1,
                                    DEFAULT_RELATION_TYPE,
                                    DEFAULT_LABEL,
                                    "lessons",
                                    1,
                                    true);
            Assert.assertNotNull("创建课件颗粒和课时的资源关系失败", result);
            relationId = (String) result.get("identifier");
            Assert.assertNotNull("创建课件颗粒和课时的资源关系失败", relationId);

            // 测试维度中间有一段没有
            resStr = getRelationsByConditions("coursewareobjects", cwoId1, "false");
            Assert.assertEquals("查询资源关系失败", "[]", resStr);

            // 创建课时和章节的资源关系
            result = createRelation("lessons",
                                    lessonId2,
                                    chapterId2,
                                    DEFAULT_RELATION_TYPE,
                                    DEFAULT_LABEL,
                                    "chapters",
                                    1,
                                    true);
            Assert.assertNotNull("创建课时和章节的资源关系失败", result);
            relationId = (String) result.get("identifier");
            Assert.assertNotNull("创建课时和章节的资源关系失败", relationId);

            // 创建课件颗粒和课时的资源关系
            result = createRelation("coursewareobjects",
                                    cwoId2,
                                    lessonId2,
                                    DEFAULT_RELATION_TYPE,
                                    DEFAULT_LABEL,
                                    "lessons",
                                    1,
                                    true);
            Assert.assertNotNull("创建课件颗粒和课时的资源关系失败", result);
            relationId = (String) result.get("identifier");
            Assert.assertNotNull("创建课件颗粒和课时的资源关系失败", relationId);

            // 测试父章节不存在
            // 删除父章节
            uri = "/v0.6/teachingmaterials/" + tmId + "/chapters/" + cid;
            resStr = MockUtil.mockGet(mockMvc, uri, null);
            Assert.assertNotNull("获取章节失败", resStr);
            Assert.assertTrue("获取章节失败", resStr.contains("identifier"));
            Chapter chapter = ObjectUtils.fromJson(resStr, Chapter.class);
            chapter.setEnable(false);
            chapter = chapterRepository.update(chapter);
            Assert.assertNotNull("删除章节失败", chapter);
            Assert.assertNotNull("删除章节失败", chapter.getIdentifier());

            // 测试父章节不存在
            resStr = getRelationsByConditions("coursewareobjects", cwoId, "false");
            Assert.assertEquals("查询资源关系失败", "[]", resStr);

            // 测试维度数据错误
            resStr = getRelationsByConditions("coursewareobjects", cwoId2, "false");
            Assert.assertEquals("查询资源关系失败", "[]", resStr);

            // 测试维度没有path
            // 删除教材的分类维度
            ResourceCategory example = new ResourceCategory();
            example.setResource(tmId);
            example.setTaxonpath("K12/$ON020000/$ON020500/$SB0300/$E005000/$E005001");
            categoryRepository.deleteAllByExample(example);
            resStr = getRelationsByConditions("coursewareobjects", cwoId, "false");
            Assert.assertEquals("查询资源关系失败", "[]", resStr);

            // 测试没有分类维度
            // 删除教材的分类维度
            example = new ResourceCategory();
            example.setResource(tmId);
            categoryRepository.deleteAllByExample(example);
            resStr = getRelationsByConditions("coursewareobjects", cwoId, "false");
            Assert.assertEquals("查询资源关系失败", "[]", resStr);

            // 测试教材不存在
            // 删除教材
            uri = "/v0.6/teachingmaterials/" + tmId + "?include=LC,CG";
            resStr = MockUtil.mockGet(mockMvc, uri, null);
            Assert.assertNotNull("获取教材失败", resStr);
            Assert.assertTrue("获取教材失败", resStr.contains("identifier"));
            TeachingMaterial teachingMaterial = ObjectUtils.fromJson(resStr, TeachingMaterial.class);
            teachingMaterial.setEnable(false);
            teachingMaterial = teachingMaterialRepository.update(teachingMaterial);
            Assert.assertNotNull("删除教材失败", teachingMaterial);
            Assert.assertNotNull("删除教材失败", teachingMaterial.getIdentifier());
            resStr = getRelationsByConditions("coursewareobjects", cwoId, "false");
            Assert.assertEquals("查询资源关系失败", "[]", resStr);

            // 测试章节不存在
            // 删除章节
            uri = "/v0.6/teachingmaterials/" + tmId + "/chapters/" + subCid;
            resStr = MockUtil.mockGet(mockMvc, uri, null);
            Assert.assertNotNull("获取章节失败", resStr);
            Assert.assertTrue("获取章节失败", resStr.contains("identifier"));
            chapter = ObjectUtils.fromJson(resStr, Chapter.class);
            chapter.setEnable(false);
            chapter = chapterRepository.update(chapter);
            Assert.assertNotNull("删除章节失败", chapter);
            Assert.assertNotNull("删除章节失败", chapter.getIdentifier());
            resStr = getRelationsByConditions("coursewareobjects", cwoId, "false");
            resStr = MockUtil.mockGet(mockMvc, uri, null);
            Assert.assertEquals("查询资源关系失败", "", resStr);
        } catch (Exception e) {
            LOG.error("资源关系单元测试出错！");
        }
        
        /******************************************************************/
        /****************************获取资源关系路径测试结束********************/
        /*****************************************************************/
        /****************************调整资源关系顺序测试开始*******************/
        /****************************************************************/
        // 创建章节和课时的资源关系
        result = createRelation("chapters", chapterId1, lessonId1, DEFAULT_RELATION_TYPE, "lcms-special-label-dev-test3", "lessons", 10, true);
        Assert.assertNotNull("创建章节和课时的资源关系失败", result);
        String relationId1 = (String) result.get("identifier");
        Assert.assertNotNull("创建章节和课时的资源关系失败", relationId1);
        
        // 创建章节和课时的资源关系
        result = createRelation("chapters", chapterId1, lessonId2, DEFAULT_RELATION_TYPE, "lcms-special-label-dev-test3", "lessons", 20, true);
        Assert.assertNotNull("创建章节和课时的资源关系失败", result);
        String relationId2 = (String) result.get("identifier");
        Assert.assertNotNull("创建章节和课时的资源关系失败", relationId2);
        
        // 创建章节和课件颗粒的资源关系
        result = createRelation("chapters", chapterId1, cwoId, DEFAULT_RELATION_TYPE, DEFAULT_LABEL, "coursewareobjects", 20, true);
        Assert.assertNotNull("创建章节和课件颗粒的资源关系失败", result);
        String relationId3 = (String) result.get("identifier");
        Assert.assertNotNull("创建章节和课件颗粒的资源关系失败", relationId3);
        
        // 测试需要移动的目标对象到两者之间
        resStr = batchAdjustRelationOrder("chapters", chapterId1, relationId2, relationId3, relationId1, "middle");
        Assert.assertEquals("批量修改目标关系的顺序失败", "", resStr);
        
        // 测试需要移动的目标对象到第一个位置
        resStr = batchAdjustRelationOrder("chapters", chapterId1, relationId2, relationId1, "none", "first");
        Assert.assertEquals("批量修改目标关系的顺序失败", "", resStr);
        
        // 测试需要移动的目标对象到第一个位置
        resStr = batchAdjustRelationOrder("chapters", chapterId1, relationId2, relationId1, "none", "last");
        Assert.assertEquals("批量修改目标关系的顺序失败", "", resStr);
        
        //测试需要移动的目标对象不存在
        resStr = batchAdjustRelationOrder("chapters", chapterId1, UUID.randomUUID().toString(), relationId, relationId1, "middle");
        Assert.assertNotNull("批量修改目标关系的顺序失败", resStr);
        result = ObjectUtils.fromJson(resStr, Map.class);
        Assert.assertEquals("批量修改目标关系的顺序失败",
                            LifeCircleErrorMessageMapper.TargetRelationNotExist.getMessage(),
                            result.get("message"));
        //测试目的地的靶心对象不存在
        resStr = batchAdjustRelationOrder("chapters", chapterId1, relationId2, UUID.randomUUID().toString(), relationId1, "middle");
        Assert.assertNotNull("批量修改目标关系的顺序失败", resStr);
        result = ObjectUtils.fromJson(resStr, Map.class);
        Assert.assertEquals("批量修改目标关系的顺序失败",
                            LifeCircleErrorMessageMapper.DestinationRelationNotExist.getMessage(),
                            result.get("message"));
        
        // 测试不合法调整
        resStr = batchAdjustRelationOrder("chapters", chapterId1, relationId2, relationId1, relationId1, "first");
        Assert.assertNotNull("批量修改目标关系的顺序失败", resStr);
        result = ObjectUtils.fromJson(resStr, Map.class);
        Assert.assertEquals("批量修改目标关系的顺序失败",
                            LifeCircleErrorMessageMapper.AdjoinValueError.getMessage(),
                            result.get("message"));
        
        // 测试相邻对象不存在
        resStr = batchAdjustRelationOrder("chapters", chapterId1, relationId2, relationId1, UUID.randomUUID().toString(), "middle");
        Assert.assertNotNull("批量修改目标关系的顺序失败", resStr);
        result = ObjectUtils.fromJson(resStr, Map.class);
        Assert.assertEquals("批量修改目标关系的顺序失败",
                            LifeCircleErrorMessageMapper.AdjoinRelationNotExist.getMessage(),
                            result.get("message"));
        
        // 测试移动的方向不存在
        resStr = batchAdjustRelationOrder("chapters", chapterId1, relationId2, relationId3, relationId1, "wrong_at");
        Assert.assertNotNull("批量修改目标关系的顺序失败", resStr);
        result = ObjectUtils.fromJson(resStr, Map.class);
        Assert.assertEquals("批量修改目标关系的顺序失败",
                            LifeCircleErrorMessageMapper.AtValueError.getMessage(),
                            result.get("message"));
        /******************************************************************/
        /****************************调整资源关系顺序测试结束********************/
        /*****************************************************************/
    }
    
    /**
     * 创建源资源课件颗粒
     * 
     * @return
     * @since
     */
    public Map<String, Object> createCoursewareObject() {
        String uuid = UUID.randomUUID().toString();

        ResourceViewModel resourceViewModel = testCreate("coursewareobjects", uuid, null);
        if (resourceViewModel == null || !uuid.equals(resourceViewModel.getIdentifier())) {
            return null;
        }
        String resStr = null;
        try {
            resStr = objectMapper.writeValueAsString(resourceViewModel);
        } catch (JsonProcessingException e) {
            LOG.error("测试资源关系-课件颗粒创建出错！");
        }
        Map<String, Object> result = ObjectUtils.fromJson(resStr, Map.class);

        return result;
    }
    
    /**
     * 创建教材
     * 
     * @return
     * @since
     */
    public Map<String, Object> createTeachingMaterial() {
        ResourceViewModel resourceViewModel = testCreate("teachingmaterials", null, getCreateTeachingMaterial());
        if (resourceViewModel == null) {
            return null;
        }
        String resStr = null;
        try {
            resStr = objectMapper.writeValueAsString(resourceViewModel);
        } catch (JsonProcessingException e) {
            LOG.error("测试资源关系-教材创建出错！");
        }
        Map<String, Object> result = ObjectUtils.fromJson(resStr, Map.class);

        return result;
    }
    
    /**
     * 创建教材
     * 
     * @return
     * @since
     */
    public Map<String, Object> createTeachingMaterial1() {
        ResourceViewModel resourceViewModel = testCreate("teachingmaterials", null, getCreateTeachingMaterial1());
        if (resourceViewModel == null) {
            return null;
        }
        
        String resStr = null;
        try {
            resStr = objectMapper.writeValueAsString(resourceViewModel);
        } catch (JsonProcessingException e) {
            LOG.error("测试资源关系-教材创建出错！");
        }
        Map<String, Object> result = ObjectUtils.fromJson(resStr, Map.class);

        return result;
    }
    
    /**
     * 创建教材
     * 
     * @return
     * @since
     */
    public Map<String, Object> createTeachingMaterial2() {
        ResourceViewModel resourceViewModel = testCreate("teachingmaterials", null, null);
        if (resourceViewModel == null) {
            return null;
        }
        String resStr = null;
        try {
            resStr = objectMapper.writeValueAsString(resourceViewModel);
        } catch (JsonProcessingException e) {
            LOG.error("测试资源关系-教材创建出错！");
        }
        Map<String, Object> result = ObjectUtils.fromJson(resStr, Map.class);

        return result;
    }
    
    /**
     * 创建章节
     * 
     * @param mid 教材id
     * @return
     * @since
     */
    public Map<String, Object> createChapter(String mid, String chapterId) {
        ChapterViewModel viewModel = getCreateChapterInfo(mid);
        if(StringUtils.isNotEmpty(chapterId)){
            viewModel.setParent(chapterId);
        }
        String uri = "/v0.6/teachingmaterials/" + mid +"/chapters";
        Map<String, Object> result = null;
        try {
            String json = objectMapper.writeValueAsString(viewModel);
            String resStr = MockUtil.mockCreate(mockMvc, uri, json);
            result = ObjectUtils.fromJson(resStr, Map.class);
        } catch (Exception e) {
            LOG.error("测试资源关系-章节创建出错！");
        }

        return result;
    }
    
    /**
     * 创建习题
     * 
     * @return
     * @since
     */
    public Map<String, Object> createQuestions() {
        String uuid = UUID.randomUUID().toString();

        ResourceViewModel resourceViewModel = testCreate("questions", uuid, null);
        if (resourceViewModel == null || !uuid.equals(resourceViewModel.getIdentifier())) {
            return null;
        }
        String resStr = null;
        try {
            resStr = objectMapper.writeValueAsString(resourceViewModel);
        } catch (JsonProcessingException e) {
            LOG.error("测试资源关系-课件颗粒创建出错！");
        }
        Map<String, Object> result = ObjectUtils.fromJson(resStr, Map.class);

        return result;
    }
    
    /**
     * 创建目标资源课时
     * 
     * @return
     * @since
     */
    public Map<String,Object> createLesson(){
        ResourceViewModel resourceViewModel = testCreate("lessons", null, null);
        if (resourceViewModel == null) {
            return null;
        }
        String resStr = null;
        try {
            resStr = objectMapper.writeValueAsString(resourceViewModel);
        } catch (JsonProcessingException e) {
            LOG.error("测试资源关系-课时创建出错！");
        }
        Map<String, Object> result = ObjectUtils.fromJson(resStr, Map.class);

        return result;
    }
    
    /**
     * 创建目标资源教学目标
     * 
     * @return
     * @since
     */
    public Map<String,Object> createInstructionalObjective(String sourceUuid){
        InstructionalObjectiveViewModel viewModel = getCreateInstructionalObjectiveInfo(sourceUuid);
        String uri = "/v0.6/instructionalobjectives";
        Map<String,Object>  result = null;
        try {
            String json = objectMapper.writeValueAsString(viewModel);
            String resStr = MockUtil.mockCreate(mockMvc, uri, json);
            result = ObjectUtils.fromJson(resStr, Map.class);
        }  catch (Exception e) {
            LOG.error("测试资源关系-教学目标创建出错！");
        }
        
        return result;
    }
    
    /**
     * 创建目标资源教学目标
     * 
     * @return
     * @since
     */
    public Map<String,Object> createInstructionalObjectiveWithSameOrderNum(String sourceUuid){
        InstructionalObjectiveViewModel viewModel = getCreateInstructionalObjectiveInfo(sourceUuid);
        List<? extends ResRelationViewModel> relations = viewModel.getRelations();
        for (ResRelationViewModel relation : relations) {
            relation.setOrderNum(1);
        }
        String uri = "/v0.6/instructionalobjectives";
        Map<String,Object>  result = null;
        try {
            String json = objectMapper.writeValueAsString(viewModel);
            String resStr = MockUtil.mockCreate(mockMvc, uri, json);
            result = ObjectUtils.fromJson(resStr, Map.class);
        }  catch (Exception e) {
            LOG.error("测试资源关系-教学目标创建出错！");
        }
        
        return result;
    }
    
    /**
     * 创建资源关系
     * 
     * @param resType
     * @param source
     * @param target
     * @param label
     * @param targetType
     * @param orderNum
     * @return
     * @since
     */
    public Map<String, Object> createRelation(String resType,
                                              String source,
                                              String target,
                                              String relationType,
                                              String label,
                                              String targetType,
                                              Integer orderNum,
                                              boolean haveLifecycle) {
        EducationRelationViewModel viewModel = getEducationRelationViewModel(target,
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
            LOG.error("测试资源关系-资源关系创建出错！");
        }

        return result;
    }
    
    /**
     * 修改资源关系
     * 
     * @param resType
     * @param source
     * @param target
     * @param label
     * @param targetType
     * @param orderNum
     * @return
     * @since
     */
    public Map<String, Object> updateRelation(String rid,
                                              String resType,
                                              String source,
                                              String relationType,
                                              String label,
                                              Integer orderNum,
                                              boolean haveLifecycle,
                                              boolean haveTags) {
        EducationRelationViewModel viewModel = getUpdateEducationRelationViewModel(relationType, label, orderNum, haveLifecycle, haveTags);
        String uri = "/v0.6/" + resType + "/" + source + "/relations/" + rid;
        Map<String, Object> result = null;
        try {
            String json = objectMapper.writeValueAsString(viewModel);
            String resStr = MockUtil.mockPut(mockMvc, uri, json);
            result = ObjectUtils.fromJson(resStr, Map.class);
        } catch (Exception e) {
            LOG.error("测试资源关系-资源关系修改出错！");
        }

        return result;
    }
    
    /**
     * 通过关系id删除资源关系
     * 
     * @param rid
     * @param resType
     * @param source
     * @return
     * @since
     */
    public Map<String, Object> deleteRelation(String rid,
                                              String resType,
                                              String source) {
        String uri = "/v0.6/" + resType + "/" +source + "/relations/" + rid;
        Map<String, Object> result = null;
        try {
            String resStr = MockUtil.mockDelete(mockMvc, uri, null);
            result = ObjectUtils.fromJson(resStr, Map.class);
        } catch (Exception e) {
            LOG.error("测试资源关系-资源关系删除出错！");
        }

        return result;
    }
    
    /**
     * 通过目标资源id删除资源关系
     * 
     * @param source
     * @param resType
     * @param target
     * @param reverse
     * @return
     * @since
     */
    public Map<String, Object> deleteRelationByTarget(String source,
                                              String resType,
                                              String target,
                                              String reverse) {
        String uri = "/v0.6/" + resType + "/" + source + "/relations?target=";
        if (StringUtils.isNotEmpty(target)) {
            uri += target;
        }
        uri += "&relation_type=ASSOCIATE&reverse=" + reverse;
        
        Map<String, Object> result = null;
        try {
            String resStr = MockUtil.mockDelete(mockMvc, uri, null);
            result = ObjectUtils.fromJson(resStr, Map.class);
        } catch (Exception e) {
            LOG.error("测试资源关系-资源关系删除出错！");
        }

        return result;
    }
    
    /**
     * 通过目标资源类型删除资源关系
     * 
     * @param source
     * @param resType
     * @param target
     * @param reverse
     * @return
     * @since
     */
    public Map<String, Object> deleteRelationByTargetType(String source,
                                              String resType,
                                              List<String> targetTypes,
                                              String reverse) {
        String uri = "/v0.6/" + resType + "/" + source + "/relations?";
        if(CollectionUtils.isNotEmpty(targetTypes)) {
            for (String targetType : targetTypes) {
                uri += "target_type=" + targetType + "&";
            }
        } else {
            uri += "target_type=";
        }
        uri += "&relation_type=ASSOCIATE&reverse=" + reverse;
        
        Map<String, Object> result = null;
        try {
            String resStr = MockUtil.mockDelete(mockMvc, uri, null);
            result = ObjectUtils.fromJson(resStr, Map.class);
        } catch (Exception e) {
            LOG.error("测试资源关系-资源关系删除出错！");
        }

        return result;
    }
    
    /**
     * 获取目标资源
     * 
     * @param resType
     * @param source
     * @param targetType
     * @param categories
     * @param ctType
     * @param ctTarget
     * @param ct
     * @param label
     * @param relationTags
     * @param relationType
     * @param limit
     * @param reverse
     * @return
     * @since
     */
    public Map<String, Object> searceByResType(String resType,
                                               String source,
                                               String targetType,
                                               String categories,
                                               String ctType,
                                               String ctTarget,
                                               String ct,
                                               String label,
                                               String relationTags,
                                               String relationType,
                                               String limit,
                                               String reverse,
                                               String recursion) {
        String uri = "/v0.6/" + resType + "/" + source + "/targets?target_type=";
        if (StringUtils.isNotEmpty(targetType)) {
            uri += targetType;
        }

        uri += "&categories=";
        if (StringUtils.isNotEmpty(categories)) {
            uri += categories;
        }

        uri += "&ct_type=";
        if (StringUtils.isNotEmpty(ctType)) {
            uri += ctType;
        }

        uri += "&ct_target=";
        if (StringUtils.isNotEmpty(ctTarget)) {
            uri += ctTarget;
        }

        uri += "&ct=";
        if (StringUtils.isNotEmpty(ct)) {
            uri += ct;
        }

        uri += "&label=";
        if (StringUtils.isNotEmpty(label)) {
            uri += label;
        }

        uri += "&relation_tags=";
        if (StringUtils.isNotEmpty(relationTags)) {
            uri += relationTags;
        }

        uri += "&relation_type=";
        if (StringUtils.isNotEmpty(relationType)) {
            uri += relationType;
        }

        uri += "&reverse=" + reverse + "&recursion=" + recursion + "&limit=" + limit;

        Map<String, Object> result = null;
        try {
            String resStr = MockUtil.mockGet(mockMvc, uri, null);
            result = ObjectUtils.fromJson(resStr, Map.class);
        } catch (Exception e) {
            LOG.error("测试资源关系-资源关系获取目标资源出错！");
        }

        return result;
    }
    
    /**
     * 获取资源关系路径
     * 
     * @param resType
     * @param source
     * @param reverse
     * @return
     * @since
     */
    public String getRelationsByConditions(String resType,
                             String source,
                             String reverse) {
        String uri = "/v0.6/" + resType + "/" + source + "/relations?relation_path=lessons/teachingmaterials&category_pattern=K12&reverse=";
        uri += reverse;
        
        String resStr = null;
        try {
            resStr = MockUtil.mockGet(mockMvc, uri, null);
        } catch (Exception e) {
            LOG.error("测试资源关系-获取资源关系路径出错！");
        }

        return resStr;
    }
    
    /**
     * 调整关系顺序
     * 
     * @param resType
     * @param source
     * @param targetRelation
     * @param destinationRelation
     * @param adjoinRelation
     * @param at
     * @return
     * @since
     */
    public String batchAdjustRelationOrder(String resType,
                             String source,
                             String targetRelation,
                             String destinationRelation,
                             String adjoinRelation,
                             String at) {
        String uri = "/v0.6/" + resType + "/" + source + "/relations/order";
        List<BatchAdjustRelationOrderModel> viewModel = getUpdateBatchAdjustRelationOrderModel(targetRelation,
                                                                                               destinationRelation,
                                                                                               adjoinRelation,
                                                                                               at);

        String resStr = null;
        try {
            String json = objectMapper.writeValueAsString(viewModel);
            resStr = MockUtil.mockPut(mockMvc, uri, json);
        } catch (Exception e) {
            LOG.error("测试资源关系-调整关系的顺序出错！");
        }

        return resStr;
    }
    
    /**
     * 批量获取目标资源
     * 
     * @param resType
     * @param sids
     * @param targetType
     * @param label
     * @param tags
     * @param relationType
     * @param limit
     * @return
     * @since
     */
    public Map<String, Object> batchQueryResources(String resType,
                                                   String source,
                                                   String targetType,
                                                   String label,
                                                   String relationTags,
                                                   String relationType,
                                                   String limit) {
        String uri = "/v0.6/" + resType + "/resources/relations/targets/bulk?sid=";
        if (StringUtils.isNotEmpty(source)) {
            uri += source;
        }

        uri += "&target_type=";
        if (StringUtils.isNotEmpty(targetType)) {
            uri += targetType;
        }

        uri += "&label=";
        if (StringUtils.isNotEmpty(label)) {
            uri += label;
        }

        uri += "&relation_tags=";
        if (StringUtils.isNotEmpty(relationTags)) {
            uri += relationTags;
        }

        uri += "&relation_type=";
        if (StringUtils.isNotEmpty(relationType)) {
            uri += relationType;
        }

        uri += "&reverse=false" + "&limit=" + limit;

        Map<String, Object> result = null;
        try {
            String resStr = MockUtil.mockGet(mockMvc, uri, null);
            result = ObjectUtils.fromJson(resStr, Map.class);
        } catch (Exception e) {
            LOG.error("测试资源关系-资源关系获取目标资源出错！");
        }

        return result;
    }
    
    /**
     * 构造model
     * 
     * @param target
     * @param destination
     * @param adjoin
     * @param at
     * @return
     * @since
     */
    public List<BatchAdjustRelationOrderModel> getUpdateBatchAdjustRelationOrderModel(String target,
                                                                                      String destination,
                                                                                      String adjoin,
                                                                                      String at) {
        List<BatchAdjustRelationOrderModel> batchAdjustRelationOrderModels = new ArrayList<BatchAdjustRelationOrderModel>();
        BatchAdjustRelationOrderModel batchAdjustRelationOrderModel = new BatchAdjustRelationOrderModel();
        batchAdjustRelationOrderModel.setAdjoin(adjoin);
        batchAdjustRelationOrderModel.setAt(at);
        batchAdjustRelationOrderModel.setDestination(destination);
        batchAdjustRelationOrderModel.setTarget(target);

        batchAdjustRelationOrderModels.add(batchAdjustRelationOrderModel);

        return batchAdjustRelationOrderModels;
    }
    
    /**
     * 构造一个用于创建资源关系的viewModel
     * 
     * @return
     * @since
     */
    public static EducationRelationViewModel getEducationRelationViewModel(String target,
                                                                           String relationType,
                                                                           String label,
                                                                           String targetType,
                                                                           Integer orderNum,
                                                                           boolean haveLifecycle) {
        EducationRelationViewModel viewModel = new EducationRelationViewModel();
        viewModel.setTarget(target);
        viewModel.setRelationType(relationType);
        viewModel.setLabel(label);
        List<String> tags = new ArrayList<String>();
        tags.add("lcms-special-tag-dev-test1");
        tags.add("lcms-special-tag-dev-test2");
        viewModel.setTags(tags);
        viewModel.setOrderNum(orderNum);
        viewModel.setResourceTargetType(targetType);

        // 生命周期
        if (haveLifecycle) {
            EducationRelationLifeCycleViewModel lifeCycleViewModel = new EducationRelationLifeCycleViewModel();
            lifeCycleViewModel.setCreator("lcms-special-creator-dev-test");
            lifeCycleViewModel.setEnable(true);
            lifeCycleViewModel.setStatus("AUDIT_WAITING");
            viewModel.setLifeCycle(lifeCycleViewModel);
        } else {
            viewModel.setLifeCycle(null);
        }

        return viewModel;
    }
    
    /**
     * 构造一个用于修改资源关系的viewModel
     * 
     * @return
     * @since
     */
    public EducationRelationViewModel getUpdateEducationRelationViewModel(String relationType,
                                                                          String label,
                                                                          Integer orderNum,
                                                                          boolean haveLifecycle,
                                                                          boolean haveTags) {
        EducationRelationViewModel viewModel = new EducationRelationViewModel();
        viewModel.setRelationType(relationType);
        viewModel.setLabel(label);
        if(haveTags){
            List<String> tags = new ArrayList<String>();
            tags.add("tag_test_update1");
            tags.add("tag_test_update2");
            viewModel.setTags(tags);
        } else {
            viewModel.setTags(null);
        }
        viewModel.setOrderNum(orderNum);
        
        // 生命周期
        if (haveLifecycle) {
            EducationRelationLifeCycleViewModel lifeCycleViewModel = new EducationRelationLifeCycleViewModel();
            lifeCycleViewModel.setStatus("AUDIT_WAITING");
            viewModel.setLifeCycle(lifeCycleViewModel);
        } else {
            viewModel.setLifeCycle(null);
        }
        
        return viewModel;
    }
    
    /**
     * 拼装教材创建的入参报文
     * @return
     */
    public static String getCreateTeachingMaterial(){
        String json = "{"+
                  "\"title\": \"LC单元测试默认用例\","+
                  "\"description\": \"lcms-special-description-dev-test\","+
                  "\"language\": \"zh_CN\","+
                  "\"keywords\": [\"教材\",\"数学\"],"+
                  "\"tags\": [\"教材\",\"数学\"],"+
                  "\"preview\": {"+
                  "   \"png\": \"{ref_path}/edu/esp/preview/123.png\""+
                  "},"+
                  "\"ext_properties\":{"+
                  "    \"isbn\": \"ISBN-10 4-88888-913-9\","+
                  "    \"attachments\":[\"http://service.edu.nd.com.cn/library/assets/100013\"],"+
                  "    \"criterion\": \"《XXX课标》\""+
                  "},"+
                  "\"life_cycle\":{"+
                  "    \"version\": \"v0.2\","+
                  "    \"status\": \"INIT\","+
                  "    \"enable\":\"true\","+
                  "    \"creator\": \"johnny\","+
                  "    \"publisher\": \"publisherValue\","+
                  "    \"provider\":\"NetDragon Inc.\","+
                  "    \"provider_source\":\"\""+
                  "},"+
                  "\"education_info\":{"+
                  "    \"interactivity\":\"2\","+
                  "    \"interactivity_level\":\"2\","+
                  "    \"end_user_type\":\"教师，管理者\","+
                  "    \"semantic_density\":\"1\","+
                  "    \"context\":\"基础教育\","+
                  "    \"age_range\":\"7岁以上\","+
                  "    \"difficulty\":\"easy\","+
                  "    \"learning_time\":\"P0Y0M0DT3H0M\","+
                  "    \"description\":{\"zh_CN\":\"lcms-special-description-dev-test\"},"+
                  "    \"language\":\"zh_CN\""+
                  "},"+
                  "\"copyright\":{"+
                  "    \"right\":\"zh\","+
                  "    \"description\":\"lcms-special-description-dev-test\","+
                  "    \"author\":\"johnny\""+
                  "},"+
                  "\"coverages\":["+
                  "    {"+
                  "        \"target_type\":\"User\","+
                  "        \"target\":\"890399\","+
                  "        \"target_title\":\"LC单元测试默认用例\","+
                  "        \"strategy\":\"OWNER\""+
                  "    }"+
                  "], "+
                  "\"categories\":{"+
                  "    \"phase\":["+
                  "        {"+
                  "        \"taxonpath\":\"K12/$ON020000/$ON020500/$SB0300/$E005000/$E005001\","+
                  "        \"taxoncode\":\"$ON020000\""+
                  "        }"+
                  "    ]"+
                  "}"+
             "}";

        
        return json;
    }
    
    /**
     * 拼装教材创建的入参报文
     * @return
     */
    public static String getCreateTeachingMaterial1(){
        String json = "{"+
                "\"title\": \"LC单元测试默认用例\","+
                "\"description\": \"lcms-special-description-dev-test\","+
                "\"language\": \"zh_CN\","+
                "\"keywords\": [\"教材\",\"数学\"],"+
                "\"tags\": [\"教材\",\"数学\"],"+
                "\"preview\": {"+
                "   \"png\": \"{ref_path}/edu/esp/preview/123.png\""+
                "},"+
                "\"ext_properties\":{"+
                "    \"isbn\": \"ISBN-10 4-88888-913-9\","+
                "    \"attachments\":[\"http://service.edu.nd.com.cn/library/assets/100013\"],"+
                "    \"criterion\": \"《XXX课标》\""+
                "},"+
                "\"life_cycle\":{"+
                "    \"version\": \"v0.2\","+
                "    \"status\": \"INIT\","+
                "    \"enable\":\"true\","+
                "    \"creator\": \"johnny\","+
                "    \"publisher\": \"publisherValue\","+
                "    \"provider\":\"NetDragon Inc.\","+
                "    \"provider_source\":\"\""+
                "},"+
                "\"education_info\":{"+
                "    \"interactivity\":\"2\","+
                "    \"interactivity_level\":\"2\","+
                "    \"end_user_type\":\"教师，管理者\","+
                "    \"semantic_density\":\"1\","+
                "    \"context\":\"基础教育\","+
                "    \"age_range\":\"7岁以上\","+
                "    \"difficulty\":\"easy\","+
                "    \"learning_time\":\"P0Y0M0DT3H0M\","+
                "    \"description\":{\"zh_CN\":\"lcms-special-description-dev-test\"},"+
                "    \"language\":\"zh_CN\""+
                "},"+
                "\"copyright\":{"+
                "    \"right\":\"zh\","+
                "    \"description\":\"lcms-special-description-dev-test\","+
                "    \"author\":\"johnny\""+
                "},"+
                "\"coverages\":["+
                "    {"+
                "        \"target_type\":\"User\","+
                "        \"target\":\"890399\","+
                "        \"target_title\":\"LC单元测试默认用例\","+
                "        \"strategy\":\"OWNER\""+
                "    }"+
                "], "+
                "\"categories\":{"+
                "    \"phase\":["+
                "        {"+
                "        \"taxonpath\":\"K12/$ON020000/$ON020500//$E005000/$E005001\","+
                "        \"taxoncode\":\"$ON020000\""+
                "        }"+
                "    ]"+
                "}"+
           "}";

        
        return json;
    }
    
    public static ChapterViewModel getCreateChapterInfo(String mid) {
        ChapterViewModel viewModel = new ChapterViewModel();
        viewModel.setTitle("LC单元测试默认用例");
        viewModel.setDescription("lcms-special-description-dev-test");
        viewModel.setParent(mid);
        return viewModel;
    }
    
    /**
     * 拼装教学目标创建的入参报文
     * 
     * @return
     */
    public static InstructionalObjectiveViewModel getCreateInstructionalObjectiveInfo(String sourceUuid) {
        InstructionalObjectiveViewModel viewModel = new InstructionalObjectiveViewModel();
        viewModel.setTitle("LC单元测试默认用例");
        viewModel.setDescription("lcms-special-description-dev-test");
        viewModel.setLanguage("zh_cn");
        List<String> keywords = new ArrayList<String>();
        keywords.add("方程");
        keywords.add("一元一次");
        viewModel.setKeywords(keywords);
        List<String> tags = new ArrayList<String>();
        tags.add("方程");
        tags.add("one element and square");
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
        path.setTaxonpath("");
        path.setTaxoncode("OTK0000");
        value.add(path);
        categories.put("objectivetypes", value);
        viewModel.setCategories(categories);
        
        List<ResRelationViewModel> relations = new ArrayList<ResRelationViewModel>();
        ResRelationViewModel relationViewModel = new ResRelationViewModel();
        relationViewModel.setSource(sourceUuid);
        relationViewModel.setSourceType("lessons");
        relationViewModel.setRelationType("ASSOCIATE");
        relationViewModel.setTags(tags);
        relationViewModel.setLabel("resource_label_test1");
        relationViewModel.setEnable(true);
        relations.add(relationViewModel);
        relationViewModel = new ResRelationViewModel();
        relationViewModel.setSource(sourceUuid);
        relationViewModel.setSourceType("lessons");
        relationViewModel.setRelationType("ASSOCIATE");
        relationViewModel.setTags(tags);
        relationViewModel.setLabel("resource_label_test2");
        relationViewModel.setOrderNum(1);
        relationViewModel.setEnable(true);
        relations.add(relationViewModel);
        relationViewModel = new ResRelationViewModel();
        relationViewModel.setSource(sourceUuid);
        relationViewModel.setSourceType("lessons");
        relationViewModel.setRelationType("ASSOCIATE");
        relationViewModel.setTags(tags);
        relationViewModel.setLabel("resource_label_test3");
        relationViewModel.setOrderNum(2);
        relationViewModel.setEnable(true);
        relations.add(relationViewModel);
        relationViewModel = new ResRelationViewModel();
        relationViewModel.setSource(sourceUuid);
        relationViewModel.setSourceType("lessons");
        relationViewModel.setRelationType("ASSOCIATE");
        relationViewModel.setTags(tags);
        relationViewModel.setLabel("resource_label_test4");
        relationViewModel.setEnable(true);
        relations.add(relationViewModel);
        relationViewModel = new ResRelationViewModel();
        relationViewModel.setSource(sourceUuid);
        relationViewModel.setSourceType("lessons");
        relationViewModel.setRelationType("ASSOCIATE");
        relationViewModel.setTags(tags);
        relationViewModel.setLabel("resource_label_test5");
        relationViewModel.setEnable(true);
        relations.add(relationViewModel);
        viewModel.setRelations(relations);

        return viewModel;
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

}
