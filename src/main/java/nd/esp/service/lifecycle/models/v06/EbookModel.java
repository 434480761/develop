package nd.esp.service.lifecycle.models.v06;

import nd.esp.service.lifecycle.educommon.models.ResourceModel;

/**
 * 电子教材业务model
 * 
 * @author linsm
 */
public class EbookModel extends ResourceModel {
    /**
     * 电子教材的扩展属性
     */
    private EbookExtPropertiesModel extProperties;

    public EbookExtPropertiesModel getExtProperties() {
        return extProperties;
    }

    public void setExtProperties(EbookExtPropertiesModel extProperties) {
        this.extProperties = extProperties;
    }
}
