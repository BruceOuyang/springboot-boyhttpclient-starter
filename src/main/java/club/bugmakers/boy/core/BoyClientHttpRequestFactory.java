package club.bugmakers.boy.core;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.*;
import java.util.List;
import java.util.Random;

/**
 * 自定义 ClientHttpRequestFactory
 * 支持多代理配置和随机选择
 */
@Slf4j
public class BoyClientHttpRequestFactory extends SimpleClientHttpRequestFactory {

    private List<String> proxies;
    private final Random random = new Random();

    /**
     * 构造方法
     */
    public BoyClientHttpRequestFactory() {
    }

    /**
     * 构造方法
     * @param proxies 代理列表，格式为 host:port
     */
    public BoyClientHttpRequestFactory(List<String> proxies) {
        this.proxies = proxies;
    }

    /**
     * 创建 HTTP 连接
     * @param uri URI
     * @param httpMethod HTTP 方法
     * @return HTTP 连接
     * @throws IOException IO 异常
     */
    @Override
    protected HttpURLConnection openConnection(URL url, Proxy proxy) throws IOException {
        // 如果指定了代理，则使用指定的代理
        if (proxy != null) {
            log.info("Using specified proxy {} for request to {}", proxy.address(), url);
            return super.openConnection(url, proxy);
        }

        // 如果配置了代理列表，则随机选择一个代理
        if (proxies != null && !proxies.isEmpty()) {
            int proxyIndex = random.nextInt(proxies.size());
            String proxyStr = proxies.get(proxyIndex);
            Proxy selectedProxy = createProxy(proxyStr);
            log.info("Selected proxy {} for request to {}", proxyStr, url);
            return super.openConnection(url, selectedProxy);
        }

        // 否则使用默认连接
        log.debug("No proxy configured for request to {}", url);
        return super.openConnection(url, null);
    }

    /**
     * 创建代理
     * @param proxyStr 代理字符串，格式为 host:port
     * @return 代理对象
     */
    private Proxy createProxy(String proxyStr) {
        try {
            String[] parts = proxyStr.split(":");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid proxy format: " + proxyStr);
            }
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);
            return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid proxy configuration: " + proxyStr, e);
        }
    }

    /**
     * 获取代理列表
     * @return 代理列表
     */
    public List<String> getProxies() {
        return proxies;
    }

    /**
     * 设置代理列表
     * @param proxies 代理列表
     */
    public void setProxies(List<String> proxies) {
        this.proxies = proxies;
    }
}
