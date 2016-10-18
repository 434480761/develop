/* =============================================================
 * Created: [2015年7月15日] by caocr
 * =============================================================
 *
 * Copyright 2014-2015 NetDragon Websoft Inc. All Rights Reserved
 *
 * =============================================================
 */

package nd.esp.service.lifecycle.services.learningplans.v06.impls;

import nd.esp.service.lifecycle.educommon.services.NDResourceService;
import nd.esp.service.lifecycle.models.v06.LearningPlanModel;
import nd.esp.service.lifecycle.services.learningplans.v06.LearningPlansServiceV06;
import nd.esp.service.lifecycle.support.annotation.TitanTransaction;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author caocr
 * @since
 */
@Service
@Transactional
public class LearningPlansServiceImplV06 implements LearningPlansServiceV06 {
    @Autowired
    private NDResourceService ndResourceService;

    @Override
    @TitanTransaction
    public LearningPlanModel create(LearningPlanModel learningPlansModel) {
        return (LearningPlanModel)ndResourceService.create(ResourceNdCode.learningplans.toString(), learningPlansModel);
    }

    @Override
    @TitanTransaction
    public LearningPlanModel update(LearningPlanModel learningPlansModel) {
        return (LearningPlanModel)ndResourceService.update(ResourceNdCode.learningplans.toString(), learningPlansModel);
    }

    @Override
    @TitanTransaction
    public LearningPlanModel patch(LearningPlanModel learningPlansModel) {
        return (LearningPlanModel)ndResourceService.patch(ResourceNdCode.learningplans.toString(), learningPlansModel);
    }

}
