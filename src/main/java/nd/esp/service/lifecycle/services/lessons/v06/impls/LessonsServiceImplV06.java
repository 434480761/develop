package nd.esp.service.lifecycle.services.lessons.v06.impls;

import nd.esp.service.lifecycle.educommon.services.NDResourceService;
import nd.esp.service.lifecycle.models.v06.LessonModel;
import nd.esp.service.lifecycle.services.lessons.v06.LessonsServiceV06;
import nd.esp.service.lifecycle.support.annotation.TitanTransaction;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 课时业务实现类
 * 
 * @author caocr
 */
@Service("lessonServiceV06")
@Transactional
public class LessonsServiceImplV06 implements LessonsServiceV06 {
    @Autowired
    private NDResourceService ndResourceService;

    @Override
    @TitanTransaction
    public LessonModel create(LessonModel model) {
        return (LessonModel)ndResourceService.create(ResourceNdCode.lessons.toString(), model);
    }

    @Override
    @TitanTransaction
    public LessonModel update(LessonModel model) {
        return (LessonModel)ndResourceService.update(ResourceNdCode.lessons.toString(), model);
    }

    @Override
    @TitanTransaction
    public LessonModel patch(LessonModel model, boolean isObvious) {
        return (LessonModel)ndResourceService.patch(ResourceNdCode.lessons.toString(), model, isObvious);
    }

}
