package nd.esp.service.lifecycle.support.busi.titan.tranaction;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Administrator on 2016/9/12.
 */
public class TitanTransaction {
    private static final int MAX_RETRY_TIMES = 3;
    private static final long MAX_TIMEOUT = 1000 * 60;
    private LinkedList<TitanRepositoryOperation> repositoryOperations;
    private int retryTimes ;
    private long crateTime;
    public TitanTransaction(){
        repositoryOperations = new LinkedList<>();
        retryTimes = 0;
        crateTime = System.currentTimeMillis();
    }

    public void addNextStep(TitanRepositoryOperation titanRepositoryOperation){
        repositoryOperations.add(titanRepositoryOperation);
    }

    public boolean isAvailable(){
        return MAX_RETRY_TIMES >= retryTimes && System.currentTimeMillis() - crateTime < MAX_TIMEOUT;
    }

    public LinkedList<TitanRepositoryOperation> getAllStep(){
        return repositoryOperations;
    }

    public void addRetryTimes(){
        retryTimes ++ ;
    }
}
