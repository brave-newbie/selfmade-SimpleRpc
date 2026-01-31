# RPC 框架终极优化：引入 SPI 扩展机制

好的，既然要优化，我们就把它做到极致。现在的 RPC 框架已经有了自定义协议、心跳检测、Spring 集成等高级特性。

还有一个非常重要的设计模式，是所有顶级开源框架（Dubbo, Spring, Netty）的灵魂，那就是 **SPI (Service Provider Interface)**。

## 为什么要引入 SPI？
目前我们的代码中，序列化方式是写死的：
`private Serializer serializer = new ProtostuffSerializer();`

如果用户想用 JSON 序列化，或者 Hessian 序列化，就必须修改源码。这违反了**开闭原则**（对扩展开放，对修改关闭）。

## 优化目标
实现类似 Dubbo 的 SPI 机制，让用户可以通过配置文件动态切换实现类，而不需要改一行代码。

## 实施计划

1.  **定义 SPI 规范**：
    *   在 `Serializer` 接口上添加 `@SPI` 注解。
2.  **实现扩展加载器 (`ExtensionLoader`)**：
    *   这是一个核心工具类，负责读取 `META-INF/services/` 下的配置文件，并利用反射加载对应的实现类。
3.  **增加 JSON 序列化实现**：
    *   为了验证 SPI 的效果，我们新增一个 `JsonSerializer`。
4.  **改造核心代码**：
    *   修改 `NettyServer` 和 `NettyClient`，不再 `new` 具体的实现类，而是通过 `ExtensionLoader` 动态获取。

## 预期效果
以后想要切换序列化算法，只需要改一下配置或者传一个参数，框架就会自动加载对应的实现类。

准备好了吗？这将是你的 RPC 框架从“硬编码”走向“微内核架构”的关键一步！
