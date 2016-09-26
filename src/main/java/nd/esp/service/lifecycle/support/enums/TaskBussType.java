package nd.esp.service.lifecycle.support.enums;

/**
 * Created by qil on 2016/9/21
 */
public enum TaskBussType {
    PACKAGING("packaging"),
    TRANSCODE("transcode"),
    IMAGE_TRANSCODE("image_transcode"),
    DOCUMENTS_TRANSCODE("documents_transcode"), ;

    private String value;

    public String getValue() {
        return value;
    }

    private TaskBussType(String value) {
        this.value = value;
    }
}
