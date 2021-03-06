package nd.esp.service.lifecycle.daos.knowledgebase.v06;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import nd.esp.service.lifecycle.models.v06.KnowledgeBaseModel;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.KnowledgeBase;
import nd.esp.service.lifecycle.repository.sdk.KnowledgeBaseRepository;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.utils.BeanMapperUtils;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

@Repository
public class KnowledgeBaseDaoImpl implements KnowledgeBaseDao {
	@Autowired
	private KnowledgeBaseRepository knowledgeBaseRepository;
	
	@PersistenceContext(unitName="entityManagerFactory")
	private EntityManager em;
	
	@Override
	public KnowledgeBaseModel createKnowledgeBase(KnowledgeBaseModel kbm) {
		KnowledgeBase kb = BeanMapperUtils.beanMapper(kbm, KnowledgeBase.class);
		kb.setPrimaryCategory("knowledgebases");
		try {
			kb = knowledgeBaseRepository.add(kb);
		} catch (EspStoreException e) {
			e.printStackTrace();
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR, "LC/CREATE_KNOWLEDGEBASE_ERROR", "创建知识库出错了");
		}
		return BeanMapperUtils.beanMapper(kb, KnowledgeBaseModel.class);
	}

	@Override
	public List<KnowledgeBaseModel> queryKnowledgeBaseListByKpid(String kpid) {
		List<KnowledgeBaseModel> returnList = new ArrayList<KnowledgeBaseModel>();
		String sql = "SELECT kb.identifier,kb.knid,kb.kpid,nd.title,nd.description,nd.creator,nd.create_time,cd1.title as kcname,cd2.title as kpname FROM knowledge_base kb,ndresource nd,category_relations cr,category_datas cd1,category_datas cd2 where cr.source=cd1.identifier and cr.target = cd2.identifier and cr.source=:kpid and kb.kpid = cr.target and nd.primary_category = 'knowledges' and nd.enable = 1 and kb.knid = nd.identifier";
		Query query = em.createNativeQuery(sql);
		query.setParameter("kpid", kpid);
		List<Object[]> list = query.getResultList();
		if(CollectionUtils.isNotEmpty(list)){
			for (Object[] o : list) {
				KnowledgeBaseModel kbm = new KnowledgeBaseModel();
				kbm.setIdentifier((String)o[0]);
				kbm.setKnid((String)o[1]);
				kbm.setKpid((String)o[2]);
				kbm.setTitle((String)o[3]);
				kbm.setDescription((String)o[4]);
				kbm.setCreator((String)o[5]);
				if(o[6] != null){
					kbm.setCreateTime(new Date(((BigInteger)o[6]).longValue()));
				}
				kbm.setKcName((String)o[7]);
				kbm.setKpName((String)o[8]);
				returnList.add(kbm);
			}
		}
		return returnList;
	}

	@Override
	public List<KnowledgeBaseModel> queryKnowledgeBaseListByCond(String kcid,
			String kpid, String knTitle) {
		List<KnowledgeBaseModel> returnList = new ArrayList<KnowledgeBaseModel>();
		knTitle = "%"+knTitle+"%";
		if(StringUtils.isNotEmpty(knTitle)){
			String sql = "SELECT kb.identifier,kb.knid,kb.kpid,nd.title,nd.description,nd.creator,nd.create_time,cd1.title as kcname,cd2.title as kpname FROM knowledge_base kb,ndresource nd,category_relations cr,category_datas cd1,category_datas cd2 where cr.source=cd1.identifier and cr.target = cd2.identifier and cr.source=:kpid and kb.kpid = cr.target and nd.primary_category = 'knowledges' and nd.enable = 1 and kb.knid = nd.identifier and (nd.title like :knTitle or nd.description like :knTitle)";
			Query query = em.createNativeQuery(sql);
			query.setParameter("kpid", kpid);
			query.setParameter("knTitle", knTitle);
			List<Object[]> list = query.getResultList();
			if(CollectionUtils.isNotEmpty(list)){
				for (Object[] o : list) {
					KnowledgeBaseModel kbm = new KnowledgeBaseModel();
					kbm.setIdentifier((String)o[0]);
					kbm.setKnid((String)o[1]);
					kbm.setKpid((String)o[2]);
					kbm.setTitle((String)o[3]);
					kbm.setDescription((String)o[4]);
					kbm.setCreator((String)o[5]);
					if(o[6] != null){
						kbm.setCreateTime(new Date(((BigInteger)o[6]).longValue()));
					}
					kbm.setKcName((String)o[7]);
					kbm.setKpName((String)o[8]);
					returnList.add(kbm);
				}
			}
		}
		return returnList;
	}

	@Override
	public List<String> queryKnowledgeByKcCode(String kcCode) {
		List<String> resultList = new ArrayList<String>();
		if(StringUtils.isNotEmpty(kcCode)){
			String sql = "SELECT ndr.identifier FROM ndresource ndr INNER JOIN resource_categories rc";
			sql += " ON ndr.identifier=rc.resource";
			sql += " WHERE ndr.primary_category='knowledges' AND ndr.enable=1";
			sql += " AND rc.primary_category='knowledges' AND rc.taxOnCode=:kccode";
			
			Query query = em.createNativeQuery(sql);
			query.setParameter("kccode", kcCode);
			List<String> list = query.getResultList();
			
			if(CollectionUtils.isNotEmpty(list)){
				for (String o : list) {
					resultList.add(o);
				}
			}
		}
		
		return resultList;
	}

	@Override
	public void batchCreateKnowledgeBase(List<KnowledgeBase> kbList) {
		if(CollectionUtils.isNotEmpty(kbList)){
			try {
				knowledgeBaseRepository.batchAdd(kbList);
			} catch (EspStoreException e) {
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
						"LC/BATCH_CREATE_KNOWLEDGEBASE_ERROR", "批量创建知识库出错了");
			}
		}
	}

	@Override
	public List<Map<String, Object>> queryInstructionalObjectiveByCond(
			String kbid, String ocid) {
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		String sql = "select nd.title,nd.create_time,nd.creator,nd.identifier,nd.description from ndresource nd,instructional_objectives io where nd.primary_category='instructionalobjectives' and nd.enable = 1 and nd.identifier = io.identifier and io.kb_id = :kbid and io.oc_id = :ocid";
		Query query = em.createNativeQuery(sql);
		query.setParameter("kbid", kbid);
		query.setParameter("ocid", ocid);
		List<Object[]> list = query.getResultList();
		if(CollectionUtils.isNotEmpty(list)){
			for (Object[] o : list) {
				String title = (String)o[0];
				BigInteger ct = (BigInteger)o[1];
				String creator = (String)o[2];
				String identifier = (String)o[3];
				String description = (String)o[4];
				Map<String,Object> m = new HashMap<String, Object>();
				m.put("identifier", identifier);
				m.put("create_time", new Date(ct.longValue()));
				m.put("creator", creator);
				m.put("title", title);
				m.put("description", description);
				resultList.add(m);
			}
		}
		return resultList;
	}
	
	public List<String> queryKpIdByKcId(String kcId){
		List<String> returnList = new ArrayList<String>();
		String sql = "select target from category_relations where source = :kcId";
		Query query = em.createNativeQuery(sql);
		query.setParameter("kcId", kcId);
		List<String> list = query.getResultList();
		if(CollectionUtils.isNotEmpty(list)){
			for (String s : list) {
				returnList.add(s);
			}
		}
		return returnList;
	}

	@Override
	public List<Map<String, Object>> queryInstructionalObjectiveByKid(
			String kcCode, String kpId) {
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		String sql = "SELECT "+
					 " nd.title,"+
					 " nd.create_time,"+
					 " nd.creator,"+
					 " nd.identifier,"+
					 " nd.description "+
					"FROM"+
					"  ndresource nd,"+
					"  instructional_objectives io,"+
					"  chapters kn,"+
					"  knowledge_base kb,"+
					"  ndresource nd2,"+
					"  resource_categories rc "+
					"WHERE nd.primary_category = 'instructionalobjectives' "+
					"  AND nd.enable = 1 "+
					"  AND nd.identifier = io.identifier "+
					"  AND io.kb_id = kb.identifier "+
					"  AND kb.knid = kn.identifier "+
					"  AND nd2.primary_category = 'knowledges' "+
					"  AND nd2.identifier = kn.identifier "+
					"  AND rc.taxOnCode = :kcCode "+
					"  AND rc.primary_category = 'knowledges' "+
					"  AND rc.resource = nd2.identifier "+
					"  AND nd2.enable = 1 "+
					"  AND kb.kpid = :kpId ";
		Query query = em.createNativeQuery(sql);
		query.setParameter("kpId", kpId);
		query.setParameter("kcCode",kcCode);
		List<Object[]> list = query.getResultList();
		if(CollectionUtils.isNotEmpty(list)){
			for (Object[] o : list) {
				String title = (String)o[0];
				BigInteger ct = (BigInteger)o[1];
				String creator = (String)o[2];
				String identifier = (String)o[3];
				String description = (String)o[4];
				Map<String,Object> m = new HashMap<String, Object>();
				m.put("identifier", identifier);
				m.put("create_time", new Date(ct.longValue()));
				m.put("creator", creator);
				m.put("title", title);
				m.put("description", description);
				resultList.add(m);
			}
		}
		return resultList;
	}

	@Override
	public List<KnowledgeBaseModel> queryKnowledgeBaseListByKcCode(
			String kcCode, String title) {
		List<KnowledgeBaseModel> returnList = new ArrayList<KnowledgeBaseModel>();
		String sql = null;
		if(StringUtils.isNotEmpty(title)){
			title = "%"+title+"%";
			sql = "SELECT kb.identifier,kb.knid,kb.kpid,nd.title,nd.description,nd.creator,nd.create_time,cd1.title as kcname,cd2.title as kpname,cd1.identifier as kcid FROM knowledge_base kb,ndresource nd,category_relations cr,category_datas cd1,category_datas cd2 where cr.source=cd1.identifier and cr.target = cd2.identifier and cd1.nd_code =:kcCode and cr.source=cd1.identifier and kb.kpid = cr.target and nd.primary_category = 'knowledges' and nd.enable = 1 and kb.knid = nd.identifier and (nd.title like :title or nd.description like :title)";
		}else{
			sql = "SELECT kb.identifier,kb.knid,kb.kpid,nd.title,nd.description,nd.creator,nd.create_time,cd1.title as kcname,cd2.title as kpname,cd1.identifier as kcid FROM knowledge_base kb,ndresource nd,category_relations cr,category_datas cd1,category_datas cd2 where cr.source=cd1.identifier and cr.target = cd2.identifier and cd1.nd_code =:kcCode and cr.source=cd1.identifier and kb.kpid = cr.target and nd.primary_category = 'knowledges' and nd.enable = 1 and kb.knid = nd.identifier";
		}
		
		Query query = em.createNativeQuery(sql);
		query.setParameter("kcCode", kcCode);
		if(StringUtils.isNotEmpty(title)){
			query.setParameter("title", title);
		}
		List<Object[]> list = query.getResultList();
		if(CollectionUtils.isNotEmpty(list)){
			for (Object[] o : list) {
				KnowledgeBaseModel kbm = new KnowledgeBaseModel();
				kbm.setIdentifier((String)o[0]);
				kbm.setKnid((String)o[1]);
				kbm.setKpid((String)o[2]);
				kbm.setTitle((String)o[3]);
				kbm.setDescription((String)o[4]);
				kbm.setCreator((String)o[5]);
				if(o[6] != null){
					kbm.setCreateTime(new Date(((BigInteger)o[6]).longValue()));
				}
				kbm.setKcName((String)o[7]);
				kbm.setKpName((String)o[8]);
				kbm.setKcid((String)o[9]);
				returnList.add(kbm);
			}
		}
		return returnList;
	}

	@Override
	public List<String> queryKpIdByKcCode(String kcCode) {
		List<String> returnList = new ArrayList<String>();
		String sql = "select cr.target from category_relations cr,category_datas cd where source = cd.identifier and cd.nd_code = :kcCode";
		Query query = em.createNativeQuery(sql);
		query.setParameter("kcCode", kcCode);
		List<String> list = query.getResultList();
		if(CollectionUtils.isNotEmpty(list)){
			for (String s : list) {
				returnList.add(s);
			}
		}
		return returnList;
	}
}
