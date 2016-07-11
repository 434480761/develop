package nd.esp.service.lifecycle.daos.titan;

import nd.esp.service.lifecycle.daos.titan.inter.TitanEspRepository;
import nd.esp.service.lifecycle.daos.titan.inter.TitanRepository;
import nd.esp.service.lifecycle.daos.titan.inter.TitanRepositoryFactory;
import nd.esp.service.lifecycle.repository.EspEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by liuran on 2016/6/2.
 */
@Repository(value = "TitanRepositoryImpl")
public class TitanRepositoryImpl<M extends EspEntity> implements TitanRepository<M> {

    @Autowired
    private TitanRepositoryFactory repositoryFactory;

    @Override
    public List<M> batchAdd(List<M> models) {
        if (models == null || models.size() == 0) {
            return null;
        }

        TitanEspRepository espRepository = repositoryFactory.getEspRepository(models.get(0));

        if (espRepository == null) {
            return null;
        }

        return espRepository.batchAdd(models);
    }

    @Override
    public M add(M model) {
        TitanEspRepository espRepository = repositoryFactory.getEspRepository(model);

        if (espRepository == null) {
            return null;
        }

        return (M) espRepository.add(model);

    }


    @Override
    public M update(M model) {
        TitanEspRepository espRepository = repositoryFactory.getEspRepository(model);

        if (espRepository == null) {
            return null;
        }

        return (M) espRepository.update(model);
    }

    @Override
    public List<M> batchUpdate(List<M> models) {
        if (models == null || models.size() == 0) {
            return null;
        }

        TitanEspRepository espRepository = repositoryFactory.getEspRepository(models.get(0));

        if (espRepository == null) {
            return null;
        }
        return espRepository.batchUpdate(models);
    }
}
