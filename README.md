# Spring Boot Boy HttpClient Starter

这是一个基于 Spring Boot + Java 8 开发的高可用 HttpClient Starter 组件，支持多目标服务端点配置、代理地址配置和重试策略。

## 功能特性

1. **多目标服务端点**：支持配置多个不同的目标服务端点，可同时调用多个不同的外部服务
2. **代理地址支持**：每个服务端点可配置多个代理地址，支持对端高可用
3. **可配置重试策略**：支持配置最大重试次数、退避时间和乘数
4. **最少三方依赖**：仅依赖 Spring Boot 核心库，无其他三方依赖
5. **Spring Boot Starter**：可作为独立的 Starter 组件引入项目
6. **默认配置文件**：默认读取 `boy-client.yml` 配置文件

## 安装

### 1. 构建项目

```bash
mvn clean install
```

### 2. 在目标项目中引入依赖

```xml
<dependency>
    <groupId>club.bugmakers.boy</groupId>
    <artifactId>springboot-boyhttpclient-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## 配置

### 1. 配置文件准备

1. 复制 `resource` 目录下的 `boy-httpclient-sample.yml` 文件为 `boy-client.yml`
2. 根据实际情况修改配置内容

### 2. 配置示例

```yaml
# Boy HttpClient 配置文件
boy:
  httpclient:
    endpoints:
      # 服务1配置
      - id: service1
        urls:
          - http://service1-host1:8080
          - http://service1-host2:8080
        # 代理配置（可选）
        proxies:
          - proxy1:3128
          - proxy2:3128
        # 重试策略配置
        retryPolicy:
          maxAttempts: 3      # 最大重试次数
          backoffMs: 100       # 基础退避时间（毫秒）
          multiplier: 1.5      # 退避乘数

      # 服务2配置
      - id: service2
        urls:
          - http://service2-host1:8080
          - http://service2-host2:8080
        # 重试策略配置
        retryPolicy:
          maxAttempts: 5      # 最大重试次数
          backoffMs: 200       # 基础退避时间（毫秒）
          multiplier: 2.0      # 退避乘数
```

## 使用示例

### 1. 使用 BoyHttpClient

适用于简单的 HTTP 请求场景。

```java
import club.bugmakers.boy.core.BoyHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class MyService {

    private final BoyHttpClient boyHttpClient;

    @Autowired
    public MyService(BoyHttpClient boyHttpClient) {
        this.boyHttpClient = boyHttpClient;
    }

    public String callService1() throws IOException {
        return boyHttpClient.get("service1", "/api/test");
    }

    public String callService2(String data) throws IOException {
        return boyHttpClient.post("service2", "/api/create", data);
    }
}
```

#### 支持的 HTTP 方法

- **GET**：`boyHttpClient.get(endpointId, path)`
- **POST**：`boyHttpClient.post(endpointId, path, body)`
- **PUT**：`boyHttpClient.put(endpointId, path, body)`
- **DELETE**：`boyHttpClient.delete(endpointId, path)`

### 2. 使用 BoyRestTemplate

基于 Spring RestTemplate，支持更丰富的 HTTP 客户端功能，适用于复杂的请求场景。

```java
import club.bugmakers.boy.core.BoyRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MyRestService {

    private final BoyRestTemplate boyRestTemplate;

    @Autowired
    public MyRestService(BoyRestTemplate boyRestTemplate) {
        this.boyRestTemplate = boyRestTemplate;
    }

    public User getUser(String userId) {
        return boyRestTemplate.getForObject("service1", "/api/users/{id}", User.class, userId);
    }

    public User createUser(User user) {
        return boyRestTemplate.postForObject("service1", "/api/users", user, User.class);
    }

    public void updateUser(String userId, User user) {
        boyRestTemplate.put("service1", "/api/users/{id}", user, userId);
    }

    public void deleteUser(String userId) {
        boyRestTemplate.delete("service1", "/api/users/{id}", userId);
    }
}

// User 类示例
class User {
    private String id;
    private String name;
    private String email;
    // getters and setters
}
```

#### 支持的 HTTP 方法

- **GET**：`boyRestTemplate.getForObject(endpointId, path, responseType)`
- **GET**：`boyRestTemplate.getForObject(endpointId, path, responseType, uriVariables)`
- **POST**：`boyRestTemplate.postForObject(endpointId, path, request, responseType)`
- **POST**：`boyRestTemplate.postForObject(endpointId, path, request, responseType, uriVariables)`
- **PUT**：`boyRestTemplate.put(endpointId, path, request)`
- **PUT**：`boyRestTemplate.put(endpointId, path, request, uriVariables)`
- **DELETE**：`boyRestTemplate.delete(endpointId, path)`
- **DELETE**：`boyRestTemplate.delete(endpointId, path, uriVariables)`

## 实现原理

### BoyHttpClient 实现原理

1. **随机负载均衡**：从配置的多个目标服务端点中随机选择一个进行请求
2. **随机代理选择**：从配置的多个代理地址中随机选择一个使用
3. **指数退避重试**：当请求失败时，使用指数退避策略进行重试
4. **基于 Java 原生 HttpURLConnection**：使用 Java 原生的 HttpURLConnection 实现 HTTP 请求

### BoyRestTemplate 实现原理

1. **基于 Spring RestTemplate**：继承自 Spring 的 RestTemplate，复用其丰富的 HTTP 客户端功能
2. **自定义 ClientHttpRequestFactory**：实现了 BoyClientHttpRequestFactory，支持多代理配置和随机选择
3. **集成高可用功能**：集成了与 BoyHttpClient 相同的高可用功能，包括多服务端点、多代理和重试策略
4. **统一配置**：与 BoyHttpClient 使用相同的配置系统，简化配置管理

### 共同特性

1. **Spring Boot 自动配置**：通过 `@EnableConfigurationProperties` 和 `spring.factories` 实现自动配置
2. **默认配置文件**：默认读取 `boy-client.yml` 配置文件，简化配置流程
3. **统一的配置结构**：两者使用相同的配置结构，包括服务端点、代理和重试策略配置

## 注意事项

1. **代理格式**：代理地址格式为 `host:port`，例如 `localhost:3128`
2. **URL 格式**：服务端点 URL 应包含协议和端口，例如 `http://localhost:8080`
3. **重试策略**：默认重试次数为 3 次，默认退避时间为 100ms，默认乘数为 1.5
4. **异常处理**：当所有重试失败后，会抛出原始的 IOException 异常
5. **配置文件**：确保在项目中创建 `boy-client.yml` 配置文件，或使用 `application.yml` 进行配置

## 版本要求与兼容性

### 核心版本要求

- **Java 版本**：Java 8+
- **Spring Boot 版本**：2.7.x+

### 兼容性影响

1. **Spring Boot 版本影响**：
   - **2.7.x**：完全兼容，为推荐版本
   - **3.0+**：可能存在兼容性问题，因为 Spring Boot 3.0 引入了大量变更，包括：
     - 移除了部分旧的 API
     - 对 Java 版本要求更高（至少 Java 17）
     - RestTemplate 虽然仍然可用，但已被标记为过时
   - **2.5.x - 2.6.x**：基本兼容，但可能需要调整部分配置

2. **Java 版本影响**：
   - **Java 8**：完全兼容，为推荐版本
   - **Java 11**：完全兼容
   - **Java 17+**：兼容，但需要配合 Spring Boot 3.0+ 使用

3. **依赖管理注意事项**：
   - 确保项目的 Spring Boot 版本与本组件的版本要求匹配
   - 当使用不同版本的 Spring Boot 时，可能需要通过 Maven 或 Gradle 解决依赖冲突
   - 在 Spring Boot 3.0+ 环境中，建议使用 WebClient 替代 RestTemplate

### 版本选择建议

- **稳定生产环境**：推荐使用 Java 8 + Spring Boot 2.7.x
- **新开发项目**：可考虑 Java 11 + Spring Boot 2.7.x
- **未来规划**：如需使用 Java 17+，建议等待本组件的 Spring Boot 3.0 兼容版本

### 降级处理

如果您的项目使用的是较低版本的 Spring Boot（如 2.3.x 或 2.4.x），可能需要：
1. 手动调整配置类中的某些 API 调用
2. 确保依赖的 Spring Boot 核心库版本一致
3. 可能需要修改 YamlPropertySourceFactory 的实现，以适应不同版本的 Spring Boot

## 许可证

MIT
