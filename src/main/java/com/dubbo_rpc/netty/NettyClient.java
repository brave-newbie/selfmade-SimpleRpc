package com.dubbo_rpc.netty;

import com.dubbo_rpc.common.RpcRequest;
import com.dubbo_rpc.common.RpcResponse;
import com.dubbo_rpc.loadbalance.LoadBalance;
import com.dubbo_rpc.loadbalance.RandomLoadBalance;
import com.dubbo_rpc.protocol.RpcMessageDecoder;
import com.dubbo_rpc.protocol.RpcMessageEncoder;
import com.dubbo_rpc.registry.ZkServiceDiscovery;
import com.dubbo_rpc.serializer.Serializer;
import com.dubbo_rpc.spi.ExtensionLoader;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.lang.reflect.Proxy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 3. 客户端发起调用（填工单）
 * 这里是客户端的核心，利用动态代理拦截方法调用，封装成 RpcRequest 发送给服务端。
 */
public class NettyClient {

    private static ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private static NettyClientHandler clientHandler;
    private static ZkServiceDiscovery serviceDiscovery = new ZkServiceDiscovery("192.168.111.130:2181");
    private static LoadBalance loadBalance = new RandomLoadBalance();
    private static Serializer serializer;
    
    static {
        // 使用 SPI 加载序列化器
        serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension("protostuff");
    }

    // 获取一个代理对象
    // 只要你调用这个代理对象的方法，就会走到 invoke 里的逻辑
    public Object getBean(final Class<?> serviceClass, final String providerName) {
        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class<?>[]{serviceClass},
                (proxy, method, args) -> {
                    // 3.1 懒加载初始化客户端连接
                    // 动态发现服务地址
                    if (clientHandler == null) {
                        String serviceName = serviceClass.getName();
                        java.util.List<String> serviceAddresses = serviceDiscovery.discover(serviceName);
                        if (serviceAddresses == null || serviceAddresses.isEmpty()) {
                            throw new RuntimeException("No available service provider for: " + serviceName);
                        }
                        
                        // 使用负载均衡选择一个地址
                        String serviceAddress = loadBalance.balance(serviceAddresses);
                        
                        String[] array = serviceAddress.split(":");
                        String host = array[0];
                        int port = Integer.parseInt(array[1]);
                        initClient(host, port);
                    }
                    
                    // 3.2 【封装工单】把调用的类名、方法名、参数都填到 RpcRequest 对象里
                    RpcRequest request = new RpcRequest();
                    request.setClassName(serviceClass.getName()); // 比如 "com.dubbo_rpc.publicinterface.HelloService"
                    request.setMethodName(method.getName());      // 比如 "hello"
                    request.setParameterTypes(method.getParameterTypes()); // 参数类型
                    request.setParameters(args);                  // 参数值 "你好 dubbo"
                    request.setRequestId(java.util.UUID.randomUUID().toString()); // 生成一个流水号

                    // 3.3 把填好的工单交给 Handler 发送
                    clientHandler.setPara(request);
                    
                    // 3.4 提交任务并阻塞等待结果返回
                    return executorService.submit(clientHandler).get();
                });
    }

    // 初始化客户端
    public void initClient(String host, int port) throws InterruptedException {

        clientHandler = new NettyClientHandler();
        EventLoopGroup group = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY,true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        // 3.5 【编解码器】使用 Protostuff
                        // CommonDecoder 用于把服务端发回来的字节流变回 RpcResponse 对象
                        pipeline.addLast(new CommonDecoder(serializer, RpcResponse.class));
                        // CommonEncoder 用于把我们发的 RpcRequest 对象变成字节流
                        pipeline.addLast(new CommonEncoder(serializer, RpcRequest.class));
                        pipeline.addLast(clientHandler);
                    }
                });
        bootstrap.connect(host, port).sync();
    }
}
