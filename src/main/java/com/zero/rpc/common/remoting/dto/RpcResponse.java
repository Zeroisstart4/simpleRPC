package com.zero.rpc.common.remoting.dto;

import com.zero.rpc.common.remoting.constant.RpcResponseCode;
import lombok.Builder;

import java.io.Serializable;

/**
 * @author Zhou
 *
 * RPC 响应类
 */
@Builder
public class RpcResponse<T> implements Serializable {

    /**
     * 请求 id
     */
    private String requestId;
    /**
     * 响应状态码
     */
    private Integer code;
    /**
     * 响应信息
     */
    private String message;
    /**
     * 返回数据
     */
    private T data;


    /**
     * 返回成功响应
     * @param data          返回数据
     * @param requestId     请求 id
     * @param <T>
     * @return
     */
    public static <T> RpcResponse<T> success(T data, String requestId){
        RpcResponse<T> response = new RpcResponse<>();
        response.setCode(RpcResponseCode.SUCCESS.getCode());
        response.setMessage(RpcResponseCode.SUCCESS.getMessage());
        response.setRequestId(requestId);
        if (null != data){
            response.setData(data);
        }
        return response;
    }

    /**
     * 返回失败响应
     * @param rpcResponseCode   RPC 响应码
     * @param <T>
     * @return
     */
    public static <T> RpcResponse<T> fail(RpcResponseCode rpcResponseCode){
        RpcResponse<T> response = new RpcResponse<>();
        response.setCode(rpcResponseCode.getCode());
        response.setMessage(rpcResponseCode.getMessage());
        return response;
    }

    public RpcResponse() {
    }

    public RpcResponse(String requestId, Integer code, String message, T data) {
        this.requestId = requestId;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "RpcResponse{" +
                "requestId='" + requestId + '\'' +
                ", code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
