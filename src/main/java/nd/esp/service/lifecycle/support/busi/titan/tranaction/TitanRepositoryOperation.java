package nd.esp.service.lifecycle.support.busi.titan.tranaction;

import nd.esp.service.lifecycle.repository.EspEntity;

/**
 * Created by Administrator on 2016/9/12.
 */
public class TitanRepositoryOperation {
    private EspEntity entity;
    private TitanOperationType operationType;

    public EspEntity getEntity() {
        return entity;
    }

    public void setEntity(EspEntity entity) {
        this.entity = entity;
    }

    public TitanOperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(TitanOperationType operationType) {
        this.operationType = operationType;
    }
}
