一. 项目概述
-------

该项目实现了一个简易的RPC框架，通过该框架可以实现和Dubbo类似的**远程服务调用**功能

项目地址：https://github.com/Zeroisstart4/simpleRPC

项目整体主要分为三大模块：**服务注册/服务发现模块，网络传输模块，Spring注解模块**

二. 服务注册/服务发现模块
--------------

本项目采用Zookeeper作为注册中心

该模块有三个主要的实现类：ServiceProviderImpl, ZkServiceRegistry, ZkServiceDiscovery

其中ZkServiceRegistry和ZkServiceDiscovery负责与Zookeeper直接交互（创建节点，获取节点内容等）

ServiceProviderImpl供外部调用，提供服务注册/服务发现功能

#### 1\. 服务注册

ServiceProviderImpl有一关键属性**serviceMap**，其实现是一ConcurrentHashMap。key为服务名，Object为服务的实现类。

```
private final Map<String, Object> serviceMap;

```

服务注册的整体过程分为两步：

1.  将服务地址注册到Zookeeper上
2.  将服务实现类添加到serviceMap中

第一步通过Curator客户端在Zookeeper上创建永久节点（**如/my-rpc/github.javaguide.HelloService/127.0.0.1:9999**）  
第二步将Service实现类put至serviceMap中即可

#### 2\. 服务发现

服务发现的整体过程分为两步：

1.  根据服务名到注册中心获取服务地址
2.  根据地址与服务提供方建立连接，并发送服务请求
3.  服务提供方根据请求找到服务实现类，进行服务调用

##### 1）获取服务地址

CuratorUtil中设置一Map**缓存服务地址列表**

```
private static final Map<String, List<String>> SERVICE_ADDRESS_MAP = new ConcurrentHashMap<>();

```

获取服务地址时**先去查缓存**，若缓存中不存在再去查询注册中心（获取地址节点的子节点）

查询完注册中心后**对地址节点设置监听**，如果地址节点发生变化说明有服务上下线，那么就在回调中**查到新的地址列表并更新缓存**

获得地址列表后通过**负载均衡策略**选择其中一个地址

##### 2）发送请求（后续通讯模块细说）

##### 3）获取服务实现类

根据请求中的请求服务名，**去serviceMap中查询得到服务实现类**，**利用反射执行服务方法**

#### 3\. 负载均衡

获得地址列表后需通过**负载均衡策略**选择其中一个地址

本项目实现了两种策略：随机算法，**一致性哈希算法**

一致性哈希算法的实现见：https://blog.csdn.net/wanger61/article/details/115726795?spm=1001.2014.3001.5501

三. 网络传输模块
---------

本项目采用Netty进行网络传输

网络模块传输模块主要分为以下几个部分：消息实体构建，解码/编码器，服务端，客户端

#### 1\. 消息实体构建

创建三个消息实体类：RpcRequest，RpcResponse，RpcMessage

**RpcRequest**对应服务调用请求，包含：请求ID+服务接口名+服务方法名+请求参数+请求参数类型+版本号+group（当接口有多个实现类时用于标识）

**RpcResponse**对应服务调用响应，包含：请求ID+响应码+响应信息+**服务调用结果数据**

**RpcMessage**用于封装消息，是网络中传输的实际类型，包含：消息类型（心跳ping，心跳pong，RpcRequest，RpcResponse）+序列化类型+压缩类型+具体数据（RpcRequest或RpcResponse）

#### 2\. 解码/编码器

解码/编码器需要负责将RpcMessage转换成字节进行网络传输，在接收时将字节重新构建回RpcMessage

另外，由于该框架采用TCP传输，还需要解决TCP的粘包半包问题

##### 1）自定义协议

为解决以上问题，需采用自定义协议，将消息分为消息头和消息体

消息头定义为：

```
 * 4B  magic code（魔数）   1B version（版本）   4B full length（消息长度）    1B messageType（消息类型）
 * 1B codec（序列化类型） 1B compress（压缩类型）  4B  requestId（请求的Id）
 * body（object类型数据）

```

编码时**按照协议规定顺序输出各个字节**，解码时**按照相同顺序读入各个字节**

##### 2）TCP粘包/半包问题的解决

自定义解码器继承自**LengthFieldBasedFrameDecoder**，该类可以**根据协议中的长度字段读取相应长度的字节**，即整个包。从而解决了TCP粘包/半包问题

```
public class RpcMessageDecoder extends LengthFieldBasedFrameDecoder {

    public RpcMessageDecoder() {
        this(RpcConstants.MAX_FRAME_LENGTH, 5, 4, -9, 0);
    }
    .......
}

```

##### 3）序列化/反序列化，压缩/解压缩

*   在编码时需要先将消息体进行序列化，然后压缩后传输
*   在解码时需先对字节进行解压缩，然后反序列化得到对象

序列化/反序列化采用Kryo框架，该框架使用较简单，但需注意**Kryo不是线程安全的**，应通过ThreadLocal获取

压缩/解压缩采用JDK的GZIPOutputStream和GZIPInputStream

#### 3\. 服务端

服务端通过ServerBootstap创建，并在Childpipeline中添加：IdleStateHandler心跳处理器，解码编码器，NettyRpcServerHandler业务处理器

##### NettyRpcServerHandler业务处理器

业务处理器处理三类事件：

1.  异常事件：直接关闭连接
2.  心跳事件：客户端会每隔5s发送一次心跳ping，如果服务端持续30s没有收到心跳消息，说明连接可能已失效，则关闭连接
3.  读事件：根据RpcMessage的属性确定接收到的消息类型：
    1.  若是心跳ping，则返回心跳pong
    2.  若是RpcRequest，则调用服务方法，返回调用结果

#### 4\. 客户端

客户端主要实现服务调用请求的发送和请求结果的接收

客户端通过Bootstrap创建，在pipeline中添加IdleStateHandler心跳处理器，编码解码器，NettyRpcClientHandler业务处理器

##### 1）发送服务调用请求

发送服务调用请求的步骤：

1.  根据服务名获取服务提供方的地址
2.  根据地址创建与服务端的连接（连接利用一个Map进行存储，如果已经创建过了则复用该连接）
3.  构建RpcMessage并通过该连接发送

##### 2）请求结果的接收

**难点：通过上述方法异步发送RpcRequest后，RpcResponse只能在NettyRpcClientHandler中通过read方法接收，那么该如何获取请求结果呢**

解决方案：通过CompletableFuture异步获取请求

步骤：

1.  为每个请求创建一个CompletableFuture<RpcResponse>
2.  用一个Map保存已发送且未收到回复的请求（key为requestId，value为该请求的CompletableFuture<RpcResponse>）
3.  通过上述发送请求时，在该Map中存入该请求的CompletableFuture<RpcResponse>，且方法返回该Future
4.  NettyRpcClientHandler收到服务器返回的调用结果后，从Map中移除该CompletableFuture<RpcResponse>，并为该Future设置好调用结果
5.  这样调用方就可以通过CompletableFuture.get()获取到调用结果了（如果未设置结果则一直阻塞）

四. Spring注解模块
-------------

通过自定义注解完成服务注册和服务调用

设计两个注解：@RpcService和@RpcReference

**@RpcService**注解用于服务注册，被标注的类会被自动注册，如：

```
@RpcService
public class HelloServiceImpl implements HelloService {
	......
}

```

**@RpcReference**注解用于服务调用，标注在属性上，调用该属性的方法时会以Rpc方式调用远程服务。如：

```
@Component
public class HelloController {
    @RpcReference
    private HelloService helloService;
}

```

#### 1\. 自定义后置处理器

通过实现自定义后置处理器RpcBeanPostProcessor完成以上注解功能

重写`Object postProcessBeforeInitialization(Object bean, String beanName)`方法：

*   如果发现该bean被@RpcService标注则调用ServiceProvider对该bean进行服务注册

重写`Object postProcessAfterInitialization(Object bean, String beanName)`方法：

*   如果发现该bean的某个Field属性被@RpcReference修饰，则获取一个**动态代理**对象，再**利用反射将代理对象赋值给该属性**
*   后续调用该属性时实际会去调用代理对象的方法

#### 2\. 动态代理

实现一个类获取动态代理对象，该类继承自InvocationHandler

对`invoke(Object proxy, Method method, Object[] args)`方法进行重写：

*   调用代理类的方法时，根据方法名，方法参数等构建出RpcRequest
*   调用客户端发送请求
*   返回请求结果

获取代理对象方法：

```
    public <T> T getProxy(Class<T> clazz){
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
    }

```

后续调用该属性的方法时实际会去调用上面的invoke方法，进行Rpc远程调用