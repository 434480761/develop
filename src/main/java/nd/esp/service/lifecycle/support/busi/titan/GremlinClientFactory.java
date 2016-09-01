package nd.esp.service.lifecycle.support.busi.titan;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import nd.esp.service.lifecycle.support.Constant;

import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.Cluster.Builder;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.driver.ResultSet;
import org.apache.tinkerpop.gremlin.driver.ser.GryoMessageSerializerV1d0;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * This Component exists to provide other beans with access to the TitanGraph
 * instance.
 * @author linsm
 */
@Component
public class GremlinClientFactory {
	// private static final Logger logger =
	// LoggerFactory.getLogger(GremlinClientFactory.class);

	private static Client client;

	@PostConstruct
	public void init() {
		Cluster cluster = null;
		// TODO get from config
		String address = "172.24.133.99";
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
	public static Client getSingleClient() {
		return client;
	}

	public static Client getSearchClient() {
		return client;
	}


	/****************************** TEST ********************************/
	public static void main(String[] args) {
		GremlinClientFactory factory = new GremlinClientFactory();
		factory.init();
		Client client = factory.getSearchClient();
		String script = "g.V().has('assets','identifier','66bc72f9-4115-4d42-ad10-fd18ab1cc2f8').has('lc_enable', lc_enable)";
		Map<String, Object> param = new HashMap<>();
		param.put("lc_enable", new Boolean(false));
		ResultSet submit = client.submit(script, param);
		Iterator<Result> iterator = submit.iterator();
		while (iterator.hasNext()) {
			Result next = iterator.next();
			System.out.println(next);
		}
	}
}