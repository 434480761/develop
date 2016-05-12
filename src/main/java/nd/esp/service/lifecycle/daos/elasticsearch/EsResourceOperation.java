package nd.esp.service.lifecycle.daos.elasticsearch;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import nd.esp.service.lifecycle.entity.elasticsearch.Resource;

import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * 资源数据索引操作
 * 
 * @author linsm
 *
 */
public interface EsResourceOperation {

	/**
	 * 添加索引数据
	 * @param resource
	 * @throws JsonProcessingException
	 */
	IndexResponse add(Resource resource) throws JsonProcessingException;
	
	
	String getJson(Resource resource)throws JsonProcessingException;

//	void asynAdd(Resource resource);

	/**
	 * 批量添加索引数据
	 * @param resources
	 * @return
	 * @throws JsonProcessingException
	 * @throws IOException
	 */
	BulkResponse batchAdd(Set<Resource> resources) throws JsonProcessingException, IOException;
//	void asynBatchAdd(Set<Resource> resources);


	/**
	 * 删除索引数据
	 * 
	 * @author linsm
	 */
	DeleteResponse delete(Resource resource);
	
	BulkResponse batchDelete(Set<Resource> resourceSet);

	/**
	 * 删除索引数据
	 * 
	 * @param primaryCategory
	 *            资源类型
	 * @author linsm
	 */
	@Deprecated
	void delete(String primaryCategory);

	/**
	 * 查询
	 * 
	 * @param resourceType
	 *            资源类型
	 * @param queryBuilder
	 *            查询条件
	 * @param from
	 *            偏离值
	 * @param size
	 *            一页的大小
	 * @param sortBuilders
	 *            排序
	 * @author linsm
	 * @return
	 */
	SearchHits search(String resourceType, QueryBuilder queryBuilder, int from,
			int size, List<SortBuilder> sortBuilders);

}