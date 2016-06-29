package nd.esp.service.lifecycle.support.busi.titan;

import nd.esp.service.lifecycle.support.enums.ResourceNdCode;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by liuran on 2016/6/7.
 */
public enum TitanTreeType {
    knowledges,chapters;

    public static Map<TitanTreeType, String> relationType = new HashMap<>();

    public static Map<TitanTreeType, String> primaryCategory = new HashMap<>();

    static {
        relationType.put(knowledges,TitanKeyWords.has_knowledge.toString());
        relationType.put(chapters,TitanKeyWords.has_chapter.toString());

        primaryCategory.put(knowledges, ResourceNdCode.knowledges.toString());
        primaryCategory.put(chapters, ResourceNdCode.chapters.toString());
    }

    public String relation(){
        return relationType.get(this);
    }

    public String primaryCategory(){
        return primaryCategory.get(this);
    }

}
