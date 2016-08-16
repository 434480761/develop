package nd.esp.service.lifecycle.services.titan;

import com.google.gson.reflect.TypeToken;
import nd.esp.service.lifecycle.educommon.models.*;
import nd.esp.service.lifecycle.educommon.vos.constant.IncludesConstant;
import nd.esp.service.lifecycle.models.teachingmaterial.v06.TeachingMaterialModel;
import nd.esp.service.lifecycle.models.teachingmaterial.v06.TmExtPropertiesModel;
import nd.esp.service.lifecycle.models.v06.*;
import nd.esp.service.lifecycle.support.busi.titan.TitanKeyWords;
import nd.esp.service.lifecycle.support.enums.ES_SearchField;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;
import nd.esp.service.lifecycle.utils.BigDecimalUtils;
import nd.esp.service.lifecycle.utils.CollectionUtils;
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
     *
     * @param resType
     * @param resultStr
     * @return
     */
    public static ListViewModel<ResourceModel> parseToListView(String resType, List<String> resultStr, List<String> includes, Boolean isCommonQuery) {
        long start = System.currentTimeMillis();
        ListViewModel<ResourceModel> viewModels = new ListViewModel<>();
        List<ResourceModel> items = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(resultStr)) {
            int resultSize = resultStr.size();
            // 处理count
            String countStr = resultStr.get(resultSize - 1);
            if (StringUtils.isNotEmpty(countStr) && countStr.contains(TitanKeyWords.TOTALCOUNT.toString())) {
                viewModels.setTotal(Long.parseLong(countStr.split("=")[1].trim()));
                resultStr.remove(resultSize - 1);
            }
            if (resultStr.size() > 0) {
                // 数据转成key-value
                List<Map<String, String>> resultStrMap = new ArrayList<>();
                for (String str : resultStr) {
                    Map<String, String> tmp = toMapWithLabel(str);
                    if (CollectionUtils.isNotEmpty(tmp)) resultStrMap.add(tmp);
                }

                // 切割资源
                List<Integer> indexArray = getIndexByLabel(resType, resultStrMap);
                if (CollectionUtils.isNotEmpty(indexArray) && indexArray.size() > 1) {
                    List<List<Map<String, String>>> allItemMaps = new ArrayList<>();
                    for (int i = 0; i < indexArray.size() - 1; i++) {
                        int begin = indexArray.get(i);
                        int end = indexArray.get(i + 1);
                        allItemMaps.add(resultStrMap.subList(begin, end));
                    }
                    // 解析资源
                    for (List<Map<String, String>> oneItemMaps : allItemMaps) {
                        items.add(parseResource(resType, oneItemMaps, includes, isCommonQuery));
                    }
                }
            }
        }
        viewModels.setItems(items);
        LOG.info("parse consume times:" + (System.currentTimeMillis() - start));
        return viewModels;
    }


    /**
     * 返回数据切割的index
     *
     * @return
     */
    public static List<Integer> getIndexByLabel(String resType, List<Map<String, String>> resultStrMap) {
        List<Integer> indexArray = new ArrayList<>();
        indexArray.add(0);
        boolean isKnowledge = ResourceNdCode.knowledges.toString().equals(resType);
        int size = resultStrMap.size();
        int endSize = size - 1;
        for (int i = 0; i < size; i++) {
            String label = resultStrMap.get(i).get("label");
            if (isKnowledge) {
                // 发现存在order跳过下一行数据 order-->has_knowledge
                if ("has_knowledge".equals(label)) i++;
            }
            // 检查切割点
            boolean isEnd = (i == endSize) || ((i < endSize) && resType.equals(resultStrMap.get(i + 1).get("label")));
            if (isEnd) indexArray.add(i + 1);
        }
        return indexArray;
    }

    /**
     *
     * @param resType
     * @param oneItemMaps
     * @param includes
     * @param isCommonQuery
     * @return
     */
    private static ResourceModel parseResource(String resType, List<Map<String, String>> oneItemMaps, List<String> includes, Boolean isCommonQuery) {
        // 识别数据
        boolean isKnowledge = ResourceNdCode.knowledges.toString().equals(resType);
        TitanResultItem titanItem = discernData(resType, oneItemMaps, isCommonQuery);

        if (ResourceNdCode.ebooks.toString().equals(resType)) {
            return generateEbookModel(titanItem, includes);
        } else if (ResourceNdCode.teachingmaterials.toString().equals(resType)) {
            generateTeachingMaterialModel(titanItem, includes);
        } /*else if (ResourceNdCode.guidancebooks.toString().equals(resType)) {
        } */ else if (ResourceNdCode.questions.toString().equals(resType)) {
            return generateQuestionModel(titanItem, includes);
        } else if (isKnowledge) {
            return generateKnowledgeModel(titanItem, includes);
        }
        return generateResourceModel(titanItem,includes);


    }


    /**
     *
     * @param resType
     * @param singleItemMaps
     * @param isCommonQuery
     * @return
     */
    private static TitanResultItem discernData(String resType, List<Map<String, String>> singleItemMaps, Boolean isCommonQuery) {
        TitanResultItem item = new TitanResultItem();
        if (CollectionUtils.isNotEmpty(singleItemMaps)) {
            Map<String, String> resource = new HashMap<>();
            List<Map<String, String>> category = new ArrayList<>();
            List<Map<String, String>> techInfo = new ArrayList<>();
            int size = singleItemMaps.size();
            for (int i = 0; i < size; i++) {
                Map<String, String> map = singleItemMaps.get(i);
                String label = map.get("label");
                if (resType.equals(label)) {
                    resource.putAll(map);
                } else if ("has_category_code".equals(label)) {
                    category.add(map);
                } else if ("tech_info".equals(label)) {
                    techInfo.add(map);
                } else if ("has_knowledge".equals(label)) {
                    resource.put("order", map.get("order"));
                    // 处理parent
                    if (i < size - 1) {
                        i++;
                        Map<String, String> parent = singleItemMaps.get(i);
                        String label2 = parent.get("label");
                        if ("category_code".equals(label2)) {
                            resource.put("parent", parent.get("cg_taxoncode"));
                        } else if ("knowledges".equals(label2) || "chapters".equals(label2)) {
                            if (!isCommonQuery) {
                                resource.put("parent", "ROOT");
                            } else {
                                resource.put("parent", map.get(ES_SearchField.identifier.toString()));
                            }
                        } else {
                            LOG.warn("parent--未能识别");
                        }
                    }
                } else {
                    LOG.warn("未能识别");
                }
            }
            item.setResource(resource);
            item.setCategory(category);
            item.setTechInfo(techInfo);
        }

        return item;
    }


    /**
     *
     * @param titanItem
     * @param includes
     * @return
     */
    private static KnowledgeModel generateKnowledgeModel(TitanResultItem titanItem, List<String> includes) {
        KnowledgeModel item = new KnowledgeModel();
        Map<String, String> mainResult = titanItem.getResource();
        if(CollectionUtils.isNotEmpty(mainResult)) {
            dealMainResult(item, mainResult, includes);
            KnowledgeExtPropertiesModel extProperties = new KnowledgeExtPropertiesModel();

            extProperties.setParent(mainResult.get("parent"));
            String order = mainResult.get("order");
            if (order != null && !"".equals(order.trim())&& !"null".equals(order.trim())) {
                extProperties.setOrder_num((int) Float.parseFloat(order));
            }

            //extProperties.setTarget(fieldMap.get("ext_target"));
            //extProperties.setDirection(fieldMap.get("ext_direction"));
            //extProperties.setRootNode(fieldMap.get("ext_rootnode"));

            item.setExtProperties(extProperties);
        }
        generateModel(item, titanItem, includes);
        return item;
    }


    /**
     *
     * @param titanItem
     * @param includes
     * @return
     */
    private static TeachingMaterialModel generateTeachingMaterialModel(TitanResultItem titanItem, List<String> includes) {
        TeachingMaterialModel item = new TeachingMaterialModel();
        Map<String, String> mainResult = titanItem.getResource();
        if (CollectionUtils.isNotEmpty(mainResult)) {
            dealMainResult(item, mainResult, includes);
            TmExtPropertiesModel extProperties = new TmExtPropertiesModel();
            extProperties.setIsbn(mainResult.get("ext_isbn"));
            extProperties.setCriterion(mainResult.get("ext_criterion"));
            String attachments = mainResult.get("ext_attachments");
            if (attachments != null) {
                extProperties.setAttachments(Arrays.asList(attachments.replaceAll("\"", "").split(",")));
            }
            item.setExtProperties(extProperties);
        }
        generateModel(item, titanItem, includes);
        return item;
    }

    /**
     * @param titanItem
     * @param includes
     * @return
     */
    private static EbookModel generateEbookModel(TitanResultItem titanItem, List<String> includes) {
        EbookModel item = new EbookModel();
        Map<String, String> mainResult = titanItem.getResource();
        if (CollectionUtils.isNotEmpty(mainResult)) {
            dealMainResult(item, mainResult, includes);
            EbookExtPropertiesModel extProperties = new EbookExtPropertiesModel();
            extProperties.setIsbn(mainResult.get("ext_isbn"));
            extProperties.setCriterion(mainResult.get("ext_criterion"));
            String attachments = mainResult.get("ext_attachments");
            if (attachments != null) {
                extProperties.setAttachments(Arrays.asList(attachments.replaceAll("\"", "").split(",")));
            }
            item.setExtProperties(extProperties);
        }
        generateModel(item, titanItem, includes);
        return item;
    }

    /**
     *
     * @param titanItem
     * @param includes
     * @return
     */
    private static QuestionModel generateQuestionModel(TitanResultItem titanItem,List<String> includes) {
        QuestionModel item = new QuestionModel();
         Map<String, String> mainResult=titanItem.getResource();
        if (CollectionUtils.isNotEmpty(mainResult)) {
            dealMainResult(item, mainResult, includes);
            QuestionExtPropertyModel extProperties = new QuestionExtPropertyModel();

            String discrimination = mainResult.get("ext_discrimination");
            if (discrimination != null) {
                extProperties.setDiscrimination(Float.parseFloat(discrimination.trim()));
            }
            String answer = mainResult.get("ext_answer");
            if (answer != null) {
                @SuppressWarnings("unchecked")
                Map<String, String> answerMap = ObjectUtils.fromJson(answer, Map.class);
                extProperties.setAnswer(answerMap);
            }
            String content = mainResult.get("ext_item_content");
            if (content != null) {
                @SuppressWarnings("unchecked")
                Map<String, String> contentMap = ObjectUtils.fromJson(content, Map.class);
                extProperties.setItemContent(contentMap);
            }
            String criterion = mainResult.get("ext_criterion");
            if (criterion != null) {
                @SuppressWarnings("unchecked")
                Map<String, String> criterionMap = ObjectUtils.fromJson(criterion, Map.class);
                extProperties.setCriterion(criterionMap);
            }
            String score = mainResult.get("ext_score");
            if (score != null) {
                extProperties.setScore(Float.parseFloat(score.trim()));
            }
            String secrecy = mainResult.get("ext_secrecy");
            if (secrecy != null) {
                extProperties.setSecrecy(Integer.parseInt(secrecy.trim()));
            }
            String modifiedDifficulty = mainResult.get("ext_modified_difficulty");
            if (modifiedDifficulty != null) {
                extProperties.setModifiedDifficulty(Float.parseFloat(modifiedDifficulty.trim()));
            }
            String difficulty = mainResult.get("ext_ext_difficulty");
            if (difficulty != null) {
                extProperties.setExtDifficulty(Float.parseFloat(difficulty.trim()));
            }
            String modifiedDiscrimination = mainResult.get("ext_modified_discrimination");
            if (modifiedDiscrimination != null) {
                extProperties.setModifiedDiscrimination(Float.parseFloat(modifiedDiscrimination.trim()));
            }
            String usedTime = mainResult.get("ext_used_time");
            if (usedTime != null) {
                extProperties.setUsedTime(Integer.parseInt(usedTime.trim()));
            }
            String exposalDate = mainResult.get("ext_exposal_date");
            if (exposalDate != null) {
                extProperties.setExposalDate(new Date(new Long(exposalDate)));
            }
            extProperties.setAutoRemark("true".equals(mainResult.get("ext_is_auto_remark")));

            item.setQuestionType(mainResult.get("ext_question_type"));
            item.setExtProperties(extProperties);
        }
        generateModel(item, titanItem, includes);
        return item;
    }


    /**
     *
     * @param titanItem
     * @param includes
     * @return
     */
    private static ResourceModel generateResourceModel(TitanResultItem titanItem, List<String> includes) {
        ResourceModel item = new ResourceModel();
        dealMainResult(item, titanItem.getResource(), includes);
        generateModel(item, titanItem, includes);
        return item;
    }

    /**
     *
     * @param item
     * @param titanItem
     * @param includes
     */
    private static void generateModel(ResourceModel item, TitanResultItem titanItem,List<String> includes) {
        List<ResTechInfoModel> techInfoList = new ArrayList<>();
        List<ResClassificationModel> categoryList = new ArrayList<>();

        if (includes.contains(IncludesConstant.INCLUDE_TI)) {
            List<Map<String, String>> techInfo = titanItem.getTechInfo();
            if (CollectionUtils.isNotEmpty(techInfo)) {
                for (Map<String, String> fieldMap : techInfo) {
                    if (CollectionUtils.isNotEmpty(fieldMap)) techInfoList.add(dealTI(fieldMap));
                }
            }
        }

        if (includes.contains(IncludesConstant.INCLUDE_CG)) {
            List<Map<String, String>> category=titanItem.getCategory();
            if (CollectionUtils.isNotEmpty(category)) {
                for (Map<String, String> fieldMap : category) {
                    if (CollectionUtils.isNotEmpty(fieldMap)) categoryList.add(dealCG(fieldMap));
                }
            }
        }

        item.setTechInfoList(techInfoList);
        item.setCategoryList(categoryList);
    }






    /**
     * @param item
     * @param fieldMap
     */
    public static void dealMainResult(ResourceModel item, Map<String, String> fieldMap,List<String> includes) {
        if (CollectionUtils.isNotEmpty(fieldMap)) {
            if (includes.contains(IncludesConstant.INCLUDE_LC)) {
                item.setLifeCycle(dealLC(fieldMap));// LifeCycle
            }
            if (includes.contains(IncludesConstant.INCLUDE_CR)) {
                item.setCopyright(dealCR(fieldMap));// Copyright
            }
            if (includes.contains(IncludesConstant.INCLUDE_EDU)) {
                item.setEducationInfo(dealEDU(fieldMap));// edu
            }
            item.setIdentifier(fieldMap.get(ES_SearchField.identifier.toString()));
            item.setTitle(fieldMap.get(ES_SearchField.title.toString()));
            item.setDescription(fieldMap.get(ES_SearchField.description.toString()));
            item.setLanguage(fieldMap.get(ES_SearchField.language.toString()));
            item.setmIdentifier(fieldMap.get(ES_SearchField.m_identifier.toString()));
            item.setNdresCode(fieldMap.get(ES_SearchField.ndres_code.toString()));

            String customProperties = fieldMap.get(ES_SearchField.custom_properties.toString());
            if (customProperties != null) {
                if (customProperties.startsWith("{\"") && customProperties.endsWith("\"}")) {
                    item.setCustomProperties(customProperties);
                } else {
                    item.setCustomProperties("{}");
                }
            }
            String preview = fieldMap.get(ES_SearchField.preview.toString());
            if (preview != null) {
                if (preview.startsWith("{\"") && preview.endsWith("\"}")) {
                    @SuppressWarnings("unchecked")
                    Map<String, String> previewMap = ObjectUtils.fromJson(preview, Map.class);
                    if (previewMap == null) {
                        previewMap = new HashMap<>();
                    }
                    item.setPreview(previewMap);
                } else {
                    item.setPreview(new HashMap<String, String>());
                }
            } else {
                //教学目标、知识点、课时没有这个值，但需要返回一个空的map集合
                item.setPreview(new HashMap<String, String>());
            }
            String tags = fieldMap.get(ES_SearchField.tags.toString());
            if (StringUtils.isNotEmpty(tags)) {
                item.setTags(Arrays.asList(tags.replaceAll("\"", "").split(",")));
            } else if (tags != null) {
                item.setTags(new ArrayList<String>());
            }
            String keywords = fieldMap.get(ES_SearchField.keywords.toString());
            if (StringUtils.isNotEmpty(keywords)) {
                item.setKeywords(Arrays.asList(keywords.replaceAll("\"", "").split(",")));
            } else if (keywords != null) {
                item.setKeywords(new ArrayList<String>());
            }
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
     * @return
     */
    public static ResClassificationModel dealCG(Map<String, String> tmpMap) {
        ResClassificationModel rcm = new ResClassificationModel();
        rcm.setIdentifier(tmpMap.get(ES_SearchField.identifier.toString()));
        rcm.setTaxoncode(tmpMap.get(ES_SearchField.cg_taxoncode.toString()));
        rcm.setTaxonname(tmpMap.get(ES_SearchField.cg_taxonname.toString()));
        rcm.setCategoryCode(tmpMap.get(ES_SearchField.cg_category_code.toString()));
        rcm.setShortName(tmpMap.get(ES_SearchField.cg_short_name.toString()));
        rcm.setCategoryName(tmpMap.get(ES_SearchField.cg_category_name.toString()));
        rcm.setTaxonpath(tmpMap.get(ES_SearchField.cg_taxonpath.toString()));

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

        String size = tmpMap.get(ES_SearchField.ti_size.toString());
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
        copyright.setHasRight("true".equals(tmpMap.get(ES_SearchField.cr_has_right.toString())));
        copyright.setRightStartDate(BigDecimalUtils.toBigDecimal(tmpMap.get(ES_SearchField.cr_right_start_date.toString())));
        copyright.setRightEndDate(BigDecimalUtils.toBigDecimal(tmpMap.get(ES_SearchField.cr_right_end_date.toString())));
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
        lifeCycle.setProviderMode(tmpMap.get(ES_SearchField.lc_provider_mode.toString()));
        return lifeCycle;
    }


    /**
     * @param str
     * @return
     */
    public static Map<String, String> toMapWithLabel(String str) {
        Map<String, String> tmpMap = new HashMap<>();
        if(StringUtils.isNotEmpty(str)) {
            str = str.replaceAll("==>", "");
            str = str.substring(1, str.length() - 1);
            if (str.endsWith("]")) str = str.substring(0, str.length() - 1);
            String[] fields = null;
            if (str.contains("], ")) {
                str = str.replaceAll("=\\[", "=").replaceAll("=\\[", "=").replaceAll("]],", "],");
                fields = str.split("], ");
                for (String s : fields) {
                    if (s.startsWith("label=") || s.startsWith("id=")) {
                        //点上的label特殊处理
                        int end = s.indexOf(", ");
                        if (end > 0) {
                            String label = s.substring(0, end);
                            String[] kv1 = label.split("=");
                            if (kv1.length == 2) tmpMap.put(kv1[0].trim(), kv1[1].trim());
                            String other = s.substring(end+1, s.length());
                            String[] kv2 = other.split("=");
                            if (kv2.length == 2) tmpMap.put(kv2[0].trim(), kv2[1].trim());

                        }else{
                            String[] kv = s.split("=");
                            if (kv.length == 2) tmpMap.put(kv[0].trim(), kv[1].trim());
                        }
                        //tmpMap.put(kv.substring(0, begin), kv.substring(begin + 1, kv.length()));
                    } else {
                        String[] kv = s.split("=");
                        if (kv.length == 2) tmpMap.put(kv[0].trim(), kv[1].trim());
                    }
                }
            }else if(str.contains(", ")){//edge
                fields = str.split(", ");
                for (String s : fields) {
                    String[] kv=s.split("=");
                    if (kv.length == 2) tmpMap.put(kv[0].trim(), kv[1].trim());
                }
            } else {
                String[] kv=str.split("=");
                if (kv.length == 2) tmpMap.put(kv[0].trim(), kv[1].trim());
            }
        }
        return tmpMap;
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


}
