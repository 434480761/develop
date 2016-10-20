package nd.esp.service.lifecycle.support.aop;

import javax.servlet.http.HttpServletRequest;

import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;
import nd.esp.service.lifecycle.support.logs.DBLogUtil;
import nd.esp.service.lifecycle.utils.StringUtils;

import org.apache.log4j.MDC;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import com.nd.gaea.rest.security.authens.UserInfo;

/**
 * 资源操作日志切面
 * @author xuzy
 *
 */
@Aspect
@Component
@Order(10005)
public class OperateAspect {
	@Autowired
	private HttpServletRequest request;
	
	@Pointcut("execution(* nd.esp.service.lifecycle.controllers..*Controller*.*(..))")
	private void performanceAnnon(){}
	
	@AfterReturning(value="performanceAnnon()",returning="result")
	public void afterReturn(JoinPoint joinPoint,Object result){
		String uri = request.getRequestURI();
		//判断uri是否应该忽略
		boolean ignoreUriFlag = isIgnoreUri(uri);
		if(ignoreUriFlag){
			return;
		}
		
		Authentication authentication  = SecurityContextHolder.getContext().getAuthentication();
		if(authentication != null){
			UserInfo ui = (UserInfo)authentication.getPrincipal();
			if(ui != null){
				//找出token中的userId
				String userId = ui.getUserId();
				if(StringUtils.isNotEmpty(userId)){
					//操作的资源类型
					String resType = null;
					//资源id
					String uuid = null;
					//http方法请求类型
					String method = request.getMethod();
					if(RequestMethod.POST.toString().equals(method) || RequestMethod.PUT.toString().equals(method) || RequestMethod.DELETE.toString().equals(method)){
						String[] pathChunks = uri.split("/");
						int index = 0;
						for (int i = 0 ; i < pathChunks.length ; i++) {
							if("v0.6".equalsIgnoreCase(pathChunks[i])){
								index = i;
								break;
							}
						}
						
						if(index > 0){
							resType = pathChunks[index + 1];
							if(pathChunks.length > index + 2){
								uuid = pathChunks[index + 2];
							}else if(result != null && result instanceof ResourceViewModel){
								ResourceViewModel rvm = (ResourceViewModel)result;
								uuid = rvm.getIdentifier();
							}
						}
					}
					
					if(StringUtils.isNotEmpty(resType) && StringUtils.isNotEmpty(uuid)){
						//保存到数据库
						saveLog(resType,uuid,method,userId,uri);
					}
				}
			}
		}
	}
	
	/**
	 * 是否为忽略的uri路径
	 * @param uri
	 * @return
	 */
	private boolean isIgnoreUri(String uri){
		//排除覆盖范围、资源关系、维度、提供商、访问控制、密码分享、版本管理、资源评注、资源置顶、资源生命周期相关接口
		if (uri.contains("relations") || uri.contains("coverages")
				|| uri.contains("categories")
				|| uri.contains("categorypatterns") || uri.contains("provider")
				|| uri.contains("3dbsys") || uri.contains("public")
				|| uri.contains("newversion") || uri.contains("annotations")
				|| uri.contains("top") || uri.contains("statisticals")
				|| uri.contains("steps")) {
			return true;
		}
		return false;
	}
	
	/**
	 * 将结果保存至数据库
	 * @param resType	资源类型
	 * @param uuid		资源id
	 * @param method	http方法
	 * @param userId	操作者id
	 */
	private void saveLog(String resType,String uuid,String method,String userId,String uri){
		MDC.put("resource", uuid);
        MDC.put("res_type", resType);
        MDC.put("operation_type", method);
        MDC.put("creator", userId);
        MDC.put("remark", uri);
        DBLogUtil.getDBlog().info("");
        MDC.clear();
	}
}
