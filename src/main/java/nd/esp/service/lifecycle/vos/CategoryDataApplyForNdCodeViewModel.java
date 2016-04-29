/* =============================================================
 * Created: [2015年7月8日] by linsm
 * =============================================================
 *
 * Copyright 2014-2015 NetDragon Websoft Inc. All Rights Reserved
 *
 * =============================================================
 */

package nd.esp.service.lifecycle.vos;

/**
 * 申请新的NDcode-输出
 * @author linsm
 * @since
 */
public class CategoryDataApplyForNdCodeViewModel {

    /**
     * 分类维度的标识名称
     */
    private String title = "请输入编码名称";
    /**
     * 英文标识名称
     */
    private String shortName = "enter shortName";
    
    /**
     * 同一分类维度下的父级节点，如果此数据为定级节点，默认值为ROOT
     */
    private String parent;
    /**
     * ND编码标识
     */
    private String ndCode;
    /**
     * 对此分类维度数据进行描述
     */
    private String description = "描述";

    /**
     * 国家标准编码
     */
    private String gbCode = "none";
    
    /**
     * 同一维度下，同一级别下，子分类的顺序
     */
    private int orderNum;
    
    /**
     * 分类维度的标识
     */
    private String category;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGbCode() {
        return gbCode;
    }

    public void setGbCode(String gbCode) {
        this.gbCode = gbCode;
    }

    public String getNdCode() {
        return ndCode;
    }

    public void setNdCode(String ndCode) {
        this.ndCode = ndCode;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public int getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(int orderNum) {
        this.orderNum = orderNum;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
    
}
