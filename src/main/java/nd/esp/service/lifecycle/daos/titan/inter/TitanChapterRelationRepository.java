package nd.esp.service.lifecycle.daos.titan.inter;

import nd.esp.service.lifecycle.repository.model.Chapter;

import java.util.List;

/**
 * Created by liuran on 2016/5/24.
 */
public interface TitanChapterRelationRepository {
    public boolean createRelation(Chapter chapter);
    public long batchCreateRelation(List<Chapter> chapters);
    public void deleteRelation(Chapter chapter);
    public void updateRelationOrderValue(List<Chapter> chapters, String type);
}
