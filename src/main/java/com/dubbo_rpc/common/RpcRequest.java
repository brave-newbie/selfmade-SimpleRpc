package com.dubbo_rpc.common;

import java.io.Serializable;

/**
 * 1. 传输协议对象（工单模板）
 * 以前是拼字符串 "HelloService#hello#参数"，现在改成了标准对象。
 * 所有的请求信息都封装在这里，包括要调用的类名、方法名、参数类型、参数值。
 */
public class RpcRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    // 请求ID，用于标识唯一的请求，虽然现在是同步调用，但为了以后异步调用做准备
    private String requestId;
    // 接口名称，例如：com.dubbo_rpc.publicinterface.HelloService
    private String className;
    // 方法名称，例如：hello
    private String methodName;
    // 参数类型列表，用于反射找到重载方法，例如：new Class[]{String.class}
    private Class<?>[] parameterTypes;
    // 参数值列表，例如：new Object[]{"你好 dubbo"}
    private Object[] parameters;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }
}
