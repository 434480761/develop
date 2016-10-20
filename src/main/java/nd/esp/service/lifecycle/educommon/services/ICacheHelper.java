package nd.esp.service.lifecycle.educommon.services;

import java.util.List;

import nd.esp.service.lifecycle.educommon.models.RedisModel;
import nd.esp.service.lifecycle.repository.model.FullModel;

/**
 * 缓存业务接口
 * @author xuzy
 *
 */
public interface ICacheHelper {
	//redis数目后缀
	public static final String REDIS_NUM_SUFFIX = "num";
	//redis结果集后缀
	public static final String REDIS_RESULT_SUFFIX = "set";
	/**
	 * 获取记录数
	 * @param rm
	 * @param ds
	 * @return
	 */
	public int getResourceQueryCount(RedisModel rm,DataService ds);
	
	/**
	 * 获取记录列表
	 * @param rm
	 * @param ds
	 * @return
	 */
	public List<FullModel> getResourceQueryResult(RedisModel rm,DataService ds);
}
