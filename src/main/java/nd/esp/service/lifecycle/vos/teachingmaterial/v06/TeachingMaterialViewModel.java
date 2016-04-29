package nd.esp.service.lifecycle.vos.teachingmaterial.v06;

import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;
/**
 * 教材V06视图层模型
 * @author xuzy
 *
 */
public class TeachingMaterialViewModel extends ResourceViewModel{
	/**
	 * 教材的扩展属性，对于教材而言，必须拥有的属性内容 
	 */
	private TmExtPropertiesViewModel extProperties;
	
	public TmExtPropertiesViewModel getExtProperties() {
		return extProperties;
	}

	public void setExtProperties(TmExtPropertiesViewModel extProperties) {
		this.extProperties = extProperties;
	}
	
	
}
