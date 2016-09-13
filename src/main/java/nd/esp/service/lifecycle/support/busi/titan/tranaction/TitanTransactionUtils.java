package nd.esp.service.lifecycle.support.busi.titan.tranaction;

import nd.esp.service.lifecycle.repository.EspEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

/**
 * Created by Administrator on 2016/9/13.
 */
@Component(value = "TitanTransactionUtils")
public class TitanTransactionUtils<M extends EspEntity> {
    @Autowired
    private TitanTransactionCollection titanTransactionCollection;

    public List<M> batchAdd(List<M> models) {
        addStep(models, TitanOperationType.add);
        return null;
    }

    public M add(M model) {
        addStep(model, TitanOperationType.add);
        return null;
    }


    public M update(M model) {
        addStep(model ,TitanOperationType.update);
        return null;
    }

    public List<M> batchUpdate(List<M> models) {
        addStep(models, TitanOperationType.update);
        return null;
    }

    public boolean delete(String id) {

        return true;
    }

    public boolean batchDelete(List<String> ids) {

        return true;
    }

    private void addStep(M entity, TitanOperationType type){
        String name = TransactionSynchronizationManager.getCurrentTransactionName();
        if (name == null){
            return;
        }
        TitanRepositoryOperation operation = new TitanRepositoryOperation();
        operation.setEntity(entity);
        operation.setOperationType(type);
        titanTransactionCollection.addOneStep(name, operation);
    }

    private void addStep(List<M> entities,TitanOperationType type){
        for (M entity : entities){
            addStep(entity, type);
        }
    }
}
