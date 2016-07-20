package nd.esp.service.lifecycle.services.usercoveragemapping.v06;

import java.util.List;

import nd.esp.service.lifecycle.BaseControllerConfig;
import nd.esp.service.lifecycle.models.UserCoverageMappingModel;
import nd.esp.service.lifecycle.utils.common.TestException;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.google.common.collect.ImmutableList;

/**
 * <p>Title: Service层UserCoverageMapping测试  </p>
 * <p>Description: UserCoverageMappingServiceTest </p>
 * <p>Copyright: Copyright (c) 2016     </p>
 * <p>Company: ND Co., Ltd.       </p>
 * <p>Create Time: 2016年7月1日           </p>
 * <p>MethodSorters 按字母顺序 TEST若有顺序要求，要注意方法名称书写 </p>
 * @author lianggz
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UserCoverageMappingServiceTest extends BaseControllerConfig {
    
    private final static Logger LOG = LoggerFactory.getLogger(UserCoverageMappingServiceTest.class);
    
    private static List<String> coverageList;
    
    @Autowired
    @Qualifier("UserCoverageMappingServiceImpl")
    private UserCoverageMappingService userCoverageMappingService;

    /**
     * 用例： 批量插入用户请求类型映射关系
     * @throws Exception
     * @author lianggz
     */
    @Test
    public void test001AddUserRestypeMappings() throws Exception {
        //例1： 新增用户请求类型映射关系 ，其他参数正确  【期望结果：正确】
        coverageList = ImmutableList.<String>of("Org/nd");
        try {
            this.userCoverageMappingService.addUserCoverageMappings(coverageList, this.userId);
            LOG.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"UserCoverageMappingServiceTest", "test001AddUserRestypeMappings", 
                    "pass", "test001AddUserRestypeMappings test pass"});
        } catch (Exception e) {
            throw new TestException();
        }
    }
    
    /**
     * 用例：查询用户覆盖类型映射关系信息列表
     * @throws Exception
     * @author lianggz
     */
    @Test
    public void test002FindUserRestypeList() throws Exception {
        //例1： 查询用户覆盖类型映射关系信息列表，其他参数正确  【期望结果：正确】
        List<String> list = this.userCoverageMappingService.findUserCoverageList(this.userId);
        if(list != null && list.size() > 0) {
            LOG.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"UserCoverageMappingServiceTest", "test002FindUserRestypeList", 
                    "pass", "test002FindUserRestypeList test pass"});
        } else {
            throw new TestException();
        }
    }

    /**
     * 用例：查询用户覆盖类型映射关系信息列表
     * @throws Exception
     * @author lianggz
     */
    @Test
    public void test003FindUserCoverageMappingModelList() throws Exception {
        //例1：查询用户覆盖类型映射关系信息列表 ，其他参数正确  【期望结果：正确】
        List<UserCoverageMappingModel> list = this.userCoverageMappingService.findUserCoverageMappingModelList(ImmutableList.<String>of(this.userId));
        if(list != null && list.size() > 0) {
            LOG.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"UserCoverageMappingServiceTest", "test003FindUserCoverageMappingModelList", 
                    "pass", "test003FindUserCoverageMappingModelList test pass"});
        } else {
            throw new TestException();
        }
    }

    /**
     * 用例：批量删除用户覆盖类型映射关系
     * @throws Exception
     * @author lianggz
     */
    @Test
    public void test004DeleteUserCoverageMappings() throws Exception {
        //例1：批量删除用户请求类型映射关系 ，其他参数正确  【期望结果：正确】
        try {
            this.userCoverageMappingService.deleteUserCoverageMappings(coverageList, userId);
            LOG.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"UserCoverageMappingServiceTest", "test004DeleteUserCoverageMappings", 
                    "pass", "test004DeleteUserCoverageMappings test pass"});
        } catch (Exception e) {
            throw new TestException();
        }
    }
}
