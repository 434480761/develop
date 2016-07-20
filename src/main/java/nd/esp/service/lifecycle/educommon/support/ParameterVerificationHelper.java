package nd.esp.service.lifecycle.educommon.support;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nd.esp.service.lifecycle.educommon.controllers.NDResourceController;
import nd.esp.service.lifecycle.educommon.vos.ResCoverageViewModel;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;
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
    public static Map<String, String> relationVerification(String relation, NDResourceController.QueryType queryType){
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
            if(queryType == NDResourceController.QueryType.DB) {
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
}
