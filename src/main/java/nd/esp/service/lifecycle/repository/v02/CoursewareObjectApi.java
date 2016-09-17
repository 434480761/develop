package nd.esp.service.lifecycle.repository.v02;

import java.util.List;

import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.CoursewareObject;

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
public interface CoursewareObjectApi extends StoreApi<CoursewareObject>,SearchApi<CoursewareObject> {
	
	/**
	 * 对应属性objectives.
	 *
	 * @param objectives the objectives
	 * @return the list by objectives
	 * @throws EspStoreException the esp store exception
	 */
	//public List<CoursewareObject>getListByObjectives(List<String> objectives) throws EspStoreException;
}