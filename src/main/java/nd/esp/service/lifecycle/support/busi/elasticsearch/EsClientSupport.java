package nd.esp.service.lifecycle.support.busi.elasticsearch;

import nd.esp.service.lifecycle.support.Constant;
import nd.esp.service.lifecycle.support.enums.ElasticSearchReservedWords;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.util.concurrent.EsExecutors;

/**
 * 用于管理elasticsearch 客户端
 * 
 * @author linsm
 *
 */
public class EsClientSupport {

	/**
	 * 获取elasticsearch 客户端（新建）
	 * 
	 * @return
	 */
	public static Client getClient() {
		Settings settgins = ImmutableSettings
				.builder()
				.put(ElasticSearchReservedWords.CLUSTER_NAME.toString(),
						Constant.ES_CLUSTER_NAME)
				.put(ElasticSearchReservedWords.CLIENT_TRANSPORT_SNIFF.toString(), true)
				/* .put(EsExecutors.PROCESSORS, 100) */.build();
		@SuppressWarnings("resource")
		TransportClient client = new TransportClient(settgins);
		return client.addTransportAddress(new InetSocketTransportAddress(
				Constant.ES_DOMAIN, Integer.valueOf(Constant.ES_PORT)));

	}

}
