package nd.esp.service.lifecycle.utils;

import java.util.regex.Pattern;

import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;

import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

/**
 * @title 参数校验工具类
 * @Desc TODO
 * @author liuwx
 * @version 1.0
 * @create 2015年1月28日 下午8:36:12
 */
public class ParamCheckUtil {

	/**
	 * 校验limit的合法性 标准格式 (0,1)&0<1
	 * 
	 * */
	public static Integer[] checkLimit(String limit) {
		if (!StringUtils.hasText(limit)) {
			throw new LifeCircleException(HttpStatus.BAD_REQUEST,
					LifeCircleErrorMessageMapper.LimitParamMissing);
		}
		Integer queryFirst;
		Integer queryLast;
		try {
			String[] pageInfo = limit.replaceAll(Pattern.quote("("), "")
					.replaceAll(Pattern.quote(")"), "").split(",");
			queryFirst = Integer.parseInt(pageInfo[0]);
			queryLast = Integer.parseInt(pageInfo[1]);
			if(queryFirst < 0 || queryLast <= 0){
				throw new LifeCircleException(HttpStatus.BAD_REQUEST,
						LifeCircleErrorMessageMapper.LimitParamIllegal);
			}
		} catch (Exception e) {
			throw new LifeCircleException(HttpStatus.BAD_REQUEST,
					LifeCircleErrorMessageMapper.LimitParamIllegal);
		}
		/*if (queryFirst > queryLast) {
			throw new LifeCircleException(
					LifeCircleErrorMessageMapper.LimitParamIllegal);
		}*/
		
		return new Integer[]{queryFirst,queryLast};
	}
}
