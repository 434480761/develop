package nd.esp.service.lifecycle.support.aop;

import nd.esp.service.lifecycle.support.busi.titan.tranaction.TitanTransactionCollection;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Random;

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
        titanTransactionCollection.submit(TransactionSynchronizationManager.getCurrentTransactionName());
    }

    private void initTitanTransaction(){
        String name = createTransactionName();
        TransactionSynchronizationManager.setCurrentTransactionName(name);
        titanTransactionCollection.initOneTransaction(name);
    }

    private String createTransactionName(){
        Random random = new Random();
        return TransactionSynchronizationManager.getCurrentTransactionName()
                + System.currentTimeMillis() + random.nextInt(10000) + "_titan";
    }
}
