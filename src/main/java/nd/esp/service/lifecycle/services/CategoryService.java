package nd.esp.service.lifecycle.services;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nd.esp.service.lifecycle.models.CategoryDataModel;
import nd.esp.service.lifecycle.models.CategoryModel;
import nd.esp.service.lifecycle.models.CategoryPatternModel;
import nd.esp.service.lifecycle.models.CategoryRelationModel;
import nd.esp.service.lifecycle.vos.ListViewModel;
import nd.esp.service.lifecycle.vos.QueryRelationViewModel;

import nd.esp.service.lifecycle.repository.exception.EspStoreException;

/**
 * @author johnny
 * @version 1.0
 * @created 20-4月-2015 15:49:41
 */
public interface CategoryService {
	
	/**
	 * 创建维度分类
	 * 
	 * @param category
	 */
	public CategoryModel creatCategory(CategoryModel category) throws EspStoreException;
	

	
	/**
	 * 修改分类维度的信息
	 * 
	 * @param category
	 */
	public CategoryModel modifyCategory(CategoryModel category) throws EspStoreException;
	
	/**
	 * 根据名字进行匹配分类维度信息
	 * 
	 * @param words
	 * @param limit
	 */
	public ListViewModel<CategoryModel> queryCategory(String words, String limit) throws EspStoreException;

	/**
	 * 通过id删除
	 * 
	 * 删除分类的时候，需要确定与其有关的维度数据需要删除，同时模式数据中与此相关的数据也要进行更新
	 * 
	 * @param id
	 */
	public void removeCategory(String id) throws EspStoreException;
	
	
	/**
	 * 创建维度数据
	 * 
	 * @param categoryDataModel
	 */
	public CategoryDataModel createCategoryData(CategoryDataModel categoryDataModel) throws EspStoreException;  //将上一个接口改成这个，应该是创建的接口？

	
	/**
	 * 修改维度数据信息
	 * 
	 * @param categoryData
	 */
	public CategoryDataModel modifyCategoryData(CategoryDataModel categoryData) throws EspStoreException;

	/**
	 * 根据上级节点类型和名称模糊匹配下级节点内容
	 * 
	 * @param all
	 * @param parent
	 * @param words
	 * @param limit
	 */
	public ListViewModel<CategoryDataModel> queryCategoryData(String category, Boolean all, String parent, String words, String limit)throws EspStoreException;//新增加属性：category

	
	/**
	 * 通过id删除
	 * 删除数据的时候，需要根据实际数据关系，进行关系模式以及资源定位相关数据的更新
	 * 
	 * @param id
	 */
	public void removeCategoryData(String id) throws EspStoreException;

	
	/**
	 * 创建一个分类检索的模式。模式的产生通过顺序、级别和数据组成。形成级联的数据导航模式
	 * @urlpattern  /patterns
	 * @Method POST
	 * 
	 * @param pattern
	 */
	public CategoryPatternModel creatCategoryPattern(CategoryPatternModel pattern) throws EspStoreException;
	
	/**
	 * 修改模式
	 * @urlpattern  /patterns/{id}
	 * @Method PUT
	 * 
	 * @param pattern
	 */
	public CategoryPatternModel modifyCategoryPattern(CategoryPatternModel pattern) throws EspStoreException;
	
	
	
	/**
	 * 查询pattern模式，根据patterns的名称和用途
	 * @urlpattern  /patterns?{words=123}&{limit=(0,20)}
	 * @Method GET
	 * 
	 * @param words
	 * @param limit
	 */
	public ListViewModel<CategoryPatternModel> queryCategoryPatterns(String words, String limit) throws EspStoreException;
	

	
	/**
	 * 删除一个pattern
	 * 
	 * @urlpattern  /patterns/{id}
	 * @Method DELETE
	 * 
	 * @param id
	 */
	public void removeCategoryPattern(String id) throws EspStoreException;
	

	/**
	 * 
	 * @param categoryRelation    建立分类体系之间的模式关系
	 */
	public CategoryRelationModel createCategoryRelation(CategoryRelationModel categoryRelation) throws EspStoreException;


	/**
	 * 根据上级节点，模式和级别来查询下级级联分类的数据信息
	 * 
	 * @param levelParent    uuid
	 * @param enable
	 * @param tag
	 * @param relationType
	 * @param source    关系源标识
	 * @param patternName
	 * @param level
	 */
	public List<QueryRelationViewModel> queryCategoryRelation(String levelParent, boolean enable, String patternPath) throws EspStoreException;


	
	/**
	 * 通过id删除
	 * 删除分类关系的时候，会造成分类导航操作失效。需要明确说明。
	 * 
	 * @param id
	 */
	public void removeCategoryRelation(String id) throws EspStoreException;
	
	
	
    /**
     * 以下是新增加的接口（主要是各种获取详情的接口）
     */

    /**
     * 通过ndCode获取分类维度详情 <br>
     * Created 2015年5月4日 上午11:02:49
     * 
     * @param ndCode
     * @return
     * @throws EspStoreException
     * @author linsm
     */
    public CategoryModel loadCategoryByNdCode(String ndCode) throws EspStoreException;
    
    /**
     * 通过id获取分类维度详情 <br>
     * Created 2015年5月4日 上午11:02:49
     * 
     * @param id
     * @return
     * @throws EspStoreException
     * @author linsm
     */
    public CategoryModel loadCategoryById(String id) throws EspStoreException;

    /**
     * 通过ndCode获取维度数据详情 <br>
     * Created 2015年5月4日 上午11:06:10
     * 
     * @param ndCode
     * @return
     * @throws EspStoreException
     * @author linsm
     */
    public CategoryDataModel loadCategoryDataByNdCode(String ndCode) throws EspStoreException;
    
    /**
     * 通过id获取维度数据详情 <br>
     * Created 2015年5月4日 上午11:06:10
     * 
     * @param id
     * @return
     * @throws EspStoreException
     * @author linsm
     */
    public CategoryDataModel loadCategoryDataById(String id) throws EspStoreException;

    /**
     * 通过模式名(patternName)获取维度模式详情 <br>
     * Created 2015年5月4日 上午11:39:43
     * 
     * @param patternName
     * @return
     * @throws EspStoreException
     * @author linsm
     */
    public CategoryPatternModel loadCategoryPatternByPatternName(String patternName) throws EspStoreException;
    
    /**
     * 通过id获取维度模式详情 <br>
     * Created 2015年5月4日 上午11:39:43
     * 
     * @param id
     * @return
     * @throws EspStoreException
     * @author linsm
     */
    public CategoryPatternModel loadCategoryPatternById(String id) throws EspStoreException;
    
    /**
     * Ndcode批量查看分类维度
     * 
     * @param ndCodeSet
     * @return
     * @throws EspStoreException
     * @since
     */
    public Map<String, CategoryModel> batchGetDetailCategory(Set<String> ndCodeSet) throws EspStoreException;
    
    /**
     * 批量加载维度数据
     * 
     * @param ndCodeSet
     * @return
     * @throws EspStoreException
     * @since
     */
    public Map<String, CategoryDataModel> batchGetDetailCategoryData(Set<String> ndCodeSet) throws EspStoreException;
    
    /**
     * 批量加载分类维度应用模式
     * 
     * @param patternNameSet
     * @return
     * @throws EspStoreException
     * @since
     */
    public Map<String, CategoryPatternModel> batchGetDetailCategoryPattern(Set<String> patternNameSet)throws EspStoreException;

    /**
     * 批量创建分类维度应用模式下的维度数据关系
     * 
     * @param paramList
     * @return
     * @throws EspStoreException
     * @since
     */
    public List<CategoryRelationModel> batchCreateCategoryRelation(List<CategoryRelationModel> paramList);

    /**
     * 批量删除分类维度应用模式下的维度数据关系
     * 
     * @param idSet
     * @return
     * @throws EspStoreException
     * @since
     */
    public void batchRemoveCategoryRelation(LinkedHashSet<String> idSet);
    
    
    /**
     * v1.0 
     * 新增：修改分类维度应用模式下的维度数据关系
     */
    
    /**
     * 修改分类维度应用模式下的维度数据关系
     * 
     * @param categoryRelation
     * @return
     * @throws EspStoreException
     * @since
     */
    public CategoryRelationModel modifyCategoryRelation(CategoryRelationModel categoryRelation) throws EspStoreException;

    // v1.2新增的接口

    /**
     * 指定了分类维度， 父结点，获取最大ndCode 若不存在对应的数据，则返回空字符串
     * 
     * @param categoryId
     * @param parentId
     * @param otherNdCode 去掉预留的维度数据（用于其它）
     * @return
     * @since
     */
    public String getMaxLevelNdCodeExcludeOther(String categoryId, String parentId, String otherNdCode);
    
    /**
     * 查询关系的个数：
     * @param patternPath 关系路径
     * @param enable 关系是否可用
     * @param ndCode target ndCode
     * @return int 关系个数
     * @author linsm
     */
    public int countRelation(String patternPath, Boolean enable, String ndCode);
}