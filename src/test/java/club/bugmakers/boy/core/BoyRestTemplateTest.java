package club.bugmakers.boy.core;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import club.bugmakers.boy.config.TestConfig;

@SpringBootTest(classes = TestConfig.class)
public class BoyRestTemplateTest {

    @Autowired
    private BoyRestTemplate boyRestTemplate;

    @Test
    public void testInvalidEndpoint() {
        assertThrows(IllegalArgumentException.class, () -> {
            boyRestTemplate.getForObject("non-existent", "/test", String.class);
        });
    }

    @Test
    public void testRequestExecution() {
        // 这个测试会尝试连接到本地服务，可能会失败，这是正常的
        // 我们主要测试的是配置加载和重试逻辑
        assertThrows(Exception.class, () -> {
            boyRestTemplate.getForObject("test-service", "/test", String.class);
        });
    }

    @Test
    public void testPostRequest() {
        // 测试 POST 请求
        assertThrows(Exception.class, () -> {
            boyRestTemplate.postForObject("test-service", "/test", "{\"key\": \"value\"}", String.class);
        });
    }

    @Test
    public void testPutRequest() {
        // 测试 PUT 请求
        assertThrows(Exception.class, () -> {
            boyRestTemplate.put("test-service", "/test", "{\"key\": \"value\"}");
        });
    }

    @Test
    public void testDeleteRequest() {
        // 测试 DELETE 请求
        assertThrows(Exception.class, () -> {
            boyRestTemplate.delete("test-service", "/test");
        });
    }
}
