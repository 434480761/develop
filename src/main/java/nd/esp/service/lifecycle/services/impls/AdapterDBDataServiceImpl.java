package nd.esp.service.lifecycle.services.impls;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Query;

import nd.esp.service.lifecycle.controllers.AdapterDBDataController;
import nd.esp.service.lifecycle.educommon.models.ResContributeModel;
import nd.esp.service.lifecycle.educommon.models.ResourceModel;
import nd.esp.service.lifecycle.educommon.services.NDResourceService;
import nd.esp.service.lifecycle.educommon.services.impl.CommonServiceHelper;
import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;
import nd.esp.service.lifecycle.educommon.vos.constant.IncludesConstant;
import nd.esp.service.lifecycle.entity.PackagingParam;
import nd.esp.service.lifecycle.entity.elasticsearch.Resource;
import nd.esp.service.lifecycle.entity.lifecycle.AdapterTaskResult;
import nd.esp.service.lifecycle.models.CategoryDataModel;
import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.EspRepository;
import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.repository.ds.ComparsionOperator;
import nd.esp.service.lifecycle.repository.ds.Item;
import nd.esp.service.lifecycle.repository.ds.LogicalOperator;
import nd.esp.service.lifecycle.repository.ds.ValueUtils;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.Knowledge;
import nd.esp.service.lifecycle.repository.model.ResourceCategory;
import nd.esp.service.lifecycle.repository.model.ResourcePreviews;
import nd.esp.service.lifecycle.repository.sdk.AssetRepository;
import nd.esp.service.lifecycle.repository.sdk.KnowledgeRepository;
import nd.esp.service.lifecycle.repository.sdk.ResourceCategoryRepository;
import nd.esp.service.lifecycle.repository.sdk.ResourcePreviewsRepository;
import nd.esp.service.lifecycle.repository.sdk.TaskStatusInfoRepository;
import nd.esp.service.lifecycle.repository.sdk.TechInfoRepository;
import nd.esp.service.lifecycle.repository.sdk.impl.ServicesManager;
import nd.esp.service.lifecycle.services.AdapterDBDataService;
import nd.esp.service.lifecycle.services.CategoryService;
import nd.esp.service.lifecycle.services.elasticsearch.SyncResourceService;
import nd.esp.service.lifecycle.services.lifecycle.v06.LifecycleServiceV06;
import nd.esp.service.lifecycle.services.packaging.v06.PackageService;
import nd.esp.service.lifecycle.support.Constant;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.support.busi.PackageUtil;
import nd.esp.service.lifecycle.support.busi.TransCodeUtil;
import nd.esp.service.lifecycle.support.enums.AdapterTaskResultStatus;
import nd.esp.service.lifecycle.support.enums.LifecycleStatus;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;
import nd.esp.service.lifecycle.vos.ListViewModel;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import com.nd.gaea.client.http.BearerAuthorizationProvider;
import com.nd.gaea.client.http.WafSecurityHttpClient;

@Service
public class AdapterDBDataServiceImpl implements AdapterDBDataService {
    private static final Logger LOG = LoggerFactory.getLogger(AdapterDBDataServiceImpl.class);

    @Autowired
    private NDResourceService ndResourceService;
    @Autowired
    @Qualifier("lifecycleServiceV06")
    private LifecycleServiceV06 lifecycleService;
    @Autowired
    private CommonServiceHelper commonServiceHelper;
    @Autowired
    @Qualifier("PackageServiceImpl")
    private PackageService packageService;
    @Autowired
    private TransCodeUtil transCodeUtil;
    @Autowired
    private AssetRepository assetRepository;
    @Autowired
    private ResourcePreviewsRepository resourcePreviewsRepository;
    @Autowired
    private TaskStatusInfoRepository taskRepository;
    @Autowired
    private TechInfoRepository techInfoRepository;
    
    @Autowired
	@Qualifier("CategoryServiceImpl")
	private CategoryService categoryService;
    
    @Autowired
    private ResourceCategoryRepository resourceCategoryRepository;
    
	@Autowired
	private SyncResourceService syncResourceService;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    
    public Map<String,Integer> adapterInstructionalobjectives(){
        Map<String,Integer> returnMap = new HashMap<String,Integer>();
        int seccess = 0;
        
        // 1. 获取单个汉字知识点
        String querySql = "SELECT identifier,title FROM `ndresource` WHERE primary_category='knowledges' AND LENGTH(title)=3 AND title NOT REGEXP '[A-Za-z]'";
        final List<Map<String,String>> knowledgeList = new ArrayList<Map<String,String>>();
        jdbcTemplate.query(querySql, new RowMapper<String>() {

            @Override
            public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                Map<String,String> rowMap = new HashMap<String,String>();
                rowMap.put("identifier", rs.getString("identifier"));
                rowMap.put("title", rs.getString("title"));
                knowledgeList.add(rowMap);
                return null;
            }

        });
        
        for(Map<String,String> knowledge : knowledgeList) {
            // 2.1 获取数据库中objective
            final List<Map<String,String>> objectivesList = new ArrayList<Map<String,String>>();
            querySql = "SELECT rr.source_uuid as 'kid', rr.identifier as 'rid', nd.identifier as 'oid', nd.title from resource_relations rr,ndresource nd where rr.res_type='knowledges' and rr.resource_target_type='instructionalobjectives' and rr.target = nd.identifier and nd.primary_category='instructionalobjectives' and rr.source_uuid in (SELECT identifier from ndresource where title like '" + knowledge.get("title") + "（%')";
            jdbcTemplate.query(querySql, new RowMapper<String>() {
                @Override
                public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                    Map<String,String> rowMap = new HashMap<String,String>();
                    rowMap.put("kid", rs.getString("kid"));
                    rowMap.put("rid", rs.getString("rid"));
                    rowMap.put("oid", rs.getString("oid"));
                    rowMap.put("title", rs.getString("title"));
                    objectivesList.add(rowMap);
                    return null;
                }

            });
            
            if(!objectivesList.isEmpty()) {
                for(Map<String,String> object : objectivesList) {
                    String fullTitle  = object.get("title");
                    String word = fullTitle.substring(0, fullTitle.indexOf("（"));
                    String pinyin = fullTitle.substring(fullTitle.indexOf("（")+1, fullTitle.length()-1);
                    String sql4Update = "UPDATE ndresource SET title='" + word + "', description='"  + pinyin + "' WHERE identifier='" + object.get("oid") + "'";
                    int count = jdbcTemplate.update(sql4Update);
                    LOG.info("更新了教学目标： "+object.get("oid"));
                    sql4Update = "UPDATE resource_relations SET source_uuid='" + knowledge.get("identifier") + "' WHERE identifier='" + object.get("rid") + "'";
                    count = jdbcTemplate.update(sql4Update);
                    LOG.info("更新了知识点关系： "+object.get("rid"));
                    sql4Update = "UPDATE resource_relations SET enable=0 WHERE target IN ( SELECT identifier FROM ndresource WHERE primary_category='instructionalobjectives' AND title='" + word + "' AND identifier!='" + object.get("oid") + "' AND description IN ('', '" + pinyin + "')  )";
                    count = jdbcTemplate.update(sql4Update);
                    LOG.info("删除了知识点关系： title="+object.get("title"));
                    sql4Update = "UPDATE ndresource  SET enable=0 WHERE primary_category='instructionalobjectives' AND title='" + word + "' AND identifier!='" + object.get("oid") + "' AND description IN ('', '" + pinyin + "')";
                    count = jdbcTemplate.update(sql4Update);
                    LOG.info("删除了教学目标： title="+object.get("title"));
                    sql4Update = "UPDATE ndresource  SET enable=0 WHERE primary_category='knowledges' AND identifier='" + object.get("kid") + "'";
                    count = jdbcTemplate.update(sql4Update);
                    LOG.info("删除了知识点： id="+object.get("kid"));
                }
                ++seccess;
            }
            
        }
        
        returnMap.put("seccess", seccess);
        return returnMap;
    }
    
    
    /**
     * 习题preview修复
     * 
     * @author:ql
     * @date:2015年12月23日
     * @return
     */
    @Override
    public Map<String, Long> fixResourcePreview(String resType) {
        // 获取资源对应仓储
        EspRepository espRepository = ServicesManager.get(resType);
        
        List<Item<? extends Object>> items = new ArrayList<>();
        Item<String> item = new Item<String>();
        item.setKey("resType");
        item.setComparsionOperator(ComparsionOperator.EQ);
        item.setLogicalOperator(LogicalOperator.AND);
        item.setValue(ValueUtils.newValue(resType));
        items.add(item);
        
        Page<ResourcePreviews> entityPage = null;
        int page = 0;
        int rows = 500;
        Pageable pageable = null;
        long FIX_TOTAL = 0;
        long FIX_SUCCESS = 0;

        do {
            // 分页查询 006e02fd-720a-43c3-b014-7ee5a8ace7f7
            pageable = new PageRequest(page, rows);
            try {
                entityPage = resourcePreviewsRepository.findByItems(items, pageable);
            } catch (EspStoreException e) {
                LOG.error("查询preview数据表失败：",e);
            }
            if(null != entityPage) {
                FIX_TOTAL = entityPage.getTotalElements();
    
                List<ResourcePreviews> entityList = entityPage.getContent();
                List<EspEntity> entityList4Update = new ArrayList<EspEntity>();
    
                for (ResourcePreviews entity : entityList) {
                    String id = entity.getIdentifier();
                    Education resource = null;
                    try {
                        resource = (Education)espRepository.get(id);
                    } catch (EspStoreException e) {
                        LOG.error("获取资源元数据失败：",e);
                    }
                    if(null == resource) {
                        continue;
                    }
                    
                    Map<String,String> previewMap = entity.getPreview();
                    if(CollectionUtils.isNotEmpty(previewMap)) {
                        resource.getPreview().putAll(previewMap);
                        entityList4Update.add(resource);
                        ++FIX_SUCCESS;
                    } else {
                        String  qtiSmall = entity.getQuestionSmall();
                        String  qtiBig = entity.getQuestionBig();
                        if(StringUtils.isNotEmpty(qtiSmall) && StringUtils.isNotEmpty(qtiBig)) {
                            resource.getPreview().put("question_small", qtiSmall);
                            resource.getPreview().put("question_big", qtiBig);
                            entityList4Update.add(resource);
                            ++FIX_SUCCESS;
                        }
                    }
                }
    
                // 批量更新
                if (CollectionUtils.isNotEmpty(entityList4Update)) {
                    try {
                        espRepository.batchAdd(entityList4Update);
                    } catch (EspStoreException e) {
                        LOG.error("更新资源preview数据失败：",e);
                    }
                }
            }
        } while (null!=entityPage && ++page<entityPage.getTotalPages());
        
        Map<String,Long> resultMap = new HashMap<String,Long>();
        resultMap.put("preview数据表数据总数", FIX_TOTAL);
        resultMap.put("此次成功适配的记录数", FIX_SUCCESS);
        return resultMap;
    }
    
    @Override
    public void triggerUpdatedResourcePack(String resType, String sql, boolean bLowPriority)  {

        AdapterTaskResult adapterTaskResult=new AdapterTaskResult();

        adapterTaskResult.setStatus(AdapterTaskResultStatus.RUNNING.getStatus());
        Constant.ADAPTER_TASK_RESULT.put(resType,adapterTaskResult);

        EspRepository<?> resourceRepository = ServicesManager.get(resType);


        List<String> listId =null;
        PackagingParam packagingParam =new PackagingParam(0);
        packagingParam.setResType(resType);
        Query query = resourceRepository.getEntityManager().createNativeQuery(sql);
        //adapterTaskResult.setTotalCount(query.getHints().size());
        listId = query.getResultList();
        if(CollectionUtils.isNotEmpty(listId)){
            List<String>uuids =new ArrayList<>();//用来查询资源任务完成情况
            for(String uuid:listId){
                packagingParam.setUuid(uuid);
                uuids.add(uuid);
                LOG.info("触发历史打包的UUID:"+uuid);
                String includes[]={"TI"};
                try {
                    ResourceModel resourceModel=ndResourceService.getDetail(resType,uuid, Arrays.asList(includes));
                    packagingParam.setPath(PackageUtil.getResourcePath(resourceModel));
                    int priority = Integer.parseInt(Constant.PACKAGING_PRIORITY);
                    if(bLowPriority && priority>0) {
                        --priority;
                    }
                    packagingParam.setPriority(priority);
                    packageService.triggerPackaging(packagingParam);
                    adapterTaskResult.increaseSuccessCount();
                }catch (Exception e){
                    adapterTaskResult.increaseFailCount();

                }

            }

        }
        LOG.info("{}资源触发打包完成,统计结果-->\n{}",resType, ObjectUtils.toJson(Constant.ADAPTER_TASK_RESULT.get(resType)));

    }


    @Override
    public void triggerResourcePack(String resType,String identifiers,String limit)  {



        EspRepository<?> resourceRepository = ServicesManager.get(resType);


        AdapterTaskResult adapterTaskResult=new AdapterTaskResult();

        adapterTaskResult.setStatus(AdapterTaskResultStatus.RUNNING.getStatus());
        Constant.ADAPTER_TASK_RESULT.put(resType,adapterTaskResult);

        StringBuffer sql =new StringBuffer("SELECT n.identifier FROM `ndresource` n, task_status_infos t \n" +
                "where t.uuid=n.identifier and t.buss_type='packaging' and unix_timestamp(t.update_time)*1000<n.last_update \n" +
                "and n.primary_category in ('questions','coursewareobjects') and n.enable=1");

        if(StringUtils.hasText(identifiers)){

            sql.append(" and a.identifier in ('").append(identifiers).append("')");
        }
      /*  if(StringUtils.hasText(limit)){

            sql.append("  limit ").append(limit);
        }else {
            sql.append( " limit 0 ,100 ");
        }*/
        sql.append("  limit 0,").append(limit);


        List<String> listId =null;
        PackagingParam packagingParam =new PackagingParam(0);
        packagingParam.setResType(resType);
        while(Constant.ADAPTER_TASK_CHARGE){
            Query query = resourceRepository.getEntityManager().createNativeQuery(sql.toString());
            listId = query.getResultList();
            if(CollectionUtils.isNotEmpty(listId)){
                List<String>uuids =new ArrayList<>();//用来查询资源任务完成情况
                for(String uuid:listId){
                    packagingParam.setUuid(uuid);
                    uuids.add(uuid);
                    LOG.info("触发历史打包的UUID:"+uuid);
                    String includes[]={"TI"};
                    try {
                        ResourceModel resourceModel=ndResourceService.getDetail(resType,uuid, Arrays.asList(includes));
                        packagingParam.setPath(PackageUtil.getResourcePath(resourceModel));
                        packageService.triggerPackaging(packagingParam);
                        adapterTaskResult.increaseSuccessCount();
                    }catch (Exception e){
                        adapterTaskResult.increaseFailCount();

                    }

                }
                if(CollectionUtils.isNotEmpty(uuids)) {
                    //查询50个状态
                    boolean queryFlag = true;
                    while (queryFlag) {
                        String queryTaskStatus = " select status  from task_status_infos c where buss_type='packaging' and uuid in ";

                        StringBuffer ids=new StringBuffer();
                        for(String uuid:uuids){
                            ids.append("'").append(uuid).append("'").append(",");
                        }
                        ids.deleteCharAt(ids.length()-1);//去掉最后一个多余的逗号

                        queryTaskStatus+="("+ids.toString()+")";

                        Query taskQuery = taskRepository.getEntityManager().createNativeQuery(queryTaskStatus);
                        List<String>statusList = taskQuery.getResultList();
                        boolean continueTaskQuery=false;
                        for(String status:statusList){
                            if("ready".equals(status)||"error".equals(status)){
                                continue;

                            }else {
                                continueTaskQuery=true;
                                break;
                            }
                        }
                        //
                        int i=0;
                        queryFlag=continueTaskQuery;
                        if(queryFlag){
                            try {
                                Thread.sleep(2000);
                                LOG.info("睡眠{}次",i++);
                                if(i>600){
                                    LOG.info("强制退出:"+queryTaskStatus);
                                    queryFlag=false;
                                }
                            }catch (Exception e){

                            }
                        }

                    }

                }

            }else {
                Constant.ADAPTER_TASK_CHARGE=false;
                adapterTaskResult.setStatus(AdapterTaskResultStatus.FINISH.getStatus());
                Constant.ADAPTER_TASK_RESULT.put(resType,adapterTaskResult);

            }

        }
        LOG.info("{}预览图修复完成,统计结果-->\n{}",resType, ObjectUtils.toJson(Constant.ADAPTER_TASK_RESULT.get(resType)));

    }

    @Override
    public Map<String, String> triggerVideoTranscode(int totCount, Set<String> statusSet, boolean bOnlyOgv) {
        Map<String,String>result =new HashMap<>();
        
        try {
            List<String> listIds = this.getVideoListToTrans(statusSet);
            result = triggerTranscodeByIds("assets", listIds, bOnlyOgv);
        } catch (Exception e) {
            LOG.error("执行转码调度失败了:" + e.getMessage(), e);
        }

        return result;
    }
    
    @Override
    public Map<String,String> triggerTranscodeByIds(String resType, List<String> listIds, boolean bOnlyOgv) {
        int successCount = 0;
        int failCount = 0;
       
        List<String> failIds = new ArrayList<String>();
        try {
            if (CollectionUtils.isNotEmpty(listIds)) {
                for (String id : listIds) {
                    try {
                        ResourceModel cm = ndResourceService.getDetail(resType, id,
                                IncludesConstant.getValidIncludes(IncludesConstant.INCLUDE_TI+","+IncludesConstant.INCLUDE_LC
                                +","+IncludesConstant.INCLUDE_CG));
                    
                        MDC.put("resource", id);
                        MDC.put("res_type", resType);
                        MDC.put("operation_type", "转码");
                        MDC.put("remark", "历史视频触发");
                        
                        String statusBackup = null;
                        if(!(cm.getLifeCycle().getStatus()!=null && cm.getLifeCycle().getStatus().contains("TRANSCOD"))) {
                            statusBackup = cm.getLifeCycle().getStatus();
                        }

                        ResContributeModel contributeModel = new ResContributeModel();
                        contributeModel.setTargetId("777");
                        contributeModel.setTargetName("LCMS");
                        contributeModel.setTargetType("USER");
                        contributeModel.setMessage("历史视频触发转码");
                        contributeModel.setLifecycleStatus(TransCodeUtil.getTransIngStatus(true));
                        contributeModel.setProcess(0.0f);
                        lifecycleService.addLifecycleStep(resType, cm.getIdentifier(), contributeModel, false);
                        transCodeUtil.triggerTransCode(cm, resType, statusBackup, bOnlyOgv);

                        LOG.info("触发转码的UUID:{}",cm.getIdentifier());
                        successCount++;
                        
                    }catch (Exception e){
                        LOG.info("无法触发转码的UUID:{}", id);
                        failIds.add(id);
                        failCount++;
                    }
                    MDC.clear();

                }
            }

        } catch (Exception e) {
            LOG.error("执行转码调度失败了:" + e.getMessage(), e);
        }
        
        Map<String,String>result =new HashMap<>();
        result.put("成功个数:",String.valueOf(successCount));
        result.put("失败个数:",String.valueOf(failCount));
        result.put("失败id:",ObjectUtils.toJson(failIds));
        return result;
    }
    
    private List<String> getVideoListToTrans(Set<String> statusSet) {
        String statusList = "('" + StringUtils.join(statusSet, "','") + "')";
        String sql = "SELECT a.identifier FROM ndresource a WHERE a.primary_category='assets' AND a.enable=1 AND estatus IN "
                + statusList + " AND EXISTS (SELECT rc.identifier FROM resource_categories rc WHERE a.identifier=rc.resource AND (rc.taxOnCode LIKE '$F03%')) ORDER BY a.create_time DESC";
        Query query = assetRepository.getEntityManager().createNativeQuery(sql);
        List<String> listId = query.getResultList();
        return listId;
    }
    
    @Override
    public Map<String, Integer> triggerResourceTranscode(String resType,int perCount,
            int totCount) {


       /* TriggerTransCodeThread triggerTransCodeThread= new TriggerTransCodeThread(resType,status,perCount);
        Thread thread=new Thread(triggerTransCodeThread);

        thread.start();*/
        int successCount=0;
        int failCount=0;
        //EspRepository repository=   ServicesManager.get(resType);
        ResourceRepository<? extends EspEntity> repository = commonServiceHelper.getRepository(resType);


        boolean running=true;
            List<Item<? extends Object>> items = buildStatusQueryParam();
            while(running) {
                Page resourcePage = null;
                Pageable pageable = null;
                //int page = 0;
                do {
                    pageable = new PageRequest(0, perCount, Direction.DESC, "dbcreateTime");
                    try {
                        resourcePage = repository.findByItems(items, pageable);
                        if(null!=resourcePage) {
                            List<Education> resources = resourcePage.getContent();
                            List<String> identifierList = new ArrayList<>();
                            //int realTranscodeCn=0;
                            if (CollectionUtils.isNotEmpty(resources)) {
                                //realTranscodeCn=0;
                                for (Education education : resources) {
                                    identifierList.add(education.getIdentifier());
                                    ResourceModel cm = ndResourceService.getDetail(resType, education.getIdentifier(), IncludesConstant.getValidIncludes(IncludesConstant.INCLUDE_TI+","+IncludesConstant.INCLUDE_LC));
                                    try {
                                        MDC.put("resource", education.getIdentifier());
                                        MDC.put("res_type", resType);
                                        MDC.put("operation_type", "转码");
                                        MDC.put("remark", "历史视频触发");
    
                                        ResourceViewModel vm = CommonHelper.convertViewModelOut(cm,ResourceViewModel.class,"assets_type");
    //                                    if(TransCodeManager.canTransCode(vm, resType)) {
                                        ResContributeModel contributeModel = new ResContributeModel();
                                        contributeModel.setTargetId("777");
                                        contributeModel.setTargetName("LCMS");
                                        contributeModel.setTargetType("USER");
                                        contributeModel.setMessage("历史视频触发转码");
                                        contributeModel.setLifecycleStatus(TransCodeUtil.getTransIngStatus(true));
                                        contributeModel.setProcess(0.0f);
                                        lifecycleService.addLifecycleStep(resType, cm.getIdentifier(), contributeModel, false);
                                        transCodeUtil.triggerTransCode(cm, resType);
    
                                        LOG.info("触发转码的UUID:{}",cm.getIdentifier());
                                        successCount++;
                                                //realTranscodeCn++;
    //                                    } else {
    //                                        LOG.info("无法触发转码的UUID:{}", cm.getIdentifier());
    //                                        lifecycleService.addLifecycleStep(resType, cm.getIdentifier(), false, "历史视频触发转码：无法转码");
    //                                        failCount++;
    //                                    }
                                    }catch (Exception e){
                                        LOG.info("无法触发转码的UUID:{}", cm.getIdentifier());
                                        failCount++;
                                    }
                                    MDC.clear();
    
                                }
                                //查询转码完成情况
    //                            boolean toSleep = true;
    //                            while (toSleep) {
    //                                int initSleepTime = 30*realTranscodeCn ;//休眠450s
    //                                LOG.info("初次睡眠时间:{},满足条件的记录数:{}",initSleepTime,realTranscodeCn);
    //                                int totalWaitTime = 0;//todo 确认到底等待了多久
    //                                toSleep = queryTranscodeResult(initSleepTime, repository, pageable, 0.8F,identifierList);
    //                            }
                            } else {//如果查询不到,就全局退出
                                running = false;
                            }
                            
                            if(successCount>=totCount) {
                                running = false;
                                break;
                            }
                        }
    
                    } catch (Exception e) {
                        LOG.error("执行转码调度失败了:" + e.getMessage(), e);
                    }

                } while (resourcePage!=null && resourcePage.getTotalPages() > 0);

            }

        Map<String,Integer>result =new HashMap<>();
        result.put("成功个数:",successCount);
        result.put("失败个数:",failCount);
        return result;
    }
    
    /**
     * 构建状态查询参数
     * @return
     */
    private List<Item<? extends Object>>  buildStatusQueryParam(){

        Item<String> status_item = new Item<String>();
        status_item.setKey("status");
        status_item.setComparsionOperator(ComparsionOperator.EQ);
        status_item.setLogicalOperator(LogicalOperator.AND);
        status_item.setValue(ValueUtils.newValue(LifecycleStatus.TRANSCODE_WAITING.getCode()));
        
        Item<Integer> enable_item = new Item<Integer>();
        enable_item.setKey("enable");
        enable_item.setComparsionOperator(ComparsionOperator.EQ);
        enable_item.setLogicalOperator(LogicalOperator.AND);
        enable_item.setValue(ValueUtils.newValue(1));
        List<Item<? extends Object>> items = new ArrayList<>();
        
//        Item<String> cate_item = new Item<String>();
//        cate_item.setKey("dbcategories");
//        cate_item.setComparsionOperator(ComparsionOperator.LIKE);
//        cate_item.setLogicalOperator(LogicalOperator.AND);
//        cate_item.setValue(ValueUtils.newValue("%"+ndcode+"%"));
        
//        Item<String> preview_item = new Item<String>();
//        preview_item.setKey("dbpreview");
//        preview_item.setComparsionOperator(ComparsionOperator.NOT_LIKE);
//        preview_item.setLogicalOperator(LogicalOperator.AND);
//        preview_item.setValue(ValueUtils.newValue("%cover%"));
        
        items.add(enable_item);
        items.add(status_item);
//        items.add(cate_item);
//        items.add(preview_item);
        return items;
    }

    public Map<String,Integer> adapter3DResource(){
    	int num = 0;
    	Map<String,Integer> returnMap = new HashMap<String, Integer>();
    	//初始化3D资源关键字规则
    	Map<String,Map<String,Object>> initMap = init3D();
    	
    	//初始化所有3D半成品的维度数据
    	ListViewModel<CategoryDataModel> modelListResult = null;
    	try {
			modelListResult = categoryService.queryCategoryData("UK", true, null,"", "(0,10)");
		} catch (EspStoreException e) {
			e.printStackTrace();
			LOG.error("修复3D半成品资源数据出错了",e);
		}
    	
    	if(modelListResult == null){
    		return null;
    	}
    	
    	List<CategoryDataModel> categoryDataList = modelListResult.getItems();
    	if(CollectionUtils.isEmpty(categoryDataList)){
    		return null;
    	}
    	
    	Map<String,CategoryDataModel> map = new HashMap<String,CategoryDataModel>();
    	for (CategoryDataModel categoryDataModel : categoryDataList) {
			map.put(categoryDataModel.getNdCode(), categoryDataModel);
		}
    	
    	
    	//加载3D半成品资源数据
    	Set<String> keySet = initMap.keySet();
    	for (String key : keySet) {
    		Map<String,Object> categoryMap = initMap.get(key);
    		List<String> rjSubList = (List)categoryMap.get("subList");
    		String defaultRc = (String)categoryMap.get("default");
    		int offSet = 0;
    		int pageNum = 200;
    		while(true){
    			if(offSet > 50000){
    				break;
    			}
    			String limit = offSet + "," + pageNum;
    			String sql = "select distinct(nd.identifier),nd.title from ndresource nd,resource_categories rc where nd.primary_category='assets' and nd.enable = 1 and rc.primary_category='assets' and nd.identifier = rc.resource and rc.taxOnCode = '"+key+"' limit " + limit;
    			List<Map<String,Object>> queryResult = jdbcTemplate.queryForList(sql);
    			if(CollectionUtils.isEmpty(queryResult)){
    				break;
    			}
    			List<ResourceCategory> rcAllList = new ArrayList<ResourceCategory>();
    			Set<Resource> resList = new HashSet<Resource>();
    			for (Map<String, Object> map2 : queryResult) {
					String identifier = (String)map2.get("identifier");
					String title = (String)map2.get("title");
					ResourceCategory tmp = new ResourceCategory();
					tmp.setPrimaryCategory("assets");
					tmp.setResource(identifier);
					List<ResourceCategory> rcList = null;
					try {
						rcList = resourceCategoryRepository.getAllByExample(tmp);
					} catch (EspStoreException e) {
						e.printStackTrace();
						LOG.error("修复3D半成品资源数据出错了",e);
					}
					
					if(CollectionUtils.isEmpty(rcList)){
						continue;
					}
					
					//判断该3D半成品的三级维度数据要用哪个
					List<Map<String,Map<String,Integer>>> matchList = new ArrayList<Map<String,Map<String,Integer>>>();
					Set<String> keySet2 = categoryMap.keySet();
					for (String key2 : keySet2) {
						if(key2.startsWith("UK")){
							String[] keywords = (String[])categoryMap.get(key2);
							Map<String,Map<String,Integer>> mm = new HashMap<String, Map<String,Integer>>();
							for (String kw : keywords) {
								if(title.contains(kw)){
									Map<String,Integer> im = new HashMap<String, Integer>();
									im.put("matchlength", kw.length());
									if(mm.containsKey(key2)){
										Map<String,Integer> tm = mm.get(key2);
										if(tm.get("matchlength").intValue() < kw.length()){
											mm.put(key2, im);
										}
									}else{
										mm.put(key2, im);
									}
									matchList.add(mm);
								}
							}
						}
					}
					
					String matchCategoryData = defaultRc;
					if(CollectionUtils.isNotEmpty(matchList)){
						int maxMatch = 0;
						for (Map<String,Map<String,Integer>> mm2 : matchList) {
							//查出匹配度最高的维度数
							Set<String> ss = mm2.keySet();
							if(CollectionUtils.isNotEmpty(ss)){
								for (String s : ss) {
									Map<String,Integer> tmm2 = mm2.get(s);
									if(tmm2.get("matchlength").intValue() > maxMatch){
										matchCategoryData = s;
										maxMatch = tmm2.get("matchlength").intValue();
									}
								}
							}
						}
					}
					
					boolean flag = false;
					for (ResourceCategory resourceCategory : rcList) {
						//判断是否已存在正确的第三级维度数据
//						if(resourceCategory.getTaxoncode().equals(matchCategoryData)){
//							flag = true;
//							break;
//						}
						
						//判断是否已存在三级维度数据
						for (String rj : rjSubList) {
							if(rj.equals(resourceCategory.getTaxoncode())){
								flag = true;
								break;
							}
						}
						if(flag){
							break;
						}
					}
					if(!flag){
						CategoryDataModel cdm = map.get(matchCategoryData);
						//新增一条维度数据
						ResourceCategory rrc = new ResourceCategory();
                        // 通过取维度数据详情，补全resource_category中间表的数据
						rrc.setShortName(cdm.getShortName());
						rrc.setTaxoncodeid(cdm.getIdentifier());
						rrc.setTaxoncode(matchCategoryData);
						rrc.setTaxonname(cdm.getTitle());
						rrc.setCategoryCode("UK");
                        // 取维度的shortName
						rrc.setCategoryName("resources_category");
						rrc.setResource(identifier);
						rrc.setIdentifier(UUID.randomUUID().toString());
                        //资源分类维度
						rrc.setPrimaryCategory("assets");
						rcAllList.add(rrc);
						
						//用于同步ES
						Resource res = new Resource("assets", identifier);
						resList.add(res);
					}
				}
    			
    			//新增数据
    			if(CollectionUtils.isNotEmpty(rcAllList)){
    				try {
						resourceCategoryRepository.batchAdd(rcAllList);
						num += rcAllList.size();
					} catch (EspStoreException e) {
						e.printStackTrace();
						LOG.error("修复3D半成品资源数据出错了",e);
					}
    			}
    			
    			if(CollectionUtils.isNotEmpty(resList)){
    				syncResourceService.syncBatchAdd(resList);
    			}
    			offSet = offSet + pageNum;
    		}
		}
    	returnMap.put("匹配数量:", num);
    	return returnMap;
    }

    /**
     * 初始化3D半成品的资源数据
     * @return
     */
    private Map<String,Map<String,Object>> init3D(){
    	Map<String,Map<String,Object>> returnMap = new HashMap<String, Map<String,Object>>();
    	
    	//人物和解剖
    	Map<String,Object> rjMap = new HashMap<String, Object>();
    	List<String> rjSubList = new ArrayList<String>();
    	//人类
    	rjSubList.add("UK005000100010001");
    	//解剖
    	rjSubList.add("UK005000100010002");
    	//魔幻和科幻
    	rjSubList.add("UK005000100010003");
    	//其他
    	rjSubList.add("UK005000100010004");
    	
    	rjMap.put("subList", rjSubList);
    	rjMap.put("default","UK005000100010004");
    	rjMap.put("UK005000100010001", new String[]{"人","中年","青年","老年","少女","男","女","教师","观众","行人","伙计","小第","侠士","官员","眺望","静坐","站立","坐下"});
    	rjMap.put("UK005000100010003", new String[]{"怪物","僵尸","精卫","神兽","麒麟","白虎","白狐","灵龟","凤凰","鼓仙","九色鹿","神","妖","幻","法师","游侠"});
    	returnMap.put("UK005000100010000", rjMap);
    	
    	//=======================================================================================================================================================
    	//动物
    	Map<String,Object> dwMap = new HashMap<String, Object>();
    	List<String> dwSubList = new ArrayList<String>();
    	//哺乳动物
    	dwSubList.add("UK005000100020001");
    	//昆虫
    	dwSubList.add("UK005000100020002");
    	//爬行动物
    	dwSubList.add("UK005000100020003");
    	//鱼
    	dwSubList.add("UK005000100020004");
    	//鸟
    	dwSubList.add("UK005000100020005");
    	//其他
    	dwSubList.add("UK005000100020006");
    	
    	dwMap.put("subList", dwSubList);
    	dwMap.put("default","UK005000100020006");
    	dwMap.put("UK005000100020001", new String[]{"海豹","海豚","狗","猪","熊","狼","河马","大象"});
    	dwMap.put("UK005000100020002", new String[]{"虫","蜘蛛","蚂蚁","蝎子"});
    	dwMap.put("UK005000100020003", new String[]{"海龟","蛇"});
    	dwMap.put("UK005000100020004", new String[]{"鱼"});
    	dwMap.put("UK005000100020005", new String[]{"鸟","鸡","鸭"});
    	returnMap.put("UK005000100020000", dwMap);
    	
    	//=======================================================================================================================================================
    	//植物
    	Map<String,Object> zwMap = new HashMap<String, Object>();
    	List<String> zwSubList = new ArrayList<String>();
    	//树
    	zwSubList.add("UK005000100030001");
    	//草
    	zwSubList.add("UK005000100030002");
    	//花卉
    	zwSubList.add("UK005000100030003");
    	//其它
    	zwSubList.add("UK005000100030004");
    	
    	zwMap.put("subList", zwSubList);
    	zwMap.put("default","UK005000100030004");
    	zwMap.put("UK005000100030001", new String[]{"树"});
    	zwMap.put("UK005000100030002", new String[]{"草"});
    	zwMap.put("UK005000100030003", new String[]{"花"});
    	returnMap.put("UK005000100030000", zwMap);
    	
    	//=======================================================================================================================================================
    	//建筑
    	Map<String,Object> jzMap = new HashMap<String, Object>();
    	List<String> jzSubList = new ArrayList<String>();
    	//室内
    	jzSubList.add("UK005000100040001");
    	//室外
    	jzSubList.add("UK005000100040002");
    	//其它
    	jzSubList.add("UK005000100040003");
    	
    	jzMap.put("subList", jzSubList);
    	jzMap.put("default","UK005000100040003");
    	jzMap.put("UK005000100040001", new String[]{"门","围墙","窖","窗"});
    	jzMap.put("UK005000100040002", new String[]{"桥","坛","塔","台","广场"});
    	returnMap.put("UK005000100040000", jzMap);
    	
    	//=======================================================================================================================================================
    	//交通工具
    	Map<String,Object> jtMap = new HashMap<String, Object>();
    	List<String> jtSubList = new ArrayList<String>();
    	//海上
    	jtSubList.add("UK005000100060001");
    	//陆地
    	jtSubList.add("UK005000100060002");
    	//天空
    	jtSubList.add("UK005000100060003");
    	//太空
    	jtSubList.add("UK005000100060004");
    	//其他
    	jtSubList.add("UK005000100060005");

    	
    	jtMap.put("subList", jtSubList);
    	jtMap.put("default","UK005000100060005");
    	jtMap.put("UK005000100060001", new String[]{"船","舰"});
    	jtMap.put("UK005000100060002", new String[]{"车"});
    	jtMap.put("UK005000100060003", new String[]{"飞机","战机"});
    	jtMap.put("UK005000100060004", new String[]{"飞船"});
    	returnMap.put("UK005000100060000", jtMap);
    	
    	//=======================================================================================================================================================
    	//器械
    	Map<String,Object> qxMap = new HashMap<String, Object>();
    	List<String> qxSubList = new ArrayList<String>();
    	//武器
    	qxSubList.add("UK005000100070001");
    	//实验
    	qxSubList.add("UK005000100070002");
    	//生活
    	qxSubList.add("UK005000100070003");
    	//医疗
    	qxSubList.add("UK005000100070004");
    	//其他
    	qxSubList.add("UK005000100070005");

    	
    	qxMap.put("subList", qxSubList);
    	qxMap.put("default","UK005000100070005");
    	qxMap.put("UK005000100070001", new String[]{"刀","枪","坦克","炮","武器","弩","棍"});
    	qxMap.put("UK005000100070002", new String[]{"实验","台"});
    	qxMap.put("UK005000100070003", new String[]{"健身"});
    	qxMap.put("UK005000100070004", new String[]{"医疗","手术"});
    	returnMap.put("UK005000100070000", qxMap);
    	
    	
    	//=======================================================================================================================================================
    	//地形地貌
    	Map<String,Object> dxMap = new HashMap<String, Object>();
    	List<String> dxSubList = new ArrayList<String>();
    	//山川
    	dxSubList.add("UK005000100090001");
    	//河流
    	dxSubList.add("UK005000100090002");
    	//湖泊
    	dxSubList.add("UK005000100090003");
    	//海洋
    	dxSubList.add("UK005000100090004");
    	//其他
    	dxSubList.add("UK005000100090005");

    	
    	dxMap.put("subList", dxSubList);
    	dxMap.put("default","UK005000100090005");
    	dxMap.put("UK005000100090001", new String[]{"山","地"});
    	dxMap.put("UK005000100090002", new String[]{"河"});
    	dxMap.put("UK005000100090003", new String[]{"湖"});
    	dxMap.put("UK005000100090004", new String[]{"海"});
    	returnMap.put("UK005000100090000", dxMap);	
    	
    	return returnMap;
    }

	@Override
	public void adapterDJGResource4Lc(){
		WafSecurityHttpClient wafSecurityHttpClient = new WafSecurityHttpClient();
		wafSecurityHttpClient.setBearerAuthorizationProvider(new BearerAuthorizationProvider() {
			
			@Override
			public String getUserid() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public String getAuthorization() {
				return "NDR_MAC";
			}
		});
		
//		String lcDomain = "http://esp-lifecycle.pre1.web.nd/v0.6";
		String lcDomain = "http://esp-lifecycle.web.sdp.101.com/v0.6";
		
		//1.查询语句
		String queryUrl = lcDomain + "/assets/management/actions/query?";
		queryUrl += "words&coverage=Org/nd/&category=$RA0101,$RA0102,$RA0103,$RA0104";
		queryUrl += "&prop=provider eq 中央电教馆&prop=provider eq 中央电化教育馆";
		queryUrl += "&include=TI,CG,LC,EDU,CR";
		queryUrl += "&prop=create_time lt 2016-01-01 00:00:00";
		
		//分页参数
		int offset = 0;
		int pageSize = 500;
		// 计数
		int count = 0;
		while (AdapterDBDataController.REPAIR_SWITCH_1) {
			queryUrl += "&limit=(" + offset + "," + pageSize + ")";
			ListViewModel<Map<String, Object>> list = wafSecurityHttpClient.get(queryUrl, ListViewModel.class);
			if (list != null && CollectionUtils.isNotEmpty(list.getItems())) {
				System.out.println(count + ": limit=(" + offset + "," + pageSize + ")");

				// 循环修改
				List<Map<String, Object>> avmList = list.getItems();
				for (Map<String, Object> avm : avmList) {
					if(avm != null){
						if(avm.containsKey("life_cycle")){
							Map<String , Object> lc = (Map<String, Object>)avm.get("life_cycle");
							if(CollectionUtils.isNotEmpty(lc)){
								lc.put("creator", "20160617");
							}
						}
						if(avm.containsKey("copyright")){
							Map<String , Object> cr = (Map<String, Object>)avm.get("copyright");
							if(CollectionUtils.isNotEmpty(cr)){
								cr.put("author", "中央电教馆");
							}
						}
						
						String updateUrl = lcDomain + "/assets/" + avm.get("identifier");
						HttpHeaders httpHeaders = new HttpHeaders();
						httpHeaders.setContentType(MediaType.APPLICATION_JSON);
						HttpEntity<Map<String, Object>> entity = new HttpEntity<Map<String, Object>>(avm, httpHeaders);
						wafSecurityHttpClient.executeForObject(updateUrl, HttpMethod.PUT, entity, Map.class);
					}
				}
			} else {
				break;
			}

			// 循环参数处理
			count++;
			queryUrl = queryUrl.substring(0, queryUrl.lastIndexOf("&limit"));
			offset += pageSize;
		}
		
		System.out.println("循环次数:" + count);
	}


	@Override
	public void adapterDJGResource4Status() {
		WafSecurityHttpClient wafSecurityHttpClient = new WafSecurityHttpClient();
		wafSecurityHttpClient.setBearerAuthorizationProvider(new BearerAuthorizationProvider() {
			
			@Override
			public String getUserid() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public String getAuthorization() {
				return "NDR_MAC";
			}
		});
		
//		String lcDomain = "http://esp-lifecycle.pre1.web.nd/v0.6";
		String lcDomain = "http://esp-lifecycle.web.sdp.101.com/v0.6";
		
		//1.查询语句
		String queryUrl = lcDomain + "/assets/management/actions/query?";
		queryUrl += "words&coverage=Org/nd/&category=$RA0106";
		queryUrl += "&include=TI,CG,LC,EDU,CR";
		
		//分页参数
		int offset = 0;
		int pageSize = 500;
		// 计数
		int count = 0;
		while (AdapterDBDataController.REPAIR_SWITCH_2) {
			queryUrl += "&limit=(" + offset + "," + pageSize + ")";
			ListViewModel<Map<String, Object>> list = wafSecurityHttpClient.get(queryUrl, ListViewModel.class);
			if (list != null && CollectionUtils.isNotEmpty(list.getItems())) {
				System.out.println(count + ": limit=(" + offset + "," + pageSize + ")");

				// 循环修改
				List<Map<String, Object>> avmList = list.getItems();
				for (Map<String, Object> avm : avmList) {
					if(avm != null && avm.containsKey("categories")){
						Map<String , Object> categories = (Map<String, Object>)avm.get("categories");
						if(CollectionUtils.isNotEmpty(categories) && categories.containsKey("resources_category")){
							List<Map<String , Object>> resourcesCategory = (List<Map<String , Object>>)categories.get("resources_category");
							if(CollectionUtils.isNotEmpty(resourcesCategory)){
								Set<String> ukTags = new HashSet<String>();
								for(Map<String, Object> codeMap : resourcesCategory){
									if(CollectionUtils.isNotEmpty(codeMap) && codeMap.containsKey("taxonname")){
										ukTags.add((String)codeMap.get("taxonname"));
									}
								}
								
								if(CollectionUtils.isEmpty(ukTags)){
									continue;
								}
								
								//获取原先的tags
								List<String> oldTags = (List<String>)avm.get("tags");
								if(CollectionUtils.isNotEmpty(oldTags)){
									ukTags.addAll(oldTags);
								}
								avm.put("tags", ukTags);
								
								//更新
								String updateUrl = lcDomain + "/assets/" + avm.get("identifier");
								HttpHeaders httpHeaders = new HttpHeaders();
								httpHeaders.setContentType(MediaType.APPLICATION_JSON);
								HttpEntity<Map<String, Object>> entity = new HttpEntity<Map<String, Object>>(avm, httpHeaders);
								try {
									wafSecurityHttpClient.executeForObject(updateUrl, HttpMethod.PUT, entity, Map.class);
								} catch (Exception e) {
									LOG.error(avm.get("identifier") + "--更新出错", e.getMessage());
									LOG.error("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
									continue;
								}
							}
						}
					}
				}
			} else {
				break;
			}

			// 循环参数处理
			count++;
			queryUrl = queryUrl.substring(0, queryUrl.lastIndexOf("&limit"));
			offset += pageSize;
		}
		
		System.out.println("循环次数:" + count);
	}


	@Override
	public void repairProvider(String type,String pre,String now) {
		WafSecurityHttpClient wafSecurityHttpClient = new WafSecurityHttpClient();
		wafSecurityHttpClient.setBearerAuthorizationProvider(new BearerAuthorizationProvider() {
			
			@Override
			public String getUserid() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public String getAuthorization() {
				return "NDR_MAC";
			}
		});
		
//		String lcDomain = "http://esp-lifecycle.pre1.web.nd/v0.6";
		String lcDomain = "http://esp-lifecycle.web.sdp.101.com/v0.6";
		
		//1.查询语句
		String queryUrl = lcDomain + "/" + type + "/management/actions/query?";
		queryUrl += "words&coverage=Org/nd/";
		queryUrl += "&prop=provider eq " + pre;
		queryUrl += "&include=TI,CG,LC,EDU,CR";
		queryUrl += "&limit=(0,500)";
//		queryUrl += "&prop=identifier eq a0584ed1-5f15-42ea-ac14-cc3a411f4666";
		if(type.equals("coursewareobjects")){
			queryUrl += "&category=$RE04*";
		}
		
		// 计数
		int count = 0;
		while (AdapterDBDataController.REPAIR_SWITCH_2) {
			ListViewModel<Map<String, Object>> list = wafSecurityHttpClient.get(queryUrl, ListViewModel.class);
			if (list != null && CollectionUtils.isNotEmpty(list.getItems())) {
				// 循环修改
				List<Map<String, Object>> avmList = list.getItems();
				for (Map<String, Object> avm : avmList) {
					if(avm != null && avm.containsKey("life_cycle")){
						if(((String)avm.get("title")).length() >200){
							avm.put("title", ((String)avm.get("title")).substring(0, 200));
						}
						
						Map<String , Object> lc = (Map<String, Object>)avm.get("life_cycle");
						if(CollectionUtils.isNotEmpty(lc)){
							lc.put("provider", now);

							String updateUrl = lcDomain + "/" + type + "/" + avm.get("identifier");
							
							HttpHeaders httpHeaders = new HttpHeaders();
							httpHeaders.setContentType(MediaType.APPLICATION_JSON);
							HttpEntity<Map<String, Object>> entity = new HttpEntity<Map<String, Object>>(avm, httpHeaders);
							try {
								wafSecurityHttpClient.executeForObject(updateUrl, HttpMethod.PUT, entity, Map.class);
							} catch (Exception e) {
								LOG.error(type + "--" + avm.get("identifier") + "--更新出错", e.getMessage());
								LOG.error("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
								continue;
							}
						}
					}
				}
			} else {
				break;
			}

			// 循环参数处理
			count++;
		}
		
		System.out.println("循环次数:" + count);
	}
    
//	@Override
//	public void update3DResource(String session,long endTime) {
//		//查询SQL
//		StringBuilder querySql = new StringBuilder("");
//		querySql.append("SELECT ndr.identifier AS identifier,ndr.custom_properties AS custom_properties ");
//		querySql.append("FROM ndresource ndr ");
//		querySql.append("INNER JOIN resource_categories rc ON ndr.identifier=rc.resource ");
//		querySql.append("INNER JOIN res_coverages rcv ON ndr.identifier=rcv.resource ");
//		querySql.append("WHERE ndr.enable=1 AND ndr.primary_category='assets' ");
//		querySql.append("AND ndr.create_time <" + endTime);
//		querySql.append(" AND rc.taxOnCode='$RA0106' ");
//		querySql.append("AND (rcv.target_type='Org' AND rcv.target='nd') ");
//		
//		//分页参数
//		int offset = 0;
//		int pageSize = 500;
//		
//		final Map<String, String> resultMap = new HashMap<String, String>();
//		WafSecurityHttpClient wafSecurityHttpClient = new WafSecurityHttpClient();
//		while (true) {
//			String limit = "LIMIT " + offset + "," + pageSize;
//			querySql.append(limit);
//			jdbcTemplate.query(querySql.toString(), new RowMapper<Map<String,String>>(){
//				@Override
//				public Map<String,String> mapRow(ResultSet rs, int rowNum) throws SQLException {
//					resultMap.put(rs.getString("identifier"), rs.getString("custom_properties"));
//					
//					return null;
//				}
//			});
//			
//			//用于批量创建或修改的list
//			List<TechInfo> techInfos = new ArrayList<TechInfo>();
//			List<Asset> assets = new ArrayList<Asset>();
//			
//			if(CollectionUtils.isNotEmpty(resultMap)){
//				for(String id : resultMap.keySet()){
//					
//					// 获取资源
//					Asset asset = null;
//					try {
//						asset = assetRepository.get(id);
//					} catch (EspStoreException e) {
//						LOG.error("获取资源失败--" + id);
//					}
//					if (asset == null) {
//						continue;
//					}
//
//					// 获取对应的techInfo的key=href,目的是获取format
//					TechInfo ti4Href = new TechInfo();
//					ti4Href.setTitle("href");
//					ti4Href.setResource(id);
//					ti4Href.setResType("assets");
//					try {
//						ti4Href = techInfoRepository.getByExample(ti4Href);
//					} catch (EspStoreException e) {
//						LOG.error("获取TI失败--" + id);
//					}
//					
//					@SuppressWarnings("unchecked")
//					Map<String, Object> customPropertiesMap = 
//							ObjectUtils.fromJson(resultMap.get(id), Map.class);
//					
//					if(customPropertiesMap.containsKey("ios") ||
//					   customPropertiesMap.containsKey("package") ||
//					   customPropertiesMap.containsKey("android") ||
//					   customPropertiesMap.containsKey("web") ||
//					   customPropertiesMap.containsKey("pc")){//说明需要修复
//						
//					   List<String> pathList = new ArrayList<String>();
//					   String iosPath = "";
//					   if(customPropertiesMap.containsKey("ios")){
//						   String a = (String)customPropertiesMap.get("ios");
//						   if(StringUtils.isNotEmpty(a) && a.contains("/")){
//							   iosPath = ((String)customPropertiesMap.get("ios")).substring(((String)customPropertiesMap.get("ios")).indexOf("/"));
//							   pathList.add(iosPath);
//						   }
//						   
//						   //去掉这个key
//						   customPropertiesMap.remove("ios");
//					   }
//					   String packagePath = "";
//					   if(customPropertiesMap.containsKey("package")){
//						   String a = (String)customPropertiesMap.get("package");
//						   if(StringUtils.isNotEmpty(a) && a.contains("/")){
//							   packagePath = ((String)customPropertiesMap.get("package")).substring(((String)customPropertiesMap.get("package")).indexOf("/"));
//							   pathList.add(packagePath);
//						   }
//						   
//						   customPropertiesMap.remove("package");
//					   }
//					   String androidPath = "";
//					   if(customPropertiesMap.containsKey("android")){
//						   String a = (String)customPropertiesMap.get("android");
//						   if(StringUtils.isNotEmpty(a) && a.contains("/")){
//							   androidPath = ((String)customPropertiesMap.get("android")).substring(((String)customPropertiesMap.get("android")).indexOf("/"));
//							   pathList.add(androidPath);
//						   }
//						   
//						   customPropertiesMap.remove("android");
//					   }
//					   String webPath = "";
//					   if(customPropertiesMap.containsKey("web")){
//						   String a = (String)customPropertiesMap.get("web");
//						   if(StringUtils.isNotEmpty(a) && a.contains("/")){
//							   webPath = ((String)customPropertiesMap.get("web")).substring(((String)customPropertiesMap.get("web")).indexOf("/"));
//							   pathList.add(webPath);
//						   }
//						   
//						   customPropertiesMap.remove("web");
//					   }
//					   String pcPath = "";
//					   if(customPropertiesMap.containsKey("pc")){
//						   String a = (String)customPropertiesMap.get("pc");
//						   if(StringUtils.isNotEmpty(a) && a.contains("/")){
//							   pcPath = ((String)customPropertiesMap.get("pc")).substring(((String)customPropertiesMap.get("pc")).indexOf("/"));
//							   pathList.add(pcPath);
//						   }
//						   
//						   customPropertiesMap.remove("pc");
//					   }
//					   if(customPropertiesMap.containsKey("anroid")){
//						   customPropertiesMap.remove("anroid");
//					   }
//					   //请求cs
//					   String csUrl = "http://cs.101.com/v0.1/dentries?session=" + session;
//					   Map<String, Object> requestBody = new HashMap<String, Object>();
//					   requestBody.put("paths", pathList);
//					   HttpHeaders httpHeaders = new HttpHeaders();
//				       httpHeaders.setContentType(MediaType.APPLICATION_JSON);
//				       HttpEntity<Map<String, Object>> entity = new HttpEntity<Map<String, Object>>(requestBody, httpHeaders);
//				       Map<String, List<Map<String, Object>>> resMap = wafSecurityHttpClient.executeForObject(csUrl, HttpMethod.PATCH, entity, Map.class);
//				       
//				       //生成需要创建的TI
//				       for(Map<String, Object> dentry : resMap.get("items")){
//				    		  TechInfo techInfo = new TechInfo();
//				    		  techInfo.setIdentifier(UUID.randomUUID().toString());
//				    		  techInfo.setFormat(ti4Href.getFormat());
//				    		  techInfo.setResource(id);
//				    		  techInfo.setResType("assets");
//				    		  
//				    		  if(StringUtils.isNotEmpty(iosPath) && iosPath.equals((String)dentry.get("path"))){
//				    			  techInfo.setTitle("ios");
//				    			  techInfo.setLocation(iosPath);
//				    			  @SuppressWarnings("unchecked")
//				    			  Map<String, Object> nodeMap = (Map<String, Object>)dentry.get("inode");
//				    			  techInfo.setMd5((String)nodeMap.get("md5"));
//				    			  techInfo.setSize(new Long((Integer)nodeMap.get("size")));
//				    			  
//				    		  }
//				    		  if(StringUtils.isNotEmpty(packagePath) && packagePath.equals((String)dentry.get("path"))){
//				    			  techInfo.setTitle("package");
//				    			  techInfo.setLocation(packagePath);
//				    			  @SuppressWarnings("unchecked")
//				    			  Map<String, Object> nodeMap = (Map<String, Object>)dentry.get("inode");
//				    			  techInfo.setMd5((String)nodeMap.get("md5"));
//				    			  techInfo.setSize(new Long((Integer)nodeMap.get("size")));
//				    			  
//				    		  }
//				    		  if(StringUtils.isNotEmpty(androidPath) && androidPath.equals((String)dentry.get("path"))){
//				    			  techInfo.setTitle("android");
//				    			  techInfo.setLocation(androidPath);
//				    			  @SuppressWarnings("unchecked")
//				    			  Map<String, Object> nodeMap = (Map<String, Object>)dentry.get("inode");
//				    			  techInfo.setMd5((String)nodeMap.get("md5"));
//				    			  techInfo.setSize(new Long((Integer)nodeMap.get("size")));
//				    			  
//				    		  }
//				    		  if(StringUtils.isNotEmpty(webPath) && webPath.equals((String)dentry.get("path"))){
//				    			  techInfo.setTitle("web");
//				    			  techInfo.setLocation(webPath);
//				    			  @SuppressWarnings("unchecked")
//				    			  Map<String, Object> nodeMap = (Map<String, Object>)dentry.get("inode");
//				    			  techInfo.setMd5((String)nodeMap.get("md5"));
//				    			  techInfo.setSize(new Long((Integer)nodeMap.get("size")));
//				    			  
//				    		  }
//				    		  if(StringUtils.isNotEmpty(pcPath) && pcPath.equals((String)dentry.get("path"))){
//				    			  techInfo.setTitle("pc");
//				    			  techInfo.setLocation(pcPath);
//				    			  @SuppressWarnings("unchecked")
//				    			  Map<String, Object> nodeMap = (Map<String, Object>)dentry.get("inode");
//				    			  techInfo.setMd5((String)nodeMap.get("md5"));
//				    			  techInfo.setSize(new Long((Integer)nodeMap.get("size")));
//				    			  
//				    		  }
//				    		  
//				    		  techInfos.add(techInfo);
//				       }
//				       
//				       //修改资源的custom_properties
//				       asset.setCustomProperties(ObjectUtils.toJson(customPropertiesMap));
//					}
//					
//					//修改preview
//					Map<String, String> previewMap = asset.getPreview();
//					if(CollectionUtils.isNotEmpty(previewMap) && previewMap.containsKey("png")){
//						String png = previewMap.get("png");
//						if(png.endsWith("?size=240") || png.endsWith("?")){
//							previewMap.put("png", png.substring(0, png.lastIndexOf("?")));
//						}
//						
//						asset.setPreview(previewMap);
//						assets.add(asset);
//					}
//				}
//			}else{
//				break;
//			}
//			
//			commonServiceHelper.save3d(techInfos, assets);
//			
//			//循环参数处理
//			querySql = new StringBuilder(querySql.substring(0, querySql.lastIndexOf("LIMIT")));
//			offset += pageSize;
//			resultMap.clear();
//		}
//	}
//	
//	/**
//	 * 要放到其他类中
//	 * @param techInfos
//	 * @param assets
//	 */
//	@Transactional
//    public void save3d(List<TechInfo> techInfos,List<Asset> assets){
//		//批量创建TI
//		if(CollectionUtils.isNotEmpty(techInfos)){
//			try {
//				techInfoRepository.batchAdd(techInfos);
//			} catch (EspStoreException e) {
//				LOG.error("批量创建TI失败");
//			}
//		}
//		//批量修改资源
//		if(CollectionUtils.isNotEmpty(assets)){
//			try {
//				assetRepository.batchAdd(assets);
//			} catch (EspStoreException e) {
//				LOG.error("批量修改资源失败");
//			}
//		}
//    }
	
//    @Override
//    @Transactional
//    public void updateProvider4Question() {
//        WafSecurityHttpClient wafSecurityHttpClient = new WafSecurityHttpClient();
//        
//        //查出要修改的习题id(事先录到一个DB表中)
//        String sql = "SELECT identifier FROM question_provider WHERE flag=0";
//        Query query = assetRepository.getEntityManager().createNativeQuery(sql);
//        List<String> qids = query.getResultList();
//        
//        if(CollectionUtils.isNotEmpty(qids)){
//            for(String qid : qids){
//                //获取需要修改成的提供商名
//                String sql4Name = "SELECT pName FROM question_provider WHERE identifier='" + qid + "'";
//                query = assetRepository.getEntityManager().createNativeQuery(sql4Name);
//                String pName = (String)query.getSingleResult();
//                
//                if(StringUtils.isNotEmpty(pName)){
//                    //获取习题详细
//                    StringBuilder sb = new StringBuilder();
//                    sb.append(LifeCircleApplicationInitializer.properties.getProperty("lcms.uri"));
////                    sb.append("http://localhost:8080/esp-lifecycle");
//                    sb.append("/v0.6/questions/");
//                    sb.append(qid);
//                    sb.append("?include=TI,CG,LC,EDU,CR");
//                    QuestionViewModel qvm = null;
//                    try {
//                        qvm = wafSecurityHttpClient.getForObject(sb.toString(), QuestionViewModel.class);
//                    } catch (Exception e) {
//                        LOG.error("获取资源元数据失败：",e);
//                        //修改flag,3
//                        updateFlag(qid, 3);
//                        continue;
//                    }
//                    
//                    //更新
//                    qvm.getLifeCycle().setProvider(pName);
//                    StringBuilder sb4update = new StringBuilder();
//                    sb4update.append(LifeCircleApplicationInitializer.properties.getProperty("lcms.uri"));
////                    sb4update.append("http://localhost:8080/esp-lifecycle");
//                    sb4update.append("/v0.6/questions/");
//                    sb4update.append(qid);
//                    try {
//                        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
//                        List<String> list = new ArrayList<String>();
//                        list.add("application/json");
//                        headers.put("Content-Type", list);
//                        HttpEntity<Object> httpEntity = new HttpEntity<Object>(ObjectUtils.fromJson(WafJsonMapper.toJson(qvm), Map.class), headers);
//                        wafSecurityHttpClient.executeForObject(sb4update.toString(), HttpMethod.PUT, httpEntity, QuestionViewModel.class);
//                    } catch (Exception e) {
//                        LOG.error("更新资源失败：",e);
//                        //修改flag,4
//                        updateFlag(qid, 4);
//                        continue;
//                    }
//                    
//                    //修改flag,1
//                    updateFlag(qid, 1);
//                }else{
//                    //修改flag,2
//                    updateFlag(qid, 2);
//                }
//            }
//        }
//    }
//    
//    /**
//     * 修改flag
//     * <p>Create Time: 2016年2月22日   </p>
//     * <p>Create author: xiezy   </p>
//     * @param qid
//     * @param flag 0=未处理 1=已处理 2=pName is Empty 3=获取习题详细失败  4=更新习题失败
//     */
//    @Transactional
//    private void updateFlag(String qid,int flag){
//        String sql4Flag = "UPDATE question_provider SET flag=" + flag + " WHERE identifier='" + qid + "'";
//        Query query = assetRepository.getEntityManager().createNativeQuery(sql4Flag);
//        query.executeUpdate();
//    }
}

/**
 * 处理知识点left、right内部类
 * 
 * @author caocr
 */
class ConvertKnowledge implements Runnable {
    private KnowledgeRepository knowledgeRepository;

    public ConvertKnowledge(KnowledgeRepository knowledgeRepository) {
        this.knowledgeRepository = knowledgeRepository;
    }

    @Override
    public void run() {
        // 1. 获取知识点中未建树的学科集合
        String querySql = "SELECT DISTINCT(subject) FROM knowledges";
        final List<String> subjectList = new ArrayList<String>();
        knowledgeRepository.getJdbcTemple().query(querySql, new RowMapper<String>() {

            @Override
            public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                String subject = rs.getString("subject");
                subjectList.add(subject);
                return null;
            }

        });
        
        // 2.循环对学科处理
        if (CollectionUtils.isNotEmpty(subjectList)) {
            for (String subject : subjectList) {
                // 2.1 获取数据库中Knowledges
                List<Knowledge> knowledges = null;
                try {
                    Knowledge knowledge = new Knowledge();
                    knowledge.setSubject(subject);
                    knowledges = knowledgeRepository.getAllByExample(knowledge);
                } catch (EspStoreException e) {
                    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                            LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                            e.getMessage());
                }

                // 2.2 添加左右值
                if (CollectionUtils.isEmpty(knowledges)) {
                    continue;
                }
                
                int i = 1;
                
                for(Knowledge knowledge : knowledges){
                    knowledge.setNodeLeft(i++);
                    knowledge.setNodeRight(i++);
                    knowledge.setParent("ROOT");
                    
                    // 2.3 更新知识点
                    try {
                        knowledgeRepository.update(knowledge);
                    } catch (EspStoreException e) {
                        throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                      LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                                      e.getMessage());
                    }
                }

            }
        }
    } 

}
