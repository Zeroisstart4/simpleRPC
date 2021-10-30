package com.zero.rpc.common.provider;

import lombok.*;

/**
 *@author Zhou
 *
 * 当需要使用版本号，标识具体实现类时，可以该Properties类构建，实现属性封装
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RpcServiceProperties {

    /**
     * 组 id
     */
    private String group;
    /**
     * 版本
     */
    private String version;
    /**
     * 服务名称
     */
    private String serviceName;

    public String toRpcServiceName(){
        return this.getServiceName() + this.getGroup() + this.getVersion();
    }


}
