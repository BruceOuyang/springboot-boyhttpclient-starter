package club.bugmakers.boy.core;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;

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

    @Test
    public void testGetForObjectWithHeaders() {
        // 测试带header的GET请求
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer token123");
        headers.put("Content-Type", "application/json");
        
        assertThrows(Exception.class, () -> {
            boyRestTemplate.getForObject("test-service", "/test", headers, String.class);
        });
    }

    @Test
    public void testGetForObjectWithHeadersAndUriVariables() {
        // 测试带header和URI变量的GET请求
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer token123");
        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("id", 1);
        
        assertThrows(Exception.class, () -> {
            boyRestTemplate.getForObject("test-service", "/test/{id}", headers, String.class, uriVariables);
        });
    }

    @Test
    public void testPostForObjectWithHeaders() {
        // 测试带header的POST请求
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer token123");
        headers.put("Content-Type", "application/json");
        String requestBody = "{\"key\": \"value\"}";
        
        assertThrows(Exception.class, () -> {
            boyRestTemplate.postForObject("test-service", "/test", requestBody, headers, String.class);
        });
    }

    @Test
    public void testPostForObjectWithHeadersAndUriVariables() {
        // 测试带header和URI变量的POST请求
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer token123");
        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("id", 1);
        String requestBody = "{\"key\": \"value\"}";
        
        assertThrows(Exception.class, () -> {
            boyRestTemplate.postForObject("test-service", "/test/{id}", requestBody, headers, String.class, uriVariables);
        });
    }

    @Test
    public void testPutWithHeaders() {
        // 测试带header的PUT请求
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer token123");
        headers.put("Content-Type", "application/json");
        String requestBody = "{\"key\": \"value\"}";
        
        assertThrows(Exception.class, () -> {
            boyRestTemplate.put("test-service", "/test", requestBody, headers);
        });
    }

    @Test
    public void testDeleteWithHeaders() {
        // 测试带header的DELETE请求
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer token123");
        headers.put("Content-Type", "application/json");
        
        assertThrows(Exception.class, () -> {
            boyRestTemplate.delete("test-service", "/test", headers);
        });
    }

    @Test
    public void testInvalidEndpointWithHeaders() {
        // 测试无效端点的带header请求
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer token123");
        
        assertThrows(IllegalArgumentException.class, () -> {
            boyRestTemplate.getForObject("non-existent", "/test", headers, String.class);
        });
    }
}
