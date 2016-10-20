package nd.esp.service.lifecycle.services.subinstruction.v06;

import nd.esp.service.lifecycle.models.v06.SubInstructionModel;


/**
 * @author yanguanyu(290536)
 * @version 0.6
 * @created 2016-07-18
 */
public interface SubInstructionService {
	/**
	 * 子教学目标创建
	 * @param sim
	 * @return
	 */
    SubInstructionModel createSubInstruction(SubInstructionModel sim);
	
	/**
	 * 子教学目标修改
	 * @param sim
	 * @return
	 */
    SubInstructionModel updateSubInstruction(SubInstructionModel sim);
    
    /**
     * 子教学目标的patch
     * @param sim
     * @return 
     */
	SubInstructionModel patchSubInstruction(SubInstructionModel sim,boolean isObvious);
}