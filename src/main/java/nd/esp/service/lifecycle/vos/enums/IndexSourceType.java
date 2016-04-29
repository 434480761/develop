package nd.esp.service.lifecycle.vos.enums;



/**
 * @title 资源生命周期错误信息定义
 * @Desc TODO
 * @see com.nd.gaea.rest.exceptions.extendExceptions.WafSimpleException
 * @author liuwx
 * @version 1.0
 * @create 2015年1月26日 上午11:19:35
 */
public enum IndexSourceType {
	/**
	 *素材库 包含下面四个子类 
	 图片 = 0
	视频 = 1
	音频 = 2
	其他 = 3
	 * */
	AssetType("asset",0),
	/**
	 *资源库 包含下面三个子类 
	 *课件颗粒模版 = 100
	 *课件颗粒 = 101
	 *课件 = 102 
	 * */
	SourceType("source",100),
	AssetImageType("image",0),
	AssetVideoType("video",1),
	AssetAudioType("audio",2),
	AssetOtherType("other",3),
	SourceCourseWareObjectType("coursewareObject",101),
	SourceCourseWareObjectTemplateType("coursewareObjectTemplate",100),
	SourceCourseWareType("courseware",102),
	SourceAddonType("addon",103),
	SourceQuestionType("question",104);
	
	private String type;
	
	private int value;
	
	IndexSourceType(String type,int value){
		
		this.type=type;
		this.value=value;
	}

	public String getType() {
		return type;
	}

	public int getValue() {
		return value;
	}

	
	
	

}
