package club.bugmakers.boy.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import club.bugmakers.boy.config.TestConfig;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestConfig.class)
@TestPropertySource(properties = {
        // 只设置少量必要的配置，模拟配置文件不存在的场景
        "boy.httpclient.endpoints[0].id=test-service",
        "boy.httpclient.endpoints[0].hosts[0]=http://localhost:8080"
})
public class BoyHttpClientPropertiesWithoutConfigTest {

    @Autowired
    private BoyHttpClientProperties properties;

    @Test
    public void testPropertiesLoadingWithoutYamlFile() {
        // 测试配置文件不存在时，通过 @TestPropertySource 加载配置
        assertNotNull(properties);
        assertNotNull(properties.getEndpoints());
        assertEquals(1, properties.getEndpoints().size());

        // 测试服务端点
        BoyHttpClientProperties.ServiceEndpoint testService = properties.getEndpoints().get(0);
        assertEquals("test-service", testService.getId());
        assertNotNull(testService.getHosts());
        assertEquals(1, testService.getHosts().size());
        assertTrue(testService.getHosts().contains("http://localhost:8080"));

        // 测试默认值
        assertNull(testService.getProxies());
        assertNull(testService.getRetryPolicy());
    }

    @Test
    public void testEmptyProperties() {
        // 测试配置为空时的情况
        assertNotNull(properties);
        // 即使没有配置，properties 对象也应该被正确创建
    }
}
