package nd.esp.service.lifecycle.services.vrlife;

import java.util.List;

import nd.esp.service.lifecycle.educommon.models.ResourceModel;
import nd.esp.service.lifecycle.vos.ListViewModel;
import nd.esp.service.lifecycle.vos.vrlife.StatusReviewViewModel4In;
import nd.esp.service.lifecycle.vos.vrlife.StatusReviewViewModel4Out;

/**
 * VR人生 定制化接口Service层
 * @author xiezy
 * @date 2016年7月19日
 */
public interface VrLifeService {
	/**
	 * 资源审核,更新内容包括了资源状态与标签分类
	 * @author xiezy
	 * @date 2016年7月19日
	 * @param statusReviewViewModel4In
	 * @return
	 */
	public StatusReviewViewModel4Out statusReview(StatusReviewViewModel4In statusReviewViewModel4In);
	
	/**
	 * VR资源推荐功能
	 * @author xiezy
	 * @date 2016年8月2日
	 * @param skeletonId
	 * @param type
	 * @param includeList
	 * @return
	 */
	public ListViewModel<ResourceModel> recommendResourceList(String skeletonId,String type,List<String> includeList);
	
	/**
	 * 获取组合资源
	 * @author xiezy
	 * @date 2016年8月3日
	 * @param skeletonId
	 * @param includeList
	 * @return
	 */
	public ListViewModel<ResourceModel> dynamicComposition(String skeletonId, List<String> includeList);
	
	/**
	 * 添加推荐列表
	 * @author xiezy
	 * @date 2016年9月2日
	 * @param resources
	 * @return
	 */
	public void addRecommendedResource(List<String> resources);
	
	/**
	 * 删除推荐资源
	 * @author xiezy
	 * @date 2016年9月2日
	 * @param id
	 */
	public void deleteRecommendedResource(String id);
}
