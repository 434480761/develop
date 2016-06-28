package nd.esp.service.lifecycle.vos.knowledges.v06;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;
import nd.esp.service.lifecycle.vos.valid.LessPropertiesDefault;

public class KnowledgeViewModel4In extends ResourceViewModel {
    // 知识点位置属性
    @NotNull(message="{knowledgeViewModel.position.notNull.validmsg}",groups={LessPropertiesDefault.class})
    private KnowledgeExtPropertiesViewModel position;

    // 知识点和知识点之间的关系属性
    @Valid
    private List<KnowledgeRelationsViewModel4Add> knowledgeRelations;

    public KnowledgeExtPropertiesViewModel getPosition() {
        return position;
    }

    public void setPosition(KnowledgeExtPropertiesViewModel position) {
        this.position = position;
    }

    public List<KnowledgeRelationsViewModel4Add> getKnowledgeRelations() {
        return knowledgeRelations;
    }

    public void setKnowledgeRelations(List<KnowledgeRelationsViewModel4Add> knowledgeRelations) {
        this.knowledgeRelations = knowledgeRelations;
    }
}
