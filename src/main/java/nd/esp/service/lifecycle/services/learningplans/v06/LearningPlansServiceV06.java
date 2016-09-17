/* =============================================================
 * Created: [2015年7月15日] by caocr
 * =============================================================
 *
 * Copyright 2014-2015 NetDragon Websoft Inc. All Rights Reserved
 *
 * =============================================================
 */

package nd.esp.service.lifecycle.services.learningplans.v06;

import nd.esp.service.lifecycle.models.v06.LearningPlanModel;

/**
 * @author caocr
 * @since
 */
public interface LearningPlansServiceV06 {
    public abstract LearningPlanModel create(LearningPlanModel learningPlansModel);

    public abstract LearningPlanModel update(LearningPlanModel learningPlansModel);

    LearningPlanModel patch(LearningPlanModel learningPlansModel);
}
