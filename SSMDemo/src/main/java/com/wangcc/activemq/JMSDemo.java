package com.wangcc.activemq;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

/**
 * @ClassName: JMSDemo
 * @Description: (这里用一句话描述这个类的作用)
 * @author wangcc
 * @date 2017年10月17日 上午10:11:25
 * 
 *       在通过Connection创建Session的时候，需要设置2个参数，一个是否支持事务，另一个是签收的模式。我们重点说一下签收模式：
 * 
 * 
 *       签收模式
 *       什么是签收？通俗点说，就是消费者接受到消息后，需要告诉消息服务器，我收到消息了。当消息服务器收到回执后，本条消息将失效。因此签收将对PTP模式产生很大影响。如果消费者收到消息后，并不签收，那么本条消息继续有效，很可能会被其他消费者消费掉！
 * 
 *       AUTO_ACKNOWLEDGE：表示在消费者receive消息的时候自动的签收
 * 
 *       CLIENT_ACKNOWLEDGE：表示消费者receive消息后必须手动的调用acknowledge()方法进行签收
 * 
 *       DUPS_OK_ACKNOWLEDGE：签不签收无所谓了，只要消费者能够容忍重复的消息接受，当然这样会降低Session的开销
 * 
 *       在实际中，我们应该采用哪种签收模式呢？CLIENT_ACKNOWLEDGE，采用手动的方式较自动的方式可能更好些，因为接收到了消息，并不意味着成功的处理了消息，假设我们采用手动签收的方式，只有在消息成功处理的前提下才进行签收，那么只要消息处理失败，那么消息还有效，仍然会继续消费，直至成功处理！
 * 
 * 
 * 
 */
public class JMSDemo {
	public static void main(String[] args) throws Exception {
		// 1.创建连接工厂，需要给出user/pasword/url 与数据库连接相似
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("wangcc", "123",
				ActiveMQConnectionFactory.DEFAULT_BROKER_BIND_URL);
		// 创建Connection连接，连接默认是关闭的
		/*
		 * Connection代表了应用程序和消息服务器之间的通信链路，获得了连接工厂之后就可以创建Connection
		 * 
		 * 
		 */
		Connection connection = connectionFactory.createConnection();
		connection.start();
		// 3.创建Session 前面两步都是为了创建Session（上下文环境对象）
		Session session = connection.createSession(Boolean.FALSE, Session.AUTO_ACKNOWLEDGE);
		// 4.通过Sesion创建Destination对象，当PP模式下是队列，在pub/sub模式下是主题（TOPIC）
		Destination destination = session.createQueue("queue1");
		// 5.创建MessageProducer对象 通过Session创建发送消息的生产者
		MessageProducer messageProducer = session.createProducer(destination);
		// 6设置持久化或非持久化,如果非持久化，意味着MQ重启后会导致消息丢失
		messageProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
		// 7 定义JMS规范的消息类型，
		TextMessage textMesssage = session.createTextMessage();
		textMesssage.setText("Hello,ActiveMQ");
		/*
		 * send
		 * 在上面的code当中，我们创建生产者的时候，指定了Destination，设置了持久化方式，实际上这些都可以不必指定的，而是到send的时候指定。
		 * 而且在实际业务开发中，往往根据各种判断，来决定将这条消息发往哪个Queue，
		 * 因此往往不会在MessageProducer创建的时候指定Destination。
		 * 
		 * TTL，消息的存活时间，一句话：生产者生产了消息，如果消费者不来消费，那么这条消息保持多久的有效期
		 * 
		 * priority，消息优先级，0-9。0-4是普通消息，5-9是加急消息，消息默认级别是4。注意，消息优先级只是一个理论上的概念，
		 * 并不能绝对保证优先级高的消息一定被消费者优先消费！也就是说ActiveMQ并不能保证消费的顺序性！
		 * 
		 * deliveryMode，如果不指定，默认是持久化的消息。如果可以容忍消息的丢失，那么采用非持久化的方式，将会改善性能、减少存储的开销。
		 * 
		 * 
		 */
		messageProducer.send(textMesssage);
		// 8.释放连接，只有关闭连接，ActiveMQ才会释放资源 关闭连接，会联级关闭session等资源，这一点对于我们来说是透明
		if (connection != null) {
			connection.close();
		}
	}
}