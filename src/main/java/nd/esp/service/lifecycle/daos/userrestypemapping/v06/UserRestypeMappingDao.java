package nd.esp.service.lifecycle.daos.userrestypemapping.v06;

import nd.esp.service.lifecycle.models.UserRestypeMappingModel;

import java.util.List;

/**
 * <p>Title: UserRestypeMappingDao</p>
 * <p>Description: UserRestypeMappingDao</p>
 * <p>Copyright: Copyright (c) 2016  </p>
 * <p>Company:ND Co., Ltd.  </p>
 * <p>Create Time: 2016/6/28 </p>
 *
 * @author lanyl
 */
public interface UserRestypeMappingDao {

	/**
	 * 新增用户请求类型映射关系
	 * @param
	 * @return
	 * @author lanyl
	 */
	/*int insert(UserRestypeMappingModel userRestypeMappingModel);*/

	/**
	 * 查询用户请求类型映射关系
	 * @param id
	 * @return
	 * @author lanyl
	 */
	UserRestypeMappingModel query(Integer id);

	/**
	 * 批量插入用户请求类型映射关系
	 * @param resTypeList
	 * @param userId
	 * @author lanyl
	 */
	void batchSave(final List<String> resTypeList, final String userId);

	/**
	 * 批量删除用户请求类型映射关系
	 * @param resTypeList
	 * @param userId
	 * @author lanyl
	 */
	void batchDelete(final List<String> resTypeList, final String userId);

	/**
	 * 删除用户请求类型映射关系
	 * @param userId
	 * @author lanyl
	 */
	/*void delete(String userId);*/

	/**
	 * 查询用户请求类型映射关系信息列表
	 * @param userIdList
	 * @return
	 * @author lanyl
	 */
	List<UserRestypeMappingModel> findUserRestypeMappingModelList(List<String> userIdList);

	/**
	 * 查询用户请求类型映射关系信息列表
	 * @param userId
	 * @return
	 * @author lanyl
	 */
	List<String> findUserRestypeList(String userId);
}