package nd.esp.service.lifecycle.repository.sdk;

import org.springframework.data.repository.Repository;

import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.model.ThirdPartyBsys;

public interface ThirdPartyBsysRepository extends ResourceRepository<ThirdPartyBsys>,
        Repository<ThirdPartyBsys, String> {

}
