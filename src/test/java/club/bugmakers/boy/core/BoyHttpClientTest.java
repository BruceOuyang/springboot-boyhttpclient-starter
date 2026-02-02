package club.bugmakers.boy.core;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import club.bugmakers.boy.config.TestConfig;

@SpringBootTest(classes = TestConfig.class)
public class BoyHttpClientTest {

    @Autowired
    private BoyHttpClient boyHttpClient;

    @Test
    public void testInvalidEndpoint() {
        assertThrows(IllegalArgumentException.class, () -> {
            boyHttpClient.get("non-existent", "/test");
        });
    }

    @Test
    public void testRequestExecution() {
        // 这个测试会尝试连接到本地服务，可能会失败，这是正常的
        // 我们主要测试的是配置加载和重试逻辑
        assertThrows(IOException.class, () -> {
            boyHttpClient.get("test-service", "/test");
        });
    }

    @Test
    public void testGetWithHeaders() {
        // 测试带header的GET请求
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer token123");
        headers.put("Content-Type", "application/json");
        
        assertThrows(IOException.class, () -> {
            boyHttpClient.get("test-service", "/test", headers);
        });
    }

    @Test
    public void testPostWithHeaders() {
        // 测试带header的POST请求
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer token123");
        headers.put("Content-Type", "application/json");
        String requestBody = "{\"key\": \"value\"}";
        
        assertThrows(IOException.class, () -> {
            boyHttpClient.post("test-service", "/test", requestBody, headers);
        });
    }

    @Test
    public void testPutWithHeaders() {
        // 测试带header的PUT请求
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer token123");
        headers.put("Content-Type", "application/json");
        String requestBody = "{\"key\": \"value\"}";
        
        assertThrows(IOException.class, () -> {
            boyHttpClient.put("test-service", "/test", requestBody, headers);
        });
    }

    @Test
    public void testDeleteWithHeaders() {
        // 测试带header的DELETE请求
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer token123");
        headers.put("Content-Type", "application/json");
        
        assertThrows(IOException.class, () -> {
            boyHttpClient.delete("test-service", "/test", headers);
        });
    }

    @Test
    public void testInvalidEndpointWithHeaders() {
        // 测试无效端点的带header请求
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer token123");
        
        assertThrows(IllegalArgumentException.class, () -> {
            boyHttpClient.get("non-existent", "/test", headers);
        });
    }
}
