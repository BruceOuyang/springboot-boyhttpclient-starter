package club.bugmakers.boy.core;

import club.bugmakers.boy.properties.BoyHttpClientProperties;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.List;
import java.util.Random;

/**
 * 高可用 HttpClient 核心类
 * 支持多服务端点、多代理和重试策略
 */
@Slf4j
@Component
public class BoyHttpClient {

    private final BoyHttpClientProperties properties;
    private final Random random = new Random();

    /**
     * 构造方法
     * @param properties 配置属性
     */
    public BoyHttpClient(BoyHttpClientProperties properties) {
        this.properties = properties;
        log.info("BoyHttpClient initialized with {} endpoints", properties.getEndpoints() != null ? properties.getEndpoints().size() : 0);
    }

    /**
     * 发送 GET 请求
     * @param endpointId 服务端点ID
     * @param path 请求路径
     * @return 响应结果
     * @throws IOException IO异常
     */
    public String get(String endpointId, String path) throws IOException {
        return executeRequest(endpointId, path, "GET", null);
    }

    /**
     * 发送 POST 请求
     * @param endpointId 服务端点ID
     * @param path 请求路径
     * @param body 请求体
     * @return 响应结果
     * @throws IOException IO异常
     */
    public String post(String endpointId, String path, String body) throws IOException {
        return executeRequest(endpointId, path, "POST", body);
    }

    /**
     * 发送 PUT 请求
     * @param endpointId 服务端点ID
     * @param path 请求路径
     * @param body 请求体
     * @return 响应结果
     * @throws IOException IO异常
     */
    public String put(String endpointId, String path, String body) throws IOException {
        return executeRequest(endpointId, path, "PUT", body);
    }

    /**
     * 发送 DELETE 请求
     * @param endpointId 服务端点ID
     * @param path 请求路径
     * @return 响应结果
     * @throws IOException IO异常
     */
    public String delete(String endpointId, String path) throws IOException {
        return executeRequest(endpointId, path, "DELETE", null);
    }

    /**
     * 执行 HTTP 请求
     * @param endpointId 服务端点ID
     * @param path 请求路径
     * @param method 请求方法
     * @param body 请求体
     * @return 响应结果
     * @throws IOException IO异常
     */
    private String executeRequest(String endpointId, String path, String method, String body) throws IOException {
        BoyHttpClientProperties.ServiceEndpoint endpoint = findEndpoint(endpointId);
        if (endpoint == null) {
            throw new IllegalArgumentException("Endpoint not found: " + endpointId);
        }

        List<String> urls = endpoint.getUrls();
        List<String> proxies = endpoint.getProxies();
        BoyHttpClientProperties.RetryPolicy retryPolicy = endpoint.getRetryPolicy();

        int attempts = 0;
        int maxAttempts = retryPolicy != null ? retryPolicy.getMaxAttempts() : 3;
        long backoffMs = retryPolicy != null ? retryPolicy.getBackoffMs() : 100;
        double multiplier = retryPolicy != null ? retryPolicy.getMultiplier() : 1.5;

        while (attempts < maxAttempts) {
            int urlIndex = random.nextInt(urls.size());
            String baseUrl = urls.get(urlIndex);
            String fullUrl = baseUrl + (path.startsWith("/") ? path : "/" + path);

            Proxy proxy = null;
            String proxyInfo = "none";
            if (proxies != null && !proxies.isEmpty()) {
                int proxyIndex = random.nextInt(proxies.size());
                String proxyStr = proxies.get(proxyIndex);
                proxy = createProxy(proxyStr);
                proxyInfo = proxyStr;
            }

            log.info("Attempt {}/{} for {} {} via proxy {}", attempts + 1, maxAttempts, method, fullUrl, proxyInfo);
            
            try {
                String result = doRequest(fullUrl, method, body, proxy);
                log.info("Request successful: {} {}", method, fullUrl);
                return result;
            } catch (IOException e) {
                attempts++;
                log.warn("Attempt {}/{} failed: {} {}. Error: {}", attempts, maxAttempts, method, fullUrl, e.getMessage());
                if (attempts >= maxAttempts) {
                    log.error("Max retry attempts reached for endpoint: {}", endpointId);
                    throw e;
                }
                long sleepTime = (long) (backoffMs * Math.pow(multiplier, attempts - 1));
                log.info("Backing off for {}ms before next attempt", sleepTime);
                backoff(backoffMs, multiplier, attempts);
            }
        }

        throw new IOException("Max retry attempts reached for endpoint: " + endpointId);
    }

    /**
     * 执行具体的 HTTP 请求
     * @param url 请求URL
     * @param method 请求方法
     * @param body 请求体
     * @param proxy 代理
     * @return 响应结果
     * @throws IOException IO异常
     */
    private String doRequest(String url, String method, String body, Proxy proxy) throws IOException {
        URLConnection connection;
        if (proxy != null) {
            connection = new URL(url).openConnection(proxy);
        } else {
            connection = new URL(url).openConnection();
        }

        HttpURLConnection httpConnection = (HttpURLConnection) connection;
        httpConnection.setRequestMethod(method);
        httpConnection.setDoOutput(true);
        httpConnection.setRequestProperty("Content-Type", "application/json");

        if (body != null) {
            try (OutputStream os = httpConnection.getOutputStream()) {
                os.write(body.getBytes());
                os.flush();
            }
        }

        int responseCode = httpConnection.getResponseCode();
        try (InputStream is = responseCode >= 400 ? httpConnection.getErrorStream() : httpConnection.getInputStream()) {
            byte[] buffer = new byte[1024];
            int len;
            StringBuilder response = new StringBuilder();
            while ((len = is.read(buffer)) != -1) {
                response.append(new String(buffer, 0, len));
            }

            if (responseCode >= 400) {
                throw new IOException("HTTP error: " + responseCode + " - " + response.toString());
            }

            return response.toString();
        } finally {
            httpConnection.disconnect();
        }
    }

    /**
     * 查找服务端点
     * @param endpointId 服务端点ID
     * @return 服务端点配置
     */
    private BoyHttpClientProperties.ServiceEndpoint findEndpoint(String endpointId) {
        if (properties.getEndpoints() == null) {
            return null;
        }
        return properties.getEndpoints().stream()
                .filter(endpoint -> endpointId.equals(endpoint.getId()))
                .findFirst()
                .orElse(null);
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
     * 退避等待
     * @param backoffMs 基础退避时间
     * @param multiplier 退避乘数
     * @param attempt 当前尝试次数
     */
    private void backoff(long backoffMs, double multiplier, int attempt) {
        try {
            long sleepTime = (long) (backoffMs * Math.pow(multiplier, attempt - 1));
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
