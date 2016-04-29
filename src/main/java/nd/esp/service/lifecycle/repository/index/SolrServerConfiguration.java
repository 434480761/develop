package nd.esp.service.lifecycle.repository.index;


import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

/**
 * 类描述：SolrJ API SolrServer配置，用于向{@link SolrServerFactory}提供配置参数  </br>
 * 修改人： Rainy(yang.lin)</br>
 * 创建时间：2015年2月27日 下午1:57:43</br>
 * 修改备注： </br>
 * @version</br>
 */
public class SolrServerConfiguration {
	
	private static final Logger logger = LoggerFactory
			.getLogger(SolrServerConfiguration.class);
	/**
	 * 连接等待时长
	 */
	public static final int CONNECTION_TIMEOUT = 500;
	/**
	 * 每个服务器最多并发连接数
	 */
	public static final int MAX_CONNS_PER_HOST = 32;
	/**
	 * 从此客户端发出的最多并发连接数
	 */
	public static final int MAX_TATAL_CONNS = 128;
	/**
	 * 等待响应时长
	 */
	public static final int READ_TIMEOUT = 1500;
	/**
	 * 服务器活动检测时间间隔
	 */
	public static final int ALIVE_CHECK_INTERVAL = 5000;
	
	public static final String COLLECTION_NAME = "solr.collection";
	
	public static final String COMMIT = "solr.commit";
	
	/**
	 * zk主机
	 */
	public static final String ZKHOST="solr.zkHost";
	/**
	 * solr服务地址
	 */
	public static final String SOLRSERVER_URL="solr.server.urls";
	/**
	 * 检索配置
	 */
	public static final String CONFIG_NAME="sdkdb/search.properties";
	
	public String collectionName = "assets"; 
	
	private boolean commit = true;

	private Set<String> serverUrls = new HashSet<String>(); // 服务器连接地址列表
	private int connectionTimeout = CONNECTION_TIMEOUT;
	private int maxConnectionsPerHost = MAX_CONNS_PER_HOST;
	private boolean retry = false;
	private int maxTotalConnections = MAX_TATAL_CONNS;
	private int readTimeout = READ_TIMEOUT;
	private int aliveCheckInterval = ALIVE_CHECK_INTERVAL;
	private Set<String> zkHosts = new HashSet<String>();
	private String zkHostsStr;
	private String solrServerUrls;
	
	
	private SolrServerConfiguration(Builder builder) {
		this.serverUrls = builder.serverUrls;
		this.connectionTimeout = builder.connectionTimeout;
		this.maxConnectionsPerHost = builder.maxConnectionsPerHost;
		this.maxTotalConnections = builder.maxTotalConnections;
		this.readTimeout = builder.readTimeout;
		this.aliveCheckInterval = builder.aliveCheckInterval;
		Properties solrProperties = null;
		try {
			solrProperties = PropertiesLoaderUtils.loadProperties(builder.pathResource);
		} catch (IOException e) {
			throw new ConfigurationException("config init error",e);
		}
		
		try {
		    zkHostsStr = solrProperties.getProperty(ZKHOST);
		    commit = Boolean.valueOf(solrProperties.getProperty(COMMIT, "true"));
		    collectionName = solrProperties.getProperty(COLLECTION_NAME);
			CollectionUtils.addAll(zkHosts,zkHostsStr.split(","));
			solrServerUrls = solrProperties.getProperty(SOLRSERVER_URL);
			CollectionUtils.addAll(serverUrls,solrServerUrls.split(","));
		} catch (Exception e) {
			throw new ConfigurationException("config param format incorrect,please confirm!",e);
		}
		
		if(zkHosts.size() == 0){
			
		    if (logger.isWarnEnabled()) {
                
		        logger.warn("zk host is not set!");
		        
            }
			
			if(serverUrls.size() == 0){
				throw new ConfigurationException("There is no server URL can service,please check config is correct!");
			}
		}
	}

	public Set<String> getServerUrls() {
		return serverUrls;
	}

	public void setServerUrls(Set<String> serverUrls) {
		this.serverUrls = serverUrls;
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public int getMaxConnectionsPerHost() {
		return maxConnectionsPerHost;
	}

	public void setMaxConnectionsPerHost(int maxConnectionsPerHost) {
		this.maxConnectionsPerHost = maxConnectionsPerHost;
	}

	public boolean isRetry() {
		return retry;
	}

	public void setRetry(boolean retry) {
		this.retry = retry;
	}

	public int getMaxTotalConnections() {
		return maxTotalConnections;
	}

	public void setMaxTotalConnections(int maxTotalConnections) {
		this.maxTotalConnections = maxTotalConnections;
	}

	public int getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}

	public int getAliveCheckInterval() {
		return aliveCheckInterval;
	}

	public void setAliveCheckInterval(int aliveCheckInterval) {
		this.aliveCheckInterval = aliveCheckInterval;
	}
	
	public static class Builder{
		private Set<String> serverUrls = new HashSet<String>(); // 服务器连接地址列表
		private int connectionTimeout = CONNECTION_TIMEOUT;
		private int maxConnectionsPerHost = MAX_CONNS_PER_HOST;
		private boolean retry = false;
		private int maxTotalConnections = MAX_TATAL_CONNS;
		private int readTimeout = READ_TIMEOUT;
		private int aliveCheckInterval = ALIVE_CHECK_INTERVAL;
		
		private ClassPathResource pathResource;
		
		public Builder(ClassPathResource pathResource) {
			super();
			this.pathResource = pathResource;
		}
		
		public SolrServerConfiguration build(){
			return new SolrServerConfiguration(this);
		}
		public Builder addServerUrl(String url){
			Builder.this.serverUrls.add(url);
			return this;
		}
		
		public Builder setServerUrls(Set<String> serverUrls){
			Builder.this.serverUrls = serverUrls;
			return this;
		}
		
		public Builder setConnectionTimeout(int onnectionTimeout){
			Builder.this.connectionTimeout = connectionTimeout;
			return this;
		}
		
		public Builder setMaxConnectionsPerHost(int onnectionTimeout){
			Builder.this.maxConnectionsPerHost = maxConnectionsPerHost;
			return this;
		}
		
		public Builder setRetry(boolean retry){
			Builder.this.retry = retry;
			return this;
		}
		
		public Builder setMaxTotalConnections(int maxTotalConnections){
			Builder.this.maxTotalConnections = maxTotalConnections;
			return this;
		}
		
		public Builder setReadTimeout(int readTimeout){
			Builder.this.readTimeout = readTimeout;
			return this;
		}
		
		public Builder setAliveCheckInterval(int aliveCheckInterval){
			Builder.this.aliveCheckInterval = aliveCheckInterval;
			return this;
		}
		
		public Builder setPathResource(ClassPathResource pathResource){
			Builder.this.pathResource = pathResource;
			return this;
		}

		public Set<String> getServerUrls() {
			return serverUrls;
		}

		public int getConnectionTimeout() {
			return connectionTimeout;
		}

		public int getMaxConnectionsPerHost() {
			return maxConnectionsPerHost;
		}

		public boolean isRetry() {
			return retry;
		}

		public int getMaxTotalConnections() {
			return maxTotalConnections;
		}

		public int getReadTimeout() {
			return readTimeout;
		}

		public int getAliveCheckInterval() {
			return aliveCheckInterval;
		}

		public ClassPathResource getPathResource() {
			return pathResource;
		}
	}

	public Set<String> getZkHosts() {
		return zkHosts;
	}

	public void setZkHosts(Set<String> zkHosts) {
		this.zkHosts = zkHosts;
	}

	public String getZkHostsStr() {
		return zkHostsStr;
	}

	public void setZkHostsStr(String zkHostsStr) {
		this.zkHostsStr = zkHostsStr;
	}

	public String getSolrServerUrls() {
		return solrServerUrls;
	}

	public void setSolrServerUrls(String solrServerUrls) {
		this.solrServerUrls = solrServerUrls;
	}

	public boolean isCommit() {
		return commit;
	}

	public void setCommit(boolean commit) {
		this.commit = commit;
	}
}
