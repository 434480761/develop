package nd.esp.service.lifecycle.support.aop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import nd.esp.service.lifecycle.app.LifeCircleApplicationInitializer;
import nd.esp.service.lifecycle.models.ivc.v06.IvcConfigModel;
import nd.esp.service.lifecycle.models.ivc.v06.IvcGlobalCategoryModel;
import nd.esp.service.lifecycle.models.ivc.v06.IvcLoadModel;
import nd.esp.service.lifecycle.models.ivc.v06.IvcUrlModel;
import nd.esp.service.lifecycle.support.Constant;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.staticdata.StaticDatas;
import nd.esp.service.lifecycle.utils.ParamCheckUtil;
import nd.esp.service.lifecycle.utils.StringUtils;

import org.apache.commons.collections.CollectionUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.nd.gaea.rest.support.WafContext;


@Aspect
@Component
@Order(9999)
public class ServiceAuthorAspect {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceAuthorAspect.class);
    @Autowired
    private HttpServletRequest request;
    private static final String DEFAULT_SERVICE_KEY = "DEFAULT_SERVICE_KEY";
    
    private static final String SERVICE_KEY="bsyskey";
    
    private static Long startTime=0L;
    
    private static Map<String,Integer> loadMap = new HashMap<String,Integer>();
    
    private static Set<String> resTypeSet = new HashSet<String>();
    static{
    	Set<Entry<Object, Object>> propEntries = LifeCircleApplicationInitializer.tablenames_properties.entrySet();
    	for(Entry<Object, Object> entry:propEntries) {
    		if(((String)entry.getKey()).startsWith("$R")) {
    			resTypeSet.add((String) entry.getValue());
    		}
    	}
    }
    

    /**
     * Pointcut
     * 定义Pointcut，Pointcut的名称为aspectjMethod()，此方法没有返回值和参数
     * 该方法就是一个标识，不进行调用
     */
    //@Pointcut("execution(* nd.esp.service.lifecycle.controllers.*.*(..)) || execution(* nd.esp.service.lifecycle.controllers.v06.*.*(..))")
    //@Pointcut("execution(* *..*Controller*.*(..))")
    @Pointcut("execution(* nd.esp.service.lifecycle.controllers..*Controller*.*(..)) || execution(* nd.esp.service.lifecycle.educommon.controllers.*.*(..))")
    private void aspectjMethod() {
    }

    ;

    /**
     * Before
     * 在核心业务执行前执行，不能阻止核心业务的调用。
     *
     * @param joinPoint
     */
    @Before("aspectjMethod()")
    public void beforeAdvice(JoinPoint joinPoint) {
    	
    	if(!StaticDatas.IS_IVC_CONFIG_ENABLED) {
    		return;
    	}

    	String ipAddr = getIp(request);
        String requestMethod = request.getMethod();
        String requestUrl = request.getRequestURL().toString();
        String serviceKey= request.getHeader(SERVICE_KEY);
        
        //FIXME 临时策略
        if(!StringUtils.hasText(serviceKey) && requestMethod.equals("GET")){
        	serviceKey = DEFAULT_SERVICE_KEY;
        }
        
        //特殊的url不需要bsyskey
    	if(isSpecialUrl(requestUrl)){
    		return;
    	}
    	
    	//本地请求的不需要bsyskey,for 单元测试
    	if(request.getLocalAddr().equals(request.getRemoteAddr())){
    		return;
    	}
    	
    	//Authorization=NAR_MAC的特殊处理
    	if(request.getHeader("Authorization") != null && request.getHeader("Authorization").startsWith("NDR_MAC") && !StringUtils.hasText(serviceKey)){
    		throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR, "LC/IVC_ERROR_SERVICE_KEY", "业务系统访问受限,service key未传");
    	}
    	
    	if(!StringUtils.hasText(serviceKey)){
			String userId = WafContext.getCurrentToken().getUserId();
    		if(StringUtils.hasText(userId)){
    			serviceKey = StaticDatas.IVC_USER_MAP.get(userId);
    		}
    	}
        
        if(!StringUtils.hasText(serviceKey)) {
        	throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR, "LC/IVC_ERROR_SERVICE_KEY", "业务系统访问受限,service key错误或不存在");
        }
        
        IvcConfigModel configModel = StaticDatas.IVC_CONFIG_MAP.get(serviceKey);
        
        if(null == configModel) {
        	throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR, "LC/IVC_ERROR_SERVICE_KEY", "业务系统访问受限,service key错误或不存在");
        }
        
        //ip限制;
        List<String> ipList = configModel.getGlobalIps();
        if(ipList!=null && !ipList.contains(ipAddr)) {
        	throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR, "LC/IVC_ERROR_IP", "业务系统访问受限,请求IP:"+ipAddr+"不在允许范围内.");
        }
        
        //url及方法排除    
        IvcUrlModel urlLoad = new IvcUrlModel();
        urlLoad.setLoad(null);
        if(!isUrlAuthorized(requestUrl, requestMethod, configModel, urlLoad)) {
        	throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR, "LC/IVC_ERROR_URL", "业务系统访问受限,接口不允许访问。");
        }
        
        List<String> globalCoverage = configModel.getGlobalCoverage();
        String[] coverages = request.getParameterValues("coverage");
        if(globalCoverage!=null && coverages!=null && coverages.length>0) {
        	for(String coverage:coverages) {
        		if(!globalCoverage.contains(coverage)) {
        			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR, "LC/IVC_ERROR_COVERAGE", "业务系统访问受限,请求私有库受限。 ");
        		}
        	}
        }
        
        //分类约束
        IvcGlobalCategoryModel globalCategory = configModel.getGlobalCategory();
        String [] paramCates = request.getParameterValues("category");
        List<String> categories = new ArrayList<String>();
        if(paramCates!=null && paramCates.length>0) {
        	categories = Arrays.asList(paramCates);
        }
        if(globalCategory!=null) {
	        if(!isResTypePermitted(requestUrl, globalCategory) || !isCategoryPermitted(categories, globalCategory)) {
	        	throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR, "LC/IVC_ERROR_CATEGORY", "业务系统访问受限,请求资源的分类维度受限。");
	        }
        }
        
        //负载控制
        String limit = request.getParameter("limit");
        long currTime = System.currentTimeMillis()/1000;
        synchronized (loadMap) {
			if(startTime!=currTime) {
				loadMap.clear();
				startTime = currTime;
			}
			if(urlLoad.getLoad()!=null) {
	        	if(StringUtils.hasText(limit)) {
	        		Integer result[] = ParamCheckUtil.checkLimit(limit);
	        		if(result[1]>urlLoad.getLoad().getMaxDpr()) {
	                	throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR, "LC/IVC_ERROR_DPR", "业务系统访问受限,每次请求数据记录数超出上限。 ");
	                }
	        	}
	        	String urlKey = serviceKey+"|"+urlLoad.getUrl()+"|"+urlLoad.getMethod();
	        	if(!validLoad(loadMap, urlKey, urlLoad.getLoad().getMaxRps())) {
	            	throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR, "LC/IVC_ERROR_RPS", "业务系统访问受限,请求过于频繁。 ");
	            }
	        } else {
	        	if(StringUtils.hasText(limit)) {
	    	        Integer result[] = ParamCheckUtil.checkLimit(limit);
	    	        if(result[1]>configModel.getGlobalLoad().getMaxDpr()) {
	    	        	throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR, "LC/IVC_ERROR_DPR", "业务系统访问受限,每次请求数据记录数超出上限。 ");
	    	        }
	            }
	        	if(configModel.getGlobalLoad()!=null && !validLoad(loadMap, serviceKey, configModel.getGlobalLoad().getMaxRps())) {
	        		throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR, "LC/IVC_ERROR_RPS", "业务系统访问受限,请求过于频繁。 ");
	        	}
	        }
		}
        
    }
    
    private boolean isSpecialUrl(String url){
		if (url.contains("/statisticals") 
				|| url.contains("/archive") 
				|| url.contains("/transcode/callback")
				|| url.contains("/transcode/videoCallback") 
				|| url.contains("/packaging/callback")
				|| url.contains("/3dbsys")
				) {
			return true;
		} 
		return false;
    }
    
    private boolean validLoad(Map<String,Integer> keyMap, String key, long maxRps) {
        if(keyMap.containsKey(key)) {
        	if(keyMap.get(key)+1>maxRps) {
        		return false;
        	}
        	keyMap.put(key, keyMap.get(key)+1);
        } else {
        	keyMap.put(key, 1);
        }
        
        return true;
    }
    
    private boolean isResTypePermitted(String requestUrl, IvcGlobalCategoryModel globalCategory) {
    	Pattern p = Pattern.compile(".*/"+Constant.LIFE_CYCLE_API_VERSION+"/(.*?)/");
        Matcher m = p.matcher(requestUrl);
        if(m.find()) {
        	if(!resTypeSet.contains(m.group(1))) {
        		return true;
        	}
        } else {
        	return true;
        }
    	
    	List<String> excludeResType = globalCategory.getExcludeResType();
    	if(CollectionUtils.isNotEmpty(excludeResType)) {
    		for(String ndcode:excludeResType) {
    			String type = LifeCircleApplicationInitializer.tablenames_properties.getProperty(ndcode);
    			if(requestUrl.contains(type)) {
    				return false;
    			}
    		}
    	}
    	
    	List<String> includeResType = globalCategory.getIncludeResType();
    	if(CollectionUtils.isNotEmpty(includeResType)) {
    		for(String ndcode:includeResType) {
    			String type = LifeCircleApplicationInitializer.tablenames_properties.getProperty(ndcode);
    			if(requestUrl.contains(type)) {
    				return true;
    			}
    		}
    		
    		return false;
    	}
    	
    	return true;
    }
    
    private boolean isCategoryPermitted(List<String> paramCategories, IvcGlobalCategoryModel globalCategory) {
    	if(CollectionUtils.isEmpty(paramCategories)) {
    		return true;
    	}
    	
    	List<String> excludeCate = globalCategory.getExcludeOtherType();
    	if(CollectionUtils.isNotEmpty(excludeCate)) {
    		for(String ndcode:excludeCate) {
    			for(String param:paramCategories) {
	    			if(param.equals(ndcode) || IsCategoryNested(ndcode,param)) {
	    				return false;
	    			}
    			}
    		}
    	}
    	
    	List<String> includeCate = globalCategory.getIncludeOtherType();
    	if(CollectionUtils.isNotEmpty(includeCate)) {
    		for(String ndcode:includeCate) {
    			for(String param:paramCategories) {
	    			if(param.equals(ndcode) || IsCategoryNested(ndcode,param)) {
	    				return true;
	    			}
    			}
    		}
    		
    		return false;
    	}
    	
    	return true;
    }
    
    private boolean isUrlAuthorized(String requestUrl, String requestMethod, IvcConfigModel configModel, IvcUrlModel matchUrlModel) {
    	List<IvcUrlModel> excludeList = configModel.getExcludeUrlList();
        if(CollectionUtils.isNotEmpty(excludeList)) {
        	for(IvcUrlModel urlModel:excludeList) {
        		if(requestUrl.matches(".*"+urlModel.getUrl())) {
        			for(String method:urlModel.getMethod()) {
        				if(method.equalsIgnoreCase(requestMethod)) {
        					return false;
        				}
        			}
        		}
        	}
        }
        
    	List<IvcUrlModel> includeList = configModel.getIncludeUrlList();
    	if(CollectionUtils.isNotEmpty(includeList)) {
	    	for(IvcUrlModel urlModel:includeList) {
	    		if(requestUrl.matches(".*"+urlModel.getUrl())) {
	    			for(String method:urlModel.getMethod()) {
	    				if(method.equalsIgnoreCase(requestMethod)) {
	    					matchUrlModel.setUrl(urlModel.getUrl());
	    					matchUrlModel.setMethod(urlModel.getMethod());
	    					if(urlModel.getLoad()!=null) {
		    					IvcLoadModel load = new IvcLoadModel();
		    					load.setMaxRps(urlModel.getLoad().getMaxRps());
		    					load.setMaxDpr(urlModel.getLoad().getMaxDpr());
		    					matchUrlModel.setLoad(load);
	    					}
	    					return true;
	    				}
	    			}
	    		}
	    	}
	    	
	    	return false;
    	}
    		
    	return true;
    }
    
    public static boolean IsCategoryNested(String parent, String children) {
    	if(!StringUtils.hasText(parent) || !StringUtils.hasText(children) || parent.length()!=children.length()) {
    		return false;
    	}
    	for(int i=1; i<=parent.length(); ++i) {
    		if(!parent.substring(0, i).equals(children.substring(0, i))) {
    			if(parent.substring(i-1).matches("^0+$")) {
    				return true;
    			} else {
    				return false;
    			}
    		}
    	}
		return false;
    }
    
    public static Set<String> getExcludeCategories(String serviceKey) {
    	Set<String> rtExcludeCate = new HashSet<String>();
    	if(StringUtils.isNotEmpty(serviceKey)) {
	    	IvcConfigModel configModel = StaticDatas.IVC_CONFIG_MAP.get(serviceKey);
	    	if(null != configModel) {
	    		IvcGlobalCategoryModel globalCategory = configModel.getGlobalCategory();
	    		if(null != globalCategory) {
	    			List<String> excludeCate = globalCategory.getExcludeOtherType();
	    	    	if(CollectionUtils.isNotEmpty(excludeCate)) {
	    	    		rtExcludeCate.addAll(excludeCate);
	    	    	}
	    		}
	    	}
    	}
    	
		return rtExcludeCate;
    }
    
    /**
     * 获取IP地址方法
     * @param request
     * @return
     */
    public static String getIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if(StringUtils.isNotEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)){
            //多次反向代理后会有多个ip值，第一个ip才是真实ip
            int index = ip.indexOf(",");
            if(index != -1){
                return ip.substring(0,index);
            }else{
                return ip;
            }
        }
        ip = request.getHeader("X-Real-IP");
        if(StringUtils.isNotEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)){
            return ip;
        }
        return request.getRemoteAddr();
    }
}
