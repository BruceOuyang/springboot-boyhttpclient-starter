package club.bugmakers.boy.core;

import club.bugmakers.boy.properties.BoyHttpClientProperties;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 高可用 RestTemplate
 * 支持多服务端点、多代理配置和重试策略
 */
@Slf4j
public class BoyRestTemplate extends RestTemplate {

    private final BoyHttpClientProperties properties;
    private final Random random = new Random();

    /**
     * 构造方法
     * @param properties 配置属性
     */
    public BoyRestTemplate(BoyHttpClientProperties properties) {
        this.properties = properties;
        log.info("BoyRestTemplate initialized with {} endpoints", properties.getEndpoints() != null ? properties.getEndpoints().size() : 0);
    }

    /**
     * 构造方法
     * @param properties 配置属性
     * @param requestFactory 请求工厂
     */
    public BoyRestTemplate(BoyHttpClientProperties properties, BoyClientHttpRequestFactory requestFactory) {
        super(requestFactory);
        this.properties = properties;
        log.info("BoyRestTemplate initialized with {} endpoints and custom BoyClientHttpRequestFactory", properties.getEndpoints() != null ? properties.getEndpoints().size() : 0);
    }

    /**
     * 发送 GET 请求
     * @param endpointId 服务端点ID
     * @param path 请求路径
     * @param responseType 响应类型
     * @param <T> 响应类型泛型
     * @return 响应结果
     */
    public <T> T getForObject(String endpointId, String path, Class<T> responseType) {
        return executeWithEndpoint(endpointId, path, HttpMethod.GET, null, null, responseType);
    }

    /**
     * 发送 GET 请求
     * @param endpointId 服务端点ID
     * @param path 请求路径
     * @param uriVariables URI 变量
     * @param responseType 响应类型
     * @param <T> 响应类型泛型
     * @return 响应结果
     */
    public <T> T getForObject(String endpointId, String path, Class<T> responseType, Map<String, ?> uriVariables) {
        return executeWithEndpoint(endpointId, path, HttpMethod.GET, null, uriVariables, responseType);
    }

    /**
     * 发送 POST 请求
     * @param endpointId 服务端点ID
     * @param path 请求路径
     * @param request 请求对象
     * @param responseType 响应类型
     * @param <T> 响应类型泛型
     * @return 响应结果
     */
    public <T> T postForObject(String endpointId, String path, Object request, Class<T> responseType) {
        return executeWithEndpoint(endpointId, path, HttpMethod.POST, request, null, responseType);
    }

    /**
     * 发送 POST 请求
     * @param endpointId 服务端点ID
     * @param path 请求路径
     * @param request 请求对象
     * @param responseType 响应类型
     * @param uriVariables URI 变量
     * @param <T> 响应类型泛型
     * @return 响应结果
     */
    public <T> T postForObject(String endpointId, String path, Object request, Class<T> responseType, Map<String, ?> uriVariables) {
        return executeWithEndpoint(endpointId, path, HttpMethod.POST, request, uriVariables, responseType);
    }

    /**
     * 发送 PUT 请求
     * @param endpointId 服务端点ID
     * @param path 请求路径
     * @param request 请求对象
     */
    public void put(String endpointId, String path, Object request) {
        executeWithEndpoint(endpointId, path, HttpMethod.PUT, request, null, Void.class);
    }

    /**
     * 发送 DELETE 请求
     * @param endpointId 服务端点ID
     * @param path 请求路径
     */
    public void delete(String endpointId, String path) {
        executeWithEndpoint(endpointId, path, HttpMethod.DELETE, null, null, Void.class);
    }

    /**
     * 执行带端点的请求
     * @param endpointId 服务端点ID
     * @param path 请求路径
     * @param method HTTP 方法
     * @param request 请求对象
     * @param uriVariables URI 变量
     * @param responseType 响应类型
     * @param <T> 响应类型泛型
     * @return 响应结果
     */
    private <T> T executeWithEndpoint(String endpointId, String path, HttpMethod method, Object request, Map<String, ?> uriVariables, Class<T> responseType) {
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

            // 配置代理
            if (proxies != null && !proxies.isEmpty() && getRequestFactory() instanceof BoyClientHttpRequestFactory) {
                ((BoyClientHttpRequestFactory) getRequestFactory()).setProxies(proxies);
                log.info("Configured {} proxies for endpoint {}", proxies.size(), endpointId);
            }

            log.info("Attempt {}/{} for {} {} via endpoint {}", attempts + 1, maxAttempts, method, fullUrl, endpointId);
            
            try {
                T result;
                if (uriVariables != null) {
                    result = super.getForObject(fullUrl, responseType, uriVariables);
                } else {
                    switch (method) {
                        case GET:
                            result = super.getForObject(fullUrl, responseType);
                            break;
                        case POST:
                            result = super.postForObject(fullUrl, request, responseType);
                            break;
                        case PUT:
                            super.put(fullUrl, request);
                            result = null;
                            break;
                        case DELETE:
                            super.delete(fullUrl);
                            result = null;
                            break;
                        default:
                            throw new IllegalArgumentException("Unsupported HTTP method: " + method);
                    }
                }
                log.info("Request successful: {} {} via endpoint {}", method, fullUrl, endpointId);
                return result;
            } catch (Exception e) {
                attempts++;
                log.warn("Attempt {}/{} failed: {} {} via endpoint {}. Error: {}", attempts, maxAttempts, method, fullUrl, endpointId, e.getMessage());
                if (attempts >= maxAttempts) {
                    log.error("Max retry attempts reached for endpoint: {}", endpointId);
                    throw e;
                }
                long sleepTime = (long) (backoffMs * Math.pow(multiplier, attempts - 1));
                log.info("Backing off for {}ms before next attempt", sleepTime);
                backoff(backoffMs, multiplier, attempts);
            }
        }

        throw new RuntimeException("Max retry attempts reached for endpoint: " + endpointId);
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
