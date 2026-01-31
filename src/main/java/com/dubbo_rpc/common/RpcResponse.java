package com.dubbo_rpc.common;

import java.io.Serializable;

/**
 * 2. 响应协议对象（回执单）
 * 服务端处理完后，不直接回传 String 结果，而是回传这个对象。
 * 包含了执行结果 result，或者执行过程中的异常 error。
 */
public class RpcResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    // 对应请求的ID
    private String requestId;
    // 正常调用的返回结果
    private Object result;
    // 调用过程中发生的异常（如果有）
    private Throwable error;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }
}
