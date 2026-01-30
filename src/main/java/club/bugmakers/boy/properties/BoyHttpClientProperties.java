package club.bugmakers.boy.properties;

import club.bugmakers.boy.config.YamlPropertySourceFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 高可用 HttpClient 配置属性类
 * 用于读取和管理 Boy HttpClient 的配置信息
 */
@Component
@PropertySource(value = "classpath:boy-client.yml", factory = YamlPropertySourceFactory.class)
@ConfigurationProperties(prefix = "boy.httpclient")
public class BoyHttpClientProperties {

    /**
     * 服务端点列表
     */
    private List<ServiceEndpoint> endpoints;

    public List<ServiceEndpoint> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(List<ServiceEndpoint> endpoints) {
        this.endpoints = endpoints;
    }

    /**
     * 服务端点配置类
     */
    public static class ServiceEndpoint {
        /**
         * 服务端点ID
         */
        private String id;
        /**
         * 服务URL列表
         */
        private List<String> urls;
        /**
         * 代理地址列表
         */
        private List<String> proxies;
        /**
         * 重试策略配置
         */
        private RetryPolicy retryPolicy;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public List<String> getUrls() {
            return urls;
        }

        public void setUrls(List<String> urls) {
            this.urls = urls;
        }

        public List<String> getProxies() {
            return proxies;
        }

        public void setProxies(List<String> proxies) {
            this.proxies = proxies;
        }

        public RetryPolicy getRetryPolicy() {
            return retryPolicy;
        }

        public void setRetryPolicy(RetryPolicy retryPolicy) {
            this.retryPolicy = retryPolicy;
        }
    }

    /**
     * 重试策略配置类
     */
    public static class RetryPolicy {
        /**
         * 最大重试次数，默认3次
         */
        private int maxAttempts = 3;
        /**
         * 退避时间，默认100ms
         */
        private long backoffMs = 100;
        /**
         * 退避乘数，默认1.5
         */
        private double multiplier = 1.5;
        /**
         * 可重试的状态码列表
         */
        private List<Integer> retryableStatusCodes;

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public long getBackoffMs() {
            return backoffMs;
        }

        public void setBackoffMs(long backoffMs) {
            this.backoffMs = backoffMs;
        }

        public double getMultiplier() {
            return multiplier;
        }

        public void setMultiplier(double multiplier) {
            this.multiplier = multiplier;
        }

        public List<Integer> getRetryableStatusCodes() {
            return retryableStatusCodes;
        }

        public void setRetryableStatusCodes(List<Integer> retryableStatusCodes) {
            this.retryableStatusCodes = retryableStatusCodes;
        }
    }
}
