package nd.esp.service.lifecycle.support.busi.titan;

import java.util.Map;

import nd.esp.service.lifecycle.support.enums.ES_SearchField;

/**
 * coverage 定置：与资源lc_status有关
 * @author linsm
 *
 */
public class TitanQueryEdgeAndVertexForCoverage extends TitanQueryEdgeAndVertex {
	private String status;
	
	public void setStatus(String status){
		this.status = status;
	}
	
	
	@Override
	public String generateScript(Map<String, Object> scriptParamMap) {
		StringBuffer scriptBuffer = new StringBuffer(super.generateScript(scriptParamMap));
		if(status != null){
			String key = TitanUtils.generateKey(scriptParamMap, ES_SearchField.lc_status.toString());
			scriptParamMap.put(key, status);
			scriptBuffer.append(".select('x').has('");
			scriptBuffer.append(ES_SearchField.lc_status.toString()).append("',").append(key).append(")");
		}
		return scriptBuffer.toString();
	}

}
