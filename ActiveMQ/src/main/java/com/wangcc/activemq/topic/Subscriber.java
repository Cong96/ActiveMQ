package com.wangcc.activemq.topic;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wangcc.activemq.Constants;

/**
 * @ClassName: Subscriber
 * @Description: 对于订阅模式，对订阅者提出了特殊的要求，要想收到消息，必须先订阅，而且订阅进程必须一直处于运行状态！实际上，有时候消费者重启了下，那么这个消费者将丢失掉一些消息，那么能否避免这样的情况呢？ActiveMQ已经替我们想好了，就是持久化订阅！
 * @author wangcc
 * @date 2017年10月18日 上午9:59:29
 * 
 */
public class Subscriber {
	private static Logger logger = LoggerFactory.getLogger(Subscriber.class);

	public static void main(String[] args) {
		// 连接工厂
		ConnectionFactory factory;
		// 连接实例
		Connection connection = null;
		// 收发的线程实例
		Session session;
		// 消息发送目标地址
		Destination destination;
		try {
			// 实例化连接工厂
			factory = new ActiveMQConnectionFactory(Constants.ACTIVEMQ_USER, Constants.ACTIVEMQ_PSW,
					Constants.ACTIVEMQ_URL);
			// 获取连接实例
			connection = factory.createConnection();
			// 启动连接
			connection.start();
			// 创建接收或发送的线程实例
			session = connection.createSession(Boolean.FALSE, Session.AUTO_ACKNOWLEDGE);
			// 创建队列（返回一个消息目的地）
			destination = session.createTopic("parryTopic");
			// 创建消息订阅者
			MessageConsumer consumer = session.createConsumer(destination);
			// 消息发布者添加监听器
			consumer.setMessageListener(new MessageListener() {

				public void onMessage(Message message) {
					try {
						logger.info("订阅者01接收到消息：{}", ((TextMessage) message).getText());
						System.out.println("订阅者01接收到消息：" + ((TextMessage) message).getText());
					} catch (JMSException e) {
						e.printStackTrace();
					}

				}
			});
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
}
