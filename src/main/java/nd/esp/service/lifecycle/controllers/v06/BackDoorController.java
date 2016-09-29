package nd.esp.service.lifecycle.controllers.v06;

import javax.servlet.http.HttpServletRequest;

import nd.esp.service.lifecycle.support.Constant;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @title 后面API controller
 * @desc
 * @atuh lwx
 * @createtime on 2015/11/6 15:14
 * @all 后面API接口只对LC开发和测试使用，原则上，不允许让外部知道并使用
 */
@RestController
@RequestMapping("/v0.1/backdoor/")
public class BackDoorController {
    private static final Logger LOG = LoggerFactory.getLogger(BackDoorController.class);


    @Autowired
    private HttpServletRequest httpServletRequest;


    final LifeCircleErrorMessageMapper StoreSdkFail = LifeCircleErrorMessageMapper.StoreSdkFail;
    final String LC_BACK_DOOR_SECRET_KEY = "lc_back_door";


    /**
     * 关闭后门开关
     *
     * @return
     */
    @RequestMapping(value = "closeBackDoor", method = RequestMethod.GET)
    public void changeQaDataSwitch() {

        Constant.BACK_DOOR_OPEN = false;

    }

    /**
     * 开启后门开关
     *
     * @param flag
     * @return
     */
    @RequestMapping(value = "open_door_back_1", method = RequestMethod.GET)
    public void changeQaDataSwitch(@RequestParam String flag) {
        String secret = httpServletRequest.getHeader("secret");
        if (LC_BACK_DOOR_SECRET_KEY.equals(secret)) {
            Constant.BACK_DOOR_OPEN = true;
        } else {
            LOG.info("尝试开启后门开关失败");
            throw new LifeCircleException(HttpStatus.FORBIDDEN, "LC/FORBIDDEN_URL_REQUEST", "fuck you, this url cannot request");

        }


    }

    /**
     * 模拟sdk失败异常请求
     */
    @RequestMapping(value = "mockSdkException", method = RequestMethod.GET)
    public void mockSdkException() {

        if (Constant.BACK_DOOR_OPEN) {
            LOG.info("模拟触发sdk调用失败");
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR, StoreSdkFail);
        }

    }

    /**
     * 提高worker优先级
     *
     * @param uuid 资源UUID
     * @param resType 资源类型
     * @param priority 优先级，最高10
     */
    @RequestMapping(value = "{res_type}/{uuid}", method = RequestMethod.GET)
    public void mockSdkException(@PathVariable(value = "uuid") String uuid,
                                 @PathVariable(value = "res_type") String resType,
                                 @RequestParam(value = "priority", required = false, defaultValue = "3") int priority) {


        int change_priority=priority>10?10:priority;

        //调用API 或则链接数据库



    }


}
