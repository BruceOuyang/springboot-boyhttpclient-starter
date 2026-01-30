package club.bugmakers.boy.core;

import org.junit.jupiter.api.Test;

import java.net.*;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BoyClientHttpRequestFactoryTest {

    @Test
    public void testCreateProxy() {
        BoyClientHttpRequestFactory factory = new BoyClientHttpRequestFactory();
        
        // 测试有效的代理字符串
        String proxyStr = "localhost:3128";
        Proxy proxy = createProxy(factory, proxyStr);
        assertNotNull(proxy);
        assertTrue(proxy.address() instanceof InetSocketAddress);
        InetSocketAddress address = (InetSocketAddress) proxy.address();
        assertTrue(address.getHostName().equals("localhost"));
        assertTrue(address.getPort() == 3128);
    }

    @Test
    public void testCreateProxyWithInvalidFormat() {
        BoyClientHttpRequestFactory factory = new BoyClientHttpRequestFactory();
        
        // 测试无效的代理字符串格式
        String invalidProxyStr = "localhost";
        try {
            createProxy(factory, invalidProxyStr);
            assertTrue(false, "Should throw IllegalArgumentException for invalid proxy format");
        } catch (IllegalArgumentException e) {
            // 预期的异常
            assertTrue(true);
        }
    }

    @Test
    public void testCreateProxyWithInvalidPort() {
        BoyClientHttpRequestFactory factory = new BoyClientHttpRequestFactory();
        
        // 测试无效的端口号
        String invalidProxyStr = "localhost:abc";
        try {
            createProxy(factory, invalidProxyStr);
            assertTrue(false, "Should throw IllegalArgumentException for invalid port");
        } catch (IllegalArgumentException e) {
            // 预期的异常
            assertTrue(true);
        }
    }

    @Test
    public void testSetProxies() {
        BoyClientHttpRequestFactory factory = new BoyClientHttpRequestFactory();
        List<String> proxies = Arrays.asList("localhost:3128", "localhost:3129");
        factory.setProxies(proxies);
        
        // 测试代理列表是否设置成功
        assertNotNull(factory.getProxies());
        assertTrue(factory.getProxies().size() == 2);
        assertTrue(factory.getProxies().contains("localhost:3128"));
        assertTrue(factory.getProxies().contains("localhost:3129"));
    }

    // 反射调用私有方法 createProxy
    private Proxy createProxy(BoyClientHttpRequestFactory factory, String proxyStr) {
        try {
            java.lang.reflect.Method method = BoyClientHttpRequestFactory.class.getDeclaredMethod("createProxy", String.class);
            method.setAccessible(true);
            return (Proxy) method.invoke(factory, proxyStr);
        } catch (java.lang.reflect.InvocationTargetException e) {
            // 直接抛出底层异常
            throw (RuntimeException) e.getTargetException();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
