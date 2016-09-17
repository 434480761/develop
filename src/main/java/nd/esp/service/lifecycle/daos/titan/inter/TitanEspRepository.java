package nd.esp.service.lifecycle.daos.titan.inter;

import java.util.List;

/**
 * Created by liuran on 2016/6/24.
 */
public interface TitanEspRepository<M> {
    M add(M entity);
    List<M> batchAdd(List<M> entityList);
    M update(M entity);
    List<M> batchUpdate(List<M> entityList);
    boolean delete(String id);
    boolean batchDelete(List<String> ids);
}
