package nd.esp.service.lifecycle.daos.vrlife;

import java.util.List;

/**
 * VR人生 定制化接口Dao层
 * @author xiezy
 * @date 2016年8月2日
 */
public interface VrLifeDao {
	
	/**
	 * 获取推荐列表的资源id
	 * @author xiezy
	 * @date 2016年8月2日
	 * @return
	 */
	public List<String> getRecommendResources();
	
	/**
	 * 根据骨骼ID获取对应的推荐资源
	 * @author xiezy
	 * @date 2016年8月2日
	 * @param skeletonId
	 * @param type
	 * @return
	 */
	public List<String> getRecommendResourcesBySkeleton(String skeletonId,String type);
	
	/**
	 * 获取组合资源
	 * @author xiezy
	 * @date 2016年8月3日
	 * @param skeletonId
	 * @return
	 */
	public List<String> dynamicComposition(String skeletonId);
}
