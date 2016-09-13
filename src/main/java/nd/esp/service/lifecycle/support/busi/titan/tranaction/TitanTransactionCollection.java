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
        transaction.addNextStep(repositoryOperation);
    }

    public void submit(String transactionName){
        TitanTransaction titanTransaction = transactionMap.get(transactionName);
        /**
         * TODO 在提交脚本前先解析出对应的资源类型和资源ID，解析策略，先资源；在techInfo、coverage等；最后通过删除资源的ID确定
         * 是否会出现在同一个事务中包含有对多个资源进行操作的
         * */

        titanSubmitTransaction.submit(titanTransaction);

        transactionMap.remove(transactionName);
    }

    /**
     * 清楚因为异常等无法正常删除的事务
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
