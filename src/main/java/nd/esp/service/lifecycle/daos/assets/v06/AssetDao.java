package nd.esp.service.lifecycle.daos.assets.v06;

import java.util.List;

import nd.esp.service.lifecycle.repository.model.Asset;

/**
 * 素材dao层
 * @author xuzy
 *
 */
public interface AssetDao {
	/**
	 * 根据分类维度查询素材
	 * @param category
	 * @return
	 */
	public List<Asset> queryByCategory(String category);
	
	/**
	 * 根据套件父级目录查询子套件
	 * @param sourceId
	 * @return
	 */
	public List<Asset> queryBySourceId(String sourceId,String category);
	
	/**
	 * 根据套件code查找教学目标类型
	 * @param sourceId
	 * @return
	 */
	public List<Asset> queryInsTypesByCategory(String likeName,String category);
}
