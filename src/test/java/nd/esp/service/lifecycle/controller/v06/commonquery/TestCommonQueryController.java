package nd.esp.service.lifecycle.controller.v06.commonquery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;
import nd.esp.service.lifecycle.impl.SimpleJunitTest4ResourceImpl;
import nd.esp.service.lifecycle.support.Constant;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.vos.ListViewModel;
import nd.esp.service.lifecycle.vos.chapters.v06.ChapterViewModel;
import nd.esp.service.lifecycle.vos.educationrelation.v06.EducationRelationLifeCycleViewModel;
import nd.esp.service.lifecycle.vos.educationrelation.v06.EducationRelationViewModel;
import nd.esp.service.lifecycle.vos.statics.CoverageConstant;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nd.gaea.rest.testconfig.MockUtil;

public class TestCommonQueryController extends SimpleJunitTest4ResourceImpl{
	private static final Logger logger = LoggerFactory.getLogger(TestCommonQueryController.class);
    private final static Map<String, String> RESOURCE_MAP = new HashMap<String, String>();
    private final static Map<String, String> CHAPTER_MAP = new HashMap<String, String>();
    
    private final static String DEFAULT_INCLUDE = "TI,CG,EDU,LC,CR";
    private final static String DEFAULT_LIMIT = "(0,20)";
    private final static String URL_TYPE_SEARCH = "search";
    private final static String URL_TYPE_QUERY = "query";
    //测试智能出题的章节id
    private final static String CHAPTER_ID_EMPTY = "0023b435-6fe6-455e-ac5b-f814e687183f";
    private final static String CHAPTER_ID = "42ae31d7-d20c-4cde-9524-969fc1178408";
    
//    @Test
    public void testAll(){
    	//创建一个教材
        ResourceViewModel tmVm = testCreate("teachingmaterials",null,null);
        String tmId = tmVm.getIdentifier();
        RESOURCE_MAP.put(tmId, "teachingmaterials");
        //创建一个章节
        ChapterViewModel chapterVm = 
        		createChapter(tmId, "TestCommonQueryController", DERAULT_DESCRIPTION, tmId, "", "");
        String chapterId = chapterVm.getIdentifier();
        CHAPTER_MAP.put(chapterId, tmId);
    	
    	//1.a 智能出题正常查询(返回404)
    	String relation = "chapters/"+chapterId+"/ASSOCIATE";
    	ListViewModel<ResourceViewModel> list4Success = testQueryIntelliKnowledge("questions", DEFAULT_INCLUDE, relation, DEFAULT_LIMIT);
    	Assert.assertNotNull("智能出题查询失败", list4Success.getItems());
    	//1.b 智能出题查询-但resType不为questions
    	ListViewModel<ResourceViewModel> list4Error = testQueryIntelliKnowledge("assets", DEFAULT_INCLUDE, relation, DEFAULT_LIMIT);
    	Assert.assertNull("智能出题查询-但resType不为questions却查询成功", list4Error.getItems());
    	//1.c 智能出题查询-但relation为空
    	list4Error = testQueryIntelliKnowledge("questions", DEFAULT_INCLUDE, "", DEFAULT_LIMIT);
    	Assert.assertNull("智能出题查询-但relation为空却查询成功", list4Error.getItems());
    	//1.d 智能出题查询-但relation有多个
    	relation += "&relation=chapters//";
    	list4Error = testQueryIntelliKnowledge("questions", DEFAULT_INCLUDE, relation, DEFAULT_LIMIT);
    	Assert.assertNull("智能出题查询-但relation有多个却查询成功", list4Error.getItems());
    	//1.e 智能出题查询-但relation格式错误
    	relation = "chapters/"+chapterId;
    	list4Error = testQueryIntelliKnowledge("questions", DEFAULT_INCLUDE, relation, DEFAULT_LIMIT);
    	Assert.assertNull("智能出题查询-但relation格式错误却查询成功", list4Error.getItems());
    	//1.f 智能出题正常查询(返回空)
    	relation = "chapters/"+CHAPTER_ID_EMPTY+"/ASSOCIATE";
    	list4Success = testQueryIntelliKnowledge("questions", DEFAULT_INCLUDE, relation, DEFAULT_LIMIT);
    	Assert.assertNotNull("智能出题查询失败", list4Success.getItems());
    	//1.g 智能出题正常查询(有值)
    	relation = "chapters/"+CHAPTER_ID+"/ASSOCIATE";
    	list4Success = testQueryIntelliKnowledge("questions", DEFAULT_INCLUDE, relation, DEFAULT_LIMIT);
    	Assert.assertNotNull("智能出题查询失败", list4Success.getItems());
    	//1.h 智能出题正常查询(有值),include不传
    	relation = "chapters/"+CHAPTER_ID+"/ASSOCIATE";
    	list4Success = testQueryIntelliKnowledge("questions", "", relation, DEFAULT_LIMIT);
    	Assert.assertNotNull("智能出题查询失败", list4Success.getItems());
    	
        //2.resType=chapters
    	list4Error = testCommonQuery(false, URL_TYPE_SEARCH, "chapters", "", "", "", "", "", "", "", "", "&words=", DEFAULT_LIMIT, "");
        Assert.assertNull("resType=chapters但查询成功", list4Error.getItems());
        //3.resType=eduresource但rescode为空
        list4Error = testCommonQuery(false, URL_TYPE_QUERY, Constant.RESTYPE_EDURESOURCE, "", "", "", "", "", "", "", "", "&words=", DEFAULT_LIMIT, "");
        Assert.assertNull("resType=eduresource但rescode为空却查询成功", list4Error.getItems());
        //4.include="ABC"
        list4Error = testCommonQuery(false, URL_TYPE_QUERY, "assets", "", "&include=ABC", "", "", "", "", "", "", "&words=", DEFAULT_LIMIT, "");
        Assert.assertNull("include值非法却查询成功", list4Error.getItems());
        //5.relation格式错误
        list4Error = testCommonQuery(false, URL_TYPE_QUERY, "assets", "", "", "", "", "&relation=chapters/111", "", "", "", "&words=", DEFAULT_LIMIT, "");
        Assert.assertNull("relation格式错误却查询成功", list4Error.getItems());
        //6.coverage格式错误
        list4Error = testCommonQuery(false, URL_TYPE_QUERY, "assets", "", DEFAULT_INCLUDE, "", "", "", "&coverage=Org/nd", "", "", "&words=", DEFAULT_LIMIT, "");
        Assert.assertNull("coverage格式错误却查询成功", list4Error.getItems());
        
    }
    
    @After
    public void after(){
    	if(CollectionUtils.isNotEmpty(CHAPTER_MAP)){
            for(String id : CHAPTER_MAP.keySet()){
            	deleteChapter(CHAPTER_MAP.get(id), id);
            }
        }
    	
        if(CollectionUtils.isNotEmpty(RESOURCE_MAP)){
            for(String id : RESOURCE_MAP.keySet()){
                testDelete(RESOURCE_MAP.get(id), id);
            }
        }
    }
    
    /**
     * 通用查询 - 通用方法
     * @param isManagement
     * @param urlType
     */
    private ListViewModel<ResourceViewModel> testCommonQuery(boolean isManagement,String urlType,String resType,String resCode,String includes,
    		String categories,String categoryExclude,String relations,String coverages,String props,
    		String orderBy,String words,String limit,String reverse){
		//URL拼接
    	StringBuilder uri = new StringBuilder();
		uri.append("/v0.6/");
		uri.append(resType);
		if(isManagement){
			uri.append("/management");
		}
		uri.append("/actions/");
		uri.append(urlType);
		uri.append("?limit=");
		uri.append(limit);
		uri.append(words);
		uri.append(includes);
		uri.append(categories);
		uri.append(categoryExclude);
		uri.append(relations);
		uri.append(coverages);
		uri.append(props);
		uri.append(orderBy);
		uri.append(reverse);
		
		String resStr = "";
        try {
            resStr = MockUtil.mockGet(mockMvc, uri.toString(), null);
        } catch (Exception e) {
            logger.error("testQueryIntelliKnowledge error",e);
        }
    	
        ListViewModel<ResourceViewModel> listViewModel = fromJson(resStr, ListViewModel.class);
    	return listViewModel;
    }
    
    
    /**
     * 通用查询--查询智能出题
     */
	private ListViewModel<ResourceViewModel> testQueryIntelliKnowledge(
    		String resType,String includes,String relations,String limit){
		String uri = "/v0.6/" + resType + "/actions/search?include=" + includes
				+ "&relation=" + relations + "&coverage=" + CoverageConstant.INTELLI_KNOWLEDGE_COVERAGE 
				+ "&words=&limit=" + limit;		
		String resStr = "";
        try {
            resStr = MockUtil.mockGet(mockMvc, uri, null);
        } catch (Exception e) {
            logger.error("testQueryIntelliKnowledge error",e);
        }
        
        ListViewModel<ResourceViewModel> listViewModel = fromJson(resStr, ListViewModel.class);
    	return listViewModel;
    }
    
    /**
     * 创建章节
     */
    private ChapterViewModel createChapter(
    		String tmId,String title,String description,String parent,String target,String direction){
    	ChapterViewModel chapter4Create = new ChapterViewModel();
    	chapter4Create.setTitle(title);
    	chapter4Create.setDescription(description);
    	chapter4Create.setParent(parent);
    	chapter4Create.setTarget(target);
    	chapter4Create.setDirection(direction);
		
    	String uri = "/v0.6/teachingmaterials/" + tmId + "/chapters";
    	String json = toJson(chapter4Create);
    	String resStr = "";
    	try {
            resStr = MockUtil.mockPost(mockMvc, uri, json);
        } catch (Exception e) {
            logger.error("createChapter error",e);
        }
    	chapter4Create = fromJson(resStr, ChapterViewModel.class);
    	return chapter4Create;
    }
    
    /**
     * 删除章节
     */
    private void deleteChapter(String tmId,String chapterId){
    	String uri = "/v0.6/teachingmaterials/" + tmId + "/chapters/" + chapterId;
    	try {
            MockUtil.mockDelete(mockMvc, uri, null);
        } catch (Exception e) {
            logger.error("deleteChapter error",e);
        }
    }
    
    /**
     * 创建关系
     */
    @SuppressWarnings("unused")
	private EducationRelationViewModel createRelation(String resType,String resId,String targetType,String targetId,Integer orderNum){
		String uri = "/v0.6/" + resType + "/" + resId + "/relations";
		String json = toJson(getEducationRelationViewModel(
						targetId, "ASSOCIATE", "common query label", targetType, orderNum, false));
		String resStr = "";
		try {
            resStr = MockUtil.mockPost(mockMvc, uri, json);
        } catch (Exception e) {
            logger.error("createChapter error",e);
        }
		EducationRelationViewModel ervm = fromJson(resStr, EducationRelationViewModel.class);
		
    	return ervm;
    }
    
    /**
     * 创建资源关系的model
     */
	private EducationRelationViewModel getEducationRelationViewModel(
			String target, String relationType, String label,
			String targetType, Integer orderNum, boolean haveLifecycle) {
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
}
