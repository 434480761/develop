package nd.esp.service.lifecycle.support.busi.titan.tranaction;

import java.util.Set;

/**
 * Created by Administrator on 2016/10/14.
 */
public class TitanRepositoryOperationPatch extends TitanRepositoryOperation{
    public TitanRepositoryOperationPatch(){
        setOperationType(TitanOperationType.patch);
    }
    private Set<String> dropProperties;

    @Override
    public void setOperationType(TitanOperationType operationType) {
        super.setOperationType(TitanOperationType.patch);
    }

    public Set<String> getDropProperties() {
        return dropProperties;
    }

    public void setDropProperties(Set<String> dropProperties) {
        this.dropProperties = dropProperties;
    }
}
