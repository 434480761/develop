package nd.esp.service.lifecycle.repository.v02;

import java.util.List;

import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.Courseware;

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
public interface CoursewareApi extends StoreApi<Courseware>,SearchApi<Courseware> {
	
	/**
	 * 对应属性lessonObjectivess.
	 *
	 * @param lessonObjectivess the lesson objectivess
	 * @return the list by lesson objectives
	 * @throws EspStoreException the esp store exception
	 */
	//public List<Courseware>getListByLessonObjectives(List<String> lessonObjectivess) throws EspStoreException;
	

	/**
	 * ppt.
	 *
	 * @param creatorId the creator id
	 * @param chapterId the chapter id
	 * @return the list by chapter and creator
	 * @throws EspStoreException the esp store exception
	 */
	//public List<Courseware> getListByChapterAndCreator(String creatorId,String chapterId)throws EspStoreException;
}