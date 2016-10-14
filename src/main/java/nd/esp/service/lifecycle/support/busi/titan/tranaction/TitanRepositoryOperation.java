package nd.esp.service.lifecycle.support.busi.titan.tranaction;

import nd.esp.service.lifecycle.repository.EspEntity;

import java.util.Map;

/**
 * Created by Administrator on 2016/9/12.
 */
public class TitanRepositoryOperation{
    protected TitanOperationType operationType;
    protected EspEntity entity;
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
