title:ActiveMQ实践，TOPIC消息使用

date：2017年10月27日11:21:04

---

编写发送TOPIC类型消息的sender

```java
package com.wangcc.activemq.cluster.topic;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

public class TopicSender {
	public static void main(String[] args) throws Exception {
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://10.18.3.66:61616");
		Connection connection = connectionFactory.createConnection();
		// 默认是关闭，所以这里需要开启
		/*
		 * 当没有事务的时候，是由签收模式来决定是否签收的
		 * 
		 */
		connection.start();
		Session session = connection.createSession(Boolean.TRUE, Session.AUTO_ACKNOWLEDGE);
		Destination destination = session.createTopic("my-topic1");
		MessageProducer producer = session.createProducer(destination);

		for (int i = 0; i < 3; i++) {
			TextMessage textMessage = session.createTextMessage();
			textMessage.setText("KOBEEEE" + i);

			producer.send(textMessage);
		}
		session.commit();
		session.close();
		connection.close();
	}
}
```

先执行下发送端，然后再编写接收端。

```
package com.wangcc.activemq.cluster.topic;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

public class TopicReceiver {
	public static void main(String[] args) throws JMSException {
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://10.18.3.66:61616");
		Connection connection = connectionFactory.createConnection();
		connection.start();
		// 当有事务时，是由session.commit事务提交与否来确定是否消费者确认签收，我们测试下，我们不commit,我们可以无数次接受到该消息

		Session session = connection.createSession(Boolean.TRUE, Session.AUTO_ACKNOWLEDGE);
		Destination destination = session.createTopic("my-topic1");
		MessageConsumer messageConsumer = session.createConsumer(destination);
		int i = 0;
		while (i < 3) {
			TextMessage textMessage = (TextMessage) messageConsumer.receive();
			System.out.println("接收的消息：" + textMessage.getText());

			i++;

		}
		session.commit();
		session.close();
		connection.close();
	}
}
```

执行接收端，我们发现，并没有接收到任何的消息，这一点和QUEUE的消息发送接收不同。TOPIC消息是默认非持久化的。

也就说当发送端发送消息的时候呢，一定要要求接收端在线，如果发送消息的时候接收端不在线，之后接收端再想接收到消息是不可能的。即生产者和消费者之间是存在相关性的。

如果此时想要让接收端能接受到消息，我们需要先启动接收端，然后让接收端一直运行，然后执行发送端，这时才能让接收端接收到消息。你们是否会觉得TOPIC消息传输中生产者和消费者之间的时间相关性对用户特别不友好呢，为了让用户有更好的体验，减少生产者和消费者之间的时间相关性，pub/sub传输方式（TOPIC消息）提供了持久化订阅来优化这一情况，使得只要在接收端注册过之后，即使接收端offLine,只要重启接收端，接收端依然能够收到订阅过的消息。我们编写一个持久化订阅程序。

```java
package com.wangcc.activemq.cluster.topic;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;

import org.apache.activemq.ActiveMQConnectionFactory;

public class DurableReceiver {
	public static void main(String[] args) throws JMSException {
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://10.18.3.66:61616");
		Connection connection = connectionFactory.createConnection();
		connection.setClientID("wt1");
		// 当有事务时，是由session.commit事务提交与否来确定是否消费者确认签收，我们测试下，我们不commit,我们可以无数次接受到该消息

		Session session = connection.createSession(Boolean.TRUE, Session.CLIENT_ACKNOWLEDGE);
		Destination destination = session.createTopic("my-topic1");
		TopicSubscriber subsrciber = session.createDurableSubscriber((Topic) destination, "w1");
		// 设置好了之后start()
		connection.start();

		int i = 0;
		while (i < 3) {
			// TextMessage textMessage = (TextMessage) messageConsumer.receive();
			TextMessage textMessage = (TextMessage) subsrciber.receive();
			System.out.println("接收的消息：" + textMessage.getText());
			// if (i == 1) {
			// textMessage.acknowledge();
			// }
			i++;

		}
		session.commit();
		session.close();
		connection.close();
	}
}
```



对于持久化订阅，我们需要给MQ注册一个ClientID,向MQ注册一个唯一标识，标识带有这个ClientID的接收端连接订阅了相应的TOPIC（这里为my-topic1），也就是这样就告诉了MQ，如果再有my-topic1的消息发送到MQ，MQ你可以一定要帮我存起来呀，等我来了，我就会来取的，我可提前给你打招呼了。

而注册ClientID这个行为是通过connection.setClientID("wt1"); 注意ClientID必须是唯一标识，代表了这个接收端在MQ中的一个省份证。

而且我们创建消息接收者的步骤和以往不一样，我们使用 session.createDurableSubscriber创建一个持久化订阅者TopicSubscriber，不再是使用session.createConsumer创建消费者来接收消息。

我们注意创建持久化订阅者时需要填写两个参数，第一个参数和创建消费者一样，是填入消息目的地destination信息，而第二个参数就是给这个订阅者命名一个名字，这个名字可以随意命名，建议和ClientID统一。

通过持久化订阅后，我们接收TOPIC类型的消息时也可以在注册之后就随时offline了，反正MQ给我留着，而且由于TOPIC是一对多的，我们也不用担心消息被其他消费者消费确认后就没有了。