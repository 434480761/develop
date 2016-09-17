package nd.esp.service.lifecycle.repository.sdk;

import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.model.ResourceSecurityKey;
import org.springframework.data.repository.Repository;

public interface ResourceSecurityKeyRepository extends ResourceRepository<ResourceSecurityKey>,
        Repository<ResourceSecurityKey, String> {

}
