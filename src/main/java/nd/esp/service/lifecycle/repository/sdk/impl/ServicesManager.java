
/**   
 * @Title: ServicesManager.java 
 * @Package: com.nd.esp.store.service.impl 
 * @Description: TODO
 * @author Rainy(yang.lin)  
 * @date 2015年5月6日 下午4:13:10 
 * @version 1.3.1 
 */


package nd.esp.service.lifecycle.repository.sdk.impl;

import java.util.Map;

import nd.esp.service.lifecycle.repository.EspRepository;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.repository.sdk.AssetRepository;
import nd.esp.service.lifecycle.repository.sdk.CategoryDataRepository;
import nd.esp.service.lifecycle.repository.sdk.CategoryPatternRepository;
import nd.esp.service.lifecycle.repository.sdk.CategoryRelationRepository;
import nd.esp.service.lifecycle.repository.sdk.CategoryRepository;
import nd.esp.service.lifecycle.repository.sdk.ChapterRepository;
import nd.esp.service.lifecycle.repository.sdk.CoursewareObjectRepository;
import nd.esp.service.lifecycle.repository.sdk.CoursewareObjectTemplateRepository;
import nd.esp.service.lifecycle.repository.sdk.CoursewareRepository;
import nd.esp.service.lifecycle.repository.sdk.EbookRepository;
import nd.esp.service.lifecycle.repository.sdk.ExaminationPaperRepository;
import nd.esp.service.lifecycle.repository.sdk.GuidanceBooksRepository;
import nd.esp.service.lifecycle.repository.sdk.HomeWorkRepository;
import nd.esp.service.lifecycle.repository.sdk.InstructionalobjectiveRepository;
import nd.esp.service.lifecycle.repository.sdk.KnowledgeBaseRepository;
import nd.esp.service.lifecycle.repository.sdk.KnowledgeRelationRepository;
import nd.esp.service.lifecycle.repository.sdk.LearningPlansRepository;
import nd.esp.service.lifecycle.repository.sdk.LessonPlansRepository;
import nd.esp.service.lifecycle.repository.sdk.LessonRepository;
import nd.esp.service.lifecycle.repository.sdk.QuestionRepository;
import nd.esp.service.lifecycle.repository.sdk.ResCoverageRepository;
import nd.esp.service.lifecycle.repository.sdk.ResourceRelationRepository;
import nd.esp.service.lifecycle.repository.sdk.TeachingActivitiesRepository;
import nd.esp.service.lifecycle.repository.sdk.TeachingMaterialRepository;
import nd.esp.service.lifecycle.repository.sdk.ToolsRepository;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.HttpStatus;

import com.google.common.collect.Maps;
import com.nd.gaea.WafException;

/**
 *  
 *
 * @author Rainy(yang.lin)
 * @version V1.0
 * @Description 
 * @date 2015年5月6日 下午4:13:10
 */

public class ServicesManager implements ApplicationContextAware {
	
	/** The Constant RESOURCE_REPOSITORIES. */
	public final static Map<String,Class<?>> RESOURCE_REPOSITORIES = Maps.newConcurrentMap();
	
	/** The application context. */
	private static ApplicationContext applicationContext; 
	
	static {
		RESOURCE_REPOSITORIES.put(IndexSourceType.QuestionType.getName(), QuestionRepository.class);
		RESOURCE_REPOSITORIES.put(IndexSourceType.AssetType.getName(), AssetRepository.class);
		RESOURCE_REPOSITORIES.put(IndexSourceType.ChapterType.getName(), ChapterRepository.class);
		RESOURCE_REPOSITORIES.put(IndexSourceType.KnowledgeType.getName(), ChapterRepository.class); //现在知识点与章节共用一个类（表）
		RESOURCE_REPOSITORIES.put(IndexSourceType.KnowledgeRelationType.getName(), KnowledgeRelationRepository.class);
		RESOURCE_REPOSITORIES.put(IndexSourceType.SourceCourseWareObjectType.getName(), CoursewareObjectRepository.class);
		RESOURCE_REPOSITORIES.put(IndexSourceType.SourceCourseWareType.getName(), CoursewareRepository.class);
		RESOURCE_REPOSITORIES.put(IndexSourceType.SourceCourseWareObjectTemplateType.getName(), CoursewareObjectTemplateRepository.class);
		RESOURCE_REPOSITORIES.put(IndexSourceType.TeachingMaterialType.getName(), TeachingMaterialRepository.class);
		RESOURCE_REPOSITORIES.put(IndexSourceType.LessonType.getName(), LessonRepository.class);
		RESOURCE_REPOSITORIES.put(IndexSourceType.InstructionalObjectiveType.getName(), InstructionalobjectiveRepository.class);
		RESOURCE_REPOSITORIES.put(IndexSourceType.ResourceRelationType.getName(), ResourceRelationRepository.class);
		RESOURCE_REPOSITORIES.put(IndexSourceType.EbookType.getName(), EbookRepository.class);
		RESOURCE_REPOSITORIES.put(IndexSourceType.CategoryType.getName(), CategoryRepository.class);
		RESOURCE_REPOSITORIES.put(IndexSourceType.CategoryDataType.getName(), CategoryDataRepository.class);
		RESOURCE_REPOSITORIES.put(IndexSourceType.CategoryRelationType.getName(), CategoryRelationRepository.class);
		RESOURCE_REPOSITORIES.put(IndexSourceType.CategoryPatternType.getName(), CategoryPatternRepository.class);
		RESOURCE_REPOSITORIES.put(IndexSourceType.LessonPlansType.getName(), LessonPlansRepository.class);
		RESOURCE_REPOSITORIES.put(IndexSourceType.HomeWorkType.getName(), HomeWorkRepository.class);
		RESOURCE_REPOSITORIES.put(IndexSourceType.ResCoverageType.getName(), ResCoverageRepository.class);
		RESOURCE_REPOSITORIES.put(IndexSourceType.LearningPlansType.getName(), LearningPlansRepository.class);
		RESOURCE_REPOSITORIES.put(IndexSourceType.GuidanceBooksType.getName(), TeachingMaterialRepository.class);
		RESOURCE_REPOSITORIES.put(IndexSourceType.MetaCurriculumType.getName(), TeachingMaterialRepository.class);
		RESOURCE_REPOSITORIES.put(IndexSourceType.ToolsType.getName(), ToolsRepository.class);  //FIXME 
		RESOURCE_REPOSITORIES.put(IndexSourceType.SourceTeachingActivitiesType.getName(), TeachingActivitiesRepository.class);
		RESOURCE_REPOSITORIES.put(IndexSourceType.ExaminationPapersType.getName(), ExaminationPaperRepository.class);
		RESOURCE_REPOSITORIES.put("knowledgebases", KnowledgeBaseRepository.class);
	}
	
	/**
	 * Gets the.
	 *
	 * @param type the type
	 * @return the esp store service
	 * @throws Exception 
	 */
	public static EspRepository<?> get(String type){
		 Class<?> clz = RESOURCE_REPOSITORIES.get(type);
		 if(clz == null){
			 throw new WafException("LC/CHECK_RESTYPE_ERROR", "资源类型不对", HttpStatus.INTERNAL_SERVER_ERROR);
		 }
		 return (EspRepository<?>) applicationContext.getBean(clz);
	}
	  
  	/**
  	 * Gets the application context.
  	 *
  	 * @return ApplicationContext
  	 */
	  public static ApplicationContext getApplicationContext() {
	    return applicationContext;
	  }
	
	/**
	 * Description .
	 *
	 * @param arg0 the new application context
	 * @throws BeansException the beans exception
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */ 
		
	@Override
	public void setApplicationContext(ApplicationContext arg0)
			throws BeansException {
		ServicesManager.applicationContext = arg0;
	}
}
