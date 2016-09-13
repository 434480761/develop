package nd.esp.service.lifecycle.support.busi.titan.tranaction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/9/12.
 */
@Component
public class TitanTransactionCollection {
    private static Map<String, TitanTransaction> transactionMap = new HashMap<>();

    @Autowired
    private TitanSubmitTransaction titanSubmitTransaction;

    public void initOneTransaction(String transactionName){
        transactionMap.put(transactionName, new TitanTransaction());
        deleteDirtyTransaction();
    }

    public void addOneStep(String transactionName, TitanRepositoryOperation repositoryOperation){
        TitanTransaction transaction = transactionMap.get(transactionName);
        if (transaction == null){
            return;
        }

        transaction.addNextStep(repositoryOperation);
    }

    public void submit(String transactionName){
        TitanTransaction titanTransaction = transactionMap.get(transactionName);
        if (titanTransaction == null){
            return;
        }

        titanSubmitTransaction.submit(titanTransaction);

        transactionMap.remove(transactionName);
    }

    /**
     * 清楚因为异常等终端无法正常删除的事务
     * */
    private void deleteDirtyTransaction(){
        for (String transactionName : transactionMap.keySet()){
            TitanTransaction titanTransaction = transactionMap.get(transactionName);
            if (!titanTransaction.isAvailable()){
                transactionMap.remove(transactionName);
            }
        }
    }
}
