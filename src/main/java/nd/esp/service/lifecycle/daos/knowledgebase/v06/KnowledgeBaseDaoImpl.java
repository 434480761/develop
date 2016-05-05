package nd.esp.service.lifecycle.daos.knowledgebase.v06;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
		String sql = "SELECT kb.identifier,kb.knid,kb.kpid,nd.title,nd.description,nd.creator,nd.create_time FROM knowledge_base kb,ndresource nd where kb.kpid = :kpid and nd.primary_category = 'knowledges' and nd.enable = 1 and kb.knid = nd.identifier";
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
			String sql = "SELECT kb.identifier,kb.knid,kb.kpid,nd.title,nd.description,nd.creator,nd.create_time FROM knowledge_base kb,ndresource nd where kb.kpid = :kpid and nd.primary_category = 'knowledges' and nd.enable = 1 and kb.knid = nd.identifier and (nd.title like :knTitle or nd.description like :knTitle)";
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
					returnList.add(kbm);
				}
			}
		}
		return returnList;
	}

}
