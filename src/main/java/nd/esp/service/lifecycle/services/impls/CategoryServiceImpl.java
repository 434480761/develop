package nd.esp.service.lifecycle.services.impls;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import nd.esp.service.lifecycle.models.CategoryDataModel;
import nd.esp.service.lifecycle.models.CategoryModel;
import nd.esp.service.lifecycle.models.CategoryPatternModel;
import nd.esp.service.lifecycle.models.CategoryRelationModel;
import nd.esp.service.lifecycle.repository.ds.ComparsionOperator;
import nd.esp.service.lifecycle.repository.ds.Item;
import nd.esp.service.lifecycle.repository.ds.LogicalOperator;
import nd.esp.service.lifecycle.repository.ds.ValueUtils;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.index.AdaptQueryRequest;
import nd.esp.service.lifecycle.repository.index.QueryRequest;
import nd.esp.service.lifecycle.repository.index.QueryResponse;
import nd.esp.service.lifecycle.repository.model.Category;
import nd.esp.service.lifecycle.repository.model.CategoryData;
import nd.esp.service.lifecycle.repository.model.CategoryPattern;
import nd.esp.service.lifecycle.repository.model.CategoryRelation;
import nd.esp.service.lifecycle.repository.sdk.CategoryDataRepository;
import nd.esp.service.lifecycle.repository.sdk.CategoryPatternRepository;
import nd.esp.service.lifecycle.repository.sdk.CategoryRelationRepository;
import nd.esp.service.lifecycle.repository.sdk.CategoryRepository;
import nd.esp.service.lifecycle.services.CategoryService;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.utils.BeanMapperUtils;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.ParamCheckUtil;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.utils.category.NdCodePattern;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;
import nd.esp.service.lifecycle.vos.ListViewModel;
import nd.esp.service.lifecycle.vos.QueryRelationAllViewModel;
import nd.esp.service.lifecycle.vos.QueryRelationViewModel;
import nd.esp.service.lifecycle.vos.RelationAllViewModel;
import nd.esp.service.lifecycle.vos.RelationViewModel;
import nd.esp.service.lifecycle.vos.TargetViewModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.google.gson.reflect.TypeToken;


/**
 * @author johnny
 * @version 1.0
 * @created 20-4月-2015 15:49:41
 */
@Service("CategoryServiceImpl")
public class CategoryServiceImpl implements CategoryService {
    
	private static final Logger LOG = LoggerFactory.getLogger(CategoryServiceImpl.class);
	private static final String NO_EXISTED_NDCODE = "$$$$$$";

	@Autowired
	CategoryRepository categoryRepository;
	
	@Autowired
	CategoryDataRepository categoryDataRepository;
	
	@Autowired 
	CategoryPatternRepository categoryPatternRepository;
	
	@Autowired
	CategoryRelationRepository categoryRelationRepository;

	/**
	 * 通过ndCode获取分类维度详情
	 * 
	 * <br>Created 2015年5月4日 上午11:02:49
	 * @param ndCode
	 * @return
	 * @throws EspStoreException
	 * @author       linsm
	 */
	@Override
	public CategoryModel loadCategoryByNdCode(String ndCode) throws EspStoreException{
		CategoryModel result = null;
        Item<String> item = new Item<String>();
        item.setKey("ndCode");
        item.setComparsionOperator(ComparsionOperator.EQ);
        item.setLogicalOperator(LogicalOperator.AND);
        item.setValue(ValueUtils.newValue(ndCode));
        List<Item<? extends Object>> items = new ArrayList<>();
        items.add(item);
        // 调用sdk
        
        LOG.debug("调用sdk方法:findOneByItems");
        
        Category beanResult = categoryRepository.findOneByItems(items);
        if (beanResult != null) {
            // 成功返回了数据
            result = BeanMapperUtils.beanMapper(beanResult, CategoryModel.class);
        }
        return result;
	}


	/**
	 * 通过uuid 获取 分类维度详情
	 * 
	 * <br>Created 2015年5月4日 上午11:03:35
	 * @param id
	 * @return
	 * @throws EspStoreException
	 * @author       linsm
	 */
	@Override
	public CategoryModel loadCategoryById(String id)throws EspStoreException {
		CategoryModel result = null;
		// 调用sdk
		
		LOG.debug("调用sdk方法:get");
		
		Category beanResult = categoryRepository.get(id);
        if (beanResult != null) {
            // 成功返回了数据
            result = BeanMapperUtils.beanMapper(beanResult, CategoryModel.class);
        }
        return result;
	}

	/**
	 * 创建分类维度(ndCode 惟一）
	 * 
	 * <br>Created 2015年5月4日 上午11:04:08
	 * @param categoryModel
	 * @return
	 * @throws EspStoreException
	 * @author       linsm
	 */
	@Override
	public CategoryModel creatCategory(CategoryModel categoryModel)
			throws EspStoreException {
		CategoryModel result = null;
		//logic check
		// ndCode, gbCode(选填)
		CategoryModel testModel = null;
		try {
			testModel = loadCategoryByNdCode(categoryModel
					.getNdCode());
		} catch (EspStoreException e) {
			
		    LOG.error(LifeCircleErrorMessageMapper.CategoryCheckingError.getMessage(),e);
			
		    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getMessage());
		}
		if (testModel != null) {
			// 增加分类编码[nd_code：。。]已经存在 ， 抛出异常：
		    
		    LOG.error(LifeCircleErrorMessageMapper.DuplicateNDcodeError.getMessage());
		    
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					LifeCircleErrorMessageMapper.DuplicateNDcodeError);
		}

		// 入参
		Category bean = BeanMapperUtils.beanMapper(categoryModel, Category.class);

		// 调用sdk
		
		LOG.debug("调用sdk方法:add");
		
		Category beanResult = categoryRepository.add(bean);
        if (beanResult != null) {
            
            LOG.debug("创建分类维度资源:{}",beanResult.getIdentifier());
            
            // 成功返回了数据
            result = BeanMapperUtils.beanMapper(beanResult, CategoryModel.class);
        }
        return result;
	}

	/**
	 * 修改分类维度
	 * 
	 * <br>Created 2015年5月4日 上午11:04:26
	 * @param categoryModel
	 * @return
	 * @throws EspStoreException
	 * @author       linsm
	 */
	@Override
	public CategoryModel modifyCategory(CategoryModel categoryModel) throws EspStoreException{
		CategoryModel result = null;
		
		//logic check

		// 通过uuid, 获取数据
		CategoryModel testModel = null;
		try {
			testModel = loadCategoryById(categoryModel.getIdentifier());
		} catch (EspStoreException e) {
		    
			LOG.error(LifeCircleErrorMessageMapper.CategoryCheckingError.getMessage(),e);
			
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
			                              LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getMessage());
		}
		if (testModel == null) {
		    
		    LOG.error(LifeCircleErrorMessageMapper.CategoryNotFound.getMessage());
		    
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					LifeCircleErrorMessageMapper.CategoryNotFound);
		}

		// 对比testModel 与 modifyModel 不能重复：nd_code
		// nd_code
		if (!testModel.getNdCode().equals(categoryModel.getNdCode())) {
			// 发生改变的属性：判断“不重复”
			CategoryModel testAttributeModel = null; // 用于暂存根据属性得到的数据
			try {
				testAttributeModel = loadCategoryByNdCode(categoryModel
						.getNdCode());
			} catch (EspStoreException e) {
				
			    LOG.error(LifeCircleErrorMessageMapper.CategoryCheckingError.getMessage(),e);
				
			    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
			                                  LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getMessage());
			}
			// 非空，且 uuid 不同，说明违反“重复”
			if (testAttributeModel != null
					&& !testAttributeModel.getIdentifier().equals(
							categoryModel.getIdentifier())) {
			    
			    LOG.error(LifeCircleErrorMessageMapper.DuplicateNDcodeError.getMessage());
			    
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
						LifeCircleErrorMessageMapper.DuplicateNDcodeError);
			}
			
            // 已经存在维度数据，不允许修必nd_code
            if (hasCategoryData(testModel.getNdCode())) {
                
                LOG.error("该维度下已经存在维度数据，不允许修改nd_code");
                
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              LifeCircleErrorMessageMapper.CategoryHasData.getCode(),
                                              "该维度下已经存在维度数据，不允许修改nd_code");
            }
		}

		// 暂时不用处理gbCode(by zl )

		// 入参
		Category bean = BeanMapperUtils.beanMapper(categoryModel, Category.class);

		// 调用sdk
		
		LOG.debug("调用sdk方法:update");
		
		Category beanResult = categoryRepository.update(bean);
        if (beanResult != null) {
            
            LOG.debug("修改分类维度资源:{}",beanResult.getIdentifier());
            
            // 成功返回了数据
            result = BeanMapperUtils.beanMapper(beanResult, CategoryModel.class);
        }
        return result;
	}

	/**
	 * 查询分类维度
	 * 
	 * <br>Created 2015年5月4日 上午11:05:21
	 * @param words
	 * @param limit
	 * @return
	 * @throws EspStoreException
	 * @author       linsm
	 */
	@Override
	public ListViewModel<CategoryModel> queryCategory(String words, String limit) throws EspStoreException{
		ListViewModel<CategoryModel> result = new ListViewModel<CategoryModel>();

		// requestParam
		QueryRequest queryRequest = new QueryRequest();
		Integer limitResult[] = ParamCheckUtil.checkLimit(limit);// 这里其实只需要分解数据
		queryRequest.setKeyword(words);
		queryRequest.setLimit(limitResult[1]);
		queryRequest.setOffset(limitResult[0]);

		// 调用sdk
		
		LOG.debug("调用sdk方法:search");
		
        QueryResponse<Category> response = categoryRepository.search(queryRequest);
		// 处理返回数据
		
        long total = 0L;
        List<CategoryModel> items = new ArrayList<CategoryModel>();
        if (response != null && response.getHits() != null) {

            items = ObjectUtils.fromJson(ObjectUtils.toJson(response.getHits().getDocs()),
                                         new TypeToken<List<CategoryModel>>() {
                                         });
            total = response.getHits().getTotal();
        }
        result.setTotal(total);
        result.setItems(items);
        result.setLimit(limit);

		return result;
	}

	/**
	 * 删除分类维度
	 * 
	 * <br>Created 2015年5月4日 上午11:05:40
	 * @param cid
	 * @return
	 * @throws EspStoreException
	 * @author       linsm
	 */
	@Override
	public void removeCategory(String cid) throws EspStoreException{
		//logic check
		// 先断定是否存在：
		CategoryModel testModel = null;
		try {
			testModel = loadCategoryById(cid);
		} catch (EspStoreException e) {
			
		    LOG.error(LifeCircleErrorMessageMapper.CategoryCheckingError.getMessage(),e);
			
		    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
		                                  LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getMessage());
		}
		if (testModel == null) {
			// 抛出不存在该数据的异常;
		    
		    LOG.error(LifeCircleErrorMessageMapper.CategoryNotFound.getMessage());
		    
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					LifeCircleErrorMessageMapper.CategoryNotFound);
		}

		if (hasCategoryData(testModel.getNdCode())) {
		    
		    LOG.error(LifeCircleErrorMessageMapper.CategoryHasData.getMessage());
		    
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					LifeCircleErrorMessageMapper.CategoryHasData);
		}
		
		LOG.debug("调用sdk方法:del");
		
		categoryRepository.del(cid);
		
		LOG.debug("删除分类维度资源:{}",cid);
		
	}


	/**
	 * 通过ndCode获取维度数据详情
	 * 
	 * <br>Created 2015年5月4日 上午11:06:10
	 * @param ndCode
	 * @return
	 * @throws EspStoreException
	 * @author       linsm
	 */
	@Override
	public CategoryDataModel loadCategoryDataByNdCode(String ndCode)
			throws EspStoreException {
        Item<String> item = new Item<>();
        item.setKey("ndCode");
        item.setComparsionOperator(ComparsionOperator.EQ);
        item.setLogicalOperator(LogicalOperator.AND);
        item.setValue(ValueUtils.newValue(ndCode));
        List<Item<? extends Object>> items = new ArrayList<>();
        items.add(item);
        
        LOG.debug("调用sdk方法:findOneByItems");
        
        return changeCategoryDataFromBean(categoryDataRepository.findOneByItems(items));
		
	}

	


	/**
	 * 通过uuid 获取维度数据详情
	 * 
	 * <br>Created 2015年5月4日 上午11:33:13
	 * @param id
	 * @return
	 * @throws EspStoreException
	 * @author       linsm
	 */
	@Override
	public CategoryDataModel loadCategoryDataById(String id) throws EspStoreException{
	    
	    LOG.debug("调用sdk方法:get");
	    
	    return changeCategoryDataFromBean(categoryDataRepository.get(id));
	}

	/**
	 * 创建 维度数据资源
	 * 
	 * <br>Created 2015年5月4日 上午11:33:49
	 * @param categoryDataModel
	 * @return
	 * @throws EspStoreException
	 * @author       linsm
	 */
	@Override
	public CategoryDataModel createCategoryData(
			CategoryDataModel categoryDataModel) throws EspStoreException{
	    //对维度数据nd_code的特殊情况进行处理，如 OTC等可扩展的nd_code
	    
	    LOG.debug("改变前(ndCode): "+categoryDataModel.getNdCode());
	    
	    changeCategoryDataNdCode(categoryDataModel);
	    
	    LOG.debug("改变后(ndCode): "+categoryDataModel.getNdCode());
		
		// logic check
		// ndCode, gbCode(选填) 惟一性
		CategoryDataModel testModel = null;

		try {
			testModel = loadCategoryDataByNdCode(categoryDataModel.getNdCode());
		} catch (EspStoreException e) {
		    
			LOG.error(LifeCircleErrorMessageMapper.CategoryCheckingError.getMessage(),e);
			
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
			                              LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getMessage());
		}
		if (testModel != null) {
		    
		    LOG.error(LifeCircleErrorMessageMapper.DuplicateNDcodeError.getMessage());
		    
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					LifeCircleErrorMessageMapper.DuplicateNDcodeError);

		}

		// 通过uuid 对应的资源是否存在：parent, category
		// parent
		CategoryDataModel parent = assertCategoryDataExistById(categoryDataModel.getParent().getIdentifier());
		// category
		CategoryModel category = assertCategoryExistById(categoryDataModel.getCategory().getIdentifier());
		
        NdCodePattern.checkNdCodeRelation(category.getNdCode(), parent.getNdCode(), categoryDataModel.getNdCode());
        
        //增加维度数据：shortName, title 局部惟一性校验
        checkLocalUniqueTitleAndShortName(categoryDataModel,true,true);
        // 处理dimension_path
        categoryDataModel.setDimensionPath(parent.getDimensionPath() + "/" + categoryDataModel.getNdCode());

        // 入参
		CategoryData bean = changeCategoryDataToBean(categoryDataModel);

		// 调用sdk
		
		LOG.debug("调用sdk方法:add");
		
		LOG.debug("创建维度数据资源:{}",bean.getIdentifier());
		
		return changeCategoryDataFromBean(categoryDataRepository.add(bean));
	}

	/**
     * 校验维度数据title shortName 局部惟一性 (非惟一性，抛出异常)
     * @param categoryDataModel
     * @param isTitleNeed
     * @param isShortNameNeed
     * @since 
     */
    private void checkLocalUniqueTitleAndShortName(CategoryDataModel categoryDataModel, boolean isTitleNeed, boolean isShortNameNeed) {
        // 都不需要校验
        if (!isTitleNeed && !isShortNameNeed) {
            return;
        }

        if (categoryDataModel == null || categoryDataModel.getCategory() == null
                || categoryDataModel.getParent() == null) {
            // 不需要校验
            return;
        }

        CategoryData condition = new CategoryData();
        condition.setCategory(categoryDataModel.getCategory().getIdentifier());
        condition.setParent(categoryDataModel.getParent().getIdentifier());
        List<CategoryData> datas = null;
        try {
            
            LOG.debug("调用sdk方法:getAllByExample");
            
            datas = categoryDataRepository.getAllByExample(condition);
        } catch (EspStoreException e) {
            
            LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(),e);
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                          e.getMessage());
        }

        if (CollectionUtils.isEmpty(datas)) {
            
            LOG.debug("还不存在维度数据");
            
            return;
        }

        // 重名抛出异常
        for (CategoryData data : datas) {
            if (data != null) {
                if (isTitleNeed && categoryDataModel.getTitle().equals(data.getTitle())) {
                    
                    LOG.error(LifeCircleErrorMessageMapper.DuplicateTitleError.getMessage());
                    
                    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                  LifeCircleErrorMessageMapper.DuplicateTitleError);
                }
                if (isShortNameNeed && categoryDataModel.getShortName().equals(data.getShortName())) {
                    
                    LOG.error(LifeCircleErrorMessageMapper.DuplicateShortNameError.getMessage());
                    
                    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                  LifeCircleErrorMessageMapper.DuplicateShortNameError);
                }
            }
        }
        
        
    }


    /**
     * 
     * @param ndCode
     * @return
     * @since 
     */
    private void changeCategoryDataNdCode(CategoryDataModel paramModel) {
        String ndCode = paramModel.getNdCode();
        NdCodeExtend ndCodeExtend = NdCodeExtend.fromString(ndCode);
        //需要扩展
        if (ndCodeExtend != null) {
            String newNdCode = ndCodeExtend.changeNdCode(0L);
            CategoryDataModel resultModel = null;
            try {
                resultModel = loadCategoryDataByNdCode(newNdCode);
            } catch (EspStoreException e) {
                
                LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(),e);
                
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                              e.getMessage());
            }
            // 判断父结点是否存在
            CategoryDataModel parent = new CategoryDataModel();
            long orderNum; // 初始值
            if (resultModel == null) {
                //不存在
                parent.setIdentifier("ROOT");
                paramModel.setParent(parent);
                paramModel.setNdCode(newNdCode);
                paramModel.setIdentifier(UUID.randomUUID().toString());// 更新
                try {
                    resultModel = createCategoryData(paramModel);
                } catch (EspStoreException e) {
                    
                    LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(),e);
                    
                    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                  LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                                  e.getMessage());
                }
                orderNum =1L;
            } else {
                //已经存在父结点
                //查询个数
                AdaptQueryRequest<CategoryData> queryRequest = new AdaptQueryRequest<>();
                CategoryData bean= new CategoryData();
                bean.setCategory(resultModel.getCategory().getIdentifier());
                bean.setParent(resultModel.getIdentifier());
                queryRequest.setParam(bean);
                queryRequest.setLimit(1);
                queryRequest.setOffset(0);
                queryRequest.setKeyword("");
                try {
                    
                    LOG.debug("调用sdk方法:searchByExample");
                    
                    orderNum = categoryDataRepository.searchByExample(queryRequest).getHits().getTotal()+1;//注意是从1开始;
                } catch (EspStoreException e) {
                   
                    LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(),e);
                    
                    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                  LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                                  e.getMessage());
                }
            }
            newNdCode = ndCodeExtend.changeNdCode(orderNum);
            
            LOG.debug("维度数据编码序号："+orderNum);
            LOG.debug("生成的维度数据编码："+newNdCode);
            
            parent.setIdentifier(resultModel.getIdentifier()); //父结点id
            paramModel.setParent(parent);
            paramModel.setNdCode(newNdCode);
            paramModel.setOrderNum((int) orderNum);
            paramModel.setIdentifier(UUID.randomUUID().toString());// 更新
        }

    }


    /**
	 * 通过uuid 确认分类维度资源存在
	 * 
	 * <br>Created 2015年5月4日 上午11:34:12
	 * @param cid
	 * @author       linsm
	 */
	private CategoryModel assertCategoryExistById(String cid) {
		CategoryModel categoryModel = null;
		try {
			categoryModel = loadCategoryById(cid);
		} catch (EspStoreException e) {
		    
			LOG.error(LifeCircleErrorMessageMapper.CategoryCheckingError.getMessage(),e);
			
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
			                              LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getMessage());
		}
		if (categoryModel == null) {
			// 抛出分类维度“资源”不存在 异常
		    
		    LOG.error(LifeCircleErrorMessageMapper.CategoryNotFound.getMessage());
		    
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					LifeCircleErrorMessageMapper.CategoryNotFound);
		}
		return categoryModel;
		
	}


	/**
	 * 修改维度数据
	 * 
	 * <br>Created 2015年5月4日 上午11:35:14
	 * @param categoryDataModel
	 * @return
	 * @throws EspStoreException
	 * @author       linsm
	 */
	@Override
	public CategoryDataModel modifyCategoryData(CategoryDataModel categoryDataModel) throws EspStoreException{
		//logic check 
		//see category

		// 通过uuid, 获取数据
		CategoryDataModel testModel = null;
		String categoryDataId = categoryDataModel.getIdentifier();
		try {
			testModel = loadCategoryDataById(categoryDataId);
		} catch (EspStoreException e) {
			
		    LOG.error(LifeCircleErrorMessageMapper.CategoryCheckingError.getMessage(),e);
			
		    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
		                                  LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getMessage());
		}
		if (testModel == null) {
		    
		    LOG.error(LifeCircleErrorMessageMapper.CategoryDataNotFound.getMessage());
		    
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					LifeCircleErrorMessageMapper.CategoryDataNotFound);
		}

		// 对比testModel 与 categoryDataModel 不能重复： nd_code
		// nd_code
		String categoryDataNdCode = categoryDataModel.getNdCode();
		if (!testModel.getNdCode().equals(categoryDataNdCode)) {
			// 发生改变的属性：判断“不重复”
			CategoryDataModel testAttributeModel = null; // 用于暂存根据属性得到的数据
			try {
				testAttributeModel = loadCategoryDataByNdCode(categoryDataNdCode);
			} catch (EspStoreException e) {
			    
				LOG.error(LifeCircleErrorMessageMapper.CategoryCheckingError.getMessage(),e);
				
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
				                              LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getMessage());
			}
			// 非空，且 uuid 不同，说明违反“重复”(有问题，ndCode与uuid一一对应，ndCode不一致，说明uuid必然不一致）
			if (testAttributeModel != null
					&& !testAttributeModel.getIdentifier().equals(
							categoryDataId)) {
			    
			    LOG.error(LifeCircleErrorMessageMapper.DuplicateNDcodeError.getMessage());
			    
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
						LifeCircleErrorMessageMapper.DuplicateNDcodeError);
			}
			
            // 若存在子结点，则不允许修改维度数据编码
            if (hasChildNode(testModel.getIdentifier())) {
                
                LOG.error("存在子结点，不允许修改nd_code");
                
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              LifeCircleErrorMessageMapper.CategoryDataHasChildNode.getCode(),
                                              "存在子结点，不允许修改nd_code");
            }
		}

		// 通过uuid 对应的资源是否存在：parent, category
		// parent
		CategoryDataModel parent = assertCategoryDataExistById(categoryDataModel.getParent().getIdentifier());
		// category
		CategoryModel category = assertCategoryExistById(categoryDataModel.getCategory().getIdentifier());
		
		//添加了对维度ndCode的校验
		NdCodePattern.checkNdCodeRelation(category.getNdCode(), parent.getNdCode(), categoryDataModel.getNdCode());
		//暂时不用处理gbCode
		
        // 增加维度数据：shortName, title 局部惟一性校验
        boolean isTitleNeed = false; // 默认无需校验惟一性
        boolean isShortNameNeed = false;
        if (!categoryDataModel.getTitle().equals(testModel.getTitle())) {
            isTitleNeed = true;
        }
        if (!categoryDataModel.getShortName().equals(testModel.getShortName())) {
            isShortNameNeed = true;
        }
        checkLocalUniqueTitleAndShortName(categoryDataModel, isTitleNeed, isShortNameNeed);
		
        // 有可能会发生了改变，当改变父结点，需要重新生成
        categoryDataModel.setDimensionPath(parent.getDimensionPath() + "/" + categoryDataModel.getNdCode());

		// 入参
		CategoryData bean = changeCategoryDataToBean(categoryDataModel);
		

		// 调用sdk
		LOG.debug("调用sdk方法:update");
		LOG.debug("修改维度数据资源:{}",bean.getIdentifier());
		
		//不可改变的值  add by xuzy 20160719
		if(StringUtils.isEmpty(bean.getPreview())){
			bean.setPreview(testModel.getPreview());
		}
		
		return changeCategoryDataFromBean(categoryDataRepository.update(bean));
	}

	/**
	 * 把维度数据转换成sdk类型
	 * 
	 * <br>Created 2015年5月4日 上午11:35:37
	 * @param categoryDataModel
	 * @return
	 * @author       linsm
	 */
	private CategoryData changeCategoryDataToBean(
			CategoryDataModel categoryDataModel) {
	    if(categoryDataModel == null){
	        return null;
	    }
		CategoryData bean = BeanMapperUtils.beanMapper(categoryDataModel, CategoryData.class);
		bean.setCategory(categoryDataModel.getCategory().getIdentifier());
		bean.setParent(categoryDataModel.getParent().getIdentifier());
		return bean;
	}


	/**
	 * 把维度数据转换成 生命周期类型
	 * 
	 * <br>Created 2015年5月4日 上午11:36:27
	 * @param bean
	 * @return
	 * @author       linsm
	 */
	private CategoryDataModel changeCategoryDataFromBean(CategoryData bean) {
	    if(bean == null){
	        return null;
	    }
		CategoryDataModel result = BeanMapperUtils.beanMapper(bean,
				CategoryDataModel.class);
		CategoryModel category = new CategoryModel();
		category.setIdentifier(bean.getCategory());
		result.setCategory(category);
		
		CategoryDataModel categoryData = new CategoryDataModel();
		categoryData.setIdentifier(bean.getParent());
		result.setParent(categoryData);
		return result;
	}


	/**
	 * 查询维度数据
	 * 
	 * <br>Created 2015年5月4日 上午11:38:24
	 * @param ndCode
	 * @param all
	 * @param parentId
	 * @param words
	 * @param limit
	 * @return
	 * @throws EspStoreException
	 * @author       linsm
	 */
	@Override
	public ListViewModel<CategoryDataModel> queryCategoryData(String ndCode,
			Boolean all, String parentId, String words, String limit) throws EspStoreException{
		ListViewModel<CategoryDataModel> result = new ListViewModel<CategoryDataModel>();
		// 根据 ndCode 找到 categoryId;
		CategoryModel category = loadCategoryByNdCode(ndCode);
		if (category == null) {
		    
		    LOG.error(LifeCircleErrorMessageMapper.CategoryNotFound.getMessage()+ndCode);
		    
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					LifeCircleErrorMessageMapper.CategoryNotFound);
		}
		String categoryId = category.getIdentifier();
		// requestParam
		AdaptQueryRequest<CategoryData> queryRequest = new AdaptQueryRequest<>();
		CategoryData bean= new CategoryData();
		bean.setCategory(categoryId);
        bean.setParent(parentId);
		queryRequest.setParam(bean);
		if(!StringUtils.isEmpty(parentId)){
			//只返回一层数据，此时，words 和limit 起作用
			Integer limitResult[] = ParamCheckUtil.checkLimit(limit);// 这里其实只需要分解数据
			queryRequest.setKeyword(words);
			queryRequest.setLimit(limitResult[1]);
			queryRequest.setOffset(limitResult[0]);	
		}else{
			//通过多一次的查询获得总量;
			queryRequest.setKeyword("");
			queryRequest.setLimit(1);
			queryRequest.setOffset(0);	
			
			// 调用sdk
			
			LOG.debug("调用sdk方法:searchByExample");
			
			QueryResponse<CategoryData> firstResponse = categoryDataRepository.searchByExample(queryRequest);
			if (firstResponse != null && firstResponse.getHits() != null) {
			    int total = (int) firstResponse.getHits().getTotal();
			    
			    LOG.info("共有维度数据："+total);
			    
                // 没有数据，不用再查询
                if (total <= 0) {

                    result.setTotal(0L);
                    // limit 不起作用时，构造成(0,total)的形式
                    result.setLimit("(0,0)");
                    return result;
                }
                queryRequest.setLimit(total); // 设置总数
			}
		}
		// 调用sdk
		
		LOG.debug("调用sdk方法:searchByExample");
		
        QueryResponse<CategoryData> response = categoryDataRepository.searchByExample(queryRequest);

		// 处理返回数据
		result.setTotal(0L);
		if (response != null && response.getHits() != null) {
			List<CategoryDataModel> items = new ArrayList<CategoryDataModel>();
			List<CategoryData> categoryDatas = response.getHits()
					.getDocs();
			if (categoryDatas != null) {
				for (CategoryData categoryData : categoryDatas) {
					CategoryDataModel categoryDataModel = changeCategoryDataFromBean(categoryData);
					items.add(categoryDataModel);
				}
			}
			result.setItems(items);
			result.setTotal(response.getHits().getTotal());
		}
		if(!StringUtils.isEmpty(parentId)){
			//只返回一层数据，此时，words 和limit 起作用
			result.setLimit(limit);
		}else{
			//limit 不起作用时，构造成(0,total)的形式
			result.setLimit("(0,"+result.getTotal()+")");
		}
		

		return result;
	}

	/**
	 * 删除维度数据
	 * 
	 * <br>Created 2015年5月4日 上午11:38:53
	 * @param did
	 * @return
	 * @throws EspStoreException
	 * @author       linsm
	 */
	@Override
	public void removeCategoryData(String did) throws EspStoreException{
		// check logic
		// 先断定是否存在：;
		assertCategoryDataExistByIdWithoutRoot(did);//没有可能是ROOT， 是资源就存在uuid 
		// 1、判断是否存在子结点，是某个维度数据的父结点
		if (hasChildNode(did)) {
		    
		    LOG.error(LifeCircleErrorMessageMapper.CategoryDataHasChildNode.getMessage()+did);
		    
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					LifeCircleErrorMessageMapper.CategoryDataHasChildNode);
		}
		// 2、判断在关系表中是否有使用到该数据
		if (isExistedInRelation(did)) {
		    
		    LOG.error(LifeCircleErrorMessageMapper.CategoryRelationHasCategoryData.getMessage());
		    
			throw new LifeCircleException(
					HttpStatus.INTERNAL_SERVER_ERROR,
					LifeCircleErrorMessageMapper.CategoryRelationHasCategoryData);
		}
		
		LOG.debug("调用sdk方法:del");
		
		categoryDataRepository.del(did);
		
		LOG.debug("删除维度数据资源:{}",did);
	}


	/**
	 * 确认维度数据存在(排除root)
	 * 
	 * <br>Created 2015年5月4日 上午11:39:06
	 * @param did
	 * @return
	 * @author       linsm
	 */
	private CategoryDataModel assertCategoryDataExistByIdWithoutRoot(String did) {
		CategoryDataModel testModel = null;
		try {
			testModel = loadCategoryDataById(did);
		} catch (EspStoreException e) {
			
		    LOG.error(LifeCircleErrorMessageMapper.CategoryCheckingError.getMessage(),e);
			
		    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
		                                  LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getMessage());
		}
		if (testModel == null) {
		    
		    LOG.error(LifeCircleErrorMessageMapper.CategoryDataNotFound.getMessage());
		    
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					LifeCircleErrorMessageMapper.CategoryDataNotFound);
		}
		return testModel;
		
	}


	/**
	 * 通过模式名(patternName)获取维度模式详情
	 * 
	 * <br>Created 2015年5月4日 上午11:39:43
	 * @param patternName
	 * @return
	 * @throws EspStoreException
	 * @author       linsm
	 */
	@Override
	public CategoryPatternModel loadCategoryPatternByPatternName(
			String patternName) throws EspStoreException{
		CategoryPatternModel result = null;
	    CategoryPattern param = new CategoryPattern();
        param.setPatternName(patternName);
        // 调用sdk
        
        LOG.debug("调用sdk方法:getByExample");
        
        CategoryPattern beanResult = categoryPatternRepository.getByExample(param);
        if (beanResult != null) {
            result = BeanMapperUtils.beanMapper(beanResult, CategoryPatternModel.class);
        }
        
        return result;
	}



	/**
	 * 能过uuid 获取维度模式详情
	 * 
	 * <br>Created 2015年5月4日 上午11:40:36
	 * @param id
	 * @return
	 * @throws EspStoreException
	 * @author       linsm
	 */
	@Override
	public CategoryPatternModel loadCategoryPatternById(String id) throws EspStoreException{
		CategoryPatternModel result = null;
		// 调用sdk
		
		LOG.debug("调用sdk方法:get");
		
        CategoryPattern beanResult = categoryPatternRepository.get(id);
        if (beanResult != null) {
            result = BeanMapperUtils.beanMapper(beanResult, CategoryPatternModel.class);
        }
        
        return result;
	}

	/**
	 * 创建维度模式资源
	 * 
	 * <br>Created 2015年5月4日 上午11:41:06
	 * @param categoryPatternModel
	 * @return
	 * @throws EspStoreException
	 * @author       linsm
	 */
	@Override
	public CategoryPatternModel creatCategoryPattern(
			CategoryPatternModel categoryPatternModel) throws EspStoreException{
		CategoryPatternModel result = null;
		
		//check logic
		//patternName
		CategoryPatternModel testModel = null;

		try {
			testModel = loadCategoryPatternByPatternName(categoryPatternModel
							.getPatternName()); 
		} catch (EspStoreException e) {
		    
			LOG.error(LifeCircleErrorMessageMapper.CategoryCheckingError.getMessage(),e);
			
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
			                              LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getMessage());
		}
		if (testModel != null) {
		    
		    LOG.error(LifeCircleErrorMessageMapper.DuplicatePatternNameError.getMessage());
		    
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					LifeCircleErrorMessageMapper.DuplicatePatternNameError);

		}

		// 入参
		CategoryPattern bean = BeanMapperUtils.beanMapper(categoryPatternModel,
				CategoryPattern.class);

		// 调用sdk
		
		LOG.debug("调用sdk方法:add");
		
        CategoryPattern beanResult = categoryPatternRepository.add(bean);
        if (beanResult != null) {
            
            LOG.debug("创建维度模式资源:{}",beanResult.getIdentifier());
            
            result = BeanMapperUtils.beanMapper(beanResult, CategoryPatternModel.class);
        }
        
        return result;
	}

	/**
	 * 修改维度模式
	 * 
	 * <br>Created 2015年5月4日 上午11:41:23
	 * @param categoryPatternModel
	 * @return
	 * @throws EspStoreException
	 * @author       linsm
	 */
	@Override
	public CategoryPatternModel modifyCategoryPattern(
			CategoryPatternModel categoryPatternModel) throws EspStoreException {
		CategoryPatternModel result = null;
		
		//check logic
		//see category

		// 通过uuid, 获取数据
		String patternId = categoryPatternModel.getIdentifier();
		CategoryPatternModel testModel = null;
		try {
			testModel = loadCategoryPatternById(patternId);
		} catch (EspStoreException e) {
		    
			LOG.error(LifeCircleErrorMessageMapper.CategoryCheckingError.getMessage(),e);
			
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
			                              LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getMessage());
		}
		if (testModel == null) {
		    
		    LOG.error(LifeCircleErrorMessageMapper.CategoryPatternNotFound.getMessage());
		    
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					LifeCircleErrorMessageMapper.CategoryPatternNotFound);
		}

		// 对比testModel 与 modifyModel 不能重复：patternName
		String patternName = categoryPatternModel.getPatternName();
		if (!testModel.getPatternName().equals(patternName)) {
			// 发生改变的属性：判断“不重复”
			CategoryPatternModel testAttributeModel = null; // 用于暂存根据属性得到的数据
			try {
				testAttributeModel = loadCategoryPatternByPatternName(patternName);
			} catch (EspStoreException e) {
				
			    LOG.error(LifeCircleErrorMessageMapper.CategoryCheckingError.getMessage(),e);
				
			    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
			                                  LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getMessage());
			}
			// 非空，且 uuid 不同，说明违反“重复”
			if (testAttributeModel != null
					&& !testAttributeModel.getIdentifier().equals(patternId)) {
			    
			    LOG.error(LifeCircleErrorMessageMapper.DuplicatePatternNameError.getMessage());
			    
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
						LifeCircleErrorMessageMapper.DuplicatePatternNameError);
			}
		}

		// 入参
		CategoryPattern bean = BeanMapperUtils.beanMapper(categoryPatternModel,
				CategoryPattern.class);

		// 调用sdk
		
		LOG.debug("调用sdk方法:update");
		
		//不能改变的属性
		bean.setSegment(testModel.getSegment());
		
        CategoryPattern beanResult = categoryPatternRepository.update(bean);
        if (beanResult != null) {
            
            LOG.debug("修改维度模式资源:{}",beanResult.getIdentifier());
            
            result = BeanMapperUtils.beanMapper(beanResult, CategoryPatternModel.class);
        }
        
        return result;
	}
	

	/**
	 * 查询维度模式
	 * 
	 * <br>Created 2015年5月4日 上午11:41:43
	 * @param words
	 * @param limit
	 * @return
	 * @throws EspStoreException
	 * @author       linsm
	 */
	@Override
	public ListViewModel<CategoryPatternModel> queryCategoryPatterns(
			String words, String limit, String gbCode) throws EspStoreException {
		ListViewModel<CategoryPatternModel> result = new ListViewModel<CategoryPatternModel>();

		// requestParam
//		QueryRequest queryRequest = new QueryRequest();
//		Integer limitResult[] = ParamCheckUtil.checkLimit(limit);// 这里其实只需要分解数据
//		queryRequest.setKeyword(words);
//		queryRequest.setLimit(limitResult[1]);
//		queryRequest.setOffset(limitResult[0]);

		AdaptQueryRequest adaptQueryRequest=new AdaptQueryRequest();
		adaptQueryRequest.and("gbCode", gbCode);
		Integer limitResult[] = ParamCheckUtil.checkLimit(limit);// 这里其实只需要分解数据
		adaptQueryRequest.setKeyword(words);
		adaptQueryRequest.setLimit(limitResult[1]);
		adaptQueryRequest.setOffset(limitResult[0]);

		// 调用sdk
		
		LOG.debug("调用sdk方法:search");
		
		QueryResponse<CategoryPattern> response = categoryPatternRepository.search(adaptQueryRequest);

		// 处理返回数据
		long total = 0L;
		List<CategoryPatternModel> items = new ArrayList<CategoryPatternModel>();
		if (response != null && response.getHits() != null) {
			List<CategoryPattern> categoryPatterns = response.getHits().getDocs();
			if(categoryPatterns != null){
				for(CategoryPattern categoryPattern: categoryPatterns){
					CategoryPatternModel categoryPatternModel = BeanMapperUtils.beanMapper(categoryPattern,
							CategoryPatternModel.class);
					items.add(categoryPatternModel);
				}
			}
			total = response.getHits().getTotal();
		}
		result.setItems(items);
        result.setTotal(total);
        result.setLimit(limit);

		return result;
	}

	/**
	 * 删除维度模式
	 * 
	 * <br>Created 2015年5月4日 上午11:41:56
	 * @param cpid
	 * @return
	 * @throws EspStoreException
	 * @author       linsm
	 */
	@Override
	public void removeCategoryPattern(String cpid) throws EspStoreException{
		//logic check
		// 先断定是否存在：
		CategoryPatternModel testModel = null;
		try {
			testModel = loadCategoryPatternById(cpid);
		} catch (EspStoreException e) {
		    
			LOG.error(LifeCircleErrorMessageMapper.CategoryCheckingError.getMessage(),e);
			
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getMessage());
		}
		if (testModel == null) {
			// 抛出不存在该数据的异常;
		    
		    LOG.error(LifeCircleErrorMessageMapper.CategoryPatternNotFound.getMessage());
		    
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					LifeCircleErrorMessageMapper.CategoryPatternNotFound);
		}
		if (patternHasRelationData(testModel.getPatternName())) {
		    
		    LOG.error(LifeCircleErrorMessageMapper.CategoryPatternHasCategoryRelation.getMessage());
		    
			throw new LifeCircleException(
					HttpStatus.INTERNAL_SERVER_ERROR,
					LifeCircleErrorMessageMapper.CategoryPatternHasCategoryRelation);
		}
		
		LOG.debug("调用sdk方法:del");
		
		categoryPatternRepository.del(cpid);
		
		LOG.debug("删除维度模式资源:{}",cpid);
	}


	/**
	 * 通过uuid 获取维度数据关系详情
	 * 
	 * <br>Created 2015年5月4日 上午11:42:18
	 * @param id
	 * @return
	 * @throws EspStoreException
	 * @author       linsm
	 */
	private CategoryRelationModel loadCategoryRelationById(String id)throws EspStoreException {
	    // 调用sdk
	    
	    LOG.debug("调用sdk方法:get");
	    
	    return changeCategoryRelationFromBean(categoryRelationRepository.get(id));
	}

	/**
	 * 维度数据关系转换成生命周期类型
	 * 
	 * <br>Created 2015年5月4日 上午11:42:47
	 * @param beanResult
	 * @return
	 * @author       linsm
	 */
	private CategoryRelationModel changeCategoryRelationFromBean(
			CategoryRelation beanResult) {
	    if(beanResult == null){
	        return null;
	    }
		CategoryRelationModel result = BeanMapperUtils.beanMapper(beanResult,
				CategoryRelationModel.class);

		// pattern
		CategoryPatternModel pattern = new CategoryPatternModel();
		pattern.setIdentifier(beanResult.getPattern());
		result.setPattern(pattern);

		// source
		CategoryDataModel source = new CategoryDataModel();
		source.setIdentifier(beanResult.getSource());
		result.setSource(source);

		// target
		CategoryDataModel target = new CategoryDataModel();
		target.setIdentifier(beanResult.getTarget());
		result.setTarget(target);

		return result;
	}


	/**
	 * 创建维度数据关系
	 * 
	 * <br>Created 2015年5月4日 上午11:43:50
	 * @param categoryRelationModel
	 * @return
	 * @throws EspStoreException
	 * @author       linsm
	 */
	@Override
	public CategoryRelationModel createCategoryRelation(
			CategoryRelationModel categoryRelationModel) throws EspStoreException{
	    
	    //通过pattern_path、target 判断是否已经存在对应的关系，若已存在则抛出异常
	    assertNotExistRelationByPatternPathAndTarget(categoryRelationModel.getPatternPath(), categoryRelationModel.getTarget().getIdentifier());
	    
		//logic check
		// 通过uuid 对应的资源是否存在：source,target,pattern, levelParent
		
		// source
		assertCategoryDataExistById(categoryRelationModel.getSource().getIdentifier());
		// target
		assertCategoryDataExistById(categoryRelationModel.getTarget().getIdentifier());
		// levelParent
		assertCategoryDataExistByNdCode(categoryRelationModel.getLevelParent());
		// pattern
		assertCategoryPatternExistById(categoryRelationModel.getPattern().getIdentifier());
		
		// 入参
		CategoryRelation bean = changeCategoryRelationToBean(categoryRelationModel);


		// 调用sdk
		
		LOG.debug("调用sdk方法:add");
		LOG.debug("创建维度关系:{}",bean.getIdentifier());
		
		return changeCategoryRelationFromBean(categoryRelationRepository.add(bean));
		
	}

	


    /**
     * 验证还未建立关系(通过pattern_path、target 判断是否已经存在对应的关系，若已存在则抛出异常)
     * @param patternPath 关系路径
     * @param identifier  关系目标
     * @since 
     */
    private void assertNotExistRelationByPatternPathAndTarget(String patternPath, String target){
        CategoryRelationModel model = getRelationByPatternPathAndTarget(patternPath, target);
        if(model != null){
            
            LOG.error(LifeCircleErrorMessageMapper.CategoryRelationAlreadyExist.getMessage()); 
           
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.CategoryRelationAlreadyExist);
        }
        
    }


    /**
     * 查询关系（通过关系路径与目标）
     * @param patternPath
     * @param target
     * @return
     * @since 
     */
    private CategoryRelationModel getRelationByPatternPathAndTarget(String patternPath, String target){
        CategoryRelation condition = new CategoryRelation();
        condition.setPatternPath(patternPath);
        condition.setTarget(target);
        List<CategoryRelation> relations =null;
        try {
            
            LOG.debug("调用sdk方法:getAllByExample");
            
            relations = categoryRelationRepository.getAllByExample(condition);
        } catch (EspStoreException e) {
           
            LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage());
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                          e.getMessage());
        }
        if(CollectionUtils.isEmpty(relations)){
            return null;
        }else{
            return changeCategoryRelationFromBean(relations.get(0));//一般情况下是只能有一个，但有可能数据库中已存在脏数据。
        }
    }

    /**
     * 查询关系的个数：
     * 
     * @param patternPath 关系路径
     * @param enable 关系是否可用
     * @param ndCode target ndCode
     * @return int 关系个数
     * @author linsm
     */
    @Override
    public int countRelation(String patternPath, Boolean enable, String ndCode) {
        CategoryRelation condition = new CategoryRelation();
        if (StringUtils.isNotEmpty(patternPath)) {
            condition.setPatternPath(patternPath);
        }
        if (StringUtils.isNotEmpty(ndCode)) {
            CategoryDataModel testModel = null;
            try {
                testModel = loadCategoryDataByNdCode(ndCode);
            } catch (EspStoreException e) {
                
                LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(),e);
                
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                              e.getMessage());
            }
            if (testModel == null) {
               
                LOG.error(LifeCircleErrorMessageMapper.CategoryDataNotFound.getMessage() + "nd_code: " + ndCode);
               
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              LifeCircleErrorMessageMapper.CategoryDataNotFound.getCode(),
                                              LifeCircleErrorMessageMapper.CategoryDataNotFound.getMessage()
                                                      + "nd_code: " + ndCode);
            }
            condition.setTarget(testModel.getIdentifier());
        }
        condition.setEnable(enable);

        List<CategoryRelation> relations = null;
        try {
            
            LOG.debug("调用sdk方法:getAllByExample");
            
            relations = categoryRelationRepository.getAllByExample(condition);

        } catch (EspStoreException e) {
            
            LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(),e);
           
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                          e.getMessage());
        }
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(relations)) {
            return 0;
        } else {
            return relations.size();
        }

    }

    /**
	 * 维度数据关系转换成sdk类型
	 * 
	 * <br>Created 2015年5月4日 上午11:44:09
	 * @param categoryRelationModel
	 * @return
	 * @author       linsm
	 */
	private CategoryRelation changeCategoryRelationToBean(
			CategoryRelationModel categoryRelationModel) {
	    if(categoryRelationModel == null){
	        return null;
	    }
		CategoryRelation bean = new CategoryRelation();
		bean.setEnable(categoryRelationModel.isEnable());
		bean.setIdentifier(categoryRelationModel.getIdentifier());
		bean.setOrderNum(categoryRelationModel.getOrderNum());
		bean.setPatternPath(categoryRelationModel.getPatternPath());
		bean.setRelationType(categoryRelationModel.getRelationType());
		bean.setTags(categoryRelationModel.getTags());

		bean.setLevelParent(categoryRelationModel.getLevelParent());
		bean.setPattern(categoryRelationModel.getPattern().getIdentifier());
		bean.setSource(categoryRelationModel.getSource().getIdentifier());
		bean.setTarget(categoryRelationModel.getTarget().getIdentifier());
		
		return bean;
	}


	/**
	 * 通过uuid 确认维度模式存在
	 * 
	 * <br>Created 2015年5月4日 上午11:44:32
	 * @param cpid
	 * @author       linsm
	 */
	private void assertCategoryPatternExistById(String cpid) {
		CategoryPatternModel testModel = null;
		try {
			testModel = loadCategoryPatternById(cpid);
		} catch (EspStoreException e) {
			
		    LOG.error(LifeCircleErrorMessageMapper.CategoryCheckingError.getMessage(),e);
			
		    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getMessage());
		}
		if (testModel == null) {
			// 抛出分类维度“资源”不存在 异常
			
		    LOG.error(LifeCircleErrorMessageMapper.CategoryPatternNotFound.getMessage());
		    
		    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					LifeCircleErrorMessageMapper.CategoryPatternNotFound);
		}
		
	}

	/**
	 * 通过ndCode 确认维度数据存在（包含ROOT）
	 * 
	 * <br>Created 2015年5月4日 上午11:45:17
	 * @param ndCode
	 * @author       linsm
	 */
	private void assertCategoryDataExistByNdCode(String ndCode) {
		CategoryDataModel testModel = null;
		if (!ndCode.equals(NdCodePattern.CATEGORYDATA_TOP_NODE_PARENT)) {
			// 非顶级节点
			try {
				testModel = loadCategoryDataByNdCode(ndCode);
			} catch (EspStoreException e) {
			    
				LOG.error(LifeCircleErrorMessageMapper.CategoryCheckingError.getMessage(),e);
				
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getMessage());
			}
			if (testModel == null) {
			    
			    LOG.error(LifeCircleErrorMessageMapper.CategoryDataNotFound.getMessage());
			    
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
						LifeCircleErrorMessageMapper.CategoryDataNotFound);
			}
		} 
		
	}

    /**
     * 通过uuid（包含ROOT） 确认维度数据存在 修改，保证生成CategoryDataModel,当did == ROOT时，手动生成CategoryData（uuid== ROOT, dimension_path=ROOT, ndCode=ROOT） <br>
     * Created 2015年5月4日 上午11:45:49
     * 
     * @param did
     * @return
     * @author linsm
     */
    private CategoryDataModel assertCategoryDataExistById(String did) {
        CategoryDataModel testModel = null;
        if (!did.equals(NdCodePattern.CATEGORYDATA_TOP_NODE_PARENT)) {
            // 非顶级节点
            testModel = assertCategoryDataExistByIdWithoutRoot(did);
        } else {
            testModel = new CategoryDataModel();
            testModel.setIdentifier(NdCodePattern.CATEGORYDATA_TOP_NODE_PARENT);
            testModel.setNdCode(NdCodePattern.CATEGORYDATA_TOP_NODE_PARENT);
            testModel.setDimensionPath(NdCodePattern.CATEGORYDATA_TOP_NODE_PARENT);
        }
        return testModel;
    }

	/**
	 * 查询维度数据关系
	 * 
	 * <br>Created 2015年5月4日 上午11:48:44
	 * @param levelParent
	 * @param enable
	 * @param patternPath
	 * @return
	 * @throws EspStoreException
	 * @author       linsm
	 */
	@Override
	public List<QueryRelationViewModel> queryCategoryRelation(
			String levelParent, boolean enable,
			String patternPath) throws EspStoreException {
		List<QueryRelationViewModel> result = new ArrayList<QueryRelationViewModel>();
		//patternPath 不可能为空，至少是patternName
		if(StringUtils.isEmpty(patternPath)){
		    return result;
		}

		
		LOG.debug("查询维度数据关系路径(patternPath):"+patternPath);
		
		// 设置条件，即调用sdk 的参数
		CategoryRelation condition = new CategoryRelation();
		condition.setPatternPath(patternPath);
		condition.setEnable(enable);
		if (StringUtils.isEmpty(levelParent)) {
			levelParent = NdCodePattern.CATEGORYDATA_TOP_NODE_PARENT;
		}else{
			condition.setLevelParent(levelParent);
		}
		
		LOG.debug("调用sdk方法:getAllByExample");
		
        List<CategoryRelation> relations = categoryRelationRepository.getAllByExample(condition);
		if (relations == null || relations.isEmpty()) {
			return result;
		}
		String[] path = patternPath.split("/");
        boolean flag = true; // 是否取出默认数据
		int levelValue = path.length;
		do {
			// 处理返回值：parentNdCode -> list<CategoryRelation>
			Map<String, List<CategoryRelation>> map = new LinkedHashMap<String, List<CategoryRelation>>();
			
			LOG.debug("存在维度数据关系数量："+relations.size());
			
			for (CategoryRelation categoryRelation : relations) {
				String parentNdCode = categoryRelation.getLevelParent(); // 维度数据父结点NdCode
				List<CategoryRelation> categoryRelationList = map
						.get(parentNdCode);
				if (categoryRelationList == null) {
					categoryRelationList = new ArrayList<CategoryRelation>();
					map.put(parentNdCode, categoryRelationList);
				}
				categoryRelationList.add(categoryRelation);

			}

			//优化，先将数据补充完全，避免多次调用批量取维度数据的接口
			Map<String, List<RelationViewModel>> viewMap = changeToViewModelAndGetTargets(map);
			
			// 处理成出参的形式;
			
			List<RelationViewModel> parentCategoryList = viewMap.get(levelParent);
			if (CollectionUtils.isNotEmpty(parentCategoryList)) {
				QueryRelationViewModel outLevelItem = new QueryRelationViewModel();
				outLevelItem.setLevel(levelValue++);
                sortByOrderNum(parentCategoryList); // 根据关系中的orderNum 来排序;
				outLevelItem.setItems(parentCategoryList);
				// 使用一个队列建树
				Queue<List<RelationViewModel>> queue = new ConcurrentLinkedQueue<List<RelationViewModel>>();
				queue.add(parentCategoryList);
				while (!queue.isEmpty()) {
					List<RelationViewModel> queryViewModels = queue.remove();
					for (RelationViewModel queryViewModel : queryViewModels) {
						List<RelationViewModel> pCategoryList = viewMap
								.get(queryViewModel.getTarget().getNdCode()); // 找到子结点
                        if (pCategoryList != null) {
                            sortByOrderNum(pCategoryList); // //根据关系中的orderNum来排序;
							queryViewModel
									.setLevelItems(pCategoryList);
							queue.add(pCategoryList);
						}else{
							queryViewModel.setLevelItems(new ArrayList<RelationViewModel>());
						}
					}
				}
				result.add(outLevelItem);
				RelationViewModel iteratorViewModel = outLevelItem.getItems()
						.get(0);
				while (iteratorViewModel.getLevelItems() != null
						&& !iteratorViewModel.getLevelItems().isEmpty()) {
					iteratorViewModel = iteratorViewModel.getLevelItems().get(0);
				}
				patternPath = iteratorViewModel.getPatternPath()+"/"+iteratorViewModel.getTarget().getNdCode();
				
				LOG.debug("查询维度数据关系路径(patternPath):"+patternPath);
				
				condition.setPatternPath(patternPath);
				condition.setLevelParent(null);
				levelParent = NdCodePattern.CATEGORYDATA_TOP_NODE_PARENT;
				
				LOG.debug("调用sdk方法:getAllByExample");
				
				relations = categoryRelationRepository.getAllByExample(condition);
				if (relations == null || relations.isEmpty()) {
					flag = false;
				}
			}else{
				flag = false;  //robust
			}

		} while (flag);

		return result;
	}

    /**
     * @param map
     * @return
     * @since
     */
    private Map<String, List<RelationViewModel>> changeToViewModelAndGetTargets(Map<String, List<CategoryRelation>> map) throws EspStoreException {
        Map<String, List<RelationViewModel>> viewMap = new LinkedHashMap<String, List<RelationViewModel>>();
        if(CollectionUtils.isEmpty(map)){
            return viewMap;
        }
        List<String> targetIdList = new ArrayList<String>();
        for (String key : map.keySet()) {
            List<CategoryRelation> parentCategoryList = map.get(key);
            List<RelationViewModel> queryViewModels = new ArrayList<RelationViewModel>();
            if (CollectionUtils.isNotEmpty(parentCategoryList)) {
                for (CategoryRelation relation : parentCategoryList) {
                    RelationViewModel queryViewModel = BeanMapperUtils.beanMapper(relation, RelationViewModel.class);
                    targetIdList.add(relation.getTarget()); // 与queryViewModels中的元素一一对应。
                    queryViewModels.add(queryViewModel);
                }
            }
            viewMap.put(key, queryViewModels);

        }

        LOG.debug("调用sdk方法:getAll");
        
        List<CategoryData> targetList = categoryDataRepository.getAll(targetIdList); // 无法保证一一对应的结果（如相同，只返回一个）
        // 先处理成可对应的形式;
        Map<String, CategoryData> uuidCategoryDataMap = new HashMap<String, CategoryData>();
        if (targetList != null && !targetList.isEmpty()) {
            for (CategoryData data : targetList) {
                if (data != null) {
                    uuidCategoryDataMap.put(data.getIdentifier(), data);
                }
            }
        }
        // 再放置在对应的关系中：
        int index = 0;
        for (String key : viewMap.keySet()) {
            List<RelationViewModel> queryViewModels = viewMap.get(key);
            if (CollectionUtils.isNotEmpty(queryViewModels)) {
                for (RelationViewModel model:queryViewModels) {
                    String uuid = targetIdList.get(index++);
                    CategoryData categoryData = uuidCategoryDataMap.get(uuid);
                    TargetViewModel targetViewModel;
                    if (categoryData != null) {
                        targetViewModel = BeanMapperUtils.beanMapper(categoryData, TargetViewModel.class);
                    } else {
                        targetViewModel = new TargetViewModel();
                    }
                    model.setTarget(targetViewModel);
                }
            }
        }

        return viewMap;
    }


    /**
	 * 通过orderNum 对维度数据关系排序
	 * 
	 * <br>Created 2015年5月4日 上午11:49:03
	 * @param categoryRelationQueryViewModels
	 * @author       linsm
	 */
	private void sortByOrderNum(
			List<RelationViewModel> categoryRelationQueryViewModels) {
		Collections.sort(categoryRelationQueryViewModels, new Comparator<RelationViewModel>() {
            public int compare(RelationViewModel arg0, RelationViewModel arg1) {
//                return arg0.getOrderNum()-(arg1.getOrderNum());
                return Float.compare(arg0.getOrderNum(), arg1.getOrderNum());
            }
        });
		
	}
	
	private void sortByOrderNumForAll(
			List<RelationAllViewModel> categoryRelationQueryViewModels) {
		Collections.sort(categoryRelationQueryViewModels, new Comparator<RelationAllViewModel>() {
            public int compare(RelationAllViewModel arg0, RelationAllViewModel arg1) {
                return Float.compare(arg0.getOrderNum(), arg1.getOrderNum());
            }
        });
		
	}




	/**
	 * 删除维度数据关系
	 * 
	 * <br>Created 2015年5月4日 上午11:49:55
	 * @param crid
	 * @return
	 * @throws EspStoreException
	 * @author       linsm
	 */
	@Override
	public void removeCategoryRelation(String crid) throws EspStoreException{
		// 先断定是否存在：
		assertCategoryRelationById(crid);
		
		LOG.debug("调用sdk方法:del");
		
		categoryRelationRepository.del(crid);
		
		LOG.debug("创建维度关系:{}",crid);
	}

	/**
	 * 通过uuid 确认维度数据关系资源存在
	 * 
	 * <br>Created 2015年5月4日 下午12:00:47
	 * @param crid
	 * @author       linsm
	 */
	private void assertCategoryRelationById(String crid) {
		CategoryRelationModel testModel = null;
		try {
			testModel = loadCategoryRelationById(crid);
		} catch (EspStoreException e) {
			
		    LOG.error(LifeCircleErrorMessageMapper.CategoryCheckingError.getMessage(),e);
			
		    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getMessage());
		}
		if (testModel == null) {
			// 抛出不存在该数据的异常;
		    
		    LOG.error(LifeCircleErrorMessageMapper.CategoryRelationNotFound.getMessage());
		    
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					LifeCircleErrorMessageMapper.CategoryRelationNotFound);
		}
	}


	/**
	 * 分类维度是否存在维度数据(通过ndCode)
	 * 
	 * <br>Created 2015年5月4日 下午12:06:22
	 * @param ndCode
	 * @return
	 * @author       linsm
	 */
	private boolean hasCategoryData(String ndCode) {
		//只需要判断是否至少存在一个数据，若有时，则parent = "ROOT"时也必有;
		Long total =0L;
		try {
			total = queryCategoryData(ndCode, true, "ROOT", "", "(0,1)").getTotal();
		} catch (EspStoreException e) {
			
		    LOG.error(LifeCircleErrorMessageMapper.CategoryCheckingError.getMessage(),e);
			
		    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getMessage());
		}
		return  total > 0;
	}

	
	/**
	 * 该维度数据是否存在子结点(如小学存在一、二、三、四年级等子结点)
	 * 
	 * <br>Created 2015年5月4日 下午12:07:29
	 * @param did
	 * @return
	 * @author       linsm
	 */
	private boolean hasChildNode(String did) {
		QueryResponse<CategoryData> queryResponse = null;
		
		AdaptQueryRequest<CategoryData> queryRequest = new AdaptQueryRequest<CategoryData>(new QueryRequest());
		CategoryData bean = new CategoryData();
		bean.setParent(did);
		queryRequest.setParam(bean);
		try {
		    
		    LOG.debug("调用sdk方法:searchByExample");
		    
		    queryResponse = categoryDataRepository.searchByExample(queryRequest);
		} catch (EspStoreException e) {
			
		    LOG.error(LifeCircleErrorMessageMapper.CategoryCheckingError.getMessage(),e);
			
		    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getMessage());
		}
		if (queryResponse != null && queryResponse.getHits() != null
				&& queryResponse.getHits().getDocs() != null
				&& !queryResponse.getHits().getDocs().isEmpty()) {
			return true;
		}
		return false;
	}

	/**
	 * 维度数据是否应用于维度数据关系中（能过uuid)
	 * 
	 * <br>Created 2015年5月4日 下午12:08:31
	 * @param did
	 * @return
	 * @author       linsm
	 */
	private boolean isExistedInRelation(String did) {
		List<CategoryRelation> relations = null;
		CategoryRelation condition = new CategoryRelation();
		// source;
		condition.setSource(did);
		try {
		    
		    LOG.debug("调用sdk方法:getAllByExample");
		    
		    relations = categoryRelationRepository.getAllByExample(condition);
		} catch (EspStoreException e) {
			
		    LOG.error(LifeCircleErrorMessageMapper.CategoryCheckingError.getMessage(),e);
			
		    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getMessage());
		}
		if (relations != null && !relations.isEmpty()) {
			return true;
		}

		// target;
		condition.setSource(null); // 还原
		condition.setTarget(did);
		try {
		    
		    LOG.debug("调用sdk方法:getAllByExample");
		    
		    relations = categoryRelationRepository.getAllByExample(condition);
		} catch (EspStoreException e) {
			
		    LOG.error(LifeCircleErrorMessageMapper.CategoryCheckingError.getMessage(),e);
			
		    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getMessage());
		}
		if (relations != null && !relations.isEmpty()) {
			return true;
		}

		// levelParent
		condition.setTarget(null);// 还原
		try {
			condition.setLevelParent(loadCategoryDataById(did).getNdCode());
			
			LOG.debug("调用sdk方法:getAllByExample");
			
			relations = categoryRelationRepository.getAllByExample(condition);
		} catch (EspStoreException e) {
			
		    LOG.error(LifeCircleErrorMessageMapper.CategoryCheckingError.getMessage(),e);
			
		    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getMessage());
		}
		if (relations != null && !relations.isEmpty()) {
			return true;
		}

		return false;
	}

	/**
	 * 该模式是否存在维度数据关系
	 * 
	 * <br>Created 2015年5月4日 下午12:09:37
	 * @param patternName
	 * @return
	 * @author       linsm
	 */
	private boolean patternHasRelationData(String patternName) {
		List<CategoryRelation> relations = null;
		//patternName 就是patternPath的一部分
		CategoryRelation condition = new CategoryRelation();
		condition.setPatternPath(patternName);  //改成使用patternId 也可行，需要传入的是patternId
		try {
		    
		    LOG.debug("调用sdk方法:getAllByExample");
		    
		    relations = categoryRelationRepository.getAllByExample(condition);
		} catch (EspStoreException e) {
			
		    LOG.error(LifeCircleErrorMessageMapper.CategoryCheckingError.getMessage(),e);
			
		    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),e.getMessage());
		}
		if (relations != null && !relations.isEmpty()) {
			return true;
		}
		return false;
	}
	
	
	/**
	 * 以下是新增接口的实现 
	 */
	
	
	/**
	 * Ndcode批量查看分类维度
	 * 
	 * @param ndCodeSet
	 * @return
	 * @throws EspStoreException
	 * @since
	 */
    @Override
    public Map<String, CategoryModel> batchGetDetailCategory(Set<String> ndCodeSet) throws EspStoreException {
        Map<String, CategoryModel> modelMap = new HashMap<String, CategoryModel>();
        
        LOG.debug("调用sdk方法:getListWhereInCondition");
        
        List<Category> beanListResult = categoryRepository.getListWhereInCondition("ndCode",
                                                                                   new ArrayList<String>(ndCodeSet));
        if (beanListResult != null && !beanListResult.isEmpty()) {
            for (Category beanResult : beanListResult) {
                if (beanResult != null) {
                    modelMap.put(beanResult.getNdCode(), BeanMapperUtils.beanMapper(beanResult, CategoryModel.class));
                }
            }
        }
        return modelMap;
    }
    
    /**
     * 批量加载维度数据
     * 
     * @param ndCodeSet
     * @return
     * @throws EspStoreException
     * @since
     */
    @Override
    public Map<String, CategoryDataModel> batchGetDetailCategoryData(Set<String> ndCodeSet) throws EspStoreException {
        Map<String, CategoryDataModel> modelMap = new HashMap<String, CategoryDataModel>();
        
        LOG.debug("调用sdk方法:getListWhereInCondition");
        
        List<CategoryData> beanListResult = categoryDataRepository.getListWhereInCondition("ndCode",
                                                                                           new ArrayList<String>(ndCodeSet));
        if (beanListResult != null && !beanListResult.isEmpty()) {
            for (CategoryData beanResult : beanListResult) {
                if (beanResult != null) {
                    modelMap.put(beanResult.getNdCode(), changeCategoryDataFromBean(beanResult));
                }
            }
        }
        return modelMap;
    }
    
    
    /**
     * 批量加载分类维度应用模式
     * 
     * @param patternNameSet
     * @return
     * @throws EspStoreException
     * @since
     */
    @Override
    public Map<String, CategoryPatternModel> batchGetDetailCategoryPattern(Set<String> patternNameSet)throws EspStoreException{
        Map<String, CategoryPatternModel> modelMap = new HashMap<String,CategoryPatternModel>();
        
        LOG.debug("调用sdk方法:getListWhereInCondition");
        
        List<CategoryPattern> beanListResult = categoryPatternRepository.getListWhereInCondition("patternName",
                                                                                   new ArrayList<String>(patternNameSet));
        if (beanListResult != null && !beanListResult.isEmpty()) {
            for (CategoryPattern beanResult : beanListResult) {
                if (beanResult != null) {
                    modelMap.put(beanResult.getPatternName(), BeanMapperUtils.beanMapper(beanResult, CategoryPatternModel.class));
                }
            }
        }
        return modelMap;
    }
    
    /**
     * 批量创建分类维度应用模式下的维度数据关系
     * 
     * @param paramList
     * @return
     * @throws EspStoreException
     * @since
     */
    @Override
    public List<CategoryRelationModel> batchCreateCategoryRelation(List<CategoryRelationModel> paramList) {
        // logic check
        // 通过uuid 对应的资源是否存在：source,target,pattern, levelParent
        for (CategoryRelationModel categoryRelationModel : paramList) {
            if (categoryRelationModel != null) {
                //关系是否已存在：通过pattern_path 与target
                assertNotExistRelationByPatternPathAndTarget(categoryRelationModel.getPatternPath(), categoryRelationModel.getTarget().getIdentifier());
                // source
                assertCategoryDataExistById(categoryRelationModel.getSource().getIdentifier());
                // target
                assertCategoryDataExistById(categoryRelationModel.getTarget().getIdentifier());
                // levelParent
                assertCategoryDataExistByNdCode(categoryRelationModel.getLevelParent());
                // pattern
                assertCategoryPatternExistById(categoryRelationModel.getPattern().getIdentifier());

            }

        }

        // 入参转换
        List<CategoryRelation> beanList = new ArrayList<CategoryRelation>();
        for (CategoryRelationModel categoryRelationModel : paramList) {
            if (categoryRelationModel != null) {
                beanList.add(changeCategoryRelationToBean(categoryRelationModel));
            }
        }

        List<CategoryRelationModel> resultList = new ArrayList<CategoryRelationModel>();
        // 调用sdk
        List<CategoryRelation> beanListResult;
        try {
            
            LOG.debug("调用sdk方法:batchAdd");
            
            beanListResult = categoryRelationRepository.batchAdd(beanList);
        } catch (EspStoreException e) {
            
            LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(),e);
           
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                          e.getMessage());
        }
        // 结果处理
        if(beanListResult != null && !beanListResult.isEmpty()){
            for(CategoryRelation bean: beanListResult){
                
                LOG.debug("创建维度关系:{}",bean.getIdentifier());
                
                resultList.add(changeCategoryRelationFromBean(bean));
            }
        }
        return resultList;
    }

    /**
     * 批量删除分类维度应用模式下的维度数据关系
     * 
     * @param idSet
     * @return
     * @throws EspStoreException
     * @since
     */
    @Override
    public void batchRemoveCategoryRelation(LinkedHashSet<String> idSet){
        // 先断定是否存在：
        for (String id : idSet) {
            assertCategoryRelationById(id);
        }
        try {
            
            LOG.debug("调用sdk方法:batchDel");
            LOG.debug("创建维度关系:{}",idSet);
            
            categoryRelationRepository.batchDel(new ArrayList<String>(idSet));
        } catch (EspStoreException e) {
           
            LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(),e);
           
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                          e.getMessage());
        }
    }
    
    
    /**
     * v1.0 
     * 新增：修改分类维度应用模式下的维度数据关系
     */
    
    /**
     * 修改分类维度应用模式下的维度数据关系
     */
    @Override
    public CategoryRelationModel modifyCategoryRelation(CategoryRelationModel categoryRelationModel) throws EspStoreException {
        //判断是否存在，若不存在则抛出异常
        assertCategoryRelationById(categoryRelationModel.getIdentifier());
        CategoryRelationModel testModel = getRelationByPatternPathAndTarget(categoryRelationModel.getPatternPath(), categoryRelationModel.getTarget().getIdentifier());
        //判断是否与其它已存在的关系冲突
        if(testModel != null && !testModel.getIdentifier().equals(categoryRelationModel.getIdentifier())){
            
            LOG.error(LifeCircleErrorMessageMapper.CategoryRelationAlreadyExist.getMessage()); 
           
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.CategoryRelationAlreadyExist);
        }
        
        // logic check
        // 通过uuid 对应的资源是否存在：source,target,pattern, levelParent
        // source
        assertCategoryDataExistById(categoryRelationModel.getSource().getIdentifier());
        // target
        assertCategoryDataExistById(categoryRelationModel.getTarget().getIdentifier());
        // levelParent
        assertCategoryDataExistByNdCode(categoryRelationModel.getLevelParent());
        // pattern
        assertCategoryPatternExistById(categoryRelationModel.getPattern().getIdentifier());

        // 入参
        CategoryRelation bean = changeCategoryRelationToBean(categoryRelationModel);

        LOG.debug("调用sdk方法:update");
        LOG.debug("修改维度关系:{}",bean.getIdentifier());
        
        return changeCategoryRelationFromBean(categoryRelationRepository.update(bean));
    }
    
    /**
     * 辅助生成扩展的ndCode(维度数据)
     * @author linsm
     * @since 
     *
     */
    private enum NdCodeExtend {

        /**
         * 教学目标
         */
        INSTRUCTIONALOBJECTIVE("OTC", 4), 
        /**
         * 课时
         */
        LESSON("$C99", 2);
        
        /**
         * 可扩展位数
         */
        private int extendDigit;
        /**
         * 前缀
         */
        private String extendprefix;

        private NdCodeExtend(String extendprefix, int extendDigit) {
            this.extendDigit = extendDigit;
            this.extendprefix = extendprefix;
        }

        /**
         * 用于保存字符串与枚举的一一对应
         */
        private static final Map<String, NdCodeExtend> stringToEnum = new HashMap<String, NdCodeExtend>();
        static {
            for (NdCodeExtend value : values()) {
                stringToEnum.put(value.toString(), value);
            }
        }

        /**
         * 通过字符串获取 对应资源的枚举 <br>
         * Created 2015年5月13日 下午4:12:07
         * 
         * @param resourceType
         * @return ResourceTypeUploadAble
         * @author linsm
         */
        public static NdCodeExtend fromString(String resourceType) {
            return stringToEnum.get(resourceType);
        }

        /**
         * 转为对应的字符串值 <br>
         * Created 2015年5月13日 下午4:13:31
         * 
         * @return
         * @author linsm
         */
        @Override
        public String toString() {
            return extendprefix;
        }

        public String changeNdCode(long existedNum) {
            long num = existedNum;
            int index = 0;
            while (num != 0) {
                num /= 10;
                index++;
            }
            if (index > extendDigit) {
                
                LOG.error(LifeCircleErrorMessageMapper.CategoryDataExtendLimit.getMessage());
                
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              LifeCircleErrorMessageMapper.CategoryDataExtendLimit);
            }
            String extend = extendprefix;
            while (index < extendDigit) {
                extend += "0";// 补0
                index++;
            }
            
            if (existedNum != 0) {
                extend += existedNum;
            }
            
            return extend;
        }

    }
    
    // v1.2新增的接口
    
    /**
     * 指定了分类维度， 父结点，获取最大ndCode
     * 若不存在对应的数据，则返回空字符串
     * @param categoryId
     * @param parentId
     * @param otherNdCode 去掉预留的ndCode
     * @return
     * @since
     */
    public String getMaxLevelNdCodeExcludeOther(String categoryId, String parentId, String otherNdCode) {
        CategoryData bean = new CategoryData();
        bean.setCategory(categoryId);
        bean.setParent(parentId);

        Pageable pageable = new PageRequest(0, 2, Direction.DESC, "ndCode");  //按ndCode字段降序排序
        try {
            
            LOG.debug("调用sdk方法:getPageByExample");
            
            Page<CategoryData> page = categoryDataRepository.getPageByExample(bean, pageable);
            if (page != null) {
                List<CategoryData> datas = page.getContent();
                if (CollectionUtils.isNotEmpty(datas)) {
//                    return datas.get(0).getNdCode();  //取得最大值
                    if(datas.get(0).getNdCode().equals(otherNdCode)){
                        if(datas.size()>1){
                            return datas.get(1).getNdCode();  //exclude other ndCode 
                        }
                    }else{
                        return datas.get(0).getNdCode();
                    }
                }
            }
        } catch (EspStoreException e) {
            
            LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(),e);
           
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                          e.getMessage());
        }
        return "";
    }
    
    @Override
	public QueryRelationAllViewModel queryCategoryRelationAll(
			String levelParent, boolean enable, String patternPath)
			throws EspStoreException {
		QueryRelationAllViewModel result = new QueryRelationAllViewModel();
		// patternPath 不可能为空，至少是patternName
		if (StringUtils.isEmpty(patternPath)) {
			return result;
		}
		// 没有限制
		if ("ROOT".equals(levelParent)) {
			levelParent = "";
		}

		if (patternPath.endsWith("/")) {
			patternPath = patternPath.substring(0, patternPath.length() - 1);
		}

		LOG.debug("查询维度数据关系路径(patternPath):" + patternPath);

		// 设置条件，即调用sdk 的参数
		// CategoryRelation condition = new CategoryRelation();
		// condition.setPatternPath(patternPath);
		// condition.setEnable(enable);

		List<Item<? extends Object>> items = new ArrayList<>();
		Item<Boolean> enableItem = new Item<Boolean>();
		enableItem.setKey("enable");
		enableItem.setComparsionOperator(ComparsionOperator.EQ);
		enableItem.setLogicalOperator(LogicalOperator.AND);
		enableItem.setValue(ValueUtils.newValue(enable));
		items.add(enableItem);

		Item<String> patternPathItem = new Item<String>();
		patternPathItem.setKey("patternPath");
		patternPathItem.setComparsionOperator(ComparsionOperator.LIKE);
		patternPathItem.setLogicalOperator(LogicalOperator.AND);
		patternPathItem.setValue(ValueUtils.newValue(patternPath + "%"));
		items.add(patternPathItem);

		LOG.debug("调用sdk方法:findByItems");

		List<CategoryRelation> relations = categoryRelationRepository
				.findByItems(items);
		if (relations == null || relations.isEmpty()) {
			return result;
		}

		Set<String> categoryDataIdSet = new HashSet<String>();

		Map<String, List<CategoryRelation>> groupByLevelFirstAndPatternPathMap = new HashMap<String, List<CategoryRelation>>();
		for (CategoryRelation categoryRelation : relations) {
			if (StringUtils.isNotEmpty(levelParent)) {
				if (patternPath.equals(categoryRelation.getPatternPath())
						&& !levelParent.equals(categoryRelation
								.getLevelParent())) {
					continue;
				}
			}
			categoryDataIdSet.add(categoryRelation.getTarget());

			if ("ROOT".equals(categoryRelation.getLevelParent())) {
				putMap(groupByLevelFirstAndPatternPathMap,
						categoryRelation.getPatternPath(), categoryRelation);
			} else {
				putMap(groupByLevelFirstAndPatternPathMap,
						createKey(categoryRelation), categoryRelation);
			}
		}
		LOG.debug("调用sdk方法:getAll");
		if (categoryDataIdSet.isEmpty()) {
			return result;
		}
		List<CategoryData> targetCategoryDatas = categoryDataRepository
				.getAll(new ArrayList<String>(categoryDataIdSet));
		if (targetCategoryDatas.isEmpty()) {
			return result;
		}
		Map<String, CategoryData> targetCategoryDataIdMap = new HashMap<String, CategoryData>();
		for (CategoryData categoryData : targetCategoryDatas) {
			targetCategoryDataIdMap.put(categoryData.getIdentifier(),
					categoryData);
		}

		String topLevelKey = "";
		if (StringUtils.isEmpty(levelParent)) {
			topLevelKey = patternPath;
		} else {
			topLevelKey = createKey(patternPath, levelParent);
		}
		if (!groupByLevelFirstAndPatternPathMap.containsKey(topLevelKey)) {
			return result;
		}
		result.setItems(changeRelationToReltionViewModel(
				groupByLevelFirstAndPatternPathMap.get(topLevelKey),
				targetCategoryDataIdMap));
		if (result.getItems().isEmpty()) {
			return result;
		}

		for (RelationAllViewModel relationAllViewModel : result.getItems()) {
			dealWithTree(relationAllViewModel,
					groupByLevelFirstAndPatternPathMap, targetCategoryDataIdMap);
		}

		return result;
	}

	private void dealWithTree(
			RelationAllViewModel relationAllViewModel,
			Map<String, List<CategoryRelation>> groupByLevelFirstAndPatternPathMap,
			Map<String, CategoryData> targetCategoryDataIdMap) {
		if (groupByLevelFirstAndPatternPathMap
				.containsKey(createKey(relationAllViewModel))) {
			relationAllViewModel.setItems(changeRelationToReltionViewModel(
					groupByLevelFirstAndPatternPathMap
							.get(createKey(relationAllViewModel)),
					targetCategoryDataIdMap));
		} else if (groupByLevelFirstAndPatternPathMap
				.containsKey(createPatternPathKey(relationAllViewModel))) {
			relationAllViewModel.setItems(changeRelationToReltionViewModel(
					groupByLevelFirstAndPatternPathMap
							.get(createPatternPathKey(relationAllViewModel)),
					targetCategoryDataIdMap));
		} else {
			return;
		}
		if (relationAllViewModel.getItems().isEmpty()) {
			return;
		}
		for (RelationAllViewModel relationAllViewModelFor : relationAllViewModel
				.getItems()) {
			dealWithTree(relationAllViewModelFor,
					groupByLevelFirstAndPatternPathMap, targetCategoryDataIdMap);
		}
	}
	
	private String createPatternPathKey(
			RelationAllViewModel relationAllViewModel) {
		if (relationAllViewModel.getTarget() == null) {
			LOG.info("category relation id:{},target data not existed",
					relationAllViewModel.getIdentifier());
			return createPatternPathKey(relationAllViewModel.getPatternPath(),
					NO_EXISTED_NDCODE);
		}
		return createPatternPathKey(relationAllViewModel.getPatternPath(),
				relationAllViewModel.getTarget().getNdCode());
	}

	private String createPatternPathKey(String patternPath, String targetNdcoe) {
		return patternPath + "/" + targetNdcoe;
	}

	private String createKey(String patternPath, String levelParent) {
		return patternPath + "#" + levelParent;
	}

	private String createKey(CategoryRelation categoryRelation) {
		return createKey(categoryRelation.getPatternPath(),
				categoryRelation.getLevelParent());
	}

	private String createKey(RelationAllViewModel relationAllViewModel) {
		if (relationAllViewModel.getTarget() == null) {
			LOG.info("category relation id:{},target data not existed",
					relationAllViewModel.getIdentifier());
			return createKey(relationAllViewModel.getPatternPath(),
					NO_EXISTED_NDCODE);
		}
		return createKey(relationAllViewModel.getPatternPath(),
				relationAllViewModel.getTarget().getNdCode());
	}

	private List<RelationAllViewModel> changeRelationToReltionViewModel(
			List<CategoryRelation> list,
			Map<String, CategoryData> targetCategoryDataIdMap) {
		List<RelationAllViewModel> result = new ArrayList<RelationAllViewModel>();
		for (CategoryRelation categoryRelation : list) {
			RelationAllViewModel oneItem = new RelationAllViewModel();
			oneItem = BeanMapperUtils.beanMapper(categoryRelation,
					RelationAllViewModel.class);
			oneItem.setTarget(beanMapper(
					targetCategoryDataIdMap.get(categoryRelation.getTarget()),
					TargetViewModel.class));
			result.add(oneItem);
		}
		sortByOrderNumForAll(result);
		return result;
	}
	
	
	private static <T> T beanMapper(Object source,Class<T> target){
		if(source ==null){
			return null;
		}
		return BeanMapperUtils.beanMapper(source, target);
	}
	private void putMap(
			Map<String, List<CategoryRelation>> groupByLevelFirstAndPatternPathMap,
			String key, CategoryRelation value) {
		List<CategoryRelation> values = groupByLevelFirstAndPatternPathMap
				.get(key);
		if (values == null) {
			values = new ArrayList<CategoryRelation>();
			groupByLevelFirstAndPatternPathMap.put(key, values);
		}
		values.add(value);

	}
    
}