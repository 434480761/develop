package nd.esp.service.lifecycle.educommon.controllers.titanV07;

import nd.esp.service.lifecycle.educommon.models.ResourceModel;
import nd.esp.service.lifecycle.educommon.services.NDResourceService;
import nd.esp.service.lifecycle.educommon.services.impl.CommonServiceHelper;
import nd.esp.service.lifecycle.educommon.services.titanV07.NDResourceTitanService;
import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;
import nd.esp.service.lifecycle.educommon.vos.constant.IncludesConstant;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by liuran on 2016/8/1.
 */
@RestController
@RequestMapping("/v0.7/{res_type}")
public class NDResourceTitanController {
    @Autowired
    private NDResourceTitanService ndResourceTitanService;

    @Autowired
    private CommonServiceHelper commonServiceHelper;
    /**
     * 资源获取详细接口
     *
     * @param resourceType
     * @param uuid
     * @param includeString
     * @return
     * @since
     */
    @RequestMapping(value = "/{uuid}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody
    ResourceViewModel getDetail(@PathVariable("res_type") String resourceType,
                                @PathVariable("uuid") String uuid,
                                @RequestParam(value = "include", required = false, defaultValue = "") String includeString,
                                @RequestParam(value = "isAll", required = false, defaultValue = "false") Boolean isAll) {
        if (!CommonHelper.checkUuidPattern(uuid)) {
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CheckIdentifierFail.getCode(),
                    LifeCircleErrorMessageMapper.CheckIdentifierFail.getMessage());
        }

        //check include;
        List<String> includeList = IncludesConstant.getValidIncludes(includeString);

        ResourceModel resourceModel = ndResourceTitanService.getDetail(resourceType,uuid,includeList,isAll);

        return CommonHelper.changeToView(resourceModel,resourceType,includeList,commonServiceHelper);
    }

    @RequestMapping(value = "/list", params = { "rid" }, method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody
    Map<String, ResourceViewModel> batchDetail(@PathVariable("res_type") String resourceType,
                                               @RequestParam(value = "rid", required = true) Set<String> uuidSet,
                                               @RequestParam(value = "include", required = false, defaultValue = "") String includeString) {

        // UUID校验
        for (String uuid : uuidSet) {
            if (!CommonHelper.checkUuidPattern(uuid)) {
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        LifeCircleErrorMessageMapper.CheckIdentifierFail.getCode(),
                        LifeCircleErrorMessageMapper.CheckIdentifierFail.getMessage()+"    invalid uuid: "+uuid);
            }
        }

        //check include
        List<String> includeList = IncludesConstant.getValidIncludes(includeString);

        List<ResourceModel> modelListResult = ndResourceTitanService.batchDetail(resourceType, uuidSet, includeList);

        Map<String, ResourceViewModel> viewMapResult = new HashMap<String, ResourceViewModel>();
        if (!CollectionUtils.isEmpty(modelListResult)) {
            for (ResourceModel model : modelListResult) {
                if (model != null) {
//                    viewMapResult.put(model.getIdentifier(), changeToView(model, resourceType,includeList));
                }
            }
        }
        return viewMapResult;
    }
}
