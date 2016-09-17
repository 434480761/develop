package nd.esp.service.lifecycle.services.titan;

import java.util.List;

/**
 * Created by liuran on 2016/6/13.
 */
public interface TitanCommonService {
    public void delete(String type, String id);
    public void batchDelete(String type , List<String> ids);
}
