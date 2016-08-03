package nd.esp.service.lifecycle.educommon.dao.impl;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import nd.esp.service.lifecycle.app.LifeCircleApplicationInitializer;
import nd.esp.service.lifecycle.educommon.dao.NDResourceDao;
import nd.esp.service.lifecycle.educommon.models.ResClassificationModel;
import nd.esp.service.lifecycle.educommon.models.ResEducationalModel;
import nd.esp.service.lifecycle.educommon.models.ResLifeCycleModel;
import nd.esp.service.lifecycle.educommon.models.ResRightModel;
import nd.esp.service.lifecycle.educommon.models.ResTechInfoModel;
import nd.esp.service.lifecycle.educommon.models.ResourceModel;
import nd.esp.service.lifecycle.educommon.models.TechnologyRequirementModel;
import nd.esp.service.lifecycle.educommon.services.impl.CommonServiceHelper;
import nd.esp.service.lifecycle.educommon.vos.ChapterStatisticsViewModel;
import nd.esp.service.lifecycle.educommon.vos.constant.IncludesConstant;
import nd.esp.service.lifecycle.models.QueryResultModel;
import nd.esp.service.lifecycle.models.teachingmaterial.v06.TeachingMaterialModel;
import nd.esp.service.lifecycle.models.teachingmaterial.v06.TmExtPropertiesModel;
import nd.esp.service.lifecycle.models.v06.EbookExtPropertiesModel;
import nd.esp.service.lifecycle.models.v06.EbookModel;
import nd.esp.service.lifecycle.models.v06.KnowledgeExtPropertiesModel;
import nd.esp.service.lifecycle.models.v06.KnowledgeModel;
import nd.esp.service.lifecycle.models.v06.QuestionExtPropertyModel;
import nd.esp.service.lifecycle.models.v06.QuestionModel;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.Chapter;
import nd.esp.service.lifecycle.repository.model.Ebook;
import nd.esp.service.lifecycle.repository.model.FullModel;
import nd.esp.service.lifecycle.repository.model.Question;
import nd.esp.service.lifecycle.repository.model.ResourceCategory;
import nd.esp.service.lifecycle.repository.model.TeachingMaterial;
import nd.esp.service.lifecycle.repository.model.TechInfo;
import nd.esp.service.lifecycle.repository.sdk.ChapterRepository;
import nd.esp.service.lifecycle.repository.sdk.EbookRepository;
import nd.esp.service.lifecycle.repository.sdk.QuestionRepository;
import nd.esp.service.lifecycle.repository.sdk.ResourceRelationRepository;
import nd.esp.service.lifecycle.repository.sdk.TeachingMaterialRepository;
import nd.esp.service.lifecycle.support.Constant;
import nd.esp.service.lifecycle.support.DbName;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.StaticDatas;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.EduRedisTemplate;
import nd.esp.service.lifecycle.utils.ParamCheckUtil;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;
import nd.esp.service.lifecycle.vos.statics.CoverageConstant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.google.gson.reflect.TypeToken;

@Repository
public class NDResourceDaoImpl implements NDResourceDao{
	private static final Logger LOG = LoggerFactory.getLogger(NDResourceDaoImpl.class);
	//redis数目后缀
	private static String REDIS_NUM_SUFFIX = "num";
	//redis结果集后缀
	private static String REDIS_RESULT_SUFFIX = "set";
	//redis资源统计后缀
	private static String REDIS_STATISTICS_SUFFIX = "statistics";
	
	private final static ExecutorService executorService = CommonHelper.getPrimaryExecutorService();
    private static ConcurrentMap<String, String> threadNameMap = new ConcurrentHashMap<String, String>();
    
    //使用IN 或者 EXISTS 的临界值
    private final static int CRITICAL_VALUE = 800;
    
    @Autowired
    private ResourceRelationRepository repository;
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private TeachingMaterialRepository teachingMaterialRepository;
    @Autowired
    private EbookRepository ebookRepository;
    @Autowired
    private ChapterRepository chapterRepository;
    
    @Autowired
    private EduRedisTemplate<FullModel> ert;
    @Autowired
    private EduRedisTemplate<QueryResultModel> ertCount;
    @Autowired
    private EduRedisTemplate<Map<String,Integer>> ertStatistics;
    
	@PersistenceContext(unitName="entityManagerFactory")
	EntityManager defaultEm;
	@PersistenceContext(unitName="questionEntityManagerFactory")
	EntityManager questionEm;
	
	@Qualifier(value="defaultJdbcTemplate")
	@Autowired
	private JdbcTemplate defaultJdbcTemplate;
	@Qualifier(value="questionJdbcTemplate")
	@Autowired
	private JdbcTemplate questionJdbcTemplate;
	
    @Override
    public List<ResourceModel> commomQueryByDB(final String resType, String resCodes, final List<String> includes,
            Set<String> categories, Set<String> categoryExclude, List<Map<String, String>> relations, List<String> coverages,
            Map<String, Set<String>> propsMap, Map<String, String> orderMap, String words, String limit,
            boolean isNotManagement, boolean reverse, boolean useIn, Boolean printable, String printableKey,
            String statisticsType,String statisticsPlatform,boolean forceStatus,List<String> tags,boolean showVersion) {
        //需要用到的变量
        List<String> querySqls = new ArrayList<String>();
        Map<String,Object> params = new HashMap<String, Object>();
        
        //获取querySqls和params,并把resTypes和onlyOneType返回
        Map<String, Object> map = this.getQuerySqlsAndParams(false, resType, resCodes, categories, categoryExclude, relations, coverages, propsMap, words, isNotManagement, reverse, useIn,
        		printable, printableKey, orderMap,statisticsType, statisticsPlatform,forceStatus,tags,showVersion,querySqls, params);
        @SuppressWarnings("unchecked")
		List<String> resTypes = (List<String>)map.get("resTypes");
        boolean onlyOneType = (boolean)map.get("onlyOneType");
        
        //判断是否是查询习题库
        final DbName dbName = getDBName4CommonQuery(onlyOneType, resTypes);
        
        //sql的ORDER BY
        String sqlOrderBy = "ORDER BY ndr.create_time DESC";//默认的排序方式
        boolean haveSizeSort = false;
        boolean haveSumSort = false;
        boolean haveSortNum = false;
        boolean haveVipLevel = false;
        boolean haveTopSort = false;
        boolean haveScoreSort = false;
        boolean haveVoteSort = false;
        boolean haveViewSort = false;
        if(CollectionUtils.isNotEmpty(orderMap)){
            List<String> ordersql = new ArrayList<String>();
            
            for(String key : orderMap.keySet()){
                if(key.equals("size")){
                	if(!showVersion){
                		haveSizeSort = true;
                        ordersql.add("ti." + key + " " + orderMap.get(key));
                	}
                }else if(key.equals("key_value")){
                	if(!showVersion){
                		haveSumSort = true;
                        ordersql.add("rs." + key + " " + orderMap.get(key));
                	}
                }else if(key.equals("top")){
                	if(!showVersion){
                		haveTopSort = true;
                        ordersql.add("rs1.key_value " + orderMap.get(key));
                	}
                }else if(key.equals("scores")){
                	if(!showVersion){
                		haveScoreSort = true;
                		ordersql.add("rs2.key_value " + orderMap.get(key));
                	}
                }else if(key.equals("votes")){
                	if(!showVersion){
                		haveVoteSort = true;
                		ordersql.add("rs3.key_value " + orderMap.get(key));
                	}
                }else if(key.equals("views")){
                	if(!showVersion){
                		haveViewSort = true;
                		ordersql.add("rs4.key_value " + orderMap.get(key));
                	}
                }else if(key.equals("sort_num")){
                	if(CollectionUtils.isNotEmpty(relations) && relations.size()==1 
                			&& dbName.equals(DbName.DEFAULT) && !reverse){
                		if(!showVersion){
                			haveSortNum = true;
                            ordersql.add("rer." + key + " " + orderMap.get(key));
                		}
                	}
                }else if(key.equals("taxOnCode")){//资源等级排序
                	if(!showVersion){
                		haveVipLevel = true;
                    	ordersql.add("rco." + key + " " + orderMap.get(key));
                	}
				}else{
                    ordersql.add("ndr." + key + " " + orderMap.get(key));
                }
            }
            
            if(CollectionUtils.isNotEmpty(ordersql)){
            	if(showVersion){
            		 sqlOrderBy = "ORDER BY ndr.m_identifier ASC," + StringUtils.join(ordersql, ",");
            	}else{
            		sqlOrderBy = "ORDER BY " + StringUtils.join(ordersql, ",");
            	}
            }
        }else{
        	if(showVersion){
            	sqlOrderBy = "ORDER BY ndr.m_identifier ASC,ndr.version ASC";
            }
        }
        
        //*******各属性的select字段及别名,当不需要时用null代替的语句--Start*******//
        //通用属性
        String commonSelect = "";
        //判断是否有preview
        boolean isNeedPreview = isNeedPreview(resType);
        commonSelect = "ndr.identifier AS identifier,ndr.m_identifier AS mIdentifier,ndr.title AS title,ndr.description AS description,ndr.elanguage AS language,"
                + (isNeedPreview ? "ndr.preview AS preview," : "null AS preview,") + "ndr.tags AS tags,ndr.keywords AS keywords,ndr.custom_properties as customProperties,"
                + "ndr.code as code," + (haveSumSort ? "rs.key_value AS statistics_num" : "null AS statistics_num");
        //LC
        String lifeCycleSelect = "ndr.version AS lifeCycle_version,ndr.estatus AS lifeCycle_status,ndr.enable AS lifeCycle_enable,ndr.creator AS lifeCycle_creator,ndr.publisher AS lifeCycle_publisher,ndr.provider AS lifeCycle_provider,ndr.provider_source AS lifeCycle_providerSource,ndr.create_time AS lifeCycle_createTime,ndr.last_update AS lifeCycle_lastUpdate";
        String lifeCycleSelect4Null = "null AS lifeCycle_version,null AS lifeCycle_status,null AS lifeCycle_enable,null AS lifeCycle_creator,null AS lifeCycle_publisher,null AS lifeCycle_provider,null AS lifeCycle_providerSource,null AS lifeCycle_createTime,null AS lifeCycle_lastUpdate";
        //EDU
        String educationInfoSelect = "ndr.interactivity AS educationInfo_interactivity,ndr.interactivity_level AS educationInfo_interactivityLevel,ndr.end_user_type AS educationInfo_endUserType,ndr.semantic_density AS educationInfo_semanticDensity,ndr.context AS educationInfo_context,ndr.age_range AS educationInfo_ageRange,ndr.difficulty AS educationInfo_difficulty,ndr.learning_time AS educationInfo_learningTime,ndr.edu_description AS educationInfo_description,ndr.edu_language AS educationInfo_language";
        String educationInfoSelect4Null = "null AS educationInfo_interactivity,null AS educationInfo_interactivityLevel,null AS educationInfo_endUserType,null AS educationInfo_semanticDensity,null AS educationInfo_context,null AS educationInfo_ageRange,null AS educationInfo_difficulty,null AS educationInfo_learningTime,null AS educationInfo_description,null AS educationInfo_language";
        //CR
        String copyRightSelect = "ndr.cr_right AS copyRight_right,ndr.cr_description AS copyRight_crDescription,ndr.author AS copyRight_author";
        String copyRightSelect4Null = "null AS copyRight_right,null AS copyRight_crDescription,null AS copyRight_author";
        //*******各属性的select字段及别名,当不需要时用null代替的语句--End*******//
        
        //sql的SELECT
        String sqlSelect = "SELECT " + commonSelect;
        
        //*******include- TI,CG,EDU,LC,CR--处理模块--Start*******//
        boolean haveTI = false;
        boolean haveEDU = false;
        boolean haveLC = false;
        boolean haveCR = false;
        boolean haveCG = false;
        if(CollectionUtils.isNotEmpty(includes)){
            for(String include : includes){
                if(include.equals(IncludesConstant.INCLUDE_TI)){
                    haveTI = true;
                }else if(include.equals(IncludesConstant.INCLUDE_EDU) && 
                        !IndexSourceType.LessonType.getName().equals(resType)){
                    sqlSelect += "," + educationInfoSelect;
                    
                    haveEDU = true;
                }else if(include.equals(IncludesConstant.INCLUDE_LC)){
                    sqlSelect += "," + lifeCycleSelect;
                    
                    haveLC = true;
                }else if(include.equals(IncludesConstant.INCLUDE_CR)){
                    sqlSelect += "," + copyRightSelect;
                    
                    haveCR = true;
                }else if(include.equals(IncludesConstant.INCLUDE_CG)){
                    haveCG = true;
                }
            }
        }else{//只查基础属性时
            sqlSelect += "," + educationInfoSelect4Null + "," + lifeCycleSelect4Null + "," + copyRightSelect4Null;
        }
        
        if(CollectionUtils.isNotEmpty(includes) && !haveEDU){
            sqlSelect += "," + educationInfoSelect4Null;
        }
        if(CollectionUtils.isNotEmpty(includes) && !haveLC){
            sqlSelect += "," + lifeCycleSelect4Null;
        }
        if(CollectionUtils.isNotEmpty(includes) && !haveCR){
            sqlSelect += "," + copyRightSelect4Null;
        }
        //*******include- TI,CG,EDU,LC,CR--处理模块--End*******//
        
        if(haveSortNum){
        	//FIXME 当根据sort_num排序的时候,对identifier做DISTINCT
            sqlSelect = sqlSelect.replaceFirst("ndr.identifier AS identifier", "DISTINCT(ndr.identifier) AS identifier");
        }
        
        //sql的LIMIT 
        Integer result[] = ParamCheckUtil.checkLimit(limit);
        String sqlLimit = "LIMIT " + result[0] + "," + result[1];
        
        //时间范围用于最外层的sql
        String timeRangSql = getTimeRangSql(propsMap);
        
        //最终查询sql,带上排序
        String sql = "";
        if(onlyOneType){//只查一种资源时的sql语句
            sql = sqlSelect + " FROM " 
                    + (haveSortNum ? "(" : "") + (haveSumSort ? "(" : "") + (haveTopSort ? "(" : "") + (haveScoreSort ? "(" : "") + (haveVoteSort ? "(" : "") 
                    + (haveViewSort ? "(" : "") + (haveSizeSort ? "(" : "") + (haveVipLevel ? "(" : "") +  "ndresource ndr " 
                    + (haveSizeSort ? "LEFT JOIN tech_infos ti ON ndr.identifier=ti.resource AND ti.res_type= '" + resTypes.get(0) + "' AND ti.title='href') " : "")
                    + (haveSumSort ? "LEFT JOIN resource_statisticals rs ON ndr.identifier=rs.resource AND rs.res_type='" + resTypes.get(0) + "' AND rs.key_title=:st AND rs.data_from=:sp) " : "")
                    + (haveTopSort ? "LEFT JOIN resource_statisticals rs1 ON ndr.identifier=rs1.resource AND rs1.res_type='" + resTypes.get(0) + "' AND rs1.key_title='top') " : "")
                    + (haveScoreSort ? "LEFT JOIN resource_statisticals rs2 ON ndr.identifier=rs2.resource AND rs2.res_type='" + resTypes.get(0) + "' AND rs2.key_title='scores') " : "")
                    + (haveVoteSort ? "LEFT JOIN resource_statisticals rs3 ON ndr.identifier=rs3.resource AND rs3.res_type='" + resTypes.get(0) + "' AND rs3.key_title='votes') " : "")
                    + (haveViewSort ? "LEFT JOIN resource_statisticals rs4 ON ndr.identifier=rs4.resource AND rs4.res_type='" + resTypes.get(0) + "' AND rs4.key_title='views') " : "")
                    + (haveSortNum ? "LEFT JOIN resource_relations rer ON ndr.identifier=rer.target AND rer.enable=1 AND rer.resource_target_type='" + resTypes.get(0) + "' AND rer.source_uuid='" 
                    + relations.get(0).get("suuid") + "' AND rer.res_type='" + relations.get(0).get("stype") + "') " : "")
                    + (haveVipLevel ? "LEFT JOIN resource_categories rco ON ndr.identifier=rco.resource AND rco.primary_category='" + resTypes.get(0) + "' AND rco.taxOnCode LIKE 'RL%')" : "")
                    + "WHERE ndr.primary_category='" + resTypes.get(0) + "' "
                    + timeRangSql
                    + " AND " + (useIn ? "ndr.identifier IN" : "EXISTS") + " (" + querySqls.get(0) + ")"
                    + " " + sqlOrderBy + " " ;
        }else{//查询多种资源时的sql语句
            sql = sqlSelect + " FROM " 
            		+ (haveSortNum ? "(" : "") + (haveSumSort ? "(" : "") + (haveTopSort ? "(" : "") + (haveScoreSort ? "(" : "") + (haveVoteSort ? "(" : "") 
                    + (haveViewSort ? "(" : "") + (haveSizeSort ? "(" : "") + (haveVipLevel ? "(" : "") +  "ndresource ndr " 
                    + (haveSizeSort ? "LEFT JOIN tech_infos ti ON ndr.identifier=ti.resource AND ti.res_type IN ('" + StringUtils.join(resTypes, "','") + "') AND ti.title='href') " : "")
                    + (haveSumSort ? "LEFT JOIN resource_statisticals rs ON ndr.identifier=rs.resource AND rs.res_type IN ('" + StringUtils.join(resTypes, "','") + "') AND rs.key_title=:st AND rs.data_from=:sp) " : "")
                    + (haveTopSort ? "LEFT JOIN resource_statisticals rs1 ON ndr.identifier=rs1.resource AND rs1.res_type IN ('" + StringUtils.join(resTypes, "','") + "') AND rs1.key_title='top') " : "")
                    + (haveScoreSort ? "LEFT JOIN resource_statisticals rs2 ON ndr.identifier=rs2.resource AND rs2.res_type IN ('" + StringUtils.join(resTypes, "','") + "') AND rs2.key_title='scores') " : "")
                    + (haveVoteSort ? "LEFT JOIN resource_statisticals rs3 ON ndr.identifier=rs3.resource AND rs3.res_type IN ('" + StringUtils.join(resTypes, "','") + "') AND rs3.key_title='votes') " : "")
                    + (haveViewSort ? "LEFT JOIN resource_statisticals rs4 ON ndr.identifier=rs4.resource AND rs4.res_type IN ('" + StringUtils.join(resTypes, "','") + "') AND rs4.key_title='views') " : "")
                    + (haveSortNum ? "LEFT JOIN resource_relations rer ON ndr.identifier=rer.target AND rer.enable=1 AND rer.resource_target_type IN ('" + StringUtils.join(resTypes, "','") + "') AND rer.source_uuid='"
                    + relations.get(0).get("suuid") +"' AND rer.res_type='" + relations.get(0).get("stype") + "') " : "")
                    + (haveVipLevel ? "LEFT JOIN resource_categories rco ON ndr.identifier=rco.resource AND rco.primary_category IN (" + StringUtils.join(resTypes, "','") + ") AND rco.taxOnCode LIKE 'RL%')" : "")
                    + "WHERE ndr.primary_category IN ('" + StringUtils.join(resTypes, "','") + "') "
                    + timeRangSql
                    + " AND " + (useIn ? "ndr.identifier IN" : "EXISTS") 
                    + " (SELECT c.identifier FROM ((" + StringUtils.join(querySqls, ") UNION ALL (") + ")) c"
                    + (useIn ? "" : " WHERE ndr.identifier=c.identifier") + ")"
                    + " " + sqlOrderBy + " " ;
        }
        
        //结果集
        final Map<String, ResourceModel> resultMap = new LinkedHashMap<String, ResourceModel>();
        
        List<FullModel> queryResult = null;
        //判断是走Redis还是走数据库查询
        if(!judgeUseRedisOrNot(limit, isNotManagement, coverages)){//走数据库
            //带上分页
            sql = sql + sqlLimit;
            
            //最终查询sql语句LOG输出
            LOG.info("查询执行的SQL语句:" + sql);
            //参数处理LOG输出
            LOG.info("查询执行的SQL的参数为:" + ObjectUtils.toJson(params));
            
            //查询
            Query query = getEntityManagerByDBName(dbName).createNativeQuery(sql, FullModel.class);
                    
            //参数设置
            for(String paramKey : params.keySet()){
            	query.setParameter(paramKey, params.get(paramKey));
            }
            queryResult = query.getResultList();
        }else{//走Redis
        	queryResult = getResult(sql, params, limit);
            LOG.info("通用走redis缓存");
            
            if(queryResult == null){//说明redis没有缓存
                //异步更新redis缓存
                updateQueryResultCount(sql,params,REDIS_RESULT_SUFFIX,dbName);
                LOG.info("redis缓存结果为空,通用数据库查询");
                
                //带上分页
                sql = sql + sqlLimit;
                
                //最终查询sql语句LOG输出
                LOG.info("查询执行的SQL语句:" + sql);
                //参数处理LOG输出
                LOG.info("查询执行的SQL的参数为:" + ObjectUtils.toJson(params));
                
                //查询
                Query query = getEntityManagerByDBName(dbName).createNativeQuery(sql, FullModel.class);
                
                //参数设置
                for(String paramKey : params.keySet()){
                	query.setParameter(paramKey, params.get(paramKey));
                }
                queryResult = query.getResultList();
            }
        }
        
        //****************************结果集处理--Start****************************//
        for(FullModel fullModel : queryResult){
            ResourceModel resourceModel = null;
            //有扩展属性的资源需要使用其本身的Model接收
            if (IndexSourceType.TeachingMaterialType.getName()
                    .equals(resType) || IndexSourceType.GuidanceBooksType.getName().equals(
                            resType)) {
                
                resourceModel = new TeachingMaterialModel();
            } else if (IndexSourceType.EbookType.getName().equals(
                    resType)) {
                
                resourceModel = new EbookModel();
            }else if (IndexSourceType.QuestionType.getName()
                    .equals(resType)) {
                
                resourceModel = new QuestionModel();
            }else if (IndexSourceType.KnowledgeType.getName()
                    .equals(resType)) {
                
                resourceModel = new KnowledgeModel();
            } else {
                
                resourceModel = new ResourceModel();
            }
            
            // 通用属性
            resourceModel.setIdentifier(fullModel.getIdentifier());
            resourceModel.setmIdentifier(fullModel.getmIdentifier());
            resourceModel.setTitle(fullModel.getTitle());
            resourceModel.setDescription(fullModel.getDescription());
            resourceModel.setLanguage(fullModel.getLanguage());
            resourceModel.setKeywords(ObjectUtils.fromJson(fullModel.getKeywords(), new TypeToken<List<String>>() {}));
            resourceModel.setTags(ObjectUtils.fromJson(fullModel.getTags(), new TypeToken<List<String>>() {}));
            if(isNeedPreview){
                resourceModel.setPreview(ObjectUtils.fromJson(fullModel.getPreview(), Map.class));
            }
            resourceModel.setCustomProperties(fullModel.getCustomProperties());
            resourceModel.setNdresCode(fullModel.getCode());
            if(haveSumSort){
            	resourceModel.setStatisticsNum(fullModel.getStatistics_num()==null ? 0D : fullModel.getStatistics_num());
            }else{
            	resourceModel.setStatisticsNum(null);
            }
            
            //EDU,LC,CR
            for (String include : includes) {
                if (include.equals(IncludesConstant.INCLUDE_EDU) &&
                    !IndexSourceType.LessonType.getName().equals(resType)) {
                    ResEducationalModel rem = new ResEducationalModel();
                    rem.setInteractivity(StringUtils.isNotEmpty(fullModel.getEducationInfo_interactivity()) ? Integer.parseInt(fullModel.getEducationInfo_interactivity()) : 0);
                    rem.setInteractivityLevel(StringUtils.isNotEmpty(fullModel.getEducationInfo_interactivityLevel()) ? Integer.parseInt(fullModel.getEducationInfo_interactivityLevel()) : 0);
                    rem.setEndUserType(fullModel.getEducationInfo_endUserType());
                    rem.setSemanticDensity(StringUtils.isNotEmpty(fullModel.getEducationInfo_semanticDensity()) ? Long.parseLong(fullModel.getEducationInfo_semanticDensity()) : 0L);
                    rem.setContext(fullModel.getEducationInfo_context());
                    rem.setAgeRange(fullModel.getEducationInfo_ageRange());
                    rem.setDifficulty(fullModel.getEducationInfo_difficulty());
                    rem.setLearningTime(fullModel.getEducationInfo_learningTime());
                    rem.setDescription(ObjectUtils.fromJson(fullModel.getEducationInfo_description(), Map.class));
                    rem.setLanguage(fullModel.getEducationInfo_language());

                    resourceModel.setEducationInfo(rem);
                }else if (include.equals(IncludesConstant.INCLUDE_LC)) {
                    ResLifeCycleModel rlc = new ResLifeCycleModel();
                    rlc.setVersion(fullModel.getLifeCycle_version());
                    rlc.setStatus(fullModel.getLifeCycle_status());
                    
                    int enableInt = StringUtils.isNotEmpty(fullModel.getLifeCycle_enable()) ? Integer.parseInt(fullModel.getLifeCycle_enable()) : 0;
                    rlc.setEnable(enableInt == 1 ? true : false);
                    rlc.setCreator(fullModel.getLifeCycle_creator());
                    rlc.setPublisher(fullModel.getLifeCycle_publisher());
                    rlc.setProvider(fullModel.getLifeCycle_provider());
                    rlc.setProviderSource(fullModel.getLifeCycle_providerSource());
                    
                    long createTimeLong = StringUtils.isNotEmpty(fullModel.getLifeCycle_createTime()) ? Long.parseLong(fullModel.getLifeCycle_createTime()) : 0L;
                    rlc.setCreateTime(new Date(createTimeLong));
                    
                    long lastUpdateLong = StringUtils.isNotEmpty(fullModel.getLifeCycle_lastUpdate()) ? Long.parseLong(fullModel.getLifeCycle_lastUpdate()) : 0L;
                    rlc.setLastUpdate(new Date(lastUpdateLong));
                    
                    resourceModel.setLifeCycle(rlc);
                }else if (include.equals(IncludesConstant.INCLUDE_CR)) {
                    ResRightModel rr = new ResRightModel();
                    rr.setRight(fullModel.getCopyRight_right());
                    rr.setDescription(fullModel.getCopyRight_crDescription());
                    rr.setAuthor(fullModel.getCopyRight_author());

                    resourceModel.setCopyright(rr);
                }
            }
            
            resultMap.put(fullModel.getIdentifier(), resourceModel);
        }
        //****************************结果集处理--End****************************//
        
        //***********************异步查询TI,CG,EXT--Start***********************//
        if(CollectionUtils.isNotEmpty(resultMap.keySet())){
            List<Callable<SubQueryThread>> threads = new ArrayList<Callable<SubQueryThread>>();
            if(haveTI){
                SubQueryThread subThread = new SubQueryThread(true,false,false,resTypes,resultMap);
                threads.add(subThread);
            }
            
            if(haveCG){
                SubQueryThread subThread = new SubQueryThread(false,true,false,resTypes,resultMap);
                threads.add(subThread);
            }
            
            //查询多种资源时都不查询扩展属性
            if(resTypes.size()==1 && haveExtThisResource(resTypes.get(0))){
                SubQueryThread subThread = new SubQueryThread(false,false,true,resTypes,resultMap);
                threads.add(subThread);
            }
            
            try {
                executorService.invokeAll(threads, 10*60, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "LC/SUBQUERY_THREAD_FAIL",
                        e.getMessage());
            }
        }
        //***********************异步查询TI,CG,EXT--End***********************//
        
        //返回的list
        List<ResourceModel> resultList = new ArrayList<ResourceModel>();
        for(String key : resultMap.keySet()){
            resultList.add(resultMap.get(key));
        }
        
        return resultList;
    }
    
    /**
     * 判断是否使用Redis.
     * 
     *  不是使用Redis的场景:
     *  a.limit=(a,b)， a+b>500时
     * 	b.管理端接口,即带management的
     *  c.coverage参数不传时
     *  d.coverage参数中有非Org/nd/，即非nd库的
     *  
     * <p>Create Time: 2016年1月12日   </p>
     * <p>Create author: xiezy   </p>
     * @param limit
     * @param isNotManagement
     * @param coverages
     * @return
     */
    public boolean judgeUseRedisOrNot(String limit, boolean isNotManagement, List<String> coverages) {
        Integer result[] = ParamCheckUtil.checkLimit(limit);
        //场景a,b,c
        if((result[0] + result[1] > 500) || !isNotManagement || CollectionUtils.isEmpty(coverages)){
            return false;
        }
        //场景d
        if(CollectionUtils.isNotEmpty(coverages)){
            boolean onlyNdCoverage = true;
            for(String cv : coverages){
                if(!cv.startsWith("Org/nd/")){
                    onlyNdCoverage = false;
                }
            }
            
            if(!onlyNdCoverage){
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 判断该资源是否有扩张属性
     * FIXME    
     * <p>Create Time: 2015年11月12日   </p>
     * <p>Create author: xiezy   </p>
     * @param resType
     * @return
     */
    private boolean haveExtThisResource(String resType){
        if(resType.equals(IndexSourceType.QuestionType.getName()) ||
                resType.equals(IndexSourceType.TeachingMaterialType.getName()) ||
                resType.equals(IndexSourceType.EbookType.getName()) ||
                resType.equals(IndexSourceType.KnowledgeType.getName()) ||
                resType.equals(IndexSourceType.GuidanceBooksType.getName())){
            return true;
        }
        
        return false;
    }
    
    /**
     * 获取时间范围的条件sql
     * @author xiezy
     * @date 2016年4月21日
     * @param propsMap
     * @return
     */
    private String getTimeRangSql(Map<String, Set<String>> propsMap){
    	 String result = "";
         
         List<String> propList = new ArrayList<String>();
         int i = 1;
         String alias = "ndr";
    	
         if(CollectionUtils.isNotEmpty(propsMap)){
        	 for(String key : propsMap.keySet()){
                 String propParam = "";
                 
                 if(key.endsWith("_GT")){//时间 gt
                     List<String> gtSqlList = new ArrayList<String>();
                     for(int j=0;j<propsMap.get(key).size();j++){
                     	String fieldName = alias + "." + key.substring(0, key.length()-3);
                         String gtSql = fieldName + " > :" + "ndrprop" + i + j;
                         gtSqlList.add(gtSql);
                     }
                     propParam = "(" + StringUtils.join(gtSqlList, " OR ") + ")";
                 }else if(key.endsWith("_LT")){//时间 lt
                     List<String> ltSqlList = new ArrayList<String>();
                     for(int j=0;j<propsMap.get(key).size();j++){
                     	String fieldName = alias + "." + key.substring(0, key.length()-3);
                         String ltSql = fieldName + " < :" + "ndrprop" + i + j;
                         ltSqlList.add(ltSql);
                     }
                     propParam = "(" + StringUtils.join(ltSqlList, " OR ") + ")";
                 }else if(key.endsWith("_LE")){//时间 le
                     List<String> leSqlList = new ArrayList<String>();
                     for(int j=0;j<propsMap.get(key).size();j++){
                     	String fieldName = alias + "." + key.substring(0, key.length()-3);
                         String leSql = fieldName + " <= :" + "ndrprop" + i + j;
                         leSqlList.add(leSql);
                     }
                     propParam = "(" + StringUtils.join(leSqlList, " OR ") + ")";
                 }else if(key.endsWith("_GE")){//时间 ge
                     List<String> geSqlList = new ArrayList<String>();
                     for(int j=0;j<propsMap.get(key).size();j++){
                     	String fieldName = alias + "." + key.substring(0, key.length()-3);
                         String geSql = fieldName + " >= :" + "ndrprop" + i + j;
                         geSqlList.add(geSql);
                     }
                     propParam = "(" + StringUtils.join(geSqlList, " OR ") + ")";
                 }
                 
                 if(StringUtils.isNotEmpty(propParam)){
                     propList.add(propParam);
                 }
                 
                 i++;
        	 }
         }
         
         if(CollectionUtils.isNotEmpty(propList)){
        	 result += "AND (" + StringUtils.join(propList, " AND ") + ")";
         }
    	
         return result;
    }
    
    /**
     * 线程-内部类 
     * --获取TI属性,CG属性,扩展属性,并发执行
     * <p>Create Time: 2015年11月30日           </p>
     * @author xiezy
     */
    class SubQueryThread implements Callable<SubQueryThread> {
        private boolean haveTI;
        private boolean haveCG;
        private boolean haveExt;
        private List<String> resTypes;
        private Map<String, ResourceModel> resultMap;
        
        public SubQueryThread(boolean haveTI,boolean haveCG,boolean haveExt,
                List<String> resTypes,Map<String, ResourceModel> resultMap) {
            this.haveTI = haveTI;
            this.haveCG = haveCG;
            this.haveExt = haveExt;
            this.resTypes = resTypes;
            this.resultMap = resultMap;
        }
        
        @Override
        public SubQueryThread call() throws Exception {
            if(haveTI){
                queryTechInfos(resTypes, resultMap);
            }else if(haveCG){
                queryCategories(resTypes, resultMap);
            }else if(haveExt){
                queryExt(resTypes.get(0), resultMap);
            }
            
            return this;
        }
    }
    
    @Override
    public long commomQueryCount(String resType, String resCodes, Set<String> categories, Set<String> categoryExclude,
            List<Map<String, String>> relations, List<String> coverages, Map<String, Set<String>> propsMap,
            String words, String limit, boolean isNotManagement, boolean reverse, boolean useIn,
            Boolean printable, String printableKey,boolean forceStatus,List<String> tags,boolean showVersion) {
        //需要用到的变量
        List<String> querySqls = new ArrayList<String>();
        Map<String,Object> params = new HashMap<String, Object>(); 
        
        //获取querySqls和params,并把resTypes和onlyOneType返回
        Map<String, Object> map = this.getQuerySqlsAndParams(true, resType, resCodes, categories, categoryExclude, relations, coverages, propsMap, words, isNotManagement, reverse, useIn,
        		printable, printableKey,null,null,null,forceStatus,tags,showVersion, querySqls, params);
        @SuppressWarnings("unchecked")
		List<String> resTypes = (List<String>)map.get("resTypes");
        boolean onlyOneType = (boolean)map.get("onlyOneType");
        
        //最终sql
        String sql = "";
        
        if(onlyOneType){//查询一种资源时
            sql = querySqls.get(0);
        }else{//查询多种资源时
            sql = "SELECT COUNT(DISTINCT c.identifier) FROM (( " + StringUtils.join(querySqls, ") UNION ALL (") + " )) c";
        }
        
        //判断是否是查询习题库
        final DbName dbName = getDBName4CommonQuery(onlyOneType, resTypes);
        
        //最终查询sql语句LOG输出
        LOG.info("查询total执行的SQL语句:" + sql);
        //参数处理LOG输出
        LOG.info("查询total执行的SQL的参数为:" + ObjectUtils.toJson(params));
        
        //获取total
        Integer[] limitResult = ParamCheckUtil.checkLimit(limit);
        int total = getResourceQueryCount(sql, params, limitResult[0] == 0 ? true : false, dbName);
        
        return new Long(total);
    }
    
    @Override
	public Map<String, Integer> resourceStatistics(String resType, Set<String> categories, List<String> coverages,
			Map<String, Set<String>> propsMap, String groupBy, boolean isNotManagement) {
    	//需要用到的变量
        List<String> querySqls = new ArrayList<String>();
        Map<String,Object> params = new HashMap<String, Object>(); 
        
        //获取querySqls和params,并把resTypes和onlyOneType返回
        Map<String, Object> map = this.getQuerySqlsAndParams(false, resType, null, categories, null, null, coverages, propsMap, null, isNotManagement, false, true, null, null,null, null,null,false,null,false, querySqls, params);
        @SuppressWarnings("unchecked")
		List<String> resTypes = (List<String>)map.get("resTypes");
    	
        //最终sql
        String sql = "SELECT rcgb.taxonCode AS taxonCode,COUNT(DISTINCT rcgb.resource) AS counts FROM resource_categories rcgb "
        		+ " WHERE rcgb.primary_category='" + resType 
        		+ "' AND rcgb.resource IN (" + querySqls.get(0) + ")"
        		+ " AND rcgb.category_name=:cn"
        		+ " GROUP BY rcgb.taxonCode";
        params.put("cn", groupBy);
        
        //判断是否是查询习题库
        final DbName dbName = getDBName4CommonQuery(true, resTypes);
        
		return getResourceStatistics(sql, params, dbName);
	}
    
    /**
     * 获取资源统计结果 by redis or DB
     * <p>Create Time: 2016年3月30日   </p>
     * <p>Create author: xiezy   </p>
     * @param sql
     * @param params
     * @param dbName
     * @return
     */
    private Map<String,Integer> getResourceStatistics(final String sql,final Map<String, Object> params,DbName dbName){
    	Map<String, Integer> resultMap = getStatistics(sql, params);
    	if(resultMap == null){//redis中没有,查数据库
    		Map<String, Integer> map = queryStatisticsByDB(sql, params, dbName);
    		saveStatistics(sql, params, map);
    		
    		return map;
    	}
    	
    	return resultMap;
    }
    
    /**
     * 通过数据库获取资源统计结果
     * <p>Create Time: 2016年3月30日   </p>
     * <p>Create author: xiezy   </p>
     * @param sql
     * @param params
     * @param dbName
     * @return
     */
    private Map<String, Integer> queryStatisticsByDB(final String sql,final Map<String, Object> params,DbName dbName){
    	final Map<String, Integer> resultMap = new HashMap<String, Integer>();
    	
    	//最终查询SQL语句LOG输出
        LOG.info("资源统计执行的SQL语句:" + sql);
        //参数处理LOG输出
        LOG.info("资源统计执行的SQL的参数为:" + ObjectUtils.toJson(params));
        
    	NamedParameterJdbcTemplate npjt = new NamedParameterJdbcTemplate(getJdbcTemplateByDBName(dbName));
    	npjt.query(sql, params, new RowMapper<Map<String,Integer>>(){

			@Override
			public Map<String, Integer> mapRow(ResultSet rs, int rowNum)
					throws SQLException {
				resultMap.put(rs.getString("taxonCode"), rs.getInt("counts"));
				return null;
			}
    		
    	});
    	
    	return resultMap;
    }
    
    /**
     * 通过redis获取资源统计结果
     * <p>Create Time: 2016年3月30日   </p>
     * <p>Create author: xiezy   </p>
     * @param sql
     * @param params
     * @return
     */
    private Map<String, Integer> getStatistics(final String sql,final Map<String, Object> params){
    	int hashCode = CommonHelper.getHashCodeKey(sql,params);
    	Map<String, Integer> value = ertStatistics.get(hashCode+REDIS_STATISTICS_SUFFIX,Map.class);
		return value;
    }
    
    /**
     * 将资源统计结果保存到redis上
     * <p>Create Time: 2016年3月30日   </p>
     * <p>Create author: xiezy   </p>
     * @param sql
     * @param params
     * @param resultMap
     */
    private void saveStatistics(final String sql,final Map<String, Object> params,Map<String, Integer> resultMap){
    	int hashCode = CommonHelper.getHashCodeKey(sql,params);
        
        //保存到redis
    	ertStatistics.set(hashCode+REDIS_STATISTICS_SUFFIX, resultMap, 10l, TimeUnit.MINUTES);
    }
    
    /**************************判断使用IN 还是 EXISTS --Start**************************/
    /**
     * 判断使用IN 还是 EXISTS	
     * TODO 该判断与业务关联比较大,存在不确定性
     * <p>Create Time: 2015年12月2日   </p>
     * <p>Create author: xiezy   </p>
     * @param relations
     * @param coverages
     * @return  true == IN  false == EXISTS
     */
    public boolean judgeUseInOrExists(String resType, String resCodes, Set<String> categories, Set<String> categoryExclude,
            List<Map<String, String>> relations, List<String> coverages, Map<String, Set<String>> propsMap,
            String words, boolean isNotManagement, boolean reverse, Boolean printable, String printableKey,
            boolean forceStatus,List<String> tags,boolean showVersion) {
        //返回值
        boolean useIn = false;
        
        //查询上次total值--start
        Map<String, Object> map4count = getCommomQueryCountSql(resType,resCodes, categories, categoryExclude, relations, coverages, propsMap, words, isNotManagement, reverse, printable, printableKey,forceStatus,tags,showVersion);
        int lastCount = getPreSqlCount((String)map4count.get("sql"), (Map<String,Object>)map4count.get("params"));
        //查询上次total值--end
        
        //1.根据上次的count结果来判断使用IN还是EXISTS
        if(lastCount != -1){//表示该sql不是第一次执行
            if(lastCount > CRITICAL_VALUE){//使用EXISTS
                useIn = false;
            }else{
                useIn = true;
            }
        }else{//2.表示该sql第一次执行,使用根据业务场景的策略:带relation,coverage=User,coverage=Debug的时候使用IN
            boolean onlyUser = true;
            boolean isQaTest = false;
            if(CollectionUtils.isNotEmpty(coverages)){
                for(String cv : coverages){
                    List<String> coverageElemnt = Arrays.asList(cv.split("/"));
                    if(coverageElemnt.get(0).equals("Org")){
                        onlyUser = false;
                        break;
                    }
                }
                
                for(String cv : coverages){
                    List<String> coverageElemnt = Arrays.asList(cv.split("/"));
                    if(coverageElemnt.get(0).equals("Debug")){
                        isQaTest = true;
                        break;
                    }
                }
            }else{
                onlyUser = false;
            }
            
            if(CollectionUtils.isNotEmpty(relations) || onlyUser || isQaTest){
                useIn = true;
            }
        }
        
        return useIn;
    }
    
    /**
     * 用于获取count的sql,用于判断使用IN 或者 EXISTS	
     * <p>Description:              </p>
     * <p>Create Time: 2015年12月3日   </p>
     * <p>Create author: xiezy   </p>
     */
    private Map<String, Object> getCommomQueryCountSql(String resType, String resCodes, Set<String> categories, Set<String> categoryExclude,
            List<Map<String, String>> relations, List<String> coverages, Map<String, Set<String>> propsMap,
            String words, boolean isNotManagement, boolean reverse, Boolean printable, String printableKey,
            boolean forceStatus,List<String> tags,boolean showVersion) {
        //需要用到的变量
        List<String> querySqls = new ArrayList<String>();
        Map<String,Object> params = new HashMap<String, Object>(); 
        
        //获取querySqls和params,并把resTypes和onlyOneType返回
        Map<String, Object> map = this.getQuerySqlsAndParams(true, resType, resCodes, categories, categoryExclude, relations, coverages, propsMap, words, isNotManagement, reverse, true,
        		printable, printableKey,null,null,null,forceStatus,tags,showVersion, querySqls, params);
        boolean onlyOneType = (boolean)map.get("onlyOneType");
        
        //最终sql
        String sql = "";
        
        if(onlyOneType){//查询一种资源时
            sql = querySqls.get(0);
        }else{//查询多种资源时
            sql = "SELECT COUNT(DISTINCT c.identifier) FROM (( " + StringUtils.join(querySqls, ") UNION ALL (") + " )) c";
        }
        
        Map<String, Object> resultap = new HashMap<String, Object>();
        resultap.put("sql", sql);
        resultap.put("params", params);
        
        return resultap;
    }
    /**************************判断使用IN 还是 EXISTS --End**************************/
    
    /**
     * 获取querySqls和params,并把resTypes和onlyOneType返回	
     * <p>Create Time: 2015年12月8日   </p>
     * <p>Create author: xiezy   </p>
     * @param querySqls sql结果集
     * @param params    参数结果集
     */
    private Map<String,Object> getQuerySqlsAndParams(boolean isCount, String resType,String resCodes, 
            Set<String> categories, Set<String> categoryExclude,
            List<Map<String, String>> relations, List<String> coverages, Map<String, Set<String>> propsMap,
            String words, boolean isNotManagement,boolean reverse,boolean useIn, Boolean printable, String printableKey,
            Map<String, String> orderMap, String statisticsType, String statisticsPlatform,boolean forceStatus,List<String> tags,boolean showVersion,
            List<String> querySqls,Map<String,Object> params){
        //判断是否需要交集
        Map<String,Object> dealCategoriesMap = dealAndCategories(categories);
        //带有and 的category的拆解集合
        @SuppressWarnings("unchecked")
		List<List<String>> andCategories = (List<List<String>>)dealCategoriesMap.get("allAndCategories");
        //去掉带有 and 之后的categories
        @SuppressWarnings("unchecked")
		Set<String> noAndcategories = (Set<String>)dealCategoriesMap.get("afterRemoveAllAndCategories");
        
        //获取resType集合
        List<String> resTypes = getResTypeList(resType, resCodes);
        if(CollectionUtils.isEmpty(resTypes)){
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CommonSearchParamError.getCode(),
                    "resType或resCode传参错误");
        }
        //只查一种资源
        boolean onlyOneType = resTypes.size()==1 ? true : false;
        
        int j = 1;
        for(String type : resTypes){
            String querySql = "";
            String paramHead = "type"+j+"_";
            
            // 查询的sql
            querySql = completeQuerySql(isCount, type, resTypes, noAndcategories, andCategories, categoryExclude, relations, coverages, propsMap,
                    words, isNotManagement, reverse, paramHead, useIn, onlyOneType, printable, printableKey,forceStatus,tags,showVersion);
            querySqls.add(querySql);

            // 参数处理
            Map<String, Object> subParams = sqlParamsDeal(isCount, type, resTypes, noAndcategories, andCategories, categoryExclude, relations,
                    coverages, propsMap, words, isNotManagement, paramHead, onlyOneType, reverse, printable, printableKey,
                    orderMap, statisticsType, statisticsPlatform,tags);
            params.putAll(subParams);
            
            j++;
        }
        
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("resTypes", resTypes);
        map.put("onlyOneType", onlyOneType);
        return map;
    }
    
    /**
     * 获取资源类型集合	
     * <p>Create Time: 2015年12月4日   </p>
     * <p>Create author: xiezy   </p>
     * @param resType
     * @param resCodes
     * @return
     */
    private List<String> getResTypeList(String resType,String resCodes){
        Set<String> resTypes = new HashSet<String>();
        
        if(resType.equals(Constant.RESTYPE_EDURESOURCE)){
            List<String> codes = Arrays.asList(resCodes.split(","));
            if(CollectionUtils.isNotEmpty(codes)){
                for(String code : codes){
                    String type = LifeCircleApplicationInitializer.tablenames_properties.getProperty(code);
                    if(type != null){
                        resTypes.add(type);
                    }else{
                        throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                LifeCircleErrorMessageMapper.CommonSearchParamError.getCode(),
                                "resCode中的" + code + "不支持查询");
                    }
                }
            }else{
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        LifeCircleErrorMessageMapper.CommonSearchParamError.getCode(),
                        "resType为" + Constant.RESTYPE_EDURESOURCE + "时,rescode必须为有效值");
            }
        }else{
            resTypes.add(resType);
        }
        
        return new ArrayList<String>(resTypes);
    }
    
    /**
     * 处理categories参数
     * --将categories分为两类,1)category=code格式的    2)category=code1 and code2格式的
     * <p>Create Time: 2015年10月27日   </p>
     * <p>Create author: xiezy   </p>
     * @param categories
     */
    private Map<String, Object> dealAndCategories(Set<String> categories){
        //复制一份categories,目的是避免Collection的增删改对原集合的影响
        Set<String> copyCategories = new HashSet<String>();
        if(CollectionUtils.isNotEmpty(categories)){
            copyCategories.addAll(categories);
        }
        
        //用于存放所有category=A and B格式时的拆解开的code集合,如{{A,B},{C,D}}
        List<List<String>> allAndCategories = new ArrayList<List<String>>();
        //用于存放category=A and B格式的category,目的是为了后面得到除了category=A and B格式的category集合
        Set<String> removeAndCategories = new HashSet<String>();
        
        if(CollectionUtils.isNotEmpty(copyCategories)){
            for(String category : copyCategories){
                if(!category.contains("/") && category.contains("and")){//说明有and
                    //用于存放category=A and B格式时的拆解开的code,如A和B
                    List<String> categoryAndOp = Arrays.asList(category.split(" and "));
                    categoryAndOp = CollectionUtils.removeEmptyDeep(categoryAndOp);// 主要是为了防止 A and B and 的情况
                    
                    allAndCategories.add(categoryAndOp);
                    removeAndCategories.add(category);
                }
            }
            
            //从一开始copy一份的copyCategories中,删掉category=A and B格式的category参数
            if(CollectionUtils.isNotEmpty(removeAndCategories)){
                copyCategories.removeAll(removeAndCategories);
            }
        }
        
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("allAndCategories", allAndCategories);
        map.put("afterRemoveAllAndCategories", copyCategories);
        
        return map;
    }
    
    /**
     * 取到collections集合中的元素中最大的size值
     * <p>Create Time: 2016年1月25日   </p>
     * <p>Create author: xiezy   </p>
     * @param collections
     * @return
     */
    private int getMaxCount(List<List<String>> collections){
        int maxCount = 0;
        for(List<String> collection : collections){
            if(collection.size() > maxCount){
                maxCount = collection.size();
            }
        }
        
        return maxCount;
    }
    
    /**
     * 完整的查询Sql	
     * <p>Create Time: 2015年10月27日   </p>
     * <p>Create author: xiezy   </p>
     * @return
     */
    private String completeQuerySql(boolean isCount, final String resType, List<String> resTypes, Set<String> categories,
            List<List<String>> andCategories, Set<String> categoryExclude,
            List<Map<String, String>> relations, List<String> coverages,
            Map<String, Set<String>> propsMap, String words, boolean isNotManagement, boolean reverse,
            String paramHead, boolean useIn, boolean onlyOneType,
            Boolean printable, String printableKey,boolean forceStatus,List<String> tags,boolean showVersion) {
        String sqlSelect = "SELECT a.identifier";
        if(isCount && onlyOneType){
            sqlSelect = "SELECT COUNT(DISTINCT a.identifier)";
        }
            
        //sql的sql的FROM,JOIN,ON  WhERE ORDER BY
        String querySqlCondition = commonQuerySql(isCount, resType, resTypes, categories, andCategories, categoryExclude, 
        		relations, coverages, propsMap, words, isNotManagement, reverse, paramHead,useIn,onlyOneType,printable,printableKey,forceStatus,tags,showVersion);

        //查询的sql
        String querySql = sqlSelect + " " + querySqlCondition;
        
        return querySql;
    }
    
    
    /**
     * 通用的查询sql  -- join where	
     * <p>Create Time: 2015年7月14日   </p>
     * <p>Create author: xiezy   </p>
     * @param categories            这里的categories为除去category=A and B格式的
     * @param andCategories         category=A and B格式参数的分解集合
     * @param categoryExclude       表示需要排除哪些类型
     */
    private String commonQuerySql(Boolean isCount, String resType, List<String> resTypes, Set<String> categories, List<List<String>> andCategories,
            Set<String> categoryExclude, List<Map<String, String>> relations, List<String> coverages, Map<String, Set<String>> propsMap,
            String words, boolean isNotManagement, boolean reverse, String paramHead, boolean useIn, boolean onlyOneType,
            Boolean printable, String printableKey, boolean forceStatus, List<String> tags, boolean showVersion) {
        //ndresource表
        String tableName = "ndresource a";
        
        //sql的FROM,JOIN,ON
        StringBuilder sqlJoin = new StringBuilder("");
        sqlJoin.append("FROM ");
        sqlJoin.append(tableName);
        //relations
        List<String> relationInWhereSqls = new ArrayList<String>();
        if(CollectionUtils.isNotEmpty(relations)){
        	if(getDBName4CommonQuery(onlyOneType, resTypes).equals(DbName.QUESTION)){//查习题库
            	int i = 1;
                for(Map<String, String> relation : relations){
                	if(!reverse && !CommonServiceHelper.isQuestionDb(relation.get("stype"))){//查默认库,跨库
                		List<String> questionIds = getTargetByRelation(relation.get("stype"), relation.get("suuid"), relation.get("rtype"), resType, DbName.DEFAULT);
                        String relationInWhereSql = "a.identifier IN ('" + (StringUtils.join(questionIds, "','")) + "')";
                        if(showVersion){
                        	relationInWhereSql = "a.m_identifier IN ('" + (StringUtils.join(questionIds, "','")) + "')";
                        }
                        relationInWhereSqls.add(relationInWhereSql);
                	}else{
                		sqlJoin.append(" INNER JOIN resource_relations rr");
                        sqlJoin.append(i);
                        if(showVersion){
                        	sqlJoin.append(" ON a.m_identifier=");
                        }else{
                        	sqlJoin.append(" ON a.identifier=");
                        }
                        if(reverse){
                            sqlJoin.append("rr" + i + ".source_uuid");
                        }else{
                            sqlJoin.append("rr" + i + ".target");
                        }
                        sqlJoin.append(" AND rr" + i + ".enable=1");
                        sqlJoin.append(" AND (");
                        sqlJoin.append(relationParam4Sql(resType,relation,reverse,paramHead,"rr"+i));
                        sqlJoin.append(")");
                        
                        i++;
                	}
                }
            }else{//查默认库
            	int i = 1;
                for(Map<String, String> relation : relations){
                    if(!reverse && CommonServiceHelper.isQuestionDb(relation.get("stype"))){//查习题库，跨库
                        List<String> resourceIds = getTargetByRelation(relation.get("stype"), relation.get("suuid"), relation.get("rtype"), resType, DbName.QUESTION);
                        String relationInWhereSql = "a.identifier IN ('" + (StringUtils.join(resourceIds, "','")) + "')";
                        if(showVersion){
                        	relationInWhereSql = "a.m_identifier IN ('" + (StringUtils.join(resourceIds, "','")) + "')";
                        }
                        relationInWhereSqls.add(relationInWhereSql);
                    }else{
                        sqlJoin.append(" INNER JOIN resource_relations rr");
                        sqlJoin.append(i);
                        if(showVersion){
                        	sqlJoin.append(" ON a.m_identifier=");
                        }else{
                        	sqlJoin.append(" ON a.identifier=");
                        }
                        if(reverse){
                            sqlJoin.append("rr" + i + ".source_uuid");
                        }else{
                            sqlJoin.append("rr" + i + ".target");
                        }
                        sqlJoin.append(" AND rr" + i + ".enable=1");
                        sqlJoin.append(" AND (");
                        sqlJoin.append(relationParam4Sql(resType,relation,reverse,paramHead,"rr"+i));
                        sqlJoin.append(")");
                        
                        i++;
                    }
                }
            }
        }
        
        //printable & printableKey
        if(printable != null){
        	if(showVersion){
        		sqlJoin.append(" INNER JOIN tech_infos tis ON a.m_identifier=tis.resource");
        	}else{
        		sqlJoin.append(" INNER JOIN tech_infos tis ON a.identifier=tis.resource");
        	}
        	sqlJoin.append(" AND tis.res_type='");
        	sqlJoin.append(resType);
        	sqlJoin.append("'");
        }
        
        //coverages
        if(CollectionUtils.isNotEmpty(coverages)){
        	if(showVersion){
        		sqlJoin.append(" INNER JOIN res_coverages rcv ON a.m_identifier=rcv.resource AND rcv.res_type='");
        	}else{
        		sqlJoin.append(" INNER JOIN res_coverages rcv ON a.identifier=rcv.resource AND rcv.res_type='");
        	}
            
            sqlJoin.append(resType);
            sqlJoin.append("'");
            sqlJoin.append(" AND (");
            if(isNotManagement && !forceStatus){
                sqlJoin.append(coverageParam4Sql4DealOnline(coverages,paramHead));
            }else{
                sqlJoin.append(coverageParam4Sql(coverages,paramHead));
            }
            sqlJoin.append(")");
        }else{
        	if(showVersion){
        		sqlJoin.append(" LEFT JOIN res_coverages rcv ON a.m_identifier=rcv.resource AND rcv.res_type='");
        	}else{
        		sqlJoin.append(" LEFT JOIN res_coverages rcv ON a.identifier=rcv.resource AND rcv.res_type='");
        	}
            sqlJoin.append(resType);
            sqlJoin.append("'");
        }
        
        //categories & andCategories
        if(CollectionUtils.isNotEmpty(categories) || CollectionUtils.isNotEmpty(andCategories)){
            boolean havePath = false;
            boolean haveCode = false;
            
            if(CollectionUtils.isNotEmpty(categories)){
                for(String str4Join : categories){//目的是减少Join表
                    if(StringUtils.isNotEmpty(str4Join)){
                        if(str4Join.contains("/")){
                            havePath = true;
                        }else{
                            haveCode = true;
                        }
                    }
                }
            }
            
            if(havePath){
            	if(showVersion){
            		sqlJoin.append(" LEFT JOIN resource_categories rc ON rc.primary_category='" + resType + "' AND a.m_identifier=rc.resource");
            	}else{
            		sqlJoin.append(" LEFT JOIN resource_categories rc ON rc.primary_category='" + resType + "' AND a.identifier=rc.resource");
            	}
            }
            if(haveCode || CollectionUtils.isNotEmpty(andCategories)){
                //至少join一次
            	if(showVersion){
            		sqlJoin.append(" LEFT JOIN resource_categories rcc0 ON rcc0.primary_category='" + resType + "' AND a.m_identifier=rcc0.resource");
            	}else{
            		sqlJoin.append(" LEFT JOIN resource_categories rcc0 ON rcc0.primary_category='" + resType + "' AND a.identifier=rcc0.resource");
            	}
                
                if(CollectionUtils.isNotEmpty(andCategories)){//注意这里的i是从1开始,使join的次数为andCategories的【maxsize-1】
                    for(int i=1;i<getMaxCount(andCategories);i++){
                        sqlJoin.append(" LEFT JOIN resource_categories rcc");
                        sqlJoin.append(i);
                        sqlJoin.append(" ON rcc");
                        sqlJoin.append(i);
                        sqlJoin.append(".primary_category='");
                        sqlJoin.append(resType);
                        if(showVersion){
                        	sqlJoin.append("' AND a.m_identifier=rcc");
                        }else{
                        	sqlJoin.append("' AND a.identifier=rcc");
                        }
                        sqlJoin.append(i);
                        sqlJoin.append(".resource");
                    }
                }
            }
        }
        
        //FIXME 临时方案,对提供商为智能出题的做控制
        String providerLimitSql = isNotManagement ? StaticDatas.CAN_QUERY_PROVIDER ? "" : " AND (a.provider IS NULL OR a.provider != '智能出题') " : "";
        
        //判断是否需要ndr.identifier=a.identifier
        String existCondition = isCount ? "" : (onlyOneType ? (useIn ? "" : " AND ndr.identifier=a.identifier") : "");
        
        //sql的WHERE
        String sqlWhere = "WHERE a.primary_category='" + resType + "'" 
                        + existCondition 
                        + " AND a.enable=1 " 
                        + providerLimitSql;
        
        if(CollectionUtils.isEmpty(coverages)){//当覆盖范围为空时
            sqlWhere = "WHERE a.primary_category='" + resType + "'" 
                    + existCondition 
                    + " AND a.enable=1 " 
                    + providerLimitSql
                    + " AND (rcv.target_type='" + CoverageConstant.TargetType.TARGET_TYPE_PB.getCode() + "'"
                    + " AND rcv.target='" + CoverageConstant.TARGET_PUBLIC + "'"
                    + " AND rcv.strategy='" + CoverageConstant.Strategy.STRATEGY_SHAREING.getCode() + "'"
                    + ")";
        }
        
        String categoryParamSql = "";
        if(CollectionUtils.isNotEmpty(categories) || CollectionUtils.isNotEmpty(andCategories)){
            categoryParamSql = categoryParam4Sql(categories,andCategories,paramHead);
        }
        
        String categoryExcludeParamSql = "";
        if(CollectionUtils.isNotEmpty(categoryExclude)){
        	if(showVersion){
        		categoryExcludeParamSql = "a.m_identifier NOT IN (" + categoryExcludeParam4Sql(resType, categoryExclude, paramHead) + ")";
        	}else{
        		categoryExcludeParamSql = "a.identifier NOT IN (" + categoryExcludeParam4Sql(resType, categoryExclude, paramHead) + ")";
        	}
        }
        
        String propParamSql = "";
        if(!CollectionUtils.isEmpty(propsMap)){
            propParamSql = propParam4Sql(propsMap,paramHead);
        }
        
        String printableParamSql = "";
        if(printable != null){
        	printableParamSql = printableParam4Sql(printable, printableKey, paramHead);
        }
        
        //FIXME 临时方案
        String tagsParamSql = "";
        if(CollectionUtils.isNotEmpty(tags)){
        	tagsParamSql = tagsParam4Sql(tags, paramHead);
        }
        
        String wordsParamSql = "";
        if(!StringUtils.isEmpty(words)){
            if(IndexSourceType.LessonType.getName().equals(resType) || 
               IndexSourceType.InstructionalObjectiveType.getName().equals(resType)){//没有EDU属性
                wordsParamSql = "(a.title LIKE :" + paramHead + "twords OR a.description LIKE :" + paramHead + "dwords OR a.keywords LIKE :" + paramHead + "kwords OR"
                        + " a.cr_description LIKE :" + paramHead + "crdwords OR a.tags LIKE :" + paramHead + "tagswords)";
            }else{
                wordsParamSql = "(a.title LIKE :" + paramHead + "twords OR a.description LIKE :" + paramHead + "dwords OR a.keywords LIKE :" + paramHead + "kwords OR"
                        + " a.cr_description LIKE :" + paramHead + "crdwords OR a.edu_description LIKE :" + paramHead + "edudwords OR a.tags LIKE :" + paramHead + "tagswords)";
            }
        }
        
        List<String> sqlWhereList = new ArrayList<String>();
        sqlWhereList.add(sqlWhere);
        if(CollectionUtils.isNotEmpty(relationInWhereSqls)){
            sqlWhereList.addAll(relationInWhereSqls);
        }
        if(StringUtils.isNotEmpty(categoryParamSql)){
            sqlWhereList.add(categoryParamSql);
        }
        if(StringUtils.isNotEmpty(categoryExcludeParamSql)){
            sqlWhereList.add(categoryExcludeParamSql);
        }
        if(StringUtils.isNotEmpty(propParamSql)){
            sqlWhereList.add(propParamSql);
        }
        if(StringUtils.isNotEmpty(printableParamSql)){
        	sqlWhereList.add(printableParamSql);
        }
        if(StringUtils.isNotEmpty(tagsParamSql)){
        	sqlWhereList.add(tagsParamSql);
        }
        if(StringUtils.isNotEmpty(wordsParamSql)){
            sqlWhereList.add(wordsParamSql);
        }
        
        sqlWhere = StringUtils.join(sqlWhereList, " AND ");
        
        return sqlJoin.toString() + " " + sqlWhere;
    }
    
    /**
     * 习题分库--资源关系的跨库处理
     * <p>Create Time: 2016年2月3日   </p>
     * <p>Create author: xiezy   </p>
     * @param sourceType
     * @param sourceId
     * @param targetType
     * @param isQuestionDB
     * @return
     */
    @SuppressWarnings("unchecked")
	private List<String> getTargetByRelation(String sourceType,String sourceId,String relationType,String targetType,DbName dbName){
        String sql = "";
        if(sourceId.endsWith("$")){
            List<String> nodes = getTreeChildrenIds4Relation(sourceType, sourceId.substring(0, sourceId.lastIndexOf("$")));
            sql = "SELECT rr.target AS target FROM resource_relations rr WHERE rr.enable=1 AND rr.res_type='" + sourceType + "' "
                    + "AND rr.source_uuid IN ('" + StringUtils.join(nodes, "','") + "') AND "
                    + "rr.resource_target_type='" + targetType + "' "
                    + "AND rr.relation_type='" + relationType + "'";
        }else{
            sql = "SELECT rr.target AS target FROM resource_relations rr WHERE rr.enable=1 AND rr.res_type='" + sourceType + "' "
                    + "AND rr.source_uuid='" + sourceId + "' AND "
                    + "rr.resource_target_type='" + targetType + "' "
                    + "AND rr.relation_type='" + relationType + "'";
        }
        
//        LOG.info("跨库查询-getTargetByRelation:" + sql);
        
        Query query = getEntityManagerByDBName(dbName).createNativeQuery(sql);
       
        return query.getResultList();
    }
    
    /**
     * 查询分类维度数据
     * <p>Create Time: 2015年7月23日   </p>
     * <p>Create author: xiezy   </p>
     * @param resultMap
     */
    protected void queryCategories(List<String> resTypes,Map<String, ResourceModel> resultMap){
        final Map<String, List<ResClassificationModel>> categoryMap = new LinkedHashMap<String, List<ResClassificationModel>>();
        
        List<ResourceCategory> result = queryCategoriesUseHql(resTypes, resultMap.keySet());
        
        if(CollectionUtils.isNotEmpty(result)){
        	for(ResourceCategory rc : result){
                String resource = rc.getResource();
                
                ResClassificationModel rcm = new ResClassificationModel();
                rcm.setIdentifier(rc.getIdentifier());
                rcm.setTaxonpath(rc.getTaxonpath());
                rcm.setTaxoncode(rc.getTaxoncode());
                rcm.setTaxonname(rc.getTaxonname());
                rcm.setCategoryCode(rc.getCategoryCode());
                rcm.setCategoryName(rc.getCategoryName());
                
                if(!categoryMap.containsKey(resource)){//新记录
                    List<ResClassificationModel> list = new ArrayList<ResClassificationModel>();
                    list.add(rcm);
                    
                    categoryMap.put(resource, list);
                }else{//已经有记录的
                    List<ResClassificationModel> list = categoryMap.get(resource);
                    list.add(rcm);
                }
            }
        }
        
        //如果categoryMap为空,全部给个默认值
        if(CollectionUtils.isEmpty(categoryMap)){
            for(String key : resultMap.keySet()){
                categoryMap.put(key, new ArrayList<ResClassificationModel>());
            }
        }else{
            //将categories加入到对应的资源中
            for(String identifier : resultMap.keySet()){
                boolean haveCategories = false;
                for(String resource : categoryMap.keySet()){
                    if(resource.equals(identifier)){//同一个资源
                        haveCategories = true;
                        ResourceModel resourceModel = resultMap.get(identifier);
                        resourceModel.setCategoryList(categoryMap.get(resource));
                        break;
                    }
                }
                
                //不存在categories的identifier对应的ResourceModel的categoryList设置默认值
                if(!haveCategories){
                    ResourceModel resourceModel = resultMap.get(identifier);
                    resourceModel.setCategoryList(new ArrayList<ResClassificationModel>());
                }
            }
        }
    }
    
    /**
     * 查询维度数据（批量resource id）
     * @author linsm
     * @param resTypes
     * @param keySet
     * @return
     * @since 
     */
    @Override
    public List<ResourceCategory> queryCategoriesUseHql(List<String> resTypes, Set<String> keySet) {
        if(CollectionUtils.isEmpty(resTypes)){
        	return null;
        }
    	
    	Query query = null;
        if(getDBName4CommonQuery(resTypes.size()>1 ? false : true , resTypes).equals(DbName.QUESTION)){
            query = questionEm.createNamedQuery("commonQueryGetCategories");
        }else{
            query = defaultEm.createNamedQuery("commonQueryGetCategories");
        }
        
        query.setParameter("rts", resTypes);
        query.setParameter("sids", keySet);
        
        return query.getResultList();
    }

    /**
     * 查询技术属性数据
     * <p>Create Time: 2015年7月23日   </p>
     * <p>Create author: xiezy   </p>
     * @param resultMap
     */
    protected void queryTechInfos(List<String> resTypes,Map<String, ResourceModel> resultMap){
        final Map<String, List<ResTechInfoModel>> techInfosMap = new LinkedHashMap<String, List<ResTechInfoModel>>();
        
        List<TechInfo> result = queryTechInfosUseHql(resTypes, resultMap.keySet());;
        
        if(CollectionUtils.isNotEmpty(result)){
        	for(TechInfo techInfo : result){
                String resource = techInfo.getResource();
                
                ResTechInfoModel ti = new ResTechInfoModel();
                ti.setIdentifier(techInfo.getIdentifier());
                ti.setLocation(techInfo.getLocation());
                ti.setFormat(techInfo.getFormat());
                ti.setSize(techInfo.getSize());
                ti.setMd5(techInfo.getMd5());
                ti.setTitle(techInfo.getTitle());
                ti.setRequirements(ObjectUtils.fromJson(techInfo.getRequirements(), new TypeToken<List<TechnologyRequirementModel>>(){}));
                ti.setEntry(techInfo.getEntry());
                ti.setSecureKey(techInfo.getSecureKey());
                ti.setPrintable(techInfo.getPrintable());
                
                if(!techInfosMap.containsKey(resource)){//新记录
                    List<ResTechInfoModel> list = new ArrayList<ResTechInfoModel>();
                    list.add(ti);
                    
                    techInfosMap.put(resource, list);
                }else{//已经有记录的
                    List<ResTechInfoModel> list = techInfosMap.get(resource);
                    list.add(ti);
                }
            }
        }
        
        //如果techInfosMap为空,全部给个默认值
        if(CollectionUtils.isEmpty(techInfosMap)){
            for(String key : resultMap.keySet()){
                techInfosMap.put(key, new ArrayList<ResTechInfoModel>());
            }
        }else{
            //将tech_infos加入到对应的资源中
            for(String identifier : resultMap.keySet()){
                boolean haveTechInfo = false;
                for(String resource : techInfosMap.keySet()){
                    if(resource.equals(identifier)){//同一个资源
                        haveTechInfo = true;
                        ResourceModel resourceModel = resultMap.get(identifier);
                        resourceModel.setTechInfoList(techInfosMap.get(resource));
                        break;
                    }
                }
                
                //不存在techInfo的identifier对应的ResourceModel的TechInfoList设置默认值
                if(!haveTechInfo){
                    ResourceModel resourceModel = resultMap.get(identifier);
                    resourceModel.setTechInfoList(new ArrayList<ResTechInfoModel>());
                }
            } 
        }
    }
    
    /**
     * 查询技术属性（批量resource id）
     * @author linsm
     * @param resTypes
     * @param keySet
     * @return
     * @since 
     */
    @Override
    public List<TechInfo> queryTechInfosUseHql(List<String> resTypes, Set<String> keySet) {
    	if(CollectionUtils.isEmpty(resTypes)){
        	return null;
        }
    	
        Query query = null;
        if(getDBName4CommonQuery(resTypes.size()>1 ? false : true , resTypes).equals(DbName.QUESTION)){
            query = questionEm.createNamedQuery("commonQueryGetTechInfos");
        }else{
            query = defaultEm.createNamedQuery("commonQueryGetTechInfos");
        }
        
        query.setParameter("rts", resTypes);
        query.setParameter("sids", keySet);
        
        return query.getResultList();
    }

    /**
     * 查询扩展属性	FIXME 当其他资源有扩展属性时这里需要做相应改动
     * <p>Create Time: 2015年11月30日   </p>
     * <p>Create author: xiezy   </p>
     * @param resType
     * @param resultMap
     */
    protected void queryExt(String resType,Map<String, ResourceModel> resultMap){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            if (IndexSourceType.TeachingMaterialType.getName().equals(resType) || IndexSourceType.GuidanceBooksType.getName().equals(resType)) {
                List<TeachingMaterial> list = 
                        teachingMaterialRepository.getAll(new ArrayList<String>(resultMap.keySet()));
                if(CollectionUtils.isNotEmpty(list)){
                	for(String identifier : resultMap.keySet()){
                        for(TeachingMaterial tm : list){
                            if(tm.getIdentifier().equals(identifier)){//同一个资源
                                //扩展属性赋值
                                TeachingMaterialModel tmm = (TeachingMaterialModel)resultMap.get(identifier);
                                TmExtPropertiesModel tmExt = new TmExtPropertiesModel();
                                tmExt.setIsbn(tm.getIsbn());
                                tmExt.setAttachments(tm.getAttachments());
                                tmExt.setCriterion(tm.getCriterion());
                                
                                tmm.setExtProperties(tmExt);
                            }
                        }
                    }
                }
            }else if (IndexSourceType.EbookType.getName().equals(resType)) {
                List<Ebook> list = 
                        ebookRepository.getAll(new ArrayList<String>(resultMap.keySet()));
                if(CollectionUtils.isNotEmpty(list)){
                	 for(String identifier : resultMap.keySet()){
                         for(Ebook eb : list){
                             if(eb.getIdentifier().equals(identifier)){//同一个资源
                                 //扩展属性赋值
                                 EbookModel ebm = (EbookModel)resultMap.get(identifier);
                                 EbookExtPropertiesModel ebExt = new EbookExtPropertiesModel();
                                 ebExt.setIsbn(eb.getIsbn());
                                 ebExt.setAttachments(eb.getAttachments());
                                 ebExt.setCriterion(eb.getCriterion());
                                 
                                 ebm.setExtProperties(ebExt);
                             }
                         }
                     }
                }
            }else if (IndexSourceType.QuestionType.getName().equals(resType)) {
                List<Question> list = 
                        questionRepository.getAll(new ArrayList<String>(resultMap.keySet()));
                
                if(CollectionUtils.isNotEmpty(list)){
                	for(String identifier : resultMap.keySet()){
                        for(Question q : list){
                            if(q.getIdentifier().equals(identifier)){//同一个资源
                                //扩展属性赋值
                                QuestionModel qm = (QuestionModel)resultMap.get(identifier);
                                QuestionExtPropertyModel qExt = new QuestionExtPropertyModel();
                                qExt.setDiscrimination(q.getDiscrimination() != null ? q.getDiscrimination() : 0f);
                                qExt.setAnswer(q.getAnswer());
                                qExt.setItemContent(q.getItemContent());
                                qExt.setCriterion(q.getCriterion());
                                qExt.setScore(q.getScore() != null ? q.getScore() : 0f);
                                qExt.setSecrecy(q.getSecrecy() != null ? q.getSecrecy() : 0);
                                qExt.setModifiedDifficulty(q.getModifiedDifficulty() != null ? q.getModifiedDifficulty() : 0f);
                                qExt.setModifiedDiscrimination(q.getModifiedDiscrimination() != null ? q.getModifiedDiscrimination() : 0f);
                                qExt.setUsedTime(q.getUsedTime() != null ? q.getUsedTime() : 0);
                                
                                String exposalDateStr = q.getExposalDate() != null ? q.getExposalDate().toString() : null;
                                if(exposalDateStr == null){
                                    qExt.setExposalDate(null);
                                }else{
                                    try {
                                        qExt.setExposalDate(sdf.parse(exposalDateStr));
                                    } catch (ParseException e) {
                                        throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                LifeCircleErrorMessageMapper.CommonSearchFail.getCode(),
                                                e.getLocalizedMessage());
                                    }
                                }
                                
                                qExt.setAutoRemark(q.getAutoRemark() != null ? q.getAutoRemark() : false);
                                qExt.setExtDifficulty(q.getExtDifficulty() != null ? q.getExtDifficulty() : 0f);
                                
                                qm.setExtProperties(qExt);
                            }
                        }
                    }
                }
            }else if (IndexSourceType.KnowledgeType.getName().equals(resType)) {
            	List<Chapter> list = 
                        chapterRepository.getAll(new ArrayList<String>(resultMap.keySet()));
            	
            	if(CollectionUtils.isNotEmpty(list)){
            		for(String identifier : resultMap.keySet()){
            			for(Chapter k : list){
                            if(k.getIdentifier().equals(identifier)){//同一个资源
                            	KnowledgeModel km = (KnowledgeModel)resultMap.get(identifier);
                            	KnowledgeExtPropertiesModel kExt = new KnowledgeExtPropertiesModel();
                            	if(k.getParent().equals(k.getTeachingMaterial())
                            			&& !k.getParent().startsWith("$S")){
                            		kExt.setParent("ROOT");
                            	}else{
                            		kExt.setParent(k.getParent());
                            	}
                            	kExt.setOrder_num(k.getLeft());
                            	km.setExtProperties(kExt);
                            }
            			}
            		}
            	}
			}
        } catch (EspStoreException e) {
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "LC/QUERY_EXT_FAIL",
                    e.getMessage());
        }
    }
    
    /**
     * 关系的sql参数拼接
     * <p>Create Time: 2015年7月14日   </p>
     * <p>Create author: xiezy   </p>
     * @param relation
     * @return
     */
    private String relationParam4Sql(String resType, Map<String, String> relation, boolean reverse, String paramHead,
            String alias) {
        String result = "";
        paramHead += alias + "_";
        
        if(reverse){
            if(relation.get("suuid").endsWith("$")){
                result += " (" + alias + ".res_type='" + resType + "' AND " + alias + ".target IN (:" + paramHead + "rsidlist) AND " + alias + ".resource_target_type=:" + paramHead + "rstype AND " + alias + ".relation_type='" + relation.get("rtype") + "')";
            }else{
                result += " (" + alias + ".res_type='" + resType + "' AND " + alias + ".target=:" + paramHead + "rsid AND " + alias + ".resource_target_type=:" + paramHead + "rstype AND " + alias + ".relation_type='" + relation.get("rtype") + "')";
            }
        }else{
            if(relation.get("suuid").endsWith("$")){
                result += " (" + alias + ".resource_target_type='" + resType + "' AND " + alias + ".source_uuid IN (:" + paramHead + "rsidlist) AND " + alias + ".res_type=:" + paramHead + "rstype AND " + alias + ".relation_type='" + relation.get("rtype") + "')";
            }else{
                result += " (" + alias + ".resource_target_type='" + resType + "' AND " + alias + ".source_uuid=:" + paramHead + "rsid AND " + alias + ".res_type=:" + paramHead + "rstype AND " + alias + ".relation_type='" + relation.get("rtype") + "')";
            }
        }   
        
        return result;
    }
    
    /**
     * 递归获取树形结构-nodeId本身以及子节点的id集合
     * <p>Create Time: 2015年12月14日   </p>
     * <p>Create author: xiezy   </p>
     * @param nodeType
     * @param nodeId
     * @return
     */
    private List<String> getTreeChildrenIds4Relation(String nodeType,String nodeId){
        if(!IndexSourceType.ChapterType.getName().equals(nodeType) &&
           !IndexSourceType.KnowledgeType.getName().equals(nodeType)){
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CommonSearchParamError.getCode(),
                    "relation参数进行递归查询时,目前仅支持:chapters,knowledges");
        }
        
        String sql = "SELECT c2.identifier from chapters c2,ndresource ndr,"
                + "(SELECT c1.tree_left as tl,c1.tree_right as tr,c1.teaching_material as tm FROM chapters c1 WHERE c1.identifier=:nodeId) c3 "
                + "WHERE c2.teaching_material=c3.tm and c2.identifier=ndr.identifier and ndr.primary_category=:nodeType "
                + "and ndr.enable=1 and c2.tree_left >= c3.tl and c2.tree_right <= c3.tr";
        
        Query query = defaultEm.createNativeQuery(sql);
        query.setParameter("nodeId", nodeId);
        query.setParameter("nodeType", nodeType);
        
        List<String> ids = query.getResultList();
        if(CollectionUtils.isEmpty(ids)){
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CommonSearchParamError.getCode(),
                    nodeId + "--该资源不存在");
        }
        
        return ids;
    }
    
    /**
     * 资源覆盖范围sql参数拼接    
     * <p>Create Time: 2015年7月14日   </p>
     * <p>Create author: xiezy   </p>
     * @param coverages
     * @return
     */
    private String coverageParam4Sql(List<String> coverages,String paramHead){
        String result = "";
        int i = 1;
        for(String coverage : coverages){
            List<String> coverageElemnt = Arrays.asList(coverage.split("/"));
            String targetTypeSql = "rcv.target_type=:" + paramHead + "cvty" + i;
            String targetSql = "rcv.target=:" + paramHead + "cvt" + i;
            String strategySql = coverageElemnt.get(2).equals("*") ? "" : "rcv.strategy=:" + paramHead + "cvs" + i;
            
            List<String> condition = new ArrayList<String>();
            condition.add(targetTypeSql);
            condition.add(targetSql);
            if(StringUtils.isNotEmpty(strategySql)){
                condition.add(strategySql);
            }
            
            result += " (" + StringUtils.join(condition, " AND ")  + ") OR";
            
            i++;
        }
        
        return result.substring(0, result.length()-2);
    }
    
    /**
     * 资源覆盖范围sql参数拼接
     * --------------------对ND库中的数据做限制的coverage参数处理方式
     * <p>Create Time: 2015年10月22日   </p>
     * <p>Create author: xiezy   </p>
     * @param coverages
     * @return
     */
    private String coverageParam4Sql4DealOnline(List<String> coverages,String paramHead){
        List<String> cvSqls = new ArrayList<String>();
        int i = 1;
        
        for(String coverage : coverages){
            List<String> coverageElemnt = Arrays.asList(coverage.split("/"));
            boolean haveStrategy = coverageElemnt.get(2).equals("*") ? false : true;
            
            String cvSql = "(rcv.target_type=:" + paramHead + "cvty" + i + " AND rcv.target=:" + paramHead + "cvt" + i;
            if(haveStrategy){
            	cvSql += " AND rcv.strategy=:" + paramHead + "cvs" + i;
            }
            if(coverageElemnt.get(0).equals(CoverageConstant.TargetType.TARGET_TYPE_ORG.getCode()) &&
            		coverageElemnt.get(1).equals(CoverageConstant.ORG_CODE_ND)){//ND库
            	cvSql += " AND a.estatus = 'ONLINE'";
            }
            cvSql += ")";
            
            cvSqls.add(cvSql);
            i++;
        }
        
        return StringUtils.join(cvSqls, " OR ");
    }
    
    /**
     * 维度数据sql参数拼接  
     * <p>Create Time: 2015年7月14日   </p>
     * <p>Create author: xiezy   </p>
     * @param categories            这里的categories为除去category=A and B格式的
     * @param andCategories         category=A and B格式参数的分解集合
     */
    private String categoryParam4Sql(Set<String> categories,List<List<String>> andCategories,String paramHead){
        String result = "";
        
        List<String> pathList = new ArrayList<String>();
        List<String> codeList = new ArrayList<String>();
        
        //处理categories
        if(CollectionUtils.isNotEmpty(categories)){
            int i =1;
            for(String category : categories){
                if(category.contains("/")){//说明是path
                    String path = "";
                    if(category.contains("*")){
                        path = "rc.taxOnPath LIKE :" + paramHead + "cgpathlike" + i;
                    }else{
                        path = "rc.taxOnPath=:" + paramHead + "cgpath" + i;
                    }
                    
                    pathList.add(path);
                }else{//说明是code
                    String code = "";
                    if(category.contains("*")){//使用通配符
                        code = "rcc0.taxOnCode LIKE :" + paramHead + "cgcodelike" + i;
                    }else{
                        code = "rcc0.taxOnCode=:" + paramHead + "cgcode" + i;
                    }

                    codeList.add(code);
                }
                
                i++;
            }
        }
        
        //处理andCategories
        if(CollectionUtils.isNotEmpty(andCategories)){
            int k = 1;
            for(List<String> andCategory : andCategories){
                List<String> andCodeSql = new ArrayList<String>();
                
                int j = 0;//rcc0以0开始
                for(String ac : andCategory){
                    String code = "";
                    if(ac.contains("*")){//使用通配符
                        code = "rcc" + j + ".taxOnCode LIKE :" + paramHead + "andcgcodelike" + j + k;
                    }else{
                        code = "rcc" + j + ".taxOnCode=:" + paramHead + "andcgcode" + j + k;
                    }
                    
                    andCodeSql.add(code);
                    j++;
                }
                
                //andCategories中的code之间为and关系
                String andCodeSqlResult = "(" + StringUtils.join(andCodeSql, " AND ") + ")";
                //andCategories整体与categories中code为OR的关系
                codeList.add(andCodeSqlResult);
                
                k++;
            }
        }
        
        if(CollectionUtils.isNotEmpty(pathList) && CollectionUtils.isNotEmpty(codeList)){//path,code同存
            result = "(" + StringUtils.join(pathList, " OR ") + ") AND (" + StringUtils.join(codeList, " OR ") + ")"; 
        }else if(CollectionUtils.isEmpty(pathList) && CollectionUtils.isNotEmpty(codeList)){//只有code
            result = "(" + StringUtils.join(codeList, " OR ") + ")";
        }else if(CollectionUtils.isNotEmpty(pathList) && CollectionUtils.isEmpty(codeList)){//只有path
            result = "(" + StringUtils.join(pathList, " OR ") + ")";
        }
        
        return result;
    }
    
    /**
     * 需要排除的维度分类sql参数拼接
     * @author xiezy
     * @date 2016年7月13日
     * @param resType
     * @param categoryExclude
     * @param paramHead
     * @return
     */
    private String categoryExcludeParam4Sql(String resType,Set<String> categoryExclude, String paramHead){
        String result = "";
        
        if(CollectionUtils.isNotEmpty(categoryExclude)){
            result = "SELECT ndex.identifier FROM ndresource ndex INNER JOIN resource_categories rcex ON ndex.identifier=rcex.resource";
            result += " WHERE ndex.enable=1 AND ndex.primary_category='" + resType + "'";
            
            List<String> cgex4Like = new ArrayList<String>();
            List<String> cgex4In = new ArrayList<String>();
            for(String cgex : categoryExclude){
            	if(cgex.contains("*")){
            		cgex4Like.add(cgex);
            	}else{
            		cgex4In.add(cgex);
            	}
            }
            
            List<String> excludeSqlList = new ArrayList<String>();
            if(CollectionUtils.isNotEmpty(cgex4In)){
            	String excludeSql = "rcex.taxOnCode IN (:" + paramHead + "cgexs)";
            	excludeSqlList.add(excludeSql);
            }
            if(CollectionUtils.isNotEmpty(cgex4Like)){
            	for(int i=0;i<cgex4Like.size();i++){
            		String excludeSql = "rcex.taxOnCode LIKE :" + paramHead + "cgexlike" + i;
            		excludeSqlList.add(excludeSql);
            	}
            }
            
            if(CollectionUtils.isNotEmpty(excludeSqlList)){
            	result += " AND (" + StringUtils.join(excludeSqlList, " OR ") + ")";
            }
        }
        
        return result;
    }
    
    /**
     * prop sql参数拼接     
     * <p>Create Time: 2015年7月14日   </p>
     * <p>Create author: xiezy   </p>
     * @param propsMap
     * @return
     */
    private String propParam4Sql(Map<String, Set<String>> propsMap,String paramHead){
        String result = "";
        int i = 1;
        
        List<String> propList = new ArrayList<String>();
        String alias = "a";
        for(String key : propsMap.keySet()){
            String propParam = "";
            
            if(key.endsWith("_GT")){//时间 gt
                List<String> gtSqlList = new ArrayList<String>();
                for(int j=0;j<propsMap.get(key).size();j++){
                	String fieldName = alias + "." + key.substring(0, key.length()-3);
                    String gtSql = fieldName + " > :" + paramHead + "prop" + i + j;
                    gtSqlList.add(gtSql);
                }
                propParam = "(" + StringUtils.join(gtSqlList, " OR ") + ")";
            }else if(key.endsWith("_LT")){//时间 lt
                List<String> ltSqlList = new ArrayList<String>();
                for(int j=0;j<propsMap.get(key).size();j++){
                	String fieldName = alias + "." + key.substring(0, key.length()-3);
                    String ltSql = fieldName + " < :" + paramHead + "prop" + i + j;
                    ltSqlList.add(ltSql);
                }
                propParam = "(" + StringUtils.join(ltSqlList, " OR ") + ")";
            }else if(key.endsWith("_LE")){//时间 le
                List<String> leSqlList = new ArrayList<String>();
                for(int j=0;j<propsMap.get(key).size();j++){
                	String fieldName = alias + "." + key.substring(0, key.length()-3);
                    String leSql = fieldName + " <= :" + paramHead + "prop" + i + j;
                    leSqlList.add(leSql);
                }
                propParam = "(" + StringUtils.join(leSqlList, " OR ") + ")";
            }else if(key.endsWith("_GE")){//时间 lt
                List<String> geSqlList = new ArrayList<String>();
                for(int j=0;j<propsMap.get(key).size();j++){
                	String fieldName = alias + "." + key.substring(0, key.length()-3);
                    String geSql = fieldName + " >= :" + paramHead + "prop" + i + j;
                    geSqlList.add(geSql);
                }
                propParam = "(" + StringUtils.join(geSqlList, " OR ") + ")";
            }else if(key.endsWith("_NE")){//ne
            	String fieldName = alias + "." + key.substring(0, key.length()-3);
                propParam = "(" + fieldName + " IS NULL OR " + fieldName + " NOT IN (:" + paramHead + "prop" + i + "))"; 
            }else if(key.endsWith("_LIKE")){//like
                List<String> likeSqlList = new ArrayList<String>();
                for(int j=0;j<propsMap.get(key).size();j++){
                    String likeSql = alias + "." + key.substring(0, key.length()-5) + " LIKE :" + paramHead + "prop" + i + j;
                    
                    likeSqlList.add(likeSql);
                }
                propParam = "(" + StringUtils.join(likeSqlList, " OR ") + ")"; 
            }else{//eq or in
            	if(propsMap.get(key).size()==1){
            		propParam = alias + "." + key + " = :" + paramHead + "prop" + i;
            	}else{
            		propParam = alias + "." + key + " IN (:" + paramHead + "prop" + i + ")"; 
            	}
            }
            
            if(StringUtils.isNotEmpty(propParam)){
                propList.add(propParam);
            }
            
            i++;
        }
        
        result += "(" + StringUtils.join(propList, " AND ") + ")";
        return result;
    }
    
    /**
     * printable的条件sql
     * @author xiezy
     * @date 2016年4月26日
     * @param printable
     * @param printableKey
     * @param paramHead
     * @return
     */
    private String printableParam4Sql(Boolean printable,String printableKey,String paramHead){
    	StringBuilder result = new StringBuilder("");
    	if(printable != null){
    		result.append("(tis.printable=");
        	if(printable){
        		result.append("1");
        	}else{
        		result.append("0");
        	}
        	
        	if(StringUtils.isNotEmpty(printableKey)){
        		result.append(" AND tis.title=:");
        		result.append(paramHead);
        		result.append("filekey");
    		}
        	
        	result.append(")");
    	}
    	
    	return result.toString();
    }
    
    /**
     * tags参数处理
     * @author xiezy
     * @date 2016年7月7日
     * @param tags
     * @param paramHead
     * @return
     */
    private String tagsParam4Sql(List<String> tags,String paramHead){
    	String result = "";
    	List<String> tagsSqlList = new ArrayList<String>();
    	if(CollectionUtils.isNotEmpty(tags)){
    		int i = 1;
    		for(String tag : tags){
    			if(StringUtils.hasText(tag)){
    				List<String> tagAndOp = Arrays.asList(tag.split(" and "));
    				if(CollectionUtils.isNotEmpty(tagAndOp)){
    					List<String> tagAndList = new ArrayList<String>();
    					int j = 1;
    					for(String tagAnd : tagAndOp){
    						if(StringUtils.hasText(tagAnd)){
    							String tagSql = "a.tags LIKE :" + paramHead + "tag" + i + j;
    							tagAndList.add(tagSql);
    							
    							j++;
    						}
    					}
    					
    					if(CollectionUtils.isNotEmpty(tagAndList)){
    						tagsSqlList.add("(" + StringUtils.join(tagAndList, " AND ") + ")");
    					}
    				}
    				
        			i++;
    			}
    		}
    		
    		if(CollectionUtils.isNotEmpty(tagsSqlList)){
    			result = "(" + StringUtils.join(tagsSqlList, " OR ") + ")";
        	}
    	}
    	
		return result;
    }
    
    /**
     * 判断是否需要preview
     * <p>Create Time: 2015年8月18日   </p>
     * <p>Create author: xiezy   </p>
     * @param resType
     * @return
     */
    public static boolean isNeedPreview(final String resType){
        if(resType.equals(IndexSourceType.InstructionalObjectiveType.getName()) ||
                resType.equals(IndexSourceType.LessonType.getName())){
            return false;
        }
        
        return true;
    }
    
    /**
     * 参数处理
     * <p>Description:              </p>
     * <p>Create Time: 2015年8月26日   </p>
     * <p>Create author: xiezy   </p>
     * @param categories            这里的categories为除去category=A and B格式的
     * @param andCategories         category=A and B格式参数的分解集合
     * @param relations
     * @param coverages
     * @param propsMap
     * @param words
     * @return
     */
    private Map<String, Object> sqlParamsDeal(boolean isCount, String resType, List<String> resTypes, Set<String> categories, 
            List<List<String>> andCategories, Set<String> categoryExclude,
            List<Map<String, String>> relations, List<String> coverages, Map<String, Set<String>> propsMap,
            String words, boolean isNotManagement, String paramHead, boolean onlyOneType, boolean reverse,
            Boolean printable, String printableKey,Map<String, String> orderMap, 
            String statisticsType, String statisticsPlatform,List<String> tags) {
        
        Map<String,Object> params = new HashMap<String, Object>();
        //1.relation的参数处理
        if(CollectionUtils.isNotEmpty(relations)){//有relation条件时才加入参数
        	if(getDBName4CommonQuery(onlyOneType, resTypes).equals(DbName.QUESTION)){//查习题库
        		int i = 1;
                for(Map<String, String> rmap : relations){
                    if(!(!reverse && !CommonServiceHelper.isQuestionDb(rmap.get("stype")))){
                        if(rmap.get("suuid").endsWith("$")){
                            String nodeId = rmap.get("suuid").substring(0, rmap.get("suuid").lastIndexOf("$"));
                            params.put(paramHead + "rr" + i + "_" + "rsidlist", getTreeChildrenIds4Relation(rmap.get("stype"), nodeId));
                        }else{
                            params.put(paramHead + "rr" + i + "_" + "rsid", rmap.get("suuid"));
                        }
                        params.put(paramHead + "rr" + i + "_" + "rstype", rmap.get("stype"));
                        
                        i++;
                    }
                }
        	}else{
        		int i = 1;
                for(Map<String, String> rmap : relations){
                    if(!(!reverse && CommonServiceHelper.isQuestionDb(rmap.get("stype")))){
                        if(rmap.get("suuid").endsWith("$")){
                            String nodeId = rmap.get("suuid").substring(0, rmap.get("suuid").lastIndexOf("$"));
                            params.put(paramHead + "rr" + i + "_" + "rsidlist", getTreeChildrenIds4Relation(rmap.get("stype"), nodeId));
                        }else{
                            params.put(paramHead + "rr" + i + "_" + "rsid", rmap.get("suuid"));
                        }
                        params.put(paramHead + "rr" + i + "_" + "rstype", rmap.get("stype"));
                        
                        i++;
                    }
                }
        	}
        }
        
        //2.coverage的参数处理
        if(CollectionUtils.isNotEmpty(coverages)){
            int i = 1;
            
            for(String cv : coverages){
                List<String> coverageElemnt = Arrays.asList(cv.split("/"));
                params.put(paramHead + "cvty" + i, coverageElemnt.get(0));
                params.put(paramHead + "cvt" + i, coverageElemnt.get(1));
                if(!coverageElemnt.get(2).equals("*")){
                    params.put(paramHead + "cvs" + i, coverageElemnt.get(2));
                }
                
                i++;
            }
        }
        //3.1 categories的参数处理
        if(CollectionUtils.isNotEmpty(categories)){
            int i = 1;
            for(String cg : categories){
                if(cg.contains("/")){
                    if(cg.contains("*")){
                        params.put(paramHead + "cgpathlike" + i, cg.replaceAll("\\*", "\\%"));
                    }else{
                        params.put(paramHead + "cgpath" + i, cg);
                    }
                }else{
                    if(cg.contains("*")){
                        params.put(paramHead + "cgcodelike" + i, cg.replaceAll("\\*", "\\%"));  
                    }else{
                        params.put(paramHead + "cgcode" + i, cg);
                    }
                }
                
                i++;
            }
        }
        //3.2 andCategories的参数处理
        if(CollectionUtils.isNotEmpty(andCategories)){
            int k = 1;
            for(List<String> andCategory : andCategories){
                int j = 0;//rcc0以0开始
                for(String ac : andCategory){
                    if(ac.contains("*")){//使用通配符
                        params.put(paramHead + "andcgcodelike" + j + k, ac.replaceAll("\\*", "\\%"));  
                    }else{
                        params.put(paramHead + "andcgcode" + j + k, ac);
                    }
                    
                    j++;
                }
                
                k++;
            }
        }
        //3.3 categoryExclude的参数处理
        if(CollectionUtils.isNotEmpty(categoryExclude)){
        	List<String> cgex4Like = new ArrayList<String>();
            List<String> cgex4In = new ArrayList<String>();
            for(String cgex : categoryExclude){
            	if(cgex.contains("*")){
            		cgex4Like.add(cgex);
            	}else{
            		cgex4In.add(cgex);
            	}
            }
            
            if(CollectionUtils.isNotEmpty(cgex4In)){
            	params.put(paramHead + "cgexs", cgex4In);
            }
            if(CollectionUtils.isNotEmpty(cgex4Like)){
            	int i = 0;
            	for(String cgexlike : cgex4Like){
            		params.put(paramHead + "cgexlike" + i, cgexlike.replaceAll("\\*", "\\%"));
            		
            		i++;
            	}
            }
        }
        
        //4.prop的参数处理
        if(CollectionUtils.isNotEmpty(propsMap)){
            int i = 1;
            for(String key : propsMap.keySet()){
                if(key.endsWith("_GT") || key.endsWith("_LT") || key.endsWith("_LE") || key.endsWith("_GE")){//时间 
                    int j = 0;
                    for(String value : propsMap.get(key)){
                        params.put(paramHead + "prop" + i + j, Timestamp.valueOf(value).getTime());//FIXME
                        
                        if(!isCount){
                        	params.put("ndrprop" + i + j, Timestamp.valueOf(value).getTime());
                        }
                        
                        j++;
                    }
                }else if(key.endsWith("_LIKE")){//like
                    int j = 0;
                    for(String likeValue : propsMap.get(key)){
                        params.put(paramHead + "prop" + i + j, "%" + likeValue + "%");
                        
                        j++;
                    }
                }else{
                    params.put(paramHead + "prop" + i, propsMap.get(key));
                }
                
                i++;
            }
        }
        
        //5.words的参数处理
        if(StringUtils.isNotEmpty(words)){
            params.put(paramHead + "twords", "%" + words + "%");
            params.put(paramHead + "dwords", "%" + words + "%");
            params.put(paramHead + "kwords", "%" + words + "%");
            params.put(paramHead + "crdwords", "%" + words + "%");
            params.put(paramHead + "tagswords", "%" + words + "%");
            if(!IndexSourceType.LessonType.getName().equals(resType) && 
                    !IndexSourceType.InstructionalObjectiveType.getName().equals(resType)){
                params.put(paramHead + "edudwords", "%" + words + "%");
            }
        }
        
        //6.printable & printableKey
        if(printable!=null && StringUtils.isNotEmpty(printableKey)){
        	params.put(paramHead + "filekey", printableKey);
        }
        
        //7.Map<String, String> orderMap, String statisticsType, String statisticsPlatform
        if(!isCount){
        	if(CollectionUtils.isNotEmpty(orderMap) && orderMap.containsKey("key_value")){
        		params.put("st", statisticsType);
        		params.put("sp", statisticsPlatform);
        	}
        }
        
        //8.tags
        if(CollectionUtils.isNotEmpty(tags)){
        	int i = 1;
        	for(String tag : tags){
        		if(StringUtils.hasText(tag)){
        			List<String> tagAndOp = Arrays.asList(tag.split(" and "));
        			if(CollectionUtils.isNotEmpty(tagAndOp)){
        				int j = 1;
        				for(String tagAnd : tagAndOp){
        					if(StringUtils.hasText(tagAnd)){
        						params.put(paramHead + "tag" + i + j, "%\"" + tagAnd + "\"%");
        						
        						j++;
        					}
        				}
        			}
        			
        			i++;
        		}
        	}
        }
        
        return params;
    }
    
    @Override
    public int getResourceQueryCount(final String sql, final Map<String, Object> param,
            boolean flag, DbName dbName){
        
        boolean newValueFlag = false;
        //返回的数目
        int count = 0;
        
        //通过Redis获取
        QueryResultModel qrm = getCount(sql, param);
        if(qrm == null){
            //3、需要根据sql去查询数据，比较耗时
            count = queryCount(sql, param, dbName);
            newValueFlag = true;
            
            //4、保存数据
            saveCount(sql, param, count);
        }else{
            count = qrm.getCount();
        }
        
        if(flag && !newValueFlag){
            //4、新起一个线程去更新
            updateQueryResultCount(sql,param,REDIS_NUM_SUFFIX,dbName);
        }
        
        return count;
    }
    
    /**
     * 根据子查询in或exists的SQL查询缓存表的count数目，如果两者都没有，返回-1，否则返回最近时间的count
     * 
     * @author:xuzy
     * @date:2015年12月3日
     * @param inSql
     * @param existSql
     * @param param
     * @return
     */
    public int getPreSqlCount(String sql, Map<String,Object> param){
        int count = -1;
        QueryResultModel qrm = getCount(sql,param);
        if(qrm != null){
            count = qrm.getCount();
        }
        return count;
    }
    
    /**
     * 通过数据库获取total 
     * <p>Create Time: 2016年1月14日   </p>
     * <p>Create author: xuzy   </p>
     * @param sql
     * @param param
     * @return
     */
    private int queryCount(String sql,Map<String,Object> param,DbName dbName){
        Query query = getEntityManagerByDBName(dbName).createNativeQuery(sql);
        
        Set<String> ks = param.keySet();
        Iterator<String> it =  ks.iterator();
        while(it.hasNext()){
            String key = it.next();
            query.setParameter(key, param.get(key));
        }
        
        BigInteger c = (BigInteger)query.getSingleResult();
        if(c != null){
            return c.intValue();
        }
        return 0;
    }
    
    /**
     * 新起线程更新数据(先查询count值再更新)
     * 
     * @author:xuzy
     * @date:2015年12月4日
     * @param sql
     * @param param
     * @param qrc
     */
    private void updateQueryResultCount(final String sql,final Map<String,Object> param,final String type,final DbName dbName){
        CommonHelper.sortMap(param);
        final int sqlHashCode = sql.hashCode();
        final int paramHashCode = param.hashCode();
        StringBuilder threadName = new StringBuilder();
        threadName.append(sqlHashCode);
        threadName.append(paramHashCode);
        threadName.append(type);
        if(!judgeThreadIsAlive(threadName.toString())){
            final String n = threadName.toString();
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    if(type.equals(REDIS_NUM_SUFFIX)){
                        int newCount = queryCount(sql,param,dbName);
                        saveCount(sql, param, newCount);
                    }else if(type.equals(REDIS_RESULT_SUFFIX)){
                        saveResult(sql, param, dbName);
                    }

					if(threadNameMap.containsKey(n)){
						threadNameMap.remove(n);
					}
				}
			});
			threadNameMap.put(n, "");
			executorService.execute(thread);
		}
	}
	
	
	/**
	 * 根据线程名判断线程是否还存活
	 * 
	 * @author:xuzy
	 * @date:2015年12月3日
	 * @param threadName
	 * @return
	 */
	private boolean judgeThreadIsAlive(String threadName){

		return threadNameMap.containsKey(threadName);
	}
	
	/**
	 * 保存新的total值到Redis	
	 * <p>Create Time: 2016年1月14日   </p>
	 * <p>Create author: xuzy   </p>
	 * @param sql
	 * @param paramMap
	 * @param count
	 */
	private void saveCount(String sql,Map<String,Object> paramMap,int count){
		int hashCode = CommonHelper.getHashCodeKey(sql,paramMap);
		QueryResultModel qrm = new QueryResultModel();
		qrm.setCount(count);
		qrm.setLastUpdate(System.currentTimeMillis());
		ertCount.set(hashCode+REDIS_NUM_SUFFIX, qrm,1,TimeUnit.DAYS);
	}
	
	/**
	 * 通过Redis获取total
	 * <p>Create Time: 2016年1月14日   </p>
	 * <p>Create author: xuzy   </p>
	 * @param sql
	 * @param paramMap
	 * @return
	 */
	private QueryResultModel getCount(String sql,Map<String,Object> paramMap){
		int hashCode = CommonHelper.getHashCodeKey(sql,paramMap);
		QueryResultModel value = ertCount.get(hashCode+REDIS_NUM_SUFFIX,QueryResultModel.class);
		return value;
	}
	
	/**
	 * 缓存items到Redis	
	 * <p>Create Time: 2016年1月14日   </p>
	 * <p>Create author: xuzy   </p>
	 * @param sql
	 * @param paramMap
	 */
    private void saveResult(String sql,Map<String,Object> paramMap,DbName dbName){
        int hashCode = CommonHelper.getHashCodeKey(sql,paramMap);
		
		String limitSql = sql + " LIMIT 0,500 ";
        //查询
        Query query = getEntityManagerByDBName(dbName).createNativeQuery(limitSql, FullModel.class);
        //参数设置
        for(String paramKey : paramMap.keySet()){
            query.setParameter(paramKey, paramMap.get(paramKey));
        }
        
        List<FullModel> queryResult = query.getResultList();
        
        //保存到redis
        ert.zSet(hashCode+REDIS_RESULT_SUFFIX, queryResult);
        
        //设置过期时间
        ert.expire(hashCode+REDIS_RESULT_SUFFIX, 10l,TimeUnit.MINUTES);
	}
	
	/**
	 * 通过Redis获取记录数items，redis缓存目前只缓存500条
	 * <p>Create Time: 2016年1月12日   </p>
	 * <p>Create author: xuzy   </p>
	 * @param sql
	 * @param paramMap
	 * @param limit
	 * @return
	 */
    private List<FullModel> getResult(String sql, Map<String, Object> paramMap, String limit) {
        int hashCode = CommonHelper.getHashCodeKey(sql,paramMap);
        Integer result[] = ParamCheckUtil.checkLimit(limit);
        long offSet = (long)result[0];
        long num = (long)result[1];
        String key = hashCode+REDIS_RESULT_SUFFIX;
        
        //判断key是否存在
        boolean flag = ert.existKey(key);
        
        if(!flag){
            return null;
        }
        
        //取出KEY对应缓存总数
        long count = ert.zSetCount(key);
        
        if(offSet >= count){
            return new ArrayList<FullModel>();
        }
        
        long toIndex = offSet + num;
        if(toIndex > count){
            toIndex = count;
        }
        
        //根据分页参数取出缓存数据
        List<FullModel> fmList = ert.zRangeByScore(key, offSet, toIndex - 1, FullModel.class);
        return fmList;
    }
    
    /**
     * 	获取对应的数据库的Name
     * <p>Create Time: 2016年2月16日   </p>
     * <p>Create author: xiezy   </p>
     * @param onlyOneType
     * @param resTypes
     * @return
     */
    private DbName getDBName4CommonQuery(boolean onlyOneType,List<String> resTypes){
    	if(onlyOneType){
    		if(resTypes.get(0).equals(IndexSourceType.QuestionType.getName()) ||
    				resTypes.get(0).equals(IndexSourceType.SourceCourseWareObjectType.getName())){
    			return DbName.QUESTION;
    		}
    	}else if(resTypes.size()==2 
    			&& resTypes.contains(IndexSourceType.QuestionType.getName())
    			&& resTypes.contains(IndexSourceType.SourceCourseWareObjectType.getName())){
    		return DbName.QUESTION;
    	}
    	
    	return DbName.DEFAULT;
    }
    
    /**
     * 获取对应库的EntityManager
     * <p>Create Time: 2016年2月16日   </p>
     * <p>Create author: xiezy   </p>
     * @param dbName
     * @return
     */
    private EntityManager getEntityManagerByDBName(DbName dbName){
        if(dbName.equals(DbName.QUESTION)){
            return questionEm;
        }else{
            return defaultEm;
        }
    }
    
    /**
     * 获取对应库的JdbcTemplate
     * <p>Create Time: 2016年3月29日   </p>
     * <p>Create author: xiezy   </p>
     * @param dbName
     * @return
     */
    private JdbcTemplate getJdbcTemplateByDBName(DbName dbName){
    	if(dbName.equals(DbName.QUESTION)){
            return questionJdbcTemplate;
        }else{
            return defaultJdbcTemplate;
        }
    }
    
	@Override
	public int queryCountByResId(String resType, String identifier) {
		String sql = "select count(identifier) from ndresource where identifier = '" + identifier + "'";
		Query query = defaultEm.createNativeQuery(sql);
		BigInteger num = (BigInteger)query.getSingleResult();
		return num.intValue();
	}

	@Override
	public int queryCountByResId4QuestionDb(String resType, String identifier) {
		String sql = "select count(identifier) from ndresource where identifier = '" + identifier + "'";
		Query query = questionEm.createNativeQuery(sql);
		BigInteger num = (BigInteger)query.getSingleResult();
		return num.intValue();
	}

	/**
	 * 先加上enable=1的限制，主要为了解决前端由于误删除之类造成无法重新创建相同code的问题  by xuzy
	 * @date 2016-03-23
	 */
	@Override
	public int queryCodeCountByResId(String resType, String identifier,String code) {
		String sql = "select count(identifier) from ndresource where primary_category = '" + resType + "' and enable = 1 and code = '" + code + "' and identifier <> '"+identifier+"'";
		Query query = defaultEm.createNativeQuery(sql);
		BigInteger num = (BigInteger)query.getSingleResult();
		return num.intValue();
	}

	@Override
	public int queryCodeCountByResId4QuestionDb(String resType, String identifier,String code){
		String sql = "select count(identifier) from ndresource where primary_category = '" + resType + "' and enable = 1 and code = '" + code + "' and identifier <> '"+identifier+"'";
		Query query = questionEm.createNativeQuery(sql);
		BigInteger num = (BigInteger)query.getSingleResult();
		return num.intValue();
	}
	
	public List<Map<String,Object>> queryResourceByMid(String resType,String mid){
		String sql = "select nd.identifier,nd.title,nd.version,nd.create_time,nd.estatus,c.message from ndresource nd left join contributes c on nd.identifier = c.resource and c.title = 'version' where nd.primary_category='"
				+ resType
				+ "' and nd.m_identifier = '"
				+ mid
				+ "' and nd.enable = 1";
		if(CommonServiceHelper.isQuestionDb(resType)){
			return questionJdbcTemplate.queryForList(sql);
		}
		return defaultJdbcTemplate.queryForList(sql);
	}
	
	public void batchUpdateSql(String resType,String[] sqls){
		if(CommonServiceHelper.isQuestionDb(resType)){
			questionJdbcTemplate.batchUpdate(sqls);
		}
		defaultJdbcTemplate.batchUpdate(sqls);
	}

	@Override
	public Map<String, ChapterStatisticsViewModel> statisticsCountsByChapters(
			String resType, String tmId, Set<String> chapterIds,
			List<String> coverages, Set<String> categories, boolean isAll) {
		StringBuilder sql = new StringBuilder("");
		sql.append("SELECT rr.source_uuid AS chapterid,COUNT(DISTINCT a.identifier) AS counts ");
		sql.append("FROM ndresource a INNER JOIN resource_relations rr ON a.identifier=rr.target ");
		//coverages
        if(CollectionUtils.isNotEmpty(coverages)){
        	sql.append(" INNER JOIN res_coverages rcv ON a.identifier=rcv.resource AND rcv.res_type='");
        	sql.append(resType);
        	sql.append("'");
        	sql.append(" AND (");
            if(!isAll){
            	sql.append(coverageParam4Sql4DealOnline(coverages,""));
            }else{
            	sql.append(coverageParam4Sql(coverages,""));
            }
            sql.append(")");
        }else{
        	sql.append(" INNER JOIN res_coverages rcv ON a.identifier=rcv.resource AND rcv.res_type='");
        	sql.append(resType);
            sql.append("'");
        }
        
        //categories
        Map<String,Object> dealCategoriesMap = dealAndCategories(categories);
        //带有and 的category的拆解集合
        @SuppressWarnings("unchecked")
		List<List<String>> andCategories = (List<List<String>>)dealCategoriesMap.get("allAndCategories");
        //去掉带有 and 之后的categories
        @SuppressWarnings("unchecked")
		Set<String> noAndcategories = (Set<String>)dealCategoriesMap.get("afterRemoveAllAndCategories");
        
        //categories & andCategories
        if(CollectionUtils.isNotEmpty(noAndcategories) || CollectionUtils.isNotEmpty(andCategories)){
            boolean havePath = false;
            boolean haveCode = false;
            
            if(CollectionUtils.isNotEmpty(noAndcategories)){
                for(String str4Join : noAndcategories){//目的是减少Join表
                    if(StringUtils.isNotEmpty(str4Join)){
                        if(str4Join.contains("/")){
                            havePath = true;
                        }else{
                            haveCode = true;
                        }
                    }
                }
            }
            
            if(havePath){
            	sql.append(" INNER JOIN resource_categories rc ON rc.primary_category='" + resType + "' AND a.identifier=rc.resource");
            }
            if(haveCode || CollectionUtils.isNotEmpty(andCategories)){
                //至少join一次
            	sql.append(" INNER JOIN resource_categories rcc0 ON rcc0.primary_category='" + resType + "' AND a.identifier=rcc0.resource");
                
                if(CollectionUtils.isNotEmpty(andCategories)){//注意这里的i是从1开始,使join的次数为andCategories的【maxsize-1】
                    for(int i=1;i<getMaxCount(andCategories);i++){
                    	sql.append(" INNER JOIN resource_categories rcc");
                    	sql.append(i);
                    	sql.append(" ON rcc");
                    	sql.append(i);
                    	sql.append(".primary_category='");
                    	sql.append(resType);
                    	sql.append("' AND a.identifier=rcc");
                        sql.append(i);
                        sql.append(".resource");
                    }
                }
            }
        }
        
        //WHERE
        String baseWhere = " WHERE a.enable=1 AND a.primary_category='" + resType + "'";
        if(CollectionUtils.isEmpty(coverages)){//当覆盖范围为空时
        	baseWhere = " WHERE a.enable=1 AND a.primary_category='" + resType + "'"
                    + " AND (rcv.target_type='" + CoverageConstant.TargetType.TARGET_TYPE_PB.getCode() + "'"
                    + " AND rcv.target='" + CoverageConstant.TARGET_PUBLIC + "'"
                    + " AND rcv.strategy='" + CoverageConstant.Strategy.STRATEGY_SHAREING.getCode() + "'"
                    + ")";
        }
        String categoryParamSql = "";
        if(CollectionUtils.isNotEmpty(noAndcategories) || CollectionUtils.isNotEmpty(andCategories)){
            categoryParamSql = categoryParam4Sql(noAndcategories,andCategories,"");
        }
        //关系where参数
        String relationParamSql = getRelationParamSql(resType, tmId, chapterIds);
        
        List<String> sqlWhereList = new ArrayList<String>();
        sqlWhereList.add(baseWhere);
        if(StringUtils.isNotEmpty(categoryParamSql)){
            sqlWhereList.add(categoryParamSql);
        }
        if(StringUtils.isNotEmpty(relationParamSql)){
            sqlWhereList.add(relationParamSql);
        }
        String sqlWhere = StringUtils.join(sqlWhereList, " AND ");
        
        sql.append(" ");
        sql.append(sqlWhere);
        
        //GROUP BY
        sql.append(" GROUP BY rr.source_uuid");
        
        //参数处理
        Map<String, Object> params = sqlParamDeal4Statistics(tmId, chapterIds, coverages, noAndcategories, andCategories);
        
        //最终查询sql语句LOG输出
        LOG.info("教材章节统计执行的SQL语句:" + sql.toString());
        //参数处理LOG输出
        LOG.info("教材章节统计执行的SQL的参数为:" + ObjectUtils.toJson(params));
        
        final Map<String, ChapterStatisticsViewModel> resultMap = new HashMap<String, ChapterStatisticsViewModel>();
        //查询
        NamedParameterJdbcTemplate npdt = new NamedParameterJdbcTemplate(defaultJdbcTemplate);
        npdt.query(sql.toString(), params, new RowMapper<String>(){
			@Override
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				ChapterStatisticsViewModel cs = new ChapterStatisticsViewModel();
				cs.setCounts(rs.getInt("counts"));
				
				resultMap.put(rs.getString("chapterid"), cs);
				return null;
			}
        });
        
        //获取全部需要返回的章节信息
        List<Chapter> chapters = new ArrayList<Chapter>();
        try {
        	if(CollectionUtils.isNotEmpty(chapterIds)){
            	chapters = chapterRepository.getAll(new ArrayList<String>(chapterIds));
            }else{
            	Chapter chapter4Tm = new Chapter();
            	chapter4Tm.setTeachingMaterial(tmId);
            	chapters = chapterRepository.getAllByExample(chapter4Tm);
            }
		} catch (EspStoreException e) {
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					LifeCircleErrorMessageMapper.StoreSdkFail.getCode(), "获取章节详细出错！");
		}
        
        //没有查到存在的章节,返回空
        if(CollectionUtils.isEmpty(chapters)){
        	return new HashMap<String, ChapterStatisticsViewModel>();
        }
        
        if(CollectionUtils.isNotEmpty(resultMap)){
        	if(CollectionUtils.isNotEmpty(chapters)){
				for(Chapter chapter : chapters){
					if(resultMap.containsKey(chapter.getIdentifier())){
						ChapterStatisticsViewModel cs = resultMap.get(chapter.getIdentifier());
						cs.setChapterTitle(chapter.getTitle());
						if(chapter.getParent().equals(chapter.getTeachingMaterial())){
							cs.setParent("ROOT");
						}else{
							cs.setParent(chapter.getParent());
						}
						resultMap.put(chapter.getIdentifier(), cs);
					}else{
						ChapterStatisticsViewModel cs = new ChapterStatisticsViewModel();
	            		cs.setChapterTitle(chapter.getTitle());
	            		if(chapter.getParent().equals(chapter.getTeachingMaterial())){
							cs.setParent("ROOT");
						}else{
							cs.setParent(chapter.getParent());
						}
	            		cs.setCounts(0);
	            		resultMap.put(chapter.getIdentifier(), cs);
					}
				}
			}
        }else{
        	if(CollectionUtils.isNotEmpty(chapters)){
        		for(Chapter chapter : chapters){
            		ChapterStatisticsViewModel cs = new ChapterStatisticsViewModel();
            		cs.setChapterTitle(chapter.getTitle());
            		if(chapter.getParent().equals(chapter.getTeachingMaterial())){
						cs.setParent("ROOT");
					}else{
						cs.setParent(chapter.getParent());
					}
            		cs.setCounts(0);
            		resultMap.put(chapter.getIdentifier(), cs);
            	}
        	}
        }
        
		return resultMap;
	}
	
	/**
	 * 关系相关参数处理
	 * @author xiezy
	 * @date 2016年7月13日
	 * @param resType
	 * @param tmId
	 * @param chapterIds
	 * @return
	 */
	private String getRelationParamSql(String resType,String tmId,Set<String> chapterIds){
		String innerSql = "";
		if(CollectionUtils.isNotEmpty(chapterIds)){
			innerSql = "SELECT c.identifier FROM chapters c INNER JOIN ndresource ndr";
			innerSql += " ON c.identifier=ndr.identifier";
			innerSql += " WHERE ndr.enable=1 AND ndr.primary_category='chapters' AND ndr.identifier IN (:cids)";
		}else{
			innerSql = "SELECT c.identifier FROM chapters c INNER JOIN ndresource ndr";
			innerSql += " ON c.teaching_material=ndr.identifier";
			innerSql += " WHERE ndr.enable=1 AND ndr.primary_category='teachingmaterials' AND ndr.identifier=:tmid";
		}
		
		String sql = "rr.enable=1 AND rr.res_type='chapters' AND rr.resource_target_type='"+resType+"'";
		sql += " AND rr.source_uuid IN (" + innerSql + ")";
		
		return sql;
	}
	
	/**
	 * sql参数赋值处理
	 * @author xiezy
	 * @date 2016年7月13日
	 * @param tmId
	 * @param chapterIds
	 * @param coverages
	 * @param noAndcategories
	 * @param andCategories
	 * @return
	 */
	private Map<String, Object> sqlParamDeal4Statistics(String tmId,Set<String> chapterIds,
			List<String> coverages,Set<String> noAndcategories,List<List<String>> andCategories){
		Map<String,Object> params = new HashMap<String, Object>();
		//coverage的参数处理
        if(CollectionUtils.isNotEmpty(coverages)){
            int i = 1;
            
            for(String cv : coverages){
                List<String> coverageElemnt = Arrays.asList(cv.split("/"));
                params.put("cvty" + i, coverageElemnt.get(0));
                params.put("cvt" + i, coverageElemnt.get(1));
                if(!coverageElemnt.get(2).equals("*")){
                    params.put("cvs" + i, coverageElemnt.get(2));
                }
                
                i++;
            }
        }
        //noAndcategories的参数处理
        if(CollectionUtils.isNotEmpty(noAndcategories)){
            int i = 1;
            for(String cg : noAndcategories){
                if(cg.contains("/")){
                    if(cg.contains("*")){
                        params.put("cgpathlike" + i, cg.replaceAll("\\*", "\\%"));
                    }else{
                        params.put("cgpath" + i, cg);
                    }
                }else{
                    if(cg.contains("*")){
                        params.put("cgcodelike" + i, cg.replaceAll("\\*", "\\%"));  
                    }else{
                        params.put("cgcode" + i, cg);
                    }
                }
                
                i++;
            }
        }
        //andCategories的参数处理
        if(CollectionUtils.isNotEmpty(andCategories)){
            int k = 1;
            for(List<String> andCategory : andCategories){
                int j = 0;//rcc0以0开始
                for(String ac : andCategory){
                    if(ac.contains("*")){//使用通配符
                        params.put("andcgcodelike" + j + k, ac.replaceAll("\\*", "\\%"));  
                    }else{
                        params.put("andcgcode" + j + k, ac);
                    }
                    
                    j++;
                }
                
                k++;
            }
        }
        
        if(CollectionUtils.isNotEmpty(chapterIds)){
        	params.put("cids", chapterIds);
        }else{
        	params.put("tmid", tmId);
        }
        
        return params;
	}
	
	//********************************通用资源删除DAO模块********************************\\
//    @Override
//    public void deleteRelationByChapters(String mid) {
//        //获取mid相关章节id集合的子查询sql语句
//        String subQuerySql4ChapterIds = "SELECT ndr.identifier FROM ndresource ndr,chapters ch "
//                     + "WHERE ndr.primary_category='chapters' AND ndr.enable=1 "
//                     +"AND ndr.identifier=ch.identifier AND ch.teaching_material='" + mid + "'";
//        
//        //删除资源关系sql语句
//        String updateSql = "UPDATE resource_relations SET enable=0 WHERE enable=1 AND ("
//                + "res_type='chapters' AND source_uuid IN (" + subQuerySql4ChapterIds + ")) "
//                + "OR (resource_target_type='chapters' AND target IN (" + subQuerySql4ChapterIds + "))";
//        
//        Query query = defaultEm.createNativeQuery(updateSql);
//        query.executeUpdate();
//    }
//
//    @Override
//    public void deleteChapters(String mid) {
//        String updateSql = "UPDATE ndresource SET enable=0 WHERE primary_category='chapters' "
//                + "AND identifier IN (SELECT ch.identifier FROM chapters ch WHERE ch.teaching_material='" + mid + "')";
//    
//        Query query = defaultEm.createNativeQuery(updateSql);
//        query.executeUpdate();
//    }
	//********************************通用资源删除DAO模块********************************\\
    
  //****************************资源统计模块-暂不使用****************************\\
//    @Override
//    public List<ResourceStatisticsViewModel> resourceStatistics(String categoryPath, Set<String> resourceTypes,
//            Map<String,String> relationsMap, List<String> coverages, Map<String, Set<String>> propsMap) {
//        //处理并分类resourceTypes
//        Map<String, List<String>> map = dealAndSortResourceTypes(resourceTypes);
//        //用于放querySql和其params的集合
//        List<String> sqlList = new ArrayList<String>();
//        List<Map<String, Object>> paramList = new ArrayList<Map<String,Object>>();
//        
//        if(CollectionUtils.isNotEmpty(map)){
//            for(String tableName : map.keySet()){
//                //获取ndCodes
//                List<String> ndCodes = map.get(tableName);
//                
//                if (LifeCircleApplicationInitializer.tablenames_properties.getProperty(
//                        IndexSourceType.QuestionType.getName()).equals(tableName)) {//习题特殊处理
//                    if(CollectionUtils.isNotEmpty(ndCodes)){
//                        for(String qt : ndCodes){
//                            if(StringUtils.isNotEmpty(qt)){
//                                //Select
//                                String sqlSelect = "SELECT COUNT(DISTINCT a.identifier) AS counts,a.question_type AS taxOnCode";
//                                //From Join On
//                                String sqlFromAndJoinAndOn = "FROM " + tableName + " a";
//                                
//                                if(!CollectionUtils.isEmpty(relationsMap)){//为习题时,不关联覆盖范围表,使用习题表中的res_coverages字段
//                                    List<Map<String,String>> relations = new ArrayList<Map<String,String>>();
//                                    relations.add(relationsMap);
//                                    
//                                    sqlFromAndJoinAndOn = "FROM (" + tableName + " a INNER JOIN resource_relations rr ON a.identifier=rr.target AND (" + relationParam4Sql(null,relations,false,"") + ")) ";
//                                }
//                                
//                                //Where
//                                String sqlWhere = "WHERE a.enable=1";
//                                //question_type
//                                String categoryParamSql = "";
//                                if(!CollectionUtils.isEmpty(ndCodes)){
//                                    categoryParamSql = "a.question_type=:qt";
//                                }
//                                //categoryPath
//                                String categoryPathParamSql = "";
//                                if(StringUtils.isNotEmpty(categoryPath)){
//                                    categoryPathParamSql = "a.categories LIKE :cpath";
//                                }
//                                //coverages
//                                String coverageParamSql = "";
//                                if(CollectionUtils.isNotEmpty(coverages)){
//                                    List<String> coverageCondition = new ArrayList<String>();
//                                    for(int i=0;i<coverages.size();i++){
//                                        String condition = "a.res_coverages LIKE :cv" + i;
//                                        if(i==0){
//                                            condition = "(" + condition;
//                                        }
//                                        if(i==(coverages.size()-1)){
//                                            condition = condition + ")";
//                                        }
//                                        
//                                        coverageCondition.add(condition);
//                                    }
//                                    
//                                    coverageParamSql = StringUtils.join(coverageCondition, " OR ");
//                                }
//                                
//                                String propParamSql = "";
//                                if(!CollectionUtils.isEmpty(propsMap)){
//                                    propParamSql = propParam4Sql(propsMap,"");
//                                }
//                                
//                                //整理合并Where条件
//                                List<String> sqlWhereList = new ArrayList<String>();
//                                sqlWhereList.add(sqlWhere);
//                                
//                                if(StringUtils.isNotEmpty(categoryParamSql)){
//                                    sqlWhereList.add(categoryParamSql);
//                                }
//                                if(StringUtils.isNotEmpty(categoryPathParamSql)){
//                                    sqlWhereList.add(categoryPathParamSql);
//                                }
//                                if(StringUtils.isNotEmpty(coverageParamSql)){
//                                    sqlWhereList.add(coverageParamSql);
//                                }
//                                if(StringUtils.isNotEmpty(propParamSql)){
//                                    sqlWhereList.add(propParamSql);
//                                }
//                                
//                                sqlWhere = StringUtils.join(sqlWhereList, " AND ");
//                                
//                                //Group By
//                                String sqlGroupBy = "GROUP BY a.question_type";
//                                
//                                //查询的sql
//                                String querySql = sqlSelect + " " + sqlFromAndJoinAndOn + " " + sqlWhere + " " + sqlGroupBy;
//                                
//                                LOG.info("资源类型统计From--{}--执行的SQL语句:" + querySql,tableName+"--"+qt);
//                                
//                                //参数处理
//                                List<Map<String, String>> relationList = new ArrayList<Map<String,String>>();
//                                if(CollectionUtils.isNotEmpty(relationsMap)){
//                                    relationList.add(relationsMap);
//                                }
//                                
//                                //习题的参数处理
//                                Map<String,Object> params = sqlParamsDeal("",null, relationList, null, propsMap, "",false,"");
//                                //特殊的参数处理
//                                //categoryPath
//                                if(StringUtils.isNotEmpty(categoryPath)){
//                                    params.put("cpath", "%" + categoryPath.replaceAll("\\*", "\\%") + "%"); 
//                                }
//                                //question_type
//                                params.put("qt", LifeCircleApplicationInitializer.ndCode_properties.
//                                        getProperty("qt_" + qt));
////                                Set<String> qtList = new HashSet<String>();
////                                if(CollectionUtils.isNotEmpty(ndCodes)){
////                                    for(String code : ndCodes){
////                                        if(code.equals("$RE0200")){//代表所有的习题类型
////                                            String allType = LifeCircleApplicationInitializer.ndCode_properties.
////                                                    getProperty("qt_" + code);
////                                            List<String> types = Arrays.asList(allType.split(","));
////                                            qtList.addAll(new HashSet<String>(types));
////                                            
////                                            break;
////                                        }else{//某种习题类型
////                                            qtList.add(LifeCircleApplicationInitializer.ndCode_properties.
////                                                    getProperty("qt_" + code));
////                                        }
////                                    }
////                                    params.put("qtList", qtList);
////                                }
//                                //coverages
//                                if(CollectionUtils.isNotEmpty(coverages)){
//                                    int i = 0;
//                                    for(String coverage : coverages){
//                                        coverage = coverage.replaceAll("\\*", "");
//                                        params.put("cv" + i, "%" + coverage + "%");
//                                        
//                                        i++;
//                                    }
//                                }
//                                
//                                LOG.info("资源类型统计From--{}--执行的SQL的参数为:" + ObjectUtils.toJson(params),tableName+"--"+qt);
//                                
//                                //放入集合中
//                                sqlList.add(querySql);
//                                paramList.add(params);
//                            }
//                        }
//                    }
//                }else{//其他资源
//                    //Select
//                    String sqlSelect = "SELECT COUNT(DISTINCT a.identifier) AS counts,rcc.taxOnCode AS taxOnCode";
//                    
//                    //From Join On
//                    String sqlFromAndJoinAndOn = "FROM " + tableName + " a";
//                    //关联关系和覆盖范围表
//                    if(!CollectionUtils.isEmpty(relationsMap) && !CollectionUtils.isEmpty(coverages)){//关系和覆盖范围都有的情况
//                        List<Map<String,String>> relations = new ArrayList<Map<String,String>>();
//                        relations.add(relationsMap);
//                        
//                        sqlFromAndJoinAndOn = "FROM ((" + tableName + " a INNER JOIN resource_relations rr ON a.identifier=rr.target AND (" + relationParam4Sql(null,relations,false,"") + ")) "
//                                + "INNER JOIN res_coverages rcv ON a.identifier=rcv.resource AND (" + coverageParam4Sql(coverages,"") + "))";
//                    }else if(!CollectionUtils.isEmpty(relationsMap) && CollectionUtils.isEmpty(coverages)){//只有关系
//                        List<Map<String,String>> relations = new ArrayList<Map<String,String>>();
//                        relations.add(relationsMap);
//                        
//                        sqlFromAndJoinAndOn = "FROM (" + tableName + " a INNER JOIN resource_relations rr ON a.identifier=rr.target AND (" + relationParam4Sql(null,relations,false,"") + ")) ";
//                    }else if(!CollectionUtils.isEmpty(coverages) && CollectionUtils.isEmpty(relationsMap)){//只有覆盖范围
//                        sqlFromAndJoinAndOn = "FROM (" + tableName + " a INNER JOIN res_coverages rcv ON a.identifier=rcv.resource AND (" + coverageParam4Sql(coverages,"") + ")) ";
//                    }
//                    
//                    
//                    //关联resource_categories表
//                    if(StringUtils.isNotEmpty(categoryPath)){
//                        sqlFromAndJoinAndOn += " LEFT JOIN resource_categories rc ON a.identifier=rc.resource";
//                        ndCodes.add(categoryPath);//其实categoryPath也是在resource_categories表中,将其加入作为条件
//                    }
//                    if(CollectionUtils.isNotEmpty(ndCodes)){//这个判断条件,应当都为true
//                        sqlFromAndJoinAndOn += " LEFT JOIN resource_categories rcc ON a.identifier=rcc.resource";
//                    }
//                    
//                    //Where
//                    String sqlWhere = "WHERE a.enable=1";
//                    
//                    String categoryParamSql = "";
//                    if(!CollectionUtils.isEmpty(ndCodes)){
//                        categoryParamSql = categoryParam4Sql(new HashSet<String>(ndCodes),"");
//                    }
//                    
//                    String propParamSql = "";
//                    if(!CollectionUtils.isEmpty(propsMap)){
//                        propParamSql = propParam4Sql(propsMap,"");
//                    }
//                    
//                    //整理合并Where条件
//                    List<String> sqlWhereList = new ArrayList<String>();
//                    sqlWhereList.add(sqlWhere);
//                    
//                    if(StringUtils.isNotEmpty(categoryParamSql)){
//                        sqlWhereList.add(categoryParamSql);
//                    }
//                    if(StringUtils.isNotEmpty(propParamSql)){
//                        sqlWhereList.add(propParamSql);
//                    }
//                    
//                    sqlWhere = StringUtils.join(sqlWhereList, " AND ");
//                    
//                    //Group By
//                    String sqlGroupBy = "GROUP BY rcc.taxOnCode";
//                    
//                    //查询的sql
//                    String querySql = sqlSelect + " " + sqlFromAndJoinAndOn + " " + sqlWhere + " " + sqlGroupBy;
//                    
//                    LOG.info("资源类型统计From--{}--执行的SQL语句:" + querySql,tableName);
//                    
//                    //参数处理
//                    List<Map<String, String>> relationList = new ArrayList<Map<String,String>>();
//                    if(CollectionUtils.isNotEmpty(relationsMap)){
//                        relationList.add(relationsMap);
//                    }
//                    Map<String,Object> params = sqlParamsDeal("",new HashSet<String>(ndCodes), relationList, coverages, propsMap, "",false,"");
//                    
//                    LOG.info("资源类型统计From--{}--执行的SQL的参数为:" + ObjectUtils.toJson(params),tableName);
//                    
//                    //放入集合中
//                    sqlList.add(querySql);
//                    paramList.add(params);
//                }
//            }
//        }
//        
//        //查询 + 处理返回结果
//        ForkJoinPool forkJoinPool = new ForkJoinPool();  
//        Future<List<ResourceStatisticsViewModel>> result = forkJoinPool.submit(new ResStatisticsThread(sqlList,paramList));  
//        List<ResourceStatisticsViewModel> resultList = null;
//        try {
//            resultList = result.get();
//        } catch (InterruptedException e) {
//            LOG.error("实时统计出错了！",e);
//        } catch (ExecutionException e) {
//            LOG.error("实时统计出错了！",e);
//        }
//        
//        //特殊处理,没有返回的count为0
//        if(CollectionUtils.isNotEmpty(resourceTypes) && resultList != null
//                && (resourceTypes.size() > resultList.size())){
//            for(String code : resourceTypes){
//                boolean isNeedZero = true;
//                for(ResourceStatisticsViewModel rsvm : resultList){
//                    if(code.equals(rsvm.getResourceType())){//数量不为0,已统计
//                        isNeedZero = false;
//                        break;
//                    }
//                }
//                
//                if(isNeedZero){
//                    ResourceStatisticsViewModel needZero = new ResourceStatisticsViewModel();
//                    needZero.setResourceType(code);
//                    needZero.setCount(0);
//                    resultList.add(needZero);
//                }
//            }
//        }
//        
//        return resultList;
//    }
//    
//    /**
//     * 处理和分类resourceTypes   
//     * <p>Create Time: 2015年9月14日   </p>
//     * <p>Create author: xiezy   </p>
//     * @param resourceTypes
//     * @return      key为表名      value为ndCode集合
//     */
//    private Map<String, List<String>> dealAndSortResourceTypes(Set<String> resourceTypes){
//        Map<String, List<String>> map = new HashMap<String, List<String>>();
//        if(CollectionUtils.isNotEmpty(resourceTypes)){
//            for(String ndCode : resourceTypes){
//                if(StringUtils.isNotEmpty(ndCode)){
//                    // 获取ndCode对应的tableName
//                    String tableName = LifeCircleApplicationInitializer.ndCode_properties.getProperty(ndCode);
//                    if(StringUtils.isNotEmpty(tableName)){
//                        if(map.containsKey(tableName)){
//                            List<String> existList = map.get(tableName);
//                            existList.add(ndCode);
//                        }else{
//                            List<String> ndCodes = new ArrayList<String>();
//                            ndCodes.add(ndCode);
//                            map.put(tableName, ndCodes);
//                        }
//                    }else{//不支持的类型,报错
//                        LOG.error("不支持--{}--的统计",ndCode);
//                        throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
//                                LifeCircleErrorMessageMapper.CommonStatisticsParamError.getCode(),
//                                "不支持--" + ndCode + "--的统计");
//                    }
//                }
//            }
//        }
//        
//        return map;
//    }
//    
//    @Override
//    public TimingStatisticsViewModel resourceTimingStatistics(Set<String> resourceTypes, final String timeUnit, int offset, int limit) {
//        //参数处理
//        Map<String,Object> params = new HashMap<String, Object>();
//        
//        //Select
//        String sqlSelect = "SELECT r.total_time AS totalTime,r.total_value AS totalValue,r.ndCode AS ndCode";
//        //From
//        String sqlFrom = "FROM report_res_count r";
//        //Where
////        String sqlWhere = "WHERE r.total_type='" + timeUnit + "' AND r.ndCode IN ('" + StringUtils.join(resourceTypes, "','") + "')";
//        int i = 0;
//        List<String> ndCodes = new ArrayList<String>();
//        for(String ndCode : resourceTypes){
//            String codeSql = "";
//            if(ndCode.contains("*")){//使用通配符
//                codeSql = "r.ndCode LIKE :codelike" + i;
//                
//                params.put("codelike" + i, ndCode.replaceAll("\\*", "\\%"));
//            }else{
//                codeSql = "r.ndCode=:code" + i; 
//                
//                params.put("code" + i, ndCode);
//            }
//            
//            ndCodes.add(codeSql);
//            i++;
//        }
//        
//        String sqlWhere = "WHERE r.total_type=:tt AND (" + StringUtils.join(ndCodes, " OR ") + ")";
//        
//        params.put("tt", timeUnit);
//        
//        // 格式化日期
//        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
//        if(!timeUnit.equals(TimeUnitConstant.TIMEUNIT_NONE)){//不等于none时,需加入时间的判断条件
//            //获取当前时间
//            Date now = new Date();
//            //统计的截止时间
//            Date end = getDateBeforeOrAfterOnZero(now, offset, true, timeUnit);
//            //统计的开始时间
//            Date start = getDateBeforeOrAfterOnZero(end, limit, true, timeUnit);
//            sqlWhere += " AND unix_timestamp(r.total_time) > unix_timestamp('" + sdf.format(start) + "') "
//                    + "AND unix_timestamp(r.total_time) <= unix_timestamp('" + sdf.format(end) + "')";
//        }
//        //Order by
//        String sqlOrder = "ORDER BY r.total_time DESC";
//        
//        //查询的sql
//        String querySql = sqlSelect + " " + sqlFrom + " " + sqlWhere + " " + sqlOrder;
//        LOG.info("资源类型的定时统计执行的SQL语句:" + querySql);
//        
//        //查询 + 处理返回结果
//        final Map<String, List<ResourceStatisticsViewModel>> resultMap = new LinkedHashMap<String, List<ResourceStatisticsViewModel>>();
//        //支持命名参数特性
//        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(repository.getJdbcTemple());
//        namedParameterJdbcTemplate.query(querySql, params, new RowMapper<ResourceStatisticsViewModel>(){
//
//            @Override
//            public ResourceStatisticsViewModel mapRow(ResultSet rs, int rowNum) throws SQLException {
//                ResourceStatisticsViewModel rsvm = new ResourceStatisticsViewModel();
//                rsvm.setResourceType(rs.getString("ndCode"));
//                rsvm.setCount(rs.getInt("totalValue"));
//                
//                //map的key值
//                String key = "";
//                if(timeUnit.equals(TimeUnitConstant.TIMEUNIT_NONE)){
//                    key = TimeUnitConstant.NONE_KEY;
//                }else{
//                    key = sdf.format(rs.getDate("totalTime"));
//                }
//                
//                if(resultMap.containsKey(key)){//已有记录
//                    List<ResourceStatisticsViewModel> list = resultMap.get(key);
//                    list.add(rsvm);
//                }else{
//                    List<ResourceStatisticsViewModel> list = new ArrayList<ResourceStatisticsViewModel>();
//                    list.add(rsvm);
//                    
//                    resultMap.put(key, list);
//                }
//                
//                return null;
//            }
//            
//        });
//        
//        TimingStatisticsViewModel tsvm = new TimingStatisticsViewModel();
//        tsvm.setRows(resultMap.size());
//        tsvm.setItems(resultMap);
//        return tsvm;
//    }
//    
//    @Override
//    public StatisticsByDayViewModel resourceStatisticsByDay(int offset, int limit) {
//        //Select
//        String sqlSelect = "SELECT r.total_time AS totalTime,r.total_value AS totalValue,r.ndCode AS ndCode";
//        //From
//        String sqlFrom = "FROM report_res_count r";
//        //Where
////        String sqlWhere = "WHERE r.total_type='" + TimeUnitConstant.TIMEUNIT_DAY + "'";
//        String sqlWhere = "WHERE r.total_type=:tt";
//        //Order by
//        String sqlOrder = "ORDER BY r.total_time DESC";
//        //Limit
//        String sqlLimit = "LIMIT " + offset + "," + limit;
//        
//        //查询的sql
//        String querySql = sqlSelect + " " + sqlFrom + " " + sqlWhere + " " + sqlOrder + " " + sqlLimit;
//        LOG.info("资源类型每天的增量统计执行的SQL语句:" + querySql);
//        
//        //参数处理
//        Map<String,Object> params = new HashMap<String, Object>();
//        params.put("tt", TimeUnitConstant.TIMEUNIT_DAY);
//        
//        final List<ResourceStatisticsViewModel> resultList = new ArrayList<ResourceStatisticsViewModel>();
//        //支持命名参数特性
//        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(repository.getJdbcTemple());
//        namedParameterJdbcTemplate.query(querySql, params, new RowMapper<ResourceStatisticsViewModel>(){
//
//            @Override
//            public ResourceStatisticsViewModel mapRow(ResultSet rs, int rowNum) throws SQLException {
//                ResourceStatisticsViewModel rsvm = new ResourceStatisticsViewModel();
//                rsvm.setResourceType(rs.getString("ndCode"));
//                rsvm.setCount(rs.getInt("totalValue"));
//                rsvm.setRecordDate(rs.getDate("totalTime"));
//                
//                resultList.add(rsvm);
//                return null;
//            }
//            
//        });
//        
//        StatisticsByDayViewModel sbdv = new StatisticsByDayViewModel();
//        sbdv.setRows(resultList.size());
//        sbdv.setItems(resultList);
//        return sbdv;
//    }
//    
//    /**
//     * 计算时间,并根据unit将日期归为起始值
//     * <p>Create Time: 2015年8月18日   </p>
//     * <p>Create author: xiezy   </p>
//     * @param date      目标日期
//     * @param offset    偏移量
//     * @param isBefore  往前还是往后
//     * @param unit      计算的单位(天,月,年)
//     * @return
//     */
//    private Date getDateBeforeOrAfterOnZero(Date date,int offset,boolean isBefore,String unit){
//        Calendar cal = Calendar.getInstance();
//        cal.setTime(date);
//        cal.set(Calendar.HOUR_OF_DAY, 0);
//        cal.set(Calendar.MINUTE, 0);
//        cal.set(Calendar.SECOND, 0);
//        cal.set(Calendar.MILLISECOND, 0);
//        
//        if(isBefore){
//            offset = -offset;
//        }
//        if(unit.equals(TimeUnitConstant.TIMEUNIT_DAY)){
//            cal.set(Calendar.DATE, cal.get(Calendar.DATE) + offset);
//        }else if(unit.equals(TimeUnitConstant.TIMEUNIT_MOUTH)){
//            cal.set(Calendar.DATE, 1);
//            cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + offset);
//        }else if(unit.equals(TimeUnitConstant.TIMEUNIT_YEAR)){
//            cal.set(Calendar.DATE, 1);
//            cal.set(Calendar.MONTH, Calendar.JANUARY);
//            cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) + offset);
//        }
//        
//        return cal.getTime();
//    }
//    
//    /**
//     * 实时统计ForkJoin类
//     * <p>Create Time: 2015年9月14日           </p>
//     * @author xuzy
//     */
//    class ResStatisticsThread extends RecursiveTask<List<ResourceStatisticsViewModel>>{
//        private static final long serialVersionUID = 1L;
//        private List<String> sqlList;
//        private List<Map<String,Object>> pmList;
//        public ResStatisticsThread(List<String> sqlList,List<Map<String,Object>> pmList) {  
//           this.sqlList = sqlList;  
//           this.pmList = pmList;
//        }  
//        
//        @Override
//        protected List<ResourceStatisticsViewModel> compute() {
//            final List<ResourceStatisticsViewModel> returnList = new ArrayList<ResourceStatisticsViewModel>();
//            List<ResStatisticsThread> cals = new ArrayList<NDResourceDaoImpl.ResStatisticsThread>();
//            if(sqlList.size() == 1){  
//                NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(repository.getJdbcTemple());
////              System.out.println("====="+System.currentTimeMillis());
//                namedParameterJdbcTemplate.query(sqlList.get(0), pmList.get(0), new RowMapper<ResourceStatisticsViewModel>(){
//                    @Override
//                    public ResourceStatisticsViewModel mapRow(ResultSet rs, int rowNum) throws SQLException {
//                        ResourceStatisticsViewModel rsvm = new ResourceStatisticsViewModel();
//                        if(rs.getString("taxOnCode").startsWith("$")){
//                            rsvm.setResourceType(rs.getString("taxOnCode"));
//                        }else{
//                            rsvm.setResourceType(
//                                    LifeCircleApplicationInitializer.ndCode_properties.getProperty(
//                                            rs.getString("taxOnCode")));
//                        }
//                        rsvm.setCount(rs.getInt("counts"));
//                        returnList.add(rsvm);
////                      System.out.println("++++++++"+System.currentTimeMillis());
//                        return null;
//                    }
//                });
//            }else{
//                for (int i = 0; i< sqlList.size() ;i++) {
//                    List<String> tmp = new ArrayList<String>();
//                    List<Map<String,Object>> pm = new ArrayList<Map<String,Object>>();
//                    tmp.add(sqlList.get(i));
//                    pm.add(pmList.get(i));
//                    
//                    ResStatisticsThread cal = new ResStatisticsThread(tmp,pm);
//                    cals.add(cal);
//                    cal.fork();
//                }
//            }  
//            
//            if(CollectionUtils.isNotEmpty(cals)){
//                for(ResStatisticsThread cal : cals){
//                    returnList.addAll(cal.join());
//                }
//            }
//            return returnList;  
//        }
//    }
    //****************************资源统计模块-暂不使用****************************\\
}
