package nd.esp.service.lifecycle.daos.titan.inter;

import nd.esp.service.lifecycle.repository.model.Chapter;
import nd.esp.service.lifecycle.repository.model.KnowledgeRelation;

import java.util.List;

/**
 * Created by liuran on 2016/5/30.
 */
public interface TitanKnowledgeRelationRepository extends TitanEspRepository<KnowledgeRelation> {
    public boolean createRelation4Tree(Chapter knowledge);
}

