package nd.esp.service.lifecycle.services.userrestypemapping.v06;

import nd.esp.service.lifecycle.models.UserRestypeMappingModel;

import java.util.List;

/**
 * <p>Title: UserRestypeMappingService</p>
 * <p>Description: UserRestypeMappingService</p>
 * <p>Copyright: Copyright (c) 2016  </p>
 * <p>Company:ND Co., Ltd.  </p>
 * <p>Create Time: 2016/6/28 </p>
 *
 * @author lanyl
 */
public interface UserRestypeMappingService {
    /**
     * 添加userRestypeMappingModel
     * @param userRestypeMappingModel
     * @return
     */
    /*int addUserRestypeMapping(UserRestypeMappingModel userRestypeMappingModel);*/

	/**
	 * 批量插入用户请求类型映射关系
	 * @param resTypes
	 * @param userId
	 * @author lanyl
	 */
	void addUserRestypeMappings(List<String> resTypes, String userId);

	/**
	 * 批量删除用户请求类型映射关系
	 * @param resTypes
	 * @param userId
	 * @author lanyl
	 */
	void deleteUserRestypeMappings(List<String> resTypes, String userId);

	/**
	 * 删除用户请求类型映射关系
	 * @param userId
	 * @author lanyl
	 */
	/*void deleteAllUserResTypeMappingsByUserId(String userId);*/

	/**
	 * 查询用户请求类型映射关系信息列表
	 * @param userId
	 * @return
	 */
	List<String> findUserRestypeList(String userId);

	/**
	 * 查询用户请求类型映射关系信息列表
	 * @param userIdList
	 * @return
	 */
	List<UserRestypeMappingModel> findUserRestypeMappingModelList(List<String> userIdList);
}