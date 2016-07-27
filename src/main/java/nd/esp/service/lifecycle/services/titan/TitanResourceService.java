package nd.esp.service.lifecycle.services.titan;

public interface TitanResourceService {
	
	
	long importData(String primaryCategory);
	long importData4Script(String primaryCategory);

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

	public void timeTaskImport4Update(Integer page, String type);

	public void importOneData4Script(String primaryCategory, String id);

	public void checkResource(String primaryCategory);
}
