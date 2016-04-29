package nd.esp.service.lifecycle.repository.sdk;


import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.model.Asset;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetRepository extends ResourceRepository<Asset>,JpaRepository<Asset, String> {
	
}
