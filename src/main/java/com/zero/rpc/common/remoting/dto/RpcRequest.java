package com.zero.rpc.common.remoting.dto;

import com.zero.rpc.common.provider.RpcServiceProperties;
import lombok.Builder;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author Zhou
 *
 * RPC 请求类
 */
@Builder
public class RpcRequest implements Serializable {

    /**
     * 请求 id
     */
    private String requestId;
    /**
     * 接口名称
     */
    private String interfaceName;
    /**
     * 方法名称
     */
    private String methodName;
    /**
     * 请求参数
     */
    private Object[] parameters;
    /**
     * 请求参数类型
     */
    private Class<?>[] paramTypes;
    /**
     * 服务版本
     */
    private String version;
    /**
     * 接口有多个实现类时的标识
     */
    private String group;

    public RpcRequest() {
    }

    public RpcRequest(String requestId, String interfaceName, String methodName, Object[] parameters, Class<?>[] paramTypes, String version, String group) {
        this.requestId = requestId;
        this.interfaceName = interfaceName;
        this.methodName = methodName;
        this.parameters = parameters;
        this.paramTypes = paramTypes;
        this.version = version;
        this.group = group;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    public Class<?>[] getParamTypes() {
        return paramTypes;
    }

    public void setParamTypes(Class<?>[] paramTypes) {
        this.paramTypes = paramTypes;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    @Override
    public String toString() {
        return "RpcRequest{" +
                "requestId='" + requestId + '\'' +
                ", interfaceName='" + interfaceName + '\'' +
                ", methodName='" + methodName + '\'' +
                ", parameters=" + Arrays.toString(parameters) +
                ", paramTypes=" + Arrays.toString(paramTypes) +
                ", version='" + version + '\'' +
                ", group='" + group + '\'' +
                '}';
    }

    public RpcServiceProperties toRpcProperties() {
        return RpcServiceProperties.builder().serviceName(this.getInterfaceName())
                .version(this.getVersion())
                .group(this.getGroup()).build();
    }

}
