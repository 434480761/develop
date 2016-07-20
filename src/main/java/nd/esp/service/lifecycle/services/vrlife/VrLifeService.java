package nd.esp.service.lifecycle.services.vrlife;

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
}
