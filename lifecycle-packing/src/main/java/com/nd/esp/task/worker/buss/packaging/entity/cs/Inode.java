package com.nd.esp.task.worker.buss.packaging.entity.cs;

import java.io.Serializable;
import java.util.Map;
/**
 * @title 表示sdp内容服务中一个索引节点信息的类。
 * @desc
 * @atuh lwx
 * @createtime on 2015年6月11日 下午10:10:21
 */
public class Inode implements Serializable {
    /**
     * 序列化版本id。
     */
    private static final long serialVersionUID = 4988097841814151066L;

    /** 索引节点码(文件MD5) */
    private String inodeId; // 
    
    /** 大小(byte) */
    private Long size; // 
    
    /** MIME */
    private String mime;
    
    /** 扩展名(小写) */
    private String ext;
    
    /** 元数据 */
    private Map<String, Object> meta; 
    
    /** 引用数 */
    private Integer links; 
    
    /** ISO8601规范创建时间 */
    private String createAt;

    /**
     * 返回索引节点码(文件MD5)
     * 
     * @return String
     */
    public String getInodeId() {
        return this.inodeId;
    }

    /**
     * 设置索引节点码(文件MD5)
     * 
     * @param inodeId String
     */
    public void setInodeId(String inodeId) {
        this.inodeId = inodeId;
    }

    /**
     * 返回大小(byte)
     * 
     * @return Long
     */
    public Long getSize() {
        return this.size;
    }

    /**
     * 设置大小(byte)
     * 
     * @param size Long
     */
    public void setSize(Long size) {
        this.size = size;
    }

    /**
     * 返回MIME
     * 
     * @return String
     */
    public String getMime() {
        return this.mime;
    }

    /**
     * 设置MIME
     * 
     * @param mime String
     */
    public void setMime(String mime) {
        this.mime = mime;
    }

    /**
     * 返回扩展名(小写)
     * 
     * @return String
     */
    public String getExt() {
        return this.ext;
    }

    /**
     * 设置扩展名(小写)
     * 
     * @param ext String
     */
    public void setExt(String ext) {
        this.ext = ext;
    }

    /**
     * 返回元数据
     * 
     * <p>
     * 图片，音频，视频等元数据，形如： {width: 1024, height: 768}
     * <p>
     * 
     * @return Map&lt;String,?&gt;
     */
    public Map<String, Object> getMeta() {
        return this.meta;
    }

    /**
     * 设置元数据
     * 
     * @param meta Map&lt;String,?&gt;
     */
    public void setMeta(Map<String, Object> meta) {
        this.meta = meta;
    }

    /**
     * 返回引用数
     * 
     * @return Integer
     */
    public Integer getLinks() {
        return this.links;
    }

    /**
     * 设置引用数
     * 
     * @param links Integer
     */
    public void setLinks(Integer links) {
        this.links = links;
    }

    /**
     * 返回ISO8601规范创建时间
     * 
     * @return String
     */
    public String getCreateAt() {
        return this.createAt;
    }

    /**
     * 设置ISO8601规范创建时间
     * 
     * @param createAt String
     */
    public void setCreateAt(String createAt) {
        this.createAt = createAt;
    }

}