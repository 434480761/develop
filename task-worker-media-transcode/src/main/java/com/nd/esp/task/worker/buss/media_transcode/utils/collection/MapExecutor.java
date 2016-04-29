package com.nd.esp.task.worker.buss.media_transcode.utils.collection;

/**
 * Map执行器，用于在For循环时的执行器
 *
 * @author bifeng.liu
 */
public interface MapExecutor {
    /**
     * 执行
     *
     * @param key
     * @param value
     */
    void execute(Object key, Object value);
}
