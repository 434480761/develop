package nd.esp.service.lifecycle.educommon.vos;

import java.util.Map;


/**
 * @author johnny
 * @version 1.0
 * @created 08-7月-2015 10:18:49
 */
public class ResEducationalViewModel {

	/**
	 * 学习对象所支持的互动形式。主动式学习对象能激励学习者产生有意义的输入或其他表现。讲解式的学习对象只向学习者展示信息，但并不鼓励学习者进行任何有意义的输入。如果学
	 * 习对象混合了主动式和讲解式交互类型，那么它的交互类型是混 合式。注：点击超链接来浏览超文本文档不能被认为是主动式行为。取值范围： 主动式=0 讲解式=1
	 * 混合式=2  主动式文档（带有学习者的动作）： l 模拟（操作、控制、输入数据或参数）； l 问卷（选择或写下答案）； l 练习（寻求解答）；
	 * 问题陈述（写下解答）。 讲解式文档（带有学习者的动作）： l 超文本文档（阅读、漫游）； l 视频段（观看、倒回、启动、停止）； l 图形材料（观看）； l
	 * 音频材料（听、倒回、启动、停止）。 混合式文档：带有嵌入式模拟小程序的超媒体文档。
	 */
	private int interactivity;
	/**
	 * 学习对象的交互程度。这里的交互是指学习者对学习对象的行为或其他方面所产生的影响程度。如果学习对象 “5.1：教育.
	 * 交互类型”=“主动式”，那么它可能具有较高的交互程度（如：一个具有多种控制的模拟环境），也可能具有较低的交互程度（如：引发行为的一组指示说明）。如果学习对象“5
	 * .1：教育.
	 * 交互类型”=“讲解式”，那么它可能具有较低的交互程度（如：由标准编辑器产生的线性的描述性文本），也可能具有中等的交互程度（如：具有很多内部链接和视图的复杂超文本
	 * ）。  很低=-1 低=0 中=1 高=2 很高=3
	 */
	private int interactivityLevel;
	/**
	 * 终端用户类型，需要关联到角色 该学习对象的主要用户，最重要的优 先列出。 第I 条“学习者”希望从学习 对象中学到东西。“作者”创作或出
	 * 版学习对象。“管理者”管理学习对 象的传播，如：大学院校。面向管理 者的文档一般是课程。 第II 条如果希望通过终端用 户所掌握的技术或者他所要完成的
	 * 任务来描述学习对象的使用者，则可 以通过类别“9：分类”来实现。 教师 作者 学习者 管理者
	 */
	private String endUserType;
	/**
	 * 学习对象的简练程度。学习对象的语义密度可以通过它的大小、范围或持续时间（自身有时间限制的资源如音频或视频）来衡量。学习对象的语义密度与它的难度无关。一个明显的例
	 * 子是讲解式资源，如：“举例”一栏所示。  很低=-1 低=0 中=1 高=2 很高=3  主动式文档：一个模拟软件的用户界面。 l
	 * 低语义密度：屏幕上充满说明性文本，一张关于燃烧引擎的图片和一个标着“点击此处继续”的按钮。 l
	 * 高语义密度：屏幕上有简短的文本，一张相同的图片和三个分别标着“改变压缩比率”、“改变辛烷含量”和“改变燃点”的按钮。 讲解式文档： l 中等难度的文本文档
	 * a) 中等语义密度：“有袋类动物主要由一些相对原始的哺乳动 物组成。它们具有短小的胎盘用于生育幼仔。幼仔在母亲的育婴袋中寻求庇护，并完成它所有的发育过程。”
	 * b) 高语义密度：“有袋类动物是原始的哺乳动物，具有短小的胎盘生育幼仔，幼仔在育婴袋中得到庇护并完成其发育过程。”l 简单的视频文档 a)
	 * 低语义密度：一段全程摄录长达30 分钟的镜头，描述两个专家谈论亚洲象与非洲象的不同。 b) 高语义密度：一段经过剪接长达5
	 * 分钟的专业镜头，其内容为对上例中谈话的概括。 l 较难的数学概念 a) 中等语义密度：定理的文本描述。如：对于任意一个集合φ， 总能定义另一个集合ψ，它是φ
	 * 的超集。 b) 很高的语义密度。如：定理的符号表示："（"j $y:  y ?j）
	 */
	private Long semanticDensity;
	/**
	 * 使用学习对象的主要语境。 注：建议使用值空间中的某个值的同时，使用该另一个该数据元素的
	 * 实例来进一步细化。语境更具体的含义是在资源类型分类里面的教育类型的划分，比如高等教育，基础教育，职业教育，培训等。。。
	 */
	private String context;
	/**
	 * 典型使用者的年龄范围。 注1：学习者的年龄对于查找学习对象是很重要的，特别是对于处于学龄的学习者和他们的教师。
	 * 如果可能的话，应使用如下格式：最小年龄—最大年龄或最小年龄—。（注：这是三元素（最小年龄、最大年龄、描述）表示法和自由文本之间的一种折衷。）
	 * 注2：如果该数据元素想要覆盖其他的方案（如使用者的阅读水平，使用者的IQ 等）可以用“9.分 类”来表示。“7-9”“0-5”“15”“18-
	 * ”（“zh”，“适合7 岁以上儿童”）（“zh”，“只用于成人”）
	 * 另外，适用年龄范围是更具体，且粒度过细的一种定义方法，在系统中，用户的年龄并没有详细的记录，所以通过教学的适用对象来定位是比较合理的。
	 * 所以这里我们采用适用对象对象的关联方式，来关联用户的使用对象的年龄范围。
	 */
	private String ageRange;
	/**
	 * 对于典型的目标用户来说学习对象的难度。注：“典型的目标用户”可以通过如下两个数据元素来描述。 “5.6：教育.语境”与“5.7：教育.典型年龄范围”。
	 * 很容易=very easy 容易=easy 中等=medium 难=difficult 很难=very difficult 
	 */
	private String difficulty;
	/**
	 * 对于典型的目标用户来说，使用该学 习对象一般或大约所需要的时间。 注：“典型的目标用户”可以通过如 下两个数据元素来描述。 “5.6：教育.语境”与 “5.
	 * 7：教育.典型年龄范围”。
	 */
	private String learningTime;
	/**
	 * 对如何使用学习对象的描述，（“zh”，“在教师指导下同教科书一起使用”）
	 */
	private Map<String,String> description;
	/**
	 * 学习对象的典型用户所使用的人类语言。“en” “en-GB” “zh” “fr-CA” “it” 注：如果学习对象使用英文， 而使用者是中国学生，那 么“1.
	 * 3：通用.语种”的 值为“en”，而“5.11：教 育.语种”的值为“zh”。
	 */
	private String language;

	public ResEducationalViewModel(){

	}

	public int getInteractivity() {
		return interactivity;
	}

	public void setInteractivity(int interactivity) {
		this.interactivity = interactivity;
	}

	public int getInteractivityLevel() {
		return interactivityLevel;
	}

	public void setInteractivityLevel(int interactivityLevel) {
		this.interactivityLevel = interactivityLevel;
	}

	public String getEndUserType() {
		return endUserType;
	}

	public void setEndUserType(String endUserType) {
		this.endUserType = endUserType;
	}

	public Long getSemanticDensity() {
		return semanticDensity;
	}

	public void setSemanticDensity(Long semanticDensity) {
		this.semanticDensity = semanticDensity;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public String getAgeRange() {
		return ageRange;
	}

	public void setAgeRange(String ageRange) {
		this.ageRange = ageRange;
	}

	public String getDifficulty() {
		return difficulty;
	}

	public void setDifficulty(String difficulty) {
		this.difficulty = difficulty;
	}

	public String getLearningTime() {
		return learningTime;
	}

	public void setLearningTime(String learningTime) {
		this.learningTime = learningTime;
	}

	public Map<String, String> getDescription() {
		return description;
	}

	public void setDescription(Map<String, String> description) {
		this.description = description;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

}