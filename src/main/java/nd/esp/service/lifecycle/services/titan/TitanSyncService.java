package nd.esp.service.lifecycle.services.titan;

/**
 * Created by liuran on 2016/7/6.
 */
public interface TitanSyncService {
    boolean deleteResource(String primaryCategory, String identifier);
    boolean reportResource(String primaryCategory, String identifier);
}
