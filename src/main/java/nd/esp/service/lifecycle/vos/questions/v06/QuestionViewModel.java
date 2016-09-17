package nd.esp.service.lifecycle.vos.questions.v06;

import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;

public class QuestionViewModel extends ResourceViewModel {
    
    private QuestionExtPropertyViewModel extProperties;
    
	public QuestionExtPropertyViewModel getExtProperties() {
        return extProperties;
    }

    public void setExtProperties(QuestionExtPropertyViewModel extProperties) {
        this.extProperties = extProperties;
    }

}
