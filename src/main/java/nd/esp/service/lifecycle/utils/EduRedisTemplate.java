package nd.esp.service.lifecycle.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import nd.esp.service.lifecycle.utils.gson.ObjectUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Component;

/**
 * Redis 工具类
 * @author xuzy
 *
 * @param <T>
 */
@Component
public class EduRedisTemplate<T>{

	@Autowired
	private StringRedisTemplate rt;
	
	public void set(String key, T value) {
		rt.opsForValue().set(key, ObjectUtils.toJson(value));
    }

    public void set(String key, T value, long timeout, TimeUnit unit){
    	 rt.opsForValue().set(key, ObjectUtils.toJson(value), timeout, unit);
    }
    
    public T get(String key, Class vClass) {
    	String value = rt.opsForValue().get(key);
    	return (T) ObjectUtils.fromJson(value, vClass);
    }
    
    public void hSet(String key, String hashKey, T value) {
    	rt.opsForHash().put(key, hashKey, ObjectUtils.toJson(value));
    }

    public T hGet(String key, String hashKey, Class vClass) {
    	 String value = (String) rt.opsForHash().get(key, hashKey);
         return (T) ObjectUtils.fromJson(value, vClass);
    }
    
    /**
     * SortedSet方式批量保存数据
     * 数据以JSON的格式保存
     * 
     * @author:xuzy
     * @date:2016年1月18日
     * @param key
     * @param lists
     */
    public void zSet(String key,List<T> lists){
    	if(CollectionUtils.isNotEmpty(lists)){
    		Set<TypedTuple<String>> sets = new HashSet<ZSetOperations.TypedTuple<String>>();
    		for (int i = 0; i < lists.size(); i++) {
    			final T v = lists.get(i);
    			final double score = i;
				TypedTuple<String> tt = new TypedTuple<String>() {
					@Override
					public int compareTo(TypedTuple<String> o) {
						return 0;
					}

					@Override
					public String getValue() {
						return ObjectUtils.toJson(v);
					}

					@Override
					public Double getScore() {
						return score;
					}
				};
				
				sets.add(tt);
			}
    		
    		rt.opsForZSet().add(key, sets);
    	}
    }
    
    /**
     * SortedSet保存单个值
     * 
     * @author:xuzy
     * @date:2016年1月18日
     * @param key
     * @param value
     * @param score
     */
    public void zSetSingle(String key,T value,Double score){
    	rt.opsForZSet().add(key,  ObjectUtils.toJson(value), score);
    }
    
    /**
     * 返回SortedSet指定区间的成员
     * 
     * @author:xuzy
     * @date:2016年1月18日
     * @param key
     * @param start
     * @param end
     * @return
     */
    public List<T> zRangeByScore(String key,Long start,Long end,Class vClass){
    	List<T> returnList = new ArrayList<T>();
    	Set<String> sets = rt.opsForZSet().range(key, start, end);
    	if(CollectionUtils.isNotEmpty(sets)){
    		for (String s : sets) {
				T t = (T)ObjectUtils.fromJson(s, vClass);
				returnList.add(t);
			}
    	}
    	return returnList;
    }
    
    /**
     * 返回SortedSet 某个key的基数
     * 
     * @author:xuzy
     * @date:2016年1月18日
     * @param key
     * @return
     */
    public long zSetCount(String key){
    	return rt.opsForZSet().size(key);
    }
    
    /**
     * 设置某个KEY的过期时间
     * 
     * @author:xuzy
     * @date:2016年1月18日
     * @param key
     * @param timeout
     * @param unit
     */
    public void expire(String key,Long timeout,TimeUnit unit){
    	rt.expire(key, timeout, unit);
    }
    
    /**
     * 判断KEY是否存在
     * 
     * @author:xuzy
     * @date:2016年1月18日
     * @param key
     * @return
     */
    public boolean existKey(String key){
    	return rt.hasKey(key);
    }
}
