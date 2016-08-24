package nd.esp.service.lifecycle.support;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nd.esp.service.lifecycle.app.LifeCircleApplicationInitializer;
import nd.esp.service.lifecycle.educommon.services.impl.CommonServiceHelper;
import nd.esp.service.lifecycle.entity.elasticsearch.Resource;
import nd.esp.service.lifecycle.services.elasticsearch.SyncResourceService;
import nd.esp.service.lifecycle.services.titan.TitanSyncService;
import nd.esp.service.lifecycle.support.enums.SynVariable;
import nd.esp.service.lifecycle.support.logs.DBLogUtil;
import nd.esp.service.lifecycle.utils.CollectionUtils;

import org.apache.log4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 清除脏垃圾数据任务(物理删除)
 * 主要用来清除QA测试用例产生的脏垃圾数据，以及单元测试产生的脏垃圾数据
 * 清除的资源垃圾数据为两天前产生的垃圾数据（近两天垃圾数据有可能会用来定位问题）
 * @author xuzy
 *
 */
@Component
public class DeleteDirtyDataTask {
	private final static Logger LOG= LoggerFactory.getLogger(DeleteDirtyDataTask.class);
	
	//QA用例默认值
	public final static String QA_DERAULT_CREATOR="lcms_special_creator_qa_test";
	public final static String QA_DERAULT_DESCRIPTION="lcms_special_description_qa_test";
	public final static String QA_DERAULT_PUBLISHER="lcms_special_publisher_qa_test";
	public final static String QA_DERAULT_PROVIDER="lcms_special_provider_qa_test";
	public final static String QA_DERAULT_RIGHT="lcms_special_right_qa_test";
	
	//开发单元测试默认值
	public final static String DERAULT_CREATOR="lcms-special-creator-dev-test"; 
	public final static String DERAULT_DESCRIPTION="lcms-special-description-dev-test";
	public final static String DERAULT_PUBLISHER="lcms-special-publisher-dev-test";
	public final static String DERAULT_PROVIDER="lcms-special-provider-dev-test";
	public final static String DERAULT_RIGHT="lcms-special-right-dev-test";
	
	@Autowired
	@Qualifier(value="defaultJdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	@Qualifier(value="questionJdbcTemplate")
	private JdbcTemplate questionJdbcTemplate;
	
	@Autowired
	@Qualifier(value="reportJdbcTemplate")
	private JdbcTemplate reportJdbcTemplate;
	
	@Autowired
	private CommonServiceHelper commonServiceHelper;
	
	@Autowired
	private SyncResourceService syncResourceService;

	@Autowired
	private TitanSyncService titanSyncService;
	
	//目前支持的资源类型
	private static final String[] resTypes = { "assets", "coursewares",
			"coursewareobjects", "chapters", "coursewareobjecttemplates",
			"ebooks", "homeworks", "instructionalobjectives", "knowledges",
			"learningplans", "lessonplans", "lessons", "questions","tools",
			"teachingmaterials", "guidancebooks","teachingactivities","examinationpapers","metacurriculums" };
	/**
	 * 执行定时任务
	 * @param createTime
	 */
	@Scheduled(cron="0 0/45 4-8 * * ?")
	public void init(){
		if(commonServiceHelper.queryAndUpdateSynVariable(SynVariable.deleteDirtyTask.getValue()) == 0){
			return;
		}
		LOG.info("清理脏数据定时任务开始跑。。。");
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -2);
		Long createTime = cal.getTime().getTime();
		
		for (String resType : resTypes) {
			if(!StaticDatas.suspendFlag){
				initDirtyData(resType);
				int num = queryIds(createTime,resType);
				if(num > 0){
					syncReport(resType);
					dealDirtyData(createTime,resType);
					syncEs(resType);
					syncTitan(resType);
				}
				
				if(!CommonServiceHelper.isQuestionDb(resType)){
					deleteResourceProviders(resType);
					deleteCopyrightOwners(resType);
				}
			}else{
				commonServiceHelper.initSynVariable(SynVariable.deleteDirtyTask.getValue());
				LOG.warn("取消定时任务!");
				return;
			}
		}
		LOG.info("清理脏数据定时任务结束。。。");
		commonServiceHelper.initSynVariable(SynVariable.deleteDirtyTask.getValue());
	}
		
	/**
	 * 按资源类型处理脏数据
	 * @param createTime
	 * @param resType
	 */
	public void dealDirtyData(Long createTime,String resType){
		StringBuffer sb = new StringBuffer();
		long t1 = System.currentTimeMillis();
		int n1 = deleteDirtyTechInfos(resType);
		sb.append("删除tech_infos数量：").append(n1).append(";用时：").append(System.currentTimeMillis() - t1).append(System.lineSeparator());
		
		
		long t2 = System.currentTimeMillis();
		int n2 = deleteDirtyResCoverages(resType);
		sb.append("删除res_coverages数量：").append(n2).append(";用时：").append(System.currentTimeMillis() - t2).append(System.lineSeparator());
		
		long t3 = System.currentTimeMillis();
		int n3 = deleteDirtyResourceCategories(resType);
		sb.append("删除resource_categories数量：").append(n3).append(";用时：").append(System.currentTimeMillis() - t3).append(System.lineSeparator());
		
		long t4 = System.currentTimeMillis();
		int n4 = deleteDirtyResourceRelations(resType);
		sb.append("删除resource_relations数量：").append(n4).append(";用时：").append(System.currentTimeMillis() - t4).append(System.lineSeparator());
		
		long t5 = System.currentTimeMillis();
		int n5 = deleteDirtyContributes(resType);
		sb.append("删除contributes数量：").append(n5).append(";用时：").append(System.currentTimeMillis() - t5).append(System.lineSeparator());
		
		long t6 = System.currentTimeMillis();
		int n6 = deleteDirtyResourceAnnotations(resType);
		sb.append("删除resource_annotations数量：").append(n6).append(";用时：").append(System.currentTimeMillis() - t6).append(System.lineSeparator());

		long t7 = System.currentTimeMillis();
		int n7 = deleteDirtyResourceStatisticals(resType);
		sb.append("删除resource_statisticals数量：").append(n7).append(";用时：").append(System.currentTimeMillis() - t7).append(System.lineSeparator());

		long t8 = System.currentTimeMillis();
		int n8 = deleteResourceByIds(resType);
		sb.append("删除resource数量：").append(n8).append(";用时：").append(System.currentTimeMillis() - t8).append(System.lineSeparator());
		
		long t9 = System.currentTimeMillis();
		int n9 = deleteNdResourceByIds(resType);
		sb.append("删除ndresource数量：").append(n9).append(";用时：").append(System.currentTimeMillis() - t9).append(System.lineSeparator());
		
		saveLog(resType, sb.toString());
	}
	
	/**
	 * 删除的数据，同步至ES
	 * @param resType
	 */
	public void syncEs(String resType){
		Set<Resource> resourceSet = queryResources(resType);
		if(resourceSet != null){
			syncResourceService.syncBatchDelete(resourceSet);
		}
	}
	
	/**
	 * 清空dirty_data表数据
	 */
	public void initDirtyData(String resType){
		String sql = "truncate dirty_data";
		excuteSql(resType,sql);
	}
	
	/**
	 * 根据创建时间与资源类型查询脏数据的id，将脏数据保存至dirty_data表中
	 * @param createTime
	 * @param resType
	 * @return
	 */
	public int queryIds(Long createTime,String resType){
		String sql = "insert into dirty_data select identifier,primary_category from ndresource where primary_category='"
				+ resType
				+ "' and (create_time < "
				+ createTime
				+ " or create_time is null) and (creator='"
				+ QA_DERAULT_CREATOR
				+ "' or creator='"
				+ DERAULT_CREATOR
				+ "' or description = '"
				+ QA_DERAULT_DESCRIPTION
				+ "' or description ='"
				+ DERAULT_DESCRIPTION
				+ "' or publisher = '"
				+ QA_DERAULT_PUBLISHER
				+ "' or publisher = '"
				+ DERAULT_PUBLISHER + "') limit 0,1000";
		int num = excuteSql(resType,sql);
		return num;
	}
	
	
	/**
	 * 查询删除的资源id
	 * @return
	 */
	private Set<Resource> queryResources(String resType){
		String sql = "select identifier from dirty_data";
		List<Map<String,Object>> returnList = null;
		if(!CommonServiceHelper.isQuestionDb(resType)){
			returnList = jdbcTemplate.queryForList(sql);
		}else{
			returnList = questionJdbcTemplate.queryForList(sql);
		}
		
		if(CollectionUtils.isNotEmpty(returnList)){
			Set<Resource> set = new HashSet<Resource>();
			for (Map<String, Object> map : returnList) {
				String identifier = (String)map.get("identifier");
				Resource res = new Resource(resType, identifier);
				set.add(res);
			}
			return set;
		}
		return null;
	}
	
	/**
	 * 根据资源类型删除tech_infos表垃圾数据
	 * @param ids
	 * @param resType
	 */
	private int deleteDirtyTechInfos(String resType){
		String sql = "DELETE rc from tech_infos rc,dirty_data dd where rc.res_type='"+resType+"' and rc.resource = dd.identifier";
		return excuteSql(resType,sql);
	}
	
	/**
	 * 根据资源类型删除res_coverages表垃圾数据
	 * @param ids
	 * @param resType
	 */
	private int deleteDirtyResCoverages(String resType){
		String sql = "DELETE rc from res_coverages rc,dirty_data dd where rc.res_type='"+resType+"' and rc.resource = dd.identifier";
		return excuteSql(resType,sql);
	}
	
	/**
	 * 根据资源类型删除resource_categories表垃圾数据
	 * @param ids
	 * @param resType
	 */
	private int deleteDirtyResourceCategories(String resType){
		String sql = "DELETE rc from resource_categories rc,dirty_data dd where rc.resource = dd.identifier";
		return excuteSql(resType,sql);
	}

	/**
	 * 根据资源类型删除resource_relations表垃圾数据
	 * @param ids
	 * @param resType
	 */
	private int deleteDirtyResourceRelations(String resType){
		String sql = "DELETE rc from resource_relations rc,dirty_data dd where rc.res_type='"+resType+"' and rc.source_uuid = dd.identifier";
		return excuteSql(resType,sql);
	}
	
	/**
	 * 根据资源类型删除contributes表垃圾数据
	 * @param ids
	 */
	private int deleteDirtyContributes(String resType){
		String sql = "DELETE rc from contributes rc,dirty_data dd where rc.res_type='"+resType+"' and rc.resource = dd.identifier";
		return excuteSql(resType,sql);
	}
	
	/**
	 * 根据资源类型删除resource_annotations表垃圾数据
	 * @param resType
	 * @return
	 */
	private int deleteDirtyResourceAnnotations(String resType){
		String sql = "DELETE rc from resource_annotations rc,dirty_data dd where rc.res_type='"+resType+"' and rc.resource = dd.identifier";
		return excuteSql(resType,sql);
	}
	
	/**
	 * 根据资源类型删除resource_statisticals表垃圾数据
	 * @param resType
	 * @return
	 */
	private int deleteDirtyResourceStatisticals(String resType){
		String sql = "DELETE rc from resource_statisticals rc,dirty_data dd where rc.res_type='"+resType+"' and rc.resource = dd.identifier";
		return excuteSql(resType,sql);
	}
	
	/**
	 * 根据资源类型动态删除资源表垃圾数据
	 * @param ids
	 * @param resType
	 */
	private int deleteResourceByIds(String resType){
		String tableName = (String)LifeCircleApplicationInitializer.tablenames_properties.get(resType);
		String sql = "delete rc from "+tableName+" rc,dirty_data dd where rc.identifier = dd.identifier";
		return excuteSql(resType,sql);
	}
	
	/**
	 * 根据资源类型动态删除资源表垃圾数据
	 * @param ids
	 * @param resType
	 */
	private int deleteNdResourceByIds(String resType){
		String sql = "delete rc from ndresource rc,dirty_data dd where rc.primary_category='" + resType + "' and rc.identifier = dd.identifier";
		return excuteSql(resType,sql);
	}
	
	/**
	 * 删除resource_providers表中的测试数据
	 * @author xiezy
	 * @date 2016年8月22日
	 * @param resType
	 * @return
	 */
	private int deleteResourceProviders(String resType){
		String sql = "delete rp from resource_providers rp where rp.title like '" + QA_DERAULT_PROVIDER + "%' OR rp.title like '" + DERAULT_PROVIDER + "%'";
		return excuteSql(resType,sql);
	}
	
	/**
	 * 删除copyright_owners表中的测试数据
	 * @author xiezy
	 * @date 2016年8月22日
	 * @param resType
	 * @return
	 */
	private int deleteCopyrightOwners(String resType){
		String sql = "delete rp from copyright_owners rp where rp.title like '" + QA_DERAULT_RIGHT + "%' OR rp.title like '" + DERAULT_RIGHT + "%'";
		return excuteSql(resType,sql);
	}
	
	/**
	 * 执行sql
	 * @param sql
	 * @return
	 */
	private int excuteSql(String resType,String sql){
		if(!CommonServiceHelper.isQuestionDb(resType)){
			return jdbcTemplate.update(sql);
		}
		return questionJdbcTemplate.update(sql);
	}
	
	/**
	 * 将结果保存至数据库
	 * @param resType
	 * @param message
	 */
	private void saveLog(String resType,String message){
		MDC.put("resource", "uuid");
        MDC.put("res_type", resType);
        MDC.put("operation_type", "清除脏数据");
        MDC.put("remark", message);
        DBLogUtil.getDBlog().info("清除脏数据，资源类型："+resType);
        MDC.clear();
	}
	
	/**
	 * 同步报表系统
	 * @param resType
	 */
	private void syncReport(String resType){
		//筛选出nd库的数据
		String sql = "select dd.identifier from dirty_data dd,res_coverages rc where dd.primary_category = rc.res_type and rc.resource = dd.identifier and rc.target_type='Org' and rc.target = 'nd'";
		List<Map<String,Object>> resultList = null;
		if(!CommonServiceHelper.isQuestionDb(resType)){
			resultList = jdbcTemplate.queryForList(sql);
		}else{
			resultList = questionJdbcTemplate.queryForList(sql);
		}
		
		if(CollectionUtils.isNotEmpty(resultList)){
			List<String> ids = new ArrayList<String>();
			String deleteCategorySql = "delete from resource_categories where resource in (:ids)";
			String deleteNdresourceSql = "delete from ndresource where identifier in (:ids)";
			for (Map<String, Object> map : resultList) {
				ids.add((String)map.get("identifier"));
			}
			NamedParameterJdbcTemplate npjt = new NamedParameterJdbcTemplate(reportJdbcTemplate);
			Map<String,Object> paramMap = new HashMap<String, Object>();
			paramMap.put("ids", ids);
			npjt.update(deleteCategorySql, paramMap);
			npjt.update(deleteNdresourceSql, paramMap);
		}
	}
	
	/**
	 * 同步 TITAN
	 * @param resType
	 */
	private void syncTitan(String resType){
		Set<Resource> resourceSet = queryResources(resType);
		titanSyncService.batchDeleteResource(resourceSet);
	}
}
