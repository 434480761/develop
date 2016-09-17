package nd.esp.service.lifecycle.services.lessons.v06;

import nd.esp.service.lifecycle.models.v06.LessonModel;

public interface LessonsServiceV06 {

	public LessonModel create(LessonModel lessonPlansModel);

	public LessonModel update(LessonModel lessonPlansViewModel);

	LessonModel patch(LessonModel model);
}