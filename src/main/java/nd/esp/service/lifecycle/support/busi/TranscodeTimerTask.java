package nd.esp.service.lifecycle.support.busi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nd.esp.service.lifecycle.educommon.models.ResourceModel;
import nd.esp.service.lifecycle.educommon.services.NDResourceService;
import nd.esp.service.lifecycle.educommon.vos.constant.IncludesConstant;
import nd.esp.service.lifecycle.entity.log.BaseLogModel;
import nd.esp.service.lifecycle.entity.log.TransCodeLogModel;
import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.EspRepository;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.repository.ds.ComparsionOperator;
import nd.esp.service.lifecycle.repository.ds.Item;
import nd.esp.service.lifecycle.repository.ds.LogicalOperator;
import nd.esp.service.lifecycle.repository.ds.ValueUtils;
import nd.esp.service.lifecycle.repository.ds.jpa.Criteria;
import nd.esp.service.lifecycle.repository.ds.jpa.Restrictions;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.TechInfo;
import nd.esp.service.lifecycle.repository.sdk.TechInfoRepository;
import nd.esp.service.lifecycle.repository.sdk.impl.ServicesManager;
import nd.esp.service.lifecycle.support.enums.LifecycleStatus;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 转码触发器 每天凌晨12点执行
 * <p>需要扫描的资源类型 coursewares,lessonplans</p>
 * <p>Create Time: 2015年9月21日           </p>
 *
 * @author liuwx
 */

@Component("transcodetimertask")
public class TranscodeTimerTask {
    private static Logger logger = LoggerFactory.getLogger(TranscodeTimerTask.class);

    private static  BaseLogModel COURSEWARE_TRANSCODE_LOG=new TransCodeLogModel();

    private static  BaseLogModel LESSONPLAN_TRANSCODE_LOG=new TransCodeLogModel();



    public static BaseLogModel getCoursewareTranscodeLog(){


        return COURSEWARE_TRANSCODE_LOG;
    }



    public static BaseLogModel getLessonplanTranscodeLog(){


        return LESSONPLAN_TRANSCODE_LOG;
    }



    @Autowired
    TechInfoRepository techInfoRepository;

    @Autowired
    private TransCodeUtil transCodeUtil;

    @Autowired
    private NDResourceService ndResourceService;

    //0点执行一次
    //@Scheduled(cron = "0 0 0 * * *")
    //@Scheduled(fixedRate=300000)
    public void doTask() throws EspStoreException {
        logger.info("开始后台转码任务");


        long startTime = System.currentTimeMillis();
        Map<String, Map<String, Long>> resultMap = new HashMap<>();
        Map<String, Long> result =  operationDb(IndexSourceType.SourceCourseWareType.getName());
        resultMap.put(IndexSourceType.SourceCourseWareType.getName(), result);

        long endTime =System.currentTimeMillis();

        long executeTime =endTime - startTime;

        logger.info("后台课件转码任务执行完成,共耗时:" + executeTime + "毫秒");


        COURSEWARE_TRANSCODE_LOG= bulidLogData( startTime,endTime,executeTime,result);


        startTime = System.currentTimeMillis();


        result =  operationDb(IndexSourceType.LessonPlansType.getName());
        resultMap.put(IndexSourceType.LessonPlansType.getName(),result);

        endTime =System.currentTimeMillis();

        executeTime =endTime - startTime;



        LESSONPLAN_TRANSCODE_LOG= bulidLogData( startTime,endTime,executeTime,result);



        logger.info("后台校验转码任务执行完成,共耗时:" + executeTime + "毫秒");

        logger.info("统计数据转换展示:" + ObjectUtils.toJson(resultMap));


    }



    private BaseLogModel bulidLogData( long startTime,long endTime,long executeTime, Map<String, Long> result){

        BaseLogModel log =new BaseLogModel();
        log.setStartTime(startTime);
        log.setEndTime(endTime);
        log.setExecuteTime(executeTime);
        log.setExecuteResult(ObjectUtils.toJson(result));
        return log;


    }


    private Map<String, Long> operationDb(String resType) throws EspStoreException {

        long CV_TOTAL = 0;//总记录数
        long CV_ALREADY_DEAL = 0;//已处理数
        long CV_UN_DEAL = 0;//未处理记录数


        Item<String> status_item = new Item<String>();
        status_item.setKey("status");
        status_item.setComparsionOperator(ComparsionOperator.EQ);
        status_item.setLogicalOperator(LogicalOperator.AND);
        status_item.setValue(ValueUtils.newValue(LifecycleStatus.TRANSCODE_WAITING.getCode()));
       // status_item.setValue(ValueUtils.newValue("TRANSCODING"));

        Item<Integer> enable_item = new Item<Integer>();
        enable_item.setKey("enable");
        enable_item.setComparsionOperator(ComparsionOperator.EQ);
        enable_item.setLogicalOperator(LogicalOperator.AND);
        enable_item.setValue(ValueUtils.newValue(1));


        List<Item<? extends Object>> items = new ArrayList<>();
        items.add(enable_item);
        items.add(status_item);

        Page assetPage = null;
        int page = 0;
        int rows = 500;
        Pageable pageable = null;
        do {
            // 分页查询
            pageable = new PageRequest(page, rows);
            EspRepository espRepository = ServicesManager.get(resType);
            //todo 如果没有查询到,报错

            assetPage = espRepository.findByItems(items, pageable);

            CV_TOTAL = assetPage.getTotalElements();
            List<Education> assets = assetPage.getContent();
            List<String> identifierList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(assets)) {
                for (Education education : assets) {
                    identifierList.add(education.getIdentifier());
                }
                //设置执行sql的wh ere内容
                Criteria<TechInfo> criteria = new Criteria<TechInfo>();
                //完全匹配查询
                criteria.add(Restrictions.eq("title", "source", false));
                criteria.add(Restrictions.in("resource", identifierList, false));
                List<TechInfo> results = techInfoRepository.findAllByCriteria(criteria);
                if (CollectionUtils.isNotEmpty(results)) {
                    for (Education education : assets) {
                        boolean isNewTurn = true;
                        for (TechInfo techInfo : results) {
                            if (education.getIdentifier().equals(techInfo.getResource())) {
                                CV_ALREADY_DEAL++;
                                isNewTurn = false;
                                ResourceModel cm = ndResourceService.getDetail(resType, education.getIdentifier(), IncludesConstant.getIncludesList());
                                //触发转码
                                transCodeUtil.triggerTransCode(cm, IndexSourceType.SourceCourseWareType.getName());
                                break;
                            }
                        }
                        if (isNewTurn) {
                            isNewTurn = true;
                            CV_UN_DEAL++;
                        }
                    }
                } else {
                    //没有找到
                    CV_UN_DEAL += assets.size();
                }
            }
        } while (++page < assetPage.getTotalPages());

        Map<String, Long> resultMap = new HashMap<String, Long>();
        resultMap.put("总数", CV_TOTAL);
        resultMap.put("此次转码已做处理的记录数", CV_ALREADY_DEAL);
        resultMap.put("此次转码未做处理的记录数", CV_UN_DEAL);


        return resultMap;
    }
}
