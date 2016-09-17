package nd.esp.service.lifecycle.support.filters;

import com.nd.gaea.rest.security.authens.UserInfo;
import nd.esp.service.lifecycle.services.securitykey.v06.SecurityKeyService;
import nd.esp.service.lifecycle.services.securitykey.v06.impl.SecurityKeyServiceImpl;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.utils.encrypt.DESUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <p>Title: DataEncryptFilter</p>
 * <p>Description: DataEncryptFilter</p>
 * <p>Copyright: Copyright (c) 2016  </p>
 * <p>Company:ND Co., Ltd.  </p>
 * <p>Create Time: 2016/7/21 </p>
 *
 * @author lanyl
 */
public class DataEncryptFilter implements Filter {

	private static Logger logger = LoggerFactory.getLogger(DataEncryptFilter.class);

	private SecurityKeyService securityKeyService;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void destroy() {
	}



	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		// 过滤无鉴权处理
		if(authentication != null && !"anonymousUser".equals(authentication.getPrincipal().toString())){
			UserInfo userInfo = (UserInfo)authentication.getPrincipal();
			HttpServletRequest httpServletRequest = (HttpServletRequest) request;
			//获取需要进行加密的请求头
			String isEncrypt = httpServletRequest.getHeader("Encrypt");
			if(isEncrypt != null && isEncrypt.equals("true")){
				//重写response
				ResponseWrapper wrapResponse = new ResponseWrapper((HttpServletResponse)response);
				chain.doFilter(request, wrapResponse);
				byte[] data = wrapResponse.getResponseData();
				try{
					//根据用户id获取deskey (deskey超过24小时更换过新的deskey)
					String deskey = this.getSecurityKeyServiceBean().getDesKey(userInfo.getUserId());
					// response数据进行des加密
					String result = DESUtils.encryptData(data, deskey);
					ServletOutputStream out = response.getOutputStream();
					out.write(result.getBytes());
					out.flush();
				}catch (Exception e){
					logger.error("DataEncryptFilter.doFilter->DESUtils.encryptData", e);
					throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR, LifeCircleErrorMessageMapper.EncryptDataFail.getCode()
							, LifeCircleErrorMessageMapper.EncryptDataFail.getMessage());
				}
			}else {
				//请求头没带isEncrypt参数，不进行数据加密处理
				chain.doFilter(request, response);
			}
		}
		// 无鉴权,不走数据加密机制
		else{
			chain.doFilter(request, response);
		}

	}

	/**
	 * 获取SecurityKeyService ps:解决filter无法注入service
	 * @return
	 * @author lanyl
	 */
	private SecurityKeyService getSecurityKeyServiceBean(){
		if (null == securityKeyService) {
			synchronized (this) {
				if (null == securityKeyService) {
					securityKeyService = ContextHolder.getBean(SecurityKeyServiceImpl.class);
				}
			}
		}
		return securityKeyService;
	}

}
