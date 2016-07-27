package nd.esp.service.lifecycle.support.busi.titan;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import nd.esp.service.lifecycle.daos.titan.inter.TitanRepository;
import nd.esp.service.lifecycle.support.Constant;

import nd.esp.service.lifecycle.support.StaticDatas;
import nd.esp.service.lifecycle.utils.SpringContextUtil;
import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.Cluster.Builder;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.driver.ResultSet;
import org.apache.tinkerpop.gremlin.driver.ser.GryoMessageSerializerV1d0;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * This Component exists to provide other beans with access to the TitanGraph
 * instance.
 * @author linsm
 */
@Component
public class GremlinClientFactory implements ApplicationContextAware {
//	 private static final Logger logger =
//	 LoggerFactory.getLogger(GremlinClientFactory.class);

	private static Client client;

	public static void init() {
		Cluster cluster = null;
		// TODO get from config
		String address = Constant.TITAN_DOMAIN;
		int port = 8182;
		GryoMessageSerializerV1d0 serializerClass = null;
		Builder clusterBuilder = null;
		Map<String, Object> configMap = null;

		// This is required so that the result vertex can be serialized to
		// string
		serializerClass = new GryoMessageSerializerV1d0();
		configMap = new HashMap<String, Object>();
		configMap.put("serializeResultToString", "true");
		configMap.put("bufferSize", "819200");
		serializerClass.configure(configMap, null);

		// build cluster configuration
		clusterBuilder = Cluster.build(address);
		clusterBuilder.port(port);
		clusterBuilder.serializer(serializerClass);
		
		clusterBuilder.minConnectionPoolSize(200);
        clusterBuilder.maxConnectionPoolSize(200);
        clusterBuilder.nioPoolSize(48);
        clusterBuilder.workerPoolSize(48);
        clusterBuilder.resultIterationBatchSize(20);
		clusterBuilder.maxContentLength(655360);

		// create a cluster instance
		cluster = clusterBuilder.create();
		client = cluster.connect();
		// client = Cluster.build("192.168.19.128").create().connect();
	}

	/**
	 * 提供连接客户对象（单例）
	 * @return
	 */
	public static Client getGremlinClient() {
		return client;
	}

	private static Client reConnectServer(){
		try {
			client.close();
		} catch (Exception ex){
			ex.printStackTrace();
		}
		init();
		return client;
	}

	private static boolean isConnection(){
		String script = "1 + 1";
		if(client == null){
			return false;
		}
		try {
			ResultSet resultSet = client.submit(script);
			Iterator<Result> iterator = resultSet.iterator();
			if (iterator.hasNext()) {
				Integer value = iterator.next().getInt();
				if(value == 2){
					return true;
				}
			}
		} catch (RuntimeException ex){
			return false;
		}
		return false;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		Thread thread = new Thread(new Runnable() {
			int sleepTime = 300000;
			@Override
			public void run() {
				while (true){
					if(isConnection()){
						sleepTime = 1000 * 60;
					} else {
						reConnectServer();
						sleepTime = 1000 * 30;
					}
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		thread.start();
	}


	/****************************** TEST ********************************/
	public static void main(String[] args) {
		init();
		System.out.println(isConnection());
		System.out.println(isConnection());
		System.out.println(reConnectServer());
		System.out.println(isConnection());
	}
}