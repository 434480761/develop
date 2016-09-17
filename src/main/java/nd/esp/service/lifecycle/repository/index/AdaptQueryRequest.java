package nd.esp.service.lifecycle.repository.index;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.params.CommonParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;
import org.springframework.util.StringUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nd.esp.service.lifecycle.repository.IndexMapper;

/**
 * @Description 用于model多条件检索适配查询类
 * @author Rainy(yang.lin)
 * @date 2015年4月28日 下午2:48:55
 * @version V1.0
 * @param <T>
 *            检索领域相应的BEAN
 */

public class AdaptQueryRequest<T extends IndexMapper> extends QueryRequest {
	
	public Map<String, Object> getExtraParam() {
        return extraParam;
    } 

    //private Map<String,Object> extraParam = Maps.newConcurrentMap();
	private Map<String,Object> extraParam = new IdentityHashMap<>();

	private List<SortClause> sortClauses;
	
	public AdaptQueryRequest() {
		super();
	}

	public AdaptQueryRequest(QueryRequest query) {
		super();
		this.setDf(query.getDf());
		this.setKeyword(query.getKeyword());
		this.setLimit(query.getLimit());
		this.setOffset(query.getOffset());
		this.setSubtype(query.getSubtype());
		this.setType(query.getType());
	}

	/**
	 * Logging
	 */
	private static Logger logger = LoggerFactory
			.getLogger(AdaptQueryRequest.class);
	
	/** The param. */
	private T param;

	/**
	 * Gets the param.
	 * 
	 * @return the param
	 */
	public T getParam() {
		return param;
	}

	/**
	 * Sets the param.
	 * 
	 * @param param
	 *            the new param
	 */
	public void setParam(T param) {
		this.param = param;
	}

	public Map<String, String> getQueryMap() {
		return getQueryMap("");
	}

	public Map<String, String> getQueryMap(String word) {

		final StringBuffer sb = new StringBuffer();

		Map<String, String> params = Maps.newHashMap();
		if(!StringUtils.isEmpty(word)){
			setKeyword(word);
		}
		params.put(CommonParams.Q,StringUtils.isEmpty(getKeyword())?"*:*":getKeyword());
		params.put(CommonParams.START, String.valueOf(this.getOffset()));
		params.put(CommonParams.FIELD, "identifier");
		params.put(CommonParams.ROWS, String.valueOf(this.getLimit()));
		if(param != null){
		ReflectionUtils.doWithFields(param.getClass(), new FieldCallback() {

			@Override
			public void doWith(Field field) throws IllegalArgumentException,
					IllegalAccessException {
				org.apache.solr.client.solrj.beans.Field solrField = field
						.getAnnotation(org.apache.solr.client.solrj.beans.Field.class);

				if (solrField != null) {
					field.setAccessible(true);
					Object value = null;
					try {
						value = field.get(param);
						field.setAccessible(false);
						if (value == null)
							return;
						and(solrField.value(),value);
					} catch (IllegalArgumentException | IllegalAccessException e) {
						        
					    if (logger.isWarnEnabled()) {
                            
					        logger.warn("获取属性值异常:{}", e);
					        
                        }
					    
					}
				}
			}
		});
		}
		params.put(CommonParams.FQ, getWhere());
		
		if(CollectionUtils.isEmpty(sortClauses)){
			params.put(CommonParams.SORT,"sortNum asc,sorttime desc");
		}else{
			StringBuilder solrSb = new StringBuilder();
		      for (SortClause sortClause : sortClauses) {
		        if (solrSb.length() > 0) sb.append(",");
		        solrSb.append(sortClause.getItem());
		        solrSb.append(" ");
		        solrSb.append(sortClause.getOrder());
		      }
		      params.put(CommonParams.SORT, solrSb.toString());
		}

		return params;
	}
	
	private String getWhere(){
		final StringBuffer sb = new StringBuffer();
		
		for(Entry<String, Object> entry : extraParam.entrySet()){
			String field = entry.getKey();
			Object value = entry.getValue();

			if (value instanceof String) {

				if ("".equals((String) value))
					continue;

				sb.append(" AND "+field)
						.append(":")
						.append(ClientUtils
								.escapeQueryChars((String) value)).append(" ");
			} else if (value instanceof Map) {
				if (((Map) value).size() == 0)
					continue;
			} else if (value instanceof Collection) {

				if (((Collection) value).size() == 0) {
					continue;
				} else {
					List<String> conditionValues = Lists.newArrayList();

					for (Object item : (Collection) value) {
						if (!"".equals((String.valueOf(item)).trim())) {
							conditionValues.add(item instanceof String ?escapeQueryChars((String) item) : String.valueOf(item));
						}
					}

					String opera = " , ";
					if(value instanceof LogicList){
						if(((LogicList)value).getOperation().equals(LogicOperation.AND)){
							opera = " AND ";
						}
					}
					
					for (int i = 0; i < conditionValues.size(); i++) {
						sb.append(i == 0 ? " AND " + field
								+ ":(" + conditionValues.get(i)
								: opera + conditionValues.get(i));
						if (i == (conditionValues.size() - 1)) {
							sb.append(") ");
						}
					}
					continue;
				}
			} else {
				sb.append(" AND " + field + ":" + value
						+ " ");
			}
		}
		return sb.toString().replaceFirst(" AND", " ");
	}
	
	public AdaptQueryRequest<T> and(String field,Object value){
		if(value!=null){
			extraParam.put(field, value);
		}
		return this;
	}
	
	public AdaptQueryRequest<T> and(String field,Collection<?> value,LogicOperation operation){
		if(value!=null){
			LogicList list  = new LogicList(operation);
			list.addAll(value);
			extraParam.put(field,list);
		}
		return this;
	}
	
	  public static String escapeQueryChars(String s) {
		    StringBuilder sb = new StringBuilder();
		    for (int i = 0; i < s.length(); i++) {
		      char c = s.charAt(i);
		      // These characters are part of the query syntax and must be escaped
		      if (c == '\\' || c == '+' || c == '-' || c == '!'  || c == '(' || c == ')' || c == ':'
		        || c == '^' || c == '[' || c == ']' || c == '\"' || c == '{' || c == '}' || c == '~'
		        || c == '?' || c == '|' || c == '&'  || c == ';' || c == '/'
		        || Character.isWhitespace(c)) {
		        sb.append('\\');
		      }
		      sb.append(c);
		    }
		    return sb.toString();
		  }
	
	
	  /**
	   * A single sort clause, encapsulating what to sort and the sort order.
	   * <p>
	   * The item specified can be "anything sortable" by solr; some examples
	   * include a simple field name, the constant string {@code score}, and functions
	   * such as {@code sum(x_f, y_f)}.
	   * <p>
	   * A SortClause can be created through different mechanisms:
	   * <PRE><code>
	   * new SortClause("product", SolrQuery.ORDER.asc);
	   * new SortClause("product", "asc");
	   * SortClause.asc("product");
	   * SortClause.desc("product");
	   * </code></PRE>
	   */
	  public static class SortClause implements java.io.Serializable {

	    private static final long serialVersionUID = 1L;

	    private final String item;
	    private final ORDER order;

	    /**
	     * Creates a SortClause based on item and order
	     * @param item item to sort on
	     * @param order direction to sort
	     */
	    public SortClause(String item, ORDER order) {
	      this.item = item;
	      this.order = order;
	    }

	    /**
	     * Creates a SortClause based on item and order
	     * @param item item to sort on
	     * @param order string value for direction to sort
	     */
	    public SortClause(String item, String order) {
	      this(item, ORDER.valueOf(order));
	    }

	    /**
	     * Creates an ascending SortClause for an item
	     * @param item item to sort on
	     */
	    public static SortClause create (String item, ORDER order) {
	      return new SortClause(item, order);
	    }

	    /**
	     * Creates a SortClause based on item and order
	     * @param item item to sort on
	     * @param order string value for direction to sort
	     */
	    public static SortClause create(String item, String order) {
	      return new SortClause(item, ORDER.valueOf(order));
	    }

	    /**
	     * Creates an ascending SortClause for an item
	     * @param item item to sort on
	     */
	    public static SortClause asc (String item) {
	      return new SortClause(item, ORDER.asc);
	    }

	    /**
	     * Creates a decending SortClause for an item
	     * @param item item to sort on
	     */
	    public static SortClause desc (String item) {
	      return new SortClause(item, ORDER.desc);
	    }

	    /**
	     * Gets the item to sort, typically a function or a fieldname
	     * @return item to sort
	     */
	    public String getItem() {
	      return item;
	    }

	    /**
	     * Gets the order to sort
	     * @return order to sort
	     */
	    public ORDER getOrder() {
	      return order;
	    }

	    public boolean equals(Object other){
	      if (this == other) return true;
	      if (!(other instanceof SortClause)) return false;
	      final SortClause that = (SortClause) other;
	      return this.getItem().equals(that.getItem()) && this.getOrder().equals(that.getOrder());
	    }

	    public int hashCode(){
	      return this.getItem().hashCode();
	    }

	    /**
	     * Gets a human readable description of the sort clause.
	     * <p>
	     * The returned string is not suitable for passing to Solr,
	     * but may be useful in debug output and the like.
	     * @return a description of the current sort clause
	     */
	    public String toString() {
	      return "[" + getClass().getSimpleName() + ": item=" + getItem() + "; order=" + getOrder() + "]";
	    }
	  }
	  
	  
	  public enum ORDER { desc, asc;
	    public ORDER reverse() {
	      return (this == asc) ? desc : asc;
	    }
	  }
	  
	  
	  /**
	   * Clears current sort information.
	   *
	   * @return the modified SolrQuery object, for easy chaining
	   * @since 4.2
	   */
	  public AdaptQueryRequest<T> clearSorts() {
	    sortClauses = null;
	    return this;
	  }

	  /**
	   * Replaces the current sort information.
	   *
	   * @return the modified SolrQuery object, for easy chaining
	   * @since 4.2
	   */
	  public AdaptQueryRequest<T> setSorts(List<SortClause> value) {
	    sortClauses = new ArrayList<>(value);
	    return this;
	  }

	  /**
	   * Gets an a list of current sort clauses.
	   *
	   * @return an immutable list of current sort clauses
	   * @since 4.2
	   */
	  public List<SortClause> getSorts() {
	    if (sortClauses == null) return Collections.emptyList();
	    else return Collections.unmodifiableList(sortClauses);
	  }

	  /**
	   * Replaces the current sort information with a single sort clause
	   *
	   * @return the modified SolrQuery object, for easy chaining
	   * @since 4.2
	   */
	  public AdaptQueryRequest<T> setSort(String field, ORDER order) {
	    return setSort(new SortClause(field, order));
	  }

	  /**
	   * Replaces the current sort information with a single sort clause
	   *
	   * @return the modified SolrQuery object, for easy chaining
	   * @since 4.2
	   */
	  public AdaptQueryRequest<T> setSort(SortClause sortClause) {
	    clearSorts();
	    return addSort(sortClause);
	  }

	  /**
	   * Adds a single sort clause to the end of the current sort information.
	   *
	   * @return the modified SolrQuery object, for easy chaining
	   * @since 4.2
	   */
	  public AdaptQueryRequest<T> addSort(String field, ORDER order) {
	    return addSort(new SortClause(field, order));
	  }

	  /**
	   * Adds a single sort clause to the end of the query.
	   *
	   * @return the modified SolrQuery object, for easy chaining
	   * @since 4.2
	   */
	  public AdaptQueryRequest<T> addSort(SortClause sortClause) {
	    if (sortClauses == null) sortClauses = new ArrayList<>();
	    sortClauses.add(sortClause);
	    return this;
	  }

	  /**
	   * Updates or adds a single sort clause to the query.
	   * If the field is already used for sorting, the order
	   * of the existing field is modified; otherwise, it is
	   * added to the end.
	   * <p>
	   * @return the modified SolrQuery object, for easy chaining
	   * @since 4.2
	   */
	  public AdaptQueryRequest<T> addOrUpdateSort(String field, ORDER order) {
	    return addOrUpdateSort(new SortClause(field, order));
	  }

	  /**
	   * Updates or adds a single sort field specification to the current sort
	   * information. If the sort field already exist in the sort information map,
	   * it's position is unchanged and the sort order is set; if it does not exist,
	   * it is appended at the end with the specified order..
	   *
	   * @return the modified SolrQuery object, for easy chaining
	   * @since 4.2
	   */
	  public AdaptQueryRequest<T> addOrUpdateSort(SortClause sortClause) {
	    if (sortClauses != null) {
	      for (int index=0 ; index<sortClauses.size() ; index++) {
	        SortClause existing = sortClauses.get(index);
	        if (existing.getItem().equals(sortClause.getItem())) {
	          sortClauses.set(index, sortClause);
	          return this;
	        }
	      }
	    }
	    return addSort(sortClause);
	  }

	  /**
	   * Removes a single sort field from the current sort information.
	   *
	   * @return the modified SolrQuery object, for easy chaining
	   * @since 4.2
	   */
	  public AdaptQueryRequest<T> removeSort(SortClause sortClause) {
	    return removeSort(sortClause.getItem());
	  }
	  

	  /**
	   * Removes a single sort field from the current sort information.
	   *
	   * @return the modified SolrQuery object, for easy chaining
	   * @since 4.2
	   */
	  public AdaptQueryRequest<T> removeSort(String itemName) {
	    if (sortClauses != null) {
	      for (SortClause existing : sortClauses) {
	        if (existing.getItem().equals(itemName)) {
	          sortClauses.remove(existing);
	          if (sortClauses.isEmpty()) sortClauses = null;
	          break;
	        }
	      }
	    }
	    return this;
	  }
}
