package com.nd.esp.task.worker.buss.media_transcode.utils.collection;


/**
 * 集合的过滤器
 *
 * @author bifeng.liu
 */
public interface CollectionFilter<T> {
    /**
     * 对数据对象进行过滤，要不过滤则返回true，要过滤返回false
     *
     * @param data
     * @return
     */
    boolean filter(T data);
}
