package nd.esp.service.lifecycle.repository.ds;

import nd.esp.service.lifecycle.repository.ds.support.GenericValue;
import nd.esp.service.lifecycle.repository.ds.support.Pair;
import nd.esp.service.lifecycle.repository.ds.support.StringValue;


/** 
 * @Description Value工具类。
 * @author Rainy(yang.lin)  
 * @date 2015年5月19日 下午1:43:12 
 * @version V1.0
 */ 
  	
public class ValueUtils {
	
	public static <T extends Comparable<T>> Value<Pair<T>> newValue(T begin, T end) {
		GenericValue<Pair<T>> value = new GenericValue<>();
		Pair<T> pair = new Pair<>();
		pair.setFirst(begin);
		pair.setSecond(end);
		value.setValue(pair);
		return value;
	}
	
	public static <T extends Object> Value<T> newValue(T value) {
		GenericValue<T> genericValue = new GenericValue<>();
		genericValue.setValue(value);
		return genericValue;
	}
	
	public static Value<String> newValue(boolean hasLeftPercent, String pattern, boolean hasRightPercent) {
		StringValue stringValue = new StringValue();
		stringValue.setHasLeftPercent(hasLeftPercent);
		stringValue.setSourceValue(pattern);
		stringValue.setHasRightPercent(hasRightPercent);
		return stringValue;
	}
}
