package nd.esp.service.lifecycle.support.aop;

import javax.servlet.http.HttpServletRequest;

import nd.esp.service.lifecycle.entity.elasticsearch.Resource;
import nd.esp.service.lifecycle.services.elasticsearch.AsynEsResourceService;
import nd.esp.service.lifecycle.services.offlinemetadata.OfflineService;
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

/**
 * 在controller 层的更新，创建，删除接口上添加注解 只有拥有上传能力的资源类型（通过commonserviceHelper 来判断)
 *
 * @author linsm
 */
@Aspect
@Component
@Order(10002)
public class OfflineMetadataAspect {

	private final static Logger LOG = LoggerFactory
			.getLogger(OfflineMetadataAspect.class);

	@Autowired
	private OfflineService offlineService;
	@Autowired
    private AsynEsResourceService esResourceOperation;

	@Autowired
	private HttpServletRequest request;

	// @Autowired CommonServiceHelper commonServiceHelper;

	@Pointcut("@annotation(nd.esp.service.lifecycle.support.annotation.MarkAspect4OfflineJsonToCS)")
	public void performanceAnnon() {

	}

	@AfterReturning("performanceAnnon()")
	public void executeAnnon(JoinPoint point) {

		try {
			String url = request.getRequestURI().toString();
			// http://localhost:8080/esp-lifecycle/v0.6/assets/3f549825-c2cd-4359-55fa-1c33e5112111
			LOG.info("url :{}", url);

			if (StringUtils.isNotEmpty(url)) {
				String[] pathChunks = url.split("/");
				if (pathChunks != null && pathChunks.length >= 2) {

					// 迁移到service中
					// commonServiceHelper.assertUploadable(pathChunks[pathChunks.length
					// - 2]);
					// offline metadata.json to cs
					String resourceType = pathChunks[pathChunks.length - 2];
					String uuid = pathChunks[pathChunks.length - 1];
					offlineService.writeToCsAsync(resourceType, uuid);
					// offline metadata(coverage) to elasticsearch
					if (ResourceTypeSupport.isValidEsResourceType(resourceType)) {
						esResourceOperation.asynAdd(
								new Resource(resourceType, uuid));
					}
					
				}

			}
		} catch (Exception e) {

			LOG.error("failed to execute the offline data aspect:{}",
					e.getMessage());
		}

	}

}
