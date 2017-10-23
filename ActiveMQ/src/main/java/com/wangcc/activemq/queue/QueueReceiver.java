package com.wangcc.activemq.queue;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
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
			session = connection.createSession(Boolean.FALSE, Session.CLIENT_ACKNOWLEDGE);
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
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (JMSException e) {
					logger.error("Close JMS Connection  ERROR:", e);
				}
			}
		}
	}
}
