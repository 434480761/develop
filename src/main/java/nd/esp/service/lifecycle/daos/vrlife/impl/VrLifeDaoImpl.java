package nd.esp.service.lifecycle.daos.vrlife.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import nd.esp.service.lifecycle.daos.vrlife.VrLifeDao;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.vrlife.VrLifeConstant;
import nd.esp.service.lifecycle.support.vrlife.VrLifeType;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class VrLifeDaoImpl implements VrLifeDao{
	private static final Logger LOG = LoggerFactory.getLogger(VrLifeDaoImpl.class);
	
	@Qualifier(value="defaultJdbcTemplate")
	@Autowired
	private JdbcTemplate defaultJdbcTemplate;
	
	@Override
	public List<String> getRecommendResources() {
		final List<String> recommendedList = new ArrayList<String>();
		
		String querySql = "SELECT recommended_id as rid FROM recommended_resources";
		defaultJdbcTemplate.query(querySql, new RowMapper<String>(){

			@Override
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				recommendedList.add(rs.getString("rid"));
				return null;
			}
			
		});
		
		return recommendedList;
	}

	@Override
	public List<String> getRecommendResourcesBySkeleton(String skeletonId, String type) {
		
		return getResourcesBySkeleton(skeletonId, type, true);
	}
	
	@Override
	public List<String> dynamicComposition(String skeletonId) {
		List<String> compositionResult = new ArrayList<String>();
		
		List<String> partTypes = VrLifeConstant.getPartTypeList();
		if(CollectionUtils.isNotEmpty(partTypes)){
			Random random = new Random();
			List<String> resourceIds = new ArrayList<String>();
			for(String type : partTypes){
				resourceIds = getResourcesBySkeleton(skeletonId, type, false);
				if(CollectionUtils.isNotEmpty(resourceIds)){
					//随机获取其中一个
					compositionResult.add(resourceIds.get(random.nextInt(resourceIds.size())));
				}
			}
		}
		
		return compositionResult;
	}
	
	/**
	 * 获取骨骼对应的某种类型的资源
	 * @author xiezy
	 * @date 2016年8月3日
	 * @param skeletonId
	 * @param type
	 * @param isRecommend
	 * @return
	 */
	private List<String> getResourcesBySkeleton(String skeletonId, String type, boolean isRecommend){
		final List<String> result = new ArrayList<String>();
		
		StringBuilder querySql = new StringBuilder();
		querySql.append("SELECT ndr.identifier AS id FROM ndresource ndr INNER JOIN resource_categories rc ");
		querySql.append("ON ndr.identifier=rc.resource INNER JOIN resource_relations rr ");
		querySql.append("ON ndr.identifier=rr.target ");
		querySql.append("WHERE ndr.enable=1 AND ndr.primary_category='");
		querySql.append(IndexSourceType.AssetType.getName());
		querySql.append("' AND rc.primary_category='");
		querySql.append(IndexSourceType.AssetType.getName());
		querySql.append("' AND rc.taxOnCode IN (:type) ");
		querySql.append("AND rr.enable=1 AND rr.res_type='");
		querySql.append(IndexSourceType.AssetType.getName());
		querySql.append("' AND rr.resource_target_type='");
		querySql.append(IndexSourceType.AssetType.getName());
		querySql.append("' AND rr.source_uuid=:skeletonId ");
		if(isRecommend){
			querySql.append("ORDER BY ndr.create_time DESC LIMIT 0,1");
		}
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("skeletonId", skeletonId);
		
		//获取对应的类型维度
		List<String> ndCodes = new ArrayList<String>();
		if(isRecommend){
			ndCodes = getNdCodeByType(type);
		}else{
			ndCodes = getPartCodeByType(type);
		}
		params.put("type", ndCodes);
		
		if(isRecommend){
			LOG.info("查询的SQL语句:" + querySql.toString());
			LOG.info("查询的SQL参数:" + ObjectUtils.toJson(params));
		}
		
		NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(defaultJdbcTemplate);
		namedJdbcTemplate.query(querySql.toString(), params, new RowMapper<String>() {

			@Override
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				result.add(rs.getString("id"));
				return null;
			}
		});
		
		return result;
	}
	
	/**
	 * 根据type获取对应的维度数据
	 * @author xiezy
	 * @date 2016年8月3日
	 * @param type
	 * @return
	 */
	private List<String> getNdCodeByType(String type){
		List<String> result = new ArrayList<String>();
		
		if(type.equals(VrLifeType.ACTION.getName())){//动作
			result.add("RS001001001001001000000");
			result.add("RS001001001002001000000");
			result.add("RS001001002001001000000");
			result.add("RS001001002002001000000");
			result.add("RS001002001001000000000");
			result.add("RS001002002001000000000");
			result.add("RS001002003001000000000");
		}else if(type.equals(VrLifeType.ROLECONFIG.getName())){//角色配置文件
			result.add("RS001002001005000000000");
			result.add("RS001002002005000000000");
			result.add("RS001002003005000000000");
		}else{
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					"LC/VRLIFE_TYPE_IS_ILLEGAL","根据type获取对应的维度数据时,type只能为roleconfig(角色配置)、action(动作)");
		}
		
		return result;
	}
	
	/**
	 * 获取组合部件的对应维度
	 * @author xiezy
	 * @date 2016年8月3日
	 * @param type
	 * @return
	 */
	private List<String> getPartCodeByType(String type){
		List<String> result = new ArrayList<String>();
		
		if(type.equals(VrLifeConstant.TYPE_HAIRS_TYLE)){//发型
			result.add("RS001002001002001000000");
			result.add("RS001002002002001000000");
			result.add("RS001002003002001000000");
		}else if(type.equals(VrLifeConstant.TYPE_UPPER_BODY)){//上身
			result.add("RS001002001002002000000");
			result.add("RS001002002002002000000");
			result.add("RS001002003002002000000");
		}else if(type.equals(VrLifeConstant.TYPE_HEAD_STYLE)){//头型
			result.add("RS001002001002003000000");
			result.add("RS001002002002003000000");
			result.add("RS001002003002003000000");
		}else if(type.equals(VrLifeConstant.TYPE_LOWER_BODY)){//下身
			result.add("RS001002001002004000000");
			result.add("RS001002002002004000000");
			result.add("RS001002003002004000000");
		}else if(type.equals(VrLifeConstant.TYPE_FOOT)){//足
			result.add("RS001002001002005000000");
			result.add("RS001002002002005000000");
			result.add("RS001002003002005000000");
		}else{
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					"LC/VRLIFE_TYPE_IS_ILLEGAL","type不属于部件类型");
		}
		
		return result;
	}

	@Override
	public void addRecommendedResource(List<String> resources) {
		if(CollectionUtils.isNotEmpty(resources)){
			for(String resource : resources){
				String sql = "INSERT INTO recommended_resources VALUES ('" + resource + "')";
				defaultJdbcTemplate.execute(sql);
			}
		}
	}

	@Override
	public void deleteRecommendedResource(String id) {
		if(StringUtils.hasText(id)){
			String sql = "DELETE FROM recommended_resources WHERE recommended_id='" + id + "'";
			defaultJdbcTemplate.execute(sql);
		}
	}
}
