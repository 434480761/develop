package nd.esp.service.lifecycle.models.v06;

import nd.esp.service.lifecycle.educommon.models.ResourceModel;
/**
 * 素材业务model
 * @author xuzy
 *
 */
public class InstructionalObjectiveModel extends ResourceModel{
    private String kbId;
    
    private String ocId;

	public String getKbId() {
		return kbId;
	}

	public void setKbId(String kbId) {
		this.kbId = kbId;
	}

	public String getOcId() {
		return ocId;
	}

	public void setOcId(String ocId) {
		this.ocId = ocId;
	}
    
    
}
