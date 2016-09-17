package nd.esp.service.lifecycle.daos.titan.inter;

import nd.esp.service.lifecycle.repository.model.ResourceRelation;
import nd.esp.service.lifecycle.support.busi.titan.TitanSyncType;

/**
 * Created by liuran on 2016/7/4.
 */
public interface TitanRepositoryUtils {
    public void titanSync4MysqlAdd(TitanSyncType errorType,String primaryCategory , String source);
    public void titanSync4MysqlAdd(TitanSyncType errorType, ResourceRelation resourceRelation);
    public void titanSync4MysqlDelete(TitanSyncType errorType, String primaryCategory, String source);
    public void titanSync4MysqlDeleteAll(String primaryCategory, String source);
    public boolean checkRelationExistInMysql(ResourceRelation resourceRelation);
    public void titanSync4MysqlImportAdd(TitanSyncType errorType, String primaryCategory, String source);
    public void titanSync4MysqlAdd(TitanSyncType errorType,String primaryCategory , String source, int executeTimes);
}
