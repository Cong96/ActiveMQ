ActiveMQ入门实例

date:2017年10月20日09:24:14



---



今天来看看怎么实现应用ActiveMQ的客户端。

首先我们编写一个发送端的程序。

```java
package com.wangcc.activemq.queue;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wangcc.activemq.Constants;

public class QueueProvider {
	private static Logger logger = LoggerFactory.getLogger(QueueProvider.class);

	public static void main(String[] args) {
		ConnectionFactory connectionFactory = null;
		Connection connection = null;
		Session session = null;
		Destination destination = null;
		MessageProducer messageProducer = null;
		try {
				// 1.创建连接工厂，需要给出user/pasword/url 与数据库连接相似

			connectionFactory = new ActiveMQConnectionFactory(Constants.ACTIVEMQ_USER, Constants.ACTIVEMQ_PSW,
					Constants.ACTIVEMQ_URL);
          //Connection代表了应用程序和消息服务器之间的通信链路，获得了连接工厂之后就可以创建Connection,Connection默认是关闭的
			connection = connectionFactory.createConnection();
			connection.start();
         /* 3.创建Session 前面两步都是为了创建Session（上下文环境对象）
         	 在通过Connection创建Session的时候，需要设置2个参数，一个是否支持事务，另一个是签收的模式。
          
          */
			// 开启事务
			session = connection.createSession(Boolean.TRUE, Session.AUTO_ACKNOWLEDGE);
	// 4.通过Sesion创建Destination对象，当PP模式下是队列，在pub/sub模式下是主题（TOPIC）

			destination = session.createQueue("testQueue");
        	// 5.创建MessageProducer对象 通过Session创建发送消息的生产者,其实一般我们不会在创建生产者的时候就带上目的地，而是在send的时候指定目的地
			messageProducer = session.createProducer(destination);
			int i = 0;
			while (i < 3) {
              
              //定义JMS规范的消息类型，
				TextMessage textMessage = session.createTextMessage();
				textMessage.setText("KOBE" + i);
              /*
              * send
		 * 在上面的code当中，我们创建生产者的时候，指定了Destination，设置了持久化方式，实际上这些都可以不必指定的，而是到send的时候指定。
		 * 而且在实际业务开发中，往往根据各种判断，来决定将这条消息发往哪个Queue，
		 * 因此往往不会在MessageProducer创建的时候指定Destination。
		 * send方法有好几个重载方法，我们看看那些方法的参数
		 * TTL，消息的存活时间，一句话：生产者生产了消息，如果消费者不来消费，那么这条消息保持多久的有效期
		 * 
		 * priority，消息优先级，0-9。0-4是普通消息，5-9是加急消息，消息默认级别是4。注意，消息优先级只是一个理论上的概念，
		 * 并不能绝对保证优先级高的消息一定被消费者优先消费！也就是说ActiveMQ并不能保证消费的顺序性！
		 * 
		 * deliveryMode，如果不指定，默认是持久化的消息。如果可以容忍消息的丢失，那么采用非持久化的方式，将会改善性能、减少存储的开销。
              
              */
				messageProducer.send(textMessage);
				i++;

			}
			// 提交事务，批量发送
			session.commit();
			logger.info("Send SUCCESS!");
		} catch (Exception e) {
			logger.error("JMS QueueProvider ERROR:", e);
		} finally {
			if (connection != null) {
				try {
                  //释放连接，只有关闭连接，ActiveMQ才会释放资源 关闭连接，会联级关闭session等资源，这一点对于我们来说是透明
					connection.close();
				} catch (JMSException e) {
					logger.error("Close JMS Connection  ERROR:", e);
				}
			}
		}
	}
}

```

我们这里创建的是一个队列，队列是支持异步发送接收的，我们可以直接先执行这个发送程序。（注意,这时发送端还没有编写）

然后我们编写下接收端

```java
package com.wangcc.activemq.queue;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wangcc.activemq.Constants;

public class QueueReceiver {
	private static Logger logger = LoggerFactory.getLogger(QueueReceiver.class);

	public static void main(String[] args) {
		ConnectionFactory connectionFacotry = null;
		Connection connection = null;
		Session session = null;
		Destination destination = null;
		MessageConsumer messageConsumer = null;
		try {
			connectionFacotry = new ActiveMQConnectionFactory(Constants.ACTIVEMQ_USER, Constants.ACTIVEMQ_PSW,
					Constants.ACTIVEMQ_URL);
			connection = connectionFacotry.createConnection();
			connection.start();
			session = connection.createSession(Boolean.FALSE, Session.AUTO_ACKNOWLEDGE);
			destination = session.createQueue("test1Queue");
			messageConsumer = session.createConsumer(destination);
          	int i = 0;
			while (true) {
				TextMessage textMessage = (TextMessage) messageConsumer.receive();
				if (textMessage != null) { // 接收到消息
					System.out.println("接收的消息：" + textMessage.getText());
					logger.info("Received message: {} ", textMessage.getText());
					if (i == 1) {
						textMessage.acknowledge();
					}
					i++;

				} else {
					break;
				}
			}
			// messageConsumer.setMessageListener(new MessageListener() {
			//
			// public void onMessage(Message message) {
			// logger.info("waiting for message ......");
			//
			// try {
			// TextMessage textMessage = (TextMessage) message;
			// logger.info("Received message: {} ", textMessage.getText());
			// System.out.println("Received message:{} " + textMessage.getText());
			// } catch (Exception e) {
			// logger.error("Listner Error:{}", e);
			// }
			// }
			// });
		} catch (Exception e) {
			logger.error("", e);
		}
}
```



```
[2017-10-20 10:24:35:827] [org.apache.activemq.transport.WireFormatNegotiator] [82] DEBUG [Sending: WireFormatInfo { version=9, properties={TcpNoDelayEnabled=true, SizePrefixDisabled=false, CacheSize=1024, StackTraceEnabled=true, CacheEnabled=true, TightEncodingEnabled=true, MaxFrameSize=9223372036854775807, MaxInactivityDuration=30000, MaxInactivityDurationInitalDelay=10000}, magic=[A,c,t,i,v,e,M,Q]}]
[2017-10-20 10:24:35:837] [org.apache.activemq.transport.InactivityMonitor] [92] DEBUG [Using min of local: WireFormatInfo { version=9, properties={TcpNoDelayEnabled=true, SizePrefixDisabled=false, CacheSize=1024, StackTraceEnabled=true, CacheEnabled=true, TightEncodingEnabled=true, MaxFrameSize=9223372036854775807, MaxInactivityDuration=30000, MaxInactivityDurationInitalDelay=10000}, magic=[A,c,t,i,v,e,M,Q]} and remote: WireFormatInfo { version=12, properties={TcpNoDelayEnabled=true, SizePrefixDisabled=false, CacheSize=1024, ProviderName=ActiveMQ, StackTraceEnabled=true, PlatformDetails=JVM: 1.8.0_144, 25.144-b01, Oracle Corporation, OS: Windows 7, 6.1, amd64, CacheEnabled=true, TightEncodingEnabled=true, MaxFrameSize=104857600, MaxInactivityDuration=30000, MaxInactivityDurationInitalDelay=10000, ProviderVersion=5.15.0}, magic=[A,c,t,i,v,e,M,Q]}]
[2017-10-20 10:24:35:840] [org.apache.activemq.transport.WireFormatNegotiator] [118] DEBUG [Received WireFormat: WireFormatInfo { version=12, properties={TcpNoDelayEnabled=true, SizePrefixDisabled=false, CacheSize=1024, ProviderName=ActiveMQ, StackTraceEnabled=true, PlatformDetails=JVM: 1.8.0_144, 25.144-b01, Oracle Corporation, OS: Windows 7, 6.1, amd64, CacheEnabled=true, TightEncodingEnabled=true, MaxFrameSize=104857600, MaxInactivityDuration=30000, MaxInactivityDurationInitalDelay=10000, ProviderVersion=5.15.0}, magic=[A,c,t,i,v,e,M,Q]}]
[2017-10-20 10:24:35:840] [org.apache.activemq.transport.WireFormatNegotiator] [125] DEBUG [tcp:///127.0.0.1:61616@51266 before negotiation: OpenWireFormat{version=9, cacheEnabled=false, stackTraceEnabled=false, tightEncodingEnabled=false, sizePrefixDisabled=false, maxFrameSize=9223372036854775807}]
[2017-10-20 10:24:35:840] [org.apache.activemq.transport.WireFormatNegotiator] [140] DEBUG [tcp:///127.0.0.1:61616@51266 after negotiation: OpenWireFormat{version=9, cacheEnabled=true, stackTraceEnabled=true, tightEncodingEnabled=true, sizePrefixDisabled=false, maxFrameSize=104857600}]
[2017-10-20 10:24:35:912] [org.apache.activemq.thread.TaskRunnerFactory] [91] DEBUG [Initialized TaskRunnerFactory[ActiveMQ Session Task] using ExecutorService: java.util.concurrent.ThreadPoolExecutor@6108b2d7[Running, pool size = 0, active threads = 0, queued tasks = 0, completed tasks = 0]]
[2017-10-20 10:24:35:924] [com.wangcc.activemq.queue.QueueReceiver] [38] INFO [waiting for message  ......]
[2017-10-20 10:24:35:924] [com.wangcc.activemq.queue.QueueReceiver] [42] INFO [Received message: KOBEeee0 ]
Received message:{} KOBEeee0
[2017-10-20 10:24:35:926] [com.wangcc.activemq.queue.QueueReceiver] [38] INFO [waiting for message  ......]
[2017-10-20 10:24:35:927] [com.wangcc.activemq.queue.QueueReceiver] [42] INFO [Received message: KOBEeee1 ]
Received message:{} KOBEeee1
[2017-10-20 10:24:35:927] [com.wangcc.activemq.queue.QueueReceiver] [38] INFO [waiting for message  ......]
[2017-10-20 10:24:35:927] [com.wangcc.activemq.queue.QueueReceiver] [42] INFO [Received message: KOBEeee2 ]
Received message:{} KOBEeee2
```



在这里要说一下签收模式的问题了，我们可以看到在接收端程序中，我们在创建Session时是设置为没有事务的。

JMS消息只有在被确认之后，才认为已经被消费了。消息的成功消费通常包含三个阶段：

客户接收消息，客户处理消息和消息被确认。

在事务性会话中，当一个事务被提交时，确认自动发生。

而在非事务性的会话中，消息何时确认取决于创建会话时的签收模式。该参数有三个可选值

- Session.AUTO_ACKNOWLEDGE,当客户成功的从receive方法返回或者从MessageListener.onMessage中返回的时候,会话自动确认客户收到的信息。
- Session.CLIENT_ACKNOWLEDGE,客户通过调用消息的acknowledge方法确认消息。需要注意的是，在这种模式中，确认是在会话层进行的，确认一个消息自动确认所有已被会话消费的消息。例如，如果一个消费者消费了10个消息，然后确认第五个信息，那么所有的10个消息都会被确认。
- Session.DUPS_OK_ACKNOWLEDGE，签不签收无所谓了，只要消费者能够容忍重复的消息接受，当然这样会降低Session的开销



在实际中，我们应该采用哪种签收模式呢？CLIENT_ACKNOWLEDGE，采用手动的方式较自动的方式可能更好些，因为接收到了消息，并不意味着成功的处理了消息，假设我们采用手动签收的方式，只有在消息成功处理的前提下才进行签收，那么只要消息处理失败，那么消息还有效，仍然会继续消费，直至成功处理！

看过这些签收模式这后，我们来利用程序测试一下。

我们再次运行上述的接收方的程序，我们会发现程序一直在运行，而且没有任何的输出，因为他一直在监听，但是很遗憾的是，队列上的消息都已经被消费而且确认了，所以她就没有可用来消费的消息了，只能一直运行着而没有输出。

然后我们将代码改一下：将Session.AUTO_ACKNOWLEDGE改成Session.CLIENT_ACKNOWLEDGE,并且在i=1时acknowledge()我们会发现再次运行时只能收到Kobe2信息，因为0和1都被签收了。

同理，我们可以进行Topic主题的测试，对于主题，与Queue有一个明显的不同点，订阅一个主题的消费者只能消费自它订阅后的消息。也就是说生产者和消费者之间有时间上的相关性。你可以写个程序测试一下，你先写歌发送端程序，然后运行，再写接收端，你会发现一脸蒙蔽，程序一直在运行，但是就是没有能够接收到消息。对于订阅模式，对订阅者提出了特殊的要求，要想收到消息，必须先订阅，而且订阅进程必须一直处于运行状态！实际上，有时候消费者重启了下，那么这个消费者将丢失掉一些消息

但是ActiveMQ如此强大的消息服务器当然能够解决这一事件相关性的问题，JMS规定允许客户创建持久订阅，这在一定程度上放松了时间上的相关性要求。持久订阅允许消费者消费它未处于激活状态时发送的消息。所谓持久化订阅，打个比方，就是说跟MQ打声招呼，即便我不在，那么给我发送的消息暂存在MQ，等我来了，再给我发过来。说白了，持久化订阅，需要给MQ备个案（你是谁，想在哪个Topic上搞特殊化）！

那么在ActiveMQ中，我们在订阅消息的时候需要注册一个clientId给消息服务器，并且告诉消息服务器我们要对哪个Topic进行持久化订阅。

​        connection.setClientID();

MessageConsumer  consumer1 = session.createDurableSubscriber((Topic) destination,JmsEnv.getProperty("clientName"));

每一个持久化订阅者都应该有一个唯一的ID作为标示以及要在哪个Topic上进行持久化订阅，一旦这些信息告知MQ之后，那么以后不论持久化订阅者在不在线，那么他的消息会暂存在MQ，以后都会发给他！





