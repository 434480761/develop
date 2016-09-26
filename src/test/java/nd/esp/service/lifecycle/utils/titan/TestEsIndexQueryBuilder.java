package nd.esp.service.lifecycle.utils.titan;

import nd.esp.service.lifecycle.BaseControllerConfig;
import nd.esp.service.lifecycle.daos.titan.inter.TitanResourceRepository;
import nd.esp.service.lifecycle.educommon.vos.constant.IncludesConstant;
import nd.esp.service.lifecycle.educommon.vos.constant.RetrieveFieldsConstant;
import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.services.titan.TitanResultParse;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.busi.elasticsearch.EsIndexQueryBuilder;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.driver.ResultSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.*;

/**
 * ******************************************
 * <p/>
 * Copyright 2016
 * NetDragon All rights reserved
 * <p/>
 * *****************************************
 * <p/>
 * *** Company ***
 * NetDragon
 * <p/>
 * *****************************************
 * <p/>
 * *** Team ***
 * <p/>
 * <p/>
 * *****************************************
 *
 * @author gsw(806801)
 * @version V1.0
 * @Title TestEsIndexQueryBuilder
 * @Package nd.esp.service.lifecycle.utils.titan
 * <p/>
 * *****************************************
 * @Description
 * @date 2016/8/29
 */
public class TestEsIndexQueryBuilder  extends BaseControllerConfig {
/*
    private static Logger logger = LoggerFactory.getLogger(TestEsIndexQueryBuilder.class);
    @Autowired
    private TitanResourceRepository<Education> titanResourceRepository;

    @Before
    public void createTestData() {
        // TODO 创建测试数据
    }

    @After
    public void cleanTestData() {
        // TODO 清除测试数据
    }

    @Test
    public void testUseScriptToSearch(){
        // 参数处理
        Map<String, Map<String, List<String>>> params = new HashMap<>();
        List<String> fields = RetrieveFieldsConstant.getValidFields("TIT");

        // 1、构建查询脚本
        EsIndexQueryBuilder builder = new EsIndexQueryBuilder();
        builder.setWords("test").setParams(params).setResTypeSet(new HashSet<String>()).setRange(1, 10).setIncludes(IncludesConstant.getIncludesList()).setFields(fields);
        String script = builder.generateScript();
        // 2、查询
        ResultSet resultSet = titanResourceRepository.search(script, null);

        List<String> resultStr = new ArrayList<>();
        if (resultSet != null) {
            long getResultBegin = System.currentTimeMillis();
            try {
                Iterator<Result> iterator = resultSet.iterator();
                while (iterator.hasNext()) {
                    resultStr.add(iterator.next().getString());
                }
            } catch (Exception e) {
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR, "LC/titan/query", "out of time or script has error");
            }
        } else {
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR, "LC/titan/query", "out of time or script has error");
        }
        logger.info("data:" + resultStr.size());


    }*/

}
