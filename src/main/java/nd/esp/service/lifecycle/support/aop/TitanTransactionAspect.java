package nd.esp.service.lifecycle.support.aop;

import nd.esp.service.lifecycle.support.busi.titan.tranaction.TitanTransactionCollection;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Random;
import java.util.UUID;

/**
 * Created by Administrator on 2016/9/12.
 */
@Aspect
@Component
@Order
public class TitanTransactionAspect {

    @Autowired
    private TitanTransactionCollection titanTransactionCollection;

    @Pointcut("@annotation(nd.esp.service.lifecycle.support.annotation.TitanTransaction)")
    public void performanceAnnon() {

    }

    @Before("performanceAnnon()")
    public void beforeExecuteAnnon() {
        initTitanTransaction();
    }

    @AfterReturning("performanceAnnon()")
    public void afterReturnExecuteAnnon(){
        titanTransactionCollection.commit(TransactionSynchronizationManager.getCurrentTransactionName());
    }

    @AfterThrowing("performanceAnnon()")
    public void exceptionExecuteAnnon(){
        titanTransactionCollection.deleteTransaction(TransactionSynchronizationManager.getCurrentTransactionName());
    }

    private void initTitanTransaction(){
        String name = createTransactionName();
        TransactionSynchronizationManager.setCurrentTransactionName(name);
        titanTransactionCollection.initOneTransaction(name);
    }

    private String createTransactionName(){
        return TransactionSynchronizationManager.getCurrentTransactionName()
                + UUID.randomUUID().toString()+"_titan";
    }
}
