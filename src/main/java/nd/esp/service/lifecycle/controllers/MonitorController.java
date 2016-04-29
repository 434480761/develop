/* =============================================================
 * Created: [2015年6月19日] by linsm
 * =============================================================
 *
 * Copyright 2014-2015 NetDragon Websoft Inc. All Rights Reserved
 *
 * =============================================================
 */

package nd.esp.service.lifecycle.controllers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import nd.esp.service.lifecycle.support.Constant;
import nd.esp.service.lifecycle.vos.MonitorStatusViewModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.Ebook;
import nd.esp.service.lifecycle.repository.sdk.EbookRepository;
import com.nd.gaea.client.http.WafSecurityHttpClient;

/**
 * @author linsm
 * @since
 */
@RequestMapping("/v0.3/system/status")
@RestController
public class MonitorController {
    
    private final Logger LOG = LoggerFactory.getLogger(MonitorController.class);
    
    @Autowired
    EbookRepository repository;


    /**
     * 应用监控服务接口
     * 
     * @return
     * @since http://wiki.sdp.nd/index.php?title=%E5%BA%94%E7%94%A8%E7%9B%91%E6%
     *        8E%A7%E6%9C%8D%E5%8A%A1%E6%8E%A5%E5%8F%A3-%E8%BE%93%E5%85%A5%E5%92%8C%E8%BE%93%E5%87%BA
     */
    @RequestMapping(value = "", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody MonitorStatusViewModel requestStatus() {
        MonitorStatusViewModel status = new MonitorStatusViewModel();
        int serviceActiveNum = 0;// 记录成功服务的个数

        LOG.info("mySql服务 验证开始：");
        if (isMySqlServiceActive()) {
            serviceActiveNum++;
            LOG.info("mySql服务 验证成功");
        } else {
            LOG.info("mySql服务 验证失败");
        }
        LOG.info("mySql服务 验证结束");

        LOG.info("CS服务 验证开始：");
        if (isCsServiceActive()) {
            serviceActiveNum++;
            LOG.info("CS服务 验证成功");
        } else {
            LOG.info("CS服务 验证失败");
        }
        LOG.info("CS服务 验证结束");

//        logger.info("TransCodeTask服务 验证开始：");
//        if (isTransCodeTaskActive()) {
//            serviceActiveNum++;
//            logger.info("TransCodeTask服务 验证成功");
//        } else {
//            logger.info("TransCodeTask服务 验证失败");
//        }
//        logger.info("TransCodeTask服务 验证结束");

        // 给出最终的结果：一个失败，就是失败
        if (serviceActiveNum == MonitorStatusViewModel.SERVICE_NUM) {
            status.setStatus(MonitorStatusViewModel.STATUS_OK);
        } else {
            status.setStatus(MonitorStatusViewModel.STATUS_EXCEPTION);
        }
        return status;
    }

//    /**
//     * 通过一个空的转码任务来判断转码调试是否可用
//     * 
//     * @return
//     * @since
//     */
//    private boolean isTransCodeTaskActive() {
//        // String location = Constant.CS_INSTANCE_MAP.get(Constant.CS_DEFAULT_INSTANCE).getUrl()
//        // + "/download?dentryId=" + fid + "&session=" + session;
//        String url = LifeCircleApplicationInitializer.properties.getProperty("task_submit_utl");
//        Map<String, String> parameters = new HashMap<String, String>();
//        // Map<String, String> arg = new HashMap<>();
//        // arg.put("location", "");
//        // String argument = ObjectUtils.toJson(arg);
//        // parameters.put("argument", argument);
//        // parameters.put("priority", ""+priority);
//        // parameters.put("service", Constant.WORKER_TRANSCODE_SERVICE);
//        // parameters.put("openID", identifier);
//        // HttpClientUtils.httpPost(url, parameters);
//        try {
//            RestTemplate restTemplate = new RestTemplate();
//            restTemplate.postForEntity(url, parameters, String.class);
//        } catch (HttpServerErrorException e) {
//            logger.error(e);
//            return true;   //FIXME
//        }catch(Exception e){
//            logger.error(e);
//            return false;
//        }
//        return true;
//    }

    /**
     * 通过获取session来判断CS 是否可用
     * 
     * @return
     * @since
     */
    private boolean isCsServiceActive() {
        Constant.CSInstanceInfo csSettingInfo = Constant.CS_INSTANCE_MAP.get(Constant.CS_DEFAULT_INSTANCE);
        Map<String, Object> requestBody = new HashMap<String, Object>();
        requestBody.put("path", csSettingInfo.getPath());
        requestBody.put("service_id", csSettingInfo.getServiceId());
        requestBody.put("uid", Constant.FILE_OPERATION_USERNAME);
        requestBody.put("role", Constant.FILE_OPERATION_ROLE);
        WafSecurityHttpClient wafSecurityHttpClient = new WafSecurityHttpClient();
//        // String url = Constant.CS_API_URL + "/sessions";
//        Map<String, String> session = wafSecurityHttpClient.post(url, requestBody, Map.class);
//        RestTemplate restTemplate = new RestTemplate();
        try {
            wafSecurityHttpClient.post(csSettingInfo.getUrl() + "/sessions", requestBody, Map.class);
        } catch (Exception e) {
            LOG.warn("CS调用异常",e);
            return false;
        }
        return true;
    }

    /**
     * 通过调用sdk来间接判断mySql是否可用
     * 
     * @return
     * @since
     */
    private boolean isMySqlServiceActive() {
        String uuid = UUID.randomUUID().toString();
        Ebook beanParam = new Ebook();
        beanParam.setIdentifier(uuid);
        Ebook beanResult = null;
        try {
            beanResult = repository.add(beanParam);
        } catch (EspStoreException e) {
            LOG.warn("SQL服务异常",e);
            return false;
        }
        if (!beanParam.getIdentifier().equals(beanResult.getIdentifier())) {
            return false;
        }
        try {
            repository.del(uuid);
        } catch (EspStoreException e) {
            LOG.warn("SQL服务异常",e);
            return false;
        }
        return true;
    }

}
