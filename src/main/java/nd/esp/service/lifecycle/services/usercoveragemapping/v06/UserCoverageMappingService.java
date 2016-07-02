package nd.esp.service.lifecycle.services.usercoveragemapping.v06;

import nd.esp.service.lifecycle.models.UserCoverageMappingModel;

import java.util.List;

/**
 * <p>Title: UserCoverageMappingService</p>
 * <p>Description: UserCoverageMappingService</p>
 * <p>Copyright: Copyright (c) 2016  </p>
 * <p>Company:ND Co., Ltd.  </p>
 * <p>Create Time: 2016/6/28 </p>
 *
 * @author lanyl
 */
public interface UserCoverageMappingService {
    
    /**
     * 添加UserCoverageMappingModel
     * @param userCoverageMappingModel
     * @return
     */
    /*int addUserCoverageMapping(UserCoverageMappingModel userCoverageMappingModel);*/

	/**
	 * 批量插入用户覆盖类型映射关系
	 * @param coverages
	 * @param userId
	 * @author lanyl
	 */
	void addUserCoverageMappings(List<String> coverages, String userId);

	/**
	 * 批量删除用户覆盖类型映射关系
	 * @param coverages
	 * @param userId
	 * @author lanyl
	 */
	void deleteUserCoverageMappings(List<String> coverages, String userId);

	/**
	 * 删除用户覆盖类型映射关系
	 * @param userId
	 * @author lanyl
	 */
	/*void deleteAllUserCoverageMappingsByUserId(String userId);*/

	/**
	 * 查询用户覆盖类型映射关系信息列表
	 * @param userIdList
	 * @return
	 * @author lanyl
	 */
	List<UserCoverageMappingModel> findUserCoverageMappingModelList(List<String> userIdList);

	/**
	 * 查询用户覆盖类型映射关系信息列表
	 * @param userId
	 * @return
	 * @author lanyl
	 */
	List<String> findUserCoverageList(String userId);
}