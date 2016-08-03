package nd.esp.service.lifecycle.services.titan;

import com.google.gson.reflect.TypeToken;
import nd.esp.service.lifecycle.educommon.models.*;
import nd.esp.service.lifecycle.models.teachingmaterial.v06.TeachingMaterialModel;
import nd.esp.service.lifecycle.models.teachingmaterial.v06.TmExtPropertiesModel;
import nd.esp.service.lifecycle.models.v06.EbookExtPropertiesModel;
import nd.esp.service.lifecycle.models.v06.EbookModel;
import nd.esp.service.lifecycle.models.v06.QuestionExtPropertyModel;
import nd.esp.service.lifecycle.models.v06.QuestionModel;
import nd.esp.service.lifecycle.support.busi.titan.TitanKeyWords;
import nd.esp.service.lifecycle.support.enums.ES_SearchField;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;
import nd.esp.service.lifecycle.vos.ListViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * @Title TitanResultParse
 * @Package nd.esp.service.lifecycle.services.titan
 * <p/>
 * *****************************************
 * @Description
 * @date 2016/6/27
 */
public class TitanResultParse {

    private static final Logger LOG = LoggerFactory.getLogger(TitanResultParse.class);

    /**
     * 解析资源
     * @param resType
     * @param resultStr
     * @return
     */
    public static ListViewModel<ResourceModel> parseToListView(String resType, List<String> resultStr) {
        long start = System.currentTimeMillis();
        ListViewModel<ResourceModel> viewModels = new ListViewModel<>();
        List<ResourceModel> items = new ArrayList<>();

        List<String> otherLines = new ArrayList<>();
        String taxOnPath = null;
        String mainResult = null;
        int count = 0;
        for (String line : resultStr) {
            if (count > 0 && (line.contains(ES_SearchField.lc_create_time.toString()) || line.startsWith(TitanKeyWords.TOTALCOUNT.toString()))) {
                items.add(TitanResultParse.parseResource(resType, mainResult, otherLines, taxOnPath));
                otherLines.clear();
                taxOnPath = null;
            }

            if (line.contains(TitanKeyWords.TOTALCOUNT.toString())) {
                viewModels.setTotal(Long.parseLong(line.split(":")[1].trim()));
            } else if (line.contains(ES_SearchField.cg_taxonpath.toString())) {
                Map<String, String> map = TitanResultParse.toMap(line);
                taxOnPath = map.get(ES_SearchField.cg_taxonpath.toString());
            } else if (line.contains(ES_SearchField.lc_create_time.toString())) {
                mainResult = line;
            } else {
                otherLines.add(line);
            }
            count++;
        }

        viewModels.setItems(items);
        LOG.info("parse consume times:" + (System.currentTimeMillis() - start));
        return viewModels;
    }

    /**
     * 解析资源
     * @param resType
     * @param mainResult
     * @param otherLines
     * @param taxOnPath
     * @return
     */
    public static ResourceModel parseResource(String resType, String mainResult, List<String> otherLines, String taxOnPath) {
        if (ResourceNdCode.ebooks.toString().equals(resType)) {
            return generateEbookModel(mainResult, otherLines, taxOnPath);
        } else if (ResourceNdCode.teachingmaterials.toString().equals(resType)) {
            generateTeachingMaterialModel(mainResult, otherLines, taxOnPath);
        } /*else if (ResourceNdCode.guidancebooks.toString().equals(resType)) {
        } */ else if (ResourceNdCode.questions.toString().equals(resType)) {
            return generateQuestionModel(mainResult, otherLines, taxOnPath);
        }
        return generateResourceModel(mainResult, otherLines, taxOnPath);
    }

    /**
     * TeachingMaterial的扩展属性
     * @param mainResult
     * @param strInOneItem
     * @param taxOnPath
     * @return
     */
    private static TeachingMaterialModel generateTeachingMaterialModel(String mainResult, List<String> strInOneItem, String taxOnPath) {
        TeachingMaterialModel item = new TeachingMaterialModel();
        Map<String, String> fieldMap = toMap(mainResult);
        dealMainResult(item, fieldMap);
        TmExtPropertiesModel extProperties = new TmExtPropertiesModel();
        extProperties.setIsbn(fieldMap.get("ext_isbn"));
        extProperties.setCriterion(fieldMap.get("ext_criterion"));
        String attachments = fieldMap.get("ext_attachments");
        if (attachments != null) {
            extProperties.setAttachments(Arrays.asList(attachments.replaceAll("\"", "").split(",")));
        }
        item.setExtProperties(extProperties);
        generateModel(item, strInOneItem, taxOnPath);
        return item;
    }

    /**
     * Ebook的扩展属性
     * @param mainResult
     * @param strInOneItem
     * @param taxOnPath
     * @return
     */
    private static EbookModel generateEbookModel(String mainResult, List<String> strInOneItem, String taxOnPath) {
        EbookModel item = new EbookModel();
        Map<String, String> fieldMap = toMap(mainResult);
        dealMainResult(item, fieldMap);
        EbookExtPropertiesModel extProperties = new EbookExtPropertiesModel();
        extProperties.setIsbn(fieldMap.get("ext_isbn"));
        extProperties.setCriterion(fieldMap.get("ext_criterion"));
        String attachments = fieldMap.get("ext_attachments");
        if (attachments != null) {
            extProperties.setAttachments(Arrays.asList(attachments.replaceAll("\"", "").split(",")));
        }
        item.setExtProperties(extProperties);
        generateModel(item, strInOneItem, taxOnPath);
        return item;
    }

    /**
     * @param mainResult
     * @param strInOneItem
     * @param taxOnPath
     * @return
     */
    private static QuestionModel generateQuestionModel(String mainResult, List<String> strInOneItem, String taxOnPath) {
        QuestionModel item = new QuestionModel();
        Map<String, String> fieldMap = toMap(mainResult);
        dealMainResult(item, fieldMap);
        QuestionExtPropertyModel extProperties = new QuestionExtPropertyModel();

        String discrimination = fieldMap.get("ext_discrimination");
        if (discrimination != null) {
            extProperties.setDiscrimination(Float.parseFloat(discrimination.trim()));
        }
        String answer = fieldMap.get("ext_answer");
        if (answer != null) {
            @SuppressWarnings("unchecked")
            Map<String, String> answerMap = ObjectUtils.fromJson(answer, Map.class);
            extProperties.setAnswer(answerMap);
        }
        String content = fieldMap.get("ext_item_content");
        if (content != null) {
            @SuppressWarnings("unchecked")
            Map<String, String> contentMap = ObjectUtils.fromJson(content, Map.class);
            extProperties.setItemContent(contentMap);
        }
        String criterion = fieldMap.get("ext_criterion");
        if (criterion != null) {
            @SuppressWarnings("unchecked")
            Map<String, String> criterionMap = ObjectUtils.fromJson(criterion, Map.class);
            extProperties.setCriterion(criterionMap);
        }
        String score = fieldMap.get("ext_score");
        if (score != null) {
            extProperties.setScore(Float.parseFloat(score.trim()));
        }
        String secrecy = fieldMap.get("ext_secrecy");
        if (secrecy != null) {
            extProperties.setSecrecy(Integer.parseInt(secrecy.trim()));
        }
        String modifiedDifficulty = fieldMap.get("ext_modified_difficulty");
        if (modifiedDifficulty != null) {
            extProperties.setModifiedDifficulty(Float.parseFloat(modifiedDifficulty.trim()));
        }
        String difficulty = fieldMap.get("ext_ext_difficulty");
        if (difficulty != null) {
            extProperties.setExtDifficulty(Float.parseFloat(difficulty.trim()));
        }
        String modifiedDiscrimination = fieldMap.get("ext_modified_discrimination");
        if (modifiedDiscrimination != null) {
            extProperties.setModifiedDiscrimination(Float.parseFloat(modifiedDiscrimination.trim()));
        }
        String usedTime = fieldMap.get("ext_used_time");
        if (usedTime != null) {
            extProperties.setUsedTime(Integer.parseInt(usedTime.trim()));
        }
        String exposalDate = fieldMap.get("ext_exposal_date");
        if (exposalDate != null) {
            extProperties.setExposalDate(new Date(new Long(exposalDate)));
        }
        extProperties.setAutoRemark("true".equals(fieldMap.get("ext_is_auto_remark")));

        item.setQuestionType(fieldMap.get("ext_question_type"));
        item.setExtProperties(extProperties);
        generateModel(item, strInOneItem, taxOnPath);
        return item;
    }

    /**
     * @param mainResult
     * @param strInOneItem
     * @param taxOnPath
     * @return
     */
    private static ResourceModel generateResourceModel(String mainResult, List<String> strInOneItem, String taxOnPath) {
        ResourceModel item = new ResourceModel();
        Map<String, String> fieldMap = toMap(mainResult);
        dealMainResult(item, fieldMap);
        generateModel(item, strInOneItem, taxOnPath);
        return item;
    }

    /**
     * @param strInOneItem
     * @param taxOnPath
     */
    private static void generateModel(ResourceModel item, List<String> strInOneItem, String taxOnPath) {
        List<ResTechInfoModel> techInfoList = new ArrayList<>();
        List<ResClassificationModel> categoryList = new ArrayList<>();

        for (String str : strInOneItem) {
            Map<String, String> fieldMap = toMap(str);
            if (str.contains(ES_SearchField.ti_format.toString())) { //tech_info
                techInfoList.add(dealTI(fieldMap));
            } else if (str.contains(ES_SearchField.cg_taxoncode.toString())) {// categoryList
                categoryList.add(dealCG(fieldMap, taxOnPath));
            }
        }
        item.setTechInfoList(techInfoList);
        item.setCategoryList(categoryList);
    }






    /**
     * @param item
     * @param fieldMap
     */
    public static void dealMainResult(ResourceModel item, Map<String, String> fieldMap) {
        item.setLifeCycle(dealLC(fieldMap));// LifeCycle
        item.setCopyright(dealCR(fieldMap));// Copyright
        item.setEducationInfo(dealEDU(fieldMap));// edu
        item.setIdentifier(fieldMap.get(ES_SearchField.identifier.toString()));
        item.setTitle(fieldMap.get(ES_SearchField.title.toString()));
        item.setDescription(fieldMap.get(ES_SearchField.description.toString()));
        item.setLanguage(fieldMap.get(ES_SearchField.language.toString()));

        String customProperties = fieldMap.get(ES_SearchField.custom_properties.toString());
        if (customProperties != null) {
            if (customProperties.startsWith("{\"") && customProperties.endsWith("\"}")) {
                item.setCustomProperties(customProperties);
            }
        }
        String preview = fieldMap.get(ES_SearchField.preview.toString());
        if (preview != null) {
            if (preview.startsWith("{\"") && preview.endsWith("\"}")) {
                @SuppressWarnings("unchecked")
                Map<String, String> previewMap = ObjectUtils.fromJson(preview, Map.class);
                if(previewMap == null){
                    previewMap = new HashMap<>();
                }
                item.setPreview(previewMap);
            } else {
                item.setPreview(new HashMap<String, String>());
            }
        }
        String tags = fieldMap.get(ES_SearchField.tags.toString());
        if (tags != null) {
            item.setTags(Arrays.asList(tags.replaceAll("\"", "").split(",")));
        }
        String keywords = fieldMap.get(ES_SearchField.keywords.toString());
        if (keywords != null) {
            item.setKeywords(Arrays.asList(keywords.replaceAll("\"", "").split(",")));
        }
    }

    /**
     * @param tmpMap
     * @return
     */
    public static ResEducationalModel dealEDU(Map<String, String> tmpMap) {
        ResEducationalModel edu = new ResEducationalModel();

        String interactivity = tmpMap.get(ES_SearchField.edu_interactivity.toString());
        if (interactivity != null) {
            edu.setInteractivity(Integer.parseInt(interactivity.trim()));
        }

        String interactivityLevel = tmpMap.get(ES_SearchField.edu_interactivity_level.toString());
        if (interactivityLevel != null) {
            edu.setInteractivity(Integer.parseInt(interactivityLevel.trim()));
        }


        edu.setEndUserType(tmpMap.get(ES_SearchField.edu_end_user_type.toString()));

        String semantic = tmpMap.get(ES_SearchField.edu_semantic_density.toString());
        if (semantic != null) {
            edu.setSemanticDensity(Long.parseLong(semantic));
        }

        edu.setAgeRange(tmpMap.get(ES_SearchField.edu_age_range.toString()));
        edu.setDifficulty(tmpMap.get(ES_SearchField.edu_difficulty.toString()));
        edu.setLearningTime(tmpMap.get(ES_SearchField.edu_learning_time.toString()));
        edu.setLanguage(tmpMap.get(ES_SearchField.edu_language.toString()));
        edu.setContext(tmpMap.get(ES_SearchField.edu_context.toString()));

        String description = tmpMap.get(ES_SearchField.edu_description.toString());
        if (description != null) {
            if (!"".equals(description.trim()) && !"null".equals(description.trim())) {

                if (description.startsWith("{\"") && description.endsWith("\"}")) {
                    @SuppressWarnings("unchecked")
                    Map<String, String> map = ObjectUtils.fromJson(description, Map.class);
                    edu.setDescription(map);
                }

            }
        }

        return edu;
    }

    /**
     * @param tmpMap
     * @param taxOnPath
     * @return
     */
    public static ResClassificationModel dealCG(Map<String, String> tmpMap, String taxOnPath) {
        ResClassificationModel rcm = new ResClassificationModel();
        rcm.setTaxoncode(tmpMap.get(ES_SearchField.cg_taxoncode.toString()));
        rcm.setTaxonname(tmpMap.get(ES_SearchField.cg_taxonname.toString()));
        rcm.setCategoryCode(tmpMap.get(ES_SearchField.cg_category_code.toString()));
        rcm.setShortName(tmpMap.get(ES_SearchField.cg_short_name.toString()));
        rcm.setCategoryName(tmpMap.get(ES_SearchField.cg_category_name.toString()));
        //System.out.println("cg_category_name:" + rcm.getCategoryName());
        if (taxOnPath != null) {
            if (taxOnPath.contains(tmpMap.get(ES_SearchField.cg_taxoncode.toString()))) {
                rcm.setTaxonpath(taxOnPath);
            }
        }
        return rcm;
    }

    /**
     * @param tmpMap
     * @return
     */
    public static ResClassificationModel dealCG(Map<String, String> tmpMap) {
        ResClassificationModel rcm = new ResClassificationModel();
        rcm.setTaxoncode(tmpMap.get("search_code"));
        rcm.setCategoryCode(tmpMap.get("search_coverage"));
        rcm.setTaxonpath(tmpMap.get("search_path"));

        return rcm;
    }

    /**
     * @param tmpMap
     * @return
     */
    public static ResTechInfoModel dealTI(Map<String, String> tmpMap) {
        ResTechInfoModel techInfo = new ResTechInfoModel();
        techInfo.setIdentifier(tmpMap.get(ES_SearchField.identifier.toString()));
        techInfo.setTitle(tmpMap.get(ES_SearchField.ti_title.toString()));
        techInfo.setFormat(tmpMap.get(ES_SearchField.ti_format.toString()));
        techInfo.setLocation(tmpMap.get(ES_SearchField.ti_location.toString()));
        techInfo.setMd5(tmpMap.get(ES_SearchField.ti_md5.toString()));

        String requirements = tmpMap.get(ES_SearchField.ti_requirements.toString());
        if (requirements != null) {
            if (!"".equals(requirements.trim()) && !"null".equals(requirements.trim())) {
                // System.out.println("requirements:" + requirements);
                List<TechnologyRequirementModel> requirementsList = ObjectUtils.fromJson("[" + requirements + "]", new TypeToken<List<TechnologyRequirementModel>>() {
                });
                techInfo.setRequirements(requirementsList);
            }
        }
        techInfo.setSecureKey(tmpMap.get(ES_SearchField.ti_secure_key.toString()));

        String size = tmpMap.get(ES_SearchField.ti_size);
        if (size != null) {
            techInfo.setSize(Long.parseLong(size));
        }
        techInfo.setEntry(tmpMap.get(ES_SearchField.ti_entry.toString()));

        return techInfo;
    }

    /**
     * @param tmpMap
     * @return
     */
    public static ResRightModel dealCR(Map<String, String> tmpMap) {
        ResRightModel copyright = new ResRightModel();
        copyright.setAuthor(tmpMap.get(ES_SearchField.cr_author.toString()));
        copyright.setRight(tmpMap.get(ES_SearchField.cr_right.toString()));
        copyright.setDescription(tmpMap.get(ES_SearchField.cr_description.toString()));
        return copyright;
    }

    /**
     * @param tmpMap
     * @return
     */
    public static ResLifeCycleModel dealLC(Map<String, String> tmpMap) {
        ResLifeCycleModel lifeCycle = new ResLifeCycleModel();
        lifeCycle.setVersion(tmpMap.get(ES_SearchField.lc_version.toString()));
        lifeCycle.setStatus(tmpMap.get(ES_SearchField.lc_status.toString()));
        lifeCycle.setEnable("true".equals(tmpMap.get(ES_SearchField.lc_enable.toString())));
        // lifeCycle.setResource();
        // lifeCycle.setResContributes();
        lifeCycle.setCreator(tmpMap.get(ES_SearchField.lc_creator.toString()));
        lifeCycle.setPublisher(tmpMap.get(ES_SearchField.lc_publisher.toString()));
        //lifeCycle.setPublisher(tmpMap.get("lc_version"));
        lifeCycle.setProvider(tmpMap.get(ES_SearchField.lc_provider.toString()));
        lifeCycle.setProviderSource(tmpMap.get(ES_SearchField.lc_provider_source.toString()));
        lifeCycle.setCreateTime(StringUtils.strTimeStampToDate(tmpMap.get(ES_SearchField.lc_create_time.toString())));
        lifeCycle.setLastUpdate(StringUtils.strTimeStampToDate(tmpMap.get(ES_SearchField.lc_last_update.toString())));
        return lifeCycle;
    }


    /**
     * @param str
     * @return
     */
    public static Map<String, String> toMap(String str) {
        str = str.replaceAll("==>", "").replaceAll("\\[", "");
        str = str.substring(1, str.length() - 1);
        String[] fields = str.split("], ");
        Map<String, String> tmpMap = new HashMap<>();
        for (String s : fields) {
            String kv = s.replaceAll("]", "");
            int begin = kv.indexOf("=");
            tmpMap.put(kv.substring(0, begin), kv.substring(begin + 1, kv.length()));
        }
        return tmpMap;
    }

    public static Map<String, String> toMapForSearchES(String str) {
        Map<String, String> tmpMap = new HashMap<>();
        str = str.replaceAll("==>", "").replaceAll("vp\\[", "");
        str = str.substring(1, str.length() - 2);
        String[] fields = str.split("], ");

        for (String s : fields) {
            String[] kv = s.split("->");
            if (kv.length != 2) continue;
            String k = kv[0];
            String v = kv[1];
            if (tmpMap.containsKey(k)) {
                tmpMap.put(k, tmpMap.get(k) + "," + v);
            } else {
                tmpMap.put(k, v);
            }

        }


        return tmpMap;


    }

}
