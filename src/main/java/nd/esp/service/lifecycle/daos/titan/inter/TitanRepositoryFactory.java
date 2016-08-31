package nd.esp.service.lifecycle.daos.titan.inter;

/**
 * Created by liuran on 2016/6/24.
 */
public interface TitanRepositoryFactory {
    public TitanEspRepository getEspRepository(Object model);
    public TitanEspRepository getEspRepositoryByLabel(String label);
}
