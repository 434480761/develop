package nd.esp.service.lifecycle.daos.usercoveragemapping.v06;

import java.util.ArrayList;
import java.util.List;

import nd.esp.service.lifecycle.BaseControllerConfig;
import nd.esp.service.lifecycle.models.UserCoverageMappingModel;
import nd.esp.service.lifecycle.utils.common.StringTestUtil;
import nd.esp.service.lifecycle.utils.common.TestException;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>Title: Dao层UserCoverageMappingDao测试</p>
 * <p>Description: UserCoverageMappingDaoTest</p>
 * <p>Copyright: Copyright (c) 2016 </p>
 * <p>Company: ND Co., Ltd.  </p>
 * <p>Create Time: 2016/7/2 </p>
 * <p>MethodSorters 按字母顺序 TEST若有顺序要求，要注意方法名称书写 </p>
 * @author gaoq
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UserCoverageMappingDaoTest extends BaseControllerConfig{
    
    private static Logger logger = LoggerFactory.getLogger(UserCoverageMappingDaoTest.class);
    private static String userId;
    private static List<String> coverageList;

    @Autowired 
    private UserCoverageMappingDao userCoverageMappingDao;
    
    /**
     * 用例：批量插入用户覆盖类型映射关系
     * @author gaoq
     * @throws Exception
     */
    @Test
    public void Test001BatchSave() throws Exception {
    	userId = "userid"+StringTestUtil.randomNumbers(10);
    	//例1：coverageList为空,其他参数正常 【期望结果：错误】
    	try {
			this.userCoverageMappingDao.batchSave(null, userId);
		} catch (Exception e) {
            logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"UserCoverageMappingDaoTest", "Test001BatchSave", "error", e});
		}
    	
    	//例2：coverageList不为空，coverage长度超过255，其他参数正常【期望结果：错误】
    	coverageList = new ArrayList<String>();
    	String coverage = "coverage_"+StringTestUtil.randomNumbers(255);
    	coverageList.add(coverage);   	
    	try {
			this.userCoverageMappingDao.batchSave(coverageList, userId);
		} catch (Exception e) {
            logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"UserCoverageMappingDaoTest", "Test001BatchSave2", "error", e});
		}
    	
    	//例3：user_id参数为空 其他参数正常【期望结果：错误】
    	coverage = "coverage_"+StringTestUtil.randomNumbers(10);
    	coverageList.add(coverage);   	
    	try {
			this.userCoverageMappingDao.batchSave(coverageList, null);
		} catch (Exception e) {
            logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"UserCoverageMappingDaoTest", "Test001BatchSave3", "error", e});
		}
    	
    	//例4：user_id参数长度超过36 其他参数正常【期望结果：错误】
    	userId = StringTestUtil.randomNumbers(40);
    	coverageList.add(coverage);   	
    	try {
			this.userCoverageMappingDao.batchSave(coverageList, userId);
		} catch (Exception e) {
            logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"UserCoverageMappingDaoTest", "Test001BatchSave4", "error", e});
		}
    	
    	//例5：所有参数正常【期望结果：通过】
    	coverageList.clear();
    	coverage = "coverage"+StringTestUtil.randomNumbers(10);
    	userId = "userid"+StringTestUtil.randomNumbers(10);
    	coverageList.add(coverage);   	
    	try {
    	    this.userCoverageMappingDao.batchSave(coverageList, userId);    
            logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"UserCoverageMappingDaoTest", "Test001BatchSave5", "pass", "Test001BatchSave5 test pass"});
        } catch (Exception e) {
            throw new TestException();
        }
    }

    /**
     * 用例：查询用户覆盖类型映射关系信息列表
     * @author gaoq
     * @throws Exception
     */
    @Test
    public void Test002FindUserCoverageList() throws Exception {
    	//例1：userId 为空 【期望结果：通过】
    	List<String> list = this.userCoverageMappingDao.findUserCoverageList(null);
    	if(CollectionUtils.isEmpty(list)){
            logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"UserCoverageMappingDaoTest", "Test002FindUserCoverageList", "pass", "Test002FindUserCoverageList test pass"});
    	} else {
			throw new TestException();
		}
    	
    	//例2：userId 不为空 值为不存在 【期望结果：通过】
    	List<String> list2 = this.userCoverageMappingDao.findUserCoverageList("-1");
    	if(CollectionUtils.isEmpty(list2)){
            logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"UserCoverageMappingDaoTest", "Test002FindUserCoverageList2", "pass", "Test002FindUserCoverageList2 test pass"});
    	} else {
			throw new TestException();
		}
    	
    	//例2：userId 不为空 值为不存在 【期望结果：通过】
    	List<String> list3 = this.userCoverageMappingDao.findUserCoverageList(userId);
    	if(list3 != null && list3.size() > 0){
            logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"UserCoverageMappingDaoTest", "Test002FindUserCoverageList3", "pass", "Test002FindUserCoverageList3 test pass"});
    	} else {
			throw new TestException();
		}
    	
    }
    
    /**
     * 用例：查询用户覆盖类型映射关系信息列表
     * @author gaoq
     * @throws Exception
     */
    @Test
    public void Test003FindUserCoverageMappingModelList() throws Exception {
    	//例1：userIdList 为空 【期望结果：错误】
    	List<UserCoverageMappingModel> list = null;
    	try {	
    		this.userCoverageMappingDao.findUserCoverageMappingModelList(null);
		} catch (Exception e) {
            logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"UserCoverageMappingDaoTest", "Test003FindUserCoverageMappingModelList", "error", e});
		}
    	if(!CollectionUtils.isEmpty(list)){
           throw new TestException();
		}
    	
    	//例2：userIdList 不为空  userId值为不存在 【期望结果：通过】
    	List<String> userIdList = new ArrayList<String>();
    	userIdList.add("-1");
    	List<UserCoverageMappingModel> list2 = this.userCoverageMappingDao.findUserCoverageMappingModelList(userIdList);
    	if(CollectionUtils.isEmpty(list2)){
            logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"UserCoverageMappingDaoTest", "Test003FindUserCoverageMappingModelList2", "pass", "Test003FindUserCoverageMappingModelList2 test pass"});
    	} else {
			throw new TestException();
		}
    	
    	//例2：userIdList 不为空 值为不存在 【期望结果：通过】
    	List<String> userIdList2 = new ArrayList<String>();
    	userIdList2.add(userId);
    	List<UserCoverageMappingModel> list3 = this.userCoverageMappingDao.findUserCoverageMappingModelList(userIdList2);
    	if(list3 != null && list3.size() > 0){
            logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"UserCoverageMappingDaoTest", "Test003FindUserCoverageMappingModelList3", "pass", "Test003FindUserCoverageMappingModelList3 test pass"});
    	} else {
			throw new TestException();
		}   	
    }
    
    /**
     * 用例： 批量删除用户覆盖类型映射关系
     * @author gaoq
     * @throws Exception
     */
    @Test
    public void Test004BatchDelete() throws Exception {
    	//例1：coverageList为空,其他参数正常 【期望结果：通过】   	
    	this.userCoverageMappingDao.batchDelete(null, userId);		
    	List<String> list = this.userCoverageMappingDao.findUserCoverageList(userId);
    	if(list != null && list.size() > 0){
    		 logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"UserCoverageMappingDaoTest", "Test004BatchDelete1", "pass", "Test004BatchDelete1 test pass"});
    	} else {
			throw new TestException();
		}   
    	
    	//例2：user_id参数为空 其他参数正常【期望结果：通过】   	
    	try {
    	    this.userCoverageMappingDao.batchDelete(coverageList, null);
    	    logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"UserCoverageMappingDaoTest", "Test004BatchDelete2", "pass", "Test004BatchDelete2 test pass"});
        } catch (Exception e) {
            throw new TestException();
        }
    	
    	//例3：coverageList为不为空，resType值不存在,其他参数正常 【期望结果：通过】   	
    	List<String> resTypes = new ArrayList<String>();
    	resTypes.add("-1");
    	this.userCoverageMappingDao.batchDelete(resTypes, userId);
    	List<String> list3 = this.userCoverageMappingDao.findUserCoverageList(userId);
    	if(list3 != null && list3.size() > 0){
    		 logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"UserCoverageMappingDaoTest", "Test004BatchDelete3", "pass", "Test004BatchDelete3 test pass"});
    	} else {
			throw new TestException();
		} 
    	
    	//例4：userId不为空 值不存在,其他参数正常 【期望结果：通过】   	
    	String userid = "-1";
    	this.userCoverageMappingDao.batchDelete(coverageList, userid);
    	List<String> list4 = this.userCoverageMappingDao.findUserCoverageList(userId);
    	if(list4 != null && list4.size() > 0){
    		 logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"UserCoverageMappingDaoTest", "Test004BatchDelete4", "pass", "Test004BatchDelete4 test pass"});
    	} else {
			throw new TestException();
		} 

    	//例5：所有参数正常【期望结果：通过】   	
    	this.userCoverageMappingDao.batchDelete(coverageList, userId);
    	List<String> list5 = this.userCoverageMappingDao.findUserCoverageList(userId);
    	if(CollectionUtils.isEmpty(list5)){
    		 logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"UserCoverageMappingDaoTest", "Test004BatchDelete5", "pass", "Test004BatchDelete5 test pass"});
    	} else {
			throw new TestException();
		} 
    }
}
