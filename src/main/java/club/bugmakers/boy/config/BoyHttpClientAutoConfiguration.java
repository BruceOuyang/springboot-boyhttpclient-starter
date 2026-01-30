package club.bugmakers.boy.config;

import club.bugmakers.boy.core.BoyHttpClient;
import club.bugmakers.boy.core.BoyClientHttpRequestFactory;
import club.bugmakers.boy.core.BoyRestTemplate;
import club.bugmakers.boy.properties.BoyHttpClientProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Boy HttpClient 自动配置类
 * 用于自动装配 BoyHttpClient 和 BoyRestTemplate 实例
 */
@Configuration
@EnableConfigurationProperties(BoyHttpClientProperties.class)
public class BoyHttpClientAutoConfiguration {

    /**
     * 创建 BoyClientHttpRequestFactory 实例
     * @return BoyClientHttpRequestFactory 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public BoyClientHttpRequestFactory boyClientHttpRequestFactory() {
        return new BoyClientHttpRequestFactory();
    }

    /**
     * 创建 BoyHttpClient 实例
     * @param properties 配置属性
     * @return BoyHttpClient 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public BoyHttpClient boyHttpClient(BoyHttpClientProperties properties) {
        return new BoyHttpClient(properties);
    }

    /**
     * 创建 BoyRestTemplate 实例
     * @param properties 配置属性
     * @param requestFactory 请求工厂
     * @return BoyRestTemplate 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public BoyRestTemplate boyRestTemplate(BoyHttpClientProperties properties, BoyClientHttpRequestFactory requestFactory) {
        return new BoyRestTemplate(properties, requestFactory);
    }
}
