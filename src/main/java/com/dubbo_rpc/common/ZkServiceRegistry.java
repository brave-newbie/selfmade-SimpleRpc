package com.dubbo_rpc.common;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.net.InetSocketAddress;

/**
 * 服务注册中心：负责将服务地址注册到 Zookeeper
 */
public class ZkServiceRegistry {

    // Curator 客户端，用于操作 ZK
    private CuratorFramework client;

    public ZkServiceRegistry(String zkAddress) {
        // 1. 创建重试策略：每 1000ms 重试一次，最多重试 3 次
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        // 2. 创建客户端并启动
        client = CuratorFrameworkFactory.builder()
                .connectString(zkAddress)
                .sessionTimeoutMs(40000)
                .retryPolicy(retryPolicy)
                .namespace("rpc") // 所有路径都会自动加上 /rpc 前缀
                .build();
        client.start();
    }

    /**
     * 注册服务
     * @param serviceName 服务名称 (例如：com.dubbo_rpc.publicinterface.HelloService)
     * @param serviceAddress 服务地址 (例如：127.0.0.1:7000)
     */
    public void register(String serviceName, String serviceAddress) {
        try {
            // 路径结构：/rpc/{serviceName}/{serviceAddress}
            // 例如：/rpc/com.dubbo_rpc.publicinterface.HelloService/127.0.0.1:7000
            String path = "/" + serviceName + "/" + serviceAddress;

            // 检查节点是否存在，不存在则创建
            if (client.checkExists().forPath(path) == null) {
                // 创建临时节点 (Ephemeral)：如果服务挂了，节点会自动删除
                client.create().creatingParentsIfNeeded()
                        .withMode(CreateMode.EPHEMERAL)
                        .forPath(path);
                System.out.println("Register service to ZK: " + path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
