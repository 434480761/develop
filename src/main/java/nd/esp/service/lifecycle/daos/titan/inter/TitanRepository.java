package nd.esp.service.lifecycle.daos.titan.inter;

import nd.esp.service.lifecycle.repository.EspEntity;

import java.util.List;

/**
 * Created by liuran on 2016/6/2.
 */
public interface TitanRepository<M extends EspEntity> {
    M add(M model);

    List<M> batchAdd(List<M> models);

    M update(M model);

    List<M> batchUpdate(List<M> models);

    boolean delete(String identifier);

    boolean batchDelete(List<String> identifiers);
}
