package nd.esp.service.lifecycle.repository.sdk.categorysync;

import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.model.categorysync.CategorySyncError;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CategorySyncErrorRepository extends ResourceRepository<CategorySyncError>,
JpaRepository<CategorySyncError, String> {

}