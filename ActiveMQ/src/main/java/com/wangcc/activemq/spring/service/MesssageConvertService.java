package com.wangcc.activemq.spring.service;

import java.io.Serializable;

import javax.jms.Destination;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
public class MesssageConvertService {
	@Autowired
	private JmsTemplate jmsTemplate;

	/*
	 * 
	 * 
	 * 使用convertAndSend方法代替直接在程序中手动创建MessageCreate()对象
	 * 这样JmsTemplate就会在其内部调用预定的MessageConverter对我们的消息对象进行转换，然后再进行发送。
	 * 当然了，实际上还是创建了MessageConverter对象进行回调的，只不过这一步Spring给我们封装起来了，这个一开源码就能知道
	 */
	public void sendMessage(Destination destination, final Serializable obj) {
		jmsTemplate.convertAndSend(destination, obj);
	}
}
