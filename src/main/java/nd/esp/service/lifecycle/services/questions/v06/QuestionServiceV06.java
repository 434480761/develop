package nd.esp.service.lifecycle.services.questions.v06;

import nd.esp.service.lifecycle.models.v06.QuestionModel;

/**
 * @author xuzy
 * @version 0.6
 * @created 2015-07-02
 */
public interface QuestionServiceV06{
	/**
	 * 习题创建
	 * @param rm
	 * @return
	 */
	public QuestionModel createQuestion(QuestionModel questionModel);
	
	/**
	 * 习题修改
	 * @param rm
	 * @return
	 */
	public QuestionModel updateQuestion(QuestionModel questionModel);

	QuestionModel patchQuestion(QuestionModel questionModel);
}