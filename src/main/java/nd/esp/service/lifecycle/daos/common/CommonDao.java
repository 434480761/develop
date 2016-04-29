package nd.esp.service.lifecycle.daos.common;

import nd.esp.service.lifecycle.support.DbName;

/**
 * 提供一些公共的DAO方法
 * @author xuzy
 *
 */
public interface CommonDao {
	
	/**
	 * 根据资源id删除tech_infos表数据
	 * 
	 * @author:xuzy
	 * @date:2015年11月4日
	 * @param resourceId
	 * @return 删除的记录条数
	 */
	public int deleteTechInfoByResource(String resType,String resourceId,DbName name);
	
	/**
	 * 根据资源id删除resource_categories表数据
	 * 
	 * @author:xuzy
	 * @date:2015年11月4日
	 * @param resourceId
	 * @return 删除的记录条数
	 */
	public int deleteResourceCategoryByResource(String resType,String resourceId,DbName name);
}
