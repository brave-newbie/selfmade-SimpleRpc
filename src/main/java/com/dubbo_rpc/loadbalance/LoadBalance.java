package com.dubbo_rpc.loadbalance;

import java.util.List;

/**
 * 负载均衡接口
 */
public interface LoadBalance {
    /**
     * 从服务列表中选择一个地址
     * @param addressList 服务地址列表
     * @return 选中的地址
     */
    String balance(List<String> addressList);
}
