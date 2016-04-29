/* =============================================================
 * Created: [2015年7月21日] by linsm
 * =============================================================
 *
 * Copyright 2014-2015 NetDragon Websoft Inc. All Rights Reserved
 *
 * =============================================================
 */

package nd.esp.service.lifecycle.services.impls;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.Category;
import nd.esp.service.lifecycle.repository.model.CategoryData;
import nd.esp.service.lifecycle.repository.model.CategoryPattern;
import nd.esp.service.lifecycle.repository.model.CategoryRelation;
import nd.esp.service.lifecycle.repository.sdk.CategoryDataRepository;
import nd.esp.service.lifecycle.repository.sdk.CategoryPatternRepository;
import nd.esp.service.lifecycle.repository.sdk.CategoryRelationRepository;
import nd.esp.service.lifecycle.repository.sdk.CategoryRepository;
import nd.esp.service.lifecycle.services.ImportDataService;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * @author linsm
 * @since
 */
@Service
public class ImportDataServiceImpl implements ImportDataService {
    @Autowired
    private CategoryDataRepository categoryDataRepository;
    
    @Autowired
    private CategoryRelationRepository categoryRelationRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private CategoryPatternRepository categoryPatternRepository;

    private static final Logger LOG = LoggerFactory.getLogger(ImportDataServiceImpl.class);
    
    public static final String TOP_LEVEL_PARENT = "ROOT"; // 若无根结点，则采用这个值

    /**
     * 导入维度数据，(第几批次，主要处理了依赖问题)合法的分类体系数据（使用数据库）: 成功创建的维度数据:
     * 
     * @return
     * @since
     */
    @Override
    public Map<String, Long> importCategoryData(List<CategoryData> categoryDatas) {
        Map<String, Long> importMessageMap = new LinkedHashMap<String, Long>();

        int batchAddNum = 0;
        while (CollectionUtils.isNotEmpty(categoryDatas)) {
            batchAddNum++;
            Set<String> toBeCreatedNdCodeSet = new HashSet<String>(); // 用于保存可能创建的数据(ndCode)
            for (CategoryData data : categoryDatas) {
                toBeCreatedNdCodeSet.add(data.getNdCode());
            }
            List<CategoryData> secondBatchDatas = new ArrayList<CategoryData>();  //存在依赖
            List<CategoryData> firstBatchDatas = new ArrayList<CategoryData>();  //当前用于插入的维度数据（没有依赖）

            Set<String> categoryNdCodeSet = new HashSet<String>();  //(分类维度)调用数据库查找信息
            Set<String> categoryDataNdCodeSet = new HashSet<String>();//(维度数据)调用数据库查找信息

            for (CategoryData data : categoryDatas) {
                if (toBeCreatedNdCodeSet.contains(data.getParent())) {
                    // 依赖于其它维度数据的创建，暂时不处理
                    secondBatchDatas.add(data);

                } else {
                    firstBatchDatas.add(data);

                    categoryNdCodeSet.add(data.getCategory());
                    categoryDataNdCodeSet.add(data.getNdCode());
                    // 非根，需要校验
                    if (!TOP_LEVEL_PARENT.equals(data.getParent())) {
                        categoryDataNdCodeSet.add(data.getParent());
                    }
                }

            }

            Map<String, String> categoryNdCodeToUuid = mapCategoryNdCodeToUuid(categoryNdCodeSet);
            Map<String, CategoryData> categoryDataNdCodeToData = mapCategoryDataNdCodeToData(categoryDataNdCodeSet);

            List<CategoryData> categoryDataDealResult = new ArrayList<CategoryData>();
            Set<String> errorNdCodeByDBSet = new HashSet<String>();// 失败，不再处理的ndCode
            for (CategoryData data : firstBatchDatas) {
                String cId = categoryNdCodeToUuid.get(data.getCategory());
                CategoryData parentData = null;

                if (TOP_LEVEL_PARENT.equals(data.getParent())) {
                    parentData = new CategoryData();
                    parentData.setIdentifier(data.getParent());
                    parentData.setDimensionPath(TOP_LEVEL_PARENT);
                } else {
                    parentData = categoryDataNdCodeToData.get(data.getParent());
                }

                CategoryData currentData = categoryDataNdCodeToData.get(data.getNdCode());
                // 参数合法
                if (cId != null && parentData != null && currentData == null) {
                    data.setCategory(cId);
                    data.setParent(parentData.getIdentifier());
                    data.setIdentifier(UUID.randomUUID().toString());

                    data.setDimensionPath(parentData.getDimensionPath() + "/" + data.getNdCode());
                    data.setDescription(data.getTitle()); // FIXME 暂时使用title
                    data.setGbCode(data.getNdCode()); // FIXME 暂时使用ndCode

                    categoryDataDealResult.add(data);
                    
                    LOG.debug(batchAddNum + " 逻辑合法（数据库）：ndCode=" + data.getNdCode());
                    
                } else {

                    LOG.debug(batchAddNum + " 该行出错：ndCode=" + data.getNdCode());
                    
                    if (currentData == null) {
                        errorNdCodeByDBSet.add(data.getNdCode()); // 且还未创建成功
                    }

                }

            }
//            for (CategoryData data : secondBatchDatas) {
//                if (errorNdCodeByDBSet.contains(data.getParent())) {
//                   
//                    LOG.debug(batchAddNum + " 该行出错(依赖)：ndCode=" + data.getNdCode());
//                    
//                    secondBatchDatas.remove(data);
//                }
//            }
            Iterator<CategoryData> iterator = secondBatchDatas.iterator();
            while (iterator.hasNext()) {
                CategoryData data = iterator.next();
                if (errorNdCodeByDBSet.contains(data.getParent())) {
                  
                   LOG.debug(batchAddNum + " 该行出错(依赖)：ndCode=" + data.getNdCode());
                   
                   iterator.remove();
               }
                
            }

            importMessageMap.put(batchAddNum + " 合法的分类体系数据（使用数据库）", (long) categoryDataDealResult.size());
            
            LOG.info(batchAddNum + " 合法的分类体系数据（使用数据库）"+ categoryDataDealResult.size());
            
            if (CollectionUtils.isNotEmpty(categoryDataDealResult)) {
                try {
                    categoryDataDealResult = categoryDataRepository.batchAdd(categoryDataDealResult);
                } catch (EspStoreException e) {
                    
                    LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(),e);
                    
                    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                  LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                                  e.getMessage());
                }
            }
            importMessageMap.put(batchAddNum + " 成功创建的维度数据:", (long) categoryDataDealResult.size());
            
            LOG.info(batchAddNum + " 成功创建的维度数据:"+categoryDataDealResult.size());
            
            categoryDatas = secondBatchDatas;

        }

        return importMessageMap;
    }

    /**
     * 批量通过categoryDataNdCode得到维度数据
     * @param categoryDataNdCodeSet
     * @return
     * @since
     */
    private Map<String, CategoryData> mapCategoryDataNdCodeToData(Set<String> categoryDataNdCodeSet) {
        Map<String, CategoryData> modelMap = new HashMap<String, CategoryData>();
        List<CategoryData> beanListResult = null;
        try {
            beanListResult = categoryDataRepository.getListWhereInCondition("ndCode",
                                                                            new ArrayList<String>(categoryDataNdCodeSet));
        } catch (EspStoreException e) {

            LOG.error("批量通过categoryDataNdCode得到维度数据" + LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(), e);

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                          "批量通过categoryDataNdCode得到维度数据" + e.getMessage());
        }
        if (beanListResult != null && !beanListResult.isEmpty()) {
            for (CategoryData beanResult : beanListResult) {
                if (beanResult != null) {
                    modelMap.put(beanResult.getNdCode(), beanResult);
                }
            }
        }
        return modelMap;
    }

    /**
     * 批量转换categoryNdCode 到 uuid
     * @param categoryNdCodeSet
     * @return
     * @since
     */
    private Map<String, String> mapCategoryNdCodeToUuid(Set<String> categoryNdCodeSet) {
        Map<String, String> modelMap = new HashMap<String, String>();
        List<Category> beanListResult = null;
        try {
            beanListResult = categoryRepository.getListWhereInCondition("ndCode",
                                                                        new ArrayList<String>(categoryNdCodeSet));
        } catch (EspStoreException e) {
            
            LOG.error("批量转换categoryNdCode 到 uuid"+LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(),e);
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                          "批量转换categoryNdCode 到 uuid" + e.getMessage());
        }
        if (beanListResult != null && !beanListResult.isEmpty()) {
            for (Category beanResult : beanListResult) {
                if (beanResult != null) {
                    modelMap.put(beanResult.getNdCode(), beanResult.getIdentifier());
                }
            }
        }
        return modelMap;
    }

    /*
     * 更新某个模式的关系顺序（保持原来的顺序，但是在同一个路径下，保证不存在相同orderNum值)
     * (non-Javadoc)
     * @see nd.esp.service.lifecycle.services.ImportDataService#updateCategoryRelationOrderNum(java.lang.String)
     */
    @Override
    public Map<String, Long> updateCategoryRelationOrderNum(String patternName) {
        
        CategoryPattern pattern = new CategoryPattern();
        pattern.setPatternName(patternName);
        try {
            pattern = categoryPatternRepository.getByExample(pattern);
        } catch (EspStoreException e) {
            
            LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(),e);
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getMessage() + e);
        }
        if (pattern == null || !patternName.equals(pattern.getPatternName())) {
            
            LOG.error(LifeCircleErrorMessageMapper.CategoryPatternNotFound.getMessage());
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.CategoryPatternNotFound);
        }
        
        //辅助用于排查可能发生的错误
        CategoryRelation testCondition = new CategoryRelation();
        testCondition.setPattern(pattern.getIdentifier());
        List<CategoryRelation> testRelations = null;
        try {
            testRelations = categoryRelationRepository.getAllByExample(testCondition);
            
            LOG.info("pattern id: total num   " + testRelations.size());
            
        } catch (EspStoreException e) {
            
            LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(),e);
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getMessage() + e);
        }

        Map<String, Long> messages = new LinkedHashMap<String, Long>();
        // 校验patternName
        // 用于保存最终用于更新的关系
        List<CategoryRelation> toBeUpdatedCategoryRelations = new ArrayList<CategoryRelation>();

        // 初始化路径
        List<String> patternPaths = new ArrayList<String>();
        patternPaths.add(patternName);

        Comparator<CategoryRelation> orderNumComparator = new Comparator<CategoryRelation>() {

            @Override
            public int compare(CategoryRelation o1, CategoryRelation o2) {
                return Float.compare(o1.getOrderNum(), o2.getOrderNum());
            }

        };

        while (CollectionUtils.isNotEmpty(patternPaths)) {
            List<String> categoryDataIds = new ArrayList<String>();
            List<CategoryData> categoryDatas = null;
            Map<String, List<String>> patternPathToTargetIDsMap = new HashMap<String, List<String>>();
            for (String patternPath : patternPaths) {
                List<String> categoryDataIdsAccordingPatternPath = new ArrayList<String>();
                CategoryRelation categoryRelation = new CategoryRelation();
                categoryRelation.setPatternPath(patternPath);

                List<CategoryRelation> relations = null;
                try {
                    relations = categoryRelationRepository.getAllByExample(categoryRelation);
                } catch (EspStoreException e) {
                    
                    LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(),e);
                    
                    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                  LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                                  LifeCircleErrorMessageMapper.StoreSdkFail.getMessage() + e);
                }

                // 处理关系：
                if (CollectionUtils.isNotEmpty(relations)) {
                    // 排序
                    Collections.sort(relations, orderNumComparator);
                    // 更新orderNum
                    // 添加到categoryDataIds
                    for (int i = 0; i < relations.size(); i++) {
                        CategoryRelation relation = relations.get(i);
                        if (relation != null) {
                            relation.setOrderNum((float) (i+1));
                            categoryDataIdsAccordingPatternPath.add(relation.getTarget());
                        }
                    }
                    categoryDataIds.addAll(categoryDataIdsAccordingPatternPath);
                    toBeUpdatedCategoryRelations.addAll(relations);
                    if (patternPathToTargetIDsMap.containsKey(patternPath)) {
                        patternPathToTargetIDsMap.get(patternPath).addAll(categoryDataIdsAccordingPatternPath);

                    } else {
                        patternPathToTargetIDsMap.put(patternPath, categoryDataIdsAccordingPatternPath);
                    }
                    
                }
            }
            patternPaths.clear(); // 清空路径（已经使用完毕）

            // 批量取维度数据（根据id）
            try {
                categoryDatas = categoryDataRepository.getAll(categoryDataIds);
                
            } catch (EspStoreException e) {
                
                LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(),e);
                
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                              LifeCircleErrorMessageMapper.StoreSdkFail.getMessage() + e);
            }

            // 处理成idToCodeMap
            Map<String, String> dataIdToNdCodeMap = new HashMap<String, String>();
            if (CollectionUtils.isNotEmpty(categoryDatas)) {
                for (CategoryData data : categoryDatas) {
                    if (data != null) {
                        dataIdToNdCodeMap.put(data.getIdentifier(), data.getNdCode());
                    }
                }
            }

            // 生成patternPaths
            for (String prefix : patternPathToTargetIDsMap.keySet()) {
                List<String> targetIds = patternPathToTargetIDsMap.get(prefix);
                if (CollectionUtils.isNotEmpty(targetIds)) {
                    for (String id : targetIds) {
                        String ndCode = dataIdToNdCodeMap.get(id);
                        if (StringUtils.isNotEmpty(ndCode)) {
                            String patternPath = prefix + "/" + ndCode;
                            patternPaths.add(patternPath);
                        }
                    }
                }
            }
            
            LOG.info("需要更新的关系数量："+ toBeUpdatedCategoryRelations.size());
            
        }
        

        // 共有需要更新的关系：
        messages.put("共有需要更新的关系:", (long) toBeUpdatedCategoryRelations.size());
        if (CollectionUtils.isNotEmpty(toBeUpdatedCategoryRelations)) {
            // 更新关系
            List<CategoryRelation> resultRelations = null;
            try {
                resultRelations = categoryRelationRepository.batchAdd(toBeUpdatedCategoryRelations);
            } catch (EspStoreException e) {
                
                LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(),e);
                
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                              LifeCircleErrorMessageMapper.StoreSdkFail.getMessage() + e);
            }

            // 成功更新关系：
            if (CollectionUtils.isNotEmpty(resultRelations)) {
                messages.put("成功更新关系", (long) resultRelations.size());
                
                LOG.info("成功更新关系"+ resultRelations.size());
                
            }
        }

        //对比更新的关系与存在的所有关系：(用于排查更新的结果)
        for(CategoryRelation relation : testRelations){
            int index = 0 ;
            while(index<toBeUpdatedCategoryRelations.size()){
                if(relation.getIdentifier().equals(toBeUpdatedCategoryRelations.get(index).getIdentifier())){
                    break;
                }
                index++;
            }
            if(index>=toBeUpdatedCategoryRelations.size()){
                
                LOG.warn("没有更新到关系的id: "+ relation.getIdentifier());
                
            }
        }
        return messages;
    }


}
