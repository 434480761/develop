package nd.esp.service.lifecycle.vos.educationrelation.v06;

/**
 * 资源关系,获取资源之间的关系的ViewModel
 * <p>Create Time: 2015年5月19日           </p>
 * @author xiezy
 */
public class RelationForPathViewModel {
    /**
     * 查询出来的层次分级
     */
    private int level;
    /**
     * 标识id
     */
    private String identifier;
    /**
     * 只有当维度数据查询的时候会返回nd_code
     */
    private String ndCode;
    /**
     * 名称
     */
    private String title;
    
    public int getLevel() {
        return level;
    }
    public void setLevel(int level) {
        this.level = level;
    }
    public String getIdentifier() {
        return identifier;
    }
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    public String getNdCode() {
        return ndCode;
    }
    public void setNdCode(String ndCode) {
        this.ndCode = ndCode;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
}
