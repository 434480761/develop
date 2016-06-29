package nd.esp.service.lifecycle.services.titan;

import com.google.gson.reflect.TypeToken;
import nd.esp.service.lifecycle.educommon.models.*;
import nd.esp.service.lifecycle.support.enums.ES_SearchField;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        item.setCustomProperties(fieldMap.get(ES_SearchField.custom_properties.toString()));
        String preview = fieldMap.get(ES_SearchField.preview.toString());
        if (preview != null) {
            @SuppressWarnings("unchecked")
            Map<String, String> previewMap = ObjectUtils.fromJson(preview, Map.class);
            item.setPreview(previewMap);
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
        edu.setLearningTime(tmpMap.get(ES_SearchField.edu_language.toString()));

        String description = tmpMap.get(ES_SearchField.edu_description.toString());
        if (description != null) {
            if (!"".equals(description.trim()) && !"null".equals(description.trim())) {
                @SuppressWarnings("unchecked")
                Map<String, String> map = ObjectUtils.fromJson(description, Map.class);
                edu.setDescription(map);
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
        lifeCycle.setEnable(tmpMap.get(ES_SearchField.lc_enable.toString()).equals("true"));
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
            String[] t = s.replaceAll("]", "").split("=");
            if (t.length == 2) {
                tmpMap.put(t[0].trim(), t[1].trim());
            }
        }
        return tmpMap;
    }

}
