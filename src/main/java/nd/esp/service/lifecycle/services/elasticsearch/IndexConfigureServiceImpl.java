package nd.esp.service.lifecycle.services.elasticsearch;

import nd.esp.service.lifecycle.daos.elasticsearch.EsIndexOperation;
import nd.esp.service.lifecycle.support.Constant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 索引配置
 * 
 * @author linsm
 *
 */
@Service
public class IndexConfigureServiceImpl implements IndexConfigureService {
	@Autowired
	private EsIndexOperation esIndexOperation;

	/**
	 * 创建索引
	 * 
	 * @author linsm
	 * @return
	 */
	@Override
	public Boolean createIndex() {
		return esIndexOperation.createIndex(
				Constant.ES_INDEX_NAME);
	}

	/**
	 * 删除索引
	 * 
	 * @author linsm
	 * @return
	 */
	@Override
	public Boolean deleteIndex() {
		return esIndexOperation.deleteIndex(
				Constant.ES_INDEX_NAME);
	}

	/**
	 * 添加或者更新mapping
	 * 
	 * @param primaryCategory
	 *            资源类型
	 * @author linsm
	 * @return
	 */
	@Override
	public Boolean putMapping(String primaryCategory) {
		return esIndexOperation.putMapping(
				Constant.ES_INDEX_NAME, primaryCategory);
	}

}
