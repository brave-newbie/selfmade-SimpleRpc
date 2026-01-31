package com.dubbo_rpc.registry;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.List;
import java.util.Random;

/**
 * 服务发现中心（客户端使用）
 * 负责从 Zookeeper 拉取服务地址
 */
public class ZkServiceDiscovery {
    private CuratorFramework client;

    public ZkServiceDiscovery(String zkAddress) {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.builder()
                .connectString(zkAddress)
                .sessionTimeoutMs(40000)
                .connectionTimeoutMs(15000)
                .retryPolicy(retryPolicy)
                .build();
        client.start();
    }

    /**
     * 发现服务地址列表
     * @param serviceName 服务名称
     * @return 服务地址列表
     */
    public List<String> discover(String serviceName) {
        try {
            String servicePath = "/rpc/" + serviceName;
            // 获取该服务下的所有子节点（即所有可用的服务地址）
            List<String> serviceAddresses = client.getChildren().forPath(servicePath);
            if (serviceAddresses == null || serviceAddresses.isEmpty()) {
                System.out.println("Zookeeper 中未找到服务: " + serviceName);
                return null;
            }
            return serviceAddresses;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
