package nd.esp.service.lifecycle.utils.titan.script.model.education;

import nd.esp.service.lifecycle.utils.titan.script.annotation.TitanField;
import nd.esp.service.lifecycle.utils.titan.script.annotation.TitanVertex;
import nd.esp.service.lifecycle.utils.titan.script.model.education.TitanEducation;

/**
 * Created by Administrator on 2016/8/24.
 */
@TitanVertex(label = "questions")
public class TitanQuestions extends TitanEducation {

    @TitanField(name = "preview")
    private String dbpreview;

    public String getDbpreview() {
        return dbpreview;
    }

    public void setDbpreview(String dbpreview) {
        this.dbpreview = dbpreview;
    }
}
