package com.zero.rpc.common.remoting.constant;

/**
 * @author Zhou
 *
 * RPC 常量类
 */
public class RpcConstants {

    /**
     * 魔数
     */
    public static final byte[] MAGIC_NUMBER = {(byte) 'g', (byte) 'r', (byte) 'p', (byte) 'c'};
    /**
     * 版本号
     */
    public static final byte VERSION = 1;
    /**
     * 头部长度
     */
    public static final int HEAD_LENGTH = 16;
    /**
     * 最大帧长
     */
    public static final int MAX_FRAME_LENGTH = 8 * 1024 * 1024 ;
}
