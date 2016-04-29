package nd.esp.service.lifecycle.repository.v02;

import java.util.List;

import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.index.QueryRequest;
import nd.esp.service.lifecycle.repository.index.QueryResponse;
import nd.esp.service.lifecycle.repository.model.InstructionalObjective;
import nd.esp.service.lifecycle.repository.model.Knowledge;

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
public interface InstructionalobjectiveApi extends StoreApi<InstructionalObjective>,SearchApi<InstructionalObjective> {
	
	/**
	 * 
	* @Title: search
	* @Description: 根据关键字(对标题和描述进行检索)
	* @param @param request
	* @param @throws EspStoreException
	* @return QueryResponse<InstructionalObjectives>
	* @throws
	 */
	public QueryResponse<InstructionalObjective> search(QueryRequest request) throws EspStoreException;
	/**
	 * @desc  通过课时id查询相关教学目标
	 * @param request
	 * @param teachingMaterial
	 * @return
	 * @throws EspStoreException
	 * @author liuwx
	 */
	public QueryResponse<InstructionalObjective> search(QueryRequest request,String lessonId) throws EspStoreException;


	/**
	 * @desc  
	 * @param objectivesKnowledge
	 * @return
	 * @throws EspStoreException
	 * @author liuwx
	 */
	//public ObjectiveKnowledge addObjectivesKnowledge(ObjectiveKnowledge objectivesKnowledge )throws EspStoreException;
	/**
	 * 删除教学目标知识点
	 * @param objectives
	 * @param knowledges
	 * @return
	 * @throws EspStoreException
	 */
	//public boolean deleteObjectivesKnowledge(String objectives, String knowledges)throws EspStoreException;
	/**
	 * 通过教学目标，获取相关知识点
	 * @param objectives
	 * @param knowledges
	 * @return
	 * @throws EspStoreException
	 */
	//public List<Knowledge> getKnowledgesByObjectives(String objectives, String knowledges)throws EspStoreException;
}