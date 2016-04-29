/*
 * Copyright © 1999-2015 NetDragon Websoft Inc. All Rights Reserved
 */
package com.nd.esp.task.worker.buss.media_transcode.entity.cs;

import java.io.Serializable;
import java.util.Map;


/**
 * @title 内容服务-目录项
 * @desc
 * @atuh lwx
 * @createtime on 2015年6月11日 下午10:11:02
 */
public class Dentry implements Serializable {
    /**
     * 序列化版本id。
     */ 
    private static final long serialVersionUID = 4988097841814151066L;

    /* Entry常量描述 */
    
    /** 类型-目录 */
    public static final Integer TYPE_DIRECTORY = 0;

    /** 类型-文件 */
    public static final Integer TYPE_FILE = 1;

    /** 类型-连接文件 */
    public static final Integer TYPE_LINK = 2;

    /** 公开范围-私密 */
    public static final Integer SCOPE_PRIVATE = 0;

    /** 公开范围-完全公开 */
    public static final Integer SCOPE_PUBLIC = 1;

    /** 公开范围-需要密码访问 */
    public static final Integer SCOPE_PASSWORD = 2;

    /** 标识-合并中 */
    public static final Integer FLAG_MERGING = -3;
    
    /** 标识-转码中 */
    public static final Integer FLAG_ENCODING = -2;
    
    /** 标识-逻辑删除 */
    public static final Integer FLAG_RECYCLED = -1;
    
    /** 标识-隐藏 */
    public static final Integer FLAG_HIDDEN = 0;
    
    /** 标识-正常显示 */
    public static final Integer FLAG_NORMAL = 1;
    
    /* Entry属性描述 */
    
    /** 目录项id（UUID） */
    private String dentryId;

    /** 父目录项id(UUID) */
    private String parentId;

    /** 目录项路径(文件包括扩展名) */
    private String path;

    /** 类型：0=目录 1=文件 2=连接文件 */
    private Integer type;

    /** 目录项名(文件一般包括扩展名) */
    private String name;

    /** 备注名 */
    private String otherName;

    /** 自定义元数据 */
    private Map<String, Object> info;

    /** 公开范围：0=私密 1=完全公开 2=需要访问密码 */
    private Integer scope;

    /** 拥有者uid/上传者uid */
    private Long uid;

    /** 访问次数 */
    private Integer hits;

    /** ISO8601规范创建时间 */
    private String createAt;

    /** ISO8601规范最后更新时间 */
    private String updateAt;

    /** ISO8601规范最后更新时间 */
    private String expireAt;

    /** 标识：-3=合并中 -2=转码中 -1=逻辑删除 0=隐藏 1=正常显示 */
    private Integer flag;

    /** 文件索引节点，当该目录项为目录时（type = 0）该字段为空 */
    private Inode inode; // 文件索引节点

    /**
     * @return the dentryId
     */
    public String getDentryId() {
        return dentryId;
    }

    /**
     * @param dentryId the dentryId to set
     */
    public void setDentryId(String dentryId) {
        this.dentryId = dentryId;
    }

    /**
     * @return the parentId
     */
    public String getParentId() {
        return parentId;
    }

    /**
     * @param parentId the parentId to set
     */
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @return the type
     */
    public Integer getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(Integer type) {
        this.type = type;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the otherName
     */
    public String getOtherName() {
        return otherName;
    }

    /**
     * @param otherName the otherName to set
     */
    public void setOtherName(String otherName) {
        this.otherName = otherName;
    }

    /**
     * @return the info
     */
    public Map<String, Object> getInfo() {
        return info;
    }

    /**
     * @param info the info to set
     */
    public void setInfo(Map<String, Object> info) {
        this.info = info;
    }

    /**
     * @return the scope
     */
    public Integer getScope() {
        return scope;
    }

    /**
     * @param scope the scope to set
     */
    public void setScope(Integer scope) {
        this.scope = scope;
    }

    /**
     * @return the uid
     */
    public Long getUid() {
        return uid;
    }

    /**
     * @param uid the uid to set
     */
    public void setUid(Long uid) {
        this.uid = uid;
    }

    /**
     * @return the hits
     */
    public Integer getHits() {
        return hits;
    }

    /**
     * @param hits the hits to set
     */
    public void setHits(Integer hits) {
        this.hits = hits;
    }

    /**
     * @return the createAt
     */
    public String getCreateAt() {
        return createAt;
    }

    /**
     * @param createAt the createAt to set
     */
    public void setCreateAt(String createAt) {
        this.createAt = createAt;
    }

    /**
     * @return the updateAt
     */
    public String getUpdateAt() {
        return updateAt;
    }

    /**
     * @param updateAt the updateAt to set
     */
    public void setUpdateAt(String updateAt) {
        this.updateAt = updateAt;
    }

    /**
     * @return the expireAt
     */
    public String getExpireAt() {
        return expireAt;
    }

    /**
     * @param expireAt the expireAt to set
     */
    public void setExpireAt(String expireAt) {
        this.expireAt = expireAt;
    }

    /**
     * @return the flag
     */
    public Integer getFlag() {
        return flag;
    }

    /**
     * @param flag the flag to set
     */
    public void setFlag(Integer flag) {
        this.flag = flag;
    }

    /**
     * @return the inode
     */
    public Inode getInode() {
        return inode;
    }

    /**
     * @param inode the inode to set
     */
    public void setInode(Inode inode) {
        this.inode = inode;
    }

}
