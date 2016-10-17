package nd.esp.service.lifecycle.educommon.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import nd.esp.service.lifecycle.app.LifeCircleApplicationInitializer;
import nd.esp.service.lifecycle.educommon.vos.ResCoverageViewModel;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.vos.statics.CoverageConstant;
import nd.esp.service.lifecycle.vos.statics.ResourceType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

/**
 * 参数校验帮助类
 * <p>Create Time: 2015年9月28日           </p>
 * @author xiezy
 */
public class ParameterVerificationHelper {
    private static final Logger LOG = LoggerFactory.getLogger(ParameterVerificationHelper.class);
    
    /**
     * Coverage的校验	
     * <p>Create Time: 2015年9月28日   </p>
     * <p>Create author: xiezy   </p>
     * @param coverage
     * @return
     */
    public static String coverageVerification(String coverage){
    	//对于入参的coverage每个在最后追加一个空格，以保证elemnt的size为3,用于支持Org/LOL/的模糊查询
        coverage = coverage + " ";
        List<String> elements = Arrays.asList(coverage.split("/"));
        //格式错误判断
        if(elements.size() != 3 || elements.get(0).trim().equals("") || elements.get(1).trim().equals("")){
            
            LOG.error(coverage + "--coverage格式错误");
           
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CommonSearchParamError.getCode(),
                    coverage + "--coverage格式错误");
        }

        //覆盖范围参数处理
        String ctType = elements.get(0).trim();
        String cTarget = elements.get(1).trim();
        String ct = elements.get(2).trim();
        
        if(!CoverageConstant.isCoverageTargetType(ctType,true)){
            
            LOG.error("覆盖范围类型不在可选范围内");
           
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CoverageTargetTypeNotExist);
        }
        
        if(StringUtils.isEmpty(ct)){
        	ct = "*";
        }else{
        	if(!CoverageConstant.isCoverageStrategy(ct,true)){
                
                LOG.error("资源操作类型不在可选范围内");
                
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        LifeCircleErrorMessageMapper.CoverageStrategyNotExist);
            }
        }
        
        return ctType + "/" + cTarget + "/" + ct;
    }
    
    /**
     * 关系参数校验
     * @author xiezy
     * @date 2016年7月13日
     * @param relation
     * @return
     */
    public static Map<String, String> relationVerification(String relation, QueryType queryType){
    	Map<String,String> map = new HashMap<String, String>();
        //对于入参的relation每个在最后追加一个空格，以保证elemnt的size为3
        relation = relation + " ";
        List<String> elements = Arrays.asList(relation.split("/"));
        //格式错误判断
        if(elements.size() != 3){
           
            LOG.error(relation + "--relation格式错误");
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CommonSearchParamError.getCode(),
                    relation + "--relation格式错误");
        }
        
        String resourceType = elements.get(0).trim();
        String resourceUuid = elements.get(1).trim();
        String relationType = elements.get(2).trim();
         
        //判断源资源是否存在,stype + suuid
        if(!resourceUuid.endsWith("$")){//不为递归查询时才校验
            if(queryType == QueryType.DB) {
                CommonHelper.resourceExist(resourceType, resourceUuid, ResourceType.RESOURCE_SOURCE);
            }
        }else{
            // "relation参数进行递归查询时,目前仅支持:chapters,knowledges"
            if (!ResourceNdCode.chapters.toString().equals(elements.get(0)) &&
                    !ResourceNdCode.knowledges.toString().equals(elements.get(0))) {
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        LifeCircleErrorMessageMapper.CommonSearchParamError.getCode(),
                        "relation参数进行递归查询时,目前仅支持:chapters,knowledges");
            }

        }
        //r_type的特殊处理
        if(StringUtils.isEmpty(relationType) || RelationType.shouldBeAssociate(relationType)){
            relationType = RelationType.ASSOCIATE.getName();
        }
        
        map.put("stype", resourceType);
        map.put("suuid", resourceUuid);
        map.put("rtype", relationType);
        
        return map;
    }

    /**
     *
     * 转换成ResCoverageViewModel对象
     * @param coverage
     * @return
     */
    public static ResCoverageViewModel convertResCoverageViewModel(String coverage){

        ResCoverageViewModel model =new ResCoverageViewModel();
        if(StringUtils.hasText(coverage)){
            String coverageArr[]=coverage.split("/");
            model.setTarget(coverageArr[1]);
            model.setTargetType(coverageArr[0]);
            model.setStrategy(coverageArr[2]);
        }
        return model;
    }
    
    /**
     * 判断是否是时间范围查询
     * <p>Create Time: 2016年3月31日   </p>
     * <p>Create author: xiezy   </p>
     */
    public static boolean isRangeQuery(String prop){
    	if(prop.startsWith("create_time") || prop.startsWith("lastupdate")){
    		return true;
    	}
    	return false;
    }
    
    /**
     * 针对category K12模式path自动添加 【K12/】的适配
     * <p>Description:              </p>
     * <p>Create Time: 2015年9月15日   </p>
     * <p>Create author: xiezy   </p>
     * @param resType
     * @param categories
     * @return
     */
	public static Set<String> doAdapterCategories4DB(Set<String> categories){
        Set<String> afterDeal = new HashSet<String>();
        
        for(String category : categories){
            //1.将category中的*去掉
//            category = category.replaceAll("\\*", "");//去掉,是为了支持path模糊匹配
            
            //2.category为path时特殊处理
            if(category!=null && category.contains("/")){
            	String categoryPattern = category.split("/")[0];
//            	if(!StaticDatas.CATEGORY_PATTERN_MAP.containsKey(categoryPattern)){
            	if(categoryPattern.startsWith("$O")){
            		category = "K12/" + category;
            	}
            }
            //3.加入处理后的结果集
            afterDeal.add(category);
        }
        
        return afterDeal;
    }
	
	/**
	 * 适配101ppt前端维度
	 * @author xiezy
	 * @date 2016年10月10日
	 * @param categories
	 * @return
	 */
	public static Set<String> doAdapterCategories4101ppt(Set<String> categories){
		Set<String> afterDeal = new HashSet<String>();
		
		if(CollectionUtils.isNotEmpty(categories)){
			Properties properties = LifeCircleApplicationInitializer.ndppt_frontend_properties;
			
			for(String category : categories){
				if(StringUtils.isNotEmpty(category) 
						&& !category.contains("/") && category.contains("PF")){//需要处理的情况
					if(category.contains(" and ")){
						List<String> categoryAndOp = Arrays.asList(category.split(" and "));
	                    categoryAndOp = CollectionUtils.removeEmptyDeep(categoryAndOp);// 主要是为了防止 A and B and 的情况
					
	                    if(CollectionUtils.isNotEmpty(categoryAndOp)){
	                    	List<List<String>> values = new ArrayList<List<String>>();
	                    	for(String cg : categoryAndOp){
	                    		List<String> innerValues = new ArrayList<String>();
	                    		if(properties.containsKey(cg)){
	                    			String ukCategory = properties.getProperty(cg);
	                    			if(StringUtils.hasText(ukCategory)){
	                    				innerValues.addAll(Arrays.asList(ukCategory.split(",")));
	                    			}else{
	                    				values = null;
	                    				break;
	                    			}
	                    		}else{
	                    			innerValues.add(cg);
	                    		}
	                    		
	                    		values.add(innerValues);
	                    	}
	                    	
	                    	if(CollectionUtils.isNotEmpty(values)){
	                    		afterDeal.addAll(combine(values));
	                    	}
	                    }
					}else{//单个code的情况
						if(properties.containsKey(category)){
							String ukCategory = properties.getProperty(category);
							if(StringUtils.hasText(ukCategory)){//没有对应UK维度的直接过滤掉
								afterDeal.addAll(Arrays.asList(ukCategory.split(",")));
							}
						}else{//不是PF合法code,直接传递
							afterDeal.add(category);
						}
					}
				}else{//不处理,直接传递
					afterDeal.add(category);
				}
			}
		}
		
		return afterDeal;
	}
	
	/**
	 * 递归处理and category -- 101ppt前端维度替换需要
	 * @author xiezy
	 * @date 2016年10月10日
	 * @param values
	 * @return
	 */
	public static List<String> combine(List<List<String>> values) {
		int size = values.size();
		if (size == 1) {
			return values.get(0);
		}
		List<String> firstValueList = values.remove(size - 1);
		List<String> secondValueList = values.remove(size - 2);
		List<String> result = new ArrayList<String>();
		for (String a : firstValueList) {
			for (String b : secondValueList) {
				result.add(a + " and " + b);
			}
		}
		values.add(result);
		return combine(values);
	}
}
