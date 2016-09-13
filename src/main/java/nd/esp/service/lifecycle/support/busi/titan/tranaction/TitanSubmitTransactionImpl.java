package nd.esp.service.lifecycle.support.busi.titan.tranaction;

import nd.esp.service.lifecycle.daos.titan.inter.TitanRepository;
import nd.esp.service.lifecycle.daos.titan.inter.TitanRepositoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedList;

/**
 * Created by Administrator on 2016/9/12.
 */
@Component
public class TitanSubmitTransactionImpl implements TitanSubmitTransaction {
    @Autowired
    private TitanRepository titanRepository;

    @Autowired
    private TitanRepositoryUtils titanRepositoryUtils;

    @Override
    public boolean submit(TitanTransaction transaction) {
        //TODO 可以做事务的重试
        LinkedList<TitanRepositoryOperation> repositoryOperations = transaction.getAllStep();
        boolean success = true;
        for (TitanRepositoryOperation operation : repositoryOperations){
            success = submit(operation);
            if (!success){
                break;
            }
        }

        //TODO 每个事务中需要获取资源的类型和ID，方案一：在事务名中存放类型和ID；方案二：在需要的时候再进行解析
        if (!success){
//            titanRepositoryUtils.titanSync4MysqlAdd();
        }

        return true;
    }

    private boolean submit(TitanRepositoryOperation operation){
        TitanOperationType type = operation.getOperationType();
        switch (type){
            case add: titanRepository.add(operation.getEntity());
            case update: titanRepository.update(operation.getEntity());
            case delete:
        }

        return true;
    }
}
