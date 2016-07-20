package nd.esp.service.lifecycle.daos.titan;

import nd.esp.service.lifecycle.daos.titan.inter.*;
import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Created by liuran on 2016/6/24.
 */
@Repository
public class TitanRepositoryFactoryImpl implements TitanRepositoryFactory{
    @Autowired
    private TitanCategoryRepository titanCategoryRepository ;
    @Autowired
    private TitanCoverageRepository titanCoverageRepository ;
    @Autowired
    private TitanRelationRepository titanRelationRepository ;
    @Autowired
    private TitanResourceRepository<Education> titanResourceRepository ;
    @Autowired
    private TitanTechInfoRepository titanTechInfoRepository ;
    @Autowired
    private TitanKnowledgeRelationRepository titanKnowledgeRelationRepository;


    public TitanEspRepository getEspRepository(Object model) {
        if (model instanceof Education) {
            return titanResourceRepository;
        } else if (model instanceof TechInfo) {
            return titanTechInfoRepository;
        } else if (model instanceof ResourceCategory) {
            return titanCategoryRepository;
        } else if (model instanceof ResourceRelation) {
            return titanRelationRepository;
        } else if (model instanceof ResCoverage) {
            return titanCoverageRepository;
        } else if (model instanceof KnowledgeRelation) {
            return titanKnowledgeRelationRepository;
        }
        return null;
    }
}
