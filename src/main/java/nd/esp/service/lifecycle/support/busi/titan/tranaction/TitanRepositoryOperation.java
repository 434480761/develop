package nd.esp.service.lifecycle.support.busi.titan.tranaction;

import nd.esp.service.lifecycle.repository.EspEntity;

import java.util.Map;

/**
 * Created by Administrator on 2016/9/12.
 */
public class TitanRepositoryOperation extends TitanRepositoryOperationBase{
    public TitanOperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(TitanOperationType operationType) {
        this.operationType = operationType;
    }
}
