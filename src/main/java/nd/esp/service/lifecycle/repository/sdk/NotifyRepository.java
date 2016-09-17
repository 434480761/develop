package nd.esp.service.lifecycle.repository.sdk;


import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.model.NotifyModel;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotifyRepository extends ResourceRepository<NotifyModel>,JpaRepository<NotifyModel, String> {
	
}
