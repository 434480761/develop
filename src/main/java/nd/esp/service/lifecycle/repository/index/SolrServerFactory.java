package nd.esp.service.lifecycle.repository.index;


import java.util.Map;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrServer;
import org.springframework.core.io.ClassPathResource;

import com.google.common.collect.Maps;
import nd.esp.service.lifecycle.repository.common.Constant;

/**
 * 类描述：SolrServerFactory </br>
 * 修改人： Rainy(yang.lin)</br>
 * 创建时间：2015年2月28日 上午11:10:42</br>
 * 修改备注： </br>
 * @version</br>
 */
public class SolrServerFactory {
	private SolrServerConfiguration config;
	private static SolrServerFactory factory;
	private CloudSolrServer  server;
	
	private final static Map<String, SolrServer> servers = Maps
			.newConcurrentMap();

	private SolrServerFactory(SolrServerConfiguration config) {
		super();
		this.config = config;
		server = new CloudSolrServer(config.getZkHostsStr());
		server.setDefaultCollection(config.collectionName);
	}

	public SolrServerConfiguration getConfig() {
		return config;
	}

	public void setConfig(SolrServerConfiguration config) {
		this.config = config;
	}
	
	public SolrServer getSolrServer(String collectionName) {
		CloudSolrServer cloudSolrServer = null;
		if (!servers.containsKey(collectionName)) {
			synchronized (SolrServerFactory.class) {
				if (!servers.containsKey(collectionName)) {
					cloudSolrServer = new CloudSolrServer(config.getZkHostsStr());
					cloudSolrServer.setDefaultCollection(collectionName);
					servers.put(collectionName, cloudSolrServer);
				}
			}
			return cloudSolrServer;
		} else {
			return servers.get(collectionName);
		}
	}

	public SolrServer getUpdateSolrServer(String collectionName) {
		ConcurrentUpdateSolrServer solrServer = null;
		if (!servers.containsKey(collectionName)) {
			synchronized (SolrServerFactory.class) {
				if (!servers.containsKey(collectionName)) {
					String serverUrl = this.config.getServerUrls().iterator().next();
					solrServer = new ConcurrentUpdateSolrServer(serverUrl, 256, 8);
					solrServer.setConnectionTimeout(this.config.getConnectionTimeout());
					solrServer.setSoTimeout(this.config.getReadTimeout());
					servers.put(serverUrl+"_"+Constant.AUTO_UPDATE + collectionName,
							solrServer);
				}
			}
			return solrServer;
		} else {
			return servers.get(Constant.AUTO_UPDATE + collectionName);
		}
	}
	
	public static SolrServerFactory getSingleton(){
		if(factory!=null){
			return factory;
		}else{
			synchronized (SolrServerFactory.class) {
				if(factory!=null){
					return factory;
				}else{
					SolrServerConfiguration config = new SolrServerConfiguration.Builder(
							new ClassPathResource(SolrServerConfiguration.CONFIG_NAME)).build();
					factory = new SolrServerFactory(config);
					return factory;
				}
			}
		}
	}

	public SolrServer getDefaulSolrServer() {
		return server;
	}
}
