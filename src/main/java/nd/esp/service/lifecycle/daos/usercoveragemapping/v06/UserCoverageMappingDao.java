package nd.esp.service.lifecycle.daos.usercoveragemapping.v06;

import nd.esp.service.lifecycle.models.UserCoverageMappingModel;

import java.util.List;

/**
 * <p>Title: UserCoverageMappingDao</p>
 * <p>Description: UserCoverageMappingDao</p>
 * <p>Copyright: Copyright (c) 2016  </p>
 * <p>Company:ND Co., Ltd.  </p>
 * <p>Create Time: 2016/6/28 </p>
 *
 * @author lanyl
 */
public interface UserCoverageMappingDao {

	/**
	 * 新增用户覆盖类型映射关系
	 * @param
	 * @return
	 * @author lanyl
	 */
	/*int insert(UserCoverageMappingModel userCoverageMappingModel);*/


	/**
	 * 查询用户覆盖类型映射关系
	 * @param id
	 * @return
	 * @author lanyl
	 */
	/*UserCoverageMappingModel query(Integer id);*/

	/**
	 * 批量插入用户请求类型映射关系
	 * @param coverageList
	 * @param userId
	 * @author lanyl
	 */
	void batchSave(final List<String> coverageList, final String userId);

	/**
	 * 批量删除用户请求类型映射关系
	 * @param coverageList
	 * @param userId
	 * @author lanyl
	 */
	void batchDelete(final List<String> coverageList, final String userId);

	/**
	 * 删除用户请求类型映射关系
	 * @param userId
	 * @author lanyl
	 */
	/*void delete(String userId);*/

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
