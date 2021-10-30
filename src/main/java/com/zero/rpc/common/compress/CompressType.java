package com.zero.rpc.common.compress;

import lombok.Getter;

/**
 * @author Zhou
 *
 * 数据压缩类型枚举类
 */
@Getter
public enum CompressType {

    /**
     * gzip 压缩
      */
    GZIP((byte) 0x01, "gzip");

    private final byte code;
    private final String name;

    CompressType(byte code, String name) {
        this.code = code;
        this.name = name;
    }
}
