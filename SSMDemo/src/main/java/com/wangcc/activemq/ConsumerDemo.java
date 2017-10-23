package com.wangcc.activemq;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;

public class ConsumerDemo {
	public static void main(String[] args) throws JMSException {
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("wangcc", "123",
				ActiveMQConnectionFactory.DEFAULT_BROKER_BIND_URL);
		Connection connection = connectionFactory.createConnection();
		connection.start();
		Session session = connection.createSession(Boolean.FALSE, Session.AUTO_ACKNOWLEDGE);
		Destination destination = session.createQueue("queue1");
		MessageConsumer messageConsumer = session.createConsumer(destination);
		session.createDurableSubscriber(arg0, arg1)
		// while (true) {
		// TextMessage textMessage = (TextMessage) messageConsumer.receive(100000);//
		// 设置延时为100s
		// if (textMessage != null) { // 接收到消息
		// System.out.println("接收的消息：" + textMessage.getText());
		// } else {
		// break;
		// }
		// }
		/*
		 * Message Listener 消息的异步接受是指当消息到达时，ActiveMQ主动通知客户端。
		 * 可以通过注册一个实现了MessageListener接口的对象到MessageConsumer。MessageListener只有一个必须要实现的方法，
		 * 即onMessage。在发往Destination的消息时，会调用该方法。
		 * 
		 * 这种异步接受“貌似”是ActiveMQ主动的推送消息给消费者，其本质还是消费者轮询消息服务器导致的，只不过这个过程被封装了！
		 * 
		 * 
		 */
		messageConsumer.setMessageListener(new MyFirstListenerForMQ());
		if (connection != null) {
			connection.close();
		}
	}
}
