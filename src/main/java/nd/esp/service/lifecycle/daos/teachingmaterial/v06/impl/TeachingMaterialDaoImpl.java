package nd.esp.service.lifecycle.daos.teachingmaterial.v06.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Query;

import nd.esp.service.lifecycle.daos.teachingmaterial.v06.TeachingMaterialDao;
import nd.esp.service.lifecycle.educommon.dao.NDResourceDao;
import nd.esp.service.lifecycle.educommon.vos.ResClassificationViewModel;
import nd.esp.service.lifecycle.educommon.vos.ResTechInfoViewModel;
import nd.esp.service.lifecycle.repository.model.ResourceCategory;
import nd.esp.service.lifecycle.repository.model.TeachingMaterial;
import nd.esp.service.lifecycle.repository.model.TechInfo;
import nd.esp.service.lifecycle.repository.sdk.TeachingMaterialRepository;
import nd.esp.service.lifecycle.utils.BeanMapperUtils;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * 教材数据层实现类
 * 
 * @author xuzy
 *
 */
@Repository
public class TeachingMaterialDaoImpl implements TeachingMaterialDao {

	@Autowired
	private TeachingMaterialRepository teachingMaterialRepository;
	
	@Autowired
	@Qualifier("defaultJdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	@Qualifier("questionJdbcTemplate")
	private JdbcTemplate questionJdbcTemplate;
	
	@Autowired
	private NDResourceDao ndResourceDao;

	@Override
	public List<Map<String, Object>> queryListByCategories(String taxonPath,
			String id) {
		final List<Map<String, Object>> tmList = new ArrayList<Map<String, Object>>();
		Query query = teachingMaterialRepository.getEntityManager().createNamedQuery("checkTeachingMaterialExist");
		query.setParameter("taxonPath", taxonPath);
		query.setParameter("identifier", id);
		List<TeachingMaterial> result = query.getResultList();
		if (CollectionUtils.isNotEmpty(result)) {
			for (TeachingMaterial tm : result) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("identifier", tm.getIdentifier());
				map.put("enable", tm.getEnable());
				map.put("primaryCategory", tm.getPrimaryCategory());
				tmList.add(map);
			}
		}
		return tmList;
	}

	@Override
	public List<Map<String, Object>> queryChaptersByTmId(String tmId) {
		String sql = "SELECT c.identifier as cid,nd.title,c.parent from ndresource nd,chapters c where nd.primary_category = 'chapters' and nd.enable = 1 and nd.identifier = c.identifier and c.teaching_material = '"+tmId+"' order by c.tree_left";
		return jdbcTemplate.queryForList(sql);
	}

	@Override
	public List<Map<String, Object>> queryResourcesByChapterIds(
			List<String> cids, String resType,final List<String> includes,String coverage) {
		Map<String,Object> params = new HashMap<String, Object>();
		String[] cs = coverage.split("/");
		
		String sql = "SELECT "+selectSql(resType, includes)+",rr.source_uuid from resource_relations rr,ndresource nd,res_coverages cov where rr.res_type='chapters' and rr.resource_target_type= '"+resType+"' and rr.enable = 1 and nd.primary_category='"+resType+"' and nd.enable = 1 and rr.source_uuid in (:cids) and rr.target = nd.identifier and cov.res_type='"+resType+"' and cov.resource = nd.identifier and cov.target_type = :targetType and cov.target = :target ";
		if(cs.length > 2 && StringUtils.isNotEmpty(cs[2])){
			sql += " and cov.strategy = :strategy";
			params.put("strategy", cs[2]);
		}
		sql += " order by rr.sort_num";
		NamedParameterJdbcTemplate npjt = new NamedParameterJdbcTemplate(jdbcTemplate);
		params.put("targetType", cs[0]);
		params.put("target", cs[1]);
		params.put("cids", cids);
    	List<Map<String,Object>> list = npjt.query(sql, params, new RowMapper<Map<String,Object>>(){
			@Override
			public Map<String, Object> mapRow(ResultSet rs, int rowNum)
					throws SQLException {
				Map<String,Object> om = resultMap(rs, includes);
				om.put("source_uuid", rs.getString("source_uuid"));
				return om;
			}
    	});
    	
    	Set<String> idSet = new HashSet<String>();
		for (Map<String, Object> map : list) {
			idSet.add((String)map.get("identifier"));
		}
		list = dealOtherInclude(list,includes,idSet,resType);
    	
    	return list;
	}
	
	@Override
	public List<Map<String, Object>> queryResourcesByChapterIds4Question(
			List<String> cids, String resType,final List<String> includes,String coverage) {
		String[] cs = coverage.split("/");
		
		String sql = "SELECT rr.source_uuid,rr.target from resource_relations rr where rr.res_type='chapters' and rr.resource_target_type= '"+resType+"' and rr.source_uuid in (:cids) and rr.enable = 1 order by rr.sort_num";
		NamedParameterJdbcTemplate npjt = new NamedParameterJdbcTemplate(jdbcTemplate);
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("cids", cids);
		List<Map<String,Object>> rrList = npjt.query(sql, params, new RowMapper<Map<String,Object>>(){
			@Override
			public Map<String, Object> mapRow(ResultSet rs, int rowNum)
					throws SQLException {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("source_uuid", rs.getString("source_uuid"));
				map.put("target", rs.getString("target"));
				return map;
			}
    	});
		
		if(CollectionUtils.isNotEmpty(rrList)){
			Map<String,Object> params2 = new HashMap<String, Object>();
			List<String> ids = new ArrayList<String>();
			for (Map<String,Object> map : rrList) {
				ids.add((String)(map.get("target")));
			}
			
			String sql2 = "select "+selectSql(resType, includes)+" from ndresource nd,res_coverages cov where nd.primary_category='"+resType+"' and nd.enable = 1 and nd.identifier in (:ids) and cov.res_type='"+resType+"' and cov.resource = nd.identifier and cov.target_type = :targetType and cov.target = :target";
			if(cs.length > 2 && StringUtils.isNotEmpty(cs[2])){
				sql2 += " and cov.strategy = :strategy";
				params2.put("strategy", cs[2]);
			}
			NamedParameterJdbcTemplate npjt2 = new NamedParameterJdbcTemplate(questionJdbcTemplate);
			params2.put("ids", ids);
			params2.put("targetType", cs[0]);
			params2.put("target", cs[1]);
			List<Map<String, Object>> list = npjt2.query(sql2, params2, new RowMapper<Map<String,Object>>(){
				@Override
				public Map<String, Object> mapRow(ResultSet rs, int rowNum)
						throws SQLException {
					return resultMap(rs, includes);
				}
	    	});
			
			Set<String> idSet = new HashSet<String>();
			for (Map<String, Object> map : list) {
				String identifier = (String)map.get("identifier");
				idSet.add(identifier);
				for (Map<String, Object> map2 : rrList) {
					if(map2.get("target").equals(identifier)){
						map.put("source_uuid", map2.get("source_uuid"));
					}
				}
			}
			
			list = dealOtherInclude(list,includes,idSet,resType);
			
			return list;
		}
		return null;
	}
	
	private List<Map<String,Object>> dealOtherInclude(List<Map<String,Object>> list,List<String> includes,Set<String> idSet,String resType){
		List<String> rts = new ArrayList<String>();
		rts.add(resType);
		
		if(CollectionUtils.isEmpty(idSet)){
			return list;
		}
		
		//处理tech_info
		if(includes.contains("TI")){
			List<TechInfo> tiList = ndResourceDao.queryTechInfosUseHql(rts, idSet);
			if(CollectionUtils.isNotEmpty(tiList)){
				for (TechInfo techInfo : tiList) {
					for (Map<String, Object> m : list) {
						if(m.get("identifier").equals(techInfo.getResource())){
							if(!m.containsKey("tech_info")){
								m.put("tech_info", new HashMap<String, Object>());
							}
							Map<String, Object> tiMap = (Map)m.get("tech_info");
							tiMap.put(techInfo.getTitle(), BeanMapperUtils.beanMapper(techInfo, ResTechInfoViewModel.class));
						}
					}
				}
			}
		}
		
		//处理categories
		if(includes.contains("CG")){
			List<ResourceCategory> rcList = ndResourceDao.queryCategoriesUseHql(rts, idSet);
			if(CollectionUtils.isNotEmpty(rcList)){
				for (ResourceCategory resourceCategory : rcList) {
					for (Map<String, Object> m : list) {
						if(m.get("identifier").equals(resourceCategory.getResource())){
							if(!m.containsKey("categories")){
								m.put("categories", new ArrayList());
							}
							List rcl = (List)m.get("categories");
							rcl.add(BeanMapperUtils.beanMapper(resourceCategory, ResClassificationViewModel.class));
						}
					}
				}
			}
		}
		return list;
	}
	
	private String selectSql(String resType,List<String> includes){
		String s = " nd.identifier,nd.title,nd.description,nd.elanguage,nd.tags,nd.keywords,nd.preview,nd.custom_properties";
		if(includes.contains("LC")){
			s += " ,nd.version,nd.estatus,nd.creator,nd.publisher,nd.provider,nd.provider_source,nd.provider_mode,nd.create_time,nd.last_update";
		}
		if(includes.contains("CR")){
			s += " ,nd.cr_right,nd.cr_description,nd.has_right,nd.right_start_date,nd.right_end_date,nd.author";
		}
		return s;
	}
	
	private Map<String,Object> resultMap(ResultSet rs,List<String> includes){
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			map.put("identifier", rs.getString("identifier"));
			map.put("title", rs.getString("title"));
			map.put("description", rs.getString("description"));
			map.put("language", rs.getString("elanguage"));
			if(StringUtils.isNotEmpty(rs.getString("tags"))){
				map.put("tags", ObjectUtils.fromJson(rs.getString("tags"), List.class));
			}else{
				map.put("tags", null);
			}
		
			if(StringUtils.isNotEmpty(rs.getString("keywords"))){
				map.put("keywords", ObjectUtils.fromJson(rs.getString("keywords"), List.class));
			}else{
				map.put("keywords", null);
			}
			
			if(StringUtils.isNotEmpty(rs.getString("preview"))){
				map.put("preview", ObjectUtils.fromJson(rs.getString("preview"), Map.class));
			}else{
				map.put("preview", null);
			}
			if(StringUtils.isNotEmpty(rs.getString("custom_properties"))){
				map.put("custom_properties", ObjectUtils.fromJson(rs.getString("custom_properties"), Map.class));
			}else{
				map.put("custom_properties", null);
			}
			
			if(includes.contains("LC")){
				Map<String,Object> lcMap = new HashMap<String, Object>();
				lcMap.put("version", rs.getString("version"));
				lcMap.put("status", rs.getString("estatus"));
				lcMap.put("creator", rs.getString("creator"));
				lcMap.put("publisher", rs.getString("publisher"));
				lcMap.put("provider", rs.getString("provider"));
				lcMap.put("provider_source", rs.getString("provider_source"));
				lcMap.put("provider_mode", rs.getString("provider_mode"));
				lcMap.put("create_time", new Date(rs.getBigDecimal("create_time").longValue()));
				lcMap.put("last_update", new Date(rs.getBigDecimal("last_update").longValue()));
				map.put("life_cycle", lcMap);
			}
			
			if(includes.contains("CR")){
				Map<String,Object> crMap = new HashMap<String, Object>();
				crMap.put("right", rs.getString("cr_right"));
				crMap.put("description", rs.getString("cr_description"));
				crMap.put("author", rs.getString("title"));
				if(rs.getBigDecimal("has_right") != null){
					int hr = rs.getBigDecimal("has_right").toBigInteger().intValue();
					if(hr == 1){
						crMap.put("has_right", true);
					}else{
						crMap.put("has_right", false);
					}
				}else{
					crMap.put("has_right", null);
				}
				
				crMap.put("right_start_date", rs.getBigDecimal("right_start_date"));
				crMap.put("right_end_date", rs.getBigDecimal("right_end_date"));
				map.put("copyright", crMap);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return map;
	}
	
}
