/* =============================================================
 * Created: [2015年7月2日] by linsm
 * =============================================================
 *
 * Copyright 2014-2015 NetDragon Websoft Inc. All Rights Reserved
 *
 * =============================================================
 */

package nd.esp.service.lifecycle.educommon.services.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import nd.esp.service.lifecycle.educommon.dao.NDResourceDao;
import nd.esp.service.lifecycle.educommon.models.ResourceModel;
import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;
import nd.esp.service.lifecycle.models.courseware.v06.CoursewareModel;
import nd.esp.service.lifecycle.models.teachingmaterial.v06.TeachingMaterialModel;
import nd.esp.service.lifecycle.models.v06.AssetModel;
import nd.esp.service.lifecycle.models.v06.CourseWareObjectModel;
import nd.esp.service.lifecycle.models.v06.CourseWareObjectTemplateModel;
import nd.esp.service.lifecycle.models.v06.EbookModel;
import nd.esp.service.lifecycle.models.v06.ExaminationPaperModel;
import nd.esp.service.lifecycle.models.v06.HomeworkModel;
import nd.esp.service.lifecycle.models.v06.InstructionalObjectiveModel;
import nd.esp.service.lifecycle.models.v06.KnowledgeBaseModel;
import nd.esp.service.lifecycle.models.v06.KnowledgeModel;
import nd.esp.service.lifecycle.models.v06.LearningPlanModel;
import nd.esp.service.lifecycle.models.v06.LessonModel;
import nd.esp.service.lifecycle.models.v06.LessonPlanModel;
import nd.esp.service.lifecycle.models.v06.QuestionModel;
import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.Asset;
import nd.esp.service.lifecycle.repository.model.Category;
import nd.esp.service.lifecycle.repository.model.CategoryData;
import nd.esp.service.lifecycle.repository.model.Chapter;
import nd.esp.service.lifecycle.repository.model.Courseware;
import nd.esp.service.lifecycle.repository.model.CoursewareObject;
import nd.esp.service.lifecycle.repository.model.CoursewareObjectTemplate;
import nd.esp.service.lifecycle.repository.model.Ebook;
import nd.esp.service.lifecycle.repository.model.ExaminationPaper;
import nd.esp.service.lifecycle.repository.model.HomeWork;
import nd.esp.service.lifecycle.repository.model.InstructionalObjective;
import nd.esp.service.lifecycle.repository.model.KnowledgeBase;
import nd.esp.service.lifecycle.repository.model.LearningPlan;
import nd.esp.service.lifecycle.repository.model.Lesson;
import nd.esp.service.lifecycle.repository.model.LessonPlan;
import nd.esp.service.lifecycle.repository.model.Question;
import nd.esp.service.lifecycle.repository.model.ResourceCategory;
import nd.esp.service.lifecycle.repository.model.TeachingActivities;
import nd.esp.service.lifecycle.repository.model.TeachingMaterial;
import nd.esp.service.lifecycle.repository.sdk.AssetRepository;
import nd.esp.service.lifecycle.repository.sdk.CategoryRepository;
import nd.esp.service.lifecycle.repository.sdk.ChapterRepository;
import nd.esp.service.lifecycle.repository.sdk.CoursewareObjectRepository;
import nd.esp.service.lifecycle.repository.sdk.CoursewareObjectTemplateRepository;
import nd.esp.service.lifecycle.repository.sdk.CoursewareRepository;
import nd.esp.service.lifecycle.repository.sdk.EbookRepository;
import nd.esp.service.lifecycle.repository.sdk.ExaminationPaperRepository;
import nd.esp.service.lifecycle.repository.sdk.HomeWorkRepository;
import nd.esp.service.lifecycle.repository.sdk.InstructionalobjectiveRepository;
import nd.esp.service.lifecycle.repository.sdk.KnowledgeBaseRepository;
import nd.esp.service.lifecycle.repository.sdk.LearningPlansRepository;
import nd.esp.service.lifecycle.repository.sdk.LessonPlansRepository;
import nd.esp.service.lifecycle.repository.sdk.LessonRepository;
import nd.esp.service.lifecycle.repository.sdk.QuestionRepository;
import nd.esp.service.lifecycle.repository.sdk.ResCoverage4QuestionDBRepository;
import nd.esp.service.lifecycle.repository.sdk.ResCoverageRepository;
import nd.esp.service.lifecycle.repository.sdk.ResourceCategory4QuestionDBRepository;
import nd.esp.service.lifecycle.repository.sdk.ResourceCategoryRepository;
import nd.esp.service.lifecycle.repository.sdk.TeachingActivitiesRepository;
import nd.esp.service.lifecycle.repository.sdk.TeachingMaterialRepository;
import nd.esp.service.lifecycle.repository.sdk.TechInfo4QuestionDBRepository;
import nd.esp.service.lifecycle.repository.sdk.TechInfoRepository;
import nd.esp.service.lifecycle.repository.sdk.ToolsRepository;
import nd.esp.service.lifecycle.support.Constant;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.vos.assets.v06.AssetViewModel;
import nd.esp.service.lifecycle.vos.coursewareobjects.v06.CourseWareObjectViewModel;
import nd.esp.service.lifecycle.vos.coursewareobjecttemplate.v06.CoursewareObjectTemplateViewModel;
import nd.esp.service.lifecycle.vos.coursewares.v06.CoursewareViewModel;
import nd.esp.service.lifecycle.vos.ebooks.v06.EbookViewModel;
import nd.esp.service.lifecycle.vos.examinationpapers.v06.ExaminationPaperViewModel;
import nd.esp.service.lifecycle.vos.homeworks.v06.HomeworkViewModel;
import nd.esp.service.lifecycle.vos.instructionalobjectives.v06.InstructionalObjectiveViewModel;
import nd.esp.service.lifecycle.vos.knowledgebase.v06.KnowledgeBaseViewModel;
import nd.esp.service.lifecycle.vos.knowledges.v06.KnowledgeViewModel4Out;
import nd.esp.service.lifecycle.vos.learningplans.v06.LearningPlanViewModel;
import nd.esp.service.lifecycle.vos.lessonplans.v06.LessonPlanViewModel;
import nd.esp.service.lifecycle.vos.lessons.v06.LessonViewModel;
import nd.esp.service.lifecycle.vos.questions.v06.QuestionViewModel;
import nd.esp.service.lifecycle.vos.teachingmaterial.v06.TeachingMaterialViewModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Transactional;

/**
 * 辅助通用接口（各类资源）根据resourceType提供repository model,并且获得各个资源的各个模型类型（entity,model,viewModel),上传、下载
 * 
 * @author linsm
 * @since
 */

public class CommonServiceHelper {
    
    private static final Logger LOG = LoggerFactory.getLogger(CommonServiceHelper.class);
    
    //素材
    @Autowired
    AssetRepository assetRepository;
    
    //教案
    @Autowired
    LessonPlansRepository lessonPlansRepository;
    // 学案
    @Autowired
    LearningPlansRepository learningPlansRepository;
    
    //作业
    @Autowired
    HomeWorkRepository homeWorkRepository;
    
    //电子教材
    @Autowired
    EbookRepository ebookRepository;
	
	//习题
    @Autowired
    QuestionRepository questionRepository;
    
    //教材
    @Autowired
    TeachingMaterialRepository teachingMaterialRepository;
    
    // 课件颗粒
    @Autowired
    CoursewareObjectRepository coursewareObjectRepository;
    
    // 学科工具
    @Autowired
    ToolsRepository toolsRepository;
    
    // 课时
    @Autowired
    LessonRepository lessonRepository;
    
    // 知识点
    @Autowired
    ChapterRepository chapterRepository;
    
    //教学目标
    @Autowired
    InstructionalobjectiveRepository instructionalObjectiveRepository;
    
	@Autowired
	CategoryRepository categoryRepository;
	
	@Autowired
	CoursewareRepository coursewareRepository;
	
	@Autowired
	TeachingActivitiesRepository teachingActivitiesRepository;

	//课件颗粒模板
    @Autowired
    CoursewareObjectTemplateRepository coursewareObjectTemplateRepository ;
    
    @Autowired
    TechInfoRepository techInfoRepository;
    
    @Autowired
    TechInfo4QuestionDBRepository techInfo4QuestionDBRepository;
    
    @Autowired
    ResCoverageRepository resCoverageRepository;
    
    @Autowired
    ResCoverage4QuestionDBRepository resCoverage4QuestionDBRepository;
    
    @Autowired
    ResourceCategoryRepository resourceCategoryRepository;
    
    @Autowired
    ResourceCategory4QuestionDBRepository resourceCategory4QuestionDBRepository;
    
    @Autowired
    KnowledgeBaseRepository knowledgeBaseRepository;
    
    @Autowired
    ExaminationPaperRepository examinationPaperRepository;
    
    @Autowired
    private NDResourceDao ndResourceDao;
    
	@PersistenceContext(unitName="entityManagerFactory")
	EntityManager em;
	
	@PersistenceContext(unitName="questionEntityManagerFactory")
	EntityManager questionEm;
	
	@Qualifier(value="defaultJdbcTemplate")
	@Autowired
	private JdbcTemplate defaultJdbcTemplate;
	
    Map<String, RepositoryAndModelAndView> repositoryAndModelMap;

    @PostConstruct
    public void postConstruct() {
        repositoryAndModelMap = new HashMap<String, RepositoryAndModelAndView>();
        //素材
        repositoryAndModelMap.put("assets", new RepositoryAndModelAndView(assetRepository, AssetModel.class, AssetViewModel.class,Asset.class,true,true));
        //教案
        repositoryAndModelMap.put("lessonplans", new RepositoryAndModelAndView(lessonPlansRepository, LessonPlanModel.class, LessonPlanViewModel.class,LessonPlan.class,true,true));
        // 学案
        repositoryAndModelMap.put("learningplans", new RepositoryAndModelAndView(learningPlansRepository,
                                                                                 LearningPlanModel.class,
                                                                                 LearningPlanViewModel.class,
                                                                                 LearningPlan.class,true,true));
        //作业
        repositoryAndModelMap.put("homeworks", new RepositoryAndModelAndView(homeWorkRepository,
                                                                             HomeworkModel.class,
                                                                             HomeworkViewModel.class,
                                                                             HomeWork.class,true,true));
        
        //教材
        repositoryAndModelMap.put("teachingmaterials", new RepositoryAndModelAndView(teachingMaterialRepository,
                                                                             TeachingMaterialModel.class,
                                                                             TeachingMaterialViewModel.class,
                                                                             TeachingMaterial.class,true,true));
																			 
		//习题
        repositoryAndModelMap.put("questions", new RepositoryAndModelAndView(questionRepository,
                                                                             QuestionModel.class,
                                                                             QuestionViewModel.class,
                                                                             Question.class,true,true));
        // 课件颗粒
        repositoryAndModelMap.put("coursewareobjects", new RepositoryAndModelAndView(coursewareObjectRepository,
                                                                                     CourseWareObjectModel.class,
                                                                                     CourseWareObjectViewModel.class,
                                                                                     CoursewareObject.class,
                                                                                     true,
                                                                                     true));
        
        // 学科工具
        repositoryAndModelMap.put("tools", new RepositoryAndModelAndView(toolsRepository,
                                                                                     CourseWareObjectModel.class,
                                                                                     CourseWareObjectViewModel.class,
                                                                                     CoursewareObject.class,
                                                                                     true,
                                                                                     true));
        
        // 试卷
        repositoryAndModelMap.put("examinationpapers", new RepositoryAndModelAndView(examinationPaperRepository,
                                                                                     ExaminationPaperModel.class,
                                                                                     ExaminationPaperViewModel.class,
                                                                                     ExaminationPaper.class,
                                                                                     true,
                                                                                     true));
        
        
        // 课时
        repositoryAndModelMap.put("lessons", new RepositoryAndModelAndView(lessonRepository,
                                                                           LessonModel.class,
                                                                           LessonViewModel.class,
                                                                           Lesson.class,
                                                                           false,
                                                                           false));
        
        // 知识点
        repositoryAndModelMap.put("knowledges", new RepositoryAndModelAndView(chapterRepository,
                                                                              KnowledgeModel.class,
                                                                              KnowledgeViewModel4Out.class,
                                                                              Chapter.class,
                                                                              false,
                                                                              false));
        
        // 教学目标
        repositoryAndModelMap.put("instructionalobjectives",
                                  new RepositoryAndModelAndView(instructionalObjectiveRepository,
                                                                InstructionalObjectiveModel.class,
                                                                InstructionalObjectiveViewModel.class,
                                                                InstructionalObjective.class,
                                                                false,
                                                                false));
        
        //课件
        repositoryAndModelMap.put("coursewares", new RepositoryAndModelAndView(coursewareRepository,
                                                                             CoursewareModel.class,
                                                                             CoursewareViewModel.class,
                                                                             Courseware.class,true,true));
        // 电子教材
        repositoryAndModelMap.put("ebooks", new RepositoryAndModelAndView(ebookRepository,
                                                                          EbookModel.class,
                                                                          EbookViewModel.class,
                                                                          Ebook.class,
                                                                          true,
                                                                          true));
        
        //课件颗粒模板
        repositoryAndModelMap.put("coursewareobjecttemplates", new RepositoryAndModelAndView(coursewareObjectTemplateRepository, 
                                                                             CourseWareObjectTemplateModel.class,
                                                                             CoursewareObjectTemplateViewModel.class, 
                                                                             CoursewareObjectTemplate.class,
                                                                             true,
                                                                             true));
        //教辅
        repositoryAndModelMap.put("guidancebooks", new RepositoryAndModelAndView(teachingMaterialRepository, 
                TeachingMaterialModel.class,
                TeachingMaterialViewModel.class, 
                TeachingMaterial.class,
                false,
                false));
        
        //元课程
        repositoryAndModelMap.put("metacurriculums", new RepositoryAndModelAndView(teachingMaterialRepository, 
                TeachingMaterialModel.class,
                TeachingMaterialViewModel.class, 
                TeachingMaterial.class,
                true,
                true));
        
        //教学活动
        repositoryAndModelMap.put("teachingactivities", new RepositoryAndModelAndView(teachingActivitiesRepository,
                                                                             CoursewareModel.class,
                                                                             CoursewareViewModel.class,
                                                                             TeachingActivities.class,true,true));
        
        //eduresource
        repositoryAndModelMap.put(Constant.RESTYPE_EDURESOURCE, new RepositoryAndModelAndView(null, 
                null,
                ResourceViewModel.class, 
                null,
                false,
                false));
        
        //knowledgebase
        repositoryAndModelMap.put("knowledgebases", new RepositoryAndModelAndView(knowledgeBaseRepository,
                KnowledgeBaseModel.class,
                KnowledgeBaseViewModel.class,
                KnowledgeBase.class,false,false));
    }
    
    /**
     * 根据资源类型判断是否有上传接口
     * 
     * @author:xuzy
     * @date:2016年1月26日
     * @param resourceType
     * @return
     */
    public boolean isUploadable(String resourceType){
    	RepositoryAndModelAndView repositoryAndModel = repositoryAndModelMap.get(resourceType);
    	if(repositoryAndModel != null){
    		return repositoryAndModel.isUploadable();
    	}
    	return false;
    }

    public ResourceRepository<? extends EspEntity> getRepository(String resourceType) {
        RepositoryAndModelAndView repositoryAndModel = repositoryAndModelMap.get(resourceType);
        if (repositoryAndModel != null) {
            return repositoryAndModel.getRepository();
        } else {
           
            LOG.error(LifeCircleErrorMessageMapper.CommonAPINotSupportResourceType.getMessage() + resourceType);
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.CommonAPINotSupportResourceType.getCode(),
                                          LifeCircleErrorMessageMapper.CommonAPINotSupportResourceType.getMessage()
                                                  + resourceType);
        }
    }
    
    public Class<? extends ResourceModel> getModel(String resourceType) {
        RepositoryAndModelAndView repositoryAndModel = repositoryAndModelMap.get(resourceType);
        if (repositoryAndModel != null) {
            return repositoryAndModel.getModel();
        } else {
           
            LOG.error(LifeCircleErrorMessageMapper.CommonAPINotSupportResourceType.getMessage() + resourceType);
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.CommonAPINotSupportResourceType.getCode(),
                                          LifeCircleErrorMessageMapper.CommonAPINotSupportResourceType.getMessage()
                                                  + resourceType);
        }
    }
    
    public Class<? extends ResourceViewModel> getViewClass(String resourceType) {
        RepositoryAndModelAndView repositoryAndModel = repositoryAndModelMap.get(resourceType);
        if (repositoryAndModel != null) {
            return repositoryAndModel.getViewClass();
        } else {
            
            LOG.error(LifeCircleErrorMessageMapper.CommonAPINotSupportResourceType.getMessage() + resourceType);
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.CommonAPINotSupportResourceType.getCode(),
                                          LifeCircleErrorMessageMapper.CommonAPINotSupportResourceType.getMessage()
                                                  + resourceType);
        }
    }
    
    public Class<? extends EspEntity> getBeanClass(String resourceType) {
        RepositoryAndModelAndView repositoryAndModel = repositoryAndModelMap.get(resourceType);
        if (repositoryAndModel != null) {
            return repositoryAndModel.getBeanClass();
        } else {
            
            LOG.error(LifeCircleErrorMessageMapper.CommonAPINotSupportResourceType.getMessage() + resourceType);
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.CommonAPINotSupportResourceType.getCode(),
                                          LifeCircleErrorMessageMapper.CommonAPINotSupportResourceType.getMessage()
                                                  + resourceType);
        }
    }
    
    public void assertUploadable(String resourceType) {
        RepositoryAndModelAndView repositoryAndModel = repositoryAndModelMap.get(resourceType);
        if (repositoryAndModel != null) {
            if (!repositoryAndModel.isUploadable()) {
                
                LOG.error(LifeCircleErrorMessageMapper.CSResourceTypeNotSupport.getMessage()
                                                      + resourceType);
                
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              LifeCircleErrorMessageMapper.CSResourceTypeNotSupport.getCode(),
                                              LifeCircleErrorMessageMapper.CSResourceTypeNotSupport.getMessage()
                                                      + resourceType);
            }

        } else {
            
            LOG.error(LifeCircleErrorMessageMapper.CommonAPINotSupportResourceType.getMessage() + resourceType);
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.CommonAPINotSupportResourceType.getCode(),
                                          LifeCircleErrorMessageMapper.CommonAPINotSupportResourceType.getMessage()
                                                  + resourceType);
        }

    }
    
    public void assertDownloadable(String resourceType) {
        RepositoryAndModelAndView repositoryAndModel = repositoryAndModelMap.get(resourceType);
        if (repositoryAndModel != null) {
            if (!repositoryAndModel.isDownloadable()) {
                
                LOG.error(LifeCircleErrorMessageMapper.CSResourceTypeNotSupport.getMessage()
                                                      + resourceType);
               
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              LifeCircleErrorMessageMapper.CSResourceTypeNotSupport.getCode(),
                                              LifeCircleErrorMessageMapper.CSResourceTypeNotSupport.getMessage()
                                                      + resourceType);
            }

        } else {
           
            LOG.error(LifeCircleErrorMessageMapper.CommonAPINotSupportResourceType.getMessage() + resourceType);
           
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.CommonAPINotSupportResourceType.getCode(),
                                          LifeCircleErrorMessageMapper.CommonAPINotSupportResourceType.getMessage()
                                                  + resourceType);
        }

    }

    /**
     * 辅助类，用于绑定LC model 与SDK repository View
     * 
     * @author linsm
     * @since
     */
    private static class RepositoryAndModelAndView {
        ResourceRepository<? extends EspEntity> repository;
        Class<? extends ResourceModel> modelClass;
        Class<? extends ResourceViewModel> viewClass;
        Class<? extends EspEntity> beanClass;
        boolean downloadable;
        boolean uploadable;

        public ResourceRepository<? extends EspEntity> getRepository() {
            return repository;
        }

        public Class<? extends ResourceModel> getModel() {
            return modelClass;
        }

        public boolean isDownloadable() {
            return downloadable;
        }

        public boolean isUploadable() {
            return uploadable;
        }

        public Class<? extends ResourceViewModel> getViewClass() {
            return viewClass;
        }
        
        public Class<? extends EspEntity> getBeanClass() {
            return beanClass;
        }

        /**
         * 
         */
        public RepositoryAndModelAndView(ResourceRepository<? extends EspEntity> repository,
                                         Class<? extends ResourceModel> modelClass,
                                         Class<? extends ResourceViewModel> viewClass,
                                         Class<? extends EspEntity> beanClass,
                                         boolean downloadable,
                                         boolean uploadable) {
            this.repository = repository;
            this.modelClass = modelClass;
            this.viewClass = viewClass;
            this.beanClass = beanClass;
            this.downloadable = downloadable;
            this.uploadable = uploadable;
        }
    }
    
	
	/**
	 * 获取维度的shortName
	 * 
	 * @author:xuzy
	 * @date:2015年8月4日
	 * @param beanListResult
	 * @return
	 */
	public Map<String,String> getCategoryByData(List<CategoryData> beanListResult){
		Map<String,String> returnMap = new HashMap<String, String>();
		List<String> cList = new ArrayList<String>();
		List<Category> categoryList = new ArrayList<Category>();
		if(CollectionUtils.isNotEmpty(beanListResult)){
			for (CategoryData categoryData : beanListResult) {
				//维度
				String s = categoryData.getNdCode().substring(0, 2);
				if(!cList.contains(s)){
					cList.add(s);
				}
			}
			try {
				categoryList = categoryRepository.getListWhereInCondition("ndCode", cList);
				for (Category category : categoryList) {
					String ndCode = category.getNdCode();
					String shortName = category.getShortName();
					if(StringUtils.isNotEmpty(ndCode)){
						returnMap.put(ndCode, shortName);
					}
				}
			} catch (EspStoreException e) {
			    
			    LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(),e);
			    
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getLocalizedMessage());
			}
		}
		return returnMap;
	}
	
    /**
     * 如果是教材或教辅,需要异步删除其相关章节和相关章节下的关系
     * ps:1.该方法使用在NDResourceServiceImpl类的delete方法中
     *    2.在新线程中使用
     *    3.执行的是update SQL语句
     *    *4.如果写在NDResourceServiceImpl中,会导致@Transactional事务不能生效,导致更新时没有事务的错误
     * <p>Create Time: 2016年1月28日   </p>
     * <p>Create author: xiezy   </p>
     * @param mid
     */
//    @Transactional
//    public void deleteChaptersAndRelations(String mid){
//        //删除mid相关章节下的关系
//        ndResourceDao.deleteRelationByChapters(mid);
//        //删除mid相关章节
//        ndResourceDao.deleteChapters(mid);
//    }
    
    /**
     * 根据资源类型获取techInfo的仓储
     * @param resType
     * @return
     */
    public ResourceRepository getTechInfoRepositoryByResType(String resType){
    	if(!isQuestionDb(resType)){
    		return techInfoRepository;
    	}else{
    		return techInfo4QuestionDBRepository;
    	}
    }
	
	/**
     * 根据资源类型获取resourceCategory的仓储
     * @param resType
     * @return
     */
    public ResourceRepository getResourceCategoryRepositoryByResType(String resType){
    	if(!isQuestionDb(resType)){
    		return resourceCategoryRepository;
    	}else{
    		return resourceCategory4QuestionDBRepository;
    	}
    }
    
    /**
     * 根据资源类型获取resourceCoverage的仓储
     * @param resType
     * @return
     */
    public ResourceRepository getResCoverageRepositoryByResType(String resType){
    	if(!isQuestionDb(resType)){
    		return resCoverageRepository;
    	}else{
    		return resCoverage4QuestionDBRepository;
    	}
    }
    
    public static boolean isQuestionDb(String resType){
		if (!(resType.equals(IndexSourceType.QuestionType.getName()) || resType
				.equals(IndexSourceType.SourceCourseWareObjectType.getName()))) {
    		return false;
    	}
    	return true;
    }
    
    /**
     * 查询并更新同步表变量
     * @param var
     * @return
     */
    @Transactional
    public int queryAndUpdateSynVariable(int pid){
    	String sql = "select value from synchronized_table where pid = " + pid +" for update";
    	Query query = em.createNativeQuery(sql);
    	Object o = query.getSingleResult();
    	if(o != null){
    		int v = (Integer)o;
    		if(v == 0){
    			String updateSql = "update synchronized_table set value = 1 where pid = " + pid;
    			Query query2 = em.createNativeQuery(updateSql);
    			return query2.executeUpdate();
    		}else{
    			return 0;
    		}
    	}
    	return 1;
    }
	
	/**
     * 删除关系（源与目标）（设置enable）
     * 
     * @param resourceType
     * @param uuid
     * @since
     */
    @Transactional(value="transactionManager")
    public void deleteRelation(String resourceType, String uuid) {
        //关系
        String sql = "UPDATE resource_relations SET enable = '0' WHERE (resource_target_type = '" + resourceType
                + "' AND target='" + uuid + "') OR (res_type = '" + resourceType + "' AND source_uuid='" + uuid + "')";
        Query query = em.createNativeQuery(sql);
        query.executeUpdate();
    }
    
    /**
     * 删除习题库中的资源关系（源与目标）（设置enable）
     * 
     * @param resourceType
     * @param uuid
     * @since
     */
    @Transactional(value="questionTransactionManager")
    public void deleteRelation4QuestionDB(String resourceType, String uuid) {
        //关系
        String sql = "UPDATE resource_relations SET enable = '0' WHERE (resource_target_type = '" + resourceType
                + "' AND target='" + uuid + "') OR (res_type = '" + resourceType + "' AND source_uuid='" + uuid + "')";
        Query query = questionEm.createNativeQuery(sql);
        query.executeUpdate();
    }
    
    /**
     * 初始化同步表变量
     * @param pid
     * @return
     */
    @Transactional
    public int initSynVariable(int pid){
    	String sql = "update synchronized_table set value = 0 where pid = " + pid;
    	Query query = em.createNativeQuery(sql);
    	return query.executeUpdate();
    }
    
    @Transactional(value="transactionManager")
    public void batchAddResourceCategory(List<ResourceCategory> rcList){
    	try {
			resourceCategoryRepository.batchAdd(rcList);
		} catch (EspStoreException e) {
			LOG.warn("工具API接口将教材的维度路径copy至目标资源出错",e);
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getLocalizedMessage());
		}
    }
    
    @Transactional(value="questionTransactionManager")
    public void batchAddResourceCategory4Question(List<ResourceCategory> rcList){
    	try {
			resourceCategory4QuestionDBRepository.batchAdd(rcList);
		} catch (EspStoreException e) {
			LOG.warn("工具API接口将教材的维度路径copy至目标资源出错",e);
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getLocalizedMessage());
		}
    }
    
	/**
	 * 判断维度数据是否是合法的PT维度
	 * @author xiezy
	 * @date 2016年7月21日
	 * @param code
	 */
	public void isPublishType(String code){
		final List<String> resultList = new ArrayList<String>();
		
		String sql = "SELECT nd_code as nc FROM category_datas WHERE nd_code LIKE 'PT%'";
		defaultJdbcTemplate.query(sql, new RowMapper<String>(){
			@Override
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				resultList.add(rs.getString("nc"));
				return null;
			}
		});
		
		if(CollectionUtils.isNotEmpty(resultList)){
			if(!resultList.contains(code)){
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
						"LC/PT_CODE_IS_NOT_EXIST",
						code + ":不是合法的PT维度");
			}
		}else{
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					"LC/PT_IS_NOT_EXIST",
					"PT维度在该环境未录入");
		}
	}
	
    public Map<String, Object> getRepositoryAndModelMap() {
		return new HashMap<String, Object>(repositoryAndModelMap);
	}

}
