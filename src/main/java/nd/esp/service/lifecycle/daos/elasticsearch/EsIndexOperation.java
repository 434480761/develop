package nd.esp.service.lifecycle.daos.elasticsearch;

/**
 * 索引配置
 * 
 * @author linsm
 *
 */
public interface EsIndexOperation {

	/**
	 * 创建索引
	 * 
	 * @param indexName
	 *            索引名
	 * @author linsm
	 * @return
	 */
	Boolean createIndex(String indexName);

	/**
	 * 添加或者更新mapping
	 * 
	 * @param indexName
	 *            索引名
	 * @param primaryCategory
	 *            资源类型
	 * @author linsm
	 * @return
	 */
	Boolean putMapping(String IndexName, String primaryCategory);

	/**
	 * 删除索引
	 * 
	 * @param esIndexName
	 *            索引名
	 * @author linsm
	 * @return
	 */
	Boolean deleteIndex(String esIndexName);

}
