package nd.esp.service.lifecycle.vos.ebooks.v06;

import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;
/**
 * 电子教材V06视图层模型
 * @author xuzy
 *
 */
public class EbookViewModel extends ResourceViewModel{
	/**
	 * 电子教材的扩展属性，对于电子教材而言，必须拥有的属性内容 
	 */
	private EbookExtPropertiesViewModel extProperties;
	
	public EbookExtPropertiesViewModel getExtProperties() {
		return extProperties;
	}

	public void setExtProperties(EbookExtPropertiesViewModel extProperties) {
		this.extProperties = extProperties;
	}
	
	
}
