package nd.esp.service.lifecycle.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.Valid;

import nd.esp.service.lifecycle.models.CategoryDataModel;
import nd.esp.service.lifecycle.models.CategoryModel;
import nd.esp.service.lifecycle.models.CategoryPatternModel;
import nd.esp.service.lifecycle.models.CategoryRelationModel;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.services.CategoryService;
import nd.esp.service.lifecycle.services.notify.NotifyReportService;
import nd.esp.service.lifecycle.services.staticdatas.StaticDataService;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.UpdateStaticDataTask;
import nd.esp.service.lifecycle.support.busi.ValidResultHelper;
import nd.esp.service.lifecycle.utils.BeanMapperUtils;
import nd.esp.service.lifecycle.utils.MessageConvertUtil;
import nd.esp.service.lifecycle.utils.ParamCheckUtil;
import nd.esp.service.lifecycle.utils.category.NdCodePattern;
import nd.esp.service.lifecycle.vos.CategoryDataViewModel;
import nd.esp.service.lifecycle.vos.CategoryPatternViewModel;
import nd.esp.service.lifecycle.vos.CategoryRelationViewModel;
import nd.esp.service.lifecycle.vos.CategoryViewModel;
import nd.esp.service.lifecycle.vos.ListViewModel;
import nd.esp.service.lifecycle.vos.QueryRelationViewModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;






/**
 * FIXME lsm:categorys ->categories (url路径中),暂时两种都先兼容，后期再把categorys 去除
 * 
 * 分类体系的对外Rest Controller 1、分类维度的增加，包括schema的创建 2、分类维度的修改 3、分类维度的删除 4、分类维度的查询
 * 5、分类维度节点数据的增加 6、分类维度节点数据的删除 7、分类维度节点数据的修改 8、分类维度节点数据的查询 9、分类维度节点关系的增加
 * 10、分类维度节点关系的删除
 * 
 * 暂且提供的分类类型有： 1、学科分类 2、适用对象分类 3、教材版本和章节目录的分类
 * 
 * @author johnny
 * @version 1.0
 * @created 20-4月-2015 15:26:21
 */
@RestController
@RequestMapping(value={"/v0.6"})
public class CategoryController {

	
    private static final Logger LOG = LoggerFactory.getLogger(CategoryController.class);
	
    @Autowired
	@Qualifier("CategoryServiceImpl")
	private CategoryService categoryService;
    
    @Autowired
    private StaticDataService staticDataService;
    
    @Autowired
    private NotifyReportService nrs;
	
	//UUID格式
	//这个用于验证uuid的正则表达式存在一定的问题（不够严格）
    private final static String uuidReg = "[a-z0-9]{8}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{12}";

	/**
	 * 添加一个分类维度：分类维度的添加，需要明确其目的以及对于分类维度的schema的描述，这个模式的描述一般都是树形结构，描述其约束关系
	 * 
	 * @URLPattern /categorys
	 * @Method POST
	 * 
	 * @param category
	 */
	@RequestMapping(value = {"/categorys","/categories"}, method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
	public @ResponseBody CategoryViewModel requestAddCategory(
			@Valid @RequestBody CategoryViewModel categoryViewModel,BindingResult bindingResult) {
		// 入参校验
		ValidResultHelper.valid(bindingResult, LifeCircleErrorMessageMapper.InvalidArgumentsError.getCode());
		//校验ndCode
		checkCategoryNdCode(categoryViewModel.getNdCode());
		
		// 生成参数
		CategoryModel paramModel = BeanMapperUtils.beanMapper(categoryViewModel,
				CategoryModel.class);
		paramModel.setIdentifier(UUID.randomUUID().toString());// 设置UUID;

		// 调用service 接口
		CategoryModel resultModel = null;
		try {
			resultModel = categoryService.creatCategory(paramModel);
		} catch (EspStoreException e) {
		    
		    LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(),e);
		    
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                          e.getMessage());
		}

		// 转成出参
		CategoryViewModel resultViewModel = BeanMapperUtils.beanMapper(resultModel,
				CategoryViewModel.class);

		//同步推送至报表系统
		nrs.addCategory(resultModel);
		return resultViewModel;
	}

	/**
     * 检查categoryNdCode 是不是符合规范且已经备案，若无，则抛出异常
     * @param ndCode
     * @since 
     */
    private void checkCategoryNdCode(String ndCode) {
        // 有点没必要了正则校验了，现在都需要通过配置很把控，改成只有配置好了，才允许创建
        // ndCode正则校验
        boolean ndCodeFlag = checkNdCodeRegex(ndCode);
        if (!ndCodeFlag) {
            
            LOG.error(LifeCircleErrorMessageMapper.CheckNdCodeRegex.getMessage()+ndCode);
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.CheckNdCodeRegex);
        }
        NdCodePattern ndCodePattern = NdCodePattern.fromString(ndCode);
        if (ndCodePattern == null) {
            // 不允许创建该分类维度，请到LC备案
            
            LOG.error("分类维度nd_code="+ndCode+" 还没有备案，有需要请与LC沟通");
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.CheckNdCodeRegex.getCode(),
                                          "分类维度nd_code="+ndCode+" 还没有备案，有需要请与LC沟通");
        }
    }

    /**
	 * 修改维度分类，主要修改维度分类的名称，缩写，介绍以及schema等信息
	 * 
	 * @URLPattern /categorys/{id}
	 * @Method PUT
	 * 
	 * @param id
	 *            唯一标识
	 * @param category
	 *            修改后的入参数据
	 */
	@RequestMapping(value = {"/categorys/{cid}", "/categories/{cid}"}, method = RequestMethod.PUT, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
	public @ResponseBody CategoryViewModel requestModifyCategory(
			@PathVariable String cid,
			@Valid @RequestBody CategoryViewModel categoryViewModel,BindingResult bindingResult) {
		// 入参校验
		ValidResultHelper.valid(bindingResult, LifeCircleErrorMessageMapper.InvalidArgumentsError.getCode());
		//ndCode正则校验
		checkCategoryNdCode(categoryViewModel.getNdCode());
		
		categoryViewModel.setIdentifier(cid);
		CategoryModel modifyModel = BeanMapperUtils.beanMapper(categoryViewModel,
				CategoryModel.class);

		CategoryModel resultModel = null;
		// 调用service接口：
		try {
			resultModel = categoryService.modifyCategory(modifyModel);
		} catch (EspStoreException e) {
		    
			LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(),e);
			
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                          e.getMessage());
		}

		// 转成出参：
		CategoryViewModel resultViewModel = BeanMapperUtils.beanMapper(resultModel,
				CategoryViewModel.class);
		//同步推送至报表系统
		nrs.updateCategory(resultModel);
		return resultViewModel;
	}

	/**
	 * 查询分类信息，可以根据名称或者介绍进行查询分类维度。
	 * 
	 * @URLPattern /categorys?{words=123}&limit=(0,20)}
	 * @Method GET
	 * 
	 * @param words
	 * @param limit
	 */
	@RequestMapping(value = {"/categorys","/categories"}, method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public @ResponseBody ListViewModel<CategoryViewModel> requestQueryCategory(
			@RequestParam(value ="words",required = true) String words, @RequestParam(value ="limit",required =true) String limit) {
		// 检查limit参数
		ParamCheckUtil.checkLimit(limit);// 有抛出异常
		// 调用service 接口
		ListViewModel<CategoryModel> modelListResult = null;
		try {
			modelListResult = categoryService.queryCategory(words, limit);
		} catch (EspStoreException e) {
		    
			LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(),e);
			
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                          e.getMessage());
		}

		// 结果转换 ，数据转化，有没有更好的方式， 内部泛型数组，使用 ModelMapper 需要一个个转。
		ListViewModel<CategoryViewModel> viewListResult = new ListViewModel<CategoryViewModel>();
		viewListResult.setLimit(modelListResult.getLimit());
		viewListResult.setTotal(modelListResult.getTotal());
		List<CategoryModel> modelItems = modelListResult.getItems();
		List<CategoryViewModel> viewItems = new ArrayList<CategoryViewModel>();
		if (modelItems != null && !modelItems.isEmpty()) {
			for (CategoryModel model : modelItems) {
				CategoryViewModel viewModel = BeanMapperUtils.beanMapper(model,
						CategoryViewModel.class);
				viewItems.add(viewModel);
			}
		}
		viewListResult.setItems(viewItems);
		return viewListResult;
	}

	/**
	 * 删除一个维度，这个维度删除的时候，需要将维度所关联的维度数据一并删除。
	 * 
	 * @URLPattern /categorys/{id}
	 * @Method DELETE
	 * 
	 * @param id
	 *            分类维度的唯一标识
	 */
	@RequestMapping(value = {"/categorys/{cid}","/categories/{cid}"}, method = RequestMethod.DELETE, produces = { MediaType.APPLICATION_JSON_VALUE })
	public @ResponseBody Map<String, String> requestRemoveCategory(
			@PathVariable String cid) {
		// 调用service 接口：
		try {
		    categoryService.removeCategory(cid);
		} catch (EspStoreException e) {
		    
			LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(),e);
			
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                          e.getMessage());
		}
		
		//同步推送至报表系统
		nrs.deleteCategory(cid);
		
		return MessageConvertUtil
				.getMessageString(LifeCircleErrorMessageMapper.DeleteCategorySuccess); 
	}

	/**
	 * 增加维度分类数据
	 * 
	 * @URLPattern /categorys/{cid}/datas
	 * @Method POST
	 * 
	 * @param categoryData
	 *            维度数据入参结构
	 */
	@RequestMapping(value = {"/categorys/datas","/categories/datas"}, method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
	public @ResponseBody CategoryDataViewModel requestAddCategoryData(
			@Valid @RequestBody CategoryDataViewModel categoryDataViewModel,BindingResult bindingResult) {
		// 入参校验
		ValidResultHelper.valid(bindingResult, LifeCircleErrorMessageMapper.InvalidArgumentsError.getCode());

		
		// 生成参数
		CategoryDataModel paramModel = changeCategoryDataFromView(categoryDataViewModel);
		paramModel.setIdentifier(UUID.randomUUID().toString());// 设置UUID;
		

		// 调用service 接口
		CategoryDataModel resultModel = null;
		try {
			resultModel = categoryService.createCategoryData(paramModel);
		} catch (EspStoreException e) {
		    
			LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(),e);
			
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                          e.getMessage());
		}

		// 转成出参
		CategoryDataViewModel resultViewModel = changeCategoryDataToView(resultModel);
		
		//同步推送至报表系统
		nrs.addCategoryData(resultModel);
		return resultViewModel;
	}

	/**
	 * 将维度数据转换成view类型
	 * 
	 * <br>Created 2015年5月4日 下午12:13:03
	 * @param resultModel
	 * @return
	 * @author       linsm
	 */
	private CategoryDataViewModel changeCategoryDataToView(
			CategoryDataModel resultModel) {
	    if(resultModel == null){
	        return null;
	    }
		CategoryDataViewModel resultViewModel = BeanMapperUtils.beanMapper(
				resultModel, CategoryDataViewModel.class);
		resultViewModel.setCategory(resultModel.getCategory().getIdentifier());
		resultViewModel.setParent(resultModel.getParent().getIdentifier());
		return resultViewModel;
	}

	/**
	 * 将维度数据转换成生命周期类型
	 * 
	 * <br>Created 2015年5月4日 下午12:13:41
	 * @param categoryDataViewModel
	 * @return
	 * @author       linsm
	 */
	private CategoryDataModel changeCategoryDataFromView(
			CategoryDataViewModel categoryDataViewModel) {
	    if(categoryDataViewModel == null){
	        return null;
	    }
		CategoryDataModel paramModel = BeanMapperUtils.beanMapper(
				categoryDataViewModel, CategoryDataModel.class);
		CategoryModel category = new CategoryModel();
		category.setIdentifier(categoryDataViewModel.getCategory());
		paramModel.setCategory(category);
		CategoryDataModel parent = new CategoryDataModel();
		parent.setIdentifier(categoryDataViewModel.getParent());
		paramModel.setParent(parent);

		return paramModel;
	}

	/**
	 * 修改分类数据信息
	 * 
	 * @URLPattern /categorys/datas/{did}
	 * @Method PUT
	 * 
	 * @param did
	 *            维度数据的标识id
	 * @param cid
	 *            分类的维度标识
	 * @param categoryData
	 */
	@RequestMapping(value = {"/categorys/datas/{did}","/categories/datas/{did}"}, method = RequestMethod.PUT, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
	public @ResponseBody CategoryDataViewModel requestModifyCategoryData( @PathVariable String did,
			@Valid @RequestBody CategoryDataViewModel categoryDataViewModel,BindingResult bindingResult) {
		// 入参校验
		ValidResultHelper.valid(bindingResult, LifeCircleErrorMessageMapper.InvalidArgumentsError.getCode());

		categoryDataViewModel.setIdentifier(did);
		CategoryDataModel modifyModel = changeCategoryDataFromView(categoryDataViewModel);
		
		CategoryDataModel resultModel = null;
		// 调用service接口：
		try {
			resultModel = categoryService.modifyCategoryData(modifyModel);
		} catch (EspStoreException e) {
		    
			LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(),e);
			
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                          e.getMessage());
		}

		// 转成出参：
		CategoryDataViewModel resultViewModel = changeCategoryDataToView(resultModel);
		
		//同步推送至报表系统
		nrs.updateCategoryData(resultModel);
		return resultViewModel;
	}

	/**
	 * 删除分类维度数据信息
	 * 
	 * @URLPattern /categorys/{cid}/datas/{id}
	 * @Method DELETE
	 * 
	 * @param cid
	 *            分类维度的标识
	 * @param did
	 */
	@RequestMapping(value = {"/categorys/datas/{did}","/categories/datas/{did}"}, method = RequestMethod.DELETE, produces = { MediaType.APPLICATION_JSON_VALUE })
	public @ResponseBody Map<String, String> requestRemoveCategoryData(
			@PathVariable String did) {
		// 调用service 接口：
		try {
		    categoryService.removeCategoryData(did);
		} catch (EspStoreException e) {
		    
			LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(),e);
			
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                          e.getMessage());
		}
		//同步推送至报表系统
		nrs.deleteCategoryData(did);
		
		return MessageConvertUtil
				.getMessageString(LifeCircleErrorMessageMapper.DeleteCategoryDataSuccess); 
	}

	/**
	 * 查询分类信息。分类信息的查询是根据关联的树形结构进行查询，根据等级进行查询 根据上级节点类型和名称模糊匹配下级节点内容
	 * 
	 * @URLPattern 
	 *             /categorys/{cid}/datas?{words=123}&{parent=988}&{limit
	 *             =(0,20)}
	 * @Method GET
	 * 
	 *         需要通过parentcode解析维度类别和定位上级节点，在通过words进行模糊匹配节点数据
	 * 
	 * @param limit
	 *            分页参数，默认每页查询20条记录
	 * @param nd_code
	 *            分类维度的nd_code
	 * @param words
	 * @param parent
	 *            父节点的id，为ROOT的时候，表示当前分类维度下的根节点
	 */
	@RequestMapping(value = {"/categorys/{nd_code}/datas","/categories/{nd_code}/datas"}, method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public @ResponseBody ListViewModel<CategoryDataViewModel> requestQueryCategoryData(
			@PathVariable("nd_code") String ndCode,
			@RequestParam(value = "parent", required = false) String parent,
			@RequestParam(value = "limit", required = true) String limit,
			@RequestParam(value = "words", required = true) String words) {

		// 校验入参:cid
		// 检查limit参数
		ParamCheckUtil.checkLimit(limit);// 有抛出异常
		// 调用service 接口
		ListViewModel<CategoryDataModel> modelListResult = null;
		try {
			modelListResult = categoryService.queryCategoryData(ndCode, true, parent,
					words, limit);  //暂时为了兼容其他地方 （如肖源导入 教材）不改变service 接口，all 可以随便赋值（在实现中不起作用）
		} catch (EspStoreException e) {
		    
			LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(),e);
			
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                          e.getMessage());
		}

		// 结果转换 ，数据转化，有没有更好的方式， 内部泛型数组，使用 ModelMapper 需要一个个转。
		ListViewModel<CategoryDataViewModel> viewListResult = new ListViewModel<CategoryDataViewModel>();
		viewListResult.setLimit(modelListResult.getLimit());
		viewListResult.setTotal(modelListResult.getTotal());
		List<CategoryDataModel> modelItems = modelListResult.getItems();
		List<CategoryDataViewModel> viewItems = new ArrayList<CategoryDataViewModel>();
		if (modelItems != null && !modelItems.isEmpty()) {
			for (CategoryDataModel model : modelItems) {
				CategoryDataViewModel viewModel = changeCategoryDataToView(model);
				viewItems.add(viewModel);
			}
		}
		viewListResult.setItems(viewItems);
		return viewListResult;
	}

	/**
	 * 增加维度应用模式
	 * 
	 * @URLPattern /categorypatterns
	 * @Method POST
	 * 
	 * @param pattern
	 *            分类维度应用模式的入参结构
	 */
	@RequestMapping(value = "/categorypatterns", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
	public @ResponseBody CategoryPatternViewModel requestAddCategoryPattern(
			@Valid @RequestBody CategoryPatternViewModel categoryPatternViewModel,BindingResult bindingResult) {
		// 入参校验
		ValidResultHelper.valid(bindingResult, LifeCircleErrorMessageMapper.InvalidArgumentsError.getCode());
	
		// 生成参数
		CategoryPatternModel paramModel = BeanMapperUtils.beanMapper(
				categoryPatternViewModel, CategoryPatternModel.class);
		paramModel.setIdentifier(UUID.randomUUID().toString());// 设置UUID;

		// 调用service 接口
		CategoryPatternModel resultModel = null;
		try {
			resultModel = categoryService.creatCategoryPattern(paramModel);
		} catch (EspStoreException e) {
		    
			LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(),e);
			
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                          e.getMessage());
		}

		// 转成出参
		CategoryPatternViewModel resultViewModel = BeanMapperUtils.beanMapper(
				resultModel, CategoryPatternViewModel.class);

		staticDataService.updateLastTime(UpdateStaticDataTask.CP_TASK_ID);
		staticDataService.updateCPMapNow();
		return resultViewModel;
	}

	/**
	 * 修改维度应用模式
	 * 
	 * @URLPattern /categorypatterns/{cpid}
	 * @Method POST
	 * 
	 * @param pattern
	 * @param id
	 *            应用模式的标识id
	 */
	@RequestMapping(value = "/categorypatterns/{cpid}", method = RequestMethod.PUT, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
	public @ResponseBody CategoryPatternViewModel requestModifyCategoryPattern(
			@PathVariable String cpid,
			@Valid @RequestBody CategoryPatternViewModel categoryPatternViewModel,BindingResult bindingResult) {
		// 入参校验
		ValidResultHelper.valid(bindingResult, LifeCircleErrorMessageMapper.InvalidArgumentsError.getCode());
		
		categoryPatternViewModel.setIdentifier(cpid);
		CategoryPatternModel modifyModel = BeanMapperUtils.beanMapper(
				categoryPatternViewModel, CategoryPatternModel.class);

		CategoryPatternModel resultModel = null;
		// 调用service接口：
		try {
			resultModel = categoryService.modifyCategoryPattern(modifyModel);
		} catch (EspStoreException e) {
			
		    LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(),e);
			
		    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                          e.getMessage());
		}

		// 转成出参：
		CategoryPatternViewModel resultViewModel = BeanMapperUtils.beanMapper(
				resultModel, CategoryPatternViewModel.class);
		staticDataService.updateLastTime(UpdateStaticDataTask.CP_TASK_ID);
		staticDataService.updateCPMapNow();
		return resultViewModel;
	}

	/**
	 * 查询维度分类的应用模式
	 * 
	 * @URLPattern /categorypatterns?words=123&limit=(0,10)
	 * @Method GET
	 * 
	 * @param limit
	 *            分页参数
	 * @param words
	 *            查询词
	 */
	@RequestMapping(value = "/categorypatterns",  method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public @ResponseBody ListViewModel<CategoryPatternViewModel> requestQueryCategoryPatterns(
			@RequestParam(value = "limit", required = true) String limit,
			@RequestParam(value = "words", required = true) String words) {
		// 检查limit参数
		ParamCheckUtil.checkLimit(limit);// 有抛出异常
		// 调用service 接口
		ListViewModel<CategoryPatternModel> modelListResult = null;
		try {
			modelListResult = categoryService.queryCategoryPatterns(words, limit);
		} catch (EspStoreException e) {
			
		    LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(),e);
			
		    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                          e.getMessage());
		}

		// 结果转换 ，数据转化，有没有更好的方式， 内部泛型数组，使用 ModelMapper 需要一个个转。
		ListViewModel<CategoryPatternViewModel> viewListResult = new ListViewModel<CategoryPatternViewModel>();
		if(modelListResult == null ||modelListResult.getItems().isEmpty()){
			viewListResult.setLimit(limit);
			viewListResult.setTotal(0L);
			viewListResult.setItems(new ArrayList<CategoryPatternViewModel>());
			return viewListResult;
		}
		viewListResult.setLimit(modelListResult.getLimit());
		viewListResult.setTotal(modelListResult.getTotal());
		List<CategoryPatternModel> modelItems = modelListResult.getItems();
		List<CategoryPatternViewModel> viewItems = null;
		if (modelItems != null && !modelItems.isEmpty()) {
			viewItems = new ArrayList<CategoryPatternViewModel>();
			for (CategoryPatternModel model : modelItems) {
				CategoryPatternViewModel viewModel = BeanMapperUtils.beanMapper(
						model, CategoryPatternViewModel.class);
				viewItems.add(viewModel);
			}
		}
		viewListResult.setItems(viewItems);
		return viewListResult;
	}

	/**
	 * 删除维度分类应用模式
	 * 
	 * @URLPattern /categorypatterns/{cpid}
	 * @Method DELETE
	 * 
	 * @param cpid
	 *            应用模式的id。需要注意的是，当删除应用规模的时候，与其相关的UI界面的参数需要进行调整，并且需要判断当前模式是否存在，
	 *            给出异常提示。
	 */
	@RequestMapping(value = "/categorypatterns/{cpid}", method = RequestMethod.DELETE, produces = { MediaType.APPLICATION_JSON_VALUE })
	public @ResponseBody Map<String, String> requestRemoveCategoryPattern(
			@PathVariable String cpid) {
		// 调用service 接口：
		try {
		    categoryService.removeCategoryPattern(cpid);
		} catch (EspStoreException e) {
			
		    LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(),e);
			
		    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                          e.getMessage());
		}
		staticDataService.updateLastTime(UpdateStaticDataTask.CP_TASK_ID);
		staticDataService.updateCPMapNow();
		return MessageConvertUtil
				.getMessageString(LifeCircleErrorMessageMapper.DeleteCategoryPatternSuccess); 
	}

	/**
	 * 增加维度数据和事实数据的关联关系
	 * 
	 * @URLPattern /categorypatterns/datas/relations
	 * @Method POST
	 * 
	 * @param relationData
	 */
	@RequestMapping(value = "/categorypatterns/datas/relations", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
	public @ResponseBody CategoryRelationViewModel requestAddRelationData(
			@Valid @RequestBody CategoryRelationViewModel categoryRelationViewModel,BindingResult bindingResult) {
		// 入参校验
		ValidResultHelper.valid(bindingResult, LifeCircleErrorMessageMapper.InvalidArgumentsError.getCode());
		

		// 生成参数
		CategoryRelationModel paramModel = changeCategoryRelationFromView(categoryRelationViewModel);
		paramModel.setIdentifier(UUID.randomUUID().toString());// 设置UUID;
		// 调用service 接口
		CategoryRelationModel resultModel = null;
		try {
			resultModel = categoryService.createCategoryRelation(paramModel);
		} catch (EspStoreException e) {
			
		    LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(),e);
			
		    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                          e.getMessage());
		}

		// 转成出参
		CategoryRelationViewModel resultViewModel = changeCategoryRelationToView(resultModel);
		return resultViewModel;
	}

	/**
	 * 维度数据关系转换为view类型
	 * 
	 * <br>Created 2015年5月4日 下午1:35:57
	 * @param resultModel
	 * @return
	 * @author       linsm
	 */
	private CategoryRelationViewModel changeCategoryRelationToView(
			CategoryRelationModel resultModel) {
	    if(resultModel == null){
	        return null;
	    }
		CategoryRelationViewModel resultViewModel = new CategoryRelationViewModel();
		resultViewModel.setIdentifier(resultModel.getIdentifier());
		resultViewModel.setRelationType(resultModel.getRelationType());
		resultViewModel.setTags(resultModel.getTags());
		resultViewModel.setOrderNum(resultModel.getOrderNum());
		resultViewModel.setEnable(resultModel.isEnable());
		resultViewModel.setPatternPath(resultModel.getPatternPath());
		resultViewModel.setLevelParent(resultModel.getLevelParent());
		resultViewModel.setPattern(resultModel.getPattern().getIdentifier());
		resultViewModel.setSource(resultModel.getSource().getIdentifier());
		resultViewModel.setTarget(resultModel.getTarget().getIdentifier());
		return resultViewModel;
	}

	/**
	 * 维度数据关系转换为生命周期类型
	 * 
	 * <br>Created 2015年5月4日 下午1:36:39
	 * @param categoryRelationViewModel
	 * @return
	 * @author       linsm
	 */
	private CategoryRelationModel changeCategoryRelationFromView(
			CategoryRelationViewModel categoryRelationViewModel) {
	    if(categoryRelationViewModel == null){
	        return  null;
	    }
		CategoryRelationModel paramModel = BeanMapperUtils.beanMapper(
				categoryRelationViewModel, CategoryRelationModel.class);
		// pattern
		CategoryPatternModel pattern = new CategoryPatternModel();
		pattern.setIdentifier(categoryRelationViewModel.getPattern());
		paramModel.setPattern(pattern);
		// source
		CategoryDataModel source = new CategoryDataModel();
		source.setIdentifier(categoryRelationViewModel.getSource());
		paramModel.setSource(source);
		// target
		CategoryDataModel target = new CategoryDataModel();
		target.setIdentifier(categoryRelationViewModel.getTarget());
		paramModel.setTarget(target);
		return paramModel;
	}

	/**
	 * 删除事实数据和维度数据的关联关系
	 * 
	 * @URLPattern /categorypatterns/datas/relations/{cprid}
	 * @Method DELETE
	 * 
	 * @param cprid
	 *            维度关系标识
	 */
	@RequestMapping(value = "/categorypatterns/datas/relations/{cprid}", method = RequestMethod.DELETE, produces = { MediaType.APPLICATION_JSON_VALUE })
	public @ResponseBody Map<String, String> requestRemoveRelationData(
			@PathVariable String cprid) {
		// 调用service 接口：
		try {
//			isDeleteSuccess = 
		    categoryService.removeCategoryRelation(cprid);
		} catch (EspStoreException e) {
			
		    LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(),e);
			
		    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                          e.getMessage());
		}
		return MessageConvertUtil
				.getMessageString(LifeCircleErrorMessageMapper.DeleteCategoryRelationSuccess); 
	}

	/**
	 * 查询分类维度之间的关系
	 * 
	 * @URLPattern 
	 *             /categorys/relations/datas?{enable=true}&{patternPath=K12}&{levelParent=ndCode}
	 * @Method GET
	 * 
	 * @param levelParent
	 *            同一个级别下的父类ndCode
	 * @param enable
	 *            是否是可用的
	 * @param patternPath
	 *            模式的路径
	 */
	@RequestMapping(value = {"/categorys/relations","/categories/relations"}, method = RequestMethod.GET,produces = { MediaType.APPLICATION_JSON_VALUE })
	public @ResponseBody List<QueryRelationViewModel> requestQueryRelationData(
			@RequestParam(value = "patternPath",required=true) String patternPath,
			@RequestParam(value = "enable",required=false,defaultValue="true") boolean enable,
			@RequestParam(value = "levelParent",required=false) String levelParent){
		// 校验入参: patternName
		// 调用service 接口
		List<QueryRelationViewModel> viewListResult = null;
		try {
			viewListResult = categoryService.queryCategoryRelation(levelParent,
			 enable,  patternPath);
		} catch (EspStoreException e) {
			
		    LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(),e);
			
		    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                          e.getMessage());
		}
		 return viewListResult;
	}


	/**
	 * ND编码标识正则校验
	 * 规则：两位大写英文字母标识，可以首位可以使用$符号开始，第二位不允许出现$符号
	 * @param ndCode
	 * @return
	 */
	private static boolean checkNdCodeRegex(String ndCode){
		Pattern pattern = Pattern.compile("^[A-Z]{2}$|^\\$[A-Z]{1}$");
		Matcher matcher = pattern.matcher(ndCode);
		boolean f = matcher.find();
		return f;
	}
	
    /**
     * 以下是新增的接口（主要是各种获取详情的接口）
     */

    /**
     * 查看分类维度
     * 
     * @param content  可能是ndCode和 uuid
     * @return
     * @since
     */
    @RequestMapping(value = "/categories/{content}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    @ResponseBody
    CategoryViewModel requestGetDetailCategory(@PathVariable("content") String content) {
        CategoryModel resultModel = null;

        //两个接口，共用一个函数，需要判断是否为uuid
        if(checkReg(content,uuidReg)){
            
            LOG.debug("通过id:{} 获取分类维度详情",content);
            
            // 调用service 接口
            try {
                resultModel = categoryService.loadCategoryById(content);
            } catch (EspStoreException e) {
               
                LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(),e);
                
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                              e.getMessage());
            }
        }else{
         // ndCode正则校验
            boolean ndCodeFlag = checkNdCodeRegex(content);
            if (!ndCodeFlag) {
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              LifeCircleErrorMessageMapper.CheckNdCodeRegex);
            }
            
            LOG.debug("通过ndCode:{} 获取分类维度详情",content);
            
            // 调用service 接口
            try {
                resultModel = categoryService.loadCategoryByNdCode(content);
            } catch (EspStoreException e) {
               
                LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(),e);
               
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                              e.getMessage());
            }
            
        }
        
        if (resultModel == null) {
            
            LOG.error(LifeCircleErrorMessageMapper.CategoryNotFound.getMessage()+content);
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.CategoryNotFound);
        }
        // 转成出参
        CategoryViewModel resultViewModel = BeanMapperUtils.beanMapper(resultModel, CategoryViewModel.class);

        return resultViewModel;
    }

    /**
     * 查看维度数据 
     * 
     * @author linsm
     * @param content  可能是ndCode和 uuid
     * @return
     * @since
     */
    @RequestMapping(value = "/categories/datas/{content}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    @ResponseBody
    CategoryDataViewModel requestGetDetailCategoryData(@PathVariable("content") String content) {
        CategoryDataModel resultModel = null;
        // 两个接口，共用一个函数，需要判断是否为uuid
        if (checkReg(content, uuidReg)) {
            
            LOG.debug("通过id:{} 获取维度数据详情",content);
            
            // 调用service 接口
            try {
                resultModel = categoryService.loadCategoryDataById(content);
            } catch (EspStoreException e) {
                
                LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(),e);
                
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                              e.getMessage());
            }
        } else {
            
            LOG.debug("通过ndCode:{} 获取维度数据详情",content);
            
            // 调用service 接口
            try {
                resultModel = categoryService.loadCategoryDataByNdCode(content);
            } catch (EspStoreException e) {
               
                LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(),e);
                
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                              e.getMessage());
            }
        }
    
        if (resultModel == null) {
            
            LOG.error(LifeCircleErrorMessageMapper.CategoryDataNotFound.getMessage());
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.CategoryDataNotFound);
        }

        // 转成出参
        CategoryDataViewModel resultViewModel = changeCategoryDataToView(resultModel);
        return resultViewModel;
    }

    /**
     * 查看分类维度应用模式
     * 
     * @param content  可能是ndCode和 uuid
     * @return
     * @since
     */
    @RequestMapping(value = "/categorypatterns/{content}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    @ResponseBody
    CategoryPatternViewModel requestGetDetailCategoryPattern(@PathVariable("content") String content) {
        CategoryPatternModel resultModel = null;
        // 两个接口，共用一个函数，需要判断是否为uuid
        if (checkReg(content, uuidReg)) {
            
            LOG.debug("通过id:{} 获取维度模式详情",content);
            
            // 调用service 接口
            try {
                resultModel = categoryService.loadCategoryPatternById(content);
            } catch (EspStoreException e) {
                
                LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(),e);
                
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                              e.getMessage());
            }

        } else {
            
            LOG.debug("通过patternName:{} 获取维度模式详情",content);
            
            // 调用service 接口
            try {
                resultModel = categoryService.loadCategoryPatternByPatternName(content);
            } catch (EspStoreException e) {
                
                LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(),e);
                
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                              e.getMessage());
            }
        }
        if (resultModel == null) {
            
            LOG.error(LifeCircleErrorMessageMapper.CategoryPatternNotFound.getMessage());
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.CategoryPatternNotFound);
        }
        // 转成出参
        CategoryPatternViewModel resultViewModel = BeanMapperUtils.beanMapper(resultModel, CategoryPatternViewModel.class);

        return resultViewModel;
    }
    
    /**
     * Ndcode批量查看分类维度
     * 
     * @param ndCodeSet
     * @return
     * @since
     */
    @RequestMapping(value = "categories/list", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    @ResponseBody
    Map<String, CategoryViewModel> requestBatchGetDetailCategory(@RequestParam(value = "nd_code", required = true) Set<String> ndCodeSet) {
        //参数校验
        // ndCode正则校验
        for (String ndCode : ndCodeSet) {
            boolean ndCodeFlag = checkNdCodeRegex(ndCode);
            if (!ndCodeFlag) {
                
                LOG.error(LifeCircleErrorMessageMapper.CheckNdCodeRegex.getMessage()+ndCode);
                
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              LifeCircleErrorMessageMapper.CheckNdCodeRegex);
            }
        }
        Map<String, CategoryModel> modelMap = null;
        try {
            modelMap = categoryService.batchGetDetailCategory(ndCodeSet);
        } catch (EspStoreException e) {
            
            LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(),e);
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                          e.getMessage());
        }
 
        //处理出参
        Map<String, CategoryViewModel> viewModelMap = new HashMap<String, CategoryViewModel>();
        if(modelMap != null && !modelMap.isEmpty()){
            for(String ndCode: modelMap.keySet()){
                CategoryViewModel viewModel = null;
                CategoryModel model = modelMap.get(ndCode);
                if(model != null){
                    viewModel = BeanMapperUtils.beanMapper(model, CategoryViewModel.class);
                }
                viewModelMap.put(ndCode, viewModel);
            }
        }

        return viewModelMap;
    }
    
    /**
     * 批量加载维度数据
     * 
     * @param ndCodeSet
     * @return
     * @since
     */
    @RequestMapping(value = "/categories/datas/list", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public Map<String, CategoryDataViewModel> requestBatchGetDetailCategoryData(@RequestParam(value = "nd_code", required = true) Set<String> ndCodeSet) {

        Map<String, CategoryDataModel> modelMap = null;
        try {
            modelMap = categoryService.batchGetDetailCategoryData(ndCodeSet);
        } catch (EspStoreException e) {
           
            LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(),e);
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                          e.getMessage());
        }
        // 处理出参
        Map<String, CategoryDataViewModel> viewModelMap = new HashMap<String, CategoryDataViewModel>();
        if (modelMap != null && !modelMap.isEmpty()) {
            for (String ndCode : modelMap.keySet()) {
                CategoryDataViewModel viewModel = null;
                CategoryDataModel model = modelMap.get(ndCode);
                if (model != null) {
                    viewModel = changeCategoryDataToView(model);
                }
                viewModelMap.put(ndCode, viewModel);
            }
        }
        return viewModelMap;
    }
    
    /**
     * 批量加载分类维度应用模式
     * 
     * @param patternNameSet
     * @return
     * @since
     */
    @RequestMapping(value = "/categorypatterns/list", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public Map<String, CategoryPatternViewModel> requestBatchGetDetailCategoryPattern(@RequestParam(value = "pattern_name", required = true) Set<String> patternNameSet) {
        Map<String, CategoryPatternModel> modelMap = null;
        try {
            modelMap = categoryService.batchGetDetailCategoryPattern(patternNameSet);
        } catch (EspStoreException e) {
            
            LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(),e);
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                          e.getMessage());
        }
        // 处理出参
        Map<String, CategoryPatternViewModel> viewModelMap = new HashMap<String, CategoryPatternViewModel>();
        if (modelMap != null && !modelMap.isEmpty()) {
            for (String ndCode : modelMap.keySet()) {
                CategoryPatternViewModel viewModel = null;
                CategoryPatternModel model = modelMap.get(ndCode);
                if (model != null) {
                    viewModel = BeanMapperUtils.beanMapper(model, CategoryPatternViewModel.class);
                }
                viewModelMap.put(ndCode, viewModel);
            }
        }
        return viewModelMap;
    }
    
    /**
     * 批量创建分类维度应用模式下的维度数据关系
     * 
     * @param paramList
     * @param bindingResult
     * @return
     * @since
     */
    @RequestMapping(value = "categorypatterns/datas/relations/bulk", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody List<CategoryRelationViewModel> requestBatchAddCategoryRelation(@Valid @RequestBody List<CategoryRelationViewModel> paramList,
                                                                                         BindingResult bindingResult) {
        // 入参校验
        ValidResultHelper.valid(bindingResult, LifeCircleErrorMessageMapper.InvalidArgumentsError.getCode());

        // 判断批量数据中是否存在重复的关系：pattern_path,target这两个字段来确定：
        if (paramList != null && paramList.size() > 1) {
            Set<String> duplicateHelperSet = new HashSet<String>();
            for(CategoryRelationViewModel view: paramList){
                if (view != null) {
                    String content = view.getPatternPath() + view.getTarget(); // 直接将这两个字段相加，能够保证惟一性（业务上）
                    boolean isSuccess = duplicateHelperSet.add(content);
                    if (!isSuccess) {
                        
                        LOG.error(LifeCircleErrorMessageMapper.CategoryRelationBatchAddDuplicate.getMessage()); 
                        
                        throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                      LifeCircleErrorMessageMapper.CategoryRelationBatchAddDuplicate);
                    }
                }
            }
        }
        
        // 生成参数
        List<CategoryRelationModel> changedParamList = new ArrayList<CategoryRelationModel>();
        for (CategoryRelationViewModel categoryRelationViewModel : paramList) {
            CategoryRelationModel paramModel = changeCategoryRelationFromView(categoryRelationViewModel);
            paramModel.setIdentifier(UUID.randomUUID().toString());// 设置UUID;
            changedParamList.add(paramModel);
        }

        // 调用service 接口
        List<CategoryRelationModel> resultModelList = null;
        resultModelList = categoryService.batchCreateCategoryRelation(changedParamList);

        // 转成出参
        List<CategoryRelationViewModel> resultViewModelList = new ArrayList<CategoryRelationViewModel>();
        if (resultModelList != null && !resultModelList.isEmpty()) {
            for (CategoryRelationModel model : resultModelList) {
                if (model != null) {
                    resultViewModelList.add(changeCategoryRelationToView(model));
                }
            }
        }

        return resultViewModelList;
    }

    /**
     * 批量删除分类维度应用模式下的维度数据关系
     * 
     * @param idSet
     * @return
     * @since
     */
    @RequestMapping(value = "categorypatterns/datas/relations/bulk", method = RequestMethod.DELETE, produces = { MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody Map<String, String> requestBatchDeleteCategoryRelation(@RequestParam(value = "crid", required = true) LinkedHashSet<String> idSet) {
 
        //异常信息已经在service  进行处理（当不成功时，在service  层会抛出异常）
        categoryService.batchRemoveCategoryRelation(idSet);
        return MessageConvertUtil.getMessageString(LifeCircleErrorMessageMapper.DeleteCategoryRelationSuccess);
    }
    
    /**
     * 正则校验
     * @author 徐震宇
     * @param value 值
     * @param pattern 正则表达式
     * @return
     */
    private boolean checkReg(String value,String pattern){
        return Pattern.matches(pattern, value);
    }
    
    
    /**
     * v1.0 
     * 新增：修改分类维度应用模式下的维度数据关系
     */
    
    /**
     * 修改分类维度应用模式下的维度数据关系
     * 
     * @param categoryRelationViewModel
     * @param bindingResult
     * @param rid
     * @return
     * @since
     */
    @RequestMapping(value = "/categorypatterns/datas/relations/{rid}", method = RequestMethod.PUT, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody CategoryRelationViewModel requestModifyRelationData(@Valid @RequestBody CategoryRelationViewModel categoryRelationViewModel,
                                                                             BindingResult bindingResult,
                                                                             @PathVariable("rid") String rid) {
        // 入参校验
        ValidResultHelper.valid(bindingResult, LifeCircleErrorMessageMapper.InvalidArgumentsError.getCode());

        // 生成参数
        CategoryRelationModel paramModel = changeCategoryRelationFromView(categoryRelationViewModel);
        paramModel.setIdentifier(rid);// 设置UUID;
        // 调用service 接口
        CategoryRelationModel resultModel = null;
        try {
            resultModel = categoryService.modifyCategoryRelation(paramModel);
        } catch (EspStoreException e) {
           
            LOG.error(LifeCircleErrorMessageMapper.StoreSdkFail.getMessage(),e);
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                                          e.getMessage());
        }

        // 转成出参
        CategoryRelationViewModel resultViewModel = changeCategoryRelationToView(resultModel);
        return resultViewModel;
    }

}