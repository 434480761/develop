package nd.esp.service.lifecycle.vos.knowledges.v06;

import java.util.Map;

import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class KnowledgeViewModel4Out extends ResourceViewModel {
    // 知识点扩展属性
    @JsonInclude(Include.NON_NULL)
    private KnowledgeExtPropertiesViewModel4Out extProperties;

    @JsonInclude(Include.NON_NULL)
    @Override
    public Map<String, String> getPreview() {
        return super.getPreview();
    }

    public KnowledgeExtPropertiesViewModel4Out getExtProperties() {
        return extProperties;
    }

    public void setExtProperties(KnowledgeExtPropertiesViewModel4Out extProperties) {
        this.extProperties = extProperties;
    }

}
