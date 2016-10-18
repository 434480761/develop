package nd.esp.service.lifecycle.services.titan;

import java.util.List;
import java.util.Map;
import java.util.Set;

import nd.esp.service.lifecycle.educommon.models.ResourceModel;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.vos.ListViewModel;
import nd.esp.service.lifecycle.vos.educationrelation.v06.RelationForQueryViewModel;

public interface TitanSearchService {

/*	public ListViewModel<ResourceModel> search(String resType,
			List<String> includes,
			Map<String, Map<String, List<String>>> params,
			Map<String, String> orderMap, int from, int size, boolean reverse,String words);
	*/
	
/*	public ListViewModel<ResourceModel> searchWithAdditionProperties(String resType,
			List<String> includes,
			Map<String, Map<String, List<String>>> params,
			Map<String, String> orderMap, int from, int size, boolean reverse,String words);*/

	public ListViewModel<ResourceModel> searchWithStatistics(Set<String> resTypeSet,
																	 List<String> includes,
																	 Map<String, Map<String, List<String>>> params,
																	 Map<String, String> orderMap, int from, int size, boolean reverse,String words, boolean forceStatus, List<String> tags, boolean showVersion, boolean onlyCount, boolean onlyResult);


	/**
	 *
	 * @param resType
	 * @param fields
	 * @param includes
	 * @param params
	 * @param orderMap
	 * @param from
	 * @param size
	 * @param reverse
     * @param words
     * @return
     */
	public ListViewModel<ResourceModel> searchUseES(Set<String> resTypeSet,List<String> fields,
			List<String> includes,
			Map<String, Map<String, List<String>>> params,
			Map<String, String> orderMap, int from, int size, boolean reverse,String words);

	/**
	 *
	 * @param resType
	 * @param sourceUuid
	 * @param categories
	 * @param targetType
	 * @param label
	 * @param tags
	 * @param relationType
	 * @param limit
	 * @param reverse
     * @param coverage
     * @return
     */
	public ListViewModel<RelationForQueryViewModel> queryListByResType(String resType,
																		   String sourceUuid,
																		   String categories,
																		   String targetType,
																		   String label,
																		   String tags,
																		   String relationType,
																		   String limit,
																		   boolean reverse,
																	       boolean recursion,
																		   String coverage);

	/**
	 * 在有些情景下，单个的获取源资源的目标资源列表的接口，业务系统使用起来过于频繁。此时业务方提出需要能够进行设置批量的源资源ID，
	 * 通过源资源的ID快速的查询目标资源的列表。
	 1.接口提供设置源资源ID的列表进行批量查询
	 2.接口提供设置关系的类型
	 3.接口提供设置目标资源的类型
	 * <p>Create Time: 2015年10月19日   </p>
	 * <p>Create author: caocr   </p>
	 * @param resType 源资源类型
	 * @param sids 源资源id，可批量
	 * @param targetType 目标资源类型
	 * @param label            资源关系标识
	 * @param tags             资源关系标签
	 * @param relationType 关系类型
	 * @param limit 分页参数
	 * @param reverse          指定查询的关系是否进行反向查询。如果reverse参数不携带，默认是从s->t查询，如果reverse=true，反向查询T->S
	 */
	public ListViewModel<RelationForQueryViewModel> batchQueryResources(String resType,
																			Set<String> sids,
																			String targetType,
																			String label,
																			String tags,
																			String relationType,
																			String limit,
																			boolean reverse);


}
