package nd.esp.service.lifecycle.services.impls;

import java.util.HashMap;
import java.util.Map;
import nd.esp.service.lifecycle.models.AccessModel;
import nd.esp.service.lifecycle.services.CSFileService;
import nd.esp.service.lifecycle.support.Constant;
import nd.esp.service.lifecycle.support.Constant.CSInstanceInfo;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.nd.gaea.client.http.WafSecurityHttpClient;

/**
 * CS文件上传
 * 
 * <br>Created 2015年5月13日 下午4:05:47
 * @version  
 * @author   linsm		
 *
 * @see 	 
 * 
 * Copyright(c) 2009-2014, TQ Digital Entertainment, All Rights Reserved
 *
 */
@Service("CSFileUploadServiceImpl")
public class CSFileServiceImpl implements CSFileService {
	
	private static final Logger LOG = LoggerFactory.getLogger(CSFileServiceImpl.class);

    /**
	 * 调用cs接口获取 session
	 * 
	 * <br>Created 2015年5月13日 下午4:07:34
	 * @param param post 请求的相关参数
	 * @return session
	 * @author       linsm
	 */
	private String getSessionIdFromCS(String url, CSGetSessionParam param){
		Map<String, Object> requestBody = new HashMap<String, Object>();
        requestBody.put("path",param.getPath());
        requestBody.put("service_id", param.getServiceId());
        requestBody.put("uid",param.getUid());
        requestBody.put("role",param.getRole());
        if(param.getExpires() != null){
        	requestBody.put("expires",param.getExpires());//optional
        }
        
        WafSecurityHttpClient wafSecurityHttpClient = new WafSecurityHttpClient();
//        String url = Constant.CS_API_URL + "/sessions";
        
        LOG.debug("调用cs获取session接口");
        
        String sessionId ="";
        try {
            Map<String, String> session = wafSecurityHttpClient.post( url, requestBody, Map.class);
            sessionId = session.get("session");
        } catch (Exception e) {
            
            LOG.error(LifeCircleErrorMessageMapper.InvokingCSFail.getMessage(), e);

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.InvokingCSFail.getCode(),
                                          e.getLocalizedMessage());
        }
        return sessionId;
	}

	/**
	 * cs获取 session 相关参数
	 * 
	 * <br>Created 2015年5月13日 下午4:08:22
	 * @version  CSFileUploadServiceImpl
	 * @author   linsm		
	 *
	 * @see 	 
	 * 
	 * Copyright(c) 2009-2014, TQ Digital Entertainment, All Rights Reserved
	 *
	 */
	private static class CSGetSessionParam{
		/**
		 * 路径
		 */
		private String path;
		/**
		 * 服务id
		 */
		private String serviceId;
		/**
		 * 用户id
		 */
		private String uid;
		/**
		 * 角色
		 */
		private String role;
		/**
		 * 过期时间(秒)
		 */
		private Integer expires;
		
		public String getPath() {
			return path;
		}
		public void setPath(String path) {
			this.path = path;
		}
		public String getServiceId() {
			return serviceId;
		}
		public void setServiceId(String serviceId) {
			this.serviceId = serviceId;
		}
		public String getUid() {
			return uid;
		}
		public void setUid(String uid) {
			this.uid = uid;
		}
		public String getRole() {
			return role;
		}
		public void setRole(String role) {
			this.role = role;
		}
		public Integer getExpires() {
			return expires;
		}
		public void setExpires(Integer expires) {
			this.expires = expires;
		}
	}

    /* (non-Javadoc)
     * @see nd.esp.service.lifecycle.services.CSFileService#getUploadUrl(java.lang.String, java.lang.String)
     */
    @Override
    public AccessModel getPlayerUploadUrl(String uid, String coverage) {
        CSInstanceInfo csInfo = null;
        if(StringUtils.isEmpty(coverage)){
            //个人
            csInfo = Constant.CS_INSTANCE_MAP.get(Constant.CS_DEFAULT_INSTANCE);
        }else{
            //nd
            csInfo = Constant.CS_INSTANCE_MAP.get(Constant.CS_DEFAULT_INSTANCE_OTHER);
        }
        if (csInfo == null) {
            // 抛出异常
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.CSInstanceKeyNotFound.getCode(),
                                          LifeCircleErrorMessageMapper.CSInstanceKeyNotFound.getMessage() + " cs配制有误");
        }
        
        String path = csInfo.getPath() + "/players";
        String url = csInfo.getUrl() + "/sessions";
        
        LOG.debug("dis_path:"+path);
        
        CSGetSessionParam param = new CSGetSessionParam();
        param.setServiceId(csInfo.getServiceId());
        param.setPath(path);
        param.setUid(uid);
        param.setRole(Constant.FILE_OPERATION_ROLE);
        param.setExpires(Constant.FILE_OPERATION_EXPIRETIME);

        //获得session
        String sessionid = getSessionIdFromCS(url,param);
        
        LOG.debug("session:"+sessionid);
        
        AccessModel accessModel = new AccessModel();
        accessModel.setAccessKey(Constant.FILE_OPERATION_ACCESSKEY);
        accessModel.setAccessUrl(csInfo.getUrl()+"/upload"); 
        accessModel.setPreview(new HashMap<String,String>());
        accessModel.setExpireTime(CommonHelper.fileOperationExpireDate());
        accessModel.setSessionId(sessionid);
        accessModel.setDistPath(path);
        
        return accessModel;
    }

}
