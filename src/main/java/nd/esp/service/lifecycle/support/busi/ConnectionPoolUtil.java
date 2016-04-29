package nd.esp.service.lifecycle.support.busi;

import java.util.concurrent.Executor;

import org.apache.http.HttpResponse;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;

public class ConnectionPoolUtil {
    
    private static PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
    
    private static CloseableHttpClient httpClient = null;
    static {
        //连接池最大生成连接数5000
        cm.setMaxTotal(5000);
        // 默认设置route最大连接数为1000
        cm.setDefaultMaxPerRoute(1000);
        ConnectionKeepAliveStrategy kaStrategy = new DefaultConnectionKeepAliveStrategy(){
            @Override
            public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
                long keepAlive = super.getKeepAliveDuration(response, context);
                if (keepAlive == -1) {
                    //如果服务器没有设置keep-alive这个参数，我们就把它设置成2s
                    keepAlive = 2000;
                }
                return keepAlive;
            }

        };
        // 创建httpClient
        httpClient = HttpClients.custom()
              .setConnectionManager(cm)
              .setKeepAliveStrategy(kaStrategy)
              .build();
    }
    
    public static CloseableHttpClient getHttpClient() {
        return httpClient;
    }

}
