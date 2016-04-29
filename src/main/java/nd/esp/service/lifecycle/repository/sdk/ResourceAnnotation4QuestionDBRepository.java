package nd.esp.service.lifecycle.repository.sdk;

import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.model.ResourceAnnotation;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 资源评注仓储(习题库)
 * 
 * @author caocr
 *
 */
public interface ResourceAnnotation4QuestionDBRepository extends ResourceRepository<ResourceAnnotation>,
JpaRepository<ResourceAnnotation, String>{

}
