/* =============================================================
 * Created: [2015年11月5日] by linsm
 * =============================================================
 *
 * Copyright 2014-2015 NetDragon Websoft Inc. All Rights Reserved
 *
 * =============================================================
 */

package nd.esp.service.lifecycle.services.offlinemetadata;

import java.util.Set;


/**
 * @author linsm
 * @since 
 *
 */
public interface OfflineService {
    
    Boolean writeToCsAsync(String resType, String uuid);
    
    Boolean writeToCsSync(String resType, String uuid);
    
    Boolean batchWriteToCsAsync(String resType, Set<String> uuidSet);

}
