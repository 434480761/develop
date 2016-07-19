package nd.esp.service.lifecycle.services.vrlife;

import nd.esp.service.lifecycle.vos.vrlife.StatusReviewViewModel4In;
import nd.esp.service.lifecycle.vos.vrlife.StatusReviewViewModel4Out;

/**
 * VR人生 定制化接口Service层
 * @author xiezy
 * @date 2016年7月19日
 */
public interface VrLifeService {
	public StatusReviewViewModel4Out statusReview(StatusReviewViewModel4In statusReviewViewModel4In);
}
