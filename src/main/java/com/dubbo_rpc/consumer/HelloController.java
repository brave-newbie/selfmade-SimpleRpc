package com.dubbo_rpc.consumer;

import com.dubbo_rpc.annotation.RpcReference;
import com.dubbo_rpc.publicinterface.HelloService;
import org.springframework.stereotype.Component;

@Component
public class HelloController {
    
    @RpcReference
    private HelloService helloService;
    
    public void test() {
        String result = helloService.hello("Spring 集成测试");
        System.out.println("调用结果: " + result);
    }
}
