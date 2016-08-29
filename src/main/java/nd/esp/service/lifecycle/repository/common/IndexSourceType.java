package nd.esp.service.lifecycle.repository.common;


/**
 * 资源类型代码定义
 * 类描述：IndexSourceType </br>
 * 修改人： Rainy(yang.lin)</br>
 * 创建时间：2015年3月12日 上午10:27:20</br>
 * 修改备注： </br>
 * @version</br>
 */
public enum IndexSourceType {
	/**
	 * 素材库 包含下面四个子类 图片 = 0 视频 = 1 音频 = 2 其他 = 3
	 * */
	AssetType("assets",0,3), 
	AssetImageType("image",0,0), 
	AssetVideoType("video", 0 ,1), 
	AssetAudioType("audio",0, 2), 
	AssetOtherType("other",0, 3),
	/**课件颗粒*/
	SourceCourseWareObjectType("coursewareobjects",100, 101), 
	/**课件颗粒模版*/
	SourceCourseWareObjectTemplateType("coursewareobjecttemplates",100, 100), 
	/**课件*/
	SourceCourseWareType("coursewares",100, 102),
	/**插件*/
	AddonType("addons",100,103),
	/**习题*/
	QuestionType("questions",100,104),
	/**电子书*/
	EbookType("ebooks",100,105),
	/**试卷*/
	ExaminationPapersType("examinationpapers", 100, 106),
	/**教材*/
	TeachingMaterialType("teachingmaterials", 200, 201),
	/**章节*/
	ChapterType("chapters", 200, 202),
	/**课时*/
	LessonType("lessons", 200, 203),
	/**教学目标*/
	InstructionalObjectiveType("instructionalobjectives", 200, 204),
	/**知识点*/
	KnowledgeType("knowledges",200,205),
	/**学研原型*/
	PrototypeType("instructionalprototypes",200,206),
	/**分类维度*/
	CategoryType("categorys",200,207),
	/**分类维度数据*/
	CategoryDataType("CategoryDataType",200,208),
	/**分类模式*/
	CategoryPatternType("NdCategoryPattern",200,209),
	/**知识点关系*/
	KnowledgeRelationType("knowledgerelations",200,210),
	/**学研原型环节*/
	PrototypeActivityType("instructionalprototypeactivities",200,211),
	/**学研原型环节步骤*/
	PrototypeActivityStepType("instructionalprototypeactivitysteps",200,212),
	/**资源覆盖范围*/
	ResCoverageType("rescoverages",200,213),
	/**教案*/
	LessonPlansType("lessonplans",200,214),
	/**学案*/
	LearningPlansType("learningplans",200,216),
	/**作业*/
	HomeWorkType("homeworks",200,215),
	/**教辅*/
	GuidanceBooksType("guidancebooks",200,217) ,
	
	/**教学活动*/
	SourceTeachingActivitiesType("teachingactivities",200, 218),
	
	ToolsType("tools",200,218),
    /**子教学目标*/
    SubInstructionType("subInstruction",200,219) ,
	/**章节资源*/
	ChapterResourceType("ChapterResourceType",300,300),
	/**章节知识点*/
	ChapterKnowledgeType("",300,301),
	/**章节习题*/
	ChapterQuestionType("",300,302),
	/**分类关系*/
	CategoryRelationType("CategoryRelationType",300,303),
	/**资源关系*/
	ResourceRelationType("ResourceRelationType",500,500),
	
	/**资源评注*/
    ResourceAnnotationType("ResourceAnnotationType",600,600) ,
    
    /**知识库*/
    KnowledgebaseType("knowledgebase",700,700),
    ;
	
	
	private String name;
	private int type;
	private int subtype;
	
	private IndexSourceType(String name, int type, int subtype) {
		this.name = name;
		this.type = type;
		this.subtype = subtype;
	}

	public String getName() {
		return name;
	}

	public int getType() {
		return type;
	}

	public int getSubtype() {
		return subtype;
	}
}
