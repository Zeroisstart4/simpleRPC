package com.zero.rpc.common.remoting.dto;

import lombok.*;

/**
 * @author Zhou
 *
 * RPC 消息类
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcMessage {

    /**
     * 消息类型
     */
    private byte messageType;
    /**
     * 序列化类型
     */
    private byte codec;
    /**
     * 压缩类型
     */
    private byte compress;
    /**
     * 请求 id
     */
    private int requestId;
    /**
     * 消息内容
     */
    private Object data;
}
