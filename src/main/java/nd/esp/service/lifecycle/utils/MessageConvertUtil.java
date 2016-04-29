package nd.esp.service.lifecycle.utils;

import java.util.HashMap;
import java.util.Map;

import nd.esp.service.lifecycle.support.MessageMapper;

/**
 * 消息统一接口状态转换
 * @author xuzy
 *
 */
public class MessageConvertUtil {
	public static Map<String,String> getMessageString(MessageMapper messageMapper){
		Map<String,String> m = new HashMap<String, String>();
		String code = messageMapper.getCode();
		String message = messageMapper.getMessage();		
		m.put("process_state", message);
		m.put("process_code", code);
		return m;
	}

}
