package nd.esp.service.lifecycle.services.Lessonplans.v06;

import nd.esp.service.lifecycle.models.v06.LessonPlanModel;

public interface LessonPlansServiceV06 {

	public abstract LessonPlanModel create(LessonPlanModel lessonPlansModel);

	public abstract LessonPlanModel update(LessonPlanModel lessonPlansViewModel);

	LessonPlanModel patch(LessonPlanModel lessonPlansModel, boolean isObvious);
}