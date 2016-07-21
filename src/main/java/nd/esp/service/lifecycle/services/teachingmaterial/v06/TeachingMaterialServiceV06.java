package nd.esp.service.lifecycle.services.teachingmaterial.v06;

import nd.esp.service.lifecycle.models.teachingmaterial.v06.TeachingMaterialModel;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.TeachingMaterial;

/**
 * 教材业务层接口
 * @author xuzy
 * @version 0.6
 * @created 2015-07-30
 */
public interface TeachingMaterialServiceV06{
	/**
	 * 教材创建
	 * @param rm
	 * @return
	 */
	public TeachingMaterialModel createTeachingMaterial(String resType,TeachingMaterialModel tmm);
	
	/**
	 * 教材修改
	 * @param rm
	 * @return
	 */
	public TeachingMaterialModel updateTeachingMaterial(String resType,TeachingMaterialModel tmm);

	/**
	 * 根据id查找教材
	 * @param id
	 * @return
	 * @throws EspStoreException
     */
	TeachingMaterial getById(String id) throws EspStoreException;
}