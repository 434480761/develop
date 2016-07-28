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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	private final static Logger LOG = LoggerFactory.getLogger(GremlinClientFactory.class);

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
			if(client != null){
				client.close();
			}
		} catch (Exception ex){
			ex.printStackTrace();
		}

		init();
		return client;
	}

	private static boolean isConnection(){
		String script = "1 + 1";
		if(client == null){
			LOG.info("titan_service_disconnect_client_null");
			return false;
		}
		Integer value = 0;
		try {
			ResultSet resultSet = client.submit(script);
			Iterator<Result> iterator = resultSet.iterator();
			if (iterator.hasNext()) {
				value = iterator.next().getInt();
			}
		} catch (RuntimeException ex){
			LOG.info("titan_service_disconnect_exception");
			return false;
		}

		if(value == 2){
			LOG.info("titan_service_connect_success");
			return true;
		} else {
			LOG.info("titan_service_disconnect");
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
						sleepTime = 1000 * 60 * 5;
					} else {
						reConnectServer();
						sleepTime = 1000 * 60;
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