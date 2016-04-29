package nd.esp.service.lifecycle.vos.offlinemetadata.v06;

import org.hibernate.validator.constraints.NotBlank;

/**
 * 
 * @author caocr
 *
 */
public class OfflineMetadataTechInfoViewModel {
    @NotBlank(message="{resourceViewModel.techInfo.location.notBlank.validmsg}")
    private String location;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
