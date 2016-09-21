package nd.esp.service.lifecycle.utils.titan.script.model;

import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.model.*;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;
import nd.esp.service.lifecycle.utils.BeanMapperUtils;
import nd.esp.service.lifecycle.utils.titan.script.model.education.*;
import org.springframework.context.annotation.Bean;

import javax.tools.Tool;

/**
 * Created by Administrator on 2016/9/14.
 */
public class EducationToTitanBeanUtils {
    public static TitanModel toVertex(EspEntity entity){
        TitanModel model = null;
        if (entity instanceof Education){
            model = toVertex4Education((Education) entity);
        }
        if (entity instanceof ResCoverage){
            model = BeanMapperUtils.beanMapper(entity,TitanResCoverageVertex.class);
        }

        if (entity instanceof ResourceCategory){
            model = BeanMapperUtils.beanMapper(entity,TitanResourceCategory.class);
        }

        if (entity instanceof TechInfo){
            model = BeanMapperUtils.beanMapper(entity,TitanTechInfo.class);
        }

        if (entity instanceof ResourceStatistical){
            model = BeanMapperUtils.beanMapper(entity, TitanResourceStatistical.class);
        }

        return model;
    }

    public static TitanModel toEdge(EspEntity entity){
        TitanModel model = null;
        if (entity instanceof  ResCoverage){
            model = BeanMapperUtils.beanMapper(entity, TitanResCoverageEdge.class);
        }

        if (entity instanceof ResourceCategory){
            model = BeanMapperUtils.beanMapper(entity,TitanResourceCategoryEdge.class);
        }

        if (entity instanceof TechInfo){
            model = BeanMapperUtils.beanMapper(entity,TitanTechInfoEdge.class);
        }

        if (entity instanceof ResourceRelation){
            model = BeanMapperUtils.beanMapper(entity,TitanResourceRelationEdge.class);
        }

        if (entity instanceof ResourceStatistical){
            model = BeanMapperUtils.beanMapper(entity, TitanResourceStatisticalEdge.class);
        }

        if (entity instanceof KnowledgeRelation){
            model = BeanMapperUtils.beanMapper(entity, TitanKnowledgeRelation.class);
        }
        return model;
    }

    private static TitanModel toVertex4Education(Education education){
        String primaryCategory = education.getPrimaryCategory();
        if (ResourceNdCode.assets.equals(ResourceNdCode.fromString(primaryCategory))){
            return BeanMapperUtils.beanMapper(education,TitanAsset.class);
        }
        if (ResourceNdCode.teachingmaterials.equals(ResourceNdCode.fromString(primaryCategory))){
            return BeanMapperUtils.beanMapper(education,TitanTeachingMaterial.class);
        }
        if (ResourceNdCode.chapters.equals(ResourceNdCode.fromString(primaryCategory))){
            return BeanMapperUtils.beanMapper(education,TitanChaper.class);
        }
        if (ResourceNdCode.lessons.equals(ResourceNdCode.fromString(primaryCategory))){
            return BeanMapperUtils.beanMapper(education,TitanLesson.class);
        }
        if (ResourceNdCode.instructionalobjectives.equals(ResourceNdCode.fromString(primaryCategory))){
            return BeanMapperUtils.beanMapper(education,TitanInstructionalObjective.class);
        }
        if (ResourceNdCode.knowledges.equals(ResourceNdCode.fromString(primaryCategory))){
            return BeanMapperUtils.beanMapper(education,TitanKnowledge.class);
        }
        if (ResourceNdCode.guidancebooks.equals(ResourceNdCode.fromString(primaryCategory))){
            return BeanMapperUtils.beanMapper(education,TitanGuidanceBooks.class);
        }
        if (ResourceNdCode.subInstruction.equals(ResourceNdCode.fromString(primaryCategory))){
            return BeanMapperUtils.beanMapper(education,TitanSubInstruction.class);
        }
        if (ResourceNdCode.coursewares.equals(ResourceNdCode.fromString(primaryCategory))){
            return BeanMapperUtils.beanMapper(education,TitanCourseware.class);
        }
        if (ResourceNdCode.coursewareobjects.equals(ResourceNdCode.fromString(primaryCategory))){
            return BeanMapperUtils.beanMapper(education,TitanCoursewareObject.class);
        }
        if (ResourceNdCode.coursewareobjecttemplates.equals(ResourceNdCode.fromString(primaryCategory))){
            return BeanMapperUtils.beanMapper(education,TitanCoursewareObjectTemplate.class);
        }
        if (ResourceNdCode.learningplans.equals(ResourceNdCode.fromString(primaryCategory))){
            return BeanMapperUtils.beanMapper(education,TitanLearningPlan.class);
        }
        if (ResourceNdCode.ebooks.equals(ResourceNdCode.fromString(primaryCategory))){
            return BeanMapperUtils.beanMapper(education,TitanEbook.class);
        }
        if (ResourceNdCode.teachingactivities.equals(ResourceNdCode.fromString(primaryCategory))){
            return BeanMapperUtils.beanMapper(education,TitanTeachingActivities.class);
        }
        if (ResourceNdCode.knowledges.equals(ResourceNdCode.fromString(primaryCategory))){
            return BeanMapperUtils.beanMapper(education,TitanKnowledge.class);
        }
        if (ResourceNdCode.questions.equals(ResourceNdCode.fromString(primaryCategory))){
            return BeanMapperUtils.beanMapper(education,TitanQuestion.class);
        }
        if (ResourceNdCode.examinationpapers.equals(ResourceNdCode.fromString(primaryCategory))){
            return BeanMapperUtils.beanMapper(education,TitanExaminationPaper.class);
        }
        if (ResourceNdCode.learningplans.equals(ResourceNdCode.fromString(primaryCategory))){
            return BeanMapperUtils.beanMapper(education,TitanLearningPlan.class);
        }
        if (ResourceNdCode.tools.equals(ResourceNdCode.fromString(primaryCategory))){
            return BeanMapperUtils.beanMapper(education,TitanTool.class);
        }
        if (ResourceNdCode.metacurriculums.equals(ResourceNdCode.fromString(primaryCategory))){
            return BeanMapperUtils.beanMapper(education,TitanTool.class);
        }
        return null;
    }
}
