package nd.esp.service.lifecycle.utils.titan.script.model;

import nd.esp.service.lifecycle.utils.titan.script.annotation.TitanField;
import nd.esp.service.lifecycle.utils.titan.script.annotation.TitanVertex;

/**
 * Created by Administrator on 2016/8/24.
 */
@TitanVertex(label = "questions")
public class TitanQuestions extends TitanEducation{

    @TitanField(name = "preview")
    private String dbpreview;

    public String getDbpreview() {
        return dbpreview;
    }

    public void setDbpreview(String dbpreview) {
        this.dbpreview = dbpreview;
    }
}
