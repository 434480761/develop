package nd.esp.service.lifecycle.vos.lessons.v06;

import java.util.Map;

import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class LessonViewModel extends ResourceViewModel {
    
    @JsonInclude(Include.NON_NULL)
    @Override
    public Map<String, String> getPreview() {
        return super.getPreview();
    }
    
}
