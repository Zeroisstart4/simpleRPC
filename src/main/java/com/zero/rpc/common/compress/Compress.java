package com.zero.rpc.common.compress;

/**
 * @author Zhou
 *
 * 数据压缩注解
 */
public interface Compress {

    /**
     * 数据压缩
     * @param bytes     字节数组
     * @return
     */
    byte[] compress(byte[] bytes);

    /**
     * 数据解压
     * @param bytes     字节数组
     * @return
     */
    byte[] decompress(byte[] bytes);

}
