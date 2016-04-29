package nd.esp.service.lifecycle.services.elasticsearch;

/**
 * 索引配置
 * 
 * @author linsm
 *
 */
public interface IndexConfigureService {

	/**
	 * 创建索引
	 * 
	 * @author linsm
	 * @return
	 */
	Boolean createIndex();

	/**
	 * 删除索引
	 * 
	 * @author linsm
	 * @return
	 */
	Boolean deleteIndex();

	/**
	 * 添加或者更新mapping
	 * 
	 * @param primaryCategory
	 *            资源类型
	 * @author linsm
	 * @return
	 */
	Boolean putMapping(String primaryCategory);

}
