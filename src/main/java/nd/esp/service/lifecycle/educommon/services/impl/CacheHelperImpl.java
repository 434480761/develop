package nd.esp.service.lifecycle.educommon.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import nd.esp.service.lifecycle.educommon.models.RedisModel;
import nd.esp.service.lifecycle.educommon.services.DataService;
import nd.esp.service.lifecycle.educommon.services.ICacheHelper;
import nd.esp.service.lifecycle.models.QueryResultModel;
import nd.esp.service.lifecycle.repository.model.FullModel;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.utils.EduRedisTemplate;
import nd.esp.service.lifecycle.utils.ParamCheckUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
@Component
public class CacheHelperImpl implements ICacheHelper {
	private static ConcurrentMap<String, String> threadNameMap = new ConcurrentHashMap<String, String>();
	private final static ExecutorService executorService = CommonHelper.getPrimaryExecutorService();
	
    @Autowired
    private EduRedisTemplate<QueryResultModel> ertCount;
    
    @Autowired
    private EduRedisTemplate<FullModel> ert;

	@Override
	public int getResourceQueryCount(RedisModel rm, DataService ds) {
        boolean newValueFlag = false;
        //返回的数目
        int count = 0;
        
        //通过Redis获取
        QueryResultModel qrm = getCount(rm);
        if(qrm == null){
            //3、需要根据sql去查询数据，比较耗时
            count = queryCount(rm,ds);
            newValueFlag = true;
            
            //4、保存数据
            saveCount(rm, count);
        }else{
            count = qrm.getCount();
        }
        
        if(rm.isRefreshFlag() && !newValueFlag){
            //4、新起一个线程去更新
            updateQueryResultCount(rm,ds);
        }
        
        return count;
	}

	@Override
	public List<FullModel> getResourceQueryResult(RedisModel rm, DataService ds) {
        Integer result[] = ParamCheckUtil.checkLimit(rm.getLimit());
        long offSet = (long)result[0];
        long num = (long)result[1];
        String key = rm.getIndexKey();
        
        //判断key是否存在
        boolean flag = ert.existKey(key);
        
        if(!flag){
        	updateQueryResultCount(rm,ds);
            return null;
        }
        
        //取出KEY对应缓存总数
        long count = ert.zSetCount(key);
        
        if(offSet >= count){
            return new ArrayList<FullModel>();
        }
        
        long toIndex = offSet + num;
        if(toIndex > count){
            toIndex = count;
        }
        
        //根据分页参数取出缓存数据
        List<FullModel> fmList = ert.zRangeByScore(key, offSet, toIndex - 1, FullModel.class);
        return fmList;
	}

	/**
	 * 通过Redis获取total
	 * <p>Create Time: 2016年1月14日   </p>
	 * <p>Create author: xuzy   </p>
	 * @param sql
	 * @param paramMap
	 * @return
	 */
	private QueryResultModel getCount(RedisModel rm){
		QueryResultModel value = ertCount.get(rm.getIndexKey(),QueryResultModel.class);
		return value;
	}
	
    /**
     * 通过数据库获取total 
     * <p>Create Time: 2016年1月14日   </p>
     * <p>Create author: xuzy   </p>
     * @param sql
     * @param param
     * @return
     */
    private int queryCount(RedisModel rm,DataService ds){
        return ds.queryCount(rm.getParams());
    }
    
	/**
	 * 保存新的total值到Redis	
	 * <p>Create Time: 2016年1月14日   </p>
	 * <p>Create author: xuzy   </p>
	 * @param sql
	 * @param paramMap
	 * @param count
	 */
	private void saveCount(RedisModel rm,int count){
		QueryResultModel qrm = new QueryResultModel();
		qrm.setCount(count);
		qrm.setLastUpdate(System.currentTimeMillis());
		ertCount.set(rm.getIndexKey(), qrm,1,TimeUnit.DAYS);
	}
	
	/**
	 * 缓存items到Redis	
	 * <p>Create Time: 2016年1月14日   </p>
	 * <p>Create author: xuzy   </p>
	 * @param sql
	 * @param paramMap
	 */
    private void saveResult(RedisModel rm,DataService ds){
        List<FullModel> queryResult = ds.queryResult(rm.getParams());
        
        //保存到redis
        ert.zSet(rm.getIndexKey(), queryResult);
        
        //设置过期时间
        ert.expire(rm.getIndexKey(), 10l,TimeUnit.MINUTES);
	}
	
	 /**
     * 新起线程更新数据(先查询count值再更新)
     * 
     * @author:xuzy
     * @date:2015年12月4日
     * @param sql
     * @param param
     * @param qrc
     */
    private void updateQueryResultCount(final RedisModel rm,final DataService ds){
        if(!judgeThreadIsAlive(rm.getIndexKey())){
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    if(rm.getIndexKey().lastIndexOf(ICacheHelper.REDIS_NUM_SUFFIX) > -1){
                        int newCount = queryCount(rm,ds);
                        saveCount(rm, newCount);
                    }else if(rm.getIndexKey().lastIndexOf(ICacheHelper.REDIS_RESULT_SUFFIX) > -1){
                        saveResult(rm,ds);
                    }

					if(threadNameMap.containsKey(rm.getIndexKey())){
						threadNameMap.remove(rm.getIndexKey());
					}
				}
			});
			threadNameMap.put(rm.getIndexKey(), "");
			executorService.execute(thread);
		}
	}
    
	/**
	 * 根据线程名判断线程是否还存活
	 * 
	 * @author:xuzy
	 * @date:2015年12月3日
	 * @param threadName
	 * @return
	 */
	private boolean judgeThreadIsAlive(String threadName){
		return threadNameMap.containsKey(threadName);
	}
}
