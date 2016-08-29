package nd.esp.service.lifecycle.utils.titan;

import nd.esp.service.lifecycle.educommon.vos.constant.IncludesConstant;
import nd.esp.service.lifecycle.services.titan.TitanResultParse;
import org.apache.commons.collections4.map.HashedMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestTitanResultParse {

    public static void main(String[] args) {
        System.out.println("测试资源1：id和label相连");
        testToMapForRelationQuery1();
        System.out.println("###########################################################################################################################################");
        System.out.println("测试资源2：id和label不相连");
        testToMapForRelationQuery4();
        System.out.println("###########################################################################################################################################");
        System.out.println("测试点：tech_info");
        testToMapForRelationQuery2();
        System.out.println("###########################################################################################################################################");
        System.out.println("测试边:has_relation");
        testToMapForRelationQuery3();
        System.out.println("###########################################################################################################################################");
        System.out.println("测试分割的index:查询结果只有一条数据（资源），应返回[0, 1]");
        testGetIndexByLabel1();
        System.out.println("###########################################################################################################################################");
        System.out.println("测试分割的index：常规返回多条数据，应返回[0, 7, 13, 19, 25],下一个测试可参考对比此结果");
        testGetIndexByLabel2();
        System.out.println("###########################################################################################################################################");
        System.out.println("测试按照index分割：常规返回多条数据");
        testGetIndexByLabel3();
        System.out.println("###########################################################################################################################################");
        System.out.println("测试统计数据：has_resource_statistical");
        testToMapForRelationQuery5();
        System.out.println("###########################################################################################################################################");
        System.out.println("测试解析：无异常信息，即为通过");
        testParse();
    }
    /**
     * 测试方法：toMapForRelationQuery
     */
    private static void testToMapForRelationQuery1() {
        // 44 key
        String resource = "==>{preview=[{\"png\":\"${ref-path}/prepub_content_edu_product/esp/assets/abc.png\"}], cr_author=[880508], search_path_string=[k12/$on030000/$on030200/$sb0501012/$e004000/$e004001], keywords=[[\"title\",\"qatest\"]], edu_description=[{\"zh_CN\":\"如何使用学习对象进行描述\"}], search_path=[K12/$ON030000/$ON030200/$SB0501012/$E004000/$E004001], description=[lcms_special_description_qa_test], search_coverage_string=[debug/qa//,debug/qa/test/creating,debug/qa//creating,debug/qa/test/], language=[zh_CN], lc_status=[CREATING], custom_properties=[{\"key\":\"test\"}], cr_has_right=[true], title=[lcms_qa_test_yqjtest_res_getinfo_with_include_of_all_attribute_ok_test_1471416223.61], cr_right_end_date=[7258089000000], lc_provider=[lcms_special_provider_qa_test], id=1658912, label=assets, cr_description=[版权描述信息], lc_create_time=[1471416133155], primary_category=[assets], search_code_string=[$f050005,$on030000,pt01001,$ra0100], search_coverage=[Debug/qa//CREATING, Debug/qa/TEST/CREATING, Debug/qa/TEST/, Debug/qa//], lc_publisher=[lcms_special_publisher_qa_test], edu_context=[基础教育], lc_provider_mode=[qatest_provider_mode], cr_right_start_date=[946656000000], identifier=[1e80454b-ae80-4dbd-994a-b3d8e55ee6b5], cr_right=[版权信息], lc_last_update=[1471416133155], edu_interactivity=[2], lc_version=[qav0.1], edu_semantic_density=[1], edu_difficulty=[easy], edu_end_user_type=[教师，管理者], tags=[[\"nd\",\"sdp.esp\"]], search_code=[$F050005, $ON030000, $RA0100, PT01001], m_identifier=[1e80454b-ae80-4dbd-994a-b3d8e55ee6b5], edu_interactivity_level=[2], lc_enable=[true], lc_provider_source=[八年级地理第一学期期末考试试卷_201407282056.doc], lc_creator=[lcms_special_creator_qa_test], edu_language=[zh_CN], edu_learning_time=[45], edu_age_range=[7岁以上]}";
        // System.out.println("resource: "+resource);
        Map<String, String> resultResourceMap = TitanResultParse.toMapForRelationQuery(resource);
        // System.out.println("resultResourceMap: "+resultResourceMap.size());

        Map<String, String> expectResourceMap = new HashedMap<String, String>();
        expectResourceMap
                .put("preview",
                        "{\"png\":\"${ref-path}/prepub_content_edu_product/esp/assets/abc.png\"}");
        expectResourceMap.put("cr_author", "880508");
        expectResourceMap.put("search_path_string",
                "k12/$on030000/$on030200/$sb0501012/$e004000/$e004001");
        expectResourceMap.put("keywords", "[\"title\",\"qatest\"]");
        expectResourceMap
                .put("edu_description", "{\"zh_CN\":\"如何使用学习对象进行描述\"}");
        expectResourceMap.put("search_path",
                "K12/$ON030000/$ON030200/$SB0501012/$E004000/$E004001");
        expectResourceMap
                .put("description", "lcms_special_description_qa_test");
        expectResourceMap
                .put("search_coverage_string",
                        "debug/qa//,debug/qa/test/creating,debug/qa//creating,debug/qa/test/");
        expectResourceMap.put("language", "zh_CN");
        expectResourceMap.put("lc_status", "CREATING");
        expectResourceMap.put("custom_properties", "{\"key\":\"test\"}");
        expectResourceMap.put("cr_has_right", "true");
        expectResourceMap
                .put("title",
                        "lcms_qa_test_yqjtest_res_getinfo_with_include_of_all_attribute_ok_test_1471416223.61");
        expectResourceMap.put("cr_right_end_date", "7258089000000");
        expectResourceMap.put("lc_provider", "lcms_special_provider_qa_test");
        expectResourceMap.put("label", "assets");
        expectResourceMap.put("cr_description", "版权描述信息");
        expectResourceMap.put("lc_create_time", "1471416133155");
        expectResourceMap.put("primary_category", "assets");
        expectResourceMap.put("search_code_string",
                "$f050005,$on030000,pt01001,$ra0100");
        expectResourceMap
                .put("search_coverage",
                        "Debug/qa//CREATING, Debug/qa/TEST/CREATING, Debug/qa/TEST/, Debug/qa//");
        expectResourceMap.put("lc_publisher", "lcms_special_publisher_qa_test");
        expectResourceMap.put("id", "1658912");
        expectResourceMap.put("edu_context", "基础教育");
        expectResourceMap.put("lc_provider_mode", "qatest_provider_mode");
        expectResourceMap.put("cr_right_start_date", "946656000000");
        expectResourceMap.put("identifier",
                "1e80454b-ae80-4dbd-994a-b3d8e55ee6b5");
        expectResourceMap.put("cr_right", "版权信息");
        expectResourceMap.put("lc_last_update", "1471416133155");
        expectResourceMap.put("edu_interactivity", "2");
        expectResourceMap.put("lc_version", "qav0.1");
        expectResourceMap.put("edu_semantic_density", "1");
        expectResourceMap.put("edu_difficulty", "easy");
        expectResourceMap.put("edu_end_user_type", "教师，管理者");
        expectResourceMap.put("tags", "[\"nd\",\"sdp.esp\"]");
        expectResourceMap.put("search_code",
                "$F050005, $ON030000, $RA0100, PT01001");
        expectResourceMap.put("m_identifier",
                "1e80454b-ae80-4dbd-994a-b3d8e55ee6b5");
        expectResourceMap.put("edu_interactivity_level", "2");
        expectResourceMap.put("lc_enable", "true");
        expectResourceMap.put("lc_provider_source",
                "八年级地理第一学期期末考试试卷_201407282056.doc");
        expectResourceMap.put("lc_creator", "lcms_special_creator_qa_test");
        expectResourceMap.put("edu_language", "zh_CN");
        expectResourceMap.put("edu_learning_time", "45");
        expectResourceMap.put("edu_age_range", "7岁以上");
        // System.out.println("expectResourceMap: "+expectResourceMap.size());
        checkMapEqual(resultResourceMap, expectResourceMap);

        //FIXME toMapForRelationQuery方法测试：暂时把其它两种情况也做下 ->龚世文
    }
    /**
     * 测试方法：toMapForRelationQuery
     */
    private static void testToMapForRelationQuery4() {
        // 44 key
        String resource = "==>{preview=[{\"png\":\"${ref-path}/prepub_content_edu_product/esp/assets/abc.png\"}], cr_author=[880508], search_path_string=[k12/$on030000/$on030200/$sb0501012/$e004000/$e004001], keywords=[[\"title\",\"qatest\"]], edu_description=[{\"zh_CN\":\"如何使用学习对象进行描述\"}], search_path=[K12/$ON030000/$ON030200/$SB0501012/$E004000/$E004001], description=[lcms_special_description_qa_test], search_coverage_string=[debug/qa//,debug/qa/test/creating,debug/qa//creating,debug/qa/test/], language=[zh_CN], lc_status=[CREATING], custom_properties=[{\"key\":\"test\"}], cr_has_right=[true], title=[lcms_qa_test_yqjtest_res_getinfo_with_include_of_all_attribute_ok_test_1471416223.61], cr_right_end_date=[7258089000000], lc_provider=[lcms_special_provider_qa_test], id=1658912, cr_description=[版权描述信息], label=assets, lc_create_time=[1471416133155], primary_category=[assets], search_code_string=[$f050005,$on030000,pt01001,$ra0100], search_coverage=[Debug/qa//CREATING, Debug/qa/TEST/CREATING, Debug/qa/TEST/, Debug/qa//], lc_publisher=[lcms_special_publisher_qa_test], edu_context=[基础教育], lc_provider_mode=[qatest_provider_mode], cr_right_start_date=[946656000000], identifier=[1e80454b-ae80-4dbd-994a-b3d8e55ee6b5], cr_right=[版权信息], lc_last_update=[1471416133155], edu_interactivity=[2], lc_version=[qav0.1], edu_semantic_density=[1], edu_difficulty=[easy], edu_end_user_type=[教师，管理者], tags=[[\"nd\",\"sdp.esp\"]], search_code=[$F050005, $ON030000, $RA0100, PT01001], m_identifier=[1e80454b-ae80-4dbd-994a-b3d8e55ee6b5], edu_interactivity_level=[2], lc_enable=[true], lc_provider_source=[八年级地理第一学期期末考试试卷_201407282056.doc], lc_creator=[lcms_special_creator_qa_test], edu_language=[zh_CN], edu_learning_time=[45], edu_age_range=[7岁以上]}";
        // System.out.println("resource: "+resource);
        Map<String, String> resultResourceMap = TitanResultParse.toMapForRelationQuery(resource);
        // System.out.println("resultResourceMap: "+resultResourceMap.size());

        Map<String, String> expectResourceMap = new HashedMap<String, String>();
        expectResourceMap
                .put("preview",
                        "{\"png\":\"${ref-path}/prepub_content_edu_product/esp/assets/abc.png\"}");
        expectResourceMap.put("cr_author", "880508");
        expectResourceMap.put("search_path_string",
                "k12/$on030000/$on030200/$sb0501012/$e004000/$e004001");
        expectResourceMap.put("keywords", "[\"title\",\"qatest\"]");
        expectResourceMap
                .put("edu_description", "{\"zh_CN\":\"如何使用学习对象进行描述\"}");
        expectResourceMap.put("search_path",
                "K12/$ON030000/$ON030200/$SB0501012/$E004000/$E004001");
        expectResourceMap
                .put("description", "lcms_special_description_qa_test");
        expectResourceMap
                .put("search_coverage_string",
                        "debug/qa//,debug/qa/test/creating,debug/qa//creating,debug/qa/test/");
        expectResourceMap.put("language", "zh_CN");
        expectResourceMap.put("lc_status", "CREATING");
        expectResourceMap.put("custom_properties", "{\"key\":\"test\"}");
        expectResourceMap.put("cr_has_right", "true");
        expectResourceMap
                .put("title",
                        "lcms_qa_test_yqjtest_res_getinfo_with_include_of_all_attribute_ok_test_1471416223.61");
        expectResourceMap.put("cr_right_end_date", "7258089000000");
        expectResourceMap.put("lc_provider", "lcms_special_provider_qa_test");
        expectResourceMap.put("label", "assets");
        expectResourceMap.put("cr_description", "版权描述信息");
        expectResourceMap.put("lc_create_time", "1471416133155");
        expectResourceMap.put("primary_category", "assets");
        expectResourceMap.put("search_code_string",
                "$f050005,$on030000,pt01001,$ra0100");
        expectResourceMap
                .put("search_coverage",
                        "Debug/qa//CREATING, Debug/qa/TEST/CREATING, Debug/qa/TEST/, Debug/qa//");
        expectResourceMap.put("lc_publisher", "lcms_special_publisher_qa_test");
        expectResourceMap.put("id", "1658912");
        expectResourceMap.put("edu_context", "基础教育");
        expectResourceMap.put("lc_provider_mode", "qatest_provider_mode");
        expectResourceMap.put("cr_right_start_date", "946656000000");
        expectResourceMap.put("identifier",
                "1e80454b-ae80-4dbd-994a-b3d8e55ee6b5");
        expectResourceMap.put("cr_right", "版权信息");
        expectResourceMap.put("lc_last_update", "1471416133155");
        expectResourceMap.put("edu_interactivity", "2");
        expectResourceMap.put("lc_version", "qav0.1");
        expectResourceMap.put("edu_semantic_density", "1");
        expectResourceMap.put("edu_difficulty", "easy");
        expectResourceMap.put("edu_end_user_type", "教师，管理者");
        expectResourceMap.put("tags", "[\"nd\",\"sdp.esp\"]");
        expectResourceMap.put("search_code",
                "$F050005, $ON030000, $RA0100, PT01001");
        expectResourceMap.put("m_identifier",
                "1e80454b-ae80-4dbd-994a-b3d8e55ee6b5");
        expectResourceMap.put("edu_interactivity_level", "2");
        expectResourceMap.put("lc_enable", "true");
        expectResourceMap.put("lc_provider_source",
                "八年级地理第一学期期末考试试卷_201407282056.doc");
        expectResourceMap.put("lc_creator", "lcms_special_creator_qa_test");
        expectResourceMap.put("edu_language", "zh_CN");
        expectResourceMap.put("edu_learning_time", "45");
        expectResourceMap.put("edu_age_range", "7岁以上");
        // System.out.println("expectResourceMap: "+expectResourceMap.size());
        checkMapEqual(resultResourceMap, expectResourceMap);

        //FIXME toMapForRelationQuery方法测试：暂时把其它两种情况也做下 ->龚世文
    }
    /**
     * {identifier=[3eaabed0-92a2-4c3c-bced-3a44e8a5f51d], ti_md5=[md5Value], id=356900872, label=tech_info, ti_title=[href], ti_location=[${ref-path}/prepub_content_edu/esp/test/abc.png], ti_size=[1024], ti_format=[image/png], ti_entry=[入口地址], ti_requirements=[[{"identifier":null,"type":"HARDWARE","name":"resolution","minVersion":null,"maxVersion":null,"installation":null,"installationFile":null,"value":"435*237","ResourceModel":null}]]}
     * 测试方法：toMapForRelationQuery
     */
    private static void testToMapForRelationQuery2() {
        String resource = "{identifier=[3eaabed0-92a2-4c3c-bced-3a44e8a5f51d], ti_md5=[md5Value], label=tech_info, ti_title=[href], ti_location=[${ref-path}/prepub_content_edu/esp/test/abc.png], ti_size=[1024], ti_format=[image/png], id=356900872, ti_entry=[入口地址], ti_requirements=[[{\"identifier\":null,\"type\":\"HARDWARE\",\"name\":\"resolution\",\"minVersion\":null,\"maxVersion\":null,\"installation\":null,\"installationFile\":null,\"value\":\"435*237\",\"ResourceModel\":null}]]}";
        Map<String, String> resultResourceMap = TitanResultParse.toMapForRelationQuery(resource);

        Map<String, String> expectResourceMap = new HashedMap<>();
        expectResourceMap.put("identifier", "3eaabed0-92a2-4c3c-bced-3a44e8a5f51d");
        expectResourceMap.put("ti_md5", "md5Value");
        expectResourceMap.put("label", "tech_info");
        expectResourceMap.put("ti_title", "href");
        expectResourceMap.put("ti_location", "${ref-path}/prepub_content_edu/esp/test/abc.png");
        expectResourceMap.put("ti_size", "1024");
        expectResourceMap.put("ti_format","image/png");
        expectResourceMap.put("id","356900872");
        expectResourceMap.put("ti_entry", "入口地址");
        expectResourceMap.put("ti_requirements", "[{\"identifier\":null,\"type\":\"HARDWARE\",\"name\":\"resolution\",\"minVersion\":null,\"maxVersion\":null,\"installation\":null,\"installationFile\":null,\"value\":\"435*237\",\"ResourceModel\":null}]");

        checkMapEqual(resultResourceMap, expectResourceMap);
    }
    /**
     * {identifier=7d4a95eb-c2da-4e28-9f14-300085396680, target_uuid=92505a6f-bef1-4641-abb7-b1454437e682, source_uuid=35ee3c2e-ce28-4959-8322-c637cf94a6f7, resource_target_type=coursewares, relation_type=ASSOCIATE, tags=["好玩","好喝"], res_type=chapters, label=has_relation, enable=true, sort_num=5000.0, order_num=0.0, rr_label=weo, id=x4se1h-6e5odk-2qs5-72ptl4}
     * 测试方法：toMapForRelationQuery
     */
    private static void testToMapForRelationQuery3() {
        String resource = "{identifier=7d4a95eb-c2da-4e28-9f14-300085396680, target_uuid=92505a6f-bef1-4641-abb7-b1454437e682, source_uuid=35ee3c2e-ce28-4959-8322-c637cf94a6f7, resource_target_type=coursewares, relation_type=ASSOCIATE, tags=[\"好玩\",\"好喝\"], res_type=chapters, label=has_relation, enable=true, sort_num=5000.0, order_num=0.0, rr_label=weo, id=x4se1h-6e5odk-2qs5-72ptl4}";
        Map<String, String> resultResourceMap = TitanResultParse.toMapForRelationQuery(resource);

        Map<String, String> expectResourceMap = new HashedMap<>();
        expectResourceMap.put("identifier", "7d4a95eb-c2da-4e28-9f14-300085396680");
        expectResourceMap.put("target_uuid", "92505a6f-bef1-4641-abb7-b1454437e682");
        expectResourceMap.put("source_uuid", "35ee3c2e-ce28-4959-8322-c637cf94a6f7");
        expectResourceMap.put("resource_target_type", "coursewares");
        expectResourceMap.put("relation_type", "ASSOCIATE");
        expectResourceMap.put("tags", "\"好玩\",\"好喝\"");
        expectResourceMap.put("res_type","chapters");
        expectResourceMap.put("label","has_relation");
        expectResourceMap.put("enable", "true");
        expectResourceMap.put("sort_num", "5000.0");
        expectResourceMap.put("order_num", "0.0");
        expectResourceMap.put("rr_label", "weo");
        expectResourceMap.put("id", "x4se1h-6e5odk-2qs5-72ptl4");

        checkMapEqual(resultResourceMap, expectResourceMap);
    }

    /**
     * ==>{identifier=531e9c35-02ec-4574-92ee-9d757c316df2, id=4qca38-134m8w-2exh-2hzby8, sta_data_from=TOTAL, sta_update_time=1470627616000, sta_res_type=assets, sta_key_value=10.0, label=has_resource_statistical, sta_key_title=downloads, sta_resource=046b6d6c-ba14-4561-8672-eaf19efc830d}
     * 测试统计数据（边）
     */
    private static void testToMapForRelationQuery5() {
        String resource = "==>{identifier=531e9c35-02ec-4574-92ee-9d757c316df2, id=4qca38-134m8w-2exh-2hzby8, sta_data_from=TOTAL, sta_update_time=1470627616000, sta_res_type=assets, sta_key_value=10.0, label=has_resource_statistical, sta_key_title=downloads, sta_resource=046b6d6c-ba14-4561-8672-eaf19efc830d}";
        Map<String, String> resultResourceMap = TitanResultParse.toMapForRelationQuery(resource);

        Map<String, String> expectResourceMap = new HashedMap<>();
        expectResourceMap.put("identifier", "531e9c35-02ec-4574-92ee-9d757c316df2");
        expectResourceMap.put("id", "4qca38-134m8w-2exh-2hzby8");
        expectResourceMap.put("sta_data_from", "TOTAL");
        expectResourceMap.put("sta_update_time", "1470627616000");
        expectResourceMap.put("sta_res_type", "assets");
        expectResourceMap.put("sta_key_value", "10.0");
        expectResourceMap.put("label","has_resource_statistical");
        expectResourceMap.put("sta_key_title","downloads");
        expectResourceMap.put("sta_resource", "046b6d6c-ba14-4561-8672-eaf19efc830d");

        checkMapEqual(resultResourceMap, expectResourceMap);
    }

    private static void testGetIndexByLabel1(){
        String resource = "==>{preview=[{\"png\":\"${ref-path}/prepub_content_edu_product/esp/assets/abc.png\"}], cr_author=[880508], search_path_string=[k12/$on030000/$on030200/$sb0501012/$e004000/$e004001], keywords=[[\"title\",\"qatest\"]], edu_description=[{\"zh_CN\":\"如何使用学习对象进行描述\"}], search_path=[K12/$ON030000/$ON030200/$SB0501012/$E004000/$E004001], description=[lcms_special_description_qa_test], search_coverage_string=[debug/qa//,debug/qa/test/creating,debug/qa//creating,debug/qa/test/], language=[zh_CN], lc_status=[CREATING], custom_properties=[{\"key\":\"test\"}], cr_has_right=[true], title=[lcms_qa_test_yqjtest_res_getinfo_with_include_of_all_attribute_ok_test_1471416223.61], cr_right_end_date=[7258089000000], lc_provider=[lcms_special_provider_qa_test], label=assets, cr_description=[版权描述信息], lc_create_time=[1471416133155], primary_category=[assets], search_code_string=[$f050005,$on030000,pt01001,$ra0100], search_coverage=[Debug/qa//CREATING, Debug/qa/TEST/CREATING, Debug/qa/TEST/, Debug/qa//], lc_publisher=[lcms_special_publisher_qa_test], id=356896776, edu_context=[基础教育], lc_provider_mode=[qatest_provider_mode], cr_right_start_date=[946656000000], identifier=[1e80454b-ae80-4dbd-994a-b3d8e55ee6b5], cr_right=[版权信息], lc_last_update=[1471416133155], edu_interactivity=[2], lc_version=[qav0.1], edu_semantic_density=[1], edu_difficulty=[easy], edu_end_user_type=[教师，管理者], tags=[[\"nd\",\"sdp.esp\"]], search_code=[$F050005, $ON030000, $RA0100, PT01001], m_identifier=[1e80454b-ae80-4dbd-994a-b3d8e55ee6b5], edu_interactivity_level=[2], lc_enable=[true], lc_provider_source=[八年级地理第一学期期末考试试卷_201407282056.doc], lc_creator=[lcms_special_creator_qa_test], edu_language=[zh_CN], edu_learning_time=[45], edu_age_range=[7岁以上]}";
        Map<String, String> resultResourceMap = TitanResultParse.toMapForRelationQuery(resource);
        List<Map<String, String>> resultStrMap=new ArrayList<>();
        resultStrMap.add(resultResourceMap);
        System.out.println(TitanResultParse.getIndexByLabel("assets",resultStrMap));
    }
    private static void testGetIndexByLabel2(){
        List<String> data = testData();
        // 数据转成key-value
        List<Map<String, String>> resultStrMap = TitanResultParse.changeStrToKeyValue(data);
        // 切割资源
        //List<List<Map<String, String>>> allItemMaps = cutOneItemMaps("assets", resultStrMap);
        /*// 解析资源
        for (List<Map<String, String>> oneItemMaps : allItemMaps) {
            parseResource("assets", oneItemMaps, IncludesConstant.getIncludesList(), false);
        }*/
        System.out.println(TitanResultParse.getIndexByLabel("assets",resultStrMap));
    }

    private static void testGetIndexByLabel3(){
        List<String> data = testData();
        // 数据转成key-value
        List<Map<String, String>> resultStrMap = TitanResultParse.changeStrToKeyValue(data);
        // 切割资源
        List<List<Map<String, String>>> allItemMaps = TitanResultParse.cutOneItemMaps("assets", resultStrMap);
        int count = 0;
        for(List<Map<String, String>> list:allItemMaps){
            count = count +list.size();
            System.out.println("map size:"+list.size()+"     first map is resource:" +list.get(0).get("label").equals("assets"));
        }
        System.out.println("afterCut:"+count+" input:"+data.size()+" isCountRight:" + (count == data.size()));

    }

    private static void testParse() {
        List<String> data = testData();
        TitanResultParse.parseToItemsResourceModel("assets",data, IncludesConstant.getIncludesList(),false);
    }


    /**
     * 校验map
     *
     * @param resultResourceMap
     * @param expectResourceMap
     */
    private static void checkMapEqual(Map<String, String> resultResourceMap,
                                      Map<String, String> expectResourceMap) {
        if (resultResourceMap.size() != expectResourceMap.size()) {
            System.out.println("size not equal");
        }

        for (Map.Entry<String, String> expectEntry : expectResourceMap
                .entrySet()) {
            String key = expectEntry.getKey();
            if (!expectEntry.getValue().equals(resultResourceMap.get(key))) {
                System.out.println("key: " + key + "\n expectValue: "
                        + expectEntry.getValue() + "\n resultValue: "
                        + resultResourceMap.get(key));
            }
        }
    }

    private static List<String> testData() {
        List<String> data = new ArrayList<>();
        data.add("==>{preview=[{\"previewKey\":\"previewValue\"}], identifier=[046b6d6c-ba14-4561-8672-eaf19efc830d], lc_last_update=[1438332053000], keywords=[[]], lc_version=[v0.3], description=[北京版纸牌屋延时摄影《麻将屋》 (1).mp4], search_coverage_string=[user/2107169263/shareing/transcode_waiting,user/2107169263//transcode_waiting,user/2107169263/owner/,user/2107169263/owner/transcode_waiting,user/2107169263/shareing/,user/2107169263//], language=[zh-CN], lc_status=[TRANSCODE_WAITING], cr_has_right=[false], title=[北京版纸牌屋延时摄影《麻将屋》 (1)], tags=[[]], search_code=[$F030001, $RA0100, $RA0103], id=65753280, label=assets, lc_enable=[true], lc_creator=[2107169263], lc_create_time=[1438246171000], primary_category=[assets], search_code_string=[$f030001,$ra0100,$ra0103], search_coverage=[User/2107169263//TRANSCODE_WAITING, User/2107169263/OWNER/TRANSCODE_WAITING, User/2107169263/OWNER/, User/2107169263/SHAREING/TRANSCODE_WAITING, User/2107169263/SHAREING/, User/2107169263//], lc_publisher=[]}");
        data.add("==>{cg_taxonpath=, identifier=94712c0c-2998-4590-a2b2-25dec7354026, cg_taxoncode=$RA0100, id=17norc-135bk0-2nmd-b9rzk, label=has_category_code, cg_taxonname=媒体素材, cg_category_code=$R, cg_short_name=assets, cg_category_name=resourcetype}");
        data.add("==>{identifier=97e63a9e-8518-45a4-b046-5c2668ceda40, cg_taxoncode=$F030001, id=17nod4-135bk0-2nmd-135uew, label=has_category_code, cg_taxonname=mp4视频文件, cg_category_code=$F, cg_short_name=video/mp4, cg_category_name=mediatype}");
        data.add("==>{cg_taxonpath=, identifier=9419e7ce-d939-4fb4-83ee-27bdb0851b51, cg_taxoncode=$RA0103, id=17np5k-135bk0-2nmd-13ixcw, label=has_category_code, cg_taxonname=视频, cg_category_code=$R, cg_short_name=video, cg_category_name=resourcetype}");
        data.add("==>{identifier=[2a4729df-62a3-4d7b-931f-73c2450b15de], id=66035872, label=tech_info, ti_title=[href], ti_printable=[false], ti_location=[${ref-path}/qa_content_edu/esp/assets/046b6d6c-ba14-4561-8672-eaf19efc830d.pkg/北京版纸牌屋延时摄影《麻将屋》 (1).mp4], ti_size=[6500269], ti_format=[video/mp4]}");
        data.add("==>{identifier=[f39dec51-82a1-4669-b722-315be3b62dc2], id=66359408, label=tech_info, ti_title=[source], ti_printable=[false], ti_location=[${ref-path}/qa_content_edu/esp/assets/046b6d6c-ba14-4561-8672-eaf19efc830d.pkg/北京版纸牌屋延时摄影《麻将屋》 (1).mp4], ti_size=[6500269], ti_format=[video/mp4]}");
        data.add("==>{identifier=531e9c35-02ec-4574-92ee-9d757c316df2, id=4vrzmg-135bk0-2fpx-2hqthc, label=has_resource_statistical, sta_data_from=TOTAL, sta_update_time=1470627616000, sta_res_type=assets, sta_key_value=10.0, sta_key_title=downloads, sta_resource=046b6d6c-ba14-4561-8672-eaf19efc830d}");
        data.add("==>{preview=[{\"previewKey\":\"previewValue\"}], identifier=[2960018c-4a12-4ccd-a81d-974c7a97f939], lc_last_update=[1438332054000], keywords=[[]], lc_version=[v0.3], description=[李行亮-回忆里的0150那个人.mp3], search_coverage_string=[user/2107169263/owner/,user/2107169263/owner/created,user/2107169263/shareing/,user/2107169263/shareing/created,user/2107169263//created,user/2107169263//], language=[zh-CN], lc_status=[CREATED], cr_has_right=[false], title=[李行亮-回忆里的0150那个人], tags=[[]], search_code=[$F020001, $RA0100, $RA0102], id=65847488, label=assets, lc_enable=[true], lc_creator=[2107169263], lc_create_time=[1438248043000], primary_category=[assets], search_code_string=[$f020001,$ra0100,$ra0102], search_coverage=[User/2107169263//CREATED, User/2107169263/OWNER/CREATED, User/2107169263/OWNER/, User/2107169263/SHAREING/CREATED, User/2107169263/SHAREING/, User/2107169263//], lc_publisher=[]}");
        data.add("==>{cg_taxonpath=, identifier=b7ce2cdc-2b38-46d5-9bc0-fcfe225dd4ff, cg_taxoncode=$RA0100, id=17s4d4-137c8w-2nmd-b9rzk, label=has_category_code, cg_taxonname=媒体素材, cg_category_code=$R, cg_short_name=assets, cg_category_name=resourcetype}");
        data.add("==>{identifier=0a104d9f-45ad-416f-b5bc-4142e68ff05f, cg_taxoncode=$F020001, id=17s3yw-137c8w-2nmd-13arjk, label=has_category_code, cg_taxonname=mp3音频文件, cg_category_code=$F, cg_short_name=audio/mp3, cg_category_name=mediatype}");
        data.add("==>{cg_taxonpath=, identifier=e7c40d95-e1ed-4350-a9c8-d9f28ce8c6ee, cg_taxoncode=$RA0102, id=17s4rc-137c8w-2nmd-13j3og, label=has_category_code, cg_taxonname=音频, cg_category_code=$R, cg_short_name=audio, cg_category_name=resourcetype}");
        data.add("==>{identifier=[6e5358d6-61ab-4d93-b233-335454fe8d93], id=66105480, label=tech_info, ti_title=[href], ti_printable=[false], ti_location=[${ref-path}/qa_content_edu/esp/assets/2960018c-4a12-4ccd-a81d-974c7a97f939.pkg/李行亮-回忆里的0150那个人.mp3], ti_size=[3642747], ti_format=[audio/mp3]}");
        data.add("==>{identifier=[d511c92d-15ad-4a0f-b733-3a0ad9526639], id=66146464, label=tech_info, ti_title=[source], ti_printable=[false], ti_location=[${ref-path}/qa_content_edu/esp/assets/2960018c-4a12-4ccd-a81d-974c7a97f939.pkg/李行亮-回忆里的0150那个人.mp3], ti_size=[3642747], ti_format=[audio/mp3]}");
        data.add("==>{preview=[{\"previewKey\":\"previewValue\"}], identifier=[23c6c5b9-de0a-46bd-a010-294f8a35e30c], lc_last_update=[1438332053000], keywords=[[]], lc_version=[v0.3], description=[IMG_1024.mp4], search_coverage_string=[user/2107169263/shareing/transcode_waiting,user/2107169263//transcode_waiting,user/2107169263/owner/,user/2107169263/owner/transcode_waiting,user/2107169263/shareing/,user/2107169263//], language=[zh-CN], lc_status=[TRANSCODE_WAITING], cr_has_right=[false], title=[IMG_1024], tags=[[]], search_code=[$F030001, $RA0100, $RA0103], id=66027656, label=assets, lc_enable=[true], lc_creator=[2107169263], lc_create_time=[1438246163000], primary_category=[assets], search_code_string=[$f030001,$ra0100,$ra0103], search_coverage=[User/2107169263//TRANSCODE_WAITING, User/2107169263/OWNER/TRANSCODE_WAITING, User/2107169263/OWNER/, User/2107169263/SHAREING/TRANSCODE_WAITING, User/2107169263/SHAREING/, User/2107169263//], lc_publisher=[]}");
        data.add("==>{cg_taxonpath=, identifier=c5c8b92c-9082-49b1-b1de-82209084ae9d, cg_taxoncode=$RA0100, id=17vhn5-13b79k-2nmd-b9rzk, label=has_category_code, cg_taxonname=媒体素材, cg_category_code=$R, cg_short_name=assets, cg_category_name=resourcetype}");
        data.add("==>{identifier=b7d3a4de-a8f2-4093-9409-029f76256ebe, cg_taxoncode=$F030001, id=17vh8x-13b79k-2nmd-135uew, label=has_category_code, cg_taxonname=mp4视频文件, cg_category_code=$F, cg_short_name=video/mp4, cg_category_name=mediatype}");
        data.add("==>{cg_taxonpath=, identifier=5984e1a0-cf91-46f9-ad91-df5577e9de17, cg_taxoncode=$RA0103, id=17vi1d-13b79k-2nmd-13ixcw, label=has_category_code, cg_taxonname=视频, cg_category_code=$R, cg_short_name=video, cg_category_name=resourcetype}");
        data.add("==>{identifier=[34601bd1-6a1d-478a-a1f8-9190eee60b17], id=65781952, label=tech_info, ti_title=[href], ti_printable=[false], ti_location=[${ref-path}/qa_content_edu/esp/assets/23c6c5b9-de0a-46bd-a010-294f8a35e30c.pkg/IMG_1024.mp4], ti_size=[2034979], ti_format=[video/mp4]}");
        data.add("==>{identifier=[582ee625-2e72-4adc-b837-e8add2860403], id=66011376, label=tech_info, ti_title=[source], ti_printable=[false], ti_location=[${ref-path}/qa_content_edu/esp/assets/23c6c5b9-de0a-46bd-a010-294f8a35e30c.pkg/IMG_1024.mp4], ti_size=[2034979], ti_format=[video/mp4]}");
        data.add("==>{preview=[{\"previewKey\":\"previewValue\"}], identifier=[3a9ade51-14a2-4e66-851e-18279a8f16cb], lc_last_update=[1438332054000], keywords=[[]], lc_version=[v0.3], description=[song.mp3], search_coverage_string=[user/2107169263/owner/,user/2107169263/owner/created,user/2107169263/shareing/,user/2107169263/shareing/created,user/2107169263//created,user/2107169263//], language=[zh-CN], lc_status=[CREATED], cr_has_right=[false], title=[song], tags=[[]], search_code=[$F020001, $RA0100, $RA0102], id=132272144, label=assets, lc_enable=[true], lc_creator=[2107169263], lc_create_time=[1438245183000], primary_category=[assets], search_code_string=[$f020001,$ra0100,$ra0102], search_coverage=[User/2107169263//CREATED, User/2107169263/OWNER/CREATED, User/2107169263/OWNER/, User/2107169263/SHAREING/CREATED, User/2107169263/SHAREING/, User/2107169263//], lc_publisher=[]}");
        data.add("==>{cg_taxonpath=, identifier=210962ed-05ba-40ab-b36a-e9b44356748f, cg_taxoncode=$RA0100, id=2g30n6-26r1u8-2nmd-b9rzk, label=has_category_code, cg_taxonname=媒体素材, cg_category_code=$R, cg_short_name=assets, cg_category_name=resourcetype}");
        data.add("==>{identifier=4abc1ec2-a7a7-463d-9336-5b11c9087c98, cg_taxoncode=$F020001, id=2g308y-26r1u8-2nmd-13arjk, label=has_category_code, cg_taxonname=mp3音频文件, cg_category_code=$F, cg_short_name=audio/mp3, cg_category_name=mediatype}");
        data.add("==>{cg_taxonpath=, identifier=518ae10b-32b8-4387-8795-5cbd223174e7, cg_taxoncode=$RA0102, id=2g311e-26r1u8-2nmd-13j3og, label=has_category_code, cg_taxonname=音频, cg_category_code=$R, cg_short_name=audio, cg_category_name=resourcetype}");
        data.add("==>{identifier=[66d04dce-d4a8-4b2d-8174-4544844d4304], id=65904696, label=tech_info, ti_title=[source], ti_printable=[false], ti_location=[${ref-path}/qa_content_edu/esp/assets/3a9ade51-14a2-4e66-851e-18279a8f16cb.pkg/song.mp3], ti_size=[160545], ti_format=[audio/mp3]}");
        data.add("==>{identifier=[bc77f29d-5057-4b27-962c-10cb21a4a07c], id=66465904, label=tech_info, ti_title=[href], ti_printable=[false], ti_location=[${ref-path}/qa_content_edu/esp/assets/3a9ade51-14a2-4e66-851e-18279a8f16cb.pkg/song.mp3], ti_size=[160545], ti_format=[audio/mp3]}");

        return data;
    }

}
