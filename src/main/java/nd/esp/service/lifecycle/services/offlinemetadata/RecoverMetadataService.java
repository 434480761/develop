/* =============================================================
 * Created: [2015年11月18日] by linsm
 * =============================================================
 *
 * Copyright 2014-2015 NetDragon Websoft Inc. All Rights Reserved
 *
 * =============================================================
 */

package nd.esp.service.lifecycle.services.offlinemetadata;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;


/**
 * @author linsm
 * @since 
 *
 */
public interface RecoverMetadataService {
    
    /**
     * @author linsm
     * @param resourceType
     * @param identifiers  需要关心顺序
     * @return
     * @since
     */
    void recoverMetadata(String resourceType, List<String> identifiers);
    
    int recoverMetaData(String resourceType, BigDecimal lastUpdate);
    
    void recoverMetaData(Timestamp   lastUpDate);

}
