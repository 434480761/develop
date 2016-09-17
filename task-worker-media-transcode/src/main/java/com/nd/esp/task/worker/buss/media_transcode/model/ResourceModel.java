package com.nd.esp.task.worker.buss.media_transcode.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @title 资源模型
 * @desc 所有的资源都需要集成ResourceModel
 * @跟LC相比,store_info属性放在了基类这边
 * @atuh lwx
 * @see CoursewareModel
 * @createtime on 2015年6月11日 下午8:04:38
 */
public class ResourceModel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    
    /**
     * 学习对象的创建时间
     */
    
    protected Date createTime;
    /**
     * 学习对象的创建者
     */
    protected String creator;
    /**
     * 学习对象的文字描述，对于文字描述的长度约定为100个汉字
     */
//    @Length(message="{learningObjectModel.description.maxlength.validmsg}",max=500)
    protected String description;
    /**
     * 学习对象的媒体类型
     */
    protected String format;
    /**
     * 学习对象的在存储中的相对路径
     */
    protected String href;
    /**
     * 对象的主键，主键类型采用UUID的形式进行存储
     */
    protected String identifier;
    /**
     * 关键字
     */
    protected List<String> keywords;
    /**
     * 学习对象的语言标识
     */
    protected String language;

    /**
     * 学习对象的最后修改时间
     */
    protected Date lastUpdate;
    /**
     * 学习对象的文件大小。单位是字节
     */
    protected Long size;
    /**
     * 学习对象的状态标识
     */
    protected String status;
    /**
     * 社会化标签
     */
    protected List<String> tags;
    /**
     * 学习对象的标题名称
     */
    protected String title;
    /**
     * 学习对象的版本
     */
    protected String version;
    
    /**
     * 文件的md5摘要
     */
    //@Expose 
    protected Map<String, String> md5 = new HashMap<String, String>();
    /**
     * 预览地址
     */
    //@Expose
    protected Map<String, String> preview = new HashMap<String, String>();
    /**
     * 发布者
     */
    protected String publisher;
    /**
     * 资源大类
     */
    
    protected int type;
    /**
     * 资源小类
     */
    
    protected int subtype;
    
    protected List<String> categories = new ArrayList<String>();

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<String, String> getMd5() {
        return md5;
    }

    public void setMd5(Map<String, String> md5) {
        this.md5 = md5;
    }

    public Map<String, String> getPreview() {
        return preview;
    }

    public void setPreview(Map<String, String> preview) {
        this.preview = preview;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getSubtype() {
        return subtype;
    }

    public void setSubtype(int subtype) {
        this.subtype = subtype;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    
    
    
    /**
     * 文件打包路径信息
     */
    private Map<String,String> storeInfo;
    

}
