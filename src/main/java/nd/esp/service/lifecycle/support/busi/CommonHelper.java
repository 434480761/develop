package nd.esp.service.lifecycle.support.busi;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import nd.esp.service.lifecycle.daos.titan.TitanCommonRepositoryImpl;
import nd.esp.service.lifecycle.daos.titan.inter.TitanCommonRepository;
import nd.esp.service.lifecycle.educommon.models.ResClassificationModel;
import nd.esp.service.lifecycle.educommon.models.ResEducationalModel;
import nd.esp.service.lifecycle.educommon.models.ResLifeCycleModel;
import nd.esp.service.lifecycle.educommon.models.ResRightModel;
import nd.esp.service.lifecycle.educommon.models.ResTechInfoModel;
import nd.esp.service.lifecycle.educommon.models.ResourceModel;
import nd.esp.service.lifecycle.educommon.services.impl.CommonServiceHelper;
import nd.esp.service.lifecycle.educommon.vos.ResClassificationViewModel;
import nd.esp.service.lifecycle.educommon.vos.ResCoverageViewModel;
import nd.esp.service.lifecycle.educommon.vos.ResEducationalViewModel;
import nd.esp.service.lifecycle.educommon.vos.ResLifeCycleViewModel;
import nd.esp.service.lifecycle.educommon.vos.ResRelationViewModel;
import nd.esp.service.lifecycle.educommon.vos.ResRightViewModel;
import nd.esp.service.lifecycle.educommon.vos.ResTechInfoViewModel;
import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;
import nd.esp.service.lifecycle.educommon.vos.constant.IncludesConstant;
import nd.esp.service.lifecycle.models.CategoryPatternModel;
import nd.esp.service.lifecycle.models.FileSessionModel;
import nd.esp.service.lifecycle.models.coverage.v06.CoverageModel;
import nd.esp.service.lifecycle.models.coverage.v06.CoverageModelForUpdate;
import nd.esp.service.lifecycle.models.ivc.v06.IvcConfigModel;
import nd.esp.service.lifecycle.models.ivc.v06.IvcGlobalCategoryModel;
import nd.esp.service.lifecycle.models.ivc.v06.IvcLoadModel;
import nd.esp.service.lifecycle.models.ivc.v06.IvcUrlModel;
import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.EspRepository;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.Chapter;
import nd.esp.service.lifecycle.repository.sdk.impl.ServicesManager;
import nd.esp.service.lifecycle.support.Constant;
import nd.esp.service.lifecycle.support.Constant.CSInstanceInfo;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.enums.OperationType;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;
import nd.esp.service.lifecycle.support.staticdata.StaticDatas;
import nd.esp.service.lifecycle.utils.BeanMapperUtils;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.HttpClientUtils;
import nd.esp.service.lifecycle.utils.ParamCheckUtil;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;
import nd.esp.service.lifecycle.vos.chapters.v06.ChapterViewModel;
import nd.esp.service.lifecycle.vos.statics.CoverageConstant;
import nd.esp.service.lifecycle.vos.statics.ResourceType;

import org.apache.commons.lang.ArrayUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.gson.reflect.TypeToken;
import com.nd.gaea.rest.o2o.JacksonCustomObjectMapper;

/**
 * @title 业务通用帮助类
 * @author liuwx
 * @version 1.0
 * @create 2015年3月19日 上午10:45:40
 */
public class CommonHelper {
	private static final Logger LOG = LoggerFactory.getLogger(CommonHelper.class);
	private static final String HREF = "href";
	private static final String SOURCE = "source";
	
	private static final ForkJoinPool forkJoinPool = new ForkJoinPool();
	private final static ExecutorService primaryExecutorService = Executors.newCachedThreadPool();
	
	/**
	 * 获取ForkJoinPool默认线程池
	 * @return
	 */
	public static ForkJoinPool getForkJoinPool(){
		return forkJoinPool;
	}
	
	/**
	 * 高优先级线程池
	 * @return
	 */
	public static ExecutorService getPrimaryExecutorService(){
		return primaryExecutorService;
	}
	
	
	/**
	 * @desc 获取文件的超时时间
	 * @author liuwx
	 * @return
	 */
	public static Date fileOperationExpireDate() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.SECOND, Constant.FILE_OPERATION_EXPIRETIME);
		return calendar.getTime();
	}
	
	/** 
	 * @desc 获取文件的超时时间 
	 * @param expireSecond
	 * @return
	 */
	public static Date fileOperationExpireDate(int expireSecond) {
		//不能小于默认失效时间(临时策略)
		expireSecond=expireSecond<Constant.FILE_OPERATION_EXPIRETIME?Constant.FILE_OPERATION_EXPIRETIME:expireSecond;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.SECOND, expireSecond);
		return calendar.getTime();
	}
	
	/**
	 * @desc 创建通用的uuid
	 * @author liuwx
	 * @return
	 */
	public static UUID creatUUID(String id) {
		if(StringUtils.isEmpty(id)){
			id=Constant.DEFAULT_UPLOAD_URL_ID;
		}
		UUID uuid=null;
		if(id.equals(Constant.DEFAULT_UPLOAD_URL_ID)){
			 uuid=UUID.randomUUID();
		}else{
			uuid=UUID.fromString(id);
		}
		return uuid;
	}
	
	
	
	/**
	 * @desc 通过uid创建一个FileSessionModel对象
	 * @author liuwx
	 * @param uid
	 * @return
	 */
	public static FileSessionModel createSessionModel(String uid){
		FileSessionModel fileSessionModel=new FileSessionModel();
		fileSessionModel.setPath(Constant.FILE_PATH_URL);
		fileSessionModel.setExpireTime(Constant.FILE_OPERATION_EXPIRETIME);
		fileSessionModel.setRole(Constant.FILE_OPERATION_ROLE);
		fileSessionModel.setUid(uid);
		return fileSessionModel;
	}
	
	
	public static String getUid(HttpServletRequest request){
		
		String uid=request.getParameter(Constant.FILE_OPERATION_UID_REQUEST_PARAM_NAME);
		
		return uid;
		
	}
	
	
	   /**
     * @desc 返回打包的路径后缀名
     * @author liuwx
     * @return
     */
    public static String packageSuffix(){
        return ".pkg";
    }
    
    /**
     * 校验UUID
     * @param value
     * @return 检验结果
     * @author xuzy
     */
    public static boolean checkUuidPattern(String value){
    	String uuidPattern = "[a-z0-9]{8}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{12}";
    	return checkReg(value, uuidPattern);
    }
    
    /**
	 * 正则校验
	 * @param value 值
	 * @param pattern 正则表达式
	 * @return 校验结果
	 * @author xuzy
	 */
	public static boolean checkReg(String value,String pattern){
		return Pattern.matches(pattern, value);
	}
	
	/**
	 * 检验List<String>里面元素的字符长度
	 * @param list
	 * @param length
	 * @return 校验结果
	 * @author xuzy
	 */
	public static boolean checkListLength(List<String> list,int length){
		if(list != null && list.size() > 0){
			String s = StringUtils.join(list, ",");
			if(s.length() >= length){
				return false;
			}
		}
		return true;
	}

	public static List<ResClassificationModel> map2List4Categories(Map<String, List<? extends ResClassificationViewModel>> map,String resourceId,ResourceNdCode rc){
		return map2List4Categories(map, resourceId, rc, false);
	}
	
	/**
	 * 用于categories的model转换,map->list	
	 * <p>Create Time: 2015年7月9日   </p>
	 * <p>Create author: xiezy   </p>
	 * @param map              需要转换的map
	 * @param resourceId       资源的id
	 * @return
	 */
	public static List<ResClassificationModel> map2List4Categories(Map<String, List<? extends ResClassificationViewModel>> map,String resourceId,ResourceNdCode rc,boolean patchMode){
        List<ResClassificationModel> resultList = new ArrayList<ResClassificationModel>();
        //用于判断是否有资源类型的ND_CODE
        boolean defaultResCode = false;
        
        for(String key : map.keySet()){
            List<? extends ResClassificationViewModel> rcvmList = map.get(key);
            ResClassificationModel resClassificationModel;
            int index = 0;
            
            if(key.equals("phase")){
                index = 1;
            }else if(key.equals("grade")){
                index = 2;
            }else if(key.equals("subject")){
                index = 3;
            }else if(key.equals("edition")){
                index = 4;
            }else if(key.equals("sub_edition")){
                index = 5;
            }
            
            if(index > 0){//phase,grade,subject,edition,sub_edition
                for(ResClassificationViewModel rcvm : rcvmList){
                    if(!StringUtils.isEmpty(rcvm.getTaxonpath())){//taxonpath不为空
                    	resClassificationModel = new ResClassificationModel();
						if(!patchMode) {
							resClassificationModel.setIdentifier(UUID.randomUUID().toString());
						} else {
							resClassificationModel.setIdentifier(rcvm.getIdentifier());
						}
                        resClassificationModel.setResourceId(resourceId);
                        resClassificationModel.setTaxonpath(rcvm.getTaxonpath());
                        resClassificationModel.setTaxoncode(Arrays.asList(rcvm.getTaxonpath().split("/")).get(index));
						resClassificationModel.setOperation(rcvm.getOperation());

                        //如果taxonCode为空就不加入到resultList中
                        if(!StringUtils.isEmpty(Arrays.asList(rcvm.getTaxonpath().split("/")).get(index))){
                            resultList.add(resClassificationModel);
                        }
                    }else if(StringUtils.hasText(rcvm.getTaxoncode())){
						resClassificationModel = new ResClassificationModel();
						if(!patchMode) {
							resClassificationModel.setIdentifier(UUID.randomUUID().toString());
						} else {
							resClassificationModel.setIdentifier(rcvm.getIdentifier());
						}
						resClassificationModel.setResourceId(resourceId);
						resClassificationModel.setTaxoncode(rcvm.getTaxoncode());
						resClassificationModel.setOperation(rcvm.getOperation());
						resultList.add(resClassificationModel);
					}
                }
            }else{
                for (ResClassificationViewModel rcvm : rcvmList) {
                    if (StringUtils.isNotEmpty(rcvm.getTaxoncode())) {
                    	String categoryPattern = null;
                    	if(rcvm.getTaxonpath() != null){
                    		categoryPattern = rcvm.getTaxonpath().split("/")[0];
                    	}
                        resClassificationModel = new ResClassificationModel();
						if(!patchMode) {
							resClassificationModel.setIdentifier(UUID.randomUUID().toString());
						} else {
							resClassificationModel.setIdentifier(rcvm.getIdentifier());
						}
                        resClassificationModel.setResourceId(resourceId);
                        //因为此时taxonpath没有任何作用，还会造成taxoncode重复数据，所以将taxonpath置为null.  modify by xuzy  20150201
                        //resClassificationModel.setTaxonpath(null);
                        
                        //非K12维度模式，需要保存taxOnPath值
                        if(categoryPattern != null && !"K12".equals(categoryPattern) && StaticDatas.CATEGORY_PATTERN_MAP.containsKey(categoryPattern)){
                        	resClassificationModel.setTaxonpath(rcvm.getTaxonpath());
                        }
                        
                        resClassificationModel.setTaxoncode(rcvm.getTaxoncode());
						resClassificationModel.setOperation(rcvm.getOperation());
                        resultList.add(resClassificationModel);

                        if (rcvm.getTaxoncode() != null && rcvm.getTaxoncode().equals(rc.getNdCode())) {
                            defaultResCode = true;
                        }
                    }
                }
            }
        }
        
        if(!defaultResCode){
        	Assert.assertNotNull(rc);
        	
        	ResClassificationModel resClassificationModel = new ResClassificationModel();
            resClassificationModel.setIdentifier(UUID.randomUUID().toString());
            resClassificationModel.setResourceId(resourceId);
            resClassificationModel.setTaxoncode(rc.getNdCode());
            resultList.add(resClassificationModel);
        }
        
        return resultList;
    }
	
	
	/**
	 * 用于categories的model转换,list->map	
	 * <p>Create Time: 2015年7月9日   </p>
	 * <p>Create author: xiezy   </p>
	 * @param list             需要转换的list
	 * @param resourceType     资源需求类型
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, List<? extends ResClassificationViewModel>> list2map4Categories(List<? extends ResClassificationModel> list,String resourceType){
		//旧的维度数据处理
		String[] cc = {"$O","$S","$E","$R"};
		//根据ndcode排序
		Collections.sort((List)list);
		
		
        Map<String, List<? extends ResClassificationViewModel>> resultMap = new HashMap<String, List<? extends ResClassificationViewModel>>();
        resultMap.put("phase", new ArrayList<ResClassificationViewModel>());
        resultMap.put("grade", new ArrayList<ResClassificationViewModel>());
        resultMap.put("subject", new ArrayList<ResClassificationViewModel>());
        resultMap.put("edition", new ArrayList<ResClassificationViewModel>());
        resultMap.put("sub_edition", new ArrayList<ResClassificationViewModel>());
        if(StringUtils.isNotEmpty(resourceType)){
        	resultMap.put(resourceType, new ArrayList<ResClassificationViewModel>());
        }
        
        if(CollectionUtils.isNotEmpty(list)){
        	for(ResClassificationModel rcm : list){
                ResClassificationViewModel resClassificationViewModel = new ResClassificationViewModel();
                resClassificationViewModel.setIdentifier(rcm.getIdentifier());
                resClassificationViewModel.setTaxonpath(rcm.getTaxonpath());
                resClassificationViewModel.setTaxoncode(rcm.getTaxoncode());
                resClassificationViewModel.setTaxonname(rcm.getTaxonname());
                
                //获取维度模式
                String categoryPattern = null;
                if(StringUtils.isNotEmpty(rcm.getTaxonpath())){
                	categoryPattern = rcm.getTaxonpath().split("/")[0];
                }
                
                // 动态生成维度数据列表的key
                if(!ArrayUtils.contains(cc, rcm.getCategoryCode()) || (categoryPattern != null && !categoryPattern.equals("K12"))){
                	String k = rcm.getCategoryName();
                	if(k != null){
                    	if(resultMap.get(k) == null){
                    		List<ResClassificationViewModel> l = new ArrayList<ResClassificationViewModel>();
                    		l.add(resClassificationViewModel);
                    		resultMap.put(k, l);
                    	}else{
                    		((List)resultMap.get(k)).add(resClassificationViewModel);
                    	}
                    	continue;
                	}
                }
                
                //确定是放入哪个key中的list
                String key = resourceType;
                
                if(StringUtils.isEmpty(rcm.getTaxonpath())){
                    key = getResCategoryKey(rcm.getTaxoncode(), resourceType);
                }else {//phase,grade,subject,edition,sub_edition
                	List<String> path = Arrays.asList(rcm.getTaxonpath().split("/"));
                	int size = path.size();
                    if(size > 1 && rcm.getTaxoncode().equals(path.get(1))){
                        key = "phase";
                    }else if(size > 2 && rcm.getTaxoncode().equals(path.get(2))){
                        key = "grade";
                    }else if(size > 3 && rcm.getTaxoncode().equals(path.get(3))){
                        key = "subject";
                    }else if(size > 4 && rcm.getTaxoncode().equals(path.get(4))){
                        key = "edition";
                    }else if(size > 5 && rcm.getTaxoncode().equals(path.get(5))){
                        key = "sub_edition";
                    }
                }

                if(resultMap.get(key) != null){
                	 ((List)resultMap.get(key)).add(resClassificationViewModel);
                }
            }
        }
        
        //没有数据不返回
        Iterator<String> iterator = resultMap.keySet().iterator();
        while (iterator.hasNext()) {
			String k = iterator.next();
			if(resultMap.get(k).size() == 0){
				iterator.remove();
			}
		}
        
        //资源类型排序
		if(resultMap.get(resourceType) != null){
			Collections.sort((List)resultMap.get(resourceType));
		}
        return resultMap;
    }
	
	private static String getResCategoryKey(String taxOnCode, String defaultKey) {
	    String key = defaultKey;
	    
	    if(StringUtils.isNotEmpty(taxOnCode)) {
	        String[] arrCategoryCode = {"$O","$S","$E"};
	        if(taxOnCode.contains("$S")) {
	            key = "subject";
	        } else if(taxOnCode.contains("$O")) {
	            if(!taxOnCode.endsWith("000000")) {
    	            if(taxOnCode.endsWith("0000")) { //phase
    	                key = "phase";
    	            } else {
    	                key = "grade";
    	            }
	            }
	        } else if(taxOnCode.contains("$E")) {
	            if(taxOnCode.endsWith("000")) {
	                key = "edition";
	            } else {
	                key = "sub_edition";
	            }
	        }
	    }
	    
	    return key;
	}
	
	/**
	 * 用于tech_info的model转换,map->list  	
	 * <p>Create Time: 2015年7月9日   </p>
	 * <p>Create author: xiezy   </p>
	 * @param map
	 * @return
	 */
	public static List<ResTechInfoModel> map2List4TechInfo(Map<String,? extends ResTechInfoViewModel> map){
	    List<ResTechInfoModel> resultList = new ArrayList<ResTechInfoModel>();
	    
	    for(String key : map.keySet()){
	        ResTechInfoViewModel rtvm = map.get(key);
	        if(rtvm != null){
		        rtvm.setTitle(key);
		        ResTechInfoModel resTechInfoModel = BeanMapperUtils.beanMapper(rtvm, ResTechInfoModel.class);
		        resultList.add(resTechInfoModel);
	        }

	    }
	    
	    return resultList;
	}
	
	/**
	 * 用于tech_info的model转换,list->map   	
	 * <p>Create Time: 2015年7月9日   </p>
	 * <p>Create author: xiezy   </p>
	 * @param list
	 * @return
	 */
	public static Map<String,? extends ResTechInfoViewModel> list2Map4TechInfo(List<? extends ResTechInfoModel> list){
	    
	    return list2Map4TechInfo(list, null);
	}
	
	/**
	 * list2Map4TechInfo扩展 -- 根据终端信息过滤
	 * @author xiezy
	 * @date 2016年9月20日
	 * @param list
	 * @param terminal
	 * @return
	 */
	public static Map<String,? extends ResTechInfoViewModel> list2Map4TechInfo(List<? extends ResTechInfoModel> list, String terminal){
	    Map<String,ResTechInfoViewModel> resultMap = new HashMap<String, ResTechInfoViewModel>();
	    
	    for(ResTechInfoModel rtm : list){
	        ResTechInfoViewModel resTechInfoViewModel = BeanMapperUtils.beanMapper(rtm, ResTechInfoViewModel.class);
	        
	        if(StringUtils.hasText(terminal)){
	        	if(rtm.getTitle().equals(HREF) || rtm.getTitle().equals(SOURCE) 
	        			|| rtm.getTitle().startsWith(terminal)){
	        		resultMap.put(rtm.getTitle(), resTechInfoViewModel);
	        	}
	        }else{
	        	resultMap.put(rtm.getTitle(), resTechInfoViewModel);
	        }
	    }
	    
	    return resultMap;
	}
	
	/**
	 * 将业务模型转为数据模型（基本信息）
	 * @param am
	 * @return
	 */
	public static <T extends Education> Education convertModelIn(ResourceModel rm,Class<T> tt){
		T t;
		try {
			t = tt.newInstance();
		} catch (InstantiationException e) {
			throw new LifeCircleException("LC/INIT_OBJECT_ERROR", tt+"对象创建失败");
		} catch (IllegalAccessException e) {
			throw new LifeCircleException("LC/INIT_OBJECT_ERROR", tt+"对象创建失败");
		}
		//生命周期属性
		ResLifeCycleModel rlcm = rm.getLifeCycle();
		//资源的教育属性
		ResEducationalModel rem = rm.getEducationInfo();
		//版权属性
		ResRightModel rrm = rm.getCopyright();
		
		//由于教育属性比较多用beanMapper转换
		if(rem != null){
			t = (T) BeanMapperUtils.beanMapper(rem, tt);
			t.setEduDescription(rem.getDescription());
			t.setEduLanguage(rem.getLanguage());
		}

		if(rlcm != null){
			t.setVersion(rlcm.getVersion());
			t.setStatus(rlcm.getStatus());
			t.setEnable(rlcm.isEnable());
			t.setCreator(rlcm.getCreator());
			t.setPublisher(rlcm.getPublisher());
			t.setProvider(rlcm.getProvider());
			t.setProviderSource(rlcm.getProviderSource());
			if(rlcm.getCreateTime() != null){
				t.setCreateTime(new Timestamp(rlcm.getCreateTime().getTime()));
			}
			if(rlcm.getLastUpdate() != null){
				t.setLastUpdate(new Timestamp(rlcm.getLastUpdate().getTime()));
			}
		}

		if(rrm != null){
			t.setCrRight(rrm.getRight());
			t.setCrDescription(rrm.getDescription());
			t.setAuthor(rrm.getAuthor());
		}
		
		//基本属性处理
		t.setIdentifier(rm.getIdentifier());
		t.setTitle(rm.getTitle());
		t.setDescription(rm.getDescription());
		t.setLanguage(rm.getLanguage());
		t.setTags(rm.getTags());
		t.setKeywords(rm.getKeywords());
		t.setPreview(rm.getPreview());
		t.setCustomProperties(rm.getCustomProperties());
		return t;
	}
	
	/**
     * 将业务模型转换为数据模型
     * 
     * @param rm
     * @param tt
     * @return
     * @since
     */
    public static <T extends Education> Education convertModelInForEducation(ResourceModel rm,Class<T> tt){
        T t;
        try {
            t = tt.newInstance();
        } catch (InstantiationException e) {
            throw new LifeCircleException("LC/INIT_OBJECT_ERROR", tt+"对象创建失败");
        } catch (IllegalAccessException e) {
            throw new LifeCircleException("LC/INIT_OBJECT_ERROR", tt+"对象创建失败");
        }
        //生命周期属性
        ResLifeCycleModel rlcm = rm.getLifeCycle();
        
        if(rlcm != null){
            t.setVersion(rlcm.getVersion());
            t.setStatus(rlcm.getStatus());
            t.setEnable(rlcm.isEnable());
            t.setCreator(rlcm.getCreator());
            t.setPublisher(rlcm.getPublisher());
            t.setProvider(rlcm.getProvider());
            t.setProviderSource(rlcm.getProviderSource());
            if(rlcm.getCreateTime() != null){
                t.setCreateTime(new Timestamp(rlcm.getCreateTime().getTime()));
            }
            if(rlcm.getLastUpdate() != null){
                t.setLastUpdate(new Timestamp(rlcm.getLastUpdate().getTime()));
            }
        }

        //基本属性处理
        t.setIdentifier(rm.getIdentifier());
        t.setTitle(rm.getTitle());
        t.setDescription(rm.getDescription());
        t.setLanguage(rm.getLanguage());
        t.setTags(rm.getTags());
        t.setKeywords(rm.getKeywords());
        return t;
    }
	
	/**
	 * 将数据模型转为业务模型（基本信息）
	 * @param am
	 * @return
	 */
	public static <T extends ResourceModel> T convertModelOut(Education nr,Class<T> tt){
		T t;
		try {
			t = tt.newInstance();
		} catch (InstantiationException e) {
			throw new LifeCircleException("LC/INIT_OBJECT_ERROR", tt+"对象创建失败");
		} catch (IllegalAccessException e) {
			throw new LifeCircleException("LC/INIT_OBJECT_ERROR", tt+"对象创建失败");
		}
		//最基本属性
		t = BeanMapperUtils.beanMapper(nr, tt);
		//生命周期属性
		ResLifeCycleModel rlcm = BeanMapperUtils.beanMapper(nr, ResLifeCycleModel.class);
		//资源的教育属性
		ResEducationalModel rem = BeanMapperUtils.beanMapper(nr, ResEducationalModel.class);
		//版权属性
		ResRightModel rrm = BeanMapperUtils.beanMapper(nr, ResRightModel.class);
		t.setCopyright(rrm);
		t.setEducationInfo(rem);
		t.setLifeCycle(rlcm);
		if(rem != null){
			rem.setDescription(nr.getEduDescription());
			rem.setLanguage(nr.getLanguage());
		}
		if(rrm != null){
			rrm.setDescription(nr.getCrDescription());
		}
		
		return t;
	}
	
	/**
     * 将数据模型转为业务模型（基本信息）
     * 
     * @param nr
     * @param tt
     * @return
     * @since
     */
    public static <T extends ResourceModel> T convertModelOutForEducation(Education nr,Class<T> tt){
        T t;
        try {
            t = tt.newInstance();
        } catch (InstantiationException e) {
            throw new LifeCircleException("LC/INIT_OBJECT_ERROR", tt+"对象创建失败");
        } catch (IllegalAccessException e) {
            throw new LifeCircleException("LC/INIT_OBJECT_ERROR", tt+"对象创建失败");
        }
        //最基本属性
        t = BeanMapperUtils.beanMapper(nr, tt);
        //生命周期属性
        ResLifeCycleModel rlcm = BeanMapperUtils.beanMapper(nr, ResLifeCycleModel.class);
        t.setLifeCycle(rlcm);
        
        return t;
    }
	
	/**
	 * 调用UC系统根据userName获取userId
	 * 
	 * @author:xuzy
	 * @date:2015年7月28日
	 * @param userName
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static String getUserIdFromUc(String userName){
		String orgName = Constant.FILTER_UC_API;
		String url = Constant.UC_API_DOMAIN+"users/"+userName+"/infos?org_name="+orgName;
		try {
			String value = HttpClientUtils.httpGet(url);
			if(value != null){
				Map<String,String> m = ObjectUtils.fromJson(value, new TypeToken<Map<String,String>>(){});
				String userId = m.get("user_id");
				if(userId != null){
					return userId;
				}
			}
		} catch (IOException e) {
            return null;
		}
		return null;
	}
	
	/**
	 * 适配维度数据（加上K12维度模式）
	 * 
	 * @author:xuzy
	 * @date:2015年8月3日
	 * @param categories
	 * @return
	 */
	public static List<String> adaptCategoriesAddK12(List<String> categories){
		if(CollectionUtils.isNotEmpty(categories)){
			List<String> newCategories = new ArrayList<String>();
			Iterator<String> tmp = categories.iterator();
			while (tmp.hasNext()) {
				String s = tmp.next();
				if(s != null && s.contains("/") && !s.startsWith("K12")){
					s = "K12/"+ s;
				}
				newCategories.add(s);
			}
			return newCategories;
		}
		return null;
	}
	
    /**
     * 添加默认的资源维度数据（当资源的categories中不存在对应的维度数据，如questions.categories 中：1、若不存在 $RE0200 则加入，2、若已存在，则不处理）
     * 
     * @param categories 资源表中的categories字段， 主要放置维度路径，维度ndCode
     * @param resourceNdCode 资源维度数据
     * @return
     * @since
     */
	public static List<String> adapterCategoriesAddResourceNdCode(List<String> categories, ResourceNdCode resourceNdCode){
	    Assert.assertNotNull(resourceNdCode);
	    
	    boolean isResourceHasNdCode = false;
	    if(categories == null){
	        categories = new ArrayList<String>();
	    }
        
        for(String category:categories) {
            if(resourceNdCode.getNdCode().equals(category)) {
                isResourceHasNdCode = true;
                break;
            }
        }
        if(!isResourceHasNdCode) {
            categories.add(resourceNdCode.getNdCode());
        }
	    return categories;
	}
	
	/**
	 * 适配维度数据（去除K12维度模式）
	 * 
	 * @author:xuzy
	 * @date:2015年8月3日
	 * @param categories
	 * @return
	 */
	public static List<String> adaptCategoriesRemoveK12(List<String> categories){
		if(CollectionUtils.isNotEmpty(categories)){
			List<String> newCategories = new ArrayList<String>();
			Iterator<String> tmp = categories.iterator();
			while (tmp.hasNext()) {
				String s = tmp.next();
				if(s!= null && s.startsWith("K12")){
					s = s.substring(4);
				}
				newCategories.add(s);
			}
			return newCategories;
		}
		return null;
	}
	
	/**
	 * 判断资源类型enable是否被删除
	 * 
	 * @author:xuzy
	 * @date:2015年8月17日
	 * @param resource
	 * @return
	 */
	public static boolean checkResourceDelete(Education resource){
		if(resource != null && resource.getEnable() != null && !resource.getEnable()){
			return true;
		}
		return false;
	}
	
	/**
	 * V06入参通用业务校验
	 * 
	 * @author:xuzy
	 * @date:2015年8月18日
	 * @param viewModel
	 * @param checkflag  <p>检验标志位，由二进制数字组成，第一位代码UUID校验，第二位代表keywords字符长度校验，第三位代表tags字符长度校验，第四位代表techInfo属性校验，第五位代表categories属性校验</p>
	 * 					  如只检验UUID，入参为00001
	 */
	public static void inputParamValid(ResourceViewModel viewModel,String checkflag,OperationType ot){
		//UUID校验
		if(checkflag == null || (checkflag != null && checkflag.charAt(4) == '1')){
			if(!checkUuidPattern(viewModel.getIdentifier())){
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.CheckIdentifierFail.getCode(),LifeCircleErrorMessageMapper.CheckIdentifierFail.getMessage());
			}
		}
		
		
		//keywords字符长度校验
		if(checkflag == null || (checkflag != null && checkflag.charAt(3) == '1')){
			if(!checkListLength(viewModel.getKeywords(),1000)){
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.CheckKeywordsLengthFail.getCode(), LifeCircleErrorMessageMapper.CheckKeywordsLengthFail.getMessage());
			}
		}
		
		//tags字符长度校验
		if(checkflag == null || (checkflag != null && checkflag.charAt(2) == '1')){
			if(!checkListLength(viewModel.getTags(), 1000)){
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.CheckTagsLengthFail.getCode(), LifeCircleErrorMessageMapper.CheckTagsLengthFail.getMessage());
			}
		}
			
		//techInfo属性校验
		if(checkflag == null || (checkflag != null && checkflag.charAt(1) == '1')){
			Map<String,? extends ResTechInfoViewModel> techInfoMap = viewModel.getTechInfo();
			if(techInfoMap == null || !techInfoMap.containsKey("href")){
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.ChecTechInfoFail);
			}
		}
		
		//categories属性校验
		if(checkflag == null || (checkflag != null && checkflag.charAt(0) == '1')){
			Map<String, List<? extends ResClassificationViewModel>> categories = viewModel.getCategories();
			if(categories != null){
				for(String key : categories.keySet()){
					List<? extends ResClassificationViewModel> cList = categories.get(key);
					if(cList != null && !cList.isEmpty()){
						for (ResClassificationViewModel c : cList) {
							if(StringUtils.isNotEmpty(c.getTaxonpath())){
								if(!checkCategoryPattern(c.getTaxonpath())){
									LOG.error("taxonpath不对，{}",c.getTaxonpath());
									throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.CheckTaxonpathFail.getCode(),LifeCircleErrorMessageMapper.CheckTaxonpathFail.getMessage());
								}
							}
						}
					}
				}
			}
		}
		
		//创建对覆盖范围的数据单独作校验，不允许有多个OWNER数据
		if(ot == OperationType.CREATE){
			List<? extends ResCoverageViewModel> coverageList = viewModel.getCoverages();
			if(CollectionUtils.isNotEmpty(coverageList)){
				int num = 0;
				for (ResCoverageViewModel resCoverageViewModel : coverageList) {
					if(resCoverageViewModel.getStrategy().toUpperCase().equals("OWNER")){
						num++;
					}
				}
				if(num > 1){
					throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.CheckCoverageFail);
				}
			}
		}
		
		//校验版权的起始与结束时间合法性
		if(viewModel.getCopyright() != null){
			ResRightViewModel vm = viewModel.getCopyright();
			if(vm.getRightStartDate() != null && vm.getRightEndDate() != null && vm.getRightEndDate().longValue() < vm.getRightStartDate().longValue()){
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.CheckRightDateFail);
			}
		}
	}
	
	/**
	 * Coverage批量创建和修改时入参一个OWNER的校验
	 * 保证批量传入的覆盖范围,一个资源的覆盖范围，有且仅有一个OWNNER的覆盖范围策略
	 * <p>Create Time: 2015年10月20日   </p>
	 * <p>Create author: xiezy   </p>
	 * @param co
	 * @param isCreate
	 */
	public static void checkCoverageHaveOnlyOneOwner(List<?> co,boolean isCreate){
        Map<String, String> haveOwner = new HashMap<String, String>();
        for (Object object : co) {
            CoverageModelForUpdate cm = null;
            if(isCreate){
                cm = BeanMapperUtils.beanMapper((CoverageModel)object, CoverageModelForUpdate.class);
            }else{
                cm = (CoverageModelForUpdate)object;
            }
            
            if (cm.getStrategy().equals(CoverageConstant.STRATEGY_OWNER)) {
                if (haveOwner.containsKey(cm.getResource())) {// 该资源已经有OWNER
                    LOG.error("入参错误,对同一资源strategy=OWNER的覆盖范围超过两个--一个资源的覆盖范围，有且仅有一个OWNNER的覆盖范围策略");

                    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR, "LC/CREATE_COVERAGE_INPUT_ERROR",
                            "入参错误,对同一资源strategy=OWNER的覆盖范围超过两个--一个资源的覆盖范围，有且仅有一个OWNNER的覆盖范围策略");
                }
                else {
                    haveOwner.put(cm.getResource(), cm.getStrategy());
                }
            }
        }
    }
	
	/**	
	 * @desc: 通过path获取cs对应的实例 
	 * @createtime: 2015年8月20日 
	 * @author: lsm 
	 * @param disPath
	 * @return
	 */
    public static CSInstanceInfo getCsInstance(String disPath){
	    
	    String instanceKey=SessionUtil.getHrefInstanceKey(disPath);
	    //todo REF_PATH应该迁移到constant
	    if(!disPath.contains(TransCodeUtil.REF_PATH)){
	        instanceKey=TransCodeUtil.REF_PATH+instanceKey;
	    }
	    Constant.CSInstanceInfo instanceInfo = Constant.CS_INSTANCE_MAP.get(instanceKey);
	    
	    if (null == instanceInfo) {
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR, "LC/COPY_FAIL", "没有找到对应的实例");
        }
	    return instanceInfo;
	}


	public static <T extends ResourceModel> T convertViewModelIn(ResourceViewModel viewModel,Class<T> t,ResourceNdCode rc){
		return convertViewModelIn(viewModel, t, rc, false);
	}

	/**
	 * 
	 * V06入参数据转换viewModel-->model
	 * @author:xuzy
	 * @date:2015年8月20日
	 * @param model				LC入参
	 * @param t					model对应的class
	 * @return
	 */
	public static <T extends ResourceModel> T convertViewModelIn(ResourceViewModel viewModel,Class<T> t,ResourceNdCode rc, boolean patchMode){
		T m = null;
		if(viewModel==null){
            throw new NullPointerException("对象不能为空");
        }
        Assert.assertNotNull(viewModel.getIdentifier());

        // 设置生命周期enable默认为true
        ResLifeCycleViewModel lifeCycleViewModel = viewModel.getLifeCycle();
        if (lifeCycleViewModel != null) {
            lifeCycleViewModel.setEnable(true);
        }

        // 设置关系enable默认为true
        List<? extends ResRelationViewModel> relationViewModels = viewModel.getRelations();
        if (CollectionUtils.isNotEmpty(relationViewModels)) {
            for (ResRelationViewModel relationViewModel : relationViewModels) {
                relationViewModel.setEnable(true);
            }
        }
        m = BeanMapperUtils.beanMapper(viewModel, t);
        if (viewModel.getCategories() != null) {
            m.setCategoryList(map2List4Categories(viewModel.getCategories(), viewModel.getIdentifier(), rc, patchMode));
        }
        if (viewModel.getTechInfo() != null) {
            m.setTechInfoList(map2List4TechInfo(viewModel.getTechInfo()));
        }
        if (viewModel.getCustomProperties() != null) {
            m.setCustomProperties(ObjectUtils.toJson(viewModel.getCustomProperties()));
        }
		
		return m;
	}
	
	/**
	 * 
	 * V06出参数据转换model-->viewModel
	 * @author:xuzy
	 * @date:2015年9月1日
	 * @param model				LC业务模型数据
	 * @param t					viewModel对应的class
	 * @param lcFlag			是否输出life_cycle值
	 * @return
	 */
	public static <T extends ResourceViewModel> T convertViewModelOut(ResourceModel model,Class<T> t){
		return convertViewModelOut(model,t,null);
	}
	
	/**
	 * 
	 * V06出参数据转换model-->viewModel
	 * @author:xuzy
	 * @date:2015年8月20日
	 * @param model				LC业务模型数据
	 * @param t					viewModel对应的class
	 * @param lcFlag			是否输出life_cycle值
	 * @param resTypeStr		返回的categories中用来存放单个维度数据的key值，默认为res_type
	 * @return
	 */
	public static <T extends ResourceViewModel> T convertViewModelOut(ResourceModel model,Class<T> t,String resTypeStr){
		T v = null;
		if(model != null){
			//v = ObjectUtils.fromJson(ObjectUtils.toJson(model), t);
			v = BeanMapperUtils.beanMapper(model, t);
			if(model.getCategoryList() != null){
				v.setCategories(list2map4Categories(model.getCategoryList(), StringUtils.isEmpty(resTypeStr) ? "res_type" : resTypeStr));
			}
			if(model.getTechInfoList() != null){
				v.setTechInfo(list2Map4TechInfo(model.getTechInfoList()));
			}
			if(StringUtils.isNotEmpty(model.getCustomProperties())){
				v.setCustomProperties(ObjectUtils.fromJson(model.getCustomProperties(), Map.class));
			}
			//无须返回前台，将relations、coverages赋值为空
			v.setRelations(null);
			v.setCoverages(null);
		}
		return v;
	}
	
	/**
	 * 校验K12模式是否合法
	 * 
	 * @author:xuzy
	 * @date:2015年9月2日
	 * @param taxonPath
	 * @return
	 */
	public static boolean checkCategoryPattern(String taxonPath){
		if(StringUtils.isNotEmpty(taxonPath) && taxonPath.contains("/")){
			String categoryPattern = taxonPath.substring(0, taxonPath.indexOf("/"));
			
			String[] segment = null;
			if(StaticDatas.CATEGORY_PATTERN_MAP.containsKey(categoryPattern)){
				CategoryPatternModel cpm = StaticDatas.CATEGORY_PATTERN_MAP.get(categoryPattern);
				String seg = cpm.getSegment();
				if(StringUtils.isNotEmpty(seg)){
					segment = seg.split(",");
				}
			}
			
			int num = 0;
			for (int i = 0; i < taxonPath.length(); i++) {
				if(taxonPath.charAt(i) == '/'){
					num++;
				}
			}
			if(segment == null){
				return true;
			}
			
			if(ArrayUtils.contains(segment, String.valueOf(num))){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 将model转为view
	 *
	 * @param model
	 * @param resourceType
	 * @return
	 * @since
	 * @update by liuwx at 201510.22
	 * @updateContent 从 NDResourceController类迁移过来,解决多处对两种资源类型的判断
	 */
	public static ResourceViewModel changeToView(ResourceModel model, String resourceType,List<String> includes,CommonServiceHelper commonServiceHelper) {

		return changeToView(model, resourceType, includes, commonServiceHelper, null);
	}

	/**
	 * changeToView扩展 -- 根据终端信息过滤TI
	 * @author xiezy
	 * @date 2016年9月20日
	 * @param terminal
	 * @return
	 */
	public static ResourceViewModel changeToView(ResourceModel model, String resourceType,List<String> includes,CommonServiceHelper commonServiceHelper, String terminal) {
		if (model == null) {
			return null;
		}
		ResourceViewModel view = BeanMapperUtils.beanMapper(model, commonServiceHelper.getViewClass(resourceType));

		if (model.getLifeCycle() != null && view.getLifeCycle() != null) {
			view.getLifeCycle().setVersion(model.getLifeCycle().getVersion());
		}
		view.setCoverages(null);
		if (model.getCategoryList()!= null) {
			view.setCategories(CommonHelper.list2map4Categories(model.getCategoryList(),
					changeResourceTypeToCategoryKey(resourceType)));
		}
		if (model.getTechInfoList()!=null) {
			view.setTechInfo(CommonHelper.list2Map4TechInfo(model.getTechInfoList(), terminal));
		}
		if(StringUtils.isNotEmpty(model.getCustomProperties())){
			view.setCustomProperties(ObjectUtils.fromJson(model.getCustomProperties(), Map.class));
		}

		// 统一处理所有的附加属性：
		if (includes == null) {
			includes = new ArrayList<String>();
		}

		// CG
		if (includes.contains(IncludesConstant.INCLUDE_CG)) {
			if (view.getCategories() == null) {
				view.setCategories(new HashMap<String, List<? extends ResClassificationViewModel>>());
			}
		} else {
			view.setCategories(null);
		}

		// CR
		if (includes.contains(IncludesConstant.INCLUDE_CR)) {
			if (view.getCopyright() == null) {
				view.setCopyright(new ResRightViewModel());
			}
		} else {
			view.setCopyright(null);
		}

		// EDU
		if (includes.contains(IncludesConstant.INCLUDE_EDU)) {
			if (view.getEducationInfo() == null) {
				view.setEducationInfo(new ResEducationalViewModel());
			}
		} else {
			view.setEducationInfo(null);
		}

		// LC
		if (includes.contains(IncludesConstant.INCLUDE_LC)) {
			if (view.getLifeCycle() == null) {
				view.setLifeCycle(new ResLifeCycleViewModel());
			}
		} else {
			view.setLifeCycle(null);
		}

		// TI
		if (includes.contains(IncludesConstant.INCLUDE_TI)) {
			if (view.getTechInfo() == null) {
				view.setTechInfo(new HashMap<String, ResTechInfoViewModel>());
			}
		} else {
			view.setTechInfo(null);
		}
		
        // ask by cst 2015.11.27 sort preview by key (String asc)
        if (CollectionUtils.isNotEmpty(view.getPreview())) {
			Map<String, String> treeMap = new TreeMap<String, String>(RESOURCE_PREIVEW_PREFIX_COMPARATOR);
            treeMap.putAll(view.getPreview());
            view.setPreview(treeMap);
        }

		return view;
	}

	/**
	 * @param resourceType
	 * @return
	 * @since
	 * @update by liuwx at 201510.22
	 * @updateContent 从 NDResourceController类迁移过来,解决多处对两种资源类型的判断
	 */
	public static  String changeResourceTypeToCategoryKey(String resourceType) {
		if (StringUtils.isEmpty(resourceType)) {
			return resourceType;
		}
		if("assets".equals(resourceType)){
			return resourceType + "_type";
		}else{
			return "res_type";
		}

	}

	/**
	 * 当且仅当ID>0的时候返回true
	 * */
	private static boolean checkEducationExistInTitan(String primaryCategory, String identifier){
		Long id;
		try {
			TitanCommonRepository titanCommonRepository = new TitanCommonRepositoryImpl();
			id = titanCommonRepository.getEnableVertexIdByLabelAndId(primaryCategory, identifier);
		} catch (Exception e) {

			LOG.error("titan_repository error:{}" ,e.getMessage());

			return false;
		}

		if(id != null && id > 0){
			return true;
		}

		return false;
	}

	/**
	 * 判断源资源是否存在
	 *
	 * @param resType 资源种类
	 * @param resId 源资源id
	 * @param type  源资源类型
	 * @since
	 */
	public static void resourceExistByTitan(String resType, String resId, String type) {
		boolean flag = false;
		try {
			flag = checkEducationExistInTitan(resType, resId);
		} catch (Exception e) {
			if (ResourceType.RESOURCE_SOURCE.equals(type)) {
				LOG.error("源资源:" + resType + "--" + resId + "未找到", e);
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
						LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
						e.getMessage());
			}
			if (ResourceType.RESOURCE_TARGET.equals(type)) {
				LOG.error("目标资源:" + resType + "--" + resId + "未找到", e);
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
						LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
						e.getMessage());
			}
		}

		// 资源不存在,抛出异常
		if (!flag) {
			if (ResourceType.RESOURCE_SOURCE.equals(type)) {
				LOG.error("源资源:" + resType + "--" + resId + "未找到");
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
						LifeCircleErrorMessageMapper.SourceResourceNotFond.getCode(),
						"源资源:" + resType + "--" + resId + "未找到");
			}
			if (ResourceType.RESOURCE_TARGET.equals(type)) {

				LOG.error("目标资源:" + resType + "--" + resId + "未找到");

				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
						LifeCircleErrorMessageMapper.TargetResourceNotFond.getCode(),
						"目标资源:" + resType + "--" + resId + "未找到");
			}
		}
	}
	/**
	 * 判断源资源是否存在
	 * 
	 * @param resType 资源种类
	 * @param resId 源资源id
	 * @param type  源资源类型
	 * @since
	 */
	public static void resourceExist(String resType, String resId, String type) {
        EspEntity flag = null;
        try {
            /*
             * 调用各个资源的获取详细方法,用于判断对应资源是否存在, 若不存在,则抛出异常
             */
            EspRepository<?> resourceRepository = ServicesManager.get(resType);
            flag = resourceRepository.get(resId);
        } catch (EspStoreException e) {
            if (ResourceType.RESOURCE_SOURCE.equals(type)) {

                LOG.error("源资源:" + resType + "--" + resId + "未找到", e);

                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                              e.getMessage());
            }
            if (ResourceType.RESOURCE_TARGET.equals(type)) {

                LOG.error("目标资源:" + resType + "--" + resId + "未找到", e);

                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                              e.getMessage());
            }
        }

        // 资源不存在,抛出异常
        if (flag == null || ((flag instanceof Education) && ((Education) flag).getEnable() != null
                && !((Education) flag).getEnable())|| !((Education)flag).getPrimaryCategory().equals(resType)) {
            if (ResourceType.RESOURCE_SOURCE.equals(type)) {

                LOG.error("源资源:" + resType + "--" + resId + "未找到");

                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              LifeCircleErrorMessageMapper.SourceResourceNotFond.getCode(),
                                              "源资源:" + resType + "--" + resId + "未找到");
            }
            if (ResourceType.RESOURCE_TARGET.equals(type)) {

                LOG.error("目标资源:" + resType + "--" + resId + "未找到");

                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              LifeCircleErrorMessageMapper.TargetResourceNotFond.getCode(),
                                              "目标资源:" + resType + "--" + resId + "未找到");
            }
        }
    }
	
	/**
     * 判断源资源是否存在--不存在时不抛异常
     * 
     * @param resType 资源种类
     * @param resId 源资源id
     * @param type  源资源类型
     * @since
     */
    public static boolean resourceExistNoException(String resType, String resId, String type) {
        EspEntity flag = null;
        try {
            /*
             * 调用各个资源的获取详细方法,用于判断对应资源是否存在, 若不存在,则抛出异常
             */
            EspRepository<?> resourceRepository = ServicesManager.get(resType);
            flag = resourceRepository.get(resId);
        } catch (EspStoreException e) {
            if (ResourceType.RESOURCE_SOURCE.equals(type)) {

                LOG.error("源资源:" + resType + "--" + resId + "获取失败", e);

                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                              e.getMessage());
            }
            if (ResourceType.RESOURCE_TARGET.equals(type)) {

                LOG.error("目标资源:" + resType + "--" + resId + "获取失败", e);

                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                              e.getMessage());
            }
        }

        // 资源不存在,抛出异常
        if (flag == null || ((flag instanceof Education) && ((Education) flag).getEnable() != null
                && !((Education) flag).getEnable()) || !((Education)flag).getPrimaryCategory().equals(resType)) {
            return false;
        }
        
        return true;
    }
	
    /**
     * 更新资源生命周期的状态属性
     * 
     * @author linsm
     * @param resourceType
     * @param uuid
     * @param status
     * @since
     */
	@Deprecated
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void updateStatusInDB(String resType, String uuid, String status) {
        // 获取通用SDK仓库
        EspRepository espRepository = ServicesManager.get(resType);
        if (espRepository == null) {
            return;
        }
        EspEntity espEntity = getResourceEntity(resType, uuid);
        if (espEntity != null && espEntity instanceof Education) {
            Education education = (Education) espEntity;
            education.setStatus(status);
            try {
                espRepository.update(education);
                
                LOG.info("资源:" + resType + "--" + uuid + "--更新成功--status--"+ status );
                
            } catch (EspStoreException e) {
                
                LOG.error("资源:" + resType + "--" + uuid + "更新失败", e);

                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                              e.getMessage());
            }
        }
    }
	
    /**
     * 将Chapter 转换成ChapterViewModel
     * 
     * @author linsm
     * @param chapter
     * @return
     * @since
     */
    @SuppressWarnings("unchecked")
	public static ChapterViewModel changeChapterToChapterViewModel(Chapter chapter) {
        ChapterViewModel chapterViewModel = new ChapterViewModel();
        chapterViewModel.setIdentifier(chapter.getIdentifier());
        chapterViewModel.setDescription(chapter.getDescription());
        chapterViewModel.setParent(chapter.getParent());
        chapterViewModel.setTeachingMaterial(chapter.getTeachingMaterial());
        chapterViewModel.setTitle(chapter.getTitle());
        chapterViewModel.setDbcreateTime(chapter.getDbcreateTime());
        chapterViewModel.setTags(ObjectUtils.fromJson(chapter.getDbtags(), List.class));
        return chapterViewModel;
    }

    /**
     * 取dao层数据
     * 
     * @author linsm
     * @param resType
     * @param uuid
     * @return
     * @since
     */
	@Deprecated
    @SuppressWarnings("rawtypes")
    public static EspEntity getResourceEntity(String resType, String uuid) {
        // 获取通用SDK仓库
        EspRepository espRepository = ServicesManager.get(resType);
        EspEntity espEntity = null;
        if (espRepository != null) {
            try {
                espEntity = espRepository.get(uuid);
            } catch (EspStoreException e) {

                LOG.error("资源:" + resType + "--" + uuid + "未找到", e);

                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                              e.getMessage());
            }
        }
        return espEntity;
    }
	
	/**
	 * 将techInfo中的href值或者source值互相拷贝，即如果href或source只有一个有值时将另一个没有值的key，设置为相同的值
	 * 如href有值，source没有值，则将href的值重新赋值给source
	 * 
	 * @author:xuzy
	 * @date:2015年11月18日
	 * @param techInfoMap
	 * @return
	 */
	public static boolean copyTechInfoValue(Map techInfoMap){
		boolean flag = false;
		if(techInfoMap != null){
			if(techInfoMap.containsKey(HREF) && !techInfoMap.containsKey(SOURCE)){
				Object ti =  techInfoMap.get(HREF);
				techInfoMap.put(SOURCE, ti);
				flag = true;
			}else if(!techInfoMap.containsKey(HREF) && techInfoMap.containsKey(SOURCE)){
				ResTechInfoViewModel ti = new ResTechInfoViewModel();
				BeanUtils.copyProperties(techInfoMap.get(SOURCE), ti);
				((ResTechInfoViewModel)ti).setRequirements(null);
				techInfoMap.put(HREF, ti);
				flag = true;
			}
		}
		return flag;
	}


	//preview key排序 by liuwx 20151208
	final static Comparator<String> RESOURCE_PREIVEW_PREFIX_COMPARATOR=new Comparator<String>() {
		String searchStr=Constant.RESOURCE_PREIVEW_PREFIX;
		@Override
		public int compare(String key2, String key1) {
			int index=0;
			if((index=hasSilde(key1,key2))>-1){
				int searchIndex=index+searchStr.length();
				try {
					return Integer.valueOf(key2.substring(searchIndex)).compareTo(Integer.valueOf(key1.substring(searchIndex)));
				}catch (Exception e){
					return key2.compareTo(key1);
				}

			}
			return key2.compareTo(key1);
		}
		//设计原则上，使用boolean会更好
		private int hasSilde(String key1,String key2){
			int index1=key1.indexOf(searchStr);
			int index2=key2.indexOf(searchStr);
			return index1>-1?index2:index1; //返回index，减少一次查询
			//  return index1>-1 &&index2>-1;

		}

	} ;
	
	/**
	 * 限制limit分页参数的最大size
	 * <p>Create Time: 2016年1月26日   </p>
	 * <p>Create author: xiezy   </p>
	 * @param limit
	 * @return
	 */
	public static String checkLimitMaxSize(String limit){
	    Integer[] result = ParamCheckUtil.checkLimit(limit);
	    if(result[1] > Constant.MAX_LIMIT){
	        return "(" + result[0] + "," + Constant.MAX_LIMIT + ")";
	    }
	    
        return limit;
	}

	/**
	 *
	 * @param words
	 * @return
     */
	public static String checkWordSegmentation(String words) {
		if (words == null || "".equals(words)) return "";
		words = checkBlank(words);//检查空格
		if(!checkBrackets(words)){//检查括号
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					LifeCircleErrorMessageMapper.CommonSearchParamError.getCode(),
					words + "--words格式错误,括号不对");
		}
		// 去掉最外层"()"
		if (words.startsWith("(")) {
			if (words.endsWith(")")) {
				words = words.substring(1, words.length() - 1).trim();
			}
		}
		String check = words.replaceAll("\\)", "").replaceAll("\\(", "").trim();
		if (check.contains(",")) {
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					LifeCircleErrorMessageMapper.CommonSearchParamError.getCode(),
					words + "--words格式错误,不支持多个words");
		}
		if (check.endsWith(" AND") || check.endsWith(" OR") || check.endsWith(" and") || check.endsWith(" or")) {
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					LifeCircleErrorMessageMapper.CommonSearchParamError.getCode(),
					words + "--words格式错误,布尔操作符不能出现在结尾");
		}
		if (check.startsWith("AND ") || check.startsWith("OR ") || check.startsWith("and ") || check.startsWith("or ")) {
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					LifeCircleErrorMessageMapper.CommonSearchParamError.getCode(),
					words + "--words格式错误,布尔操作符不能出现在开头");
		}
		// 操作符转化
		words = words.replaceAll(" or ", " OR ").replaceAll(" and ", " AND ");
		if (words.contains(" OR ")) {
			if(!checkOptNum(words, " OR ")){
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
						LifeCircleErrorMessageMapper.CommonSearchParamError.getCode(),
						words + "--words格式错误,检查OR逻辑是否出错");
			}
		}

		if (words.contains(" AND ")) {
			if(!checkOptNum(words, " AND ")){
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
						LifeCircleErrorMessageMapper.CommonSearchParamError.getCode(),
						words + "--words格式错误,检查AND逻辑是否出错");
			}
		}

		return words;
	}

	/**
	 * 检查操作符
	 * @param words
	 * @param opt
     * @return
     */
	public static boolean checkOptNum(String words, String opt) {
		if (words == null) return true;
		int total = getSubStrAppearTimes(words,opt);
		if (total == words.split(opt).length - 1) {
			return true;
		}
		return false;
	}

	/**
	 * 检查括号
	 * @param words
	 * @return
     */
	public static boolean checkBrackets(String words) {
		if (words == null) return true;
		int left = getSubStrAppearTimes(words, "(");
		int right = getSubStrAppearTimes(words, ")");
		if (left == right) return true;
		return false;
	}

	/**
	 * 多个连续空格变成单个空格
	 * @param str
	 * @return
     */
	public static String checkBlank(String str) {
		if (str == null) return "";
		return str.replaceAll("\\s{1,}", " ");
	}

	/**
	 * 取字符在字符串中出现的次数
	 * @param str
	 * @param subStr
     * @return
     */
	public static int getSubStrAppearTimes(String str, String subStr) {
		if (str == null) return 0;
		int total = 0;
		for (String tmp = str; tmp.length() >= subStr.length(); ) {
			if (tmp.indexOf(subStr) == 0) {
				total++;
			}
			tmp = tmp.substring(1);
		}
		return total;
	}
	
	/**
     * 返回一个hashMap
     * <p>Create Time: 2016年3月1日   </p>
     * <p>Create author: xiezy   </p>
     * @return
     */
    public static <K,V> Map<K, V> newHashMap(){
        return new HashMap<K, V>();
    }
    
    /**
     * 将json转换为IvcConfigModel
     * @param json
     * @return
     */
    public static IvcConfigModel convertJson2IvcConfig(String json){
    	Map<String,Object> map = null;
    	JacksonCustomObjectMapper mapper = new JacksonCustomObjectMapper();
    	try {
			map = mapper.readValue(json,Map.class);
		} catch (JsonParseException e) {
			LOG.error("转换IvcConfigModel出错",e);
		} catch (JsonMappingException e) {
			LOG.error("转换IvcConfigModel出错",e);
		} catch (IOException e) {
			LOG.error("转换IvcConfigModel出错",e);
		}
    	
    	if(map == null){
    		return null;
    	}
    	
    	IvcConfigModel icm = new IvcConfigModel();
    	Set<String> keySet = map.keySet();
    	try {
	    	for (String key : keySet) {
	    		if(key.startsWith("+")){
	    			Map<String,Object> value = (Map)map.get(key);
	    			if(icm.getIncludeUrlList() == null){
	    				icm.setIncludeUrlList(new ArrayList<IvcUrlModel>());
	    			}
	    			IvcUrlModel ium = new IvcUrlModel();
	    			String url = key.split("\\|")[1];
	    			List<String> method = (List)value.get("method");
	    			
	    			ium.setUrl(url);
	    			ium.setMethod(method);
	    			
	    			if(value.containsKey("max_rps") || value.containsKey("max_dpr")){
	    				IvcLoadModel ilm = new IvcLoadModel();
	    				if(value.get("max_rps") != null){
	    					ilm.setMaxRps(Long.valueOf(value.get("max_rps").toString()));
	    				}
	    				if(value.get("max_dpr") != null){
	    					ilm.setMaxDpr(Long.valueOf(value.get("max_dpr").toString()));
	    				} 
	    				ium.setLoad(ilm);
	    			}
	    			icm.getIncludeUrlList().add(ium);
	    			
	    		}else if(key.startsWith("-")){
	    			Map<String,Object> value = (Map)map.get(key);
	    			if(icm.getExcludeUrlList() == null){
	    				icm.setExcludeUrlList(new ArrayList<IvcUrlModel>());
	    			}
	    			IvcUrlModel ium = new IvcUrlModel();
	    			String url = key.split("\\|")[1];
	    			List<String> method = (List)value.get("method");
	    			
	    			ium.setUrl(url);
	    			ium.setMethod(method);
	    			icm.getExcludeUrlList().add(ium);
	    			
	    		}else if(key.equals("global_ips")){
					List<String> value = (List)map.get(key);
					icm.setGlobalIps(value);
				}else if(key.equals("global_load")){
					Map<String,Object> value = (Map)map.get(key);
					if(icm.getGlobalLoad() == null){
						icm.setGlobalLoad(new IvcLoadModel());
					}
					if(value.get("max_rps") != null){
						icm.getGlobalLoad().setMaxRps(Long.valueOf(value.get("max_rps").toString()));
					}
					if(value.get("max_dpr") != null){
						icm.getGlobalLoad().setMaxDpr(Long.valueOf(value.get("max_dpr").toString()));
					}
				}else if(key.equals("global_coverage")){
					List<String> value = (List)map.get(key);
					icm.setGlobalCoverage(value);
				}else if(key.equals("global_category")){
					IvcGlobalCategoryModel igcm = new IvcGlobalCategoryModel();
					Map<String,Object> value = (Map)map.get(key);
					Set<String> k = value.keySet();
					for (String s : k) {
						if(s.endsWith("+")){
							if(s.startsWith("res_type")){
								List<String> includeResType = (List)value.get(s);
								igcm.setIncludeResType(includeResType);
							}else if(s.startsWith("other_type")){
								List<String> includeOtherType = (List)value.get(s);
								igcm.setIncludeOtherType(includeOtherType);
							}
						}else if(s.endsWith("-")){
							if(s.startsWith("res_type")){
								List<String> excludeResType = (List)value.get(s);
								igcm.setExcludeResType(excludeResType);
							}else if(s.startsWith("other_type")){
								List<String> excludeOtherType = (List)value.get(s);
								igcm.setExcludeOtherType(excludeOtherType);
							}
						}
					}
					
					icm.setGlobalCategory(igcm);
				}
			}
    	} catch (Exception e) {
    		LOG.error("转换IvcConfigModel出错",e);
    	}
    	
		return icm;
    	
    }
	
	/**
     * 通过sql及参数获得hashcode   
     * <p>Create Time: 2016年1月14日   </p>
     * <p>Create author: xuzy   </p>
     * @param sql
     * @param paramMap
     * @return
     */
	public static int getHashCodeKey(String sql,Map<String,Object> paramMap){
        sortMap(paramMap);
        int sqlHashCode = sql.hashCode();
        int paramHashCode = paramMap.hashCode();
        int hashCode = calcHashCodeKey(sqlHashCode,paramHashCode);
        return hashCode;
    }
    
    /**
     * 计算hashcode   
     * <p>Create Time: 2016年1月14日   </p>
     * <p>Create author: xuzy   </p>
     * @param sqlHashCode
     * @param paramHashCode
     * @return
     */
	public static int calcHashCodeKey(int sqlHashCode,int paramHashCode){
        return sqlHashCode * 13 + paramHashCode;
    }
    
    /**
     * 对sql的参数进行排序处理,目的是让参数的顺序不影响hashcode的计算    
     * <p>Create Time: 2016年1月14日   </p>
     * <p>Create author: xuzy   </p>
     * @param map
     */
	public static void sortMap(Map<String,Object> map){
        Set<String> keySet = map.keySet();
        Iterator<String> it =  keySet.iterator();
        while(it.hasNext()){
            String key = it.next();
            Object value = map.get(key);
            if(value instanceof List){
                Collections.sort((List) value);
                map.put(key, value);
            }
        }
    }

	public static void includeFilter(List<String> includes, String primaryCategory){
		if(ResourceNdCode.instructionalobjectives.toString().equals(primaryCategory)||
				ResourceNdCode.knowledges.toString().equals(primaryCategory)){
			includes.remove(IncludesConstant.INCLUDE_EDU);
		}
	}
	
	/**
	 * MD5加密
	 * @author xiezy
	 * @date 2016年8月29日
	 * @param plainText
	 * @return
	 */
	public static String encryptToMD5(String plainText) {
		try {
			// 生成实现指定摘要算法的 MessageDigest 对象。
			MessageDigest md = MessageDigest.getInstance("MD5");
			// 使用指定的字节数组更新摘要。
			md.update(plainText.getBytes());
			// 通过执行诸如填充之类的最终操作完成哈希计算。
			byte b[] = md.digest();
			// 生成具体的md5密码到buf数组
			int i;
			StringBuffer buf = new StringBuffer("");
			for (int offset = 0; offset < b.length; offset++) {
				i = b[offset];
				if (i < 0)
					i += 256;
				if (i < 16)
					buf.append("0");
				buf.append(Integer.toHexString(i));
			}

			return buf.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					LifeCircleErrorMessageMapper.EncryptDataFail.getCode(),
					"加密MD5异常!");
		}
	}
}
