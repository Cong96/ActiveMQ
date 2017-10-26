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
		connection.start();
		// 当有事务时，是由session.commit事务提交与否来确定是否消费者确认签收，我们测试下，我们不commit,我们可以无数次接受到该消息

		Session session = connection.createSession(Boolean.TRUE, Session.AUTO_ACKNOWLEDGE);
		Destination destination = session.createTopic("my-topic");
		TopicSubscriber subsrciber = session.createDurableSubscriber((Topic) destination, "");
		subsrciber.receive();
		// MessageConsumer messageConsumer = session.createConsumer(destination);
		int i = 0;
		while (i < 3) {
			// TextMessage textMessage = (TextMessage) messageConsumer.receive();
			TextMessage textMessage = (TextMessage) subsrciber.receive();
			System.out.println("接收的消息：" + textMessage.getText());

			i++;

		}
		// session.commit();
		session.close();
		connection.close();
	}
}
