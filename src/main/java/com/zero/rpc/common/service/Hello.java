package com.zero.rpc.common.service;

import lombok.*;

import java.io.Serializable;
/**
 * @author Zhou
 *
 * 服务端响应类
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class Hello implements Serializable {

    /**
     * 响应信息
     */
    private String message;
    /**
     * 具体描述
     */
    private String description;
}
