package nd.esp.service.lifecycle.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.nativejdbc.Jdbc4NativeJdbcExtractor;

import nd.esp.service.lifecycle.repository.ds.Item;
import nd.esp.service.lifecycle.repository.ds.jpa.Criteria;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.sdk.DBCallBack;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;

// TODO: Auto-generated Javadoc
/**
 * The Interface EducationRepository.
 *
 * @author Rainy(yang.lin)
 * @version V1.0
 * @param <T> the generic type
 * @Description 
 * @date 2015年5月15日 下午6:09:12
 */

public interface EspRepository<T extends EspEntity> {

	/**
	 * Adds the.
	 *
	 * @param bean            the bean
	 * @return the t
	 * @throws EspStoreException the esp store exception
	 */
	public T add(T bean) throws EspStoreException;
	
	/**
	 * Batch add.
	 *
	 * @param bean the bean
	 * @return the list
	 * @throws EspStoreException the esp store exception
	 */
	public List<T> batchAdd(List<T> bean) throws EspStoreException;

	/**
	 * Delete.
	 *
	 * @param id            the id
	 * @throws EspStoreException the esp store exception
	 */
	public void del(String id) throws EspStoreException;
	
	/**
	 * Batch del.
	 *
	 * @param id the id
	 * @throws EspStoreException the esp store exception
	 */
	public void batchDel(List<String> id)throws EspStoreException;

	/**
	 * Update.
	 *
	 * @param bean            the bean
	 * @return the t
	 * @throws EspStoreException the esp store exception
	 */
	public T update(T bean) throws EspStoreException;

	/**
	 * Gets the.
	 *
	 * @param id            the id
	 * @return the t
	 * @throws EspStoreException the esp store exception
	 */
	public T get(String id) throws EspStoreException;

	/**
	 * Gets the all.
	 *
	 * @param ids            the ids
	 * @return the all
	 * @throws EspStoreException the esp store exception
	 */
	public List<T> getAll(List<String> ids) throws EspStoreException;
	
	/**
	 * Gets the all.
	 *
	 * @param callBack the call back
	 * @return the all
	 * @throws EspStoreException the esp store exception
	 */
	public int getAll(DBCallBack callBack) throws EspStoreException;
	
	public Page<T> findAll(Pageable pageable);
	
	public T converterOut(final T bean) throws EspStoreException;
	
	/**
	 * Example1：模糊查询名字中包含yanglin的数据  <br>
	 * <p>List&lt;Shop&gt; shops; <br>
	 * Item&lt;String&gt; item = new Item&lt;&gt;(); <br>
	 * item.setKey("name");<br>
	 * item.setComparsionOperator(ComparsionOperator.LIKE);<br>
	 * item.setLogicalOperator(LogicalOperator.AND);<br>
	 * item.setValue(ValueUtils.newValue(true, "yanglin", true));<br>
	 * 
	 * List&lt;Item&lt;? extends Object&gt;&gt; items = new ArrayList&lt;&gt;(); <br>
	 * items.add(item);<br>
	 * shops = shopRepository.findByItems(items);<br>
	 * </P>
	 *
	 * @param items the items
	 * @return the list
	 * @throws EspStoreException the esp store exception
	 */
	List<T> findByItems(List<Item<? extends Object>> items) throws EspStoreException;
	
	/**
	 * Find by items.<br>
	 * 
	 * Example2：按照时间范围查询<br>
	 * 
	 * Page&lt;Question&gt; questions = null;<br>
	 * Item&lt;Pair&lt;Date&gt;&gt; item = new Item&lt;&gt;();<br>
	 * item.setKey("lastUpdate");<br>
	 * item.setComparsionOperator(ComparsionOperator.BETWEEN);<br>
	 * item.setLogicalOperator(LogicalOperator.AND);<br>
	 * item.setValue(ValueUtils.newValue(start, new Date()));<br>
	 * 
	 * List&lt;Item&lt;? extends Object&gt;&gt; items = new ArrayList&lt;&gt;(); <br>
	 * items.add(item);<br>
	 * PageRequest pageAble = new PageRequest(0, 5000); <br>
	 * questions =questionRepository.findByItems(items, pageAble);<br>
	 *
	 * @param items            the items
	 * @param page the page
	 * @return the list
	 * @throws EspStoreException the esp store exception
	 */
	Page<T> findByItems(List<Item<? extends Object>> items,Pageable page) throws EspStoreException;
	
	/**
	 * Example1：模糊查询名字中包含yanglin的数据 List<Shop> shops; <br>
	 * Item&lt;String&gt; item = new Item&lt;&gt;(); <br>
	 * item.setKey("name");<br>
	 * item.setComparsionOperator(ComparsionOperator.LIKE);<br>
	 * item.setLogicalOperator(LogicalOperator.AND);<br>
	 * item.setValue(ValueUtils.newValue(true, "yanglin", true));<br>
	 * 
	 * List&lt;Item&lt;? extends Object&gt;&gt; items = new ArrayList&lt;&gt;(); <br>
	 * items.add(item);<br>
	 * 
	 * shops = shopRepository.findOneByItems(items);<br>
	 *
	 * @param items the items
	 * @return the list
	 * @throws EspStoreException the esp store exception
	 */
	T findOneByItems(List<Item<? extends Object>> items) throws EspStoreException;
	
	/**
	 * Gets the list in property.<br>
	 * 
	 * Example:查询名字为"yanglin","yanglin2"的结果<br>
	 * List&lt;Addon&gt; ls = addonRepository.getListWhereInCondition("name",Arrays.asList("yanglin","yanglin2"));<br>
	 *
	 * @param propertyName            the property name
	 * @param propertyValues            the property values
	 * @return the list in property
	 * @throws EspStoreException the esp store exception
	 */
	List<T> getListWhereInCondition(String propertyName,List<?> propertyValues) throws EspStoreException;
	
	/**
	 * 通过bean设置值查询.
	 *
	 * @param entity the entity
	 * @return the by example
	 * @throws EspStoreException the esp store exception
	 */
	T getByExample(T entity) throws EspStoreException;
	
	/**
	 * Gets the all by example.
	 *
	 * @param entity the entity
	 * @return the all by example
	 * @throws EspStoreException the esp store exception
	 */
	List<T> getAllByExample(T entity) throws EspStoreException;
	
	/**
	 * Gets the page by example.
	 *
	 * @param entity the entity
	 * @param pageable the pageable
	 * @return the page by example
	 * @throws EspStoreException the esp store exception
	 */
	Page<T> getPageByExample(final T entity, Pageable pageable) throws EspStoreException;
	
	/**
	 * Delete all by example.
	 *
	 * @param entity the entity
	 * @throws EspStoreException the esp store exception
	 */
	void deleteAllByExample(final T entity) throws EspStoreException;
	
	/**
	 * Gets the index repository.
	 *
	 * @return the index repository
	 */
	public IndexRepository<T> getIndexRepository();
	
	
	/**
	 * Find one by criteria.
	 *
	 * @param criteria the criteria
	 * @return the t
	 * @throws EspStoreException the esp store exception
	 */
	T findOneByCriteria(Criteria<T> criteria) throws EspStoreException;
	
	/**
	 * Find all by criteria.
	 *
	 * @param criteria the criteria
	 * @return the list
	 * @throws EspStoreException the esp store exception
	 */
	List<T> findAllByCriteria(Criteria<T> criteria) throws EspStoreException;
	
	/**
	 * Find by criteria.
	 *
	 * @param criteria the criteria
	 * @param page the page
	 * @return the page
	 * @throws EspStoreException the esp store exception
	 */
	Page<T> findByCriteria(Criteria<T> criteria,Pageable page) throws EspStoreException;
	
	
	/**
	 * Count by criteria.
	 *
	 * @param criteria the criteria
	 * @return the int
	 */
	long countByCriteria(Criteria<T> criteria);
	
	/**
	 * Count by example.
	 *
	 * @param entity the entity
	 * @return the int
	 * @throws EspStoreException 
	 */
	long countByExample(T entity) throws EspStoreException;
	/**
	 * 调用存储过程
	 * @param procedure
	 * @return
	 */
	Object callProcedure(String procedure);
	/**
	 * Gets the jdbc temple.
	 *
	 * @return the jdbc temple
	 */
	JdbcTemplate getJdbcTemple();
	
//	public void addQuestionsCategories(String dataBase);



	TransactionTemplate getTransactionTemplate();


	EntityManager getEntityManager();
}