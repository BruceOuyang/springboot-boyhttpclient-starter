package club.bugmakers.boy.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import club.bugmakers.boy.config.TestConfig;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestConfig.class)
public class BoyHttpClientPropertiesTest {

    @Autowired
    private BoyHttpClientProperties properties;

    @Test
    public void testPropertiesLoading() {
        // 测试配置文件加载成功
        assertNotNull(properties);
        assertNotNull(properties.getEndpoints());
        assertEquals(2, properties.getEndpoints().size());

        // 测试第一个服务端点
        BoyHttpClientProperties.ServiceEndpoint testService = properties.getEndpoints().get(0);
        assertEquals("test-service", testService.getId());
        assertNotNull(testService.getUrls());
        assertEquals(2, testService.getUrls().size());
        assertTrue(testService.getUrls().contains("http://localhost:8080"));
        assertTrue(testService.getUrls().contains("http://localhost:8081"));

        // 测试代理配置
        assertNotNull(testService.getProxies());
        assertEquals(2, testService.getProxies().size());
        assertTrue(testService.getProxies().contains("localhost:3128"));
        assertTrue(testService.getProxies().contains("localhost:3129"));

        // 测试重试策略
        assertNotNull(testService.getRetryPolicy());
        assertEquals(3, testService.getRetryPolicy().getMaxAttempts());
        assertEquals(100, testService.getRetryPolicy().getBackoffMs());
        assertEquals(1.5, testService.getRetryPolicy().getMultiplier());

        // 测试第二个服务端点
        BoyHttpClientProperties.ServiceEndpoint anotherService = properties.getEndpoints().get(1);
        assertEquals("another-service", anotherService.getId());
        assertNotNull(anotherService.getUrls());
        assertEquals(1, anotherService.getUrls().size());
        assertTrue(anotherService.getUrls().contains("http://localhost:8082"));

        // 测试第二个服务端点的重试策略
        assertNotNull(anotherService.getRetryPolicy());
        assertEquals(2, anotherService.getRetryPolicy().getMaxAttempts());
        assertEquals(50, anotherService.getRetryPolicy().getBackoffMs());
        assertEquals(1.0, anotherService.getRetryPolicy().getMultiplier());
    }

    @Test
    public void testFindEndpoint() {
        // 测试查找存在的服务端点
        BoyHttpClientProperties.ServiceEndpoint testService = findEndpoint("test-service");
        assertNotNull(testService);
        assertEquals("test-service", testService.getId());

        // 测试查找不存在的服务端点
        BoyHttpClientProperties.ServiceEndpoint nonExistent = findEndpoint("non-existent");
        assertNull(nonExistent);
    }

    // 辅助方法：查找服务端点
    private BoyHttpClientProperties.ServiceEndpoint findEndpoint(String endpointId) {
        if (properties.getEndpoints() == null) {
            return null;
        }
        return properties.getEndpoints().stream()
                .filter(endpoint -> endpointId.equals(endpoint.getId()))
                .findFirst()
                .orElse(null);
    }
}
