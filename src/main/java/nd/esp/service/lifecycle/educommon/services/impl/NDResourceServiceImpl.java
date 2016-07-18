package nd.esp.service.lifecycle.educommon.services.impl;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import nd.esp.service.lifecycle.app.LifeCircleApplicationInitializer;
import nd.esp.service.lifecycle.daos.common.CommonDao;
import nd.esp.service.lifecycle.daos.teachingmaterial.v06.ChapterDao;
import nd.esp.service.lifecycle.daos.titan.inter.TitanRelationRepository;
import nd.esp.service.lifecycle.educommon.dao.NDResourceDao;
import nd.esp.service.lifecycle.educommon.models.ResClassificationModel;
import nd.esp.service.lifecycle.educommon.models.ResContributeModel;
import nd.esp.service.lifecycle.educommon.models.ResCoverageModel;
import nd.esp.service.lifecycle.educommon.models.ResEducationalModel;
import nd.esp.service.lifecycle.educommon.models.ResLifeCycleModel;
import nd.esp.service.lifecycle.educommon.models.ResRelationModel;
import nd.esp.service.lifecycle.educommon.models.ResRightModel;
import nd.esp.service.lifecycle.educommon.models.ResTechInfoModel;
import nd.esp.service.lifecycle.educommon.models.ResourceModel;
import nd.esp.service.lifecycle.educommon.models.TechnologyRequirementModel;
import nd.esp.service.lifecycle.educommon.services.NDResourceService;
import nd.esp.service.lifecycle.educommon.support.RelationType;
import nd.esp.service.lifecycle.educommon.vos.ChapterStatisticsViewModel;
import nd.esp.service.lifecycle.educommon.vos.ResEducationalViewModel;
import nd.esp.service.lifecycle.educommon.vos.ResLifeCycleViewModel;
import nd.esp.service.lifecycle.educommon.vos.ResRightViewModel;
import nd.esp.service.lifecycle.educommon.vos.ResTechInfoViewModel;
import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;
import nd.esp.service.lifecycle.educommon.vos.VersionViewModel;
import nd.esp.service.lifecycle.educommon.vos.constant.IncludesConstant;
import nd.esp.service.lifecycle.educommon.vos.constant.PropOperationConstant;
import nd.esp.service.lifecycle.entity.elasticsearch.Resource;
import nd.esp.service.lifecycle.models.AccessModel;
import nd.esp.service.lifecycle.models.coverage.v06.CoverageModel;
import nd.esp.service.lifecycle.models.v06.EducationRelationLifeCycleModel;
import nd.esp.service.lifecycle.models.v06.EducationRelationModel;
import nd.esp.service.lifecycle.models.v06.KnowledgeModel;
import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.CategoryData;
import nd.esp.service.lifecycle.repository.model.Chapter;
import nd.esp.service.lifecycle.repository.model.ResRepoInfo;
import nd.esp.service.lifecycle.repository.model.ResourceCategory;
import nd.esp.service.lifecycle.repository.model.TechInfo;
import nd.esp.service.lifecycle.repository.sdk.CategoryDataRepository;
import nd.esp.service.lifecycle.repository.sdk.ResRepoInfoRepository;
import nd.esp.service.lifecycle.services.coverages.v06.CoverageService;
import nd.esp.service.lifecycle.services.educationrelation.v06.EducationRelationServiceForQuestionV06;
import nd.esp.service.lifecycle.services.educationrelation.v06.EducationRelationServiceV06;
import nd.esp.service.lifecycle.services.elasticsearch.AsynEsResourceService;
import nd.esp.service.lifecycle.services.elasticsearch.ES_Search;
import nd.esp.service.lifecycle.services.lifecycle.v06.LifecycleServiceV06;
import nd.esp.service.lifecycle.services.notify.NotifyReportService;
import nd.esp.service.lifecycle.services.offlinemetadata.OfflineService;
import nd.esp.service.lifecycle.services.titan.TitanSearchService;
import nd.esp.service.lifecycle.support.Constant;
import nd.esp.service.lifecycle.support.Constant.CSInstanceInfo;
import nd.esp.service.lifecycle.support.DbName;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.support.busi.elasticsearch.ResourceTypeSupport;
import nd.esp.service.lifecycle.support.busi.tree.preorder.TreeDirection;
import nd.esp.service.lifecycle.support.busi.tree.preorder.TreeModel;
import nd.esp.service.lifecycle.support.busi.tree.preorder.TreeService;
import nd.esp.service.lifecycle.support.busi.tree.preorder.TreeTrargetAndParentModel;
import nd.esp.service.lifecycle.support.enums.ES_SearchField;
import nd.esp.service.lifecycle.support.enums.OperationType;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;
import nd.esp.service.lifecycle.utils.BeanMapperUtils;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.JDomUtils;
import nd.esp.service.lifecycle.utils.ParamCheckUtil;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;
import nd.esp.service.lifecycle.vos.ListViewModel;
import nd.esp.service.lifecycle.vos.statics.CoverageConstant;
import nd.esp.service.lifecycle.vos.statics.ResRepositoryConstant;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Assert;
import org.modelmapper.Condition;
import org.modelmapper.ModelMapper;
import org.modelmapper.spi.MappingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.collect.Sets;
import com.google.gson.reflect.TypeToken;
import com.nd.gaea.WafException;
import com.nd.gaea.client.WafResourceAccessException;
import com.nd.gaea.client.http.WafSecurityHttpClient;
import com.nd.gaea.rest.security.authens.UserCenterRoleDetails;
import com.nd.gaea.rest.security.authens.UserInfo;

@Service
public class NDResourceServiceImpl implements NDResourceService{
    
    private static final Logger LOG = LoggerFactory.getLogger(NDResourceServiceImpl.class);
    private final static ExecutorService executorService = CommonHelper.getPrimaryExecutorService();
    
    //忽略categoryList, techInfoList
    private final static ModelMapper specialModelMapper = new ModelMapper();
    static{
        final String[] names = {"categoryList","techInfoList"};
        Condition skipIds = new Condition() {
            @Override
            public boolean applies(MappingContext context) {
                String name = context.getMapping().getLastDestinationProperty().getName();
                boolean flag = true;
                if(Arrays.binarySearch(names, name) > -1){
                    flag = false;
                }
                return flag;
            }
        };
        
        specialModelMapper.getConfiguration().setAmbiguityIgnored(true);
        specialModelMapper.getConfiguration().setPropertyCondition(skipIds);
    }
    
    //忽略target
    private final static ModelMapper specialModelMapper4Ext = new ModelMapper();
    static{
        final String[] names = {"target"};
        Condition skipIds = new Condition() {
            @Override
            public boolean applies(MappingContext context) {
                String name = context.getMapping().getLastDestinationProperty().getName();
                boolean flag = true;
                if(Arrays.binarySearch(names, name) > -1){
                    flag = false;
                }
                return flag;
            }
        };
        
        specialModelMapper4Ext.getConfiguration().setAmbiguityIgnored(true);
        specialModelMapper4Ext.getConfiguration().setPropertyCondition(skipIds);
    }
    
    @Autowired
    @Qualifier("educationRelationServiceV06")
    private EducationRelationServiceV06 educationRelationService;
    
    @Autowired
    @Qualifier("educationRelationServiceForQuestionV06")
    private EducationRelationServiceForQuestionV06 educationRelationService4QuestionDB;
    
    @Autowired
    @Qualifier("coverageServiceImpl")
    private CoverageService coverageService;
    
    @Autowired
    @Qualifier("coverageService4QuestionDBImpl")
    private CoverageService coverageService4QuestionDB;
    
	@Autowired
    private AsynEsResourceService esResourceOperation;
	
	@Autowired
	private OfflineService offlineService;
    
    /**
     * SDK注入
     */
    @Autowired
    private CommonServiceHelper commonServiceHelper;
    @Autowired
    private NDResourceDao ndResourceDao;
    @Autowired
    private ResRepoInfoRepository resRepoInfoRepository;
    @Autowired
    private CommonDao commonDao;
    @Autowired
    private CategoryDataRepository categoryDataRepository;
    @Autowired
    private TreeService treeService;
    @Autowired
    private ChapterDao chapterDao;
    
    @Autowired
    private ES_Search eS_Search;
    
 // just for test titan;
 	@Autowired
 	private TitanSearchService titanSearchService;
     @Autowired
     private TitanRelationRepository titanRelationRepository;
    
    @Autowired
    private NotifyReportService nds;
    
    @Autowired()
    @Qualifier("lifecycleServiceV06")
    private LifecycleServiceV06 lifecycleService;
    
    @Autowired()
    @Qualifier("lifecycleService4QtiV06")
    private LifecycleServiceV06 lifecycleService4Qti;
    
    //默认路径key
    private static final String DEFAULT_LOCATION_KEY="href"; 
    private static final String PPT_LOCATION_KEY = "$ppt2html$";
    private static final int ND_AND_PERSON_ROOT_PATH_LENGTH=2;
    private static final String ND_AND_PERSON_DEFAUL_ORG = "esp";
    private static final int OTHER_ORG_ROOT_PATH_LENGTH=3;
    
    @Override
	public ListViewModel<ResourceModel> resourceQueryByEla(String resType,
			List<String> includes, Set<String> categories, Set<String> categoryExclude,
			List<Map<String, String>> relations, List<String> coverages,
			Map<String, Set<String>> propsMap, Map<String, String> orderMap,
			String words, String limit, boolean isNotManagement, boolean reverse,
			Boolean printable, String printableKey) {
		// 返回的结果集
		ListViewModel<ResourceModel> listViewModel = new ListViewModel<ResourceModel>();

		// 参数整理
		Map<String, Map<String, List<String>>> params = this
				.dealFieldAndValues(categories, categoryExclude, relations, coverages, propsMap, isNotManagement, printable, printableKey);
		Integer result[] = ParamCheckUtil.checkLimit(limit);
		if(includes == null){
			includes = new ArrayList<String>();
		}
		listViewModel = eS_Search.searchByES(resType, includes, params, orderMap, result[0], result[1]);
		listViewModel.setLimit(limit);
		return listViewModel;
	}
    
    /**
     * 资源检索(titan)
     * @author linsm
     */
    @Override
	public ListViewModel<ResourceModel> resourceQueryByTitan(String resType,
			List<String> includes, Set<String> categories, Set<String> categoryExclude,
			List<Map<String, String>> relations, List<String> coverages,
			Map<String, Set<String>> propsMap, Map<String, String> orderMap,
			String words, String limit, boolean isNotManagement, boolean reverse,Boolean printable, String printableKey) {
		// 返回的结果集
		ListViewModel<ResourceModel> listViewModel = new ListViewModel<ResourceModel>();

		// 参数整理
		Map<String, Map<String, List<String>>> params = this
				.dealFieldAndValues(categories, categoryExclude, relations, coverages, propsMap, isNotManagement,printable,printableKey);
		Integer result[] = ParamCheckUtil.checkLimit(limit);
		if(includes == null){
			includes = new ArrayList<String>();
		}
		//just for test by lsm
		listViewModel = 
				titanSearchService.searchWithAdditionProperties(resType, includes, params, orderMap,
						result[0], result[1],reverse,words);
		listViewModel.setLimit(limit);
		return listViewModel;
	}
    
    /**
     * 资源检索(titan)
     * @author linsm
     */
    @Override
	public ListViewModel<ResourceModel> resourceQueryByTitanES(String resType,
			List<String> includes, Set<String> categories, Set<String> categoryExclude,
			List<Map<String, String>> relations, List<String> coverages,
			Map<String, Set<String>> propsMap, Map<String, String> orderMap,
			String words, String limit, boolean isNotManagement, boolean reverse,Boolean printable, String printableKey) {
		// 返回的结果集
		ListViewModel<ResourceModel> listViewModel = new ListViewModel<ResourceModel>();

		// 参数整理
		Map<String, Map<String, List<String>>> params = this
				.dealFieldAndValues(categories, categoryExclude, relations, coverages, propsMap, isNotManagement,printable,printableKey);
		Integer result[] = ParamCheckUtil.checkLimit(limit);
		if(includes == null){
			includes = new ArrayList<String>();
		}
		//just for test by lsm
		listViewModel = 
				titanSearchService.searchUseES(resType, includes, params, orderMap,
						result[0], result[1],reverse,words);
		if (listViewModel != null)listViewModel.setLimit(limit);
		return listViewModel;
	}
    
    /**
     * ES的参数处理
     * @param categories
     * @param categoryExclude
     * @param relations
     * @param coverages
     * @param propsMap
     * @param isNotManagement
     * @return
     */
	private Map<String, Map<String, List<String>>> dealFieldAndValues(
			Set<String> categories, Set<String> categoryExclude, List<Map<String, String>> relations,
			List<String> coverages, Map<String, Set<String>> propsMap, boolean isNotManagement,
			Boolean printable, String printableKey) {
		Map<String, Map<String, List<String>>> params = CommonHelper
				.newHashMap();

		// categories(ne, eq for elasticsearch)
		// params key值定义
		final String key_cgPath = ES_SearchField.cg_taxonpath.toString();
		final String key_cgCode = ES_SearchField.cg_taxoncode.toString();
		List<String> cgPathEq = new ArrayList<String>();
		List<String> cgCodeEq = new ArrayList<String>();
		if (CollectionUtils.isNotEmpty(categories)) {
			for (String cg : categories) {
				if (cg.contains("/")) {// path
					cgPathEq.add(cg);
				} else {
					cgCodeEq.add(cg);
				}
			}
		}
		if (CollectionUtils.isNotEmpty(cgPathEq)) {
			if (!params.containsKey(key_cgPath)) {
				params.put(key_cgPath, new HashMap<String, List<String>>());
			}
			params.get(key_cgPath).put(PropOperationConstant.OP_EQ, cgPathEq);
		}
		if (CollectionUtils.isNotEmpty(categoryExclude)) {
			if (!params.containsKey(key_cgCode)) {
				params.put(key_cgCode, new HashMap<String, List<String>>());
			}
			params.get(key_cgCode).put(PropOperationConstant.OP_NE,
					new ArrayList<String>(categoryExclude));
		}
		if (CollectionUtils.isNotEmpty(cgCodeEq)) {
			if (!params.containsKey(key_cgCode)) {
				params.put(key_cgCode, new HashMap<String, List<String>>());
			}
			params.get(key_cgCode).put(PropOperationConstant.OP_EQ, cgCodeEq);
		}
		
		// coverage(in for elasticsearch)
		// params key值定义
		final String key_cv = ES_SearchField.coverages.toString();
		List<String> cvIn = new ArrayList<String>();
		if (CollectionUtils.isNotEmpty(coverages)) {
			if(isNotManagement){
				cvIn.addAll(coverageParam4DealOnline(coverages));
			}else{
				cvIn.addAll(coverages);
			}
		}else{//不传coverage时    
			//默认空间,公共库
			cvIn.add(CoverageConstant.TargetType.TARGET_TYPE_PB.getCode() + "/"
					+ CoverageConstant.TARGET_PUBLIC + "/"
					+ CoverageConstant.Strategy.STRATEGY_SHAREING.getCode());
		}

		if (CollectionUtils.isNotEmpty(cvIn)) {
			if(!params.containsKey(key_cv)){
				params.put(key_cv, new HashMap<String, List<String>>());
			}
			params.get(key_cv).put(PropOperationConstant.OP_IN, cvIn);
		}

		
		// relations
		// params key值定义
		final String key_re = "relation";
		List<String> reEq = new ArrayList<String>();
		// List<String> reLike = new ArrayList<String>();
		if (CollectionUtils.isNotEmpty(relations)) {
			for (Map<String, String> relation : relations) {
				String r = relation.get("stype") + "/" + relation.get("suuid")
						+ "/";
				if (relation.get("rtype") == null) {
					r += "*";
				} else {
					r += relation.get("rtype");
				}
				reEq.add(r);
			}
		}

		if (CollectionUtils.isNotEmpty(reEq)) {
			params.put(key_re, new HashMap<String, List<String>>());
			params.get(key_re).put(PropOperationConstant.OP_EQ, reEq);
		}
		// if (CollectionUtils.isNotEmpty(reLike)) {
		// params.put(key_re, new HashMap<String, List<String>>());
		// params.get(key_re).put(PropOperationConstant.OP_LIKE, reLike);
		// }

		// props
		if (CollectionUtils.isNotEmpty(propsMap)) {
			for (String key : propsMap.keySet()) {
				String pkey = "";
				if (key.endsWith("_GT")) {// 时间 gt
					pkey = key.substring(0, key.length() - 3);
					if (!params.containsKey(pkey)) {
						params.put(pkey, new HashMap<String, List<String>>());
					}
					params.get(pkey).put(PropOperationConstant.OP_GT,
							new ArrayList<String>(propsMap.get(key)));
				} else if (key.endsWith("_LT")) {// 时间 lt
					pkey = key.substring(0, key.length() - 3);
					if (!params.containsKey(pkey)) {
						params.put(pkey, new HashMap<String, List<String>>());
					}
					params.get(pkey).put(PropOperationConstant.OP_LT,
							new ArrayList<String>(propsMap.get(key)));
				} else if (key.endsWith("_LE")) {// 时间 le
					pkey = key.substring(0, key.length() - 3);
					if (!params.containsKey(pkey)) {
						params.put(pkey, new HashMap<String, List<String>>());
					}
					params.get(pkey).put(PropOperationConstant.OP_LE,
							new ArrayList<String>(propsMap.get(key)));
				} else if (key.endsWith("_GE")) {// 时间 ge
					pkey = key.substring(0, key.length() - 3);
					if (!params.containsKey(pkey)) {
						params.put(pkey, new HashMap<String, List<String>>());
					}
					params.get(pkey).put(PropOperationConstant.OP_GE,
							new ArrayList<String>(propsMap.get(key)));
				}else if (key.endsWith("_NE")) {// ne
					pkey = key.substring(0, key.length() - 3);
					if (!params.containsKey(pkey)) {
						params.put(pkey, new HashMap<String, List<String>>());
					}
					params.get(pkey).put(PropOperationConstant.OP_NE,
							new ArrayList<String>(propsMap.get(key)));
				} else if (key.endsWith("_LIKE")) {// like
					pkey = key.substring(0, key.length() - 5);
					if (!params.containsKey(pkey)) {
						params.put(pkey, new HashMap<String, List<String>>());
					}
					params.get(pkey).put(PropOperationConstant.OP_LIKE,
							new ArrayList<String>(propsMap.get(key)));
				} else {// eq or in
					pkey = key;
					if (!params.containsKey(pkey)) {
						params.put(pkey, new HashMap<String, List<String>>());
					}
					params.get(pkey).put(PropOperationConstant.OP_IN,
							new ArrayList<String>(propsMap.get(key)));
				}
			}
		}
		
		if(printable!=null){
			String key = "ti_printable";
			String printableString = printable ? "true" : "false";
			params.put(key, new HashMap<String, List<String>>());
			List<String> values = new ArrayList<String>();
			if(StringUtils.isNotEmpty(printableKey)){
				values.add(printableString + "#" + printableKey);
			}else {
				values.add(printableString);
			}
			
			params.get(key).put(PropOperationConstant.OP_EQ,values);
		}

		return params;
	}
	
	/**
	 * ES覆盖范围参数处理--针对isNotManagement=true时
	 * @param coverages
	 * @return
	 */
	private List<String> coverageParam4DealOnline(List<String> coverages){
		List<String> cvIn = new ArrayList<String>();
        
        for(String coverage : coverages){
            List<String> coverageElemnt = Arrays.asList(coverage.split("/"));
            if(coverageElemnt.get(0).equals(CoverageConstant.TargetType.TARGET_TYPE_ORG.getCode()) &&
            		coverageElemnt.get(1).equals(CoverageConstant.ORG_CODE_ND)){//ND库
            	cvIn.add(coverage + "/ONLINE");
            }else{
            	cvIn.add(coverage);
            }
        }
        
        return cvIn;
    }
    
    @Override
    public ListViewModel<ResourceModel> resourceQueryByDB(String resType,String resCodes, List<String> includes,
            Set<String> categories, Set<String> categoryExclude, List<Map<String, String>> relations, List<String> coverages,
            Map<String, Set<String>> propsMap,Map<String, String>orderMap, String words, String limit,boolean isNotManagement,boolean reverse,
            Boolean printable, String printableKey,String statisticsType,String statisticsPlatform,boolean forceStatus,List<String> tags,boolean showVersion) {
    	
    	ListViewModel<ResourceModel> rListViewModel = new ListViewModel<ResourceModel>();
        rListViewModel.setLimit(limit);
        
        //判断使用IN还是EXISTS
        boolean useIn = ndResourceDao.judgeUseInOrExists(resType, resCodes, categories, categoryExclude, relations, coverages, propsMap, words, isNotManagement, reverse, printable, printableKey,forceStatus,tags,showVersion);
        
        //查总数和Items使用线程同时查询
        List<Callable<QueryThread>> threads = new ArrayList<Callable<QueryThread>>();
        QueryThread countThread = new QueryThread(true, resType, resCodes, includes, categories, categoryExclude, relations, coverages, propsMap, null, words, limit, isNotManagement, reverse,useIn, printable, printableKey,statisticsType,statisticsPlatform,forceStatus,tags,showVersion);
        QueryThread queryThread = null;
        if(ndResourceDao.judgeUseRedisOrNot("(0,1)", isNotManagement, coverages)){//如果是走Redis的,useIn=true
            queryThread = new QueryThread(false, resType, resCodes, includes, categories, categoryExclude, relations, coverages, propsMap, orderMap, words, limit, isNotManagement, reverse, true, printable, printableKey,statisticsType,statisticsPlatform,forceStatus,tags,showVersion);
        }else{
            queryThread = new QueryThread(false, resType, resCodes, includes, categories, categoryExclude, relations, coverages, propsMap, orderMap, words, limit, isNotManagement, reverse, useIn, printable, printableKey,statisticsType,statisticsPlatform,forceStatus,tags,showVersion);
        }
        threads.add(countThread);
        threads.add(queryThread);
        
        try {
            List<Future<QueryThread>> results = executorService.invokeAll(threads, 10*60, TimeUnit.SECONDS);
            for(Future<QueryThread> result:results){
                try {
                    if(result.get().isCount()){
                        rListViewModel.setTotal(result.get().getTotal());
                    }else{
                        rListViewModel.setItems(result.get().getItems());
                    }
                } catch (ExecutionException e) {
                    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "LC/COMMON_QUERY_GET_FAIL",
                            e.getMessage());
                }
            }
        } catch (InterruptedException e) {
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "LC/QUERY_THREAD_FAIL",
                    e.getMessage());
        }
        
        //TODO 由于count采取缓存方式,为防止第一页显示total和资源数目不一致做的处理
        Integer result[] = ParamCheckUtil.checkLimit(limit);
        if(result[0] == 0 && rListViewModel.getItems().size() < result[1]){
            rListViewModel.setTotal(new Long(rListViewModel.getItems().size()));
        }
        
        return rListViewModel;
    }
    
    /**
     * 内部类
     * <p>Create Time: 2015年11月30日           </p>
     * @author xiezy
     */
    class QueryThread implements Callable<QueryThread> {
        private boolean isCount;
        private String resType;
        private String resCodes;
        private List<String> includes;
        private Set<String> categories;
        private Set<String> categoryExclude;
        private List<Map<String, String>> relations;
        private List<String> coverages;
        private Map<String, Set<String>> propsMap;
        private Map<String, String>orderMap;
        private String words;
        private String limit;
        private boolean isNotManagement;
        private boolean reverse;
        private boolean useIn;
        private Boolean printable;
        private String printableKey;
        private String statisticsType;
        private String statisticsPlatform;
        private boolean forceStatus;
        private List<String> tags;
        private boolean showVersion;
        
        //返回值
        private Long total;
        private List<ResourceModel> items;
        
        public Long getTotal() {
            return total;
        }

        public List<ResourceModel> getItems() {
            return items;
        }

        public boolean isCount() {
            return isCount;
        }

        QueryThread(boolean isCount, String resType,String resCodes, List<String> includes,
            Set<String> categories, Set<String> categoryExclude, List<Map<String, String>> relations, List<String> coverages,
            Map<String, Set<String>> propsMap,Map<String, String> orderMap, String words, String limit,boolean isNotManagement,boolean reverse,boolean useIn,
            Boolean printable, String printableKey,String statisticsType,String statisticsPlatform,boolean forceStatus,List<String> tags,boolean showVersion){
            this.isCount = isCount;
            this.resType = resType;
            this.resCodes = resCodes;
            this.includes = includes;
            this.categories = categories;
            this.categoryExclude = categoryExclude;
            this.relations = relations;
            this.coverages = coverages;
            this.propsMap = propsMap;
            this.orderMap = orderMap;
            this.words = words;
            this.limit = limit;
            this.isNotManagement = isNotManagement;
            this.reverse = reverse;
            this.useIn = useIn;
            this.printable = printable;
            this.printableKey = printableKey;
            this.statisticsType = statisticsType;
            this.statisticsPlatform = statisticsPlatform;
            this.forceStatus = forceStatus;
            this.tags = tags;
            this.showVersion = showVersion;
        }
        
        @Override
        public QueryThread call() throws Exception {
            if(isCount){
                this.total = ndResourceDao.commomQueryCount(resType, resCodes, categories, categoryExclude, relations, coverages, propsMap, words, limit,isNotManagement,reverse,useIn, printable, printableKey,forceStatus,tags,showVersion);
            }else{
                this.items = ndResourceDao.commomQueryByDB(resType, resCodes, includes, categories, categoryExclude, relations, coverages, propsMap, orderMap, words, limit,isNotManagement,reverse,useIn, printable, printableKey, statisticsType, statisticsPlatform,forceStatus,tags,showVersion);
            }
            
            return this;
        }
    }
    
    @Override
    public ListViewModel<ResourceViewModel> resourceQuery4IntelliKnowledge(List<String> includesList,String chapterId, String pageSize, String offset) {
        ListViewModel<ResourceViewModel> rListViewModel = new ListViewModel<ResourceViewModel>();
        
        //include有哪些值的判断
        boolean haveTI = false;
        boolean haveEDU = false;
        boolean haveLC = false;
        boolean haveCR = false;
        boolean haveCG = false;
        if(CollectionUtils.isNotEmpty(includesList)){
            for(String include : includesList){
                if(include.equals(IncludesConstant.INCLUDE_TI)){
                    haveTI = true;
                }else if(include.equals(IncludesConstant.INCLUDE_EDU)){
                    haveEDU = true;
                }else if(include.equals(IncludesConstant.INCLUDE_LC)){
                    haveLC = true;
                }else if(include.equals(IncludesConstant.INCLUDE_CR)){
                    haveCR = true;
                }else if(include.equals(IncludesConstant.INCLUDE_CG)){
                    haveCG = true;
                }
            }
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(LifeCircleApplicationInitializer.properties.getProperty("intelli.uri"));
        sb.append("v0.2/api/lcmsquestions/list/");
        sb.append(chapterId);
        sb.append("?page_size=");
        sb.append(pageSize);
        sb.append("&offset=");
        sb.append(offset);
        
        String queryUrl = sb.toString();
        WafSecurityHttpClient wafSecurityHttpClient = new WafSecurityHttpClient();
        
        Map<String, Object> response = new HashMap<String, Object>();
        try {
            response = wafSecurityHttpClient.getForObject(queryUrl, Map.class);
        } catch (WafResourceAccessException e) {
//        } catch (WafException e) {
            if("SMARTQ/NOT_FOUND".equalsIgnoreCase(e.getRemoteResponseEntity().getBody().getCode())){
//            if("WAF/API_NOT_FOUND".equalsIgnoreCase(e.getResponseEntity().getBody().getCode())){
                
                rListViewModel.setTotal(0L);
                rListViewModel.setItems(new ArrayList<ResourceViewModel>());
                return rListViewModel;
            }else{
                throw e;
            }
        }
        
        if(CollectionUtils.isNotEmpty(response)){
            if(CollectionUtils.isNotEmpty((List<Map>)(response.get("rows")))){
                List<ResourceViewModel> resultList = new ArrayList<ResourceViewModel>();
                
                for(Map map : (List<Map>)(response.get("rows"))){
                    ResourceViewModel rvm = ObjectUtils.fromJson(ObjectUtils.toJson(map), ResourceViewModel.class);
                    
                    if(!haveCG){
                        rvm.setCategories(null);
                    }
                    if(!haveLC){
                        rvm.setLifeCycle(null); 
                    }else{
                        ResLifeCycleViewModel rlc = new ResLifeCycleViewModel();
                        rvm.setLifeCycle(rlc);
                    }
                    if(!haveCR){
                        rvm.setCopyright(null);
                    }else{
                        ResRightViewModel rrvm = new ResRightViewModel();
                        rvm.setCopyright(rrvm);
                    }
                    if(!haveEDU){
                        rvm.setEducationInfo(null);
                    }else{
                        ResEducationalViewModel rem = new ResEducationalViewModel();
                        rem.setSemanticDensity(((Map)map.get("education_info")).get("semantic_density") == null ? 0L : Long.parseLong((String)((Map)map.get("education_info")).get("semantic_density")));
                        rem.setDifficulty(((Map)map.get("education_info")).get("difficulty") == null ? null : (String)((Map)map.get("education_info")).get("difficulty"));
                        rem.setLearningTime(((Map)map.get("education_info")).get("learning_time") == null ? null : (String)((Map)map.get("education_info")).get("learning_time"));
                        rem.setDescription(((Map)map.get("education_info")).get("edu_description") == null ? null : (Map)((Map)map.get("education_info")).get("edu_description"));
                        rem.setLanguage(((Map)map.get("education_info")).get("edu_language") == null ? null : (String)((Map)map.get("education_info")).get("edu_language"));
                        rvm.setEducationInfo(rem);
                    }
                    if(!haveTI){
                        rvm.setTechInfo(null);
                    }else{
                        ResTechInfoViewModel rti = new ResTechInfoViewModel();
                        rti.setLocation((String)((Map)((Map)map.get("tech_info")).get("href")).get("location"));
                        Map<String, ResTechInfoViewModel> tiMap = new HashMap<String, ResTechInfoViewModel>();
                        tiMap.put("href", rti);
                        rvm.setTechInfo(tiMap); 
                    }
                    
                    //添加到resultList
                    resultList.add(rvm);
                }
                
                rListViewModel.setItems(resultList);
                rListViewModel.setTotal(((Integer)response.get("total")).longValue());
            }else{
                rListViewModel.setTotal(0L);
                rListViewModel.setItems(new ArrayList<ResourceViewModel>());
                return rListViewModel;
            }
        }
        
        return rListViewModel;
    }
    
    @Override
	public Map<String, Integer> resourceStatistics(String resType,
			Set<String> categories, List<String> coverages,
			Map<String, Set<String>> propsMap, String groupBy,
			boolean isNotManagement) {
		
		return ndResourceDao.resourceStatistics(resType, categories, coverages, propsMap, groupBy, isNotManagement);
	}

    /**	
     * @desc:set转list  
     * @createtime: 2015年6月26日 
     * @author: liuwx 
     * @param target
     * @return
     */
    private List<String> setToList(Set<String> target) {
        List<String> results = new ArrayList<String>();
        if(CollectionUtils.isNotEmpty(target)){
            results.addAll(target);  
        }
        return results;
    }

    
    /*
     * (non-Javadoc)
     * @see nd.esp.service.lifecycle.educommon.services.LsmService#getDetail(java.lang.String)
     */
    @Override
    public ResourceModel getDetail(String resourceType, String uuid, List<String> includeList) {
        return getDetail(resourceType,uuid,includeList,false);
    }

    private ResourceModel changeToModel(EspEntity bean, String resourceType, List<String> includeList) {
        if (bean == null) {
            return null;
        }
        // 忽略categoryList, techInfoList
        ResourceModel model = specialModelMapper.map(bean, commonServiceHelper.getModel(resourceType));
        try {
        	//反射处理扩展属性 add by xuzy 2015-08-05
        	Field[] fs = model.getClass().getDeclaredFields();
        	Field f = null;
        	for (int i = 0; i < fs.length; i++) {
				if(fs[i].getName().equals("extProperties")){
					f = fs[i];
					break;
				}
			}
        	if(f != null){
                Object o = null;
                o = specialModelMapper4Ext.map(bean, f.getType());
                Method m =  model.getClass().getMethod("setExtProperties",f.getType());
                m.invoke(model, o);
        	}
		} catch (Exception e) {
		    
		    LOG.warn("反射处理扩展属性出错！",e);
		    
		} 
        
        try {
            addIncludes(includeList, model, bean,resourceType);
        } catch (EspStoreException e) {
            
            LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(),e);
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                          e.getMessage());
        }
        return model;
    }
    
    /**
     * 处理include所包含的返回值
     * <p>Create Time: 2015年7月10日   </p>
     * <p>Create author: xiezy   </p>
     * @param includes
     * @param resourceModel
     * @param ndResource
     * @throws EspStoreException
     */
    private void addIncludes(List<String> includes,ResourceModel resourceModel,EspEntity ndResource,String resType) throws EspStoreException{
        for(String include : includes){
            if(include.equals(IncludesConstant.INCLUDE_TI)){
                List<ResTechInfoModel> resTechInfoModels = new ArrayList<ResTechInfoModel>();
                
                TechInfo techInfo = new TechInfo();
                techInfo.setResource(resourceModel.getIdentifier());
                List<TechInfo> techInfos = commonServiceHelper.getTechInfoRepositoryByResType(resType).getAllByExample(techInfo);
                
                if(!CollectionUtils.isEmpty(techInfos)){
                    for(TechInfo ti : techInfos){
                        resTechInfoModels.add(changeTechInfoToModel(ti));
                    }
                }
                
                resourceModel.setTechInfoList(resTechInfoModels);
            }else if(include.equals(IncludesConstant.INCLUDE_EDU)){
                ResEducationalModel resEducationalModel = specialModelMapper.map(ndResource,
                                                                                     ResEducationalModel.class);
                // description和language属性名不一致,单独处理
                if (ndResource instanceof Education) {
                    resEducationalModel.setDescription(((Education) ndResource).getEduDescription());
                    resEducationalModel.setLanguage(((Education) ndResource).getEduLanguage());
                }
                resourceModel.setEducationInfo(resEducationalModel);
            }else if(include.equals(IncludesConstant.INCLUDE_LC)){
                ResLifeCycleModel resLifeCycleModel = specialModelMapper.map(ndResource, ResLifeCycleModel.class);
                resourceModel.setLifeCycle(resLifeCycleModel);
            }else if(include.equals(IncludesConstant.INCLUDE_CR)){
                ResRightModel resRightModel = specialModelMapper.map(ndResource, ResRightModel.class);
                // description/right属性名不一致,单独处理
                if (ndResource instanceof Education) {
                    resRightModel.setDescription(((Education) ndResource).getCrDescription());
                    resRightModel.setRight(((Education)ndResource).getCrRight());
                }
                resourceModel.setCopyright(resRightModel);
            }else if(include.equals(IncludesConstant.INCLUDE_CG)){
                List<ResClassificationModel> resClassificationModels = new ArrayList<ResClassificationModel>();
                
                ResourceCategory resourceCategory = new ResourceCategory();
                resourceCategory.setResource(resourceModel.getIdentifier());
                List<ResourceCategory> resourceCategories = commonServiceHelper.getResourceCategoryRepositoryByResType(resType).getAllByExample(resourceCategory);
            
                if(!CollectionUtils.isEmpty(resourceCategories)){
                    for (ResourceCategory rc : resourceCategories) {
                        resClassificationModels.add(changeResourceCategoryToModel(rc));
                    }
                }
                
                resourceModel.setCategoryList(resClassificationModels);
            }
        }
    }

    /* (non-Javadoc)
     * @see nd.esp.service.lifecycle.educommon.services.NDResourceService#batchDetail(java.lang.String, java.util.Set, java.util.List)
     */
    @Override
    public List<ResourceModel> batchDetail(String resourceType, Set<String> uuidSet, List<String> includeList) {
    	return batchDetail(resourceType, uuidSet, includeList,false);
    }

    /* (non-Javadoc)
     * @see nd.esp.service.lifecycle.educommon.services.NDResourceService#batchDetail(java.lang.String, java.util.Set, java.util.List)
     */
    @Override
    public List<ResourceModel> batchDetail(String resourceType, Set<String> uuidSet, List<String> includeList,Boolean isAll) {
        List<ResourceModel> resourceModels = new ArrayList<ResourceModel>();
        if(CollectionUtils.isEmpty(uuidSet)){
            return resourceModels;
        }
        
        ResourceRepository<? extends EspEntity> resourceRepository = commonServiceHelper.getRepository(resourceType);
        List<? extends EspEntity> beanListResult = null;
        try {
            beanListResult = resourceRepository.getAll(new ArrayList<String>(uuidSet));
        } catch (EspStoreException e) {
           
            LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(),e);
           
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                          e.getMessage());
        }
        //不再抛出异常：改成返回空
//        if (CollectionUtils.isEmpty(beanListResult)) {
//            
//            LOG.error(LifeCircleErrorMessageMapper.ResourceNotFound.getMessage() + "resourceType:" + resourceType
//                    + "  uuidSet: " + uuidSet);
//            
//            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
//                                          LifeCircleErrorMessageMapper.ResourceNotFound.getCode(),
//                                          LifeCircleErrorMessageMapper.ResourceNotFound.getMessage()
//                                                  + "resourceType:"                                + resourceType + "  uuidSet: " + uuidSet);
//        }
        if(CollectionUtils.isEmpty(beanListResult)){
            return  resourceModels;
        }
        ResourceModel modelResult = null;
        List<String> newIncludeList = new ArrayList<String>(includeList);//需要拷贝一份，controller 也要使用到includeList
        boolean isNeedTI = newIncludeList.contains(IncludesConstant.INCLUDE_TI);
        boolean isNeedCG = newIncludeList.contains(IncludesConstant.INCLUDE_CG);
        boolean isNeedCollectID = isNeedTI||isNeedCG;
        if(isNeedTI){
            newIncludeList.remove(IncludesConstant.INCLUDE_TI);
        }
        if(isNeedCG){
            newIncludeList.remove(IncludesConstant.INCLUDE_CG);
        }
        Set<String> uuids = new HashSet<String>();
        
        
		if (!isAll) {
			for (EspEntity espEntity : beanListResult) {
				if (espEntity != null
						&& ((Education) espEntity).getEnable() != null
						&& ((Education) espEntity).getEnable()
						&& resourceType.equals(((Education) espEntity)
								.getPrimaryCategory())) {
					modelResult = changeToModel(espEntity, resourceType,
							newIncludeList);
					if (modelResult != null) {
						resourceModels.add(modelResult);
						if (isNeedCollectID) {
							uuids.add(modelResult.getIdentifier());
						}
					}
				}
			}
		} else {
			// 不考虑enable（包含已删除数据）
			for (EspEntity espEntity : beanListResult) {
				if (espEntity != null
						&& resourceType.equals(((Education) espEntity)
								.getPrimaryCategory())) {
					modelResult = changeToModel(espEntity, resourceType,
							newIncludeList);
					if (modelResult != null) {
						resourceModels.add(modelResult);
						if (isNeedCollectID) {
							uuids.add(modelResult.getIdentifier());
						}
					}
				}
			}

		}
        
        //不存在非删除的数据
        if(CollectionUtils.isEmpty(resourceModels)){
            return resourceModels;
        }
        
        if(isNeedTI){
           addTI(resourceModels,resourceType,uuids); 
        }
        
        if(isNeedCG){
            addCG(resourceModels,resourceType,uuids);
        }

        
        return resourceModels;
    }

    /**
     * 添加resource_category
     * @author linsm
     * @param resourceModels
     * @param resourceType
     * @param uuids
     * @since 
     */
    private void addCG(List<ResourceModel> resourceModels, String resourceType, Set<String> uuids) {
        if(CollectionUtils.isEmpty(uuids)||CollectionUtils.isEmpty(resourceModels)){
            return;
        }
        List<String> resourceTypes = new ArrayList<String>();
        resourceTypes.add(resourceType);
        List<ResourceCategory> resourceCategories = ndResourceDao.queryCategoriesUseHql(resourceTypes, uuids);
        
        Map<String, List<ResClassificationModel>> resClassificationModelMap = new HashMap<String, List<ResClassificationModel>>();
        if(CollectionUtils.isNotEmpty(resourceCategories)){
            for(ResourceCategory resourceCategory:resourceCategories){
                if(resourceCategory != null){
                    String key = resourceCategory.getResource();
                    List<ResClassificationModel> resClassificationModels = resClassificationModelMap.get(key);
                    if(resClassificationModels == null){
                        resClassificationModels = new ArrayList<ResClassificationModel>();
                        resClassificationModelMap.put(key, resClassificationModels);
                    }
                    resClassificationModels.add(changeResourceCategoryToModel(resourceCategory));
                }
            }
        }
        
        //add to model
        if(CollectionUtils.isNotEmpty(resourceModels)){
            for(ResourceModel resourceModel:resourceModels){
                if(resourceModel != null){
                    resourceModel.setCategoryList(resClassificationModelMap.get(resourceModel.getIdentifier()));
                }
            }
        }
    }

    /**
     * @author linsm
     * @param resourceCategory
     * @return
     * @since 
     */
    private ResClassificationModel changeResourceCategoryToModel(ResourceCategory resourceCategory) {
        return  BeanMapperUtils.beanMapper(resourceCategory, ResClassificationModel.class);
    }

    /**
     * 添加tech_infos
     * @author linsm
     * @param resourceModels
     * @param resourceType
     * @param uuids
     * @since 
     */
    private void addTI(List<ResourceModel> resourceModels, String resourceType, Set<String> uuids) {
        if(CollectionUtils.isEmpty(uuids)||CollectionUtils.isEmpty(resourceModels)){
            return;
        }
        List<String> resourceTypes = new ArrayList<String>();
        resourceTypes.add(resourceType);
        List<TechInfo> techInfos = ndResourceDao.queryTechInfosUseHql(resourceTypes, uuids);
        
        Map<String, List<ResTechInfoModel>> resTechInfoModelMap = new HashMap<String, List<ResTechInfoModel>>();
        if(CollectionUtils.isNotEmpty(techInfos)){
            for(TechInfo techInfo:techInfos){
                if(techInfo != null){
                    String key = techInfo.getResource();
                    List<ResTechInfoModel> resTechInfoModels = resTechInfoModelMap.get(key);
                    if(resTechInfoModels == null){
                        resTechInfoModels = new ArrayList<ResTechInfoModel>();
                        resTechInfoModelMap.put(key, resTechInfoModels);
                    }
                    resTechInfoModels.add(changeTechInfoToModel(techInfo));
                }
            }
        }
        
        //add to model
        if(CollectionUtils.isNotEmpty(resourceModels)){
            for(ResourceModel resourceModel:resourceModels){
                if(resourceModel != null){
                    resourceModel.setTechInfoList(resTechInfoModelMap.get(resourceModel.getIdentifier()));
                }
            }
        }
        
    }

    /**
     * @author linsm
     * @param techInfo
     * @return
     * @since 
     */
    private ResTechInfoModel changeTechInfoToModel(TechInfo techInfo) {
        ResTechInfoModel rtim = BeanMapperUtils.beanMapper(techInfo, ResTechInfoModel.class);
        //requirements单独处理
        List<TechnologyRequirementModel> req = ObjectUtils.fromJson(techInfo.getRequirements(), new TypeToken<List<TechnologyRequirementModel>>(){});
        rtim.setRequirements(req); 
        return rtim;
    }

    /**
     * 删除资源（并删除关系）
     * 
     * @param resourceType
     * @param uuid
     * @since
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    @Transactional
    public void delete(String resourceType, String uuid) {
        // 1、获取资源， 查询关系，更新enable(资源，关系)
        ResourceRepository resourceRepository = commonServiceHelper.getRepository(resourceType);
        EspEntity beanResult = null;
        try {
            beanResult = resourceRepository.get(uuid);
        } catch (EspStoreException e) {
           
            LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(),e);
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                          e.getMessage());
        }
        if (beanResult == null || ((Education) beanResult).getEnable() == null
                || !((Education) beanResult).getEnable()||!resourceType.equals(((Education)beanResult).getPrimaryCategory())) {
            
            LOG.error(LifeCircleErrorMessageMapper.ResourceNotFound.getMessage() + "resourceType:" + resourceType
                    + "uuid:" + uuid);
           
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.ResourceNotFound.getCode(),
                                          LifeCircleErrorMessageMapper.ResourceNotFound.getMessage()
                                                  + "resourceType:" + resourceType + "uuid:" + uuid);
        }
        
        //2、判断该资源下有没有在用的版本
        List<Map<String,Object>> list = ndResourceDao.queryResourceByMid(resourceType,uuid);
        if(list != null && list.size() > 1){
        	throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,"LC/DELETE_RESOURCE_DENY","资源存在子版本，无法删除");
        }
        
        //所有通用接口支持的资源都继承了NdResource
        //存在问题，教学目标等，并没有继承NdResource
        ((Education)beanResult).setEnable(false);
        ((Education)beanResult).setLastUpdate(new Timestamp(new Date().getTime()));
        
        LOG.debug("删除类型:{},id:{}资源",resourceType,beanResult.getIdentifier());
        
        try {
            beanResult = resourceRepository.update(beanResult);
            String resourceId = beanResult.getIdentifier();
            deleteRelation(resourceType,resourceId);
            
//            //如果是教材或教辅,需要异步删除其相关章节和相关章节下的关系
//            if(IndexSourceType.TeachingMaterialType.getName().equals(resourceType) || 
//                    IndexSourceType.GuidanceBooksType.getName().equals(resourceType)){
//                final String mid = uuid;
//                Thread thread = new Thread(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        commonServiceHelper.deleteChaptersAndRelations(mid);
//                    }
//                    
//                });
//                
//                executorService.execute(thread);
//            }
        } catch (EspStoreException e) {
            
            LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(),e);
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                          e.getMessage());
        }
        
        //同步推送至报表系统 add by xuzy 20160512
        nds.deleteResourceCategory(uuid);
        nds.deleteResource(resourceType, uuid);
    }
    
    /**
     * 删除资源
     * 
     * @param resourceType
     * @param uuid
     * @since
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    @Transactional(value="questionTransactionManager")
    public void deleteInQuestionDB(String resourceType, String uuid) {
        // 1、获取资源， 查询关系，更新enable(资源，关系)
        ResourceRepository resourceRepository = commonServiceHelper.getRepository(resourceType);
        EspEntity beanResult = null;
        try {
            beanResult = resourceRepository.get(uuid);
        } catch (EspStoreException e) {
           
            LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(),e);
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                          e.getMessage());
        }
        if (beanResult == null || ((Education) beanResult).getEnable() == null
                || !((Education) beanResult).getEnable()||!resourceType.equals(((Education)beanResult).getPrimaryCategory())) {
            
            LOG.error(LifeCircleErrorMessageMapper.ResourceNotFound.getMessage() + "resourceType:" + resourceType
                    + "uuid:" + uuid);
           
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.ResourceNotFound.getCode(),
                                          LifeCircleErrorMessageMapper.ResourceNotFound.getMessage()
                                                  + "resourceType:" + resourceType + "uuid:" + uuid);
        }
        //所有通用接口支持的资源都继承了NdResource
        //存在问题，教学目标等，并没有继承NdResource
        ((Education)beanResult).setEnable(false);
        ((Education)beanResult).setLastUpdate(new Timestamp(new Date().getTime()));
        
        LOG.debug("删除类型:{},id:{}资源",resourceType,beanResult.getIdentifier());
        
        try {
            beanResult = resourceRepository.update(beanResult);
            String resourceId = beanResult.getIdentifier();
            deleteRelation(resourceType,resourceId);
        } catch (EspStoreException e) {
            LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(),e);
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                          e.getMessage());
        }
        
        //同步推送至报表系统 add by xuzy 20160518
        nds.deleteResourceCategory(uuid);
        nds.deleteResource(resourceType, uuid);
    }
    
    /**
     * 删除各个数据库中的资源关系（源与目标）
     * 
     * @param resourceType
     * @param uuid
     * @since
     */
    private void deleteRelation(String resourceType, String uuid) {
    	commonServiceHelper.deleteRelation(resourceType,uuid);
    	commonServiceHelper.deleteRelation4QuestionDB(resourceType, uuid);
    	
    	//TODO delete relation for titan
        titanRelationRepository.deleteRelationSoft(resourceType,uuid);
    }
    

    
    /**
     * CS文件上传 <br>
     * Created 2015年5月13日 下午4:06:25
     * 
     * @param resourceType
     * @param uuid
     * @param uid
     * @param renew 是否续约
     * @param coverage 资源库类型（默认为个人库，非空时，为nd私有库)
     * @return
     * @author linsm
     */
    @Override
    public AccessModel getUploadUrl(String resourceType, String uuid, String uid, Boolean renew, String coverage) {
        String rootPath = "";
        // 逻辑校验 uuid,只能是已存在的资源或是none
        if (uuid.equals(Constant.DEFAULT_UPLOAD_URL_ID)) {
            uuid = UUID.randomUUID().toString();
            if(isPersonal(coverage)){
                //个人
                rootPath = Constant.CS_INSTANCE_MAP.get(Constant.CS_DEFAULT_INSTANCE).getPath(); 
            }else{
                rootPath = assertHasAuthorizationAndGetPath(coverage,uid);
            }
           
        } else {
            // 非续约，要判断是否存在对应的元数据
            if (!renew) {
                // check exist resource
                ResourceModel resourceModel = getDetail(resourceType, uuid, Arrays.asList(IncludesConstant.INCLUDE_TI));

                if (resourceModel == null) {
                    // 不存在对应的资源
                    
                   
                    LOG.error(LifeCircleErrorMessageMapper.CSResourceNotFound.getMessage());
                   
                    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                  LifeCircleErrorMessageMapper.CSResourceNotFound);
                }
                String location = getLocation(resourceModel);
                rootPath = getRootPathFromLocation(location);
            } else {
                if(isPersonal(coverage)){
                    //个人
                    rootPath = Constant.CS_INSTANCE_MAP.get(Constant.CS_DEFAULT_INSTANCE).getPath();
                }else{
                    rootPath = assertHasAuthorizationAndGetPath(coverage,uid);
                }
            }

        }
        
        LOG.debug("各个组织cs根目录："+rootPath);
        
        CSInstanceInfo csInstance = getCsInstanceAccordingRootPath(rootPath); // 保证不为空;

        // 设置获取session参数
        CSGetSessionParam param = new CSGetSessionParam();
        param.setServiceId(csInstance.getServiceId());
        String path = producePath(rootPath, resourceType, uuid);
        
        LOG.debug("dis_path:"+path);
        
        String url = csInstance.getUrl() + "/sessions";
        // param.setPath(path);
        // 当包含其它组织时，与这个地址serviceName/esp 是并行的，导致没有权限
        // param.setPath(csInstance.getPath());// FIXME 应文鑫要求扩大授权范围
        // String sessionPath = csInstance.getPath().replace("/esp", ""); //暂时去除esp
        String sessionPath = StringUtils.removeEnd(csInstance.getPath(), "/esp");
        LOG.info("sessionPath:{}", sessionPath);

        param.setPath(sessionPath);
        param.setUid(uid);
        param.setRole(Constant.FILE_OPERATION_ROLE);
        param.setExpires(Constant.FILE_OPERATION_EXPIRETIME);

        // 获得session
        String sessionid = getSessionIdFromCS(url, param);
        
        LOG.debug("session:"+sessionid);

        // 设置返回的对象
        AccessModel accessModel = new AccessModel();
        accessModel.setAccessKey(Constant.FILE_OPERATION_ACCESSKEY);
        accessModel.setAccessUrl(csInstance.getUrl() + "/upload");
        accessModel.setPreview(new HashMap<String, String>());
        accessModel.setUuid(UUID.fromString(uuid));
        accessModel.setExpireTime(CommonHelper.fileOperationExpireDate());
        accessModel.setSessionId(sessionid);
        accessModel.setDistPath(path);

        return accessModel;
    }

    /**
     * 上传接口，判断是否为个人
     * @author linsm
     * @param coverage
     * @return
     * @since 
     */
    private boolean isPersonal(String coverage) {
        if(StringUtils.isEmpty(coverage)){
            return true;
        }
        
        if(StringUtils.startsWith(coverage, "User")){
            return true;
        }
        return false;
    }

    /**
     * 
     * @param location
     * @return
     * @since 
     */
    public static String getRootPathFromLocation(String location) {
        //${ref-path}/edu/esp/questions/b129f99d-464f-4562-ada1-1a8fff50e981.pkg/item.xml
        //暂时通过是否包含“esp"来判断目录结构（若有esp则为两层，个人，nd,若无，则是组织)
        String[] pathChunk = location.split("/");
        if (pathChunk.length < ND_AND_PERSON_ROOT_PATH_LENGTH + 1) {
            // 抛出目录结构异常
            
            LOG.error(LifeCircleErrorMessageMapper.CSFilePathError.getMessage() + location);
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.CSFilePathError.getCode(),
                                          LifeCircleErrorMessageMapper.CSFilePathError.getMessage() + location);
        }
        StringBuilder builder = new StringBuilder();
        if (ND_AND_PERSON_DEFAUL_ORG.equals(pathChunk[2])) {
            // 个人或者是nd
            builder.append("/").append(pathChunk[1]).append("/").append(pathChunk[2]);
        } else {
            // 其它组织
            if (pathChunk.length < OTHER_ORG_ROOT_PATH_LENGTH + 1) {
                // 抛出目录结构异常
                
                LOG.error(LifeCircleErrorMessageMapper.CSFilePathError.getMessage() + location);
                
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              LifeCircleErrorMessageMapper.CSFilePathError.getCode(),
                                              LifeCircleErrorMessageMapper.CSFilePathError.getMessage() + location);
            }
            builder.append("/").append(pathChunk[1]).append("/").append(pathChunk[2]).append("/").append(pathChunk[3]);
        }
        return builder.toString();
    }

    /**
     * 
     * @param rootPath
     * @return
     * @since 
     */
    public static CSInstanceInfo getCsInstanceAccordingRootPath(String rootPath) {
        CSInstanceInfo csInfo = null;
        if (StringUtils.isEmpty(rootPath)) {
            // 抛出物理地址错误
            
            LOG.error(LifeCircleErrorMessageMapper.CSFilePathError.getMessage() + rootPath);
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.CSFilePathError.getCode(),
                                          LifeCircleErrorMessageMapper.CSFilePathError.getMessage() + rootPath);
        }
        String[] pathChunk = rootPath.split("/");
        if (pathChunk.length < 2) {
            // 抛出物理地址错误
            
            LOG.error(LifeCircleErrorMessageMapper.CSFilePathError.getMessage() + rootPath);
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.CSFilePathError.getCode(),
                                          LifeCircleErrorMessageMapper.CSFilePathError.getMessage() + rootPath);
        }
        String serviceName = pathChunk[1];

        //FIXME 后期需要修改，目前这种做法无法保证抛出正确的”错误信息“ （by lsm 2015.11.18)
        if (Constant.CS_DEFAULT_INSTANCE.contains(serviceName)) {
            csInfo = Constant.CS_INSTANCE_MAP.get(Constant.CS_DEFAULT_INSTANCE); 
        }else if(Constant.CS_DEFAULT_INSTANCE_OTHER.contains(serviceName)){
            csInfo =  Constant.CS_INSTANCE_MAP.get(Constant.CS_DEFAULT_INSTANCE_OTHER); 
        }
        
        if (csInfo == null) {
            
            LOG.error(LifeCircleErrorMessageMapper.CSInstanceKeyNotFound.getMessage());
           
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.CSInstanceKeyNotFound); // 抛出异常
        }
        return csInfo;
    }

    /**
     * CS文件上传 <br>
     * Created 2015年5月13日 下午4:06:25
     * 
     * @param resourceType
     * @param uuid
     * @param uid
     * @param renew 是否续约
     * @return
     * @author linsm
     */
    @Override
    public AccessModel getUploadUrl(String resourceType, String uuid, String uid, Boolean renew) {
        return getUploadUrl(resourceType, uuid, uid, renew, "");
    }

    @Override
    public AccessModel getDownloadUrl(String resourceType, String uuid, String uid, String key) {
        CSInstanceInfo csInstanceInfo = null;
        // 逻辑校验 uuid,只能是已存在的资源
        // check exist resource
        ResourceModel resourceModel = getDetail(resourceType, uuid, Arrays.asList(IncludesConstant.INCLUDE_TI));
        if (resourceModel == null) {
            // 不存在对应的资源
            
            LOG.error(LifeCircleErrorMessageMapper.CSResourceNotFound.getMessage());
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.CSResourceNotFound);
        }
        // 获取到目录
        String location = "";
        boolean isPpt2Html = PPT_LOCATION_KEY.equals(key)&&"coursewares".equals(resourceType);
        if (StringUtils.isEmpty(key)||isPpt2Html) {
            //都使用转码后的实例
            location = getLocation(resourceModel);
        } else {
            location = getLocation(resourceModel, key);
        }

        String rootPath = getRootPathFromLocation(location);
        csInstanceInfo = getCsInstanceAccordingRootPath(rootPath); // 保证不为空，若空会抛出异常
        if (isPpt2Html) {
            location = producePptLocation(rootPath, uuid);
        }
        // String path = producePath(rootPath, resourceType, uuid); //FIXME 到时需要再综合考虑特殊处理ppt2html

        CSGetSessionParam param = new CSGetSessionParam();
        // param.setPath(path); FIXME 需要特殊处理一下路径
        // param.setPath(csInstanceInfo.getPath());// FIXME 应文鑫要求扩大授权范围
        // 见上传
        String sessionPath = StringUtils.removeEnd(csInstanceInfo.getPath(), "/esp");
        LOG.info("sessionPath:{}", sessionPath);
        param.setPath(sessionPath);
        param.setServiceId(csInstanceInfo.getServiceId());
        param.setUid(uid);
        param.setRole(Constant.FILE_OPERATION_ROLE);
        param.setExpires(Constant.FILE_OPERATION_EXPIRETIME);

        // 获得session
        String sessionid = getSessionIdFromCS(csInstanceInfo.getUrl() + "/sessions", param);

        
        LOG.debug("session:"+sessionid);
        
        AccessModel accessModel = new AccessModel();
        accessModel.setAccessKey(Constant.FILE_OPERATION_ACCESSKEY);
        accessModel.setAccessMethod(RequestMethod.GET.toString());
        accessModel.setAccessUrl(location);
        accessModel.setPreview(new HashMap<String, String>());
        accessModel.setUuid(UUID.fromString(uuid));
        accessModel.setExpireTime(CommonHelper.fileOperationExpireDate());
        accessModel.setSessionId(sessionid);
        
        return accessModel;
    }


	/**
     * 
     * @param uuid
     * @return
     * @since 
     */
    private String producePptLocation(String root,String uuid) {
        StringBuilder stringBuilder = new StringBuilder("${ref-path}");
        stringBuilder.append(root).append("/assets/ppts/").append(uuid).append(".pkg");
        return stringBuilder.toString();
    }

    @Override
	public Map<String, Object> getResPreviewUrls(String resType, String uuid) {
		Map<String, Object> returnMap = new HashMap<String, Object>();
        //1、逻辑校验 uuid,只能是已存在的资源
        ResourceModel resourceModel = getDetail(resType, uuid, Arrays.asList(IncludesConstant.INCLUDE_TI));
        
        //2、判断是否为PPT文件
        List<ResTechInfoModel> techInfoList = resourceModel.getTechInfoList();
        boolean sourceFlag = false;
        if(CollectionUtils.isNotEmpty(techInfoList)){
        	for (ResTechInfoModel resTechInfoModel : techInfoList) {
        		String title = resTechInfoModel.getTitle();
				if(StringUtils.isNotEmpty(title) && resTechInfoModel.getTitle().equals("source")){
					sourceFlag = true;
					break;
				}
			}
        }
        if(!sourceFlag){
        	return returnMap;
        }
        //3、 获取到目录
        String location = getLocation(resourceModel);
        if(!location.endsWith("xml")){
            return returnMap;
        }
        
        return getResPreviewByHref(resType, location);
        
    }
    
    @Override
    public Map<String, Object> getResPreviewByHref(String resType, String location) {
        Map<String, Object> returnMap = new HashMap<String, Object>();
        String rootPath = getRootPathFromLocation(location);
        CSInstanceInfo csInstanceInfo = getCsInstanceAccordingRootPath(rootPath); // 保证不为空，若空会抛出异常

        //4、获取session
        CSGetSessionParam param = new CSGetSessionParam();
        param.setPath(csInstanceInfo.getPath());
        param.setServiceId(csInstanceInfo.getServiceId());
        param.setUid("777");
        param.setRole(Constant.FILE_OPERATION_ROLE);
        param.setExpires(Constant.FILE_OPERATION_EXPIRETIME);
        String sessionid = getSessionIdFromCS(csInstanceInfo.getUrl() + "/sessions", param);
        
        //5、获取转码后的列表信息
        WafSecurityHttpClient wafSecurityHttpClient = new WafSecurityHttpClient();
        StringBuilder sb = new StringBuilder();
        sb.append(csInstanceInfo.getUrl());
        sb.append("/");
        sb.append(sessionid); 
        sb.append("/static");
        String xmlPath = "";
        if(IndexSourceType.LessonPlansType.getName().equals(resType)
                || IndexSourceType.LearningPlansType.getName().equals(resType)) {
            xmlPath = location.substring(location.indexOf("/")).replace("main.xml", "pages/imagePage.xml");
        } else {
            xmlPath = location.substring(location.indexOf("/"));
        }
        sb.append(xmlPath);
        String responseXml = wafSecurityHttpClient.getForObject(sb.toString(), String.class);
		
        JDomUtils jdu = new JDomUtils();
        try {
			Document doc = jdu.buildDoc(responseXml);
			if(doc != null){
				Element root =  doc.getRootElement();
				List<String> urlList = new ArrayList<String>();
				if(IndexSourceType.LessonPlansType.getName().equals(resType)
				        || IndexSourceType.LearningPlansType.getName().equals(resType)) {
				    Element modulesEle = root.getChild("modules");
				    if(modulesEle!=null) {
    				    for(Element module : (List<Element>)modulesEle.getChildren("addonModule")) {
    				        if(module.getAttributeValue("addonId").equals("EBookContent")) {
    				            Element propEle = module.getChild("properties");
    				            if(propEle!=null) {
        				            for(Element prop : (List<Element>)propEle.getChildren("property")) {
        				                if("images".equals(prop.getAttributeValue("name"))) {
        				                    Element itemsEle = prop.getChild("items");
        				                    if(itemsEle!=null) {
        				                        for(Element item : (List<Element>)itemsEle.getChildren("item")) {
        				                            for(Element itemProp : (List<Element>)item.getChildren("property")) {
        				                                if("src".equals(itemProp.getAttributeValue("name"))) {
                				                            String url = itemProp.getAttributeValue("value");
                				                            if(StringUtils.isNotEmpty(url)) {
                				                                url = url.replace("..", location.substring(0, location.lastIndexOf("/")));
                				                                urlList.add(url);
                				                            }
                				                            break;
        				                                }
        				                            }
        				                        }
        				                    }
        				                    break;
        				                }
        				            }
    				            }
    				        }
    				    }
				    }
				} else {
    				Element pagesEle = root.getChild("pages");
    				List pages = pagesEle.getChildren("page");
    				for (Object object : pages) {
    					Element ele = (Element)object;
                        String preivew =ele.getAttributeValue("preview");
                        if(StringUtils.isNotEmpty(preivew)) {
                            urlList.add(preivew);
                        }
    				}
				}
				returnMap.put("previewUrls", urlList);
			}
		} catch (JDOMException e) {
			LOG.warn("解析main.xml文件失败",e);
		} catch (IOException e) {
			LOG.warn("获取资源的预览列表失败",e);
		}
        returnMap.put("session", sessionid);
        returnMap.put("access_method", "GET");
        returnMap.put("cs_host_url", csInstanceInfo.getUrl()+"/download");
		return returnMap;
	}
	
    /**
     * 调用cs接口获取 session <br>
     * Created 2015年5月13日 下午4:07:34
     * 
     * @param param post 请求的相关参数
     * @return session
     * @author linsm
     */
    @SuppressWarnings({ "unchecked", "deprecation" })
    private String getSessionIdFromCS(String url, CSGetSessionParam param) {
        Map<String, Object> requestBody = new HashMap<String, Object>();
        requestBody.put("path", param.getPath());
        requestBody.put("service_id", param.getServiceId());
        //FIXME uid 在lcms中是String， 在cs中是long,可能会存在问题
        requestBody.put("uid", param.getUid());
        requestBody.put("role", param.getRole());
        if (param.getExpires() != null) {
            requestBody.put("expires", param.getExpires());// optional
        }

        LOG.debug("调用cs接口："+url);
        
        WafSecurityHttpClient wafSecurityHttpClient = new WafSecurityHttpClient();
        String sessionId="";
        try {
            Map<String, String> session = wafSecurityHttpClient.post(url, requestBody, Map.class);
            sessionId = session.get("session");
        } catch (Exception e) {
            LOG.error(LifeCircleErrorMessageMapper.InvokingCSFail.getMessage(),e);
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.InvokingCSFail.getCode(),e.getLocalizedMessage());
        }
        return sessionId;
    }

    /**
     * @param root
     * @param resourceType
     * @param uuid
     * @return
     * @since
     */
    public static String producePath(String root, String resourceType, String uuid) {
        StringBuilder stringBuilder = new StringBuilder(root);
        stringBuilder.append("/").append(resourceType).append("/").append(uuid).append(".pkg");
        return stringBuilder.toString();
    }

    /**
     * 校验是否授权，检查物理空间是否已申请 更新：2015.10.26 (当没有申请时，自动申请物理空间),添加参数uid
     * 
     * @author linsm
     * @param coverage
     * @since
     */
    private String assertHasAuthorizationAndGetPath(String coverage, String uid) {
        String[] coverageChunk = coverage.split("/");
        //update 2015.12.11 放开长度的要求，改成只要不少于2段
        if (coverageChunk == null || coverageChunk.length < 2) {
            // 抛出异常

            LOG.error(LifeCircleErrorMessageMapper.CSUploadCoverageError.getMessage());

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.CSUploadCoverageError);
        }

        ResRepoInfo repoInfo = new ResRepoInfo();
        repoInfo.setTargetType(coverageChunk[0]);
        repoInfo.setTarget(coverageChunk[1]);
        repoInfo.setEnable(null);

        try {
            repoInfo = resRepoInfoRepository.getByExample(repoInfo);
        } catch (EspStoreException e) {

            LOG.error("获取物理空间信息失败", e);

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.GetRepositoryFail);
        }

        // 将物理空间不存在时抛出未授权，改为申请物理空间
        // if (repoInfo == null || !repoInfo.getEnable()) {
        //
        // LOG.error(LifeCircleErrorMessageMapper.CSUploadCoverageNotExist.getMessage());
        //
        // throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
        // LifeCircleErrorMessageMapper.CSUploadCoverageNotExist);
        // }
        if (repoInfo == null) {
            // 未创建
            // 校验targetType,创建物理空间
            if (!ResRepositoryConstant.isRepositoryTargetType(coverageChunk[0])) {

                LOG.error("targetType不在可选范围内-targetType取值范围为:Org,Group");

                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              LifeCircleErrorMessageMapper.TargetTypeIsNotExist);
            }

            repoInfo = new ResRepoInfo();

            repoInfo.setIdentifier(UUID.randomUUID().toString());
            repoInfo.setCreateTime(new Timestamp(System.currentTimeMillis()));
            repoInfo.setEnable(true);
            repoInfo.setStatus(ResRepositoryConstant.STATUS_RUNNING);
            repoInfo.setRepositoryAdmin(uid);
            repoInfo.setRepositoryName(coverage);
            repoInfo.setDescription(coverage);
            repoInfo.setTargetType(coverageChunk[0]);
            repoInfo.setTarget(coverageChunk[1]);
            repoInfo.setTitle(coverage);
            repoInfo.setRepositoryPath(Constant.CS_SESSION_PATH + "/" + coverage);

            try {
                repoInfo = resRepoInfoRepository.add(repoInfo);
            } catch (EspStoreException e) {

                LOG.error("申请物理资源存储空间失败", e);

                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              LifeCircleErrorMessageMapper.CreateRepositoryFail);
            }
            
            // 将创建记录写在日志中
            LOG.info("申请物理资源存储空间" + coverage);

        } else if (repoInfo.getEnable()==null||!repoInfo.getEnable()) {
            // 已创建，但被禁用
            // 启用
            repoInfo.setCreateTime(new Timestamp(System.currentTimeMillis()));
            repoInfo.setEnable(true);
            repoInfo.setStatus(ResRepositoryConstant.STATUS_RUNNING);
            try {
                repoInfo = resRepoInfoRepository.update(repoInfo);
            } catch (EspStoreException e) {

                LOG.error("修改资源物理空间信息失败", e);
                
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              LifeCircleErrorMessageMapper.UpdateRepositoryFail);
            }
            
            // 将启用记录写在日志中
            LOG.info("启用物理资源存储空间" + coverage);
        }

        if (repoInfo == null) {

            LOG.error("申请物理资源存储空间失败");

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.CreateRepositoryFail);
        }

        return repoInfo.getRepositoryPath();

    }

    /**
     * @param resourceModel
     * @return
     * @since
     */
    private String getLocation(ResourceModel resourceModel) {
        return getLocation(resourceModel, DEFAULT_LOCATION_KEY);
    }

    /**
     * @param resourceModel
     * @param defaultLocationKey
     * @return
     * @since
     */
    private String getLocation(ResourceModel resourceModel, String defaultLocationKey) {
        Assert.assertNotNull(resourceModel);
        String location = "";

        List<ResTechInfoModel> techInfoModels = resourceModel.getTechInfoList();
        if (CollectionUtils.isNotEmpty(techInfoModels)) {
            for (ResTechInfoModel model : techInfoModels) {
                if (model != null && defaultLocationKey.equals(model.getTitle())) {
                    location =model.getLocation();
                    break;
                }
            }
        }
        
        if (StringUtils.isEmpty(location)) {
            // 抛出异常，没有对应key的地址
            
            LOG.error(LifeCircleErrorMessageMapper.CSFilePathError.getMessage() + "不存在对应于key = "+defaultLocationKey+"的地址");
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.CSFilePathError.getCode(),
                                          LifeCircleErrorMessageMapper.CSFilePathError.getMessage() + "不存在对应于key = "+defaultLocationKey+"的地址");
        }
        return location;
    }

    /**
     * cs获取 session 相关参数 <br>
     * Created 2015年5月13日 下午4:08:22
     * 
     * @version CSFileUploadServiceImpl
     * @author linsm
     * @see Copyright(c) 2009-2014, TQ Digital Entertainment, All Rights Reserved
     */
    private static class CSGetSessionParam {
        /**
         * 路径
         */
        private String path;
        /**
         * 服务id
         */
        private String serviceId;
        /**
         * 用户id
         */
        private String uid;
        /**
         * 角色
         */
        private String role;
        /**
         * 过期时间(秒)
         */
        private Integer expires;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getServiceId() {
            return serviceId;
        }

        public void setServiceId(String serviceId) {
            this.serviceId = serviceId;
        }

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public Integer getExpires() {
            return expires;
        }

        public void setExpires(Integer expires) {
            this.expires = expires;
        }
    }

    /* (non-Javadoc)
     * @see nd.esp.service.lifecycle.educommon.services.NDResourceService#getDetail(java.lang.String, java.lang.String, java.util.List, java.lang.Boolean)
     */
    @Override
    public ResourceModel getDetail(String resourceType, String uuid, List<String> includeList, Boolean isAll) {
        ResourceRepository<? extends EspEntity> resourceRepository = commonServiceHelper.getRepository(resourceType);
        EspEntity beanResult = null;
        try {
            beanResult = resourceRepository.get(uuid);
        } catch (EspStoreException e) {
           
            LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(),e);
           
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                          e.getMessage());
        }
        if (!isAll) {
            if (beanResult == null || ((Education) beanResult).getEnable() == null
                    || !((Education) beanResult).getEnable() || !resourceType.equals(((Education)beanResult).getPrimaryCategory())) {

                LOG.error(LifeCircleErrorMessageMapper.ResourceNotFound.getMessage() + " resourceType:" + resourceType
                        + " uuid:" + uuid);

                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              LifeCircleErrorMessageMapper.ResourceNotFound.getCode(),
                                              LifeCircleErrorMessageMapper.ResourceNotFound.getMessage()
                                                      + " resourceType:" + resourceType + " uuid:" + uuid);
            }
        } else {
            if (beanResult == null||!resourceType.equals(((Education)beanResult).getPrimaryCategory())) {

                LOG.info("never created");
                
                LOG.error(LifeCircleErrorMessageMapper.ResourceNotFound.getMessage() + " resourceType:" + resourceType
                        + " uuid:" + uuid);

                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              LifeCircleErrorMessageMapper.ResourceNotFound.getCode(),
                                              LifeCircleErrorMessageMapper.ResourceNotFound.getMessage()
                                                      + " resourceType:" + resourceType + " uuid:" + uuid);
            }
        }
        
        
        
        ResourceModel modelResult = null;
        try {
            modelResult = changeToModel(beanResult, resourceType,includeList);
        } catch (Exception e) {
            
            LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(),e);
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                          e.getMessage());
        }
        return modelResult;
    }

    /*
     * 创建资源 (non-Javadoc)
     * @see nd.esp.service.lifecycle.educommon.services.NDResourceService#create(java.lang.String,
     * nd.esp.service.lifecycle.educommon.models.ResourceModel)
     */
    @Override
    public ResourceModel create(String resourceType, ResourceModel resourceModel) {
    	return create(resourceType, resourceModel,DbName.DEFAULT);
    }
    
    
    public ResourceModel create(String resourceType, ResourceModel resourceModel,DbName dbName){
    	//1、判断主键id是否已经存在
    	boolean flag = isDuplicateId(resourceType,resourceModel.getIdentifier());
    	if(flag){
    		throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.CheckDuplicateIdFail);
    	}
    	
    	//2、判断资源编码是否唯一
    	if(StringUtils.isNotEmpty(resourceModel.getNdresCode())){
        	boolean flagCode = isDuplicateCode(resourceType,resourceModel.getIdentifier(),resourceModel.getNdresCode());
        	if(flagCode){
        		throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.CheckDuplicateCodeFail);
        	}
    	}

    	
        // 3、基本属性的处理
        dealBasicInfo(resourceType, resourceModel, OperationType.CREATE, null);

        // 4、tech_info属性处理
        dealTechInfo(resourceType, resourceModel, OperationType.CREATE, dbName);

        // 5、coverages属性处理
        dealCoverages(resourceType, resourceModel, dbName);

        // 6、categories属性处理
        dealCategories(resourceType, resourceModel, OperationType.CREATE, dbName);

        // 7、relations属性处理
        dealRelations(resourceType, resourceModel,dbName);
        
        // 8、同步推送至报表系统
        nds.notifyReport4Resource(resourceType,resourceModel,OperationType.CREATE);
        return resourceModel;
    }
    
    /**
     * 判断主键ID是否重复
     * 
     * @author:xuzy
     * @date:2016年1月26日
     * @param resourceType
     * @param identifier
     * @return
     */
    private boolean isDuplicateId(String resourceType,String identifier){
    	boolean uploadable = commonServiceHelper.isUploadable(resourceType);
    	//有上传接口的资源id是由外部传进来的，需要做下id重复性判断
    	if(uploadable){
    		int num = 0;
    		if(!CommonServiceHelper.isQuestionDb(resourceType)){
    			num = ndResourceDao.queryCountByResId(resourceType, identifier);
    		}else{
    			num = ndResourceDao.queryCountByResId4QuestionDb(resourceType, identifier);
    		}
    		if(num > 0){
    			return true;
    		}
    	}
    	return false;
    }  
    
    /**
     * 判断资源编码是否重复
     * 
     * @author:xuzy
     * @date:2016年3月1日
     * @param resourceType
     * @param identifier
     * @return
     */
    private boolean isDuplicateCode(String resourceType,String identifier,String code){
    	int num;
		if(!CommonServiceHelper.isQuestionDb(resourceType)){
			num = ndResourceDao.queryCodeCountByResId(resourceType, identifier, code);
		}else{
			num = ndResourceDao.queryCodeCountByResId4QuestionDb(resourceType, identifier, code);
		}
		if(num > 0){
			return true;
		}
    	return false;
    }

    /**
     * 处理主表的内容（注意，不再维护冗余字段）
     * 
     * @author linsm
     * @param resourceType
     * @param resourceModel
     * @param update
     * @param oldEbook
     * @since
     */
    @SuppressWarnings("unchecked")
    private boolean dealBasicInfo(String resourceType,
                                  ResourceModel resourceModel,
                                  OperationType operationType,
                                  Education oldBean) {
        
        @SuppressWarnings("rawtypes")
        ResourceRepository resourceRepository =  commonServiceHelper.getRepository(resourceType);
        // 转换为数据模型
        // 所有通用接口支持的资源在sdk 都继承了Education
        Education education = changeModelToBean(resourceType, resourceModel);  //have set primary_category
        try {
            if (operationType == OperationType.CREATE) {
                // 默认值
                Timestamp ts = new Timestamp(System.currentTimeMillis());
                education.setCreateTime(ts);
                education.setLastUpdate(ts);
                education.setmIdentifier(education.getIdentifier());
                if(ResourceNdCode.knowledges.toString().equals(resourceType)){
                    //需要处理下树型结构
                    Chapter knowledge = (Chapter)education;
                    KnowledgeModel knowledgeModel = (KnowledgeModel) resourceModel;
                    if(StringUtils.isNotBlank(knowledgeModel.getExtProperties().getRootNode())){
                    	knowledge.setTeachingMaterial(knowledgeModel.getExtProperties().getRootNode());
                    }else{
                    	knowledge.setTeachingMaterial(getSubjectWithCheck(resourceModel));
                    }
                    TreeDirection treeDirection = TreeDirection.fromString(knowledgeModel.getExtProperties().getDirection());
                    TreeModel current = treeService.insertLeaf(getTreeTargetAndParent(knowledgeModel,knowledge), treeDirection);

                    // 3.转换为SDK的Knowledge
                    knowledge.setLeft(current.getLeft());
                    knowledge.setRight(current.getRight());
                    knowledge.setParent(current.getParent());
                }

                LOG.debug("调用sdk方法：add");
                LOG.debug("创建资源类型:{},uuid:{}", resourceType, education.getIdentifier());

                education = (Education) resourceRepository.add(education);

            } else if (operationType == OperationType.UPDATE) {
                
                if(ResourceNdCode.knowledges.toString().equals(resourceType)){
                    //树型结构不变
                    Chapter knowledge = (Chapter) education;
                    Chapter oldKnowledge = (Chapter) oldBean;
                    //left, right, teaching_material,parent
                    knowledge.setLeft(oldKnowledge.getLeft());
                    knowledge.setRight(oldKnowledge.getRight());
                    knowledge.setTeachingMaterial(oldKnowledge.getTeachingMaterial());
                    knowledge.setParent(oldKnowledge.getParent());
                }
                // 不可变的值特别处理
                education.setCreateTime(oldBean.getCreateTime());
                education.setmIdentifier(oldBean.getmIdentifier());
                
                ResLifeCycleModel lc = resourceModel.getLifeCycle();
                if(lc == null || StringUtils.isEmpty(lc.getCreator())){
                	education.setCreator(oldBean.getCreator());
                }
                
                education.setLastUpdate(new Timestamp(System.currentTimeMillis()));

                LOG.debug("调用sdk方法：update");
                LOG.debug("修改资源类型:{},uuid:{}", resourceType, education.getIdentifier());

                education = (Education) resourceRepository.update(education);
            }
        } catch (EspStoreException e) {

            LOG.error("创建操作出错了", e);

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                          e.getLocalizedMessage());
        }
        // 转换为业务模型
        // 只是将主表的内容转换出来
        updateBasicInModel(resourceModel, education);

        return true;

    }

    /**
     * @author linsm
     * @param knowledgeModel
     * @param knowledge
     * @return
     * @since 
     */
    @Override
    public TreeTrargetAndParentModel getTreeTargetAndParent(KnowledgeModel knowledgeModel, Chapter knowledge) {
        TreeTrargetAndParentModel model = new TreeTrargetAndParentModel();
        //target and parent
      String mid = knowledge.getTeachingMaterial();
      
      if(StringUtils.isNotEmpty(knowledgeModel.getExtProperties().getTarget())){
          Chapter targetChapter = getKnowledgeFromSdkWithCheck(knowledge.getPrimaryCategory(),mid,knowledgeModel.getExtProperties().getTarget());
          model.setTarget(new TreeModel(targetChapter));
      }else if(StringUtils.isNotEmpty(knowledgeModel.getExtProperties().getParent())){
          if(!knowledgeModel.getExtProperties().getParent().equals("ROOT")){
              Chapter parentKnowledge = getKnowledgeFromSdkWithCheck(knowledge.getPrimaryCategory(),mid,knowledgeModel.getExtProperties().getParent());
              model.setParent(new TreeModel(parentKnowledge));
          }else{
            //root 默认为mid
              
              int nodeNum = (int)chapterDao.countQueryChapterList(knowledge.getPrimaryCategory(),mid, null);
              //虚根
              TreeModel parent = new TreeModel();
              parent.setLeft(0);
              parent.setRight(nodeNum*2+1);
              parent.setRoot(mid);
              parent.setParent(null);
              parent.setIdentifier(mid);
              model.setParent(parent);
          }
      }else{
          throw new WafException(LifeCircleErrorMessageMapper.KnowledgeCheckParamFail.getCode(),
                               LifeCircleErrorMessageMapper.KnowledgeCheckParamFail.getMessage());
      }
        return model;
    }

    /**
     * @author linsm
     * @param primaryCategory
     * @param mid
     * @param target
     * @return
     * @since 
     */
    private Chapter getKnowledgeFromSdkWithCheck(String primaryCategory, String mid, String target) {
        Chapter chapter = chapterDao.getChapterFromSdk(primaryCategory, mid, target);
        if(chapter == null || chapter.getEnable()==null || !chapter.getEnable()){
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.KnowledgeNotFound);
        }
        return chapter;
    }

    /**
     * @author linsm
     * @param resourceModel
     * @return
     * @since 
     */
    private String getSubjectWithCheck(ResourceModel resourceModel) {
        List<ResClassificationModel> categoryList = resourceModel.getCategoryList();
        if (CollectionUtils.isNotEmpty(categoryList)) {
            for (ResClassificationModel resClassificationModel : categoryList) {
                String taxoncode = resClassificationModel.getTaxoncode();
                if (taxoncode.contains("$S")) {
                    return taxoncode;
                }
            }
        }
        throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.CheckParamValidFail.getCode(),"not existed subject");
    }

    /**
     * 更新存在于主表的信息到resourceModel
     * 
     * @author linsm
     * @param resourceModel
     * @param education
     * @since
     */
    private void updateBasicInModel(ResourceModel resourceModel, Education education) {

        // FIXME 怎么解决这个问题，
        ResourceModel keepResourceModel = new ResourceModel();
        keepResourceModel.setTechInfoList(resourceModel.getTechInfoList());
        keepResourceModel.setCoverages(resourceModel.getCoverages());
        keepResourceModel.setCategoryList(resourceModel.getCategoryList());
        keepResourceModel.setRelations(resourceModel.getRelations());

        // 基础属性
        try {
            BeanUtils.copyProperties(resourceModel, education);
        } catch (Exception e) {

            LOG.error(e.getMessage());

            throw new RuntimeException(e); // 这种错误，有没有必要使用lc code. (应该是调试阶段处理的)
        }
        // 生命周期属性
        ResLifeCycleModel resLifeCycleModel = BeanMapperUtils.beanMapper(education, ResLifeCycleModel.class);
        resourceModel.setLifeCycle(resLifeCycleModel);
        // 资源的教育属性
        ResEducationalModel resEducationalModel = BeanMapperUtils.beanMapper(education, ResEducationalModel.class);
        if (resEducationalModel != null) {
            resEducationalModel.setDescription(education.getEduDescription());
            //注意下这里与commonhelper不一致，但认为这个是对的。
            resEducationalModel.setLanguage(education.getEduLanguage());
        }
        resourceModel.setEducationInfo(resEducationalModel);
        // 版权属性
        ResRightModel resRightModel = BeanMapperUtils.beanMapper(education, ResRightModel.class);
        if (resRightModel != null) {
            resRightModel.setDescription(education.getCrDescription());
        }
        resourceModel.setCopyright(resRightModel);
        // 扩展属性：
        try {
            Field[] fs = resourceModel.getClass().getDeclaredFields();
            Field f = null;
            for (int i = 0; i < fs.length; i++) {
                if (fs[i].getName().equals("extProperties")) {
                    f = fs[i];
                    break;
                }
            }
            if (f != null) {
                Object o = null;
                // resourceModel-> education
                o = BeanMapperUtils.beanMapper(education, f.getType());
                Method m = resourceModel.getClass().getMethod("setExtProperties", f.getType());
                m.invoke(resourceModel, o);
            }
        } catch (Exception e) {

            LOG.warn("反射处理扩展属性出错！", e);

        }

        // 恢复
        resourceModel.setTechInfoList(keepResourceModel.getTechInfoList());
        resourceModel.setCoverages(keepResourceModel.getCoverages());
        resourceModel.setCategoryList(keepResourceModel.getCategoryList());
        resourceModel.setRelations(keepResourceModel.getRelations());

    }

    /**
     * @author linsm
     * @param resourceType
     * @param resourceModel
     * @return
     * @since
     */
    private Education changeModelToBean(String resourceType, ResourceModel resourceModel) {
        // 基础属性
        Education education = null;
        education = (Education) BeanMapperUtils.beanMapper(resourceModel,
                                                           commonServiceHelper.getBeanClass(resourceType));
        education.setPrimaryCategory(resourceType);
        // 生命周期属性
        if (resourceModel.getLifeCycle() != null) {
            //直接手动转换，个数是已知，目前对方法的控制力不够
            ResLifeCycleModel resLifeCycleModel = resourceModel.getLifeCycle();
            education.setVersion(resLifeCycleModel.getVersion());
            education.setStatus(resLifeCycleModel.getStatus());
            education.setEnable(resLifeCycleModel.isEnable());
            education.setCreator(resLifeCycleModel.getCreator());
            education.setPublisher(resLifeCycleModel.getPublisher());
            education.setProvider(resLifeCycleModel.getProvider());
            education.setProviderSource(resLifeCycleModel.getProviderSource());
            if (resLifeCycleModel.getCreateTime() != null) {
                education.setCreateTime(new Timestamp(resLifeCycleModel.getCreateTime().getTime()));
            }
            if (resLifeCycleModel.getLastUpdate() != null) {
                education.setLastUpdate(new Timestamp(resLifeCycleModel.getLastUpdate().getTime()));
            }
        }

        // 资源的教育属性
        if (resourceModel.getEducationInfo() != null) {
            ResEducationalModel resEducationalModel = resourceModel.getEducationInfo();
            education.setAgeRange(resEducationalModel.getAgeRange());
            education.setContext(resEducationalModel.getContext());
            education.setEduDescription(resEducationalModel.getDescription());
            education.setDifficulty(resEducationalModel.getDifficulty());
            education.setEndUserType(resEducationalModel.getEndUserType());
            education.setInteractivity(resEducationalModel.getInteractivity());
            education.setInteractivityLevel(resEducationalModel.getInteractivityLevel());
            education.setEduLanguage(resEducationalModel.getLanguage()); 
            education.setLearningTime(resEducationalModel.getLearningTime());
//            education.setSemanticDensity(resEducationalModel.getSemanticDensity().intValue());
            //xuzy提的
            if(resEducationalModel.getSemanticDensity() != null){
                education.setSemanticDensity(resEducationalModel.getSemanticDensity().intValue());
            }
            
        }
        // 版权属性
        if (resourceModel.getCopyright() != null) {
            ResRightModel resRightModel = resourceModel.getCopyright();
            education.setAuthor(resRightModel.getAuthor());
            education.setCrDescription(resRightModel.getDescription());
            education.setCrRight(resRightModel.getRight());
        }
        // 扩展属性：
        try {
            Field[] fs = resourceModel.getClass().getDeclaredFields();
            Field f = null;
            for (int i = 0; i < fs.length; i++) {
                if (fs[i].getName().equals("extProperties")) {
                    f = fs[i];
                    break;
                }
            }
            if (f != null) {
                Method m = resourceModel.getClass().getMethod("getExtProperties");
                Object object = m.invoke(resourceModel);
                //注意有可能会有属性值被重写。
                if(object !=null){
                    BeanUtils.copyProperties(education, object); 
                }
                
            }
        } catch (Exception e) {

            LOG.error("反射处理扩展属性出错！", e);

        }

        return education;
    }

    /**
     * @author linsm
     * @param resourceType
     * @param resourceModel
     * @since
     */
    private boolean dealCoverages(String resourceType, ResourceModel resourceModel,DbName dbName) {
        //model转换,获取CoverageModel列表
        List<CoverageModel> coverageModels = new ArrayList<CoverageModel>();
        List<ResCoverageModel> coverages = resourceModel.getCoverages();
        if (CollectionUtils.isNotEmpty(coverages)) {
            for (ResCoverageModel resCoverageModel : coverages) {
                CoverageModel coverageModel = new CoverageModel();
                coverageModel.setIdentifier(UUID.randomUUID().toString());
                coverageModel.setResource(resourceModel.getIdentifier());
                coverageModel.setResType(resourceType);
                coverageModel.setTargetType(resCoverageModel.getTargetType());
                coverageModel.setTarget(resCoverageModel.getTarget());
                coverageModel.setStrategy(resCoverageModel.getStrategy());
                coverageModel.setTargetTitle(resCoverageModel.getTargetTitle());
                coverageModels.add(coverageModel);
            }
        }
        
        if(CollectionUtils.isNotEmpty(coverageModels)){
        	if(dbName.equals(DbName.DEFAULT)){
        		coverageService.batchCreateCoverage(coverageModels, true);
        	}else{
        		coverageService4QuestionDB.batchCreateCoverage(coverageModels, true);
        	}
            
        }
        return true;

    }

    /*
     * (non-Javadoc)
     * @see nd.esp.service.lifecycle.educommon.services.NDResourceService#update(java.lang.String,
     * nd.esp.service.lifecycle.educommon.models.ResourceModel)
     */
    @Override
    public ResourceModel update(String resourceType, ResourceModel resourceModel) {
    	return update(resourceType, resourceModel,DbName.DEFAULT);
    }
    
    public ResourceModel update(String resourceType, ResourceModel resourceModel,DbName dbName){
        // 0、校验资源是否存在
        Education oldBean = checkResourceExist(resourceType, resourceModel.getIdentifier());
        
    	// 2、判断资源编码是否唯一
    	if(StringUtils.isNotEmpty(resourceModel.getNdresCode())){
        	boolean flagCode = isDuplicateCode(resourceType,resourceModel.getIdentifier(),resourceModel.getNdresCode());
        	if(flagCode){
        		throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.CheckDuplicateCodeFail);
        	}
    	}

        // 3、基本属性的处理
        dealBasicInfo(resourceType, resourceModel, OperationType.UPDATE, oldBean);

        //FIXME 当资源本身没有tech_info 时，不需要清空,这个信息由controller传入，还是在service(commonservicehelper中配制)
        // 4、tech_info属性处理
        // 优化，当不是更新时，不必清空
        dealTechInfo(resourceType, resourceModel, OperationType.UPDATE,dbName);

        // 5、categories属性处理
        // 优化，当不是更新时，不必清空
        dealCategories(resourceType, resourceModel, OperationType.UPDATE,dbName);
        
        // 6、同步推送至报表系统
        nds.notifyReport4Resource(resourceType,resourceModel,OperationType.UPDATE);
        return resourceModel;
    }

	@Override
	public void patch(String resourceType, ResourceModel resourceModel) {
		patch(resourceType, resourceModel,DbName.DEFAULT);
	}

	@Override
	public void patch(String resourceType, ResourceModel resourceModel, DbName dbName) {
		// 0、校验资源是否存在
		Education oldBean = checkResourceExist(resourceType, resourceModel.getIdentifier());

		// 1、判断资源编码是否唯一
		if(StringUtils.isNotEmpty(resourceModel.getNdresCode())){
			boolean flagCode = isDuplicateCode(resourceType,resourceModel.getIdentifier(),resourceModel.getNdresCode());
			if(flagCode){
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.CheckDuplicateCodeFail);
			}
		}

		List<String> includeList = new ArrayList<>();

		// 2、基本属性的处理
		dealBasicInfoPatch(resourceType, resourceModel, oldBean);

		// 3、categories属性处理
		if(resourceModel.getCategoryList()!=null && CollectionUtils.isNotEmpty(resourceModel.getCategoryList())) {
			dealCategoryPatch(resourceType, resourceModel);
			includeList.add(IncludesConstant.INCLUDE_CG);
		}

		// 4、tech_info属性处理
		if(resourceModel.getTechInfoList()!=null && CollectionUtils.isNotEmpty(resourceModel.getTechInfoList())){
			dealTechInfoPatch(resourceType, resourceModel, dbName);
			includeList.add(IncludesConstant.INCLUDE_TI);
		}

		// 5、同步推送至报表系统
		nds.notifyReport4Resource(resourceType,resourceModel,OperationType.UPDATE);

	}

	private boolean dealTechInfoPatch(String resourceType, ResourceModel resourceModel, DbName dbName) {
		ResourceRepository repository = commonServiceHelper.getTechInfoRepositoryByResType(resourceType);
		// resource id
		String uuid = resourceModel.getIdentifier();
		List<ResTechInfoModel> techInfoList = resourceModel.getTechInfoList();
		List<TechInfo> list = new ArrayList<TechInfo>();
		List<ResTechInfoModel> returnList = new ArrayList<ResTechInfoModel>();
		if (CollectionUtils.isNotEmpty(techInfoList)) {
			for (ResTechInfoModel rtim : techInfoList) {
				if(StringUtils.isEmpty(rtim.getOperation())) {
					rtim.setOperation("add");
				}
				TechInfo ti = new TechInfo();
				ti.setResource(uuid);
				ti.setResType(resourceType);
				ti.setTitle(rtim.getTitle());
				try {
					TechInfo target = (TechInfo)repository.getByExample(ti);
					if(target!=null) {
						if("update".equals(rtim.getOperation()) || "add".equals(rtim.getOperation())) {
							ti = BeanMapperUtils.beanMapper(rtim, TechInfo.class);
							ti.setResource(uuid);
							ti.setResType(resourceType);
							ti.setRequirements(ObjectUtils.toJson(rtim.getRequirements()));
							ti.setIdentifier(target.getIdentifier());
							list.add(ti);
						} else if("delete".equals(rtim.getOperation())) {
							repository.del(target.getIdentifier());
						}
					} else if ("add".equals(rtim.getOperation())) {
						ti = BeanMapperUtils.beanMapper(rtim, TechInfo.class);
						ti.setResource(uuid);
						ti.setResType(resourceType);
						ti.setRequirements(ObjectUtils.toJson(rtim.getRequirements()));
						ti.setIdentifier(UUID.randomUUID().toString());
						list.add(ti);
					}
				} catch (EspStoreException e) {
					LOG.error("技术属性创建操作出错了", e);

					throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
							LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
							e.getLocalizedMessage());
				}
			}
		}
		try {
			if (!list.isEmpty()) {

				LOG.debug("调用sdk方法：batchAdd");

				list = commonServiceHelper.getTechInfoRepositoryByResType(resourceType).batchAdd(list);
			}
			resourceModel.setTechInfoList(returnList);
		} catch (EspStoreException e) {

			LOG.error("技术属性创建操作出错了", e);

			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
					e.getLocalizedMessage());
		}
		return true;

	}

	private void dealCategoryPatch(String resourceType, ResourceModel resourceModel) {
		ResourceRepository repository = commonServiceHelper.getResourceCategoryRepositoryByResType(resourceType);
		String uuid = resourceModel.getIdentifier();
		List<ResClassificationModel> categories = resourceModel.getCategoryList();
		Set<ResClassificationModel> resClassificationModelSet = new HashSet<ResClassificationModel>(categories);
		List<ResourceCategory> resourceCategories = new ArrayList<ResourceCategory>();
		if (CollectionUtils.isNotEmpty(categories)) {
			Set<String> ndCodeSet = new HashSet<String>();
			List<CategoryData> categoryDatas = null;
			for (ResClassificationModel resClassificationModel : categories) {
				ndCodeSet.add(resClassificationModel.getTaxoncode());
			}

			LOG.debug("调用sdk方法：getListWhereInCondition");

			try {
				categoryDatas = categoryDataRepository.getListWhereInCondition("ndCode",
						new ArrayList<String>(ndCodeSet));
			} catch (EspStoreException e) {

				LOG.error("根据ndCode获取维度数据操作出错了", e);

				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
						LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
						e.getLocalizedMessage());
			}
			if (categoryDatas.size() != ndCodeSet.size()) {

				LOG.error(LifeCircleErrorMessageMapper.CheckNdCodeFail.getMessage() + ndCodeSet);

				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
						LifeCircleErrorMessageMapper.CheckNdCodeFail);
			}

			// 取 分类维度到shortName的映射
			Map<String, String> categoryNdCodeToShortNameMap = commonServiceHelper.getCategoryByData(categoryDatas);
			if (CollectionUtils.isEmpty(categoryDatas)) {
				return;
			}
			for (CategoryData cd : categoryDatas) {
				for (ResClassificationModel resClassificationModel : resClassificationModelSet) {
					if (resClassificationModel.getTaxoncode().equals(cd.getNdCode())) {
						// 通过取维度数据详情，补全resource_category中间表的数据
						resClassificationModel.setShortName(cd.getShortName());
						resClassificationModel.setTaxoncodeId(cd.getIdentifier());
						resClassificationModel.setTaxonname(cd.getTitle());
						resClassificationModel.setCategoryCode(cd.getNdCode().substring(0, 2));

						// 取维度的shortName
						if (categoryNdCodeToShortNameMap.get(resClassificationModel.getCategoryCode()) != null) {
							resClassificationModel.setCategoryName(categoryNdCodeToShortNameMap.get(resClassificationModel.getCategoryCode()));
						}

						// 转换到sdk bean
						ResourceCategory resourceCategory = BeanMapperUtils.beanMapper(resClassificationModel,
								ResourceCategory.class);
						resourceCategory.setResource(uuid);
						//资源分类维度
						resourceCategory.setPrimaryCategory(resourceType);

						if(StringUtils.isEmpty(resClassificationModel.getOperation())) {
							resClassificationModel.setOperation("add");
						}

						switch (resClassificationModel.getOperation()) {
							case "add":
								resourceCategory.setIdentifier(UUID.randomUUID().toString());
								resourceCategories.add(resourceCategory);
								break;
							case "update":
								if (resourceCategory.getIdentifier() != null) {
									resourceCategories.add(resourceCategory);
								}
								break;
							case "delete":
								try {
									if (resourceCategory.getIdentifier() != null) {
										repository.del(resourceCategory.getIdentifier());
									} else {
										ResourceCategory bean = (ResourceCategory) repository.getByExample(resourceCategory);
										if (null != bean) {
											repository.del(bean.getIdentifier());
										}
									}
								} catch (EspStoreException e) {
									LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(), e);

									throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
											LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
											e.getLocalizedMessage());
								}
								break;
						}
					}
				}
			}

			if (!resourceCategories.isEmpty()) {
				try {
					LOG.debug("调用sdk方法：batchAdd");

					resourceCategories = repository.batchAdd(resourceCategories);

				} catch (EspStoreException e) {

					LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(), e);

					throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
							LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
							e.getLocalizedMessage());
				}
			}
		}
	}

	private void dealBasicInfoPatch(String resourceType, ResourceModel resourceModel, Education oldBean) {
		@SuppressWarnings("rawtypes")
		ResourceRepository resourceRepository =  commonServiceHelper.getRepository(resourceType);
		// 转换为数据模型
		// 所有通用接口支持的资源在sdk 都继承了Education
		Education education = changeModelToBean(resourceType, resourceModel);  //have set primary_category

		Education initEdu = BeanMapperUtils.beanMapper(new ResourceViewModel().getLifeCycle(), Education.class);

		Set<String> ignoreSet = Sets.newHashSet("serialVersionUID", "PROP_CREATETIME", "PROP_LASTUPDATE", "PROP_CATEGORYS", "PROP_RELATIONS", "PROP_TAGS", "PROP_KEYWORDS", "mIdentifier",
				"enable", "createTime", "dbcreateTime", "primaryCategory");

		try {
			Field[] fs = Education.class.getDeclaredFields();

			for (int i = 0; i < fs.length; i++) {
				String name = fs[i].getName();
				if (!ignoreSet.contains(name)) {
					String getterName = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
					Method m = Education.class.getMethod(getterName);
					Object o = m.invoke(education);
					Object initValue = m.invoke(initEdu);
					if (o != null && !o.equals(initValue)) {
						if(!"creator".equals(name) || StringUtils.isNotEmpty((String)o)) {
							String setterName = "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
							m = Education.class.getMethod(setterName, fs[i].getType());
							m.invoke(oldBean, o);
						}
					}
				}
			}
		} catch (Exception e) {

			LOG.warn("反射处理扩展属性出错！", e);

		}
		if(StringUtils.isNotEmpty(education.getTitle())) {
			oldBean.setTitle(education.getTitle());
		}
		if(StringUtils.isNotEmpty(education.getDescription())) {
			oldBean.setDescription(education.getDescription());
		}

		oldBean.setLastUpdate(new Timestamp(System.currentTimeMillis()));
		try {
			education = (Education) resourceRepository.update(oldBean);
		} catch (EspStoreException e) {

			LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(),e);

			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
					e.getMessage());
		}
	}

	/**
     * @author linsm
     * @param resourceType
     * @param resourceModel
     * @param operationType
     * @since
     */
    private boolean dealCategories(String resourceType, ResourceModel resourceModel, OperationType operationType,DbName dbName) {
        // 后期应该使用上resourceType
        // rsource uuid
        String uuid = resourceModel.getIdentifier();
        List<ResClassificationModel> categories = resourceModel.getCategoryList();
        Set<ResClassificationModel> resClassificationModelSet = new HashSet<ResClassificationModel>(categories);
        List<ResourceCategory> resourceCategories = new ArrayList<ResourceCategory>();
        if (CollectionUtils.isNotEmpty(categories)) {
            Set<String> ndCodeSet = new HashSet<String>();
            List<CategoryData> categoryDatas = null;
            for (ResClassificationModel resClassificationModel : categories) {
                ndCodeSet.add(resClassificationModel.getTaxoncode());
            }

            LOG.debug("调用sdk方法：getListWhereInCondition");

            try {
                categoryDatas = categoryDataRepository.getListWhereInCondition("ndCode",
                                                                               new ArrayList<String>(ndCodeSet));
            } catch (EspStoreException e) {

                LOG.error("根据ndCode获取维度数据操作出错了", e);

                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                              e.getLocalizedMessage());
            }
            if (categoryDatas.size() != ndCodeSet.size()) {

                LOG.error(LifeCircleErrorMessageMapper.CheckNdCodeFail.getMessage() + ndCodeSet);

                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              LifeCircleErrorMessageMapper.CheckNdCodeFail);
            }

            // 取 分类维度到shortName的映射
            Map<String, String> categoryNdCodeToShortNameMap = commonServiceHelper.getCategoryByData(categoryDatas);
            if (CollectionUtils.isEmpty(categoryDatas)) {
                return false;
            }
            for (CategoryData cd : categoryDatas) {
                for (ResClassificationModel resClassificationModel : resClassificationModelSet) {
                    if (resClassificationModel.getTaxoncode().equals(cd.getNdCode())) {
                        // 通过取维度数据详情，补全resource_category中间表的数据
                        resClassificationModel.setShortName(cd.getShortName());
                        resClassificationModel.setTaxoncodeId(cd.getIdentifier());
                        resClassificationModel.setTaxonname(cd.getTitle());
                        resClassificationModel.setCategoryCode(cd.getNdCode().substring(0, 2));

                        // 取维度的shortName
                        if (categoryNdCodeToShortNameMap.get(resClassificationModel.getCategoryCode()) != null) {
                            resClassificationModel.setCategoryName(categoryNdCodeToShortNameMap.get(resClassificationModel.getCategoryCode()));
                        }

                        // 转换到sdk bean
                        ResourceCategory resourceCategory = BeanMapperUtils.beanMapper(resClassificationModel,
                                                                                       ResourceCategory.class);
                        resourceCategory.setResource(uuid);
                        resourceCategory.setIdentifier(UUID.randomUUID().toString());
                        
                        //资源分类维度
                        resourceCategory.setPrimaryCategory(resourceType);
                        
                        resourceCategories.add(resourceCategory);
                    }
                }
            }

            if (!resourceCategories.isEmpty()) {
                try {
                    // 先将关联关系数据删除
                    // 同tech_info
                    if (OperationType.UPDATE == operationType) {
                        commonDao.deleteResourceCategoryByResource(resourceType,uuid,dbName);
                    }

                    LOG.debug("调用sdk方法：batchAdd");

                    resourceCategories = commonServiceHelper.getResourceCategoryRepositoryByResType(resourceType).batchAdd(resourceCategories);
                    // 返回值转换为model
                    if (CollectionUtils.isNotEmpty(resourceCategories)) {
                        categories = new ArrayList<ResClassificationModel>();
                        for (ResourceCategory resourceCategory : resourceCategories) {
                            ResClassificationModel model = BeanMapperUtils.beanMapper(resourceCategory,
                                                                                      ResClassificationModel.class);
                            categories.add(model);
                        }
                        resourceModel.setCategoryList(categories);
                    }
                } catch (EspStoreException e) {

                    LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(), e);

                    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                  LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                                  e.getLocalizedMessage());
                }
            }
        }
        return true;

    }

    /**
     * @author linsm
     * @param resourceType
     * @param resourceModel
     * @since
     */
    private boolean dealRelations(String resourceType, ResourceModel resourceModel,DbName dbName) {
        //model转换,获取EducationRelationModel列表
        List<EducationRelationModel> educationRelationModels = new ArrayList<EducationRelationModel>();
        List<ResRelationModel> relationModels = resourceModel.getRelations();
        if (CollectionUtils.isNotEmpty(relationModels)) {
            for (ResRelationModel relationModel : relationModels) {
                EducationRelationModel erm = new EducationRelationModel();
                erm.setTarget(resourceModel.getIdentifier());
                erm.setResourceTargetType(resourceType);
                erm.setResType(relationModel.getSourceType());
                erm.setSource(relationModel.getSource());
                erm.setRelationType(relationModel.getRelationType());
                if(StringUtils.isEmpty(relationModel.getLabel())){
                    relationModel.setLabel(null);
                }
                erm.setLabel(relationModel.getLabel());
                erm.setOrderNum(relationModel.getOrderNum());
                erm.setTags(relationModel.getTags());
                
                EducationRelationLifeCycleModel erlc = new EducationRelationLifeCycleModel();
                ResLifeCycleModel lc = resourceModel.getLifeCycle();
                if(lc != null){
                	erlc.setCreator(lc.getCreator());
                	if(lc.getCreateTime() != null){
                		erm.setTargetCT(new BigDecimal(lc.getCreateTime().getTime()));
                	}
                }
                erlc.setStatus("AUDIT_WAITING");
                erm.setLifeCycle(erlc);
                relationModel.setIdentifier(UUID.randomUUID().toString());
                relationModel.setTargetType(resourceType);
                relationModel.setTarget(resourceModel.getIdentifier());
                erm.setIdentifier(relationModel.getIdentifier());
                
                educationRelationModels.add(erm);
            }
            Set<ResRelationModel> resRelationModelSet = new HashSet<ResRelationModel>(relationModels);
            if (resRelationModelSet.size() != relationModels.size()) {
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              LifeCircleErrorMessageMapper.RelationDuplication.getCode(),
                                              LifeCircleErrorMessageMapper.RelationDuplication.getMessage());
            }
        }
        
        if(CollectionUtils.isNotEmpty(educationRelationModels)){
        	//分离出所有resType为习题的关系
        	List<EducationRelationModel> questionList = new ArrayList<EducationRelationModel>();
        	Iterator<EducationRelationModel> it = educationRelationModels.iterator();
        	while (it.hasNext()) {
        		EducationRelationModel erm = it.next();
        		if(CommonServiceHelper.isQuestionDb(erm.getResType())){
        			questionList.add(erm);
        			it.remove();
        		}
			}
        	if(CollectionUtils.isNotEmpty(questionList)){
        		educationRelationService4QuestionDB.createRelation(questionList, true);
        	}
        	if(CollectionUtils.isNotEmpty(educationRelationModels)){
        		educationRelationService.createRelation(educationRelationModels, true);
        	}
        }
        
        return true;
    }

    /**
     * @author linsm
     * @param resourceType
     * @param resourceModel
     * @param operationType
     * @since
     */
    private boolean dealTechInfo(String resourceType, ResourceModel resourceModel, OperationType operationType,DbName dbName) {
        // resource id
        String uuid = resourceModel.getIdentifier();
        List<ResTechInfoModel> techInfoList = resourceModel.getTechInfoList();
        List<TechInfo> list = new ArrayList<TechInfo>();
        List<ResTechInfoModel> returnList = new ArrayList<ResTechInfoModel>();
        if (CollectionUtils.isNotEmpty(techInfoList)) {
            for (ResTechInfoModel rtim : techInfoList) {
                TechInfo ti = BeanMapperUtils.beanMapper(rtim, TechInfo.class);
                ti.setIdentifier(UUID.randomUUID().toString());
                ti.setResource(uuid);
                ti.setResType(resourceType);
                ti.setRequirements(ObjectUtils.toJson(rtim.getRequirements()));
                list.add(ti);
            }
        }
        try {
            // 先清空旧数据
            // 应该也需要resourceType (xuzy)
            // 但要求外界创建时，id必须不一样。
            if (OperationType.UPDATE == operationType) {
                commonDao.deleteTechInfoByResource(resourceType,uuid,dbName);
            }

            if (!list.isEmpty()) {

                LOG.debug("调用sdk方法：batchAdd");

                list = commonServiceHelper.getTechInfoRepositoryByResType(resourceType).batchAdd(list);
                for (TechInfo techInfo : list) {
                    // 数据回填
                    ResTechInfoModel rtim = BeanMapperUtils.beanMapper(techInfo, ResTechInfoModel.class);
                    // requirements单独处理
                    List<TechnologyRequirementModel> req = ObjectUtils.fromJson(techInfo.getRequirements(),
                                                                                new TypeToken<List<TechnologyRequirementModel>>() {
                                                                                });
                    rtim.setRequirements(req);
                    returnList.add(rtim);
                }

            }
            resourceModel.setTechInfoList(returnList);
        } catch (EspStoreException e) {

            LOG.error("技术属性创建操作出错了", e);

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                          e.getLocalizedMessage());
        }
        return true;

    }

    /**
     * @author linsm
     * @param resourceType
     * @param identifier
     * @return
     * @since
     */
    private Education checkResourceExist(String resourceType, String identifier) {
        // 后期是否可以结合取详情一起来处理
        // 判断资源是否存在
        Education oldBean = null;

        LOG.debug("调用sdk方法：get");

        try {
            oldBean = (Education) commonServiceHelper.getRepository(resourceType).get(identifier);
        } catch (EspStoreException e) {

            LOG.error("调用存储SDK出错了", e);

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                          e.getLocalizedMessage());
        }
        if (oldBean == null || !resourceType.equals(oldBean.getPrimaryCategory())) {

            LOG.error(LifeCircleErrorMessageMapper.ChangeObjectNotExist.getMessage() + identifier);

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.ChangeObjectNotExist);
        }
        // 资源是否被删除过
        if (!oldBean.getEnable()) {

            LOG.error(LifeCircleErrorMessageMapper.ChangeObjectNotExist.getMessage() + identifier);

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.ChangeObjectNotExist);
        }
        return oldBean;
    }

	@Override
    @Transactional(value="transactionManager")
	public ResourceViewModel createNewVersion(String resType,String uuid,VersionViewModel vvm,UserInfo userInfo) {
		ResourceModel rm = getDetail(resType, uuid, Arrays.asList(IncludesConstant.INCLUDE_LC,IncludesConstant.INCLUDE_EDU,IncludesConstant.INCLUDE_CR));
		//1、判断是否可以创建版本
		if(rm.getmIdentifier() != null && !rm.getIdentifier().equals(rm.getmIdentifier())){
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,"LC/CREATE_NEW_VERSION_ERROR","已是版本分支，不允许创建新版本");
		}
		
		//2、判断版本号是否重复
		String version = vvm.getLifeCycle().getVersion();
		List<Map<String,Object>> list = ndResourceDao.queryResourceByMid(resType, uuid);
		if(CollectionUtils.isNotEmpty(list)){
			for (Map<String, Object> map : list) {
				String v = (String)map.get("version");
				if(version.equals(v)){
					throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,"LC/CREATE_NEW_VERSION_ERROR","已存在此版本号");
				}
			}
		}
		
		//3、创建新资源
		long time = System.currentTimeMillis();
		Timestamp ts = new Timestamp(time);
		ResourceRepository resourceRepository =  commonServiceHelper.getRepository(resType);
		Education education = changeModelToBean(resType, rm);
		String newUuid = UUID.randomUUID().toString();
		education.setIdentifier(newUuid);
		education.setmIdentifier(uuid);
		education.setVersion(version);
		education.setStatus(vvm.getLifeCycle().getStatus());
		education.setCreator(vvm.getLifeCycle().getCreator());
		education.setCreateTime(ts);
		education.setLastUpdate(ts);
		
		try {
			education = (Education) resourceRepository.add(education);
		} catch (EspStoreException e) {
			e.printStackTrace();
		}
		
		//4、建立资源关系
		List<EducationRelationModel> educationRelationModels = new ArrayList<EducationRelationModel>();
		EducationRelationModel erm = new EducationRelationModel();
		erm.setResType(resType);
		erm.setResourceTargetType(resType);
		erm.setSource(uuid);
		erm.setTarget(newUuid);
		erm.setRelationType(RelationType.VERSION.getName());
		erm.setTags(vvm.getRelations().get("tags"));
		educationRelationModels.add(erm);
		educationRelationService.createRelation(educationRelationModels, true);
		
		//5、创建资源生命周期
        LifecycleServiceV06 service = CommonServiceHelper.isQuestionDb(resType) ? lifecycleService4Qti : lifecycleService;
        ResContributeModel contributeModel = new ResContributeModel();
        contributeModel.setLifecycleStatus(vvm.getLifeCycle().getStatus());
        contributeModel.setMessage("新版本创建，来源id:"+uuid);
        contributeModel.setTitle("version");
        if(userInfo != null){
        	try {
        		contributeModel.setTargetType(userInfo.getUserType());
            	contributeModel.setTargetId(userInfo.getUserId());
            	contributeModel.setTargetName(userInfo.getUserName());
            	
            	List<UserCenterRoleDetails> userRoleList = userInfo.getUserRoles();
            	if(CollectionUtils.isNotEmpty(userRoleList)){
            		UserCenterRoleDetails ur = userRoleList.get(0);
            		contributeModel.setRoleId(ur.getRoleId());
            		contributeModel.setRoleName(ur.getRoleName());
            	}
			} catch (Exception e) {
				LOG.error("获取UC用户信息出错",e);
			}
        	
        }
        service.addLifecycleStep(resType, newUuid, contributeModel);
        ResourceModel resourceModel = new ResourceModel();
        updateBasicInModel(resourceModel,education);
        String type = "res_type";
        if(IndexSourceType.AssetType.getName().equals(resType)){
        	type = "assets_type";
        }
        ResourceViewModel resourceViewModel = CommonHelper.convertViewModelOut(resourceModel,ResourceViewModel.class,type);
		return resourceViewModel;
	}
	
	@Override
	@Transactional(value="questionTransactionManager")
	public ResourceViewModel createNewVersion4Question(String resType,String uuid,VersionViewModel vvm,UserInfo userInfo) {
		ResourceModel rm = getDetail(resType, uuid, Arrays.asList(IncludesConstant.INCLUDE_LC,IncludesConstant.INCLUDE_EDU,IncludesConstant.INCLUDE_CR));
		//1、判断是否可以创建版本
		if(rm.getmIdentifier() != null && !rm.getIdentifier().equals(rm.getmIdentifier())){
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,"LC/CREATE_NEW_VERSION_ERROR","已是版本分支，不允许创建新版本");
		}
		
		//2、判断版本号是否重复
		String version = vvm.getLifeCycle().getVersion();
		List<Map<String,Object>> list = ndResourceDao.queryResourceByMid(resType, uuid);
		if(CollectionUtils.isNotEmpty(list)){
			for (Map<String, Object> map : list) {
				String v = (String)map.get("version");
				if(version.equals(v)){
					throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,"LC/CREATE_NEW_VERSION_ERROR","已存在此版本号");
				}
			}
		}
		
		//3、创建新资源
		long time = System.currentTimeMillis();
		Timestamp ts = new Timestamp(time);
		ResourceRepository resourceRepository =  commonServiceHelper.getRepository(resType);
		Education education = changeModelToBean(resType, rm);
		String newUuid = UUID.randomUUID().toString();
		education.setIdentifier(newUuid);
		education.setmIdentifier(uuid);
		education.setVersion(version);
		education.setStatus(vvm.getLifeCycle().getStatus());
		education.setCreator(vvm.getLifeCycle().getCreator());
		education.setCreateTime(ts);
		education.setLastUpdate(ts);
		
		try {
			education = (Education) resourceRepository.add(education);
		} catch (EspStoreException e) {
			e.printStackTrace();
		}
		
		//4、建立资源关系
		List<EducationRelationModel> educationRelationModels = new ArrayList<EducationRelationModel>();
		EducationRelationModel erm = new EducationRelationModel();
		erm.setResType(resType);
		erm.setResourceTargetType(resType);
		erm.setSource(uuid);
		erm.setTarget(newUuid);
		erm.setRelationType(RelationType.VERSION.getName());
		erm.setTags(vvm.getRelations().get("tags"));
		educationRelationModels.add(erm);
		educationRelationService4QuestionDB.createRelation(educationRelationModels, true);
		
		//5、创建资源生命周期
        LifecycleServiceV06 service = CommonServiceHelper.isQuestionDb(resType) ? lifecycleService4Qti : lifecycleService;
        ResContributeModel contributeModel = new ResContributeModel();
        contributeModel.setLifecycleStatus(vvm.getLifeCycle().getStatus());
        contributeModel.setMessage("新版本创建，来源id:"+uuid);
        contributeModel.setTitle("version");
        if(userInfo != null){
        	try {
        		contributeModel.setTargetType(userInfo.getUserType());
            	contributeModel.setTargetId(userInfo.getUserId());
            	contributeModel.setTargetName(userInfo.getUserName());
            	
            	List<UserCenterRoleDetails> userRoleList = userInfo.getUserRoles();
            	if(CollectionUtils.isNotEmpty(userRoleList)){
            		UserCenterRoleDetails ur = userRoleList.get(0);
            		contributeModel.setRoleId(ur.getRoleId());
            		contributeModel.setRoleName(ur.getRoleName());
            	}
			} catch (Exception e) {
				LOG.error("获取UC用户信息出错",e);
			}
        	
        }
        service.addLifecycleStep(resType, newUuid, contributeModel);
        ResourceModel resourceModel = new ResourceModel();
        updateBasicInModel(resourceModel,education);
        String type = "res_type";
        if(IndexSourceType.AssetType.getName().equals(resType)){
        	type = "assets_type";
        }
        ResourceViewModel resourceViewModel = CommonHelper.convertViewModelOut(resourceModel,ResourceViewModel.class,type);
		return resourceViewModel;
	}
	
	@Override
	public Map<String, Map<String, Object>> versionCheck(String resType,String uuid){
		ResourceModel rm = getDetail(resType, uuid, Arrays.asList(IncludesConstant.INCLUDE_LC));
		String mid = rm.getmIdentifier();
		if(mid == null){
			mid = rm.getIdentifier();
		}
		List<Map<String,Object>> list = ndResourceDao.queryResourceByMid(resType, mid);
		Map<String, Map<String, Object>> returnMap = new HashMap<String, Map<String,Object>>();
		
		if(CollectionUtils.isNotEmpty(list)){
			for (Map<String, Object> map : list) {
				String version = (String)map.get("version");
				String identifier = (String)map.get("identifier");
				Long createTime = (Long)map.get("create_time");
				String content = (String)map.get("message");
				Map<String,Object> m = new HashMap<String, Object>();
				m.put("identifier", identifier);
				m.put("createTime", new Date(createTime));
				m.put("content", content);
				if(StringUtils.isEmpty(content)){
					m.put("content", "");
				}
				
				returnMap.put(version, m);
			}
		}
		return returnMap;
	}
	
	public Map<String, Object> versionRelease(String resType,String uuid,Map<String,String> paramMap){
		List<String> sqls = new ArrayList<String>();
		List<String> ids = new ArrayList<String>();
		
		ResourceModel rm = getDetail(resType, uuid, Arrays.asList(IncludesConstant.INCLUDE_LC));
		String mid = rm.getmIdentifier();
		List<Map<String,Object>> list = ndResourceDao.queryResourceByMid(resType, mid);
		if(CollectionUtils.isNotEmpty(list)){
			for (Map<String, Object> map : list) {
				String identifier = (String)map.get("identifier");
				String status = (String)map.get("estatus");
				if(identifier.equals(uuid)){
					if(!"ONLINE".equals(status)){
						String s1 = updateStatusSql(resType, uuid, "ONLINE");
						sqls.add(s1);
						ids.add(identifier);
					}
					continue;
				}
				
				if(paramMap.containsKey(identifier)){
					if(status != null && !status.equals(paramMap.get(identifier))){
						String s = updateStatusSql(resType, identifier, paramMap.get(identifier));
						sqls.add(s);
						ids.add(identifier);
					}
				}else{
					if(status != null && status.equals("ONLINE")){
						String s = updateStatusSql(resType, identifier, "OFFLINE");
						sqls.add(s);
						ids.add(identifier);
					}
				}
			}
		}
		if(CollectionUtils.isNotEmpty(sqls)){
			String[] a = new String[]{};
			ndResourceDao.batchUpdateSql(resType,sqls.toArray(a));
		}
		
		//通知ES和同步离线文件
		if(ResourceTypeSupport.isValidEsResourceType(resType) && CollectionUtils.isNotEmpty(ids)){
			for (String id : ids) {
				esResourceOperation.asynAdd(new Resource(resType, id));
				offlineService.writeToCsAsync(resType, id);
			}
		}
		
		List<Map<String,Object>> list2 = ndResourceDao.queryResourceByMid(resType, mid);
		Map<String,Object> returnMap = new HashMap<String, Object>();
		if(CollectionUtils.isNotEmpty(list2)){
			for (Map<String, Object> map : list2) {
				returnMap.put((String)map.get("identifier"), (String)map.get("estatus"));
			}
			return returnMap;
		}
		return null;
	}
	
	private String updateStatusSql(String resType,String uuid,String status){
		return "update ndresource set estatus='"+status+"',last_update="+System.currentTimeMillis()+" where primary_category='"+resType+"' and identifier = '"+uuid+"' and enable = 1";
	}

	@Override
	public Map<String, ChapterStatisticsViewModel> statisticsCountsByChapters(
			String resType, String tmId, Set<String> chapterIds,
			List<String> coverages, Set<String> categories, boolean isAll) {
		
		return ndResourceDao.statisticsCountsByChapters(resType, tmId, chapterIds, coverages, categories, isAll);
	}
}
