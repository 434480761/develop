/* =============================================================
 * Created: [2015年11月6日] by linsm
 * =============================================================
 *
 * Copyright 2014-2015 NetDragon Websoft Inc. All Rights Reserved
 *
 * =============================================================
 */

package nd.esp.service.lifecycle.services.offlinemetadata;

import java.util.Set;

import nd.esp.service.lifecycle.educommon.services.impl.CommonServiceHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * @author linsm
 * @since 
 *
 */
@Service
public class OfflineServiceImplWithCheckUpload extends OfflineServiceImpl {
    
    private final static Logger LOG = LoggerFactory.getLogger(OfflineServiceImplWithCheckUpload.class);
    
    @Autowired
    private CommonServiceHelper commonServiceHelper;

    /* (non-Javadoc)
     * @see nd.esp.service.lifecycle.services.offlinemetadata.service.OfflineService#writeToCs(java.lang.String, java.lang.String)
     */
    @Override
    public Boolean writeToCsAsync(String resType, String uuid) {
        
        try {
            
            commonServiceHelper.assertUploadable(resType);
            return super.writeToCsAsync(resType, uuid);
            
        } catch (Exception e) {
            
            LOG.error("failed to execute the offline json data to cs aspect:{}",e.getMessage());
        }
        return false;
    }
    
    
    @Override
    public Boolean batchWriteToCsAsync(String resType, Set<String> uuidSet) {
        try {

            commonServiceHelper.assertUploadable(resType);
            return super.batchWriteToCsAsync(resType, uuidSet);

        } catch (Exception e) {

            LOG.error("failed to execute the offline json data to cs aspect:{}", e.getMessage());
        }
        return false;
    }

}
