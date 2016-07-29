package nd.esp.service.lifecycle.services.titan;

/**
 * Created by liuran on 2016/7/28.
 */
public interface TitanResourceServiceNew {
    void importOneData4Script(String primaryCategory, String identifier);
    void importData4Script(String primaryCategory);
    String importStatus();
    void createChapterRelation();
    void importAllRelation();
    void createKnowledgeRelation();
    void importKnowledgeRelation();
    void checkResource(String primaryCategory);
    void checkOneData(String primaryCategory, String id);
    void checkAllData(String primaryCategory);
}
