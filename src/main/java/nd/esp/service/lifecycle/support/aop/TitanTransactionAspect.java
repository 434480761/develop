package nd.esp.service.lifecycle.support.aop;

import nd.esp.service.lifecycle.support.busi.titan.tranaction.TitanTransactionCollection;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Random;
import java.util.UUID;

/**
 * Created by Administrator on 2016/9/12.
 * 切面的执行顺序必须在@Transaction之后
 */
@Aspect
@Component
@Order
public class TitanTransactionAspect {
    private final static Logger LOG = LoggerFactory
            .getLogger(TitanTransactionAspect.class);

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
    public void afterReturnExecuteAnnon(JoinPoint point){
        Signature signature = point.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        String className = methodSignature.getMethod().getDeclaringClass().getPackage().getName();
        String method =  methodSignature.getMethod().getName();
        String currentTransactionName = TransactionSynchronizationManager.getCurrentTransactionName();
        //防止被方法中的其它titanTransaction事务提交
        if (currentTransactionName.contains(className) && currentTransactionName.contains(method)) {
            titanTransactionCollection.commit(TransactionSynchronizationManager.getCurrentTransactionName());
        }
    }

    @AfterThrowing("performanceAnnon()")
    public void exceptionExecuteAnnon(){
        titanTransactionCollection.deleteTransaction(TransactionSynchronizationManager.getCurrentTransactionName());
    }

    /**
     * 事务名由三部分组成
     * 1、方法名（包括类名和包名），用防止嵌套的titan_transaction错误提交
     * 2、UUID，唯一判断一个事务
     * 3、_titan标记该事务被titan事务管理
     * */
    private void initTitanTransaction(){
        String oldName = TransactionSynchronizationManager.getCurrentTransactionName();
        if (oldName != null &&  oldName.endsWith("_titan")){
            //TODO 稳定去掉日志
            LOG.info("有内嵌titan事务 {}",oldName);
            return;
        }
        String name = TransactionSynchronizationManager.getCurrentTransactionName()
                + UUID.randomUUID().toString()+"_titan";

        TransactionSynchronizationManager.setCurrentTransactionName(name);
        titanTransactionCollection.initOneTransaction(name);
    }
}
