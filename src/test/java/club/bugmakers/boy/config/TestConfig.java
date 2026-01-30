package club.bugmakers.boy.config;

import club.bugmakers.boy.core.BoyHttpClient;
import club.bugmakers.boy.core.BoyClientHttpRequestFactory;
import club.bugmakers.boy.core.BoyRestTemplate;
import club.bugmakers.boy.properties.BoyHttpClientProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(BoyHttpClientProperties.class)
public class TestConfig {

    @Bean
    public BoyHttpClient boyHttpClient(BoyHttpClientProperties properties) {
        return new BoyHttpClient(properties);
    }

    @Bean
    public BoyClientHttpRequestFactory boyClientHttpRequestFactory() {
        return new BoyClientHttpRequestFactory();
    }

    @Bean
    public BoyRestTemplate boyRestTemplate(BoyHttpClientProperties properties, BoyClientHttpRequestFactory requestFactory) {
        return new BoyRestTemplate(properties, requestFactory);
    }
}
