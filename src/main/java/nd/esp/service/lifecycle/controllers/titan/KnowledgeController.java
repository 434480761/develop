package nd.esp.service.lifecycle.controllers.titan;

import com.nd.gaea.rest.o2o.JacksonCustomObjectMapper;
import nd.esp.service.lifecycle.models.v06.KnowledgeModel;
import nd.esp.service.lifecycle.models.v06.KnowledgePathViewModel;
import nd.esp.service.lifecycle.services.titan.TitanKnowledgeResourceService;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * ******************************************
 * <p/>
 * Copyright 2016
 * NetDragon All rights reserved
 * <p/>
 * *****************************************
 * <p/>
 * *** Company ***
 * NetDragon
 * <p/>
 * *****************************************
 * <p/>
 * *** Team ***
 * <p/>
 * <p/>
 * *****************************************
 *
 * @author gsw(806801)
 * @version V1.0
 * @Title KnowledgeController
 * @Package nd.esp.service.lifecycle.controllers.knowledges.v06
 * <p/>
 * *****************************************
 * @Description
 * @date 2016/6/21
 */

@RestController
@RequestMapping("/knowledgemap/knowledges")
public class KnowledgeController {

    @Autowired
    private TitanKnowledgeResourceService service;

    private static final JacksonCustomObjectMapper jacksonMapper = new JacksonCustomObjectMapper();

    @RequestMapping(method = RequestMethod.POST, consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public
    @ResponseBody
    KnowledgeModel create(@RequestBody KnowledgeModel knowledgeViewModel) {
        return service.create(knowledgeViewModel);
    }

    @RequestMapping(value = "/{uuid}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public
    @ResponseBody
    KnowledgeModel getDetail(@PathVariable("uuid") String uuid) {
        return service.get(uuid);
    }

    @RequestMapping(value = "/{uuid}", method = RequestMethod.DELETE)
    public void delete(@PathVariable("uuid") String uuid) {
        service.delete(uuid);
    }


    @RequestMapping(value = "/actions/findPaths", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public
    @ResponseBody
    KnowledgePathViewModel findPaths(@RequestParam(value = "startId", required = false) String startId,
                                     @RequestParam(value = "endId", required = false) String endId,
                                     @RequestParam(value = "minDepth", required = false, defaultValue = "1") int minDepth,
                                     @RequestParam(value = "maxDepth", required = false, defaultValue = "5") int maxDepth) {
        if (minDepth > maxDepth) {
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CommonSearchParamError.getCode(), "minDepth>=maxDepth");
        }
        if ((startId == null || "".equals(startId)) && (endId == null || "".equals(endId))) {
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CommonSearchParamError.getCode(), "startId endId 都为空");
        }
        if (startId != null && endId != null) {
            if (!"".equals(endId.trim()) && !"".equals(startId.trim())) {
                maxDepth = 5;
            }
            if ("".equals(endId.trim())) {
                endId = null;
            }
            if ("".equals(startId.trim())) {
                startId = null;
            }


        }

        return service.queryKnowledgePath(startId, endId, minDepth, maxDepth);
    }

    @RequestMapping(value = "/actions/queryNode", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public
    @ResponseBody
    KnowledgePathViewModel queryNode(@RequestParam(value = "startId", required = false) String startId,
                                     @RequestParam(value = "endId", required = false) String endId,
                                     @RequestParam(value = "isQueryStart", required = false, defaultValue = "true") boolean isQueryStart,
                                     @RequestParam(value = "limit", required = false, defaultValue = "10") int limit) {
        if (limit < 1) {
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CommonSearchParamError.getCode(), "limit must not be less than one");
        }
        if (isQueryStart) {
            System.out.println("queryStartNode by endId");
            return service.queryStartNode(endId, limit);
        } else {
            System.out.println("queryEndNode by startId");
            return service.queryEndNode(startId, limit);
        }
    }

}


