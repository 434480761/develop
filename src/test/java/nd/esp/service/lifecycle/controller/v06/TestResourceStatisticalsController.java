package nd.esp.service.lifecycle.controller.v06;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.PropertyNamingStrategy;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.nd.gaea.WafException;
import com.nd.gaea.rest.o2o.JacksonCustomObjectMapper;
import com.nd.gaea.rest.testconfig.MockUtil;

import nd.esp.service.lifecycle.BaseControllerConfig;
import nd.esp.service.lifecycle.impl.SimpleJunitTest4ResourceImpl;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;
import nd.esp.service.lifecycle.vos.CategoryPatternViewModel;
import nd.esp.service.lifecycle.vos.ListViewModel;
import nd.esp.service.lifecycle.vos.statistical.v06.ResourceStatisticalViewModel;

public class TestResourceStatisticalsController extends BaseControllerConfig {

    private static String adduri = "/v0.6/assets/";

    private static String geturi = "/v0.6/resources/statisticals/tolist";

    private JacksonCustomObjectMapper ObjectMapper = new JacksonCustomObjectMapper();
    
    private static String uuid = UUID.randomUUID().toString();

    /**
     * 正常的添加数据,并且获取数据
     * @throws Exception 
     * @throws UnsupportedEncodingException 
     * */
//    @Test
    public void test() throws UnsupportedEncodingException, Exception {
        // 创建一个素材资源
        String uri = "/v0.6/assets/" + uuid;
        String resourcejson = getResourceJson();
        String result = null;
        Map<String , Object> resultMap = null;
        MockUtil.mockCreate(mockMvc, uri, resourcejson);

        // 非法创建统计数据
        String urif = "/v0.6/assets/006b5383-aaaa-aaaa-aaaa-5cef04103c8c/statisticals";
        String json = asJsonString(getAddData());
        result = MockUtil.mockCreate(mockMvc, urif, json);
        resultMap = ObjectUtils.fromJson(result,Map.class);
        Assert.assertEquals("资源统计指标对应的源资源未找到", resultMap.get("message"));
        
        
        // 正常添加数据
        // 第一次添加统计数据
        json = asJsonString(getAddData());
        adduri = adduri + uuid + "/statisticals";
        result = MockUtil.mockCreate(mockMvc, adduri, json);
        
        // 第二次添加统计数据
        result = MockUtil.mockCreate(mockMvc, adduri, json);
        
        // UUID不合法
        uri = "/v0.6/assets/0093fd30-875a-489e-8b2c-775a05a0ad/statisticals";
        json = asJsonString(getAddData());
        result = MockUtil.mockCreate(mockMvc, uri, json);
        resultMap = ObjectUtils.fromJson(result,Map.class);
        Assert.assertEquals("UUID格式不对", resultMap.get("message"));

        // title不合法
        List<ResourceStatisticalViewModel> list = getAddData();
        list.get(0).setKeyTitle("test");
        list.get(1).setKeyTitle("test");
        json = asJsonString(list);
        result = MockUtil.mockCreate(mockMvc, adduri, json);
        resultMap = ObjectUtils.fromJson(result,Map.class);
        Assert.assertEquals("不能有重复的key_title", resultMap.get("message"));

        // title不合法
        list = getAddData();
        list.get(0).setKeyTitle("");
        json = asJsonString(list);
        result = MockUtil.mockCreate(mockMvc, adduri, json);
        resultMap = ObjectUtils.fromJson(result,Map.class);
        Assert.assertEquals("key_title不能为空", resultMap.get("message"));
        

        // dataFrom不合法
        list = getAddData();
        list.get(0).setDataFrom("");
        json = asJsonString(list);
        MockUtil.mockCreate(mockMvc, adduri, json);
        result = MockUtil.mockCreate(mockMvc, adduri, json);
        resultMap = ObjectUtils.fromJson(result,Map.class);
        Assert.assertEquals("data_from不能为空", resultMap.get("message"));

        // keyValue不合法
        list = getAddData();
        list.get(0).setKeyValue(new Double(-10));
        json = asJsonString(list);
        MockUtil.mockCreate(mockMvc, adduri, json);
        result = MockUtil.mockCreate(mockMvc, adduri, json);
        resultMap = ObjectUtils.fromJson(result,Map.class);
        Assert.assertEquals("key_value不能为空或者不能小于0", resultMap.get("message"));
        
        // 获取统计数据
        // 正常获取数据
        String geturitrue = geturi + "?key=test1&key=test2&rid=" + uuid;
        result = MockUtil.mockGet(mockMvc, geturitrue, null);

        // 错误的ID获取资源
        String geturiErrorIdIsNull = geturi + "?key=test1&key=test2&rid=62c747f8-e256-4cd9-86b5";
        MockUtil.mockGet(mockMvc, geturiErrorIdIsNull, null);
        result = MockUtil.mockGet(mockMvc, geturiErrorIdIsNull, null);
        resultMap = ObjectUtils.fromJson(result,Map.class);
        Assert.assertEquals("UUID格式不对", resultMap.get("message"));

        // ID为空获取资源
        String geturiErrorIdIsEr = geturi + "?key=&key=test2&rid=";
        result = MockUtil.mockGet(mockMvc, geturiErrorIdIsEr, null);
        resultMap = ObjectUtils.fromJson(result,Map.class);
        Assert.assertEquals("UUID格式不对", resultMap.get("message"));
        
        // 删除源资源
        uri = "/v0.6/assets/" + uuid;
        MockUtil.mockDelete(mockMvc, uri, null);
    }

    private List<ResourceStatisticalViewModel> getAddData() {
        List<ResourceStatisticalViewModel> list = new ArrayList<ResourceStatisticalViewModel>();
        ResourceStatisticalViewModel vm1 = new ResourceStatisticalViewModel();
        vm1.setKeyTitle("test1");
        vm1.setKeyValue(new Double(10));
        vm1.setDataFrom("test");

        ResourceStatisticalViewModel vm2 = new ResourceStatisticalViewModel();
        vm2.setKeyTitle("test2");
        vm2.setKeyValue(new Double(20));
        vm2.setDataFrom("test");

        list.add(vm1);
        list.add(vm2);

        return list;
        
    }

    private String getResourceJson() {
        return "{" + "    \"identifier\": \"003e371b-176f-46ed-8b82-6ccb8138c9be\",\n" + "    \"title\": \"meme\",\n"
                + "    \"description\": \""+SimpleJunitTest4ResourceImpl.DERAULT_DESCRIPTION+"\",\n" + "    \"language\": \"zh_cn\",\n" + "    \"preview\": {\n"
                + "        \"png\": \"{ref_path}/edu/esp/assets/preview/123.png\"\n" + "    },\n" + "    \"tags\": [\n"
                + "        \"baidu\",\n" + "        \"image\"\n" + "    ],\n" + "    \"keywords\": [\n"
                + "        \"baidu\",\n" + "        \"image\"\n" + "    ],\n" + "    \"custom_properties\": {\n"
                + "        \"key\": \"test\"\n" + "    },\n" + "    \"categories\": {\n" + "        \"edition\": [\n"
                + "            {\n" + "                \"identifier\": \"39db2aa7-8576-4181-b057-e36531bd7408\",\n"
                + "                \"taxonpath\": \"K12/$ON030000/$ON030200/$SB0500/$E004000/$E004001\",\n"
                + "                \"taxonname\": \"人教版一年级起点\",\n" + "                \"taxoncode\": \"$E004000\"\n"
                + "            }\n" + "        ],\n" + "        \"sub_edition\": [\n" + "            {\n"
                + "                \"identifier\": \"f553a4f9-9678-463f-8ffe-dcbfed23379e\",\n"
                + "                \"taxonpath\": \"K12/$ON030000/$ON030200/$SB0500/$E004000/$E004001\",\n"
                + "                \"taxonname\": \"上册\",\n" + "                \"taxoncode\": \"$E004001\"\n"
                + "            }\n" + "        ],\n" + "        \"subject\": [\n" + "            {\n"
                + "                \"identifier\": \"e1b70b73-c165-4a57-9671-981f22b303b7\",\n"
                + "                \"taxonpath\": \"K12/$ON030000/$ON030200/$SB0500/$E004000/$E004001\",\n"
                + "                \"taxonname\": \"化学\",\n" + "                \"taxoncode\": \"$SB0500\"\n"
                + "            }\n" + "        ],\n" + "        \"grade\": [\n" + "            {\n"
                + "                \"identifier\": \"e372fbea-0090-45b4-935c-1d6c3eec4b52\",\n"
                + "                \"taxonpath\": \"K12/$ON030000/$ON030200/$SB0500/$E004000/$E004001\",\n"
                + "                \"taxonname\": \"八年级\",\n" + "                \"taxoncode\": \"$ON030200\"\n"
                + "            }\n" + "        ],\n" + "        \"assets_type\": [\n" + "            {\n"
                + "                \"identifier\": \"dbd78384-e8e9-4287-a5e5-3c6184aff388\",\n"
                + "                \"taxonpath\": null,\n" + "                \"taxonname\": \"媒体素材\",\n"
                + "                \"taxoncode\": \"$RA0100\"\n" + "            },\n" + "            {\n"
                + "                \"identifier\": \"b09549ac-c7b2-4fe7-a90a-33e6c1fa037c\",\n"
                + "                \"taxonpath\": \"\",\n" + "                \"taxonname\": \"图片\",\n"
                + "                \"taxoncode\": \"$RA0101\"\n" + "            }\n" + "        ],\n"
                + "        \"phase\": [\n" + "            {\n"
                + "                \"identifier\": \"cb7b4297-85cf-44ab-90f5-3555e9bf872c\",\n"
                + "                \"taxonpath\": \"K12/$ON030000/$ON030200/$SB0501012/$E004000/$E004001\",\n"
                + "                \"taxonname\": \"初中\",\n" + "                \"taxoncode\": \"$ON030000\"\n"
                + "            }\n" + "        ]\n" + "    },\n" + "    \"life_cycle\": {\n"
                + "        \"version\": \"v0.2\",\n" + "        \"status\": \"INIT\",\n"
                + "        \"enable\": true,\n" + "        \"creator\": \""+SimpleJunitTest4ResourceImpl.DERAULT_CREATOR+"\",\n"
                + "        \"publisher\": \""+SimpleJunitTest4ResourceImpl.DERAULT_PUBLISHER+"\",\n" + "        \"provider\": \"NetDragon Inc.\",\n"
                + "        \"provider_source\": \"八年级 地理第一学期期末考试试卷_201407282056.doc\",\n"
                + "        \"create_time\": \"2015-08-12T07:25:51.000+0000\",\n"
                + "        \"last_update\": \"2015-11-12T10:23:46.296+0000\"\n" + "    },\n"
                + "    \"education_info\": {\n" + "        \"interactivity\": 2,\n"
                + "        \"interactivity_level\": 2,\n" + "        \"end_user_type\": \"教师，管理者\",\n"
                + "        \"semantic_density\": 1,\n" + "        \"context\": \"基础教育\",\n"
                + "        \"age_range\": \"7岁以上\",\n" + "        \"difficulty\": \"easy\",\n"
                + "        \"learning_time\": \"0\",\n" + "        \"description\": {\n"
                + "            \"zh\": \"如何使用学习对象进行描述\"\n" + "        },\n" + "        \"language\": \"zh\"\n"
                + "    },\n" + "    \"tech_info\": {\n" + "        \"source\": {\n"
                + "            \"format\": \"image/jpg\",\n" + "            \"size\": 1024,\n"
                + "            \"location\": \"{ref_path}/edu/esp/test/abc.jpg\",\n"
                + "            \"requirements\": [\n" + "                {\n"
                + "                    \"identifier\": null,\n" + "                    \"type\": \"QUOTA\",\n"
                + "                    \"name\": \"resolution\",\n" + "                    \"min_version\": \"1.0\",\n"
                + "                    \"max_version\": \"2.0\",\n" + "                    \"installation\": \"中国\",\n"
                + "                    \"installation_file\": \"456465\",\n"
                + "                    \"value\": \"435*237\",\n" + "                    \"resource_model\": null\n"
                + "                }\n" + "            ],\n" + "            \"md5\": \"md5Value\",\n"
                + "            \"entry\": \"入口地址\"\n" + "        },\n" + "        \"href\": {\n"
                + "            \"format\": \"image/jpg\",\n" + "            \"size\": 1024,\n"
                + "            \"location\": \"{ref_path}/edu/esp/test/abc.jpg\",\n"
                + "            \"requirements\": [\n" + "                {\n"
                + "                    \"identifier\": null,\n" + "                    \"type\": \"QUOTA\",\n"
                + "                    \"name\": \"resolution\",\n" + "                    \"min_version\": \"1.0\",\n"
                + "                    \"max_version\": \"2.0\",\n" + "                    \"installation\": \"中国\",\n"
                + "                    \"installation_file\": \"456465\",\n"
                + "                    \"value\": \"435*237\",\n" + "                    \"resource_model\": null\n"
                + "                }\n" + "            ],\n" + "            \"md5\": \"md5Value\",\n"
                + "            \"entry\": \"入口地址\"\n" + "        }\n" + "    },\n" + "    \"copyright\": {\n"
                + "        \"right\": \"zh\",\n" + "        \"description\": \"如何使用学习对象进行描述\",\n"
                + "        \"author\": \"johnny\"\n" + "    }\n" + "}";
    }

    private static String asJsonString(final Object obj) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
            final String jsonContent = mapper.writeValueAsString(obj);
            return jsonContent;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
