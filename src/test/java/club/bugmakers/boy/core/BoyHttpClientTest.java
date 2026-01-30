package club.bugmakers.boy.core;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

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
}
