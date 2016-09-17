package nd.esp.service.lifecycle.services.examinationpapers.v06;

import nd.esp.service.lifecycle.models.v06.ExaminationPaperModel;

/**
 * 试卷业务层接口
 * 
 * @author xuzy
 * @since 
 *
 */
public interface ExaminationPaperServiceV06 {
	/**
	 * 试卷创建
	 */
	public ExaminationPaperModel createExaminationPaper(ExaminationPaperModel model);
	
	/**
	 * 试卷修改
	 */
	public ExaminationPaperModel updateExaminationPaper(ExaminationPaperModel model);

	ExaminationPaperModel patchExaminationPaper(ExaminationPaperModel model);
}