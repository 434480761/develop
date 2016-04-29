package nd.esp.service.lifecycle.utils;



import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nd.esp.service.lifecycle.utils.collection.CollectionFilter;
import nd.esp.service.lifecycle.utils.collection.CollectionSelector;
import nd.esp.service.lifecycle.utils.collection.MapExecutor;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;

/**
 * 集合工具类
 *
 * @bifeng.liu
 * @see org.apache.commons.collections.CollectionUtils
 */
public class CollectionUtils extends org.apache.commons.collections.CollectionUtils {
    /**
     * 私有化构造函数，不允许实例化该类
     */
    private CollectionUtils() {
    }

    /**
     * 检查指定Map是否为空
     * <p><pre>
     * CollectionUtils.isEmpty(null) = true
     * CollectionUtils.isEmpty(new HashMap()) = true
     * </pre>
     *
     * @param map 要检查的Map
     * @return 如果Map为空，则返回<code>true</code>
     */
    public static boolean isEmpty(final Map map) {
        return map == null || map.isEmpty();
    }

    /**
     * 检查指定Map是否不为空
     * <p><pre>
     * CollectionUtils.isEmpty(null) = false
     * CollectionUtils.isEmpty(new HashMap()) = false
     * </pre>
     *
     * @param map 要检查的Map
     * @return 如果Map为空，则返回<code>false</code>
     */
    public static boolean isNotEmpty(final Map map) {
        return !isEmpty(map);
    }
    
    
//    public static boolean isEmpty(Collection<?>collection) {
//         return (collection == null || collection.isEmpty());
//    }
//    
//    
//    public static boolean isNotEmpty(Collection<?> collection) {
//        return !CollectionUtils.isEmpty(collection);
//    }
    
    

//    /**
//     * 过滤集合数据
//     *
//     * @param collection
//     * @param filter
//     * @return
//     */
//    public static Collection where(final Collection collection, CollectionFilter filter) {
//        if (collection == null) {
//            return null;
//        }
//        if (filter == null) {
//            return collection;
//        }
//        Collection result = newInstance(collection);
//        for (Iterator iterator = collection.iterator(); iterator.hasNext(); ) {
//            Object data = iterator.next();
//            if (data != null && filter.filter(data)) {
//                result.add(data);
//            }
//        }
//        return result;
//    }

//    /**
//     * 选择集合数据的字段
//     *
//     * @param collection
//     * @param selector   选择器
//     * @return
//     */
//    public static Collection select(final Collection collection, CollectionSelector selector) {
//        if (collection == null) {
//            return null;
//        }
//        if (selector == null) {
//            return collection;
//        }
//        Collection result = newInstance(collection);
//        for (Iterator iterator = collection.iterator(); iterator.hasNext(); ) {
//            Object data = iterator.next();
//            if (data != null) {
//                result.add(selector.select(data));
//            }
//        }
//        return result;
//    }

    /**
     * 对Map对象中的所有元素执行某个执行器
     * <p/>
     * 如果map为空或者执行器为NULL，则不处理。
     *
     * @param map
     * @param executor
     */
    public static void forAllDo(Map map, MapExecutor executor) {
        if (!isEmpty(map) && executor != null) {
            Iterator<Map.Entry> iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = iterator.next();
                executor.execute(entry.getKey(), entry.getValue());
            }
        }
    }

//    /**
//     * 新建集合
//     * <p/>
//     * 部分没有默认构造函数列表，则使用deepClone，但如果列表里面有大数据量时不推荐使用
//     *
//     * @param collection 集合
//     * @return
//     */
//    public static Collection newInstance(final Collection collection) {
//        //用于保证返回的类型与传入的类型一致
//        Collection result = null;
//        try {
//            //有部分没有默认的构造函数，则使用deepClone
//            result = collection.getClass().newInstance();
//        } catch (Exception e) {
//            result = ObjectUtils.deepClone(collection);
//            result.clear();
//        }
//        return result;
//    }
//
//
//    /**
//     * Set对象转换List对象
//     * <p/>
//     * 如果Set为Null，返回空List对象
//     *
//     * @param set Set对象
//     * @return
//     */
//    public static final <T> List<T> setAsList(Set<T> set) {
//        if (isEmpty(set)) {
//            return new ArrayList<T>(0);
//        }
//        List<T> result = new ArrayList<T>(set.size());
//        Iterator<T> it = set.iterator();
//        while (it.hasNext()) {
//            result.add(it.next());
//        }
//        return result;
//    }
    
    /**
     * 将Set中的null去掉,如果T为String,将元素为""的也去掉
     * <p>Create Time: 2015年6月29日   </p>
     * <p>Create author: xiezy   </p>
     * @param targetSet
     * @return
     */
    public static final <T> Set<T> removeEmptyDeep(Set<T> targetSet){
        Set<T> result = new HashSet<T>();
        
        if(isEmpty(targetSet)){
            return targetSet;
        }else{
            for (Iterator<T> iterator = targetSet.iterator(); iterator.hasNext(); ) {
                T data = iterator.next();
                if (data != null) {
                    if(data instanceof String){
                        String dataStr = (String)data;
                        if(!dataStr.equals("")){
                            result.add(data);
                        }
                    }else {
                        result.add(data);
                    }
                }
            }
        }
        
        return result;
    }
    
    /**
     * 将List中的null去掉,如果T为String,将元素为""的也去掉
     * <p>Create Time: 2015年7月14日   </p>
     * <p>Create author: xiezy   </p>
     * @param targetList
     * @return
     */
    public static final <T> List<T> removeEmptyDeep(List<T> targetList){
        List<T> result = new ArrayList<T>();
        
        if(isEmpty(targetList)){
            return targetList;
        }else{
            for (Iterator<T> iterator = targetList.iterator(); iterator.hasNext(); ) {
                T data = iterator.next();
                if (data != null) {
                    if(data instanceof String){
                        String dataStr = (String)data;
                        if(!dataStr.equals("")){
                            result.add(data);
                        }
                    }else {
                        result.add(data);
                    }
                }
            }
        }
        
        return result;
    }

    /**
     * 获取集合中第一个满足条件的对象
     * 集合过滤器返回true时表示满足条件
     *
     * @param collection       集合
     * @param collectionFilter 集合过滤器
     * @param <T>
     * @return 满足条件的对象 or null
     */
    public static final <T> T first(final Collection<T> collection, CollectionFilter<T> collectionFilter) {
        Iterator<T> iterator = collection.iterator();
        while (iterator.hasNext()) {
            T t = iterator.next();
            if (collectionFilter.filter(t)) {
                return t;
            }
        }
        return null;
    }
}
