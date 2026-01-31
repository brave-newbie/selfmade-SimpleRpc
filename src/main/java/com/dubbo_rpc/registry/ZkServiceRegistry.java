package com.dubbo_rpc.registry;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

/**
 * 服务注册中心（服务端使用）
 * 负责将服务地址注册到 Zookeeper
 */
public class ZkServiceRegistry {
    private CuratorFramework client;

    public ZkServiceRegistry(String zkAddress) {
        // 重试策略：重试3次，每次间隔1秒
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        // 使用 Builder 构建客户端，设置连接超时和会话超时
        client = CuratorFrameworkFactory.builder()
                .connectString(zkAddress)
                .sessionTimeoutMs(40000)
                .connectionTimeoutMs(15000)
                .retryPolicy(retryPolicy)
                .build();
        client.start();
    }

    /**
     * 注册服务
     * @param serviceName 服务名称 (e.g., com.dubbo_rpc.publicinterface.HelloService)
     * @param serviceAddress 服务地址 (e.g., 127.0.0.1:7000)
     */
    public void register(String serviceName, String serviceAddress) {
        try {
            // 创建服务根节点（持久节点），例如：/rpc/com.dubbo_rpc.publicinterface.HelloService
            String servicePath = "/rpc/" + serviceName;
            if (client.checkExists().forPath(servicePath) == null) {
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(servicePath);
            }

            // 创建服务地址节点（临时节点），例如：/rpc/com.dubbo_rpc.publicinterface.HelloService/127.0.0.1:7000
            // 临时节点的好处是：如果服务端断开连接，这个节点会自动删除
            String addressPath = servicePath + "/" + serviceAddress;
            client.create().withMode(CreateMode.EPHEMERAL).forPath(addressPath);
            System.out.println("成功注册服务到 Zookeeper: " + serviceName + " -> " + addressPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
