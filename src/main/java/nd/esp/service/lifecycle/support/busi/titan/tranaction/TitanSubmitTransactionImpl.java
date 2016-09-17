package nd.esp.service.lifecycle.support.busi.titan.tranaction;

import nd.esp.service.lifecycle.daos.titan.inter.TitanCommonRepository;
import nd.esp.service.lifecycle.daos.titan.inter.TitanRepository;
import nd.esp.service.lifecycle.daos.titan.inter.TitanRepositoryUtils;
import nd.esp.service.lifecycle.repository.model.ResCoverage;
import nd.esp.service.lifecycle.utils.titan.script.model.EducationToTitanBeanUtils;
import nd.esp.service.lifecycle.utils.titan.script.script.TitanScriptBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.Map;

/**
 * Created by Administrator on 2016/9/12.
 */
@Component
public class TitanSubmitTransactionImpl implements TitanSubmitTransaction {
    @Autowired
    private TitanRepository titanRepository;

    @Autowired
    private TitanRepositoryUtils titanRepositoryUtils;

    @Autowired
    private TitanCommonRepository titanCommonRepository;

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

        TitanScriptBuilder  builder = new TitanScriptBuilder();

        switch (type){
            case add:
                if (operation.getEntity() instanceof ResCoverage){
//                    builder.add(EducationToTitanBeanUtils.toEdge(operation.getEntity()));
                    builder.saveOrUpdate(EducationToTitanBeanUtils.toEdge(operation.getEntity()));
                } else {
                    builder.saveOrUpdate(EducationToTitanBeanUtils.toVertex(operation.getEntity()));
                }
                break;
            case update: titanRepository.update(operation.getEntity());
                break;
            case delete: titanRepository.delete(operation.getEntity().getIdentifier());
                break;
            default:
        }


        Map<String, Object> param = builder.getParam();
        StringBuilder script = builder.getScript();
        if (param != null && param.size() > 0) {
            String id = null;
            try {
                id = titanCommonRepository.executeScriptUniqueString(script.toString(), param);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return true;
    }
}
