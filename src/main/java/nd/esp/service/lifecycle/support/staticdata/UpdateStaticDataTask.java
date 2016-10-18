package nd.esp.service.lifecycle.support.staticdata;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import nd.esp.service.lifecycle.services.AdapterDBDataService;
import nd.esp.service.lifecycle.services.staticdatas.StaticDataService;
import nd.esp.service.lifecycle.utils.CollectionUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 修改静态变量任务
 * <p>Create Time: 2016年3月1日           </p>
 * @author xiezy
 */
@Component
public class UpdateStaticDataTask {
    private final static Logger LOG= LoggerFactory.getLogger(UpdateStaticDataTask.class);
    
    @Autowired
    private StaticDataService staticDataService;
    
    @Autowired
    private AdapterDBDataService adapterDBDataService;
    
    private static Long lastUpdateTime = 0L;
    private static Long lastUpdateTime4Ivc = 0L;
    private static Long lastUpdateTime4CP = 0L;
    
    public static int SWITCH_TASK_ID = 1;
    public static int IVC_TASK_ID = 2;
    public static int CP_TASK_ID = 3;
    
    /**
     * 静态变量更新任务
     */
//    @Scheduled(cron="0 25 17 * * ?")
    @Scheduled(fixedRate=60000)
    @PostConstruct
    public void runTask(){
        if(lastUpdateTime == 0){//项目刚启动,进行初始化
            lastUpdateTime = staticDataService.queryLastUpdateTime(SWITCH_TASK_ID);
            LOG.info("lastUpdateTime初始化");
            
            synchronizeDbStatusToStaticVariable();
            
            LOG.info("静态变量初始化");
        }else{
        	Long lastUpdateInDB = staticDataService.queryLastUpdateTime(SWITCH_TASK_ID);
            if(!lastUpdateInDB.equals(lastUpdateTime)){
            	
            	synchronizeDbStatusToStaticVariable();
                
                lastUpdateTime = lastUpdateInDB;
                LOG.info("静态变量更新--end!");
            }
        }
    }
    
    /**
     * 同步数据库状态到静态变量
     * @author xiezy
     * @date 2016年8月18日
     */
    private void synchronizeDbStatusToStaticVariable(){
    	//获取静态变量名和现在状态值
        List<Map<String, Integer>> list = staticDataService.queryNowStatus();
        if(CollectionUtils.isNotEmpty(list)){
            for(Map<String, Integer> map : list){
                for(String name : map.keySet()){
                	staticDataService.setValues(name, map.get(name));
                }
            }
        }
    }
    
    /**
     * 访问控制策略map更新任务
     */
//  @Scheduled(cron="0 7 15 * * ?")
    @Scheduled(fixedRate=60000)
    @PostConstruct
    public void runTask4Ivc(){
        if(lastUpdateTime4Ivc == 0){//项目刚启动,进行初始化
        	lastUpdateTime4Ivc = staticDataService.queryLastUpdateTime(IVC_TASK_ID);
            LOG.info("lastUpdateTime4Ivc初始化");
            staticDataService.flashIvcConfigMap(StaticDatas.IVC_CONFIG_MAP);
            staticDataService.flashIvcUserMap(StaticDatas.IVC_USER_MAP);
            LOG.info("iVC_CONFIG_MAP初始化");
        }else{
        	Long lastUpdateInDB = staticDataService.queryLastUpdateTime(IVC_TASK_ID);
            if(!lastUpdateInDB.equals(lastUpdateTime4Ivc)){
            	staticDataService.flashIvcConfigMap(StaticDatas.IVC_CONFIG_MAP);
            	staticDataService.flashIvcUserMap(StaticDatas.IVC_USER_MAP);
            	
                lastUpdateTime4Ivc = lastUpdateInDB;
                LOG.info("访问控制更新--end!");
            }
        }
    }
    
    /**
     * 维度模式任务
     */
//  @Scheduled(cron="0 7 15 * * ?")
    @Scheduled(fixedRate=60000)
    @PostConstruct
    public void runTask4CategoryPattern(){
        if(lastUpdateTime4CP == 0){//项目刚启动,进行初始化
        	lastUpdateTime4CP = staticDataService.queryLastUpdateTime(CP_TASK_ID);
            LOG.info("lastUpdateTime4CP初始化");
            StaticDatas.CATEGORY_PATTERN_MAP = staticDataService.getCategoryPatternMap();
            LOG.info("CATEGORY_PATTERN_MAP初始化");
        }else{
        	Long lastUpdateInDB = staticDataService.queryLastUpdateTime(CP_TASK_ID);
            if(!lastUpdateInDB.equals(lastUpdateTime4CP)){
            	StaticDatas.CATEGORY_PATTERN_MAP = staticDataService.getCategoryPatternMap();
                
            	lastUpdateTime4CP = lastUpdateInDB;
                LOG.info("维度模式更新--end!");
            }
        }
    }
}