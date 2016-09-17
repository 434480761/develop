package nd.esp.service.lifecycle.educommon.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import nd.esp.service.lifecycle.app.LifeCircleApplicationInitializer;
import nd.esp.service.lifecycle.educommon.services.NDResourceStatisticsService;
import nd.esp.service.lifecycle.educommon.vos.constant.TimeUnitConstant;
import nd.esp.service.lifecycle.educommon.vos.statistics.ResourceStatisticsViewModel;
import nd.esp.service.lifecycle.educommon.vos.statistics.StatisticsByDayViewModel;
import nd.esp.service.lifecycle.educommon.vos.statistics.TimingStatisticsViewModel;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.ParamCheckUtil;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.vos.statics.CoverageConstant;
import nd.esp.service.lifecycle.vos.statics.ResourceType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 资源统计 
 * <p>Create Time: 2015年8月15日           </p>
 * @author xiezy
 */
@RestController
//@RequestMapping("/v0.6/resources/actions/statistics")
public class NDResourceStatisticsController {
	private static final Logger LOG = LoggerFactory.getLogger(NDResourceStatisticsController.class);
    
    @Autowired
    private NDResourceStatisticsService ndResourceStatisticsService;

    /**
     * 资源类型统计接口	
     * <p>Create Time: 2015年8月15日   </p>
     * <p>Create author: xiezy   </p>
     * @param categoryPath      分类为维度的路径，这里特指学段，学科，版本，教材等。路径需要以模式名称开始
     * @param resourceTypes     资源类型的编码
     * @param relations         根据资源关系进行查询统计
     * @param coverages         按照覆盖范围进行统计
     * @param props             根据属性定制化统计条件
     */
    @RequestMapping(value = "", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE },params = {"resource_type"})
    public List<ResourceStatisticsViewModel> resourceStatistics(
            @RequestParam(required=false,value="category_path") String categoryPath,
            @RequestParam(value="resource_type") Set<String> resourceTypes,
            @RequestParam(required=false) String relation,
            @RequestParam(required=false,value="coverage") Set<String> coverages,
            @RequestParam(required=false,value="prop") List<String> props){
        /*
         * 入参处理 + 校验
         */
        //先将参数中的4个Set中的null和""去掉
        resourceTypes = CollectionUtils.removeEmptyDeep(resourceTypes);
        coverages = CollectionUtils.removeEmptyDeep(coverages);
        props = CollectionUtils.removeEmptyDeep(props);
        
//        // 0.categoryPath
//        if(StringUtils.isEmpty(categoryPath)){
//            LOG.error("category_path不能为空");
//            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
//                    LifeCircleErrorMessageMapper.CommonStatisticsParamError.getCode(),
//                    "category_path不能为空");
//        }
        
        // 1.resourceTypes
        if(CollectionUtils.isEmpty(resourceTypes)){
            LOG.error("resource_type至少要有一个");
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CommonStatisticsParamError.getCode(),
                    "resource_type至少要有一个");
        }else{//对通配进行处理
            Set<String> needRemove = new HashSet<String>();
            Set<String> adapterCodes = new HashSet<String>();
            
            for(String resType : resourceTypes){
                if(resType!=null && resType.contains("*")){//对带有*的进行处理
                    if((resType.indexOf("*")==resType.lastIndexOf("*")) && 
                        resType.endsWith("*") && resType.length()>=6 && resType.length()<=7 ){
                        
                        Set<Object> allCodes = LifeCircleApplicationInitializer.ndCode_properties.keySet();
                        String compareStr = resType.replaceAll("\\*", "");
                        for(Object code : allCodes){
                            String key = (String)code;
                            if(key.startsWith(compareStr) && !key.endsWith("00")){
                                //符合通配,加入到通配集合
                                adapterCodes.add(key);
                            }
                        }
                        
                        //将带*的加到待删除集合中
                        needRemove.add(resType);
                    }else{
                        throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                LifeCircleErrorMessageMapper.CommonStatisticsParamError.getCode(),
                                "resource_type使用通配符时仅支持在同一资源时查询多个小类使用");
                    }
                }
            }
            
            //将带*的删除
            if(CollectionUtils.isNotEmpty(needRemove)){
                resourceTypes.removeAll(needRemove);
            }
            //符合通配的加入
            if(CollectionUtils.isNotEmpty(adapterCodes)){
                resourceTypes.addAll(adapterCodes);
            }
        }
        
        // 2.relations,格式:stype/suuid/r_type
        Map<String,String> relationsMap = new HashMap<String, String>();
        if(StringUtils.isEmpty(relation)){
            relationsMap = null;
        }else{ 
            List<String> elements = Arrays.asList(relation.split("/"));
            // 格式错误判断
            if (elements.size() != 3) {
                LOG.error(relation + "--relation格式错误");
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        LifeCircleErrorMessageMapper.CommonStatisticsParamError.getCode(), relation + "--relation格式错误");
            }
            
            // 判断源资源是否存在,stype + suuid
            CommonHelper.resourceExist(elements.get(0), elements.get(1), ResourceType.RESOURCE_SOURCE);
            
            // r_type的特殊处理
            if (StringUtils.isEmpty(elements.get(2))) {
                elements.set(2, null);
            }
            
            relationsMap.put("stype", elements.get(0));
            relationsMap.put("suuid", elements.get(1));
            relationsMap.put("rtype", elements.get(2));
        }
        
        // 3.coverages,格式:Org/uuid/SHAREING
        List<String> coveragesList = new ArrayList<String>();
        if(CollectionUtils.isEmpty(coverages)){
            coveragesList = null;
        }else{
            for(String coverage : coverages){
                //对于入参的coverage每个在最后追加一个空格，以保证elemnt的size为3,用于支持Org/LOL/的模糊查询
                coverage = coverage + " ";
                List<String> elements = Arrays.asList(coverage.split("/"));
                //格式错误判断
                if(elements.size() != 3){
                    LOG.error(coverage + "--coverage格式错误");
                    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                            LifeCircleErrorMessageMapper.CommonSearchParamError.getCode(),
                            coverage + "--coverage格式错误");
                }

                //覆盖范围参数处理
                String ctType = elements.get(0).trim();
                String cTarget = elements.get(1).trim();
                String ct = elements.get(2).trim();
                
                if(StringUtils.isEmpty(ctType)){
                    ctType = "*";
                }else{
                    if(!CoverageConstant.isCoverageTargetType(ctType,true)){
                        LOG.error("覆盖范围类型不在可选范围内");
                        throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                LifeCircleErrorMessageMapper.CoverageTargetTypeNotExist);
                    }
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
                if(StringUtils.isEmpty(cTarget)){
                    cTarget = "*";
                }
                String c = ctType + "/" + cTarget + "/" + ct;
                if(c.equals("*/*/*")){
                    continue;
                }
                
                coveragesList.add(c);
            }
        }
        
        // 4.props,语法 [属性] [操作] [值],目前仅支持eq和in操作
        Map<String,Set<String>> propsMap = new HashMap<String, Set<String>>();
        //获取props的.properties文件,目的是筛选匹配支持的属性
        Properties properties = null;
        properties = LifeCircleApplicationInitializer.props_properties_db;
                
        if(CollectionUtils.isEmpty(props)){
            propsMap = null;
        }else{
            for(String prop : props){
                if(prop.contains(" eq ")){
                    List<String> elements = Arrays.asList(prop.split(" eq "));
                    //格式错误判断
                    if(elements.size() != 2 || StringUtils.isEmpty(elements.get(0)) || StringUtils.isEmpty(elements.get(1))){
                        LOG.error(prop + "--prop格式错误");
                        throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                LifeCircleErrorMessageMapper.CommonStatisticsParamError.getCode(),
                                prop + "--prop格式错误");
                    }
                   
                    if(propsMap.containsKey(properties.getProperty(elements.get(0)))){//已存在该属性
                        Set<String> propValues = propsMap.get(properties.getProperty(elements.get(0)));
                        propValues.add(elements.get(1));
                    }else{//新属性
                        if(properties.containsKey(elements.get(0))){
                            Set<String> propValuesNew = new HashSet<String>();
                            propValuesNew.add(elements.get(1));
                            propsMap.put(properties.getProperty(elements.get(0)), propValuesNew);
                        }else{
                            LOG.error(prop + ":" + elements.get(0) + "--不支持该属性查询 OR 属性名错误(驼峰形式)");
                            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                    LifeCircleErrorMessageMapper.CommonStatisticsParamError.getCode(),
                                    prop + ":" + elements.get(0) + "--不支持该属性查询 OR 属性名错误(驼峰形式)");
                        }
                    }
                }else if(prop.contains(" in ")){
                    List<String> elements = Arrays.asList(prop.split(" in "));
                    //格式错误判断
                    if(elements.size() != 2 || StringUtils.isEmpty(elements.get(0)) || StringUtils.isEmpty(elements.get(1))){
                        LOG.error(prop + "--prop格式错误");
                        throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                LifeCircleErrorMessageMapper.CommonStatisticsParamError.getCode(),
                                prop + "--prop格式错误");
                    }
                   
                    if(propsMap.containsKey(properties.getProperty(elements.get(0)))){//已存在该属性
                        Set<String> propValues = propsMap.get(properties.getProperty(elements.get(0)));
                        propValues.addAll(Arrays.asList(elements.get(1).split("\\|")));
                    }else{//新属性
                        if(properties.containsKey(elements.get(0))){
                            Set<String> propValuesNew = new HashSet<String>();
                            propValuesNew.addAll(Arrays.asList(elements.get(1).split("\\|")));
                            propsMap.put(properties.getProperty(elements.get(0)), propValuesNew);
                        }else{
                            LOG.error(prop + ":" + elements.get(0) + "--不支持该属性查询 OR 属性名错误(驼峰形式)");
                            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                    LifeCircleErrorMessageMapper.CommonStatisticsParamError.getCode(),
                                    prop + ":" + elements.get(0) + "--不支持该属性查询 OR 属性名错误(驼峰形式)");
                        }
                    }
                }else{
                    LOG.error(prop + "--prop目前仅支持eq和in操作");
                    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                            LifeCircleErrorMessageMapper.CommonStatisticsParamError.getCode(),
                            prop + "--prop目前仅支持eq和in操作");
                }
            }
        }
        
        //调用service
        return ndResourceStatisticsService.resourceStatistics
                (categoryPath, resourceTypes, relationsMap, coveragesList, propsMap);
    }
    
    /**
     * 资源类型的定时统计
     * <p>Create Time: 2015年8月15日   </p>
     * <p>Create author: xiezy   </p>
     * @param resourceTypes     资源类型的编码
     * @param timeUnit          支持none、day、mouth、year，默认是day。当为none的时候，返回资源类型的总数，只有一条记录，limit无效
     * @param limit             统计记录的数量
     */
    @RequestMapping(value = "/bytype", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE },params = {"resource_type"})
    public TimingStatisticsViewModel resourceTimingStatistics(
            @RequestParam(value="resource_type") Set<String> resourceTypes,
            @RequestParam(required=false,value="time_unit") String timeUnit,
            @RequestParam(required=false) String limit){
        /*
         * 参数校验
         */
        // 1.resourceTypes
        resourceTypes = CollectionUtils.removeEmptyDeep(resourceTypes);
        if(CollectionUtils.isEmpty(resourceTypes)){
            LOG.error("resource_type至少要有一个");
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.CommonStatisticsParamError.getCode(),
                    "resource_type至少要有一个");
        }
        
        // 2.timeUnit
        if(StringUtils.isNotEmpty(timeUnit)){
            if(!TimeUnitConstant.isValidTimeUnit(timeUnit)){
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        LifeCircleErrorMessageMapper.TimeUnitParamError.getCode(),
                        timeUnit + "不在规定范围内");
            }
        }else{//不传时,默认为day
            timeUnit = TimeUnitConstant.TIMEUNIT_DAY;
        }
        
        // 3.limit
        if(StringUtils.isEmpty(limit)){//不传时设置
            limit = "(0,1)";
        }
        Integer result[] = ParamCheckUtil.checkLimit(limit);
        
        //调用service
        TimingStatisticsViewModel tsvm = 
                ndResourceStatisticsService.resourceTimingStatistics(resourceTypes, timeUnit, result[0], result[1]);
        return tsvm;
    }
    
    /**
     * 资源类型每天的增量统计	
     * <p>Create Time: 2015年8月15日   </p>
     * <p>Create author: xiezy   </p>
     * @param limit     统计记录的数量
     */
    @RequestMapping(value = "/byday", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public StatisticsByDayViewModel resourceStatisticsByDay(@RequestParam(required=false) String limit){
        if(StringUtils.isEmpty(limit)){//不传时设置
            limit = "(0,1)";
        }
        Integer result[] = ParamCheckUtil.checkLimit(limit);
        
        //调用service
        StatisticsByDayViewModel sbdv = 
                ndResourceStatisticsService.resourceStatisticsByDay(result[0], result[1]);
        return sbdv;
    }
}
