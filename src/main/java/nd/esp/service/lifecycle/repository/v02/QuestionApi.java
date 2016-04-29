package nd.esp.service.lifecycle.repository.v02;

import java.util.List;

import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.index.QueryRequest;
import nd.esp.service.lifecycle.repository.index.QueryResponse;
import nd.esp.service.lifecycle.repository.model.Question;

// TODO: Auto-generated Javadoc
/**
 * 项目名字:nd esp<br>
 * 类描述:<br>
 * 创建人:wengmd<br>
 * 创建时间:2015年2月4日<br>
 * 修改人:<br>
 * 修改时间:2015年2月4日<br>
 * 修改备注:<br>
 * .
 *
 * @version 0.2<br>
 */
public interface QuestionApi extends StoreApi<Question>, SearchApi<Question> {

	/**
	 * 
	 * @Title: search
	 * @Description: 检索
	 * @param @param chapterid
	 * @param @param creatorid
	 * @param @param questionType
	 * @param @param query
	 * @param @return
	 * @param @throws EspStoreException
	 * @param @throws SolrServerException
	 * @return QueryResponse<Question>
	 * @throws
	 */
	public QueryResponse<Question> search(String chapterid, String creatorid,
			String publisher, List<String> questionType, String difficulty,
			QueryRequest query) throws EspStoreException;
}