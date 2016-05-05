package nd.esp.service.lifecycle.services.knowledgebase.v06.impls;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import nd.esp.service.lifecycle.daos.knowledgebase.v06.KnowledgeBaseDao;
import nd.esp.service.lifecycle.models.v06.KnowledgeBaseModel;
import nd.esp.service.lifecycle.services.knowledgebase.v06.KnowledgeBaseService;
@Service
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService {

	@Autowired
	private KnowledgeBaseDao kbd;
	
	@Override
	@Transactional
	public KnowledgeBaseModel createKnowledgeBase(KnowledgeBaseModel kbm) {
		return kbd.createKnowledgeBase(kbm);
	}

	@Override
	public List<KnowledgeBaseModel> queryKnowledgeBaseListByKpid(String kpid) {
		return kbd.queryKnowledgeBaseListByKpid(kpid);
	}

	@Override
	public List<KnowledgeBaseModel> queryKnowledgeBaseListByCond(String kcid,
			String kpid, String knTitle) {
		return kbd.queryKnowledgeBaseListByCond(kcid, kpid, knTitle);
	}

}
