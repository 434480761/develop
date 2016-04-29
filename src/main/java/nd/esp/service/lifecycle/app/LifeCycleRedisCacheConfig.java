package nd.esp.service.lifecycle.app;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurationSelector;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
/**
 * redis缓存配置文件
 * @author xuzy
 *
 */
@Configuration
@EnableCaching
public class LifeCycleRedisCacheConfig extends CachingConfigurationSelector{
	@Bean
    public JedisConnectionFactory redisConnectionFactory() {  
        JedisConnectionFactory redisConnectionFactory = new JedisConnectionFactory();  
        redisConnectionFactory.setHostName(LifeCircleApplicationInitializer.properties.getProperty("redis_url"));  
        redisConnectionFactory.setPort(Integer.valueOf(LifeCircleApplicationInitializer.properties.getProperty("redis_port")));
        redisConnectionFactory.setDatabase(Integer.valueOf(LifeCircleApplicationInitializer.properties.getProperty("redis_index")));
        return redisConnectionFactory;  
    }  
  
    @Bean
    public StringRedisTemplate redisTemplate(RedisConnectionFactory cf) {  
        RedisTemplate<String,Object> redisTemplate = new RedisTemplate<String,Object>();  
        redisTemplate.setConnectionFactory(cf);  
    	StringRedisTemplate srt = new StringRedisTemplate();
    	srt.setConnectionFactory(cf);
        return srt;
    }   

    @Bean
    public CacheManager cacheManager(RedisTemplate redisTemplate) {  
        RedisCacheManager cacheManager = new RedisCacheManager(redisTemplate);  
        cacheManager.setDefaultExpiration(3600); // Sets the default expire time (in seconds)  
	    return cacheManager;  
	}  
}
