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
			destination = session.createQueue("firstQueue");
			messageConsumer = session.createConsumer(destination);
			messageConsumer.receive(50000);
			messageConsumer.setMessageListener(new MessageListener() {

				public void onMessage(Message message) {
					logger.info("waiting for message  ......");

					try {
						TextMessage textMessage = (TextMessage) message;
						logger.info("Received message: {} ", textMessage.getText());
						System.out.println("Received message:{} " + textMessage.getText());
						Thread.sleep(1000);
					} catch (Exception e) {
						logger.error("Listner Error:{}", e);
					}
				}
			});
		} catch (Exception e) {
			logger.error("", e);
		}
	}
}
