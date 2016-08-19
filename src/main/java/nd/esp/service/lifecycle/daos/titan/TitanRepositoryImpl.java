package nd.esp.service.lifecycle.daos.titan;

import nd.esp.service.lifecycle.daos.titan.inter.TitanCommonRepository;
import nd.esp.service.lifecycle.daos.titan.inter.TitanEspRepository;
import nd.esp.service.lifecycle.daos.titan.inter.TitanRepository;
import nd.esp.service.lifecycle.daos.titan.inter.TitanRepositoryFactory;
import nd.esp.service.lifecycle.repository.EspEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by liuran on 2016/6/2.
 */
@Repository(value = "TitanRepositoryImpl")
public class TitanRepositoryImpl<M extends EspEntity> implements TitanRepository<M> {
    private static final Logger LOG = LoggerFactory
            .getLogger(TitanCategoryRepositoryImpl.class);
    @Autowired
    private TitanRepositoryFactory repositoryFactory;

    @Autowired
    private TitanCommonRepository titanCommonRepository;

    @Override
    public List<M> batchAdd(List<M> models) {
        try {
            if (models == null || models.size() == 0) {
                return null;
            }

            TitanEspRepository espRepository = repositoryFactory.getEspRepository(models.get(0));

            if (espRepository == null) {
                return null;
            }

            return espRepository.batchAdd(models);
        } catch (Exception e) {
            LOG.info("titan batch add error");
        }
        return null;
    }

    @Override
    public M add(M model) {
        try {
            TitanEspRepository espRepository = repositoryFactory.getEspRepository(model);

            if (espRepository == null) {
                return null;
            }
            return (M) espRepository.add(model);
        } catch (Exception e) {
            LOG.info("titan add error");
        }

        return null;
    }


    @Override
    public M update(M model) {
        try {
            TitanEspRepository espRepository = repositoryFactory.getEspRepository(model);

            if (espRepository == null) {
                return null;
            }

            return (M) espRepository.update(model);
        } catch (Exception e) {
            LOG.info("titan update error");
        }
        return null;
    }

    @Override
    public List<M> batchUpdate(List<M> models) {
        try {
            if (models == null || models.size() == 0) {
                return null;
            }

            TitanEspRepository espRepository = repositoryFactory.getEspRepository(models.get(0));

            if (espRepository == null) {
                return null;
            }
            return espRepository.batchUpdate(models);
        } catch (Exception e) {
            LOG.info("titan batch update error");
        }

        return null;
    }

    @Override
    public boolean delete(String id) {
        try {
            String label = titanCommonRepository.getEdgeLabelById(id);
            TitanEspRepository espRepository = repositoryFactory.getEspRepositoryByLabel(label);
            if(espRepository == null){
                LOG.info("titan batch update error");
            } else {
                espRepository.delete(id);
            }
        } catch (Exception e) {
            LOG.info("titan batch update error");
        }
        return true;
    }

    @Override
    public boolean batchDelete(List<String> ids) {
        for (String id : ids){
            delete(id);
        }

        return true;
    }
}
