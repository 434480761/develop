package nd.esp.service.lifecycle.support.icrs;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import nd.esp.service.lifecycle.educommon.services.impl.CommonServiceHelper;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.repository.model.icrs.IcrsResource;
import nd.esp.service.lifecycle.repository.sdk.icrs.IcrsResourceRepository;
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
				List<IcrsResource> syncList = new ArrayList<IcrsResource>();
				for(SyncIcrsModel model : list){
					IcrsResource searchExample = new IcrsResource();
					searchExample.setResUuid(model.getIdentifier());
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
