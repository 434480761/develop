package nd.esp.service.lifecycle.support.rabbitmq;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.lcmq.component.listener.OnMessageListener;
import com.lcmq.component.mq.RabbitMqConsumer;
/**
 * 消息队列 -- 消费者执行器
 * @author xiezy
 * @date 2016年8月1日
 */
//@Component
public class ConsumerExecutor {
	private static final Logger logger = LoggerFactory.getLogger(ConsumerExecutor.class);
	
	private ExecutorService excutorService = Executors.newCachedThreadPool();
	
	public void startConsumer(final String queueName, final OnMessageListener onMessageListener) {
		this.excutorService.submit(new Thread() {
			public void run() {
				RabbitMqConsumer rabbitMqConsumer = null;
				try {
					logger.info("consumer start");
					rabbitMqConsumer = new RabbitMqConsumer(queueName, onMessageListener);
					rabbitMqConsumer.start();
				} catch (Exception e) {
					logger.error("consumer execute exception", e);
				} finally {
					logger.info("consumer stop");
					if(rabbitMqConsumer != null){
						rabbitMqConsumer.stop();
					}
				}
			}
		});
	}
}
