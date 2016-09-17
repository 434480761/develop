package nd.esp.service.lifecycle.services.resourcesharing.v06;

import nd.esp.service.lifecycle.models.resourcesharing.v06.ResourceSharingModel;
import nd.esp.service.lifecycle.vos.ListViewModel;

import com.nd.gaea.rest.security.authens.UserInfo;

public interface ResourceSharingService {
	
	/**
	 * 创建资源分享
	 * @author xiezy
	 * @date 2016年8月29日
	 * @param rsm
	 * @param userInfo
	 * @return
	 */
	public ResourceSharingModel createResourceSharing(ResourceSharingModel rsm,UserInfo userInfo);
	
	/**
	 * 删除对应用户指定资源的所有分享
	 * @author xiezy
	 * @date 2016年8月29日
	 * @param resType
	 * @param resourceId
	 * @param userInfo
	 * @return
	 */
	public boolean deleteAllResourceSharingBySomeone(String resType, String resourceId, 
			UserInfo userInfo);
	
	/**
	 * 删除特定的资源分享
	 * @author xiezy
	 * @date 2016年8月29日
	 * @param resType
	 * @param resourceId
	 * @param sharingId
	 * @param userInfo
	 * @return
	 */
	public boolean deleteResourceSharingBySharingId(String resType, String resourceId, 
			String sharingId, UserInfo userInfo);
	
	/**
	 * 获取分享列表
	 * @author xiezy
	 * @date 2016年8月29日
	 * @param resType
	 * @param resourceId
	 * @param limit
	 * @param userInfo
	 * @return
	 */
	public ListViewModel<ResourceSharingModel> getResourceSharingList(
			String resType, String resourceId, String limit, UserInfo userInfo);
	
	/**
	 * 验证密码是否正确
	 * @author xiezy
	 * @date 2016年8月30日
	 * @param sharingId
	 * @param passwd
	 * @return
	 */
	public ResourceSharingModel verifyPasswd(String sharingId, String passwd);
}
