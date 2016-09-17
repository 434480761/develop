package nd.esp.service.lifecycle.models.v06;

import java.util.List;

import nd.esp.service.lifecycle.educommon.models.ResourceModel;

/**
 * 知识点service层模型
 * 
 * @author caocr
 */
public class KnowledgeModel extends ResourceModel {
    // 知识点扩展属性
    private KnowledgeExtPropertiesModel extProperties;

    // 知识点和知识点之间的关系属性
    private List<KnowledgeRelationsModel> knowledgeRelations;

    public KnowledgeExtPropertiesModel getExtProperties() {
        return extProperties;
    }

    public void setExtProperties(KnowledgeExtPropertiesModel extProperties) {
        this.extProperties = extProperties;
    }
    
    public List<KnowledgeRelationsModel> getKnowledgeRelations() {
        return knowledgeRelations;
    }

    public void setKnowledgeRelations(List<KnowledgeRelationsModel> knowledgeRelations) {
        this.knowledgeRelations = knowledgeRelations;
    }
}
