package nd.esp.service.lifecycle.services.updatamediatype.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import nd.esp.service.lifecycle.controllers.AdapterDBDataController;
import nd.esp.service.lifecycle.services.updatamediatype.UpdateMediatypeService;
import nd.esp.service.lifecycle.services.updatamediatype.dao.UpdateMediatypeDao;
import nd.esp.service.lifecycle.services.updatamediatype.model.UpdateMediatypeModel;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.annotation.TitanTransaction;
import nd.esp.service.lifecycle.utils.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.EspRepository;
import nd.esp.service.lifecycle.repository.ds.ComparsionOperator;
import nd.esp.service.lifecycle.repository.ds.Item;
import nd.esp.service.lifecycle.repository.ds.LogicalOperator;
import nd.esp.service.lifecycle.repository.ds.ValueUtils;
import nd.esp.service.lifecycle.repository.ds.jpa.Criteria;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.CategoryData;
import nd.esp.service.lifecycle.repository.model.ResourceCategory;
import nd.esp.service.lifecycle.repository.model.TechInfo;
import nd.esp.service.lifecycle.repository.sdk.CategoryDataRepository;
import nd.esp.service.lifecycle.repository.sdk.ResourceCategoryRepository;
import nd.esp.service.lifecycle.repository.sdk.TechInfoRepository;
import nd.esp.service.lifecycle.repository.sdk.impl.ServicesManager;
import org.springframework.transaction.annotation.Transactional;

@Service("UpdataMediatypeServiceImpl")
public class UpdateMediatypeServiceImpl implements UpdateMediatypeService {

    @Autowired
    private ResourceCategoryRepository resourceCategoryRepository;

    @Autowired
    private CategoryDataRepository categoryDataRepository;

    @Autowired
    private TechInfoRepository techInfoRepository;

    @Autowired
    private UpdateMediatypeDao dao;

    private Map<String, CategoryData> shortNameMap = new HashMap<String, CategoryData>();

    private UpdateMediatypeModel model;

    private Map<String, List<TechInfo>> techInfoMap = null;

    private Map<String, List<ResourceCategory>> categoryMap = null;

    private static final Logger LOG = LoggerFactory.getLogger(AdapterDBDataController.class);

    @Transactional
    @TitanTransaction
    public UpdateMediatypeModel tupdate(String type, boolean save) {
        // 构造维度数据
        createNdcodeMap();

        // createTechInfoMap();

        try {
            execute(type, save);
        } catch (EspStoreException e) {
            LOG.info("更新媒体资源类型数据失败");

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(), e.getLocalizedMessage());
        }

        return model;
    }

    @SuppressWarnings("unchecked")
    private void execute(String type, boolean save) throws EspStoreException {
        // service返回的数据
        model = new UpdateMediatypeModel();
        // 分页
        List entitylist = null;
        int updatecount = 0;
        int page = 0;
        int row = 500;
        EspRepository<?> espRepository = ServicesManager.get(type);
        Page resourcePage = null;

        Item<Integer> item = new Item<Integer>();
        item.setKey("enable");
        item.setComparsionOperator(ComparsionOperator.EQ);
        item.setLogicalOperator(LogicalOperator.AND);
        item.setValue(ValueUtils.newValue(1));

        List<Item<? extends Object>> items = new ArrayList<>();
        items.add(item);

        do {
            Pageable pageable = new PageRequest(page, row);

            // 分页查询
            resourcePage = espRepository.findByItems(items, pageable);

            entitylist = resourcePage.getContent();

            categoryMap = getCategoryFromDB(entitylist);

            techInfoMap = getTechInfoFromDB(entitylist, type);

            Map<String, List<? extends Object>> reData = check(entitylist);
            
            
            // 添加媒体资源类型
            if (save) {
                resourceCategoryRepository.batchAdd((List<ResourceCategory>) reData.get("categories"));
                
                espRepository.batchAdd((List)reData.get("content"));
            }

            for (ResourceCategory rc : (List<ResourceCategory>) reData.get("categories")) {
                model.getIds().add(rc.getResource() + "--" + rc.getShortName());
            }

            updatecount = updatecount + reData.get("categories").size();
            LOG.info("第" + (page + 1) + "次分页查询");

        } while (++page < resourcePage.getTotalPages());

        model.setSaveScuccess(updatecount);

        return;
    }

    private Map<String, List<? extends Object>> check(List entitylist) {

        List<ResourceCategory> returnCategory = new ArrayList<ResourceCategory>();
        List<Education> returnNdResourc = new ArrayList<Education>() ;

        if (entitylist != null && !entitylist.isEmpty()) {
            for (Object obj : entitylist) {

                Education entity = (Education) obj;

                String id = entity.getIdentifier();
                
                boolean entityChanged = false ;

                // 获取ID对应的tech_info
                List<TechInfo> techInforList = techInfoMap.get(id);

                // 获取ID对应的categories
                List<ResourceCategory> categoryList = categoryMap.get(id);

                Set<String> formatSet = new HashSet<String>();

                Set<String> formatSave = new HashSet<String>();

                // 保证tech_info不能为null
                if (techInforList == null || techInforList.isEmpty()) {
                    continue;
                }

                // 添加媒体资源类型，获取formatset集合
                for (TechInfo techInfo : techInforList) {
                    // 判断tech_info中的format与categories中是否有对应的数据。
                    // 如果没有以other名字保存，这样保证同一个资源中只有一个other
                    CategoryData categoryData = shortNameMap.get(techInfo.getFormat());
                    if (!StringUtils.isEmpty(techInfo.getFormat()) && categoryData == null) {
                        techInfo.setFormat("other");
                    }
                    // 添加维度的条件：format不能为空，添加到set集合中，可以消去重复的format保证categories中维度编码唯一
                    if (!StringUtils.isEmpty(techInfo.getFormat())) {
                        formatSet.add(techInfo.getFormat());
                    }
                }

                if (formatSet.isEmpty()) {
                    continue;
                } else {
                    // 循环遍历formatSet集合，判断Resource_category中分已经存在相应的维度数据
                    for (String format : formatSet) {
                        boolean exist = false;
                        
                        for (ResourceCategory rc : categoryList) {
                            if (format != null && format != "") {
                                CategoryData rcd = shortNameMap.get(format) ;
                                //不保存相同的NdCode
                                if(rc.getTaxoncode().equals(rcd.getNdCode())){
                                    exist = true;
                                }
                            }
                        }

                        if (!exist) {
                            formatSave.add(format);
                        }

                    }
                }

                for (String format : formatSave) {
                    if (format != null && format != "") {
                        CategoryData categoryData = shortNameMap.get(format);

                        if (categoryData == null) {
                            categoryData = shortNameMap.get("other");
                            for (ResourceCategory rc : categoryList) {
                                if (categoryData != null && rc.getTaxoncode().equals(categoryData.getNdCode())) {
                                    categoryData = null;
                                }
                            }

                            if (categoryData == null) {
                                continue;
                            }
                        }

                        ResourceCategory category = new ResourceCategory();

                        category.setIdentifier(UUID.randomUUID().toString());
                        category.setResource(id);
                        category.setTaxonname(categoryData.getTitle());
                        category.setTaxoncode(categoryData.getNdCode());

                        category.setCategoryCode("$F");
                        category.setCategoryName("mediatype");
                        category.setShortName(format);
                        category.setTaxoncodeid(categoryData.getIdentifier());

                        // 修改主表中的数据
//                        if (entity.getCategories() == null) {
//                            List<String> list = new ArrayList<String>();
//                            list.add(category.getTaxoncode());
//                            entity.setCategories(list);
//                            
//                            entityChanged = true ;
//                        }
//                        else {
//                            if (!entity.getCategories().contains(category.getTaxoncode())) {
//                                entity.getCategories().add(category.getTaxoncode());
//                                
//                                entityChanged = true ;
//                            }
//                        }
                        returnCategory.add(category);
                    }
                }
                if(entityChanged){
                    returnNdResourc.add(entity) ;
                }
            }
        }
        
        Map<String, List<? extends Object>> returnMap = new HashMap<String, List<? extends Object>>() ;
        returnMap.put("categories", returnCategory) ;
        returnMap.put("content", returnNdResourc) ;
        
        return returnMap;
    }

    /**
     * 根据资源id获取ResourceCategory
     * */
    private Map<String, List<ResourceCategory>> getCategoryFromDB(List entitylist) {
        Map<String, List<ResourceCategory>> returnMap = new HashMap<String, List<ResourceCategory>>();

        List<ResourceCategory> exampleList = new ArrayList<ResourceCategory>();
        for (Object obj : entitylist) {
            Education entity = (Education) obj;
            ResourceCategory rc = new ResourceCategory();
            rc.setCategoryName("mediatype");
            rc.setResource(entity.getIdentifier());
            exampleList.add(rc);
        }

        try {
            for (ResourceCategory rc : exampleList) {

                List<ResourceCategory> nrc = resourceCategoryRepository.getAllByExample(rc);

                returnMap.put(rc.getResource(), nrc);
            }
        } catch (EspStoreException e) {
            LOG.info("利用Resource ID查询ResourceCategory失败");

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(), e.getLocalizedMessage());
        }

        return returnMap;
    }

    /**
     * 更具资源id获取TechInfo
     * */
    private Map<String, List<TechInfo>> getTechInfoFromDB(List entitylist, String resType) {
        Map<String, List<TechInfo>> returnMap = new HashMap<String, List<TechInfo>>();
        for (Object obj : entitylist) {
            Education entity = (Education) obj;
            TechInfo example = new TechInfo();
            example.setResource(entity.getIdentifier());

            try {
                List<TechInfo> list = techInfoRepository.getAllByExample(example);

                returnMap.put(entity.getIdentifier(), list);
            } catch (EspStoreException e) {
                LOG.info("利用Resource ID查询TechInfo失败");

                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                        LifeCircleErrorMessageMapper.StoreSdkFail.getCode(), e.getLocalizedMessage());
            }
        }

        return returnMap;
    }

    /**
     * 创建NdCode映射表
     * */
    private void createNdcodeMap() {
        List<CategoryData> categorydatas = null;
        try {
            categorydatas = categoryDataRepository.findAllByCriteria(new Criteria<CategoryData>());
        } catch (EspStoreException e) {
            e.printStackTrace();
        }
        if (categorydatas != null && !categorydatas.isEmpty()) {
            for (CategoryData category : categorydatas) {
                String type = category.getNdCode().substring(0, 2);
                if ("$F".equals(type)) {
                    shortNameMap.put(category.getShortName(), category);
                }
            }
        }
    }

    public static void main(String[] args) {
        List<TechInfo> techInfoList = new LinkedList<TechInfo>();

        for (int i = 0; i < 10000000; i++) {
            techInfoList.add(new TechInfo());
        }

        System.out.println(techInfoList.size());
    }

}
