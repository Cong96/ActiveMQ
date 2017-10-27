package com.wangcc.activemq.cluster.topic;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
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

		final Session session = connection.createSession(Boolean.FALSE, Session.AUTO_ACKNOWLEDGE);
		Destination destination = session.createTopic("my-topic1");
		TopicSubscriber subsrciber = session.createDurableSubscriber((Topic) destination, "w1");
		// 设置好了之后start()
		connection.start();
		subsrciber.setMessageListener(new MessageListener() {

			public void onMessage(Message message) {
				// TODO Auto-generated method stub
				if (message instanceof TextMessage) {
					try {
						System.out.println("接收的消息：" + ((TextMessage) message).getText());

					} catch (JMSException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
		});
		// int i = 0;
		// while (i < 3) {
		// TextMessage textMessage = (TextMessage) subsrciber.receive();
		// System.out.println("接收的消息：" + textMessage.getText());
		//
		// i++;
		//
		// }
		//
		// session.commit();
		// session.close();
		// connection.close();
	}
}
