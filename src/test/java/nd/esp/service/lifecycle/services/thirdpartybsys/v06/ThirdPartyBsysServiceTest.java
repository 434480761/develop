package nd.esp.service.lifecycle.services.thirdpartybsys.v06;

import nd.esp.service.lifecycle.BaseControllerConfig;
import nd.esp.service.lifecycle.utils.common.TestException;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * <p>Title: Service层ThirdPartyBsysService测试  </p>
 * <p>Description: ThirdPartyBsysServiceTest </p>
 * <p>Copyright: Copyright (c) 2016     </p>
 * <p>Company: ND Co., Ltd.       </p>
 * <p>Create Time: 2016年7月1日           </p>
 * <p>MethodSorters 按字母顺序 TEST若有顺序要求，法名称书写 </p>
 * @author lianggz
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ThirdPartyBsysServiceTest extends BaseControllerConfig {
    
    private final static Logger LOG = LoggerFactory.getLogger(ThirdPartyBsysServiceTest.class);
    
    @Autowired
    @Qualifier("ThirdPartyBsysServiceImpl")
    private ThirdPartyBsysService thirdPartyBsysService;

    
    /**
     * 用例：查询第三方服务
     * @throws Exception
     * @author lanyl
     */
    @Test
    public void test001checkThirdPartyBsys() throws Exception {
        //例1：userId 为空 【期望结果：通过】
        if(!this.thirdPartyBsysService.checkThirdPartyBsys(null)) {
            LOG.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"ThirdPartyBsysServiceTest", "test001checkThirdPartyBsys",
                    "pass", "test001checkThirdPartyBsys test pass"});
        } else {
            throw new TestException();
        }

        //例2：userId 不为空 值为不存在 【期望结果：通过】
        if(!this.thirdPartyBsysService.checkThirdPartyBsys("-1")){
            LOG.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"ThirdPartyBsysServiceTest", "test001checkThirdPartyBsys2",
                    "pass", "test001checkThirdPartyBsys test pass"});
        } else {
            throw new TestException();
        }

        //例3：userId 不为空 值为存在 【期望结果：通过】
        if(this.thirdPartyBsysService.checkThirdPartyBsys("2080538299")){
            LOG.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"ThirdPartyBsysServiceTest", "test001checkThirdPartyBsys3",
                    "pass", "test001checkThirdPartyBsys test pass"});
        } else {
            throw new TestException();
        }
    }

}
