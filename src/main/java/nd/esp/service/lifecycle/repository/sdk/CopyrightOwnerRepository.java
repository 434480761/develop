package nd.esp.service.lifecycle.repository.sdk;

import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.model.CopyrightOwner;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 资源版权方仓储
 * @author xiezy
 * @date 2016年8月15日
 */
public interface CopyrightOwnerRepository extends ResourceRepository<CopyrightOwner>,
JpaRepository<CopyrightOwner, String> {

}