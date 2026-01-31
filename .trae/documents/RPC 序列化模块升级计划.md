# RPC 序列化模块升级计划

既然你选择了继续推进计划，我们现在的重点是 **方案 B：序列化优化**。

## 目标
替换掉 Netty 原生的 `ObjectEncoder` / `ObjectDecoder`（基于 Java 原生序列化），改用更高效、更通用的序列化方式（如 **JSON** 或 **Kryo**）。

## 核心步骤

### 1. 定义序列化接口
创建一个 `Serializer` 接口，定义两个核心方法：
*   `serialize`: 把对象变成字节数组 `byte[]`。
*   `deserialize`: 把字节数组 `byte[]` 变回对象。

### 2. 实现具体的序列化器
*   **JsonSerializer**: 使用 `FastJson` 或 `Gson` 实现（我们将使用 FastJson，因为它快且简单）。
*   *(可选) ObjectSerializer: 保留一个基于 Java 原生的实现作为兜底。*

### 3. 重写 Netty 的编解码器
*   **RpcEncoder**: 继承 `MessageToByteEncoder`。
    *   作用：将 `RpcRequest` / `RpcResponse` 对象 -> 序列化 -> 字节流。
*   **RpcDecoder**: 继承 `ByteToMessageDecoder`。
    *   作用：读取字节流 -> 反序列化 -> 还原成对象。

### 4. 替换旧组件
修改 `NettyServer` 和 `NettyClient` 的 pipeline 初始化代码：
*   **移除**: `ObjectEncoder`, `ObjectDecoder`
*   **加入**: 这里的 `RpcEncoder`, `RpcDecoder`

## 依赖管理
我们需要在 `pom.xml` 中引入 JSON 库。
```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>fastjson</artifactId>
    <version>1.2.83</version>
</dependency>
```

准备好了吗？我们将从引入依赖开始。
