package nd.esp.service.lifecycle.repository.sdk;


import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.model.ResourceTags;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceTagRepository extends ResourceRepository<ResourceTags>,
JpaRepository<ResourceTags, String> {

}