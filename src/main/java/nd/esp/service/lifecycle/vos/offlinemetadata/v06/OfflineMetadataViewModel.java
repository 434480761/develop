package nd.esp.service.lifecycle.vos.offlinemetadata.v06;

import java.util.Map;

import javax.validation.Valid;

/**
 * 
 * @author Administrator
 *
 */
public class OfflineMetadataViewModel {
    @Valid
    private Map<String,? extends OfflineMetadataTechInfoViewModel> techInfo;

    public Map<String, ? extends OfflineMetadataTechInfoViewModel> getTechInfo() {
        return techInfo;
    }

    public void setTechInfo(Map<String, ? extends OfflineMetadataTechInfoViewModel> techInfo) {
        this.techInfo = techInfo;
    }
}
