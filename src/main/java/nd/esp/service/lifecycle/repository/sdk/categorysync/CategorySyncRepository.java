package nd.esp.service.lifecycle.repository.sdk.categorysync;

import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.model.categorysync.CategorySync;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CategorySyncRepository extends ResourceRepository<CategorySync>,
JpaRepository<CategorySync, String> {

}