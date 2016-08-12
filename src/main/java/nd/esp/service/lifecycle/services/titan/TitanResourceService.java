package nd.esp.service.lifecycle.services.titan;

public interface TitanResourceService {
	long importData4Script(String primaryCategory);

	long createChapterRelation();

	long createKnowledgeRealtion();

	void updateChapterRelation();
	void updateKnowledgeRelation();

	void timeTaskImport(Integer page , String type);
	long importAllRelation();

	long importKnowledgeRelation();

	void timeTaskImport4Update(Integer page, String type);

	void importOneData4Script(String primaryCategory, String id);

	void checkResource(String primaryCategory);

	void checkOneData(String primaryCategory, String id);

	void checkAllData(String primaryCategory);
	String importStatus();
}
