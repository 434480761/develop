package nd.esp.service.lifecycle.services.titan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nd.esp.service.lifecycle.educommon.models.ResClassificationModel;
import nd.esp.service.lifecycle.educommon.models.ResEducationalModel;
import nd.esp.service.lifecycle.educommon.models.ResLifeCycleModel;
import nd.esp.service.lifecycle.educommon.models.ResRightModel;
import nd.esp.service.lifecycle.educommon.models.ResTechInfoModel;
import nd.esp.service.lifecycle.educommon.models.ResourceModel;
import nd.esp.service.lifecycle.educommon.models.TechnologyRequirementModel;
import nd.esp.service.lifecycle.educommon.vos.constant.IncludesConstant;
import nd.esp.service.lifecycle.models.teachingmaterial.v06.TeachingMaterialModel;
import nd.esp.service.lifecycle.models.teachingmaterial.v06.TmExtPropertiesModel;
import nd.esp.service.lifecycle.models.v06.EbookExtPropertiesModel;
import nd.esp.service.lifecycle.models.v06.EbookModel;
import nd.esp.service.lifecycle.models.v06.KnowledgeExtPropertiesModel;
import nd.esp.service.lifecycle.models.v06.KnowledgeModel;
import nd.esp.service.lifecycle.models.v06.QuestionExtPropertyModel;
import nd.esp.service.lifecycle.models.v06.QuestionModel;
import nd.esp.service.lifecycle.support.busi.titan.TitanKeyWords;
import nd.esp.service.lifecycle.support.enums.ES_SearchField;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;
import nd.esp.service.lifecycle.utils.BigDecimalUtils;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;
import nd.esp.service.lifecycle.vos.ListViewModel;

import org.apache.commons.collections4.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.reflect.TypeToken;

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
    public static ListViewModel<ResourceModel> parseToListViewResourceModel(String resType, List<String> resultStr, List<String> includes, Boolean isCommonQuery) {
        ListViewModel<ResourceModel> viewModels = new ListViewModel<>();
        List<ResourceModel> items = new ArrayList<ResourceModel>();
        if (CollectionUtils.isNotEmpty(resultStr)) {
            int resultSize = resultStr.size();
            // 处理count
            String countStr = resultStr.get(resultSize - 1);
            if (StringUtils.isNotEmpty(countStr) && countStr.contains(TitanKeyWords.TOTALCOUNT.toString())) {
                viewModels.setTotal(Long.parseLong(countStr.split("=")[1].trim()));
                resultStr.remove(resultSize - 1);
            }
            // 解析items
            items = parseToItemsResourceModel(resType, resultStr, includes, isCommonQuery);
        }
        viewModels.setItems(items);
        return viewModels;
    }


    /**
     *
     * @param resType
     * @param resultStr
     * @param includes
     * @param isCommonQuery
     * @return
     */
    public static List<ResourceModel> parseToItemsResourceModel(String resType, List<String> resultStr, List<String> includes, Boolean isCommonQuery) {
        long start = System.currentTimeMillis();
        List<ResourceModel> items = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(resultStr)) {
            // 数据转成key-value
            List<Map<String, String>> resultStrMap = changeStrToKeyValue(resultStr);
            // 切割资源
            List<List<Map<String, String>>> allItemMaps = cutOneItemMaps(resType, resultStrMap);
            // 解析资源
            for (List<Map<String, String>> oneItemMaps : allItemMaps) {
                items.add(parseResource(resType, oneItemMaps, includes, isCommonQuery));
            }
        }
        LOG.info("parse consume times:" + (System.currentTimeMillis() - start));
        return items;
    }

    /**
     *
     * @param resultStr
     * @return
     */
    public static List<Map<String, String>> changeStrToKeyValue(List<String> resultStr) {
        List<Map<String, String>> resultStrMap = new ArrayList<>();
        for (String str : resultStr) {
            Map<String, String> tmp = toMapForRelationQuery(str);
            if (CollectionUtils.isNotEmpty(tmp)) resultStrMap.add(tmp);
        }
        return resultStrMap;
    }

    /**
     *
     * @param resType
     * @param resultStrMap
     * @return
     */
    public static List<List<Map<String, String>>> cutOneItemMaps(String resType, List<Map<String, String>> resultStrMap) {
        // 切割资源
        List<Integer> indexArray = getIndexByLabel(resType, resultStrMap);
        List<List<Map<String, String>>> allItemMaps = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(indexArray) && indexArray.size() > 1) {
            for (int i = 0; i < indexArray.size() - 1; i++) {
                int begin = indexArray.get(i);
                int end = indexArray.get(i + 1);
                allItemMaps.add(resultStrMap.subList(begin, end));
            }

        }
        return allItemMaps;
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
                if (TitanKeyWords.tree_has_knowledge.toString().equals(label)) i++;
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
    public static TitanResultItem discernData(String resType, List<Map<String, String>> singleItemMaps, Boolean isCommonQuery) {
        TitanResultItem item = new TitanResultItem();
        if (CollectionUtils.isNotEmpty(singleItemMaps)) {
            Map<String, String> resource = new HashMap<>();
            List<Map<String, String>> category = new ArrayList<>();
            List<Map<String, String>> techInfo = new ArrayList<>();
            Map<String, String> statistics = new HashMap<>();
            int size = singleItemMaps.size();
            for (int i = 0; i < size; i++) {
                Map<String, String> map = singleItemMaps.get(i);
                String label = map.get("label");
                if (resType.equals(label)) {
                    resource.putAll(map);
                } else if (TitanKeyWords.has_category_code.toString().equals(label)) {
                    category.add(map);
                } else if (TitanKeyWords.tech_info.toString().equals(label)) {
                    techInfo.add(map);
                } else if (TitanKeyWords.tree_has_knowledge.toString().equals(label)) {
                    resource.put(TitanKeyWords.tree_order.toString(), map.get(TitanKeyWords.tree_order.toString()));
                    // 处理parent
                    if (i < size - 1) {
                        i++;
                        Map<String, String> parent = singleItemMaps.get(i);
                        String label2 = parent.get("label");
                        if (TitanKeyWords.category_code.toString().equals(label2)) {
                            resource.put("parent", parent.get("cg_taxoncode"));
                        } else if (ResourceNdCode.knowledges.toString().equals(label2)) {
                            resource.put("parent", parent.get(ES_SearchField.identifier.toString()));
                        }else if(ResourceNdCode.chapters.toString().equals(label2)){
                            if (!isCommonQuery) {
                                resource.put("parent", "ROOT");
                            } else {
                                resource.put("parent", parent.get(ES_SearchField.identifier.toString()));
                            }
                        } else {
                            LOG.warn("parent--未能识别");
                        }
                    }
                } else if (TitanKeyWords.has_resource_statistical.toString().equals(label)) {
                    statistics.putAll(map);
                } else {
                    LOG.warn("未能识别");
                }
            }
            item.setResource(resource);
            item.setCategory(category);
            item.setTechInfo(techInfo);
            item.setStatisticsValues(statistics);
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
            String order = mainResult.get(TitanKeyWords.tree_order.toString());
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
        // 处理统计数据
        Map<String, String> statistics = titanItem.getStatisticsValues();
        if (CollectionUtils.isNotEmpty(statistics)) {
            String value = statistics.get("sta_key_value");
            if (StringUtils.isNotEmpty(value)) item.setStatisticsNum(Double.parseDouble(value));
        }

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
        String code = tmpMap.get(ES_SearchField.cg_taxoncode.toString());
        if (code == null) code = "";
        rcm.setTaxoncode(code);
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
                if (!requirements.startsWith("[")) {
                    requirements = "[" + requirements;
                }
                if (!requirements.endsWith("]")) {
                    requirements = requirements + "]";
                }
                List<TechnologyRequirementModel> requirementsList = ObjectUtils.fromJson(requirements, new TypeToken<List<TechnologyRequirementModel>>() {
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
     * 资源数据示例(点)：特殊字段（label,id）
     * {preview=[{"png":"${ref-path}/prepub_content_edu_product/esp/assets/abc.png"}], cr_author=[880508], search_path_string=[k12/$on030000/$on030200/$sb0501012/$e004000/$e004001], keywords=[["title","qatest"]], edu_description=[{"zh_CN":"如何使用学习对象进行描述"}], search_path=[K12/$ON030000/$ON030200/$SB0501012/$E004000/$E004001], description=[lcms_special_description_qa_test], search_coverage_string=[debug/qa//,debug/qa/test/creating,debug/qa//creating,debug/qa/test/], language=[zh_CN], lc_status=[CREATING], custom_properties=[{"key":"test"}], cr_has_right=[true], title=[lcms_qa_test_yqjtest_res_getinfo_with_include_of_all_attribute_ok_test_1471416223.61], cr_right_end_date=[7258089000000], lc_provider=[lcms_special_provider_qa_test], label=assets, cr_description=[版权描述信息], lc_create_time=[1471416133155], primary_category=[assets], search_code_string=[$f050005,$on030000,pt01001,$ra0100], search_coverage=[Debug/qa//CREATING, Debug/qa/TEST/CREATING, Debug/qa/TEST/, Debug/qa//], lc_publisher=[lcms_special_publisher_qa_test], id=356896776, edu_context=[基础教育], lc_provider_mode=[qatest_provider_mode], cr_right_start_date=[946656000000], identifier=[1e80454b-ae80-4dbd-994a-b3d8e55ee6b5], cr_right=[版权信息], lc_last_update=[1471416133155], edu_interactivity=[2], lc_version=[qav0.1], edu_semantic_density=[1], edu_difficulty=[easy], edu_end_user_type=[教师，管理者], tags=[["nd","sdp.esp"]], search_code=[$F050005, $ON030000, $RA0100, PT01001], m_identifier=[1e80454b-ae80-4dbd-994a-b3d8e55ee6b5], edu_interactivity_level=[2], lc_enable=[true], lc_provider_source=[八年级地理第一学期期末考试试卷_201407282056.doc], lc_creator=[lcms_special_creator_qa_test], edu_language=[zh_CN], edu_learning_time=[45], edu_age_range=[7岁以上]}
     * {identifier=[3eaabed0-92a2-4c3c-bced-3a44e8a5f51d], ti_md5=[md5Value], label=tech_info, ti_title=[href], ti_location=[${ref-path}/prepub_content_edu/esp/test/abc.png], ti_size=[1024], ti_format=[image/png], id=356900872, ti_entry=[入口地址], ti_requirements=[[{"identifier":null,"type":"HARDWARE","name":"resolution","minVersion":null,"maxVersion":null,"installation":null,"installationFile":null,"value":"435*237","ResourceModel":null}]]}
     * 关系上的数据示例（边）：特殊字段（tags）
     * {res_type=chapters, identifier=094ee58e-b446-4093-853a-118e046f77fe, enable=true, sort_num=5000.0, target_uuid=b453a4fa-f82d-4a90-9693-36ff1b8341bb, source_uuid=5001185b-07b5-43af-90c1-9872cddbe1ce, relation_type=ASSOCIATE, order_num=3.0, resource_target_type=lessons, id=fu7nde-20s5s-2nmd-1kv4i8, tags=[], label=has_relation}
     * @param str
     * @return
     */
    public static Map<String, String> toMapForRelationQuery(String str) {
        Map<String, String> tmpMap = new HashMap<>();
        if(StringUtils.isNotEmpty(str)) {
            // 分割前，预处理
            str = preProcessData(str);
            // 上面预处理已经把边上的"[]"都去掉了，所以简单的以：
            // 1、包含"], "，说明是点，按点方式分割；
            // 2、不包含"], "，而包含", "，按边方式分割；
            // 3、都不包含，说明只有一个字段，直接分割
            if (str.contains("], ")) {
                str = str.replaceAll("=\\[", "=").replaceAll("=\\[", "=").replaceAll("]],", "],");
                String[] fields = str.split("], ");
                for (String s : fields) {
                    if (s.startsWith("label=") || s.startsWith("id=")) {
                        //点上的label和id特殊处理
                        dealSpecialField(s, tmpMap);
                    } else {
                        splitKeyValue(tmpMap,s);
                        //String[] kv = s.split("=");
                        //if (kv.length == 2) tmpMap.put(kv[0].trim(), kv[1].trim());
                    }
                }
            } else if (str.contains(", ")) {//edge
                String[] fields = str.split(", ");
                for (String s : fields) {
                    splitKeyValue(tmpMap,s);
                    //String[] kv = s.split("=");
                    //if (kv.length == 2) tmpMap.put(kv[0].trim(), kv[1].trim());
                }
            } else {
                splitKeyValue(tmpMap,str);
                //String[] kv = str.split("=");
                //if (kv.length == 2) tmpMap.put(kv[0].trim(), kv[1].trim());
            }
        }
        return tmpMap;
    }

    /**
     * 从第一个等号开始分割
     * @param tmpMap
     * @param keyValue
     */
    private static void splitKeyValue(Map<String, String> tmpMap, String keyValue) {
        int begin = keyValue.indexOf("=");
        tmpMap.put(keyValue.substring(0, begin), keyValue.substring(begin + 1, keyValue.length()));
    }

    /**
     * 预处理
     * @param str
     * @return
     */
    public static String preProcessData(String str) {
        // 去掉数据头部的特殊字符
        if (str.startsWith("==>")) str = str.substring(3, str.length());
        // 去掉最外层的{}
        str = str.substring(1, str.length() - 1);
        // 去掉最后一个字段的右中括号"]"
        if (str.endsWith("]")) str = str.substring(0, str.length() - 1);
        // 边上的字段特殊处理，把边上数据的"[]"都去掉，就可以用", "分割：如上面的边上数据示例
        if (str.contains("label=has_relation")) {
            if (str.contains("tags=")) str = dealSpecialField("tags=", str);
            // keywords好像不会出现在边上，不用处理也没关系
            if (str.contains("keywords=")) str = dealSpecialField("keywords=", str);
        }
        return str;
    }

    /**
     * 处理点上的特殊字段
     * @param field
     * @param tmpMap
     */
    public static void dealSpecialField(String field, Map<String, String> tmpMap) {
        //点上的label和id特殊处理
        int end = field.indexOf(", ");
        if (end > 0) {
            String label = field.substring(0, end);
            splitKeyValue(tmpMap,label);
            //String[] kv1 = label.split("=");
            //if (kv1.length == 2) tmpMap.put(kv1[0].trim(), kv1[1].trim());
            String other = field.substring(end + 1, field.length()).trim();
            if (other.startsWith("label=") || other.startsWith("id=")) {
                dealSpecialField(other, tmpMap);
            } else {
                splitKeyValue(tmpMap,other);
                //String[] kv2 = other.split("=");
                //if (kv2.length == 2) tmpMap.put(kv2[0].trim(), kv2[1].trim());
            }


        } else {
            splitKeyValue(tmpMap,field);
            //String[] kv = field.split("=");
            //if (kv.length == 2) tmpMap.put(kv[0].trim(), kv[1].trim());
        }
    }

    /**
     * 处理边上的特殊字段
     * @param field
     * @param line
     * @return
     */
    public static String dealSpecialField(String field, String line) {
        if (line.contains(field)) {
            String[] tmp = line.split(field);
            int end = tmp[1].indexOf("]");
            if (end > 0) {
                String value = field + tmp[1].substring(0, end + 1);
                String replace = value.replaceAll("=\\[", "=").replaceAll("]", "").trim();
                line = line.replace(value, replace);
            }
        }
        return line;
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
    
	/**
	 * 
	 * @param args
	 */
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
		Map<String, String> resultResourceMap = toMapForRelationQuery(resource);
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
        Map<String, String> resultResourceMap = toMapForRelationQuery(resource);
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
        Map<String, String> resultResourceMap = toMapForRelationQuery(resource);

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
        Map<String, String> resultResourceMap = toMapForRelationQuery(resource);

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
        Map<String, String> resultResourceMap = toMapForRelationQuery(resource);

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
        Map<String, String> resultResourceMap = toMapForRelationQuery(resource);
        List<Map<String, String>> resultStrMap=new ArrayList<>();
        resultStrMap.add(resultResourceMap);
        System.out.println(getIndexByLabel("assets",resultStrMap));
    }
    private static void testGetIndexByLabel2(){
        List<String> data = testData();
        // 数据转成key-value
        List<Map<String, String>> resultStrMap = changeStrToKeyValue(data);
        // 切割资源
        //List<List<Map<String, String>>> allItemMaps = cutOneItemMaps("assets", resultStrMap);
        /*// 解析资源
        for (List<Map<String, String>> oneItemMaps : allItemMaps) {
            parseResource("assets", oneItemMaps, IncludesConstant.getIncludesList(), false);
        }*/
        System.out.println(getIndexByLabel("assets",resultStrMap));
    }

    private static void testGetIndexByLabel3(){
        List<String> data = testData();
        // 数据转成key-value
        List<Map<String, String>> resultStrMap = changeStrToKeyValue(data);
        // 切割资源
        List<List<Map<String, String>>> allItemMaps = cutOneItemMaps("assets", resultStrMap);
        int count = 0;
        for(List<Map<String, String>> list:allItemMaps){
            count = count +list.size();
            System.out.println("map size:"+list.size()+"     first map is resource:" +list.get(0).get("label").equals("assets"));
        }
        System.out.println("afterCut:"+count+" input:"+data.size()+" isCountRight:" + (count == data.size()));

    }

    private static void testParse() {
        List<String> data = testData();
        parseToItemsResourceModel("assets",data,IncludesConstant.getIncludesList(),false);
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
