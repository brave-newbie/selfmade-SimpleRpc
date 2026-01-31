package com.dubbo_rpc.loadbalance;

import java.util.List;

/**
 * 轮询负载均衡策略
 */
public class RoundRobinLoadBalance implements LoadBalance {
    private int choose = -1;

    @Override
    public String balance(List<String> addressList) {
        choose++;
        choose = choose % addressList.size();
        System.out.println("负载均衡选择了" + choose + "服务器");
        return addressList.get(choose);
    }
}
