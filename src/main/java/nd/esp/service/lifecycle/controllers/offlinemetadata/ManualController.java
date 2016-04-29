/* =============================================================
 * Created: [2015年11月18日] by linsm
 * =============================================================
 *
 * Copyright 2014-2015 NetDragon Websoft Inc. All Rights Reserved
 *
 * =============================================================
 */

package nd.esp.service.lifecycle.controllers.offlinemetadata;

import java.math.BigDecimal;
import java.util.List;

import nd.esp.service.lifecycle.educommon.services.impl.CommonServiceHelper;
import nd.esp.service.lifecycle.services.offlinemetadata.RecoverMetadataService;
import nd.esp.service.lifecycle.utils.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author linsm
 * @since
 */
@RestController
@RequestMapping("/v0.6/metadata")
public class ManualController {
    
    @Autowired
    private RecoverMetadataService recoverMetadataService;
    
    @Autowired
    private CommonServiceHelper commonServiceHelper;

    @RequestMapping(value = "/actions/recovermetadata/{res_type}", method = RequestMethod.GET)
    @ResponseBody
    int recoverMetadata(@PathVariable(value = "res_type") String resourceType,
                        @RequestParam(value = "last_update", defaultValue = "") String lastUpdate) {
        
        commonServiceHelper.assertUploadable(resourceType);
        long upToLastUpdate = 0L;
        if(StringUtils.isEmpty(lastUpdate)){
            upToLastUpdate= System.currentTimeMillis();
        }else{
            upToLastUpdate = Long.valueOf(lastUpdate);
        }
        return recoverMetadataService.recoverMetaData(resourceType, BigDecimal.valueOf(upToLastUpdate));
    }
    
    
    @RequestMapping(value = "/actions/recovermetadata/{res_type}", method = RequestMethod.POST)
    void recoverMetadataForIds(@PathVariable(value = "res_type") String resourceType,@RequestBody List<String> identifiers) {
        
        commonServiceHelper.assertUploadable(resourceType);
        
        recoverMetadataService.recoverMetadata(resourceType, identifiers);
    }
    
    @RequestMapping(value = "/actions/recovermetadata", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE })
    @ResponseBody
    int recoverMetadataForTypes(@RequestBody List<String> resourceTypes,
                                @RequestParam(value = "last_update", defaultValue = "") String lastUpdate) {
        int totalNum = 0;
        long upToLastUpdate = 0L;
        if (StringUtils.isEmpty(lastUpdate)) {
            upToLastUpdate = System.currentTimeMillis();
        } else {
            upToLastUpdate = Long.valueOf(lastUpdate);
        }
        for (String resourceType : resourceTypes) {
            commonServiceHelper.assertUploadable(resourceType);
            totalNum += recoverMetadataService.recoverMetaData(resourceType, BigDecimal.valueOf(upToLastUpdate));
        }
        return totalNum;
    }

}
