package com.zero.rpc.common.remoting.constant;

/**
 * @author Zhou
 *
 * RPC 响应状态类型
 */
public enum RpcResponseCode {

    /**
     * 响应成功
     */
    SUCCESS(200, "OK"),
    /**
     * 响应失败
     */
    FAIL(500, "Failed")
    ;

    private final Integer code;

    private final String message;

    RpcResponseCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
