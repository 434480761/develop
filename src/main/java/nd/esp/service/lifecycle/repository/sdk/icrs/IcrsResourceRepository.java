package nd.esp.service.lifecycle.repository.sdk.icrs;

import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.model.icrs.IcrsResource;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 智慧课堂的数据统计仓储
 * @author xiezy
 * @date 2016年9月12日
 */
public interface IcrsResourceRepository extends ResourceRepository<IcrsResource>,
JpaRepository<IcrsResource, String> {

}