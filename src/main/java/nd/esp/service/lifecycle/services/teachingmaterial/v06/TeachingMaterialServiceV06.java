package nd.esp.service.lifecycle.services.teachingmaterial.v06;

import nd.esp.service.lifecycle.models.teachingmaterial.v06.TeachingMaterialModel;

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

	TeachingMaterialModel patchTeachingMaterial(String resType,
												TeachingMaterialModel tmm);
}