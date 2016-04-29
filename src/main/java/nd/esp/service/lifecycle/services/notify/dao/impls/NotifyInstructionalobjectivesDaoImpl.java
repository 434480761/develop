package nd.esp.service.lifecycle.services.notify.dao.impls;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.services.notify.dao.NotifyInstructionalobjectivesDao;
import nd.esp.service.lifecycle.services.notify.models.NotifyInstructionalobjectivesRelationModel;
import nd.esp.service.lifecycle.utils.CollectionUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class NotifyInstructionalobjectivesDaoImpl implements NotifyInstructionalobjectivesDao{
	
	@Qualifier(value="defaultJdbcTemplate")
	@Autowired
	private JdbcTemplate defaultJdbcTemplate;

	@Override
	public boolean resourceBelongToNDLibrary(String resId) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT(*) FROM res_coverages rv");
		sql.append(" WHERE rv.res_type='instructionalobjectives' AND rv.resource='");
		sql.append(resId);
		sql.append("' AND rv.target_type='Org' AND rv.target='nd'");
		
		long count = defaultJdbcTemplate.queryForObject(sql.toString(), Long.class);
		
		if(count>0){
			return true;
		}
		return false;
	}

	@Override
	public List<NotifyInstructionalobjectivesRelationModel> resourceBelongToRelations(String resId) {
		final List<NotifyInstructionalobjectivesRelationModel> list = new ArrayList<NotifyInstructionalobjectivesRelationModel>();
		
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT r1.source_uuid AS cid,r2.source_uuid AS lid");
		sql.append(" FROM resource_relations r1 INNER JOIN resource_relations r2");
		sql.append(" ON r1.target=r2.source_uuid");
		sql.append(" WHERE r1.res_type='chapters' AND r1.resource_target_type='lessons'");
		sql.append(" AND r2.res_type='lessons' AND r2.resource_target_type='instructionalobjectives'");
		sql.append(" AND r1.enable=1 AND r2.enable=1");
		sql.append(" AND r2.target='");
		sql.append(resId);
		sql.append("'");
		
		defaultJdbcTemplate.query(sql.toString(), new RowMapper<NotifyInstructionalobjectivesRelationModel>(){

			@Override
			public NotifyInstructionalobjectivesRelationModel mapRow(ResultSet rs, int rowNum)
					throws SQLException {
				NotifyInstructionalobjectivesRelationModel notifyRelationModel = new NotifyInstructionalobjectivesRelationModel();
				notifyRelationModel.setChapterId(rs.getString("cid"));
				notifyRelationModel.setLessonId(rs.getString("lid"));
				list.add(notifyRelationModel);
				return null;
			}
			
		});
		
		//去重list
		List<NotifyInstructionalobjectivesRelationModel> result = new ArrayList<NotifyInstructionalobjectivesRelationModel>();
		if(CollectionUtils.isNotEmpty(list)){
			Map<String,String> map = new HashMap<String,String>();
			for(NotifyInstructionalobjectivesRelationModel nrm : list){
				String key = nrm.getChapterId() + "-" + nrm.getLessonId();
				if(!map.containsKey(key)){
					result.add(nrm);
				}
			}
		}
		
		return result;
	}
	
	@Override
	public Set<String> resourceBelongToRelation4ChapterIds(String ioId,String lessonId) {
		final Set<String> set = new HashSet<String>();
		
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT r1.source_uuid AS cid");
		sql.append(" FROM resource_relations r1 INNER JOIN resource_relations r2");
		sql.append(" ON r1.target=r2.source_uuid");
		sql.append(" WHERE r1.res_type='chapters' AND r1.resource_target_type='lessons'");
		sql.append(" AND r2.res_type='lessons' AND r2.resource_target_type='instructionalobjectives'");
		sql.append(" AND r1.enable=1 AND r2.enable=1");
		sql.append(" AND r2.target='");
		sql.append(ioId);
		sql.append("' AND r2.target='");
		sql.append(lessonId);
		sql.append("'");
		
		defaultJdbcTemplate.query(sql.toString(), new RowMapper<String>(){

			@Override
			public String mapRow(ResultSet rs, int rowNum)
					throws SQLException {
				set.add(rs.getString("cid"));
				return null;
			}
			
		});
		
		return set;
	}
	
	@Override
	public Set<String> resourceBelongToRelation4ChapterIdsByRid(String rid) {
		final Set<String> set = new HashSet<String>();
		
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT r1.source_uuid AS cid");
		sql.append(" FROM resource_relations r1 INNER JOIN resource_relations r2");
		sql.append(" ON r1.target=r2.source_uuid");
		sql.append(" WHERE r1.res_type='chapters' AND r1.resource_target_type='lessons'");
		sql.append(" AND r2.res_type='lessons' AND r2.resource_target_type='instructionalobjectives'");
		sql.append(" AND r1.enable=1");
		sql.append(" AND r2.identifier='");
		sql.append(rid);
		sql.append("'");
		
		defaultJdbcTemplate.query(sql.toString(), new RowMapper<String>(){

			@Override
			public String mapRow(ResultSet rs, int rowNum)
					throws SQLException {
				set.add(rs.getString("cid"));
				return null;
			}
			
		});
		
		return set;
	}
	
	@Override
	public Set<String> resourceBelongToRelation4instructionalobjectives(String chapterId, String lessonId) {
		final Set<String> set = new HashSet<String>();
		
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ndr.identifier AS id");
		sql.append(" FROM ndresource ndr INNER JOIN resource_relations r1 ON ndr.identifier=r1.target");
		sql.append(" INNER JOIN resource_relations r2 ON r1.source_uuid=r2.target");
		sql.append(" WHERE ndr.primary_category='instructionalobjectives' AND ndr.enable=1 AND ndr.estatus='ONLINE'");
		sql.append(" AND r1.enable=1 AND r1.resource_target_type='instructionalobjectives' AND r1.res_type='lessons'");
		sql.append(" AND r2.enable=1 AND r2.resource_target_type='lessons' AND r2.res_type='chapters'");
		sql.append(" AND r1.source_uuid='");
		sql.append(lessonId);
		sql.append("' AND r2.source_uuid='");
		sql.append(chapterId);
		sql.append("'");
		
		defaultJdbcTemplate.query(sql.toString(), new RowMapper<String>(){

			@Override
			public String mapRow(ResultSet rs, int rowNum)
					throws SQLException {
				set.add(rs.getString("id"));
				return null;
			}
			
		});
		
		return set;
	}
	
	@Override
	public Set<String> resourceBelongToRelation4instructionalobjectivesByRid(String rid) {
		final Set<String> set = new HashSet<String>();
		
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ndr.identifier AS id");
		sql.append(" FROM ndresource ndr INNER JOIN resource_relations r1 ON ndr.identifier=r1.target");
		sql.append(" INNER JOIN resource_relations r2 ON r1.source_uuid=r2.target");
		sql.append(" WHERE ndr.primary_category='instructionalobjectives' AND ndr.enable=1 AND ndr.estatus='ONLINE'");
		sql.append(" AND r1.enable=1 AND r1.resource_target_type='instructionalobjectives' AND r1.res_type='lessons'");
		sql.append(" AND r2.resource_target_type='lessons' AND r2.res_type='chapters'");
		sql.append(" AND r2.identifier='");
		sql.append(rid);
		sql.append("'");
		
		defaultJdbcTemplate.query(sql.toString(), new RowMapper<String>(){

			@Override
			public String mapRow(ResultSet rs, int rowNum)
					throws SQLException {
				set.add(rs.getString("id"));
				return null;
			}
			
		});
		
		return set;
	}
	
	@Override
	public List<NotifyInstructionalobjectivesRelationModel> resourceBelongToRelations4LessonOrChapter(final String type, final String id) {
		final List<NotifyInstructionalobjectivesRelationModel> list = new ArrayList<NotifyInstructionalobjectivesRelationModel>();
		
		StringBuilder sql = new StringBuilder();
		if(type.equals(IndexSourceType.LessonType.getName())){
			sql.append("SELECT ndr.identifier AS id,r2.source_uuid AS cid");
		}else if(type.equals(IndexSourceType.ChapterType.getName())){
			sql.append("SELECT ndr.identifier AS id,r1.source_uuid AS lid");
		}
		
		sql.append(" FROM ndresource ndr INNER JOIN resource_relations r1 ON ndr.identifier=r1.target");
		sql.append(" INNER JOIN resource_relations r2 ON r1.source_uuid=r2.target");
		sql.append(" WHERE ndr.primary_category='instructionalobjectives' AND ndr.enable=1 AND ndr.estatus='ONLINE'");
		sql.append(" AND r1.enable=1 AND r1.resource_target_type='instructionalobjectives' AND r1.res_type='lessons'");
		sql.append(" AND r2.enable=1 AND r2.resource_target_type='lessons' AND r2.res_type='chapters'");
		
		if(type.equals(IndexSourceType.LessonType.getName())){
			sql.append(" AND r1.source_uuid='");
		}else if(type.equals(IndexSourceType.ChapterType.getName())){
			sql.append(" AND r2.source_uuid='");
		}
		
		sql.append(id);
		sql.append("'");
		
		defaultJdbcTemplate.query(sql.toString(), new RowMapper<NotifyInstructionalobjectivesRelationModel>(){

			@Override
			public NotifyInstructionalobjectivesRelationModel mapRow(ResultSet rs, int rowNum)
					throws SQLException {
				NotifyInstructionalobjectivesRelationModel notifyRelationModel = new NotifyInstructionalobjectivesRelationModel();
				if(type.equals(IndexSourceType.LessonType.getName())){
					notifyRelationModel.setChapterId(rs.getString("cid"));
					notifyRelationModel.setLessonId(id);
				}else if(type.equals(IndexSourceType.ChapterType.getName())){
					notifyRelationModel.setChapterId(id);
					notifyRelationModel.setLessonId(rs.getString("lid"));
				}
				notifyRelationModel.setInstructionalobjectiveId(rs.getString("id"));
				list.add(notifyRelationModel);
				return null;
			}
			
		});
		
		//去重list
		List<NotifyInstructionalobjectivesRelationModel> result = new ArrayList<NotifyInstructionalobjectivesRelationModel>();
		if(CollectionUtils.isNotEmpty(list)){
			Map<String,String> map = new HashMap<String,String>();
			for(NotifyInstructionalobjectivesRelationModel nrm : list){
				String key = "";
				if(type.equals(IndexSourceType.LessonType.getName())){
					key = nrm.getChapterId() + "-" + nrm.getInstructionalobjectiveId();
				}else if(type.equals(IndexSourceType.ChapterType.getName())){
					key = nrm.getLessonId() + "-" + nrm.getInstructionalobjectiveId();
				}
				if(!map.containsKey(key)){
					result.add(nrm);
				}
			}
		}
		
		return result;
	}

	@Override
	public String getResourceTitle(String resId) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ndr.title AS title FROM ndresource ndr");
		sql.append(" WHERE ndr.primary_category='instructionalobjectives' AND ndr.identifier='");
		sql.append(resId);
		sql.append("'");
		
		List<Map<String, Object>> titleList = defaultJdbcTemplate.queryForList(sql.toString());
		
		String title = "";
		if(CollectionUtils.isNotEmpty(titleList) && titleList.size() == 1){
			title = (String)titleList.get(0).get("title");
		}
		return title;
	}

	@Override
	public String getResourceStatus(String resId) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ndr.estatus AS status FROM ndresource ndr");
		sql.append(" WHERE ndr.primary_category='instructionalobjectives' AND ndr.identifier='");
		sql.append(resId);
		sql.append("'");
		
		List<Map<String, Object>> statusList = defaultJdbcTemplate.queryForList(sql.toString());
		
		String status = "";
		if(CollectionUtils.isNotEmpty(statusList) && statusList.size() == 1){
			status = (String)statusList.get(0).get("status");
		}
		return status;
	}
}
