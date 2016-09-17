package nd.esp.service.lifecycle.repository;

import org.springframework.data.repository.NoRepositoryBean;

import nd.esp.service.lifecycle.repository.index.Searchable;

/** 
 * @Description 
 * @author Rainy(yang.lin)  
 * @date 2015年5月15日 下午6:09:58 
 * @version V1.0
 * @param <T>
 */ 
  	
@NoRepositoryBean
public interface ResourceRepository<T extends EspEntity> extends EspRepository<T>,Searchable<T>{
	
}
