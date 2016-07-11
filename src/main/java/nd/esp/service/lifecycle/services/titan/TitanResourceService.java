package nd.esp.service.lifecycle.services.titan;

public interface TitanResourceService {
	
	
	long importData(String primaryCategory);
	
	long count(String primaryCategory);

	long updateData(String primaryCategory);

	long createChapterRelation();

	long createKnowledgeRealtion();

	void importOneData(String primaryCategory, String id);

	void importKnowledge();

	void updateChapterRelation();
	void updateKnowledgeRelation();

	void timeTaskImport(Integer page , String type);
	long importAllRelation();
	long importRelation(String sourceType,String targetType);

	long importKnowledgeRelation();
}
