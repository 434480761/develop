package nd.esp.service.lifecycle.models.teachingmaterial.v06;

import nd.esp.service.lifecycle.educommon.models.ResourceModel;
/**
 * 教材业务model
 * @author xuzy
 *
 */
public class TeachingMaterialModel extends ResourceModel{
	/**
	 * 教材的扩展属性
	 */
	private TmExtPropertiesModel extProperties;

	public TmExtPropertiesModel getExtProperties() {
		return extProperties;
	}

	public void setExtProperties(TmExtPropertiesModel extProperties) {
		this.extProperties = extProperties;
	}
	
}
