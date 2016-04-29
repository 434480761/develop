package nd.esp.service.lifecycle.controller.v06;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import nd.esp.service.lifecycle.BaseControllerConfig;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;
import nd.esp.service.lifecycle.vos.CategoryDataViewModel;
import nd.esp.service.lifecycle.vos.CategoryPatternViewModel;
import nd.esp.service.lifecycle.vos.CategoryRelationViewModel;
import nd.esp.service.lifecycle.vos.CategoryViewModel;
import nd.esp.service.lifecycle.vos.ListViewModel;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.nd.gaea.rest.o2o.JacksonCustomObjectMapper;
import com.nd.gaea.rest.testconfig.MockUtil;

/**
 * 分类体系controller层接口测试
 * 
 * <br>Created 2015年5月6日 下午5:35:11
 * @version  
 * @author   linsm		
 *
 * @see 	 
 * 
 * Copyright(c) 2009-2014, TQ Digital Entertainment, All Rights Reserved
 *
 */
public class TestCategoryController extends BaseControllerConfig {
    Logger logger = Logger.getLogger(this.getClass().getName());

    private static final double MIN_DELTA = 0.001;

    private JacksonCustomObjectMapper ObjectMapper = new JacksonCustomObjectMapper();

    private static final String FIRST_DIMMENSION_UPDATE_TITLE = "更新第一维度";

    private static final String FIRST_DIMMENSION_UPDATE_NDCODE = "WW";

    private static final int CATEGORY_NUM = 3;

    private static final String[] CATEGORY_TITLE = { "第一维度", "第二维度", "第三维度" };

    private static final String[] Category_NDCODE = { "WX", "XW", "XX" };

    private static final String PATTERN_NAME = "esp_2";

    private static Long total = 0L; // 记录数据库中，分类维度的总数;

    private static List<CategoryViewModel> categoryList = new ArrayList<CategoryViewModel>();

    private static List<CategoryDataViewModel> firstCategoryDataList = new ArrayList<CategoryDataViewModel>();

    private static List<CategoryDataViewModel> secondCategoryDataList = new ArrayList<CategoryDataViewModel>();

    private static List<CategoryDataViewModel> thirdCategoryDataList = new ArrayList<CategoryDataViewModel>();

    private static CategoryPatternViewModel patternViewModel = null;

    private static List<CategoryRelationViewModel> relationList = new ArrayList<CategoryRelationViewModel>();

    // 四大块接口：
    // 一、分类维度：增、改、查、删,新增： uuid取详情，nd_code 取详情，批量取详情
    // 二、维度数据:增、改、查、删,新增： uuid取详情，nd_code 取详情，批量取详情
    // 三、维度模式：增、改、查、删,新增： uuid取详情，pattern_name 取详情，批量取详情 ，模型：新增字段（patternPath)
    // 四、维度数据关系：增、查、删,新增： 批量创建，批量删除;
    /**
     * 一、分类维度：增、改、查、删,新增： uuid取详情，nd_code 取详情，批量取详情
     * */
//    @Test
    public void testCategory() {
        String json = "";
        String uri = "";
        String result = "";

        logger.info("分类维度 controller层测试开始");
        try {
            CategoryViewModel createCategoryParam = null;
            CategoryViewModel createCategoryResult = null;
            // 一、分类维度：增
            //lr
            //增加不合法维度数据:ndcode没有进行备案
            uri = "/v0.6/categories";
            createCategoryParam = createCategory(CATEGORY_TITLE[0], "JJ");
            json = ObjectMapper.writeValueAsString(createCategoryParam);
            result = MockUtil.mockCreate(mockMvc, uri, json);
            assertExceptionMessage(result, "分类维度nd_code=JJ 还没有备案，有需要请与LC沟通");
            
            //增加合法的维度数据
            // 参数设置：
            uri = "/v0.6/categories";
            createCategoryParam = createCategory(CATEGORY_TITLE[0], Category_NDCODE[0]);
            json = ObjectMapper.writeValueAsString(createCategoryParam);
            result = MockUtil.mockCreate(mockMvc, uri, json);
            createCategoryResult = ObjectMapper.readValue(result, CategoryViewModel.class);
            // 验证结果：
            assertCategoryEqualWithoutId(createCategoryParam, createCategoryResult);
            logger.info("创建分类维度测试成功");
            categoryList.add(createCategoryResult); // 把资源添加到全局
            
            //lr
            //增加不合法维度数据：ndcode在数据库中已经存在
            uri = "/v0.6/categories";
            createCategoryParam = createCategory(createCategoryResult.getTitle(), createCategoryResult.getNdCode());
            json = ObjectMapper.writeValueAsString(createCategoryParam);
            result = MockUtil.mockCreate(mockMvc, uri, json);
            assertExceptionMessage(result, "分类的ND编码已经存在");

            // 一、分类维度：改 ,依赖于创建接口，需要ID和对应的数据
            // 参数设置
            String categoryId = categoryList.get(0).getIdentifier();
            uri = "/v0.6/categories/" + categoryId; // 暂时修改第一个资源
            createCategoryParam.setTitle(FIRST_DIMMENSION_UPDATE_TITLE);
            createCategoryParam.setNdCode(FIRST_DIMMENSION_UPDATE_NDCODE);
            json = ObjectMapper.writeValueAsString(createCategoryParam);
            result = MockUtil.mockPut(mockMvc, uri, json);
            CategoryViewModel updateCategoryresult = ObjectMapper.readValue(result, CategoryViewModel.class);
            // 验证结果：
            assertCategoryEqualWithoutId(createCategoryParam, updateCategoryresult);
            Assert.assertEquals("分类维度id不对", categoryId, updateCategoryresult.getIdentifier());
            logger.info("修改分类维度测试成功");
            
            //lr
            //修改分类维度：错误的ID
            categoryId = "006b5383-aaaa-aaaa-aaaa-5cef04103c8c";
            uri = "/v0.6/categories/" + categoryId; 
            createCategoryParam.setTitle(FIRST_DIMMENSION_UPDATE_TITLE);
            createCategoryParam.setNdCode(FIRST_DIMMENSION_UPDATE_NDCODE);
            json = ObjectMapper.writeValueAsString(createCategoryParam);
            result = MockUtil.mockPut(mockMvc, uri, json);
            assertExceptionMessage(result, "分类维度资源未找到");
            
            //lr
            //增加第二个分类维度(测试数据)
            uri = "/v0.6/categories";
            createCategoryParam = createCategory(CATEGORY_TITLE[1], Category_NDCODE[1]);
            json = ObjectMapper.writeValueAsString(createCategoryParam);
            result = MockUtil.mockCreate(mockMvc, uri, json);
            createCategoryResult = ObjectMapper.readValue(result, CategoryViewModel.class);
            categoryList.add(createCategoryResult); // 把资源添加到全局
            //修改分类维度：id和ndcode不匹配的情况
            uri = "/v0.6/categories/" + createCategoryResult.getIdentifier(); 
            createCategoryParam.setTitle(FIRST_DIMMENSION_UPDATE_TITLE);
            createCategoryParam.setNdCode(FIRST_DIMMENSION_UPDATE_NDCODE);
            json = ObjectMapper.writeValueAsString(createCategoryParam);
            result = MockUtil.mockPut(mockMvc, uri, json);
            assertExceptionMessage(result, "分类的ND编码已经存在");
            
            // 依赖于更新后的结果： updateCategoryResult
            // 一、分类维度：取
            // 1、uuid
            uri = "/v0.6/categories/" + updateCategoryresult.getIdentifier();
            json = "";
            result = MockUtil.mockGet(mockMvc, uri, json);
            CategoryViewModel detailCategoryModel = ObjectMapper.readValue(result, CategoryViewModel.class);
            // 验证结果
            assertCategoryEqual(updateCategoryresult, detailCategoryModel);
            logger.info("通过uuid取分类维度测试成功");

            // 2、nd_code
            uri = "/v0.6/categories/" + updateCategoryresult.getNdCode();
            json = "";
            result = MockUtil.mockGet(mockMvc, uri, json);
            detailCategoryModel = ObjectMapper.readValue(result, CategoryViewModel.class);
            // 验证结果
            assertCategoryEqual(updateCategoryresult, detailCategoryModel);
            logger.info("通过nd_code取分类维度测试成功");
            
            //lr
            //获取分类维度：不合法的 nd_code
            uri = "/v0.6/categories/" + "3JJadf";
            json = "";
            result = MockUtil.mockGet(mockMvc, uri, json);
            assertExceptionMessage(result, "nd_code必须使用两位大写英文字母标识，首位可以使用$符号开始，第二位不允许出现$符号");
            
            //lr
            //获取分类维度：不存在的UUID
            uri = "/v0.6/categories/" + UUID.randomUUID().toString();
            json = "";
            result = MockUtil.mockGet(mockMvc, uri, json);
            assertExceptionMessage(result, "分类维度资源未找到");
            

            // 3、nd_code（批量）（暂时也只取一个）
            uri = "/v0.6/categories/list?nd_code=" + updateCategoryresult.getNdCode();
            json = "";
            result = MockUtil.mockGet(mockMvc, uri, json);
            Map<String, CategoryViewModel> batchDetailCategoryModel = ObjectMapper.readValue(result,
                    new TypeReference<Map<String, CategoryViewModel>>() {
                    });
            detailCategoryModel = batchDetailCategoryModel.get(updateCategoryresult.getNdCode());
            // 验证结果
            assertCategoryEqual(updateCategoryresult, detailCategoryModel);
            logger.info("通过nd_code（批量）取分类维度测试成功");
            
            // 3、nd_code（批量）（暂时也只取一个）
            //lr
            //批量获取：不合法的nd_code
            uri = "/v0.6/categories/list?nd_code=" + "3JJadf";
            json = "";
            result = MockUtil.mockGet(mockMvc, uri, json);
            assertExceptionMessage(result, "nd_code必须使用两位大写英文字母标识，首位可以使用$符号开始，第二位不允许出现$符号");
            

            // 一、分类维度：查
            uri = "/v0.6/categories?words=lsm&limit=(0,800)"; // 确保自己创建的内容可以返回
            json = "";
            result = MockUtil.mockGet(mockMvc, uri, json);
            ListViewModel<CategoryViewModel> queryCategoryListResult = ObjectMapper.readValue(result,
                    new TypeReference<ListViewModel<CategoryViewModel>>() {
                    });
            // 验证结果：
            // 查询的结果中存在我自己创建的内容
            total = queryCategoryListResult.getTotal();
            Assert.assertTrue("个数不对", total > 0);
            for (CategoryViewModel viewModel : queryCategoryListResult.getItems()) {
                if (viewModel.getNdCode().equals(createCategoryParam.getNdCode())) {
                    assertCategoryEqualWithoutId(createCategoryParam, viewModel);
                }
            }
            logger.info("查询分类维度测试成功");

            // 一、分类维度：删 (依赖于创建的接口，需要uuid)
            
            //lr
            //传递不存在的UUID删除
            uri = "/v0.6/categories/" + UUID.randomUUID().toString();
            json = "";
            result = MockUtil.mockDelete(mockMvc, uri, json);
            assertExceptionMessage(result, "分类维度资源未找到");
            
            //正常删除
            categoryId = categoryList.get(0).getIdentifier();
            categoryList.remove(0);// 怎么保证一定成功？
            uri = "/v0.6/categories/" + categoryId;
            json = "";
            result = MockUtil.mockDelete(mockMvc, uri, json);
            
            // 验证结果： 通过再次查询，个数应该少1;
            total--;
            uri = "/v0.6/categories?words=lsm&limit=(0,800)"; // 确保自己创建的内容可以返回
            json = "";
            result = MockUtil.mockGet(mockMvc, uri, json);
            queryCategoryListResult = ObjectMapper.readValue(result,
                    new TypeReference<ListViewModel<CategoryViewModel>>() {
                    });
            // 验证结果：
            Assert.assertEquals("删除后个数不对", total, queryCategoryListResult.getTotal());
            logger.info("删除分类维度测试成功");
            // 清除所有分类维度
            clearCategoryList(categoryList);
        } catch (Exception e) {
            logger.error(e);
        }

        logger.info("分类维度 controller层测试结束");
    }

    /**
     * 二、维度数据:增、改、查、删,新增： uuid取详情，nd_code 取详情，批量取详情
     * */
//    @Test
    public void testCategoryData() {
        String json = "";
        String uri = "";
        String result = "";

        try {
            // 二、维度数据
            logger.info("维度数据 controller 层测试开始");
            {
                // 一、分类维度：增 (维度数据依赖于分类维度，为了测试用，创建一个维度分类
                // 参数设置：
                uri = "/v0.6/categories";
                CategoryViewModel createCategoryParam = createCategory(CATEGORY_TITLE[0], Category_NDCODE[0]);
                json = ObjectMapper.writeValueAsString(createCategoryParam);
                result = MockUtil.mockCreate(mockMvc, uri, json);
                CategoryViewModel createCategoryResult = ObjectMapper.readValue(result, CategoryViewModel.class);
                categoryList.add(createCategoryResult); // 把资源添加到全局
                // 验证结果：
                assertCategoryEqualWithoutId(createCategoryParam, createCategoryResult);
                logger.info("创建分类维度测试成功");
            }

            
            // 二、维度数据:增
            uri = "/v0.6/categories/datas";
            CategoryDataViewModel createCategoryDataParam = new CategoryDataViewModel();
            createCategoryDataParam.setTitle("第一维度 一");
            createCategoryDataParam.setShortName("first dimension first");
            createCategoryDataParam.setParent("ROOT");
            createCategoryDataParam.setNdCode(categoryList.get(0).getNdCode() + "0100");
            createCategoryDataParam.setDescription("lsm 第一维度第一数据");
            createCategoryDataParam.setGbCode("A");
            createCategoryDataParam.setOrderNum(1);
            createCategoryDataParam.setCategory(categoryList.get(0).getIdentifier()); // 暂时还是采用第一个
            json = ObjectMapper.writeValueAsString(createCategoryDataParam);
            result = MockUtil.mockCreate(mockMvc, uri, json);
            // 验证结果
            CategoryDataViewModel createCategoryDataResult = ObjectMapper
                    .readValue(result, CategoryDataViewModel.class);
            assertCategoryDataEqualsWithoutId(createCategoryDataParam, createCategoryDataResult);
            firstCategoryDataList.add(createCategoryDataResult);
            logger.info("创建维度数据 测试成功");
            
            
//            CategoryViewModel createCategoryParam = categoryList.get(0);
//            uri = "/v0.6/categories/" + categoryList.get(0).getIdentifier(); 
//            createCategoryParam.setTitle(FIRST_DIMMENSION_UPDATE_TITLE);
//            createCategoryParam.setNdCode(FIRST_DIMMENSION_UPDATE_NDCODE);
//            json = ObjectMapper.writeValueAsString(createCategoryParam);
//            result = MockUtil.mockPut(mockMvc, uri, json);
            
            // 二、维度数据:增
            //lr
            //传递已经存在的nd_code
            uri = "/v0.6/categories/datas";
            CategoryDataViewModel createCategoryDataParam2 = new CategoryDataViewModel();
            createCategoryDataParam2.setTitle("第一维度 一");
            createCategoryDataParam2.setShortName("first dimension first");
            createCategoryDataParam2.setParent("ROOT");
            createCategoryDataParam2.setNdCode(categoryList.get(0).getNdCode() + "0100");
            createCategoryDataParam2.setDescription("lsm 第一维度第一数据");
            createCategoryDataParam2.setGbCode("A");
            createCategoryDataParam2.setOrderNum(1);
            createCategoryDataParam2.setCategory(categoryList.get(0).getIdentifier()); // 暂时还是采用第一个
            json = ObjectMapper.writeValueAsString(createCategoryDataParam2);
            result = MockUtil.mockCreate(mockMvc, uri, json);
            assertExceptionMessage(result, "分类的ND编码已经存在");
            
            // 二、维度数据:改
            String categoryDataId = firstCategoryDataList.get(0).getIdentifier();
            uri = "/v0.6/categories/datas/" + categoryDataId;
            createCategoryDataParam.setTitle("更新后的Title");
            json = ObjectMapper.writeValueAsString(createCategoryDataParam);
            result = MockUtil.mockPut(mockMvc, uri, json);
            // 验证结果
            CategoryDataViewModel updateCategoryDataResult = ObjectMapper
                    .readValue(result, CategoryDataViewModel.class);
            assertCategoryDataEqualsWithoutId(createCategoryDataParam, updateCategoryDataResult);
            Assert.assertEquals("维度数据id 不对", categoryDataId, updateCategoryDataResult.getIdentifier());
            logger.info("修改维度数据 测试成功");
            
            

            // 依赖于更新后的结果： updateCategoryDataResult
            // 一、维度数据：取
            // 1、uuid
            uri = "/v0.6/categories/datas/" + updateCategoryDataResult.getIdentifier();
            json = "";
            result = MockUtil.mockGet(mockMvc, uri, json);
            CategoryDataViewModel detailCategoryDataModel = ObjectMapper.readValue(result, CategoryDataViewModel.class);
            // 验证结果
            assertCategoryDataEqual(updateCategoryDataResult, detailCategoryDataModel);
            logger.info("通过uuid取维度数据测试成功");

            // 2、nd_code
            uri = "/v0.6/categories/datas/" + updateCategoryDataResult.getNdCode();
            json = "";
            result = MockUtil.mockGet(mockMvc, uri, json);
            detailCategoryDataModel = ObjectMapper.readValue(result, CategoryDataViewModel.class);
            // 验证结果
            assertCategoryDataEqual(updateCategoryDataResult, detailCategoryDataModel);
            logger.info("通过nd_code取维度数据测试成功");
            
            //lr
            //2、不存在的nd_code
            uri = "/v0.6/categories/datas/" + "errorcode";
            json = "";
            result = MockUtil.mockGet(mockMvc, uri, json);
            assertExceptionMessage(result, "维度数据资源未找到");

            // 3、nd_code（批量）（暂时也只取一个）
            uri = "/v0.6/categories/datas/list?nd_code=" + updateCategoryDataResult.getNdCode();
            json = "";
            result = MockUtil.mockGet(mockMvc, uri, json);
            Map<String, CategoryDataViewModel> batchDetailCategoryDataModel = ObjectMapper.readValue(result,
                    new TypeReference<Map<String, CategoryDataViewModel>>() {
                    });
            detailCategoryDataModel = batchDetailCategoryDataModel.get(updateCategoryDataResult.getNdCode());
            // 验证结果
            assertCategoryDataEqual(updateCategoryDataResult, detailCategoryDataModel);
            logger.info("通过nd_code（批量）维度数据测试成功");

            // 二、维度数据:查
            uri = "/v0.6/categories/" + Category_NDCODE[0] + "/datas?words=lsm&limit=(0,100)";
            json = "";
            result = MockUtil.mockGet(mockMvc, uri, json);
            ListViewModel<CategoryDataViewModel> queryCategoryDataListResult = ObjectMapper.readValue(result,
                    new TypeReference<ListViewModel<CategoryDataViewModel>>() {
                    });
            // 验证结果：
            // 查询的结果中存在我自己创建的内容
            total = queryCategoryDataListResult.getTotal();
            Assert.assertTrue("个数不对", total > 0);
            for (CategoryDataViewModel viewModel : queryCategoryDataListResult.getItems()) {
                if (viewModel.getNdCode().equals(createCategoryDataParam.getNdCode())) {
                    assertCategoryDataEqualsWithoutId(createCategoryDataParam, viewModel);
                }
            }
            logger.info("查询维度数据 测试成功");
            
            //lr
            //维度数据：查,不存在的nd_code
            uri = "/v0.6/categories/" + "errorcode"+ "/datas?words=lsm&limit=(0,100)";
            json = "";
            result = MockUtil.mockGet(mockMvc, uri, json);
            assertExceptionMessage(result, "分类维度资源未找到");
            
            // 二、维度数据:删
            categoryDataId = firstCategoryDataList.get(0).getIdentifier();
            firstCategoryDataList.remove(0);// 怎么保证一定成功？
            uri = "/v0.6/categories/datas/" + categoryDataId;
            json = "";
            result = MockUtil.mockDelete(mockMvc, uri, json);
            // 验证结果： 通过再次查询，个数应该少1;
            total--;
            uri = "/v0.6/categories/" + Category_NDCODE[0] + "/datas?words=lsm&limit=(0,100)"; // 确保自己创建的内容可以返回
            json = "";
            result = MockUtil.mockGet(mockMvc, uri, json);
            queryCategoryDataListResult = ObjectMapper.readValue(result,
                    new TypeReference<ListViewModel<CategoryDataViewModel>>() {
                    });
            // 验证结果：
            Assert.assertEquals("删除后个数不对", total, queryCategoryDataListResult.getTotal());
            logger.info("删除维度数据 测试成功");
            // 清除所有维度数据
            clearCategoryDataAll();
            // 清除依赖的分类维度
            clearCategoryList(categoryList);
        } catch (Exception e) {
            logger.error(e);
        }
        logger.info("维度数据 controller 层测试结束");
    }

    /**
     * 三、维度模式：增、改、查、删,新增： uuid取详情，pattern_name 取详情，批量取详情 ，模型：新增字段（patternPath)
     * */
//    @Test
    public void testCategoryPattern() {
        String json = "";
        String uri = "";
        String result = "";

        try {
            // 三、维度模式：增、改、查、删
            logger.info("维度模式 controller 层测试开始");
            // 三、维度模式：增
            uri = "/v0.6/categorypatterns";
            CategoryPatternViewModel createPatternParam = createCategoryPatternViewModel();
            json = ObjectMapper.writeValueAsString(createPatternParam);
            result = MockUtil.mockCreate(mockMvc, uri, json);
            // 验证结果：
            CategoryPatternViewModel createPatternResult = ObjectMapper.readValue(result,
                    CategoryPatternViewModel.class);
            assertCategoryPatternEqualsWithoutId(createPatternParam, createPatternResult);
            logger.info("维度模式 创建 成功");
            patternViewModel = createPatternResult; // 保存在全局中
            
            //lr
            //维度模式：增 :重复的模式名称
            uri = "/v0.6/categorypatterns";
            createPatternParam = createCategoryPatternViewModel();
            json = ObjectMapper.writeValueAsString(createPatternParam);
            result = MockUtil.mockCreate(mockMvc, uri, json);
            assertExceptionMessage(result, "维度模式名称已经存在");

            // 三、维度模式：改
            // 设置参数
            uri = "/v0.6/categorypatterns/" + patternViewModel.getIdentifier();
            CategoryPatternViewModel updatePatternParam = patternViewModel;
            updatePatternParam.setTitle("更新后 " + patternViewModel.getTitle());
            json = ObjectMapper.writeValueAsString(updatePatternParam);
            // 调用接口
            result = MockUtil.mockPut(mockMvc, uri, json);
            // 验证结果
            CategoryPatternViewModel updatePatternResult = ObjectMapper.readValue(result,
                    CategoryPatternViewModel.class);
            assertCategoryPatternEqualsWithoutId(updatePatternParam, updatePatternResult);
            logger.info("维度模式 修改 成功");
            patternViewModel = updatePatternResult;

            // 依赖于更新后的结果： updatePatternResult
            // 一、模式：取
            // 1、uuid
            uri = "/v0.6/categorypatterns/" + updatePatternResult.getIdentifier();
            json = "";
            result = MockUtil.mockGet(mockMvc, uri, json);
            CategoryPatternViewModel detailCategoryPatternModel = ObjectMapper.readValue(result,
                    CategoryPatternViewModel.class);
            // 验证结果
            assertCategoryPatternEqual(updatePatternResult, detailCategoryPatternModel);
            logger.info("通过uuid取模式测试成功");

            // 2、pattern_name
            uri = "/v0.6/categorypatterns/" + updatePatternResult.getPatternName();
            json = "";
            result = MockUtil.mockGet(mockMvc, uri, json);
            detailCategoryPatternModel = ObjectMapper.readValue(result, CategoryPatternViewModel.class);
            // 验证结果
            assertCategoryPatternEqual(updatePatternResult, detailCategoryPatternModel);
            logger.info("通过pattern_name取模式测试成功");
            
            //lr
            //不存在的pattern_name
            uri = "/v0.6/categorypatterns/" + "errorPatternName";
            json = "";
            result = MockUtil.mockGet(mockMvc, uri, json);
            assertExceptionMessage(result, "维度模式资源未找到");
            
            // 3、pattern_name（批量）（暂时也只取一个）
            uri = "/v0.6/categorypatterns/list?pattern_name=" + updatePatternResult.getPatternName();
            json = "";
            result = MockUtil.mockGet(mockMvc, uri, json);
            Map<String, CategoryPatternViewModel> batchDetailCategoryPatternModel = ObjectMapper.readValue(result,
                    new TypeReference<Map<String, CategoryPatternViewModel>>() {
                    });
            detailCategoryPatternModel = batchDetailCategoryPatternModel.get(updatePatternResult.getPatternName());
            // 验证结果
            assertCategoryPatternEqual(updatePatternResult, detailCategoryPatternModel);
            logger.info("通过pattern_name（批量）取模式测试成功");

            // 三、维度模式：查
            // 设置入参:
            uri = "/v0.6/categorypatterns?words=lsm&limit=(0,100)";
            json = "";
            // 调用接口
            result = MockUtil.mockGet(mockMvc, uri, json);
            // 验证结果
            ListViewModel<CategoryPatternViewModel> queryPatternResult = ObjectMapper.readValue(result,
                    new TypeReference<ListViewModel<CategoryPatternViewModel>>() {
                    });
            total = queryPatternResult.getTotal();
            Assert.assertTrue("查询个数不对", total > 0);
            for (CategoryPatternViewModel viewModel : queryPatternResult.getItems()) {
                if (viewModel.getIdentifier().equals(patternViewModel.getIdentifier())) {
                    // 找到我自己创建的对应模式
                    assertCategoryPatternEqualsWithoutId(patternViewModel, viewModel);
                }
            }

            // //

            logger.info("维度模式 查询 成功");
            // 三、维度模式：删
            
            //lr 
            //不存在的维度模式ID
            uri = "/v0.6/categorypatterns/" + UUID.randomUUID().toString();
            json = "";
            result = MockUtil.mockDelete(mockMvc, uri, json);
            assertExceptionMessage(result, "维度模式资源未找到");
            
            //正常删除
            String patternId = patternViewModel.getIdentifier();
            patternViewModel = null; // 已删除
            uri = "/v0.6/categorypatterns/" + patternId;
            json = "";
            result = MockUtil.mockDelete(mockMvc, uri, json);
            // validate result (by query)
            total--;
            uri = "/v0.6/categorypatterns?words=lsm&limit=(0,100)";
            json = "";
            // 调用接口
            result = MockUtil.mockGet(mockMvc, uri, json);
            // 验证结果
            queryPatternResult = ObjectMapper.readValue(result,
                    new TypeReference<ListViewModel<CategoryPatternViewModel>>() {
                    });
            Assert.assertEquals("删除后个数不对", total, queryPatternResult.getTotal());
            for (CategoryPatternViewModel viewModel : queryPatternResult.getItems()) {
                Assert.assertNotEquals("对应的模式已删除，但是还出现", patternId, viewModel.getIdentifier());
            }
            logger.info("维度模式 删除 成功");
            //
            clearCategoryPattern(patternViewModel);
            patternViewModel = null;
        } catch (Exception e) {
            logger.error(e);
        }
        logger.info("维度模式 controller 层测试结束");
    }

    /**
     * 四、维度数据关系：增、查、删,新增： 批量创建，批量删除;
     * */
//    @Test
    public void testCategoryRelations() {
        String json = "";
        String uri = "";
        String result = "";

        try {
            // 四、维度数据关系：增、查、删
            // 创建依赖的资源
            {
                // 维度模式
                uri = "/v0.6/categorypatterns";
                CategoryPatternViewModel createPatternParam = createCategoryPatternViewModel();
                json = ObjectMapper.writeValueAsString(createPatternParam);
                result = MockUtil.mockCreate(mockMvc, uri, json);
                // 验证结果：
                CategoryPatternViewModel createPatternResult = ObjectMapper.readValue(result, CategoryPatternViewModel.class);
                assertCategoryPatternEqualsWithoutId(createPatternParam, createPatternResult);
                logger.info("维度模式 创建 成功");
                patternViewModel = createPatternResult; // 保存在全局中
                // 分类维度
                for (int i = 0; i < CATEGORY_NUM; i++) {
                    uri = "/v0.6/categories";
                    CategoryViewModel createCategoryParam = createCategory(CATEGORY_TITLE[i], Category_NDCODE[i]);
                    json = ObjectMapper.writeValueAsString(createCategoryParam);
                    result = MockUtil.mockCreate(mockMvc, uri, json);
                    CategoryViewModel createCategoryResult = ObjectMapper.readValue(result, CategoryViewModel.class);
                    // 验证结果：
                    assertCategoryEqualWithoutId(createCategoryParam, createCategoryResult);
                    logger.info("创建分类维度测试成功");
                    categoryList.add(createCategoryResult); // 把资源添加到全局
                }
                // 维度数据
                createCategoryDataList(categoryList.get(0), firstCategoryDataList);
                createCategoryDataList(categoryList.get(1), secondCategoryDataList);
                createCategoryDataList(categoryList.get(2), thirdCategoryDataList);
            }
            logger.info("维度数据关系 controller 层测试开始");
            // 四、维度数据关系：增
            // 第一层关系：
            // 粗粒度
            uri = "/v0.6/categorypatterns/datas/relations";
            CategoryRelationViewModel createRelationParam = new CategoryRelationViewModel();
            createRelationParam.setSource("ROOT");
            createRelationParam.setTarget(firstCategoryDataList.get(0).getIdentifier());
            createRelationParam.setRelationType("ASSOCIATE");
            createRelationParam.setTags(new ArrayList<String>());
            createRelationParam.setOrderNum(1);
            createRelationParam.setEnable(true);
            createRelationParam.setPatternPath(patternViewModel.getPatternName());
            createRelationParam.setPattern(patternViewModel.getIdentifier());
            createRelationParam.setLevelParent("ROOT");
            json = ObjectMapper.writeValueAsString(createRelationParam);
            // 调用接口
            result = MockUtil.mockCreate(mockMvc, uri, json);
            // validate result
            CategoryRelationViewModel createRelationResult = ObjectMapper.readValue(result,
                    CategoryRelationViewModel.class);
            assertCategoryRelationEqualsWithoutId(createRelationParam, createRelationResult);
            relationList.add(createRelationResult);

            // lr
            // 指定关系条件，查询满足条件的关系个数
            uri = "/v0.6/categories/relations/actions/count?" + "patternPath=" + createRelationResult.getPatternPath()
                    + "&nd_code=" + firstCategoryDataList.get(0).getNdCode();
            json = "";
            MockUtil.mockGet(mockMvc, uri, json);

            // lr
            // 查询分类维度之间的关系
            uri = "/v0.6/categories/relations?patternPath=" + createRelationResult.getPatternPath();
            json = "";
            MockUtil.mockGet(mockMvc, uri, json);

            // lr
            // 修改分类维度应用模式下的维度数据关系
            uri = "/v0.6/categorypatterns/datas/relations/" + createRelationResult.getIdentifier();
            json = ObjectMapper.writeValueAsString(createRelationResult);
            MockUtil.mockPut(mockMvc, uri, json);
            // 修改成功验证

            // lr
            // 批量增加分类维度关系数（只增加了一个）
            uri = "/v0.6/categorypatterns/datas/relations/bulk";
            List<CategoryRelationViewModel> list = new LinkedList<CategoryRelationViewModel>();
            CategoryRelationViewModel createRelationParam2 = new CategoryRelationViewModel();
            createRelationParam2.setSource("ROOT");
            createRelationParam2.setTarget(firstCategoryDataList.get(1).getIdentifier());
            createRelationParam2.setRelationType("ASSOCIATE");
            createRelationParam2.setTags(new ArrayList<String>());
            createRelationParam2.setOrderNum(1);
            createRelationParam2.setEnable(true);
            createRelationParam2.setPatternPath(patternViewModel.getPatternName());
            createRelationParam2.setPattern(patternViewModel.getIdentifier());
            createRelationParam2.setLevelParent("ROOT");
            list.add(createRelationParam2);

            json = ObjectMapper.writeValueAsString(list);
            result = MockUtil.mockCreate(mockMvc, uri, json);

            List<CategoryRelationViewModel> createRelationResultList = ObjectMapper.readValue(result,
                    new TypeReference<List<CategoryRelationViewModel>>() {
                    });
            
            //增加两条重复的关系
            uri = "/v0.6/categorypatterns/datas/relations/bulk";
            list.add(createRelationParam2);
            json = ObjectMapper.writeValueAsString(list);
            result = MockUtil.mockCreate(mockMvc, uri, json);
            assertExceptionMessage(result, "批量关系数据中存在重复关系");
            
            // relationList.addAll(createRelationResultList);

            // lr
            // 批量删除分类维度之间的关系
            uri = "/v0.6/categorypatterns/datas/relations/bulk?";
            for (CategoryRelationViewModel crvm : createRelationResultList) {
                uri = uri + "crid=" + crvm.getIdentifier() + "&";
            }
            json = "";
            MockUtil.mockDelete(mockMvc, uri, json);
            
            // lr
            uri = "/v0.6/categories/datas/actions/apply?nd_code=" + Category_NDCODE[0];
            json = "";
            MockUtil.mockGet(mockMvc, uri, json);
            
            // lr:长度大于2的nd_code
            uri = "/v0.6/categories/datas/actions/apply?nd_code=" + "TEST" ;
            json = "";
            result = MockUtil.mockGet(mockMvc, uri, json);
//            assertExceptionMessage(result, "批量关系数据中存在重复关系");
            
            // lr:长度小于2的nd_code
            uri = "/v0.6/categories/datas/actions/apply?nd_code=" + "T" ;
            json = "";
            MockUtil.mockGet(mockMvc, uri, json);

            uri = "/v0.6/categorypatterns/datas/relations";
            // 细粒度
            for (int i = 0; i < CATEGORY_NUM; i++) {
                createRelationParam.setSource("ROOT");
                createRelationParam.setTarget(firstCategoryDataList.get(CATEGORY_NUM + i).getIdentifier());
                createRelationParam.setRelationType("ASSOCIATE");
                createRelationParam.setTags(new ArrayList<String>());
                createRelationParam.setOrderNum(i + 1);
                createRelationParam.setEnable(true);
                createRelationParam.setPatternPath(patternViewModel.getPatternName());
                createRelationParam.setPattern(patternViewModel.getIdentifier());
                createRelationParam.setLevelParent(firstCategoryDataList.get(0).getNdCode());
                json = ObjectMapper.writeValueAsString(createRelationParam);
                // 调用接口
                result = MockUtil.mockCreate(mockMvc, uri, json);
                // validate result
                createRelationResult = ObjectMapper.readValue(result, CategoryRelationViewModel.class);
                assertCategoryRelationEqualsWithoutId(createRelationParam, createRelationResult);
                relationList.add(createRelationResult);
            }
            //
            // 第二维
            // 细粒度
            for (int i = 0; i < CATEGORY_NUM; i++) {
                createRelationParam.setSource(firstCategoryDataList.get(CATEGORY_NUM).getIdentifier());
                createRelationParam.setTarget(secondCategoryDataList.get(CATEGORY_NUM + i).getIdentifier());
                createRelationParam.setRelationType("ASSOCIATE");
                createRelationParam.setTags(new ArrayList<String>());
                createRelationParam.setOrderNum(i + 1);
                createRelationParam.setEnable(true);
                createRelationParam.setPatternPath(patternViewModel.getPatternName() + "/"
                        + firstCategoryDataList.get(CATEGORY_NUM).getNdCode());
                createRelationParam.setPattern(patternViewModel.getIdentifier());
                createRelationParam.setLevelParent("ROOT");
                json = ObjectMapper.writeValueAsString(createRelationParam);
                // 调用接口
                result = MockUtil.mockCreate(mockMvc, uri, json);
                // validate result
                createRelationResult = ObjectMapper.readValue(result, CategoryRelationViewModel.class);
                assertCategoryRelationEqualsWithoutId(createRelationParam, createRelationResult);
                relationList.add(createRelationResult);
            }

            // 第三维
            // 细粒度
            for (int i = 0; i < CATEGORY_NUM; i++) {
                createRelationParam.setSource(secondCategoryDataList.get(CATEGORY_NUM).getIdentifier());
                createRelationParam.setTarget(thirdCategoryDataList.get(CATEGORY_NUM + i).getIdentifier());
                createRelationParam.setRelationType("ASSOCIATE");
                createRelationParam.setTags(new ArrayList<String>());
                createRelationParam.setOrderNum(i + 1);
                createRelationParam.setEnable(true);
                createRelationParam.setPatternPath(patternViewModel.getPatternName() + "/"
                        + firstCategoryDataList.get(CATEGORY_NUM).getNdCode() + "/"
                        + secondCategoryDataList.get(CATEGORY_NUM).getNdCode());
                createRelationParam.setPattern(patternViewModel.getIdentifier());
                createRelationParam.setLevelParent("ROOT");
                json = ObjectMapper.writeValueAsString(createRelationParam);
                // 调用接口
                result = MockUtil.mockCreate(mockMvc, uri, json);
                // validate result
                createRelationResult = ObjectMapper.readValue(result, CategoryRelationViewModel.class);
                assertCategoryRelationEqualsWithoutId(createRelationParam, createRelationResult);
                relationList.add(createRelationResult);
            }


            // 四、维度数据关系：查
            // 四、维度数据关系：删

            for (CategoryRelationViewModel model : relationList) {
                uri = "/v0.6/categorypatterns/datas/relations/" + model.getIdentifier();
                json = "";
                result = MockUtil.mockDelete(mockMvc, uri, json);
            }
            relationList.clear();

            logger.info("维度数据关系 controller 层测试结束");

            // 后续的清除工作，放在after 中（先维度数据，再分类维度，最后维度模式）

        } catch (Exception e) {
            logger.error(e);
        }
    }

    /**
    * 
    * @param updatePatternResult
    * @param detailCategoryPatternModel
    * @since 
    */
    private void assertCategoryPatternEqual(CategoryPatternViewModel updatePatternResult,
            CategoryPatternViewModel detailCategoryPatternModel) {
        assertCategoryPatternEqualsWithoutId(updatePatternResult, detailCategoryPatternModel);
        Assert.assertEquals("Identifier 不对", updatePatternResult.getIdentifier(),
                detailCategoryPatternModel.getIdentifier());

    }

    /**
     * 
     * @param updateCategoryDataResult
     * @param detailCategoryDataModel
     * @since 
     */
    private void assertCategoryDataEqual(CategoryDataViewModel updateCategoryDataResult,
            CategoryDataViewModel detailCategoryDataModel) {
        assertCategoryDataEqualsWithoutId(updateCategoryDataResult, detailCategoryDataModel);
        Assert.assertEquals("uuid 不一致", updateCategoryDataResult.getIdentifier(),
                detailCategoryDataModel.getIdentifier());
        Assert.assertEquals("DimensionPath 不一致", updateCategoryDataResult.getDimensionPath(),
                detailCategoryDataModel.getDimensionPath());
    }

    /**
     * 
     * @param updateCategoryresult
     * @param detailCategoryModel
     * @since 
     */
    private void assertCategoryEqual(CategoryViewModel updateCategoryresult, CategoryViewModel detailCategoryModel) {
        assertCategoryEqualWithoutId(updateCategoryresult, detailCategoryModel);
        Assert.assertEquals("uuid 不一致", updateCategoryresult.getIdentifier(), detailCategoryModel.getIdentifier());

    }

    private void assertCategoryRelationEqualsWithoutId(CategoryRelationViewModel createRelationParam,
            CategoryRelationViewModel createRelationResult) {
        Assert.assertEquals("source 不对", createRelationParam.getSource(), createRelationResult.getSource());
        Assert.assertEquals("target 不对", createRelationParam.getTarget(), createRelationResult.getTarget());
        Assert.assertEquals("relationType 不对", createRelationParam.getRelationType(),
                createRelationResult.getRelationType());
        Assert.assertEquals("orderNum 不对", createRelationParam.getOrderNum(), createRelationResult.getOrderNum(),
                MIN_DELTA);
        Assert.assertEquals("enable 不对", createRelationParam.isEnable(), createRelationResult.isEnable());
        Assert.assertEquals("patternPath 不对", createRelationParam.getPatternPath(),
                createRelationResult.getPatternPath());
        Assert.assertEquals("pattern 不对", createRelationParam.getPattern(), createRelationResult.getPattern());
        Assert.assertEquals("levelParent 不对", createRelationParam.getLevelParent(),
                createRelationResult.getLevelParent());

    }

    private void createCategoryDataList(CategoryViewModel category, List<CategoryDataViewModel> list) {
        // 第一层
        for (int i = 0; i < CATEGORY_NUM; i++) {
            String uri = "/v0.6/categories/datas";
            CategoryDataViewModel createCategoryDataParam = new CategoryDataViewModel();
            createCategoryDataParam.setTitle(category.getTitle() + i);
            createCategoryDataParam.setShortName(category.getShortName() + i);
            createCategoryDataParam.setParent("ROOT");
            createCategoryDataParam.setNdCode(category.getNdCode() + "0" + (i + 1) + "00");// FIXME i<9
            createCategoryDataParam.setDescription(category.getDescription() + i);
            createCategoryDataParam.setGbCode(category.getGbCode());
            createCategoryDataParam.setOrderNum(i + 1);
            createCategoryDataParam.setCategory(category.getIdentifier());
            try {
                String json = ObjectMapper.writeValueAsString(createCategoryDataParam);
                String result = MockUtil.mockCreate(mockMvc, uri, json);
                CategoryDataViewModel createCategoryDataResult = ObjectMapper.readValue(result,
                        CategoryDataViewModel.class);
                assertCategoryDataEqualsWithoutId(createCategoryDataParam, createCategoryDataResult);
                list.add(createCategoryDataResult);
            } catch (Exception e) {
                logger.info("创建维度数据失败");
            }

        }
        Assert.assertEquals("维度数据个数不对", CATEGORY_NUM, list.size());

        // 第二层
        for (int j = 0; j < CATEGORY_NUM; j++) {
            for (int i = 0; i < CATEGORY_NUM; i++) {
                String uri = "/v0.6/categories/datas";
                CategoryDataViewModel createCategoryDataParam = new CategoryDataViewModel();
                createCategoryDataParam.setTitle((category.getTitle() + i) + j);
                createCategoryDataParam.setShortName((category.getShortName() + i) + j);
                createCategoryDataParam.setParent(list.get(j).getIdentifier());
                createCategoryDataParam.setNdCode(category.getNdCode() + "0" + (j + 1) + "0" + (i + 1));
                createCategoryDataParam.setDescription(category.getDescription() + i);
                createCategoryDataParam.setGbCode(category.getGbCode());
                createCategoryDataParam.setOrderNum(i + 1);
                createCategoryDataParam.setCategory(category.getIdentifier());
                try {
                    String json = ObjectMapper.writeValueAsString(createCategoryDataParam);
                    String result = MockUtil.mockCreate(mockMvc, uri, json);
                    CategoryDataViewModel createCategoryDataResult = ObjectMapper.readValue(result,
                            CategoryDataViewModel.class);
                    assertCategoryDataEqualsWithoutId(createCategoryDataParam, createCategoryDataResult);
                    list.add(createCategoryDataResult);
                } catch (Exception e) {
                    logger.info("创建维度数据失败");
                }

            }

        }
        Assert.assertEquals("维度数据个数不对", CATEGORY_NUM * (CATEGORY_NUM + 1), list.size());
    }

    private void clearCategoryPattern(CategoryPatternViewModel model) {
        if (model != null) {
            String patternId = model.getIdentifier();
            String uri = "/v0.6/categorypatterns/" + patternId;
            String json = "";
            try {
                MockUtil.mockDelete(mockMvc, uri, json);
            } catch (Exception e) {
                logger.info("清除维度模式失败");
            }
        }
        // model = null; 这样没用
    }

    private void clearCategoryDataAll() {
        // 第三维
        clearCategoryDataList(thirdCategoryDataList);
        // 第二维
        clearCategoryDataList(secondCategoryDataList);
        // 第一维
        clearCategoryDataList(firstCategoryDataList);
    }

    private void assertCategoryPatternEqualsWithoutId(CategoryPatternViewModel createPatternParam,
            CategoryPatternViewModel createPatternResult) {
        Assert.assertEquals("title 不对", createPatternParam.getTitle(), createPatternResult.getTitle());
        Assert.assertEquals("patternName 不对", createPatternParam.getPatternName(), createPatternResult.getPatternName());
        Assert.assertEquals("purpose 不对", createPatternParam.getPurpose(), createPatternResult.getPurpose());
        Assert.assertEquals("scope 不对", createPatternParam.getScope(), createPatternResult.getScope());
        Assert.assertEquals("description 不对", createPatternParam.getDescription(), createPatternResult.getDescription());

        /**
         * 新增字段
         */
        Assert.assertEquals("PatternPath 不对", createPatternParam.getPatternPath(), createPatternResult.getPatternPath());

    }

    private CategoryPatternViewModel createCategoryPatternViewModel() {
        CategoryPatternViewModel result = new CategoryPatternViewModel();
        result.setTitle("第一维度，第二维度，第三维度");
        result.setPatternName(PATTERN_NAME);
        result.setPurpose("just for test pattern");
        result.setScope("test");
        result.setDescription("just for lsm unit test");

        /**
         * 新增字段(patternPath)
         */
        StringBuffer patternPath = new StringBuffer();
        for (String ndCode : Category_NDCODE) {
            patternPath.append(ndCode);
        }
        result.setPatternPath(patternPath.toString());

        return result;
    }

    private void assertCategoryDataEqualsWithoutId(CategoryDataViewModel createCategoryParam,
            CategoryDataViewModel createCategoryResult) {
        Assert.assertEquals("title 不一致", createCategoryParam.getTitle(), createCategoryResult.getTitle());
        Assert.assertEquals("shortName 不一致", createCategoryParam.getShortName(), createCategoryResult.getShortName());
        Assert.assertEquals("ndCode 不一致", createCategoryParam.getNdCode(), createCategoryResult.getNdCode());
        Assert.assertEquals("parent 不一致", createCategoryParam.getParent(), createCategoryResult.getParent());
        Assert.assertEquals("description 不一致", createCategoryParam.getDescription(),
                createCategoryResult.getDescription());
        Assert.assertEquals("orderNum 不一致", createCategoryParam.getOrderNum(), createCategoryResult.getOrderNum());
        Assert.assertEquals("gbCode 不一致", createCategoryParam.getGbCode(), createCategoryResult.getGbCode());
        Assert.assertEquals("category 不一致", createCategoryParam.getCategory(), createCategoryResult.getCategory());
        // Assert.assertEquals("dimensionPath 不一致",
        // createCategoryParam.getDimensionPath(),
        // createCategoryResult.getDimensionPath());

    }

    private void assertCategoryEqualWithoutId(CategoryViewModel createCategoryParam,
            CategoryViewModel createCategoryResult) {
        Assert.assertEquals("title 不一致", createCategoryParam.getTitle(), createCategoryResult.getTitle());
        Assert.assertEquals("shortName 不一致", createCategoryParam.getShortName(), createCategoryResult.getShortName());
        Assert.assertEquals("purpose 不一致", createCategoryParam.getPurpose(), createCategoryResult.getPurpose());
        Assert.assertEquals("ndCode 不一致", createCategoryParam.getNdCode(), createCategoryResult.getNdCode());
        Assert.assertEquals("source 不一致", createCategoryParam.getSource(), createCategoryResult.getSource());
        Assert.assertEquals("description 不一致", createCategoryParam.getDescription(),
                createCategoryResult.getDescription());
        Assert.assertEquals("gbCode 不一致", createCategoryParam.getGbCode(), createCategoryResult.getGbCode());

    }

    private CategoryViewModel createCategory(String title, String ndCode) {
        CategoryViewModel model = new CategoryViewModel();
        model.setTitle(title);
        model.setShortName("first dimension");
        model.setPurpose("just for test");
        model.setNdCode(ndCode);
        model.setSource("LSM");
        model.setDescription("lsm from nd");
        model.setGbCode("A");
        return model;
    }

    public void before() {
        // String ndCode = "$SB0000";
        // getCategoryDataIdByNdCode(ndCode);
    }

    public void after() {
        // super.after(); //不需要依赖于其它的资源
        // 删除所有的资源;
        // 维度数据：
        clearCategoryDataAll();

        // 分类维度：
        clearCategoryList(categoryList);

        // 模式
        clearCategoryPattern(patternViewModel);
        patternViewModel = null;

    }

    private void clearCategoryList(List<CategoryViewModel> list) {
        if (!list.isEmpty()) {
            for (CategoryViewModel model : list) {
                String id = model.getIdentifier();
                String uri = "/v0.6/categories/" + id;
                String json = "";
                try {
                    MockUtil.mockDelete(mockMvc, uri, json);
                } catch (Exception e) {
                    logger.info("清除分类维度资源失败");
                }
            }
            list.clear();

        }

    }

    private void clearCategoryDataList(List<CategoryDataViewModel> list) {
        if (!list.isEmpty()) {
            // for (CategoryDataViewModel model : list) {
            // String id = model.getIdentifier();
            // String uri = "/v0.6/categories/datas/" + id;
            // String json = "";
            // try {
            // MockUtil.mockDelete(mockMvc, uri, json);
            // } catch (Exception e) {
            // logger.info("清除分类维度资源失败");
            // }
            //
            // } //foreach 怎么实现后序
            int num = list.size();
            for (int i = num - 1; i >= 0; i--) {
                CategoryDataViewModel model = list.get(i);
                String id = model.getIdentifier();
                String uri = "/v0.6/categories/datas/" + id;
                String json = "";
                try {
                    MockUtil.mockDelete(mockMvc, uri, json);
                } catch (Exception e) {
                    logger.info("清除分类维度资源失败");
                }

            }
            list.clear();

        }

    }
    
    private void assertExceptionMessage(String result ,String message){
        Map<String, Object>  resultMap = ObjectUtils.fromJson(result,Map.class);
        Assert.assertEquals(resultMap.get("message") , message);
    }
    
    /**
     * 
     * 测试接口
     * <br>
     * Created 2015年5月6日 下午5:35:33
     * 
     * @throws UnsupportedEncodingException
     * @throws Exception
     * @author linsm
     */
//    @Test
    public void test() {
        String json = "";
        String uri = "";
        String result = "";

        try {
            logger.info("分类维度 controller层测试开始");

            // 一、分类维度：增
            // 参数设置：
            uri = "/v0.6/categories";
            CategoryViewModel createCategoryParam = createCategory(CATEGORY_TITLE[0], Category_NDCODE[0]);
            json = ObjectMapper.writeValueAsString(createCategoryParam);
            result = MockUtil.mockCreate(mockMvc, uri, json);
            CategoryViewModel createCategoryResult = ObjectMapper.readValue(result, CategoryViewModel.class);
            // 验证结果：
            assertCategoryEqualWithoutId(createCategoryParam, createCategoryResult);
            logger.info("创建分类维度测试成功");
            categoryList.add(createCategoryResult); // 把资源添加到全局

            // 一、分类维度：改 ,依赖于创建接口，需要ID和对应的数据
            // 参数设置
            String categoryId = categoryList.get(0).getIdentifier();
            uri = "/v0.6/categories/" + categoryId; // 暂时修改第一个资源
            createCategoryParam.setTitle(FIRST_DIMMENSION_UPDATE_TITLE);
            createCategoryParam.setNdCode(FIRST_DIMMENSION_UPDATE_NDCODE);
            json = ObjectMapper.writeValueAsString(createCategoryParam);
            result = MockUtil.mockPut(mockMvc, uri, json);
            CategoryViewModel updateCategoryresult = ObjectMapper.readValue(result, CategoryViewModel.class);
            // 验证结果：
            assertCategoryEqualWithoutId(createCategoryParam, updateCategoryresult);
            Assert.assertEquals("分类维度id不对", categoryId, updateCategoryresult.getIdentifier());
            logger.info("修改分类维度测试成功");

            // 依赖于更新后的结果： updateCategoryResult
            // 一、分类维度：取
            // 1、uuid
            uri = "/v0.6/categories/" + updateCategoryresult.getIdentifier();
            json = "";
            result = MockUtil.mockGet(mockMvc, uri, json);
            CategoryViewModel detailCategoryModel = ObjectMapper.readValue(result, CategoryViewModel.class);
            // 验证结果
            assertCategoryEqual(updateCategoryresult, detailCategoryModel);
            logger.info("通过uuid取分类维度测试成功");

            // 2、nd_code
            uri = "/v0.6/categories/" + updateCategoryresult.getNdCode();
            json = "";
            result = MockUtil.mockGet(mockMvc, uri, json);
            detailCategoryModel = ObjectMapper.readValue(result, CategoryViewModel.class);
            // 验证结果
            assertCategoryEqual(updateCategoryresult, detailCategoryModel);
            logger.info("通过nd_code取分类维度测试成功");

            // 3、nd_code（批量）（暂时也只取一个）
            uri = "/v0.6/categories/list?nd_code=" + updateCategoryresult.getNdCode();
            json = "";
            result = MockUtil.mockGet(mockMvc, uri, json);
            Map<String, CategoryViewModel> batchDetailCategoryModel = ObjectMapper.readValue(result,
                    new TypeReference<Map<String, CategoryViewModel>>() {
                    });
            detailCategoryModel = batchDetailCategoryModel.get(updateCategoryresult.getNdCode());
            // 验证结果
            assertCategoryEqual(updateCategoryresult, detailCategoryModel);
            logger.info("通过nd_code（批量）取分类维度测试成功");

            // 一、分类维度：查
            uri = "/v0.6/categories?words=lsm&limit=(0,800)"; // 确保自己创建的内容可以返回
            json = "";
            result = MockUtil.mockGet(mockMvc, uri, json);
            ListViewModel<CategoryViewModel> queryCategoryListResult = ObjectMapper.readValue(result,
                    new TypeReference<ListViewModel<CategoryViewModel>>() {
                    });
            // 验证结果：
            // 查询的结果中存在我自己创建的内容
            total = queryCategoryListResult.getTotal();
            Assert.assertTrue("个数不对", total > 0);
            for (CategoryViewModel viewModel : queryCategoryListResult.getItems()) {
                if (viewModel.getNdCode().equals(createCategoryParam.getNdCode())) {
                    assertCategoryEqualWithoutId(createCategoryParam, viewModel);
                }
            }
            logger.info("查询分类维度测试成功");

            // 一、分类维度：删 (依赖于创建的接口，需要uuid)
            categoryId = categoryList.get(0).getIdentifier();
            categoryList.remove(0);// 怎么保证一定成功？
            uri = "/v0.6/categories/" + categoryId;
            json = "";
            result = MockUtil.mockDelete(mockMvc, uri, json);
            // 验证结果： 通过再次查询，个数应该少1;
            total--;
            uri = "/v0.6/categories?words=lsm&limit=(0,800)"; // 确保自己创建的内容可以返回
            json = "";
            result = MockUtil.mockGet(mockMvc, uri, json);
            queryCategoryListResult = ObjectMapper.readValue(result,
                    new TypeReference<ListViewModel<CategoryViewModel>>() {
                    });
            // 验证结果：
            Assert.assertEquals("删除后个数不对", total, queryCategoryListResult.getTotal());
            logger.info("删除分类维度测试成功");
            // 清除所有分类维度
            clearCategoryList(categoryList);

            logger.info("分类维度 controller层测试结束");

            // 二、维度数据
            logger.info("维度数据 controller 层测试开始");
            {
                // 一、分类维度：增 (维度数据依赖于分类维度，为了测试用，创建一个维度分类
                // 参数设置：
                uri = "/v0.6/categories";
                createCategoryParam = createCategory(CATEGORY_TITLE[0], Category_NDCODE[0]);
                json = ObjectMapper.writeValueAsString(createCategoryParam);
                result = MockUtil.mockCreate(mockMvc, uri, json);
                createCategoryResult = ObjectMapper.readValue(result, CategoryViewModel.class);
                categoryList.add(createCategoryResult); // 把资源添加到全局
                // 验证结果：
                assertCategoryEqualWithoutId(createCategoryParam, createCategoryResult);
                logger.info("创建分类维度测试成功");
            }

            // 二、维度数据:增
            uri = "/v0.6/categories/datas";
            CategoryDataViewModel createCategoryDataParam = new CategoryDataViewModel();
            createCategoryDataParam.setTitle("第一维度 一");
            createCategoryDataParam.setShortName("first dimension first");
            createCategoryDataParam.setParent("ROOT");
            createCategoryDataParam.setNdCode(categoryList.get(0).getNdCode() + "0100");
            createCategoryDataParam.setDescription("lsm 第一维度第一数据");
            createCategoryDataParam.setGbCode("A");
            createCategoryDataParam.setOrderNum(1);
            createCategoryDataParam.setCategory(categoryList.get(0).getIdentifier()); // 暂时还是采用第一个
            json = ObjectMapper.writeValueAsString(createCategoryDataParam);
            result = MockUtil.mockCreate(mockMvc, uri, json);
            // 验证结果
            CategoryDataViewModel createCategoryDataResult = ObjectMapper
                    .readValue(result, CategoryDataViewModel.class);
            assertCategoryDataEqualsWithoutId(createCategoryDataParam, createCategoryDataResult);
            firstCategoryDataList.add(createCategoryDataResult);

            logger.info("创建维度数据 测试成功");
            // 二、维度数据:改
            String categoryDataId = firstCategoryDataList.get(0).getIdentifier();
            uri = "/v0.6/categories/datas/" + categoryDataId;
            createCategoryDataParam.setTitle("更新后的Title");
            json = ObjectMapper.writeValueAsString(createCategoryDataParam);
            result = MockUtil.mockPut(mockMvc, uri, json);
            // 验证结果
            CategoryDataViewModel updateCategoryDataResult = ObjectMapper
                    .readValue(result, CategoryDataViewModel.class);
            assertCategoryDataEqualsWithoutId(createCategoryDataParam, updateCategoryDataResult);
            Assert.assertEquals("维度数据id 不对", categoryDataId, updateCategoryDataResult.getIdentifier());
            logger.info("修改维度数据 测试成功");

            // 依赖于更新后的结果： updateCategoryDataResult
            // 一、维度数据：取
            // 1、uuid
            uri = "/v0.6/categories/datas/" + updateCategoryDataResult.getIdentifier();
            json = "";
            result = MockUtil.mockGet(mockMvc, uri, json);
            CategoryDataViewModel detailCategoryDataModel = ObjectMapper.readValue(result, CategoryDataViewModel.class);
            // 验证结果
            assertCategoryDataEqual(updateCategoryDataResult, detailCategoryDataModel);
            logger.info("通过uuid取维度数据测试成功");

            // 2、nd_code
            uri = "/v0.6/categories/datas/" + updateCategoryDataResult.getNdCode();
            json = "";
            result = MockUtil.mockGet(mockMvc, uri, json);
            detailCategoryDataModel = ObjectMapper.readValue(result, CategoryDataViewModel.class);
            // 验证结果
            assertCategoryDataEqual(updateCategoryDataResult, detailCategoryDataModel);
            logger.info("通过nd_code取维度数据测试成功");

            // 3、nd_code（批量）（暂时也只取一个）
            uri = "/v0.6/categories/datas/list?nd_code=" + updateCategoryDataResult.getNdCode();
            json = "";
            result = MockUtil.mockGet(mockMvc, uri, json);
            Map<String, CategoryDataViewModel> batchDetailCategoryDataModel = ObjectMapper.readValue(result,
                    new TypeReference<Map<String, CategoryDataViewModel>>() {
                    });
            detailCategoryDataModel = batchDetailCategoryDataModel.get(updateCategoryDataResult.getNdCode());
            // 验证结果
            assertCategoryDataEqual(updateCategoryDataResult, detailCategoryDataModel);
            logger.info("通过nd_code（批量）维度数据测试成功");

            // 二、维度数据:查
            uri = "/v0.6/categories/" + Category_NDCODE[0] + "/datas?words=lsm&limit=(0,100)";
            json = "";
            result = MockUtil.mockGet(mockMvc, uri, json);
            ListViewModel<CategoryDataViewModel> queryCategoryDataListResult = ObjectMapper.readValue(result,
                    new TypeReference<ListViewModel<CategoryDataViewModel>>() {
                    });
            // 验证结果：
            // 查询的结果中存在我自己创建的内容
            total = queryCategoryDataListResult.getTotal();
            Assert.assertTrue("个数不对", total > 0);
            for (CategoryDataViewModel viewModel : queryCategoryDataListResult.getItems()) {
                if (viewModel.getNdCode().equals(createCategoryDataParam.getNdCode())) {
                    assertCategoryDataEqualsWithoutId(createCategoryDataParam, viewModel);
                }
            }
            logger.info("查询维度数据 测试成功");
            // 二、维度数据:删
            categoryDataId = firstCategoryDataList.get(0).getIdentifier();
            firstCategoryDataList.remove(0);// 怎么保证一定成功？
            uri = "/v0.6/categories/datas/" + categoryDataId;
            json = "";
            result = MockUtil.mockDelete(mockMvc, uri, json);
            // 验证结果： 通过再次查询，个数应该少1;
            total--;
            uri = "/v0.6/categories/" + Category_NDCODE[0] + "/datas?words=lsm&limit=(0,100)"; // 确保自己创建的内容可以返回
            json = "";
            result = MockUtil.mockGet(mockMvc, uri, json);
            queryCategoryDataListResult = ObjectMapper.readValue(result,
                    new TypeReference<ListViewModel<CategoryDataViewModel>>() {
                    });
            // 验证结果：
            Assert.assertEquals("删除后个数不对", total, queryCategoryDataListResult.getTotal());
            logger.info("删除维度数据 测试成功");
            // 清除所有维度数据
            clearCategoryDataAll();
            // 清除依赖的分类维度
            clearCategoryList(categoryList);
            logger.info("维度数据 controller 层测试结束");

            // 三、维度模式：增、改、查、删
            logger.info("维度模式 controller 层测试开始");
            // 三、维度模式：增
            uri = "/v0.6/categorypatterns";
            CategoryPatternViewModel createPatternParam = createCategoryPatternViewModel();
            json = ObjectMapper.writeValueAsString(createPatternParam);
            result = MockUtil.mockCreate(mockMvc, uri, json);
            // 验证结果：
            CategoryPatternViewModel createPatternResult = ObjectMapper.readValue(result,
                    CategoryPatternViewModel.class);
            assertCategoryPatternEqualsWithoutId(createPatternParam, createPatternResult);
            logger.info("维度模式 创建 成功");
            patternViewModel = createPatternResult; // 保存在全局中

            // 三、维度模式：改
            // 设置参数
            uri = "/v0.6/categorypatterns/" + patternViewModel.getIdentifier();
            CategoryPatternViewModel updatePatternParam = patternViewModel;
            updatePatternParam.setTitle("更新后 " + patternViewModel.getTitle());
            json = ObjectMapper.writeValueAsString(updatePatternParam);
            // 调用接口
            result = MockUtil.mockPut(mockMvc, uri, json);
            // 验证结果
            CategoryPatternViewModel updatePatternResult = ObjectMapper.readValue(result,
                    CategoryPatternViewModel.class);
            assertCategoryPatternEqualsWithoutId(updatePatternParam, updatePatternResult);
            logger.info("维度模式 修改 成功");
            patternViewModel = updatePatternResult;

            // 依赖于更新后的结果： updatePatternResult
            // 一、模式：取
            // 1、uuid
            uri = "/v0.6/categorypatterns/" + updatePatternResult.getIdentifier();
            json = "";
            result = MockUtil.mockGet(mockMvc, uri, json);
            CategoryPatternViewModel detailCategoryPatternModel = ObjectMapper.readValue(result,
                    CategoryPatternViewModel.class);
            // 验证结果
            assertCategoryPatternEqual(updatePatternResult, detailCategoryPatternModel);
            logger.info("通过uuid取模式测试成功");

            // 2、pattern_name
            uri = "/v0.6/categorypatterns/" + updatePatternResult.getPatternName();
            json = "";
            result = MockUtil.mockGet(mockMvc, uri, json);
            detailCategoryPatternModel = ObjectMapper.readValue(result, CategoryPatternViewModel.class);
            // 验证结果
            assertCategoryPatternEqual(updatePatternResult, detailCategoryPatternModel);
            logger.info("通过pattern_name取模式测试成功");

            // 3、pattern_name（批量）（暂时也只取一个）
            uri = "/v0.6/categorypatterns/list?pattern_name=" + updatePatternResult.getPatternName();
            json = "";
            result = MockUtil.mockGet(mockMvc, uri, json);
            Map<String, CategoryPatternViewModel> batchDetailCategoryPatternModel = ObjectMapper.readValue(result,
                    new TypeReference<Map<String, CategoryPatternViewModel>>() {
                    });
            detailCategoryPatternModel = batchDetailCategoryPatternModel.get(updatePatternResult.getPatternName());
            // 验证结果
            assertCategoryPatternEqual(updatePatternResult, detailCategoryPatternModel);
            logger.info("通过pattern_name（批量）取模式测试成功");

            // 三、维度模式：查
            // 设置入参:
            uri = "/v0.6/categorypatterns?words=lsm&limit=(0,100)";
            json = "";
            // 调用接口
            result = MockUtil.mockGet(mockMvc, uri, json);
            // 验证结果
            ListViewModel<CategoryPatternViewModel> queryPatternResult = ObjectMapper.readValue(result,
                    new TypeReference<ListViewModel<CategoryPatternViewModel>>() {
                    });
            total = queryPatternResult.getTotal();
            Assert.assertTrue("查询个数不对", total > 0);
            for (CategoryPatternViewModel viewModel : queryPatternResult.getItems()) {
                if (viewModel.getIdentifier().equals(patternViewModel.getIdentifier())) {
                    // 找到我自己创建的对应模式
                    assertCategoryPatternEqualsWithoutId(patternViewModel, viewModel);
                }
            }

            // //

            logger.info("维度模式 查询 成功");
            // 三、维度模式：删
            String patternId = patternViewModel.getIdentifier();
            patternViewModel = null; // 已删除
            uri = "/v0.6/categorypatterns/" + patternId;
            json = "";
            result = MockUtil.mockDelete(mockMvc, uri, json);
            // validate result (by query)
            total--;
            uri = "/v0.6/categorypatterns?words=lsm&limit=(0,100)";
            json = "";
            // 调用接口
            result = MockUtil.mockGet(mockMvc, uri, json);
            // 验证结果
            queryPatternResult = ObjectMapper.readValue(result,
                    new TypeReference<ListViewModel<CategoryPatternViewModel>>() {
                    });
            Assert.assertEquals("删除后个数不对", total, queryPatternResult.getTotal());
            for (CategoryPatternViewModel viewModel : queryPatternResult.getItems()) {
                Assert.assertNotEquals("对应的模式已删除，但是还出现", patternId, viewModel.getIdentifier());
            }
            logger.info("维度模式 删除 成功");
            //
            clearCategoryPattern(patternViewModel);
            patternViewModel = null;
            logger.info("维度模式 controller 层测试结束");

            // 四、维度数据关系：增、查、删
            // 创建依赖的资源
            {
                // 维度模式
                uri = "/v0.6/categorypatterns";
                createPatternParam = createCategoryPatternViewModel();
                json = ObjectMapper.writeValueAsString(createPatternParam);
                result = MockUtil.mockCreate(mockMvc, uri, json);
                // 验证结果：
                createPatternResult = ObjectMapper.readValue(result, CategoryPatternViewModel.class);
                assertCategoryPatternEqualsWithoutId(createPatternParam, createPatternResult);
                logger.info("维度模式 创建 成功");
                patternViewModel = createPatternResult; // 保存在全局中
                // 分类维度
                for (int i = 0; i < CATEGORY_NUM; i++) {
                    uri = "/v0.6/categories";
                    createCategoryParam = createCategory(CATEGORY_TITLE[i], Category_NDCODE[i]);
                    json = ObjectMapper.writeValueAsString(createCategoryParam);
                    result = MockUtil.mockCreate(mockMvc, uri, json);
                    createCategoryResult = ObjectMapper.readValue(result, CategoryViewModel.class);
                    // 验证结果：
                    assertCategoryEqualWithoutId(createCategoryParam, createCategoryResult);
                    logger.info("创建分类维度测试成功");
                    categoryList.add(createCategoryResult); // 把资源添加到全局
                }
                // 维度数据
                createCategoryDataList(categoryList.get(0), firstCategoryDataList);
                createCategoryDataList(categoryList.get(1), secondCategoryDataList);
                createCategoryDataList(categoryList.get(2), thirdCategoryDataList);
            }
            logger.info("维度数据关系 controller 层测试开始");
            // 四、维度数据关系：增
            // 第一层关系：
            // 粗粒度
            uri = "/v0.6/categorypatterns/datas/relations";
            CategoryRelationViewModel createRelationParam = new CategoryRelationViewModel();
            createRelationParam.setSource("ROOT");
            createRelationParam.setTarget(firstCategoryDataList.get(0).getIdentifier());
            createRelationParam.setRelationType("ASSOCIATE");
            createRelationParam.setTags(new ArrayList<String>());
            createRelationParam.setOrderNum(1);
            createRelationParam.setEnable(true);
            createRelationParam.setPatternPath(patternViewModel.getPatternName());
            createRelationParam.setPattern(patternViewModel.getIdentifier());
            createRelationParam.setLevelParent("ROOT");
            json = ObjectMapper.writeValueAsString(createRelationParam);
            // 调用接口
            result = MockUtil.mockCreate(mockMvc, uri, json);
            // validate result
            CategoryRelationViewModel createRelationResult = ObjectMapper.readValue(result,
                    CategoryRelationViewModel.class);
            assertCategoryRelationEqualsWithoutId(createRelationParam, createRelationResult);
            relationList.add(createRelationResult);

            // lr
            // 指定关系条件，查询满足条件的关系个数
            uri = "/v0.6/categories/relations/actions/count?" + "patternPath=" + createRelationResult.getPatternPath()
                    + "&nd_code=" + firstCategoryDataList.get(0).getNdCode();
            json = "";
            MockUtil.mockGet(mockMvc, uri, json);

            // lr
            // 查询分类维度之间的关系
            uri = "/v0.6/categories/relations?patternPath=" + createRelationResult.getPatternPath();
            json = "";
            MockUtil.mockGet(mockMvc, uri, json);

            // lr
            // 修改分类维度应用模式下的维度数据关系
            uri = "/v0.6/categorypatterns/datas/relations/" + createRelationResult.getIdentifier();
            json = ObjectMapper.writeValueAsString(createRelationResult);
            MockUtil.mockPut(mockMvc, uri, json);
            // 修改成功验证

            // lr
            // 批量增加分类维度关系数（只增加了一个）
            uri = "/v0.6/categorypatterns/datas/relations/bulk";
            List<CategoryRelationViewModel> list = new LinkedList<CategoryRelationViewModel>();
            CategoryRelationViewModel createRelationParam2 = new CategoryRelationViewModel();
            createRelationParam2.setSource("ROOT");
            createRelationParam2.setTarget(firstCategoryDataList.get(1).getIdentifier());
            createRelationParam2.setRelationType("ASSOCIATE");
            createRelationParam2.setTags(new ArrayList<String>());
            createRelationParam2.setOrderNum(1);
            createRelationParam2.setEnable(true);
            createRelationParam2.setPatternPath(patternViewModel.getPatternName());
            createRelationParam2.setPattern(patternViewModel.getIdentifier());
            createRelationParam2.setLevelParent("ROOT");
            list.add(createRelationParam2);

            json = ObjectMapper.writeValueAsString(list);
            result = MockUtil.mockCreate(mockMvc, uri, json);

            List<CategoryRelationViewModel> createRelationResultList = ObjectMapper.readValue(result,
                    new TypeReference<List<CategoryRelationViewModel>>() {
                    });
            // relationList.addAll(createRelationResultList);

            // lr
            // 批量删除分类维度之间的关系
            uri = "/v0.6/categorypatterns/datas/relations/bulk?";
            for (CategoryRelationViewModel crvm : createRelationResultList) {
                uri = uri + "crid=" + crvm.getIdentifier() + "&";
            }
            json = "";
            MockUtil.mockDelete(mockMvc, uri, json);

            // lr
            uri = "/v0.6/categories/datas/actions/apply?nd_code=" + Category_NDCODE[0];
            json = "";
            MockUtil.mockGet(mockMvc, uri, json);

            uri = "/v0.6/categorypatterns/datas/relations";
            // 细粒度
            for (int i = 0; i < CATEGORY_NUM; i++) {
                createRelationParam.setSource("ROOT");
                createRelationParam.setTarget(firstCategoryDataList.get(CATEGORY_NUM + i).getIdentifier());
                createRelationParam.setRelationType("ASSOCIATE");
                createRelationParam.setTags(new ArrayList<String>());
                createRelationParam.setOrderNum(i + 1);
                createRelationParam.setEnable(true);
                createRelationParam.setPatternPath(patternViewModel.getPatternName());
                createRelationParam.setPattern(patternViewModel.getIdentifier());
                createRelationParam.setLevelParent(firstCategoryDataList.get(0).getNdCode());
                json = ObjectMapper.writeValueAsString(createRelationParam);
                // 调用接口
                result = MockUtil.mockCreate(mockMvc, uri, json);
                // validate result
                createRelationResult = ObjectMapper.readValue(result, CategoryRelationViewModel.class);
                assertCategoryRelationEqualsWithoutId(createRelationParam, createRelationResult);
                relationList.add(createRelationResult);
            }
            //
            // 第二维
            // 细粒度
            for (int i = 0; i < CATEGORY_NUM; i++) {
                createRelationParam.setSource(firstCategoryDataList.get(CATEGORY_NUM).getIdentifier());
                createRelationParam.setTarget(secondCategoryDataList.get(CATEGORY_NUM + i).getIdentifier());
                createRelationParam.setRelationType("ASSOCIATE");
                createRelationParam.setTags(new ArrayList<String>());
                createRelationParam.setOrderNum(i + 1);
                createRelationParam.setEnable(true);
                createRelationParam.setPatternPath(patternViewModel.getPatternName() + "/"
                        + firstCategoryDataList.get(CATEGORY_NUM).getNdCode());
                createRelationParam.setPattern(patternViewModel.getIdentifier());
                createRelationParam.setLevelParent("ROOT");
                json = ObjectMapper.writeValueAsString(createRelationParam);
                // 调用接口
                result = MockUtil.mockCreate(mockMvc, uri, json);
                // validate result
                createRelationResult = ObjectMapper.readValue(result, CategoryRelationViewModel.class);
                assertCategoryRelationEqualsWithoutId(createRelationParam, createRelationResult);
                relationList.add(createRelationResult);
            }

            // 第三维
            // 细粒度
            for (int i = 0; i < CATEGORY_NUM; i++) {
                createRelationParam.setSource(secondCategoryDataList.get(CATEGORY_NUM).getIdentifier());
                createRelationParam.setTarget(thirdCategoryDataList.get(CATEGORY_NUM + i).getIdentifier());
                createRelationParam.setRelationType("ASSOCIATE");
                createRelationParam.setTags(new ArrayList<String>());
                createRelationParam.setOrderNum(i + 1);
                createRelationParam.setEnable(true);
                createRelationParam.setPatternPath(patternViewModel.getPatternName() + "/"
                        + firstCategoryDataList.get(CATEGORY_NUM).getNdCode() + "/"
                        + secondCategoryDataList.get(CATEGORY_NUM).getNdCode());
                createRelationParam.setPattern(patternViewModel.getIdentifier());
                createRelationParam.setLevelParent("ROOT");
                json = ObjectMapper.writeValueAsString(createRelationParam);
                // 调用接口
                result = MockUtil.mockCreate(mockMvc, uri, json);
                // validate result
                createRelationResult = ObjectMapper.readValue(result, CategoryRelationViewModel.class);
                assertCategoryRelationEqualsWithoutId(createRelationParam, createRelationResult);
                relationList.add(createRelationResult);
            }

            // 四、维度数据关系：查
            // 四、维度数据关系：删

            for (CategoryRelationViewModel model : relationList) {
                uri = "/v0.6/categorypatterns/datas/relations/" + model.getIdentifier();
                json = "";
                result = MockUtil.mockDelete(mockMvc, uri, json);
            }
            relationList.clear();

            logger.info("维度数据关系 controller 层测试结束");

            // 后续的清除工作，放在after 中（先维度数据，再分类维度，最后维度模式）

        } catch (Exception e) {
            logger.error(e);
        }

    }

    

    // @Test
    // public void importRelation() {
    //
    // int errorAddRelationNum = 0;
    // int logNum = 0;
    // // 从文件读取一行;
    // String line = "";//PP=K12|LP=ROOT|TC=$ON020000
    // FileInputStream fis = null;
    // InputStreamReader isr = null;
    // BufferedReader br = null; // 用于包装InputStreamReader,提高处理性能。因为BufferedReader有缓冲的，而InputStreamReader没有。
    // try {
    //
    // ClassLoader classLoader = getClass().getClassLoader();
    // File file = new File(classLoader.getResource("datarelations_20150527.txt")
    // .getFile());
    // fis = new FileInputStream(file);// FileInputStream
    // // 从文件系统中的某个文件中获取字节
    // isr = new InputStreamReader(fis);// InputStreamReader 是字节流通向字符流的桥梁,
    // br = new BufferedReader(isr);// 从字符输入流中读取文件中的内容,封装了一个new
    // // InputStreamReader的对象
    // while ((line = br.readLine()) != null) {
    // String[] chunks = line.split("\\|");
    // if (chunks.length != 4) {
    // logNum++;
    // continue; // 不是规范的数据，可能是注释
    // }
    //
    // // patternPath;
    // String[] pps = chunks[0].split("=");
    // if (pps.length != 2) {
    // continue; // 不符合规范;
    // }
    // String patternPath = pps[1];
    //
    // // levelParent
    // String[] lps = chunks[1].split("=");
    // if (lps.length != 2) {
    // continue;
    // }
    // String levelParent = lps[1];
    //
    // // targetNdCode;
    // String[] tgs = chunks[2].split("=");
    // if (tgs.length != 2) {
    // continue;
    // }
    // String targetNdCode = tgs[1];
    //
    // // orderNum;
    // String[] ons = chunks[3].split("=");
    // if (tgs.length != 2) {
    // continue;
    // }
    // float orderNum = Float.valueOf(ons[1]); //变更为float
    //
    // // 读取文件内容,并得到一条条数据， 保住patternPath , levelParent;
    // // String patternPath = ""; // k12/$ON00100100/$S0001/$E0001
    // // String levelParent = ""; // ndCode;
    //
    // String patternId = "9de92145-6dd0-43eb-96a4-9e0747d9759e"; // 这个会发生变化
    // // 预备参数;
    // String sourceId;
    // String[] path = patternPath.split("/");
    // if (path.length == 1) {
    // sourceId = "ROOT";
    // } else {
    // sourceId = getCategoryDataByNdCode(path[path.length - 1]).getIdentifier();
    // }
    // String targetId = getCategoryDataByNdCode(targetNdCode).getIdentifier();
    // CategoryRelation bean = new CategoryRelation();
    // bean.setIdentifier(UUID.randomUUID().toString());
    // bean.setEnable(true);
    // bean.setLevelParent(levelParent);
    // bean.setOrderNum(orderNum);
    // bean.setPattern(patternId);
    // bean.setPatternPath(patternPath);
    // bean.setRelationType("ASSOCIATE");
    // bean.setSource(sourceId);
    // // bean.setTags(null);
    // bean.setTarget(targetId);
    //
    // try {
    // StoreApiFactory.builderCategoryRelationApi().add(bean);
    // } catch (EspStoreException e) {
    // errorAddRelationNum++;
    // logger.info("error Line "+ line);
    // // e.printStackTrace();
    // }
    // }
    // } catch (FileNotFoundException e) {
    // logger.info("找不到指定文件");
    // } catch (IOException e) {
    // logger.info("读取文件失败");
    // } finally {
    // try {
    // br.close();
    // isr.close();
    // fis.close();
    // // 关闭的时候最好按照先后顺序关闭最后开的先关闭所以先关s,再关n,最后关m
    // } catch (IOException e) {
    // logger.error("连接关闭异常:"+e.getMessage());
    // }
    // }
    // logger.info("errorAddRelationNum" + errorAddRelationNum);
    // logger.info("logNum" + logNum);
    // }
    // @Test
    // public void importCategoryData(){
    //
    // final String ROOT = "ROOT";
    // final int CHUNK_NUM = 8;
    // final int TITLE= 0;
    // final int SHORT_NAME=1;
    // final int ND_CODE=2;
    // final int PARENT=3;
    // final int DESCRIPTION = 4;
    // final int ORDER_NUM=5;
    // final int GB_CODE=6;
    // final int CATEGORY=7;
    // // int
    // int errorAddRelationNum = 0;
    // int logNum = 0;
    // // 从文件读取一行;
    // String line = "";//
    // CD=北师大版|SN=BSDEP|NC=$E006000|P=ROOT|DP=北京师范大学出版社|ON=6|GC=|CG=c66e6c26-67b7-44a8-ade1-08e4412d08fe
    // FileInputStream fis = null;
    // InputStreamReader isr = null;
    // BufferedReader br = null; // 用于包装InputStreamReader,提高处理性能。因为BufferedReader有缓冲的，而InputStreamReader没有。
    // try {
    //
    // ClassLoader classLoader = getClass().getClassLoader();
    // File file = new File(classLoader.getResource("datarelations_20150527.txt")
    // .getFile());
    // fis = new FileInputStream(file);// FileInputStream
    // // 从文件系统中的某个文件中获取字节
    // isr = new InputStreamReader(fis);// InputStreamReader 是字节流通向字符流的桥梁,
    // br = new BufferedReader(isr);// 从字符输入流中读取文件中的内容,封装了一个new
    // // InputStreamReader的对象
    // while ((line = br.readLine()) != null) {
    // String[] chunks = line.split("\\|");
    // if (chunks.length != CHUNK_NUM) {
    // logNum++;
    // continue; // 不是规范的维度数据，可能是注释或关系
    // }
    // List<String> values = new ArrayList<String>();
    // for(int i = 0; i<chunks.length; i++){
    // String[] pps = chunks[i].split("=");
    // if(i != GB_CODE){
    // Assert.assertEquals(2, pps.length);
    // values.add(pps[1]);
    // }else{
    // Assert.assertEquals(1, pps.length);
    // values.add("none");//不用这个值
    // }
    // }
    //
    // // 预备参数;
    // CategoryData bean = new CategoryData();
    // bean.setCategory(values.get(CATEGORY));
    // bean.setDescription(values.get(DESCRIPTION));
    // if(values.get(PARENT).equals(ROOT)){
    // bean.setDimensionPath(ROOT+"/"+values.get(ND_CODE));
    // }else{
    // bean.setDimensionPath(getCategoryDataByNdCode(values.get(PARENT)).getDimensionPath()+"/"+values.get(ND_CODE));
    // }
    // bean.setGbCode(values.get(GB_CODE));// 暂时没有内容
    // bean.setIdentifier(UUID.randomUUID().toString());
    // bean.setNdCode(values.get(ND_CODE));
    // bean.setOrderNum(Integer.valueOf(values.get(ORDER_NUM)));
    // if(values.get(PARENT).equals(ROOT)){
    // bean.setParent(ROOT);
    // }else{
    // bean.setParent(getCategoryDataByNdCode(values.get(PARENT)).getIdentifier());
    // }
    // bean.setShortName(values.get(SHORT_NAME));
    // bean.setTitle(values.get(TITLE));
    //
    // try {
    // StoreApiFactory.builderCategoryDataApi().add(bean);
    // } catch (EspStoreException e) {
    // errorAddRelationNum++;
    // logger.info("error Line "+ line);
    // }
    // }
    // } catch (FileNotFoundException e) {
    // logger.info("找不到指定文件");
    // } catch (IOException e) {
    // logger.info("读取文件失败");
    // } finally {
    // try {
    // br.close();
    // isr.close();
    // fis.close();
    // // 关闭的时候最好按照先后顺序关闭最后开的先关闭所以先关s,再关n,最后关m
    // } catch (IOException e) {
    // logger.error("连接关闭异常:"+e.getMessage());
    // }
    // }
    // logger.info("errorAddRelationNum" + errorAddRelationNum);
    // logger.info("logNum" + logNum);
    //
    // }

    // private CategoryData getCategoryDataByNdCode(String ndCode) {
    //
    // ReturnInfo<CategoryData> returnInfo = null;
    // try {
    // returnInfo = StoreApiFactory.builderCategoryDataApi()
    // .getDetailByNdCode(ndCode);
    // } catch (EspStoreException e) {
    // logger.info(e);
    // }
    //
    // if (returnInfo != null && returnInfo.getCode() == 1 && returnInfo.getData()!= null) {
    // Assert.assertEquals("ndCode 不一致", ndCode, returnInfo.getData()
    // .getNdCode());
    // } else {
    // logger.info("通过ndCode 取维度数据失败"+ ndCode);
    // }
    // return returnInfo.getData();
    // }

}
