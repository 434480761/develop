package nd.esp.service.lifecycle.support.busi.titan;

import java.util.*;

import nd.esp.service.lifecycle.support.Constant;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.utils.StringUtils;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.Cluster.Builder;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.driver.ResultSet;
import org.apache.tinkerpop.gremlin.driver.ser.GryoMessageSerializerV1d0;
import org.apache.tinkerpop.shaded.minlog.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * This Component exists to provide other beans with access to the TitanGraph
 * instance.
 * 
 * @author linsm
 */
@Component
public class GremlinClientFactory implements ApplicationContextAware {
	private final static Logger LOG = LoggerFactory
			.getLogger(GremlinClientFactory.class);

	private static Client singleClient; // 用于除查询外的其它操作；
	private static Client searchClient;// 用于查询操作；

	/**
	 * 用于除查询外的其它操作
	 * 
	 * @return
	 */
	public static Client getSingleClient() {
		return singleClient;
	}

	/**
	 * 用于查询操作；
	 * 
	 * @return
	 */
	public static Client getSearchClient() {
		return searchClient;
	}

	private static void setSingleClient(Client client) {
		singleClient = client;
	}

	private static void setSearchClient(Client client) {
		searchClient = client;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		Thread thread = new Thread(new Runnable() {
			int sleepTime = 300000;

			@Override
			public void run() {
				while (true) {
					boolean ClientAllConnectSuccess = connect(ClientType.single)
							&& connect(ClientType.search);
					if (ClientAllConnectSuccess) {
						sleepTime = 1000 * 60 * 5;
					} else {
						sleepTime = 1000 * 60;
					}
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
						LOG.error(e.getLocalizedMessage());
					}
				}
			}

			private boolean connect(ClientType single) {
				if (single.isConnection()) {
					return true;
				} else {
					single.reConnectServer();
					return false;
				}
			}
		});
		thread.start();
	}

	/**
	 * 客户端类型
	 * 
	 * @author linsm
	 *
	 */
	private static enum ClientType {
		single {
			@Override
			public Builder builder(String address) {
				Builder singleBuilder = defaulBuilder();
				List<String> addressList = checkAndGetAddress(address);
				singleBuilder.addContactPoint(addressList.get(RandomUtils.nextInt(addressList.size())));// only add one
				// address;
				singleBuilder.minConnectionPoolSize(50);
				singleBuilder.maxConnectionPoolSize(50);
				singleBuilder.nioPoolSize(24);
				singleBuilder.workerPoolSize(24);
				return singleBuilder;
			}

			@Override
			public Client getClient() {
				return getSingleClient();
			}

			@Override
			public void setClient(Client client) {
				setSingleClient(client);
			}
		}, // 单个节点（暂时固定一台机器）
		search {
			@Override
			public Builder builder(String address) {
				Builder searchBuilder = defaulBuilder();
				searchBuilder.addContactPoints(checkAndGetAddress(address)
						.toArray(new String[1]));
				searchBuilder.minConnectionPoolSize(100);
				searchBuilder.maxConnectionPoolSize(100);
				searchBuilder.nioPoolSize(48);
				searchBuilder.workerPoolSize(48);
				return searchBuilder;
			}

			@Override
			public Client getClient() {
				return getSearchClient();
			}

			@Override
			public void setClient(Client client) {
				setSearchClient(client);
			}
		}, // 用于查询接口（使用集群中的所有结点）
		;

		/**
		 * 对所有client 默认的配置
		 * 
		 * @return
		 */
		private static Builder defaulBuilder() {
			int port = 8182;
			// This is required so that the result vertex can be serialized to
			// string
			GryoMessageSerializerV1d0 serializerClass = new GryoMessageSerializerV1d0();
			Map<String, Object> configMap = new HashMap<String, Object>();
			configMap.put("serializeResultToString", "true");
			configMap.put("bufferSize", "819200");
			serializerClass.configure(configMap, null);

			// build cluster configuration
			Builder clusterBuilder = Cluster.build();
			clusterBuilder.port(port);
			clusterBuilder.serializer(serializerClass);

			clusterBuilder.resultIterationBatchSize(20);
			clusterBuilder.maxContentLength(655360);
			return clusterBuilder;
		}

		/**
		 * 重新连接服务
		 * 
		 * @param address
		 */
		public void reConnectServer(String address) {
			Client client = getClient();
			try {
				if (client != null) {
					client.close();
				}
			} catch (Exception ex) {
				Log.error(ex.getLocalizedMessage());
			}
			init(address);
		}

		/**
		 * 重新连接服务
		 */
		public void reConnectServer() {
			reConnectServer(null);
		}

		/**
		 * 检查并获取titan节点地址
		 * 
		 * @param address
		 * @return
		 */
		protected List<String> checkAndGetAddress(String address) {
			if (StringUtils.isEmpty(address)) {
				address = Constant.TITAN_DOMAIN;
			}
			if (StringUtils.isEmpty(address)) {
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
						"LC/titan/client", "addres conf is empty");
			}
			String[] addressChunks = address.split(",");
			if (addressChunks == null || addressChunks.length == 0) {
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
						"LC/titan/client", "addres conf is empty");
			}

			LinkedHashSet<String> addressSet = new LinkedHashSet<String>();
			for (String oneAddress : addressChunks) {
				if (oneAddress != null) {
					String validAddress = oneAddress.trim();
					if (StringUtils.isNotEmpty(validAddress)) {
						addressSet.add(validAddress);
					}
				}

			}
			if (addressSet.size() == 0) {
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
						"LC/titan/client", "not have valid address");
			}
			LOG.info("addressSet: " + addressSet);
			return new ArrayList<String>(addressSet);
		}

		/**
		 * 判断是否连接
		 * 
		 * @return
		 */
		private boolean isConnection() {
			Client client = getClient();
			String script = "1 + 1";
			if (client == null) {
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
			} catch (RuntimeException ex) {
				LOG.info("titan_service_disconnect_exception");
				return false;
			}

			if (value == 2) {
				LOG.info("titan_service_connect_success");
				return true;
			} else {
				LOG.error("titan_service_disconnect");
			}
			return false;
		}

		/**
		 * 初始化客户端
		 * 
		 * @param address
		 */
		private void init(String address) {
			setClient(builder(address).create().connect());
		}

		public abstract Builder builder(String address);

		public abstract Client getClient();

		public abstract void setClient(Client client);
	}

	/****************************** TEST ********************************/
	public static void main(String[] args) {
		String address = "172.24.133.42 , 172.24.133.99 ";
		ClientType clientType = ClientType.search;
		System.out.println(clientType.isConnection());
		clientType.init(address);
		System.out.println(clientType.isConnection());
		System.out.println(clientType.isConnection());
	}
}