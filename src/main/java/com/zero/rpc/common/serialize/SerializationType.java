package com.zero.rpc.common.serialize;

import lombok.Getter;

/**
 * @author Zhou
 *
 * 序列化类型
 */
@Getter
public enum  SerializationType {

    /**
     * KYRO 类型，数值描述为 0x01
     */
    KYRO((byte) 0x01, "kyro"),

    /**
     * PROTOSTUFF 类型，数值描述为 0x02
     */
    PROTOSTUFF((byte) 0x02, "protostuff");


    private final byte code;

    private final String name;

    SerializationType(byte code, String name) {
        this.code = code;
        this.name = name;
    }
}
