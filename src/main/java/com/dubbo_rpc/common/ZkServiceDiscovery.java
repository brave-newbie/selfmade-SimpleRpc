package com.dubbo_rpc.common;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.List;
import java.util.Random;

/**
 * 服务发现：负责从 Zookeeper 获取服务地址
 */
public class ZkServiceDiscovery {

    private CuratorFramework client;

    public ZkServiceDiscovery(String zkAddress) {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.builder()
                .connectString(zkAddress)
                .sessionTimeoutMs(40000)
                .retryPolicy(retryPolicy)
                .namespace("rpc")
                .build();
        client.start();
    }

    /**
     * 发现服务地址（负载均衡：随机）
     * @param serviceName 服务名称
     * @return 服务地址 (IP:Port)
     */
    public String discover(String serviceName) {
        try {
            String path = "/" + serviceName;
            // 1. 获取该服务下的所有子节点（即所有可用的服务地址）
            List<String> serviceAddresses = client.getChildren().forPath(path);
            
            if (serviceAddresses == null || serviceAddresses.isEmpty()) {
                System.out.println("No service found for: " + serviceName);
                return null;
            }

            // 2. 简单的负载均衡：随机选择一个地址
            String address = serviceAddresses.get(new Random().nextInt(serviceAddresses.size()));
            System.out.println("Discover service: " + serviceName + " -> " + address);
            return address;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
