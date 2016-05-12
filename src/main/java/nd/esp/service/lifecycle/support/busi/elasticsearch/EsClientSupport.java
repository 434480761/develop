package nd.esp.service.lifecycle.support.busi.elasticsearch;

import java.net.InetAddress;
import java.net.UnknownHostException;

import nd.esp.service.lifecycle.support.Constant;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.enums.ElasticSearchReservedWords;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用于管理elasticsearch 客户端
 * 
 * @author linsm
 *
 */
public class EsClientSupport {
	private static final Logger logger = LoggerFactory
			.getLogger(EsClientSupport.class);

	/**
	 * 获取elasticsearch 客户端（新建）
	 * 
	 * @return
	 * @throws UnknownHostException 
	 * @throws NumberFormatException 
	 */
	public static Client getClient(){
		Settings settgins = Settings
				.builder()
				.put(ElasticSearchReservedWords.CLUSTER_NAME.toString(),
						Constant.ES_CLUSTER_NAME)
				.put(ElasticSearchReservedWords.CLIENT_TRANSPORT_SNIFF.toString(), true)
				/* .put(EsExecutors.PROCESSORS, 100) */.build();
		TransportClient client = TransportClient.builder().settings(settgins).build();
		try {
			return client.addTransportAddress(new InetSocketTransportAddress(
					InetAddress.getByName(Constant.ES_DOMAIN), Integer.valueOf(Constant.ES_PORT)));
		} catch (NumberFormatException | UnknownHostException e) {
			logger.error(e.getMessage());
			throw new LifeCircleException(e.getMessage());
		}

	}

}
