package nd.esp.service.lifecycle.support.busi.titan.tranaction;

import nd.esp.service.lifecycle.daos.titan.inter.TitanCommonRepository;
import nd.esp.service.lifecycle.daos.titan.inter.TitanRepository;
import nd.esp.service.lifecycle.repository.model.ResCoverage;
import nd.esp.service.lifecycle.repository.model.ResourceCategory;
import nd.esp.service.lifecycle.repository.model.ResourceRelation;
import nd.esp.service.lifecycle.repository.model.TechInfo;
import nd.esp.service.lifecycle.utils.titan.script.model.EducationToTitanBeanUtils;
import nd.esp.service.lifecycle.utils.titan.script.script.TitanScriptBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.Map;

/**
 * Created by Administrator on 2016/9/12.
 */
@Component
public class TitanSubmitTransactionImpl implements TitanSubmitTransaction {
    private static final Logger LOG = LoggerFactory
            .getLogger(TitanSubmitTransactionImpl.class);
    @Autowired
    private TitanRepository titanRepository;

    @Autowired
    private TitanCommonRepository titanCommonRepository;

    @Override
    public boolean submit(TitanTransaction transaction) {
        //TODO 可以做事务的重试
        LinkedList<TitanRepositoryOperation> repositoryOperations = transaction.getAllStep();
        boolean success = true;
        long t1 =  System.currentTimeMillis();
        submit(repositoryOperations);
        long t2 =  System.currentTimeMillis();
        System.out.println(t2 - t1);

        //TODO 每个事务中需要获取资源的类型和ID，方案一：在事务名中存放类型和ID；方案二：在需要的时候再进行解析
        if (!success){
//            titanRepositoryUtils.titanSync4MysqlAdd();
        }

        return true;
    }

    private boolean submit(LinkedList<TitanRepositoryOperation> repositoryOperations){
        TitanScriptBuilder  builder = new TitanScriptBuilder();

        for (TitanRepositoryOperation operation : repositoryOperations) {
            TitanOperationType type = operation.getOperationType();
            switch (type) {
                case add:
                    if (operation.getEntity() instanceof ResCoverage || operation.getEntity() instanceof ResourceCategory ||operation.getEntity() instanceof TechInfo) {
                        builder.addBeforeCheckExist(EducationToTitanBeanUtils.toVertex(operation.getEntity()));
                        builder.addOrUpdate(EducationToTitanBeanUtils.toEdge(operation.getEntity()));
                    } else if (operation.getEntity() instanceof ResourceRelation){
                        builder.addOrUpdate(EducationToTitanBeanUtils.toEdge(operation.getEntity()));
                    } else {
                        builder.addOrUpdate(EducationToTitanBeanUtils.toVertex(operation.getEntity()));
                    }
                    break;
                case update:
                    if (operation.getEntity() instanceof ResCoverage || operation.getEntity() instanceof ResourceCategory) {
                        builder.addBeforeCheckExist(EducationToTitanBeanUtils.toVertex(operation.getEntity()));
                        builder.addOrUpdate(EducationToTitanBeanUtils.toEdge(operation.getEntity()));
                    }else if (operation.getEntity() instanceof ResourceRelation){
                        builder.addOrUpdate(EducationToTitanBeanUtils.toEdge(operation.getEntity()));
                    }  else {
                        builder.addOrUpdate(EducationToTitanBeanUtils.toVertex(operation.getEntity()));
                    }
                    break;
                case delete:
                    titanRepository.delete(operation.getEntity().getIdentifier());
                    break;
                default:
                    LOG.info("没有对应的处理方法");
            }
        }

        builder.scriptEnd();

        Map<String, Object> param = builder.getParam();
        StringBuilder script = builder.getScript();
        if (param != null && param.size() > 0) {
            String id = null;
            try {
                id = titanCommonRepository.executeScriptUniqueString(script.toString(), param);
            } catch (Exception e) {
                return false;
            }
        }

        return true;
    }
}
