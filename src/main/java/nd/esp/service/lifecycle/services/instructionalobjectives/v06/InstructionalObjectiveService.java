package nd.esp.service.lifecycle.services.instructionalobjectives.v06;

import nd.esp.service.lifecycle.models.v06.InstructionalObjectiveModel;


/**
 * @author xuzy
 * @version 0.6
 * @created 2015-07-02
 */
public interface InstructionalObjectiveService{
	/**
	 * 素材创建
	 * @param rm
	 * @return
	 */
	public InstructionalObjectiveModel createInstructionalObjective(InstructionalObjectiveModel am);
	
	/**
	 * 素材修改
	 * @param rm
	 * @return
	 */
	public InstructionalObjectiveModel updateInstructionalObjective(InstructionalObjectiveModel am);

	InstructionalObjectiveModel patchInstructionalObjective(InstructionalObjectiveModel instructionalObjectiveModel);
}