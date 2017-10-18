package com.wangcc.activemq.spring.service;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;

/**
 * @ClassName: ProducerService
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author wangcc
 * @date 2017年10月17日 下午6:24:03
 *       MessageListenerAdapter除了会自动的把一个普通Java类当做MessageListener来处理接收到的消息之外，其另外一个主要的功能是可以自动的发送返回消息。
 *       当我们用于处理接收到的消息的方法的返回值不为空的时候，Spring会自动将它封装为一个JMS
 *       Message，然后自动进行回复。那么这个时候这个回复消息将发送到哪里呢？这主要有两种方式可以指定。
 *       第一，可以通过发送的Message的setJMSReplyTo方法指定该消息对应的回复消息的目的地。这里我们把我们的生产者发送消息的代码做一下修改，在发送消息之前先指定该消息对应的回复目的地为一个叫responseQueue的队列目的地
 */
@Service
public class ProducerService {
	@Autowired
	private JmsTemplate jmsTemplate;
	// @Autowired
	// @Qualifier("responseQueue")
	//
	// private Destination responseDestination;

	public void sendMessage(Destination destination, final String message) {
		System.out.println("---------------生产者发送消息-----------------");
		System.out.println("---------------生产者发了一个消息：" + message);
		/*
		 * 
		 * A调用B 比如 A 让 B 做事，根据粒度不同，可以理解成 A 函数调用 B 函数，或者 A 类使用 B 类，或者 A 组件使用 B 组件等等。反正就是
		 * 当 B 做这件事情的时候，自身的需要的信息不够，而 A 又有。就需要 A 从外面传进来，或者 B 做着做着再向外面申请。对于 B
		 * 来说，一种被动得到信息，一种是主动去得到信息，有人给这两种方式术语，叫信息的 push，和信息的 pull。
		 * 
		 * 
		 * A 叫 B 做事。 A 调用 B，A 需要向 B 传参数
		 * 
		 * 
		 * 
		 * 在这里插一句唠唠回调函数callback 回调函数在框架中的运用非常的常见 虽然很常见，但是想真正理解回调函数却没有想象中的那么简单容易
		 * 
		 * 
		 * 在这里MessageCreator对象中的createMessage就是回调方法
		 * createMessage这个方法中的Session类型的参数需要JmsTemplate对象的send方法给出
		 * 而send方法的执行又需要MessageCreator对象的传入
		 * 
		 * 对这个场景来说，本来是MessageCreator用调用jmsTemplate的send方法， A调用B
		 * 但是现在send方法是不完整的，需要传入MessageCreator对象才能够执行
		 * 
		 * A调用B中的send方法，send方法得到A所需要的Session参数，将参数给到B，然后B执行回调函数createMessage 然后完成流程
		 * 
		 */
		jmsTemplate.send(destination, new MessageCreator() {
			public Message createMessage(Session session) throws JMSException {

				TextMessage textMessage = session.createTextMessage(message);
				return textMessage;
			}
		});
	}
}
