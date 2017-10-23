title:ActiveMQ简介

date：2017年10月19日18:51:18

---



ActiveMQ是最大的开源组织Apache推出的开源的，完全支持JMS1.1和J2EE1.4规范的JMS Provider实现的消息中间件（MOM,message oriented middleware）。

ActiveMQ能做什么：实现JMS Provider,用来帮助实现高可用，高性能，可伸缩，易用和安全的企业级面向消息服务的系统。除了可以实现系统直之间的解耦之外，更重要的是能够实现异步发送和接收。

ActiveMQ特点：

​	

- 完成支持JMS1.1和J2EE1.4规范（持久化，XA消息，事务)
- 支持多种传送协议：TCP ,SSL,NIO,UDP等。
- 可插拔的的体系结构，可以灵活定制，如：消息存储方式，安全管理。
- 支持多语言，当然最好的还是Java，因为毕竟ActiveMQ使用Java编写的。





#### 消息中间件

基本功能：将信息已消息的形式，从一个应用程序传送到另一个或多个应用程序。

主要特点：



- 消息异步接收
- 消息可靠接收



#### JMS基本概念

JMS（JAVA Message Service,java消息服务）API是一个消息服务的标准或者说是规范，允许应用程序组件基于JavaEE平台创建、发送、接收和读取消息。它使分布式通信耦合度更低，消息服务更加可靠以及异步性。

- JMS是java的消息服务，JMS的客户端之间可以通过JMS服务进行异步的消息传输。

- JMS  provider：实现了JMS接口和规范的消息中间件

- JMS message：JMS的消息，JMS消息由一下三个部分组成：

  ​	

  - 消息头：每个消息头字段都有相应的gettter和setter方法
  - 消息属性：如果需要除消息头字段意外的值，那么可以使用消息属性
  - 消息体：封装具体的消息数据

- JMS producer:消息生产者，创建和发送JMS消息的客户端应用

- jMS consumer:消息消费者，接收和处理JMS消息的客户端应用

  消息的消费可以采用两种方式：

  ​	同步消费：通过调用消费者的receive方法从目的地中显式的提取信息，receive方法可以一直阻塞到消息到达

  ​	异步消费：客户可以为消费者注册一个消息监听器，以定义在消息到达时所采取的动作。（实际上还是在消息服务器上轮询，但是是通过注册回调方法的方式实现的）

- JMS domains:消息传递域,JMS规范中规定了两种消息传递域：点对点（point-to-point,ptp）消息传递域和发布/订阅传递域（publish/subscribe,pub/sub）

   -  点对点的特点

      ​	1.每个消息只能有一个消费者（不是说一个队列只能对应一个消费者，只是这条消息如果别某一个消费者消费了，其他的消费者就不能消费了）

      ​	2.消息的生产者和消费者之间没有时间的相关性。无论消费者在生产者发送消息的时候是否处于运行状态，它都可以提取消息。

  - 发布订阅消息传递域的特点：

    - 每个消息都可以有多个消费者。
    - 生产者和消费者之间有时间上的相关性，订阅一个主题的消费者只能消费自它订阅后的消息。JMS规定允许客户创建持久订阅，这在一定程度上放松了时间上的相关性要求。持久订阅允许消费者消费它未处于激活状态时发送的消息。

- JMS编程模型

  - ConnectionFactory:连接工厂，用来创建连接对象，以连接到JMS的provider

  - JMS Connection：封装了客户与JMS提供者之间的一个虚拟的连接

  - JMS Session:是生产者和消费者的一个单线程上下文

    ​	会话优化创建生产者，消费者和消息。会话提供了一个事务性的上下文，在这个上下文中，一种发送和接收被组合到了一个原子操作中。

  - Destination：消息发送到的目的地 （Topic 和 Queue）

  - Acknowledge：签收

  - Transcation:事务   批量发送消息

  - JMS client：用来收发消息的Java应用

  -  消息的生产者

    消息生产者由Session创建，并用于将消息发送到Destination。同样，消息生产者分两种类型：QueueSender和TopicPublisher。可以调用消息生产者的方法（send或publish方法）发送消息。

  - 消息消费者

    消息消费者由Session创建，用于接收被发送到Destination的消息。两种类型：QueueReceiver和TopicSubscriber。可分别通过session的createReceiver(Queue)或createSubscriber(Topic)来创建。当然，也可以session的creatDurableSubscriber方法来创建持久化的订阅者

  - MessageListener

    消息监听器。如果注册了消息监听器，一旦消息到达，将自动调用监听器的onMessage方法。EJB中的MDB（Message-Driven Bean）就是一种MessageListener。



#### JMS message

JMS消息结构

JMS消息由消息头,消息属性和消息体组成。（与HTTP请求的构成是类似的）

- 消息头：

  消息头一般包含消息的识别信息和路由信息。它的标准属性如下：

  ​

  - JMSDestination:由send方法指定

    ​	消息发哦送的目的地：Queue和Topic    session.createQueue()   session.createTopic

  - JMSDeliveryMode：由send方法指定

    传送模式，有两种：持久模式和非持久模式。一条持久性的消息应该被传送“一次仅仅一次”，这意味着如果JMS提供者如果出现故障，该信息不会丢失，它会在服务器恢复之后再次传递。（传送一次之后就会存在服务器中，类似于事务ACID中的Durability）。一条非持久化的信息最多发送一次，发送完一次之后就会失效。

  - JMSExpiration：由send方法指定

    ​	

  - JMSPriority:由send方法指定

  - JMSMessageID：由send方法指定

  - JMSTimestamp：由客户端指定

  - JMSCorrelationID:由客户端指定

  - JMSReplyTo：由客户端指定

  - JMSType:由客户端自动

  - JMSRedelivered:由JMS Provider指定

    ​