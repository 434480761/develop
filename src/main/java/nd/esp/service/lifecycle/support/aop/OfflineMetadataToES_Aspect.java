package nd.esp.service.lifecycle.support.aop;

import javax.servlet.http.HttpServletRequest;

import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;
import nd.esp.service.lifecycle.entity.elasticsearch.Resource;
import nd.esp.service.lifecycle.services.elasticsearch.AsynEsResourceService;
import nd.esp.service.lifecycle.support.busi.elasticsearch.ResourceTypeSupport;
import nd.esp.service.lifecycle.utils.StringUtils;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 在controller 层的更新，创建，删除接口上添加注解 只有拥有上传能力的资源类型（通过commonserviceHelper 来判断)
 *
 * @author linsm
 */
@Aspect
@Component
@Order(10002)
public class OfflineMetadataToES_Aspect {

	private final static Logger LOG = LoggerFactory
			.getLogger(OfflineMetadataToES_Aspect.class);
	@Autowired
	private HttpServletRequest request;
	@Autowired
    private AsynEsResourceService esResourceOperation;

	@Pointcut("@annotation(nd.esp.service.lifecycle.support.annotation.MarkAspect4OfflineToES)")
	public void performanceAnnon() {

	}

	@AfterReturning(pointcut = "performanceAnnon()", returning = "result")
	public void executeAnnon(JoinPoint point, Object result) {

		String uuid = null;
		String resourceType = null;
		String url = request.getRequestURI().toString();
		// http://localhost:8080/esp-lifecycle/v0.6/assets/3f549825-c2cd-4359-55fa-1c33e5112111
		LOG.info("url :{}", url);
		String httpMethod = request.getMethod();
		if (StringUtils.isNotEmpty(url)) {
			String[] pathChunks = url.split("/");
			if (pathChunks != null) {
				if (RequestMethod.POST.toString().equals(httpMethod)) {
					if (pathChunks.length >= 1) {
						resourceType = pathChunks[pathChunks.length - 1];
						if (result != null
								&& result instanceof ResourceViewModel) {
							ResourceViewModel resourceViewModel = (ResourceViewModel) result;
							uuid = resourceViewModel.getIdentifier();
						}
					}

				} else if (RequestMethod.PUT.toString().equals(httpMethod)) {
					if (pathChunks.length >= 2) {
						resourceType = pathChunks[pathChunks.length - 2];
						uuid = pathChunks[pathChunks.length - 1];
					}
				}
			}

		}

		// 在common service 层做切面的处理
		// Object[] argsObjects = point.getArgs();
		// if (argsObjects != null) {
		// int argLength = argsObjects.length;
		// if (argLength == 2) {
		// Object objectTwo = argsObjects[1];
		//
		// if (objectTwo instanceof ResourceModel) {
		// // service create or update
		// uuid = ((ResourceModel) objectTwo).getIdentifier();
		// } else if (objectTwo instanceof String) {
		// // delete
		// uuid = (String) objectTwo;
		// }
		// Object objectOne = argsObjects[0];
		// if (objectOne instanceof String) {
		// resourceType = (String) objectOne;
		// }
		//
		// }
		//
		// }
		if (ResourceTypeSupport.isValidEsResourceType(resourceType)
				&& StringUtils.isNotEmpty(uuid)) {
			esResourceOperation.asynAdd(
					new Resource(resourceType, uuid));
		}

	}

}
