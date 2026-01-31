# 🚀 Dubbo-RPC (Lightweight RPC Framework)

![License](https://img.shields.io/badge/license-Apache%202.0-blue)
![Java](https://img.shields.io/badge/Java-8%2B-orange)
![Netty](https://img.shields.io/badge/Netty-4.1-green)

一款基于 **Netty + Zookeeper + Spring** 实现的轻量级分布式 RPC 框架。
实现了服务注册发现、负载均衡、自定义通信协议、SPI 扩展、心跳检测等核心功能，旨在帮助开发者深入理解 RPC 底层原理。

---

## ✨ 核心特性

*   **高性能通信**：基于 **Netty** 实现 NIO 高性能网络传输，解决 TCP 粘包/拆包问题。
*   **自定义协议**：设计了包含魔数、版本号、消息类型、序列化方式等信息的**私有二进制协议**。
*   **服务治理**：
    *   **注册中心**：集成 **Zookeeper** (Curator) 实现服务的自动注册与发现。
    *   **负载均衡**：支持 **Random** (随机) 和 **RoundRobin** (轮询) 策略。
    *   **心跳检测**：基于 `IdleStateHandler` 实现双向心跳保活，自动断开失效连接。
*   **高扩展性**：
    *   **SPI 机制**：实现简易版 `ExtensionLoader`，支持动态加载插件（如切换序列化方式）。
    *   **序列化**：默认支持 **Protostuff** (高性能)，可扩展 **JSON** 等其他实现。
*   **易用性**：
    *   **Spring 集成**：提供 `@RpcService` 和 `@RpcReference` 注解，零侵入接入。
    *   **动态代理**：客户端无感调用远程服务。

---

## 🏗 架构设计

```text
+---------------------------------------------------------------+
|                       User Layer (Spring)                     |
|          @RpcService                    @RpcReference         |
+---------------------------------------------------------------+
|                       Proxy Layer                             |
|             Dynamic Proxy (JDK/CGLIB)                         |
+---------------------------------------------------------------+
|                       Registry Layer                          |
|             Zookeeper (Service Discovery/Registration)        |
+---------------------------------------------------------------+
|                       Protocol Layer                          |
|    Custom Protocol (Magic | Ver | Type | Status | ID | Len)   |
+---------------------------------------------------------------+
|                       Transport Layer                         |
|             Netty (NIO / Heartbeat / IdleState)               |
+---------------------------------------------------------------+
```

## 🛠 快速开始

### 1. 环境准备
*   JDK 8+
*   Maven 3+
*   Zookeeper 3.x

### 2. 定义服务接口
```java
public interface HelloService {
    String hello(String msg);
}
```

### 3. 服务端开发
```java
@RpcService(interfaceClass = HelloService.class)
public class HelloServiceImpl implements HelloService {
    @Override
    public String hello(String msg) {
        return "Hello, " + msg;
    }
}
```
配置并启动：
```java
@Configuration
@ComponentScan(basePackages = "com.dubbo_rpc.provider")
public class ServerConfig {
    @Bean
    public NettyServer nettyServer() {
        return new NettyServer("127.0.0.1", 7000);
    }
}
```

### 4. 客户端调用
```java
@Component
public class HelloController {
    @RpcReference
    private HelloService helloService;
    
    public void test() {
        String result = helloService.hello("RPC");
        System.out.println(result);
    }
}
```

---

## 🔌 SPI 扩展配置
在 `META-INF/services/` 下配置实现类，例如切换序列化方式：

文件：`com.dubbo_rpc.serializer.Serializer`
```properties
protostuff=com.dubbo_rpc.serializer.ProtostuffSerializer
json=com.dubbo_rpc.serializer.JsonSerializer
```

---

## 📝 联系作者
本项目为教学/实战项目，欢迎 Star 和 Fork！
