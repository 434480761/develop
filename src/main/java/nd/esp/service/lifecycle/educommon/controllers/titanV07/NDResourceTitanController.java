package nd.esp.service.lifecycle.educommon.controllers.titanV07;

import com.nd.gaea.rest.security.authens.UserInfo;

import nd.esp.service.lifecycle.educommon.models.ResourceModel;
import nd.esp.service.lifecycle.educommon.services.impl.CommonServiceHelper;
import nd.esp.service.lifecycle.educommon.services.titanV07.NDResourceTitanService;
import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;
import nd.esp.service.lifecycle.educommon.vos.constant.IncludesConstant;
import nd.esp.service.lifecycle.models.AccessModel;
import nd.esp.service.lifecycle.repository.model.report.ReportResourceUsing;
import nd.esp.service.lifecycle.services.notify.NotifyReportService;
import nd.esp.service.lifecycle.services.statisticals.v06.ResourceStatisticalService;
import nd.esp.service.lifecycle.support.Constant;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.support.terminal.TerminalTypeEnum;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.vos.statics.CoverageConstant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

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

    @Autowired
    private HttpServletRequest httpServletRequest;
    
    @Autowired
    @Qualifier(value = "StatisticalServiceImpl")
    private ResourceStatisticalService statisticalService;

    @Autowired
    @Qualifier(value = "StatisticalService4QuestionDBImpl")
    private ResourceStatisticalService statisticalService4QuestionDB;

    @Autowired
    NotifyReportService nrs;
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
    public @ResponseBody ResourceViewModel getDetail(@PathVariable("res_type") String resourceType,
                                @PathVariable("uuid") String uuid,
                                @RequestParam(value = "include", required = false, defaultValue = "") String includeString,
                                @RequestParam(value = "isAll", required = false, defaultValue = "false") Boolean isAll) {
        
    	//获取终端类型
    	String terminal = httpServletRequest.getHeader(Constant.TERMINAL);
    	terminal = TerminalTypeEnum.getTerminalType(terminal);
    	
    	if (!CommonHelper.checkUuidPattern(uuid)) {
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CheckIdentifierFail.getCode(),
                    LifeCircleErrorMessageMapper.CheckIdentifierFail.getMessage());
        }
        commonServiceHelper.getRepository(resourceType);
        //check include;
        List<String> includeList = IncludesConstant.getValidIncludes(includeString);

        ResourceModel resourceModel = ndResourceTitanService.getDetail(resourceType,uuid,includeList,isAll);

        return CommonHelper.changeToView(resourceModel,resourceType,includeList,commonServiceHelper,terminal);
    }

    @RequestMapping(value = "/list", params = { "rid" }, method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody Map<String, ResourceViewModel> batchDetail(@PathVariable("res_type") String resourceType,
                                               @RequestParam(value = "rid", required = true) Set<String> uuidSet,
                                               @RequestParam(value = "include", required = false, defaultValue = "") String includeString) {

    	//获取终端类型
    	String terminal = httpServletRequest.getHeader(Constant.TERMINAL);
    	terminal = TerminalTypeEnum.getTerminalType(terminal);
    	
    	// UUID校验
        for (String uuid : uuidSet) {
            if (!CommonHelper.checkUuidPattern(uuid)) {
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        LifeCircleErrorMessageMapper.CheckIdentifierFail.getCode(),
                        LifeCircleErrorMessageMapper.CheckIdentifierFail.getMessage()+"    invalid uuid: "+uuid);
            }
        }
        commonServiceHelper.getRepository(resourceType);
        //check include
        List<String> includeList = IncludesConstant.getValidIncludes(includeString);

        List<ResourceModel> modelListResult = ndResourceTitanService.batchDetail(resourceType, uuidSet, includeList);

        Map<String, ResourceViewModel> viewMapResult = new HashMap<String, ResourceViewModel>();
        if (!CollectionUtils.isEmpty(modelListResult)) {
            for (ResourceModel model : modelListResult) {
                if (model != null) {
                    viewMapResult.put(model.getIdentifier(), 
                    		CommonHelper.changeToView(model, resourceType,includeList,commonServiceHelper,terminal));
                }
            }
        }
        return viewMapResult;
    }

    /**
     * 资源的上传：提供资源实体文件的上传操作，返回的是拥有授权令牌的存储地址 URLPattern：{res_apptype}/uploadurl
     * Method:GET {res_apptype}：资源的应用类型
     * @author linsm
     * @param res_type 资源类型
     * @param uuid 资源id
     * @param uid  用户id
     * @param renew 是否续约
     * @param coverage 资源库类型（默认为个人库，非空时，为nd私有库)
     */
    @RequestMapping(value = "/{uuid}/uploadurl", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public AccessModel requestUploading(@PathVariable String res_type,
                                        @PathVariable String uuid,
                                        @RequestParam(value = "uid", required = true) String uid,
                                        @RequestParam(value = "renew", required = false, defaultValue = "false") Boolean renew,
                                        @RequestParam(value = "coverage", required = false, defaultValue = "") String coverage,
                                        HttpServletRequest request) {
        // ResourceTypesUtil.checkResType(res_type, LifeCircleErrorMessageMapper.CSResourceTypeNotSupport);
        // UUID校验
        if (!Constant.DEFAULT_UPLOAD_URL_ID.equals(uuid) && !CommonHelper.checkUuidPattern(uuid)) {
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CheckIdentifierFail.getCode(),
                    LifeCircleErrorMessageMapper.CheckIdentifierFail.getMessage());
        }
        commonServiceHelper.assertUploadable(res_type);
        return ndResourceTitanService.getUploadUrl(res_type, uuid, uid, renew, coverage);
    }

    @RequestMapping(value = "/{uuid}/downloadurl", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public AccessModel requestDownloading(@PathVariable String res_type,
                                          @PathVariable String uuid,
                                          @RequestParam(value = "uid", required = true) String uid,
                                          @RequestParam(value = "key", required = false) String key,
                                          @RequestParam(value = "coverage", required = false) String coverage,
                                          @AuthenticationPrincipal UserInfo userInfo,
                                          HttpServletRequest request) {
        //        ResourceTypesUtil.checkResType(res_type, LifeCircleErrorMessageMapper.CSResourceTypeNotSupport);
        commonServiceHelper.assertDownloadable(res_type);
        //下载接口适配智能出题
        if (CoverageConstant.INTELLI_KNOWLEDGE_COVERAGE.equals(coverage)) {
            AccessModel accessModel = new AccessModel();
            accessModel.setAccessUrl(Constant.INTELLI_URI+Constant.INTELLI_DETAIL_URL);
            return accessModel;
        }
        AccessModel am = ndResourceTitanService.getDownloadUrl(res_type, uuid, uid, key);

        //同步至统计表中  add by xuzy 20160615
        String bsyskey = request.getHeader("bsyskey");
        syncResourceStatis(bsyskey,res_type,uuid);

        //同步至报表系统  add by xuzy 20160517
        if(nrs.checkCoverageIsNd(res_type,uuid)){
            long time = System.currentTimeMillis();
            ReportResourceUsing rru = new ReportResourceUsing();
            rru.setResourceId(uuid);
            rru.setBizSys(request.getHeader("bsyskey"));
            rru.setIdentifier(UUID.randomUUID().toString());

            rru.setCreateTime(new Timestamp(time));
            rru.setLastUpdate(new BigDecimal(time));

            if(userInfo != null){
                rru.setUserId(userInfo.getUserId());
                if(CollectionUtils.isNotEmpty(userInfo.getOrgExinfo())){
                    Map<String,Object> map = userInfo.getOrgExinfo();
                    if(map.get("org_id") != null){
                        rru.setOrgId(map.get("org_id").toString());
                    }
                    rru.setOrgName((String)map.get("org_name"));
                    rru.setRealName((String)map.get("real_name"));
                }
            }
            nrs.addResourceUsing(rru);
        }
        return am;
    }

    private void syncResourceStatis(String bsyskey,String resType,String uuid){
        if(CommonServiceHelper.isQuestionDb(resType)){
            statisticalService4QuestionDB.addDownloadStatistical(bsyskey, resType, uuid);
        }else{
            statisticalService.addDownloadStatistical(bsyskey, resType, uuid);
        }
    }

}
