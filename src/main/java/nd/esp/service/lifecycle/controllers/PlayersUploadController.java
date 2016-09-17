/* =============================================================
 * Created: [2015年7月22日] by linsm
 * =============================================================
 *
 * Copyright 2014-2015 NetDragon Websoft Inc. All Rights Reserved
 *
 * =============================================================
 */

package nd.esp.service.lifecycle.controllers;

import nd.esp.service.lifecycle.models.AccessModel;
import nd.esp.service.lifecycle.services.CSFileService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author linsm
 * @since
 */
@RestController
@RequestMapping("v0.6")
public class PlayersUploadController {
    @Autowired
    @Qualifier("CSFileUploadServiceImpl")
    private CSFileService csFileUploadService;

    @RequestMapping(value = "/players/uploadurl", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public AccessModel requestUploading(@RequestParam(value = "uid", required = true) String uid,
                                        @RequestParam(value = "coverage", required = false, defaultValue = "") String coverage) {
        return csFileUploadService.getPlayerUploadUrl(uid, coverage);
    }

}
