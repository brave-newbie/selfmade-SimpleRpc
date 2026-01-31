package com.dubbo_rpc.netty;

import com.dubbo_rpc.protocol.RpcMessageDecoder;
import com.dubbo_rpc.protocol.RpcMessageEncoder;
import com.dubbo_rpc.registry.ZkServiceRegistry;
import com.dubbo_rpc.serializer.Serializer;
import com.dubbo_rpc.spi.ExtensionLoader;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * 5. 服务端启动类
 * 负责启动 Netty 服务，并配置编解码器。
 */
public class NettyServer {

    private String host;
    private int port;
    private Map<String, Object> serviceMap = new HashMap<>();
    private ZkServiceRegistry registry;
    private Serializer serializer;

    public NettyServer(String host, int port) {
        this.host = host;
        this.port = port;
        this.registry = new ZkServiceRegistry("192.168.111.130:2181");
        // 使用 SPI 加载序列化器，这里可以改成从配置文件读取
        this.serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension("protostuff");
    }

    /**
     * 手动注册服务
     * @param service 服务实现类实例
     */
    public void register(Object service) {
        Class<?>[] interfaces = service.getClass().getInterfaces();
        if (interfaces.length == 0) {
            throw new IllegalArgumentException("Service must implement an interface");
        }
        // 默认使用第一个接口作为服务名
        String serviceName = interfaces[0].getName();
        serviceMap.put(serviceName, service);
        
        // 注册到 Zookeeper
        registry.register(serviceName, host + ":" + port);
        
        System.out.println("Register service: " + serviceName + " -> " + service.getClass().getName());
    }

    public void start() {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try{

            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            // 5.1 【编解码器】使用 Protostuff
                            // CommonDecoder 把收到的字节流转成 RpcRequest 对象
                            pipeline.addLast(new CommonDecoder(serializer, RpcRequest.class));
                            // CommonEncoder 把要发回去的 RpcResponse 对象转成字节流
                            pipeline.addLast(new CommonEncoder(serializer, RpcResponse.class));
                            // 将 serviceMap 传递给 Handler
                            pipeline.addLast(new NettyServerHandler(serviceMap));
                        }
                    });

            System.out.println("Netty服务端启动成功");
            ChannelFuture channelFuture = serverBootstrap.bind(host, port).sync();
            channelFuture.channel().closeFuture().sync();

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
