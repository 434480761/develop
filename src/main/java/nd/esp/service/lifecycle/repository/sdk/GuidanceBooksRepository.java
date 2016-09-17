package nd.esp.service.lifecycle.repository.sdk;

import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.model.GuidanceBooks;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GuidanceBooksRepository extends ResourceRepository<GuidanceBooks>,
JpaRepository<GuidanceBooks, String> {

}
