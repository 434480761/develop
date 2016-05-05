package nd.esp.service.lifecycle.repository.sdk;

import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.model.KnowledgeBase;

import org.springframework.data.jpa.repository.JpaRepository;

public interface KnowledgeBaseRepository extends ResourceRepository<KnowledgeBase>,
JpaRepository<KnowledgeBase, String> {

}
