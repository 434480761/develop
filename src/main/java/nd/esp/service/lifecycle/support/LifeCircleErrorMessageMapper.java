package nd.esp.service.lifecycle.support;

import java.io.UnsupportedEncodingException;

import nd.esp.service.lifecycle.app.LifeCircleApplicationInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @title 资源生命周期错误信息定义
 * @Desc TODO
 * @see com.nd.gaea.rest.exceptions.extendExceptions.WafSimpleException
 * @author liuwx
 * @version 1.0
 * @create 2015年1月26日 上午11:19:35
 */
public enum LifeCircleErrorMessageMapper implements MessageMapper{

    RegValidationFail("LC/REG_VALID_FAIL",getPropertyValue("regValidator.check.loadProp.fail")),
    CheckIdentifierFail("LC/CHECK_PARAM_VALID_FAIL",getPropertyValue("paramCheckUtil.checkIdentifier.reg.fail")),
    CheckKeywordsLengthFail("LC/CHECK_PARAM_VALID_FAIL",getPropertyValue("paramCheckUtil.checkKeywordsLength.fail")),
    CheckTagsLengthFail("LC/CHECK_PARAM_VALID_FAIL",getPropertyValue("paramCheckUtil.checkTagsLength.fail")),
	AssetListViewModelError("LC/ASSET_GETDOWNLOADURL_ERROR",getPropertyValue("assetController.getDownloadUrl.operation.fail")),
	AssetListViewModelWordsError("LC/ASSET_QUERY_ERROR",getPropertyValue("assetController.queryAssets.operation.fail")),
	AssetQueryListBatchError("LC/ASSET_QUERY_LIST_BATCH_ERROR",getPropertyValue("assetController.assetList.operation.fail")),
	AssetListViewModelCategoryssError("LC/ASSET_QUERY_ERROR",getPropertyValue("assetController.queryAssetsByCategory.operation.fail")),	
	AssetGetDownloadUrlError("LC/ASSET_GETDOWNLOADURL_ERROR",getPropertyValue("assetController.getDownloadUrl.operation.fail")),
	AssetNotFound("LC/ASSET_NOT_FOUND",getPropertyValue("assetController.getDetail.operation.fail")),
	CourseWareNotFound("LC/COURSEWARE_NOT_FOUND",getPropertyValue("courseWareController.queryList.operation.fail")),
	CourseWareQueryListBatchError("LC/COURSEWARE_QUERY_LIST_BATCH_ERROR",getPropertyValue("courseWareController.courseWareList.operation.fail")),
	
	QueryCourseWareFail("LC/QUERY_COURSEWARE_FAIL",getPropertyValue("courseWareController.queryListByCategories.operation.fail")),
	CourseWareUpdatePageFail("LC/COURSEWARE_UPDATE_PAGE_FAIL",getPropertyValue("CourseWareServiceImpl.updateLom.operation.fail")),
	CourseWareObjectTemplateNotFound("LC/COURSEWARE_OBJECT_TEMPLATE_NOT_FOUND",getPropertyValue("courseWareObjectTemplateController.deleteCourseWareObjectTemplate.check.fail")),
	QuestionNotFound("LC/QUESTION_NOT_FOUND",getPropertyValue("questionController.getDetail.operation.fail")),
	LimitParamMissing("LC/LIMIT_PARAM_MISSING",getPropertyValue("paramCheckUtil.checkLimit.missParam.fail")),
	LimitParamIllegal("LC/LIMIT_PARAM_ILLEGAL",getPropertyValue("paramCheckUtil.checkLimit.illegalParam.fail")),
	LessonNotFound("LC/LESSON_NOT_FOUND",getPropertyValue("lessonController.getLesson.operation.fail")),
	ChangeObjectNotExist("LC/CHANGE_OBJECT_NOT_EXIST",getPropertyValue("teachingMaterialController.updateTeachingMaterial.check.fail")),
	CourseWareNotExist("LC/COURSEWARE_NOT_EXIST",getPropertyValue("courseWareController.updateCourseWare.check.fail")),
	LessonNotExist("LC/LESSON_NOT_EXIST",getPropertyValue("lessonController.updateLesson.check.fail")),
	PrototypeActivityStepNotExist("LC/PROTOTYPE_ACTIVITY_STEP_NOT_EXIST",getPropertyValue("prototypeActivityStepController.update.check.fail")),
	CreateCourseWareObjectFail("LC/CREATE_COURSEWAREOBJECT_FAIL",getPropertyValue("courseWareObjectServiceImpl.initLom.operation.fail")),
	
	DeleteAssetSuccess("LC/DELETE_ASSET_SUCCESS",getPropertyValue("assetController.deleteAsset.operation.success")),
	DeleteAssetFail("LC/DELETE_ASSET_FAIL",getPropertyValue("assetController.deleteAsset.operation.fail")),
	DeleteCourseWareSuccess("LC/DELETE_COURSEWARE_SUCCESS",getPropertyValue("courseWareController.deleteCourseWare.operation.success")),
	DeleteCourseWareFail("LC/DELETE_COURSEWARE_FAIL",getPropertyValue("courseWareController.deleteCourseWare.operation.fail")),
	DeleteCourseWareObjectSuccess("LC/DELETE_COURSEWAREOBJECT_SUCCESS",getPropertyValue("courseWareObjectController.deleteCourseWareObject.operation.success")),
	DeleteCourseWareObjectFail("LC/DELETE_COURSEWAREOBJECT_FAIL",getPropertyValue("courseWareObjectController.deleteCourseWareObject.operation.fail")),
	DeleteCourseWareObjectTemplateSuccess("LC/DELETE_COURSEWAREOBJECTTEMPLATE_SUCCESS",getPropertyValue("courseWareObjectTemplateController.deleteCourseWareObjectTemplate.operation.success")),
	DeleteCourseWareObjectTemplateFail("LC/DELETE_COURSEWAREOBJECTTEMPLATE_FAIL",getPropertyValue("courseWareObjectTemplateController.deleteCourseWareObjectTemplate.operation.fail")),
    DeleteObjectiveKnowledgeRelationSuccess("LC/DELETE_OBJECTIVEKNOWLEDGERELATION_SUCCESS",getPropertyValue("instructionalObjectiveController.deleteKnowledgeRelation.operation.success")),
    DeleteLessonSuccess("LC/DELETE_LESSON_SUCCESS",getPropertyValue("lessonController.deleteLesson.operation.success")),
    DeleteLessonFail("LC/DELETE_LESSON_FAIL",getPropertyValue("lessonController.deleteLesson.operation.fail")),
    DeleteKnowledgeFail("LC/DELETE_KNOWLEDGE_FAIL",getPropertyValue("knowledgeController.delete.operation.fail")),
    DeletePrototypeFail("LC/DELETE_KNOWLEDGE_FAIL",getPropertyValue("instructionalPrototypesController.delete.operation.fail")),
    DeleteKnowledgeSuccess("LC/DELETE_KNOWLEDGE_SUCCESS",getPropertyValue("knowledgeController.delete.operation.success")),
    DeleteEducationRelationSuccess("LC/DELETE_EDUCATION_RELATION_SUCCESS",getPropertyValue("educationRelationController..delete.operation.success")),
    DeletePrototypeSuccess("LC/DELETE_KNOWLEDGE_SUCCESS",getPropertyValue("instructionalPrototypesController.delete.operation.success")),
    DeleteKnowledgesRelationFail("LC/DELETE_KNOWLEDGESRELATION_FAIL",getPropertyValue("knowledgeRelationController.removeRelation.operation.fail")),
    DeleteKnowledgesChapterFail("LC/DELETE_KNOWLEDGESCHAPTER_FAIL",getPropertyValue("knowledgeController.removeKnowledgeChapter.operation.fail")),
    TagNotExist("LC/TAG_NOT_EXIST",getPropertyValue("knowledgeServiceImplV06.deleteKnowledgeChapterKnowledge.check.fail")),
    DeleteProtoAssRelationFail("LC/DELETE_PROTOTYPE_ASSIGNMENT_RELATION_FAIL",getPropertyValue("protoAssRelationController.removeRelation.operation.fail")),
    DeleteChapterQuestionRelationFail("LC/DELETE_CHAPTER_QUESTION_RELATION_FAIL",getPropertyValue("questionServiceImpl.delChapterRelation.operation.fail")),
    DeleteKnowledgesRelationSuccess("LC/DELETE_KNOWLEDGESRELATION_SUCCESS",getPropertyValue("knowledgeRelationController.removeRelation.operation.success")),
    DeleteKnowledgesChapterSuccess("LC/DELETE_KNOWLEDGESCHAPTER_SUCCESS",getPropertyValue("knowledgeController.removeKnowledgeChapter.operation.success")),
    DeleteProtoAssRelationSuccess("LC/DELETE_PROTOTYPE_ASSIGNMENT_RELATION_SUCCESS",getPropertyValue("protoAssRelationController.removeRelation.operation.success")),
    DeleteEducationRelationFail("LC/DELETE_RELATION_FAIL",getPropertyValue("educationRelationController.remove.operation.fail")),
    DeleteLessonPlansSuccess("LC/DELETE_LESSON_PLANS_SUCCESS",getPropertyValue("lessonPlansController.deleteLessonPlans.operation.success")),
    
    QuestionAlreadyExist("LC/QUESTION_ALREADY_EXIST","相同id习题已存在"),
    DeleteQuestionSuccess("LC/DELETE_QUESTION_SUCCESS",getPropertyValue("questionController.deleteQuestion.operation.success")),
    DeleteQuestionChapterRelationSuccess("LC/DELETE_QUESTION_CHAPTER_RELATION_SUCCESS",getPropertyValue("questionController.deleteChatperRelation.operation.success")),
    DeleteQuestionFail("LC/DELETE_QUESTION_FAIL",getPropertyValue("questionController.deleteQuestion.operation.fail")),
	DeleteQuestionChapterRelationFail("LC/DELETE_QUESTION_CHAPTER_RELATION_FAIL",getPropertyValue("questionController.deleteChatperRelation.operation.fail")),
    DeletePrototypeActivityStepFail("LC/DELETE_PROTOTYPE_ACTIVITY_STEP_FAIL",getPropertyValue("prototypeActivityStepController.delete.operation.fail")),
    DeletePrototypeActivityStepSuccess("LC/DELETE_PROTOTYPE_ACTIVITY_STEP_SUCCESS",getPropertyValue("prototypeActivityStepController.delete.operation.success")),
    
	UpdateAssetFail("LC/UPDATE_ASSET_FAIL",getPropertyValue("assetController.updateAsset.operation.fail")),
	UpdateCourseWareFail("LC/UPDATE_COURSEWARE_FAIL",getPropertyValue("courseWareController.updateCourseWare.operation.fail")),
	UpdateLessonFail("LC/UPDATE_LESSON_FAIL",getPropertyValue("lessonController.updateLesson.operation.fail")),
	UpdateCourseWareObjectFail("LC/UPDATE_COURSEWAREOBJECT_FAIL",getPropertyValue("courseWareObjectController.updateCourseWareObject.operation.fail")),
	UpdateCourseWareObjectTemplFail("LC/UPDATE_COURSEWAREOBJECTTEMPLATE_FAIL",getPropertyValue("courseWareObjectTemplateController.updateCourseWareObjectTemplate.operation.fail")),
	CourseWareObjectTemplGetPageFail("LC/COURSEWAREOBJECTTEMPLATE_GET_PAGE_FAIL",getPropertyValue("courseWareObjectTemplateController.getFileUrlForGet.operation.fail")),
	CourseWareObjectTemplUpdatePageFail("LC/COURSEWAREOBJECTTEMPLATE_UPDATE_PAGE_FAIL",getPropertyValue("courseWareObjectTemplateController.getFileUrlForPost.operation.fail")),
	CourseWareObjectTemplGetSessionFail("LC/COURSEWAREOBJECTTEMPLATE_GET_SESSION_FAIL",getPropertyValue("courseWareObjectTemplateController.getFileUrlForGet.operation2.fail")),
	UpdateQuestionFail("LC/UPDATE_QUESTION_FAIL",getPropertyValue("questionController.updateQuestion.operation.fail")),
	UpdateKnowledgeFail("LC/UPDATE_KNOWLEDGE_FAIL",getPropertyValue("knowledgeController.modify.operation.fail")),
	UpdatePrototypeFail("LC/UPDATE_PROTOTYPE_FAIL",getPropertyValue("instructionalPrototypesController.modify.operation.fail")),
	UpdatePrototypeActivityStepFail("LC/UPDATE_PROTOTYPE_ACTIVITY_STEP_FAIL",getPropertyValue("prototypeActivityStepServiceImpl.update.operation.fail")),
	UpdateEducationRelationFail("LC/UPDATE_EDUCATION_RELATION_FAIL",getPropertyValue("educationRelationServiceImpl.modify.operation.fail")),
	UpdateLessonPlanFail("LC/UPDATE_LESSON_PLAN_FAIL",getPropertyValue("LessonPlansServiceImpl.modify.operation.fail")),
	
	CreateAssetFail("LC/CREATE_ASSET_FAIL",getPropertyValue("assetController.createAsset.operation.fail")),
	CreateCourseWareFail("LC/CREATE_COURSEWARE_FAIL",getPropertyValue("courseWareController.createCourseWare.operation.fail")),
	CreateQuestionFail("LC/CREATE_QUESTION_FAIL",getPropertyValue("questionController.createQuestion.operation.fail")),
	CreateQuestionChapterRelationFail("LC/CREATE_QUESTION_CHAPTER_RELATION_FAIL",getPropertyValue("questionController.createChapterRelation.operation.fail")),
	QuestionChapterRelationExist("LC/QUESTION_CHAPTER_RELATION_EXIST",getPropertyValue("questionServiceImpl.addChapterRelation.check.fail")),
	CreateCourseWareObjectTemplateFail("LC/CREATE_COURSEWAREOBJECTTEMPLATE_FAIL",getPropertyValue("courseWareObjectTemplateController.createCourseCellTemplate.operation.fail")),
	CreateLessonFail("LC/CREATE_INSTRUCTIONALOBJECTIVE_FAIL",getPropertyValue("lessonController.createLesson.operation.fail")),
	CreateKnowledgeFail("LC/CREATE_KNOWLEDGE_FAIL",getPropertyValue("knowledgeController.add.operation.fail")),
	CreatePrototypeFail("LC/CREATE_PROTOTYPE_FAIL",getPropertyValue("instructionalPrototypesController.add.operation.fail")),
	CreateKnowledgeRelationFail("LC/CREATE_KNOWLEDGERELATION_FAIL",getPropertyValue("knowledgeRelationController.addRelation.operation.fail")),
	CreateChapterQuestionRelationFail("LC/CREATE_CHAPTER_QUESTION_RELATION_FAIL",getPropertyValue("questionServiceImpl.addChapterRelation.operation.fail")),
	CreateProtoAssRelationFail("LC/CREATE_PROTOTYPE_ASSIGNMENT_RELATION_FAIL",getPropertyValue("protoAssRelationController.addRelation.operation.fail")),
	QuestionUpdatePageFail("LC/QUESTION_UPDATE_PAGE_FAIL",getPropertyValue("questionController.updateFileurl.operation.fail")),
	QuestionQueryListFail("LC/QUESTION_QUERY_LIST_FAIL",getPropertyValue("questionController.queryList.operation.fail")),	
	CourseWareObjectTemplateQueryListFail("LC/COURSEWAREOBJECTTEMPLATE_QUERY_LIST_FAIL",getPropertyValue("courseWareObjectTemplateController.queryList.operation.fail")),	
	CourseWareObjectTemplateUpdatePageFail("LC/COURSEWAREOBJECTTEMPLATE_UPDATE_PAGE_FAIL",getPropertyValue("courseWareObjectTemplateServiceImpl.getFileUrl.operation.fail")),
	CreatePrototypeActivityStepFail("LC/CREATE_PROTOTYPE_ACTIVITY_STEP_FAIL",getPropertyValue("prototypeActivityStepServiceImpl.add.operation.fail")),
	CreateCourseWareSessionFail("LC/CREATE_COURSEWARE_SESSION_FAIL",getPropertyValue("courseWareServiceImpl.create.courseware.session.fail")),
	CreateEducationRelationFail("LC/CREATE_EDUCATION_RELATION_FAIL",getPropertyValue("educationRelationServiceImpl.createRelation.operation.fail")),
	GetCourseWareObjectTemplateUploadUploadUrlFail("LC/GET_COURSEWAREOBJECTTEMPLATE_UPLOADURL_FAIL",getPropertyValue("courseWareObjectTemplateServiceImpl.getUploadUrl.operation.fail")),
	GetCourseWareObjectTemplateDownloadUploadUrlFail("LC/GET_COURSEWAREOBJECTTEMPLATE_DOWNLOADURL_FAIL",getPropertyValue("courseWareObjectTemplateServiceImpl.getDownloadUrl.operation.fail")),
	GetDetailCourseWareObjectTemplateFail("LC/GETDTAIL_COURSEWAREOBJECTTEMPLATE_FAIL",getPropertyValue("courseWareObjectTemplateController.getDetail.operation.fail")),
	GetProtoAssRelationFail("LC/GET_PROTOTYPE_ASSIGNMENT_RELATION_FAIL",getPropertyValue("protoAssRelationController.searchRelation.operation.fail")),
	GetCourseWareUploadUrlFail("LC/GET_COURSEWARE_UPLOADURL_FAIL",getPropertyValue("courseWareController.getUploadUrl.operation.fail")),
	GetCourseWareObjectUploadUrlFail("LC/GET_COURSEWAREOBJECT_UPLOADURL_FAIL",getPropertyValue("courseWareObjectController.getUploadUrl.operation.fail")),
	GetCourseWareObjectTemplateUploadUrlFail("LC/GET_COURSEWAREOBJECTTEMPLATE_UPLOADURL_FAIL",getPropertyValue("courseWareObjectTemplateController.getUploadUrl.operation.fail")),
	GetQuestionUploadUrlFail("LC/GET_QUESTION_UPLOAD_FAIL",getPropertyValue("questionController.getUploadUrl.operation.fail")),
	GetAssetUploadUrlFail("LC/GET_ASSET_UPLOADURL_FAIL",getPropertyValue("assetController.getUploadUrl.operation.fail")),
	GetCourseWareDownloadUrlFail("LC/GET_COURSEWARE_DOWNLOAD_FAIL",getPropertyValue("courseWareController.getDownloadUrl.operation.fail")),
	GetCourseWareObjectDownloadUrlFail("LC/GET_COURSEWAREOBJECT_DOWNLOAD_FAIL",getPropertyValue("courseWareObjectController.getDownloadUrl.operation.fail")),
	GetCourseWareObjectTemplateDownloadUrlFail("LC/GET_COURSEWAREOBJECTTEMPLATE_DOWNLOAD_FAIL",getPropertyValue("courseWareObjectTemplateController.getDownloadUrl.operation.fail")),
	GetQuestionDownloadUrlFail("LC/GET_QUESTION_DOWNLOAD_FAIL",getPropertyValue("questionController.getDownloadUrl.operation.fail")),
	CreateTeachingMaterialFail("LC/CREATE_TEACHING_MATERIAL_FAIL",getPropertyValue("teachingMaterialController.createTeachingMaterial.operation.fail")),
	SameTeachingMaterialFail("LC/SAME_TEACHING_MATERIAL_FAIL",getPropertyValue("teachingMaterialController.createTeachingMaterial.check.fail")),
	UpdateTeachingMaterialFail("LC/UPDATE_TEACHING_MATERIAL_FAIL",getPropertyValue("teachingMaterialController.update.operation.fail")),
	GetDetailTeachingMaterialFail("LC/GETDETAIL_TEACHING_MATERIAL_FAIL",getPropertyValue("teachingMaterialController.getDetail.operation.fail")),
	TeachingMaterialQueryListFail("LC/TEACHING_MATERIAL_QUERY_LIST_FAIL",getPropertyValue("teachingMaterialController.queryList.operation.fail")),
	TeachingMaterialQueryListBatchError("LC/TEACHING_MATERIAL_QUERY_LIST_BATCH_ERROR",getPropertyValue("teachingMaterialController.getTeachingMaterialList.operation.fail")),
	ChaptersQueryListBatchError("LC/CHAPTERS_QUERY_LIST_BATCH_ERROR",getPropertyValue("teachingMaterialController.getChapterList.operation.fail")),
	BatchCountResourceByMaterialsFail("LC/BATCH_COUNT_RESOURCE_BY_MATERIALS_FAIL",getPropertyValue("teachingMaterialController.countResourceByMaterials.operation.fail")),
	TeachingMaterialNdCodeError("LC/TEACHING_MATERIAL_NDCODE_CHECK_ERROR",getPropertyValue("teachingMaterialController.ndCode.check.fail")),
	
	TeachingMaterialQueryListByCategoryFail("LC/TEACHING_MATERIAL_QUERY_LIST_FAIL",getPropertyValue("teachingMaterialController.queryListByCategory.operation.fail")),
	DeleteTeachingMaterialSuccess("LC/DELETE_TEACHING_MATERIAL_SUCCESS",getPropertyValue("teachingMaterialController.deleteTeachingMaterial.operation.success")),
	DeleteTeachingMaterialFail("LC/DELETE_TEACHING_MATERIAL_FAIL",getPropertyValue("teachingMaterialController.deleteTeachingMaterial.operation.fail")),
	
	CreateChapterFail("LC/CREATE_CHAPTER_FAIL",getPropertyValue("teachingMaterialController.createChapter.operation.fail")),
	GetChapterDetailFail("LC/GET_CHAPTER_DETAIL_FAIL",getPropertyValue("teachingMaterialController.getChapterDetail.operation.fail")),
	UpdateChapterFail("LC/UPDATE_CHAPTER_FAIL",getPropertyValue("teachingMaterialController.updateChapter.operation.fail")),
	DeleteChapterFail("LC/DELETE_CHAPTER_FAIL",getPropertyValue("teachingMaterialController.deleteChapter.operation.fail")),
	DeleteChapterSuccess("LC/DELETE_CHAPTER_SUCCESS",getPropertyValue("teachingMaterialController.deleteChapter.operation.success")),
	QueryChapterListFail("LC/QUERY_CHAPTER_LIST_FAIL",getPropertyValue("teachingMaterialController.queryChapterList.operation.fail")),
	ChapterNotFound("LC/CHAPTER_NOT_FOUND",getPropertyValue("teachingMaterialController.updateChapter.check.fail")),
	ParentNotFound("LC/PARENT_NOT_FOUND",getPropertyValue("teachingMaterialController.createChapter.checkParent.fail")),
	TeachingMaterialNotFound("LC/TEACHING_MATERIAL_NOT_FOUND",getPropertyValue("teachingMaterialController.createChapter.check.fail")),
	HaveChildrenDeleteChapterFail("LC/HAVE_CHILDREN_DELETE_CHAPTER_FAIL",getPropertyValue("teachingMaterialController.deleteChapter.check.fail")),
	CheckResourceTagFail("LC/CHECK_PARAM_VALID_FAIL",getPropertyValue("resourceTagServiceImpl.addResourceTags.check.fail")),
	
	//Addon
	CreateAddonFail("LC/CREATE_ADDON_FAIL",getPropertyValue("addonsController.createAddon.operation.fail")),
	DeleteAddonSuccess("LC/DELETE_ADDON_SUCCESS",getPropertyValue("addonsController.deleteAddon.operation.success")),
    DeleteAddonFail("LC/DELETE_ADDON_FAIL",getPropertyValue("addonsController.deleteAddon.operation.fail")),
    AddonNotFound("LC/ADDON_NOT_FOUND",getPropertyValue("addonsController.updateAddon.check.fail")),
    UpdateAddonFail("LC/UPDATE_ADDON_FAIL",getPropertyValue("addonsController.updateAddon.operation.fail")),
    AddonQueryListFail("LC/ADDON_QUERY_LIST_FAIL",getPropertyValue("addonsController.queryList.operation.fail")),
	AddonGetListFail("LC/ADDON_GET_LIST_FAIL",getPropertyValue("addonsController.getAllAddon.operation.fail")),
	GetDetailAddonFail("LC/GETDTAIL_ADDON_FAIL",getPropertyValue("addonsController.getDetail.operation.fail")),
	AddonUpdatePageFail("LC/ADDON_UPDATE_PAGE_FAIL",getPropertyValue("addonsController.updatePage.operation.fail")),
	GetAddonUploadUrlFail("LC/GET_ADDON_UPLOADURL_FAIL",getPropertyValue("addonsController.getUploadUrl.operation.fail")),
	GetAddonDownloadUrlFail("LC/GET_ADDON_DOWNLOAD_FAIL",getPropertyValue("addonsController.getDownloadUrl.operation.fail")),
	MoveChapterFail("LC/MOVE_CHAPTER_FAIL",""),
	
	//Lesson
    LessonQueryListFail("LC/LESSON_QUERY_LIST_FAIL",getPropertyValue("lessonController.query.operation.fail")),
    
    //Knowledge
    KnowledgeCheckParamFail("LC/KNOWLEDGE_CHECK_PARAM_FAIL",getPropertyValue("knowledgeServiceImplV06.createKnowledge.checkParamValid.fail")),
    MoveKnowledgeFail("LC/MOVE_KNOWLEDGE_FAIL",""),
    KnowledgeParentNotFound("LC/KNOWLEDEG_PARENT_NOT_FOUND",getPropertyValue("knowledgeServiceImplV06.createKnowledge.checkParent.fail")),
    CheckResKnowledgeFail("LC/CHECK_RES_KNOWLEDGE_VALID_FAIL",getPropertyValue("knowledgeControllerV06.checkResKnowledgeFail.fail")),
	checkResKnowledgeTitleUniqueFail("LC/CHECK_RES_KNOWLEDGE_TITLE_VALID_FAIL",getPropertyValue("knowledgeControllerV06.checkResKnowledgeTitleUniqueFail.fail")),
    CheckAddTagsOutlineParamFail("LC/CHECK_ADD_TAGS_OUTLINE_PARAM_FAIL",getPropertyValue("knowledgeControllerV06.checkAddTagsOutlineParam.fail")),
    CheckAddTagsKnowledgeParamFail("LC/CHECK_ADD_TAGS_KNOWLEDGE_PARAM_FAIL",getPropertyValue("knowledgeControllerV06.checkAddTagsKnowledgeParam.fail")),
    CheckAddTagsTagsParamFail("LC/CHECK_ADD_TAGS_TAGS_PARAM_FAIL",getPropertyValue("knowledgeControllerV06.checkAddTagsTagsParam.fail")),
    CheckGetKnowledgeRelationsKnowledgeParamFail("LC/CHECK_GET_KNOWLEDGE_RELATIONS_KNOWLEDGE_PARAM_FAIL",getPropertyValue("knowledgeControllerV06.checkGetKnowledgeRelationsKnowledgeParamFail.fail")),
    CheckvGetKnowledgeRelationsContexttypeParamFail("LC/CHECK_GET_KNOWLEDGE_RELATIONS_TAGS_CONTEXTTYPE_PARAM_FAIL",getPropertyValue("knowledgeControllerV06.checkvGetKnowledgeRelationsContexttypeParamFail.fail")),
    CheckvGetKnowledgeRelationsContextobjectidParamFail("LC/CHECK_GET_KNOWLEDGE_RELATIONS_CONTEXTOBJECTID_PARAM_FAIL",getPropertyValue("knowledgeControllerV06.checkvGetKnowledgeRelationsContextobjectidParamFail.fail")),
    CheckDeleteTagOutlineParamFail("LC/CHECK_DELETE_TAG_OUTLINE_PARAM_FAIL",getPropertyValue("knowledgeControllerV06.checkDeleteTagOutlineParamFail.fail")),
    CheckDeleteTagTagParamFail("LC/CHECK_DELETE_TAG_TAG_PARAM_FAIL",getPropertyValue("knowledgeControllerV06.checkDeleteTagTagParamFail.fail")),
	
	//InstructionalObjective
	CreateInstructionalObjectiveFail("LC/CREATE_INSTRUCTIONALOBJECTIVE_FAIL",getPropertyValue("instructionalObjectiveController.create.operation.fail")),
	InstructionalObjectiveNotFound("LC/INSTRUCTIONALOBJECTIVE_NOT_FOUND",getPropertyValue("instructionalObjectiveController.update.check.fail")),
	DeleteInstructionalObjectiveSuccess("LC/DELETE_INSTRUCTIONALOBJECTIVE_SUCCESS",getPropertyValue("instructionalObjectiveController.delete.operation.success")),
	DeleteInstructionalObjectiveFail("LC/DELETE_INSTRUCTIONALOBJECTIVE_FAIL",getPropertyValue("instructionalObjectiveController.delete.operation.fail")),
	UpdateInstructionalObjectiveFail("LC/UPDATE_INSTRUCTIONALOBJECTIVE_FAIL",getPropertyValue("instructionalObjectiveController.update.operation.fail")),
	InstructionalObjectiveQueryListFail("LC/INSTRUCTIONALOBJECTIVE_QUERY_LIST_FAIL",getPropertyValue("instructionalObjectiveController.query.operation.fail")),
	GetDetailInstructionalObjectiveFail("LC/GETDTAIL_INSTRUCTIONALOBJECTIVE_FAIL",getPropertyValue("instructionalObjectiveController.getDetail.operation.fail")),
	//InstructionalObjective Knowledge
	CreateObjectiveKnowledgeRelationFail("LC/CREATE_OBJECTIVEKNOWLEDGERELATION_FAIL",getPropertyValue("instructionalObjectiveController.createRelation.operation.fail")),
	KnowledgeByObjectiveQueryListFail("LC/KNOWLEDGEBYOBJECTIVE_QUERY_LIST_FAIL",getPropertyValue("instructionalObjectiveController.queryKnowledge.check.fail")),
	DeleteObjectiveKnowledgeRelationFail("LC/DELETE_OBJECTIVEKNOWLEDGERELATION_FAIL",getPropertyValue("instructionalObjectiveController.deleteKnowledgeRelation.operation.fail")),
	
	//Ebooks
	DeleteEBooksSuccess("LC/DELETE_EBOOKS_SUCCESS",getPropertyValue("eBooksController.delete.operation.success")),
	EBooksNotFound("LC/EBOOKS_NOT_FOUND",getPropertyValue("eBooksController.assert.exist.operation.fail")),
	
	//homeworks
	DeleteHomeworksSuccess("LC/DELETE_HOMEWORKS_SUCCESS",getPropertyValue("homeworksController.delete.operation.success")),
	HomeworksNotFound("LC/HOMEWORKS_NOT_FOUND",getPropertyValue("homeworksController.assert.exist.operation.fail")),
	CreateHomeworksFail("LC/CREATE_HOMEWORKS_FAIL",getPropertyValue("homeworksController.create.operation.fail")),
	UpdateHomeworksFail("LC/UPDATE_HOMEWORKS_FAIL",getPropertyValue("homeworksController.update.operation.fail")),
	
	// 参数异常
	InvalidArgumentsError("LC/INVALIDARGUMENTS_ERROR", getPropertyValue("categoryController.common.checkParam.fail")), 
	DuplicateTitleError("LC/DUPLICATE_TITLE_ERRORR", getPropertyValue("categoryController.requestAddCategory.checkDuplicateTitle.fail")),
	DuplicateNDcodeError("LC/DUPLICATE_NDCODE_ERRORR", getPropertyValue("categoryController.requestAddCategory.checkDuplicateNDcode.fail")), 
	DuplicateShortNameError("LC/DUPLICATE_SHORT_NAME_ERRORR", getPropertyValue("categoryController.requestAddCategory.checkDuplicateShortName.fail")),
	DuplicatePatternNameError("LC/DUPLICATE_PATTERN_NAME_ERRORR", getPropertyValue("categoryController.requestAddCategory.checkDuplicatePatternName.fail")),
	
	
	// 打包
	ResourceRequestArchivingFail("LC/REQUEST_ARCHIVING_FAIL","请求打包失败"),
	ResourceTypeNotSupportFail("LC/REQUEST_TYPE_NOT_SUPPORT_FAIL","请求打包资源类型不支持"),
	
	//LifecycleStep
    CreateLifecycleFail("LC/CREATE_LIFECYCLE_STEP_FAIL", "添加生命周期阶段失败"),
    CreateBatchLifecycleFail("LC/CREATE_BATCH_LIFECYCLE_STEP_FAIL", "批量添加生命周期阶段失败"),
    LifecycleNotFound("LC/LIFECYCLE_STEP_NOT_FOUND", "指定的生命周期阶段不存在"),
    GetLifecycleFail("LC/GET_LIFECYCLE_STEP_FAIL", "获取指定生命周期阶段详细失败"),
    DeleteLifecycleSuccess("LC/DELETE_LIFECYCLE_STEP_SUCCESS", "删除生命周期阶段成功"),
    DeleteLifecycleFail("LC/DELETE_LIFECYCLE_STEP_FAIL", "删除生命周期阶段失败"),
    UpdateLifecycleFail("LC/UPDATE_LIFECYCLE_STEP_FAIL", "修改生命周期阶段失败"),
    UpdateBatchLifecycleFail("LC/UPDATE_BATCH_LIFECYCLE_STEP_FAIL", "批量修改生命周期阶段成功"),
    QueryLifecycleFail("LC/QUERY_LIFECYCLE_STEPS_FAIL", "获取指定资源的生命周期阶段详细失败"),
    UpdateLifecyclePropertyFail("LC/QUERY_LIFECYCLE_PROPERTRY_FAIL", "更新lifecycle相关字段失败"),
    
    //ThirdPartyBsys
    CreateBsysFail("LC/IVC_ERROR", "注册第三方业务系统失败"),
    BsysNotFound("LC/ANNOTATION_NOT_FOUND", "指定的第三方业务系统不存在"),
    GetBsysFail("LC/GET_3DSYS_FAIL", "获取指定第三方业务系统详细失败"),
    DeleteBsysSuccess("LC/DELETE_3DSYS_SUCCESS", "删除第三方业务系统成功"),
    DeleteBsysFail("LC/DELETE_3DSYS_FAIL", "删除第三方业务系统失败"),
    UpdateBsysFail("LC/UPDATE_3DSYS_FAIL", "修改第三方业务系统失败"),
    QueryBsysFail("LC/STORE_SDK_FAIL", "查询数据存储异常"),
    
		
	//Category
	CreateCategoryFail("LC/CREATE_CATEGORY_FAIL", getPropertyValue("categoryController.requestAddCategory.operation.fail")),
	CheckNdCodeRegex("LC/CHECK_PARAM_NDCODE_FAIL", getPropertyValue("categoryController.requestAddCategoryData.checkNdCodeRegex.fail")),
	CategoryNotFound("LC/CATEGORY_NOT_FOUND", getPropertyValue("categoryController.requestModifyCategory.checkExist.fail")),
	DeleteCategorySuccess("LC/DELETE_CATEGORY_SUCCESS", getPropertyValue("categoryController.requestRemoveCategory.operation.success")),
	DeleteCategoryFail("LC/DELETE_CATEGORY_FAIL", getPropertyValue("categoryController.requestRemoveCategory.operation.fail")),
	UpdateCategoryFail("LC/UPDATE_CATEGORY_FAIL", getPropertyValue("categoryController.requestModifyCategory.operation.fail")),
	CategoryQueryListFail("LC/CATEGORY_QUERY_LIST_FAIL", getPropertyValue("categoryController.requestQueryCategory.operation.fail")),
	GetCategoryListFail("LC/GET_CATEGORY_LIST_FAIL", getPropertyValue("knowledgeServiceImplV06.moveKnowledge.operation.fail")),
	
	
	
	//CategoryData
	CreateCategoryDataFail("LC/CREATE_CATEGORYDATA_FAIL", getPropertyValue("categoryController.requestAddCategoryData.operation.fail")),
	CategoryDataNotFound("LC/CATEGORYDATA_NOT_FOUND", getPropertyValue("categoryController.requestModifyCategoryData.checkExist.fail")),
	DeleteCategoryDataSuccess("LC/DELETE_CATEGORYDATA_SUCCESS", getPropertyValue("categoryController.requestRemoveCategoryData.operation.success")),
	DeleteCategoryDataFail("LC/DELETE_CATEGORYDATA_FAIL", getPropertyValue("categoryController.requestRemoveCategoryData.operation.fail")),
	UpdateCategoryDataFail("LC/UPDATE_CATEGORYDATA_FAIL", getPropertyValue("categoryController.requestModifyCategoryData.operation.fail")),
	CategoryDataQueryListFail("LC/CATEGORYDATA_QUERY_LIST_FAIL", getPropertyValue("categoryController.requestQueryCategoryData.operation.fail")),
	CategoryDataExtendLimit("LC/CATEGORYDATA_EXTEND_LIMIT", getPropertyValue("categoryController.requestAddCategoryData.extendLimit.fail")),
	
	//CategoryPattern
	CreateCategoryPatternFail("LC/CREATE_CATEGORYPATTERN_FAIL", getPropertyValue("categoryController.requestAddCategoryPattern.operation.fail")),
	CategoryPatternNotFound("LC/CATEGORYPATTERN_NOT_FOUND", getPropertyValue("categoryController.requestModifyCategoryPattern.checkExist.fail")),
	DeleteCategoryPatternSuccess("LC/DELETE_CATEGORYPATTERN_SUCCESS", getPropertyValue("categoryController.requestRemoveCategoryPattern.operation.success")),
	DeleteCategoryPatternFail("LC/DELETE_CATEGORYPATTERN_FAIL", getPropertyValue("categoryController.requestRemoveCategoryPattern.operation.fail")),
	UpdateCategoryPatternFail("LC/UPDATE_CATEGORYPATTERN_FAIL", getPropertyValue("categoryController.requestModifyCategoryPattern.operation.fail")),
	CategoryPatternQueryListFail("LC/CATEGORYPATTERN_QUERY_LIST_FAIL", getPropertyValue("categoryController.requestQueryCategoryPatterns.operation.fail")),
	GbCodeNotExist("LC/GB_CODE_NOT_EXIST",getPropertyValue("categoryController.gbCodeNotExist")),
	
	//CategoryRelation
	CreateCategoryRelationFail("LC/CREATE_CATEGORYRELATION_FAIL", getPropertyValue("categoryController.requestAddRelationData.operation.fail")),
	CategoryRelationNotFound("LC/CATEGORYRELATION_NOT_FOUND", getPropertyValue("categoryController.requestRemoveRelationData.checkExist.fail")),
	DeleteCategoryRelationSuccess("LC/DELETE_CATEGORYRELATION_SUCCESS", getPropertyValue("categoryController.requestRemoveRelationData.operation.success")),
	DeleteCategoryRelationFail("LC/DELETE_CATEGORYRELATION_FAIL", getPropertyValue("categoryController.requestRemoveRelationData.operation.fail")),
	UpdateCategoryRelationFail("LC/UPDATE_CATEGORYRELATION_FAIL", getPropertyValue("categoryController.requestModifyRelationData.operation.fail")),
	CategoryRelationQueryListFail("LC/CATEGORYRELATION_QUERY_LIST_FAIL", getPropertyValue("categoryController.requestQueryRelationData.operation.fail")),
	CategoryRelationAlreadyExist("LC/CATEGORY_RELATION_ALREADY_EXIST",getPropertyValue("categoryController.requestAddRelationData.checkNotExistRelation.fail")),
	CategoryRelationBatchAddDuplicate("LC/CATEGORY_RELATION_BATCH_ADD_DUPLICATE",getPropertyValue("categoryController.requestBatchAddRelation.checkDataDuplicate.fail")),
	
	//CategoryLogicCheck  
	CategoryCheckingError("LC/CATEGORY_CHECKING_ERROR", getPropertyValue("categoryController.checkingLogic.fail")),
	CategoryHasData("LC/CATEGORY_HAS_DATA", getPropertyValue("categoryController.requestRemoveCategory.checkExistData.fail")),
	CategoryDataHasChildNode("LC/CATEGORYDATA_HAS_CHILD_NODE", getPropertyValue("categoryController.requestRemoveCategoryData.checkExistChild.fail")),
	CategoryRelationHasCategoryData("LC/CATEGORYRELATION_HAS_CATEGORYDATA", getPropertyValue("categoryController.requestRemoveCategoryData.checkExistInRelation.fail")),
	CategoryPatternHasCategoryRelation("LC/CATEGORYPATTERN_HAS_CATEGORYRELATION", getPropertyValue("categoryController.requestRemoveCategoryPattern.checkExistRelation.fail")),
	
	//CSupload
	CSResourceTypeNotSupport("LC/CS_RESOURCETYPE_NOT_SUPPORT",getPropertyValue("resourceController.requestUploading.checkResourceTypeSupport.fail")),
	CSResourceNotFound("LC/CS_Resource_Not_Found",getPropertyValue("resourceController.requestUploading.checkResourceExist.fail")),
	CSCheckingError("LC/CS_Checking_Error",getPropertyValue("resourceController.requestUploading.checkingResource.fail")),
	CSDownloadKeyNotFound("LC/CS_DOWNLOAD_KEY_NOT_FOUND",getPropertyValue("resourceController.requestDownloading.checkingKey.fail")),
	CSInstanceKeyNotFound("LC/CS_INSTANCE_KEY_NOT_FOUND",getPropertyValue("resourceController.requestUploading.checkingInstanceKey.fail")),
	CSFilePathError("LC/CS_FILE_PATH_ERROR",getPropertyValue("NDResourceServiceImpl.getUploadUrl.checkPath.fail")),
	CSUploadCoverageError("LC/CS_UPLOAD_COVERAGE_ERROR",getPropertyValue("NDResourceServiceImpl.getUploadUrl.checkCoverage.fail")),
	CSUploadCoverageNotExist("LC/CS_UPLOAD_COVERAGE_NOT_EXIST",getPropertyValue("NDResourceServiceImpl.getUploadUrl.checkCoverageExist.fail")),
	
	
	//CommonServiceHelper
	CommonAPINotSupportResourceType("LC/COMMON_API_NOT_SUPPORT_RESOURCE_TYPE",getPropertyValue("commonServiceHelper.getRepositoryOrModel.fail")),
	DeleteResourceSuccess("LC/DELETE_RESOURCE_SUCCESS",getPropertyValue("NDResourceController.delete.operation.success")),
	ResourceNotFound("LC/RESOURCE_NOT_FOUND",getPropertyValue("NDResourceServiceImpl.resource.exist.check.fail")),
	CheckDeleteInstructionalObjectiveParamFail("LC/CHECK_PARAM_VALID_FAIL",getPropertyValue("NDResourceController.deleteInstructionalObjective.checkParam.fail")),


	//ResAnnotation
	CourseWareObjectNotFound("LC/COURSEWAREOBJECT_NOT_FOUND",getPropertyValue("courseWareObjectController.deleteResAnnotation.check.fail")),
	ResAnnotationNotFound("LC/RESANNOTATION_NOT_FOUND",getPropertyValue("courseWareObjectController.updateResAnnotation.check.fail")),
	DeleteResAnnotationSuccess("LC/DELETE_RESANNOTATION_SUCCESS",getPropertyValue("courseWareObjectController.deleteResAnnotation.operation.success")),
	DeleteResAnnotationFail("LC/DELETE_RESANNOTATION_FAIL",getPropertyValue("courseWareObjectController.deleteResAnnotation.operation.fail")),
	CreateResAnnotationFail("LC/CREATE_RESANNOTATION_FAIL",getPropertyValue("courseWareObjectController.createResAnnotation.operation.fail")),
	GetResAnnotationDetailFail("LC/GET_RESANNOTATION_DETAIL_FAIL",getPropertyValue("courseWareObjectController.getResAnnotationDetail.operation.fail")),
	UpdateResAnnotationFail("LC/UPDATE_RESANNOTATION_FAIL",getPropertyValue("courseWareObjectController.updateResAnnotation.operation.fail")),
	QueryResAnnotationListFail("LC/QUERY_RESANNOTATION_LIST_FAIL",getPropertyValue("courseWareObjectController.queryResAnnotationList.operation.fail")),
	QueryResAnnotationAmountFail("LC/QUERY_RESANNOTATION_AMOUNT_FAIL",getPropertyValue("courseWareObjectController.queryResAnnotationAmount.operation.fail")),
	
	//ResourceAnnotation
	ResourceAnnotationNotFound("LC/ANNOTATION_NOT_FOUND",getPropertyValue("resourceAnnotationsServiceImplV06.updateResourceAnnotation.check.fail")),
    DeleteResourceAnnotationSuccess("LC/DELETE_ANNOTATION_SUCCESS",getPropertyValue("resourceAnnotationsServiceImplV06.deleteResourceAnnotation.operation.success")),
    DeleteResourceAnnotationFail("LC/DELETE_ANNOTATION_FAIL",getPropertyValue("resourceAnnotationsServiceImplV06.deleteResourceAnnotation.operation.fail")),
    CreateResourceAnnotationFail("LC/CREATE_ANNOTATION_FAIL",getPropertyValue("resourceAnnotationsServiceImplV06.createResourceAnnotation.operation.fail")),
    UpdateResourceAnnotationFail("LC/UPDATE_ANNOTATION_FAIL",getPropertyValue("resourceAnnotationsServiceImplV06.updateResourceAnnotation.operation.fail")),
    
    //ResourceStatistical
    CheckResourceStatisticalTitlesFail("LC/STATISTICAL_KEYTITLES_FAIL",getPropertyValue("resourceStatisticalController.add.check.keyTitles.fail")) ,
    CheckResourceStatisticalTitleFail("LC/STATISTICAL_KEYTITLE_FAIL",getPropertyValue("resourceStatisticalController.add.check.keyTitle.fail")) ,
    CheckResourceStatisticalDataFromFail("LC/STATISTICAL_DATAFROM_FAIL",getPropertyValue("resourceStatisticalController.add.check.dataFrom.fail")) ,
    CheckResourceStatisticalKeyValueFail("LC/STATISTICAL_KEYVALUE_FAIL",getPropertyValue("resourceStatisticalController.add.check.keyValue.fail")) ,
    ResourceStatisticalCheckReourceFail("LC/STATISTICAL_CHECK_RESOURCE_FAIL",getPropertyValue("resourceStatisticalController.add.check.resource.fail")) ,
	
	//PrototypeActivity
	PrototypeActivityNotFound("LC/PROTOTYPEACTIVITY_NOT_FOUND",getPropertyValue("prototypeActivityController.updatePrototypeActivity.check.fail")),
	BatchAddPrototypeActivityFail("LC/BATCHADD_PROTOTYPEACTIVITY_FAIL",getPropertyValue("prototypeActivityController.batchAddPrototypeActivity.operation.fail")),
	UpdatePrototypeActivityFail("LC/UPDATE_PROTOTYPEACTIVITY_FAIL",getPropertyValue("prototypeActivityController.updatePrototypeActivity.operation.fail")),
	QueryPrototypeActivityFail("LC/QUERY_PROTOTYPEACTIVITY_FAIL",getPropertyValue("prototypeActivityController.queryPrototypeActivity.operation.fail")),
	DeletePrototypeActivityFail("LC/DELETE_PROTOTYPEACTIVITY_FAIL",getPropertyValue("prototypeActivityController.deletePrototypeActivity.operation.fail")),
	DeletePrototypeActivitySuccess("LC/DELETE_PROTOTYPEACTIVITY_SUCCESS",getPropertyValue("prototypeActivityController.deletePrototypeActivity.operation.success")),
	
	
	KnowledgeNotFound("LC/KNOWLEDGE_NOT_FOUND",getPropertyValue("knowledgeController.createRelation.check.fail")),
	KnowledgeHaveChildrens("LC/KNOWLEDGE_HAVE_CHILDRENS",getPropertyValue("knowledgesServicesImplV06.delete.check.isHaveChildren")),
	InstructionalPrototypeNotFound("LC/INSTRUCTIONALPROTOTYPE_NOT_FOUND",getPropertyValue("prototypeActivityController.batchAddPrototypeActivity.check.fail")),
	
	
	OutlineCanotBeNull("LC/OUTLINE_CANOT_BE_NULL",getPropertyValue("knowledgeController.findByOutline.check.fail")),
	CategoryCanotBeNull("LC/CATEGORY_CANOT_BE_NULL",getPropertyValue("knowledgeController.findByCategory.check.fail")),
	GetKnowledgeListByOutlineFail("LC/GET_KNOWLEDGE_LIST_BYOUTLINE_FAIL",getPropertyValue("knowledgeController.findByOutline.operation.fail")),
	GetKnowledgeListFail("LC/GET_KNOWLEDGE_LIST_FAIL",getPropertyValue("knowledgeController.find.operation.fail")),
	GetPrototypeListFail("LC/GET_PROTOTYPE_LIST_FAIL",getPropertyValue("instructionalPrototypesController.find.operation.fail")),
	GetPrototypeListFailByCategory("LC/GET_PROTOTYPE_LIST_BY_CATEGORY_FAIL",getPropertyValue("instructionalPrototypesController.findByCategory.operation.fail")),
	GetKnowledgeDetailFail("LC/GET_KNOWLEDGE_DETAIL_FAIL",getPropertyValue("knowledgeController.get.operation.fail")),
	GetPrototypeDetailFail("LC/GET_PROTOTYPE_DETAIL_FAIL",getPropertyValue("instructionalPrototypesController.get.operation.fail")),
	AddChapterKnowledgeFail("LC/ADD_CHAPTERKNOWLEDGE_FAIL",getPropertyValue("knowledgeController.addBatchChapterKnowledges.operation.fail")),
	GetEducationRelationListFail("LC/GET_EDUCATION_RELATION_LIST_FAIL",getPropertyValue("educationRelationController.searchByResType.operation.fail")),
	QueryEducationRelationFail("LC/QUERY_EDUCATION_RELATION_FAIL",getPropertyValue("educationRelationServiceImpl.queryRelation.operation.fail")),
	RelationSupportTypeError("LC/RELATION_SUPPORT_TYPE_ERROR",getPropertyValue("educationRelationController.supportType.check.error")),
	GetStatisticsByEducationRelationFail("LC/GET_STATISTICS_EDUCATION_RELATION_FAIL",getPropertyValue("educationRelationController.searchByGroup.operation.fail")),
	
	EditionNotExist("LC/EDITION_NOT_EXIST",getPropertyValue("importServiceImpl.importMaterial.checkEdition.fail")),
	SubjectNotExist("LC/SUBJECT_NOT_EXIST",getPropertyValue("importServiceImpl.importMaterial.checkSubject.fail")),
	GradeNotExist("LC/GRADE_NOT_EXIST",getPropertyValue("importServiceImpl.importMaterial.checkGrade.fail")),
	PhaseNotExist("LC/PHASE_NOT_EXIST",getPropertyValue("importServiceImpl.importMaterial.checkPhase.fail")),
	
	CreateKnowledgeRelationValidContextTypeFail("LC/CREATE_KNOWLEDGERELATION_FAIL",getPropertyValue("knowledgeRelationController.checkParam.checkContextType.fail")),
	CreateKnowledgeRelationValidObjectIdFail("LC/CREATE_KNOWLEDGERELATION_FAIL",getPropertyValue("knowledgeRelationController.checkParam.checkContextObjectId.fail")),
	CreateKnowledgeRelationValidKlFail("LC/CREATE_KNOWLEDGERELATION_FAIL",getPropertyValue("knowledgeRelationController.checkParam.checkKnowledge.fail")),
	
	OrderNumError("LC/ORDER_NUM_ERROR",""),
	RelationDuplication("LC/RELATION_DUPLICATION",getPropertyValue("ndResourceServiceImpl.dealRelations.relationDuplication")),
	
	//Coverage
	CoverageTargetTypeNotExist("LC/COVERAGE_TARGET_TYPE_NOT_EXIST",getPropertyValue("coverageController.targetType.check.fail")),
	CoverageStrategyNotExist("LC/COVERAGE_STRATEGY_NOT_EXIST",getPropertyValue("coverageController.strategy.check.fail")),
	CoverageAleadyExist("LC/COVERAGE_ALEADY_EXIST",getPropertyValue("coverageController.coverage.check.fail")),
	CoverageNotExist("LC/COVERAGE_NOT_EXIST",getPropertyValue("coverageController.coverage.checkExist.fail")),
	AllCoverageNotExist("LC/ALL_COVERAGE_NOT_EXIST",getPropertyValue("coverageController.coverage.checkAllExist.fail")),
	CanNotUpdateCoverageAleadyExist("LC/CANNOT_UPDATE_COVERAGE_ALEADY_EXIST",getPropertyValue("coverageController.canNotUpdateCoverageAleadyExist.operation.fail")),
	CoverageAleadyHaveOwner("LC/COVERAGE_ALEADY_HAVE_OWNER",""),
	
	GetCoverageByConditionFail("LC/GET_COVERAGE_BY_CONDITION_FAIL",getPropertyValue("coverageServiceImpl.getCoverageByCondition.operation.fail")),
	GetCoverageListByConditionFail("LC/GET_COVERAGE_LIST_BY_CONDITION_FAIL",getPropertyValue("coverageServiceImpl.getCoverageListByCondition.operation.fail")),
	CreateCoverageFail("LC/CREATE_COVERAGE_FAIL",getPropertyValue("coverageServiceImpl.createCoverage.operation.fail")),
	GetCoverageFail("LC/GET_COVERAGE_FAIL",getPropertyValue("coverageServiceImpl.getCoverageDetail.operation.fail")),
	UpdateCoverageFail("LC/UPDATE_COVERAGE_FAIL",getPropertyValue("coverageServiceImpl.updateCoverage.operation.fail")),
	DeleteCoverageFail("LC/DELETE_COVERAGE_FAIL",getPropertyValue("coverageServiceImpl.deleteCoverage.operation.fail")),
	DeleteCoverageSuccess("LC/DELETE_COVERAGE_SUCCESS",getPropertyValue("coverageServiceImpl.deleteCoverage.operation.success")),
	BatchDeleteCoverageFail("LC/BATCH_DELETE_COVERAGE_FAIL",getPropertyValue("coverageServiceImpl.batchDeleteCoverage.operation.fail")),
	BatchDeleteCoverageSuccess("LC/BATCH_DELETE_COVERAGE_SUCCESS",getPropertyValue("coverageServiceImpl.batchDeleteCoverage.operation.success")),
	BatchGetCoverageFail("LC/BATCH_GET_COVERAGE_FAIL",getPropertyValue("coverageServiceImpl.batchGetCoverageDetail.operation.fail")),
	BatchCreateCoverageFail("LC/BATCH_CREATE_COVERAGE_FAIL",getPropertyValue("coverageServiceImpl.batchCreateCoverage.operation.fail")),
	UpdateResCoverageInDBFail("LC/Update_RESCOVERAGE_IN_DB_FAIL",getPropertyValue("coverageServiceImpl.updateResCoverageInDB.operation.fail")),
	UpdateResCoverageGetResourceFail("LC/Update_RESCOVERAGE_GET_RESOURCE_FAIL",getPropertyValue("coverageServiceImpl.updateResCoverageGetResource.operation.fail")),
	
	//ResRepository
	CreateRepositoryFail("LC/CREATE_REPOSITORY_FAIL",getPropertyValue("resRepositoryServiceImpl.createRepository.operation.fail")),
	UpdateRepositoryFail("LC/UPDATE_REPOSITORY_FAIL",getPropertyValue("resRepositoryServiceImpl.updateRepository.operation.fail")),
	GetRepositoryFail("LC/GET_REPOSITORY_FAIL",getPropertyValue("resRepositoryServiceImpl.getRepositoryDetail.operation.fail")),
	DeleteRepositoryFail("LC/DELETE_REPOSITORY_FAIL",getPropertyValue("resRepositoryServiceImpl.deleteRepository.operation.fail")),
    DeleteRepositorySuccess("LC/DELETE_REPOSITORY_SUCCESS",getPropertyValue("resRepositoryServiceImpl.deleteRepository.operation.success")),
    
    RepositoryNotFind("LC/REPOSITORY_NOT_FIND",getPropertyValue("resRepositoryController.resRepository.isNotExist")),
    RepositoryIsExist("LC/REPOSITORY_IS_EXIST",getPropertyValue("resRepositoryController.resRepository.isExist")),
    TargetTypeIsNotExist("LC/TARGET_TYPE_IS_NOT_EXIST",getPropertyValue("resRepositoryController.targetType.isNotExist")),
    StatusIsNotExist("LC/STATUS_IS_NOT_EXIST",getPropertyValue("resRepositoryController.status.isNotExist")),
    
	//common search
	CommonSearchParamError("LC/COMMON_SEARCH_PARAM_ERROR",""),
	CommonStatisticsParamError("LC/COMMON_STATISTICS_PARAM_ERROR",""),
	CommonSearchFail("LC/COMMON_SEARCH_Fail",""),
	
	//includes
	IncludesParamError("INCLUDES_PARAM_ERROR",""),
	// Retrieve Fields
	RetrieveFieldsParamError("RETRIEVE_FIELDS_PARAM_ERROR",""),
	TimeUnitParamError("TIMEUNIT_PARAM_ERROR",""),
	//direction
	DirectionParamError("DIRECTION_PARAM_ERROR",""),
	
	//resource
	ImportNotSupport("LC/IMPORT_NOT_TSUPPORT",getPropertyValue("resourceController.requestSubfile.operation.fail")),
	ImportMaterialSuccess("LC/UPLOAD_RESOURCE_SUCCESS",getPropertyValue("resourceController.requestSubfile.operation.success")),
	ImportMaterialFail("LC/IMPORT_MATERIAL_FAIL",getPropertyValue("resourceController.requestSubfile.importMaterialNdCode.fail")), 
	ErrorFileType("LC/ERROR_FILE_TYPE",getPropertyValue("resourceController.requestSubfile.checkfileType.error")),
	GetNdCodeFail("LC/GET_ND_CODE_FAIL",getPropertyValue("resourceController.queryCategoryDataoperation.fail")),
	ResourceNotFond("LC/RESOURCE_NOT_FOUND",""),
	SourceResourceNotFond("LC/SOURCE_RESOURCE_NOT_FOUND",getPropertyValue("educationRelationServiceImpl.sourceResourceExist.fail")),
	TargetResourceNotFond("LC/TARGET_RESOURCE_NOT_FOUND",getPropertyValue("educationRelationServiceImpl.targetResourceExist.fail")),
	ResourceRelationExist("LC/RESOURCE_RELATION_EXIST",getPropertyValue("educationRelationController.checkExistRelation.success")),
	ResourceRelationNotExist("LC/RESOURCE_RELATION_NOT_EXIST",getPropertyValue("educationRelationController.checkExistRelation.fail")),
	GetResourceTitleFail("LC/GET_RESOURCE_TITLE_FAIL",getPropertyValue("educationRelationServiceImpl.getResourceTitle.fail")),
	GetRelationDetailFail("LC/GET_RELATION_DETATIL_FAIL",getPropertyValue("educationRelationServiceImpl.getRelation.operation.fail")),
	GetRelationByTargetFail("LC/GET_RELATION_BY_TARGET_FAIL",getPropertyValue("educationRelationServiceImpl.deleteRelationByTarget.getRelationByTaget.fail")),
	GetRelationByTargetTypeFail("LC/GET_RELATION_BY_TARGET_TYPE_FAIL",getPropertyValue("educationRelationServiceImpl.deleteRelationByTargetType.getRelationByTaget.fail")),
	GetRelationsFail("LC/GET_RELATIONS_FAIL",getPropertyValue("educationRelationServiceImpl.getRelationsByConditions.fail")),
	TargetRelationNotExist("LC/TARGET_RELATION_NOT_EXIST",getPropertyValue("educationRelationServiceImpl.batchAdjustRelationOrder.checkTargetRelationNotExist.fail")),
	DestinationRelationNotExist("LC/DESTINATION_RELATION_NOT_EXIST",getPropertyValue("educationRelationServiceImpl.batchAdjustRelationOrder.checkDestinationRelationNotExist.fail")),
	AdjoinRelationNotExist("LC/ADJOIN_RELATION_NOT_EXIST",getPropertyValue("educationRelationServiceImpl.batchAdjustRelationOrder.checkAdjoinRelationNotExist.fail")),
	AdjoinValueError("LC/ADJOIN_VALUE_ERROR",getPropertyValue("educationRelationServiceImpl.batchAdjustRelationOrder.checkAdjoinValueError.fail")),
	AtValueError("LC/AT_VALUE_ERROR",getPropertyValue("educationRelationServiceImpl.batchAdjustRelationOrder.checkAtValueError.fail")),
	AdjustOrderFail("LC/ADJUST_ORDER_FAIL",getPropertyValue("educationRelationServiceImpl.batchAdjustRelationOrder.AdjustOrderFail.fail")),
	DeleteBatchRelationFail("LC/DELETE_BATCH_RELATION_FAIL",getPropertyValue("educationRelationServiceImpl.deleteBatchRelation.fail")),
	UpdateBatchRelationFail("LC/UPDATE_BATCH_RELATION_FAIL",getPropertyValue("educationRelationServiceImpl.updateBatchRelation.fail")),
	DeleteBatchRelationSuccess("LC/DELETE_BATCH_RELATION_SUCCESS",getPropertyValue("educationRelationServiceImpl.deleteBatchRelation.success")),
	CheckTargetTypeIsNull("LC/CHECKTARGETTYPEISNULL",getPropertyValue("educationRelationController.searchByResType.check.targetType.fail")),
	CheckTargetTypeError("LC/CHECKTARGETTYPEERROR",getPropertyValue("educationRelationController.searchByResType.check.targetType.error")),
	
    //V0.6
	CheckRightDateFail("LC/CHECK_PARAM_VALID_FAIL",getPropertyValue("commonHelper.inputParamValid.checkRightDate.fail")),
    CheckNdCodeFail("LC/CHECK_PARAM_VALID_FAIL",getPropertyValue("AssetServiceImplV06.dealCategories.checkParam.fail")),
    CheckRelationSourceFail("LC/CHECK_PARAM_VALID_FAIL",getPropertyValue("AssetServiceImplV06.dealRelations.checkParam.fail")),
    CheckTaxonpathFail("LC/CHECK_PARAM_VALID_FAIL",getPropertyValue("AssetServiceImplV06.create.checkTaxonpath.fail")),
    CheckCoverageFail("LC/CHECK_PARAM_VALID_FAIL",getPropertyValue("AssetServiceImplV06.create.checkCoverage.fail")),
    ChecTechInfoFail("LC/CHECK_PARAM_VALID_FAIL",getPropertyValue("AssetServiceImplV06.create.checkTechInfo.fail")),
    ChecTechInfoHrefOrSourceFail("LC/CHECK_PARAM_VALID_FAIL",getPropertyValue("CoursewareControllerV06.create.checkTechInfo.fail")),
    CheckLifecycleFail("LC/CHECK_PARAM_VALID_FAIL",getPropertyValue("duplicate.checkTechInfo.fail")),
    CheckParamValidFail("LC/CHECK_PARAM_VALID_FAIL",""),
    CheckDuplicateIdFail("LC/DUPLICATE_ID_VALID_FAIL",getPropertyValue("NDResourceServiceImpl.create.isDuplicateId.fail")),
    CheckDuplicateCodeFail("LC/DUPLICATE_CODE_VALID_FAIL",getPropertyValue("NDResourceServiceImpl.create.isDuplicateCode.fail")),
    
	TeachingMaterialDisable("LC/TEACHING_MATERIAL_DISABLE",getPropertyValue("teachingMaterialControllerV06.operateTeachingMaterial.check.fail")),

	ConvertCallbackSuccess("LC/CONVERT_CALLBACK_SUCCESS","TransCodeController.convertCallback.success"),
	
	StoreSdkFail("LC/STORE_SDK_FAIL",getPropertyValue("store.sdk.fail")),
	CSSdkFail("CS/CS_SDK_FAIL",getPropertyValue("cs.sdk.fail")),
	InvokingCSFail("CS/INVOKING_CS_FAIL",getPropertyValue("invoking.cs.fail")),
	InvokingUCFail("UC/INVOKING_UC_FAIL",getPropertyValue("invoking.uc.fail")),
	
	//提供商
	CreateProviderFail("LC/CREATE_PROVIDER_FAIL",getPropertyValue("resourceProviderServiceImpl.createResourceProvider.operation.fail")),
	UpdateProviderFail("LC/UPDATE_PROVIDER_FAIL",getPropertyValue("resourceProviderServiceImpl.updateResourceProvider.operation.fail")),
	DeleteProviderSuccess("LC/DELETE_PROVIDER_SUCCESS",getPropertyValue("resourceProviderServiceImpl.deleteResourceProvider.operation.success")),
	CheckDuplicateProviderTitleFail("LC/CHECK_DUPLICATE_PROVIDER_TITLE_FAIL",getPropertyValue("resourceProviderServiceImpl.createResourceProvider.check.titleDuplicate.fail")),
	ResourceProviderNotFound("LC/RESOURCE_PROVIDER_NOT_FOUND",getPropertyValue("resourceProviderServiceImpl.updateResourceProvider.check.providerNotExist.fail")),
	//版权方
	CreateCopyrightOwnerFail("LC/CREATE_PROVIDER_FAIL",getPropertyValue("copyrightOwnerServiceImpl.createCopyrightOwner.operation.fail")),
	UpdateCopyrightOwnerFail("LC/UPDATE_PROVIDER_FAIL",getPropertyValue("copyrightOwnerServiceImpl.updateCopyrightOwner.operation.fail")),
	DeleteCopyrightOwnerSuccess("LC/DELETE_PROVIDER_SUCCESS",getPropertyValue("copyrightOwnerServiceImpl.deleteCopyrightOwner.operation.success")),
	CheckDuplicateCopyrightOwnerTitleFail("LC/CHECK_DUPLICATE_PROVIDER_TITLE_FAIL",getPropertyValue("copyrightOwnerServiceImpl.createCopyrightOwner.check.titleDuplicate.fail")),
	CopyrightOwnerNotFound("LC/RESOURCE_PROVIDER_NOT_FOUND",getPropertyValue("copyrightOwnerServiceImpl.updateCopyrightOwner.check.copyrightOwnerNotExist.fail")),
	
	//库分享
	CoverageSharingParamFail("LC/COVERAGE_SHARING_PARAM_FAIL",getPropertyValue("coverageSharingController.createCoverageSharing.check.coverage.fail")),
	CoverageSharingExistFail("LC/COVERAGE_SHARING_EXIST_FAIL",getPropertyValue("coverageSharingController.createCoverageSharing.coveragesharing.exist.fail")),
	CreateCoverageSharingFail("LC/CREATE_COVERAGE_SHARING_FAIL",getPropertyValue("coverageSharingServiceImpl.createCoverageSharing.operation.fail")),
	CoverageSharingNotFound("LC/COVERAGE_SHARING_NOT_FOUND",getPropertyValue("coverageSharingServiceImpl.deleteCoverageSharing.check.coverageSharingNotExist.fail")),
	DeleteCoverageSharingSuccess("LC/DELETE_COVERAGE_SHARING_SUCCESS",getPropertyValue("coverageSharingController.deleteCoverageSharing.operation.success")),
	
	//资源分享
	GetUserInfoFail("LC/GET_USER_INFO_FAIL",getPropertyValue("resourceSharingController.getUserInfo.operation.fail")),
	CreateResourceSharingFail("LC/CREATE_RESOURCE_SHARING_FAIL",getPropertyValue("resourceSharingServiceImpl.createResourceSharing.operation.fail")),
	DeleteResourceSharingFail("LC/DELETE_RESOURCE_SHARING_FAIL",getPropertyValue("resourceSharingServiceImpl.deleteResourceSharingBySharingId.operation.fail")),
	DeleteResourceSharingSuccess("LC/DELETE_RESOURCE_SHARING_SUCCESS",getPropertyValue("resourceSharingServiceImpl.deleteResourceSharingBySharingId.operation.success")),
	ProtectPasswdIsNotEmpty("LC/PROTECT_PASSWD_IS_NOT_EMPTY",getPropertyValue("resourceSharingController.protectPasswd.checkNotEmpty.fail")),
	ProtectedSharingResourceNotFound("LC/PROTECTED_SHARING_RESOURCE_NOT_FOUND",getPropertyValue("resourceSharingController.protectedSharingResource.checkIsExist.fail")),
	
	//copy
	CopyFail("LC/COPY_FAIL","DuplicateControllerV06.copy.fail"),
	
	
	//icrs
	DateFormatFail("LC/DateFormatError",getPropertyValue("icrsController.getResourceTotal.dataFormat.fail")),
	ResourceTypeNotFound("ResourceTypeNotFound",getPropertyValue("icrsServiceImpl.getResourceByDay.ResourceTypeNotFound")),
    
	CheckIcrsParamValidFail("LC/CHECK_PARAM_VALID_FAIL",getPropertyValue("icrsController.check.input.param.fail")),
	
	//说明 add by @author lanyl
	//数据加密相关错误配置
	//====Start=====
	EncryptDataFail("LC/ENCRYPT_DATA_FAIL",getPropertyValue("encrypt.data.fail")),
	//====End=====

	//说明 add by @author lanyl
	//角色权限控制相关错误配置
	//====Start=====
	CheckParamEmpty("LC/CHECK_PARAM_VALID_FAIL",getPropertyValue("common.checkParam.empty")),
	CoverageAdminDenied("LC/COVERAGE_ADMIN_DENIED",getPropertyValue("coverage.admin.denied")),
	Forbidden("LC/FORBIDDEN",getPropertyValue("forbidden"));
	//====End=====

	private static final Logger log = LoggerFactory.getLogger(LifeCircleErrorMessageMapper.class);

	private String code;
	
	private String message;
	
	LifeCircleErrorMessageMapper(String code,String message){
		
		this.code=code;
		this.message=message;
	}

	public String getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}
	
	private static String getPropertyValue(String key){		
		try {
			return new String(LifeCircleApplicationInitializer.message_properties.getProperty(key).getBytes("ISO8859-1"),"UTF-8");
		} catch (UnsupportedEncodingException e) {

			log.warn("加载exception_message.properties出错！", e);

		} catch(Exception e){

			log.warn("加载exception_message.properties中(" + key + ")出错！", e);
		}
		return "";
	}
}
