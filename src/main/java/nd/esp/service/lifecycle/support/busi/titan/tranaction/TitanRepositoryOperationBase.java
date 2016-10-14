package nd.esp.service.lifecycle.support.busi.titan.tranaction;

import nd.esp.service.lifecycle.repository.EspEntity;

/**
 * Created by Administrator on 2016/10/14.
 */
public class TitanRepositoryOperationBase {
    protected TitanOperationType operationType;
    protected EspEntity entity;
    public EspEntity getEntity() {
        return entity;
    }

    public void setEntity(EspEntity entity) {
        this.entity = entity;
    }
}
