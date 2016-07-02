package nd.esp.service.lifecycle.services.userrestypemapping.v06.impl;

import java.util.List;

import nd.esp.service.lifecycle.daos.userrestypemapping.v06.UserRestypeMappingDao;
import nd.esp.service.lifecycle.models.UserRestypeMappingModel;
import nd.esp.service.lifecycle.services.userrestypemapping.v06.UserRestypeMappingService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>Title: UserRestypeMappingServiceImpl</p>
 * <p>Description: UserRestypeMappingServiceImpl</p>
 * <p>Copyright: Copyright (c) 2016  </p>
 * <p>Company:ND Co., Ltd.  </p>
 * <p>Create Time: 2016/6/28 </p>
 *
 * @author lanyl
 */
@Service(value="UserRestypeMappingServiceImpl")
public class UserRestypeMappingServiceImpl implements UserRestypeMappingService {

	@Autowired
	private UserRestypeMappingDao userRestypeMappingDao;

	/**
	 * 新增UserRestypeMappingModel
	 * @param userRestypeMappingModel
	 * @return
	 * @author lanyl
	 */
	/*public int addUserRestypeMapping(UserRestypeMappingModel userRestypeMappingModel){
		return  userRestypeMappingDao.insert(userRestypeMappingModel);
	}*/

	/**
	 * 批量插入用户请求类型映射关系
	 * @param resTypes
	 * @param userId
	 * @author lanyl
	 */
	public void addUserRestypeMappings(List<String> resTypes, String userId){
		userRestypeMappingDao.batchSave(resTypes,userId);
	}

	/**
	 * 批量删除用户请求类型映射关系
	 * @param resTypes
	 * @param userId
	 * @author lanyl
	 */
	public void deleteUserRestypeMappings(List<String> resTypes, String userId){
		userRestypeMappingDao.batchDelete(resTypes, userId);
	}

	/**
	 * 删除用户请求类型映射关系
	 * @param userId
	 * @author lanyl
	 */
	/*public void deleteAllUserResTypeMappingsByUserId(String userId){
		userRestypeMappingDao.delete(userId);
	}*/

	/**
	 * 查询用户请求类型映射关系信息列表
	 * @param userIdList
	 * @return
	 */
	public List<UserRestypeMappingModel> findUserRestypeMappingModelList(List<String> userIdList){
		return  userRestypeMappingDao.findUserRestypeMappingModelList(userIdList);
	}

	/**
	 * 查询用户请求类型映射关系信息列表
	 * @param userId
	 * @return
	 */
	public List<String> findUserRestypeList(String userId){
		return  userRestypeMappingDao.findUserRestypeList(userId);
	}
}