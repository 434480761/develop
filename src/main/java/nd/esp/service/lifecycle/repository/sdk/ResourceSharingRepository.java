package nd.esp.service.lifecycle.repository.sdk;

import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.model.ResourceSharing;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 资源分享仓储
 * @author xiezy
 * @date 2016年8月24日
 */
public interface ResourceSharingRepository extends ResourceRepository<ResourceSharing>,
JpaRepository<ResourceSharing, String> {

}