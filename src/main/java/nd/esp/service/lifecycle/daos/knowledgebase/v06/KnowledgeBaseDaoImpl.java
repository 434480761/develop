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
			String sql = "SELECT kb.identifier,kb.knid,kb.kpid,nd.title nd.description,nd.creator,nd.create_time,cd1.title as kcname,cd2.title as kpname FROM knowledge_base kb,ndresource nd,category_relations cr,category_datas cd1,category_datas cd2 where cr.source=cd1.identifier and cr.target = cd2.identifier and cr.source=:kpid and kb.kpid = cr.target and nd.primary_category = 'knowledges' and nd.enable = 1 and kb.knid = nd.identifier and (nd.title like :knTitle or nd.description like :knTitle)";
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
			sql += " WHERE ndr.primary_category='knowledge' AND ndr.enable=1";
			sql += " AND rc.primary_category='knowledge' AND rc.taxOnCode=:kccode";
			
			Query query = em.createNativeQuery(sql);
			query.setParameter("kccode", kcCode);
			List<Object[]> list = query.getResultList();
			
			if(CollectionUtils.isNotEmpty(list)){
				for (Object[] o : list) {
					String kid = (String)o[0];
					resultList.add(kid);
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
		String sql = "select nd.title,nd.create_time,nd.creator,nd.identifier from ndresource nd,instructional_objectives io where nd.primary_category='instructionalobjectives' and nd.enable = 1 and nd.identifier = io.identifier and io.kb_id = :kbid and io.oc_id = :ocid";
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
				Map<String,Object> m = new HashMap<String, Object>();
				m.put("identifier", identifier);
				m.put("create_time", new Date(ct.longValue()));
				m.put("creator", creator);
				m.put("title", title);
				resultList.add(m);
			}
		}
		return resultList;
	}
}
