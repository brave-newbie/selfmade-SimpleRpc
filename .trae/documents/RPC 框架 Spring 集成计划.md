# Spring 集成计划

我们已经完成了 RPC 框架的核心功能（网络通信、序列化、注册中心、负载均衡）。最后一步是将其与 **Spring** 框架集成，使其在实际开发中易于使用。

## 目标

让用户像使用 Dubbo 或 Feign 一样使用我们的 RPC 框架：

* **服务端**：只需在实现类上加一个注解 `@RpcService`，服务就会自动注册到 Zookeeper。

* **客户端**：只需在字段上加一个注解 `@RpcReference`，框架就会自动注入代理对象。

## 实施步骤

1. **引入 Spring 依赖**：在 `pom.xml` 中添加 `spring-context`。
2. **定义注解**：

   * `@RpcService`：用于标记服务提供者。

   * `@RpcReference`：用于标记服务消费者引用。
3. **实现 BeanPostProcessor**：

   * **服务端处理器**：扫描带有 `@RpcService` 的 Bean，将其注册到 Zookeeper，并缓存到本地 `serviceMap`。

   * **客户端处理器**：扫描 Bean 中的字段，如果带有 `@RpcReference`，则生成动态代理并注入。
4. **改造 NettyServer**：

   * 使其能够接收 Spring 容器管理的服务实例。

   * 实现 `ApplicationListener` 或 `InitializingBean`，在 Spring 容器启动完成后自动启动 Netty 服务。
5. **验证**：

   * 创建 Spring 配置文件/配置类。

   * 编写测试用例，验证注解是否生效，RPC 调用是否成功。

这个计划将彻底改变我们框架的使用体验，从“手写 main 方法”进化到“注解驱动开发”。准备好了吗？我们开始吧！
