package nd.esp.service.lifecycle.repository.sdk;

import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.model.TitanSync;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by liuran on 2016/6/29.
 */
public interface TitanSyncRepository extends ResourceRepository<TitanSync>,
        JpaRepository<TitanSync, String> {
}
