package com.wangcc.activemq.cluster.queue;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

/**
 * @ClassName: QueueReceiver
 * @Description:
 * 
 * 				QUEUE默认持久化，默认持久化方式kahadb,所以在发送端发送消息的时候，消费者不要求一定在线
 *               即使当ActiveMQ宕机了之后，重启AcitveMQ后，消费者还是能取到之前生产者发送的消息
 * 
 * 
 * 
 * @author wangcc
 * @date 2017年10月26日 下午6:58:15
 * 
 */
public class QueueReceiver {
	public static void main(String[] args) throws JMSException {
		// ConnectionFactory connectionFactory = new
		// ActiveMQConnectionFactory("tcp://10.18.3.66:61617");
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("failover:(tcp://10.18.3.66:61617)");
		Connection connection = connectionFactory.createConnection();
		connection.start();
		// 当有事务时，是由session.commit事务提交与否来确定是否消费者确认签收，我们测试下，我们不commit,我们可以无数次接受到该消息

		Session session = connection.createSession(Boolean.TRUE, Session.CLIENT_ACKNOWLEDGE);
		Destination destination = session.createQueue("my-queue");
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
