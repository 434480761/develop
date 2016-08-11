package nd.esp.service.lifecycle.daos.userrestypemapping.v06;

import java.util.ArrayList;
import java.util.List;

import nd.esp.service.lifecycle.BaseControllerConfig;
import nd.esp.service.lifecycle.models.UserRestypeMappingModel;
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
 * <p>Title:  用户请求类型映射关系</p>
 * <p>Description: UserRestypeMappingDaoTest</p>
 * <p>Copyright: Copyright (c) 2016 </p>
 * <p>Company: ND Co., Ltd.  </p>
 * <p>Create Time: 2016/7/2 </p>
 * <p>MethodSorters 按字母顺序 TEST若有顺序要求，要注意方法名称书写 </p>
 * @author gaoq
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UserRestypeMappingDaoTest  extends BaseControllerConfig{
    private static Logger logger = LoggerFactory.getLogger(UserRestypeMappingDaoTest.class);
    private static String userId;
    private static List<String> resTypeList;

    @Autowired 
    private UserRestypeMappingDao userRestypeMappingDao;
    
    /**
     * 用例：新增用户请求类型映射关系
     * @author gaoq
     * @throws Exception
     */
    @Test
    public void Test001BatchSave() throws Exception {
    	userId = "userid"+StringTestUtil.randomNumbers(10);
    	//例1：resTypeList为空,其他参数正常 【期望结果：错误】
    	try {
			this.userRestypeMappingDao.batchSave(null, userId);
		} catch (Exception e) {
            logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"UserRestypeMappingDaoTest", "Test001BatchSave", "error", e});
		}
    	
    	//例2：resTypeList不为空，res_type长度超过64，其他参数正常【期望结果：错误】
    	resTypeList = new ArrayList<String>();
    	String resType = "res_type"+StringTestUtil.randomNumbers(65);
    	resTypeList.add(resType);   	
    	try {
			this.userRestypeMappingDao.batchSave(resTypeList, userId);
		} catch (Exception e) {
            logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"UserRestypeMappingDaoTest", "Test001BatchSave2", "error", e});
		}
    	
    	//例3：user_id参数为空 其他参数正常【期望结果：错误】
    	resType = "res_type"+StringTestUtil.randomNumbers(10);
    	resTypeList.add(resType);   	
    	try {
			this.userRestypeMappingDao.batchSave(resTypeList, null);
		} catch (Exception e) {
            logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"UserRestypeMappingDaoTest", "Test001BatchSave3", "error", e});
		}
    	
    	//例4：user_id参数长度超过36 其他参数正常【期望结果：错误】
    	userId = StringTestUtil.randomNumbers(40);
    	resTypeList.add(resType);   	
    	try {
			this.userRestypeMappingDao.batchSave(resTypeList, userId);
		} catch (Exception e) {
            logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"UserRestypeMappingDaoTest", "Test001BatchSave4", "error", e});
		}
    	
    	//例5：所有参数正常【期望结果：通过】
    	resTypeList.clear();
    	resType = "res_type"+StringTestUtil.randomNumbers(10);
    	userId = "userid"+StringTestUtil.randomNumbers(10);
    	resTypeList.add(resType); 
    	try {
    	    this.userRestypeMappingDao.batchSave(resTypeList, userId);
    	    logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"UserRestypeMappingDaoTest", "Test001BatchSave5", "pass", "Test001BatchSave5 test pass"});
        } catch (Exception e) {
            throw new TestException();
        }
    	
    }

    /**
     * 用例：查询用户请求类型映射关系信息列表
     * @author gaoq
     * @throws Exception
     */
    @Test
    public void Test002FindUserRestypeList() throws Exception {
    	//例1：userId 为空 【期望结果：通过】
    	List<String> list = this.userRestypeMappingDao.findUserRestypeList(null);
    	if(CollectionUtils.isEmpty(list)){
            logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"UserRestypeMappingDaoTest", "Test002FindUserRestypeList", "pass", "Test002FindUserRestypeList test pass"});
    	} else {
			throw new TestException();
		}
    	
    	//例2：userId 不为空 值为不存在 【期望结果：通过】
    	List<String> list2 = this.userRestypeMappingDao.findUserRestypeList("-1");
    	if(CollectionUtils.isEmpty(list2)){
            logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"UserRestypeMappingDaoTest", "Test002FindUserRestypeList2", "pass", "Test002FindUserRestypeList2 test pass"});
    	} else {
			throw new TestException();
		}
    	
    	//例2：userId 不为空 值为不存在 【期望结果：通过】
    	List<String> list3 = this.userRestypeMappingDao.findUserRestypeList(userId);
    	if(list3 != null && list3.size() > 0){
            logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"UserRestypeMappingDaoTest", "Test002FindUserRestypeList3", "pass", "Test002FindUserRestypeList3 test pass"});
    	} else {
			throw new TestException();
		}
    	
    }
    
    /**
     * 用例：查询用户请求类型映射关系信息列表
     * @author gaoq
     * @throws Exception
     */
    @Test
    public void Test003FindUserRestypeMappingModelList() throws Exception {
    	//例1：userIdList 为空 【期望结果：错误】
    	List<UserRestypeMappingModel> list = null;
    	try {	
    		this.userRestypeMappingDao.findUserRestypeMappingModelList(null);
		} catch (Exception e) {
            logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"UserRestypeMappingDaoTest", "Test003FindUserRestypeMappingModelList", "error", e});
		}
    	if(!CollectionUtils.isEmpty(list)){
           throw new TestException();
		}
    	
    	//例2：userIdList 不为空  userId值为不存在 【期望结果：通过】
    	List<String> userIdList = new ArrayList<String>();
    	userIdList.add("-1");
    	List<UserRestypeMappingModel> list2 = this.userRestypeMappingDao.findUserRestypeMappingModelList(userIdList);
    	if(CollectionUtils.isEmpty(list2)){
            logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"UserRestypeMappingDaoTest", "Test003FindUserRestypeMappingModelList2", "pass", "Test003FindUserRestypeMappingModelList2 test pass"});
    	} else {
			throw new TestException();
		}
    	
    	//例2：userIdList 不为空 值为不存在 【期望结果：通过】
    	List<String> userIdList2 = new ArrayList<String>();
    	userIdList2.add(userId);
    	List<UserRestypeMappingModel> list3 = this.userRestypeMappingDao.findUserRestypeMappingModelList(userIdList2);
    	if(list3 != null && list3.size() > 0){
            logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"UserRestypeMappingDaoTest", "Test003FindUserRestypeMappingModelList3", "pass", "Test003FindUserRestypeMappingModelList3 test pass"});
    	} else {
			throw new TestException();
		}   	
    }
    
    /**
     * 用例： 批量删除用户请求类型映射关系
     * @author gaoq
     * @throws Exception
     */
    @Test
    public void Test004BatchDelete() throws Exception {
    	//例1：resTypeList为空,其他参数正常 【期望结果：通过】   	
    	this.userRestypeMappingDao.batchDelete(null, userId);		
    	List<String> list = this.userRestypeMappingDao.findUserRestypeList(userId);
    	if(list != null && list.size() > 0){
    		 logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"UserRestypeMappingDaoTest", "Test004BatchDelete1", "pass", "Test004BatchDelete1 test pass"});
    	} else {
			throw new TestException();
		}   
    	
    	//例2：user_id参数为空 其他参数正常【期望结果：通过】   
    	try {
    	    this.userRestypeMappingDao.batchDelete(resTypeList, null);
    	    logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"UserRestypeMappingDaoTest", "Test004BatchDelete2", "pass", "Test004BatchDelete2 test pass"});
        } catch (Exception e) {
            throw new TestException();
        }
    	
    	//例3：resTypeList为不为空，resType值不存在,其他参数正常 【期望结果：通过】   	
    	List<String> resTypes = new ArrayList<String>();
    	resTypes.add("-1");
    	this.userRestypeMappingDao.batchDelete(resTypes, userId);
    	List<String> list3 = this.userRestypeMappingDao.findUserRestypeList(userId);
    	if(list3 != null && list3.size() > 0){
    		 logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"UserRestypeMappingDaoTest", "Test004BatchDelete3", "pass", "Test004BatchDelete3 test pass"});
    	} else {
			throw new TestException();
		} 

    	//例4：userId不为空 值不存在,其他参数正常 【期望结果：通过】   	
    	String userid = "-1";
    	this.userRestypeMappingDao.batchDelete(resTypeList, userid);
    	List<String> list4 = this.userRestypeMappingDao.findUserRestypeList(userId);
    	if(list4 != null && list4.size() > 0){
    		 logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"UserRestypeMappingDaoTest", "Test004BatchDelete4", "pass", "Test004BatchDelete4 test pass"});
    	} else {
			throw new TestException();
		} 	
    	
    	//例5：所有参数正常【期望结果：通过】   	
    	this.userRestypeMappingDao.batchDelete(resTypeList, userId);
    	List<String> list5 = this.userRestypeMappingDao.findUserRestypeList(userId);
    	if(CollectionUtils.isEmpty(list5)){
    		 logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"UserRestypeMappingDaoTest", "Test004BatchDelete5", "pass", "Test004BatchDelete5 test pass"});
    	} else {
			throw new TestException();
		} 
    }
}
