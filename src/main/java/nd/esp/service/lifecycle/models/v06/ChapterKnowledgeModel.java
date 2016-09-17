package nd.esp.service.lifecycle.models.v06;

import java.util.List;

/**
 * 章节知识点
 * 
 * @author caocr
 */
public class ChapterKnowledgeModel {
    private String identifier;

    private String outline;

    private String knowledge;

    private List<String> tags;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getOutline() {
        return outline;
    }

    public void setOutline(String outline) {
        this.outline = outline;
    }

    public String getKnowledge() {
        return knowledge;
    }

    public void setKnowledge(String knowledge) {
        this.knowledge = knowledge;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

}
