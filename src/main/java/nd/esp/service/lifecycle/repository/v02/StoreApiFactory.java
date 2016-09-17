/**
 * 
 */
package nd.esp.service.lifecycle.repository.v02;

import nd.esp.service.lifecycle.repository.config.SpringContextHolder;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;

// TODO: Auto-generated Javadoc
/**
 * 项目名字:nd esp<br>
 * 类描述:<br>
 * 创建人:wengmd<br>
 * 创建时间:2015年2月4日<br>
 * 修改人:<br>
 * 修改时间:2015年2月4日<br>
 * 修改备注:<br>.
 *
 * @version 0.2<br>
 */
public class StoreApiFactory {

	/**
	 * Builder.
	 *
	 * @param <T> the generic type
	 * @param classType the class type
	 * @return the t
	 * @throws EspStoreException the esp store exception
	 */
	private static <T> T builder(Class<T> classType) throws EspStoreException {
		return (T) SpringContextHolder.getBean(classType.getSimpleName());
	}

	/**
	 * Builder asset api.
	 *
	 * @return the asset api
	 * @throws EspStoreException the esp store exception
	 */
	public static AssetApi builderAssetApi() throws EspStoreException {
		return builder(AssetApi.class);
	}
	
	/**
	 * Builder course ware api.
	 *
	 * @return the courseware api
	 * @throws EspStoreException the esp store exception
	 */
	public static CoursewareApi builderCourseWareApi()
			throws EspStoreException {
		return builder(CoursewareApi.class);
	}

	/**
	 * Builder courseware object api.
	 *
	 * @return the courseware object api
	 * @throws EspStoreException the esp store exception
	 */
	public static CoursewareObjectApi builderCoursewareObjectApi()
			throws EspStoreException {
		return builder(CoursewareObjectApi.class);
	}

	/**
	 * Builder course ware object template api.
	 *
	 * @return the courseware object template api
	 * @throws EspStoreException the esp store exception
	 */
	public static CoursewareObjectTemplateApi builderCourseWareObjectTemplateApi()
			throws EspStoreException {
		return builder(CoursewareObjectTemplateApi.class);
	}
	
	/**
	 * Builder teaching material api.
	 *
	 * @return the teaching material api
	 * @throws EspStoreException the esp store exception
	 */
	public static TeachingMaterialApi builderTeachingMaterialApi() throws EspStoreException{
		return builder(TeachingMaterialApi.class);
	}
	
	/**
	 * Builder chapter api.
	 *
	 * @return the chapter api
	 * @throws EspStoreException the esp store exception
	 */
	public static ChapterApi builderChapterApi() throws EspStoreException{
		return builder(ChapterApi.class);
	}
	
	/**
	 * Builder lesson api.
	 *
	 * @return the lesson api
	 * @throws EspStoreException the esp store exception
	 */
	public static LessonApi builderLessonApi() throws EspStoreException{
		return builder(LessonApi.class);
	}
	
	/**
	 * Builder instructional objectives api.
	 *
	 * @return the instructionalobjective api
	 * @throws EspStoreException the esp store exception
	 */
	public static InstructionalobjectiveApi builderInstructionalObjectivesApi() throws EspStoreException{
		return builder(InstructionalobjectiveApi.class);
	}
	
	/**
	 * Builder question api.
	 *
	 * @return the question api
	 * @throws EspStoreException the esp store exception
	 */
	public static QuestionApi builderQuestionApi() throws EspStoreException{
		return builder(QuestionApi.class);
	}
	
	/**
	 * Builder knowledge api.
	 *
	 * @return the knowledge api
	 * @throws EspStoreException the esp store exception
	 */
	public static KnowledgeApi builderKnowledgeApi() throws EspStoreException{
		return builder(KnowledgeApi.class);
	}

	
//	public static PrototypeActivityApi builderPrototypeActivityApi() throws EspStoreException{
//		return builder(IPrototypeActivityApi.class);
//	}
	
	
	/**
	 * Builder category data api.
	 *
	 * @return the category data api
	 * @throws EspStoreException the esp store exception
	 */
	public static CategoryDataApi builderCategoryDataApi() throws EspStoreException{
		return builder(CategoryDataApi.class);
	}
	
	/**
	 * Builder category relation api.
	 *
	 * @return the category relation api
	 * @throws EspStoreException the esp store exception
	 */
	public static CategoryRelationApi builderCategoryRelationApi() throws EspStoreException{
		return builder(CategoryRelationApi.class);
	}
	
	/**
	 * Builder category api.
	 *
	 * @return the category api
	 * @throws EspStoreException the esp store exception
	 */
	public static CategoryApi builderCategoryApi() throws EspStoreException{
		return builder(CategoryApi.class);
	}
	
	/**
	 * Builder category pattern api.
	 *
	 * @return the category pattern api
	 * @throws EspStoreException the esp store exception
	 */
	public static CategoryPatternApi builderCategoryPatternApi() throws EspStoreException{
		return builder(CategoryPatternApi.class);
	}
	
	/**
	 * Builder resource relation api.
	 *
	 * @return the resource relation api service
	 * @throws EspStoreException the esp store exception
	 */
	public static ResourceRelationApiService builderResourceRelationApi()throws EspStoreException{
		return builder(ResourceRelationApiService.class);
	}
	
	
	/**
	 * Builder knowledge relation api.
	 *
	 * @return the knowledge relation api
	 * @throws EspStoreException the esp store exception
	 */
	public static KnowledgeRelationApi builderKnowledgeRelationApi() throws EspStoreException{
		return builder(KnowledgeRelationApi.class);
	}
	
}
