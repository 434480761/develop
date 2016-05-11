package nd.esp.service.lifecycle.services.knowledgebase.v06.impls;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import nd.esp.service.lifecycle.daos.knowledgebase.v06.KnowledgeBaseDao;
import nd.esp.service.lifecycle.models.v06.KnowledgeBaseModel;
import nd.esp.service.lifecycle.repository.model.KnowledgeBase;
import nd.esp.service.lifecycle.services.knowledgebase.v06.KnowledgeBaseService;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.utils.CollectionUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService {
	
	private final static ExecutorService executorService = CommonHelper.getPrimaryExecutorService();
	
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

	@Override
	@Transactional
	public void batchAddKbWhenKpAdd(final String kcCode, final String kpid) {
		executorService.execute(new Runnable() {

			@Override
			public void run() {
				//根据kcCode查出所有相关的知识点id
				List<String> kids = kbd.queryKnowledgeByKcCode(kcCode);
				
				//批量添加KnowledgeBase
				if(CollectionUtils.isNotEmpty(kids)){
					List<KnowledgeBase> kbList = new ArrayList<KnowledgeBase>();
					for(String kid : kids){
						KnowledgeBase kb = new KnowledgeBase();
						kb.setKnid(kid);
						kb.setKpid(kpid);
						kb.setIdentifier(UUID.randomUUID().toString());
						
						kbList.add(kb);
					}
					
					if(CollectionUtils.isNotEmpty(kbList)){
						kbd.batchCreateKnowledgeBase(kbList);
					}
				}
			}
			
		});
	}
	
	public List<Map<String,Object>> queryInstructionalObjectiveByCond(String kbid,String ocid){
		return kbd.queryInstructionalObjectiveByCond(kbid, ocid);
	}
}
