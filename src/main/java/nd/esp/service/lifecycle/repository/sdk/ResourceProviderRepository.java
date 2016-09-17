package nd.esp.service.lifecycle.repository.sdk;

import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.model.ResourceProvider;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 资源提供商仓储
 * @author xiezy
 * @date 2016年8月15日
 */
public interface ResourceProviderRepository extends ResourceRepository<ResourceProvider>,
JpaRepository<ResourceProvider, String> {

}