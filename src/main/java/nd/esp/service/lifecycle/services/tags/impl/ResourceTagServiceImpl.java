package nd.esp.service.lifecycle.services.tags.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.Chapter;
import nd.esp.service.lifecycle.repository.model.ResourceTags;
import nd.esp.service.lifecycle.repository.sdk.ChapterRepository;
import nd.esp.service.lifecycle.repository.sdk.ResourceTagRepository;
import nd.esp.service.lifecycle.services.tags.ResourceTagService;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.ParamCheckUtil;
import nd.esp.service.lifecycle.vos.v06.ResourceTagViewModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
@Service
public class ResourceTagServiceImpl implements ResourceTagService {
	@Autowired
	private ChapterRepository chapterRepository;
	
	@Autowired
	private ResourceTagRepository resourceTagRepository;
	
	@Autowired
	@Qualifier(value="defaultJdbcTemplate")
	private JdbcTemplate jt;
	
	@PersistenceContext(unitName="entityManagerFactory")
	private EntityManager em;
	
	@Override
	public Map<String, String> addResourceTags(String cid,
			Map<String, Integer> params) {
		long time = System.currentTimeMillis();
		Map<String,String> returnMap = new HashMap<String, String>();
		//1、判断章节是否存在
		try {
			Chapter c = chapterRepository.get(cid);
			if(c == null){
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.ChapterNotFound);
			}
		} catch (EspStoreException e) {
			e.printStackTrace();
		}
		
		//2、根据章节、标签获取统计数量
		List<ResourceTags> queryResult = null;
		if(params != null){
			Set<String> keys = params.keySet();
			String sql = "select * from resource_tags where resource = :resource and tag in (:tags)";
			Query query = em.createNativeQuery(sql, ResourceTags.class);
			query.setParameter("resource", cid);
			query.setParameter("tags", keys);
			queryResult = query.getResultList();
		}
		
		//3、检验入参是否通过
		List<ResourceTags> addList = new ArrayList<ResourceTags>();
		Iterator<Entry<String, Integer>> it = params.entrySet().iterator();
		while (it.hasNext()) {
			boolean flag = false;
			Entry<String, Integer> entry = it.next();
			String key = entry.getKey();
			Integer c = entry.getValue();
			for (ResourceTags rt : queryResult) {
				if(rt.getTag().equals(key)){
					flag = true;
					if(c == null){
						returnMap.put(key, "参数有错误");
						break;
					}
					int r = rt.getCount() + c.intValue();
					if(r < 0){
						returnMap.put(key, "参数有错误，目前tag的统计值为"+rt.getCount());
						break;
					}else{
						ResourceTags tmp = new ResourceTags();
						tmp.setIdentifier(rt.getIdentifier());
						tmp.setCount(r);
						tmp.setCt(rt.getCt());
						tmp.setLu(new BigDecimal(time));
						tmp.setResource(cid);
						tmp.setTag(key);
						addList.add(tmp);
					}
				}
			}
			if(!flag && CollectionUtils.isEmpty(returnMap)){
				if(c.intValue() < 0){
					returnMap.put(key, "不存在此tag，参数值不能为负整数");
				}else{
					ResourceTags tmp = new ResourceTags();
					tmp.setIdentifier(UUID.randomUUID().toString());
					tmp.setCount(c.intValue());
					tmp.setCt(new BigDecimal(time));
					tmp.setLu(new BigDecimal(time));
					tmp.setResource(cid);
					tmp.setTag(key);
					addList.add(tmp);
				}
			}
		}
		
		if(CollectionUtils.isNotEmpty(returnMap)){
			return returnMap;
		}
		
		//4、更新数据入库
		if(CollectionUtils.isNotEmpty(addList)){
			try {
				resourceTagRepository.batchAdd(addList);
			} catch (EspStoreException e) {
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.StoreSdkFail);
			}
		}
		return null;
	}

	@Override
	public List<ResourceTagViewModel> queryResourceTagsByCid(String cid,
			String limit) {
		List<ResourceTagViewModel> returnList = new ArrayList<ResourceTagViewModel>();
		//1、校验limit的合法性
        Integer[] result = ParamCheckUtil.checkLimit(limit);
        
        String sql = "select * from resource_tags where resource = :resource order by count desc limit :offset,:pagesize";
		Query query = em.createNativeQuery(sql, ResourceTags.class);
		query.setParameter("resource", cid);
		query.setParameter("offset", result[0]);
		query.setParameter("pagesize", result[1]);
		List<ResourceTags> queryResult = query.getResultList();
		if(CollectionUtils.isNotEmpty(queryResult)){
			for (ResourceTags resourceTag : queryResult) {
				ResourceTagViewModel rtvm = new ResourceTagViewModel();
				rtvm.setCount(resourceTag.getCount());
				rtvm.setResource(resourceTag.getResource());
				rtvm.setTag(resourceTag.getTag());
				returnList.add(rtvm);
			}
		}
		return returnList;
	}

}
