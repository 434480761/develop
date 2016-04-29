package nd.esp.service.lifecycle.services;

import nd.esp.service.lifecycle.models.CategoryPatternModel;
import nd.esp.service.lifecycle.vos.ListViewModel;

/**
 * @author johnny
 * @version 1.0
 * @created 24-3月-2015 12:06:04
 */
public interface CategoryPatternService {

	/**
	 * 创建一个分类检索的模式。模式的产生通过顺序、级别和数据组成。形成级联的数据导航模式
	 * @urlpattern  /patterns
	 * @Method POST
	 * 
	 * @param pattern
	 */
	public CategoryPatternModel creatPattern(CategoryPatternModel pattern);

	/**
	 * 修改模式
	 * @urlpattern  /patterns/{id}
	 * @Method PUT
	 * 
	 * @param pattern
	 */
	public CategoryPatternModel modifyPattern(CategoryPatternModel pattern);

	/**
	 * 删除一个pattern
	 * 
	 * @urlpattern  /patterns/{id}
	 * @Method DELETE
	 * 
	 * @param id
	 */
	public boolean removePattern(String id);

	/**
	 * 查询pattern模式，根据patterns的名称和用途
	 * @urlpattern  /patterns?{words=123}&{limit=(0,20)}
	 * @Method GET
	 * 
	 * @param words
	 * @param limit
	 */
	public ListViewModel queryPatterns(String words, String limit);

}