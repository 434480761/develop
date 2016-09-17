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
import nd.esp.service.lifecycle.vos.educationrelation.v06.RelationForQueryViewModel;
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
public class TitanResultParse2 {

    private static final Logger LOG = LoggerFactory.getLogger(TitanResultParse2.class);


    /**
     * 解析资源
     *
     * @param resType
     * @param resultStr
     * @return
     */
    public static ListViewModel<RelationForQueryViewModel> parseToListViewRelationForQueryViewModel(String resType, List<String> resultStr,boolean reverse) {
        ListViewModel<RelationForQueryViewModel> viewModels = new ListViewModel<>();
        List<RelationForQueryViewModel> items = null;
        if (CollectionUtils.isNotEmpty(resultStr)) {
            int resultSize = resultStr.size();
            // 处理count
            String countStr = resultStr.get(resultSize - 1);
            if (StringUtils.isNotEmpty(countStr) && countStr.contains(TitanKeyWords.TOTALCOUNT.toString())) {
                viewModels.setTotal(Long.parseLong(countStr.split("=")[1].trim()));
                resultStr.remove(resultSize - 1);
            }
            // 解析items
            items = parseToItemsRelationForQueryViewModel(resType, resultStr,reverse);
        }
        viewModels.setItems(items);
        return viewModels;
    }


    /**
     * @param resType
     * @param resultStr
     * @return
     */
    public static List<RelationForQueryViewModel> parseToItemsRelationForQueryViewModel(String resType, List<String> resultStr,boolean reverse) {
        long start = System.currentTimeMillis();
        List<RelationForQueryViewModel> items = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(resultStr)) {
            // 数据转成key-value
            List<Map<String, String>> resultStrMap = TitanResultParse.changeStrToKeyValue(resultStr);
            List<TitanResultItem> titanResultItems = discernData(resType, resultStrMap);
            // 解析资源
            if (CollectionUtils.isNotEmpty(titanResultItems)) {
                for (TitanResultItem titanResultItem : titanResultItems) {
                    items.add(generateResourceModel(titanResultItem,reverse));
                }
            }
        }
        LOG.info("parse consume times:" + (System.currentTimeMillis() - start));
        return items;
    }


    private static RelationForQueryViewModel generateResourceModel(TitanResultItem titanItem,boolean reverse) {
        RelationForQueryViewModel item = new RelationForQueryViewModel();
        Map<String, String> resource = titanItem.getResource();
        Map<String, String> relationValues = titanItem.getRelationValues();// 边上的关系数据
        if (CollectionUtils.isNotEmpty(resource) && CollectionUtils.isNotEmpty(relationValues)) {
            item.setRelationId(relationValues.get(ES_SearchField.identifier.toString()));
            item.setRelationType(relationValues.get("relation_type"));
            item.setLabel(relationValues.get("rr_label"));
            String orderStr = relationValues.get("order_num");
            if (StringUtils.isNotEmpty(orderStr)) {
                int order = (int) Float.parseFloat(orderStr);
                item.setOrderNum(order);
            }
            item.setEnable("true".equals(relationValues.get("enable")));
            String relationTags = relationValues.get(ES_SearchField.tags.toString());
            if (StringUtils.isNotEmpty(relationTags)) {
                item.setRelationTags(Arrays.asList(relationTags.replaceAll("\"", "").split(",")));
            }else if (relationTags != null) {
                item.setTags(new ArrayList<String>());
            }
            item.setIdentifier(resource.get(ES_SearchField.identifier.toString()));
            item.setTitle(resource.get(ES_SearchField.title.toString()));
            item.setDescription(resource.get(ES_SearchField.description.toString()));
            String preview = resource.get(ES_SearchField.preview.toString());
            if (preview != null && preview.startsWith("{\"") && preview.endsWith("\"}")) {
                @SuppressWarnings("unchecked")
                Map<String, String> previewMap = ObjectUtils.fromJson(preview, Map.class);
                if (previewMap == null) {
                    previewMap = new HashMap<>();
                }
                item.setPreview(previewMap);

            } else {
                item.setPreview(new HashMap<String, String>());
            }
            String tags = resource.get(ES_SearchField.tags.toString());
            if (StringUtils.isNotEmpty(tags)) {
                item.setTags(Arrays.asList(tags.replaceAll("\"", "").split(",")));
            } else if (tags != null) {
                item.setTags(new ArrayList<String>());
            }
            String keywords = resource.get(ES_SearchField.keywords.toString());
            if (StringUtils.isNotEmpty(keywords)) {
                item.setKeywords(Arrays.asList(keywords.replaceAll("\"", "").split(",")));
            } else if (keywords != null) {
                item.setKeywords(new ArrayList<String>());
            }
            // 反向查询时，返回数据sid错误，应为url中的uuid
            if (reverse) {
                item.setSid(relationValues.get("target_uuid"));
            } else {
                item.setSid(relationValues.get("source_uuid"));
            }
            item.setTargetType(resource.get("label"));
            item.setCreator(resource.get(ES_SearchField.lc_creator.toString()));
            item.setStatus(resource.get(ES_SearchField.lc_status.toString()));
            item.setCreateTime(StringUtils.strTimeStampToDate(resource.get(ES_SearchField.lc_create_time.toString())));
            item.setLastUpdate(StringUtils.strTimeStampToDate(resource.get(ES_SearchField.lc_last_update.toString())));
        }
        return item;
    }

    /**
     *
     * @param resType
     * @param allItemMaps
     * @return
     */
    public static List<TitanResultItem> discernData(String resType, List<Map<String, String>> allItemMaps) {
        List<TitanResultItem> items = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(allItemMaps)) {
            int size = allItemMaps.size();
            for (int i = 0; i < size; i = i + 2) {
                Map<String, String> resource = allItemMaps.get(i);
                Map<String, String> relationValues = i + 1 < size ? allItemMaps.get(i + 1) : null;
                if (CollectionUtils.isNotEmpty(resource) && CollectionUtils.isNotEmpty(relationValues)) {
                    if (resType.equals(resource.get("label")) && "has_relation".equals(relationValues.get("label"))) {
                        TitanResultItem item = new TitanResultItem();
                        item.setResource(resource);
                        item.setRelationValues(relationValues);
                        items.add(item);
                    }

                }
            }
        }
        return items;
    }


}
