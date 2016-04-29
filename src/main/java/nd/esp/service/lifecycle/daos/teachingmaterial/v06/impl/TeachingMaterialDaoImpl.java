package nd.esp.service.lifecycle.daos.teachingmaterial.v06.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import nd.esp.service.lifecycle.daos.teachingmaterial.v06.TeachingMaterialDao;
import nd.esp.service.lifecycle.utils.CollectionUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import nd.esp.service.lifecycle.repository.model.TeachingMaterial;
import nd.esp.service.lifecycle.repository.sdk.TeachingMaterialRepository;

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
}
