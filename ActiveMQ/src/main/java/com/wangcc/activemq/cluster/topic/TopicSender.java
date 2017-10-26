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
		connection.start();
		/*
		 * 当没有事务的时候，是由签收模式来决定是否签收的
		 * 
		 */
		Session session = connection.createSession(Boolean.TRUE, Session.AUTO_ACKNOWLEDGE);
		Destination destination = session.createTopic("my-topic");
		MessageProducer producer = session.createProducer(destination);
		for (int i = 0; i < 3; i++) {
			TextMessage textMessage = session.createTextMessage();
			textMessage.setText("KOBEAAA" + i);

			producer.send(textMessage);
		}
		session.commit();
		session.close();
		connection.close();
	}
}
