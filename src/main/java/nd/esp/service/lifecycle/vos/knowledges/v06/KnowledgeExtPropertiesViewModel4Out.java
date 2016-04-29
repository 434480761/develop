package nd.esp.service.lifecycle.vos.knowledges.v06;



/**
 * 知识点扩展属性(v06)
 * 
 * @author coacr
 *
 */
public class KnowledgeExtPropertiesViewModel4Out {
    //用于标识知识点的父级节点
    private String parent;
    
    /**
     * 获取知识点的父级节点
     * 
     * @return
     * @since
     */
    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }
}
