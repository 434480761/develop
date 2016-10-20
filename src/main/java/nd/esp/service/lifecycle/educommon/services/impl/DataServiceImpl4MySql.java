package nd.esp.service.lifecycle.educommon.services.impl;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import nd.esp.service.lifecycle.educommon.services.DataService;
import nd.esp.service.lifecycle.repository.model.FullModel;
import nd.esp.service.lifecycle.support.DbName;

import org.springframework.stereotype.Component;
@Component(value="dataServiceImpl4MySql")
public class DataServiceImpl4MySql implements DataService {
	@PersistenceContext(unitName="entityManagerFactory")
	EntityManager defaultEm;
	@PersistenceContext(unitName="questionEntityManagerFactory")
	EntityManager questionEm;
	
	@Override
	public int queryCount(Map<String,Object> params) {
		String sql = (String)params.get("sql");
		Map<String,Object> param = (Map)params.get("param");
		DbName dbName = (DbName)params.get("dbName");
		Query query = getEntityManagerByDBName(dbName).createNativeQuery(sql);
    
	    Set<String> ks = param.keySet();
	    Iterator<String> it =  ks.iterator();
	    while(it.hasNext()){
	        String key = it.next();
	        query.setParameter(key, param.get(key));
	    }
	    
	    BigInteger c = (BigInteger)query.getSingleResult();
	    if(c != null){
	        return c.intValue();
	    }
	    return 0;
    }

	@Override
	public List<FullModel> queryResult(Map<String,Object> params) {
		String sql = (String)params.get("sql");
		Map<String,Object> paramMap = (Map)params.get("param");
		DbName dbName = (DbName)params.get("dbName");
		String limitSql = sql + " LIMIT 0,500 ";
		
		//查询
        Query query = getEntityManagerByDBName(dbName).createNativeQuery(limitSql, FullModel.class);
        
        //参数设置
        for(String paramKey : paramMap.keySet()){
        	query.setParameter(paramKey, params.get(paramKey));
        }
        List<FullModel> queryResult = query.getResultList();
        return queryResult;
    }
	
    /**
     * 获取对应库的EntityManager
     * <p>Create Time: 2016年2月16日   </p>
     * <p>Create author: xiezy   </p>
     * @param dbName
     * @return
     */
    private EntityManager getEntityManagerByDBName(DbName dbName){
        if(dbName.equals(DbName.QUESTION)){
            return questionEm;
        }else{
            return defaultEm;
        }
    }

}
