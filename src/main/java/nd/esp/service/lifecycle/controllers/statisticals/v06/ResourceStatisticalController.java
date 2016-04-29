package nd.esp.service.lifecycle.controllers.statisticals.v06;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;

import nd.esp.service.lifecycle.educommon.services.impl.CommonServiceHelper;
import nd.esp.service.lifecycle.models.statisticals.v06.ResourceStatisticalModel;
import nd.esp.service.lifecycle.services.statisticals.v06.ResourceStatisticalService;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.utils.BeanMapperUtils;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.vos.statistical.v06.ResourceStatisticalViewModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nd.gaea.WafException;

/**
 * 课件颗粒模板接口V0.6API
 * @author liur
 * */
@RestController
@RequestMapping(value = { "/v0.6/{res_type}", "/v0.6/resources" })
public class ResourceStatisticalController {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceStatisticalController.class);

    @Autowired
    @Qualifier(value = "StatisticalServiceImpl")
    private ResourceStatisticalService statisticalService;

    @Autowired
    @Qualifier(value = "StatisticalService4QuestionDBImpl")
    private ResourceStatisticalService statisticalService4QuestionDB;

    /**
     * 增加资源评价统计指标数据
     * @param svms 评价数据
     * @param resType 资源类型
     * @param id 资源ID
     * */
    @RequestMapping(value = "/{id}/statisticals", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public void add(@RequestBody List<ResourceStatisticalViewModel> svms, @PathVariable("res_type") String resType,
            @PathVariable String id) {

        checkParams(svms, resType, id);

        List<ResourceStatisticalModel> sms = new LinkedList<ResourceStatisticalModel>();

        for (ResourceStatisticalViewModel svm : svms) {
            ResourceStatisticalModel sm = BeanMapperUtils.beanMapper(svm, ResourceStatisticalModel.class);
            sms.add(sm);
        }

        // 保存资源
//        synchronized ("xu") {
            if (CommonServiceHelper.isQuestionDb(resType)) {
                sms = statisticalService4QuestionDB.addStatistical(sms, resType, id);
            }
            else {
                sms = statisticalService.addStatistical(sms, resType, id);
            }
//        }
        

        List<ResourceStatisticalViewModel> returnList = new ArrayList<ResourceStatisticalViewModel>();
        for (ResourceStatisticalModel sm : sms) {
            ResourceStatisticalViewModel svm = BeanMapperUtils.beanMapper(sm, ResourceStatisticalViewModel.class);
            returnList.add(svm);
        }
        return;
    }

    /**
     * @param key 多个key_title
     * @param rid 多个资源id
     * */
    @RequestMapping(value = "/statisticals/tolist", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public Map<String, List<ResourceStatisticalViewModel>> getList(@RequestParam("key") List<String> key,
            @RequestParam("rid") List<String> rid) {


        //验证uuid格式
        if(CollectionUtils.isNotEmpty(rid)){
            for (String uuid : rid) {
                if (!CommonHelper.checkUuidPattern(uuid)) {
                    throw new WafException(LifeCircleErrorMessageMapper.CheckIdentifierFail.getCode(),
                            LifeCircleErrorMessageMapper.CheckIdentifierFail.getMessage());
                }
            }
        } else {
            throw new WafException(LifeCircleErrorMessageMapper.CheckIdentifierFail.getCode(),
                    LifeCircleErrorMessageMapper.CheckIdentifierFail.getMessage());
        }

        final List<String> tempKey = new LinkedList<String>();
        // 去除空白key
        for (String title : key) {
            if (StringUtils.isEmpty(title) || (title != null && StringUtils.isEmpty(title.trim()))) {
                continue;
            }
            else {
                tempKey.add(title);
            }
        }
        // old
        // Map<String, List<ResourceStatisticalModel>> smMap = statisticalService.getList(tempKey, rid);
        //
        // if (smMap == null) {
        // smMap = new HashMap<String, List<ResourceStatisticalModel>>();
        // }
        //
        // List<String> rid4Questions = new LinkedList<String>();
        //
        // for(String rkey : smMap.keySet()){
        // List<ResourceStatisticalModel> list = smMap.get(rkey);
        // if(CollectionUtils.isEmpty(list)){
        // rid4Questions.add(rkey);
        // }
        // }
        //
        // Map<String, List<ResourceStatisticalModel>> smMap4Questions = statisticalService4QuestionDB.getList(tempKey,
        // rid4Questions);
        // if (smMap4Questions != null) {
        // for (String quekey : smMap4Questions.keySet()) {
        // smMap.put(quekey, smMap4Questions.get(quekey));
        // }
        // }
        // old--end

        //forkJoin查询数据库
        //从不同的数据库查询统计数据
        ForkJoinPool forkJoinPool = CommonHelper.getForkJoinPool();
        Future<Map<String, List<ResourceStatisticalModel>>> smMapFuture = forkJoinPool.submit(new StatisticsThread(
                tempKey, rid));
        Future<Map<String, List<ResourceStatisticalModel>>> smMap4QuestionstFuture = forkJoinPool
                .submit(new StatisticsThread4Questions(tempKey, rid));

        Map<String, List<ResourceStatisticalModel>> smMap = null;
        Map<String, List<ResourceStatisticalModel>> smMap4Questions = null;

        try {
            smMap4Questions = smMap4QuestionstFuture.get();
            smMap = smMapFuture.get();
        } catch (InterruptedException e) {
            LOG.error("获取资源统计出错", e);
        } catch (ExecutionException e) {
            LOG.error("获取资源统计出错", e);
        }

        if (smMap == null) {
            smMap = new HashMap<String, List<ResourceStatisticalModel>>();
        }

        /**
         * 合并map，smMap4Questions的会覆盖smMap中questions的统计数据
         * */
        if (smMap4Questions != null) {
            for (String quekey : smMap4Questions.keySet()) {
                List<ResourceStatisticalModel> list4Questions = smMap4Questions.get(quekey);
                if (!CollectionUtils.isEmpty(list4Questions)) {
                    smMap.put(quekey, list4Questions);
                }
            }
        }

        // 模型转出
        Map<String, List<ResourceStatisticalViewModel>> svmMap = new HashMap<String, List<ResourceStatisticalViewModel>>();
        if (CollectionUtils.isNotEmpty(smMap)) {
            for (String uuid : smMap.keySet()) {
                List<ResourceStatisticalModel> smList = smMap.get(uuid);
                List<ResourceStatisticalViewModel> svmList = new ArrayList<ResourceStatisticalViewModel>();
                if (CollectionUtils.isNotEmpty(smList)) {
                    for (ResourceStatisticalModel statisticalModel : smList) {
                        ResourceStatisticalViewModel statisticalViewModel = BeanMapperUtils.beanMapper(
                                statisticalModel, ResourceStatisticalViewModel.class);
                        svmList.add(statisticalViewModel);
                    }
                }
                svmMap.put(uuid, svmList);
            }
        }

        return svmMap;
    }

    /**
     * 验证
     * */
    private void checkParams(List<ResourceStatisticalViewModel> svms, String resType, String id) {
        // 验证UUID格式
        if (!CommonHelper.checkUuidPattern(id)) {
            throw new WafException(LifeCircleErrorMessageMapper.CheckIdentifierFail.getCode(),
                    LifeCircleErrorMessageMapper.CheckIdentifierFail.getMessage());
        }

        // 验证key_title不重复
        Set<String> titles = new HashSet<String>();
        for (ResourceStatisticalViewModel rsvm : svms) {
            titles.add(rsvm.getKeyTitle());
        }
        if (titles.size() < svms.size()) {
            throw new WafException(LifeCircleErrorMessageMapper.CheckResourceStatisticalTitlesFail.getCode(),
                    LifeCircleErrorMessageMapper.CheckResourceStatisticalTitlesFail.getMessage());
        }

        // 验证参数不为空
        for (ResourceStatisticalViewModel svm : svms) {

            if (StringUtils.isEmpty(svm.getKeyTitle())
                    || (svm.getKeyTitle() != null && StringUtils.isEmpty(svm.getKeyTitle().trim()))) {
                throw new WafException(LifeCircleErrorMessageMapper.CheckResourceStatisticalTitleFail.getCode(),
                        LifeCircleErrorMessageMapper.CheckResourceStatisticalTitleFail.getMessage());
            }

            if (StringUtils.isEmpty(svm.getDataFrom())
                    || (svm.getDataFrom() != null && StringUtils.isEmpty(svm.getDataFrom().trim()))) {
                throw new WafException(LifeCircleErrorMessageMapper.CheckResourceStatisticalDataFromFail.getCode(),
                        LifeCircleErrorMessageMapper.CheckResourceStatisticalDataFromFail.getMessage());

            }

            if (svm.getKeyValue() == null || (svm.getKeyValue() != null && svm.getKeyValue() < 0)) {
                throw new WafException(LifeCircleErrorMessageMapper.CheckResourceStatisticalKeyValueFail.getCode(),
                        LifeCircleErrorMessageMapper.CheckResourceStatisticalKeyValueFail.getMessage());

            }
        }
    }

    /**
     * 查询统计资源线程
     * */
    class StatisticsThread extends RecursiveTask<Map<String, List<ResourceStatisticalModel>>> {

        private List<String> key;

        private List<String> rid;

        public List<String> getKey() {
            return key;
        }

        public void setKey(List<String> key) {
            this.key = key;
        }

        public List<String> getRid() {
            return rid;
        }

        public void setRid(List<String> rid) {
            this.rid = rid;
        }

        public StatisticsThread(List<String> key, List<String> rid) {
            this.key = new LinkedList<String>(key);
            this.rid = new LinkedList<String>(rid);
        }

        protected Map<String, List<ResourceStatisticalModel>> compute() {
            return statisticalService.getList(getKey(), getRid());
        }
    }

    /**
     * 查询习题课统计资源线程
     * */
    class StatisticsThread4Questions extends StatisticsThread {
        public StatisticsThread4Questions(List<String> key, List<String> rid) {
            super(key, rid);
        }

        protected Map<String, List<ResourceStatisticalModel>> compute() {
            return statisticalService4QuestionDB.getList(getKey(), getRid());
        }
    }
}
