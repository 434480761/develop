package nd.esp.service.lifecycle.support.busi.titan;

import java.util.*;

import nd.esp.service.lifecycle.support.Constant;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.utils.StringUtils;

import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.Cluster.Builder;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.driver.ResultSet;
import org.apache.tinkerpop.gremlin.driver.ser.GryoMessageSerializerV1d0;
import org.apache.tinkerpop.shaded.minlog.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * This Component exists to provide other beans with access to the TitanGraph
 * instance.
 *
 * @author linsm
 */
@Component
public class GremlinClientFactory{
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

	@Scheduled(fixedDelay = 1000 * 60 *2)
	public void titanConnectTimeTask(){
		try {
			connect(ClientType.search);
			connect(ClientType.single);
		} catch (Exception e){
			LOG.error("重连发生异常");
		}

	}

	private boolean connect(ClientType clientType) {
		if (clientType.isConnection()) {
			return true;
		} else {
			return clientType.reConnectServer();
		}
	}

//	public void setApplicationContext(ApplicationContext applicationContext)
//			throws BeansException {
//		Thread thread = new Thread(new Runnable() {
//			int sleepTime = 300000;
//
//			@Override
//			public void run() {
//				while (true) {
//					//优先保证查询 （search and then single) (add by lsm)
//					boolean ClientAllConnectSuccess = connect(ClientType.search)
//							&& connect(ClientType.single);
//					if (ClientAllConnectSuccess) {
//						sleepTime = 1000 * 60 * 5;
//					} else {
//						sleepTime = 1000 * 60;
//					}
//					try {
//						Thread.sleep(sleepTime);
//					} catch (InterruptedException e) {
//						LOG.error(e.getLocalizedMessage());
//					}
//				}
//			}
//
//			private boolean connect(ClientType clientType) {
//				if (clientType.isConnection()) {
//					return true;
//				} else {
//					return clientType.reConnectServer();
//				}
//			}
//		});
//		thread.start();
//	}

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
				// only add one address;
				int index = 0;
				// index=RandomUtils.nextInt(addressList.size());
				singleBuilder.addContactPoint(addressList.get(index));
				checkAndSetPoolSizePoolSize(singleBuilder);
				return singleBuilder;
			}

			@Override
			protected void checkAndSetPoolSizePoolSize(Builder singleBuilder) {
				checkAndSetPoolSizePoolSize(singleBuilder,
						Constant.TITAN_SINGLE_POOL_SIZE);
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
				checkAndSetPoolSizePoolSize(searchBuilder);
				return searchBuilder;
			}

			@Override
			protected void checkAndSetPoolSizePoolSize(
					Builder searchBuilder) {
				checkAndSetPoolSizePoolSize(searchBuilder,Constant.TITAN_SEARCH_POOL_SIZE);
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


		private static final int minConnectionPoolSize = 0;
		private static final int maxConnectionPoolSize = 1;
		private static final int nioPoolSize = 2;
		private static final int workerPoolSize = 3;

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
		 * 读取并验证titan线程池相关配置
		 *
		 * @param titanSearchPoolSize
		 * @return
		 */
		protected void checkAndSetPoolSizePoolSize(Builder searchBuilder, String titanSearchPoolSize) {
			// minConnectionPoolSize,maxConnectionPoolSize,nioPoolSize,workerPoolSize
			if (StringUtils.isEmpty(titanSearchPoolSize)) {
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
						"LC/titan/conf", "pool size conf is empty");
			}
			String[] poolSizeStrings = titanSearchPoolSize.split(",");
			if (poolSizeStrings == null || poolSizeStrings.length != 4) {
				throw new LifeCircleException(
						HttpStatus.INTERNAL_SERVER_ERROR,
						"LC/titan/conf",
						"pool size conf is invalid(minConnectionPoolSize,maxConnectionPoolSize,nioPoolSize,workerPoolSize)");
			}
			List<Integer> poolSizes = new ArrayList<Integer>();
			for (String value : poolSizeStrings) {
				poolSizes.add(Integer.valueOf(value.trim()));
			}

			// valid the conf value;
			for (Integer value : poolSizes) {
				if (value <= 0) {
					throw new LifeCircleException(
							HttpStatus.INTERNAL_SERVER_ERROR, "LC/titan/conf",
							"pool size must be great than 0");
				}
			}

			if (poolSizes.get(minConnectionPoolSize) > poolSizes
					.get(maxConnectionPoolSize)) {
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
						"LC/titan/conf",
						"minConnectionPoolSize must not be greater than maxConnectionPoolSize");
			}

			searchBuilder.minConnectionPoolSize(poolSizes.get(minConnectionPoolSize));
			searchBuilder.maxConnectionPoolSize(poolSizes.get(maxConnectionPoolSize));
			searchBuilder.nioPoolSize(poolSizes.get(nioPoolSize));
			searchBuilder.workerPoolSize(poolSizes.get(workerPoolSize));
		}

		/**
		 * 重新连接服务
		 *
		 * @param address
		 */
		public boolean reConnectServer(String address) {
			Client client = getClient();
			try {
				if (client != null) {
					client.close();
				}
			} catch (Exception ex) {
				Log.error(ex.getLocalizedMessage());
			}
			init(address);
			return isConnection();
		}

		/**
		 * 重新连接服务
		 */
		public boolean reConnectServer() {
			return reConnectServer(null);
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

		protected abstract void checkAndSetPoolSizePoolSize(
				Builder searchBuilder);
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