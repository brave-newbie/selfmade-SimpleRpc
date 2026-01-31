# 🗺️ RPC 框架源码阅读指南

别被代码量吓到！RPC 框架的核心流程其实就三步：**启动服务** -> **注入代理** -> **远程调用**。
请按以下顺序阅读代码，你就能彻底搞懂它。

---

## 第一站：服务端启动 (Server Startup)
**目标**：理解服务是如何发布并注册到 Zookeeper 的。

1.  **入口**：[SpringServerBootStrap.java](src/main/java/com/dubbo_rpc/provider/SpringServerBootStrap.java)
    *   这是一个标准的 Spring 启动类，加载配置。
2.  **配置**：[ServerConfig.java](src/main/java/com/dubbo_rpc/config/ServerConfig.java)
    *   这里配置了 `NettyServer` 和核心处理器 `RpcServerBeanPostProcessor`。
3.  **核心魔法**：[RpcServerBeanPostProcessor.java](src/main/java/com/dubbo_rpc/spring/RpcServerBeanPostProcessor.java)
    *   **重点看**：`postProcessAfterInitialization` 方法。
    *   它扫描所有 Bean，如果发现 `@RpcService` 注解，就调用 `nettyServer.register()`。
4.  **注册逻辑**：[NettyServer.java](src/main/java/com/dubbo_rpc/netty/NettyServer.java)
    *   **重点看**：`register()` 方法。它做了两件事：
        1.  把服务实例存到本地 Map (`serviceMap`)，供后续反射调用。
        2.  调用 `ZkServiceRegistry` 把地址写到 Zookeeper。

---

## 第二站：客户端启动 (Client Startup)
**目标**：理解客户端是如何拿到一个“假的”接口实现类（代理对象）的。

1.  **入口**：[SpringClientBootStrap.java](src/main/java/com/dubbo_rpc/consumer/SpringClientBootStrap.java)
    *   Spring 容器启动，获取 `HelloController`。
2.  **业务代码**：[HelloController.java](src/main/java/com/dubbo_rpc/consumer/HelloController.java)
    *   注意这里的 `HelloService` 属性上加了 `@RpcReference`。
3.  **核心魔法**：[RpcClientBeanPostProcessor.java](src/main/java/com/dubbo_rpc/spring/RpcClientBeanPostProcessor.java)
    *   **重点看**：`postProcessBeforeInitialization` 方法。
    *   它利用反射遍历字段，发现 `@RpcReference` 就调用 `nettyClient.getBean()` 生成代理对象并注入。

---

## 第三站：远程调用 (Remote Invocation) —— 最精彩的部分！
**目标**：当你在客户端调用 `helloService.hello("RPC")` 时，底层发生了什么？

### 1. 客户端发起
*   **代理拦截**：[NettyClient.java](src/main/java/com/dubbo_rpc/netty/NettyClient.java)
    *   **重点看**：`invoke` 方法（JDK 动态代理）。
    *   它负责：
        1.  去 Zookeeper 发现服务地址 (`serviceDiscovery.discover`)。
        2.  负载均衡选一个地址 (`loadBalance.balance`)。
        3.  封装 `RpcRequest` 对象。
        4.  交给 `clientHandler` 发送。
*   **发送数据**：[NettyClientHandler.java](src/main/java/com/dubbo_rpc/netty/NettyClientHandler.java)
    *   **重点看**：`call()` 方法。
    *   它把 `RpcRequest` 包装成协议对象 `RpcMessage`，通过 Netty 发送出去，然后 `wait()` 阻塞等待结果。

### 2. 网络传输 (编码与解码)
*   **编码器**：[RpcMessageEncoder.java](src/main/java/com/dubbo_rpc/protocol/RpcMessageEncoder.java)
    *   把对象变成二进制流：魔数(4B) + 版本(1B) + ... + 长度 + 数据。
*   **解码器**：[RpcMessageDecoder.java](src/main/java/com/dubbo_rpc/protocol/RpcMessageDecoder.java)
    *   解决粘包拆包，把二进制流变回对象。

### 3. 服务端处理
*   **业务处理**：[NettyServerHandler.java](src/main/java/com/dubbo_rpc/netty/NettyServerHandler.java)
    *   **重点看**：`channelRead0` 方法。
    *   它拿到 `RpcRequest`，从 `serviceMap` 找到服务实例，**通过反射** (`method.invoke`) 执行真正的业务逻辑 `HelloServiceImpl.hello()`。
    *   拿到结果后，封装成 `RpcResponse` 发回给客户端。

### 4. 客户端接收
*   **唤醒线程**：[NettyClientHandler.java](src/main/java/com/dubbo_rpc/netty/NettyClientHandler.java)
    *   **重点看**：`channelRead0` 方法。
    *   收到响应，`notify()` 唤醒之前阻塞的线程，方法返回结果。

---

## 第四站：进阶阅读 (Advanced)
如果你看完了上面的流程，可以再看看这些“加分项”：

*   **SPI 机制**：[ExtensionLoader.java](src/main/java/com/dubbo_rpc/spi/ExtensionLoader.java)
    *   看它是如何加载 `META-INF/services` 下的配置文件的。
*   **序列化**：[ProtostuffSerializer.java](src/main/java/com/dubbo_rpc/serializer/ProtostuffSerializer.java)
    *   看看 Protostuff 是怎么用的。

---

祝你阅读愉快！如果有哪一步卡住了，随时问我。
