package nd.esp.service.lifecycle.services.titan;

import java.util.Date;

public interface TitanResourceService {
	long importData4Script(String primaryCategory);

	long createChapterRelation();

	long createKnowledgeRealtion();

	void updateChapterRelation();
	void updateKnowledgeRelation();

	void timeTaskImport(Integer page , String type);
	long importAllRelation();
	void repairAllRelation();
	void repairOne(String resourceType, String id);
	void timeTaskRepair(Integer page , String type);

	long importKnowledgeRelation();

	void timeTaskImport4Update(Integer page, String type);

	void importOneData4Script(String primaryCategory, String id);

	void checkResource(String primaryCategory);

	void checkOneData(String primaryCategory, String id);

	void checkAllData(String primaryCategory);
	String importStatus();
	void code();
	void repairData(String primaryCategory);

	void importStatistical(String type);


    void checkOneResourceTypeData(String primaryCategory, Date beginDate, Date endDate);

}
