package com.zero.rpc.common.remoting.constant;

import lombok.Getter;

/**
 * @author Zhou
 *
 * RPC 消息类型
 */
@Getter
public enum  RpcMessageType {

    /**
     * 请求类型
     */
    REQUEST_TYPE((byte) 1),
    /**
     * 响应类型
     */
    RESPONSE_TYPE((byte) 2),
    /**
     * 心跳机制 Ping 类型
     */
    HEARTBEAT_PING_TYPE((byte) 3),
    /**
     * 心跳机制 Pong 类型
     */
    HEARTBEAT_PONG_TYPE((byte) 4)
    ;

    private final byte code;

    RpcMessageType(byte code) {
        this.code = code;
    }
}
