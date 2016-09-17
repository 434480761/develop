package nd.esp.service.lifecycle.support.icrs;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.nd.gaea.client.http.WafHttpClient;
import com.nd.gaea.client.http.WafSecurityHttpClient;

import nd.esp.service.lifecycle.app.LifeCircleApplicationInitializer;
import nd.esp.service.lifecycle.educommon.services.impl.CommonServiceHelper;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.icrs.IcrsResource;
import nd.esp.service.lifecycle.repository.sdk.icrs.IcrsResourceRepository;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.utils.CollectionUtils;

public class IcrsServiceHelper {
	
	@Autowired
	private IcrsResourceRepository icrsResourceRepository;
	
	@Qualifier(value="defaultJdbcTemplate")
	@Autowired
	private JdbcTemplate defaultJdbcTemplate;
	@Qualifier(value="questionJdbcTemplate")
	@Autowired
	private JdbcTemplate questionJdbcTemplate;
	
	public void syncIcrsByType(String resType, boolean isInit){
		String querySql = "select distinct ndr.identifier as id,ndr.create_time as ct,rv.target as target "
				+ "from ndresource ndr inner join res_coverages rv "
				+ "on ndr.identifier=rv.resource ";
		if(resType.equals(IndexSourceType.AssetType.getName()) ||
				resType.equals(IndexSourceType.SourceCourseWareObjectType.getName())){//assets,coursewareobjects
			querySql += " inner join resource_categories rc on ndr.identifier=rc.resource ";
		}
		
		querySql += " where " + (isInit ? "ndr.enable=1 and " : "") + "ndr.primary_category='" + resType + "' "
				+ "and rv.target_type='User' and rv.strategy='OWNER' and rv.res_type='" + resType + "' ";
		if(resType.equals(IndexSourceType.AssetType.getName())){//assets
			querySql += " and rc.primary_category='" + resType + "' and rc.taxOnCode in "
					+ "('$RA0101','$RA0102','$RA0103','$RA0104')";
		}
		if(resType.equals(IndexSourceType.SourceCourseWareObjectType.getName())){//coursewareobjects
			querySql += " and rc.primary_category='" + resType + "' and rc.taxOnCode like '$RE04%'";
		}
		
		if(!isInit){//获取当前时间前一小时的数据有变动的数据
			querySql += " and ndr.last_update > " + getOneHourAgoTime();
		}
		
		System.out.println("ICRS查询SQL:" + querySql);
		
		//分页参数
		int offset = 0;
		int pageSize = 500;
		
		while(true){
			querySql += "  limit " + offset + "," + pageSize;
			
			final List<SyncIcrsModel> list = new ArrayList<SyncIcrsModel>();
			getJdbcTemplate(resType).query(querySql, new RowMapper<String>(){
				@Override
				public String mapRow(ResultSet rs, int rowNum) throws SQLException {
					SyncIcrsModel sim = new SyncIcrsModel();
					sim.setIdentifier(rs.getString("id"));
					sim.setCreateTime(rs.getLong("ct"));
					sim.setTarget(rs.getString("target"));
					list.add(sim);
					
					return null;
				}
			});
			
			if(CollectionUtils.isNotEmpty(list)){
				try {
					List<IcrsResource> syncList = new ArrayList<IcrsResource>();
					for(SyncIcrsModel model : list){
						//先查询是否同步过
						IcrsResource searchExample = new IcrsResource();
						searchExample.setResUuid(model.getIdentifier());
						List<IcrsResource> existList = icrsResourceRepository.getAllByExample(searchExample);
						
						//如果已同步过，先删除旧数据
						if(CollectionUtils.isNotEmpty(existList)){
							List<String> deleteIds = new ArrayList<String>();
							for(IcrsResource ir : existList){
								deleteIds.add(ir.getIdentifier());
							}
							
							if(CollectionUtils.isNotEmpty(deleteIds)){
								icrsResourceRepository.batchDel(deleteIds);
							}
						}
						
						//新增同步记录
						//1.查询用户姓名,调用UC接口
						String userName = getUserName(model.getTarget());
						//2.获取对应学校id,调用Admin接口
						
					}
				} catch (Exception e) {
					throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
	                        LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
	                        e.getLocalizedMessage());
				}
			}else{
				break;
			}
			
			//处理分页
			querySql = querySql.substring(0, querySql.lastIndexOf("limit"));
			offset += pageSize;
		}
	}
	
	/**
	 * 获取当前系统时间前一个小时的时间戳
	 * @author xiezy
	 * @date 2016年9月12日
	 * @return
	 */
	private long getOneHourAgoTime(){
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) - 1);
		return calendar.getTimeInMillis();
	}
	
	/**
	 * 获取用户姓名
	 * @author xiezy
	 * @date 2016年9月14日
	 * @param userid
	 * @return
	 * @throws Exception
	 */
	private String getUserName(String userid) throws Exception {
		WafSecurityHttpClient wafSecurityHttpClient = new WafSecurityHttpClient();
		String url = LifeCircleApplicationInitializer.properties.getProperty("waf.uc.uri") + "users/" + userid;
		@SuppressWarnings("deprecation")
		Map<String, Object> userInfo = wafSecurityHttpClient.get(url, Map.class);
		
		if(CollectionUtils.isEmpty(userInfo)){
			return "";
		}
		
		return (String) userInfo.get("nick_name");
	}
	
	/**
	 * 根据教师id获取学校id
	 * @author xiezy
	 * @date 2016年9月14日
	 * @param teacherId
	 * @return
	 */
	private String getSchoolId(String teacherId){
		WafSecurityHttpClient wafSecurityHttpClient = new WafSecurityHttpClient();
		String url = LifeCircleApplicationInitializer.properties.getProperty("admin.uri") 
				+ "v06/schools/" + teacherId;
		
		
		return teacherId;
	}
	
	/**
	 * 根据资源类型返回JdbcTemplate
	 * @author xiezy
	 * @date 2016年9月12日
	 * @param resType
	 * @return
	 */
	private JdbcTemplate getJdbcTemplate(String resType){
		if(CommonServiceHelper.isQuestionDb(resType)){
			return questionJdbcTemplate;
		}
		
		return defaultJdbcTemplate;
	}
}
