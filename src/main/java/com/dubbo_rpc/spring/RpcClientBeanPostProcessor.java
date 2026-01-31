package com.dubbo_rpc.spring;

import com.dubbo_rpc.annotation.RpcReference;
import com.dubbo_rpc.netty.NettyClient;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Field;

/**
 * 客户端 Bean 后置处理器
 * 扫描带有 @RpcReference 的字段，注入代理对象
 */
public class RpcClientBeanPostProcessor implements BeanPostProcessor {

    private NettyClient nettyClient;

    public RpcClientBeanPostProcessor() {
        this.nettyClient = new NettyClient();
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            RpcReference rpcReference = field.getAnnotation(RpcReference.class);
            if (rpcReference != null) {
                // 生成代理对象
                Class<?> interfaceClass = field.getType();
                // 暂时不处理 version
                String providerName = interfaceClass.getName() + "#" + "hello" + "#"; 
                // 注意：这里我们的 getBean 方法需要 providerName，但实际上我们在动态代理里才去发现服务
                // 之前的 getBean 设计稍微有点耦合，这里我们可以简化一下 getBean 的参数
                // 暂时为了兼容之前的代码，我们还是传入一个构造好的 providerName，或者修改 NettyClient 的 getBean
                
                Object proxy = nettyClient.getBean(interfaceClass, "");
                
                // 注入代理对象
                field.setAccessible(true);
                try {
                    field.set(bean, proxy);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
