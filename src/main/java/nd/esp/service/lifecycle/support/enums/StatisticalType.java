package nd.esp.service.lifecycle.support.enums;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import nd.esp.service.lifecycle.utils.CollectionUtils;

import com.google.common.collect.Maps;

public enum StatisticalType {
	top,
	scores,
	votes,
	views,
	statisticals;
	
	// 排序字段top,scores,votes,views,statisticals被映射为 key_value
	private final static String mapValue = "key_value";
	// 缺省值，与数据库通用查询保持一致
	private final static String statisticsType = "valuesum";

	private static Map<String, String> sta = new HashMap<String, String>();
    static {
        for (StatisticalType st : StatisticalType.values()) {
        	sta.put(st.toString(), mapValue);
        }
    }
    
    public static String mapping(String key){
    	return sta.get(key) == null ? key : sta.get(key);
    }
    
    public static Map<String, String> mapping(Map<String, String> map){
    	// 排序字段top,scores,votes,views,statisticals被映射为 key_value
    	HashMap<String, String> newMap = Maps.newLinkedHashMap();
    	if (CollectionUtils.isNotEmpty(map)) {
    		for (Entry<String, String> entry : map.entrySet()) {
				String key = mapping(entry.getKey());
				String value = entry.getValue();
				newMap.put(key, value);
    		}
		}
    	
		return newMap;
    }
    
    public static String getStatisticsType(Map<String, String> map, String type){
    	// 排序字段top,scores,votes,views,statisticals 一次有且仅有一个排序值，取第一个出现的排序字段
    	if (CollectionUtils.isNotEmpty(map)) {
	    	for (Entry<String, String> entry : map.entrySet()) {
	    		if (sta.containsKey(entry.getKey())) {
	    			return entry.getKey();
				}
	    	}
    	}
    	return type;
    }
    
    public static void main(String[] args) {
    	Map<String, String> orders = new LinkedHashMap<String, String>();
    	orders.put("3", "desc");
    	orders.put("4", "desc");
    	orders.put("1", "desc");
    	orders.put("2", "desc");
    	orders.put("5", "desc");
    	System.out.println(StatisticalType.mapping(orders));
    	
	}
}
