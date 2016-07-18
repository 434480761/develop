package nd.esp.service.lifecycle.educommon.controllers;

import java.io.InputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import nd.esp.service.lifecycle.app.LifeCircleApplicationInitializer;
import nd.esp.service.lifecycle.educommon.models.ResourceModel;
import nd.esp.service.lifecycle.educommon.services.NDResourceService;
import nd.esp.service.lifecycle.educommon.services.impl.CommonServiceHelper;
import nd.esp.service.lifecycle.educommon.services.impl.NDResourceServiceImpl;
import nd.esp.service.lifecycle.educommon.support.ParameterVerificationHelper;
import nd.esp.service.lifecycle.educommon.vos.ChapterStatisticsViewModel;
import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;
import nd.esp.service.lifecycle.educommon.vos.VersionViewModel;
import nd.esp.service.lifecycle.educommon.vos.constant.IncludesConstant;
import nd.esp.service.lifecycle.educommon.vos.constant.PropOperationConstant;
import nd.esp.service.lifecycle.entity.cs.CsSession;
import nd.esp.service.lifecycle.entity.elasticsearch.Resource;
import nd.esp.service.lifecycle.models.AccessModel;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.repository.model.report.ReportResourceUsing;
import nd.esp.service.lifecycle.services.ContentService;
import nd.esp.service.lifecycle.services.elasticsearch.AsynEsResourceService;
import nd.esp.service.lifecycle.services.knowledges.v06.KnowledgeService;
import nd.esp.service.lifecycle.services.notify.NotifyInstructionalobjectivesService;
import nd.esp.service.lifecycle.services.notify.NotifyReportService;
import nd.esp.service.lifecycle.services.notify.models.NotifyInstructionalobjectivesRelationModel;
import nd.esp.service.lifecycle.services.offlinemetadata.OfflineService;
import nd.esp.service.lifecycle.services.statisticals.v06.ResourceStatisticalService;
import nd.esp.service.lifecycle.support.Constant;
import nd.esp.service.lifecycle.support.Constant.CSInstanceInfo;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.StaticDatas;
import nd.esp.service.lifecycle.support.annotation.MarkAspect4OfflineJsonToCS;
import nd.esp.service.lifecycle.support.aop.ServiceAuthorAspect;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.support.busi.ValidResultHelper;
import nd.esp.service.lifecycle.support.busi.elasticsearch.ResourceTypeSupport;
import nd.esp.service.lifecycle.support.enums.LifecycleStatus;
import nd.esp.service.lifecycle.support.enums.OperationType;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.MessageConvertUtil;
import nd.esp.service.lifecycle.utils.ParamCheckUtil;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.vos.ListViewModel;
import nd.esp.service.lifecycle.vos.offlinemetadata.v06.OfflineMetadataViewModel;
import nd.esp.service.lifecycle.vos.statics.CoverageConstant;
import nd.esp.service.lifecycle.vos.statics.ResourceType;
import nd.esp.service.lifecycle.vos.valid.LifecycleDefault;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.nd.gaea.client.http.WafSecurityHttpClient;
import com.nd.gaea.rest.security.authens.UserInfo;


/**
 * 资源对外发布的接口：
 * 1、资源的上传：提供资源实体文件的上传操作，返回的是拥有授权令牌的存储地址
 * 2、资源的按需下载：在符合资源【课件、课件颗粒等】，通过main文件加载page或者addon
 * 3、资源的打包下载：资源进行打包下载。如果是素材等，单文件不需要打包操作，直接下载
 * 4、资源的创建：资源创建此处主要是指元数据记录的创建。
 * 5、获取详细：获取元数据信息
 * 6、高级分类检索：按照分类体系进行维度检索
 * 7、元数据的修改：修改元数据信息，参数是元数据信息和文件信息，如果元数据为null，文件的id传入进来，那么直接返回更新实体文件的路径
 * 8、资源的删除【伪删除】：删除资源信息，包括删除符合资源中的文件。
 * @author johnny
 * @version 1.0
 * @created 01-7月-2015 13:52:11
 */
@RestController
@RequestMapping("/v0.6/{res_type}")
public class NDResourceController {

    private static final Logger LOG = LoggerFactory.getLogger(NDResourceController.class);

    /**
     * 通用查询,ES和DB properties属性对应静态块
     */
    private static Map<String, String> changeFieldNameDBToES = new HashMap<String, String>();
    private static Map<String, String> changeFieldNameESToDB = new HashMap<String, String>();
    static{
        for(Map.Entry<Object,Object> entry:LifeCircleApplicationInitializer.props_properties_db.entrySet())
        {
            changeFieldNameDBToES.put((String)entry.getValue(), (String)LifeCircleApplicationInitializer.props_properties_es.get(entry.getKey()));
        }

        for(Map.Entry<Object,Object> entry:LifeCircleApplicationInitializer.props_properties_es.entrySet())
        {
            changeFieldNameESToDB.put((String)entry.getValue(), (String)LifeCircleApplicationInitializer.props_properties_db.get(entry.getKey()));
        }
    }

    @Autowired
    private NDResourceService ndResourceService;

    @Autowired
    private ContentService contentService;

    @Autowired
    private NotifyInstructionalobjectivesService notifyService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    CommonServiceHelper commonServiceHelper;

    @Autowired
    WafSecurityHttpClient wafSecurityHttpClient;

    @Autowired
    @Qualifier("knowledgeServiceV06")
    KnowledgeService KnowledgeServiceV06;

    @Autowired
    NotifyReportService nrs;

    @Autowired
    @Qualifier(value = "StatisticalServiceImpl")
    private ResourceStatisticalService statisticalService;

    @Autowired
    @Qualifier(value = "StatisticalService4QuestionDBImpl")
    private ResourceStatisticalService statisticalService4QuestionDB;

    @Autowired
    private AsynEsResourceService esResourceOperation;

    @Autowired
    private OfflineService offlineService;


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

        // UUID校验
        if (!CommonHelper.checkUuidPattern(uuid)) {
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CheckIdentifierFail.getCode(),
                    LifeCircleErrorMessageMapper.CheckIdentifierFail.getMessage());
        }
        //check include;
        List<String> includeList = IncludesConstant.getValidIncludes(includeString);
        //调用servicere
        ResourceModel modelResult = ndResourceService.getDetail(resourceType, uuid,includeList,isAll);
        // model出参转换
        return changeToView(modelResult, resourceType,includeList);
    }

    /**
     * 资源批量获取详细接口
     *
     * @param resourceType
     * @param uuidSet
     * @param includeString
     * @return
     * @since
     */
    @RequestMapping(value = "/list", params = { "rid" }, method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody Map<String, ResourceViewModel> batchDetail(@PathVariable("res_type") String resourceType,
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

        List<ResourceModel> modelListResult = ndResourceService.batchDetail(resourceType, uuidSet, includeList);

        Map<String, ResourceViewModel> viewMapResult = new HashMap<String, ResourceViewModel>();
        if (!CollectionUtils.isEmpty(modelListResult)) {
            for (ResourceModel model : modelListResult) {
                if (model != null) {
                    viewMapResult.put(model.getIdentifier(), changeToView(model, resourceType,includeList));
                }
            }
        }
        return viewMapResult;
    }

    /**
     * 资源删除接口
     *
     * @param resourceType
     * @param uuid
     * @return
     * @since
     */
    @MarkAspect4OfflineJsonToCS
    @RequestMapping(value = "/{uuid}", method = RequestMethod.DELETE, produces = { MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody Map<String, String> delete(@PathVariable("res_type") String resourceType,@PathVariable("uuid") String uuid) {
        // UUID校验
        if (!CommonHelper.checkUuidPattern(uuid)) {
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CheckIdentifierFail.getCode(),
                    LifeCircleErrorMessageMapper.CheckIdentifierFail.getMessage());
        }

        LOG.debug("删除知识点V06---判断是否有子知识点开始");

        if(resourceType.equals(IndexSourceType.KnowledgeType.getName())){
            KnowledgeServiceV06.isHaveChildrens(uuid);
        }

        LOG.debug("删除知识点V06---判断是否有子知识点结束");

        //add by xiezy - 2016.04.15
        List<NotifyInstructionalobjectivesRelationModel> relateRelations = new ArrayList<NotifyInstructionalobjectivesRelationModel>();
        String nowStatus = "";
        if(resourceType.equals(IndexSourceType.InstructionalObjectiveType.getName())){
            nowStatus = notifyService.getResourceStatus(uuid);
            if(nowStatus.equals(LifecycleStatus.ONLINE.getCode())){
                relateRelations = notifyService.resourceBelongToRelations(uuid);
            }
        }else if(resourceType.equals(IndexSourceType.LessonType.getName())){
            relateRelations = notifyService.resourceBelongToRelations4LessonOrChapter(resourceType,uuid);
        }

        // 调用service
        if(!CommonServiceHelper.isQuestionDb(resourceType)){
            ndResourceService.delete(resourceType, uuid);
        }else{
            ndResourceService.deleteInQuestionDB(resourceType, uuid);
        }

        //add by xiezy - 2016.04.15
        //异步通知智能出题
        if(resourceType.equals(IndexSourceType.InstructionalObjectiveType.getName())){
            if(nowStatus.equals(nowStatus.equals(LifecycleStatus.ONLINE.getCode()))){
                notifyService.asynNotify4Resource(uuid, nowStatus, null, relateRelations, OperationType.DELETE);
            }
        }else if(resourceType.equals(IndexSourceType.LessonType.getName())){
            notifyService.asynNotify4LessonOrChapter(resourceType, uuid, relateRelations, OperationType.DELETE);
        }

        return MessageConvertUtil.getMessageString(LifeCircleErrorMessageMapper.DeleteResourceSuccess);
    }

    /**
     * 资源检索 -- 通过solr检索,数据存在延时性
     *
     * @param res_type    明确的资源类型
     * @param resCodes          支持多种资源查询,resType=eduresource时生效
     * @param includes    默认情况下，只返回资源的通用属性，不返回资源的其他扩展属性。
     *                    TI：技术属性, LC：生命周期属性, EDU：教育属性, CG：分类维度数据属性, CR:版权信息
     *         该检索接口只支持:TI,EDU,LC,CG,CR
     * @param category    通用查询的分类维度数据的入参信息
     * @param relation    关系查询的入参
     * @param coverage    覆盖范围的入参查询数据信息
     * @param prop    属性入参
     * @param words    关键字
     * @param limit    分页参数
     * @param reverse 判断关系查询是否反转
     * @param printable     资源是否可打印(针对TI中的printable)
     * @param printable_key 指定资源哪个文件可打印(针对TI中的title),只有当printable!=null的时候生效
     * @param statistics_type 仅当orderby=statisticals asc/desc时生效,表示统计的类型
     * @param statistics_platform 仅当orderby=statisticals asc/desc时生效,表示统计的平台或业务方
     */
    @RequestMapping(value = "/actions/search", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE },params = { "words","limit"})
    public ListViewModel<ResourceViewModel> requestQueringBySolr(
            @PathVariable(value="res_type") String resType,
            @RequestParam(required=false,value="rescode") String resCodes,
            @RequestParam(required=false,value="include")  String includes,
            @RequestParam(required=false,value="category") Set<String> categories,
            @RequestParam(required=false,value="category_exclude") Set<String> categoryExclude,
            @RequestParam(required=false,value="relation") Set<String> relations,
            @RequestParam(required=false,value="coverage") Set<String> coverages,
            @RequestParam(required=false,value="prop") List<String> props,
            @RequestParam(required=false,value="orderby") List<String> orderBy,
            @RequestParam(required=false,value="reverse") String reverse,
            @RequestParam(required=false,value="printable") Boolean printable,
            @RequestParam(required=false,value="printable_key") String printableKey,
            @RequestParam(required=false,value="statistics_type") String statisticsType,
            @RequestParam(required=false,value="statistics_platform",defaultValue="all") String statisticsPlatform,
            @RequestParam(required=false,value="force_status",defaultValue="false") boolean forceStatus,
            @RequestParam(required=false,value="tags") List<String> tags,
            @RequestParam(required=false,value="show_version",defaultValue="false") boolean showVersion,
            @RequestParam String words,@RequestParam String limit){

        return requestQuering(resType, resCodes, includes, categories, categoryExclude, relations, coverages, props, orderBy, words, limit, QueryType.DB, true, reverse, printable, printableKey, statisticsType, statisticsPlatform, forceStatus,tags, showVersion);

    }

    /**
     * 资源检索 -- 通过eslasticsearch检索,数据存在延时性
     *
     * @param res_type
     *            明确的资源类型
     * @param resCodes
     *            支持多种资源查询,resType=eduresource时生效
     * @param includes
     *            默认情况下，只返回资源的通用属性，不返回资源的其他扩展属性。 TI：技术属性, LC：生命周期属性, EDU：教育属性,
     *            CG：分类维度数据属性, CR:版权信息 该检索接口只支持:TI,EDU,LC,CG,CR
     * @param category
     *            通用查询的分类维度数据的入参信息
     * @param relation
     *            关系查询的入参
     * @param coverage
     *            覆盖范围的入参查询数据信息
     * @param prop
     *            属性入参
     * @param words
     *            关键字
     * @param limit
     *            分页参数
     * @param reverse
     *            判断关系查询是否反转
     * @param printable
     * 			     资源是否可打印(针对TI中的printable)
     * @param printable_key
     * 			     指定资源哪个文件可打印(针对TI中的title),只有当printable!=null的时候生效
     */
    @RequestMapping(value = "/actions/es", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE }, params = { "limit" })
    public ListViewModel<ResourceViewModel> requestQueringByEs(
            @PathVariable(value = "res_type") String resType,
            @RequestParam(required = false, value = "rescode") String resCodes,
            @RequestParam(required = false, value = "include") String includes,
            @RequestParam(required = false, value = "category") Set<String> categories,
            @RequestParam(required = false, value = "category_exclude") Set<String> categoryExclude,
			/*
			 * @RequestParam(required=false,value="relation") Set<String>
			 * relations,
			 */
            @RequestParam(required = false, value = "coverage") Set<String> coverages,
            @RequestParam(required = false, value = "prop") List<String> props,
            @RequestParam(required = false, value = "orderby") List<String> orderBy,
            @RequestParam(required = false, value = "isAll",defaultValue="false") Boolean isAll,
            @RequestParam(required=false,value="printable") Boolean printable,
            @RequestParam(required=false,value="printable_key") String printableKey,
			/* @RequestParam(required=false,value="reverse") String reverse, */
			/* @RequestParam String words, */@RequestParam String limit) {
        return requestQuering(resType, resCodes, includes, categories,
                categoryExclude, null, coverages, props, orderBy, null, limit,

                QueryType.ES, !isAll, "false", printable, printableKey, null,null,false,null,false);
    }


    /**
     *
     * @param resType
     * @param resCodes
     * @param includes
     * @param categories
     * @param categoryExclude
     * @param relations
     * @param coverages
     * @param props
     * @param orderBy
     * @param isAll
     * @param reverse
     * @param limit
     * @author linsm
     * @return
     */
    @RequestMapping(value = "/actions/titan", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE }, params = { "limit" })
    public ListViewModel<ResourceViewModel> requestQueringByTitan(
            @PathVariable(value = "res_type") String resType,
            @RequestParam(required = false, value = "rescode") String resCodes,
            @RequestParam(required = false, value = "include") String includes,
            @RequestParam(required = false, value = "category") Set<String> categories,
            @RequestParam(required = false, value = "category_exclude") Set<String> categoryExclude,
            @RequestParam(required = false, value = "relation") Set<String> relations,
            @RequestParam(required = false, value = "coverage") Set<String> coverages,
            @RequestParam(required = false, value = "prop") List<String> props,
            @RequestParam(required = false, value = "orderby") List<String> orderBy,
            @RequestParam(required = false, value = "isAll", defaultValue = "false") Boolean isAll,
            @RequestParam(required = false, value = "reverse") String reverse,
            @RequestParam String words,
            @RequestParam(required = false, value = "fulltext") String fulltext,
            @RequestParam(required=false,value="printable") Boolean printable,
            @RequestParam(required=false,value="printable_key") String printableKey,
            @RequestParam String limit) {
        // FIXME 暂时先共用一个参数 (by lsm)
        return requestQuering(resType, resCodes, includes, categories,
                categoryExclude, relations, coverages, props, orderBy, words
                        + "$" + fulltext, limit, QueryType.TITAN, !isAll,
                reverse,printable, printableKey,null,null,false,null,false);
    }

    @RequestMapping(value = "/actions/titan_es", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE }, params = { "limit" })
    public ListViewModel<ResourceViewModel> requestQueringByTitanES(
            @PathVariable(value = "res_type") String resType,
            @RequestParam(required = false, value = "rescode") String resCodes,
            @RequestParam(required = false, value = "include") String includes,
            @RequestParam(required = false, value = "category") Set<String> categories,
            @RequestParam(required = false, value = "category_exclude") Set<String> categoryExclude,
            @RequestParam(required = false, value = "relation") Set<String> relations,
            @RequestParam(required = false, value = "coverage") Set<String> coverages,
            @RequestParam(required = false, value = "prop") List<String> props,
            @RequestParam(required = false, value = "orderby") List<String> orderBy,
            @RequestParam(required = false, value = "isAll", defaultValue = "false") Boolean isAll,
            @RequestParam(required = false, value = "reverse") String reverse,
            @RequestParam String words,
            @RequestParam(required=false,value="printable") Boolean printable,
            @RequestParam(required=false,value="printable_key") String printableKey,
            @RequestParam String limit) {

        return requestQuering(resType, resCodes, includes, categories,
                categoryExclude, relations, coverages, props, orderBy, words, limit, QueryType.TITAN_ES, !isAll,
                reverse,printable, printableKey,null,null,false,null,false);
    }

    /**
     * 资源检索 -- 直接查询数据库,数据可以保证实时性
     * <p>Description:  资源检索升级目的主要是使得查询效率更高，准确度更高。
     * 使得用户可以根据分类维度数据，关系维度数据，覆盖范围，属性，关键字进行分页查询。
     * 在这个几个条件下，优化数据结构，提高检索效率。            </p>
     * <p>Create Time: 2015年6月19日   </p>
     * <p>Create author: xiezy   </p>
     * @param resType           资源类型
     * @param resCodes          支持多种资源查询,resType=eduresource时生效
     * @param includes    默认情况下，只返回资源的通用属性，不返回资源的其他扩展属性。
     *                    TI：技术属性, LC：生命周期属性, EDU：教育属性, CG：分类维度数据属性, CR:版权信息
     *         该检索接口只支持:TI,EDU,LC,CG,CR
     * @param categories        分类维度数据
     * @param relations         关系维度数据
     * @param coverages         覆盖范围，根据目标类型，目标值以及覆盖方式进行查询
     * @param words             关键字
     * @param limit             分页参数，第一个值为记录索引参数，第二个值为偏移量
     * @param reverse 判断关系查询是否反转
     * @param printable     资源是否可打印(针对TI中的printable)
     * @param printable_key 指定资源哪个文件可打印(针对TI中的title),只有当printable!=null的时候生效
     * @param statistics_type 仅当orderby=statisticals asc/desc时生效,表示统计的类型
     * @param statistics_platform 仅当orderby=statisticals asc/desc时生效,表示统计的平台或业务方
     * @return
     */
    @RequestMapping(value = "/management/actions/query", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE },params = { "words","limit"})
    public ListViewModel<ResourceViewModel> requestQueringByDBAndManagement(
            @PathVariable(value="res_type") String resType,
            @RequestParam(required=false,value="rescode") String resCodes,
            @RequestParam(required=false,value="include") String includes,
            @RequestParam(required=false,value="category") Set<String> categories,
            @RequestParam(required=false,value="category_exclude") Set<String> categoryExclude,
            @RequestParam(required=false,value="relation") Set<String> relations,
            @RequestParam(required=false,value="coverage") Set<String> coverages,
            @RequestParam(required=false,value="prop") List<String> props,
            @RequestParam(required=false,value="orderby") List<String> orderBy,
            @RequestParam(required=false,value="reverse") String reverse,
            @RequestParam(required=false,value="printable") Boolean printable,
            @RequestParam(required=false,value="printable_key") String printableKey,
            @RequestParam(required=false,value="statistics_type") String statisticsType,
            @RequestParam(required=false,value="statistics_platform",defaultValue="all") String statisticsPlatform,
            @RequestParam(required=false,value="tags") List<String> tags,
            @RequestParam(required=false,value="show_version",defaultValue="false") boolean showVersion,
            @RequestParam String words,@RequestParam String limit){


        return requestQuering(resType, resCodes, includes, categories, categoryExclude, relations, coverages, props, orderBy, words, limit, QueryType.DB, false, reverse, printable, printableKey, statisticsType, statisticsPlatform, false, tags,showVersion);

    }

    /**
     * 该接口对ND库中的数据做了限制！
     * 资源检索 -- 直接查询数据库,数据可以保证实时性
     * <p>Description:  资源检索升级目的主要是使得查询效率更高，准确度更高。
     * 使得用户可以根据分类维度数据，关系维度数据，覆盖范围，属性，关键字进行分页查询。
     * 在这个几个条件下，优化数据结构，提高检索效率。            </p>
     * <p>Create Time: 2015年6月19日   </p>
     * <p>Update Time: 2015年10月20日   </p>
     * <p>Create author: xiezy   </p>
     * @param resType           资源类型
     * @param resCodes          支持多种资源查询,resType=eduresource时生效
     * @param includes    默认情况下，只返回资源的通用属性，不返回资源的其他扩展属性。
     *                    TI：技术属性, LC：生命周期属性, EDU：教育属性, CG：分类维度数据属性, CR:版权信息
     *         该检索接口只支持:TI,EDU,LC,CG,CR
     * @param categories        分类维度数据
     * @param relations         关系维度数据
     * @param coverages         覆盖范围，根据目标类型，目标值以及覆盖方式进行查询
     * @param words             关键字
     * @param limit             分页参数，第一个值为记录索引参数，第二个值为偏移量
     * @param reverse 判断关系查询是否反转
     * @param printable     资源是否可打印(针对TI中的printable)
     * @param printable_key 指定资源哪个文件可打印(针对TI中的title),只有当printable!=null的时候生效
     * @param statistics_type 仅当orderby=statisticals asc/desc时生效,表示统计的类型
     * @param statistics_platform 仅当orderby=statisticals asc/desc时生效,表示统计的平台或业务方
     * @return
     */
    @RequestMapping(value = "/actions/query", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE },params = { "words","limit"})
    public ListViewModel<ResourceViewModel> requestQueringByDB(
            @PathVariable(value="res_type") String resType,
            @RequestParam(required=false,value="rescode") String resCodes,
            @RequestParam(required=false,value="include") String includes,
            @RequestParam(required=false,value="category") Set<String> categories,
            @RequestParam(required=false,value="category_exclude") Set<String> categoryExclude,
            @RequestParam(required=false,value="relation") Set<String> relations,
            @RequestParam(required=false,value="coverage") Set<String> coverages,
            @RequestParam(required=false,value="prop") List<String> props,
            @RequestParam(required=false,value="orderby") List<String> orderBy,
            @RequestParam(required=false,value="reverse") String reverse,
            @RequestParam(required=false,value="printable") Boolean printable,
            @RequestParam(required=false,value="printable_key") String printableKey,
            @RequestParam(required=false,value="statistics_type") String statisticsType,
            @RequestParam(required=false,value="statistics_platform",defaultValue="all") String statisticsPlatform,
            @RequestParam(required=false,value="force_status",defaultValue="false") boolean forceStatus,
            @RequestParam(required=false,value="tags") List<String> tags,
            @RequestParam(required=false,value="show_version",defaultValue="false") boolean showVersion,
            @RequestParam String words,@RequestParam String limit){


        return requestQuering(resType, resCodes, includes, categories, categoryExclude, relations, coverages, props, orderBy, words, limit, QueryType.DB, true, reverse, printable, printableKey, statisticsType, statisticsPlatform, forceStatus,tags, showVersion);

    }

    /**
     * 资源统计
     * <p>Create Time: 2016年3月28日   </p>
     * <p>Create author: xiezy   </p>
     * @param resType
     * @param categories
     * @param coverages
     * @param props
     * @param groupBy
     * @return
     */
    @RequestMapping(value = "/actions/query/count", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE },params = { "groupby"})
    public Map<String, Integer> requestCountByDB(
            @PathVariable(value="res_type") String resType,
            @RequestParam(required=false,value="category") Set<String> categories,
            @RequestParam(required=false,value="coverage") Set<String> coverages,
            @RequestParam(required=false,value="prop") List<String> props,
            @RequestParam(value="groupby") String groupBy){

        return requestCounting(resType, categories, coverages, props, true, groupBy);
    }

    /**
     * 资源统计-管理端
     * <p>Create Time: 2016年3月28日   </p>
     * <p>Create author: xiezy   </p>
     * @param resType
     * @param categories
     * @param coverages
     * @param props
     * @param groupBy
     * @return
     */
    @RequestMapping(value = "management/actions/query/count", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE },params = { "groupby"})
    public Map<String, Integer> requestCountByDBAndManagement(
            @PathVariable(value="res_type") String resType,
            @RequestParam(required=false,value="category") Set<String> categories,
            @RequestParam(required=false,value="coverage") Set<String> coverages,
            @RequestParam(required=false,value="prop") List<String> props,
            @RequestParam(value="groupby") String groupBy){

        return requestCounting(resType, categories, coverages, props, false, groupBy);
    }

    /**
     * 查询的通用方法
     * <p>Create Time: 2015年9月28日   </p>
     * <p>Create author: xiezy   </p>
     * @return
     */
    @SuppressWarnings("unchecked")
    private ListViewModel<ResourceViewModel> requestQuering(String resType, String resCodes, String includes,
                                                            Set<String> categories, Set<String> categoryExclude, Set<String> relations, Set<String> coverages, List<String> props,
                                                            List<String> orderBy, String words, String limit, QueryType queryType, boolean isNotManagement, String reverse,
                                                            Boolean printable, String printableKey,String statisticsType,String statisticsPlatform,boolean forceStatus,List<String> tags,
                                                            boolean showVersion) {

        //智能出题对接外部接口--入口
        if(CollectionUtils.isNotEmpty(coverages) && coverages.size()==1
                && coverages.iterator().next().equals(CoverageConstant.INTELLI_KNOWLEDGE_COVERAGE)){
            return queryIntelliKnowledge(resType, includes, relations, limit);
        }

        //对接安全接口中需要过滤掉的category code
        String bsyskey = httpServletRequest.getHeader("bsyskey");
        Set<String> excludeCategories4bsyskey = ServiceAuthorAspect.getExcludeCategories(bsyskey);
        if(CollectionUtils.isNotEmpty(categoryExclude)){
            categoryExclude.addAll(excludeCategories4bsyskey);
        }else{
            categoryExclude = excludeCategories4bsyskey;
        }

        //statisticsType,statisticsPlatform 参数处理
        if(!StringUtils.hasText(statisticsType)){
            statisticsType = "valuesum";
        }
        if("self".equals(statisticsPlatform) && StringUtils.hasText(bsyskey) && bsyskey.equals(Constant.BSYSKEY_101PPT)){
            statisticsPlatform = "101PPT";
        }else{
            statisticsPlatform = "TOTAL";
        }

        //参数校验和处理
        Map<String, Object> paramMap =
                requestParamVerifyAndHandle(resType, resCodes, includes, categories, categoryExclude,
                        relations, coverages, props, orderBy, limit, queryType, reverse);

        // include
        List<String> includesList = (List<String>)paramMap.get("include");

        //categories
        categories = (Set<String>)paramMap.get("category");

        //categoryExclude
        categoryExclude = (Set<String>)paramMap.get("categoryExclude");

        // relations,格式:stype/suuid/r_type
        List<Map<String,String>> relationsMap = (List<Map<String,String>>)paramMap.get("relation");

        // coverages,格式:Org/uuid/SHAREING
        List<String> coveragesList = (List<String>)paramMap.get("coverage");

        // props,语法 [属性] [操作] [值]
        Map<String,Set<String>> propsMap = (Map<String,Set<String>>)paramMap.get("prop");

        // orderBy
        Map<String,String> orderMap = (Map<String,String>)paramMap.get("orderby");

        //reverse,默认为false
        boolean reverseBoolean = (boolean)paramMap.get("reverse");

        //limit
        limit = (String)paramMap.get("limit");

        //调用service,获取到业务模型的list
        ListViewModel<ResourceModel> rListViewModel = new ListViewModel<ResourceModel>();
        switch (queryType) {
            case DB:
                if (StaticDatas.QUERY_BY_ES_FIRST
                        && canQueryByEla(resType, relationsMap, orderMap, words,
                        coveragesList, isNotManagement,forceStatus,tags,showVersion)) {// 数据库走ES查询判断
                    try {
                        Map<String, Object> changeMap = changeKey(propsMap,
                                orderMap, false);
                        propsMap = (Map<String, Set<String>>) changeMap
                                .get("propsMapNew");
                        orderMap = (Map<String, String>) changeMap
                                .get("orderMapNew");
                        rListViewModel = ndResourceService.resourceQueryByEla(
                                resType, includesList, categories, categoryExclude,
                                relationsMap, coveragesList, propsMap, orderMap,
                                words, limit, isNotManagement, reverseBoolean,printable,printableKey);
                    } catch (Exception e) {// 如果ES出错,通过数据库查一遍
                        LOG.error("ES查询出错,通用DB查询");
                        Map<String, Object> changeMap = changeKey(propsMap,
                                orderMap, true);
                        propsMap = (Map<String, Set<String>>) changeMap
                                .get("propsMapNew");
                        orderMap = (Map<String, String>) changeMap
                                .get("orderMapNew");
                        rListViewModel = ndResourceService.resourceQueryByDB(
                                resType, resCodes, includesList, categories,
                                categoryExclude, relationsMap, coveragesList,
                                propsMap, orderMap, words, limit, isNotManagement,
                                reverseBoolean, printable, printableKey, statisticsType, statisticsPlatform,forceStatus,tags,showVersion);
                    }
                } else {
                    rListViewModel = ndResourceService.resourceQueryByDB(resType,
                            resCodes, includesList, categories, categoryExclude,
                            relationsMap, coveragesList, propsMap, orderMap, words,
                            limit, isNotManagement, reverseBoolean, printable, printableKey, statisticsType, statisticsPlatform,forceStatus,tags,showVersion);
                }
                break;
            case ES:
                rListViewModel = ndResourceService.resourceQueryByEla(resType,
                        includesList, categories, categoryExclude, relationsMap,
                        coveragesList, propsMap, orderMap, words, limit,
                        isNotManagement, reverseBoolean,printable,printableKey);
                break;
            case TITAN:
                rListViewModel = ndResourceService.resourceQueryByTitan(resType,
                        includesList, categories, categoryExclude, relationsMap,
                        coveragesList, propsMap, orderMap, words, limit,
                        isNotManagement, reverseBoolean,printable,printableKey);
                break;
            case TITAN_ES:
                rListViewModel = ndResourceService.resourceQueryByTitanES(resType,
                        includesList, categories, categoryExclude, relationsMap,
                        coveragesList, propsMap, orderMap, words, limit,
                        isNotManagement, reverseBoolean,printable,printableKey);
                break;
            default:
                break;
        }

        //ListViewModel<ResourceModel> 转换为  ListViewModel<ResourceViewModel>
        ListViewModel<ResourceViewModel> result = new ListViewModel<ResourceViewModel>();
        result.setTotal(rListViewModel.getTotal());
        result.setLimit(rListViewModel.getLimit());
        //items处理
        List<ResourceViewModel> items = new ArrayList<ResourceViewModel>();
        for(ResourceModel resourceModel : rListViewModel.getItems()){
            ResourceViewModel resourceViewModel = changeToView(resourceModel, resType,includesList);
            items.add(resourceViewModel);
        }
        result.setItems(items);

        return result;
    }

    /**
     * 判断走数据库的通用查询是否可以通用ES查询
     * <p>Create Time: 2016年4月5日   </p>
     * <p>Create author: xiezy   </p>
     * @param resType
     * @param relations
     * @param orderMap
     * @param words
     * @return
     */
    private boolean canQueryByEla(String resType, List<Map<String, String>> relations,
                                  Map<String, String>orderMap, String words, List<String> coveragesList, boolean isNotManagement,
                                  boolean forceStatus,List<String> tags,boolean showVersion){
        boolean haveUserCoverage = false;
        if(CollectionUtils.isNotEmpty(coveragesList)){
            for(String coverage : coveragesList){
                if(StringUtils.isNotEmpty(coverage)){
                    if(coverage.startsWith("User")){
                        haveUserCoverage = true;
                        break;
                    }
                }
            }
        }

        if(isNotManagement &&
                !forceStatus &&
                !showVersion &&
                !haveUserCoverage &&
                CollectionUtils.isEmpty(tags) &&
                !resType.equals(Constant.RESTYPE_EDURESOURCE) &&
                CollectionUtils.isEmpty(relations) &&
                StringUtils.isEmpty(words) &&
                (CollectionUtils.isEmpty(orderMap) ||
                        (CollectionUtils.isNotEmpty(orderMap) &&
                                !(orderMap.containsKey("size") || orderMap.containsKey("key_value") ||
                                        orderMap.containsKey("top") || orderMap.containsKey("scores") ||
                                        orderMap.containsKey("votes") || orderMap.containsKey("views") ||
                                        orderMap.containsKey("sort_num") || orderMap.containsKey("taxOnCode"))))){

            return true;
        }

        return false;
    }

    /**
     * ES和DB prop和orderby之间key的转换
     * <p>Create Time: 2016年4月6日   </p>
     * <p>Create author: xiezy   </p>
     * @param propsMap
     * @param orderMap
     * @param isDB
     */
    private Map<String, Object> changeKey(Map<String,Set<String>> propsMap,Map<String,String> orderMap,boolean isDB){
        Map<String, Object> map = new HashMap<String, Object>();

        Map<String,Set<String>> propsMapNew = null;
        Map<String,String> orderMapNew = null;

        if(CollectionUtils.isNotEmpty(orderMap)){
            orderMapNew = new HashMap<String, String>();
            for(String orderKey : orderMap.keySet()){
                String afterChangeKey = "";
                if(isDB){
                    afterChangeKey = changeFieldNameESToDB.get(orderKey);
                }else{
                    afterChangeKey = changeFieldNameDBToES.get(orderKey);
                }

                orderMapNew.put(afterChangeKey, orderMap.get(orderKey));
            }
        }

        if(CollectionUtils.isNotEmpty(propsMap)){
            propsMapNew = new HashMap<String, Set<String>>();
            for(String propKey : propsMap.keySet()){
                String realKey = "";
                String afterChangeKey = "";
                if (propKey.endsWith("_GT")) {// 时间 gt
                    realKey = propKey.substring(0, propKey.length() - 3);
                    if(isDB){
                        afterChangeKey = changeFieldNameESToDB.get(realKey);
                    }else{
                        afterChangeKey = changeFieldNameDBToES.get(realKey);
                    }

                    propsMapNew.put(afterChangeKey+"_GT", propsMap.get(propKey));
                } else if (propKey.endsWith("_LT")) {// 时间 lt
                    realKey = propKey.substring(0, propKey.length() - 3);
                    if(isDB){
                        afterChangeKey = changeFieldNameESToDB.get(realKey);
                    }else{
                        afterChangeKey = changeFieldNameDBToES.get(realKey);
                    }

                    propsMapNew.put(afterChangeKey+"_LT", propsMap.get(propKey));
                } else if (propKey.endsWith("_LE")) {// 时间 le
                    realKey = propKey.substring(0, propKey.length() - 3);
                    if(isDB){
                        afterChangeKey = changeFieldNameESToDB.get(realKey);
                    }else{
                        afterChangeKey = changeFieldNameDBToES.get(realKey);
                    }

                    propsMapNew.put(afterChangeKey+"_LE", propsMap.get(propKey));
                } else if (propKey.endsWith("_GE")) {// 时间 ge
                    realKey = propKey.substring(0, propKey.length() - 3);
                    if(isDB){
                        afterChangeKey = changeFieldNameESToDB.get(realKey);
                    }else{
                        afterChangeKey = changeFieldNameDBToES.get(realKey);
                    }

                    propsMapNew.put(afterChangeKey+"_GE", propsMap.get(propKey));
                } else if (propKey.endsWith("_NE")) {// ne
                    realKey = propKey.substring(0, propKey.length() - 3);
                    if(isDB){
                        afterChangeKey = changeFieldNameESToDB.get(realKey);
                    }else{
                        afterChangeKey = changeFieldNameDBToES.get(realKey);
                    }

                    propsMapNew.put(afterChangeKey+"_NE", propsMap.get(propKey));
                } else if (propKey.endsWith("_LIKE")) {// like
                    realKey = propKey.substring(0, propKey.length() - 5);
                    if(isDB){
                        afterChangeKey = changeFieldNameESToDB.get(realKey);
                    }else{
                        afterChangeKey = changeFieldNameDBToES.get(realKey);
                    }

                    propsMapNew.put(afterChangeKey+"_LIKE", propsMap.get(propKey));
                } else {// eq or in
                    if(isDB){
                        afterChangeKey = changeFieldNameESToDB.get(propKey);
                    }else{
                        afterChangeKey = changeFieldNameDBToES.get(propKey);
                    }

                    propsMapNew.put(afterChangeKey, propsMap.get(propKey));
                }
            }
        }

        map.put("propsMapNew", propsMapNew);
        map.put("orderMapNew", orderMapNew);
        return map;
    }

    /**
     * 通用的资源统计
     * <p>Create Time: 2016年3月28日   </p>
     * <p>Create author: xiezy   </p>
     */
    @SuppressWarnings("unchecked")
    private Map<String, Integer> requestCounting(String resType, Set<String> categories, Set<String> coverages,
                                                 List<String> props, boolean isNotManagement, String groupBy){
        //参数校验和处理
        Map<String, Object> paramMap =
                requestParamVerifyAndHandle(resType, null, null, categories, null, null,
                        coverages, props, null, "(0,1)", QueryType.DB, null);

        //categories
        categories = (Set<String>)paramMap.get("category");

        // coverages,格式:Org/uuid/SHAREING
        List<String> coveragesList = (List<String>)paramMap.get("coverage");

        // props,语法 [属性] [操作] [值]
        Map<String,Set<String>> propsMap = (Map<String,Set<String>>)paramMap.get("prop");

        // groupBy
        if(StringUtils.isEmpty(groupBy)){
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CommonSearchParamError.getCode(),
                    "groupby不能为空");
        }

        return ndResourceService.resourceStatistics(resType, categories, coveragesList, propsMap, groupBy, isNotManagement);
    }

    /**
     * 获取智能出题
     * <p>Create Time: 2015年12月24日   </p>
     * <p>Create author: xiezy   </p>
     * @param resType
     * @param includes
     * @param relations
     * @param limit
     * @return
     */
    public ListViewModel<ResourceViewModel> queryIntelliKnowledge(String resType,String includes,Set<String> relations,String limit){
        //1.resType校验
        if(!IndexSourceType.QuestionType.getName().equals(resType)){
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CommonSearchParamError.getCode(),
                    "查询智能出题时resType只能为questions");
        }

        //2.includes
        List<String> includesList = IncludesConstant.getValidIncludes(includes);

        //2.relations,有且只有一个relation
        String chapterId = "";
        if(CollectionUtils.isEmpty(relations)
                || (CollectionUtils.isNotEmpty(relations) && relations.size()>1)){
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CommonSearchParamError.getCode(),
                    "查询智能出题时relation有且只有一个");
        }else{
            String relation = relations.iterator().next();
            //对于入参的coverage每个在最后追加一个空格，以保证elemnt的size为3
            relation = relation + " ";
            List<String> elements = Arrays.asList(relation.split("/"));
            //格式错误判断
            if(elements.size() != 3){

                LOG.error(relation + "--relation格式错误");

                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        LifeCircleErrorMessageMapper.CommonSearchParamError.getCode(),
                        relation + "--relation格式错误");
            }
            //判断源资源是否存在,stype + suuid
            CommonHelper.resourceExist(elements.get(0), elements.get(1), ResourceType.RESOURCE_SOURCE);

            //获取章节id
            chapterId = elements.get(1);
        }

        //3.limit
        Integer[] result = ParamCheckUtil.checkLimit(limit);
        String offset = result[0].toString();
        String pageSize = result[1].toString();

        ListViewModel<ResourceViewModel> rListViewModel =
                ndResourceService.resourceQuery4IntelliKnowledge(includesList, chapterId, pageSize, offset);
        rListViewModel.setLimit(limit);

        return rListViewModel;
    }

    /**
     * 参数校验和处理
     * <p>Create Time: 2016年3月28日   </p>
     * <p>Create author: xiezy   </p>
     */
    private Map<String, Object> requestParamVerifyAndHandle(String resType, String resCodes, String includes,
                                                            Set<String> categories, Set<String> categoryExclude, Set<String> relations, Set<String> coverages, List<String> props,
                                                            List<String> orderBy, String limit, QueryType queryType, String reverse){
        //reverse,默认为false
        boolean reverseBoolean = false;
        if(StringUtils.isNotEmpty(reverse) && reverse.equals("true")){
            reverseBoolean = true;
        }

        /*
         * 入参处理 + 校验
         */
        // 0.res_type
        verificateResType(resType, resCodes);

        // 1.includes
        List<String> includesList = IncludesConstant.getValidIncludes(includes);
//        List<String> ignoreAttributes = new ArrayList<String>();
//        if(!IncludesConstant.isNotContainsAttributes(includesList, ignoreAttributes)){
//
//            LOG.error("检索接口目前只支持:TI,EDU,LC,CG,CR");
//
//            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
//                    LifeCircleErrorMessageMapper.CommonSearchParamError.getCode(),
//                    "检索接口目前只支持:TI,EDU,LC,CG,CR");
//        }

        //先将参数中的5个Set中的null和""去掉
        categories = CollectionUtils.removeEmptyDeep(categories);
        categoryExclude = CollectionUtils.removeEmptyDeep(categoryExclude);
        relations = CollectionUtils.removeEmptyDeep(relations);
        coverages = CollectionUtils.removeEmptyDeep(coverages);
        props = CollectionUtils.removeEmptyDeep(props);

        // 2.categories
        if(CollectionUtils.isEmpty(categories)){
            categories = null;
        }else {
            categories = CommonHelper.doAdapterCategories4DB(resType, categories);
        }

        //categoryExclude
        if(CollectionUtils.isEmpty(categoryExclude)){
            categoryExclude = null;
        }

        // 3.relations,格式:stype/suuid/r_type
        List<Map<String,String>> relationsMap = new ArrayList<Map<String,String>>();
        if(CollectionUtils.isEmpty(relations)){
            relationsMap = null;
        }else{
            for(String relation : relations){
               /* Map<String,String> map = new HashMap<String, String>();
                //对于入参的relation每个在最后追加一个空格，以保证elemnt的size为3
                relation = relation + " ";
                List<String> elements = Arrays.asList(relation.split("/"));
                //格式错误判断
                if(elements.size() != 3){

                    LOG.error(relation + "--relation格式错误");

                    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                            LifeCircleErrorMessageMapper.CommonSearchParamError.getCode(),
                            relation + "--relation格式错误");
                }

                String resourceType = elements.get(0).trim();
                String resourceUuid = elements.get(1).trim();
                String relationType = elements.get(2).trim();

                //判断源资源是否存在,stype + suuid
                if(!elements.get(1).trim().endsWith("$")){//不为递归查询时才校验
                    CommonHelper.resourceExist(elements.get(0).trim(), elements.get(1).trim(), ResourceType.RESOURCE_SOURCE);
                }else{
                	// "relation参数进行递归查询时,目前仅支持:chapters,knowledges"
					if (!IndexSourceType.ChapterType.getName().equals(elements.get(0)) &&
							!IndexSourceType.KnowledgeType.getName().equals(elements.get(0))) {
						throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
								LifeCircleErrorMessageMapper.CommonSearchParamError.getCode(),
								"relation参数进行递归查询时,目前仅支持:chapters,knowledges");
					}

                }
                //r_type的特殊处理
                if(StringUtils.isEmpty(relationType) || RelationType.shouldBeAssociate(relationType)){
                    relationType = RelationType.ASSOCIATE.getName();
                }

                map.put("stype", resourceType);
                map.put("suuid", resourceUuid);
                map.put("rtype", relationType);*/

                Map<String,String> map = ParameterVerificationHelper.relationVerification(relation,queryType);
                relationsMap.add(map);
            }
        }

        // 4.coverages,格式:Org/uuid/SHAREING
        List<String> coveragesList = new ArrayList<String>();
        if(CollectionUtils.isEmpty(coverages)){
            coveragesList = null;
        }else{
            for(String coverage : coverages){
                String c = ParameterVerificationHelper.coverageVerification(coverage);

                coveragesList.add(c);
            }
        }

        // 5.props,语法 [属性] [操作] [值]
        Map<String,Set<String>> propsMap = new HashMap<String, Set<String>>();
        //获取props的.properties文件,目的是筛选匹配支持的属性
        Properties properties = null;
        switch (queryType) {
            case DB:
                properties = LifeCircleApplicationInitializer.props_properties_db;
                break;
            case ES:
            case TITAN:
                properties = LifeCircleApplicationInitializer.props_properties_es;
                break;
            default:
                break;
        }

        if(CollectionUtils.isEmpty(props)){
            propsMap = null;
        }else{
            for(String prop : props){
                if(ParameterVerificationHelper.isRangeQuery(prop)){
                    if (ParameterVerificationHelper.isRangeQuery(prop) && judgeOnlyContainTheOneRangOp(prop, PropOperationConstant.OP_GT)) {//Only GT

                        this.dealTimeParam(resType, prop, properties, propsMap, "GT");
                    }else if (ParameterVerificationHelper.isRangeQuery(prop) && judgeOnlyContainTheOneRangOp(prop, PropOperationConstant.OP_LT)) {//Only LT

                        this.dealTimeParam(resType, prop, properties, propsMap, "LT");
                    }else if (ParameterVerificationHelper.isRangeQuery(prop) && judgeOnlyContainTheOneRangOp(prop, PropOperationConstant.OP_LE)) {//Only LE

                        this.dealTimeParam(resType, prop, properties, propsMap, "LE");
                    }else if (ParameterVerificationHelper.isRangeQuery(prop) && judgeOnlyContainTheOneRangOp(prop, PropOperationConstant.OP_GE)) {//Only GE

                        this.dealTimeParam(resType, prop, properties, propsMap, "GE");
                    }else if (ParameterVerificationHelper.isRangeQuery(prop) && prop.contains(PropOperationConstant.OP_GT)
                            && prop.contains(PropOperationConstant.OP_LT)) {//GT,LT

                        dealTimeParam4HaveAndOp(resType, prop, properties, propsMap, PropOperationConstant.OP_GT, PropOperationConstant.OP_LT);
                    }else if (ParameterVerificationHelper.isRangeQuery(prop) && prop.contains(PropOperationConstant.OP_GT)
                            && prop.contains(PropOperationConstant.OP_LE)) {//GT,LE

                        dealTimeParam4HaveAndOp(resType, prop, properties, propsMap, PropOperationConstant.OP_GT, PropOperationConstant.OP_LE);
                    }else if (ParameterVerificationHelper.isRangeQuery(prop) && prop.contains(PropOperationConstant.OP_GT)
                            && prop.contains(PropOperationConstant.OP_GE)) {//GT,GE

                        dealTimeParam4HaveAndOp(resType, prop, properties, propsMap, PropOperationConstant.OP_GT, PropOperationConstant.OP_GE);
                    }else if (ParameterVerificationHelper.isRangeQuery(prop) && prop.contains(PropOperationConstant.OP_LT)
                            && prop.contains(PropOperationConstant.OP_LE)) {//LT,LE

                        dealTimeParam4HaveAndOp(resType, prop, properties, propsMap, PropOperationConstant.OP_LT, PropOperationConstant.OP_LE);
                    }else if (ParameterVerificationHelper.isRangeQuery(prop) && prop.contains(PropOperationConstant.OP_LT)
                            && prop.contains(PropOperationConstant.OP_GE)) {//LT,GE

                        dealTimeParam4HaveAndOp(resType, prop, properties, propsMap, PropOperationConstant.OP_LT, PropOperationConstant.OP_GE);
                    }else if (ParameterVerificationHelper.isRangeQuery(prop) && prop.contains(PropOperationConstant.OP_LE)
                            && prop.contains(PropOperationConstant.OP_GE)) {//LE,GE

                        dealTimeParam4HaveAndOp(resType, prop, properties, propsMap, PropOperationConstant.OP_LE, PropOperationConstant.OP_GE);
                    }
                }else if(prop.contains(" " + PropOperationConstant.OP_EQ + " ")){//eq
                    List<String> elements = Arrays.asList(prop.split(" " + PropOperationConstant.OP_EQ + " "));
                    //格式错误判断
                    if(CollectionUtils.isEmpty(elements) || elements.size() != 2 || StringUtils.isEmpty(elements.get(0)) || StringUtils.isEmpty(elements.get(1))){

                        LOG.error(prop + "--prop格式错误");

                        throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                LifeCircleErrorMessageMapper.CommonSearchParamError.getCode(),
                                prop + "--prop格式错误");
                    }

                    if(propsMap.containsKey(properties.getProperty(resType + "_" + elements.get(0)))){//已存在该属性
                        Set<String> propValues = propsMap.get(properties.getProperty(resType + "_" + elements.get(0)));
                        propValues.add(elements.get(1));
                    }else{//新属性
                        if(properties.containsKey(resType + "_" + elements.get(0))){
                            Set<String> propValuesNew = new HashSet<String>();
                            propValuesNew.add(elements.get(1));
                            propsMap.put(properties.getProperty(resType + "_" + elements.get(0)), propValuesNew);
                        }else{

                            LOG.error(prop + ":" + elements.get(0) + "--不支持该属性查询 OR 属性名错误(驼峰形式)");

                            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                    LifeCircleErrorMessageMapper.CommonSearchParamError.getCode(),
                                    prop + ":" + elements.get(0) + "--不支持该属性查询 OR 属性名错误(驼峰形式)");
                        }
                    }
                }else if(prop.contains(" " + PropOperationConstant.OP_IN + " ")){//in
                    List<String> elements = Arrays.asList(prop.split(" " + PropOperationConstant.OP_IN + " "));
                    //格式错误判断
                    if(CollectionUtils.isEmpty(elements) || elements.size() != 2 || StringUtils.isEmpty(elements.get(0)) || StringUtils.isEmpty(elements.get(1))){

                        LOG.error(prop + "--prop格式错误");

                        throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                LifeCircleErrorMessageMapper.CommonSearchParamError.getCode(),
                                prop + "--prop格式错误");
                    }

                    if(propsMap.containsKey(properties.getProperty(resType + "_" + elements.get(0)))){//已存在该属性
                        Set<String> propValues = propsMap.get(properties.getProperty(resType + "_" + elements.get(0)));
                        propValues.addAll(Arrays.asList(elements.get(1).split("\\|")));
                    }else{//新属性
                        if(properties.containsKey(resType + "_" + elements.get(0))){
                            Set<String> propValuesNew = new HashSet<String>();
                            propValuesNew.addAll(Arrays.asList(elements.get(1).split("\\|")));
                            propsMap.put(properties.getProperty(resType + "_" + elements.get(0)), propValuesNew);
                        }else{

                            LOG.error(prop + ":" + elements.get(0) + "--不支持该属性查询 OR 属性名错误(驼峰形式)");

                            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                    LifeCircleErrorMessageMapper.CommonSearchParamError.getCode(),
                                    prop + ":" + elements.get(0) + "--不支持该属性查询 OR 属性名错误(驼峰形式)");
                        }
                    }
                }else if(prop.contains(" " + PropOperationConstant.OP_NE + " ")){//ne
                    List<String> elements = Arrays.asList(prop.split(" " + PropOperationConstant.OP_NE + " "));
                    //格式错误判断
                    if(CollectionUtils.isEmpty(elements) || elements.size() != 2 || StringUtils.isEmpty(elements.get(0)) || StringUtils.isEmpty(elements.get(1))){

                        LOG.error(prop + "--prop格式错误");

                        throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                LifeCircleErrorMessageMapper.CommonSearchParamError.getCode(),
                                prop + "--prop格式错误");
                    }

                    if(propsMap.containsKey(properties.getProperty(resType + "_" + elements.get(0)) + "_NE")){//已存在该属性
                        Set<String> propValues = propsMap.get(properties.getProperty(resType + "_" + elements.get(0)) + "_NE");
                        propValues.add(elements.get(1));
                    }else{//新属性
                        if(properties.containsKey(resType + "_" + elements.get(0))){
                            Set<String> propValuesNew = new HashSet<String>();
                            propValuesNew.add(elements.get(1));
                            propsMap.put(properties.getProperty(resType + "_" + elements.get(0)) + "_NE", propValuesNew);
                        }else{

                            LOG.error(prop + ":" + elements.get(0) + "--不支持该属性查询 OR 属性名错误(驼峰形式)");

                            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                    LifeCircleErrorMessageMapper.CommonSearchParamError.getCode(),
                                    prop + ":" + elements.get(0) + "--不支持该属性查询 OR 属性名错误(驼峰形式)");
                        }
                    }
                }else if(prop.contains(" " + PropOperationConstant.OP_LIKE + " ")){//like
                    List<String> elements = Arrays.asList(prop.split(" " + PropOperationConstant.OP_LIKE + " "));
                    //格式错误判断
                    if(CollectionUtils.isEmpty(elements) || elements.size() != 2 || StringUtils.isEmpty(elements.get(0)) || StringUtils.isEmpty(elements.get(1))){

                        LOG.error(prop + "--prop格式错误");

                        throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                LifeCircleErrorMessageMapper.CommonSearchParamError.getCode(),
                                prop + "--prop格式错误");
                    }

                    if(propsMap.containsKey(properties.getProperty(resType + "_" + elements.get(0)) + "_LIKE")){//已存在该属性
                        Set<String> propValues = propsMap.get(properties.getProperty(resType + "_" + elements.get(0)) + "_LIKE");
                        propValues.add(elements.get(1));
                    }else{//新属性
                        if(properties.containsKey(resType + "_" + elements.get(0))){
                            Set<String> propValuesNew = new HashSet<String>();
                            propValuesNew.add(elements.get(1));
                            propsMap.put(properties.getProperty(resType + "_" + elements.get(0)) + "_LIKE", propValuesNew);
                        }else{

                            LOG.error(prop + ":" + elements.get(0) + "--不支持该属性查询 OR 属性名错误(驼峰形式)");

                            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                    LifeCircleErrorMessageMapper.CommonSearchParamError.getCode(),
                                    prop + ":" + elements.get(0) + "--不支持该属性查询 OR 属性名错误(驼峰形式)");
                        }
                    }
                }else{

                    LOG.error(prop + "--prop目前支持eq,in,ne,like操作,以及支持create_time和lastupdate的gt,lt操作");

                    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                            LifeCircleErrorMessageMapper.CommonSearchParamError.getCode(),
                            prop + "--prop目前支持eq,in,ne,like操作,以及支持create_time和lastupdate的gt,lt操作");
                }
            }
        }

        //6.orderBy
        Map<String,String> orderMap = new LinkedHashMap<String, String>();
        if(CollectionUtils.isEmpty(orderBy)){
            orderMap = null;
        }else{
            for(String order : orderBy){
                List<String> elements = Arrays.asList(order.split(" "));

                //格式错误判断
                if(CollectionUtils.isEmpty(elements) || elements.size() != 2 || StringUtils.isEmpty(elements.get(0)) || StringUtils.isEmpty(elements.get(1))){

                    LOG.error(orderBy + "--orderBy格式错误");

                    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                            LifeCircleErrorMessageMapper.CommonSearchParamError.getCode(),
                            orderBy + "--orderBy格式错误");
                }

                if(!properties.containsKey("order_" + elements.get(0))){
                    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                            LifeCircleErrorMessageMapper.CommonSearchParamError.getCode(),
                            elements.get(0) + "--该属性暂不支持排序");
                }

                if(!elements.get(1).equalsIgnoreCase(PropOperationConstant.OP_DESC) &&
                        !elements.get(1).equalsIgnoreCase(PropOperationConstant.OP_ASC)){

                    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                            LifeCircleErrorMessageMapper.CommonSearchParamError.getCode(),
                            orderBy + "--orderBy格式错误,排序方式仅有DESC和ASC");
                }

                if(!orderMap.containsKey(properties.getProperty("order_" + elements.get(0)))){
                    orderMap.put(properties.getProperty("order_" + elements.get(0)), elements.get(1));
                }
            }
        }

        //7. limit
        limit = CommonHelper.checkLimitMaxSize(limit);

        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("include", includesList);
        paramMap.put("category", categories);
        paramMap.put("categoryExclude", categoryExclude);
        paramMap.put("relation", relationsMap);
        paramMap.put("coverage", coveragesList);
        paramMap.put("prop", propsMap);
        paramMap.put("orderby", orderMap);
        paramMap.put("reverse", reverseBoolean);
        paramMap.put("limit", limit);

        return paramMap;
    }

    /**
     * 验证resType
     * @author xiezy
     * @date 2016年7月13日
     * @param resType
     * @param resCodes
     */
    private void verificateResType(String resType, String resCodes){
        if (resType.equals(IndexSourceType.ChapterType.getName())) {

            LOG.error("resType不能为chapters");

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CommonSearchParamError
                            .getCode(), "resType不能为chapters");
        } else if (resType.equals(Constant.RESTYPE_EDURESOURCE)) {
            if (StringUtils.isEmpty(resCodes)) {
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        LifeCircleErrorMessageMapper.CommonSearchParamError
                                .getCode(), "resType为"
                        + Constant.RESTYPE_EDURESOURCE
                        + "时,rescode不能为空");
            }
        } else {
            commonServiceHelper.getRepository(resType);
        }
    }

    /**
     * 时间的校验和格式化
     * <p>Create Time: 2015年9月30日   </p>
     * <p>Create author: xiezy   </p>
     * @param time
     * @return
     */
    private String verificateAndFormatTime(String time){
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        //验证time是否符合时间格式
        Date date = null;
        try {
            if(StringUtils.isNotEmpty(time)){
                date  = sdf2.parse(time);
                if(date != null){
                    time = sdf2.format(date);
                }
            }
        } catch (ParseException e) {
            try {
                if(StringUtils.isNotEmpty(time)){
                    date  = sdf1.parse(time);
                    if(date != null){
                        time = sdf1.format(date);
                    }
                }
            } catch (ParseException e2) {
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        LifeCircleErrorMessageMapper.CommonSearchParamError.getCode(),
                        "时间格式错误,格式为:yyyy-MM-dd HH:mm:ss或 yyyy-MM-dd HH:mm:ss.SSS");
            }
        }

        return time;
    }

    /**
     * 判断是哪个时间范围操作符
     * @author xiezy
     * @date 2016年4月21日
     * @param prop
     * @return
     */
    private boolean judgeOnlyContainTheOneRangOp(String prop,String op){
        if(op.equals(PropOperationConstant.OP_GT)){
            if(prop.contains(PropOperationConstant.OP_GT)
                    && !prop.contains(PropOperationConstant.OP_LT)
                    && !prop.contains(PropOperationConstant.OP_LE)
                    && !prop.contains(PropOperationConstant.OP_GE)){
                return true;
            }
        }else if(op.equals(PropOperationConstant.OP_LT)){
            if(!prop.contains(PropOperationConstant.OP_GT)
                    && prop.contains(PropOperationConstant.OP_LT)
                    && !prop.contains(PropOperationConstant.OP_LE)
                    && !prop.contains(PropOperationConstant.OP_GE)){
                return true;
            }
        }else if(op.equals(PropOperationConstant.OP_LE)){
            if(!prop.contains(PropOperationConstant.OP_GT)
                    && !prop.contains(PropOperationConstant.OP_LT)
                    && prop.contains(PropOperationConstant.OP_LE)
                    && !prop.contains(PropOperationConstant.OP_GE)){
                return true;
            }
        }else if(op.equals(PropOperationConstant.OP_GE)){
            if(!prop.contains(PropOperationConstant.OP_GT)
                    && !prop.contains(PropOperationConstant.OP_LT)
                    && !prop.contains(PropOperationConstant.OP_LE)
                    && prop.contains(PropOperationConstant.OP_GE)){
                return true;
            }
        }

        return false;
    }

    /**
     * 处理时间常数
     * <p>Create Time: 2015年9月30日   </p>
     * <p>Create author: xiezy   </p>
     * @param resType
     * @param prop
     * @param properties
     * @param propsMap
     * @param gtOrLt
     */
    private void dealTimeParam(String resType,String prop,Properties properties,Map<String,Set<String>> propsMap,String op){
        List<String> elements = new ArrayList<String>();
        if(op.equals("LT")){
            elements = Arrays.asList(prop.split(" " + PropOperationConstant.OP_LT + " "));
        }else if(op.equals("GT")){
            elements = Arrays.asList(prop.split(" " + PropOperationConstant.OP_GT + " "));
        }else if(op.equals("GE")){
            elements = Arrays.asList(prop.split(" " + PropOperationConstant.OP_GE + " "));
        }else if(op.equals("LE")){
            elements = Arrays.asList(prop.split(" " + PropOperationConstant.OP_LE + " "));
        }

        //格式错误判断
        if(CollectionUtils.isEmpty(elements) || elements.size() != 2 || StringUtils.isEmpty(elements.get(0)) || StringUtils.isEmpty(elements.get(1))){

            LOG.error(prop + "--prop格式错误");

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CommonSearchParamError.getCode(),
                    prop + "--prop格式错误");
        }

        if(propsMap.containsKey(properties.getProperty(resType + "_" + elements.get(0)) + "_" + op)){//已有属性
            Set<String> propValues = propsMap.get(properties.getProperty(resType + "_" + elements.get(0)) + "_" + op);
            String time = this.verificateAndFormatTime(elements.get(1));
            propValues.add(time);
        }else{
            if(properties.containsKey(resType + "_" + elements.get(0))){
                Set<String> propValuesNew = new HashSet<String>();
                String time = this.verificateAndFormatTime(elements.get(1));
                propValuesNew.add(time);
                propsMap.put(properties.getProperty(resType + "_" + elements.get(0)) + "_" + op, propValuesNew);
            }else{
                LOG.error(prop + ":" + elements.get(0) + "--不支持该属性查询 OR 属性名错误(驼峰形式)");

                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        LifeCircleErrorMessageMapper.CommonSearchParamError.getCode(),
                        prop + ":" + elements.get(0) + "--不支持该属性查询 OR 属性名错误(驼峰形式)");
            }
        }
    }

    /**
     * 通用查询-时间参数带and的处理
     * @author xiezy
     * @date 2016年7月11日
     * @param resType
     * @param prop
     * @param properties
     * @param propsMap
     * @param op1
     * @param op2
     */
    private void dealTimeParam4HaveAndOp(String resType,String prop,Properties properties,Map<String,Set<String>> propsMap,String op1,String op2){
        List<String> elements = Arrays.asList(prop.split(" and "));
        // 格式错误判断
        if (CollectionUtils.isEmpty(elements) || elements.size() != 2
                || StringUtils.isEmpty(elements.get(0)) || StringUtils.isEmpty(elements.get(1))) {

            LOG.error(prop + "--prop格式错误");

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CommonSearchParamError.getCode(), prop + "--prop格式错误");
        }

        if (elements.get(0).contains(op1)) {
            this.dealTimeParam(resType, elements.get(0), properties, propsMap, op1.toUpperCase());
        }else {
            this.dealTimeParam(resType, elements.get(0), properties, propsMap, op2.toUpperCase());
        }

        if (elements.get(1).contains(op1)) {
            if(prop.startsWith("create_time")){
                this.dealTimeParam(resType, "create_time " + elements.get(1), properties, propsMap, op1.toUpperCase());
            }else{
                this.dealTimeParam(resType, "lastupdate " + elements.get(1), properties, propsMap, op1.toUpperCase());
            }
        }else {
            if(prop.startsWith("create_time")){
                this.dealTimeParam(resType, "create_time " + elements.get(1), properties, propsMap, op2.toUpperCase());
            }else{
                this.dealTimeParam(resType, "lastupdate " + elements.get(1), properties, propsMap, op2.toUpperCase());
            }
        }
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
        return ndResourceService.getUploadUrl(res_type, uuid, uid, renew, coverage);
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
        AccessModel am = ndResourceService.getDownloadUrl(res_type, uuid, uid, key);

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

    /**
     * 获取资源预览图的列表
     *
     * @author:xuzy
     * @date:2015年9月28日
     * @param res_type
     * @param uuid
     * @return
     */
    @RequestMapping(value="/{uuid}/previews", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public Map<String,Object> getResPreviewUrls(@PathVariable(value="res_type") String resType,@PathVariable String uuid){
        return ndResourceService.getResPreviewUrls(resType, uuid);
    }

    /**
     * 离线版资源同步入库，具体流程是：
     * 1.同步上传离线版的元数据。
     * 2.上传结束后，提交元数据的存储相对地址信息。
     * 3.解析离线元数据进行入库。
     * http://wiki.sdp.nd/index.php?title=LCMS_API_RA00110
     * @param viewModel 元数据的存储相对地址信息
     * @param validResult BindingResult
     * @param resType 资源类型
     * @param uuid 资源id
     * @since
     */
    @RequestMapping(value = "/{uuid}/actions/init", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE })
    public void createOfflineMetadata(@Valid @RequestBody OfflineMetadataViewModel viewModel,
                                      BindingResult validResult,
                                      @PathVariable(value = "res_type") String resType,
                                      @PathVariable String uuid) throws Exception {
        ValidResultHelper.valid(validResult,
                "LC/CREATE_OFFLINE_METADATA_PARAM_VALID_FAIL",
                "NDResourceController",
                "createOfflineMetadata");

        commonServiceHelper.assertDownloadable(resType);

        if (!CommonHelper.checkUuidPattern(uuid)) {
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CheckIdentifierFail.getCode(),
                    LifeCircleErrorMessageMapper.CheckIdentifierFail.getMessage());
        }

        if(CommonHelper.resourceExistNoException(resType, uuid, ResourceType.RESOURCE_SOURCE)) {
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR, "LC/RESOURCE_CREATED", "元数据已创建");
        }

        if (viewModel.getTechInfo() == null || !viewModel.getTechInfo().containsKey("href")) {
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.ChecTechInfoFail);
        }

        String rootPath = NDResourceServiceImpl.getRootPathFromLocation(viewModel.getTechInfo()
                .get("href")
                .getLocation());

        CSInstanceInfo csInstanceInfo = NDResourceServiceImpl.getCsInstanceAccordingRootPath(rootPath);

        String path = NDResourceServiceImpl.producePath(rootPath, resType, uuid);

        CsSession csSession = contentService.getAssignSession(path, csInstanceInfo.getServiceId());

        if (csSession == null) {
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR, "LC/MEDIA_DOWNLOAD_FAIL", "取不到session，路径:" + path);
        }

        String url = csInstanceInfo.getUrl() + "/download?path=" + path + "/metadata.json";

        String metaDataJson = DownloadFile(url, csSession.getSession());

        if (StringUtils.isEmpty(metaDataJson)) {
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR, "LC/MEDIA_DOWNLOAD_FAIL", "下载文件：" + url
                    + "失败");
        }

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(metaDataJson, httpHeaders);

        String result = wafSecurityHttpClient.executeForObject(Constant.LIFE_CYCLE_DOMAIN_URL + "/v0.6/" + resType
                + "/" + uuid, HttpMethod.POST, entity, String.class);

        if (null == result) {

            LOG.warn("创建资源metadata失败");

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR, "LC/MEDIA_CREATE_FAIL", "创建资源metadata失败");

        }
    }

    /**
     * 离线版资源同步入库，具体流程是：
     * 1.同步上传离线版的元数据。
     * 2.上传结束后，提交元数据的存储相对地址信息。
     * 3.解析离线元数据进行入库。
     * http://wiki.sdp.nd/index.php?title=LCMS_API_RA00110
     * @param viewModel 元数据的存储相对地址信息
     * @param validResult BindingResult
     * @param resType 资源类型
     * @param uuid 资源id
     * @since
     */
    @RequestMapping(value = "/{uuid}/actions/refresh", method = RequestMethod.PUT, consumes = { MediaType.APPLICATION_JSON_VALUE })
    public void refreshOfflineMetadata(@Valid @RequestBody OfflineMetadataViewModel viewModel,
                                       BindingResult validResult,
                                       @PathVariable(value = "res_type") String resType,
                                       @PathVariable String uuid) throws Exception {
        ValidResultHelper.valid(validResult,
                "LC/CREATE_OFFLINE_METADATA_PARAM_VALID_FAIL",
                "NDResourceController",
                "createOfflineMetadata");

        commonServiceHelper.assertDownloadable(resType);

        if (!CommonHelper.checkUuidPattern(uuid)) {
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CheckIdentifierFail.getCode(),
                    LifeCircleErrorMessageMapper.CheckIdentifierFail.getMessage());
        }

        if(!CommonHelper.resourceExistNoException(resType, uuid, ResourceType.RESOURCE_SOURCE)) {
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR, "LC/RESOURCE_NOT_EXIST", "资源不存在");
        }

        if (viewModel.getTechInfo() == null || !viewModel.getTechInfo().containsKey("href")) {
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.ChecTechInfoFail);
        }

        String rootPath = NDResourceServiceImpl.getRootPathFromLocation(viewModel.getTechInfo()
                .get("href")
                .getLocation());

        CSInstanceInfo csInstanceInfo = NDResourceServiceImpl.getCsInstanceAccordingRootPath(rootPath);

        String path = NDResourceServiceImpl.producePath(rootPath, resType, uuid);

        CsSession csSession = contentService.getAssignSession(path, csInstanceInfo.getServiceId());

        if (csSession == null) {
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR, "LC/MEDIA_DOWNLOAD_FAIL", "取不到session，路径:" + path);
        }

        String url = csInstanceInfo.getUrl() + "/download?path=" + path + "/metadata.json";

        String metaDataJson = DownloadFile(url, csSession.getSession());

        if (StringUtils.isEmpty(metaDataJson)) {
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR, "LC/MEDIA_DOWNLOAD_FAIL", "下载文件：" + url
                    + "失败");
        }

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(metaDataJson, httpHeaders);

        String result = wafSecurityHttpClient.executeForObject(Constant.LIFE_CYCLE_DOMAIN_URL + "/v0.6/" + resType
                + "/" + uuid, HttpMethod.PUT, entity, String.class);

        if (null == result) {

            LOG.warn("创建资源metadata失败");

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR, "LC/MEDIA_CREATE_FAIL", "创建资源metadata失败");

        }
    }

    /**
     * 新增版本资源
     * @author xuzy
     * @date 2016年7月11日
     * @param versionViewModel
     * @param validResult
     * @param resourceType
     * @param uuid
     * @param userInfo
     * @return
     */
    @RequestMapping(value = "/{uuid}/newversion", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE },produces={MediaType.APPLICATION_JSON_VALUE})
    public ResourceViewModel createNewVersion(
            @Validated(LifecycleDefault.class) @RequestBody VersionViewModel versionViewModel,
            BindingResult validResult,
            @PathVariable("res_type") String resourceType,
            @PathVariable String uuid,
            @AuthenticationPrincipal UserInfo userInfo) {
        //1、参数校验
        ValidResultHelper.valid(validResult,
                "LC/CREATE_RESOURCE_NEW_VERSION",
                "NDResourceController",
                "createNewVersion");

        Map<String,List<String>> tagMap = versionViewModel.getRelations();
        if(tagMap != null && tagMap.containsKey("tags") && tagMap.get("tags") != null && CollectionUtils.isNotEmpty(tagMap.get("tags"))){
            ResourceViewModel newResource = null;
            if(CommonServiceHelper.isQuestionDb(resourceType)){
                newResource = ndResourceService.createNewVersion4Question(resourceType, uuid, versionViewModel,userInfo);
            }else{
                newResource = ndResourceService.createNewVersion(resourceType, uuid, versionViewModel,userInfo);
            }
            if (ResourceTypeSupport.isValidEsResourceType(resourceType)
                    && StringUtils.isNotEmpty(newResource.getIdentifier())) {
                esResourceOperation.asynAdd(new Resource(resourceType, newResource.getIdentifier()));
            }
            //由于tech_info数据没有拷贝，不异步上传离线文件
//    		offlineService.writeToCsAsync(resourceType, newResource.getIdentifier());
            return newResource;
        }else{
            //参数校验不通过
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR, "LC/CHECK_PARAM_FAIL", "relations.tags不能为空");
        }
    }

    /**
     * 资源版本检测接口
     * @author xuzy
     * @date 2016年7月11日
     * @param resourceType
     * @param uuid
     * @return
     */
    @RequestMapping(value = "/{uuid}/version/check", method = RequestMethod.GET, produces={MediaType.APPLICATION_JSON_VALUE})
    public Map<String, Map<String, Object>> versionCheck(
            @PathVariable("res_type") String resourceType,
            @PathVariable String uuid) {
        return ndResourceService.versionCheck(resourceType, uuid);
    }

    /**
     * 资源版本发布接口
     * @author xuzy
     * @date 2016年7月11日
     * @param resourceType
     * @param uuid
     * @param paramMap
     * @return
     */
    @RequestMapping(value = "/{uuid}/release", method = RequestMethod.PUT, produces={MediaType.APPLICATION_JSON_VALUE})
    public Map<String, Object> versionRelease(@PathVariable("res_type") String resourceType,
                                              @PathVariable String uuid,@RequestBody Map<String,String> paramMap){
        if(CollectionUtils.isNotEmpty(paramMap)){
            Iterator<Map.Entry<String,String>> it = paramMap.entrySet().iterator();
            if(it.hasNext()){
                Entry<String,String> entry = it.next();
                boolean flag = LifecycleStatus.isLegalStatus(entry.getValue());
                if(!flag){
                    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR, "LC/CHECK_PARAM_FAIL", "status参数不正确！值："+entry.getValue());
                }
            }
        }
        return ndResourceService.versionRelease(resourceType, uuid, paramMap);
    }

    /**
     * 由Model转为ViewModel
     */
    private ResourceViewModel changeToView(ResourceModel model, String resourceType,List<String> includes) {

        return  CommonHelper.changeToView(model,resourceType,includes,commonServiceHelper);
    }

    /**
     * 从cs下载文件到本地
     *
     * @param url 文件所在下载路径
     * @param destDir 存储目标路径
     *
     * @return
     */
    private String DownloadFile(String url, String session) throws Exception {
        HttpURLConnection connection = (HttpURLConnection)new URL(url+"&session="+session).openConnection();
        connection.setRequestMethod("GET");
        connection.connect();

        int responseCode = connection.getResponseCode();
        if (responseCode < 200 || responseCode >= 300) {
            InputStream in = connection.getErrorStream();
            StringBuffer out = new StringBuffer();
            byte[] b = new byte[4096];
            for (int n; (n = in.read(b)) != -1;) {
                out.append(new String(b, 0, n));
            }

            LOG.error("下载文件：" + url + "失败:" + out.toString());

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR, "LC/MEDIA_DOWNLOAD_FAIL", "下载文件：" + url
                    + "失败:" + out.toString());
        }

        InputStream input = connection.getInputStream();
        String rt = null;
        try {
            rt = IOUtils.toString(input, "utf-8");
        } finally {
            if (input != null)
                input.close();
        }

        return rt;
    }

    /**
     * 将下载信息统计至数据库中
     * @param bsyskey
     * @param res_type
     * @param uuid
     */
    private void syncResourceStatis(String bsyskey,String resType,String uuid){
        if(CommonServiceHelper.isQuestionDb(resType)){
            statisticalService4QuestionDB.addDownloadStatistical(bsyskey, resType, uuid);
        }else{
            statisticalService.addDownloadStatistical(bsyskey, resType, uuid);
        }
    }

    public static enum QueryType{
        DB,ES,TITAN,TITAN_ES
    }

    /**
     * 统计教材章节下的资源数量
     * @author xiezy
     * @date 2016年7月13日
     * @param resType
     * @param tmId
     * @param chapterIds
     * @param coverages
     * @param categories
     * @param isAll
     * @return
     */
    @RequestMapping(value = "/statistics/counts", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE },params = { "tmid"})
    public Map<String, ChapterStatisticsViewModel> statisticsCountsByChapters(
            @PathVariable(value="res_type") String resType,
            @RequestParam(value="tmid") String tmId,
            @RequestParam(required=false,value="chapterid") Set<String> chapterIds,
            @RequestParam(required=false,value="coverage") Set<String> coverages,
            @RequestParam(required=false,value="category") Set<String> categories,
            @RequestParam(required=false,value="is_all",defaultValue="false") boolean isAll){

        //参数校验
        verificateResType(resType, "");
        if(resType.equals(IndexSourceType.QuestionType.getName()) ||
                resType.equals(IndexSourceType.SourceCourseWareObjectType.getName())){
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CommonSearchParamError.getCode(),
                    "暂不支持questions和coursewareobjects资源类型的查询！");
        }

        categories = CollectionUtils.removeEmptyDeep(categories);
        coverages = CollectionUtils.removeEmptyDeep(coverages);
        chapterIds = CollectionUtils.removeEmptyDeep(chapterIds);

        if(StringUtils.isEmpty(tmId)){
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CommonSearchParamError.getCode(),
                    "教材id不能为空");
        }else if(tmId.equals("none")){
            if(CollectionUtils.isEmpty(chapterIds)){
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        LifeCircleErrorMessageMapper.CommonSearchParamError.getCode(),
                        "tmid=none时,chapterid不允许为空");
            }
        }

        List<String> coverageList = new ArrayList<String>();
        if(CollectionUtils.isNotEmpty(coverages)){
            for(String cv : coverages){
                String coverage = ParameterVerificationHelper.coverageVerification(cv);
                coverageList.add(coverage);
            }
        }

        return ndResourceService.statisticsCountsByChapters(resType, tmId, chapterIds, coverageList, categories, isAll);
    }
}