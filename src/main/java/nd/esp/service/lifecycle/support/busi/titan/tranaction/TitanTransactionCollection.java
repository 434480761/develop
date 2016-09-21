package nd.esp.service.lifecycle.support.busi.titan.tranaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Administrator on 2016/9/12.
 */
@Component
public class TitanTransactionCollection {
    private static ConcurrentHashMap<String, TitanTransaction> transactionMap = new ConcurrentHashMap<>();
    private static final Logger LOG = LoggerFactory
            .getLogger(TitanTransactionCollection.class);
    @Autowired
    private TitanSubmitTransaction titanSubmitTransaction;

    /**
     * 对事务进行初始化
     * */
    public void initOneTransaction(String transactionName){
        transactionMap.put(transactionName, new TitanTransaction());
        if (transactionMap.size() > 1000){
            LOG.warn("titan transaction容器不正常，容器累积事务数:{}",transactionMap.size());
        }
    }

    /**
     * 通过事务名增加事务中的一个步骤
     * */
    public void addOneStep(String transactionName, TitanRepositoryOperation repositoryOperation){
        TitanTransaction transaction = transactionMap.get(transactionName);
        transaction.addNextStep(repositoryOperation);
    }

    /**
     * 提交事务
     * */
    public void commit(String transactionName){
        TitanTransaction titanTransaction = transactionMap.get(transactionName);
        /**
         * TODO 在提交脚本前先解析出对应的资源类型和资源ID，解析策略，再资源；在techInfo、coverage等；最后通过删除资源的ID确定
         * 是否会出现在同一个事务中包含有对多个资源进行操作的
         * */

        titanSubmitTransaction.submit(titanTransaction);

        deleteTransaction(transactionName);
    }

    /**
     * 删除事务
     * */
    public void deleteTransaction(String transactionName){
        transactionMap.remove(transactionName);
    }

    /**
     * 清楚因为异常等无法正常删除的事务
     * */
    public void deleteDirtyTransaction(){
        for (String transactionName : transactionMap.keySet()){
            TitanTransaction titanTransaction = transactionMap.get(transactionName);
            if (!titanTransaction.isAvailable()){
                transactionMap.remove(transactionName);
            }
        }
    }
}
