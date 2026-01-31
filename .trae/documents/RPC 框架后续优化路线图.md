# 负载均衡实现方案

你的理解有一半是对的，但在这个 RPC 场景下，情况略有不同。

## 1. 为什么不需要 Nginx？
Nginx 通常用于 **HTTP/Web 场景** 的负载均衡，它是一个**中心化**的代理（所有请求都先打给 Nginx，再由 Nginx 分发）。

而在 **RPC（特别是 Dubbo 这种架构）** 中，我们通常使用的是 **客户端负载均衡 (Client-side Load Balancing)**。
*   **注册中心 (Zookeeper)**：告诉客户端“现在有 3 个服务提供者，地址分别是 A, B, C”。
*   **客户端 (NettyClient)**：自己手里攥着这份名单 `[A, B, C]`。
*   **决策**：客户端在发起调用前，自己运行算法（比如随机、轮询），决定“这次我要调 B”。
*   **直连**：客户端直接连接 B 进行通信。

这样做的好处是**去中心化**，没有中间商赚差价，性能更高，且消除了单点故障（Nginx 挂了全完）。

## 2. 具体实现步骤

### Step 1: 模拟多实例环境
你说得对，现在只有一个 Server 实例，没法验证负载均衡。
*   我们会修改 `ServerBootStrap`，或者复制一份，启动两个 Server 实例。
*   Server 1: 监听 7000 端口。
*   Server 2: 监听 7001 端口。
*   它们都会把自己注册为 `HelloService`。
*   这样 Zookeeper 上 `HelloService` 下就会有两个节点。

### Step 2: 定义负载均衡策略
创建一个接口 `LoadBalancer`，并实现两种最简单的策略：
1.  **随机 (Random)**：
    ```java
    List<String> addresses = ...;
    return addresses.get(new Random().nextInt(addresses.size()));
    ```
2.  **轮询 (RoundRobin)**：
    ```java
    // 维护一个计数器 index
    return addresses.get(index++ % addresses.size());
    ```

### Step 3: 改造客户端
修改 `NettyClient` 的 `getBean` 逻辑：
*   **以前**：直接拿 ZK 返回的第一个地址。
*   **以后**：拿到 ZK 返回的**地址列表** -> 丢给 `LoadBalancer` -> 选出一个地址 -> 连接。

## 3. 验证
启动 Server 1 (7000) 和 Server 2 (7001)。
启动 Client，连续发起 10 次调用。
观察控制台，看请求是不是交替打到了 Server 1 和 Server 2 上。
