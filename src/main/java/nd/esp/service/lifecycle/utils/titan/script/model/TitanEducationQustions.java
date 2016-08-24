package nd.esp.service.lifecycle.utils.titan.script.model;

import nd.esp.service.lifecycle.utils.titan.script.annotation.TitanField;
import nd.esp.service.lifecycle.utils.titan.script.annotation.TitanVertex;

/**
 * Created by Administrator on 2016/8/24.
 */
@TitanVertex(label = "questions")
public class TitanEducationQustions {

    @TitanField()
    private String dbpreview;
}
