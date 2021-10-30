package com.zero.rpc.common.exception;

/**
 * @author Zhou
 *
 * 自定义 RPC 异常
 */
public class RpcException extends RuntimeException {

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(RpcErrorMessage rpcErrorMessageEnum, String detail) {
        super(rpcErrorMessageEnum.getMessage() + ":" + detail);
    }

    public RpcException(RpcErrorMessage rpcErrorMessageEnum) {
        super(rpcErrorMessageEnum.getMessage());
    }
}
