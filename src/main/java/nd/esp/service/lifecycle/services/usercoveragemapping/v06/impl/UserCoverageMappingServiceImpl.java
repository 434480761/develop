package nd.esp.service.lifecycle.services.usercoveragemapping.v06.impl;

import java.util.List;

import nd.esp.service.lifecycle.daos.usercoveragemapping.v06.UserCoverageMappingDao;
import nd.esp.service.lifecycle.models.UserCoverageMappingModel;
import nd.esp.service.lifecycle.services.usercoveragemapping.v06.UserCoverageMappingService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>Title: UserCoverageMappingServiceImpl</p>
 * <p>Description: UserCoverageMappingServiceImpl</p>
 * <p>Copyright: Copyright (c) 2016  </p>
 * <p>Company:ND Co., Ltd.  </p>
 * <p>Create Time: 2016/6/28 </p>
 *
 * @author lanyl
 */
@Service(value="UserCoverageMappingServiceImpl")
public class UserCoverageMappingServiceImpl implements UserCoverageMappingService {

	@Autowired
	private UserCoverageMappingDao userCoverageMappingDao;

	/**
	 * 新增UserRestypeMappingModel
	 * @param userCoverageMappingModel
	 * @return
	 * @author lanyl
	 */
	public int addUserCoverageMapping(UserCoverageMappingModel userCoverageMappingModel){
		return  userCoverageMappingDao.insert(userCoverageMappingModel);
	}

	/**
	 * 批量插入用户覆盖类型映射关系
	 * @param coverages
	 * @param userId
	 * @author lanyl
	 */
	public void addUserCoverageMappings(List<String> coverages, String userId){
		userCoverageMappingDao.batchSave(coverages,userId);
	}

	/**
	 * 批量删除用户覆盖类型映射关系
	 * @param coverages
	 * @param userId
	 * @author lanyl
	 */
	public void deleteUserCoverageMappings(List<String> coverages, String userId){
		userCoverageMappingDao.batchDelete(coverages,userId);
	}

	/**
	 * 删除用户覆盖类型映射关系
	 * @param userId
	 * @author lanyl
	 */
	public void deleteAllUserCoverageMappingsByUserId(String userId){
		userCoverageMappingDao.delete(userId);
	}

	/**
	 * 查询用户覆盖类型映射关系信息列表
	 * @param userIdList
	 * @return
	 * @author lanyl
	 */
	public List<UserCoverageMappingModel> findUserCoverageMappingModelList(List<String> userIdList){
		return userCoverageMappingDao.findUserCoverageMappingModelList(userIdList);
	}

	/**
	 * 查询用户覆盖类型映射关系信息列表
	 * @param userId
	 * @return
	 * @author lanyl
	 */
	public List<String> findUserCoverageList(String userId){
		return userCoverageMappingDao.findUserCoverageList(userId);
	}

}
