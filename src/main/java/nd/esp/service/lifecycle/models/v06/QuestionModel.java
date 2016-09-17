package nd.esp.service.lifecycle.models.v06;

import nd.esp.service.lifecycle.educommon.models.ResourceModel;
/**
 * 素材业务model
 * @author xuzy
 *
 */
public class QuestionModel extends ResourceModel{
    
    private String questionType;
    
    private QuestionExtPropertyModel extProperties;

    public QuestionExtPropertyModel getExtProperties() {
        return extProperties;
    }

    public void setExtProperties(QuestionExtPropertyModel extProperties) {
        this.extProperties = extProperties;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }
}
