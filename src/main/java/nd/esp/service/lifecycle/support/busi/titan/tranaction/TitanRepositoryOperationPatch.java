package nd.esp.service.lifecycle.support.busi.titan.tranaction;

import java.util.Map;
import java.util.Set;

/**
 * Created by Administrator on 2016/10/14.
 */
public class TitanRepositoryOperationPatch extends TitanRepositoryOperation{
    public TitanRepositoryOperationPatch(){
        setOperationType(TitanOperationType.patch);
    }
    private Map<String, Object> patchPropertyMap;

    @Override
    public void setOperationType(TitanOperationType operationType) {
        super.setOperationType(TitanOperationType.patch);
    }

    public Map<String, Object> getPatchPropertyMap() {
        return patchPropertyMap;
    }

    public void setPatchPropertyMap(Map<String, Object> patchPropertyMap) {
        this.patchPropertyMap = patchPropertyMap;
    }
}
