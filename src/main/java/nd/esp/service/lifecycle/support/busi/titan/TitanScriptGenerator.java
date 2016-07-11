package nd.esp.service.lifecycle.support.busi.titan;

import java.util.Map;

/**
 * 产生脚本接口，所有查询模块都现实该接口
 * @author linsm
 *
 */
public interface TitanScriptGenerator {
	
	/**
	 * 产生脚本块
	 * @param scriptParamMap 整个脚本的参数
	 * @return
	 */
	String generateScript(Map<String, Object> scriptParamMap);
	
	/**
	 * 判断脚本块是否为空
	 * @return
	 */
	Boolean isNotHavingAnyCondition();

}
