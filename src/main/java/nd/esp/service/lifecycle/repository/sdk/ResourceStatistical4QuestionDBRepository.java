package nd.esp.service.lifecycle.repository.sdk;

import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.model.ResourceStatistical;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceStatistical4QuestionDBRepository extends ResourceRepository<ResourceStatistical>, JpaRepository<ResourceStatistical, String> {

}
