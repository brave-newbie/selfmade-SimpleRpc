package com.dubbo_rpc.spring;

import com.dubbo_rpc.annotation.RpcService;
import com.dubbo_rpc.netty.NettyServer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 服务端 Bean 后置处理器
 * 扫描带有 @RpcService 的 Bean，注册到 NettyServer
 */
public class RpcServerBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware, InitializingBean {

    private NettyServer nettyServer;
    private ApplicationContext applicationContext;

    public RpcServerBeanPostProcessor(NettyServer nettyServer) {
        this.nettyServer = nettyServer;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 检查 Bean 是否有 @RpcService 注解
        RpcService rpcService = bean.getClass().getAnnotation(RpcService.class);
        if (rpcService != null) {
            // 获取服务接口
            Class<?> interfaceClass = rpcService.interfaceClass();
            if (interfaceClass == void.class) {
                // 如果没有指定接口，默认使用实现的第一个接口
                interfaceClass = bean.getClass().getInterfaces()[0];
            }
            
            // 注册到 NettyServer
            nettyServer.register(bean);
        }
        return bean;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // Spring 容器启动完成后，启动 Netty 服务
        // 这里需要在一个新线程中启动，否则会阻塞 Spring 主线程
        new Thread(() -> {
            nettyServer.start();
        }).start();
    }
}
