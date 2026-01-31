# 📚 RPC 项目面试题库 (Q&A)

## 📌 Netty 与网络通信

### Q1: 为什么选择 Netty 而不是 Java 原生 NIO？
*   **原生 NIO 的痛点**：API 复杂（Buffer 的指针操作容易出错）、虽然是非阻塞但编程模型繁琐、存在 Epoll 空轮询 Bug (CPU 100%)。
*   **Netty 的优势**：API 简单（Bootstrap 引导类）、功能强大（预置多种编解码器）、性能高（零拷贝、对象池）、社区活跃。

### Q2: 什么是 TCP 粘包/拆包？你的框架是怎么解决的？
*   **现象**：TCP 是流式协议，没有消息边界。发送方发了两个包 `ABC` 和 `DEF`，接收方可能收到 `ABCDEF` (粘包) 或者 `AB` 和 `CDEF` (拆包)。
*   **解决方案**：我使用了**自定义协议 + LengthFieldBasedFrameDecoder**。
    *   在协议头中定义了一个 `Data Length` (4字节) 字段。
    *   Netty 的 `LengthFieldBasedFrameDecoder` 会根据这个长度字段，自动把接收到的字节流拆分成一个个完整的消息帧，再交给后面的 Handler 处理。

### Q3: 你的自定义协议是怎么设计的？
*   我的协议由 **Header** 和 **Body** 组成，Header 总长 20 字节：
    *   **Magic Number (4B)**: `dubb`，用于快速校验非本协议请求。
    *   **Version (1B)**: 协议版本，便于后续升级。
    *   **Codec (1B)**: 序列化方式（1=Protostuff, 2=JSON）。
    *   **Type (1B)**: 消息类型（请求/响应/心跳）。
    *   **Status (1B)**: 响应状态。
    *   **Request ID (8B)**: 请求唯一标识，用于异步请求的响应匹配。
    *   **Body Length (4B)**: 消息体长度。

---

## 📌 序列化与 SPI

### Q4: 为什么要引入 SPI (Service Provider Interface)？
*   **解耦**：框架核心逻辑不应依赖具体的实现类（如 Protostuff）。
*   **扩展性**：遵循**开闭原则**。如果用户想用 Hessian 序列化，不需要修改框架源码，只需实现接口并在 `META-INF/services` 下配置即可。
*   **实现原理**：参考了 Dubbo 的设计，实现了一个 `ExtensionLoader`，利用 Java 反射机制动态加载配置文件中指定的实现类。

### Q5: 为什么默认使用 Protostuff 而不是 Java 原生序列化？
*   **Java 原生**：序列化后的码流太大（传输占带宽）、性能差（CPU 占用高）、无法跨语言。
*   **Protostuff**：基于 Google Protobuf，但不需要写 `.proto` 文件。生成的字节流非常小，序列化/反序列化速度是 Java 原生的 10 倍以上。

---

## 📌 分布式与服务治理

### Q6: Zookeeper 在这里起什么作用？
*   **注册中心**：服务端启动时，将自己的地址（IP:Port）写到 ZK 的临时节点下。
*   **服务发现**：客户端启动或调用时，从 ZK 拉取服务对应的所有地址列表。
*   **自动下线**：因为是**临时节点**，当服务端宕机（Session 断开），ZK 会自动删除该节点，客户端通过 Watcher 机制或轮询就能感知到服务下线，避免调用失败。

### Q7: 你的心跳机制是怎么实现的？
*   **背景**：长连接如果长时间没有数据传输，可能会被防火墙或中间设备断开，且两端不知情。
*   **实现**：利用 Netty 的 `IdleStateHandler`。
    *   **客户端**：30秒没有写数据，就发送一个 `PING` 消息。
    *   **服务端**：30秒没有读到数据，就断开连接（视为客户端已挂）。收到 `PING` 后回复 `PONG`。

### Q8: 客户端如何处理并发请求的响应匹配？
*   利用 **Request ID** 和 **CompletableFuture** (或 `wait/notify`)。
*   发送时：生成唯一 ID，将 `ID -> Future` 存入 Map。
*   接收时：解析响应中的 ID，从 Map 中取出对应的 Future，填入结果，唤醒等待的线程。

---

## 📌 Spring 集成

### Q9: 如何实现 @RpcService 和 @RpcReference 的注入？
*   **服务端**：实现了 `BeanPostProcessor`。在 Bean 初始化后，扫描类上是否有 `@RpcService` 注解，如果有，则将其注册到 Zookeeper 和本地缓存。
*   **客户端**：实现了 `BeanPostProcessor`。在 Bean 初始化前，扫描字段上是否有 `@RpcReference` 注解，如果有，则利用 JDK 动态代理生成一个代理对象，并反射注入给该字段。
