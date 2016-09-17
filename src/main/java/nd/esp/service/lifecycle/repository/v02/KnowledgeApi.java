package nd.esp.service.lifecycle.repository.v02;

import java.util.List;

import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.index.QueryRequest;
import nd.esp.service.lifecycle.repository.index.QueryResponse;
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
public interface KnowledgeApi extends StoreApi<Knowledge>,SearchApi<Knowledge> {
	
	/**
	 * @desc  通过学科ID获取知识点
	 * @param subjectId
	 * @return
	 * @author liuwx
	 */
	public List<Knowledge> queryBySubject(String subjectId) throws EspStoreException;
	
	
	/**
	 * @desc  通过学科ID分页获取知识点
	 * @param subjectId
	 * @return
	 * @author liuwx
	 */
	public QueryResponse<Knowledge> queryBySubject(String subjectId,QueryRequest request) throws EspStoreException;
	
	
	

	/**
	 * @desc  通过章节ID获取章节知识点
	 * @param outlineId
	 * @return
	 * @author liuwx
	 */
	//public List<Knowledge> queryByOutline(String outlineId) throws EspStoreException;
	
	/**
	 * @desc  通过教学目标ID获取知识点
	 * @param outlineId
	 * @return
	 * @author liuwx
	 */
	//public List<Knowledge> queryByInstructional(String instructionalId) throws EspStoreException;
	
	
	
	/**
	 * @desc  通过教学目标和知识点ID删除两者关联
	 * @param instructionalId
	 * @param knowledgeId
	 * @return
	 * @author liuwx
	 */
	//public  boolean deleteByInstructional(String instructionalId,String knowledgeId) throws EspStoreException;
}