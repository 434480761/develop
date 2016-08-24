package nd.esp.service.lifecycle.repository.sdk;

import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.model.CoverageSharing;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 库分享仓储
 * @author xiezy
 * @date 2016年8月24日
 */
public interface CoverageSharingRepository extends ResourceRepository<CoverageSharing>,
JpaRepository<CoverageSharing, String> {

}